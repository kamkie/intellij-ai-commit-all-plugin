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

import com.intellij.dvcs.push.PushSpec
import com.intellij.dvcs.push.PushSupport
import com.intellij.dvcs.repo.Repository
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
    fun hasOutgoingCommitsToPush(): Boolean {
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
