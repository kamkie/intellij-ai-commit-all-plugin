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

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

internal object AiCommitAllControlAssetGenerator {
    fun shouldGenerateAssets(): Boolean = System.getProperty("aicommitall.generateUserGuideAssets") == "true" ||
        System.getenv("AICOMMITALL_GENERATE_USER_GUIDE_ASSETS") == "true"

    fun writeDocumentationVisualAssets() {
        Files.createDirectories(assetDirectory)
        Files.createDirectories(marketplaceAssetDirectory)

        AiCommitAllImageAssetWriter.writePng(
            file = assetDirectory.resolve("ai-commit-all-control-light.png"),
            image = AiCommitAllControlAssetRenderer.renderControl(
                dark = false,
                state = AiCommitAllControlAssetRenderer.enabledState(),
            ),
        )
        AiCommitAllImageAssetWriter.writePng(
            file = assetDirectory.resolve("ai-commit-all-control-dark.png"),
            image = AiCommitAllControlAssetRenderer.renderControl(
                dark = true,
                state = AiCommitAllControlAssetRenderer.enabledState(),
            ),
        )
        AiCommitAllImageAssetWriter.writeGif(
            file = assetDirectory.resolve("ai-commit-all-control-running.gif"),
            frames = AiCommitAllControlAssetRenderer.runningAnimationFrames(),
        )
        AiCommitAllImageAssetWriter.writeGif(
            file = marketplaceAssetDirectory.resolve("ai-commit-all-realtime-progress.gif"),
            frames = AiCommitAllMarketplaceAssetRenderer.marketplaceAnimationFrames(),
            frameDelayCentiseconds = AiCommitAllMarketplaceAssetRenderer.MARKETPLACE_GIF_FRAME_DELAY_CENTISECONDS,
        )
        AiCommitAllImageAssetWriter.writePng(
            file = marketplaceAssetDirectory.resolve("ai-commit-all-realtime-progress.png"),
            image = AiCommitAllMarketplaceAssetRenderer.renderMarketplaceProgressFrame(),
        )
    }

    fun renderDocumentationAssets(): GeneratedDocumentationAssets = GeneratedDocumentationAssets(
        lightControl = AiCommitAllControlAssetRenderer.renderControl(
            dark = false,
            state = AiCommitAllControlAssetRenderer.enabledState(),
        ),
        darkControl = AiCommitAllControlAssetRenderer.renderControl(
            dark = true,
            state = AiCommitAllControlAssetRenderer.enabledState(),
        ),
        runningAnimationFrames = AiCommitAllControlAssetRenderer.runningAnimationFrames(),
        marketplaceAnimationFrames = AiCommitAllMarketplaceAssetRenderer.marketplaceAnimationFrames(),
        marketplaceProgress = AiCommitAllMarketplaceAssetRenderer.renderMarketplaceProgressFrame(),
    )

    private val assetDirectory = Path.of("docs", "assets", "user-guide").toAbsolutePath()
    private val marketplaceAssetDirectory = Path.of("docs", "assets", "marketplace").toAbsolutePath()
}

internal data class GeneratedDocumentationAssets(
    val lightControl: BufferedImage,
    val darkControl: BufferedImage,
    val runningAnimationFrames: List<BufferedImage>,
    val marketplaceAnimationFrames: List<BufferedImage>,
    val marketplaceProgress: BufferedImage,
)
