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
package pl.devopssolutions.aicommitall.actions

import com.intellij.ide.ui.laf.darcula.DarculaUIUtil
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
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
    fun `disabled section suppresses hover but delegates activation for action-time routing`() {
        val activations = mutableListOf<AiCommitAllControlSection>()
        val control = testControl(
            state = testState(
                AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            ),
        ) { section, _ -> activations += section }

        control.dispatchMove(AiCommitAllControlSection.Push)
        control.dispatchClick(AiCommitAllControlSection.Push)

        assertEquals(listOf(AiCommitAllControlSection.Push), activations)
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

        assertEquals(AI_COMMIT_ALL_CONTROL_COMPONENT_NAME, control.name)
        assertEquals("AI Commit All", control.accessibleContext.accessibleName)
        assertEquals("AI, Commit, and Push sections", control.accessibleContext.accessibleDescription)
    }

    @Test
    fun `accessibility reports disabled and running states`() {
        val disabledControl = testControl(
            state = testState(
                AiCommitAllControlSection.Ai to AiCommitAllWorkflowActionAvailability.Disabled,
                AiCommitAllControlSection.Commit to AiCommitAllWorkflowActionAvailability.Disabled,
                AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            ),
        )
        val runningControl = testControl(
            state = testState(runningSection = AiCommitAllControlSection.Commit),
        )

        assertEquals(
            "AI, Commit, and Push sections; no sections are enabled",
            disabledControl.accessibleContext.accessibleDescription,
        )
        assertEquals(
            "AI, Commit, and Push sections; Commit is running",
            runningControl.accessibleContext.accessibleDescription,
        )
    }

    @Test
    fun `hidden state hides and disables the component`() {
        val control = testControl(state = AiCommitAllControlState.Hidden)

        assertTrue(!control.isVisible)
        assertTrue(!control.isEnabled)
    }

    @Test
    fun `running indicator paints after animation offset advances`() {
        val control = testControl(
            state = testState(runningSection = AiCommitAllControlSection.Commit),
        )
        control.setSnakeOffsetForTest(8f)
        val image = BufferedImage(control.width, control.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()

        try {
            control.paint(graphics)
        } finally {
            graphics.dispose()
        }

        assertTrue((image.getRGB(control.width / 2, control.height / 2) ushr 24) > 0)
    }

    @Test
    fun `running indicator dash cycle is longer than section path`() {
        val control = testControl(
            state = testState(runningSection = AiCommitAllControlSection.Commit),
        )

        AiCommitAllControlSection.entries.forEach { section ->
            val dash = control.runningIndicatorDashForTest(section)

            assertTrue(
                dash.cycleLength > dash.pathLength,
                "${section.label} dash cycle ${dash.cycleLength} must exceed path ${dash.pathLength}",
            )
        }
    }

    @Test
    fun `running indicator phase advances by animation offset`() {
        val control = testControl(
            state = testState(runningSection = AiCommitAllControlSection.Commit),
        )

        control.setSnakeOffsetForTest(8f)
        val dash = control.runningIndicatorDashForTest(AiCommitAllControlSection.Commit)

        assertTrue(dash.phase in 7.9f..8.1f, "phase was ${dash.phase}")
    }

    @Test
    fun `control corner arc follows platform button arc`() {
        val control = testControl()

        assertEquals(DarculaUIUtil.BUTTON_ARC.getFloat(), control.cornerArcForTest())
    }

    @Test
    fun `control rendering smoke is nonblank in light and dark modes`() {
        val control = testControl(
            state = testState(runningSection = AiCommitAllControlSection.Push),
        )

        listOf(false, true).forEach { dark ->
            withDarkMode(dark) {
                assertTrue(control.renderImage().hasNonblankContent(), "dark=$dark rendering must be nonblank")
            }
        }
    }

    private fun testControl(
        state: AiCommitAllControlState = testState(),
        activateSection: (AiCommitAllControlSection, java.awt.event.InputEvent?) -> Unit = { _, _ -> },
    ): AiCommitAllThreeSectionControl = AiCommitAllThreeSectionControl(activateSection).apply {
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

    private fun AiCommitAllThreeSectionControl.toolTipFor(section: AiCommitAllControlSection): String? {
        val event = mouseEvent(MouseEvent.MOUSE_MOVED, section, MouseEvent.NOBUTTON)
        return getToolTipText(event)
    }

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

    private fun AiCommitAllThreeSectionControl.setSnakeOffsetForTest(offset: Float) {
        testPeerForTest.setSnakeOffset(offset)
    }

    private fun AiCommitAllThreeSectionControl.renderImage(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            paint(graphics)
        } finally {
            graphics.dispose()
        }
        return image
    }

    private fun BufferedImage.hasNonblankContent(): Boolean {
        var opaquePixels = 0
        val colors = mutableSetOf<Int>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = getRGB(x, y)
                if ((pixel ushr 24) > 0) {
                    opaquePixels++
                    colors += pixel
                }
            }
        }
        return opaquePixels > 0 && colors.size > 1
    }

    private fun withDarkMode(
        dark: Boolean,
        block: () -> Unit,
    ) {
        val previousDarkMode = !JBColor.isBright()
        JBColor.setDark(dark)
        try {
            block()
        } finally {
            JBColor.setDark(previousDarkMode)
        }
    }
}

internal fun AiCommitAllThreeSectionControl.sectionLabels(): List<String> = testPeerForTest.sectionLabels()

internal fun AiCommitAllThreeSectionControl.isSectionEnabledForTest(
    section: AiCommitAllControlSection,
): Boolean = testPeerForTest.isSectionEnabled(section)

internal fun AiCommitAllThreeSectionControl.setHoverSectionForTest(section: AiCommitAllControlSection?) {
    testPeerForTest.setHoverSection(section)
}

internal fun AiCommitAllThreeSectionControl.highlightedSectionsForTest() = testPeerForTest.highlightedSections()

internal fun AiCommitAllThreeSectionControl.dividerColorsForTest(): Pair<Color, Color> = testPeerForTest.dividerColors()

internal fun AiCommitAllThreeSectionControl.runningIndicatorDashForTest(
    section: AiCommitAllControlSection,
): RunningIndicatorDash = testPeerForTest.runningIndicatorDash(section)

internal fun AiCommitAllThreeSectionControl.cornerArcForTest(): Float = testPeerForTest.cornerArc()
