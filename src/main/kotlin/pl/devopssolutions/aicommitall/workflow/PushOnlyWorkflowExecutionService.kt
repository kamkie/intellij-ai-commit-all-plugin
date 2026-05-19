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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import pl.devopssolutions.aicommitall.vcs.SafeImmediateOutgoingPushSupport
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushService
import java.awt.event.InputEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
internal class PushOnlyWorkflowExecutionService(
    private val outgoingPushSupport: SafeImmediateOutgoingPushSupport,
    private val idePushAction: PushOnlyIdeAction,
    private val immediatePushExecutor: ImmediatePushExecutor = IntellijImmediatePushExecutor,
) {
    constructor(project: Project) : this(
        outgoingPushSupport = SafeImmediatePushService.getInstance(project),
        idePushAction = IntellijPushOnlyIdeAction(ActionManager.getInstance()),
    )

    fun executePush(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult {
        val decision = outgoingPushSupport.prepareOutgoingCommits()
        if (decision is SafeImmediatePushDecision.Immediate) {
            return executeImmediatePush(decision.plan)
        }
        return idePushAction.execute(dataContext, inputEvent)
    }

    private fun executeImmediatePush(pushPlan: SafeImmediatePushPlan): CommitWorkflowExecutionResult {
        val completion = CompletableFuture<Unit>()
        try {
            immediatePushExecutor.push(pushPlan)
                .whenComplete { _, throwable ->
                    if (throwable != null) {
                        completion.completeExceptionally(throwable)
                    } else {
                        completion.complete(Unit)
                    }
                }
        } catch (throwable: Throwable) {
            completion.completeExceptionally(throwable)
            throw throwable
        }
        return CommitWorkflowExecutionResult.Started(completion)
    }

    companion object {
        fun getInstance(project: Project): PushOnlyWorkflowExecutionService = project.service()
    }
}

internal interface PushOnlyIdeAction {
    fun canExecute(dataContext: DataContext): Boolean

    fun execute(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult
}

private class IntellijPushOnlyIdeAction(
    private val actionManager: ActionManager,
) : PushOnlyIdeAction {
    override fun canExecute(dataContext: DataContext): Boolean = onEdtAndWait {
        canExecuteOnEdt(dataContext)
    }

    override fun execute(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult = onEdtAndWait {
        executeOnEdt(dataContext, inputEvent)
    }

    private fun canExecuteOnEdt(dataContext: DataContext): Boolean {
        val action = actionManager.getAction(IDE_PUSH_ACTION_ID) ?: return false
        val event = action.event(dataContext, inputEvent = null)
        ActionUtil.updateAction(action, event)
        return event.presentation.isEnabled && event.presentation.isVisible
    }

    private fun executeOnEdt(
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

    private fun <T> onEdtAndWait(action: () -> T): T {
        val application = ApplicationManager.getApplication()
        if (application == null || application.isDispatchThread) {
            return action()
        }

        val result = AtomicReference<Result<T>?>()
        application.invokeAndWait {
            result.set(runCatching(action))
        }
        return result.get()?.getOrThrow()
            ?: error("EDT action did not produce a result")
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

    private companion object {
        const val IDE_PUSH_ACTION_ID: String = "Vcs.Push"
    }
}
