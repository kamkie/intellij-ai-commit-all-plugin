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

internal interface SafeImmediatePushSupport {
    fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision
}

internal fun interface SafeImmediatePushPlan {
    fun push()
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
    val targetCanBePushed: Boolean,
    val targetAllowedByStandardPushRules: Boolean,
    val targetIsNewBranch: Boolean,
    val targetIsSpecialRef: Boolean,
    val repositoryStateIsNormal: Boolean,
)

internal object SafeImmediatePushDecisionPolicy {
    fun fallbackReason(
        repositories: Collection<SafeImmediatePushRepositoryState>,
        hasUnresolvedConflicts: Boolean,
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
        if (repositories.any { repository -> !repository.localMatchesTrackedUpstream }) {
            return SafeImmediatePushFallbackReason.ForcePushStateUnverified
        }
        if (repositories.any { repository -> repository.hasAmbiguousTarget }) {
            return SafeImmediatePushFallbackReason.AmbiguousTarget
        }
        if (repositories.any { repository -> !repository.targetCanBePushed || !repository.targetAllowedByStandardPushRules }) {
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
internal class SafeImmediatePushService(private val project: Project) : SafeImmediatePushSupport {
    override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision {
        val paths = selection.affectedPaths()
        if (paths.isEmpty()) {
            return SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.NoAffectedRepositories)
        }

        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repositories = linkedSetOf<GitRepository>()
        for (path in paths) {
            val repository = repositoryManager.getRepositoryForFileQuick(path)
                ?: return SafeImmediatePushDecision.Fallback(
                    SafeImmediatePushFallbackReason.MissingAffectedRepository,
                )
            repositories += repository
        }

        val pushSupport = gitPushSupport()
            ?: return SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.UnsupportedPushApi)

        val pushSpecs = linkedMapOf<GitRepository, PushSpec<GitPushSource, GitPushTarget>>()
        val repositoryStates = repositories.map { repository ->
            repository.pushState(pushSupport, pushSpecs)
        }

        val fallbackReason = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = repositoryStates,
            hasUnresolvedConflicts = selection.hasUnresolvedConflicts(),
        )

        return if (fallbackReason == null) {
            SafeImmediatePushDecision.Immediate(
                SafeImmediatePushPlan {
                    pushSupport.pusher.push(pushSpecs, null, false)
                },
            )
        } else {
            SafeImmediatePushDecision.Fallback(fallbackReason)
        }
    }

    private fun gitPushSupport(): GitPushSupport? =
        PushSupport.PUSH_SUPPORT_EP
            .getExtensionList(project)
            .filterIsInstance<GitPushSupport>()
            .firstOrNull { pushSupport -> pushSupport.vcs === GitVcs.getInstance(project) }

    private fun GitRepository.pushState(
        pushSupport: GitPushSupport,
        pushSpecs: MutableMap<GitRepository, PushSpec<GitPushSource, GitPushTarget>>,
    ): SafeImmediatePushRepositoryState {
        val currentBranch = this.currentBranch
        val trackInfo = currentBranch?.let { branch -> getBranchTrackInfo(branch.name) }
        val source = pushSupport.getSource(this)
        val target = if (source == null) {
            null
        } else {
            pushSupport.getDefaultTarget(this, source)
        }

        if (source != null && target != null) {
            pushSpecs[this] = PushSpec(source, target)
        }

        return SafeImmediatePushRepositoryState(
            hasTrackedUpstream = trackInfo != null,
            localMatchesTrackedUpstream = trackInfo != null && localMatchesTrackedUpstream(trackInfo),
            targetIsTrackingBranch = target?.targetType == GitPushTargetType.TRACKING_BRANCH,
            targetMatchesTrackedUpstream = trackInfo != null && target?.branch == trackInfo.remoteBranch,
            targetCanBePushed = source != null && target != null && pushSupport.canBePushed(this, source, target),
            targetAllowedByStandardPushRules = target != null && pushSupport.isForcePushAllowed(this, target),
            targetIsNewBranch = target?.isNewBranchCreated != false,
            targetIsSpecialRef = target?.isSpecialRef != false,
            repositoryStateIsNormal = state == Repository.State.NORMAL,
        )
    }

    private fun GitRepository.localMatchesTrackedUpstream(
        trackInfo: git4idea.repo.GitBranchTrackInfo,
    ): Boolean {
        val localBranch = currentBranch ?: return false
        val localHash = info.localBranchesWithHashes[localBranch] ?: return false
        val remoteHash = info.remoteBranchesWithHashes[trackInfo.remoteBranch] ?: return false
        return localHash == remoteHash
    }

    companion object {
        fun getInstance(project: Project): SafeImmediatePushService = project.service()
    }
}

private fun GitChangeSelection.affectedPaths(): List<FilePath> =
    buildList {
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

private fun GitChangeSelection.hasUnresolvedConflicts(): Boolean =
    trackedChanges.any { change -> change.fileStatus in unresolvedConflictStatuses }

private val unresolvedConflictStatuses = setOf(
    FileStatus.MERGED_WITH_CONFLICTS,
    FileStatus.MERGED_WITH_BOTH_CONFLICTS,
    FileStatus.MERGED_WITH_PROPERTY_CONFLICTS,
)
