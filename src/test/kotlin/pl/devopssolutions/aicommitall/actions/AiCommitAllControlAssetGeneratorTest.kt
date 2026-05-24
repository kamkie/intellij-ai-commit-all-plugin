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
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
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
    fun `generate documentation visual assets from actual control`() {
        assumeTrue(
            System.getProperty("aicommitall.generateUserGuideAssets") == "true" ||
                System.getenv("AICOMMITALL_GENERATE_USER_GUIDE_ASSETS") == "true",
            "Set AICOMMITALL_GENERATE_USER_GUIDE_ASSETS=true to refresh documentation assets.",
        )

        Files.createDirectories(assetDirectory)
        Files.createDirectories(marketplaceAssetDirectory)

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
        writeGif(
            file = marketplaceAssetDirectory.resolve("ai-commit-all-realtime-progress.gif"),
            frames = marketplaceAnimationFrames(),
            frameDelayCentiseconds = MARKETPLACE_GIF_FRAME_DELAY_CENTISECONDS,
        )
        writePng(
            file = marketplaceAssetDirectory.resolve("ai-commit-all-realtime-progress.png"),
            image = renderMarketplaceFrame(
                step = marketplaceStepBySection.getValue(AiCommitAllControlSection.Push),
                snakeOffset = 0f,
            ),
        )
    }

    private fun runningAnimationFrames(): List<BufferedImage> = (0 until GIF_FRAME_COUNT).map { frame ->
        renderControl(
            dark = false,
            state = enabledState(runningSection = AiCommitAllControlSection.Commit),
            snakeOffset = frame * GIF_FRAME_OFFSET_STEP,
        )
    }

    private fun marketplaceAnimationFrames(): List<BufferedImage> = buildList {
        add(renderMarketplaceFrame(marketplaceReadyStep, snakeOffset = 0f))
        addProgressFrames(marketplaceStepBySection.getValue(AiCommitAllControlSection.Ai))
        addProgressFrames(marketplaceStepBySection.getValue(AiCommitAllControlSection.Commit))
        addProgressFrames(marketplaceStepBySection.getValue(AiCommitAllControlSection.Push))
        add(renderMarketplaceFrame(marketplaceDoneStep, snakeOffset = 0f))
    }

    private fun MutableList<BufferedImage>.addProgressFrames(step: MarketplaceStep) {
        repeat(MARKETPLACE_PHASE_FRAME_COUNT) { frame ->
            add(
                renderMarketplaceFrame(
                    step = step,
                    snakeOffset = frame * MARKETPLACE_GIF_FRAME_OFFSET_STEP,
                ),
            )
        }
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

    private fun renderMarketplaceFrame(
        step: MarketplaceStep,
        snakeOffset: Float,
    ): BufferedImage = withDarkMode(false) {
        val image = BufferedImage(
            MARKETPLACE_WIDTH,
            MARKETPLACE_HEIGHT,
            BufferedImage.TYPE_INT_RGB,
        )
        val graphics = image.createGraphics() as Graphics2D
        try {
            graphics.enableQualityRendering()
            graphics.color = MARKETPLACE_BACKGROUND
            graphics.fillRect(0, 0, image.width, image.height)
            drawMarketplaceHeader(graphics)
            drawIdePreview(graphics, step, snakeOffset)
            drawProgressSequence(graphics, step.runningSection, snakeOffset)
        } finally {
            graphics.dispose()
        }
        check(image.hasNonblankContent()) {
            "Generated AI Commit All marketplace asset is blank."
        }
        image
    }

    private fun drawMarketplaceHeader(graphics: Graphics2D) {
        graphics.color = TEXT_PRIMARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 30)
        graphics.drawString("AI Commit All real-time progress", 66, 48)

        graphics.color = TEXT_SECONDARY
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, 17)
        graphics.drawString(
            "One Commit tool window control moves through AI -> Commit -> Push while the workflow runs.",
            68,
            78,
        )
    }

    private fun drawIdePreview(
        graphics: Graphics2D,
        step: MarketplaceStep,
        snakeOffset: Float,
    ) {
        graphics.color = CARD_SHADOW
        graphics.fillRoundRect(62, 100, 1078, 522, 28, 28)
        graphics.color = IDE_BACKGROUND
        graphics.fillRoundRect(58, 96, 1078, 522, 28, 28)

        drawIdeTitleBar(graphics)
        drawProjectPane(graphics)
        drawEditorPane(graphics)
        drawCommitPane(graphics, step, snakeOffset)
    }

    private fun drawIdeTitleBar(graphics: Graphics2D) {
        graphics.color = IDE_HEADER
        graphics.fillRoundRect(58, 96, 1078, 44, 28, 28)
        graphics.fillRect(58, 120, 1078, 20)
        graphics.color = Color(0xF87171)
        graphics.fillOval(82, 113, 12, 12)
        graphics.color = Color(0xFBBF24)
        graphics.fillOval(104, 113, 12, 12)
        graphics.color = Color(0x34D399)
        graphics.fillOval(126, 113, 12, 12)
        graphics.color = Color(0xE5E7EB)
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 14)
        graphics.drawString("AI Commit All / Commit", 168, 124)
    }

    private fun drawProjectPane(graphics: Graphics2D) {
        graphics.color = IDE_PANEL
        graphics.fillRoundRect(78, 158, 242, 436, 14, 14)
        graphics.color = TEXT_PRIMARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 15)
        graphics.drawString("Project", 98, 188)
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, 14)
        listOf(
            "src/main/kotlin",
            "  workflow/CommitWorkflow.kt",
            "  workflow/PushWorkflow.kt",
            "docs/user-guide.md",
            "README.md",
        ).forEachIndexed { index, line ->
            graphics.color = if (index in 1..2) ACCENT_BLUE else TEXT_SECONDARY
            graphics.drawString(line, 98, 222 + index * 30)
        }
    }

    private fun drawEditorPane(graphics: Graphics2D) {
        graphics.color = Color.WHITE
        graphics.fillRoundRect(340, 158, 414, 436, 14, 14)
        graphics.color = BORDER
        graphics.stroke = BasicStroke(1.2f)
        graphics.drawRoundRect(340, 158, 414, 436, 14, 14)
        graphics.color = TEXT_PRIMARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 15)
        graphics.drawString("Diff Preview", 362, 188)

        val rows = listOf(
            "+ Add Marketplace real-time progress copy",
            "+ Generate workflow GIF and PNG media",
            "+ Record media upload release validation",
            "- Link only small control assets",
        )
        graphics.font = Font(Font.MONOSPACED, Font.PLAIN, 15)
        rows.forEachIndexed { index, row ->
            val y = 230 + index * 44
            graphics.color = if (row.startsWith("+")) DIFF_ADDED_BACKGROUND else DIFF_REMOVED_BACKGROUND
            graphics.fillRoundRect(362, y - 22, 360, 32, 8, 8)
            graphics.color = if (row.startsWith("+")) DIFF_ADDED_TEXT else DIFF_REMOVED_TEXT
            graphics.drawString(row, 376, y)
        }
    }

    private fun drawCommitPane(
        graphics: Graphics2D,
        step: MarketplaceStep,
        snakeOffset: Float,
    ) {
        graphics.color = IDE_PANEL
        graphics.fillRoundRect(774, 158, 338, 436, 14, 14)
        graphics.color = TEXT_PRIMARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 15)
        graphics.drawString("Commit", 798, 188)

        val control = renderControl(
            dark = false,
            state = enabledState(runningSection = step.runningSection),
            snakeOffset = snakeOffset,
        )
        graphics.drawImage(control, 834, 205, null)

        drawChangedFileList(graphics)
        drawCommitMessage(graphics, step)
        drawPhaseStatus(graphics, step)
    }

    private fun drawChangedFileList(graphics: Graphics2D) {
        graphics.color = TEXT_SECONDARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 13)
        graphics.drawString("Included changes", 798, 292)
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        listOf(
            "docs/user-guide.md",
            "config/intellij-platform/description.html",
            "docs/assets/marketplace/*",
        ).forEachIndexed { index, row ->
            graphics.color = TEXT_SECONDARY
            graphics.drawString(row, 812, 322 + index * 24)
        }
    }

    private fun drawCommitMessage(
        graphics: Graphics2D,
        step: MarketplaceStep,
    ) {
        graphics.color = Color.WHITE
        graphics.fillRoundRect(798, 400, 286, 80, 10, 10)
        graphics.color = BORDER
        graphics.drawRoundRect(798, 400, 286, 80, 10, 10)
        graphics.color = TEXT_SECONDARY
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        graphics.drawString("Commit message", 814, 424)
        graphics.color = TEXT_PRIMARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 14)
        graphics.drawString(step.commitMessage, 814, 452)
    }

    private fun drawPhaseStatus(
        graphics: Graphics2D,
        step: MarketplaceStep,
    ) {
        graphics.color = TEXT_PRIMARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        graphics.drawString(step.title, 798, 516)
        graphics.color = TEXT_SECONDARY
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, 13)
        graphics.drawString(step.description, 798, 540)

        graphics.color = Color(0xD1D5DB)
        graphics.fillRoundRect(798, 558, 286, 8, 8, 8)
        graphics.color = step.color
        graphics.fillRoundRect(798, 558, (286 * step.progress).toInt(), 8, 8, 8)
    }

    private fun drawProgressSequence(
        graphics: Graphics2D,
        activeSection: AiCommitAllControlSection?,
        snakeOffset: Float,
    ) {
        graphics.color = Color.WHITE
        graphics.fillRoundRect(58, 642, 1078, 84, 20, 20)
        graphics.color = BORDER
        graphics.stroke = BasicStroke(1.1f)
        graphics.drawRoundRect(58, 642, 1078, 84, 20, 20)

        drawSequenceItem(
            graphics = graphics,
            x = 126,
            label = "Ready",
            section = null,
            active = activeSection == null,
            snakeOffset = 0f,
        )
        drawSequenceItem(
            graphics = graphics,
            x = 382,
            label = "AI",
            section = AiCommitAllControlSection.Ai,
            active = activeSection == AiCommitAllControlSection.Ai,
            snakeOffset = snakeOffset,
        )
        drawSequenceItem(
            graphics = graphics,
            x = 638,
            label = "Commit",
            section = AiCommitAllControlSection.Commit,
            active = activeSection == AiCommitAllControlSection.Commit,
            snakeOffset = snakeOffset,
        )
        drawSequenceItem(
            graphics = graphics,
            x = 894,
            label = "Push",
            section = AiCommitAllControlSection.Push,
            active = activeSection == AiCommitAllControlSection.Push,
            snakeOffset = snakeOffset,
        )
    }

    private fun drawSequenceItem(
        graphics: Graphics2D,
        x: Int,
        label: String,
        section: AiCommitAllControlSection?,
        active: Boolean,
        snakeOffset: Float,
    ) {
        graphics.color = if (active) TEXT_PRIMARY else TEXT_SECONDARY
        graphics.font = Font(Font.SANS_SERIF, Font.BOLD, 13)
        graphics.drawString(label, x, 668)

        val control = renderControl(
            dark = false,
            state = enabledState(runningSection = section),
            snakeOffset = snakeOffset,
        )
        graphics.drawImage(control, x, 680, null)
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
        frameDelayCentiseconds: Int = GIF_FRAME_DELAY_CENTISECONDS,
    ) {
        check(frames.isNotEmpty()) { "GIF animation needs at least one frame." }
        val writer = requireNotNull(ImageIO.getImageWritersBySuffix("gif").asSequence().firstOrNull()) {
            "No ImageIO GIF writer is available."
        }
        ImageIO.createImageOutputStream(file.toFile()).use { output ->
            writeGifSequence(writer, output, frames, frameDelayCentiseconds)
        }
        assertImageFile(file)
    }

    private fun writeGifSequence(
        writer: ImageWriter,
        output: ImageOutputStream,
        frames: List<BufferedImage>,
        frameDelayCentiseconds: Int,
    ) {
        writer.output = output
        writer.prepareWriteSequence(null)
        try {
            frames.forEach { frame ->
                writer.writeToSequence(
                    IIOImage(frame, null, gifMetadata(writer, frame, frameDelayCentiseconds)),
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
        frameDelayCentiseconds: Int,
    ): IIOMetadata {
        val type = ImageTypeSpecifier.createFromBufferedImageType(frame.type)
        val metadata = writer.getDefaultImageMetadata(type, writer.defaultWriteParam)
        val root = metadata.getAsTree(GIF_METADATA_FORMAT) as IIOMetadataNode
        root.child("GraphicControlExtension").apply {
            setAttribute("disposalMethod", "none")
            setAttribute("userInputFlag", "FALSE")
            setAttribute("transparentColorFlag", "FALSE")
            setAttribute("delayTime", frameDelayCentiseconds.toString())
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

    private fun Graphics2D.enableQualityRendering() {
        setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
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
        private const val MARKETPLACE_WIDTH = 1200
        private const val MARKETPLACE_HEIGHT = 760
        private const val MARKETPLACE_PHASE_FRAME_COUNT = 5
        private const val MARKETPLACE_GIF_FRAME_DELAY_CENTISECONDS = 30
        private const val MARKETPLACE_GIF_FRAME_OFFSET_STEP = 4.0f
        private val assetDirectory = Path.of("docs", "assets", "user-guide").toAbsolutePath()
        private val marketplaceAssetDirectory = Path.of("docs", "assets", "marketplace").toAbsolutePath()
        private val LIGHT_BACKGROUND = Color(0xF7F8FA)
        private val DARK_BACKGROUND = Color(0x2B2D30)
        private val MARKETPLACE_BACKGROUND = Color(0xF3F4F6)
        private val IDE_BACKGROUND = Color(0xF9FAFB)
        private val IDE_HEADER = Color(0x2B2D30)
        private val IDE_PANEL = Color(0xF4F6FA)
        private val CARD_SHADOW = Color(0xD8DCE3)
        private val BORDER = Color(0xD1D5DB)
        private val TEXT_PRIMARY = Color(0x1F2937)
        private val TEXT_SECONDARY = Color(0x6B7280)
        private val ACCENT_BLUE = Color(0x315FAE)
        private val DIFF_ADDED_BACKGROUND = Color(0xE8F5EC)
        private val DIFF_ADDED_TEXT = Color(0x146C36)
        private val DIFF_REMOVED_BACKGROUND = Color(0xFDECEC)
        private val DIFF_REMOVED_TEXT = Color(0xA43B3B)
        private val marketplaceReadyStep = MarketplaceStep(
            title = "Ready for Push workflow",
            description = "All eligible Git changes are included.",
            commitMessage = "Waiting for AI message",
            runningSection = null,
            progress = 0.08,
            color = TEXT_SECONDARY,
        )
        private val marketplaceDoneStep = MarketplaceStep(
            title = "Workflow complete",
            description = "The control returns to idle after completion.",
            commitMessage = "docs: update Marketplace media",
            runningSection = null,
            progress = 1.0,
            color = Color(0x238449),
        )
        private val marketplaceStepBySection = mapOf(
            AiCommitAllControlSection.Ai to MarketplaceStep(
                title = "AI is running",
                description = "AI Assistant drafts the commit message.",
                commitMessage = "Generating commit message...",
                runningSection = AiCommitAllControlSection.Ai,
                progress = 0.34,
                color = Color(0x834DF0),
            ),
            AiCommitAllControlSection.Commit to MarketplaceStep(
                title = "Commit is running",
                description = "The IDE commit workflow is in charge.",
                commitMessage = "docs: update Marketplace media",
                runningSection = AiCommitAllControlSection.Commit,
                progress = 0.68,
                color = Color(0x315FAE),
            ),
            AiCommitAllControlSection.Push to MarketplaceStep(
                title = "Push is running",
                description = "The commit is pushed to its upstream.",
                commitMessage = "docs: update Marketplace media",
                runningSection = AiCommitAllControlSection.Push,
                progress = 0.92,
                color = Color(0x238449),
            ),
        )
    }

    private data class MarketplaceStep(
        val title: String,
        val description: String,
        val commitMessage: String,
        val runningSection: AiCommitAllControlSection?,
        val progress: Double,
        val color: Color,
    )
}
