package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.vcs.commit.AmendCommitHandler
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowHandlerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CommitWorkflowExecutionServiceTest {
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

    private class CapturingCommitWorkflowHandler : CommitWorkflowHandler, CommitExecutorListener {
        var executorCallCount = 0
        var executor: CommitExecutor? = null

        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for execution tests.")

        override fun executorCalled(executor: CommitExecutor?) {
            executorCallCount++
            this.executor = executor
        }

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = error("Default commit should use executor listener.")

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
}
