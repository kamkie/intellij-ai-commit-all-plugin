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
) : DumbAwareAction(), CustomComponentAction {
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
    ): CompletableFuture<AiCommitAllWorkflowResult>? =
        project?.let {
            workflowStarter.start(
                project = it,
                mode = section.mode,
                dataContext = dataContext,
                inputEvent = inputEvent,
            )
        }

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

internal data class AiCommitAllControlState(
    val sections: Map<AiCommitAllControlSection, AiCommitAllWorkflowActionAvailability>,
    val runningSection: AiCommitAllControlSection?,
) {
    val visible: Boolean = sections.values.any { availability -> availability.visible }
    val enabled: Boolean = runningSection == null && sections.values.any { availability -> availability.enabled }

    fun isSectionEnabled(section: AiCommitAllControlSection): Boolean =
        runningSection == null && sections[section]?.enabled == true

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

private object ProjectAiCommitAllWorkflowActivityProvider : AiCommitAllWorkflowActivityProvider {
    override fun runningSection(project: Project): AiCommitAllControlSection? =
        AiGenerationActivityStateService.getInstance(project).runningPhase()?.controlSection
}

private object ProjectAiCommitAllWorkflowStarter : AiCommitAllWorkflowStarter {
    override fun start(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult> =
        AiCommitAllWorkflowCoordinator.getInstance(project)
            .start(
                mode = mode,
                dataContext = dataContext,
                inputEvent = inputEvent,
            )
}

private object ProjectAiCommitAllWorkflowAvailabilityProvider : AiCommitAllWorkflowAvailabilityProvider {
    override fun availability(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
    ): AiCommitAllWorkflowActionAvailability {
        val workflowHandler = VcsDataKeys.COMMIT_WORKFLOW_HANDLER.getData(dataContext)
        val workflowUi = VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
        if (workflowHandler == null || workflowUi == null) {
            return AiCommitAllWorkflowActionAvailability.Hidden
        }

        val selectionService = GitChangeSelectionService.getInstance(project)
        if (selectionService.supportStatus() != GitVcsSupportStatus.Supported) {
            return AiCommitAllWorkflowActionAvailability.Hidden
        }
        if (!selectionService.collectSelection().hasCommittableContent) {
            return AiCommitAllWorkflowActionAvailability.Disabled
        }

        val executionService = CommitWorkflowExecutionService.getInstance(project)
        val canExecute = when (mode) {
            AiCommitAllWorkflowMode.Ai -> true
            AiCommitAllWorkflowMode.Commit ->
                executionService.canExecuteCommit(workflowHandler)
            AiCommitAllWorkflowMode.Push ->
                executionService.canExecuteCommitAndPush(workflowHandler)
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
