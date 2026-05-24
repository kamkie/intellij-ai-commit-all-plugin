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
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent
import javax.swing.UIManager

internal class ThreeSectionControlRenderer(
    private val model: ThreeSectionControlModel,
    private val geometry: ThreeSectionControlGeometry,
) {
    fun paint(
        component: JComponent,
        graphics: Graphics2D,
    ) {
        val bounds = geometry.controlBounds(component)
        val sectionBounds = geometry.sectionBounds(bounds)
        val highlighted = model.highlightedSections()
        val runningSection = model.state.runningSection
        graphics.font = controlFont(component)

        controlSections.forEach { section ->
            val sectionRectangle = sectionBounds.getValue(section)
            graphics.color = ControlColors.sectionFill(model.state, section, highlighted.contains(section))
            graphics.fill(geometry.sectionShape(sectionRectangle, section))
        }

        paintDividers(graphics, bounds, sectionBounds, highlighted)
        paintLabels(component, graphics, sectionBounds, highlighted)
        if (runningSection != null) {
            paintSnake(graphics, sectionBounds.getValue(runningSection), runningSection)
        }
        graphics.color = ControlColors.border
        graphics.stroke = BasicStroke(JBUI.scale(CONTROL_BORDER_STROKE_WIDTH).toFloat())
        graphics.draw(
            RoundRectangle2D.Float(
                bounds.x + CONTROL_BORDER_PIXEL_OFFSET,
                bounds.y + CONTROL_BORDER_PIXEL_OFFSET,
                bounds.width - CONTROL_BORDER_SIZE_ADJUSTMENT,
                bounds.height - CONTROL_BORDER_SIZE_ADJUSTMENT,
                geometry.buttonArc(),
                geometry.buttonArc(),
            ),
        )
    }

    fun sectionAt(
        component: JComponent,
        point: Point,
    ): AiCommitAllControlSection? = geometry.sectionBounds(geometry.controlBounds(component))
        .entries
        .firstOrNull { (_, bounds) -> bounds.contains(point) }
        ?.key

    fun dividerColors(): Pair<Color, Color> {
        val highlighted = model.highlightedSections()
        return Pair(
            ControlColors.divider(
                state = model.state,
                leftSection = AiCommitAllControlSection.Ai,
                rightSection = AiCommitAllControlSection.Commit,
                highlighted = highlighted,
            ),
            ControlColors.divider(
                state = model.state,
                leftSection = AiCommitAllControlSection.Commit,
                rightSection = AiCommitAllControlSection.Push,
                highlighted = highlighted,
            ),
        )
    }

    fun runningIndicatorDash(
        component: JComponent,
        section: AiCommitAllControlSection,
    ): RunningIndicatorDash {
        val sectionRectangle = geometry.sectionBounds(geometry.controlBounds(component)).getValue(section)
        val snakeBounds = geometry.runningIndicatorBounds(sectionRectangle)
        return geometry.runningIndicatorDash(snakeBounds, model.snakeOffset)
    }

    private fun controlFont(component: JComponent): Font {
        val fallbackFont = Font(Font.SANS_SERIF, Font.BOLD, JBUI.scale(CONTROL_FONT_SIZE))
        val baseFont = component.font ?: UIManager.getFont(BUTTON_FONT_KEY) ?: fallbackFont
        return baseFont.deriveFont(Font.BOLD, JBUI.scale(CONTROL_FONT_SIZE).toFloat())
    }

    private fun paintDividers(
        graphics: Graphics2D,
        bounds: Rectangle,
        sectionBounds: Map<AiCommitAllControlSection, Rectangle>,
        highlighted: Set<AiCommitAllControlSection>,
    ) {
        val aiBounds = sectionBounds.getValue(AiCommitAllControlSection.Ai)
        val commitBounds = sectionBounds.getValue(AiCommitAllControlSection.Commit)
        graphics.stroke = BasicStroke(JBUI.scale(DIVIDER_STROKE_WIDTH).toFloat())
        graphics.color = ControlColors.divider(
            state = model.state,
            leftSection = AiCommitAllControlSection.Ai,
            rightSection = AiCommitAllControlSection.Commit,
            highlighted = highlighted,
        )
        graphics.drawLine(aiBounds.right, bounds.y, aiBounds.right, bounds.y + bounds.height)
        graphics.color = ControlColors.divider(
            state = model.state,
            leftSection = AiCommitAllControlSection.Commit,
            rightSection = AiCommitAllControlSection.Push,
            highlighted = highlighted,
        )
        graphics.drawLine(commitBounds.right, bounds.y, commitBounds.right, bounds.y + bounds.height)
    }

    private fun paintLabels(
        component: JComponent,
        graphics: Graphics2D,
        sectionBounds: Map<AiCommitAllControlSection, Rectangle>,
        highlighted: Set<AiCommitAllControlSection>,
    ) {
        val metrics = graphics.fontMetrics
        val baseline = (component.height - metrics.height) / BASELINE_CENTER_DIVISOR + metrics.ascent
        val aiBounds = sectionBounds.getValue(AiCommitAllControlSection.Ai)
        val commitBounds = sectionBounds.getValue(AiCommitAllControlSection.Commit)
        val pushBounds = sectionBounds.getValue(AiCommitAllControlSection.Push)

        val aiColor = ControlColors.sectionForeground(
            state = model.state,
            section = AiCommitAllControlSection.Ai,
            highlighted = highlighted.contains(AiCommitAllControlSection.Ai),
        )
        SectionIconPainter.paintAiMark(
            graphics = graphics,
            x = aiBounds.x + JBUI.scale(AI_ICON_X_OFFSET),
            y = geometry.centerIconY(component, AI_ICON_SIZE),
            size = JBUI.scale(AI_ICON_SIZE),
            color = aiColor,
        )
        graphics.color = aiColor
        graphics.drawString(AiCommitAllControlSection.Ai.label, aiBounds.x + JBUI.scale(AI_LABEL_X_OFFSET), baseline)

        graphics.color = ControlColors.sectionForeground(
            state = model.state,
            section = AiCommitAllControlSection.Commit,
            highlighted = highlighted.contains(AiCommitAllControlSection.Commit),
        )
        graphics.drawString(
            AiCommitAllControlSection.Commit.label,
            commitBounds.x + (commitBounds.width - metrics.stringWidth(AiCommitAllControlSection.Commit.label)) /
                LABEL_CENTER_DIVISOR,
            baseline,
        )

        val pushHighlighted = highlighted.contains(AiCommitAllControlSection.Push)
        val pushColor = ControlColors.sectionForeground(model.state, AiCommitAllControlSection.Push, pushHighlighted)
        graphics.color = pushColor
        graphics.drawString(
            AiCommitAllControlSection.Push.label,
            pushBounds.x + JBUI.scale(PUSH_LABEL_X_OFFSET),
            baseline,
        )
        SectionIconPainter.paintPushMark(
            graphics = graphics,
            x = pushBounds.x + pushBounds.width - JBUI.scale(PUSH_ICON_TRAILING_OFFSET),
            y = geometry.centerIconY(component, PUSH_ICON_SIZE),
            size = JBUI.scale(PUSH_ICON_SIZE),
            color = if (pushHighlighted) ControlColors.pushIconHighlighted else pushColor,
        )
    }

    private fun paintSnake(
        graphics: Graphics2D,
        rectangle: Rectangle,
        section: AiCommitAllControlSection,
    ) {
        val strokeWidth = JBUI.scale(RUNNING_INDICATOR_STROKE_WIDTH).toFloat()
        val snakeBounds = geometry.runningIndicatorBounds(rectangle)
        graphics.color = when (section) {
            AiCommitAllControlSection.Push -> ControlColors.pushSnake
            else -> ControlColors.aiCommitSnake
        }
        val dash = geometry.runningIndicatorDash(snakeBounds, model.snakeOffset)
        graphics.stroke = BasicStroke(
            strokeWidth,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            JBUI.scale(RUNNING_INDICATOR_MITER_LIMIT).toFloat(),
            floatArrayOf(dash.dashLength, dash.gapLength),
            dash.phase,
        )
        graphics.draw(
            RoundRectangle2D.Float(
                snakeBounds.x.toFloat(),
                snakeBounds.y.toFloat(),
                snakeBounds.width.toFloat(),
                snakeBounds.height.toFloat(),
                JBUI.scale(RUNNING_INDICATOR_CORNER_ARC).toFloat(),
                JBUI.scale(RUNNING_INDICATOR_CORNER_ARC).toFloat(),
            ),
        )
    }
}
