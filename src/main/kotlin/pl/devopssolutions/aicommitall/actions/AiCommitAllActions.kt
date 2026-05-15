package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

internal class AiCommitAllCommitAction : AiCommitAllWorkflowAction(AiCommitAllActionMode.Commit)

internal class AiCommitAllCommitAndPushAction : AiCommitAllWorkflowAction(AiCommitAllActionMode.CommitAndPush)

internal abstract class AiCommitAllWorkflowAction(
    private val mode: AiCommitAllActionMode,
) : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = false
    }

    override fun actionPerformed(event: AnActionEvent) = Unit

    protected fun mode(): AiCommitAllActionMode = mode
}

internal enum class AiCommitAllActionMode {
    Commit,
    CommitAndPush,
}
