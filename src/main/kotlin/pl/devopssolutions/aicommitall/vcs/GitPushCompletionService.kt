/*
 * Copyright 2026 DevOps Solutions Kamil Kiewisz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.devopssolutions.aicommitall.vcs

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import git4idea.push.GitPushListener
import git4idea.push.GitPushRepoResult
import git4idea.repo.GitRepository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
internal class GitPushCompletionService @JvmOverloads constructor(
    project: Project,
    timeoutScheduler: GitPushCompletionTimeoutScheduler = IntellijGitPushCompletionTimeoutScheduler,
) : Disposable {
    private val tracker = GitPushCompletionTracker(timeoutScheduler)

    init {
        project.messageBus.connect(this)
            .subscribe(
                GitPushListener.TOPIC,
                GitPushCompletionListener { repository, pushResult ->
                    tracker.completeRepositoryPush(repository, pushResult)
                },
            )
    }

    fun awaitCompletion(
        repositories: Collection<GitRepository>,
    ): CompletableFuture<GitPushCompletionResult> = tracker.awaitCompletion(
        repositories = repositories,
        timeoutMillis = DEFAULT_PUSH_COMPLETION_TIMEOUT_MILLIS,
    )

    fun addCompletionListener(
        parentDisposable: Disposable,
        listener: () -> Unit,
    ) = tracker.addCompletionListener(parentDisposable, listener)

    override fun dispose() = tracker.dispose()

    companion object {
        fun getInstance(project: Project): GitPushCompletionService = project.service()
    }
}

internal class GitPushCompletionTracker(
    private val timeoutScheduler: GitPushCompletionTimeoutScheduler,
) {
    private val lock = Any()
    private val waiters = mutableListOf<GitPushCompletionWaiter>()
    private val listeners = mutableListOf<GitPushCompletionListenerRegistration>()

    fun awaitCompletion(
        repositories: Collection<GitRepository>,
        timeoutMillis: Long,
    ): CompletableFuture<GitPushCompletionResult> {
        val remainingRepositories = repositories.toSet()
        if (remainingRepositories.isEmpty()) {
            return CompletableFuture.completedFuture(GitPushCompletionResult.Success(emptyMap()))
        }

        val waiter = GitPushCompletionWaiter(
            remainingRepositories = remainingRepositories.toMutableSet(),
            completion = CompletableFuture(),
        )
        synchronized(lock) {
            waiters += waiter
        }

        val timeoutHandle = timeoutScheduler.schedule(timeoutMillis) {
            completeTimedOut(waiter)
        }
        synchronized(lock) {
            waiter.timeoutHandle = timeoutHandle
            if (waiter.completion.isDone) {
                timeoutHandle.cancel()
            }
        }
        waiter.completion.whenComplete { _, _ -> removeWaiter(waiter)?.cancel() }
        return waiter.completion
    }

    fun addCompletionListener(
        parentDisposable: Disposable,
        listener: () -> Unit,
    ) {
        val registration = GitPushCompletionListenerRegistration(listener)
        synchronized(lock) {
            listeners += registration
        }
        Disposer.register(parentDisposable) {
            synchronized(lock) {
                listeners -= registration
            }
        }
    }

    fun completeRepositoryPush(
        repository: GitRepository,
        pushResult: GitPushRepoResult,
    ) {
        val completedWaiters = mutableListOf<GitPushCompletionWaiterCompletion>()
        val completionListeners = synchronized(lock) {
            val iterator = waiters.iterator()
            while (iterator.hasNext()) {
                val waiter = iterator.next()
                if (!waiter.remainingRepositories.remove(repository)) {
                    continue
                }
                waiter.completedResults[repository] = pushResult
                if (waiter.remainingRepositories.isEmpty()) {
                    iterator.remove()
                    completedWaiters += GitPushCompletionWaiterCompletion(
                        completion = waiter.completion,
                        result = GitPushCompletionResult.completed(waiter.completedResults),
                        timeoutHandle = waiter.timeoutHandle,
                    )
                }
            }
            listeners.map { registration -> registration.listener }
        }

        completedWaiters.forEach { waiterCompletion -> waiterCompletion.complete() }
        completionListeners.forEach { listener -> listener() }
    }

    private fun completeTimedOut(waiter: GitPushCompletionWaiter) {
        val completion = synchronized(lock) {
            if (waiters.remove(waiter)) {
                GitPushCompletionWaiterCompletion(
                    completion = waiter.completion,
                    result = GitPushCompletionResult.TimedOut(
                        completedResults = waiter.completedResults.toMap(),
                        pendingRepositories = waiter.remainingRepositories.toSet(),
                    ),
                    timeoutHandle = null,
                )
            } else {
                null
            }
        }
        completion?.complete()
    }

    private fun removeWaiter(waiter: GitPushCompletionWaiter): GitPushCompletionTimeoutHandle? = synchronized(lock) {
        if (waiters.remove(waiter)) waiter.timeoutHandle else null
    }

    fun dispose() {
        val pendingWaiters = synchronized(lock) {
            listeners.clear()
            waiters.toList().also {
                waiters.clear()
            }
        }
        pendingWaiters.forEach { waiter ->
            waiter.timeoutHandle?.cancel()
            waiter.completion.complete(
                GitPushCompletionResult.Cancelled(
                    completedResults = waiter.completedResults.toMap(),
                    pendingRepositories = waiter.remainingRepositories.toSet(),
                ),
            )
        }
    }
}

internal sealed interface GitPushCompletionResult {
    val completedResults: Map<GitRepository, GitPushRepoResult>

    data class Success(
        override val completedResults: Map<GitRepository, GitPushRepoResult>,
    ) : GitPushCompletionResult

    data class Cancelled(
        override val completedResults: Map<GitRepository, GitPushRepoResult>,
        val pendingRepositories: Set<GitRepository> = emptySet(),
    ) : GitPushCompletionResult

    data class Failed(
        override val completedResults: Map<GitRepository, GitPushRepoResult>,
    ) : GitPushCompletionResult

    data class TimedOut(
        override val completedResults: Map<GitRepository, GitPushRepoResult>,
        val pendingRepositories: Set<GitRepository>,
    ) : GitPushCompletionResult

    companion object {
        fun completed(results: Map<GitRepository, GitPushRepoResult>): GitPushCompletionResult {
            val snapshot = results.toMap()
            return when {
                snapshot.values.any { result -> result.type.isFailureType } -> Failed(snapshot)

                snapshot.values.any { result -> result.type == GitPushRepoResult.Type.NOT_PUSHED } ->
                    Cancelled(snapshot)

                else -> Success(snapshot)
            }
        }
    }
}

private data class GitPushCompletionWaiter(
    val remainingRepositories: MutableSet<GitRepository>,
    val completedResults: MutableMap<GitRepository, GitPushRepoResult> = linkedMapOf(),
    val completion: CompletableFuture<GitPushCompletionResult>,
    var timeoutHandle: GitPushCompletionTimeoutHandle? = null,
)

private data class GitPushCompletionListenerRegistration(
    val listener: () -> Unit,
)

private data class GitPushCompletionWaiterCompletion(
    val completion: CompletableFuture<GitPushCompletionResult>,
    val result: GitPushCompletionResult,
    val timeoutHandle: GitPushCompletionTimeoutHandle?,
) {
    fun complete() {
        timeoutHandle?.cancel()
        completion.complete(result)
    }
}

internal fun interface GitPushCompletionTimeoutScheduler {
    fun schedule(
        timeoutMillis: Long,
        onTimeout: () -> Unit,
    ): GitPushCompletionTimeoutHandle
}

internal fun interface GitPushCompletionTimeoutHandle {
    fun cancel()
}

private object IntellijGitPushCompletionTimeoutScheduler : GitPushCompletionTimeoutScheduler {
    override fun schedule(
        timeoutMillis: Long,
        onTimeout: () -> Unit,
    ): GitPushCompletionTimeoutHandle {
        val scheduledFuture = JobScheduler.getScheduler()
            .schedule(onTimeout, timeoutMillis, TimeUnit.MILLISECONDS)
        return GitPushCompletionTimeoutHandle { scheduledFuture.cancel(false) }
    }
}

private val GitPushRepoResult.Type.isFailureType: Boolean
    get() = this !in successfulPushResultTypes && this != GitPushRepoResult.Type.NOT_PUSHED

private val successfulPushResultTypes = setOf(
    GitPushRepoResult.Type.SUCCESS,
    GitPushRepoResult.Type.NEW_BRANCH,
    GitPushRepoResult.Type.UP_TO_DATE,
    GitPushRepoResult.Type.FORCED,
)

private const val DEFAULT_PUSH_COMPLETION_TIMEOUT_MILLIS: Long = 30_000L
