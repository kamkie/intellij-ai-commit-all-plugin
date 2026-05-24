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

import com.intellij.ui.JBColor
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

internal data class ImageDimensions(
    val width: Int,
    val height: Int,
)

internal val BufferedImage.dimensions: ImageDimensions
    get() = ImageDimensions(width, height)

internal fun BufferedImage.hasNonblankContent(): Boolean {
    val colors = mutableSetOf<Int>()
    for (x in 0 until width) {
        for (y in 0 until height) {
            colors += getRGB(x, y)
        }
    }
    return colors.size > 1
}

internal fun Graphics2D.enableQualityRendering() {
    setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
}

internal fun <T> withDarkMode(
    dark: Boolean,
    block: () -> T,
): T {
    val previousDarkMode = !JBColor.isBright()
    JBColor.setDark(dark)
    return try {
        block()
    } finally {
        JBColor.setDark(previousDarkMode)
    }
}
