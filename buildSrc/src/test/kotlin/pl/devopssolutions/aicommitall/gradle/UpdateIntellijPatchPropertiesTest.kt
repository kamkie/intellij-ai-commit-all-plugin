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

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class UpdateIntellijPatchPropertiesTest {
    @Test
    fun `updates only platform and AI Assistant versions while preserving the file`() {
        val fixture = fixture(
            platformVersion = "2026.2.0.1",
            aiAssistantPluginVersion = "262.8665.344",
        )
        val expected = fixture(
            platformVersion = "2026.2.1",
            aiAssistantPluginVersion = "262.9000.1",
        )
        val path = writeFixture(fixture)

        UpdateIntellijPatchProperties.update(
            propertiesFile = path,
            platformVersion = "2026.2.1",
            aiAssistantPluginVersion = "262.9000.1",
        )

        val updatedBytes = Files.readAllBytes(path)
        assertContentEquals(expected.toByteArray(StandardCharsets.UTF_8), updatedBytes)
        assertFalse(updatedBytes.contains('\r'.code.toByte()), "LF line endings must be preserved")
    }

    @Test
    fun `rejects platform versions outside the approved release line without mutation`() {
        listOf("2026.3", "2026.20").forEach { rejectedVersion ->
            assertRejectedWithoutMutation(platformVersion = rejectedVersion)
        }
    }

    @Test
    fun `rejects an AI Assistant build-prefix mismatch without mutation`() {
        assertRejectedWithoutMutation(aiAssistantPluginVersion = "261.9000.1")
    }

    @Test
    fun `rejects malformed UTF-8 without mutation`() {
        val originalBytes = fixture().toByteArray(StandardCharsets.UTF_8) + byteArrayOf(0xC3.toByte())
        val path = createTempDirectory("update-intellij-patch").resolve("gradle.properties")
        Files.write(path, originalBytes)

        assertFailsWith<IllegalArgumentException> {
            UpdateIntellijPatchProperties.update(
                propertiesFile = path,
                platformVersion = "2026.2.1",
                aiAssistantPluginVersion = "262.9000.1",
            )
        }

        assertContentEquals(originalBytes, Files.readAllBytes(path))
    }

    @Test
    fun `rejects every missing contract key without mutation`() {
        REQUIRED_KEYS.forEach { missingKey ->
            val original = fixture().lineSequence()
                .filterNot { line -> line.startsWith("$missingKey=") }
                .joinToString(separator = "\n", postfix = "\n")
            assertRejectedWithoutMutation(original = original)
        }
    }

    @Test
    fun `rejects every duplicate contract key without mutation`() {
        REQUIRED_KEYS.forEach { duplicateKey ->
            val original = fixture() + fixtureValueLine(duplicateKey) + "\n"
            assertRejectedWithoutMutation(original = original)
        }
    }

    @Test
    fun `rejects non-canonical duplicates of contract keys without mutation`() {
        REQUIRED_KEYS.forEach { duplicateKey ->
            val original = fixture() + "$duplicateKey : hidden-duplicate\n"
            assertRejectedWithoutMutation(original = original)
        }
    }

    private fun assertRejectedWithoutMutation(
        original: String = fixture(),
        platformVersion: String = "2026.2.1",
        aiAssistantPluginVersion: String = "262.9000.1",
    ) {
        val originalBytes = original.toByteArray(StandardCharsets.UTF_8)
        val path = writeFixture(original)

        assertFailsWith<IllegalArgumentException> {
            UpdateIntellijPatchProperties.update(
                propertiesFile = path,
                platformVersion = platformVersion,
                aiAssistantPluginVersion = aiAssistantPluginVersion,
            )
        }

        assertContentEquals(originalBytes, Files.readAllBytes(path))
    }

    private fun writeFixture(contents: String): Path {
        val path = createTempDirectory("update-intellij-patch").resolve("gradle.properties")
        Files.write(path, contents.toByteArray(StandardCharsets.UTF_8))
        return path
    }

    private fun fixture(
        platformVersion: String = "2026.2.0.1",
        aiAssistantPluginVersion: String = "262.8665.344",
    ): String = """
        # UTF-8 comment: zażółć
        unrelated.before=keep-me
        platformReleaseLine=2026.2
        platformVersion=$platformVersion
        # Coordinate order and comments stay unchanged.
        pluginSinceBuild=262
        aiAssistantPluginVersion=$aiAssistantPluginVersion
        pluginVerifierIdeVersions=IU-2026.2,PY-2026.2,WS-2026.2
        unrelated.after=also-keep-me
    """.trimIndent() + "\n"

    private fun fixtureValueLine(key: String): String = when (key) {
        "platformReleaseLine" -> "platformReleaseLine=2026.2"
        "platformVersion" -> "platformVersion=2026.2.0.1"
        "pluginSinceBuild" -> "pluginSinceBuild=262"
        "aiAssistantPluginVersion" -> "aiAssistantPluginVersion=262.8665.344"
        "pluginVerifierIdeVersions" -> "pluginVerifierIdeVersions=IU-2026.2"
        else -> error("Unsupported fixture key: $key")
    }

    companion object {
        private val REQUIRED_KEYS = listOf(
            "platformReleaseLine",
            "platformVersion",
            "pluginSinceBuild",
            "aiAssistantPluginVersion",
            "pluginVerifierIdeVersions",
        )
    }
}
