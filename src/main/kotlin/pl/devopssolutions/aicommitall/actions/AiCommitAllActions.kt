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

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.VcsDataKeys
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityPhase
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityStateService
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService
import pl.devopssolutions.aicommitall.vcs.GitOutgoingCommitsService
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowCoordinator
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import pl.devopssolutions.aicommitall.workflow.CommitWorkflowExecutionService
import java.awt.event.InputEvent
import java.util.concurrent.CompletableFuture
import javax.swing.JComponent

internal class AiCommitAllThreeSectionAction(
    private val workflowStarter: AiCommitAllWorkflowStarter = ProjectAiCommitAllWorkflowStarter,
    private val availabilityProvider: AiCommitAllWorkflowAvailabilityProvider =
        ProjectAiCommitAllWorkflowAvailabilityProvider,
    private val activityProvider: AiCommitAllWorkflowActivityProvider =
        ProjectAiCommitAllWorkflowActivityProvider,
) : DumbAwareAction(),
    CustomComponentAction {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val state = controlState(event.project, event.dataContext)
        event.presentation.putClientProperty(CONTROL_STATE_KEY, state)
        event.presentation.isVisible = state.visible
        event.presentation.isEnabled = state.enabled
    }

    override fun actionPerformed(event: AnActionEvent) {
        startSection(
            project = event.project,
            section = AiCommitAllControlSection.Commit,
            dataContext = event.dataContext,
            inputEvent = event.inputEvent,
        )
    }

    override fun createCustomComponent(
        presentation: Presentation,
        place: String,
    ): JComponent {
        AiCommitAllCommitToolbarCustomizer.removeStandardCommitAndPushAction()
        lateinit var control: AiCommitAllThreeSectionControl
        control = AiCommitAllThreeSectionControl { section, inputEvent ->
            startSection(
                project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(control)),
                section = section,
                dataContext = DataManager.getInstance().getDataContext(control),
                inputEvent = inputEvent,
            )
        }
        control.updateState(presentation.getClientProperty(CONTROL_STATE_KEY) ?: AiCommitAllControlState.Hidden)
        return control
    }

    override fun updateCustomComponent(
        component: JComponent,
        presentation: Presentation,
    ) {
        val control = component as? AiCommitAllThreeSectionControl ?: return
        control.updateState(presentation.getClientProperty(CONTROL_STATE_KEY) ?: AiCommitAllControlState.Hidden)
    }

    internal fun startSection(
        project: Project?,
        section: AiCommitAllControlSection,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult>? = project
        ?.takeIf { currentProject -> isSectionAvailableAtActionTime(currentProject, section, dataContext) }
        ?.let { currentProject ->
            workflowStarter.start(
                project = currentProject,
                mode = section.mode,
                dataContext = dataContext,
                inputEvent = inputEvent,
            )
        }

    private fun isSectionAvailableAtActionTime(
        project: Project,
        section: AiCommitAllControlSection,
        dataContext: DataContext,
    ): Boolean = activityProvider.runningSection(project) == null &&
        availabilityProvider.availability(
            project = project,
            mode = section.mode,
            dataContext = dataContext,
        ).enabled

    private fun controlState(
        project: Project?,
        dataContext: DataContext,
    ): AiCommitAllControlState {
        if (project == null) {
            return AiCommitAllControlState.Hidden
        }

        val sections = AiCommitAllControlSection.entries.associateWith { section ->
            availabilityProvider.availability(
                project = project,
                mode = section.mode,
                dataContext = dataContext,
            )
        }
        val runningSection = activityProvider.runningSection(project)
        return AiCommitAllControlState(
            sections = sections,
            runningSection = runningSection,
        )
    }

    companion object {
        private val CONTROL_STATE_KEY =
            Key.create<AiCommitAllControlState>("pl.devopssolutions.aicommitall.controlState")
    }
}

internal enum class AiCommitAllControlSection(
    val label: String,
    val mode: AiCommitAllWorkflowMode,
) {
    Ai("AI", AiCommitAllWorkflowMode.Ai),
    Commit("Commit", AiCommitAllWorkflowMode.Commit),
    Push("Push", AiCommitAllWorkflowMode.Push),
}

internal const val AI_COMMIT_ALL_THREE_SECTION_ACTION_ID: String =
    "pl.devopssolutions.aicommitall.actions.ThreeSectionControl"

internal const val AI_COMMIT_ALL_CONTROL_COMPONENT_NAME: String =
    "AI Commit All three-section control"

internal data class AiCommitAllControlState(
    val sections: Map<AiCommitAllControlSection, AiCommitAllWorkflowActionAvailability>,
    val runningSection: AiCommitAllControlSection?,
) {
    val visible: Boolean = sections.values.any { availability -> availability.visible }
    val enabled: Boolean = runningSection == null && sections.values.any { availability -> availability.enabled }

    fun isSectionEnabled(section: AiCommitAllControlSection): Boolean {
        val sectionAvailability = sections[section]
        return runningSection == null && sectionAvailability?.enabled == true
    }

    companion object {
        val Hidden = AiCommitAllControlState(
            sections = AiCommitAllControlSection.entries.associateWith {
                AiCommitAllWorkflowActionAvailability.Hidden
            },
            runningSection = null,
        )
    }
}

internal data class AiCommitAllWorkflowActionAvailability(
    val visible: Boolean,
    val enabled: Boolean,
) {
    companion object {
        val Hidden: AiCommitAllWorkflowActionAvailability =
            AiCommitAllWorkflowActionAvailability(visible = false, enabled = false)
        val Disabled: AiCommitAllWorkflowActionAvailability =
            AiCommitAllWorkflowActionAvailability(visible = true, enabled = false)
        val Enabled: AiCommitAllWorkflowActionAvailability =
            AiCommitAllWorkflowActionAvailability(visible = true, enabled = true)
    }
}

internal interface AiCommitAllWorkflowAvailabilityProvider {
    fun availability(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
    ): AiCommitAllWorkflowActionAvailability
}

internal interface AiCommitAllWorkflowActivityProvider {
    fun runningSection(project: Project): AiCommitAllControlSection?
}

internal interface AiCommitAllWorkflowStarter {
    fun start(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult>
}

internal object ProjectAiCommitAllWorkflowActivityProvider : AiCommitAllWorkflowActivityProvider {
    override fun runningSection(project: Project): AiCommitAllControlSection? {
        val runningPhase = AiGenerationActivityStateService.getInstance(project).runningPhase()
        return runningPhase?.controlSection
    }
}

internal object ProjectAiCommitAllWorkflowStarter : AiCommitAllWorkflowStarter {
    override fun start(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult> = AiCommitAllWorkflowCoordinator.getInstance(project)
        .start(
            mode = mode,
            dataContext = dataContext,
            inputEvent = inputEvent,
        )
}

internal object ProjectAiCommitAllWorkflowAvailabilityProvider : AiCommitAllWorkflowAvailabilityProvider {
    override fun availability(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
    ): AiCommitAllWorkflowActionAvailability {
        val workflowHandler = VcsDataKeys.COMMIT_WORKFLOW_HANDLER.getData(dataContext)
        val workflowUi = VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
        return if (workflowHandler == null || workflowUi == null) {
            AiCommitAllWorkflowActionAvailability.Hidden
        } else {
            val selectionService = GitChangeSelectionService.getInstance(project)
            if (selectionService.supportStatus() != GitVcsSupportStatus.Supported) {
                AiCommitAllWorkflowActionAvailability.Hidden
            } else {
                val selection = selectionService.collectSelection()
                val executionService = CommitWorkflowExecutionService.getInstance(project)
                AiCommitAllWorkflowAvailabilityPolicy.availability(
                    mode = mode,
                    hasCommittableContent = selection.hasCommittableContent,
                    canExecuteCommit = { executionService.canExecuteCommit(workflowHandler) },
                    canExecuteCommitAndPush = { executionService.canExecuteCommitAndPush(workflowHandler) },
                    hasOutgoingCommitsToPush = {
                        GitOutgoingCommitsService.getInstance(project).cachedHasOutgoingCommitsToPush()
                    },
                )
            }
        }
    }
}

internal object AiCommitAllWorkflowAvailabilityPolicy {
    fun availability(
        mode: AiCommitAllWorkflowMode,
        hasCommittableContent: Boolean,
        canExecuteCommit: () -> Boolean,
        canExecuteCommitAndPush: () -> Boolean,
        hasOutgoingCommitsToPush: () -> Boolean,
    ): AiCommitAllWorkflowActionAvailability {
        val canExecute = if (!hasCommittableContent) {
            mode == AiCommitAllWorkflowMode.Push && hasOutgoingCommitsToPush()
        } else {
            when (mode) {
                AiCommitAllWorkflowMode.Ai -> true

                AiCommitAllWorkflowMode.Commit -> canExecuteCommit()

                AiCommitAllWorkflowMode.Push ->
                    canExecuteCommitAndPush()
            }
        }

        return if (canExecute) {
            AiCommitAllWorkflowActionAvailability.Enabled
        } else {
            AiCommitAllWorkflowActionAvailability.Disabled
        }
    }
}

private val AiGenerationActivityPhase.controlSection: AiCommitAllControlSection
    get() = when (this) {
        AiGenerationActivityPhase.Ai -> AiCommitAllControlSection.Ai
        AiGenerationActivityPhase.Commit -> AiCommitAllControlSection.Commit
        AiGenerationActivityPhase.Push -> AiCommitAllControlSection.Push
    }
