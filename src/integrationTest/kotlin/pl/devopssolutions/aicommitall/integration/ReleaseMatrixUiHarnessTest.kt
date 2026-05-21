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
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.ui
import com.intellij.driver.sdk.waitFor
import com.intellij.driver.sdk.waitForOne
import com.intellij.ide.starter.ci.CIServer
import com.intellij.ide.starter.ci.NoCIServer
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import pl.devopssolutions.aicommitall.integration.fixtures.IntegrationGitCli
import pl.devopssolutions.aicommitall.integration.fixtures.ReleaseMatrixGitFixture
import pl.devopssolutions.aicommitall.integration.fixtures.ReleaseMatrixGitFixtureBuilder
import java.awt.Point
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
                    ) {
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
    fun startsIdeaWithPluginFakeAiDependencyAndGitFixture() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.create(tempDirectory.resolve("ide-fixture"))

        runIdeaWithFixture(
            testName = "release-matrix-ui-harness",
            fixture = fixture,
        ) {
            val project = waitForReleaseMatrixProject()
            assertEquals("release-matrix-project", project.getName())
            assertTrue(
                utility(RemoteFakeAiAssistantProbe::class).isCommitMessageActionRegistered(),
                "Fake AI Assistant plugin did not register Vcs.LLMCommitMessageAction.",
            )
            assertEquals(
                "AI Commit All release matrix message",
                withWriteAction {
                    utility(RemoteFakeAiAssistantProbe::class).generatedCommitMessageThroughDataContext()
                },
            )
        }
    }

    @Test
    fun commitToolWindowShowsPluginControlAndScreenshots() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createClean(tempDirectory.resolve("clean-ide-fixture"))

        runIdeaWithFixture(
            testName = "release-matrix-ui-control",
            fixture = fixture,
        ) {
            val project = waitForReleaseMatrixProject()
            val probe = utility(RemoteFakeAiAssistantProbe::class)

            openToolWindow(COMMIT_TOOL_WINDOW_ID)
            assertTrue(probe.activateCommitToolWindow(project))
            waitFor(
                message = "AI Commit All control is visible in the Commit tool window",
                timeout = 60.seconds,
                interval = 1.seconds,
            ) {
                probe.isAiCommitAllControlShowing(project)
            }
            waitFor(
                message = "standard Commit and Push toolbar action is absent",
                timeout = 30.seconds,
                interval = 1.seconds,
                errorMessage = { "Primary commit actions: ${probe.primaryCommitActionIds()}" },
            ) {
                !probe.primaryCommitActionsContain(IDE_COMMIT_AND_PUSH_ACTION_ID)
            }

            val control = ui.x { byJavaClass(AI_COMMIT_ALL_CONTROL_CLASS_NAME) }
            assertEquals(AI_COMMIT_ALL_CONTROL_ACCESSIBLE_NAME, control.component.getAccessibleContext()?.getAccessibleName())
            assertEquals(AI_COMMIT_ALL_CONTROL_ACCESSIBLE_NAME, probe.aiCommitAllControlAccessibleName(project))
            assertEquals(DISABLED_CONTROL_ACCESSIBLE_DESCRIPTION, probe.aiCommitAllControlAccessibleDescription(project))
            assertFalse(probe.isAiCommitAllControlEnabled(project), "A clean fixture should leave all sections disabled.")

            listOf("AI", "Commit", "Push").forEach { section ->
                control.clickSection(section)
            }
            assertEquals(DISABLED_CONTROL_ACCESSIBLE_DESCRIPTION, probe.aiCommitAllControlAccessibleDescription(project))

            val screenshotDirectory = Path.of(
                "build",
                "reports",
                "releaseMatrixUiTest",
                "screenshots",
                "commit-control",
            ).toAbsolutePath()
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
    fun vcsShortcutTakeoverCanBeToggledInReleaseMatrixIde() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitOnly(tempDirectory.resolve("shortcut-ide-fixture"))

        runIdeaWithFixture(
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
    fun commitSectionCreatesLocalCommitThroughCommitToolWindow() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitOnly(tempDirectory.resolve("commit-flow-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()

        runIdeaWithFixture(
            testName = "release-matrix-ui-commit-flow",
            fixture = fixture,
        ) {
            openReleaseMatrixCommitToolWindow()
            ui.x { byJavaClass(AI_COMMIT_ALL_CONTROL_CLASS_NAME) }.clickSection("Commit")
            waitForPrimaryRepositoryCommit(
                fixture = fixture,
                initialCommitCount = initialCommitCount,
                expectedMessage = GENERATED_COMMIT_MESSAGE,
            )
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
        }
    }

    @Test
    fun pushSectionCommitsAndPushesToTemporaryBareRemote() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createCommitAndPush(tempDirectory.resolve("push-flow-fixture"))
        val initialCommitCount = fixture.primaryRepository.commitCount()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runIdeaWithFixture(
            testName = "release-matrix-ui-push-flow",
            fixture = fixture,
        ) {
            openReleaseMatrixCommitToolWindow()
            ui.x { byJavaClass(AI_COMMIT_ALL_CONTROL_CLASS_NAME) }.clickSection("Push")
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
        }
    }

    @Test
    fun pushSectionPushesOutgoingOnlyLocalCommitToTemporaryBareRemote() {
        assumeTrue(IntegrationGitCli.isAvailable(), "git executable is required for release-matrix UI fixtures")
        val fixture = ReleaseMatrixGitFixtureBuilder.createOutgoingOnly(tempDirectory.resolve("outgoing-only-fixture"))
        val outgoingHead = fixture.primaryRepository.head()
        val initialRemoteHead = fixture.bareRemote.remoteHead()

        runIdeaWithFixture(
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
            ui.x { byJavaClass(AI_COMMIT_ALL_CONTROL_CLASS_NAME) }.clickSection("Push")
            waitFor(
                message = "outgoing-only local commit reaches temporary bare remote",
                timeout = 60.seconds,
                interval = 1.seconds,
                errorMessage = { "Remote HEAD stayed at ${fixture.bareRemote.remoteHead()}" },
            ) {
                fixture.bareRemote.remoteHead() != initialRemoteHead && fixture.bareRemote.remoteHead() == outgoingHead
            }
            assertEquals(emptyList(), fixture.primaryRepository.statusLines())
        }
    }

    private fun Driver.openReleaseMatrixCommitToolWindow(): Project {
        val project = waitForReleaseMatrixProject()
        val probe = utility(RemoteFakeAiAssistantProbe::class)
        openToolWindow(COMMIT_TOOL_WINDOW_ID)
        assertTrue(probe.openCommitWorkflow(project))
        waitFor(
            message = "AI Commit All control is visible in the Commit tool window",
            timeout = 60.seconds,
            interval = 1.seconds,
        ) {
            probe.openCommitWorkflow(project) && probe.isAiCommitAllControlShowing(project)
        }
        return project
    }

    private fun waitForPrimaryRepositoryCommit(
        fixture: ReleaseMatrixGitFixture,
        initialCommitCount: Int,
        expectedMessage: String,
    ) {
        waitFor(
            message = "AI Commit All creates local commit '$expectedMessage'",
            timeout = 60.seconds,
            interval = 1.seconds,
            errorMessage = { "Latest commit: ${fixture.primaryRepository.latestCommitSubject()}" },
        ) {
            fixture.primaryRepository.commitCount() > initialCommitCount &&
                fixture.primaryRepository.latestCommitSubject() == expectedMessage
        }
    }

    private fun runIdeaWithFixture(
        testName: String,
        fixture: ReleaseMatrixGitFixture,
        block: Driver.() -> Unit,
    ) {
        val ideVersion = requiredSystemProperty("aicommitall.ide.version")
        val pluginPath = Path.of(requiredSystemProperty("path.to.build.plugin"))
        val fakeAiPluginPath = Path.of(requiredSystemProperty("aicommitall.fake.ai.plugin.path"))

        Starter.newContext(
            testName = testName,
            testCase = TestCase(
                IdeProductProvider.IU,
                LocalProjectInfo(fixture.projectDirectory),
            ).withVersion(ideVersion),
        ).apply {
            PluginConfigurator(this).installPluginFromPath(fakeAiPluginPath)
            PluginConfigurator(this).installPluginFromPath(pluginPath)
        }.runIdeWithDriver().useDriverAndCloseIde(block = block)
    }

    private fun Driver.waitForReleaseMatrixProject(): Project = waitForOne(
        message = "release matrix project opens",
        timeout = 60.seconds,
        interval = 1.seconds,
        getter = { getOpenProjects() },
        checker = { project -> project.getName() == "release-matrix-project" },
    )

    private fun UiComponent.clickSection(section: String) {
        val bounds = component.getBounds()
        val xRatio = when (section) {
            "AI" -> 0.13
            "Commit" -> 0.45
            "Push" -> 0.82
            else -> error("Unknown AI Commit All control section: $section")
        }
        click(Point((bounds.width * xRatio).toInt(), bounds.height / 2))
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
    fun openCommitWorkflow(project: Project): Boolean
    fun activateCommitToolWindow(project: Project): Boolean
    fun isAiCommitAllControlShowing(project: Project): Boolean
    fun aiCommitAllControlAccessibleName(project: Project): String?
    fun aiCommitAllControlAccessibleDescription(project: Project): String?
    fun isAiCommitAllControlEnabled(project: Project): Boolean
    fun writeAiCommitAllControlScreenshots(
        project: Project,
        outputDirectory: String,
    ): List<String>
    fun generatedCommitMessageThroughDataContext(): String
    fun registeredKeyboardShortcutText(actionId: String): String?
    fun setUseVcsShortcutsForAiCommitAll(enabled: Boolean)
    fun useVcsShortcutsForAiCommitAll(): Boolean
    fun isShortcutActionEnabled(
        project: Project,
        actionId: String,
    ): Boolean

    fun hasOutgoingCommitsToPush(project: Project): Boolean
}
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
