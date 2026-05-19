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

    @Test
    fun `ci workflow uploads jacoco coverage to codecov`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("id-token: write"),
            "CI workflow must grant OIDC permission for Codecov upload authentication.",
        )
        assertTrue(
            content.contains("./gradlew test jacocoTestReport verifyPluginStructure buildPlugin"),
            "CI workflow must generate the JaCoCo XML report before uploading coverage.",
        )
        assertTrue(
            content.contains("codecov/codecov-action@v6"),
            "CI workflow must use the current Codecov action line.",
        )
        assertTrue(
            content.contains("files: build/reports/jacoco/test/jacocoTestReport.xml"),
            "CI workflow must upload the explicit JaCoCo XML report.",
        )
        assertTrue(
            content.contains("disable_search: true"),
            "CI workflow must avoid uploading unintended coverage files.",
        )
        assertTrue(
            content.contains("use_oidc: true"),
            "CI workflow must use OIDC instead of requiring a Codecov token secret.",
        )
    }

    @Test
    fun `ci workflow uploads gradle test results to codecov`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("if: \${{ !cancelled() }}"),
            "CI workflow must upload test results even after test failures when the workflow is not cancelled.",
        )
        assertTrue(
            content.contains("codecov/test-results-action@v1"),
            "CI workflow must upload test results through the Codecov test-results action.",
        )
        assertTrue(
            content.contains("files: build/test-results/test/*.xml"),
            "CI workflow must upload Gradle's explicit JUnit XML test result reports.",
        )
        assertTrue(
            content.contains("name: ai-commit-all-unit-test-results"),
            "CI workflow must name the test-results upload separately from coverage.",
        )
        assertTrue(
            content.contains("disable_search: true"),
            "CI workflow must avoid uploading unintended test result files.",
        )
        assertTrue(
            content.contains("use_oidc: true"),
            "CI workflow must use OIDC for test-results upload authentication.",
        )
    }

    private fun gradleWorkflows(): List<Path> = Files.list(Path.of(".github", "workflows")).use { workflows ->
        workflows
            .filter { workflow -> workflow.name.endsWith(".yml") || workflow.name.endsWith(".yaml") }
            .filter { workflow -> Files.readString(workflow).contains("./gradlew") }
            .toList()
    }
}
