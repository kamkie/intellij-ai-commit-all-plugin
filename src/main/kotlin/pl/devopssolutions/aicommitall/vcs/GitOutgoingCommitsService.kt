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
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import git4idea.GitVcs
import git4idea.push.GitPushSource
import git4idea.push.GitPushSupport
import git4idea.push.GitPushTarget
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

@Service(Service.Level.PROJECT)
internal class GitOutgoingCommitsService(private val project: Project) {
    private val outgoingCommitsStatus = GitOutgoingCommitsStatus(
        loader = ::loadHasOutgoingCommitsToPush,
        scheduler = IntellijGitOutgoingCommitsRefreshScheduler,
        actionRefresh = IntellijGitOutgoingCommitsActionRefresh,
    )

    fun hasOutgoingCommitsToPush(): Boolean = outgoingCommitsStatus.hasOutgoingCommitsToPush()

    fun cachedHasOutgoingCommitsToPush(): Boolean = outgoingCommitsStatus.cachedHasOutgoingCommitsToPush()

    private fun loadHasOutgoingCommitsToPush(): Boolean {
        if (project.isDisposed) {
            return false
        }

        val pushSupport = gitPushSupport() ?: return false
        return GitRepositoryManager.getInstance(project).repositories.any { repository ->
            repository.hasOutgoingCommits(pushSupport)
        }
    }

    private fun gitPushSupport(): GitPushSupport? = PushSupport.PUSH_SUPPORT_EP
        .getExtensionList(project)
        .filterIsInstance<GitPushSupport>()
        .firstOrNull { pushSupport -> pushSupport.vcs === GitVcs.getInstance(project) }

    private fun GitRepository.hasOutgoingCommits(pushSupport: GitPushSupport): Boolean {
        if (state != Repository.State.NORMAL) {
            return false
        }

        val source = pushSupport.getSource(this) ?: return false
        val target = pushSupport.getDefaultTarget(this, source) ?: return false
        if (!pushSupport.canBePushed(this, source, target)) {
            return false
        }

        val outgoingResult = runCatching {
            pushSupport.outgoingCommitsProvider.getOutgoingCommits(
                this,
                PushSpec<GitPushSource, GitPushTarget>(source, target),
                false,
            )
        }.getOrNull()
        return outgoingResult?.commits?.isNotEmpty() == true
    }

    companion object {
        fun getInstance(project: Project): GitOutgoingCommitsService = project.service()
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
    private var lastRefreshRequestMillis: Long? = null

    fun cachedHasOutgoingCommitsToPush(): Boolean {
        scheduleRefreshIfNeeded()
        return cachedHasOutgoingCommitsToPush
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

    private fun scheduleRefreshIfNeeded() {
        val shouldSchedule = synchronized(lock) {
            val now = nowMillis()
            if (refreshInProgress || !isRefreshDue(now)) {
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
        val changed = synchronized(lock) {
            refreshInProgress = false
            if (loadedValue != null) {
                updateCachedValueLocked(loadedValue)
            } else {
                false
            }
        }

        if (changed) {
            actionRefresh.refreshActions()
        }
    }

    private fun isRefreshDue(now: Long): Boolean {
        val lastRefresh = lastRefreshRequestMillis ?: return true
        return now - lastRefresh >= refreshIntervalMillis
    }

    private fun updateCachedValueLocked(value: Boolean): Boolean {
        val changed = cachedHasOutgoingCommitsToPush != value
        cachedHasOutgoingCommitsToPush = value
        return changed
    }
}

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
