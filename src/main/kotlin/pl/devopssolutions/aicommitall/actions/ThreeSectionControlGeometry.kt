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
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.Path2D
import javax.swing.JComponent
import kotlin.math.max

internal class ThreeSectionControlGeometry {
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

internal data class RunningIndicatorDash(
    val dashLength: Float,
    val gapLength: Float,
    val phase: Float,
    val pathLength: Float,
) {
    val cycleLength: Float
        get() = dashLength + gapLength
}

internal val Rectangle.right: Int
    get() = x + width

private val Rectangle.edgeLength: Int
    get() = width + height

private fun positivePhase(
    offset: Float,
    cycleLength: Float,
): Float {
    if (cycleLength <= 0f) {
        return 0f
    }

    return ((offset % cycleLength) + cycleLength) % cycleLength
}
