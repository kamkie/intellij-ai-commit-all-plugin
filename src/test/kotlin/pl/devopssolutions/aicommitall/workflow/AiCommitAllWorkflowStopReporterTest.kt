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

    @Test
    fun `does not report AI assistant and platform owned stop reasons`() {
        val reasons = listOf(
            AiCommitAllWorkflowStopReason.MissingWorkflow,
            AiCommitAllWorkflowStopReason.VcsFrozen,
            AiCommitAllWorkflowStopReason.VcsBackgroundOperationRunning,
            AiCommitAllWorkflowStopReason.UnsupportedVcs,
            AiCommitAllWorkflowStopReason.UnsupportedWorkflow,
            AiCommitAllWorkflowStopReason.MissingAiAction,
            AiCommitAllWorkflowStopReason.AiCompletionFailed,
            AiCommitAllWorkflowStopReason.UserEditedMessage,
            AiCommitAllWorkflowStopReason.CommitExecutionUnavailable,
            AiCommitAllWorkflowStopReason.PushExecutionUnavailable,
        )

        reasons.forEach { reason ->
            val notifier = CapturingWorkflowStopNotifier()

            WorkflowStopReporter(notifier).report(reason)

            assertEquals(emptyList(), notifier.warnings, "Unexpected notification for $reason.")
        }
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
