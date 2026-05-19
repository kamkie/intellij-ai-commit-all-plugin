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
        val validationCommand = "./gradlew test jacocoTestReport " +
            "verifyJacocoCoverageReport verifyPluginStructure buildPlugin"

        assertTrue(
            content.contains("id-token: write"),
            "CI workflow must grant OIDC permission for Codecov upload authentication.",
        )
        assertTrue(
            content.contains(validationCommand),
            "CI workflow must generate and verify the JaCoCo XML report before uploading coverage.",
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
            content.contains("report_type: coverage"),
            "CI workflow must upload the JaCoCo XML report as Codecov coverage.",
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
    fun `jacoco report uses intellij instrumented production classes`() {
        val content = Files.readString(Path.of("build.gradle.kts"))

        assertTrue(
            content.contains("classDirectories.setFrom(layout.buildDirectory.dir(\"instrumented/instrumentCode\"))"),
            "JaCoCo must report against the IntelliJ-instrumented production classes used by plugin tests.",
        )
        assertTrue(
            content.contains("isIncludeNoLocationClasses = true"),
            "JaCoCo must instrument plugin classes loaded through the IntelliJ test classloader.",
        )
        assertTrue(
            content.contains("verifyJacocoCoverageReport"),
            "The build must fail when the JaCoCo XML report contains no executed production coverage.",
        )
    }

    @Test
    fun `gradle configures detekt static analysis reports`() {
        val content = Files.readString(Path.of("build.gradle.kts"))

        assertTrue(
            content.contains("id(\"dev.detekt\") version \"2.0.0-alpha.3\""),
            "The build must apply the Detekt Gradle plugin version aligned with the Kotlin toolchain.",
        )
        assertTrue(
            content.contains("toolVersion = \"2.0.0-alpha.3\""),
            "The Detekt runtime version must stay pinned instead of drifting with plugin defaults.",
        )
        assertTrue(
            content.contains("source.setFrom(\"src/main/kotlin\", \"src/test/kotlin\")"),
            "Detekt must analyze production and test Kotlin sources explicitly.",
        )
        assertTrue(
            content.contains("baseline = file(\"config/detekt/baseline.xml\")"),
            "Detekt must use a checked-in baseline so existing findings do not block incremental adoption.",
        )
        assertTrue(
            content.contains("basePath.set(projectDir)"),
            "Detekt reports must use repository-relative paths for CI annotations.",
        )
        assertTrue(
            content.contains("sarif.required.set(true)"),
            "Detekt must emit SARIF for GitHub code scanning.",
        )
        assertTrue(
            content.contains("markdown.required.set(true)"),
            "Detekt must emit a Markdown report for GitHub Actions artifacts.",
        )
    }

    @Test
    fun `ci workflow publishes detekt reports to code scanning and artifacts`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("security-events: write"),
            "CI workflow must grant permission to upload Detekt SARIF to GitHub code scanning.",
        )
        assertTrue(
            content.contains("id: detekt-analysis"),
            "CI workflow must give the Detekt step a stable id for report upload and failure propagation.",
        )
        assertTrue(
            content.contains("./gradlew detekt"),
            "CI workflow must run Detekt static analysis.",
        )
        assertTrue(
            content.contains("github/codeql-action/upload-sarif@v4"),
            "CI workflow must upload Detekt SARIF through GitHub's SARIF upload action.",
        )
        assertTrue(
            content.contains("sarif_file: build/reports/detekt/detekt.sarif"),
            "CI workflow must upload the Detekt SARIF report path produced by Gradle.",
        )
        assertTrue(
            content.contains("category: detekt"),
            "CI workflow must give Detekt code-scanning results a stable category.",
        )
        assertTrue(
            content.contains("name: ai-commit-all-detekt-reports"),
            "CI workflow must upload Detekt HTML, XML, Markdown, and SARIF reports as an artifact.",
        )
        assertTrue(
            content.contains("path: build/reports/detekt/**"),
            "CI workflow must upload all generated Detekt report files.",
        )
        assertTrue(
            content.contains("steps.detekt-analysis.outcome == 'failure'"),
            "CI workflow must fail after publishing Detekt reports when analysis fails.",
        )
    }

    @Test
    fun `security workflow runs trivy filesystem scan`() {
        val content = Files.readString(Path.of(".github", "workflows", "security.yml"))

        assertTrue(
            content.contains("name: Security"),
            "Security workflow must have a stable display name.",
        )
        assertTrue(
            content.contains("security-events: write"),
            "Security workflow must grant permission to upload Trivy SARIF to GitHub code scanning.",
        )
        assertTrue(
            content.contains("workflow_dispatch:"),
            "Security workflow must be manually runnable.",
        )
        assertTrue(
            content.contains("aquasecurity/trivy-action@v0.36.0"),
            "Security workflow must run the pinned Trivy GitHub Action version.",
        )
        assertTrue(
            content.contains("scan-type: 'fs'"),
            "Trivy must scan the checked-out repository filesystem.",
        )
        assertTrue(
            content.contains("scan-ref: '.'"),
            "Trivy must scan the repository root.",
        )
        assertTrue(
            content.contains("format: 'sarif'"),
            "Trivy must emit SARIF for GitHub code scanning.",
        )
        assertTrue(
            content.contains("output: 'build/reports/trivy/trivy-results.sarif'"),
            "Trivy SARIF must be written under build reports.",
        )
        assertTrue(
            content.contains("severity: 'HIGH,CRITICAL'"),
            "Trivy must gate high and critical findings.",
        )
        assertTrue(
            content.contains("ignore-unfixed: true"),
            "Trivy must avoid failing on vulnerability findings without known fixes.",
        )
        assertTrue(
            content.contains("exit-code: '1'"),
            "Trivy must fail the workflow after report upload when gated findings exist.",
        )
    }

    @Test
    fun `security workflow publishes trivy reports`() {
        val content = Files.readString(Path.of(".github", "workflows", "security.yml"))

        assertTrue(
            content.contains("format: 'sarif'"),
            "Trivy must emit SARIF for GitHub code scanning.",
        )
        assertTrue(
            content.contains("output: 'build/reports/trivy/trivy-results.sarif'"),
            "Trivy SARIF must be written under build reports.",
        )
        assertTrue(
            content.contains("github/codeql-action/upload-sarif@v4"),
            "Security workflow must upload Trivy SARIF through GitHub's SARIF upload action.",
        )
        assertTrue(
            content.contains("sarif_file: build/reports/trivy/trivy-results.sarif"),
            "Security workflow must upload the Trivy SARIF report path.",
        )
        assertTrue(
            content.contains("category: trivy"),
            "Security workflow must give Trivy code-scanning results a stable category.",
        )
        assertTrue(
            content.contains("name: ai-commit-all-trivy-reports"),
            "Security workflow must upload Trivy results as an artifact.",
        )
        assertTrue(
            content.contains("steps.trivy-scan.outcome == 'failure'"),
            "Security workflow must fail after publishing Trivy reports when analysis fails.",
        )
    }

    @Test
    fun `ci workflow uploads gradle test results to codecov`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertFalse(
            content.contains("codecov/test-results-action@"),
            "CI workflow must not use the deprecated Codecov test-results action.",
        )
        assertTrue(
            content.contains("if: \${{ !cancelled() }}"),
            "CI workflow must upload test results even after test failures when the workflow is not cancelled.",
        )
        assertTrue(
            content.contains("id: codecov-test-results"),
            "CI workflow must collect explicit JUnit XML paths before uploading test results.",
        )
        assertTrue(
            content.contains("find build/test-results/test -name '*.xml' -type f"),
            "CI workflow must expand Gradle JUnit XML paths before passing them to Codecov.",
        )
        assertTrue(
            content.contains("codecov/codecov-action@v6"),
            "CI workflow must upload test results through the current Codecov action.",
        )
        assertTrue(
            content.contains("files: \${{ steps.codecov-test-results.outputs.files }}"),
            "CI workflow must pass explicit Gradle JUnit XML paths to Codecov.",
        )
        assertTrue(
            content.contains("report_type: test_results"),
            "CI workflow must upload Gradle JUnit XML reports as Codecov test results.",
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

    @Test
    fun `ci workflow uploads built plugin zip artifact`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("actions/upload-artifact@v6"),
            "CI workflow must upload the packaged plugin ZIP as a GitHub Actions artifact.",
        )
        assertTrue(
            content.contains("name: ai-commit-all-plugin"),
            "CI workflow must give the plugin ZIP artifact a stable name.",
        )
        assertTrue(
            content.contains("path: build/distributions/*.zip"),
            "CI workflow must upload the ZIP files produced by buildPlugin.",
        )
        assertTrue(
            content.contains("if-no-files-found: error"),
            "CI workflow must fail visibly when buildPlugin does not produce a ZIP.",
        )
        assertTrue(
            content.contains("compression-level: 0"),
            "CI workflow must avoid recompressing plugin ZIP artifacts.",
        )
    }

    private fun gradleWorkflows(): List<Path> = Files.list(Path.of(".github", "workflows")).use { workflows ->
        workflows
            .filter { workflow -> workflow.name.endsWith(".yml") || workflow.name.endsWith(".yaml") }
            .filter { workflow -> Files.readString(workflow).contains("./gradlew") }
            .toList()
    }
}
