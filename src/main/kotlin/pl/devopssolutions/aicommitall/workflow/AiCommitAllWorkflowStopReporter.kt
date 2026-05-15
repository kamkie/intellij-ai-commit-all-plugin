package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsBundle
import pl.devopssolutions.aicommitall.notifications.AiCommitAllNotificationService
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings

@Service(Service.Level.PROJECT)
internal class AiCommitAllWorkflowStopReporter(private val project: Project) {
    private val reporter = WorkflowStopReporter(IntellijWorkflowStopNotifier(project))

    fun report(reason: AiCommitAllWorkflowStopReason) {
        reporter.report(reason)
    }

    companion object {
        fun getInstance(project: Project): AiCommitAllWorkflowStopReporter = project.service()
    }
}

internal class WorkflowStopReporter(
    private val notifier: WorkflowStopNotifier,
) {
    fun report(reason: AiCommitAllWorkflowStopReason) {
        when (reason) {
            AiCommitAllWorkflowStopReason.EmptySelection ->
                notifier.warning(
                    title = VcsBundle.message("commit.dialog.no.changes.detected.title"),
                    content = VcsBundle.message("error.no.changes.to.commit"),
                )
            AiCommitAllWorkflowStopReason.AiTimeout ->
                notifier.warning(
                    title = AiCommitAllSettings.DISPLAY_NAME,
                    content = AI_TIMEOUT_NOTIFICATION_CONTENT,
                )
            AiCommitAllWorkflowStopReason.EmptyMessage ->
                notifier.warning(
                    title = VcsBundle.message("error.title.check.in.with.empty.comment"),
                    content = VcsBundle.message("error.no.commit.message"),
                )
            else -> Unit
        }
    }

    companion object {
        const val AI_TIMEOUT_NOTIFICATION_CONTENT: String =
            "AI Assistant did not finish generating a commit message before the configured timeout."
    }
}

internal fun interface WorkflowStopNotifier {
    fun warning(
        title: String,
        content: String,
    )
}

private class IntellijWorkflowStopNotifier(private val project: Project) : WorkflowStopNotifier {
    override fun warning(
        title: String,
        content: String,
    ) {
        AiCommitAllNotificationService.getInstance(project)
            .notifyWarning(title = title, content = content)
    }
}
