package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushFallbackReason
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushSupport

@Service(Service.Level.PROJECT)
internal class CommitWorkflowExecutionService(
    private val scheduler: CommitWorkflowExecutionScheduler = IntellijCommitWorkflowExecutionScheduler,
    private val safeImmediatePushSupport: SafeImmediatePushSupport = FallbackSafeImmediatePushSupport,
    private val postCommitPushRegistrar: PostCommitPushRegistrar = IntellijPostCommitPushRegistrar,
) {
    fun canExecuteCommit(workflowHandler: CommitWorkflowHandler?): Boolean =
        workflowHandler is CommitExecutorListener

    fun executeCommit(workflowHandler: CommitWorkflowHandler?): CommitWorkflowExecutionResult {
        if (workflowHandler == null) {
            return CommitWorkflowExecutionResult.MissingWorkflow
        }

        val executorListener = workflowHandler as? CommitExecutorListener
            ?: return CommitWorkflowExecutionResult.UnsupportedExecutor
        scheduler.schedule {
            executorListener.executorCalled(null)
        }
        return CommitWorkflowExecutionResult.Started
    }

    fun canExecuteCommitAndPush(workflowHandler: CommitWorkflowHandler?): Boolean {
        if (workflowHandler == null) {
            return false
        }

        val executor = workflowHandler.getExecutor(GIT_COMMIT_AND_PUSH_EXECUTOR_ID)
            ?: return false
        return workflowHandler.isExecutorEnabled(executor)
    }

    fun executeCommitAndPush(
        workflowHandler: CommitWorkflowHandler?,
        selection: GitChangeSelection? = null,
        safeImmediatePushSupport: SafeImmediatePushSupport = this.safeImmediatePushSupport,
    ): CommitWorkflowExecutionResult {
        if (workflowHandler == null) {
            return CommitWorkflowExecutionResult.MissingWorkflow
        }

        val executor = workflowHandler.getExecutor(GIT_COMMIT_AND_PUSH_EXECUTOR_ID)
            ?: return CommitWorkflowExecutionResult.UnsupportedExecutor
        if (!workflowHandler.isExecutorEnabled(executor)) {
            return CommitWorkflowExecutionResult.DisabledExecutor
        }

        scheduler.schedule {
            if (workflowHandler.isExecutorEnabled(executor)) {
                val immediatePushStarted = selection != null &&
                    executeImmediatePushWhenSafe(
                        workflowHandler = workflowHandler,
                        selection = selection,
                        safeImmediatePushSupport = safeImmediatePushSupport,
                    )
                if (!immediatePushStarted) {
                    workflowHandler.execute(executor)
                }
            }
        }
        return CommitWorkflowExecutionResult.Started
    }

    private fun executeImmediatePushWhenSafe(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
        safeImmediatePushSupport: SafeImmediatePushSupport,
    ): Boolean {
        val executorListener = workflowHandler as? CommitExecutorListener
            ?: return false
        val decision = safeImmediatePushSupport.prepare(selection)
        if (decision !is SafeImmediatePushDecision.Immediate) {
            return false
        }

        val registration = postCommitPushRegistrar.register(workflowHandler, decision.plan)
            ?: return false

        try {
            executorListener.executorCalled(null)
        } catch (throwable: Throwable) {
            registration.dispose()
            throw throwable
        }
        return true
    }

    companion object {
        fun getInstance(project: Project): CommitWorkflowExecutionService = project.service()

        private const val GIT_COMMIT_AND_PUSH_EXECUTOR_ID = "Git.Commit.And.Push.Executor"
    }
}

private object FallbackSafeImmediatePushSupport : SafeImmediatePushSupport {
    override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision =
        SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.UnsupportedPushApi)
}

internal fun interface CommitWorkflowExecutionScheduler {
    fun schedule(action: () -> Unit)
}

private object IntellijCommitWorkflowExecutionScheduler : CommitWorkflowExecutionScheduler {
    override fun schedule(action: () -> Unit) {
        val application = ApplicationManager.getApplication()
        if (application == null) {
            action()
        } else {
            application.invokeLater(action)
        }
    }
}

internal enum class CommitWorkflowExecutionResult {
    Started,
    MissingWorkflow,
    UnsupportedExecutor,
    DisabledExecutor,
}
