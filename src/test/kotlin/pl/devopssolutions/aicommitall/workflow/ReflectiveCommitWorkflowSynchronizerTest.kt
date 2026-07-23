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
package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.vcs.commit.AmendCommitHandler
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker
import java.io.File
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ReflectiveCommitWorkflowSynchronizerTest {
    @Test
    fun `authoritative tracked paths constrain confirmation when tracker state is stale`() {
        val root = LightVirtualFile("repo")
        val trackerVisible = TestFilePath("/repo/A.txt")
        val trackerMissing = TestFilePath("/repo/B.txt")
        val staleState = GitStageTracker.State(
            mapOf(
                root to GitStageTracker.RootState(
                    root,
                    true,
                    mapOf(trackerVisible to GitFileStatus(' ', 'M', trackerVisible, null)),
                ),
            ),
        )

        val result = ReflectiveCommitWorkflowSynchronizer.gitStageSelectionPaths(
            state = staleState,
            selectedPaths = listOf(trackerVisible, trackerMissing),
            unversionedFiles = emptyList(),
        )

        assertEquals(listOf(trackerVisible, trackerMissing), result.expectedPathsByRoot[root])
        assertEquals(listOf(trackerVisible), result.pathsToStageByRoot[root])
        assertTrue(result.allSelectedPathsMapped)
    }

    @Test
    fun `authoritative paths outside tracker roots cannot be confirmed`() {
        val root = LightVirtualFile("repo")
        val outsideRoot = TestFilePath("/other-repo/B.txt")

        val result = ReflectiveCommitWorkflowSynchronizer.gitStageSelectionPaths(
            state = GitStageTracker.State(
                mapOf(root to GitStageTracker.RootState(root, true, emptyMap())),
            ),
            selectedPaths = listOf(outsideRoot),
            unversionedFiles = emptyList(),
        )

        assertEquals(emptyMap(), result.expectedPathsByRoot)
        assertEquals(false, result.allSelectedPathsMapped)
    }

    @Test
    fun `synchronizes compatible commit workflow handlers`() {
        val handler = CompatibleHandler()
        val changeList = TestChangeList("Default")
        val items = listOf(Any(), Any())

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = items,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Synchronized, result)
        assertEquals(listOf(changeList), handler.synchronizedChangeLists)
        assertEquals(0, handler.synchronizedUnversionedCount)
        assertEquals(changeList, handler.activeChangeList)
        assertEquals(items, handler.inclusionItems)
        assertTrue(handler.replaceInclusion)
    }

    @Test
    fun `synchronizes compatible commit workflow handlers with unversioned files`() {
        val handler = CompatibleHandler()
        val changeList = TestChangeList("Default")
        val unversionedFile = TestFilePath("/repo/new.txt")
        val items = listOf(unversionedFile)

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            selectedPaths = listOf(unversionedFile),
            unversionedFiles = listOf(unversionedFile),
            activeChangeList = changeList,
            inclusionItems = items,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Synchronized, result)
        assertEquals(listOf(changeList), handler.synchronizedChangeLists)
        assertEquals(1, handler.synchronizedUnversionedCount)
        assertEquals(items, handler.inclusionItems)
    }

    @Test
    fun `fails closed when workflow handler has no inclusion methods`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = IncompatibleHandler(),
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Incompatible, result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = IncompatibleHandler::class.java.name,
                    methodName = "commitWorkflowMethods",
                    reason = "required methods missing",
                    missingMethodNames = listOf("synchronizeInclusion", "setCommitState"),
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `fails closed before reflection when inclusion items are empty`() {
        val handler = CompatibleHandler()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = emptyList(),
        )

        assertEquals(CommitWorkflowSynchronizationResult.Incompatible, result)
        assertEquals(null, handler.synchronizedChangeLists)
        assertEquals(null, handler.activeChangeList)
    }

    @Test
    fun `fails closed when workflow handler has only synchronize inclusion method`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = MissingSetCommitStateHandler(),
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Incompatible, result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = MissingSetCommitStateHandler::class.java.name,
                    methodName = "commitWorkflowMethods",
                    reason = "required methods missing",
                    missingMethodNames = listOf("setCommitState"),
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `fails closed when workflow handler has only set commit state method`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = MissingSynchronizeInclusionHandler(),
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Incompatible, result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = MissingSynchronizeInclusionHandler::class.java.name,
                    methodName = "commitWorkflowMethods",
                    reason = "required methods missing",
                    missingMethodNames = listOf("synchronizeInclusion"),
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `fails closed when workflow synchronization throws`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = ThrowingHandler(),
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Incompatible, result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = ThrowingHandler::class.java.name,
                    methodName = "synchronize",
                    reason = "method invocation failed",
                    exceptionClassName = java.lang.reflect.InvocationTargetException::class.java.name,
                    causeClassName = IllegalStateException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `fails closed when set commit state invocation throws`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = SetCommitStateThrowingHandler(),
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
            synchronizationRetry = singleAttemptRetry(),
        )

        assertEquals(CommitWorkflowSynchronizationResult.Incompatible, result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = SetCommitStateThrowingHandler::class.java.name,
                    methodName = "synchronize",
                    reason = "method invocation failed",
                    exceptionClassName = java.lang.reflect.InvocationTargetException::class.java.name,
                    causeClassName = IllegalStateException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `succeeds when compatible workflow synchronization fails once then settles`() {
        val handler = TransientThrowingHandler()
        val changeList = TestChangeList("Default")
        val items = listOf(Any())

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            selectedPaths = emptyList(),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = items,
        )

        assertEquals(CommitWorkflowSynchronizationResult.Synchronized, result)
        assertEquals(2, handler.synchronizeCallCount)
        assertEquals(1, handler.setCommitStateCallCount)
        assertEquals(changeList, handler.activeChangeList)
        assertEquals(items, handler.inclusionItems)
    }

    @Test
    fun `git stage reflection access invokes the 262 handler boundary`() {
        val project = testProject()
        val state = GitStageTracker.State(emptyMap())
        val root = LightVirtualFile("repo")
        val uiCalls = mutableListOf<String>()
        val ui = testGitStageWorkflowUi { methodName -> uiCalls += methodName }
        val handler = ReflectiveGitStageHandler(ReflectiveGitStageWorkflow(project), ui)
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()

        val access = createGitStageCommitWorkflowAccess(handler, diagnostics)
            ?: error("Expected compatible git-stage reflection access.")
        access.assignState(state)
        access.setTrackerState(state)
        access.setIncludedRoots(listOf(root))

        assertSame(project, access.project)
        assertSame(ui, access.workflowUi)
        assertSame(state, handler.assignedState)
        assertEquals(listOf("setTrackerState", "setIncludedRoots"), uiCalls)
        assertEquals(emptyList(), diagnostics.events)
    }

    @Test
    fun `git stage reflection access fails closed when 262 members are missing`() {
        val handler = IncompatibleHandler()
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()

        val access = createGitStageCommitWorkflowAccess(handler, diagnostics)

        assertEquals(null, access)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = IncompatibleHandler::class.java.name,
                    methodName = "gitStageCommitWorkflowAccess",
                    reason = "required methods missing",
                    missingMethodNames = listOf("getWorkflow", "getUi", "setState"),
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `assigns git stage workflow state before scheduling visual UI refresh`() {
        val scheduler = CapturingUiRefreshScheduler()
        val diagnostics = CapturingGitStageDiagnostics()
        val synchronization = GitStageWorkflowStateSynchronization(
            uiScheduler = scheduler,
            diagnostics = diagnostics,
        )
        val events = mutableListOf<String>()

        synchronization.synchronize(
            assignState = { events += "assign-state" },
            refreshUi = {
                setTrackerState { events += "set-tracker-state" }
                setIncludedRoots { events += "set-included-roots" }
            },
        )

        assertEquals(listOf("assign-state"), events)
        assertEquals(1, scheduler.scheduledActionCount)
        assertEquals(
            listOf(
                "started:state assignment",
                "finished:state assignment",
                "started:ui refresh scheduling",
                "finished:ui refresh scheduling",
            ),
            diagnostics.stepEvents,
        )

        scheduler.runScheduledActions()

        assertEquals(
            listOf("assign-state", "set-tracker-state", "set-included-roots"),
            events,
        )
        assertEquals(
            listOf(
                "started:state assignment",
                "finished:state assignment",
                "started:ui refresh scheduling",
                "finished:ui refresh scheduling",
                "started:ui refresh completion",
                "started:setTrackerState",
                "finished:setTrackerState",
                "started:setIncludedRoots",
                "finished:setIncludedRoots",
                "finished:ui refresh completion",
            ),
            diagnostics.stepEvents,
        )
        assertEquals(1, diagnostics.queueDelays.size)
        assertTrue(diagnostics.queueDelays.single() >= 0L)
    }

    @Test
    fun `required git stage UI handoff applies and verifies inclusion synchronously`() {
        val diagnostics = CapturingGitStageDiagnostics()
        val synchronization = GitStageWorkflowStateSynchronization(
            uiScheduler = CapturingUiRefreshScheduler(),
            diagnostics = diagnostics,
        )
        val events = mutableListOf<String>()

        val result = synchronization.applyRequiredUiHandoff(
            assignState = { events += "assign-state" },
            setTrackerState = { events += "set-tracker-state" },
            setIncludedRoots = { events += "set-included-roots" },
            verifyIncludedPaths = {
                events += "verify-included-paths"
                true
            },
        )

        assertTrue(result)
        assertEquals(
            listOf(
                "assign-state",
                "set-tracker-state",
                "set-included-roots",
                "verify-included-paths",
            ),
            events,
        )
        assertTrue("finished:EDT model handoff" in diagnostics.stepEvents)
        assertTrue("finished:included-path verification" in diagnostics.stepEvents)
    }

    @Test
    fun `git stage selection includes unversioned paths missing from tracker roots`() {
        val firstRoot = LightVirtualFile("repo-a")
        val secondRoot = LightVirtualFile("repo-b")
        val firstUnversioned = TestFilePath("${firstRoot.path}/new-a.txt")
        val secondUnversioned = TestFilePath("${secondRoot.path}/new-b.txt")

        val result = includeAdditionalSelectionPathsByRoot(
            pathsByRoot = emptyMap(),
            roots = listOf(firstRoot, secondRoot),
            additionalPaths = listOf(firstUnversioned, secondUnversioned),
        )

        assertEquals(
            mapOf<VirtualFile, List<FilePath>>(
                firstRoot to listOf(firstUnversioned),
                secondRoot to listOf(secondUnversioned),
            ),
            result,
        )
    }

    @Test
    fun `required git stage UI handoff fails closed when expected paths remain hidden`() {
        val diagnostics = CapturingGitStageDiagnostics()
        val synchronization = GitStageWorkflowStateSynchronization(
            uiScheduler = CapturingUiRefreshScheduler(),
            diagnostics = diagnostics,
        )
        val events = mutableListOf<String>()

        val result = synchronization.applyRequiredUiHandoff(
            assignState = { events += "assign-state" },
            setTrackerState = { events += "set-tracker-state" },
            setIncludedRoots = { events += "set-included-roots" },
            verifyIncludedPaths = {
                events += "verify-included-paths"
                false
            },
        )

        assertEquals(false, result)
        assertEquals(
            listOf(
                "assign-state",
                "set-tracker-state",
                "set-included-roots",
                "verify-included-paths",
            ),
            events,
        )
        assertEquals(
            listOf(
                "failed:included-path verification:IllegalStateException",
                "failed:EDT model handoff:IllegalStateException",
            ),
            diagnostics.failures,
        )
    }

    @Test
    fun `keeps git stage UI refresh best effort when visual update throws`() {
        val scheduler = CapturingUiRefreshScheduler()
        val diagnostics = CapturingGitStageDiagnostics()
        val synchronization = GitStageWorkflowStateSynchronization(
            uiScheduler = scheduler,
            diagnostics = diagnostics,
        )
        val events = mutableListOf<String>()

        synchronization.synchronize(
            assignState = { events += "assign-state" },
            refreshUi = {
                setTrackerState {
                    events += "set-tracker-state"
                    error("tracker UI blocked")
                }
                setIncludedRoots { events += "set-included-roots" }
            },
        )
        scheduler.runScheduledActions()

        assertEquals(
            listOf("assign-state", "set-tracker-state", "set-included-roots"),
            events,
        )
        assertEquals(
            listOf(
                "failed:setTrackerState:IllegalStateException",
            ),
            diagnostics.failures,
        )
        assertTrue("finished:setIncludedRoots" in diagnostics.stepEvents)
        assertTrue("finished:ui refresh completion" in diagnostics.stepEvents)
    }

    @Test
    fun `reports git stage visual UI scheduling failure without failing state synchronization`() {
        val diagnostics = CapturingGitStageDiagnostics()
        val synchronization = GitStageWorkflowStateSynchronization(
            uiScheduler = ThrowingUiRefreshScheduler(IllegalStateException("EDT unavailable")),
            diagnostics = diagnostics,
        )
        var assigned = false

        synchronization.synchronize(
            assignState = { assigned = true },
            refreshUi = { setTrackerState { error("should not run") } },
        )

        assertTrue(assigned)
        assertEquals(
            listOf("failed:ui refresh scheduling:IllegalStateException"),
            diagnostics.failures,
        )
    }

    @Test
    fun `reports and rethrows git stage state assignment failures`() {
        val diagnostics = CapturingGitStageDiagnostics()
        val failure = IllegalStateException("state write failed")
        val synchronization = GitStageWorkflowStateSynchronization(
            uiScheduler = CapturingUiRefreshScheduler(),
            diagnostics = diagnostics,
        )

        val thrown = assertFailsWith<IllegalStateException> {
            synchronization.synchronize(
                assignState = { throw failure },
                refreshUi = { setTrackerState { error("should not run") } },
            )
        }

        assertSame(failure, thrown)
        assertEquals(
            listOf("failed:state assignment:IllegalStateException"),
            diagnostics.failures,
        )
    }

    @Test
    fun `default diagnostics and scheduler synchronize git stage state without an application`() {
        val synchronization = GitStageWorkflowStateSynchronization()
        val events = mutableListOf<String>()

        synchronization.synchronize(
            assignState = { events += "assign-state" },
            refreshUi = {
                setTrackerState { events += "set-tracker-state" }
                setIncludedRoots { events += "set-included-roots" }
            },
        )

        assertEquals(
            listOf("assign-state", "set-tracker-state", "set-included-roots"),
            events,
        )
    }

    @Test
    fun `default diagnostics record a best effort git stage UI failure without throwing`() {
        val synchronization = GitStageWorkflowStateSynchronization()
        var assigned = false

        synchronization.synchronize(
            assignState = { assigned = true },
            refreshUi = { setTrackerState { error("tracker UI blocked") } },
        )

        assertTrue(assigned)
    }

    @Test
    fun `synchronization retry sleeps between failures and returns the final failure`() {
        val sleeper = CapturingSynchronizationSleeper()
        var attempts = 0
        val firstFailure = IllegalStateException("first")
        val finalFailure = IllegalArgumentException("final")

        val result = CommitWorkflowSynchronizationRetry(
            maxAttempts = 2,
            retryInterval = Duration.ofMillis(25),
            sleeper = sleeper,
        ).run {
            attempts++
            if (attempts == 1) {
                throw firstFailure
            }
            throw finalFailure
        }

        assertSame(finalFailure, result)
        assertEquals(listOf(Duration.ofMillis(25)), sleeper.delays)
    }

    @Test
    fun `synchronization retry stops after transient failure settles`() {
        val sleeper = CapturingSynchronizationSleeper()
        var attempts = 0

        val result = CommitWorkflowSynchronizationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(25),
            sleeper = sleeper,
        ).run {
            attempts++
            if (attempts == 1) {
                error("transient failure")
            }
        }

        assertEquals(null, result)
        assertEquals(2, attempts)
        assertEquals(listOf(Duration.ofMillis(25)), sleeper.delays)
    }

    @Test
    fun `synchronization retry rejects invalid settings`() {
        assertFailsWith<IllegalArgumentException> {
            CommitWorkflowSynchronizationRetry(maxAttempts = 0, retryInterval = Duration.ZERO)
        }
        assertFailsWith<IllegalArgumentException> {
            CommitWorkflowSynchronizationRetry(maxAttempts = 1, retryInterval = Duration.ofMillis(-1))
        }
    }

    private open class TestCommitWorkflowHandler : CommitWorkflowHandler {
        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for reflection tests.")

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = Unit
    }

    private class CompatibleHandler : TestCommitWorkflowHandler() {
        var synchronizedChangeLists: List<LocalChangeList>? = null
        var synchronizedUnversionedCount: Int? = null
        var activeChangeList: LocalChangeList? = null
        var inclusionItems: Collection<Any>? = null
        var replaceInclusion: Boolean = false

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizedChangeLists = changeLists
            synchronizedUnversionedCount = unversionedFiles.size
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            activeChangeList = changeList
            inclusionItems = items
            this.replaceInclusion = replaceInclusion
        }
    }

    private class IncompatibleHandler : TestCommitWorkflowHandler()

    private class MissingSetCommitStateHandler : TestCommitWorkflowHandler() {
        var synchronizedInputCount = 0

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizedInputCount = changeLists.size + unversionedFiles.size
        }
    }

    private class MissingSynchronizeInclusionHandler : TestCommitWorkflowHandler() {
        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            error("setCommitState should not run for ${changeList.name}, ${items.size}, $replaceInclusion")
        }
    }

    private class ThrowingHandler : TestCommitWorkflowHandler() {
        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            error("synchronization failed for ${changeLists.size} lists and ${unversionedFiles.size} files")
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            error("setCommitState should not run for ${changeList.name}, ${items.size}, $replaceInclusion")
        }
    }

    private class SetCommitStateThrowingHandler : TestCommitWorkflowHandler() {
        var synchronizedInputCount = 0

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizedInputCount = changeLists.size + unversionedFiles.size
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            error("set commit state failed for ${changeList.name}, ${items.size}, $replaceInclusion")
        }
    }

    private class TransientThrowingHandler : TestCommitWorkflowHandler() {
        var synchronizeCallCount = 0
        var setCommitStateCallCount = 0
        var activeChangeList: LocalChangeList? = null
        var inclusionItems: Collection<Any>? = null
        var synchronizedInputCount = 0
        var replaceInclusion = false

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizeCallCount++
            synchronizedInputCount = changeLists.size + unversionedFiles.size
            if (synchronizeCallCount == 1) {
                error("transient synchronization failure")
            }
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            setCommitStateCallCount++
            activeChangeList = changeList
            inclusionItems = items
            this.replaceInclusion = replaceInclusion
        }
    }

    private class ReflectiveGitStageHandler(
        private val workflowValue: ReflectiveGitStageWorkflow,
        private val uiValue: TestGitStageWorkflowUi,
    ) : TestCommitWorkflowHandler() {
        var assignedState: GitStageTracker.State? = null

        fun getWorkflow(): ReflectiveGitStageWorkflow = workflowValue

        fun getUi(): TestGitStageWorkflowUi = uiValue

        fun setState(state: GitStageTracker.State) {
            assignedState = state
        }
    }

    private class ReflectiveGitStageWorkflow(val project: Project)

    private interface TestGitStageWorkflowUi : CommitWorkflowUi {
        fun setTrackerState(state: GitStageTracker.State)

        fun setIncludedRoots(roots: Collection<VirtualFile>)
    }

    private class CapturingCommitWorkflowCompatibilityDiagnostics : CommitWorkflowCompatibilityDiagnostics {
        val events = mutableListOf<CommitWorkflowCompatibilityDiagnostic>()

        override fun report(diagnostic: CommitWorkflowCompatibilityDiagnostic) {
            events += diagnostic
        }
    }

    private class CapturingUiRefreshScheduler : CommitWorkflowUiRefreshScheduler {
        private val actions = mutableListOf<() -> Unit>()

        val scheduledActionCount: Int
            get() = actions.size

        override fun schedule(action: () -> Unit) {
            actions += action
        }

        fun runScheduledActions() {
            val scheduledActions = actions.toList()
            actions.clear()
            scheduledActions.forEach { action -> action() }
        }
    }

    private class ThrowingUiRefreshScheduler(
        private val failure: RuntimeException,
    ) : CommitWorkflowUiRefreshScheduler {
        override fun schedule(action: () -> Unit): Unit = throw failure
    }

    private class CapturingGitStageDiagnostics : GitStageWorkflowStateSynchronizationDiagnostics {
        val stepEvents = mutableListOf<String>()
        val failures = mutableListOf<String>()
        val queueDelays = mutableListOf<Long>()

        override fun started(step: String) {
            stepEvents += "started:$step"
        }

        override fun startedAfterQueue(step: String, queueDelayMillis: Long) {
            queueDelays += queueDelayMillis
        }

        override fun finished(step: String, elapsedMillis: Long) {
            stepEvents += "finished:$step"
        }

        override fun failed(step: String, elapsedMillis: Long, exception: Throwable) {
            stepEvents += "failed:$step"
            failures += "failed:$step:${exception.javaClass.simpleName}"
        }
    }

    private class CapturingSynchronizationSleeper : CommitWorkflowSynchronizationSleeper {
        val delays = mutableListOf<Duration>()

        override fun sleep(duration: Duration) {
            delays += duration
        }
    }

    private class TestChangeList(private val listName: String) : LocalChangeList() {
        override fun getChanges(): Collection<Change> = emptyList()

        override fun getName(): String = listName

        override fun getComment(): String? = null

        override fun isDefault(): Boolean = false

        override fun isReadOnly(): Boolean = false

        override fun getData(): Any? = null

        override fun copy(): LocalChangeList = TestChangeList(listName)
    }

    private class TestFilePath(private val rawPath: String) : FilePath {
        override fun getVirtualFile(): VirtualFile? = null

        override fun getVirtualFileParent(): VirtualFile? = null

        override fun getIOFile(): File = File(rawPath)

        override fun getName(): String = ioFile.name

        override fun getPresentableUrl(): String = rawPath

        override fun getCharset(): Charset = Charsets.UTF_8

        override fun getCharset(project: Project?): Charset = Charsets.UTF_8

        override fun getFileType(): FileType = PlainTextFileType.INSTANCE

        override fun getPath(): String = rawPath

        override fun isDirectory(): Boolean = false

        override fun isUnder(parent: FilePath, strict: Boolean): Boolean = false

        override fun getParentPath(): FilePath? = null

        override fun isNonLocal(): Boolean = false
    }

    private fun testProject(): Project = Proxy.newProxyInstance(
        Project::class.java.classLoader,
        arrayOf(Project::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "equals" -> proxy === args?.singleOrNull()
            "hashCode" -> System.identityHashCode(proxy)
            "toString" -> "TestProject"
            else -> error("Unexpected Project method: ${method.name}")
        }
    } as Project

    private fun testGitStageWorkflowUi(onCall: (String) -> Unit): TestGitStageWorkflowUi = Proxy.newProxyInstance(
        TestGitStageWorkflowUi::class.java.classLoader,
        arrayOf(TestGitStageWorkflowUi::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "setTrackerState", "setIncludedRoots" -> onCall(method.name)
            else -> error("Unexpected git-stage UI method: ${method.name}")
        }
    } as TestGitStageWorkflowUi

    private companion object {
        private fun singleAttemptRetry(): CommitWorkflowSynchronizationRetry = CommitWorkflowSynchronizationRetry(
            maxAttempts = 1,
            retryInterval = Duration.ZERO,
        )
    }
}

internal class GitStageReflectionAccessFailureTest {
    @Test
    fun `git stage reflection access reports handler invocation failure and cause`() {
        val handlerFailure = IllegalStateException("workflow unavailable")
        val handler = BoundaryThrowingGitStageHandler(
            handlerFailure,
            testGitStageWorkflowUi { error("UI should not be used after the workflow lookup fails.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "method invocation failed",
            exceptionClass = java.lang.reflect.InvocationTargetException::class.java,
            causeClass = handlerFailure.javaClass,
        )
    }

    @Test
    fun `git stage reflection access reports null workflow result`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = null,
            uiValue = testGitStageWorkflowUi { error("UI should not be used without a workflow.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "method invocation failed",
            exceptionClass = IllegalStateException::class.java,
        )
    }

    @Test
    fun `git stage reflection access reports null UI result`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryReflectiveGitStageWorkflow(testProject()),
            uiValue = null,
        )

        assertAccessFailure(
            handler = handler,
            reason = "method invocation failed",
            exceptionClass = IllegalStateException::class.java,
        )
    }

    @Test
    fun `git stage reflection access reports missing nested boundary methods`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = Any(),
            uiValue = Any(),
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("getProject", "setTrackerState", "setIncludedRoots"),
        )
    }

    @Test
    fun `git stage reflection access rejects wrong workflow method signature`() {
        val handler = BoundaryGitStageHandlerWithWrongWorkflowSignature(
            testGitStageWorkflowUi { error("UI should not be used without a compatible workflow method.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("getWorkflow"),
        )
    }

    @Test
    fun `git stage reflection access reports missing handler UI method`() {
        val handler = BoundaryGitStageHandlerWithoutUi(
            BoundaryReflectiveGitStageWorkflow(testProject()),
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("getUi"),
        )
    }

    @Test
    fun `git stage reflection access reports missing handler state method`() {
        val handler = BoundaryGitStageHandlerWithoutState(
            BoundaryReflectiveGitStageWorkflow(testProject()),
            testGitStageWorkflowUi { error("UI should not be used without a compatible state method.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("setState"),
        )
    }

    @Test
    fun `git stage reflection access reports missing nested project method`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryGitStageWorkflowWithoutProject(),
            uiValue = testGitStageWorkflowUi { error("UI should not be used without a project method.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("getProject"),
        )
    }

    @Test
    fun `git stage reflection access reports missing nested tracker state method`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryReflectiveGitStageWorkflow(testProject()),
            uiValue = BoundaryGitStageUiWithoutTrackerState(),
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("setTrackerState"),
        )
    }

    @Test
    fun `git stage reflection access reports missing nested included roots method`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryReflectiveGitStageWorkflow(testProject()),
            uiValue = BoundaryGitStageUiWithoutIncludedRoots(),
        )

        assertAccessFailure(
            handler = handler,
            reason = "required methods missing",
            missingMethodNames = listOf("setIncludedRoots"),
        )
    }

    @Test
    fun `git stage reflection access reports incompatible project result`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryIncompatibleGitStageWorkflow(Any()),
            uiValue = testGitStageWorkflowUi { error("UI should not be used with an incompatible project.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "incompatible method result",
            exceptionClass = IllegalStateException::class.java,
        )
    }

    @Test
    fun `git stage reflection access reports incompatible UI result`() {
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryReflectiveGitStageWorkflow(testProject()),
            uiValue = BoundaryGitStageUiWithoutCommitWorkflowUi(),
        )

        assertAccessFailure(
            handler = handler,
            reason = "incompatible method result",
            exceptionClass = IllegalStateException::class.java,
        )
    }

    @Test
    fun `git stage reflection access reports nested invocation failure and cause`() {
        val projectFailure = IllegalArgumentException("project unavailable")
        val handler = BoundaryReflectiveGitStageHandler(
            workflowValue = BoundaryThrowingGitStageWorkflow(projectFailure),
            uiValue = testGitStageWorkflowUi { error("UI should not be used after the project lookup fails.") },
        )

        assertAccessFailure(
            handler = handler,
            reason = "incompatible method result",
            exceptionClass = java.lang.reflect.InvocationTargetException::class.java,
            causeClass = projectFailure.javaClass,
        )
    }

    private fun assertAccessFailure(
        handler: CommitWorkflowHandler,
        reason: String,
        missingMethodNames: List<String> = emptyList(),
        exceptionClass: Class<out Throwable>? = null,
        causeClass: Class<out Throwable>? = null,
    ) {
        val diagnostics = BoundaryCapturingCommitWorkflowCompatibilityDiagnostics()

        val access = createGitStageCommitWorkflowAccess(handler, diagnostics)

        assertEquals(null, access)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = handler.javaClass.name,
                    methodName = "gitStageCommitWorkflowAccess",
                    reason = reason,
                    missingMethodNames = missingMethodNames,
                    exceptionClassName = exceptionClass?.name,
                    causeClassName = causeClass?.name,
                ),
            ),
            diagnostics.events,
        )
    }

    private open class BoundaryCommitWorkflowHandler : CommitWorkflowHandler {
        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for reflection tests.")

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = Unit
    }

    private class BoundaryThrowingGitStageHandler(
        private val failure: RuntimeException,
        private val uiValue: BoundaryTestGitStageWorkflowUi,
    ) : BoundaryCommitWorkflowHandler() {
        var assignedState: GitStageTracker.State? = null

        fun getWorkflow(): Any = throw failure

        fun getUi(): BoundaryTestGitStageWorkflowUi = uiValue

        fun setState(state: GitStageTracker.State) {
            assignedState = state
        }
    }

    private class BoundaryGitStageHandlerWithWrongWorkflowSignature(
        private val uiValue: BoundaryTestGitStageWorkflowUi,
    ) : BoundaryCommitWorkflowHandler() {
        var assignedState: GitStageTracker.State? = null

        fun getWorkflow(parameter: String): Any = parameter

        fun getUi(): BoundaryTestGitStageWorkflowUi = uiValue

        fun setState(state: GitStageTracker.State) {
            assignedState = state
        }
    }

    private class BoundaryGitStageHandlerWithoutUi(
        private val workflowValue: Any,
    ) : BoundaryCommitWorkflowHandler() {
        var assignedState: GitStageTracker.State? = null

        fun getWorkflow(): Any = workflowValue

        fun setState(state: GitStageTracker.State) {
            assignedState = state
        }
    }

    private class BoundaryGitStageHandlerWithoutState(
        private val workflowValue: Any,
        private val uiValue: BoundaryTestGitStageWorkflowUi,
    ) : BoundaryCommitWorkflowHandler() {
        fun getWorkflow(): Any = workflowValue

        fun getUi(): BoundaryTestGitStageWorkflowUi = uiValue
    }

    private class BoundaryReflectiveGitStageHandler(
        private val workflowValue: Any?,
        private val uiValue: Any?,
    ) : BoundaryCommitWorkflowHandler() {
        var assignedState: GitStageTracker.State? = null

        fun getWorkflow(): Any? = workflowValue

        fun getUi(): Any? = uiValue

        fun setState(state: GitStageTracker.State) {
            assignedState = state
        }
    }

    private class BoundaryReflectiveGitStageWorkflow(val project: Project)

    private class BoundaryGitStageWorkflowWithoutProject

    private class BoundaryIncompatibleGitStageWorkflow(val project: Any)

    private class BoundaryThrowingGitStageWorkflow(
        private val failure: RuntimeException,
    ) {
        fun getProject(): Project = throw failure
    }

    private interface BoundaryTestGitStageWorkflowUi : CommitWorkflowUi {
        fun setTrackerState(state: GitStageTracker.State)

        fun setIncludedRoots(roots: Collection<VirtualFile>)
    }

    private class BoundaryGitStageUiWithoutCommitWorkflowUi {
        var capturedTrackerState: GitStageTracker.State? = null
        var capturedIncludedRoots: Collection<VirtualFile> = emptyList()

        fun setTrackerState(state: GitStageTracker.State) {
            capturedTrackerState = state
        }

        fun setIncludedRoots(roots: Collection<VirtualFile>) {
            capturedIncludedRoots = roots
        }
    }

    private class BoundaryGitStageUiWithoutTrackerState {
        var capturedIncludedRoots: Collection<VirtualFile> = emptyList()

        fun setIncludedRoots(roots: Collection<VirtualFile>) {
            capturedIncludedRoots = roots
        }
    }

    private class BoundaryGitStageUiWithoutIncludedRoots {
        var capturedTrackerState: GitStageTracker.State? = null

        fun setTrackerState(state: GitStageTracker.State) {
            capturedTrackerState = state
        }
    }

    private class BoundaryCapturingCommitWorkflowCompatibilityDiagnostics : CommitWorkflowCompatibilityDiagnostics {
        val events = mutableListOf<CommitWorkflowCompatibilityDiagnostic>()

        override fun report(diagnostic: CommitWorkflowCompatibilityDiagnostic) {
            events += diagnostic
        }
    }

    private fun testProject(): Project = Proxy.newProxyInstance(
        Project::class.java.classLoader,
        arrayOf(Project::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "equals" -> proxy === args?.singleOrNull()
            "hashCode" -> System.identityHashCode(proxy)
            "toString" -> "TestProject"
            else -> error("Unexpected Project method: ${method.name}")
        }
    } as Project

    private fun testGitStageWorkflowUi(
        onCall: (String) -> Unit,
    ): BoundaryTestGitStageWorkflowUi = Proxy.newProxyInstance(
        BoundaryTestGitStageWorkflowUi::class.java.classLoader,
        arrayOf(BoundaryTestGitStageWorkflowUi::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "setTrackerState", "setIncludedRoots" -> onCall(method.name)
            else -> error("Unexpected git-stage UI method: ${method.name}")
        }
    } as BoundaryTestGitStageWorkflowUi
}

internal class GitStageSelectionBoundaryTest {
    @Test
    fun `mapped selection paths pass through without diagnostics`() {
        val root = LightVirtualFile("repo")
        val selected = BoundaryTestFilePath("/repo/selected.txt")
        val diagnostics = BoundaryCapturingCompatibilityDiagnostics()

        val result = ReflectiveCommitWorkflowSynchronizer.mappedGitStageSelectionPaths(
            state = GitStageTracker.State(
                mapOf(root to GitStageTracker.RootState(root, true, emptyMap())),
            ),
            selectedPaths = listOf(selected),
            unversionedFiles = emptyList(),
            diagnostics = diagnostics,
            sourceClassName = "TestGitStageHandler",
        ) ?: error("Expected mapped selection paths.")

        assertEquals(listOf(selected), result.expectedPathsByRoot[root])
        assertTrue(result.allSelectedPathsMapped)
        assertEquals(emptyList(), diagnostics.events)
    }

    @Test
    fun `unmapped selection paths fail closed with compatibility diagnostic`() {
        val root = LightVirtualFile("repo")
        val outsideRoot = BoundaryTestFilePath("/other-repo/selected.txt")
        val diagnostics = BoundaryCapturingCompatibilityDiagnostics()

        val result = ReflectiveCommitWorkflowSynchronizer.mappedGitStageSelectionPaths(
            state = GitStageTracker.State(
                mapOf(root to GitStageTracker.RootState(root, true, emptyMap())),
            ),
            selectedPaths = listOf(outsideRoot),
            unversionedFiles = emptyList(),
            diagnostics = diagnostics,
            sourceClassName = "TestGitStageHandler",
        )

        assertEquals(null, result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = "TestGitStageHandler",
                    methodName = "synchronizeGitStageWorkflow",
                    reason = "selected paths could not be mapped to Git roots",
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `expected AI paths include staged rename sides and exclude unstaged and HEAD identical paths`() {
        val root = LightVirtualFile("repo")
        val renameSource = BoundaryTestFilePath("/repo/before.txt")
        val renameTarget = BoundaryTestFilePath("/repo/after.txt")
        val staged = BoundaryTestFilePath("/repo/staged.txt")
        val unstaged = BoundaryTestFilePath("/repo/unstaged.txt")
        val headIdentical = BoundaryTestFilePath("/repo/head-identical.txt")
        val state = GitStageTracker.State(
            mapOf(
                root to GitStageTracker.RootState(
                    root,
                    true,
                    mapOf(
                        renameTarget to GitFileStatus('R', ' ', renameTarget, renameSource),
                        staged to GitFileStatus('M', ' ', staged, null),
                        unstaged to GitFileStatus(' ', 'M', unstaged, null),
                    ),
                ),
            ),
        )

        val result = state.expectedStagedPathTexts(
            listOf(renameSource, renameTarget, staged, unstaged, headIdentical),
        )

        assertEquals(
            setOf(renameSource.path, renameTarget.path, staged.path),
            result,
        )
    }

    @Test
    fun `additional selection paths use the deepest root and deduplicate normalized paths`() {
        val root = LightVirtualFile("repo")
        val nestedRoot = LightVirtualFile("repo/nested")
        val nestedPath = BoundaryTestFilePath("${nestedRoot.path}/selected.txt")

        val result = includeAdditionalSelectionPathsByRoot(
            pathsByRoot = emptyMap(),
            roots = listOf(root, nestedRoot),
            additionalPaths = listOf(nestedPath, nestedPath),
        )

        assertEquals(mapOf<VirtualFile, List<FilePath>>(nestedRoot to listOf(nestedPath)), result)
    }

    @Test
    fun `commit UI inclusion verification rejects unexpected visible paths`() {
        val expected = BoundaryTestFilePath("/repo/expected.txt")
        val unexpected = BoundaryTestFilePath("/repo/unexpected.txt")
        val workflowUi = testWorkflowUi(
            changes = listOf(testChange(expected), testChange(unexpected)),
        )

        val verification = workflowUi.verifyIncludedPathTexts(setOf(expected.path.replace('\\', '/')))

        assertFalse(verification.matchesExactly)
        assertEquals(setOf(unexpected.path.replace('\\', '/')), verification.unexpectedPathTexts)
    }

    @Test
    fun `commit UI inclusion verification preserves rename sides and unversioned paths`() {
        val renameSource = BoundaryTestFilePath("/repo/before.txt")
        val renameTarget = BoundaryTestFilePath("/repo/after.txt")
        val unversioned = BoundaryTestFilePath("/repo/new.txt")
        val workflowUi = testWorkflowUi(
            changes = listOf(
                Change(
                    BoundaryTestContentRevision(renameSource),
                    BoundaryTestContentRevision(renameTarget),
                    FileStatus.MODIFIED,
                ),
            ),
            unversionedFiles = listOf(unversioned),
        )

        assertTrue(
            workflowUi.verifyIncludedPathTexts(
                expectedPathTexts = setOf(
                    renameSource.path.replace('\\', '/'),
                    renameTarget.path.replace('\\', '/'),
                    unversioned.path.replace('\\', '/'),
                ),
            ).matchesExactly,
        )
    }

    @Test
    fun `tracker paths added after selection are excluded from confirmation`() {
        val root = LightVirtualFile("repo")
        val selected = BoundaryTestFilePath("/repo/selected.txt")
        val addedAfterSelection = BoundaryTestFilePath("/repo/added-after-selection.txt")
        val currentState = GitStageTracker.State(
            mapOf(
                root to GitStageTracker.RootState(
                    root,
                    true,
                    mapOf(
                        selected to GitFileStatus(' ', 'M', selected, null),
                        addedAfterSelection to GitFileStatus(' ', 'M', addedAfterSelection, null),
                    ),
                ),
            ),
        )

        val result = ReflectiveCommitWorkflowSynchronizer.gitStageSelectionPaths(
            state = currentState,
            selectedPaths = listOf(selected),
            unversionedFiles = emptyList(),
        )

        assertEquals(listOf(selected), result.expectedPathsByRoot[root])
        assertEquals(listOf(selected), result.pathsToStageByRoot[root])
        assertTrue(result.allSelectedPathsMapped)
    }

    private class BoundaryTestContentRevision(private val filePath: FilePath) : ContentRevision {
        override fun getFile(): FilePath = filePath

        override fun getContent(): String? = null

        override fun getRevisionNumber() = error("Not needed for inclusion verification tests.")
    }

    private class BoundaryCapturingCompatibilityDiagnostics : CommitWorkflowCompatibilityDiagnostics {
        val events = mutableListOf<CommitWorkflowCompatibilityDiagnostic>()

        override fun report(diagnostic: CommitWorkflowCompatibilityDiagnostic) {
            events += diagnostic
        }
    }

    private fun testChange(filePath: FilePath): Change = Change(
        BoundaryTestContentRevision(filePath),
        BoundaryTestContentRevision(filePath),
        FileStatus.MODIFIED,
    )

    private fun testWorkflowUi(
        changes: List<Change>,
        unversionedFiles: List<FilePath> = emptyList(),
    ): CommitWorkflowUi = Proxy.newProxyInstance(
        CommitWorkflowUi::class.java.classLoader,
        arrayOf(CommitWorkflowUi::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "getIncludedChanges" -> changes
            "getIncludedUnversionedFiles" -> unversionedFiles
            else -> error("Unexpected CommitWorkflowUi method: ${method.name}")
        }
    } as CommitWorkflowUi

    private class BoundaryTestFilePath(private val rawPath: String) : FilePath {
        override fun getVirtualFile(): VirtualFile? = null

        override fun getVirtualFileParent(): VirtualFile? = null

        override fun getIOFile(): File = File(rawPath)

        override fun getName(): String = ioFile.name

        override fun getPresentableUrl(): String = rawPath

        override fun getCharset(): Charset = Charsets.UTF_8

        override fun getCharset(project: Project?): Charset = Charsets.UTF_8

        override fun getFileType(): FileType = PlainTextFileType.INSTANCE

        override fun getPath(): String = rawPath

        override fun isDirectory(): Boolean = false

        override fun isUnder(parent: FilePath, strict: Boolean): Boolean = false

        override fun getParentPath(): FilePath? = null

        override fun isNonLocal(): Boolean = false
    }
}
