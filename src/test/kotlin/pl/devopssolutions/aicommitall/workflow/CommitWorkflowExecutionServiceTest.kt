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
    fun `starts default commit through workflow executor listener`() {
        val scheduler = CapturingScheduler()
        val service = CommitWorkflowExecutionService(scheduler)
        val workflowHandler = CapturingCommitWorkflowHandler()

        val result = service.executeCommit(workflowHandler)

        val started = result.asStarted()
        assertFalse(started.completion.isDone)
        assertEquals(1, scheduler.scheduledActionCount)

        scheduler.runScheduledActions()

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
            actions.forEach { action -> action() }
        }
    }

    private class TestSafeImmediatePushSupport(
        private val decision: SafeImmediatePushDecision,
    ) : SafeImmediatePushSupport {
        override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision = decision
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
    }

    private class CapturingImmediatePushExecutor : ImmediatePushExecutor {
        var pushCallCount = 0

        override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> {
            pushCallCount++
            return pushPlan.push()
        }
    }

    private class CapturingCommitResultRegistrar(
        private val registered: Boolean = true,
    ) : CommitWorkflowResultRegistrar {
        var registerCallCount = 0
        var resultHandler: CommitWorkflowResultHandler? = null

        override fun register(
            workflowHandler: CommitWorkflowHandler,
            resultHandler: CommitWorkflowResultHandler,
        ): CommitWorkflowResultRegistration? {
            registerCallCount++
            this.resultHandler = resultHandler
            return if (registered) {
                CommitWorkflowResultRegistration { }
            } else {
                null
            }
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

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = executor === commitAndPushExecutor && commitAndPushEnabled

        override fun execute(executor: CommitExecutor) {
            executeCallCount++
            executedExecutor = executor
            executeFailure?.let { failure -> throw failure }
        }

        override fun getState(): CommitWorkflowHandlerState = CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
    }

    private object UnsupportedCommitWorkflowHandler : CommitWorkflowHandler {
        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for execution tests.")

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = error("Not needed for execution tests.")

        override fun getState(): CommitWorkflowHandlerState = CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
    }

    private object TestCommitAndPushExecutor : CommitExecutor {
        override fun getActionText(): String = "Commit and Push"
    }

    private fun CommitWorkflowExecutionResult.asStarted(): CommitWorkflowExecutionResult.Started = this as CommitWorkflowExecutionResult.Started
}
