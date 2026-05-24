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

import org.junit.jupiter.api.Assumptions.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AiCommitAllControlAssetGeneratorTest {
    @Test
    fun `generate documentation visual assets from actual control`() {
        assumeTrue(
            AiCommitAllControlAssetGenerator.shouldGenerateAssets(),
            "Set AICOMMITALL_GENERATE_USER_GUIDE_ASSETS=true to refresh documentation assets.",
        )

        AiCommitAllControlAssetGenerator.writeDocumentationVisualAssets()
    }

    @Test
    fun `generated documentation asset dimensions stay fixed`() {
        val assets = AiCommitAllControlAssetGenerator.renderDocumentationAssets()

        assertEquals(ImageDimensions(width = 214, height = 54), assets.lightControl.dimensions)
        assertEquals(ImageDimensions(width = 214, height = 54), assets.darkControl.dimensions)
        assertEquals(12, assets.runningAnimationFrames.size)
        assets.runningAnimationFrames.forEach { frame ->
            assertEquals(ImageDimensions(width = 214, height = 54), frame.dimensions)
        }
        assertEquals(17, assets.marketplaceAnimationFrames.size)
        assets.marketplaceAnimationFrames.forEach { frame ->
            assertEquals(ImageDimensions(width = 1200, height = 760), frame.dimensions)
        }
        assertEquals(ImageDimensions(width = 1200, height = 760), assets.marketplaceProgress.dimensions)
    }
}
