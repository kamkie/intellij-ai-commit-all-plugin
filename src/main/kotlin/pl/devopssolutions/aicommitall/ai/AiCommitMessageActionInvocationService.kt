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
package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
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
        dataContextFactory = IntellijAiInvocationDataContextFactory,
    )

    fun invokeCommitMessageGeneration(
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        inputEvent: InputEvent? = null,
    ): AiCommitMessageActionInvocationResult = invoker.invokeCommitMessageGeneration(
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
    private val dataContextFactory: AiInvocationDataContextFactory = IntellijAiInvocationDataContextFactory,
) {
    fun invokeCommitMessageGeneration(
        project: Project,
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        inputEvent: InputEvent? = null,
    ): AiCommitMessageActionInvocationResult = AiCommitMessageInvocationWorkflow.create(workflowHandler, workflowUi)
        ?.let { workflow ->
            invokeCommitMessageGeneration(
                project = project,
                workflow = workflow,
                parentDataContext = parentDataContext,
                inputEvent = inputEvent,
            )
        }
        ?: AiCommitMessageActionInvocationResult.MissingWorkflow

    private fun invokeCommitMessageGeneration(
        project: Project,
        workflow: AiCommitMessageInvocationWorkflow,
        parentDataContext: DataContext,
        inputEvent: InputEvent?,
    ): AiCommitMessageActionInvocationResult {
        val actionReference = actionFinder.findCommitMessageAction()
        return if (actionReference == null) {
            AiCommitMessageActionInvocationResult.MissingAction
        } else {
            invokeCommitMessageGeneration(
                project = project,
                workflow = workflow,
                actionReference = actionReference,
                parentDataContext = parentDataContext,
                inputEvent = inputEvent,
            )
        }
    }

    private fun invokeCommitMessageGeneration(
        project: Project,
        workflow: AiCommitMessageInvocationWorkflow,
        actionReference: AiCommitMessageActionReference,
        parentDataContext: DataContext,
        inputEvent: InputEvent?,
    ): AiCommitMessageActionInvocationResult {
        val dataContext = dataContextFactory.createDataContext(
            project = project,
            workflowHandler = workflow.workflowHandler,
            workflowUi = workflow.workflowUi,
            parentDataContext = parentDataContext,
        )

        actionSystemInvoker.invoke(
            actionReference = actionReference,
            dataContext = dataContext,
            inputEvent = inputEvent,
        )

        return AiCommitMessageActionInvocationResult.Invoked(
            action = actionReference.action,
            actionId = actionReference.actionId,
            source = actionReference.source,
        )
    }
}

private data class AiCommitMessageInvocationWorkflow(
    val workflowHandler: CommitWorkflowHandler,
    val workflowUi: CommitWorkflowUi,
) {
    companion object {
        fun create(
            workflowHandler: CommitWorkflowHandler?,
            workflowUi: CommitWorkflowUi?,
        ): AiCommitMessageInvocationWorkflow? = if (workflowHandler != null && workflowUi != null) {
            AiCommitMessageInvocationWorkflow(workflowHandler, workflowUi)
        } else {
            null
        }
    }
}

internal sealed interface AiCommitMessageActionInvocationResult {
    data class Invoked(
        val action: com.intellij.openapi.actionSystem.AnAction,
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

internal fun interface AiInvocationDataContextFactory {
    fun createDataContext(
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
    ): DataContext
}

private object IntellijAiInvocationDataContextFactory : AiInvocationDataContextFactory {
    override fun createDataContext(
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
    ): DataContext = AiCommitMessageActionInvocationContextFactory.createDataContext(
        project = project,
        workflowHandler = workflowHandler,
        workflowUi = workflowUi,
        parentDataContext = parentDataContext,
    )
}

private object IntellijAiActionSystemInvoker : AiActionSystemInvoker {
    override fun invoke(
        actionReference: AiCommitMessageActionReference,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ) {
        val event = AnActionEvent.createEvent(
            actionReference.action,
            dataContext,
            actionReference.action.templatePresentation.clone(),
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            inputEvent,
        )
        ActionUtil.performAction(actionReference.action, event)
    }
}
