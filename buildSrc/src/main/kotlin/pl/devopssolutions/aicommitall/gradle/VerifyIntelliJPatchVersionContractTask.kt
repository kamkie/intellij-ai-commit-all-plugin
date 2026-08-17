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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class VerifyIntelliJPatchVersionContractTask : DefaultTask() {
    @get:Input
    abstract val platformReleaseLine: Property<String>

    @get:Input
    abstract val platformVersion: Property<String>

    @get:Input
    abstract val pluginSinceBuild: Property<String>

    @get:Input
    abstract val aiAssistantPluginVersion: Property<String>

    @get:Input
    abstract val pluginVerifierIdeVersions: ListProperty<String>

    @TaskAction
    fun verify() {
        val violations = IntelliJPatchVersionContract.violations(
            platformReleaseLine = platformReleaseLine.get(),
            platformVersion = platformVersion.get(),
            pluginSinceBuild = pluginSinceBuild.get(),
            aiAssistantPluginVersion = aiAssistantPluginVersion.get(),
            pluginVerifierIdeVersions = pluginVerifierIdeVersions.get(),
        )
        if (violations.isNotEmpty()) {
            throw GradleException(
                violations.joinToString(
                    prefix = "IntelliJ patch-version contract failed:\n- ",
                    separator = "\n- ",
                ),
            )
        }
    }
}

internal object IntelliJPatchVersionContract {
    fun violations(
        platformReleaseLine: String,
        platformVersion: String,
        pluginSinceBuild: String,
        aiAssistantPluginVersion: String,
        pluginVerifierIdeVersions: List<String>,
    ): List<String> = buildList {
        if (!platformVersion.belongsTo(platformReleaseLine)) {
            add("platformVersion '$platformVersion' must belong to platformReleaseLine '$platformReleaseLine'.")
        }

        val aiVersionPrefix = "$pluginSinceBuild."
        if (!aiAssistantPluginVersion.startsWith(aiVersionPrefix)) {
            add("aiAssistantPluginVersion '$aiAssistantPluginVersion' must begin with pluginSinceBuild '$aiVersionPrefix'.")
        }

        pluginVerifierIdeVersions.forEach { verifierTarget ->
            val verifierVersion = verifierTarget.substringAfter('-', missingDelimiterValue = "")
            if (!verifierVersion.belongsTo(platformReleaseLine)) {
                add(
                    "pluginVerifierIdeVersions target '$verifierTarget' must belong to " +
                        "platformReleaseLine '$platformReleaseLine'.",
                )
            }
        }
    }

    private fun String.belongsTo(releaseLine: String): Boolean = this == releaseLine || startsWith("$releaseLine.")
}
