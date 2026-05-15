package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.awt.event.InputEvent

@Service(Service.Level.PROJECT)
internal class AiCommitMessageActionInvocationService(private val project: Project) {
    private val invoker = AiCommitMessageActionInvoker(
        actionFinder = AiCommitMessageActionDiscoveryService.getInstance(),
        actionSystemInvoker = IntellijAiActionSystemInvoker,
    )

    fun invokeCommitMessageGeneration(
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        inputEvent: InputEvent? = null,
    ): AiCommitMessageActionInvocationResult =
        invoker.invokeCommitMessageGeneration(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
            inputEvent = inputEvent,
        )

    companion object {
        fun getInstance(project: Project): AiCommitMessageActionInvocationService = project.service()
    }
}

internal class AiCommitMessageActionInvoker(
    private val actionFinder: AiCommitMessageActionFinder,
    private val actionSystemInvoker: AiActionSystemInvoker,
) {
    fun invokeCommitMessageGeneration(
        project: Project,
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        inputEvent: InputEvent? = null,
    ): AiCommitMessageActionInvocationResult {
        if (workflowHandler == null || workflowUi == null) {
            return AiCommitMessageActionInvocationResult.MissingWorkflow
        }

        val actionReference = actionFinder.findCommitMessageAction()
            ?: return AiCommitMessageActionInvocationResult.MissingAction
        val dataContext = AiCommitMessageActionInvocationContextFactory.createDataContext(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
        )

        actionSystemInvoker.invoke(
            actionReference = actionReference,
            dataContext = dataContext,
            inputEvent = inputEvent,
        )

        return AiCommitMessageActionInvocationResult.Invoked(
            actionId = actionReference.actionId,
            source = actionReference.source,
        )
    }
}

internal sealed interface AiCommitMessageActionInvocationResult {
    data class Invoked(
        val actionId: String?,
        val source: AiCommitMessageActionSource,
    ) : AiCommitMessageActionInvocationResult

    data object MissingWorkflow : AiCommitMessageActionInvocationResult

    data object MissingAction : AiCommitMessageActionInvocationResult
}

internal interface AiActionSystemInvoker {
    fun invoke(
        actionReference: AiCommitMessageActionReference,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    )
}

private object IntellijAiActionSystemInvoker : AiActionSystemInvoker {
    @Suppress("DEPRECATION")
    override fun invoke(
        actionReference: AiCommitMessageActionReference,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ) {
        ActionUtil.invokeAction(
            actionReference.action,
            dataContext,
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            inputEvent,
            null,
        )
    }
}
