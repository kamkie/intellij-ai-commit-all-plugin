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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class CommitWorkflowExecutionServiceTest {
    private val gitCommitAndPushExecutorId = "Git.Commit.And.Push.Executor"

    @Test
    fun `starts default commit through workflow executor listener`() {
        val scheduler = CapturingScheduler()
        val service = CommitWorkflowExecutionService(scheduler)
        val workflowHandler = CapturingCommitWorkflowHandler()

        val result = service.executeCommit(workflowHandler)

        assertEquals(CommitWorkflowExecutionResult.Started, result)
        assertEquals(1, scheduler.scheduledActionCount)

        scheduler.runScheduledActions()

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

        assertEquals(CommitWorkflowExecutionResult.Started, result)
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

        assertEquals(CommitWorkflowExecutionResult.Started, result)
        assertEquals(listOf(gitCommitAndPushExecutorId), workflowHandler.requestedExecutorIds)
        assertEquals(1, scheduler.scheduledActionCount)

        scheduler.runScheduledActions()

        assertEquals(1, workflowHandler.executeCallCount)
        assertSame(TestCommitAndPushExecutor, workflowHandler.executedExecutor)
        assertEquals(0, workflowHandler.executorCallCount)
    }

    @Test
    fun `starts safe immediate push through default commit and post-commit push listener`() {
        val scheduler = CapturingScheduler()
        val pushPlan = CapturingSafeImmediatePushPlan()
        val registrar = CapturingPostCommitPushRegistrar()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(pushPlan),
            ),
            postCommitPushRegistrar = registrar,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
        )

        assertEquals(CommitWorkflowExecutionResult.Started, result)

        scheduler.runScheduledActions()

        assertEquals(1, registrar.registerCallCount)
        assertSame(pushPlan, registrar.pushPlan)
        assertEquals(1, workflowHandler.executorCallCount)
        assertNull(workflowHandler.executor)
        assertEquals(0, workflowHandler.executeCallCount)
        assertEquals(0, pushPlan.pushCallCount)

        registrar.pushPlan?.push()

        assertEquals(1, pushPlan.pushCallCount)
    }

    @Test
    fun `falls back to Git commit and push executor when safe immediate push is unavailable`() {
        val scheduler = CapturingScheduler()
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.MissingTrackedUpstream),
            ),
            postCommitPushRegistrar = CapturingPostCommitPushRegistrar(),
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
        )

        assertEquals(CommitWorkflowExecutionResult.Started, result)

        scheduler.runScheduledActions()

        assertEquals(1, workflowHandler.executeCallCount)
        assertSame(TestCommitAndPushExecutor, workflowHandler.executedExecutor)
        assertEquals(0, workflowHandler.executorCallCount)
    }

    @Test
    fun `falls back to Git commit and push executor when post-commit listener cannot be registered`() {
        val scheduler = CapturingScheduler()
        val registrar = CapturingPostCommitPushRegistrar(registered = false)
        val service = CommitWorkflowExecutionService(
            scheduler = scheduler,
            safeImmediatePushSupport = TestSafeImmediatePushSupport(
                SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
            ),
            postCommitPushRegistrar = registrar,
        )
        val workflowHandler = CapturingCommitWorkflowHandler(
            commitAndPushExecutor = TestCommitAndPushExecutor,
            commitAndPushEnabled = true,
        )

        val result = service.executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = GitChangeSelection(emptyList()),
        )

        assertEquals(CommitWorkflowExecutionResult.Started, result)

        scheduler.runScheduledActions()

        assertEquals(1, registrar.registerCallCount)
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

        assertEquals(CommitWorkflowExecutionResult.Started, result)
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

        assertEquals(CommitWorkflowExecutionResult.Started, result)
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
        var pushCallCount = 0

        override fun push() {
            pushCallCount++
        }
    }

    private class CapturingPostCommitPushRegistrar(
        private val registered: Boolean = true,
    ) : PostCommitPushRegistrar {
        var registerCallCount = 0
        var pushPlan: SafeImmediatePushPlan? = null

        override fun register(
            workflowHandler: CommitWorkflowHandler,
            pushPlan: SafeImmediatePushPlan,
        ): PostCommitPushRegistration? {
            registerCallCount++
            this.pushPlan = pushPlan
            return if (registered) {
                PostCommitPushRegistration { }
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
    ) : CommitWorkflowHandler, CommitExecutorListener {
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

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean =
            executor === commitAndPushExecutor && commitAndPushEnabled

        override fun execute(executor: CommitExecutor) {
            executeCallCount++
            executedExecutor = executor
            executeFailure?.let { failure -> throw failure }
        }

        override fun getState(): CommitWorkflowHandlerState =
            CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
    }

    private object UnsupportedCommitWorkflowHandler : CommitWorkflowHandler {
        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for execution tests.")

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = error("Not needed for execution tests.")

        override fun getState(): CommitWorkflowHandlerState =
            CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
    }

    private object TestCommitAndPushExecutor : CommitExecutor {
        override fun getActionText(): String = "Commit and Push"
    }
}
