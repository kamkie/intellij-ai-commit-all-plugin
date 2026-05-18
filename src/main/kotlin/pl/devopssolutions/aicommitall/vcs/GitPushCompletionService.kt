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

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import git4idea.push.GitPushListener
import git4idea.push.GitPushRepoResult
import git4idea.repo.GitRepository
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class GitPushCompletionService(private val project: Project) : Disposable {
    private val lock = Any()
    private val waiters = mutableListOf<GitPushCompletionWaiter>()
    private val listeners = mutableListOf<GitPushCompletionListenerRegistration>()

    init {
        project.messageBus.connect(this)
            .subscribe(
                GitPushListener.TOPIC,
                object : GitPushListener {
                    override fun onCompleted(
                        repository: GitRepository,
                        pushResult: GitPushRepoResult,
                    ) {
                        completeRepositoryPush(repository)
                    }
                },
            )
    }

    fun awaitCompletion(repositories: Collection<GitRepository>): CompletableFuture<Unit> {
        val remainingRepositories = repositories.toSet()
        if (remainingRepositories.isEmpty()) {
            return CompletableFuture.completedFuture(Unit)
        }

        val waiter = GitPushCompletionWaiter(
            remainingRepositories = remainingRepositories.toMutableSet(),
            completion = CompletableFuture(),
        )
        synchronized(lock) {
            waiters += waiter
        }
        waiter.completion.whenComplete { _, _ -> removeWaiter(waiter) }
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

    private fun completeRepositoryPush(repository: GitRepository) {
        val completedWaiters = mutableListOf<CompletableFuture<Unit>>()
        val completionListeners = synchronized(lock) {
            val iterator = waiters.iterator()
            while (iterator.hasNext()) {
                val waiter = iterator.next()
                waiter.remainingRepositories -= repository
                if (waiter.remainingRepositories.isEmpty()) {
                    iterator.remove()
                    completedWaiters += waiter.completion
                }
            }
            listeners.map { registration -> registration.listener }
        }

        completedWaiters.forEach { completion -> completion.complete(Unit) }
        completionListeners.forEach { listener -> listener() }
    }

    private fun removeWaiter(waiter: GitPushCompletionWaiter) {
        synchronized(lock) {
            waiters -= waiter
        }
    }

    override fun dispose() {
        val pendingWaiters = synchronized(lock) {
            listeners.clear()
            waiters.toList().also {
                waiters.clear()
            }
        }
        pendingWaiters.forEach { waiter -> waiter.completion.cancel(false) }
    }

    companion object {
        fun getInstance(project: Project): GitPushCompletionService = project.service()
    }
}

private data class GitPushCompletionWaiter(
    val remainingRepositories: MutableSet<GitRepository>,
    val completion: CompletableFuture<Unit>,
)

private data class GitPushCompletionListenerRegistration(
    val listener: () -> Unit,
)
