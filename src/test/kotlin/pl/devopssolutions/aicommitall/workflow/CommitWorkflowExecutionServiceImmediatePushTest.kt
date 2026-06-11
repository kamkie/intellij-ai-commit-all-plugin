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

import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushFallbackReason
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CommitWorkflowExecutionServiceImmediatePushTest {
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
}
