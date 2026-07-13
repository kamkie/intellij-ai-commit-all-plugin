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

import com.intellij.vcs.commit.CommitWorkflowHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CommitWorkflowExecutionServiceTest {
    private val gitCommitAndPushExecutorId = "Git.Commit.And.Push.Executor"

    @Test
    fun `reports whether default commit executor can be called`() {
        val service = CommitWorkflowExecutionService(CapturingScheduler())

        assertTrue(service.canExecuteCommit(CapturingCommitWorkflowHandler()))
        assertFalse(service.canExecuteCommit(UnsupportedCommitWorkflowHandler))
        assertFalse(service.canExecuteCommit(null))
    }

    @Test
    fun `reports whether Git commit and push executor can be called`() {
        val service = CommitWorkflowExecutionService(CapturingScheduler())
        val enabledWorkflow = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )
        val disabledWorkflow = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = false,
        )

        assertTrue(service.canExecuteCommitAndPush(enabledWorkflow))
        assertFalse(service.canExecuteCommitAndPush(disabledWorkflow))
        assertFalse(service.canExecuteCommitAndPush(CapturingCommitWorkflowHandler()))
        assertFalse(service.canExecuteCommitAndPush(null))
    }

    @Test
    fun `starts default commit through workflow executor listener after readiness gate opens`() {
        val scheduler = CapturingScheduler()
        val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            defaultCommitExecutionGate = defaultCommitExecutionGate,
        )
        val workflowHandler = CapturingCommitWorkflowHandler()

        val result = service.executeCommit(workflowHandler)

        val started = result.asStarted()
        assertFalse(started.completion.isDone)
        assertEquals(1, scheduler.scheduledActionCount)

        scheduler.runScheduledActions()

        assertFalse(started.completion.isDone)
        assertEquals(1, defaultCommitExecutionGate.readyActionCount)
        assertEquals(0, workflowHandler.executorCallCount)

        defaultCommitExecutionGate.runReadyActions()

        assertTrue(started.completion.isDone)
        assertEquals(1, workflowHandler.executorCallCount)
        assertNull(workflowHandler.executor)
    }

    @Test
    fun `reapplies confirmed commit ui handoff immediately before default executor`() {
        val scheduler = CapturingScheduler()
        val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
        val defaultCommitUiHandoff = CapturingDefaultCommitUiHandoff()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            defaultCommitExecutionGate = defaultCommitExecutionGate,
            defaultCommitUiHandoff = defaultCommitUiHandoff,
        )
        val workflowHandler = CapturingCommitWorkflowHandler()

        service.executeCommit(workflowHandler).asStarted()
        scheduler.runScheduledActions()
        defaultCommitExecutionGate.runReadyActions()

        assertEquals(1, defaultCommitUiHandoff.applyCallCount)
        assertEquals(1, workflowHandler.executorCallCount)
    }

    @Test
    fun `stops when workflow is missing`() {
        val result = CommitWorkflowExecutionService(CapturingScheduler())
            .executeCommit(null)

        assertEquals(CommitWorkflowExecutionResult.MissingWorkflow, result)
    }

    @Test
    fun `stops when workflow does not expose default executor listener`() {
        val result = CommitWorkflowExecutionService(CapturingScheduler())
            .executeCommit(UnsupportedCommitWorkflowHandler)

        assertEquals(CommitWorkflowExecutionResult.UnsupportedExecutor, result)
    }

    @Test
    fun `does not catch default commit execution failures`() {
        val scheduler = CapturingScheduler()
        val workflowHandler = CapturingCommitWorkflowHandler(
            defaultCommitFailure = IllegalStateException("commit failed"),
        )

        val result = CommitWorkflowExecutionService(scheduler)
            .executeCommit(workflowHandler)

        result.asStarted()
        assertFailsWith<IllegalStateException> {
            scheduler.runScheduledActions()
        }
        assertEquals(1, workflowHandler.executorCallCount)
    }

    @Test
    fun `registered default commit completion completes on cancel or failure`() {
        listOf<CommitWorkflowResultHandler.() -> Unit>(
            CommitWorkflowResultHandler::onCancel,
            CommitWorkflowResultHandler::onFailure,
        ).forEach { complete ->
            val scheduler = CapturingScheduler()
            val registrar = CapturingCommitResultRegistrar()
            val service = CommitWorkflowExecutionService(
                scheduler = scheduler,
                commitResultRegistrar = registrar,
            )
            val workflowHandler = CapturingCommitWorkflowHandler()

            val result = service.executeCommit(workflowHandler).asStarted()

            scheduler.runScheduledActions()

            assertFalse(result.completion.isDone)

            registrar.resultHandler?.complete()

            assertTrue(result.completion.isDone)
        }
    }

    @Test
    fun `registered default commit completion completes on success`() {
        val scheduler = CapturingScheduler()
        val registrar = CapturingCommitResultRegistrar()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            commitResultRegistrar = registrar,
        )
        val workflowHandler = CapturingCommitWorkflowHandler()

        val result = service.executeCommit(workflowHandler).asStarted()

        scheduler.runScheduledActions()

        assertFalse(result.completion.isDone)
        assertEquals(1, workflowHandler.executorCallCount)

        registrar.resultHandler?.onSuccess()

        assertTrue(result.completion.isDone)
    }

    @Test
    fun `disposes registered default commit listener when executor throws`() {
        val failure = IllegalStateException("commit failed")
        val scheduler = CapturingScheduler()
        val registrar = CapturingCommitResultRegistrar()
        val workflowHandler = CapturingCommitWorkflowHandler(defaultCommitFailure = failure)
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            commitResultRegistrar = registrar,
        )

        val result = service.executeCommit(workflowHandler).asStarted()

        val thrown = assertFailsWith<IllegalStateException> {
            scheduler.runScheduledActions()
        }

        assertSame(failure, thrown)
        assertTrue(result.completion.isCompletedExceptionally)
        assertEquals(1, registrar.disposeCallCount)
    }

    @Test
    fun `default commit completion fails when readiness gate throws before listener registration`() {
        val failure = IllegalStateException("readiness gate failed")
        val scheduler = CapturingScheduler()
        val registrar = CapturingCommitResultRegistrar()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            commitResultRegistrar = registrar,
            defaultCommitExecutionGate = ThrowingDefaultCommitExecutionGate(failure),
        )

        val result = service.executeCommit(CapturingCommitWorkflowHandler()).asStarted()

        val thrown = assertFailsWith<IllegalStateException> {
            scheduler.runScheduledActions()
        }

        assertSame(failure, thrown)
        assertTrue(result.completion.isCompletedExceptionally)
        assertEquals(0, registrar.registerCallCount)
    }

    @Test
    fun `starts commit and push through Git commit and push executor`() {
        val scheduler = CapturingScheduler()
        val service = CommitWorkflowExecutionService(scheduler)
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(workflowHandler)

        val started = result.asStarted()
        assertFalse(started.completion.isDone)
        assertEquals(listOf(gitCommitAndPushExecutorId), workflowHandler.requestedExecutorIds)
        assertEquals(1, scheduler.scheduledActionCount)

        scheduler.runScheduledActions()

        assertTrue(started.completion.isDone)
        assertEquals(1, workflowHandler.executeCallCount)
        assertSame(TestCommitAndPushExecutor, workflowHandler.executedExecutor)
        assertEquals(0, workflowHandler.executorCallCount)
    }

    @Test
    fun `stops commit and push when workflow is missing`() {
        val result = CommitWorkflowExecutionService(CapturingScheduler())
            .executeCommitAndPush(null)

        assertEquals(CommitWorkflowExecutionResult.MissingWorkflow, result)
    }

    @Test
    fun `stops commit and push when Git commit and push executor is missing`() {
        val scheduler = CapturingScheduler()
        val workflowHandler = CapturingCommitWorkflowHandler()

        val result = CommitWorkflowExecutionService(scheduler)
            .executeCommitAndPush(workflowHandler)

        assertEquals(CommitWorkflowExecutionResult.UnsupportedExecutor, result)
        assertEquals(listOf(gitCommitAndPushExecutorId), workflowHandler.requestedExecutorIds)
        assertEquals(0, scheduler.scheduledActionCount)
    }

    @Test
    fun `stops commit and push when Git commit and push executor is disabled`() {
        val scheduler = CapturingScheduler()
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = false,
        )

        val result = CommitWorkflowExecutionService(scheduler)
            .executeCommitAndPush(workflowHandler)

        assertEquals(CommitWorkflowExecutionResult.DisabledExecutor, result)
        assertEquals(0, scheduler.scheduledActionCount)
    }

    @Test
    fun `does not execute commit and push when executor becomes disabled before scheduled execution`() {
        val scheduler = CapturingScheduler()
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = CommitWorkflowExecutionService(scheduler)
            .executeCommitAndPush(workflowHandler)
        workflowHandler.commitAndPushEnabled = false

        scheduler.runScheduledActions()

        assertTrue(result.asStarted().completion.isDone)
        assertEquals(0, workflowHandler.executeCallCount)
    }

    @Test
    fun `does not catch commit and push execution failures`() {
        val scheduler = CapturingScheduler()
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
            executeFailure = IllegalStateException("push failed"),
        )

        val result = CommitWorkflowExecutionService(scheduler)
            .executeCommitAndPush(workflowHandler)

        result.asStarted()
        assertFailsWith<IllegalStateException> {
            scheduler.runScheduledActions()
        }
        assertEquals(1, workflowHandler.executeCallCount)
    }
}

private class CapturingDefaultCommitUiHandoff : DefaultCommitUiHandoff {
    var applyCallCount = 0

    override fun apply(workflowHandler: CommitWorkflowHandler) {
        applyCallCount += 1
    }
}
