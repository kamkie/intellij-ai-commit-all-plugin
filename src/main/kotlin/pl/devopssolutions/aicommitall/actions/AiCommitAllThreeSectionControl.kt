package pl.devopssolutions.aicommitall.actions

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import javax.accessibility.AccessibleContext
import javax.swing.*
import javax.swing.event.MouseInputAdapter
import kotlin.math.max

internal class AiCommitAllThreeSectionControl(
    private val activateSection: (AiCommitAllControlSection, InputEvent?) -> Unit,
) : JComponent() {
    private var state: AiCommitAllControlState = AiCommitAllControlState.Hidden
    private var hoverSection: AiCommitAllControlSection? = null
    private var keyboardSection: AiCommitAllControlSection = AiCommitAllControlSection.Commit
    private var snakeOffset = 0f
    private val snakeTimer = Timer(SNAKE_FRAME_DELAY_MS) {
        snakeOffset = (snakeOffset + JBUI.scale(8)) % JBUI.scale(180)
        repaint()
    }

    init {
        isOpaque = false
        isFocusable = true
        border = JBUI.Borders.empty()
        cursor = Cursor.getDefaultCursor()
        toolTipText = ""
        ToolTipManager.sharedInstance().registerComponent(this)
        installMouseHandling()
        installKeyboardHandling()
    }

    override fun getAccessibleContext(): AccessibleContext {
        if (accessibleContext == null) {
            accessibleContext = object : AccessibleJComponent() {
                override fun getAccessibleName(): String =
                    super.getAccessibleName()?.takeIf { it.isNotBlank() } ?: "AI Commit All"

                override fun getAccessibleDescription(): String =
                    super.getAccessibleDescription()?.takeIf { it.isNotBlank() } ?: "AI, Commit, and Push sections"
            }
        }
        return accessibleContext
    }

    override fun getPreferredSize(): Dimension =
        Dimension(JBUI.scale(CONTROL_WIDTH), JBUI.scale(CONTROL_HEIGHT))

    override fun getMinimumSize(): Dimension = preferredSize

    override fun getToolTipText(event: MouseEvent): String? =
        sectionAt(event.point)?.toolTipText

    fun updateState(nextState: AiCommitAllControlState) {
        state = nextState
        isVisible = nextState.visible
        isEnabled = nextState.enabled
        val currentHover = hoverSection
        if (currentHover != null && !state.isSectionEnabled(currentHover)) {
            hoverSection = null
        }
        if (!state.isSectionEnabled(keyboardSection)) {
            keyboardSection = firstEnabledSection() ?: AiCommitAllControlSection.Commit
        }
        updateAnimationState()
        repaint()
    }

    override fun paintComponent(graphics: Graphics) {
        val graphics2D = graphics.create() as Graphics2D
        try {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            paintControl(graphics2D)
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

    internal fun sectionLabels(): List<String> =
        AiCommitAllControlSection.entries.map { section -> section.label }

    internal fun isSectionEnabledForTest(section: AiCommitAllControlSection): Boolean =
        state.isSectionEnabled(section)

    internal fun setHoverSectionForTest(section: AiCommitAllControlSection?) {
        hoverSection = section
    }

    internal fun highlightedSectionsForTest(): Set<AiCommitAllControlSection> =
        highlightedSections()

    internal fun dividerColorsForTest(): Pair<Color, Color> {
        val highlighted = highlightedSections()
        return Pair(
            dividerColor(AiCommitAllControlSection.Ai, AiCommitAllControlSection.Commit, highlighted),
            dividerColor(AiCommitAllControlSection.Commit, AiCommitAllControlSection.Push, highlighted),
        )
    }

    private fun paintControl(graphics: Graphics2D) {
        val bounds = controlBounds()
        val sectionBounds = sectionBounds(bounds)
        val highlighted = highlightedSections()
        val runningSection = state.runningSection
        graphics.font = controlFont()

        AiCommitAllControlSection.entries.forEach { section ->
            val sectionRectangle = sectionBounds.getValue(section)
            graphics.color = sectionFill(section, highlighted.contains(section))
            graphics.fill(sectionShape(sectionRectangle, section))
        }

        paintDividers(graphics, sectionBounds, highlighted)
        paintLabels(graphics, sectionBounds, highlighted)
        if (runningSection != null) {
            paintSnake(graphics, sectionBounds.getValue(runningSection), runningSection)
        }
        graphics.color = ControlColors.border
        graphics.stroke = BasicStroke(JBUI.scale(1).toFloat())
        graphics.draw(RoundRectangle2D.Float(
            bounds.x + 0.5f,
            bounds.y + 0.5f,
            bounds.width - 1f,
            bounds.height - 1f,
            JBUI.scale(CORNER_RADIUS).toFloat(),
            JBUI.scale(CORNER_RADIUS).toFloat(),
        ))
    }

    private fun controlFont(): Font =
        (font ?: UIManager.getFont("Button.font") ?: Font(Font.SANS_SERIF, Font.BOLD, JBUI.scale(12)))
            .deriveFont(Font.BOLD, JBUI.scale(12).toFloat())

    private fun paintDividers(
        graphics: Graphics2D,
        sectionBounds: Map<AiCommitAllControlSection, Rectangle>,
        highlighted: Set<AiCommitAllControlSection>,
    ) {
        val aiBounds = sectionBounds.getValue(AiCommitAllControlSection.Ai)
        val commitBounds = sectionBounds.getValue(AiCommitAllControlSection.Commit)
        val bounds = controlBounds()
        graphics.stroke = BasicStroke(JBUI.scale(1).toFloat())
        graphics.color = dividerColor(AiCommitAllControlSection.Ai, AiCommitAllControlSection.Commit, highlighted)
        graphics.drawLine(aiBounds.right, bounds.y, aiBounds.right, bounds.y + bounds.height)
        graphics.color = dividerColor(AiCommitAllControlSection.Commit, AiCommitAllControlSection.Push, highlighted)
        graphics.drawLine(commitBounds.right, bounds.y, commitBounds.right, bounds.y + bounds.height)
    }

    private fun paintLabels(
        graphics: Graphics2D,
        sectionBounds: Map<AiCommitAllControlSection, Rectangle>,
        highlighted: Set<AiCommitAllControlSection>,
    ) {
        val metrics = graphics.fontMetrics
        val baseline = (height - metrics.height) / 2 + metrics.ascent
        val aiBounds = sectionBounds.getValue(AiCommitAllControlSection.Ai)
        val commitBounds = sectionBounds.getValue(AiCommitAllControlSection.Commit)
        val pushBounds = sectionBounds.getValue(AiCommitAllControlSection.Push)

        val aiColor = sectionForeground(AiCommitAllControlSection.Ai, highlighted.contains(AiCommitAllControlSection.Ai))
        paintAiMark(graphics, aiBounds.x + JBUI.scale(8), centerIconY(AI_ICON_SIZE), JBUI.scale(AI_ICON_SIZE), aiColor)
        graphics.color = aiColor
        graphics.drawString(AiCommitAllControlSection.Ai.label, aiBounds.x + JBUI.scale(27), baseline)

        graphics.color = sectionForeground(
            AiCommitAllControlSection.Commit,
            highlighted.contains(AiCommitAllControlSection.Commit),
        )
        graphics.drawString(
            AiCommitAllControlSection.Commit.label,
            commitBounds.x + (commitBounds.width - metrics.stringWidth(AiCommitAllControlSection.Commit.label)) / 2,
            baseline,
        )

        val pushHighlighted = highlighted.contains(AiCommitAllControlSection.Push)
        val pushColor = sectionForeground(AiCommitAllControlSection.Push, pushHighlighted)
        graphics.color = pushColor
        graphics.drawString(AiCommitAllControlSection.Push.label, pushBounds.x + JBUI.scale(10), baseline)
        paintPushMark(
            graphics = graphics,
            x = pushBounds.x + pushBounds.width - JBUI.scale(28),
            y = centerIconY(PUSH_ICON_SIZE),
            size = JBUI.scale(PUSH_ICON_SIZE),
            color = if (pushHighlighted) ControlColors.pushIconHighlighted else pushColor,
        )
    }

    private fun paintSnake(
        graphics: Graphics2D,
        rectangle: Rectangle,
        section: AiCommitAllControlSection,
    ) {
        val inset = JBUI.scale(2)
        val strokeWidth = JBUI.scale(2).toFloat()
        val snakeBounds = Rectangle(
            rectangle.x + inset,
            rectangle.y + inset,
            rectangle.width - inset * 2,
            rectangle.height - inset * 2,
        )
        graphics.color = when (section) {
            AiCommitAllControlSection.Push -> ControlColors.pushSnake
            else -> ControlColors.aiCommitSnake
        }
        val dashLength = JBUI.scale(18).toFloat()
        val gapLength = max(JBUI.scale(116).toFloat(), snakeBounds.width * 2f)
        val dashPhase = positiveReversePhase(
            offset = snakeOffset,
            cycleLength = dashLength + gapLength,
        )
        graphics.stroke = BasicStroke(
            strokeWidth,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            JBUI.scale(10).toFloat(),
            floatArrayOf(dashLength, gapLength),
            dashPhase,
        )
        graphics.draw(RoundRectangle2D.Float(
            snakeBounds.x.toFloat(),
            snakeBounds.y.toFloat(),
            snakeBounds.width.toFloat(),
            snakeBounds.height.toFloat(),
            JBUI.scale(3).toFloat(),
            JBUI.scale(3).toFloat(),
        ))
    }

    private fun paintAiMark(
        graphics: Graphics2D,
        x: Int,
        y: Int,
        size: Int,
        color: Color,
    ) {
        val scale = size / 22.0
        val path = Path2D.Double()
        val points = listOf(
            11.0 to 1.5,
            13.7 to 7.6,
            20.0 to 10.6,
            13.7 to 13.6,
            11.0 to 20.0,
            8.3 to 13.6,
            2.0 to 10.6,
            8.3 to 7.6,
        )
        points.forEachIndexed { index, point ->
            val pointX = x + point.first * scale
            val pointY = y + point.second * scale
            if (index == 0) {
                path.moveTo(pointX, pointY)
            } else {
                path.lineTo(pointX, pointY)
            }
        }
        path.closePath()
        graphics.color = color
        graphics.fill(path)
    }

    private fun paintPushMark(
        graphics: Graphics2D,
        x: Int,
        y: Int,
        size: Int,
        color: Color,
    ) {
        val scale = size / 18f
        graphics.color = color
        graphics.stroke = BasicStroke(JBUI.scale(17) / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        graphics.drawLine(x + (9 * scale).toInt(), y + (3 * scale).toInt(), x + (9 * scale).toInt(), y + (12 * scale).toInt())
        val arrow = Path2D.Float()
        arrow.moveTo(x + 5 * scale, y + 7 * scale)
        arrow.lineTo(x + 9 * scale, y + 3 * scale)
        arrow.lineTo(x + 13 * scale, y + 7 * scale)
        graphics.draw(arrow)
        graphics.drawLine(x + (4 * scale).toInt(), y + (15 * scale).toInt(), x + (14 * scale).toInt(), y + (15 * scale).toInt())
    }

    private fun sectionFill(
        section: AiCommitAllControlSection,
        highlighted: Boolean,
    ): Color =
        if (!state.isSectionEnabled(section) && state.runningSection == null) {
            ControlColors.disabledFill(section)
        } else if (highlighted) {
            ControlColors.activeFill(section)
        } else {
            ControlColors.passiveFill(section)
        }

    private fun sectionForeground(
        section: AiCommitAllControlSection,
        highlighted: Boolean,
    ): Color =
        if (!state.isSectionEnabled(section) && state.runningSection == null) {
            ControlColors.disabledForeground
        } else if (highlighted) {
            ControlColors.activeForeground
        } else {
            ControlColors.passiveForeground(section)
        }

    private fun dividerColor(
        leftSection: AiCommitAllControlSection,
        rightSection: AiCommitAllControlSection,
        highlighted: Set<AiCommitAllControlSection>,
    ): Color {
        if (state.runningSection == null &&
            (!state.isSectionEnabled(leftSection) || !state.isSectionEnabled(rightSection))
        ) {
            return ControlColors.disabledDivider
        }

        val leftHighlighted = highlighted.contains(leftSection)
        val rightHighlighted = highlighted.contains(rightSection)
        return when {
            leftHighlighted && rightHighlighted -> ControlColors.activeDivider
            leftHighlighted || rightHighlighted -> ControlColors.activePassiveDivider
            else -> ControlColors.passiveDivider
        }
    }

    private fun highlightedSections(): Set<AiCommitAllControlSection> {
        val activeSection = state.runningSection ?: hoverSection ?: return emptySet()
        return AiCommitAllControlSection.entries
            .filter { section -> section.ordinal <= activeSection.ordinal }
            .toSet()
    }

    private fun installMouseHandling() {
        val listener = object : MouseInputAdapter() {
            override fun mouseMoved(event: MouseEvent) {
                updateHover(sectionAt(event.point))
            }

            override fun mouseExited(event: MouseEvent) {
                updateHover(null)
            }

            override fun mousePressed(event: MouseEvent) {
                requestFocusInWindow()
            }

            override fun mouseClicked(event: MouseEvent) {
                val section = sectionAt(event.point)
                if (section != null && state.isSectionEnabled(section)) {
                    activateSection(section, event)
                }
            }
        }
        addMouseListener(listener)
        addMouseMotionListener(listener)
    }

    private fun installKeyboardHandling() {
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previousSection")
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextSection")
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "activateSection")
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "activateSection")
        actionMap.put("previousSection", object : AbstractAction() {
            override fun actionPerformed(event: java.awt.event.ActionEvent) {
                moveKeyboardSection(-1)
            }
        })
        actionMap.put("nextSection", object : AbstractAction() {
            override fun actionPerformed(event: java.awt.event.ActionEvent) {
                moveKeyboardSection(1)
            }
        })
        actionMap.put("activateSection", object : AbstractAction() {
            override fun actionPerformed(event: java.awt.event.ActionEvent) {
                if (state.isSectionEnabled(keyboardSection)) {
                    activateSection(keyboardSection, null)
                }
            }
        })
    }

    private fun moveKeyboardSection(direction: Int) {
        val enabledSections = AiCommitAllControlSection.entries
            .filter { section -> state.isSectionEnabled(section) }
        if (enabledSections.isEmpty()) {
            return
        }

        val currentIndex = enabledSections.indexOf(keyboardSection).takeIf { index -> index >= 0 } ?: 0
        val nextIndex = (currentIndex + direction).floorMod(enabledSections.size)
        keyboardSection = enabledSections[nextIndex]
        updateHover(keyboardSection)
    }

    private fun updateHover(section: AiCommitAllControlSection?) {
        val nextHover = section?.takeIf { candidate -> state.isSectionEnabled(candidate) }
        if (hoverSection != nextHover) {
            hoverSection = nextHover
            cursor = if (nextHover == null) Cursor.getDefaultCursor() else Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            repaint()
        }
    }

    private fun sectionAt(point: Point): AiCommitAllControlSection? =
        sectionBounds(controlBounds())
            .entries
            .firstOrNull { (_, bounds) -> bounds.contains(point) }
            ?.key

    private fun firstEnabledSection(): AiCommitAllControlSection? =
        AiCommitAllControlSection.entries.firstOrNull { section -> state.isSectionEnabled(section) }

    private fun updateAnimationState() {
        if (state.runningSection != null && isDisplayable) {
            if (!snakeTimer.isRunning) {
                snakeTimer.start()
            }
        } else {
            snakeTimer.stop()
            snakeOffset = 0f
        }
    }

    private fun controlBounds(): Rectangle =
        Rectangle(0, 0, width.takeIf { it > 0 } ?: preferredSize.width, height.takeIf { it > 0 } ?: preferredSize.height)

    private fun sectionBounds(bounds: Rectangle): Map<AiCommitAllControlSection, Rectangle> {
        val aiWidth = JBUI.scale(AI_SECTION_WIDTH)
        val commitWidth = JBUI.scale(COMMIT_SECTION_WIDTH)
        val pushWidth = max(JBUI.scale(PUSH_SECTION_WIDTH), bounds.width - aiWidth - commitWidth)
        return linkedMapOf(
            AiCommitAllControlSection.Ai to Rectangle(bounds.x, bounds.y, aiWidth, bounds.height),
            AiCommitAllControlSection.Commit to Rectangle(bounds.x + aiWidth, bounds.y, commitWidth, bounds.height),
            AiCommitAllControlSection.Push to Rectangle(bounds.x + aiWidth + commitWidth, bounds.y, pushWidth, bounds.height),
        )
    }

    private fun sectionShape(
        bounds: Rectangle,
        section: AiCommitAllControlSection,
    ): Shape {
        val radius = JBUI.scale(CORNER_RADIUS).toDouble()
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

    private fun centerIconY(iconSize: Int): Int =
        ((height.takeIf { it > 0 } ?: preferredSize.height) - JBUI.scale(iconSize)) / 2

    private fun Int.floorMod(other: Int): Int =
        ((this % other) + other) % other

    private fun positiveReversePhase(
        offset: Float,
        cycleLength: Float,
    ): Float {
        if (cycleLength <= 0f) {
            return 0f
        }

        val normalizedOffset = ((offset % cycleLength) + cycleLength) % cycleLength
        return if (normalizedOffset == 0f) {
            0f
        } else {
            cycleLength - normalizedOffset
        }
    }

    private val Rectangle.right: Int
        get() = x + width

    private val Rectangle.bottom: Int
        get() = y + height

    private object ControlColors {
        val border = JBColor(Color(0xD1D5DB), Color(0x4B5563))
        val activeDivider = JBColor(Color(0xE9, 0xF0, 0xFF, 110), Color(0xE9, 0xF0, 0xFF, 90))
        val activePassiveDivider = JBColor(Color(0xDD, 0xE8, 0xFF, 150), Color(0xDB, 0xEA, 0xFE, 90))
        val passiveDivider = JBColor(Color(0xCB, 0xD5, 0xE1, 190), Color(0x4B, 0x55, 0x63, 190))
        val disabledDivider = JBColor(Color(0xC3CBD8), Color(0x4B5563))
        val activeForeground = JBColor(Color.WHITE, Color.WHITE)
        val disabledForeground = JBColor(Color(0x6B7280), Color(0x8792A1))
        val aiCommitSnake = JBColor(Color(0xD9EAFF), Color(0xD9EAFF))
        val pushSnake = JBColor(Color(0xD9FFE3), Color(0xD9FFE3))
        val pushIconHighlighted = JBColor(Color(0xD9FFE3), Color(0xD9FFE3))

        fun passiveFill(section: AiCommitAllControlSection): Color =
            when (section) {
                AiCommitAllControlSection.Ai -> JBColor(Color(0xF0E9FF), Color(0x342A47))
                AiCommitAllControlSection.Commit -> JBColor(Color(0xEAF1FF), Color(0x28394E))
                AiCommitAllControlSection.Push -> JBColor(Color(0xE8F5EC), Color(0x263C32))
            }

        fun activeFill(section: AiCommitAllControlSection): Color =
            when (section) {
                AiCommitAllControlSection.Ai -> JBColor(Color(0x834DF0), Color(0xA571E6))
                AiCommitAllControlSection.Commit -> JBColor(Color(0x315FAE), Color(0x2F5AA0))
                AiCommitAllControlSection.Push -> JBColor(Color(0x238449), Color(0x2E9D50))
            }

        fun disabledFill(section: AiCommitAllControlSection): Color =
            when (section) {
                AiCommitAllControlSection.Push -> JBColor(Color(0xDCEAE2), Color(0x2F3A35))
                else -> JBColor(Color(0xE4E8F0), Color(0x303641))
            }

        fun passiveForeground(section: AiCommitAllControlSection): Color =
            when (section) {
                AiCommitAllControlSection.Ai -> JBColor(Color(0x6F4BB8), Color(0xB99BE8))
                AiCommitAllControlSection.Commit -> JBColor(Color(0x315FAE), Color(0x9DB6E3))
                AiCommitAllControlSection.Push -> JBColor(Color(0x238449), Color(0x8AF0A1))
            }
    }

    companion object {
        private const val CONTROL_WIDTH = 190
        private const val CONTROL_HEIGHT = 30
        private const val CORNER_RADIUS = 4
        private const val AI_SECTION_WIDTH = 50
        private const val COMMIT_SECTION_WIDTH = 70
        private const val PUSH_SECTION_WIDTH = 70
        private const val AI_ICON_SIZE = 14
        private const val PUSH_ICON_SIZE = 18
        private const val SNAKE_FRAME_DELAY_MS = 80
    }
}

internal val AiCommitAllControlSection.toolTipText: String
    get() = when (this) {
        AiCommitAllControlSection.Ai -> "Generate an AI commit message for all Git changes."
        AiCommitAllControlSection.Commit -> "Generate an AI commit message and commit all Git changes."
        AiCommitAllControlSection.Push -> "Generate an AI commit message, commit all Git changes, and push."
    }
