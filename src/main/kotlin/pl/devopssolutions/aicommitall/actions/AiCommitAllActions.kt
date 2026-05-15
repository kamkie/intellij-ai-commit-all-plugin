package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.DumbAwareAction
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowCoordinator
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import java.awt.event.InputEvent
import java.util.concurrent.CompletableFuture

internal class AiCommitAllCommitAction(
    workflowStarter: AiCommitAllWorkflowStarter = ProjectAiCommitAllWorkflowStarter,
) : AiCommitAllWorkflowAction(AiCommitAllWorkflowMode.Commit, workflowStarter)

internal class AiCommitAllCommitAndPushAction(
    workflowStarter: AiCommitAllWorkflowStarter = ProjectAiCommitAllWorkflowStarter,
) : AiCommitAllWorkflowAction(AiCommitAllWorkflowMode.CommitAndPush, workflowStarter)

internal abstract class AiCommitAllWorkflowAction(
    private val mode: AiCommitAllWorkflowMode,
    private val workflowStarter: AiCommitAllWorkflowStarter,
) : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = false
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
