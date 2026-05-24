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
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.awt.event.InputEvent

internal object AiCommitMessageActionInvocationContextFactory {
    fun createInvocationContext(
        actionReference: AiCommitMessageActionReference,
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        inputEvent: InputEvent? = null,
    ): AiCommitMessageActionInvocationContext {
        val dataContext = createDataContext(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
        )
        val presentation = actionReference.action.templatePresentation.clone()
        val event = AnActionEvent.createEvent(
            actionReference.action,
            dataContext,
            presentation,
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            inputEvent,
        )

        return AiCommitMessageActionInvocationContext(
            dataContext = dataContext,
            event = event,
        )
    }

    internal fun createDataContext(
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
    ): DataContext {
        val data = collectData(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
        )
        val builder = SimpleDataContext.builder()
            .setParent(parentDataContext)
            .add(CommonDataKeys.PROJECT, data.project)
            .add(VcsDataKeys.COMMIT_WORKFLOW_HANDLER, data.workflowHandler)
            .add(VcsDataKeys.COMMIT_WORKFLOW_UI, data.workflowUi)

        data.commitMessageControl?.let { commitMessageControl ->
            builder.add(VcsDataKeys.COMMIT_MESSAGE_CONTROL, commitMessageControl)
        }
        data.commitMessageDocument?.let { commitMessageDocument ->
            builder.add(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT, commitMessageDocument)
        }

        return builder.build()
    }

    internal fun collectData(
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
    ): AiCommitMessageActionInvocationData = AiCommitMessageActionInvocationData(
        project = project,
        workflowHandler = workflowHandler,
        workflowUi = workflowUi,
        commitMessageControl = resolveCommitMessageControl(workflowUi, parentDataContext),
        commitMessageDocument = resolveCommitMessageDocument(workflowUi, parentDataContext),
    )

    private fun resolveCommitMessageControl(
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
    ): CommitMessageI? = workflowUi.commitMessageUi as? CommitMessageI
        ?: VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(parentDataContext)

    private fun resolveCommitMessageDocument(
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
    ): Document? = CommitMessageUiAccessors.editorDocument(workflowUi.commitMessageUi)
        ?: VcsDataKeys.COMMIT_MESSAGE_DOCUMENT.getData(parentDataContext)
}

internal data class AiCommitMessageActionInvocationContext(
    val dataContext: DataContext,
    val event: AnActionEvent,
)

internal data class AiCommitMessageActionInvocationData(
    val project: Project,
    val workflowHandler: CommitWorkflowHandler,
    val workflowUi: CommitWorkflowUi,
    val commitMessageControl: CommitMessageI?,
    val commitMessageDocument: Document?,
)
