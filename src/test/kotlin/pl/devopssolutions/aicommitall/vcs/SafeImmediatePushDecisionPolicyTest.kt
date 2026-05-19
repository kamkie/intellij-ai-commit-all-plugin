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
    fun `allows outgoing-only push when local branch is ahead of tracked upstream`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(localMatchesTrackedUpstream = false)),
            hasUnresolvedConflicts = false,
            requireTrackedUpstreamHeadMatch = false,
        )

        assertNull(result)
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

    @Test
    fun `falls back when repository state is not normal`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(repositoryStateIsNormal = false)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.UnsafeRepositoryState, result)
    }

    @Test
    fun `falls back when target is not the tracking branch`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(targetIsTrackingBranch = false)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.AmbiguousTarget, result)
    }

    @Test
    fun `falls back when target would create a new branch`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(targetIsNewBranch = true)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.AmbiguousTarget, result)
    }

    @Test
    fun `falls back when target is a special ref`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(targetIsSpecialRef = true)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.AmbiguousTarget, result)
    }

    @Test
    fun `falls back when push spec cannot be created`() {
        val result = SafeImmediatePushDecisionPolicy.fallbackReason(
            repositories = listOf(safeRepositoryState(pushSpecAvailable = false)),
            hasUnresolvedConflicts = false,
        )

        assertEquals(SafeImmediatePushFallbackReason.UnsupportedPushApi, result)
    }

    private fun safeRepositoryState(
        hasTrackedUpstream: Boolean = true,
        localMatchesTrackedUpstream: Boolean = true,
        targetIsTrackingBranch: Boolean = true,
        targetMatchesTrackedUpstream: Boolean = true,
        pushSpecAvailable: Boolean = true,
        targetIsNewBranch: Boolean = false,
        targetIsSpecialRef: Boolean = false,
        repositoryStateIsNormal: Boolean = true,
    ): SafeImmediatePushRepositoryState = SafeImmediatePushRepositoryState(
        hasTrackedUpstream = hasTrackedUpstream,
        localMatchesTrackedUpstream = localMatchesTrackedUpstream,
        targetIsTrackingBranch = targetIsTrackingBranch,
        targetMatchesTrackedUpstream = targetMatchesTrackedUpstream,
        pushSpecAvailable = pushSpecAvailable,
        targetIsNewBranch = targetIsNewBranch,
        targetIsSpecialRef = targetIsSpecialRef,
        repositoryStateIsNormal = repositoryStateIsNormal,
    )
}
