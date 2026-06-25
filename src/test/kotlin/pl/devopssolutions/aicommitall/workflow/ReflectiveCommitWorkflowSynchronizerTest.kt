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
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.commit.AmendCommitHandler
import com.intellij.vcs.commit.CommitWorkflowHandler
import java.io.File
import java.nio.charset.Charset
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ReflectiveCommitWorkflowSynchronizerTest {
    @Test
    fun `synchronizes compatible commit workflow handlers`() {
        val handler = CompatibleHandler()
        val changeList = TestChangeList("Default")
        val items = listOf(Any(), Any())

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
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

        override fun started(step: String) {
            stepEvents += "started:$step"
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

    private companion object {
        private fun singleAttemptRetry(): CommitWorkflowSynchronizationRetry = CommitWorkflowSynchronizationRetry(
            maxAttempts = 1,
            retryInterval = Duration.ZERO,
        )
    }
}
