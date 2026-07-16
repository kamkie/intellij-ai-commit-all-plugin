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
package pl.devopssolutions.aicommitall.integration

import com.intellij.driver.client.Driver
import com.intellij.driver.client.Remote
import com.intellij.driver.sdk.Project
import com.intellij.driver.sdk.getOpenProjects
import com.intellij.driver.sdk.openToolWindow
import com.intellij.driver.sdk.waitFor
import com.intellij.driver.sdk.waitForOne
import com.intellij.ide.starter.ci.CIServer
import com.intellij.ide.starter.ci.NoCIServer
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IDETestContext
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import com.intellij.platform.testFramework.teamCity.TeamCityReporter
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import com.intellij.tools.ide.starter.product.pycharm.PyCharm
import com.intellij.tools.ide.starter.product.webstorm.WebStorm
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import pl.devopssolutions.aicommitall.integration.fakeai.FakeAiAssistantProbe
import pl.devopssolutions.aicommitall.integration.fixtures.IntegrationGitCli
import pl.devopssolutions.aicommitall.integration.fixtures.ReleaseMatrixGitFixture
import pl.devopssolutions.aicommitall.integration.fixtures.ReleaseMatrixGitFixtureBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ReleaseMatrixUiHarnessTest {
    @TempDir
    lateinit var tempDirectory: Path

    init {
        di = DI {
            extend(di)
            bindSingleton<CIServer>(overrides = true) {
                object : CIServer by NoCIServer {
                    override fun reportTestFailure(
                        testName: String,
                        message: String,
                        details: String,
                        linkToLogs: String?,
                        kind: TeamCityReporter.SyntheticTestKind,
                        generifyTestName: Boolean,
                    ) {
                        val isIntellij2026Point2KnownError =
                            testName == INTELLIJ_2026_2_ISLANDS_THEME_KNOWN_ERROR ||
                                (
                                    testName.startsWith(INTELLIJ_2026_2_DTRACE_PROFILER_KNOWN_ERROR_PREFIX) &&
                                        details.contains(INTELLIJ_DTRACE_PROFILER_KNOWN_ERROR_STACK_FRAME)
                                    ) ||
                                isIntellij2026Point2WorkspaceCacheAccessDeniedError(testName, details)
                        if (System.getProperty("aicommitall.ide.version") == "2026.2" && isIntellij2026Point2KnownError) {
                            return
                        }
                        fail { "$testName failed: $message\n$details" }
                    }
                }
            }
        }
    }

    @Test
    fun fixtureBuilderCreatesReleaseMatrixGitStates() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.create(tempDirectory.resolve("fixture"))

        val primaryStatus = fixture.primaryRepository.statusLines()
        val ignoredStatus = fixture.primaryRepository.statusLines("--ignored")
        val secondaryStatus = fixture.secondaryRepository.statusLines()

        assertContains(primaryStatus, " M modified.txt")
        assertContains(primaryStatus, " D delete-me.txt")
        assertContains(primaryStatus, "R  rename-source.txt -> rename-target.txt")
        assertContains(primaryStatus, "M  already-staged.txt")
        assertContains(primaryStatus, "?? unversioned.txt")
        assertFalse(
            primaryStatus.any { line -> line.contains("ignored.txt") },
            "Ignored files must stay outside the committable fixture state.",
        )
        assertContains(ignoredStatus, "!! ignored.txt")
        assertContains(secondaryStatus, " M secondary-tracked.txt")
        assertContains(secondaryStatus, "?? secondary-unversioned.txt")
        assertEquals(
            fixture.bareRemote.root.toString(),
            fixture.primaryRepository.git("remote", "get-url", "origin").stdout.trim(),
        )
        assertEquals(
            "origin/main",
            fixture.primaryRepository.git("rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}").stdout.trim(),
        )
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun startsIdeWithPluginFakeAiDependencyAndGitFixture() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.create(tempDirectory.resolve("ide-fixture"))

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-harness",
            fixture = fixture,
        ) {
            val project = waitForReleaseMatrixProject()
            waitForProjectSmart(project)
            assertEquals("release-matrix-project", project.getName())
            assertTrue(
                utility(RemoteFakeAiAssistantProbe::class).isCommitMessageActionRegistered(),
                "Fake AI Assistant plugin did not register Vcs.LLMCommitMessageAction.",
            )
            assertEquals(
                "AI Commit All release matrix message",
                utility(RemoteFakeAiAssistantProbe::class).generatedCommitMessageThroughDataContext(),
            )
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun commitToolWindowShowsPluginControlAndScreenshots() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createClean(tempDirectory.resolve("clean-ide-fixture"))

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-control",
            fixture = fixture,
        ) {
            val project = waitForReleaseMatrixProject()
            waitForProjectSmart(project)
            val probe = utility(RemoteFakeAiAssistantProbe::class)

            openToolWindow(COMMIT_TOOL_WINDOW_ID)
            waitFor(
                message = "AI Commit All control is visible in the Commit tool window",
                timeout = 90.seconds,
                interval = 1.seconds,
            ) {
                probe.openCommitToolWindow(project) && probe.isIdeFrameAndAiCommitAllControlVisible(project)
            }
            waitFor(
                message = "standard Commit and Push toolbar action is absent",
                timeout = 30.seconds,
                interval = 1.seconds,
                errorMessage = { "Primary commit actions: ${probe.primaryCommitActionIds()}" },
            ) {
                !probe.primaryCommitActionsContain(IDE_COMMIT_AND_PUSH_ACTION_ID)
            }

            val control = visibleAiCommitAllControl(project)
            assertEquals(AI_COMMIT_ALL_CONTROL_ACCESSIBLE_NAME, probe.aiCommitAllControlAccessibleName(project))
            assertEquals(DISABLED_CONTROL_ACCESSIBLE_DESCRIPTION, probe.aiCommitAllControlAccessibleDescription(project))
            assertFalse(probe.isAiCommitAllControlEnabled(project), "A clean fixture should leave all sections disabled.")

            listOf("AI", "Commit", "Push").forEach { section ->
                clickAiCommitAllSection(project, section)
            }
            assertEquals(DISABLED_CONTROL_ACCESSIBLE_DESCRIPTION, probe.aiCommitAllControlAccessibleDescription(project))

            val screenshotDirectory = releaseMatrixReportDirectory()
                .resolve("screenshots")
                .resolve("commit-control")
            val screenshots = probe.writeAiCommitAllControlScreenshots(project, screenshotDirectory.toString())
            assertEquals(2, screenshots.size)
            screenshots.forEach { screenshot ->
                val screenshotPath = Path.of(screenshot)
                assertTrue(Files.isRegularFile(screenshotPath), "Missing screenshot: $screenshot")
                assertTrue(Files.size(screenshotPath) > 0, "Screenshot is empty: $screenshot")
            }
        }
    }

    @Test
    fun fixtureBuilderCreatesAllStagedReleaseMatrixGitState() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createAllStaged(tempDirectory.resolve("all-staged-fixture"))

        assertContains(fixture.primaryRepository.statusLines(), "M  modified.txt")
        assertContains(fixture.primaryRepository.statusLines(), "D  delete-me.txt")
        assertContains(fixture.primaryRepository.statusLines(), "R  rename-source.txt -> rename-target.txt")
        assertContains(fixture.primaryRepository.statusLines(), "A  unversioned.txt")
        assertContains(fixture.secondaryRepository.statusLines(), "M  secondary-tracked.txt")
        assertContains(fixture.secondaryRepository.statusLines(), "A  secondary-unversioned.txt")
        assertFalse(
            fixture.primaryRepository.statusLines("--ignored").any { line -> line.contains("ignored.txt") && !line.startsWith("!!") },
            "Ignored files must stay outside the staged fixture state.",
        )
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun vcsShortcutTakeoverCanBeToggledInReleaseMatrixIde() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitOnly(tempDirectory.resolve("shortcut-ide-fixture"))

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-shortcuts",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)

            val commitShortcutText = probe.registeredKeyboardShortcutText(AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID).orEmpty()
            val pushShortcutText = probe.registeredKeyboardShortcutText(AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID).orEmpty()
            assertTrue(commitShortcutText.contains("K", ignoreCase = true), "Unexpected commit shortcut: $commitShortcutText")
            assertTrue(pushShortcutText.contains("K", ignoreCase = true), "Unexpected push shortcut: $pushShortcutText")
            probe.setUseVcsShortcutsForAiCommitAll(true)
            assertTrue(probe.useVcsShortcutsForAiCommitAll())
            assertTrue(
                probe.isShortcutActionEnabled(project, AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID),
                "Commit shortcut takeover should be enabled when committable content is available.",
            )

            probe.setUseVcsShortcutsForAiCommitAll(false)
            assertFalse(probe.useVcsShortcutsForAiCommitAll())
            assertFalse(
                probe.isShortcutActionEnabled(project, AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID),
                "Commit shortcut takeover should be disabled when the setting is off.",
            )
            probe.setUseVcsShortcutsForAiCommitAll(true)
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun commitShortcutCreatesLocalCommitWhenTakeoverEnabled() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitOnly(tempDirectory.resolve("commit-shortcut-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-commit-shortcut",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            probe.setUseVcsShortcutsForAiCommitAll(true)
            assertTrue(probe.performAction(project, AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID))
            waitForPrimaryRepositoryCommit(
                fixture = fixture,
                initialCommitCount = initialCommitCount,
                expectedMessage = GENERATED_COMMIT_MESSAGE,
            )
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
            writeGitEvidence("commit-shortcut", fixture)
        }
    }

    @Test
    fun pushShortcutCommitsAndPushesToTemporaryBareRemoteWhenTakeoverEnabled() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitAndPush(tempDirectory.resolve("push-shortcut-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-push-shortcut",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            probe.setUseVcsShortcutsForAiCommitAll(true)
            assertTrue(probe.performAction(project, AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID))
            waitForPrimaryRepositoryCommit(
                fixture = fixture,
                initialCommitCount = initialCommitCount,
                expectedMessage = GENERATED_COMMIT_MESSAGE,
            )
            waitFor(
                message = "temporary bare remote receives AI Commit All shortcut push",
                timeout = 60.seconds,
                interval = 1.seconds,
                errorMessage = { "Remote HEAD stayed at ${fixture.bareRemote.remoteHead()}" },
            ) {
                fixture.bareRemote.remoteHead() != initialRemoteHead &&
                    fixture.bareRemote.remoteHead() == fixture.primaryRepository.head()
            }
            writeGitEvidence("push-shortcut", fixture)
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun pushShortcutPushesOutgoingOnlyLocalCommitWhenTakeoverEnabled() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createOutgoingOnly(tempDirectory.resolve("outgoing-only-shortcut-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()
        val outgoingHead = fixture.primaryRepository.head()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-outgoing-only-push-shortcut",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            probe.setUseVcsShortcutsForAiCommitAll(true)
            waitFor(
                message = "outgoing-only fixture enables push shortcut takeover",
                timeout = 60.seconds,
                interval = 1.seconds,
            ) {
                probe.hasOutgoingCommitsToPush(project) &&
                    probe.isShortcutActionEnabled(project, AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID)
            }
            assertTrue(probe.performAction(project, AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID))
            waitFor(
                message = "outgoing-only local commit reaches temporary bare remote through shortcut",
                timeout = 60.seconds,
                interval = 1.seconds,
                errorMessage = { "Remote HEAD stayed at ${fixture.bareRemote.remoteHead()}" },
            ) {
                fixture.bareRemote.remoteHead() != initialRemoteHead && fixture.bareRemote.remoteHead() == outgoingHead
            }
            assertEquals(initialCommitCount, fixture.primaryRepository.commitCount())
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
            writeGitEvidence("outgoing-only-push-shortcut", fixture)
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun aiSectionGeneratesCommitMessageWithoutCreatingCommit() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitOnly(tempDirectory.resolve("ai-only-flow-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-ai-only-flow",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            activateAiCommitAllSection(project, "AI")
            waitForCommitMessage(project, GENERATED_COMMIT_MESSAGE)
            assertEquals(initialCommitCount, fixture.primaryRepository.commitCount())
            assertTrue(
                fixture.primaryRepository.statusLines().isNotEmpty(),
                "AI-only generation must not consume the pending Git changes.",
            )
            writeGitEvidence("ai-only-flow", fixture)
        }
    }

    @Test
    fun missingFakeAiDependencyDisablesAiCommitAllPlugin() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createClean(tempDirectory.resolve("missing-fake-ai-dependency-fixture"))
        val probePluginPath = createReleaseMatrixProbePlugin(tempDirectory.resolve("release-matrix-probe-plugin.zip"))

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-missing-fake-ai-dependency",
            fixture = fixture,
            installFakeAiPlugin = false,
            extraDisabledPluginIds = setOf(FAKE_AI_ASSISTANT_PLUGIN_ID),
            extraPluginPaths = listOf(probePluginPath),
        ) {
            waitForReleaseMatrixProject()
            val probe = utility(RemoteReleaseMatrixProbe::class)

            assertFalse(
                probe.isAiCommitAllPluginEnabled(),
                "AI Commit All should be disabled when the AI Assistant dependency is absent.",
            )
            assertFalse(
                probe.isAiCommitAllThreeSectionActionRegistered(),
                "AI Commit All toolbar action should not be registered when the plugin is disabled.",
            )
            writeGitEvidence(
                name = "missing-fake-ai-dependency",
                fixture = fixture,
                details = mapOf("aiCommitAllPluginEnabled" to probe.isAiCommitAllPluginEnabled().toString()),
            )
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun missingAiActionStopsWithoutCommitOrPush() {
        fakeAiStopPathLeavesGitStateUnchanged(
            testNameSuffix = "missing-ai-action",
            expectFakeInvocation = false,
        ) { probe, _ ->
            probe.unregisterFakeAiAction()
        }
    }

    @Test
    fun unavailableAiCompletionSignalStopsWithoutCommitOrPush() {
        fakeAiStopPathLeavesGitStateUnchanged(
            testNameSuffix = "unavailable-ai-signal",
        ) { probe, _ ->
            probe.replaceFakeAiActionWithUnavailableSignal()
        }
    }

    @Test
    fun aiTimeoutStopsWithoutCommitOrPush() {
        fakeAiStopPathLeavesGitStateUnchanged(
            testNameSuffix = "ai-timeout",
        ) { probe, _ ->
            probe.setAiCompletionOptions(timeoutMillis = 1_000, checkIntervalMillis = 100)
            probe.setFakeAiBehavior("never-finishes")
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun emptyGeneratedMessageStopsWithoutCommitOrPush() {
        fakeAiStopPathLeavesGitStateUnchanged(
            testNameSuffix = "empty-message",
        ) { probe, _ ->
            probe.setAiCompletionOptions(timeoutMillis = 5_000, checkIntervalMillis = 100)
            probe.setFakeAiBehavior("empty")
        }
    }

    @Test
    fun unchangedPrefilledGeneratedMessageCommitsAndPushesWhenClearingIsDisabled() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitAndPush(
            tempDirectory.resolve("unchanged-prefilled-message-fixture"),
        )
        val initialCommitCount = fixture.primaryRepository.commitCount()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-unchanged-prefilled-message-push",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            waitForGitSelectionPaths(
                project = project,
                expectedPaths = setOf(
                    fixture.primaryRepository.root.resolve("modified.txt"),
                    fixture.primaryRepository.root.resolve("push-flow.txt"),
                ),
            )
            probe.setAiCompletionOptions(timeoutMillis = 5_000, checkIntervalMillis = 100)
            probe.setClearCommitMessageBeforeGeneration(false)
            assertTrue(probe.setCommitMessageText(project, EXISTING_COMMIT_MESSAGE))
            probe.setFakeAiBehavior("unchanged")
            activateAiCommitAllSection(project, "Push")
            waitForPrimaryRepositoryCommit(
                fixture = fixture,
                initialCommitCount = initialCommitCount,
                expectedMessage = EXISTING_COMMIT_MESSAGE,
            )
            waitFor(
                message = "temporary bare remote receives unchanged prefilled message push",
                timeout = 60.seconds,
                interval = 1.seconds,
                errorMessage = { "Remote HEAD stayed at ${fixture.bareRemote.remoteHead()}" },
            ) {
                fixture.bareRemote.remoteHead() != initialRemoteHead &&
                    fixture.bareRemote.remoteHead() == fixture.primaryRepository.head()
            }
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
            writeGitEvidence("unchanged-prefilled-message-push", fixture)
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun userEditedMessageStopsWithoutCommitOrPush() {
        fakeAiStopPathLeavesGitStateUnchanged(
            testNameSuffix = "user-edited-message",
            workflowIdleTimeout = 5.seconds,
            configure = { probe, _ ->
                probe.setAiCompletionOptions(timeoutMillis = 30_000, checkIntervalMillis = 100)
                probe.setFakeAiBehavior("never-finishes")
            },
            afterFakeInvocation = { probe, project ->
                assertTrue(probe.dispatchUserCommitMessageEdit(project, USER_EDITED_COMMIT_MESSAGE))
                assertEquals(USER_EDITED_COMMIT_MESSAGE, probe.commitMessageText(project))
            },
        )
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun commitSectionCreatesLocalCommitWithStagingAreaDisabled() {
        commitSectionCreatesLocalCommitThroughCommitToolWindow(stagingAreaEnabled = false)
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun commitSectionCreatesLocalCommitWithStagingAreaEnabled() {
        commitSectionCreatesLocalCommitThroughCommitToolWindow(stagingAreaEnabled = true)
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun stagingAreaAiInvocationSeesExactMultiRootCommitUiPaths() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.create(
            tempDirectory.resolve("staging-ai-paths-fixture"),
        )

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-staging-ai-paths",
            fixture = fixture,
        ) {
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            probe.setGitStagingAreaEnabled(true)
            val project = openReleaseMatrixCommitToolWindow()
            waitForCommitWorkflowMode(project, stagingAreaEnabled = true)
            val expectedStagingSelectionPaths = listOf(
                fixture.primaryRepository.root.resolve("modified.txt"),
                fixture.primaryRepository.root.resolve("delete-me.txt"),
                fixture.primaryRepository.root.resolve("rename-target.txt"),
                fixture.primaryRepository.root.resolve("already-staged.txt"),
                fixture.primaryRepository.root.resolve("unversioned.txt"),
                fixture.secondaryRepository.root.resolve("secondary-tracked.txt"),
                fixture.secondaryRepository.root.resolve("secondary-unversioned.txt"),
            ).map { path -> path.toString().replace('\\', '/') }.sorted()
            waitFor(
                message = "staging tracker exposes every intended fixture path",
                timeout = 60.seconds,
                interval = 100.milliseconds,
                errorMessage = { "paths=${probe.stagingAreaSelectionPaths(project)}" },
            ) {
                probe.stagingAreaSelectionPaths(project) == expectedStagingSelectionPaths
            }
            val initialInvocationCount = probe.fakeAiInvocationCount()

            activateAiCommitAllSection(project, "AI")
            waitFor(
                message = "fake AI action observes staging-area Commit UI paths",
                timeout = 60.seconds,
                interval = 100.milliseconds,
                errorMessage = {
                    "invocations=${probe.fakeAiInvocationCount()}, " +
                        "paths=${probe.commitUiPathsAtFakeAiInvocation()}"
                },
            ) {
                probe.fakeAiInvocationCount() > initialInvocationCount
            }

            val expectedPaths = listOf(
                fixture.primaryRepository.root.resolve("modified.txt"),
                fixture.primaryRepository.root.resolve("delete-me.txt"),
                fixture.primaryRepository.root.resolve("rename-source.txt"),
                fixture.primaryRepository.root.resolve("rename-target.txt"),
                fixture.primaryRepository.root.resolve("already-staged.txt"),
                fixture.primaryRepository.root.resolve("unversioned.txt"),
                fixture.secondaryRepository.root.resolve("secondary-tracked.txt"),
                fixture.secondaryRepository.root.resolve("secondary-unversioned.txt"),
            ).map { path -> path.toString().replace('\\', '/') }.sorted()
            assertEquals(expectedPaths, probe.commitUiPathsAtFakeAiInvocation())
            waitForCommitMessage(project, GENERATED_COMMIT_MESSAGE)
            waitForWorkflowIdle(project)
        }
    }

    private fun commitSectionCreatesLocalCommitThroughCommitToolWindow(stagingAreaEnabled: Boolean) {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitOnly(
            tempDirectory.resolve("commit-flow-fixture-${if (stagingAreaEnabled) "staging" else "changelists"}"),
        )
        val initialCommitCount = fixture.primaryRepository.commitCount()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-commit-flow-${if (stagingAreaEnabled) "staging" else "changelists"}",
            fixture = fixture,
        ) {
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            probe.setGitStagingAreaEnabled(stagingAreaEnabled)
            assertEquals(stagingAreaEnabled, probe.isGitStagingAreaEnabled())
            val project = openReleaseMatrixCommitToolWindow()
            waitForCommitWorkflowMode(project, stagingAreaEnabled)
            activateAiCommitAllSection(project, "Commit")
            waitForPrimaryRepositoryCommit(
                fixture = fixture,
                initialCommitCount = initialCommitCount,
                expectedMessage = GENERATED_COMMIT_MESSAGE,
                timeout = if (stagingAreaEnabled) 120.seconds else 60.seconds,
            )
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
            writeGitEvidence("commit-flow-${if (stagingAreaEnabled) "staging" else "changelists"}", fixture)
        }
    }

    @Test
    @Tag(RELEASE_MATRIX_SMOKE_TAG)
    fun pushSectionCommitsAndPushesToTemporaryBareRemote() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitAndPush(tempDirectory.resolve("push-flow-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-push-flow",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            activateAiCommitAllSection(project, "Push")
            waitForPrimaryRepositoryCommit(
                fixture = fixture,
                initialCommitCount = initialCommitCount,
                expectedMessage = GENERATED_COMMIT_MESSAGE,
            )
            waitFor(
                message = "temporary bare remote receives AI Commit All push",
                timeout = 60.seconds,
                interval = 1.seconds,
                errorMessage = { "Remote HEAD stayed at ${fixture.bareRemote.remoteHead()}" },
            ) {
                fixture.bareRemote.remoteHead() != initialRemoteHead &&
                    fixture.bareRemote.remoteHead() == fixture.primaryRepository.head()
            }
            writeGitEvidence("push-flow", fixture)
        }
    }

    @Test
    fun pushSectionPushesOutgoingOnlyLocalCommitToTemporaryBareRemote() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createOutgoingOnly(tempDirectory.resolve("outgoing-only-fixture"))
        val outgoingHead = fixture.primaryRepository.head()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-outgoing-only-push",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            waitFor(
                message = "outgoing-only fixture enables push workflow",
                timeout = 60.seconds,
                interval = 1.seconds,
            ) {
                probe.hasOutgoingCommitsToPush(project) && probe.isAiCommitAllControlEnabled(project)
            }
            activateAiCommitAllSection(project, "Push")
            waitFor(
                message = "outgoing-only local commit reaches temporary bare remote",
                timeout = 60.seconds,
                interval = 1.seconds,
                errorMessage = { "Remote HEAD stayed at ${fixture.bareRemote.remoteHead()}" },
            ) {
                fixture.bareRemote.remoteHead() != initialRemoteHead && fixture.bareRemote.remoteHead() == outgoingHead
            }
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
            writeGitEvidence("outgoing-only-push", fixture)
        }
    }

    private fun fakeAiStopPathLeavesGitStateUnchanged(
        testNameSuffix: String,
        expectFakeInvocation: Boolean = true,
        workflowIdleTimeout: Duration = 60.seconds,
        afterFakeInvocation: (RemoteFakeAiAssistantProbe, Project) -> Unit = { _, _ -> },
        configure: (RemoteFakeAiAssistantProbe, Project) -> Unit,
    ) {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitAndPush(
            tempDirectory.resolve("$testNameSuffix-fixture"),
        )
        val initialState = GitStateSnapshot.capture(fixture)

        runReleaseMatrixIdeWithFixture(
            testName = "release-matrix-ui-$testNameSuffix-stop",
            fixture = fixture,
        ) {
            val project = openReleaseMatrixCommitToolWindow()
            val probe = utility(RemoteFakeAiAssistantProbe::class)
            configure(probe, project)
            val initialInvocationCount = probe.fakeAiInvocationCount()
            activateAiCommitAllSection(project, "Push")
            if (expectFakeInvocation) {
                waitFor(
                    message = "fake AI action is invoked for $testNameSuffix",
                    timeout = 30.seconds,
                    interval = 100.milliseconds,
                    errorMessage = { "invocations=${probe.fakeAiInvocationCount()}" },
                ) {
                    probe.fakeAiInvocationCount() > initialInvocationCount
                }
                afterFakeInvocation(probe, project)
            }
            waitForWorkflowIdle(project, timeout = workflowIdleTimeout)
            initialState.assertUnchanged(fixture)
            writeGitEvidence(
                name = "$testNameSuffix-stop",
                fixture = fixture,
                details = initialState.evidence("initial"),
            )
        }
    }

    private fun Driver.openReleaseMatrixCommitToolWindow(): Project {
        val project = waitForReleaseMatrixProject()
        waitForProjectSmart(project)
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        openToolWindow(COMMIT_TOOL_WINDOW_ID)
        assertTrue(probe.openCommitToolWindow(project))
        waitFor(
            message = "IDE window and AI Commit All control are visible before clicking",
            timeout = 60.seconds,
            interval = 1.seconds,
        ) {
            probe.openCommitToolWindow(project) && probe.isIdeFrameAndAiCommitAllControlVisible(project)
        }
        waitFor(
            message = "AI Commit All control is enabled before workflow activation",
            timeout = 60.seconds,
            interval = 1.seconds,
            errorMessage = {
                "controlEnabled=${probe.isAiCommitAllControlEnabled(project)}, " +
                    "hasCommittableContent=${probe.hasCommittableContent(project)}, " +
                    "hasOutgoingCommits=${probe.hasOutgoingCommitsToPush(project)}, " +
                    "description=${probe.aiCommitAllControlAccessibleDescription(project)}"
            },
        ) {
            probe.isAiCommitAllControlEnabled(project)
        }
        return project
    }

    private fun Driver.visibleAiCommitAllControl(project: Project) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        waitFor(
            message = "IDE window and AI Commit All control are visible before in-process input dispatch",
            timeout = 30.seconds,
            interval = 1.seconds,
        ) {
            probe.openCommitToolWindow(project) && probe.isIdeFrameAndAiCommitAllControlVisible(project)
        }
    }

    private fun Driver.clickAiCommitAllSection(
        project: Project,
        section: String,
    ) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        visibleAiCommitAllControl(project)
        assertTrue(
            probe.clickAiCommitAllSection(project, section),
            "AI Commit All $section section was not visible for in-process click dispatch.",
        )
    }

    private fun Driver.activateAiCommitAllSection(
        project: Project,
        section: String,
    ) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        visibleAiCommitAllControl(project)
        assertTrue(
            probe.activateAiCommitAllSection(project, section),
            "AI Commit All $section section was not available for in-process action activation.",
        )
    }

    private fun Driver.waitForWorkflowIdle(
        project: Project,
        timeout: Duration = 60.seconds,
    ) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        waitFor(
            message = "AI Commit All workflow returns to idle",
            timeout = timeout,
            interval = 1.seconds,
            errorMessage = { "description=${probe.aiCommitAllControlAccessibleDescription(project)}" },
        ) {
            !probe.aiCommitAllControlAccessibleDescription(project).orEmpty().contains("running")
        }
    }

    private fun Driver.waitForGitSelectionPaths(
        project: Project,
        expectedPaths: Set<Path>,
    ) {
        val expected = expectedPaths
            .map { path -> path.toAbsolutePath().normalize().toString().replace('\\', '/') }
            .toSet()
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        waitFor(
            message = "release matrix Git selection contains the complete fixture",
            timeout = 60.seconds,
            interval = 1.seconds,
            errorMessage = { "expected=$expected, actual=${probe.gitSelectionPaths(project)}" },
        ) {
            probe.gitSelectionPaths(project).toSet().containsAll(expected)
        }
    }

    private fun Driver.waitForCommitWorkflowMode(
        project: Project,
        stagingAreaEnabled: Boolean,
    ) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        waitFor(
            message = "Commit workflow mode matches stagingAreaEnabled=$stagingAreaEnabled",
            timeout = 30.seconds,
            interval = 1.seconds,
            errorMessage = { "handler=${probe.commitWorkflowHandlerClassName(project)}" },
        ) {
            val handlerClass = probe.commitWorkflowHandlerClassName(project) ?: return@waitFor false
            handlerClass.isNotBlank() && (handlerClass.contains("GitStageCommitWorkflowHandler") == stagingAreaEnabled)
        }
    }

    private fun Driver.waitForCommitMessage(
        project: Project,
        expectedMessage: String,
    ) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        waitFor(
            message = "Commit message becomes '$expectedMessage'",
            timeout = 60.seconds,
            interval = 1.seconds,
            errorMessage = { "message=${probe.commitMessageText(project)}" },
        ) {
            probe.commitMessageText(project) == expectedMessage
        }
    }

    private fun waitForPrimaryRepositoryCommit(
        fixture: ReleaseMatrixGitFixture,
        initialCommitCount: Int,
        expectedMessage: String,
        timeout: Duration = 60.seconds,
    ) {
        waitFor(
            message = "AI Commit All creates local commit '$expectedMessage'",
            timeout = timeout,
            interval = 1.seconds,
            errorMessage = { "Latest commit: ${fixture.primaryRepository.latestCommitSubject()}" },
        ) {
            fixture.primaryRepository.commitCount() > initialCommitCount &&
                fixture.primaryRepository.latestCommitSubject() == expectedMessage
        }
    }

    private fun writeGitEvidence(
        name: String,
        fixture: ReleaseMatrixGitFixture,
        details: Map<String, String> = emptyMap(),
    ): Path {
        val evidenceDirectory = releaseMatrixReportDirectory().resolve("git-evidence")
        Files.createDirectories(evidenceDirectory)
        val evidenceFile = evidenceDirectory.resolve("$name.txt")
        val lines = buildList {
            add("name=$name")
            add("project=${fixture.projectDirectory}")
            add("primary.root=${fixture.primaryRepository.root}")
            add("primary.commitCount=${fixture.primaryRepository.commitCount()}")
            add("primary.head=${fixture.primaryRepository.head()}")
            add("primary.latestSubject=${fixture.primaryRepository.latestCommitSubject()}")
            add("primary.status=${fixture.primaryRepository.statusLines().joinToString(separator = "|")}")
            add("remote.root=${fixture.bareRemote.root}")
            add("remote.main=${fixture.bareRemote.remoteHead()}")
            details.toSortedMap().forEach { (key, value) -> add("$key=$value") }
        }
        Files.write(evidenceFile, lines)
        return evidenceFile
    }

    private fun releaseMatrixReportDirectory(): Path = Path.of(
        "build",
        "reports",
        "releaseMatrixUiTest",
        releaseMatrixIdeProductCode(),
    ).toAbsolutePath()

    private fun runReleaseMatrixIdeWithFixture(
        testName: String,
        fixture: ReleaseMatrixGitFixture,
        installFakeAiPlugin: Boolean = true,
        extraDisabledPluginIds: Set<String> = emptySet(),
        extraPluginPaths: List<Path> = emptyList(),
        block: Driver.() -> Unit,
    ) {
        val ideProductCode = releaseMatrixIdeProductCode()
        val ideVersion = requiredSystemProperty("aicommitall.ide.version")
        val pluginPath = Path.of(requiredSystemProperty("path.to.build.plugin"))
        val fakeAiPluginPath = Path.of(requiredSystemProperty("aicommitall.fake.ai.plugin.path"))

        Starter.newContext(
            testName = testName,
            testCase = TestCase(
                ideProductProvider(ideProductCode),
                LocalProjectInfo(fixture.projectDirectory),
            ).withVersion(ideVersion),
        ).apply {
            attachCoverageAgentIfRequested()
            val pluginConfigurator = PluginConfigurator(this)
            Files.deleteIfExists(pluginConfigurator.disabledPluginsPath)
            pluginConfigurator
                .disablePlugins(RELEASE_MATRIX_DISABLED_PLUGIN_IDS + extraDisabledPluginIds)
                .apply {
                    extraPluginPaths.forEach { pluginPath -> installPluginFromPath(pluginPath) }
                    if (installFakeAiPlugin) {
                        installPluginFromPath(fakeAiPluginPath)
                    }
                }
                .installPluginFromPath(pluginPath)
        }.runIdeWithDriver().useDriverAndCloseIde {
            if (installFakeAiPlugin) {
                utility(RemoteFakeAiAssistantProbe::class).resetReleaseMatrixSettings()
            }
            var blockCompleted = false
            try {
                block()
                blockCompleted = true
            } finally {
                if (installFakeAiPlugin && blockCompleted) {
                    waitForOpenProjectSmart()
                }
            }
        }
    }

    private fun IDETestContext.attachCoverageAgentIfRequested() {
        val agentJar = System.getProperty("aicommitall.coverage.agent.jar")
            ?.takeIf { value -> value.isNotBlank() } ?: return
        val execFile = System.getProperty("aicommitall.coverage.exec.file")
            ?.takeIf { value -> value.isNotBlank() } ?: return
        val classDumpDir = System.getProperty("aicommitall.coverage.class.dump.dir")
            ?.takeIf { value -> value.isNotBlank() }
            ?.let(Path::of)
            ?: error("JaCoCo class dump directory is required when release-matrix UI coverage is enabled.")
        if (!Files.isRegularFile(Path.of(agentJar))) {
            return
        }
        Path.of(execFile).parent?.let { parent -> Files.createDirectories(parent) }
        Files.createDirectories(classDumpDir)
        applyVMOptionsPatch {
            addLine(
                line = "-javaagent:$agentJar=destfile=$execFile,append=true,output=file," +
                    "includes=pl.devopssolutions.aicommitall.*," +
                    "excludes=pl.devopssolutions.aicommitall.integration.*," +
                    "inclnolocationclasses=true," +
                    "classdumpdir=$classDumpDir",
                filterPrefix = "-javaagent:$agentJar",
            )
        }
    }

    private fun createReleaseMatrixProbePlugin(outputFile: Path): Path {
        val classesRoot = Path.of(FakeAiAssistantProbe::class.java.protectionDomain.codeSource.location.toURI())
        val packageRoot = classesRoot.resolve("pl/devopssolutions/aicommitall/integration/fakeai")
        require(Files.isDirectory(packageRoot)) {
            "Fake AI probe classes directory was not found: $packageRoot"
        }

        val jarFile = outputFile.parent.resolve("release-matrix-probe-plugin.jar")
        ZipOutputStream(Files.newOutputStream(jarFile)).use { jar ->
            jar.putTextEntry("META-INF/plugin.xml", RELEASE_MATRIX_PROBE_PLUGIN_XML)
            Files.walk(packageRoot).use { paths ->
                paths
                    .filter(Files::isRegularFile)
                    .forEach { classFile ->
                        val entryName = "pl/devopssolutions/aicommitall/integration/fakeai/" +
                            packageRoot.relativize(classFile).toString().replace(File.separatorChar, '/')
                        jar.putFileEntry(entryName, classFile)
                    }
            }
        }

        ZipOutputStream(Files.newOutputStream(outputFile)).use { zip ->
            zip.putFileEntry(
                "release-matrix-probe-plugin/lib/release-matrix-probe-plugin.jar",
                jarFile,
            )
        }
        return outputFile
    }

    private fun ZipOutputStream.putTextEntry(
        name: String,
        text: String,
    ) {
        putNextEntry(ZipEntry(name))
        write(text.toByteArray(StandardCharsets.UTF_8))
        closeEntry()
    }

    private fun ZipOutputStream.putFileEntry(
        name: String,
        source: Path,
    ) {
        putNextEntry(ZipEntry(name))
        Files.newInputStream(source).use { input -> input.copyTo(this) }
        closeEntry()
    }

    private fun Driver.waitForReleaseMatrixProject(): Project = waitForOne(
        message = "release matrix project opens",
        timeout = 60.seconds,
        interval = 1.seconds,
        getter = { getOpenProjects() },
        checker = { project -> project.getName() == "release-matrix-project" },
    )

    private fun Driver.waitForOpenProjectSmart() {
        getOpenProjects()
            .firstOrNull { project -> project.getName() == "release-matrix-project" }
            ?.let { project -> waitForProjectSmart(project) }
    }

    private fun Driver.waitForProjectSmart(project: Project) {
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        waitFor(
            message = "release matrix project indexing is idle",
            timeout = 120.seconds,
            interval = 1.seconds,
        ) {
            probe.isProjectSmart(project)
        }
    }

    private fun releaseMatrixIdeProductCode(): String = requiredSystemProperty("aicommitall.ide.product")

    private fun ideProductProvider(productCode: String): IdeInfo = when (productCode) {
        "IU" -> IdeInfo.IdeaUltimate
        "PY" -> IdeInfo.PyCharm
        "WS" -> IdeInfo.WebStorm
        else -> error("Unsupported release-matrix IDE product: $productCode")
    }

    private fun requiredSystemProperty(name: String): String {
        val value = System.getProperty(name)?.takeIf { propertyValue -> propertyValue.isNotBlank() }
        return requireNotNull(value) {
            "Missing required system property: $name"
        }
    }
}

@Remote("pl.devopssolutions.aicommitall.integration.fakeai.FakeAiAssistantProbe", plugin = "com.intellij.ml.llm")
private interface RemoteFakeAiAssistantProbe {
    fun isCommitMessageActionRegistered(): Boolean
    fun primaryCommitActionsContain(actionId: String): Boolean
    fun primaryCommitActionIds(): List<String>
    fun openCommitToolWindow(project: Project): Boolean
    fun activateCommitToolWindow(project: Project): Boolean
    fun isAiCommitAllControlShowing(project: Project): Boolean
    fun isIdeFrameAndAiCommitAllControlVisible(project: Project): Boolean
    fun clickAiCommitAllSection(project: Project, section: String): Boolean
    fun activateAiCommitAllSection(project: Project, section: String): Boolean
    fun aiCommitAllControlAccessibleName(project: Project): String?
    fun aiCommitAllControlAccessibleDescription(project: Project): String?
    fun isAiCommitAllControlEnabled(project: Project): Boolean
    fun writeAiCommitAllControlScreenshots(
        project: Project,
        outputDirectory: String,
    ): List<String>

    fun generatedCommitMessageThroughDataContext(): String
    fun isProjectSmart(project: Project): Boolean
    fun registeredKeyboardShortcutText(actionId: String): String?
    fun setUseVcsShortcutsForAiCommitAll(enabled: Boolean)
    fun useVcsShortcutsForAiCommitAll(): Boolean
    fun isShortcutActionEnabled(
        project: Project,
        actionId: String,
    ): Boolean

    fun hasOutgoingCommitsToPush(project: Project): Boolean
    fun hasCommittableContent(project: Project): Boolean
    fun stagingAreaSelectionPaths(project: Project): List<String>
    fun gitSelectionPaths(project: Project): List<String>
    fun setGitStagingAreaEnabled(enabled: Boolean)
    fun isGitStagingAreaEnabled(): Boolean
    fun commitWorkflowHandlerClassName(project: Project): String?
    fun commitMessageText(project: Project): String?
    fun performAction(project: Project, actionId: String): Boolean
    fun resetReleaseMatrixSettings()
    fun setAiCompletionOptions(timeoutMillis: Long, checkIntervalMillis: Long)
    fun setClearCommitMessageBeforeGeneration(enabled: Boolean)
    fun setFakeAiBehavior(behaviorName: String)
    fun fakeAiInvocationCount(): Int
    fun commitUiPathsAtFakeAiInvocation(): List<String>
    fun unregisterFakeAiAction()
    fun replaceFakeAiActionWithUnavailableSignal()
    fun setCommitMessageText(project: Project, message: String): Boolean
    fun dispatchUserCommitMessageEdit(project: Project, message: String): Boolean
}

@Remote("pl.devopssolutions.aicommitall.integration.fakeai.FakeAiAssistantProbe", plugin = RELEASE_MATRIX_PROBE_PLUGIN_ID)
private interface RemoteReleaseMatrixProbe {
    fun isAiCommitAllPluginEnabled(): Boolean
    fun isAiCommitAllThreeSectionActionRegistered(): Boolean
}

private data class GitStateSnapshot(
    private val commitCount: Int,
    private val head: String,
    private val statusLines: List<String>,
    private val remoteHead: String,
) {
    fun assertUnchanged(fixture: ReleaseMatrixGitFixture) {
        assertEquals(commitCount, fixture.primaryRepository.commitCount())
        assertEquals(head, fixture.primaryRepository.head())
        assertEquals(statusLines, fixture.primaryRepository.statusLines())
        assertEquals(remoteHead, fixture.bareRemote.remoteHead())
    }

    fun evidence(prefix: String): Map<String, String> = mapOf(
        "$prefix.primary.commitCount" to commitCount.toString(),
        "$prefix.primary.head" to head,
        "$prefix.primary.status" to statusLines.joinToString(separator = "|"),
        "$prefix.remote.main" to remoteHead,
    )

    companion object {
        fun capture(fixture: ReleaseMatrixGitFixture): GitStateSnapshot = GitStateSnapshot(
            commitCount = fixture.primaryRepository.commitCount(),
            head = fixture.primaryRepository.head(),
            statusLines = fixture.primaryRepository.statusLines(),
            remoteHead = fixture.bareRemote.remoteHead(),
        )
    }
}

private val RELEASE_MATRIX_DISABLED_PLUGIN_IDS = setOf(
    "Docker",
    "com.intellij.kubernetes",
    "org.jetbrains.plugins.docker.gateway",
)
private const val AI_COMMIT_ALL_CONTROL_ACCESSIBLE_NAME = "AI Commit All"
private const val AI_COMMIT_ALL_CONTROL_CLASS_NAME =
    "pl.devopssolutions.aicommitall.actions.AiCommitAllThreeSectionControl"
private const val COMMIT_TOOL_WINDOW_ID = "Commit"
private const val DISABLED_CONTROL_ACCESSIBLE_DESCRIPTION =
    "AI, Commit, and Push sections; no sections are enabled"
private const val IDE_COMMIT_AND_PUSH_ACTION_ID = "Git.Commit.And.Push.Executor"
private const val AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID = "pl.devopssolutions.aicommitall.actions.CommitShortcut"
private const val AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID = "pl.devopssolutions.aicommitall.actions.PushShortcut"
private const val GENERATED_COMMIT_MESSAGE = "AI Commit All release matrix message"
private const val EXISTING_COMMIT_MESSAGE = "Existing release matrix message"
private const val USER_EDITED_COMMIT_MESSAGE = "User edited release matrix message"
private const val FAKE_AI_ASSISTANT_PLUGIN_ID = "com.intellij.ml.llm"
private const val RELEASE_MATRIX_SMOKE_TAG = "releaseMatrixSmoke"
private const val RELEASE_MATRIX_PROBE_PLUGIN_ID = "pl.devopssolutions.aicommitall.integration.probe"
private const val INTELLIJ_2026_2_ISLANDS_THEME_KNOWN_ERROR =
    "java.lang.Throwable: Theme Islands Dark refers to unknown color scheme Islands Dark"
private const val INTELLIJ_2026_2_DTRACE_PROFILER_KNOWN_ERROR_PREFIX =
    "java.lang.Throwable: Can't write state 'displayName = JVM DTrace based profiler"
private const val INTELLIJ_DTRACE_PROFILER_KNOWN_ERROR_STACK_FRAME =
    "com.intellij.profiler.api.configurations.ProfilerRunConfigurationsManager.writeConfigurationSafely"
private const val INTELLIJ_WORKSPACE_CACHE_ACCESS_DENIED_PREFIX = "java.nio.file.AccessDeniedException: "
private const val INTELLIJ_WORKSPACE_CACHE_SOURCE_PATH = "\\project-model-cache\\cache"
private const val INTELLIJ_WORKSPACE_CACHE_MOVE_SEPARATOR = ".tmp -> "
private const val INTELLIJ_WORKSPACE_CACHE_TARGET_PATH = "\\project-model-cache\\cache.data"
private const val INTELLIJ_WORKSPACE_CACHE_SAVE_STACK_FRAME =
    "com.intellij.workspaceModel.ide.impl.WorkspaceModelCacheSerializer.saveCacheToFile"

private fun isIntellij2026Point2WorkspaceCacheAccessDeniedError(
    testName: String,
    details: String,
): Boolean = System.getProperty("os.name").startsWith("Windows", ignoreCase = true) &&
    testName.startsWith(INTELLIJ_WORKSPACE_CACHE_ACCESS_DENIED_PREFIX) &&
    testName.contains(INTELLIJ_WORKSPACE_CACHE_SOURCE_PATH) &&
    testName.contains(INTELLIJ_WORKSPACE_CACHE_MOVE_SEPARATOR) &&
    testName.endsWith(INTELLIJ_WORKSPACE_CACHE_TARGET_PATH) &&
    details.contains(INTELLIJ_WORKSPACE_CACHE_SAVE_STACK_FRAME)

private val RELEASE_MATRIX_PROBE_PLUGIN_XML = """
    <idea-plugin>
        <id>$RELEASE_MATRIX_PROBE_PLUGIN_ID</id>
        <name>AI Commit All Release Matrix Probe</name>
        <version>0.0.1-test</version>
        <depends>com.intellij.modules.platform</depends>
        <depends>com.intellij.modules.vcs</depends>
        <depends>Git4Idea</depends>
    </idea-plugin>
""".trimIndent()
