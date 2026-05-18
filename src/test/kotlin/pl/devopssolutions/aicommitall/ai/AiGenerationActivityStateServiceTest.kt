package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.ui.AnimatedIcon
import javax.swing.Icon
import kotlin.test.*

internal class AiGenerationActivityStateServiceTest {
    @Test
    fun `tracks running activity until token closes`() {
        val service = testService()

        val token = service.start()

        assertTrue(service.isRunning())
        assertEquals(AiGenerationActivityPhase.Ai, service.runningPhase())
        token.close()
        assertFalse(service.isRunning())
        assertNull(service.runningPhase())
    }

    @Test
    fun `tracks requested activity phase`() {
        val service = testService()

        val token = service.start(AiGenerationActivityPhase.Push)

        assertEquals(AiGenerationActivityPhase.Push, service.runningPhase())
        token.close()
        assertNull(service.runningPhase())
    }

    @Test
    fun `moves running activity phase until token closes`() {
        val service = testService()
        val token = service.start(AiGenerationActivityPhase.Ai)

        token.moveTo(AiGenerationActivityPhase.Commit)

        assertEquals(AiGenerationActivityPhase.Commit, service.runningPhase())

        token.close()
        token.moveTo(AiGenerationActivityPhase.Push)

        assertNull(service.runningPhase())
    }

    @Test
    fun `closing activity token is idempotent`() {
        val service = testService()
        val token = service.start()

        token.close()
        token.close()

        assertFalse(service.isRunning())
    }

    @Test
    fun `refreshes actions when activity starts and finishes`() {
        val actionRefresh = CapturingActionRefresh()
        val service = testService(actionRefresh)

        val token = service.start(AiGenerationActivityPhase.Commit)
        token.close()

        assertEquals(2, actionRefresh.refreshCount)
    }

    @Test
    fun `applies animated disabled presentation while running`() {
        val service = testService()
        val presentation = Presentation()

        service.start()
        service.applyToPresentation(
            presentation = presentation,
            idleIcon = TestIcon,
            enabledWhenIdle = true,
        )

        assertFalse(presentation.isEnabled)
        assertSame(AnimatedIcon.Default.INSTANCE, presentation.icon)
    }

    @Test
    fun `restores idle presentation when not running`() {
        val service = testService()
        val presentation = Presentation()

        service.applyToPresentation(
            presentation = presentation,
            idleIcon = TestIcon,
            enabledWhenIdle = true,
        )

        assertTrue(presentation.isEnabled)
        assertSame(TestIcon, presentation.icon)
    }

    private fun testService(
        actionRefresh: CapturingActionRefresh = CapturingActionRefresh(),
    ): AiGenerationActivityStateService =
        AiGenerationActivityStateService().apply {
            replaceActionRefreshForTest(actionRefresh)
        }

    private class CapturingActionRefresh : AiGenerationActivityActionRefresh {
        var refreshCount: Int = 0

        override fun refreshActions() {
            refreshCount += 1
        }
    }

    private object TestIcon : Icon {
        override fun paintIcon(component: java.awt.Component?, graphics: java.awt.Graphics?, x: Int, y: Int) = Unit

        override fun getIconWidth(): Int = 16

        override fun getIconHeight(): Int = 16
    }
}
