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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GitHubReleaseWorkflowTest {
    @Test
    fun `release matrix UI workflow uploads Starter IDE logs`() {
        val content = Files.readString(Path.of(".github", "workflows", "release-matrix-ui.yml"))

        assertTrue(
            content.contains("out/ide-tests/tests/**/log/**"),
            "Release matrix UI workflow must upload JetBrains Starter IDE logs and screenshots.",
        )
        assertTrue(
            content.contains("build/jacoco/releaseMatrixUiClassDump/**"),
            "Release matrix UI workflow must upload JaCoCo classdumpdir output for coverage diagnosis.",
        )
        assertTrue(
            content.contains("build/jacoco/releaseMatrixUiIde.exec"),
            "Release matrix UI workflow must upload the IDE JaCoCo exec file for coverage diagnosis.",
        )
        assertTrue(
            content.contains("Verify release-matrix UI coverage data") &&
                content.contains("test -s build/jacoco/releaseMatrixUiIde.exec") &&
                content.contains("find build/jacoco/releaseMatrixUiClassDump -name '*.class'"),
            "Release matrix UI workflow must prove the IDE JaCoCo exec and dumped classes exist.",
        )
        assertTrue(
            content.contains("build/reports/jacoco/jacocoAggregateReport/**"),
            "Release matrix UI workflow must keep the aggregate JaCoCo report in the evidence artifact.",
        )
        assertFalse(
            content.contains("Upload release-matrix UI coverage to Codecov") ||
                content.contains("codecov/codecov-action@") ||
                content.contains("report_type: coverage"),
            "Release matrix UI workflow must not add extra Codecov sessions to release commits.",
        )
        assertFalse(
            content.contains("hashFiles('build/jacoco/releaseMatrixUiTest.exec')"),
            "Release matrix UI workflow must not infer IDE coverage from the Gradle test worker exec file.",
        )
    }

    @Test
    fun `release matrix UI workflow runs on semantic version tag pushes`() {
        val content = Files.readString(Path.of(".github", "workflows", "release-matrix-ui.yml"))

        listOf(
            "push:\n    tags:\n      - 'v*.*.*'",
            "workflow_dispatch:",
            "IDE_PRODUCTS: \${{ inputs.ide-products || 'IU,PY,WS' }}",
            "-PideVersion=\"\${{ inputs.ide-version || '2026.1.2' }}\"",
        ).forEach { snippet ->
            assertTrue(content.contains(snippet), "Release matrix UI workflow is missing: $snippet")
        }
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
            content.contains("uses: actions/setup-node@"),
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
            "uses: actions/setup-java@",
            "uses: gradle/actions/setup-gradle@",
            "validate-wrappers: true",
            "Build release distribution",
            "./gradlew verifyPluginStructure buildPlugin",
            "GH_TOKEN: \${{ secrets.GITHUB_TOKEN }}",
            "gh release view \"\${RELEASE_TAG}\"",
            "gh release edit \"\${RELEASE_TAG}\"",
            "gh release create \"\${RELEASE_TAG}\"",
            "--notes-file \"\${RELEASE_NOTES}\"",
            "--verify-tag",
            "distribution_files=(build/distributions/*.zip)",
            "Expected exactly one distribution ZIP",
            "gh release upload \"\${RELEASE_TAG}\" \"\${distribution_files[0]}\" --clobber",
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
        assertTrue(
            content.indexOf("Build release distribution") < content.lastIndexOf("Create or update GitHub Release"),
            "GitHub Release workflow must build the distribution ZIP before attaching release assets.",
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
}
