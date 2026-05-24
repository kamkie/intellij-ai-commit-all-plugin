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
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageOutputStream
import kotlin.test.Test
import kotlin.test.assertTrue

internal class AiCommitAllControlAssetGeneratorTest {
    @Test
    fun `generate user guide visual assets from actual control`() {
        assumeTrue(
            System.getProperty("aicommitall.generateUserGuideAssets") == "true" ||
                System.getenv("AICOMMITALL_GENERATE_USER_GUIDE_ASSETS") == "true",
            "Set AICOMMITALL_GENERATE_USER_GUIDE_ASSETS=true to refresh documentation assets.",
        )

        Files.createDirectories(assetDirectory)

        writePng(
            file = assetDirectory.resolve("ai-commit-all-control-light.png"),
            image = renderControl(dark = false, state = enabledState()),
        )
        writePng(
            file = assetDirectory.resolve("ai-commit-all-control-dark.png"),
            image = renderControl(dark = true, state = enabledState()),
        )
        writeGif(
            file = assetDirectory.resolve("ai-commit-all-control-running.gif"),
            frames = runningAnimationFrames(),
        )
    }

    private fun runningAnimationFrames(): List<BufferedImage> = (0 until GIF_FRAME_COUNT).map { frame ->
        renderControl(
            dark = false,
            state = enabledState(runningSection = AiCommitAllControlSection.Commit),
            snakeOffset = frame * GIF_FRAME_OFFSET_STEP,
        )
    }

    private fun renderControl(
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

    private fun writePng(
        file: Path,
        image: BufferedImage,
    ) {
        check(ImageIO.write(image, "png", file.toFile())) {
            "No ImageIO PNG writer is available."
        }
        assertImageFile(file)
    }

    private fun writeGif(
        file: Path,
        frames: List<BufferedImage>,
    ) {
        check(frames.isNotEmpty()) { "GIF animation needs at least one frame." }
        val writer = requireNotNull(ImageIO.getImageWritersBySuffix("gif").asSequence().firstOrNull()) {
            "No ImageIO GIF writer is available."
        }
        ImageIO.createImageOutputStream(file.toFile()).use { output ->
            writeGifSequence(writer, output, frames)
        }
        assertImageFile(file)
    }

    private fun writeGifSequence(
        writer: ImageWriter,
        output: ImageOutputStream,
        frames: List<BufferedImage>,
    ) {
        writer.output = output
        writer.prepareWriteSequence(null)
        try {
            frames.forEach { frame ->
                writer.writeToSequence(
                    IIOImage(frame, null, gifMetadata(writer, frame)),
                    writer.defaultWriteParam,
                )
            }
        } finally {
            writer.endWriteSequence()
            writer.dispose()
        }
    }

    private fun gifMetadata(
        writer: ImageWriter,
        frame: BufferedImage,
    ): IIOMetadata {
        val type = ImageTypeSpecifier.createFromBufferedImageType(frame.type)
        val metadata = writer.getDefaultImageMetadata(type, writer.defaultWriteParam)
        val root = metadata.getAsTree(GIF_METADATA_FORMAT) as IIOMetadataNode
        root.child("GraphicControlExtension").apply {
            setAttribute("disposalMethod", "none")
            setAttribute("userInputFlag", "FALSE")
            setAttribute("transparentColorFlag", "FALSE")
            setAttribute("delayTime", GIF_FRAME_DELAY_CENTISECONDS.toString())
            setAttribute("transparentColorIndex", "0")
        }
        val extensions = root.child("ApplicationExtensions")
        val loop = IIOMetadataNode("ApplicationExtension").apply {
            setAttribute("applicationID", "NETSCAPE")
            setAttribute("authenticationCode", "2.0")
            userObject = byteArrayOf(1, 0, 0)
        }
        extensions.appendChild(loop)
        metadata.setFromTree(GIF_METADATA_FORMAT, root)
        return metadata
    }

    private fun IIOMetadataNode.child(name: String): IIOMetadataNode {
        for (index in 0 until length) {
            val node = item(index)
            if (node.nodeName == name) {
                return node as IIOMetadataNode
            }
        }
        return IIOMetadataNode(name).also(::appendChild)
    }

    private fun assertImageFile(file: Path) {
        assertTrue(Files.isRegularFile(file), "Missing generated asset: $file")
        assertTrue(Files.size(file) > 0, "Generated asset is empty: $file")
    }

    private fun enabledState(
        runningSection: AiCommitAllControlSection? = null,
    ): AiCommitAllControlState = AiCommitAllControlState(
        sections = AiCommitAllControlSection.entries.associateWith {
            AiCommitAllWorkflowActionAvailability.Enabled
        },
        runningSection = runningSection,
    )

    private fun AiCommitAllThreeSectionControl.setSnakeOffsetForAsset(offset: Float) {
        val field = AiCommitAllThreeSectionControl::class.java.getDeclaredField("snakeOffset")
        field.isAccessible = true
        field.setFloat(this, offset)
    }

    private fun BufferedImage.hasNonblankContent(): Boolean {
        val colors = mutableSetOf<Int>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                colors += getRGB(x, y)
            }
        }
        return colors.size > 1
    }

    private fun <T> withDarkMode(
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

    private companion object {
        private const val ASSET_PADDING = 12
        private const val GIF_FRAME_COUNT = 12
        private const val GIF_FRAME_DELAY_CENTISECONDS = 7
        private const val GIF_FRAME_OFFSET_STEP = 3.5f
        private const val GIF_METADATA_FORMAT = "javax_imageio_gif_image_1.0"
        private val assetDirectory = Path.of("docs", "assets", "user-guide").toAbsolutePath()
        private val LIGHT_BACKGROUND = Color(0xF7F8FA)
        private val DARK_BACKGROUND = Color(0x2B2D30)
    }
}
