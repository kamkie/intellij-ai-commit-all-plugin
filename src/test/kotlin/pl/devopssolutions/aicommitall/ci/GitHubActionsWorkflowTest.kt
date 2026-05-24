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
import kotlin.test.assertEquals
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
    fun `ci workflow validates repository agent artifacts`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("Validate documentation and agent artifacts"),
            "CI workflow must name the combined documentation and agent-artifact validation gate.",
        )
        assertTrue(
            content.contains("scripts/ai/validate-agent-artifacts.ps1"),
            "CI workflow must run repository agent-artifact validation.",
        )
        assertTrue(
            content.indexOf("Validate documentation and agent artifacts") <
                content.indexOf("Run Detekt static analysis"),
            "CI workflow must validate repository docs and agent artifacts before static analysis.",
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
    fun `gradle build enables configuration cache`() {
        val properties = Files.readString(Path.of("gradle.properties"))
        val build = Files.readString(Path.of("build.gradle.kts"))

        assertTrue(
            properties.contains("org.gradle.configuration-cache=true"),
            "Gradle must enable the configuration cache persistently.",
        )
        assertTrue(
            build.contains("abstract class VerifyJacocoCoverageReportTask : DefaultTask()"),
            "Custom verification tasks must avoid script-object closures that break the configuration cache.",
        )
        assertTrue(
            build.contains("tasks.registering(VerifyJacocoCoverageReportTask::class)"),
            "JaCoCo coverage verification must use the cache-compatible task type.",
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
            content.contains("val detektBaselineFile = layout.projectDirectory.file(\"config/detekt/baseline.xml\")"),
            "Detekt and the baseline guard must share the checked-in baseline path.",
        )
        assertTrue(
            content.contains("baseline = detektBaselineFile.asFile"),
            "Detekt must read the same checked-in baseline file verified by the guardrail.",
        )
        assertTrue(
            content.contains("abstract class VerifyDetektBaselineTask : DefaultTask()"),
            "The build must define a configuration-cache-compatible Detekt baseline guard task.",
        )
        assertTrue(
            content.contains("tasks.registering(VerifyDetektBaselineTask::class)"),
            "The build must register the Detekt baseline guard through Gradle task registration.",
        )
        assertTrue(
            content.contains("dependsOn(verifyDetektBaseline)"),
            "The Gradle verification lifecycle must fail when the Detekt baseline grows.",
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
    fun `ci workflow rejects detekt baseline growth before analysis`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("Verify Detekt baseline is empty"),
            "CI workflow must name the empty Detekt baseline guardrail.",
        )
        assertTrue(
            content.contains("./gradlew verifyDetektBaseline"),
            "CI workflow must fail when the Detekt baseline contains suppressed issues.",
        )
        assertTrue(
            content.indexOf("Verify Detekt baseline is empty") <
                content.indexOf("Run Detekt static analysis"),
            "CI workflow must reject baseline growth before Detekt report publication continues.",
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
    fun `ci workflow publishes gradle test report`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertFalse(
            content.contains("test-summary/action@"),
            "CI workflow must not use the replaced test-summary action.",
        )
        assertTrue(
            content.contains("checks: write"),
            "CI workflow must grant permission for Test Reporter to create check runs.",
        )
        assertTrue(
            content.contains("uses: dorny/test-reporter@v3"),
            "CI workflow must publish test summaries through Test Reporter.",
        )
        assertTrue(
            content.contains("name: Gradle unit tests"),
            "CI workflow must give the Test Reporter check a stable name.",
        )
        assertTrue(
            content.contains("path: build/test-results/test/*.xml"),
            "CI workflow must pass Gradle JUnit XML files to Test Reporter.",
        )
        assertTrue(
            content.contains("reporter: java-junit"),
            "CI workflow must parse Gradle test results as Java JUnit XML.",
        )
    }

    @Test
    fun `ci workflow uploads built plugin zip artifact`() {
        val content = Files.readString(Path.of(".github", "workflows", "ci.yml"))

        assertTrue(
            content.contains("actions/upload-artifact@v7"),
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

    @Test
    fun `release matrix UI workflow uploads Starter IDE logs`() {
        val content = Files.readString(Path.of(".github", "workflows", "release-matrix-ui.yml"))

        assertTrue(
            content.contains("out/ide-tests/tests/**/log/**"),
            "Release matrix UI workflow must upload JetBrains Starter IDE logs and screenshots.",
        )
    }

    @Test
    fun `release workflow validates full release gate before publishing`() {
        val content = Files.readString(Path.of(".github", "workflows", "release.yml"))

        assertTrue(
            content.contains("github.ref != 'refs/heads/main'"),
            "Release workflow must reject manual publication from non-main refs.",
        )
        assertTrue(
            content.contains("release_tag:"),
            "Release workflow must require the intended release tag as a manual input.",
        )
        assertTrue(
            content.contains("fetch-depth: 0"),
            "Release workflow must fetch full history and tags before validating the release tag.",
        )
        assertTrue(
            content.contains("Verify annotated release tag"),
            "Release workflow must verify that the current main commit has an annotated release tag.",
        )
        assertTrue(
            content.contains("git for-each-ref --points-at HEAD"),
            "Release workflow must inspect tags pointing at the checked-out release commit.",
        )
        assertTrue(
            content.contains("EXPECTED_RELEASE_TAG: \${{ inputs.release_tag }}"),
            "Release workflow must compare the checked-out tag with the requested release tag.",
        )
        assertTrue(
            content.contains("actions/setup-node@v6"),
            "Release workflow must install Node.js before documentation validation.",
        )
        assertTrue(
            content.contains("scripts/validate-docs.ps1"),
            "Release workflow must validate documentation before publication.",
        )
        assertTrue(
            content.contains("spotlessCheck verifyDetektBaseline detekt test jacocoTestReport"),
            "Release workflow must include formatting, the Detekt baseline guard, " +
                "static analysis, tests, and coverage generation.",
        )
        assertTrue(
            content.contains("verifyJacocoCoverageReport verifyPluginStructure buildPlugin verifyPlugin"),
            "Release workflow must verify coverage, plugin structure, packaging, and Plugin Verifier.",
        )
        assertTrue(
            content.contains("-PpluginVerifierIdeVersions=IU-2026.1.1,PY-2026.1.1,WS-2026.1.1"),
            "Release workflow must verify the same supported IDE matrix used by compatibility CI.",
        )
        assertTrue(
            content.indexOf("Validate release candidate") <
                content.indexOf("Sign and publish to JetBrains Marketplace"),
            "Release workflow must finish validation before signing and publishing.",
        )
        assertTrue(
            content.indexOf("Verify annotated release tag") <
                content.indexOf("Validate release candidate"),
            "Release workflow must validate the annotated tag before building the release candidate.",
        )
    }

    @Test
    fun `github release notes generator uses matching section and rejects invalid inputs`() {
        val success = runReleaseNotesGenerator(
            "v1.2.3",
            changelogWithReleaseNotes(
                """
                    ### Added

                    - Added GitHub Release automation.
                """,
            ),
        )
        assertEquals(0, success.exitCode, success.log)
        assertEquals("### Added\n\n- Added GitHub Release automation.\n", success.releaseNotes)

        assertGeneratorFails(
            runReleaseNotesGenerator("1.2.3", changelogWithReleaseNotes()),
            "Release tag must use vMAJOR.MINOR.PATCH or vMAJOR.MINOR.PATCH-PRERELEASE.",
        )
        assertGeneratorFails(
            runReleaseNotesGenerator("v9.9.9", changelogWithReleaseNotes()),
            "CHANGELOG.md is missing release section [v9.9.9].",
        )
        assertGeneratorFails(
            runReleaseNotesGenerator("v1.2.3", changelogWithReleaseNotes("### Changed\n\n-")),
            "CHANGELOG.md section [v1.2.3] has an empty release-note item.",
        )
    }

    @Test
    fun `github release workflow creates releases from generated changelog notes`() {
        val content = Files.readString(Path.of(".github", "workflows", "github-release.yml"))

        listOf(
            "name: GitHub Release",
            "push:",
            "tags:",
            "- 'v*.*.*'",
            "permissions:\n  contents: write",
            "fetch-depth: 0",
            "Validate release tag",
            "^v[0-9]+\\.[0-9]+\\.[0-9]+(-[0-9A-Za-z][0-9A-Za-z.-]*)?$",
            "scripts/generate-github-release-notes.ps1",
            "-Tag '\${{ github.ref_name }}'",
            "-OutputPath 'build/github-release-notes.md'",
            "GH_TOKEN: \${{ secrets.GITHUB_TOKEN }}",
            "gh release view \"\${RELEASE_TAG}\"",
            "gh release edit \"\${RELEASE_TAG}\"",
            "gh release create \"\${RELEASE_TAG}\"",
            "--notes-file \"\${RELEASE_NOTES}\"",
            "--verify-tag",
        ).forEach { snippet ->
            assertTrue(content.contains(snippet), "GitHub Release workflow is missing: $snippet")
        }

        assertFalse(
            content.contains("jetbrains-marketplace") ||
                content.contains("PUBLISH_TOKEN") ||
                content.contains("CERTIFICATE_CHAIN") ||
                content.contains("PRIVATE_KEY_PASSWORD") ||
                content.contains("signPlugin") ||
                content.contains("publishPlugin"),
            "GitHub Release workflow must not require Marketplace publication configuration.",
        )
        assertTrue(
            content.indexOf("Validate release tag") < content.indexOf("Generate release notes"),
            "GitHub Release workflow must validate the tag before reading changelog release notes.",
        )
        assertTrue(
            content.indexOf("Generate release notes") < content.lastIndexOf("Create or update GitHub Release"),
            "GitHub Release workflow must generate notes before creating or updating the release.",
        )
    }

    private data class ScriptResult(val exitCode: Int, val log: String, val releaseNotes: String)

    private fun runReleaseNotesGenerator(tag: String, changelogText: String): ScriptResult {
        val tempDirectory = Files.createTempDirectory("github-release-notes-test")
        try {
            val changelogPath = tempDirectory.resolve("CHANGELOG.md")
            val outputPath = tempDirectory.resolve("release-notes.md")
            Files.writeString(changelogPath, changelogText.trimIndent().trimStart())
            val process = ProcessBuilder(
                "pwsh",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                Path.of("scripts", "generate-github-release-notes.ps1").toAbsolutePath().toString(),
                "-Tag",
                tag,
                "-ChangelogPath",
                changelogPath.toString(),
                "-OutputPath",
                outputPath.toString(),
            ).redirectErrorStream(true).start()

            val log = process.inputStream.bufferedReader().readText()
            val releaseNotes = if (Files.exists(outputPath)) Files.readString(outputPath) else ""
            return ScriptResult(process.waitFor(), log, releaseNotes)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    private fun assertGeneratorFails(result: ScriptResult, message: String) {
        assertTrue(result.exitCode != 0, result.log)
        assertTrue(result.log.contains(message), "Generator output did not contain: $message\n${result.log}")
    }

    private fun changelogWithReleaseNotes(body: String = "### Changed\n\n- Added release notes."): String = listOf(
        "# Changelog",
        "",
        "## [Unreleased]",
        "",
        "### Changed",
        "",
        "- Work in progress.",
        "",
        "## [v1.2.3] - 2026-05-24",
        "",
        body.trimIndent(),
    ).joinToString("\n") + "\n"

    private fun gradleWorkflows(): List<Path> = Files.list(Path.of(".github", "workflows")).use { workflows ->
        workflows
            .filter { workflow -> workflow.name.endsWith(".yml") || workflow.name.endsWith(".yaml") }
            .filter { workflow -> Files.readString(workflow).contains("./gradlew") }
            .toList()
    }
}
