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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class UpdateIntellijPatchTask : DefaultTask() {
    @get:Internal
    abstract val propertiesFile: RegularFileProperty

    @get:Input
    abstract val platformVersion: Property<String>

    @get:Input
    abstract val aiAssistantPluginVersion: Property<String>

    @TaskAction
    fun update() {
        try {
            UpdateIntellijPatchProperties.update(
                propertiesFile = propertiesFile.get().asFile.toPath(),
                platformVersion = platformVersion.get(),
                aiAssistantPluginVersion = aiAssistantPluginVersion.get(),
            )
        } catch (exception: IllegalArgumentException) {
            throw GradleException(exception.message ?: "Invalid IntelliJ patch update.", exception)
        } catch (exception: IllegalStateException) {
            throw GradleException(exception.message ?: "IntelliJ patch update failed.", exception)
        }

        logger.lifecycle(
            "Updated platformVersion to {} and aiAssistantPluginVersion to {} in {}.",
            platformVersion.get(),
            aiAssistantPluginVersion.get(),
            propertiesFile.get().asFile,
        )
    }
}
