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
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import javax.accessibility.AccessibleContext
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.Timer
import javax.swing.ToolTipManager
import javax.swing.UIManager
import javax.swing.event.MouseInputAdapter
import kotlin.math.max

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

private class ThreeSectionControlModel {
    var state: AiCommitAllControlState = AiCommitAllControlState.Hidden
    var hoverSection: AiCommitAllControlSection? = null
    var keyboardSection: AiCommitAllControlSection = AiCommitAllControlSection.Commit
    var snakeOffset: Float = 0f

    fun updateState(nextState: AiCommitAllControlState) {
        state = nextState
        val currentHover = hoverSection
        if (currentHover != null && !state.isSectionEnabled(currentHover)) {
            hoverSection = null
        }
        if (!state.isSectionEnabled(keyboardSection)) {
            keyboardSection = firstEnabledSection() ?: AiCommitAllControlSection.Commit
        }
    }

    fun highlightedSections(): Set<AiCommitAllControlSection> {
        val activeSection = state.runningSection ?: hoverSection ?: return emptySet()
        return controlSections
            .filter { section -> section.ordinal <= activeSection.ordinal }
            .toSet()
    }

    fun accessibleDescription(): String {
        state.runningSection?.let { section ->
            return "AI, Commit, and Push sections; ${section.label} is running"
        }
        val enabledSections = controlSections
            .filter { section -> state.isSectionEnabled(section) }
            .joinToString(", ") { section -> section.label }
        return if (enabledSections.isBlank()) {
            "AI, Commit, and Push sections; no sections are enabled"
        } else {
            "AI, Commit, and Push sections"
        }
    }

    private fun firstEnabledSection(): AiCommitAllControlSection? = controlSections.firstOrNull(state::isSectionEnabled)
}

private class ThreeSectionControlInteraction(
    private val component: JComponent,
    private val model: ThreeSectionControlModel,
    private val renderer: ThreeSectionControlRenderer,
    private val activateSection: (AiCommitAllControlSection, InputEvent?) -> Unit,
) {
    fun install() {
        installMouseHandling()
        installKeyboardHandling()
    }

    private fun installMouseHandling() {
        val listener = object : MouseInputAdapter() {
            override fun mouseMoved(event: MouseEvent) {
                updateHover(renderer.sectionAt(component, event.point))
            }

            override fun mouseExited(event: MouseEvent) {
                updateHover(null)
            }

            override fun mousePressed(event: MouseEvent) {
                component.requestFocusInWindow()
            }

            override fun mouseClicked(event: MouseEvent) {
                val section = renderer.sectionAt(component, event.point)
                if (section != null && model.state.isSectionEnabled(section)) {
                    activateSection(section, event)
                }
            }
        }
        component.addMouseListener(listener)
        component.addMouseMotionListener(listener)
    }

    private fun installKeyboardHandling() {
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, NO_KEY_MODIFIERS), PREVIOUS_SECTION_ACTION)
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, NO_KEY_MODIFIERS), NEXT_SECTION_ACTION)
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, NO_KEY_MODIFIERS), ACTIVATE_SECTION_ACTION)
        component.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, NO_KEY_MODIFIERS), ACTIVATE_SECTION_ACTION)
        component.actionMap.put(
            PREVIOUS_SECTION_ACTION,
            object : AbstractAction() {
                override fun actionPerformed(event: java.awt.event.ActionEvent) {
                    moveKeyboardSection(PREVIOUS_SECTION_DIRECTION)
                }
            },
        )
        component.actionMap.put(
            NEXT_SECTION_ACTION,
            object : AbstractAction() {
                override fun actionPerformed(event: java.awt.event.ActionEvent) {
                    moveKeyboardSection(NEXT_SECTION_DIRECTION)
                }
            },
        )
        component.actionMap.put(
            ACTIVATE_SECTION_ACTION,
            object : AbstractAction() {
                override fun actionPerformed(event: java.awt.event.ActionEvent) {
                    if (model.state.isSectionEnabled(model.keyboardSection)) {
                        activateSection(model.keyboardSection, null)
                    }
                }
            },
        )
    }

    private fun moveKeyboardSection(direction: Int) {
        val enabledSections = controlSections
            .filter { section -> model.state.isSectionEnabled(section) }
        if (enabledSections.isEmpty()) {
            return
        }

        val currentIndex = enabledSections.indexOf(model.keyboardSection).takeIf { index -> index >= 0 } ?: 0
        val nextIndex = (currentIndex + direction).floorMod(enabledSections.size)
        model.keyboardSection = enabledSections[nextIndex]
        updateHover(model.keyboardSection)
    }

    private fun updateHover(section: AiCommitAllControlSection?) {
        val nextHover = section?.takeIf { candidate -> model.state.isSectionEnabled(candidate) }
        if (model.hoverSection != nextHover) {
            model.hoverSection = nextHover
            component.cursor = if (nextHover == null) {
                Cursor.getDefaultCursor()
            } else {
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            }
            component.repaint()
        }
    }
}

private class ThreeSectionControlRenderer(
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

private class ThreeSectionControlGeometry {
    fun preferredSize(): Dimension = Dimension(JBUI.scale(CONTROL_WIDTH), JBUI.scale(CONTROL_HEIGHT))

    fun controlBounds(component: JComponent): Rectangle = Rectangle(
        CONTROL_BOUNDS_X,
        CONTROL_BOUNDS_Y,
        component.width.takeIf { it > 0 } ?: component.preferredSize.width,
        component.height.takeIf { it > 0 } ?: component.preferredSize.height,
    )

    fun sectionBounds(bounds: Rectangle): Map<AiCommitAllControlSection, Rectangle> {
        val aiWidth = JBUI.scale(AI_SECTION_WIDTH)
        val commitWidth = JBUI.scale(COMMIT_SECTION_WIDTH)
        val pushWidth = max(JBUI.scale(PUSH_SECTION_WIDTH), bounds.width - aiWidth - commitWidth)
        return linkedMapOf(
            AiCommitAllControlSection.Ai to Rectangle(bounds.x, bounds.y, aiWidth, bounds.height),
            AiCommitAllControlSection.Commit to Rectangle(bounds.x + aiWidth, bounds.y, commitWidth, bounds.height),
            AiCommitAllControlSection.Push to Rectangle(
                bounds.x + aiWidth + commitWidth,
                bounds.y,
                pushWidth,
                bounds.height,
            ),
        )
    }

    fun sectionShape(
        bounds: Rectangle,
        section: AiCommitAllControlSection,
    ): Shape {
        val radius = buttonArc().toDouble()
        return when (section) {
            AiCommitAllControlSection.Ai -> Path2D.Double().apply {
                moveTo(bounds.maxX, bounds.y.toDouble())
                lineTo(bounds.maxX, bounds.maxY)
                lineTo(bounds.x + radius, bounds.maxY)
                quadTo(bounds.x.toDouble(), bounds.maxY, bounds.x.toDouble(), bounds.maxY - radius)
                lineTo(bounds.x.toDouble(), bounds.y + radius)
                quadTo(bounds.x.toDouble(), bounds.y.toDouble(), bounds.x + radius, bounds.y.toDouble())
                closePath()
            }

            AiCommitAllControlSection.Commit -> bounds

            AiCommitAllControlSection.Push -> Path2D.Double().apply {
                moveTo(bounds.x.toDouble(), bounds.y.toDouble())
                lineTo(bounds.maxX - radius, bounds.y.toDouble())
                quadTo(bounds.maxX, bounds.y.toDouble(), bounds.maxX, bounds.y + radius)
                lineTo(bounds.maxX, bounds.maxY - radius)
                quadTo(bounds.maxX, bounds.maxY, bounds.maxX - radius, bounds.maxY)
                lineTo(bounds.x.toDouble(), bounds.maxY)
                closePath()
            }
        }
    }

    fun centerIconY(
        component: JComponent,
        iconSize: Int,
    ): Int = (
        (
            component.height.takeIf { it > 0 }
                ?: component.preferredSize.height
            ) - JBUI.scale(iconSize)
        ) / ICON_CENTER_DIVISOR

    fun buttonArc(): Float = DarculaUIUtil.BUTTON_ARC.getFloat()

    fun runningIndicatorBounds(rectangle: Rectangle): Rectangle {
        val inset = JBUI.scale(RUNNING_INDICATOR_INSET)
        return Rectangle(
            rectangle.x + inset,
            rectangle.y + inset,
            rectangle.width - inset * INSET_WIDTH_FACTOR,
            rectangle.height - inset * INSET_WIDTH_FACTOR,
        )
    }

    fun runningIndicatorDash(
        snakeBounds: Rectangle,
        snakeOffset: Float,
    ): RunningIndicatorDash {
        val dashLength = JBUI.scale(RUNNING_DASH_LENGTH).toFloat()
        val pathLength = runningPathLength(snakeBounds)
        val gapLength = max(JBUI.scale(RUNNING_MIN_GAP_LENGTH).toFloat(), pathLength + dashLength)
        val cycleLength = dashLength + gapLength
        return RunningIndicatorDash(
            dashLength = dashLength,
            gapLength = gapLength,
            phase = positivePhase(
                offset = snakeOffset,
                cycleLength = cycleLength,
            ),
            pathLength = pathLength,
        )
    }

    private fun runningPathLength(bounds: Rectangle): Float = RUNNING_PATH_PERIMETER_FACTOR * bounds.edgeLength
}

private object SectionIconPainter {
    fun paintAiMark(
        graphics: Graphics2D,
        x: Int,
        y: Int,
        size: Int,
        color: Color,
    ) {
        val scale = size / AI_MARK_VIEWBOX_SIZE
        val path = Path2D.Double()
        val points = listOf(
            AI_MARK_CENTER_X to AI_MARK_TOP_Y,
            AI_MARK_RIGHT_INNER_X to AI_MARK_UPPER_INNER_Y,
            AI_MARK_RIGHT_X to AI_MARK_MIDDLE_Y,
            AI_MARK_RIGHT_INNER_X to AI_MARK_LOWER_INNER_Y,
            AI_MARK_CENTER_X to AI_MARK_BOTTOM_Y,
            AI_MARK_LEFT_INNER_X to AI_MARK_LOWER_INNER_Y,
            AI_MARK_LEFT_X to AI_MARK_MIDDLE_Y,
            AI_MARK_LEFT_INNER_X to AI_MARK_UPPER_INNER_Y,
        )
        points.forEachIndexed { index, point ->
            val pointX = x + point.first * scale
            val pointY = y + point.second * scale
            if (index == FIRST_PATH_POINT_INDEX) {
                path.moveTo(pointX, pointY)
            } else {
                path.lineTo(pointX, pointY)
            }
        }
        path.closePath()
        graphics.color = color
        graphics.fill(path)
    }

    fun paintPushMark(
        graphics: Graphics2D,
        x: Int,
        y: Int,
        size: Int,
        color: Color,
    ) {
        val scale = size / PUSH_MARK_VIEWBOX_SIZE
        graphics.color = color
        graphics.stroke = BasicStroke(
            JBUI.scale(PUSH_MARK_STROKE_WIDTH_TENTHS) / PUSH_MARK_STROKE_TENTHS_DIVISOR,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
        )
        graphics.drawLine(
            x + (PUSH_MARK_CENTER_X * scale).toInt(),
            y + (PUSH_MARK_TOP_Y * scale).toInt(),
            x + (PUSH_MARK_CENTER_X * scale).toInt(),
            y + (PUSH_MARK_STEM_BOTTOM_Y * scale).toInt(),
        )
        val arrow = Path2D.Float()
        arrow.moveTo(x + PUSH_MARK_ARROW_LEFT_X * scale, y + PUSH_MARK_ARROW_Y * scale)
        arrow.lineTo(x + PUSH_MARK_CENTER_X * scale, y + PUSH_MARK_TOP_Y * scale)
        arrow.lineTo(x + PUSH_MARK_ARROW_RIGHT_X * scale, y + PUSH_MARK_ARROW_Y * scale)
        graphics.draw(arrow)
        graphics.drawLine(
            x + (PUSH_MARK_TRAY_LEFT_X * scale).toInt(),
            y + (PUSH_MARK_TRAY_Y * scale).toInt(),
            x + (PUSH_MARK_TRAY_RIGHT_X * scale).toInt(),
            y + (PUSH_MARK_TRAY_Y * scale).toInt(),
        )
    }
}

private object ControlColors {
    val border = color(ControlColorDefaults.BORDER_LIGHT, ControlColorDefaults.BORDER_DARK)
    val activeDivider = translucentColor(
        lightRgb = ControlColorDefaults.ACTIVE_DIVIDER_LIGHT,
        lightAlpha = ControlColorDefaults.ACTIVE_DIVIDER_LIGHT_ALPHA,
        darkRgb = ControlColorDefaults.ACTIVE_DIVIDER_DARK,
        darkAlpha = ControlColorDefaults.ACTIVE_DIVIDER_DARK_ALPHA,
    )
    val activePassiveDivider = translucentColor(
        lightRgb = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_LIGHT,
        lightAlpha = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_LIGHT_ALPHA,
        darkRgb = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_DARK,
        darkAlpha = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_DARK_ALPHA,
    )
    val passiveDivider = translucentColor(
        lightRgb = ControlColorDefaults.PASSIVE_DIVIDER_LIGHT,
        lightAlpha = ControlColorDefaults.PASSIVE_DIVIDER_LIGHT_ALPHA,
        darkRgb = ControlColorDefaults.PASSIVE_DIVIDER_DARK,
        darkAlpha = ControlColorDefaults.PASSIVE_DIVIDER_DARK_ALPHA,
    )
    val disabledDivider = color(ControlColorDefaults.DISABLED_DIVIDER_LIGHT, ControlColorDefaults.DISABLED_DIVIDER_DARK)
    val activeForeground = JBColor(Color.WHITE, Color.WHITE)
    val disabledForeground = color(
        ControlColorDefaults.DISABLED_FOREGROUND_LIGHT,
        ControlColorDefaults.DISABLED_FOREGROUND_DARK,
    )
    val aiCommitSnake = color(ControlColorDefaults.AI_COMMIT_SNAKE_LIGHT, ControlColorDefaults.AI_COMMIT_SNAKE_DARK)
    val pushSnake = color(ControlColorDefaults.PUSH_SNAKE_LIGHT, ControlColorDefaults.PUSH_SNAKE_DARK)
    val pushIconHighlighted = color(
        ControlColorDefaults.PUSH_ICON_HIGHLIGHTED_LIGHT,
        ControlColorDefaults.PUSH_ICON_HIGHLIGHTED_DARK,
    )

    fun sectionFill(
        state: AiCommitAllControlState,
        section: AiCommitAllControlSection,
        highlighted: Boolean,
    ): Color = if (!state.isSectionEnabled(section) && state.runningSection == null) {
        disabledFill(section)
    } else if (highlighted) {
        activeFill(section)
    } else {
        passiveFill(section)
    }

    fun sectionForeground(
        state: AiCommitAllControlState,
        section: AiCommitAllControlSection,
        highlighted: Boolean,
    ): Color = if (!state.isSectionEnabled(section) && state.runningSection == null) {
        disabledForeground
    } else if (highlighted) {
        activeForeground
    } else {
        passiveForeground(section)
    }

    fun divider(
        state: AiCommitAllControlState,
        leftSection: AiCommitAllControlSection,
        rightSection: AiCommitAllControlSection,
        highlighted: Set<AiCommitAllControlSection>,
    ): Color {
        if (state.runningSection == null &&
            (!state.isSectionEnabled(leftSection) || !state.isSectionEnabled(rightSection))
        ) {
            return disabledDivider
        }

        val leftHighlighted = highlighted.contains(leftSection)
        val rightHighlighted = highlighted.contains(rightSection)
        return when {
            leftHighlighted && rightHighlighted -> activeDivider
            leftHighlighted || rightHighlighted -> activePassiveDivider
            else -> passiveDivider
        }
    }

    private fun passiveFill(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Ai -> color(
            ControlColorDefaults.AI_PASSIVE_FILL_LIGHT,
            ControlColorDefaults.AI_PASSIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Commit -> color(
            ControlColorDefaults.COMMIT_PASSIVE_FILL_LIGHT,
            ControlColorDefaults.COMMIT_PASSIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_PASSIVE_FILL_LIGHT,
            ControlColorDefaults.PUSH_PASSIVE_FILL_DARK,
        )
    }

    private fun activeFill(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Ai -> color(
            ControlColorDefaults.AI_ACTIVE_FILL_LIGHT,
            ControlColorDefaults.AI_ACTIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Commit -> color(
            ControlColorDefaults.COMMIT_ACTIVE_FILL_LIGHT,
            ControlColorDefaults.COMMIT_ACTIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_ACTIVE_FILL_LIGHT,
            ControlColorDefaults.PUSH_ACTIVE_FILL_DARK,
        )
    }

    private fun disabledFill(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_DISABLED_FILL_LIGHT,
            ControlColorDefaults.PUSH_DISABLED_FILL_DARK,
        )

        else -> color(
            ControlColorDefaults.DEFAULT_DISABLED_FILL_LIGHT,
            ControlColorDefaults.DEFAULT_DISABLED_FILL_DARK,
        )
    }

    private fun passiveForeground(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Ai -> color(
            ControlColorDefaults.AI_PASSIVE_FOREGROUND_LIGHT,
            ControlColorDefaults.AI_PASSIVE_FOREGROUND_DARK,
        )

        AiCommitAllControlSection.Commit -> color(
            ControlColorDefaults.COMMIT_PASSIVE_FOREGROUND_LIGHT,
            ControlColorDefaults.COMMIT_PASSIVE_FOREGROUND_DARK,
        )

        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_PASSIVE_FOREGROUND_LIGHT,
            ControlColorDefaults.PUSH_PASSIVE_FOREGROUND_DARK,
        )
    }

    private fun color(
        lightRgb: Int,
        darkRgb: Int,
    ): Color = JBColor(Color(lightRgb), Color(darkRgb))

    private fun translucentColor(
        lightRgb: Int,
        lightAlpha: Int,
        darkRgb: Int,
        darkAlpha: Int,
    ): Color = JBColor(
        Color((lightAlpha shl COLOR_ALPHA_SHIFT) or lightRgb, true),
        Color((darkAlpha shl COLOR_ALPHA_SHIFT) or darkRgb, true),
    )
}

private object ControlColorDefaults {
    const val BORDER_LIGHT = 0xD1D5DB
    const val BORDER_DARK = 0x4B5563
    const val ACTIVE_DIVIDER_LIGHT = 0xE9F0FF
    const val ACTIVE_DIVIDER_LIGHT_ALPHA = 110
    const val ACTIVE_DIVIDER_DARK = 0xE9F0FF
    const val ACTIVE_DIVIDER_DARK_ALPHA = 90
    const val ACTIVE_PASSIVE_DIVIDER_LIGHT = 0xDDE8FF
    const val ACTIVE_PASSIVE_DIVIDER_LIGHT_ALPHA = 150
    const val ACTIVE_PASSIVE_DIVIDER_DARK = 0xDBEAFE
    const val ACTIVE_PASSIVE_DIVIDER_DARK_ALPHA = 90
    const val PASSIVE_DIVIDER_LIGHT = 0xCBD5E1
    const val PASSIVE_DIVIDER_LIGHT_ALPHA = 190
    const val PASSIVE_DIVIDER_DARK = 0x4B5563
    const val PASSIVE_DIVIDER_DARK_ALPHA = 190
    const val DISABLED_DIVIDER_LIGHT = 0xC3CBD8
    const val DISABLED_DIVIDER_DARK = 0x4B5563
    const val DISABLED_FOREGROUND_LIGHT = 0x6B7280
    const val DISABLED_FOREGROUND_DARK = 0x8792A1
    const val AI_COMMIT_SNAKE_LIGHT = 0xD9EAFF
    const val AI_COMMIT_SNAKE_DARK = 0xD9EAFF
    const val PUSH_SNAKE_LIGHT = 0xD9FFE3
    const val PUSH_SNAKE_DARK = 0xD9FFE3
    const val PUSH_ICON_HIGHLIGHTED_LIGHT = 0xD9FFE3
    const val PUSH_ICON_HIGHLIGHTED_DARK = 0xD9FFE3
    const val AI_PASSIVE_FILL_LIGHT = 0xF0E9FF
    const val AI_PASSIVE_FILL_DARK = 0x342A47
    const val COMMIT_PASSIVE_FILL_LIGHT = 0xEAF1FF
    const val COMMIT_PASSIVE_FILL_DARK = 0x28394E
    const val PUSH_PASSIVE_FILL_LIGHT = 0xE8F5EC
    const val PUSH_PASSIVE_FILL_DARK = 0x263C32
    const val AI_ACTIVE_FILL_LIGHT = 0x834DF0
    const val AI_ACTIVE_FILL_DARK = 0xA571E6
    const val COMMIT_ACTIVE_FILL_LIGHT = 0x315FAE
    const val COMMIT_ACTIVE_FILL_DARK = 0x2F5AA0
    const val PUSH_ACTIVE_FILL_LIGHT = 0x238449
    const val PUSH_ACTIVE_FILL_DARK = 0x2E9D50
    const val PUSH_DISABLED_FILL_LIGHT = 0xDCEAE2
    const val PUSH_DISABLED_FILL_DARK = 0x2F3A35
    const val DEFAULT_DISABLED_FILL_LIGHT = 0xE4E8F0
    const val DEFAULT_DISABLED_FILL_DARK = 0x303641
    const val AI_PASSIVE_FOREGROUND_LIGHT = 0x6F4BB8
    const val AI_PASSIVE_FOREGROUND_DARK = 0xB99BE8
    const val COMMIT_PASSIVE_FOREGROUND_LIGHT = 0x315FAE
    const val COMMIT_PASSIVE_FOREGROUND_DARK = 0x9DB6E3
    const val PUSH_PASSIVE_FOREGROUND_LIGHT = 0x238449
    const val PUSH_PASSIVE_FOREGROUND_DARK = 0x8AF0A1
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other

private fun positivePhase(
    offset: Float,
    cycleLength: Float,
): Float {
    if (cycleLength <= 0f) {
        return 0f
    }

    return ((offset % cycleLength) + cycleLength) % cycleLength
}

private val Rectangle.right: Int
    get() = x + width

private val Rectangle.edgeLength: Int
    get() = width + height

private val controlSections = AiCommitAllControlSection.entries

private const val CONTROL_WIDTH = 190
private const val CONTROL_HEIGHT = 30
private const val CONTROL_BOUNDS_X = 0
private const val CONTROL_BOUNDS_Y = 0
private const val AI_SECTION_WIDTH = 50
private const val COMMIT_SECTION_WIDTH = 70
private const val PUSH_SECTION_WIDTH = 70
private const val CONTROL_FONT_SIZE = 12
private const val BUTTON_FONT_KEY = "Button.font"
private const val NO_KEY_MODIFIERS = 0
private const val PREVIOUS_SECTION_ACTION = "previousSection"
private const val NEXT_SECTION_ACTION = "nextSection"
private const val ACTIVATE_SECTION_ACTION = "activateSection"
private const val PREVIOUS_SECTION_DIRECTION = -1
private const val NEXT_SECTION_DIRECTION = 1
private const val CONTROL_BORDER_STROKE_WIDTH = 1
private const val CONTROL_BORDER_PIXEL_OFFSET = 0.5f
private const val CONTROL_BORDER_SIZE_ADJUSTMENT = 1f
private const val DIVIDER_STROKE_WIDTH = 1
private const val BASELINE_CENTER_DIVISOR = 2
private const val LABEL_CENTER_DIVISOR = 2
private const val ICON_CENTER_DIVISOR = 2
private const val AI_ICON_SIZE = 14
private const val AI_ICON_X_OFFSET = 8
private const val AI_LABEL_X_OFFSET = 27
private const val PUSH_ICON_SIZE = 18
private const val PUSH_LABEL_X_OFFSET = 10
private const val PUSH_ICON_TRAILING_OFFSET = 28
private const val SNAKE_FRAME_DELAY_MS = 80
private const val SNAKE_FRAME_STEP = 8
private const val RUNNING_INDICATOR_STROKE_WIDTH = 2
private const val RUNNING_INDICATOR_MITER_LIMIT = 10
private const val RUNNING_INDICATOR_CORNER_ARC = 3
private const val RUNNING_INDICATOR_INSET = 2
private const val RUNNING_DASH_LENGTH = 18
private const val RUNNING_MIN_GAP_LENGTH = 116
private const val RUNNING_PATH_PERIMETER_FACTOR = 2f
private const val INSET_WIDTH_FACTOR = 2
private const val FIRST_PATH_POINT_INDEX = 0
private const val AI_MARK_VIEWBOX_SIZE = 22.0
private const val AI_MARK_CENTER_X = 11.0
private const val AI_MARK_TOP_Y = 1.5
private const val AI_MARK_RIGHT_INNER_X = 13.7
private const val AI_MARK_UPPER_INNER_Y = 7.6
private const val AI_MARK_RIGHT_X = 20.0
private const val AI_MARK_MIDDLE_Y = 10.6
private const val AI_MARK_LOWER_INNER_Y = 13.6
private const val AI_MARK_BOTTOM_Y = 20.0
private const val AI_MARK_LEFT_INNER_X = 8.3
private const val AI_MARK_LEFT_X = 2.0
private const val PUSH_MARK_VIEWBOX_SIZE = 18f
private const val PUSH_MARK_STROKE_WIDTH_TENTHS = 17
private const val PUSH_MARK_STROKE_TENTHS_DIVISOR = 10f
private const val PUSH_MARK_CENTER_X = 9f
private const val PUSH_MARK_TOP_Y = 3f
private const val PUSH_MARK_STEM_BOTTOM_Y = 12f
private const val PUSH_MARK_ARROW_LEFT_X = 5f
private const val PUSH_MARK_ARROW_Y = 7f
private const val PUSH_MARK_ARROW_RIGHT_X = 13f
private const val PUSH_MARK_TRAY_LEFT_X = 4f
private const val PUSH_MARK_TRAY_Y = 15f
private const val PUSH_MARK_TRAY_RIGHT_X = 14f
private const val COLOR_ALPHA_SHIFT = 24

internal data class RunningIndicatorDash(
    val dashLength: Float,
    val gapLength: Float,
    val phase: Float,
    val pathLength: Float,
) {
    val cycleLength: Float
        get() = dashLength + gapLength
}

internal val AiCommitAllControlSection.toolTipText: String
    get() = when (this) {
        AiCommitAllControlSection.Ai -> "Generate an AI commit message for all Git changes."
        AiCommitAllControlSection.Commit -> "Generate an AI commit message and commit all Git changes."
        AiCommitAllControlSection.Push -> "Generate an AI commit message, commit all Git changes, and push."
    }
