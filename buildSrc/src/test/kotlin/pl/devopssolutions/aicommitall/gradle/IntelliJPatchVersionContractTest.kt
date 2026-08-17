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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntelliJPatchVersionContractTest {
    @Test
    fun `accepts base and patch versions within the configured release line`() {
        listOf("2026.2", "2026.2.0.1", "2026.2.1").forEach { platformVersion ->
            val violations = violations(
                platformVersion = platformVersion,
                verifierVersions = listOf("IU-$platformVersion"),
            )

            assertTrue(violations.isEmpty(), "$platformVersion produced $violations")
        }
    }

    @Test
    fun `rejects a platform patch outside the configured release line`() {
        val violations = violations(platformVersion = "2026.3.1")

        assertEquals(
            listOf("platformVersion '2026.3.1' must belong to platformReleaseLine '2026.2'."),
            violations,
        )
    }

    @Test
    fun `rejects an AI Assistant version from another platform build`() {
        val violations = violations(aiAssistantPluginVersion = "261.9999.1")

        assertEquals(
            listOf("aiAssistantPluginVersion '261.9999.1' must begin with pluginSinceBuild '262.'."),
            violations,
        )
    }

    @Test
    fun `rejects verifier targets outside the configured release line`() {
        val violations = violations(verifierVersions = listOf("IU-2026.2.1", "PY-2026.3"))

        assertEquals(
            listOf("pluginVerifierIdeVersions target 'PY-2026.3' must belong to platformReleaseLine '2026.2'."),
            violations,
        )
    }

    @Test
    fun `rejects delimiter near misses`() {
        val violations = violations(
            platformVersion = "2026.20",
            aiAssistantPluginVersion = "2620.9999.1",
            verifierVersions = listOf("IU-2026.20"),
        )

        assertEquals(3, violations.size)
    }

    private fun violations(
        platformVersion: String = "2026.2.0.1",
        aiAssistantPluginVersion: String = "262.8665.344",
        verifierVersions: List<String> = listOf("IU-2026.2"),
    ): List<String> = IntelliJPatchVersionContract.violations(
        platformReleaseLine = "2026.2",
        platformVersion = platformVersion,
        pluginSinceBuild = "262",
        aiAssistantPluginVersion = aiAssistantPluginVersion,
        pluginVerifierIdeVersions = verifierVersions,
    )
}
