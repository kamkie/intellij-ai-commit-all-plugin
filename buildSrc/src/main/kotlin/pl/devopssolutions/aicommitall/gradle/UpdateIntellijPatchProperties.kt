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
package pl.devopssolutions.aicommitall.gradle

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

internal object UpdateIntellijPatchProperties {
    private val numericCoordinate = Regex("[0-9]+(?:\\.[0-9]+)+")
    private val requiredKeys = listOf(
        "platformReleaseLine",
        "platformVersion",
        "pluginSinceBuild",
        "aiAssistantPluginVersion",
        "pluginVerifierIdeVersions",
    )

    fun update(
        propertiesFile: Path,
        platformVersion: String,
        aiAssistantPluginVersion: String,
    ) {
        require(numericCoordinate.matches(platformVersion)) {
            "platformVersion must be a dot-delimited numeric coordinate."
        }
        require(numericCoordinate.matches(aiAssistantPluginVersion)) {
            "aiAssistantPluginVersion must be a dot-delimited numeric coordinate."
        }

        val originalBytes = Files.readAllBytes(propertiesFile)
        val originalText = decodeUtf8(originalBytes)
        val properties = requiredKeys.associateWith { key -> requiredProperty(originalText, key) }
        val violations = IntelliJPatchVersionContract.violations(
            platformReleaseLine = properties.getValue("platformReleaseLine").value,
            platformVersion = platformVersion,
            pluginSinceBuild = properties.getValue("pluginSinceBuild").value,
            aiAssistantPluginVersion = aiAssistantPluginVersion,
            pluginVerifierIdeVersions = properties.getValue("pluginVerifierIdeVersions").value
                .split(',')
                .map(String::trim),
        )
        require(violations.isEmpty()) {
            violations.joinToString(
                prefix = "IntelliJ patch-version contract failed:\n- ",
                separator = "\n- ",
            )
        }

        val replacements = mapOf(
            "platformVersion" to platformVersion,
            "aiAssistantPluginVersion" to aiAssistantPluginVersion,
        )
        val updatedText = replacements.entries
            .map { (key, value) -> properties.getValue(key) to value }
            .sortedByDescending { (property, _) -> property.valueStart }
            .fold(originalText) { text, (property, value) ->
                text.replaceRange(property.valueStart, property.valueEnd, value)
            }
        val updatedBytes = updatedText.toByteArray(StandardCharsets.UTF_8)
        if (updatedBytes.contentEquals(originalBytes)) {
            return
        }

        replaceAtomically(propertiesFile, updatedBytes)
    }

    private fun decodeUtf8(bytes: ByteArray): String = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (exception: CharacterCodingException) {
        throw IllegalArgumentException("gradle.properties must be valid UTF-8.", exception)
    }

    private fun requiredProperty(text: String, key: String): PropertyValue {
        val escapedKey = Regex.escape(key)
        val occurrences = Regex("(?m)^[\\t ]*(?:\\uFEFF)?$escapedKey(?=[\\t :=]|$)[^\\r\\n]*$")
            .findAll(text)
            .toList()
        require(occurrences.size == 1) {
            when (occurrences.size) {
                0 -> "gradle.properties is missing required key '$key'."
                else -> "gradle.properties contains duplicate key '$key'."
            }
        }

        val exactMatches = Regex("(?m)^(?:\\uFEFF)?$escapedKey=([^\\r\\n]*)$")
            .findAll(text)
            .toList()
        require(exactMatches.size == 1 && exactMatches.single().range == occurrences.single().range) {
            "gradle.properties key '$key' must use exact key=value syntax."
        }

        val valueGroup = exactMatches.single().groups[1] ?: error("Missing value capture for '$key'.")
        require(valueGroup.value.isNotBlank()) { "gradle.properties key '$key' must not be blank." }
        return PropertyValue(
            value = valueGroup.value,
            valueStart = valueGroup.range.first,
            valueEnd = valueGroup.range.last + 1,
        )
    }

    private fun replaceAtomically(propertiesFile: Path, bytes: ByteArray) {
        val parent = propertiesFile.toAbsolutePath().parent
        val temporaryFile = Files.createTempFile(parent, ".${propertiesFile.fileName}.", ".tmp")
        try {
            FileChannel.open(temporaryFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING).use { channel ->
                val buffer = ByteBuffer.wrap(bytes)
                while (buffer.hasRemaining()) {
                    channel.write(buffer)
                }
                channel.force(true)
            }
            Files.move(
                temporaryFile,
                propertiesFile,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (exception: AtomicMoveNotSupportedException) {
            throw IllegalStateException(
                "The filesystem does not support atomic replacement of $propertiesFile.",
                exception,
            )
        } finally {
            Files.deleteIfExists(temporaryFile)
        }
    }

    private data class PropertyValue(
        val value: String,
        val valueStart: Int,
        val valueEnd: Int,
    )
}
