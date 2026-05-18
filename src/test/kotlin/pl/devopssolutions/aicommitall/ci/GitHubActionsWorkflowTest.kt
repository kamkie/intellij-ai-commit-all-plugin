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
package pl.devopssolutions.aicommitall.ci

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GitHubActionsWorkflowTest {
    @Test
    fun `gradle workflows use setup gradle wrapper validation`() {
        gradleWorkflows().forEach { workflow ->
            val content = Files.readString(workflow)

            assertFalse(
                content.contains("gradle/actions/wrapper-validation@"),
                "${workflow.name} must not use standalone wrapper validation.",
            )
            assertTrue(
                content.contains("gradle/actions/setup-gradle@"),
                "${workflow.name} must configure Gradle through setup-gradle.",
            )
            assertTrue(
                content.contains("validate-wrappers: true"),
                "${workflow.name} must keep Gradle wrapper validation enabled.",
            )
        }
    }

    private fun gradleWorkflows(): List<Path> = Files.list(Path.of(".github", "workflows")).use { workflows ->
        workflows
            .filter { workflow -> workflow.name.endsWith(".yml") || workflow.name.endsWith(".yaml") }
            .filter { workflow -> Files.readString(workflow).contains("./gradlew") }
            .toList()
    }
}
