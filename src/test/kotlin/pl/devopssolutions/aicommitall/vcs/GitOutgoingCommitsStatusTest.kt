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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GitOutgoingCommitsStatusTest {
    @Test
    fun `cached status returns last known value and refreshes in background`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertTrue(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, actionRefresh.count)
        assertEquals(0, scheduler.pendingCount)
    }

    @Test
    fun `cached status coalesces repeated refresh requests`() {
        val scheduler = CapturingRefreshScheduler()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = CountingActionRefresh(),
            nowMillis = { 0L },
        )

        status.cachedHasOutgoingCommitsToPush()
        status.cachedHasOutgoingCommitsToPush()

        assertEquals(1, scheduler.pendingCount)
    }

    @Test
    fun `synchronous status refreshes cache without scheduling background work`() {
        val scheduler = CapturingRefreshScheduler()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = CountingActionRefresh(),
            nowMillis = { 0L },
        )

        assertTrue(status.hasOutgoingCommitsToPush())
        assertTrue(status.cachedHasOutgoingCommitsToPush())

        assertEquals(0, scheduler.pendingCount)
    }

    private class CapturingRefreshScheduler : GitOutgoingCommitsRefreshScheduler {
        private val pendingRefreshes = mutableListOf<() -> Unit>()
        val pendingCount: Int
            get() = pendingRefreshes.size

        override fun schedule(refresh: () -> Unit) {
            pendingRefreshes += refresh
        }

        fun runNext() {
            pendingRefreshes.removeAt(0).invoke()
        }
    }

    private class CountingActionRefresh : GitOutgoingCommitsActionRefresh {
        var count: Int = 0

        override fun refreshActions() {
            count++
        }
    }
}
