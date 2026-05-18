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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.awt.event.InputEvent

@Service(Service.Level.PROJECT)
internal class PushOnlyWorkflowExecutionService(
    private val actionManager: ActionManager = ActionManager.getInstance(),
) {
    fun canExecutePush(dataContext: DataContext): Boolean {
        val action = actionManager.getAction(IDE_PUSH_ACTION_ID) ?: return false
        val event = action.event(dataContext, inputEvent = null)
        ActionUtil.updateAction(action, event)
        return event.presentation.isEnabled && event.presentation.isVisible
    }

    fun executePush(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult {
        val action = actionManager.getAction(IDE_PUSH_ACTION_ID)
            ?: return CommitWorkflowExecutionResult.UnsupportedExecutor
        val event = action.event(dataContext, inputEvent)
        ActionUtil.updateAction(action, event)
        if (!event.presentation.isEnabled || !event.presentation.isVisible) {
            return CommitWorkflowExecutionResult.DisabledExecutor
        }

        ActionUtil.performAction(action, event)
        return CommitWorkflowExecutionResult.Started()
    }

    private fun AnAction.event(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): AnActionEvent = AnActionEvent(
        dataContext,
        Presentation(),
        ActionPlaces.CHANGES_VIEW_TOOLBAR,
        ActionUiKind.NONE,
        inputEvent,
        0,
        actionManager,
    )

    companion object {
        fun getInstance(project: Project): PushOnlyWorkflowExecutionService = project.service()

        internal const val IDE_PUSH_ACTION_ID: String = "Vcs.Push"
    }
}
