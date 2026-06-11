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

import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.vcs.commit.AmendCommitHandler
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowHandlerState
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushFallbackReason
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushSupport
import java.util.concurrent.CompletableFuture
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
    fun `starts safe immediate push through default commit and post-commit push listener`() {
        val scheduler = CapturingScheduler()
        val postCommitPushScheduler = CapturingScheduler()
        val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
        val pushPlan = CapturingSafeImmediatePushPlan()
        val immediatePushExecutor = CapturingImmediatePushExecutor()
        val registrar = CapturingCommitResultRegistrar()
        var pushStartedCount = 0
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            postCommitPushScheduler = postCommitPushScheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(pushPlan),
            ),
            immediatePushExecutor = immediatePushExecutor,
            commitResultRegistrar = registrar,
            defaultCommitExecutionGate = defaultCommitExecutionGate,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
            onPushStarted = { pushStartedCount++ },
        )

        val started = result.asStarted()

        scheduler.runScheduledActions()

        assertEquals(1, registrar.registerCallCount)
        assertEquals(1, defaultCommitExecutionGate.readyActionCount)
        assertEquals(0, workflowHandler.executorCallCount)
        assertEquals(0, workflowHandler.executeCallCount)
        assertEquals(0, pushStartedCount)
        assertEquals(0, pushPlan.pushCallCount)
        assertFalse(started.completion.isDone)

        defaultCommitExecutionGate.runReadyActions()

        assertEquals(1, workflowHandler.executorCallCount)
        assertNull(workflowHandler.executor)
        assertEquals(0, workflowHandler.executeCallCount)
        assertEquals(0, pushStartedCount)
        assertEquals(0, pushPlan.pushCallCount)
        assertFalse(started.completion.isDone)

        registrar.resultHandler?.onSuccess()

        assertEquals(1, pushStartedCount)
        assertEquals(0, pushPlan.pushCallCount)
        assertEquals(1, postCommitPushScheduler.scheduledActionCount)
        assertFalse(started.completion.isDone)

        postCommitPushScheduler.runScheduledActions()

        assertEquals(1, immediatePushExecutor.pushCallCount)
        assertEquals(1, pushPlan.pushCallCount)
        assertFalse(started.completion.isDone)

        pushPlan.completePush()

        assertTrue(started.completion.isDone)
    }

    @Test
    fun `safe immediate push does not start after post-commit cancel or failure`() {
        val cases = listOf<CommitWorkflowResultHandler.() -> Unit>(
            CommitWorkflowResultHandler::onCancel,
            CommitWorkflowResultHandler::onFailure,
        )

        cases.forEach { completeCommit ->
            val scheduler = CapturingScheduler()
            val postCommitPushScheduler = CapturingScheduler()
            val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
            val pushPlan = CapturingSafeImmediatePushPlan()
            val immediatePushExecutor = CapturingImmediatePushExecutor()
            val registrar = CapturingCommitResultRegistrar()
            var pushStartedCount = 0
            val service = CommitWorkflowExecutionService(
                scheduler = scheduler,
                postCommitPushScheduler = postCommitPushScheduler,
                safeImmediatePushSupport = TestSafeImmediatePushSupport(
                    SafeImmediatePushDecision.Immediate(pushPlan),
                ),
                immediatePushExecutor = immediatePushExecutor,
                commitResultRegistrar = registrar,
                defaultCommitExecutionGate = defaultCommitExecutionGate,
            )
            val workflowHandler = CapturingCommitWorkflowHandler(
                commitAndPushExecutor = TestCommitAndPushExecutor,
                commitAndPushEnabled = true,
            )

            val result = service.executeCommitAndPush(
                workflowHandler = workflowHandler,
                selection = GitChangeSelection(emptyList()),
                onPushStarted = { pushStartedCount++ },
            ).asStarted()

            scheduler.runScheduledActions()
            defaultCommitExecutionGate.runReadyActions()

            registrar.resultHandler?.completeCommit()

            assertTrue(result.completion.isDone)
            assertEquals(0, pushStartedCount)
            assertEquals(0, postCommitPushScheduler.scheduledActionCount)
            assertEquals(0, immediatePushExecutor.pushCallCount)
            assertEquals(0, pushPlan.pushCallCount)
        }
    }

    @Test
    fun `safe immediate push completion fails when push executor throws synchronously`() {
        val failure = IllegalStateException("push failed")
        val scheduler = CapturingScheduler()
        val postCommitPushScheduler = CapturingScheduler()
        val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
        val registrar = CapturingCommitResultRegistrar()
        var pushStartedCount = 0
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            postCommitPushScheduler = postCommitPushScheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
            ),
            immediatePushExecutor = ThrowingImmediatePushExecutor(failure),
            commitResultRegistrar = registrar,
            defaultCommitExecutionGate = defaultCommitExecutionGate,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
            onPushStarted = { pushStartedCount++ },
        ).asStarted()

        scheduler.runScheduledActions()
        defaultCommitExecutionGate.runReadyActions()
        registrar.resultHandler?.onSuccess()

        val thrown = assertFailsWith<IllegalStateException> {
            postCommitPushScheduler.runScheduledActions()
        }

        assertSame(failure, thrown)
        assertEquals(1, pushStartedCount)
        assertTrue(result.completion.isCompletedExceptionally)
    }

    @Test
    fun `safe immediate push completion fails when push future fails asynchronously`() {
        val scheduler = CapturingScheduler()
        val postCommitPushScheduler = CapturingScheduler()
        val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
        val pushPlan = CapturingSafeImmediatePushPlan()
        val registrar = CapturingCommitResultRegistrar()
        val immediatePushExecutor = CapturingImmediatePushExecutor()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            postCommitPushScheduler = postCommitPushScheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(pushPlan),
            ),
            immediatePushExecutor = immediatePushExecutor,
            commitResultRegistrar = registrar,
            defaultCommitExecutionGate = defaultCommitExecutionGate,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
        ).asStarted()

        scheduler.runScheduledActions()
        defaultCommitExecutionGate.runReadyActions()
        registrar.resultHandler?.onSuccess()
        postCommitPushScheduler.runScheduledActions()

        pushPlan.failPush(IllegalStateException("push failed"))

        assertEquals(1, immediatePushExecutor.pushCallCount)
        assertTrue(result.completion.isCompletedExceptionally)
    }

    @Test
    fun `falls back to Git commit and push executor when safe immediate push is unavailable`() {
        val scheduler = CapturingScheduler()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.MissingTrackedUpstream),
            ),
            commitResultRegistrar = CapturingCommitResultRegistrar(),
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
        )

        result.asStarted()

        scheduler.runScheduledActions()

        assertEquals(1, workflowHandler.executeCallCount)
        assertSame(TestCommitAndPushExecutor, workflowHandler.executedExecutor)
        assertEquals(0, workflowHandler.executorCallCount)
    }

    @Test
    fun `fallback commit and push stays active between commit success and workflow refresh`() {
        val scheduler = CapturingScheduler()
        val registrar = CapturingCommitResultRegistrar()
        var pushStartedCount = 0
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.MissingTrackedUpstream),
            ),
            commitResultRegistrar = registrar,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
            onPushStarted = { pushStartedCount++ },
        )

        val started = result.asStarted()

        scheduler.runScheduledActions()
        registrar.resultHandler?.onSuccess()

        assertFalse(started.completion.isDone)
        assertEquals(1, pushStartedCount)

        registrar.resultHandler?.onAfterRefresh()

        assertTrue(started.completion.isDone)
    }

    @Test
    fun `fallback commit and push completes on cancel or failure before workflow refresh`() {
        val cases = listOf<CommitWorkflowResultHandler.() -> Unit>(
            CommitWorkflowResultHandler::onCancel,
            CommitWorkflowResultHandler::onFailure,
        )

        cases.forEach { completeCommit ->
            val scheduler = CapturingScheduler()
            val registrar = CapturingCommitResultRegistrar()
            var pushStartedCount = 0
            val service = CommitWorkflowExecutionService(
                scheduler = scheduler,
                safeImmediatePushSupport = TestSafeImmediatePushSupport(
                    SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.MissingTrackedUpstream),
                ),
                commitResultRegistrar = registrar,
            )
            val workflowHandler = CapturingCommitWorkflowHandler(
                commitAndPushExecutor = TestCommitAndPushExecutor,
                commitAndPushEnabled = true,
            )

            val result = service.executeCommitAndPush(
                workflowHandler = workflowHandler,
                selection = GitChangeSelection(emptyList()),
                onPushStarted = { pushStartedCount++ },
            ).asStarted()

            scheduler.runScheduledActions()
            registrar.resultHandler?.completeCommit()

            assertTrue(result.completion.isDone)
            assertEquals(0, pushStartedCount)
        }
    }

    @Test
    fun `falls back to Git commit and push executor when post-commit listener cannot be registered`() {
        val scheduler = CapturingScheduler()
        val registrar = CapturingCommitResultRegistrar(registered = false)
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
            ),
            commitResultRegistrar = registrar,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
        )

        result.asStarted()

        scheduler.runScheduledActions()

        assertEquals(2, registrar.registerCallCount)
        assertEquals(1, workflowHandler.executeCallCount)
        assertSame(TestCommitAndPushExecutor, workflowHandler.executedExecutor)
        assertEquals(0, workflowHandler.executorCallCount)
    }

    @Test
    fun `describes safe immediate push fallback reason in immediate push attempt diagnostics`() {
        val registrar = CapturingCommitResultRegistrar()
        val service = CommitWorkflowExecutionService(
            scheduler = CapturingScheduler(),
            commitResultRegistrar = registrar,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val attempt = service.executeImmediatePushWhenSafe(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.MissingTrackedUpstream),
            ),
            onPushStarted = {},
            completion = CompletableFuture(),
        )

        assertEquals(ImmediatePushAttempt.Fallback("MissingTrackedUpstream"), attempt)
        assertEquals(
            "immediatePushStarted=false, fallbackReason=MissingTrackedUpstream",
            attempt.diagnosticSummary(),
        )
        assertEquals(0, registrar.registerCallCount)
    }

    @Test
    fun `describes missing selection in immediate push attempt diagnostics`() {
        val support = TestSafeImmediatePushSupport(
            SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.MissingTrackedUpstream),
        )
        val service = CommitWorkflowExecutionService(CapturingScheduler())

        val attempt = service.executeImmediatePushWhenSafe(
            workflowHandler = CapturingCommitWorkflowHandler(),
            selection = null,
            safeImmediatePushSupport = support,
            onPushStarted = {},
            completion = CompletableFuture(),
        )

        assertEquals(ImmediatePushAttempt.Fallback("NoSelection"), attempt)
        assertEquals(
            "immediatePushStarted=false, fallbackReason=NoSelection",
            attempt.diagnosticSummary(),
        )
        assertEquals(0, support.prepareCallCount)
    }

    @Test
    fun `describes unsupported workflow handler in immediate push attempt diagnostics`() {
        val support = TestSafeImmediatePushSupport(
            SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
        )
        val service = CommitWorkflowExecutionService(CapturingScheduler())

        val attempt = service.executeImmediatePushWhenSafe(
            workflowHandler = UnsupportedCommitWorkflowHandler,
            selection = GitChangeSelection(emptyList()),
            safeImmediatePushSupport = support,
            onPushStarted = {},
            completion = CompletableFuture(),
        )

        assertEquals(ImmediatePushAttempt.Fallback("UnsupportedHandler"), attempt)
        assertEquals(
            "immediatePushStarted=false, fallbackReason=UnsupportedHandler",
            attempt.diagnosticSummary(),
        )
        assertEquals(1, support.prepareCallCount)
    }

    @Test
    fun `describes unavailable result listener in immediate push attempt diagnostics`() {
        val registrar = CapturingCommitResultRegistrar(registered = false)
        val service = CommitWorkflowExecutionService(
            scheduler = CapturingScheduler(),
            commitResultRegistrar = registrar,
        )

        val attempt = service.executeImmediatePushWhenSafe(
            workflowHandler = CapturingCommitWorkflowHandler(
                commitAndPushExecutor = TestCommitAndPushExecutor,
                commitAndPushEnabled = true,
            ),
            selection = GitChangeSelection(emptyList()),
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
            ),
            onPushStarted = {},
            completion = CompletableFuture(),
        )

        assertEquals(ImmediatePushAttempt.Fallback("ResultListenerUnavailable"), attempt)
        assertEquals(1, registrar.registerCallCount)
    }

    @Test
    fun `reports started immediate push attempt without fallback reason`() {
        val registrar = CapturingCommitResultRegistrar()
        val defaultCommitExecutionGate = CapturingDefaultCommitExecutionGate()
        val service = CommitWorkflowExecutionService(
            scheduler = CapturingScheduler(),
            commitResultRegistrar = registrar,
            defaultCommitExecutionGate = defaultCommitExecutionGate,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val attempt = service.executeImmediatePushWhenSafe(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
            ),
            onPushStarted = {},
            completion = CompletableFuture(),
        )

        assertEquals(ImmediatePushAttempt.Started, attempt)
        assertEquals("immediatePushStarted=true", attempt.diagnosticSummary())
        assertEquals(1, registrar.registerCallCount)
        assertEquals(1, defaultCommitExecutionGate.readyActionCount)
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

    private class CapturingScheduler : CommitWorkflowExecutionScheduler {
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

    private class CapturingDefaultCommitExecutionGate : DefaultCommitExecutionGate {
        private val actions = mutableListOf<() -> Unit>()

        val readyActionCount: Int
            get() = actions.size

        override fun runWhenReady(
            workflowHandler: CommitWorkflowHandler,
            action: () -> Unit,
        ) {
            actions += action
        }

        fun runReadyActions() {
            val readyActions = actions.toList()
            actions.clear()
            readyActions.forEach { action -> action() }
        }
    }

    private class ThrowingDefaultCommitExecutionGate(
        private val failure: RuntimeException,
    ) : DefaultCommitExecutionGate {
        override fun runWhenReady(
            workflowHandler: CommitWorkflowHandler,
            action: () -> Unit,
        ): Unit = throw failure
    }

    private class TestSafeImmediatePushSupport(
        private val decision: SafeImmediatePushDecision,
    ) : SafeImmediatePushSupport {
        var prepareCallCount = 0

        override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision {
            prepareCallCount++
            return decision
        }
    }

    private class CapturingSafeImmediatePushPlan : SafeImmediatePushPlan {
        private val completion = CompletableFuture<Unit>()
        var pushCallCount = 0

        override fun push(): CompletableFuture<Unit> {
            pushCallCount++
            return completion
        }

        fun completePush() {
            completion.complete(Unit)
        }

        fun failPush(failure: RuntimeException) {
            completion.completeExceptionally(failure)
        }
    }

    private class CapturingImmediatePushExecutor : ImmediatePushExecutor {
        var pushCallCount = 0

        override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> {
            pushCallCount++
            return pushPlan.push()
        }
    }

    private class ThrowingImmediatePushExecutor(
        private val failure: RuntimeException,
    ) : ImmediatePushExecutor {
        override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> = throw failure
    }

    private class CapturingCommitResultRegistrar(
        private val registered: Boolean = true,
    ) : CommitWorkflowResultRegistrar {
        var registerCallCount = 0
        var resultHandler: CommitWorkflowResultHandler? = null
        private val registrations = mutableListOf<CapturingCommitWorkflowResultRegistration>()

        val disposeCallCount: Int
            get() = registrations.sumOf { registration -> registration.disposeCallCount }

        override fun register(
            workflowHandler: CommitWorkflowHandler,
            resultHandler: CommitWorkflowResultHandler,
        ): CommitWorkflowResultRegistration? {
            registerCallCount++
            this.resultHandler = resultHandler
            return if (registered) {
                CapturingCommitWorkflowResultRegistration()
                    .also(registrations::add)
            } else {
                null
            }
        }
    }

    private class CapturingCommitWorkflowResultRegistration : CommitWorkflowResultRegistration {
        var disposeCallCount = 0

        override fun dispose() {
            disposeCallCount += 1
        }
    }

    private class CapturingCommitWorkflowHandler(
        private val commitAndPushExecutor: CommitExecutor? = null,
        var commitAndPushEnabled: Boolean = false,
        private val defaultCommitFailure: RuntimeException? = null,
        private val executeFailure: RuntimeException? = null,
    ) : CommitWorkflowHandler,
        CommitExecutorListener {
        var executorCallCount = 0
        var executor: CommitExecutor? = null
        var executeCallCount = 0
        var executedExecutor: CommitExecutor? = null
        val requestedExecutorIds = mutableListOf<String>()

        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for execution tests.")

        override fun executorCalled(executor: CommitExecutor?) {
            executorCallCount++
            this.executor = executor
            defaultCommitFailure?.let { failure -> throw failure }
        }

        override fun getExecutor(executorId: String): CommitExecutor? {
            requestedExecutorIds += executorId
            return if (executorId == "Git.Commit.And.Push.Executor") {
                commitAndPushExecutor
            } else {
                null
            }
        }

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean {
            val isCommitAndPushExecutor = executor === commitAndPushExecutor
            return isCommitAndPushExecutor && commitAndPushEnabled
        }

        override fun execute(executor: CommitExecutor) {
            executeCallCount++
            executedExecutor = executor
            executeFailure?.let { failure -> throw failure }
        }

        override fun getState(): CommitWorkflowHandlerState {
            val state = CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
            return state
        }
    }

    private object UnsupportedCommitWorkflowHandler : CommitWorkflowHandler {
        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for execution tests.")

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = error("Not needed for execution tests.")

        override fun getState(): CommitWorkflowHandlerState {
            val state = CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
            return state
        }
    }

    private object TestCommitAndPushExecutor : CommitExecutor {
        override fun getActionText(): String = "Commit and Push"
    }

    private fun CommitWorkflowExecutionResult.asStarted(): CommitWorkflowExecutionResult.Started {
        val started = this as CommitWorkflowExecutionResult.Started
        return started
    }
}
