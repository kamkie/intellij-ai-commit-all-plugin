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

import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import javax.accessibility.AccessibleContext
import javax.swing.JComponent
import javax.swing.Timer
import javax.swing.ToolTipManager

internal class AiCommitAllThreeSectionControl(
    private val activateSection: (AiCommitAllControlSection, InputEvent?) -> Unit,
) : JComponent() {
    private val model = ThreeSectionControlModel()
    private val geometry = ThreeSectionControlGeometry()
    private val renderer = ThreeSectionControlRenderer(model, geometry)
    private val interaction = ThreeSectionControlInteraction(this, model, renderer, activateSection)
    private val snakeTimer = Timer(SNAKE_FRAME_DELAY_MS) {
        model.snakeOffset += JBUI.scale(SNAKE_FRAME_STEP)
        repaint()
    }

    internal val testPeerForTest = TestPeer()

    init {
        name = AI_COMMIT_ALL_CONTROL_COMPONENT_NAME
        isOpaque = false
        isFocusable = true
        border = JBUI.Borders.empty()
        cursor = Cursor.getDefaultCursor()
        toolTipText = ""
        ToolTipManager.sharedInstance().registerComponent(this)
        interaction.install()
    }

    override fun getAccessibleContext(): AccessibleContext {
        if (accessibleContext == null) {
            accessibleContext = object : AccessibleJComponent() {
                override fun getAccessibleName(): String = super.getAccessibleName()?.takeIf { it.isNotBlank() }
                    ?: "AI Commit All"

                override fun getAccessibleDescription(): String = super.getAccessibleDescription()
                    ?.takeIf { it.isNotBlank() }
                    ?: model.accessibleDescription()
            }
        }
        return accessibleContext
    }

    override fun getPreferredSize(): Dimension = geometry.preferredSize()

    override fun getMinimumSize(): Dimension = preferredSize

    override fun getToolTipText(event: MouseEvent): String? = renderer.sectionAt(this, event.point)?.toolTipText

    fun updateState(nextState: AiCommitAllControlState) {
        model.updateState(nextState)
        isVisible = nextState.visible
        isEnabled = nextState.enabled
        updateAnimationState()
        repaint()
    }

    override fun paintComponent(graphics: Graphics) {
        val graphics2D = graphics.create() as Graphics2D
        try {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            renderer.paint(this, graphics2D)
        } finally {
            graphics2D.dispose()
        }
    }

    override fun addNotify() {
        super.addNotify()
        updateAnimationState()
    }

    override fun removeNotify() {
        snakeTimer.stop()
        super.removeNotify()
    }

    private fun updateAnimationState() {
        if (model.state.runningSection != null && isDisplayable) {
            if (!snakeTimer.isRunning) {
                snakeTimer.start()
            }
        } else {
            snakeTimer.stop()
            model.snakeOffset = 0f
        }
    }

    internal inner class TestPeer {
        fun sectionLabels(): List<String> = controlSections.map { section -> section.label }

        fun isSectionEnabled(section: AiCommitAllControlSection): Boolean = model.state.isSectionEnabled(section)

        fun setHoverSection(section: AiCommitAllControlSection?) {
            model.hoverSection = section
        }

        fun highlightedSections(): Set<AiCommitAllControlSection> = model.highlightedSections()

        fun dividerColors(): Pair<Color, Color> = renderer.dividerColors()

        fun runningIndicatorDash(
            section: AiCommitAllControlSection,
        ): RunningIndicatorDash = renderer.runningIndicatorDash(
            this@AiCommitAllThreeSectionControl,
            section,
        )

        fun cornerArc(): Float = geometry.buttonArc()

        fun setSnakeOffset(offset: Float) {
            model.snakeOffset = offset
        }
    }
}
