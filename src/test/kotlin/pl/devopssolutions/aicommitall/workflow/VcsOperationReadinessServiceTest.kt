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
    fun `stops and reports when background VCS operation is running`() {
        val state = TestVcsOperationState(backgroundOperationRunning = true)
        val reporter = CapturingReporter()

        val result = VcsOperationReadinessGuard(state, reporter).checkAndReport()

        assertEquals(VcsOperationReadinessResult.BackgroundOperationRunning, result)
        assertEquals(1, reporter.backgroundNotificationCount)
    }

    private class TestVcsOperationState(
        private val frozen: Boolean = false,
        private val backgroundOperationRunning: Boolean = false,
    ) : VcsOperationState {
        override fun isFrozenWithNotification(): Boolean = frozen

        override fun isBackgroundOperationRunning(): Boolean = backgroundOperationRunning
    }

    private class CapturingReporter : VcsOperationReadinessReporter {
        var backgroundNotificationCount = 0

        override fun notifyBackgroundOperationRunning() {
            backgroundNotificationCount++
        }
    }
}
