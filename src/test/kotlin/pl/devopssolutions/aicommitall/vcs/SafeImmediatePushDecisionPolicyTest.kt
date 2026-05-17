package pl.devopssolutions.aicommitall.vcs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SafeImmediatePushDecisionPolicyTest {
    @Test
    fun `allows immediate push when every repository state is safe`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState()),
            hasUnresolvedConflicts = false,
        )

        assertNull(result)
    }

    @Test
    fun `falls back when no affected repository can be resolved`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = emptyList(),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.NoAffectedRepositories, result)
    }

    @Test
    fun `falls back when selected changes contain unresolved conflicts`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState()),
            hasUnresolvedConflicts = true,
        )

        assertEquals(SafeImmediatePushFallbackReason.UnresolvedConflict, result)
    }

    @Test
    fun `falls back when a repository has no tracked upstream`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(hasTrackedUpstream = false)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.MissingTrackedUpstream, result)
    }

    @Test
    fun `falls back when force-push safety cannot be proven`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(localMatchesTrackedUpstream = false)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.ForcePushStateUnverified, result)
    }

    @Test
    fun `falls back when a multi-root target is ambiguous`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(
                safeRepositoryState(),
                safeRepositoryState(targetMatchesTrackedUpstream = false),
            ),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.AmbiguousTarget, result)
    }

    private fun safeRepositoryState(
        hasTrackedUpstream: Boolean = true,
        localMatchesTrackedUpstream: Boolean = true,
        targetIsTrackingBranch: Boolean = true,
        targetMatchesTrackedUpstream: Boolean = true,
        targetCanBePushed: Boolean = true,
        targetAllowedByStandardPushRules: Boolean = true,
        targetIsNewBranch: Boolean = false,
        targetIsSpecialRef: Boolean = false,
        repositoryStateIsNormal: Boolean = true,
    ): SafeImmediatePushRepositoryState =
        SafeImmediatePushRepositoryState(
            hasTrackedUpstream = hasTrackedUpstream,
            localMatchesTrackedUpstream = localMatchesTrackedUpstream,
            targetIsTrackingBranch = targetIsTrackingBranch,
            targetMatchesTrackedUpstream = targetMatchesTrackedUpstream,
            targetCanBePushed = targetCanBePushed,
            targetAllowedByStandardPushRules = targetAllowedByStandardPushRules,
            targetIsNewBranch = targetIsNewBranch,
            targetIsSpecialRef = targetIsSpecialRef,
            repositoryStateIsNormal = repositoryStateIsNormal,
        )
}
