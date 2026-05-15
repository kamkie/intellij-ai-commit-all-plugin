package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import pl.devopssolutions.aicommitall.ai.AiCommitMessageActionInvocationResult
import pl.devopssolutions.aicommitall.ai.AiCommitMessageActionInvocationService
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityStateService
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionResult
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionService
import java.awt.event.InputEvent
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class AiCommitAllWorkflowCoordinator(private val project: Project) {
    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> {
        val workflowHandler = VcsDataKeys.COMMIT_WORKFLOW_HANDLER.getData(dataContext)
        val workflowUi = VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
        if (workflowHandler == null || workflowUi == null) {
            return stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
        }

        when (VcsOperationReadinessService.getInstance(project).checkAndReport()) {
            VcsOperationReadinessResult.Ready -> Unit
            VcsOperationReadinessResult.Frozen ->
                return stopped(AiCommitAllWorkflowStopReason.VcsFrozen)
            VcsOperationReadinessResult.BackgroundOperationRunning ->
                return stopped(AiCommitAllWorkflowStopReason.VcsBackgroundOperationRunning)
        }

        return when (val selectionResult = CommitWorkflowSelectionService.getInstance(project)
            .prepareAllFilesSelection(workflowHandler, workflowUi)) {
            is CommitWorkflowSelectionResult.Prepared -> {
                val activityToken = AiGenerationActivityStateService.getInstance(project).start()
                val completionService = AiGenerationCompletionService.getInstance(project)
                val snapshot = completionService.captureInitialMessage(workflowUi.commitMessageUi)
                when (val invocation = AiCommitMessageActionInvocationService.getInstance(project)
                    .invokeCommitMessageGeneration(
                        workflowHandler = workflowHandler,
                        workflowUi = workflowUi,
                        parentDataContext = dataContext,
                        inputEvent = inputEvent,
                    )) {
                    is AiCommitMessageActionInvocationResult.Invoked ->
                        completionService.awaitCompletionAsync(
                            snapshot = snapshot,
                            invocation = invocation,
                            commitMessageUi = workflowUi.commitMessageUi,
                        ).handle { completionResult, throwable ->
                            activityToken.close()
                            if (throwable != null) {
                                stoppedResult(AiCommitAllWorkflowStopReason.AiCompletionFailed)
                            } else {
                                completeAfterAiGeneration(mode, workflowHandler, completionResult)
                            }
                        }
                    AiCommitMessageActionInvocationResult.MissingAction -> {
                        activityToken.close()
                        stopped(AiCommitAllWorkflowStopReason.MissingAiAction)
                    }
                    AiCommitMessageActionInvocationResult.MissingWorkflow -> {
                        activityToken.close()
                        stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
                    }
                }
            }
            CommitWorkflowSelectionResult.EmptySelection ->
                stopped(AiCommitAllWorkflowStopReason.EmptySelection)
            CommitWorkflowSelectionResult.MissingWorkflow ->
                stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
            is CommitWorkflowSelectionResult.UnsupportedVcs ->
                stopped(AiCommitAllWorkflowStopReason.UnsupportedVcs)
            is CommitWorkflowSelectionResult.UnsupportedWorkflow ->
                stopped(AiCommitAllWorkflowStopReason.UnsupportedWorkflow)
        }
    }

    private fun completeAfterAiGeneration(
        mode: AiCommitAllWorkflowMode,
        workflowHandler: com.intellij.vcs.commit.CommitWorkflowHandler,
        completionResult: AiGenerationCompletionResult,
    ): AiCommitAllWorkflowResult =
        when (completionResult) {
            is AiGenerationCompletionResult.Completed ->
                executeCompletedWorkflow(mode, workflowHandler)
            is AiGenerationCompletionResult.Timeout ->
                stoppedResult(AiCommitAllWorkflowStopReason.AiTimeout)
            AiGenerationCompletionResult.EmptyMessage ->
                stoppedResult(AiCommitAllWorkflowStopReason.EmptyMessage)
            is AiGenerationCompletionResult.UnchangedMessage ->
                stoppedResult(AiCommitAllWorkflowStopReason.UnchangedMessage)
            is AiGenerationCompletionResult.NoCompletionSignal ->
                stoppedResult(AiCommitAllWorkflowStopReason.NoCompletionSignal)
            is AiGenerationCompletionResult.UserEditedMessage ->
                stoppedResult(AiCommitAllWorkflowStopReason.UserEditedMessage)
        }

    private fun executeCompletedWorkflow(
        mode: AiCommitAllWorkflowMode,
        workflowHandler: com.intellij.vcs.commit.CommitWorkflowHandler,
    ): AiCommitAllWorkflowResult =
        when (mode) {
            AiCommitAllWorkflowMode.Commit ->
                CommitWorkflowExecutionService.getInstance(project)
                    .executeCommit(workflowHandler)
                    .toWorkflowResult(AiCommitAllWorkflowStopReason.CommitExecutionUnavailable)
            AiCommitAllWorkflowMode.CommitAndPush ->
                CommitWorkflowExecutionService.getInstance(project)
                    .executeCommitAndPush(workflowHandler)
                    .toWorkflowResult(AiCommitAllWorkflowStopReason.PushExecutionUnavailable)
        }

    private fun CommitWorkflowExecutionResult.toWorkflowResult(
        unavailableReason: AiCommitAllWorkflowStopReason,
    ): AiCommitAllWorkflowResult =
        when (this) {
            CommitWorkflowExecutionResult.Started ->
                AiCommitAllWorkflowResult.Started
            CommitWorkflowExecutionResult.MissingWorkflow ->
                stoppedResult(AiCommitAllWorkflowStopReason.MissingWorkflow)
            CommitWorkflowExecutionResult.UnsupportedExecutor ->
                stoppedResult(unavailableReason)
            CommitWorkflowExecutionResult.DisabledExecutor ->
                stoppedResult(unavailableReason)
        }

    private fun stopped(reason: AiCommitAllWorkflowStopReason): CompletableFuture<AiCommitAllWorkflowResult> =
        CompletableFuture.completedFuture(
            stoppedResult(reason),
        )

    private fun stoppedResult(reason: AiCommitAllWorkflowStopReason): AiCommitAllWorkflowResult {
        AiCommitAllWorkflowStopReporter.getInstance(project).report(reason)
        return AiCommitAllWorkflowResult.Stopped(reason)
    }

    companion object {
        fun getInstance(project: Project): AiCommitAllWorkflowCoordinator = project.service()
    }
}

internal enum class AiCommitAllWorkflowMode {
    Commit,
    CommitAndPush,
}

internal sealed interface AiCommitAllWorkflowResult {
    data object Started : AiCommitAllWorkflowResult

    data class Stopped(val reason: AiCommitAllWorkflowStopReason) : AiCommitAllWorkflowResult
}

internal enum class AiCommitAllWorkflowStopReason {
    MissingWorkflow,
    VcsFrozen,
    VcsBackgroundOperationRunning,
    EmptySelection,
    UnsupportedVcs,
    UnsupportedWorkflow,
    MissingAiAction,
    AiCompletionFailed,
    AiTimeout,
    EmptyMessage,
    UnchangedMessage,
    NoCompletionSignal,
    UserEditedMessage,
    CommitExecutionUnavailable,
    PushExecutionUnavailable,
}
