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
import java.awt.Graphics2D
import java.awt.geom.Path2D

internal object SectionIconPainter {
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
