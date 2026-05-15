package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler

@Service(Service.Level.PROJECT)
internal class CommitWorkflowExecutionService(
    private val scheduler: CommitWorkflowExecutionScheduler = IntellijCommitWorkflowExecutionScheduler,
) {
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

    companion object {
        fun getInstance(project: Project): CommitWorkflowExecutionService = project.service()
    }
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
}
