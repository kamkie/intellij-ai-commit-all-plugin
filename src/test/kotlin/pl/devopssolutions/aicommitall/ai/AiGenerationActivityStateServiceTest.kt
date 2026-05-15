package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.ui.AnimatedIcon
import javax.swing.Icon
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AiGenerationActivityStateServiceTest {
    @Test
    fun `tracks running activity until token closes`() {
        val service = AiGenerationActivityStateService()

        val token = service.start()

        assertTrue(service.isRunning())
        token.close()
        assertFalse(service.isRunning())
    }

    @Test
    fun `closing activity token is idempotent`() {
        val service = AiGenerationActivityStateService()
        val token = service.start()

        token.close()
        token.close()

        assertFalse(service.isRunning())
    }

    @Test
    fun `applies animated disabled presentation while running`() {
        val service = AiGenerationActivityStateService()
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
        val service = AiGenerationActivityStateService()
        val presentation = Presentation()

        service.applyToPresentation(
            presentation = presentation,
            idleIcon = TestIcon,
            enabledWhenIdle = true,
        )

        assertTrue(presentation.isEnabled)
        assertSame(TestIcon, presentation.icon)
    }

    private object TestIcon : Icon {
        override fun paintIcon(component: java.awt.Component?, graphics: java.awt.Graphics?, x: Int, y: Int) = Unit

        override fun getIconWidth(): Int = 16

        override fun getIconHeight(): Int = 16
    }
}
