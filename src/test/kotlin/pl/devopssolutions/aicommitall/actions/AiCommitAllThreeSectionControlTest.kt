package pl.devopssolutions.aicommitall.actions

import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.KeyStroke
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AiCommitAllThreeSectionControlTest {
    @Test
    fun `mouse click activates selected enabled section`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl { section, _ -> activations += section }

        control.dispatchClick(AiCommitAllControlSection.Push)

        assertEquals(listOf(AiCommitAllControlSection.Push), activations)
    }

    @Test
    fun `disabled section ignores mouse interaction`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl(
            state = testState(
                AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            ),
        ) { section, _ -> activations += section }

        control.dispatchMove(AiCommitAllControlSection.Push)
        control.dispatchClick(AiCommitAllControlSection.Push)

        assertTrue(activations.isEmpty())
        assertEquals(emptySet(), control.highlightedSectionsForTest())
    }

    @Test
    fun `mouse hover highlights cumulative sections and clears after exit`() {
        val control = testControl()

        control.dispatchMove(AiCommitAllControlSection.Commit)

        assertEquals(
            setOf(AiCommitAllControlSection.Ai, AiCommitAllControlSection.Commit),
            control.highlightedSectionsForTest(),
        )

        control.dispatchExit()

        assertEquals(emptySet(), control.highlightedSectionsForTest())
    }

    @Test
    fun `state update clears stale hover when hovered section becomes unavailable`() {
        val control = testControl()
        control.dispatchMove(AiCommitAllControlSection.Push)

        control.updateState(
            testState(
                AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            ),
        )

        assertEquals(emptySet(), control.highlightedSectionsForTest())
    }

    @Test
    fun `tooltips describe section effects`() {
        val control = testControl()

        assertEquals(
            "Generate an AI commit message for all Git changes.",
            control.toolTipFor(AiCommitAllControlSection.Ai),
        )
        assertEquals(
            "Generate an AI commit message and commit all Git changes.",
            control.toolTipFor(AiCommitAllControlSection.Commit),
        )
        assertEquals(
            "Generate an AI commit message, commit all Git changes, and push.",
            control.toolTipFor(AiCommitAllControlSection.Push),
        )
    }

    @Test
    fun `enter activates commit section by default`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl { section, _ -> activations += section }

        control.performKey(KeyEvent.VK_ENTER)

        assertEquals(listOf(AiCommitAllControlSection.Commit), activations)
    }

    @Test
    fun `right arrow moves keyboard activation to push section`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl { section, _ -> activations += section }

        control.performKey(KeyEvent.VK_RIGHT)
        control.performKey(KeyEvent.VK_ENTER)

        assertEquals(listOf(AiCommitAllControlSection.Push), activations)
        assertEquals(
            setOf(
                AiCommitAllControlSection.Ai,
                AiCommitAllControlSection.Commit,
                AiCommitAllControlSection.Push,
            ),
            control.highlightedSectionsForTest(),
        )
    }

    @Test
    fun `left arrow moves keyboard activation to ai section`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl { section, _ -> activations += section }

        control.performKey(KeyEvent.VK_LEFT)
        control.performKey(KeyEvent.VK_ENTER)

        assertEquals(listOf(AiCommitAllControlSection.Ai), activations)
        assertEquals(setOf(AiCommitAllControlSection.Ai), control.highlightedSectionsForTest())
    }

    @Test
    fun `keyboard navigation skips disabled sections`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl(
            state = testState(
                AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            ),
        ) { section, _ -> activations += section }

        control.performKey(KeyEvent.VK_RIGHT)
        control.performKey(KeyEvent.VK_ENTER)

        assertEquals(listOf(AiCommitAllControlSection.Ai), activations)
        assertEquals(setOf(AiCommitAllControlSection.Ai), control.highlightedSectionsForTest())
    }

    @Test
    fun `keyboard activation is ignored while a section is running`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl(
            state = testState(runningSection = AiCommitAllControlSection.Commit),
        ) { section, _ -> activations += section }

        control.performKey(KeyEvent.VK_ENTER)

        assertTrue(activations.isEmpty())
    }

    @Test
    fun `accessibility names the segmented control`() {
        val control = testControl()

        assertEquals("AI Commit All", control.accessibleContext.accessibleName)
        assertEquals("AI, Commit, and Push sections", control.accessibleContext.accessibleDescription)
    }

    @Test
    fun `hidden state hides and disables the component`() {
        val control = testControl(state = AiCommitAllControlState.Hidden)

        assertTrue(!control.isVisible)
        assertTrue(!control.isEnabled)
    }

    private fun testControl(
        state: AiCommitAllControlState = testState(),
        activateSection: (AiCommitAllControlSection, java.awt.event.InputEvent?) -> Unit = { _, _ -> },
    ): AiCommitAllThreeSectionControl =
        AiCommitAllThreeSectionControl(activateSection).apply {
            setSize(preferredSize)
            updateState(state)
        }

    private fun testState(
        vararg overrides: Pair<AiCommitAllControlSection, AiCommitAllWorkflowActionAvailability>,
        runningSection: AiCommitAllControlSection? = null,
    ): AiCommitAllControlState {
        val overrideMap = overrides.toMap()
        return AiCommitAllControlState(
            sections = AiCommitAllControlSection.entries.associateWith { section ->
                overrideMap[section] ?: AiCommitAllWorkflowActionAvailability.Enabled
            },
            runningSection = runningSection,
        )
    }

    private fun AiCommitAllThreeSectionControl.dispatchClick(section: AiCommitAllControlSection) {
        dispatchEvent(mouseEvent(MouseEvent.MOUSE_CLICKED, section, MouseEvent.BUTTON1))
    }

    private fun AiCommitAllThreeSectionControl.dispatchMove(section: AiCommitAllControlSection) {
        dispatchEvent(mouseEvent(MouseEvent.MOUSE_MOVED, section, MouseEvent.NOBUTTON))
    }

    private fun AiCommitAllThreeSectionControl.dispatchExit() {
        dispatchEvent(
            MouseEvent(
                this,
                MouseEvent.MOUSE_EXITED,
                System.currentTimeMillis(),
                0,
                -1,
                -1,
                0,
                false,
                MouseEvent.NOBUTTON,
            ),
        )
    }

    private fun AiCommitAllThreeSectionControl.toolTipFor(section: AiCommitAllControlSection): String? =
        getToolTipText(mouseEvent(MouseEvent.MOUSE_MOVED, section, MouseEvent.NOBUTTON))

    private fun AiCommitAllThreeSectionControl.mouseEvent(
        id: Int,
        section: AiCommitAllControlSection,
        button: Int,
    ): MouseEvent {
        val point = pointInside(section)
        return MouseEvent(
            this,
            id,
            System.currentTimeMillis(),
            0,
            point.first,
            point.second,
            1,
            false,
            button,
        )
    }

    private fun AiCommitAllThreeSectionControl.pointInside(section: AiCommitAllControlSection): Pair<Int, Int> {
        val xRatio = when (section) {
            AiCommitAllControlSection.Ai -> 0.13
            AiCommitAllControlSection.Commit -> 0.45
            AiCommitAllControlSection.Push -> 0.82
        }
        return (width * xRatio).toInt() to height / 2
    }

    private fun AiCommitAllThreeSectionControl.performKey(keyCode: Int) {
        val actionKey = requireNotNull(inputMap.get(KeyStroke.getKeyStroke(keyCode, 0)))
        val action = requireNotNull(actionMap.get(actionKey))
        action.actionPerformed(ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionKey.toString()))
    }
}
