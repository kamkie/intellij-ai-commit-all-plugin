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
import com.intellij.dvcs.push.PushSpec
import com.intellij.dvcs.push.PushSupport
import com.intellij.dvcs.repo.Repository
import com.intellij.ide.ActivityTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import git4idea.GitVcs
import git4idea.push.GitPushSource
import git4idea.push.GitPushSupport
import git4idea.push.GitPushTarget
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import git4idea.repo.GitRepositoryManager

@Service(Service.Level.PROJECT)
internal class GitOutgoingCommitsService @JvmOverloads constructor(
    project: Project,
    environment: GitOutgoingCommitsEnvironment = IntellijGitOutgoingCommitsEnvironment(project),
    scheduler: GitOutgoingCommitsRefreshScheduler = IntellijGitOutgoingCommitsRefreshScheduler,
    actionRefresh: GitOutgoingCommitsActionRefresh = IntellijGitOutgoingCommitsActionRefresh,
) : Disposable {
    private val outgoingCommitsStatus = GitOutgoingCommitsStatus(
        loader = environment::hasOutgoingCommitsToPush,
        scheduler = scheduler,
        actionRefresh = actionRefresh,
    )

    init {
        environment.subscribeToRepositoryChanges(this) {
            refreshHasOutgoingCommitsToPush()
        }
        environment.subscribeToPushCompletion(this) {
            refreshHasOutgoingCommitsToPush()
        }
    }

    fun hasOutgoingCommitsToPush(): Boolean = outgoingCommitsStatus.hasOutgoingCommitsToPush()

    fun cachedHasOutgoingCommitsToPush(): Boolean = outgoingCommitsStatus.cachedHasOutgoingCommitsToPush()

    private fun refreshHasOutgoingCommitsToPush() {
        outgoingCommitsStatus.requestRefresh()
    }

    override fun dispose() = Unit

    companion object {
        fun getInstance(project: Project): GitOutgoingCommitsService = project.service()
    }
}

internal interface GitOutgoingCommitsEnvironment {
    fun subscribeToRepositoryChanges(parentDisposable: Disposable, refresh: () -> Unit)

    fun subscribeToPushCompletion(parentDisposable: Disposable, refresh: () -> Unit)

    fun hasOutgoingCommitsToPush(): Boolean
}

private class IntellijGitOutgoingCommitsEnvironment(private val project: Project) : GitOutgoingCommitsEnvironment {
    override fun subscribeToRepositoryChanges(parentDisposable: Disposable, refresh: () -> Unit) {
        project.messageBus.connect(parentDisposable).apply {
            subscribe(
                GitRepository.GIT_REPO_CHANGE,
                GitRepositoryChangeListener { refresh() },
            )
        }
    }

    override fun subscribeToPushCompletion(parentDisposable: Disposable, refresh: () -> Unit) {
        GitPushCompletionService.getInstance(project)
            .addCompletionListener(parentDisposable) { refresh() }
    }

    override fun hasOutgoingCommitsToPush(): Boolean = !project.isDisposed &&
        gitPushSupport()?.let { pushSupport ->
            GitRepositoryManager.getInstance(project).repositories.any { repository ->
                repository.hasOutgoingCommits(pushSupport)
            }
        } == true

    private fun gitPushSupport(): GitPushSupport? = PushSupport.PUSH_SUPPORT_EP
        .getExtensionList(project)
        .filterIsInstance<GitPushSupport>()
        .firstOrNull { pushSupport -> pushSupport.vcs === GitVcs.getInstance(project) }

    private fun GitRepository.hasOutgoingCommits(pushSupport: GitPushSupport): Boolean {
        val source = pushSupport.getSource(this)
        val target = source?.let { pushSource -> pushSupport.getDefaultTarget(this, pushSource) }
        return outgoingCommits(pushSupport, source, target)?.commits?.isNotEmpty() == true
    }

    private fun GitRepository.outgoingCommits(
        pushSupport: GitPushSupport,
        source: GitPushSource?,
        target: GitPushTarget?,
    ) = if (state == Repository.State.NORMAL && source != null && target != null) {
        runCatching {
            pushSupport.outgoingCommitsProvider.getOutgoingCommits(
                this,
                PushSpec<GitPushSource, GitPushTarget>(source, target),
                false,
            )
        }.getOrNull()
    } else {
        null
    }
}

internal class GitOutgoingCommitsStatus(
    private val loader: () -> Boolean,
    private val scheduler: GitOutgoingCommitsRefreshScheduler,
    private val actionRefresh: GitOutgoingCommitsActionRefresh,
    private val refreshIntervalMillis: Long = DEFAULT_OUTGOING_COMMITS_REFRESH_INTERVAL_MILLIS,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val lock = Any()

    @Volatile
    private var cachedHasOutgoingCommitsToPush = false
    private var refreshInProgress = false
    private var refreshPending = false
    private var lastRefreshRequestMillis: Long? = null

    fun cachedHasOutgoingCommitsToPush(): Boolean {
        scheduleRefresh(force = false)
        return cachedHasOutgoingCommitsToPush
    }

    fun requestRefresh() {
        scheduleRefresh(force = true)
    }

    fun hasOutgoingCommitsToPush(): Boolean {
        val value = loader()
        val changed = synchronized(lock) {
            lastRefreshRequestMillis = nowMillis()
            updateCachedValueLocked(value)
        }
        if (changed) {
            actionRefresh.refreshActions()
        }
        return value
    }

    private fun scheduleRefresh(force: Boolean) {
        val shouldSchedule = synchronized(lock) {
            val now = nowMillis()
            if (!force && !isRefreshDue(now)) {
                false
            } else if (refreshInProgress) {
                refreshPending = true
                lastRefreshRequestMillis = now
                false
            } else {
                refreshInProgress = true
                lastRefreshRequestMillis = now
                true
            }
        }

        if (shouldSchedule) {
            scheduler.schedule { refreshInBackground() }
        }
    }

    private fun refreshInBackground() {
        val loadedValue = runCatching { loader() }.getOrNull()
        val refreshResult = synchronized(lock) {
            if (loadedValue != null) {
                GitOutgoingCommitsRefreshResult(
                    changed = updateCachedValueLocked(loadedValue),
                    reschedule = completeRefreshLocked(),
                )
            } else {
                GitOutgoingCommitsRefreshResult(
                    changed = false,
                    reschedule = completeRefreshLocked(),
                )
            }
        }

        if (refreshResult.changed) {
            actionRefresh.refreshActions()
        }
        if (refreshResult.reschedule) {
            scheduler.schedule { refreshInBackground() }
        }
    }

    private fun isRefreshDue(now: Long): Boolean {
        val lastRefresh = lastRefreshRequestMillis ?: return true
        return now - lastRefresh >= refreshIntervalMillis
    }

    private fun completeRefreshLocked(): Boolean = if (refreshPending) {
        refreshPending = false
        true
    } else {
        refreshInProgress = false
        false
    }

    private fun updateCachedValueLocked(value: Boolean): Boolean {
        val changed = cachedHasOutgoingCommitsToPush != value
        cachedHasOutgoingCommitsToPush = value
        return changed
    }
}

private data class GitOutgoingCommitsRefreshResult(
    val changed: Boolean,
    val reschedule: Boolean,
)

internal fun interface GitOutgoingCommitsRefreshScheduler {
    fun schedule(refresh: () -> Unit)
}

private object IntellijGitOutgoingCommitsRefreshScheduler : GitOutgoingCommitsRefreshScheduler {
    override fun schedule(refresh: () -> Unit) {
        JobScheduler.getScheduler().execute(refresh)
    }
}

internal fun interface GitOutgoingCommitsActionRefresh {
    fun refreshActions()
}

private object IntellijGitOutgoingCommitsActionRefresh : GitOutgoingCommitsActionRefresh {
    override fun refreshActions() {
        ActivityTracker.getInstance().inc()
    }
}

private const val DEFAULT_OUTGOING_COMMITS_REFRESH_INTERVAL_MILLIS: Long = 2_000L
