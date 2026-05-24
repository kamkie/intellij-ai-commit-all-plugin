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
package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings

internal class AiCommitAllCommitShortcutAction :
    AiCommitAllShortcutAction(
        section = AiCommitAllControlSection.Commit,
        sourceActionId = IDE_COMMIT_ACTION_ID,
    )

internal class AiCommitAllPushShortcutAction :
    AiCommitAllShortcutAction(
        section = AiCommitAllControlSection.Push,
        sourceActionId = IDE_PUSH_ACTION_ID,
    )

internal abstract class AiCommitAllShortcutAction(
    private val section: AiCommitAllControlSection,
    internal val sourceActionId: String,
    private val workflowStarter: AiCommitAllWorkflowStarter = ProjectAiCommitAllWorkflowStarter,
    private val availabilityProvider: AiCommitAllWorkflowAvailabilityProvider =
        ProjectAiCommitAllWorkflowAvailabilityProvider,
    private val settingsProvider: AiCommitAllShortcutSettingsProvider =
        ProjectAiCommitAllShortcutSettingsProvider,
    private val standardActionDelegate: StandardVcsShortcutActionDelegate =
        IntellijStandardVcsShortcutActionDelegate,
    private val activityProvider: AiCommitAllWorkflowActivityProvider =
        ProjectAiCommitAllWorkflowActivityProvider,
) : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isVisible = true
        event.presentation.isEnabled = isTakeoverAvailable(event.dataContext) &&
            !isWorkflowRunning(event.dataContext)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        when {
            project == null || !settingsProvider.useVcsShortcutsForAiCommitAll() ->
                standardActionDelegate.perform(sourceActionId, event)

            isWorkflowRunning(project) -> Unit

            isWorkflowAvailable(project, event.dataContext) ->
                workflowStarter.start(
                    project = project,
                    mode = section.mode,
                    dataContext = event.dataContext,
                    inputEvent = event.inputEvent,
                )

            else ->
                standardActionDelegate.perform(sourceActionId, event)
        }
    }

    internal fun isTakeoverAvailable(dataContext: DataContext): Boolean {
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        return settingsProvider.useVcsShortcutsForAiCommitAll() &&
            project?.let { isWorkflowAvailable(it, dataContext) } == true
    }

    private fun isWorkflowAvailable(
        project: Project,
        dataContext: DataContext,
    ): Boolean = availabilityProvider.availability(
        project = project,
        mode = section.mode,
        dataContext = dataContext,
    ).enabled

    private fun isWorkflowRunning(dataContext: DataContext): Boolean {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false
        return isWorkflowRunning(project)
    }

    private fun isWorkflowRunning(project: Project): Boolean {
        val runningSection = activityProvider.runningSection(project)
        return runningSection != null
    }
}

internal class AiCommitAllShortcutActionPromoter(
    private val settingsProvider: AiCommitAllShortcutSettingsProvider =
        ProjectAiCommitAllShortcutSettingsProvider,
    private val actionIdProvider: AiCommitAllActionIdProvider =
        IntellijAiCommitAllActionIdProvider,
) : ActionPromoter {
    override fun promote(
        actions: List<AnAction>,
        context: DataContext,
    ): List<AnAction> = takeoverActions(actions, context)

    override fun suppress(
        actions: List<AnAction>,
        context: DataContext,
    ): List<AnAction> {
        val sourceActionIds = takeoverActions(actions, context)
            .map { action -> action.sourceActionId }
            .toSet()
        if (sourceActionIds.isEmpty()) {
            return emptyList()
        }

        return actions.filter { action -> actionIdProvider.id(action) in sourceActionIds }
    }

    private fun takeoverActions(
        actions: List<AnAction>,
        context: DataContext,
    ): List<AiCommitAllShortcutAction> {
        if (!settingsProvider.useVcsShortcutsForAiCommitAll()) {
            return emptyList()
        }

        return actions
            .filterIsInstance<AiCommitAllShortcutAction>()
            .filter { action -> action.isTakeoverAvailable(context) }
    }
}

internal fun interface AiCommitAllShortcutSettingsProvider {
    fun useVcsShortcutsForAiCommitAll(): Boolean
}

internal object ProjectAiCommitAllShortcutSettingsProvider : AiCommitAllShortcutSettingsProvider {
    override fun useVcsShortcutsForAiCommitAll(): Boolean {
        val settings = AiCommitAllSettings.getInstance()
        return settings.useVcsShortcutsForAiCommitAll()
    }
}

internal fun interface StandardVcsShortcutActionDelegate {
    fun perform(sourceActionId: String, event: AnActionEvent)
}

internal object IntellijStandardVcsShortcutActionDelegate : StandardVcsShortcutActionDelegate {
    override fun perform(sourceActionId: String, event: AnActionEvent) {
        val sourceAction = ActionManager.getInstance().getAction(sourceActionId) ?: return
        ActionUtil.performAction(sourceAction, event)
    }
}

internal fun interface AiCommitAllActionIdProvider {
    fun id(action: AnAction): String?
}

internal object IntellijAiCommitAllActionIdProvider : AiCommitAllActionIdProvider {
    override fun id(action: AnAction): String? = ActionManager.getInstance().getId(action)
}

internal const val IDE_COMMIT_ACTION_ID: String = "CheckinProject"
internal const val IDE_COMMIT_AND_PUSH_ACTION_ID: String = "Git.Commit.And.Push.Executor"
internal const val IDE_PUSH_ACTION_ID: String = "Vcs.Push"
internal const val AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID: String =
    "pl.devopssolutions.aicommitall.actions.CommitShortcut"
internal const val AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID: String =
    "pl.devopssolutions.aicommitall.actions.PushShortcut"
