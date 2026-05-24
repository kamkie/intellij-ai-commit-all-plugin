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
package pl.devopssolutions.aicommitall.workflow

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VcsOperationReadinessServiceTest {
    @Test
    fun `is ready when VCS is not frozen or busy`() {
        val state = TestVcsOperationState()
        val reporter = CapturingReporter()

        val result = VcsOperationReadinessGuard(state, reporter).checkAndReport()

        assertEquals(VcsOperationReadinessResult.Ready, result)
        assertEquals(0, reporter.backgroundNotificationCount)
    }

    @Test
    fun `stops when changelist manager is frozen`() {
        val state = TestVcsOperationState(frozen = true, backgroundOperationRunning = true)
        val reporter = CapturingReporter()

        val result = VcsOperationReadinessGuard(state, reporter).checkAndReport()

        assertEquals(VcsOperationReadinessResult.Frozen, result)
        assertEquals(0, reporter.backgroundNotificationCount)
    }

    @Test
    fun `continues when changelist manager unfreezes during bounded readiness settling`() {
        val state = SequencedVcsOperationState(
            frozenResults = listOf(true, false),
        )
        val reporter = CapturingReporter()

        val result = VcsOperationReadinessGuard(state, reporter).checkAndReport()

        assertEquals(VcsOperationReadinessResult.Ready, result)
        assertEquals(0, reporter.backgroundNotificationCount)
        assertEquals(2, state.frozenCheckCount)
        assertEquals(1, state.backgroundCheckCount)
    }

    @Test
    fun `stops and reports when background VCS operation is running`() {
        val state = TestVcsOperationState(backgroundOperationRunning = true)
        val reporter = CapturingReporter()

        val result = VcsOperationReadinessGuard(state, reporter).checkAndReport()

        assertEquals(VcsOperationReadinessResult.BackgroundOperationRunning, result)
        assertEquals(1, reporter.backgroundNotificationCount)
    }

    @Test
    fun `continues when background operation finishes during bounded readiness settling`() {
        val state = SequencedVcsOperationState(
            backgroundResults = listOf(true, false),
        )
        val reporter = CapturingReporter()

        val result = VcsOperationReadinessGuard(state, reporter).checkAndReport()

        assertEquals(VcsOperationReadinessResult.Ready, result)
        assertEquals(0, reporter.backgroundNotificationCount)
        assertEquals(2, state.frozenCheckCount)
        assertEquals(2, state.backgroundCheckCount)
    }

    private class TestVcsOperationState(
        private val frozen: Boolean = false,
        private val backgroundOperationRunning: Boolean = false,
    ) : VcsOperationState {
        override fun isFrozenWithNotification(): Boolean = frozen

        override fun isBackgroundOperationRunning(): Boolean = backgroundOperationRunning
    }

    private class SequencedVcsOperationState(
        private val frozenResults: List<Boolean> = listOf(false),
        private val backgroundResults: List<Boolean> = listOf(false),
    ) : VcsOperationState {
        var frozenCheckCount = 0
        var backgroundCheckCount = 0

        override fun isFrozenWithNotification(): Boolean {
            val result = frozenResults.valueAt(frozenCheckCount)
            frozenCheckCount++
            return result
        }

        override fun isBackgroundOperationRunning(): Boolean {
            val result = backgroundResults.valueAt(backgroundCheckCount)
            backgroundCheckCount++
            return result
        }

        private fun List<Boolean>.valueAt(index: Int): Boolean = getOrElse(index) { last() }
    }

    private class CapturingReporter : VcsOperationReadinessReporter {
        var backgroundNotificationCount = 0

        override fun notifyBackgroundOperationRunning() {
            backgroundNotificationCount++
        }
    }
}
