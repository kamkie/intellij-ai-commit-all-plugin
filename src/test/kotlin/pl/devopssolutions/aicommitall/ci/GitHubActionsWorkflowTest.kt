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

private const val GRADLE_TASK_ROOT = "buildSrc/src/main/kotlin/pl/devopssolutions/aicommitall/gradle"

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
            content.contains("uses: codecov/codecov-action@"),
            "CI workflow must use the Codecov action line.",
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
    fun `ci workflow validates repository documentation and agent artifacts`() {
        val workflow = Files.readString(Path.of(".github", "workflows", "ci.yml"))
        val docsValidator = Files.readString(Path.of("scripts", "validate-docs.ps1"))

        assertTrue(
            workflow.contains("Validate documentation and agent artifacts"),
            "CI workflow must name the combined documentation and agent-artifact validation gate.",
        )
        assertTrue(
            workflow.contains("scripts/validate-docs.ps1"),
            "CI workflow must run the full repository documentation validator.",
        )
        assertTrue(
            docsValidator.contains("scripts/ai/validate-agent-artifacts.ps1"),
            "The full documentation validator must keep repository agent-artifact validation in scope.",
        )
        assertTrue(
            workflow.indexOf("Validate documentation and agent artifacts") <
                workflow.indexOf("Run Detekt static analysis"),
            "CI workflow must validate repository docs and agent artifacts before static analysis.",
        )
    }

    @Test
    fun `jacoco reports use matching production class bytes for each coverage source`() {
        val content = Files.readString(Path.of("build.gradle.kts"))

        assertTrue(
            content.contains("classDirectories.setFrom(instrumentedClassesDir)"),
            "Unit JaCoCo reports must keep using the IntelliJ-instrumented production classes.",
        )
        assertTrue(
            content.contains("val integrationCoverageClassDumpDir = layout.buildDirectory.dir(\"jacoco/releaseMatrixUiClassDump\")"),
            "The release-matrix UI lane must keep JaCoCo classdumpdir output under build/jacoco.",
        )
        assertTrue(
            content.contains("val integrationCoverageExecFile = layout.buildDirectory.file(\"jacoco/releaseMatrixUiIde.exec\")"),
            "The IDE JaCoCo agent must write to an exec file distinct from the Gradle releaseMatrixUiTest task.",
        )
        assertFalse(
            content.contains("jacoco/releaseMatrixUiTest.exec"),
            "The IDE JaCoCo agent must not share the Gradle releaseMatrixUiTest task's default exec path.",
        )
        assertTrue(
            content.contains("val cleanReleaseMatrixUiCoverage by tasks.registering(Delete::class)") &&
                content.contains("delete(integrationCoverageExecFile, integrationCoverageClassDumpDir)") &&
                content.contains("dependsOn(cleanReleaseMatrixUiCoverage, extractJacocoAgentJar)"),
            "The release-matrix UI lane must remove stale IDE coverage before attaching the JaCoCo agent.",
        )
        assertTrue(
            content.contains("val jacocoIntegrationReport by tasks.registering(JacocoReport::class)") &&
                content.contains("executionData(integrationCoverageExecFile)") &&
                content.contains("classDirectories.setFrom(integrationCoverageClassDumpDir)"),
            "Integration JaCoCo reports must match the IDE JVM's dumped class bytes.",
        )
        assertTrue(
            content.contains("tasks.registering(MergeJacocoXmlReportsTask::class)") &&
                content.contains("reportFiles.from(jacocoXmlReport, jacocoIntegrationXmlReport)") &&
                content.contains("outputReportFile.set(jacocoAggregateXmlReport)"),
            "The aggregate report must merge unit XML with the dumped-class integration XML.",
        )
        assertTrue(
            content.contains("isIncludeNoLocationClasses = true"),
            "JaCoCo must instrument plugin classes loaded through the IntelliJ test classloader.",
        )
        assertTrue(
            content.contains("isEnabled = false"),
            "The Gradle releaseMatrixUiTest worker must not create a misleading host-JVM JaCoCo exec file.",
        )
        assertTrue(
            content.contains("verifyJacocoCoverageReport"),
            "The build must fail when the JaCoCo XML report contains no executed production coverage.",
        )
    }

    @Test
    fun `release matrix harness passes jacoco class dump directory to starter ide`() {
        val content = Files.readString(
            Path.of(
                "src",
                "integrationTest",
                "kotlin",
                "pl",
                "devopssolutions",
                "aicommitall",
                "integration",
                "ReleaseMatrixUiHarnessTest.kt",
            ),
        )

        assertTrue(
            content.contains("aicommitall.coverage.class.dump.dir"),
            "The release-matrix harness must accept the Gradle-provided JaCoCo class dump directory.",
        )
        assertTrue(
            content.contains("Files.createDirectories(classDumpDir)"),
            "The harness must create the JaCoCo classdumpdir before launching the IDE.",
        )
        assertTrue(
            content.contains("classdumpdir=\$classDumpDir"),
            "The IDE JaCoCo agent must dump the transformed classes it records in the exec file.",
        )
        assertTrue(
            content.contains("inclnolocationclasses=true"),
            "The IDE JaCoCo agent must include plugin classes loaded without protection-domain source locations.",
        )
        assertTrue(
            content.contains("excludes=pl.devopssolutions.aicommitall.integration.*"),
            "The IDE JaCoCo agent must leave test-only integration probe classes uninstrumented.",
        )
    }

    @Test
    fun `gradle build enables configuration cache`() {
        val properties = Files.readString(Path.of("gradle.properties"))
        val build = Files.readString(Path.of("build.gradle.kts"))
        val buildLogic = Files.readString(gradleVerificationTask("VerifyJacocoCoverageReportTask.kt"))

        assertTrue(
            properties.contains("org.gradle.configuration-cache=true"),
            "Gradle must enable the configuration cache persistently.",
        )
        assertTrue(
            buildLogic.contains("abstract class VerifyJacocoCoverageReportTask : DefaultTask()"),
            "Custom verification tasks must live in buildSrc task types instead of root script implementations.",
        )
        assertTrue(
            build.contains("tasks.registering(VerifyJacocoCoverageReportTask::class)"),
            "JaCoCo coverage verification must use the cache-compatible task type.",
        )
        assertFalse(
            build.contains("abstract class VerifyJacocoCoverageReportTask"),
            "The root build script must keep custom verification task registration separate from task implementation.",
        )
    }

    @Test
    fun `gradle configures detekt static analysis reports`() {
        val content = Files.readString(Path.of("build.gradle.kts"))
        val buildLogic = Files.readString(gradleVerificationTask("VerifyDetektBaselineTask.kt"))
        val detektPluginDeclaration = Regex("id\\(\"dev\\.detekt\"\\) version \"[^\"]+\"")
        val detektToolVersionDeclaration = Regex("toolVersion = \"[^\"]+\"")

        assertTrue(
            detektPluginDeclaration.containsMatchIn(content),
            "The build must apply the Detekt Gradle plugin with an explicit version.",
        )
        assertTrue(
            detektToolVersionDeclaration.containsMatchIn(content),
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
            buildLogic.contains("abstract class VerifyDetektBaselineTask : DefaultTask()"),
            "The build must define the configuration-cache-compatible Detekt baseline guard task in buildSrc.",
        )
        assertTrue(
            content.contains("tasks.registering(VerifyDetektBaselineTask::class)"),
            "The build must register the Detekt baseline guard through Gradle task registration.",
        )
        assertFalse(
            content.contains("abstract class VerifyDetektBaselineTask"),
            "The root build script must keep the Detekt baseline task implementation out of the root script.",
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
            content.contains("uses: github/codeql-action/upload-sarif@"),
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
            content.contains("uses: aquasecurity/trivy-action@"),
            "Security workflow must run the Trivy GitHub Action.",
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
            content.contains("uses: github/codeql-action/upload-sarif@"),
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
        val collectTestResultsStep = workflowStep(content, "Collect Codecov test result files")
        val uploadTestResultsStep = workflowStep(content, "Upload test results to Codecov")

        assertFalse(
            content.contains("codecov/test-results-action@"),
            "CI workflow must not use the deprecated Codecov test-results action.",
        )
        assertTrue(
            collectTestResultsStep.contains("if: \${{ !env.ACT && !cancelled()"),
            "CI workflow must collect test results even after test failures when the workflow is not cancelled.",
        )
        assertTrue(
            uploadTestResultsStep.contains("if: \${{ !env.ACT && !cancelled()"),
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
            content.contains("uses: codecov/codecov-action@"),
            "CI workflow must upload test results through the Codecov action.",
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
            content.contains("uses: dorny/test-reporter@"),
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
            content.contains("uses: actions/upload-artifact@"),
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

    private fun gradleVerificationTask(fileName: String): Path = Path.of(GRADLE_TASK_ROOT).resolve(fileName)

    private fun workflowStep(content: String, stepName: String): String {
        val marker = "      - name: $stepName"
        val start = content.indexOf(marker)

        assertTrue(start >= 0, "CI workflow must contain the '$stepName' step.")

        val nextStep = content.indexOf("\n      - name:", start + marker.length)
        return if (nextStep == -1) content.substring(start) else content.substring(start, nextStep)
    }
}
