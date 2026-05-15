package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.vcs.VcsBundle
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AiCommitAllWorkflowStopReporterTest {
    @Test
    fun `reports empty selection with standard VCS message`() {
        val notifier = CapturingWorkflowStopNotifier()

        WorkflowStopReporter(notifier).report(AiCommitAllWorkflowStopReason.EmptySelection)

        assertEquals(
            listOf(
                WarningNotification(
                    title = VcsBundle.message("commit.dialog.no.changes.detected.title"),
                    content = VcsBundle.message("error.no.changes.to.commit"),
                ),
            ),
            notifier.warnings,
        )
    }

    @Test
    fun `reports AI timeout with plugin owned timeout message`() {
        val notifier = CapturingWorkflowStopNotifier()

        WorkflowStopReporter(notifier).report(AiCommitAllWorkflowStopReason.AiTimeout)

        assertEquals(
            listOf(
                WarningNotification(
                    title = "AI Commit All",
                    content = WorkflowStopReporter.AI_TIMEOUT_NOTIFICATION_CONTENT,
                ),
            ),
            notifier.warnings,
        )
    }

    @Test
    fun `reports empty generated commit message with standard VCS message`() {
        val notifier = CapturingWorkflowStopNotifier()

        WorkflowStopReporter(notifier).report(AiCommitAllWorkflowStopReason.EmptyMessage)

        assertEquals(
            listOf(
                WarningNotification(
                    title = VcsBundle.message("error.title.check.in.with.empty.comment"),
                    content = VcsBundle.message("error.no.commit.message"),
                ),
            ),
            notifier.warnings,
        )
    }

    @Test
    fun `does not report stop reasons owned by platform workflow paths`() {
        val notifier = CapturingWorkflowStopNotifier()

        WorkflowStopReporter(notifier).report(AiCommitAllWorkflowStopReason.PushExecutionUnavailable)

        assertEquals(emptyList(), notifier.warnings)
    }

    private class CapturingWorkflowStopNotifier : WorkflowStopNotifier {
        val warnings = mutableListOf<WarningNotification>()

        override fun warning(
            title: String,
            content: String,
        ) {
            warnings += WarningNotification(title = title, content = content)
        }
    }

    private data class WarningNotification(
        val title: String,
        val content: String,
    )
}
