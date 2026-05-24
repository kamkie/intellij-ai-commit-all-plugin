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

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

internal object AiCommitAllControlAssetRenderer {
    fun renderControl(
        dark: Boolean,
        state: AiCommitAllControlState,
        snakeOffset: Float = 0f,
    ): BufferedImage = withDarkMode(dark) {
        val control = AiCommitAllThreeSectionControl { _, _ -> }.apply {
            setSize(preferredSize)
            updateState(state)
            setSnakeOffsetForAsset(snakeOffset)
        }
        val image = BufferedImage(
            control.width + ASSET_PADDING * 2,
            control.height + ASSET_PADDING * 2,
            BufferedImage.TYPE_INT_RGB,
        )
        val graphics = image.createGraphics() as Graphics2D
        try {
            graphics.color = if (dark) DARK_BACKGROUND else LIGHT_BACKGROUND
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.translate(ASSET_PADDING, ASSET_PADDING)
            control.paint(graphics)
        } finally {
            graphics.dispose()
        }
        check(image.hasNonblankContent()) {
            "Generated AI Commit All control asset is blank for dark=$dark."
        }
        image
    }

    fun runningAnimationFrames(): List<BufferedImage> = (0 until GIF_FRAME_COUNT).map { frame ->
        renderControl(
            dark = false,
            state = enabledState(runningSection = AiCommitAllControlSection.Commit),
            snakeOffset = frame * GIF_FRAME_OFFSET_STEP,
        )
    }

    fun enabledState(
        runningSection: AiCommitAllControlSection? = null,
    ): AiCommitAllControlState = AiCommitAllControlState(
        sections = AiCommitAllControlSection.entries.associateWith {
            AiCommitAllWorkflowActionAvailability.Enabled
        },
        runningSection = runningSection,
    )

    private fun AiCommitAllThreeSectionControl.setSnakeOffsetForAsset(offset: Float) {
        testPeerForTest.setSnakeOffset(offset)
    }

    private const val ASSET_PADDING = 12
    private const val GIF_FRAME_COUNT = 12
    private const val GIF_FRAME_OFFSET_STEP = 3.5f
    private val LIGHT_BACKGROUND = Color(0xF7F8FA)
    private val DARK_BACKGROUND = Color(0x2B2D30)
}
