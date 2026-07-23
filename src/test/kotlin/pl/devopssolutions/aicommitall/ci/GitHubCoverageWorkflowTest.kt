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
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GitHubCoverageWorkflowTest {
    @Test
    fun `ci workflow runs on pull requests and main pushes only`() {
        val content = ciWorkflow()

        assertTrue(
            content.contains("pull_request:"),
            "CI workflow must run for pull requests.",
        )
        assertTrue(
            content.contains("push:\n    branches:\n      - main"),
            "CI workflow must run for pushes to main.",
        )
        assertFalse(
            content.contains("workflow_dispatch:"),
            "CI workflow must not become a manual release-validation workflow.",
        )
        assertFalse(
            Regex("(?m)^    tags:").containsMatchIn(content),
            "CI workflow must not run tag-release behavior.",
        )
    }

    @Test
    fun `ci workflow publishes unit coverage artifact for aggregate job`() {
        val content = ciWorkflow()
        val buildJob = workflowJob(content, "build")
        val uiCoverageJob = workflowJob(content, "ui-coverage")
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
            buildJob.contains("name: ai-commit-all-unit-coverage") &&
                buildJob.contains("path: build/reports/jacoco/test/jacocoTestReport.xml"),
            "Build job must publish the existing unit JaCoCo XML as an artifact for the aggregate job.",
        )
        assertFalse(
            buildJob.contains("report_type: coverage"),
            "Build job must not upload unit-only coverage before the aggregate UI coverage job completes.",
        )
        assertTrue(
            uiCoverageJob.contains("needs: build"),
            "UI coverage job must wait for the build job's unit JaCoCo artifact.",
        )
        assertTrue(
            uiCoverageJob.contains("uses: actions/download-artifact@") &&
                uiCoverageJob.contains("name: ai-commit-all-unit-coverage") &&
                uiCoverageJob.contains("path: build/reports/jacoco/unit"),
            "UI coverage job must download the unit JaCoCo artifact instead of rerunning unit coverage.",
        )
    }

    @Test
    fun `ci workflow uploads aggregate jacoco coverage to codecov`() {
        val content = ciWorkflow()
        val uiCoverageJob = workflowJob(content, "ui-coverage")

        assertTrue(
            uiCoverageJob.contains("releaseMatrixUiTest") &&
                uiCoverageJob.contains("-PideProducts=PY") &&
                uiCoverageJob.contains("-PideVersion=2026.2"),
            "UI coverage job must run the smallest release-matrix UI lane that exposes integration coverage.",
        )
        assertTrue(
            uiCoverageJob.contains("test -s build/reports/jacoco/unit/jacocoTestReport.xml") &&
                uiCoverageJob.contains("test -s build/jacoco/releaseMatrixUiIde.exec") &&
                uiCoverageJob.contains("find build/jacoco/releaseMatrixUiClassDump -name '*.class'"),
            "UI coverage job must verify the downloaded unit XML and IDE JaCoCo coverage inputs.",
        )
        assertTrue(
            uiCoverageJob.contains("./gradlew --no-configuration-cache jacocoAggregateReport") &&
                uiCoverageJob.contains(
                    "-Paicommitall.unitJacocoXmlReport=build/reports/jacoco/unit/jacocoTestReport.xml",
                ),
            "UI coverage job must build aggregate coverage from the downloaded unit XML.",
        )
        assertTrue(
            uiCoverageJob.contains("files: build/reports/jacoco/jacocoAggregateReport/jacocoAggregateReport.xml") &&
                uiCoverageJob.contains("flags: unit,aggregate") &&
                uiCoverageJob.contains("name: ai-commit-all-unit-tests") &&
                uiCoverageJob.contains("report_type: coverage"),
            "UI coverage job must upload the aggregate JaCoCo XML report to Codecov's stable coverage stream.",
        )
        assertTrue(
            content.contains("disable_search: true") && content.contains("use_oidc: true"),
            "CI workflow must authenticate explicitly and avoid uploading unintended coverage files.",
        )
    }

    @Test
    fun `ci workflow keeps normal builds separate from release automation`() {
        val content = ciWorkflow()

        listOf(
            "signPlugin",
            "publishPlugin",
            "PUBLISH_TOKEN",
            "CERTIFICATE_CHAIN",
            "PRIVATE_KEY",
            "PRIVATE_KEY_PASSWORD",
            "jetbrains-marketplace",
            "gh release",
            "release_tag:",
            "Verify annotated release tag",
            "github.ref != 'refs/heads/main'",
        ).forEach { forbidden ->
            assertFalse(content.contains(forbidden), "CI workflow must not contain release behavior: $forbidden")
        }

        assertTrue(
            content.contains("buildPlugin"),
            "CI workflow may still package the plugin for normal build validation.",
        )
    }

    @Test
    fun `jacoco aggregate report can use downloaded unit coverage xml`() {
        val content = Files.readString(Path.of("build.gradle.kts"))

        assertTrue(
            content.contains("val jacocoAggregateUnitXmlReportOverride = ") &&
                content.contains("providers.gradleProperty(\"aicommitall.unitJacocoXmlReport\")"),
            "The aggregate report must accept a downloaded unit XML path.",
        )
        assertTrue(
            content.contains(".orElse(jacocoXmlReport)"),
            "The aggregate report must keep the local unit XML as the default input.",
        )
        assertTrue(
            content.contains("if (!jacocoAggregateUnitXmlReportOverride.isPresent)") &&
                content.contains("dependsOn(tasks.jacocoTestReport)"),
            "The aggregate report must not rerun jacocoTestReport when CI supplies a downloaded unit XML.",
        )
    }

    private fun ciWorkflow(): String = Files.readString(Path.of(".github", "workflows", "ci.yml"))

    private fun workflowJob(content: String, jobName: String): String {
        val marker = "  $jobName:\n"
        val start = content.indexOf(marker)

        assertTrue(start >= 0, "CI workflow must contain the '$jobName' job.")

        val nextJob = Regex("(?m)^  [A-Za-z0-9_-]+:\\s*$").find(content, start + marker.length)
        return if (nextJob == null) content.substring(start) else content.substring(start, nextJob.range.first)
    }
}
