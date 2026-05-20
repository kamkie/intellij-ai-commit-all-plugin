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
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.Change
import git4idea.GitVcs
import git4idea.push.GitPushSource
import git4idea.push.GitPushSupport
import git4idea.push.GitPushTarget
import git4idea.push.GitPushTargetType
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import java.util.concurrent.CompletableFuture

private typealias GitPushSpec = PushSpec<GitPushSource, GitPushTarget>

internal interface SafeImmediatePushSupport {
    fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision
}

internal interface SafeImmediateOutgoingPushSupport {
    fun prepareOutgoingCommits(): SafeImmediatePushDecision
}

internal fun interface SafeImmediatePushPlan {
    fun push(): CompletableFuture<Unit>
}

internal sealed interface SafeImmediatePushDecision {
    data class Immediate(val plan: SafeImmediatePushPlan) : SafeImmediatePushDecision

    data class Fallback(val reason: SafeImmediatePushFallbackReason) : SafeImmediatePushDecision
}

internal enum class SafeImmediatePushFallbackReason {
    NoAffectedRepositories,
    MissingAffectedRepository,
    UnsafeRepositoryState,
    UnresolvedConflict,
    MissingTrackedUpstream,
    ForcePushStateUnverified,
    AmbiguousTarget,
    UnsupportedPushApi,
}

internal data class SafeImmediatePushRepositoryState(
    val hasTrackedUpstream: Boolean,
    val localMatchesTrackedUpstream: Boolean,
    val targetIsTrackingBranch: Boolean,
    val targetMatchesTrackedUpstream: Boolean,
    val pushSpecAvailable: Boolean,
    val targetIsNewBranch: Boolean,
    val targetIsSpecialRef: Boolean,
    val repositoryStateIsNormal: Boolean,
)

internal object SafeImmediatePushDecisionPolicy {
    fun fallbackReason(
        repositories: Collection<SafeImmediatePushRepositoryState>,
        hasUnresolvedConflicts: Boolean,
        requireTrackedUpstreamHeadMatch: Boolean = true,
    ): SafeImmediatePushFallbackReason? {
        if (repositories.isEmpty()) {
            return SafeImmediatePushFallbackReason.NoAffectedRepositories
        }
        if (hasUnresolvedConflicts) {
            return SafeImmediatePushFallbackReason.UnresolvedConflict
        }
        if (repositories.any { repository -> !repository.repositoryStateIsNormal }) {
            return SafeImmediatePushFallbackReason.UnsafeRepositoryState
        }
        if (repositories.any { repository -> !repository.hasTrackedUpstream }) {
            return SafeImmediatePushFallbackReason.MissingTrackedUpstream
        }
        if (requireTrackedUpstreamHeadMatch && repositories.any { repository -> !repository.localMatchesTrackedUpstream }) {
            return SafeImmediatePushFallbackReason.ForcePushStateUnverified
        }
        if (repositories.any { repository -> repository.hasAmbiguousTarget }) {
            return SafeImmediatePushFallbackReason.AmbiguousTarget
        }
        if (repositories.any { repository -> !repository.pushSpecAvailable }) {
            return SafeImmediatePushFallbackReason.UnsupportedPushApi
        }
        return null
    }

    private val SafeImmediatePushRepositoryState.hasAmbiguousTarget: Boolean
        get() = !targetIsTrackingBranch ||
            !targetMatchesTrackedUpstream ||
            targetIsNewBranch ||
            targetIsSpecialRef
}

@Service(Service.Level.PROJECT)
internal class SafeImmediatePushService @JvmOverloads constructor(
    private val project: Project,
    private val environment: SafeImmediatePushEnvironment = IntellijSafeImmediatePushEnvironment(project),
) : SafeImmediatePushSupport,
    SafeImmediateOutgoingPushSupport {
    override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision {
        val paths = selection.affectedPaths()
        if (paths.isEmpty()) {
            return SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.NoAffectedRepositories)
        }

        val repositories = linkedSetOf<SafeImmediatePushRepositoryHandle>()
        for (path in paths) {
            val repository = environment.repositoryForPath(path)
                ?: return SafeImmediatePushDecision.Fallback(
                    SafeImmediatePushFallbackReason.MissingAffectedRepository,
                )
            repositories += repository
        }

        if (!environment.isPushSupportAvailable()) {
            return SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.UnsupportedPushApi)
        }

        val pushSpecs = linkedMapOf<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>()
        val repositoryStates = repositories.map { repository ->
            environment.pushState(repository).also { state ->
                state.pushSpec?.let { pushSpec -> pushSpecs[repository] = pushSpec }
            }.repositoryState
        }

        return decision(
            pushSpecs = pushSpecs,
            repositoryStates = repositoryStates,
            hasUnresolvedConflicts = selection.hasUnresolvedConflicts(),
        )
    }

    override fun prepareOutgoingCommits(): SafeImmediatePushDecision {
        if (project.isDisposed) {
            return SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.NoAffectedRepositories)
        }

        if (!environment.isPushSupportAvailable()) {
            return SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.UnsupportedPushApi)
        }

        val pushSpecs = linkedMapOf<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>()
        val repositoryStates = mutableListOf<SafeImmediatePushRepositoryState>()

        environment.repositories().forEach { repository ->
            val state = environment.pushState(repository)
            val pushSpec = state.pushSpec ?: return@forEach
            if (environment.hasOutgoingCommits(repository, pushSpec)) {
                pushSpecs[repository] = pushSpec
                repositoryStates += state.repositoryState
            }
        }

        return decision(
            pushSpecs = pushSpecs,
            repositoryStates = repositoryStates,
            hasUnresolvedConflicts = false,
            requireTrackedUpstreamHeadMatch = false,
        )
    }

    private fun decision(
        pushSpecs: Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>,
        repositoryStates: Collection<SafeImmediatePushRepositoryState>,
        hasUnresolvedConflicts: Boolean,
        requireTrackedUpstreamHeadMatch: Boolean = true,
    ): SafeImmediatePushDecision {
        val fallbackReason = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = repositoryStates,
            hasUnresolvedConflicts = hasUnresolvedConflicts,
            requireTrackedUpstreamHeadMatch = requireTrackedUpstreamHeadMatch,
        )
        return if (fallbackReason == null) {
            SafeImmediatePushDecision.Immediate(
                SafeImmediatePushPlan {
                    val pushCompletion = environment.awaitPushCompletion(pushSpecs.keys)
                    try {
                        environment.push(pushSpecs)
                    } catch (throwable: Throwable) {
                        pushCompletion.completeExceptionally(throwable)
                        throw throwable
                    }
                    pushCompletion
                },
            )
        } else {
            SafeImmediatePushDecision.Fallback(fallbackReason)
        }
    }

    companion object {
        fun getInstance(project: Project): SafeImmediatePushService = project.service()
    }
}

internal data class SafeImmediatePushRepositoryHandle(val value: Any)

internal data class SafeImmediatePushSpecHandle(val value: Any)

internal interface SafeImmediatePushEnvironment {
    fun repositoryForPath(path: FilePath): SafeImmediatePushRepositoryHandle?

    fun repositories(): List<SafeImmediatePushRepositoryHandle>

    fun isPushSupportAvailable(): Boolean

    fun pushState(repository: SafeImmediatePushRepositoryHandle): SafeImmediatePushRepositoryPushState

    fun hasOutgoingCommits(
        repository: SafeImmediatePushRepositoryHandle,
        pushSpec: SafeImmediatePushSpecHandle,
    ): Boolean

    fun awaitPushCompletion(
        repositories: Collection<SafeImmediatePushRepositoryHandle>,
    ): CompletableFuture<Unit>

    fun push(pushSpecs: Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>)
}

internal data class SafeImmediatePushRepositoryPushState(
    val repositoryState: SafeImmediatePushRepositoryState,
    val pushSpec: SafeImmediatePushSpecHandle?,
)

private class IntellijSafeImmediatePushEnvironment(private val project: Project) : SafeImmediatePushEnvironment {
    override fun repositoryForPath(path: FilePath) = GitRepositoryManager.getInstance(project)
        .getRepositoryForFileQuick(path)
        ?.let(::SafeImmediatePushRepositoryHandle)

    override fun repositories() = projectRepositories()

    override fun isPushSupportAvailable(): Boolean = gitPushSupport() != null

    override fun pushState(repository: SafeImmediatePushRepositoryHandle) = stateOf(repository)

    override fun hasOutgoingCommits(
        repository: SafeImmediatePushRepositoryHandle,
        pushSpec: SafeImmediatePushSpecHandle,
    ): Boolean = repository.gitRepository().hasOutgoingCommits(pushSupport(), pushSpec.pushSpec())

    override fun awaitPushCompletion(
        repositories: Collection<SafeImmediatePushRepositoryHandle>,
    ): CompletableFuture<Unit> = GitPushCompletionService.getInstance(project)
        .awaitCompletion(repositories.map { repository -> repository.gitRepository() })

    override fun push(pushSpecs: Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>) {
        val gitPushSpecs = pushSpecs.entries.associate { (repository, pushSpec) ->
            repository.gitRepository() to pushSpec.pushSpec()
        }
        pushSupport().pusher.push(gitPushSpecs, null, false)
    }

    private fun pushSupport(): GitPushSupport = requireNotNull(gitPushSupport()) {
        "Git push support is unavailable."
    }

    private fun gitPushSupport(): GitPushSupport? = PushSupport.PUSH_SUPPORT_EP
        .getExtensionList(project)
        .filterIsInstance<GitPushSupport>()
        .firstOrNull { pushSupport -> pushSupport.vcs === GitVcs.getInstance(project) }

    private fun stateOf(repo: SafeImmediatePushRepositoryHandle) = repo.gitRepository().pushState(pushSupport())

    private fun projectRepositories(): List<SafeImmediatePushRepositoryHandle> {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        return repositoryManager.repositories.map(::SafeImmediatePushRepositoryHandle)
    }
}

private fun GitRepository.pushState(
    pushSupport: GitPushSupport,
): SafeImmediatePushRepositoryPushState {
    val currentBranch = this.currentBranch
    val trackInfo = currentBranch?.let { branch -> getBranchTrackInfo(branch.name) }
    val source = pushSupport.getSource(this)
    val target = if (source == null) {
        null
    } else {
        pushSupport.getDefaultTarget(this, source)
    }

    val pushSpec = if (source != null && target != null) PushSpec(source, target) else null

    return SafeImmediatePushRepositoryPushState(
        repositoryState = SafeImmediatePushRepositoryState(
            hasTrackedUpstream = trackInfo != null,
            localMatchesTrackedUpstream = trackInfo != null && localMatchesTrackedUpstream(trackInfo),
            targetIsTrackingBranch = target?.targetType == GitPushTargetType.TRACKING_BRANCH,
            targetMatchesTrackedUpstream = trackInfo != null && target?.branch == trackInfo.remoteBranch,
            pushSpecAvailable = pushSpec != null,
            targetIsNewBranch = target?.isNewBranchCreated != false,
            targetIsSpecialRef = target?.isSpecialRef != false,
            repositoryStateIsNormal = state == Repository.State.NORMAL,
        ),
        pushSpec = pushSpec?.let(::SafeImmediatePushSpecHandle),
    )
}

private fun GitRepository.hasOutgoingCommits(pushSupport: GitPushSupport, pushSpec: GitPushSpec): Boolean {
    val outgoingResult = runCatching {
        pushSupport.outgoingCommitsProvider.getOutgoingCommits(this, pushSpec, false)
    }.getOrNull()
    return outgoingResult?.commits?.isNotEmpty() == true
}

private fun GitRepository.localMatchesTrackedUpstream(
    trackInfo: git4idea.repo.GitBranchTrackInfo,
): Boolean {
    val localBranch = currentBranch
    val localHash = localBranch?.let { branch -> info.localBranchesWithHashes[branch] }
    val remoteHash = info.remoteBranchesWithHashes[trackInfo.remoteBranch]
    return localHash != null && localHash == remoteHash
}

private fun SafeImmediatePushRepositoryHandle.gitRepository(): GitRepository = value as GitRepository

@Suppress("UNCHECKED_CAST")
private fun SafeImmediatePushSpecHandle.pushSpec(): GitPushSpec = value as GitPushSpec

private fun GitChangeSelection.affectedPaths(): List<FilePath> = buildList {
    trackedChanges.forEach { change ->
        addAffectedPaths(change)
    }
    addAll(unversionedFiles)
    addAll(resolvedConflictPaths)
    addAll(stagingAreaPaths)
}.distinctBy { path -> path.path }

private fun MutableList<FilePath>.addAffectedPaths(change: Change) {
    change.beforeRevision?.file?.let(::add)
    change.afterRevision?.file?.let(::add)
}

private fun GitChangeSelection.hasUnresolvedConflicts(): Boolean = trackedChanges.any { change -> change.fileStatus in unresolvedConflictStatuses }

private val unresolvedConflictStatuses = setOf(
    FileStatus.MERGED_WITH_CONFLICTS,
    FileStatus.MERGED_WITH_BOTH_CONFLICTS,
    FileStatus.MERGED_WITH_PROPERTY_CONFLICTS,
)
