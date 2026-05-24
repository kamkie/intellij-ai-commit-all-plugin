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
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageOutputStream
import kotlin.test.assertTrue

internal object AiCommitAllImageAssetWriter {
    fun writePng(
        file: Path,
        image: BufferedImage,
    ) {
        check(ImageIO.write(image, "png", file.toFile())) {
            "No ImageIO PNG writer is available."
        }
        assertImageFile(file)
    }

    fun writeGif(
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

    private const val GIF_FRAME_DELAY_CENTISECONDS = 7
    private const val GIF_METADATA_FORMAT = "javax_imageio_gif_image_1.0"
}
