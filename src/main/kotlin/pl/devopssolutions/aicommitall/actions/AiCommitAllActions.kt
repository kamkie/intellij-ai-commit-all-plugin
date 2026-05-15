package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.VcsDataKeys
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowCoordinator
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import pl.devopssolutions.aicommitall.workflow.CommitWorkflowExecutionService
import java.awt.event.InputEvent
import java.util.concurrent.CompletableFuture

internal class AiCommitAllCommitAction(
    workflowStarter: AiCommitAllWorkflowStarter = ProjectAiCommitAllWorkflowStarter,
    availabilityProvider: AiCommitAllWorkflowAvailabilityProvider = ProjectAiCommitAllWorkflowAvailabilityProvider,
) : AiCommitAllWorkflowAction(AiCommitAllWorkflowMode.Commit, workflowStarter, availabilityProvider)

internal class AiCommitAllCommitAndPushAction(
    workflowStarter: AiCommitAllWorkflowStarter = ProjectAiCommitAllWorkflowStarter,
    availabilityProvider: AiCommitAllWorkflowAvailabilityProvider = ProjectAiCommitAllWorkflowAvailabilityProvider,
) : AiCommitAllWorkflowAction(AiCommitAllWorkflowMode.CommitAndPush, workflowStarter, availabilityProvider)

internal abstract class AiCommitAllWorkflowAction(
    private val mode: AiCommitAllWorkflowMode,
    private val workflowStarter: AiCommitAllWorkflowStarter,
    private val availabilityProvider: AiCommitAllWorkflowAvailabilityProvider,
) : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        val availability = if (project == null) {
            AiCommitAllWorkflowActionAvailability.Hidden
        } else {
            availabilityProvider.availability(
                project = project,
                mode = mode,
                dataContext = event.dataContext,
            )
        }

        event.presentation.isVisible = availability.visible
        event.presentation.isEnabled = availability.enabled
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        workflowStarter.start(
            project = project,
            mode = mode,
            dataContext = event.dataContext,
            inputEvent = event.inputEvent,
        )
    }

    protected fun mode(): AiCommitAllWorkflowMode = mode
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

internal interface AiCommitAllWorkflowStarter {
    fun start(
        project: Project,
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult>
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
            AiCommitAllWorkflowMode.Commit ->
                executionService.canExecuteCommit(workflowHandler)
            AiCommitAllWorkflowMode.CommitAndPush ->
                executionService.canExecuteCommitAndPush(workflowHandler)
        }

        return if (canExecute) {
            AiCommitAllWorkflowActionAvailability.Enabled
        } else {
            AiCommitAllWorkflowActionAvailability.Disabled
        }
    }
}
