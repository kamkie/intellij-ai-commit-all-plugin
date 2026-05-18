package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.ai.*
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushService
import java.awt.event.InputEvent
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class AiCommitAllWorkflowCoordinator(private val project: Project) {
    private val runner = AiCommitAllWorkflowRunner(ProjectAiCommitAllWorkflowDependencies(project))

    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> =
        runner.start(
            mode = mode,
            dataContext = dataContext,
            inputEvent = inputEvent,
        )

    companion object {
        fun getInstance(project: Project): AiCommitAllWorkflowCoordinator = project.service()
    }
}

internal class AiCommitAllWorkflowRunner(
    private val dependencies: AiCommitAllWorkflowDependencies,
) {
    private val activeWorkflowLock = Any()
    private var activeWorkflow: CompletableFuture<AiCommitAllWorkflowResult>? = null

    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> =
        synchronized(activeWorkflowLock) {
            activeWorkflow?.takeIf { future -> !future.isDone }
                ?: startNewWorkflow(mode, dataContext, inputEvent).also { future ->
                    activeWorkflow = future
                    future.whenComplete { _, _ -> clearActiveWorkflow(future) }
                }
        }

    private fun startNewWorkflow(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> {
        val workflowHandler = VcsDataKeys.COMMIT_WORKFLOW_HANDLER.getData(dataContext)
        val workflowUi = VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
        if (workflowHandler == null || workflowUi == null) {
            return stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
        }

        when (dependencies.checkReadiness()) {
            VcsOperationReadinessResult.Ready -> Unit
            VcsOperationReadinessResult.Frozen ->
                return stopped(AiCommitAllWorkflowStopReason.VcsFrozen)
            VcsOperationReadinessResult.BackgroundOperationRunning ->
                return stopped(AiCommitAllWorkflowStopReason.VcsBackgroundOperationRunning)
        }

        return when (val selectionResult = dependencies.prepareAllFilesSelection(workflowHandler, workflowUi)) {
            is CommitWorkflowSelectionResult.Prepared -> {
                when (
                    val generationResult = dependencies.runAiGeneration(
                        phase = mode.activityPhase,
                        workflowHandler = workflowHandler,
                        workflowUi = workflowUi,
                        parentDataContext = dataContext,
                        inputEvent = inputEvent,
                    )
                ) {
                    is AiCommitAllAiGenerationResult.AwaitingCompletion ->
                        generationResult.completion.handle { completionResult, throwable ->
                            if (throwable != null) {
                                stoppedResult(AiCommitAllWorkflowStopReason.AiCompletionFailed)
                            } else {
                                completeAfterAiGeneration(
                                    mode = mode,
                                    workflowHandler = workflowHandler,
                                    selection = selectionResult.selection,
                                    completionResult = completionResult,
                                )
                            }
                        }

                    is AiCommitAllAiGenerationResult.Stopped ->
                        stopped(generationResult.reason)
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

    private fun clearActiveWorkflow(future: CompletableFuture<AiCommitAllWorkflowResult>) {
        synchronized(activeWorkflowLock) {
            if (activeWorkflow === future) {
                activeWorkflow = null
            }
        }
    }

    private fun completeAfterAiGeneration(
        mode: AiCommitAllWorkflowMode,
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
        completionResult: AiGenerationCompletionResult,
    ): AiCommitAllWorkflowResult =
        when (completionResult) {
            is AiGenerationCompletionResult.Completed ->
                executeCompletedWorkflow(mode, workflowHandler, selection)
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
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
    ): AiCommitAllWorkflowResult =
        when (mode) {
            AiCommitAllWorkflowMode.Ai ->
                AiCommitAllWorkflowResult.Started
            AiCommitAllWorkflowMode.Commit ->
                dependencies.executeCommit(workflowHandler)
                    .toWorkflowResult(AiCommitAllWorkflowStopReason.CommitExecutionUnavailable)
            AiCommitAllWorkflowMode.Push ->
                dependencies.executeCommitAndPush(workflowHandler, selection)
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
        dependencies.reportStop(reason)
        return AiCommitAllWorkflowResult.Stopped(reason)
    }
}

internal interface AiCommitAllWorkflowDependencies {
    fun checkReadiness(): VcsOperationReadinessResult

    fun prepareAllFilesSelection(
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
    ): CommitWorkflowSelectionResult

    fun runAiGeneration(
        phase: AiGenerationActivityPhase,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
        inputEvent: InputEvent?,
    ): AiCommitAllAiGenerationResult

    fun executeCommit(workflowHandler: CommitWorkflowHandler): CommitWorkflowExecutionResult

    fun executeCommitAndPush(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
    ): CommitWorkflowExecutionResult

    fun reportStop(reason: AiCommitAllWorkflowStopReason)
}

internal sealed interface AiCommitAllAiGenerationResult {
    data class AwaitingCompletion(
        val completion: CompletableFuture<AiGenerationCompletionResult>,
    ) : AiCommitAllAiGenerationResult

    data class Stopped(
        val reason: AiCommitAllWorkflowStopReason,
    ) : AiCommitAllAiGenerationResult
}

private class ProjectAiCommitAllWorkflowDependencies(private val project: Project) : AiCommitAllWorkflowDependencies {
    override fun checkReadiness(): VcsOperationReadinessResult =
        VcsOperationReadinessService.getInstance(project).checkAndReport()

    override fun prepareAllFilesSelection(
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
    ): CommitWorkflowSelectionResult =
        CommitWorkflowSelectionService.getInstance(project)
            .prepareAllFilesSelection(workflowHandler, workflowUi)

    override fun runAiGeneration(
        phase: AiGenerationActivityPhase,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
        inputEvent: InputEvent?,
    ): AiCommitAllAiGenerationResult {
        val activityToken = AiGenerationActivityStateService.getInstance(project).start(phase)
        val completionService = AiGenerationCompletionService.getInstance(project)
        val snapshot = AiCommitMessagePreparation.prepareInitialSnapshot(
            commitMessageUi = workflowUi.commitMessageUi,
            clearBeforeGeneration = AiCommitAllSettings.getInstance()
                .clearCommitMessageBeforeGeneration(),
            parentDataContext = parentDataContext,
        )
        return when (val invocation = AiCommitMessageActionInvocationService.getInstance(project)
            .invokeCommitMessageGeneration(
                workflowHandler = workflowHandler,
                workflowUi = workflowUi,
                parentDataContext = parentDataContext,
                inputEvent = inputEvent,
            )) {
            is AiCommitMessageActionInvocationResult.Invoked ->
                AiCommitAllAiGenerationResult.AwaitingCompletion(
                    completionService.awaitCompletionAsync(
                        snapshot = snapshot,
                        invocation = invocation,
                        commitMessageUi = workflowUi.commitMessageUi,
                    ).whenComplete { _, _ -> activityToken.close() },
                )

            AiCommitMessageActionInvocationResult.MissingAction -> {
                activityToken.close()
                AiCommitAllAiGenerationResult.Stopped(AiCommitAllWorkflowStopReason.MissingAiAction)
            }

            AiCommitMessageActionInvocationResult.MissingWorkflow -> {
                activityToken.close()
                AiCommitAllAiGenerationResult.Stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
            }
        }
    }

    override fun executeCommit(workflowHandler: CommitWorkflowHandler): CommitWorkflowExecutionResult =
        CommitWorkflowExecutionService.getInstance(project)
            .executeCommit(workflowHandler)

    override fun executeCommitAndPush(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
    ): CommitWorkflowExecutionResult =
        CommitWorkflowExecutionService.getInstance(project)
            .executeCommitAndPush(
                workflowHandler = workflowHandler,
                selection = selection,
                safeImmediatePushSupport = SafeImmediatePushService.getInstance(project),
            )

    override fun reportStop(reason: AiCommitAllWorkflowStopReason) {
        AiCommitAllWorkflowStopReporter.getInstance(project).report(reason)
    }
}

internal enum class AiCommitAllWorkflowMode {
    Ai,
    Commit,
    Push,
}

private val AiCommitAllWorkflowMode.activityPhase: AiGenerationActivityPhase
    get() = when (this) {
        AiCommitAllWorkflowMode.Ai -> AiGenerationActivityPhase.Ai
        AiCommitAllWorkflowMode.Commit -> AiGenerationActivityPhase.Commit
        AiCommitAllWorkflowMode.Push -> AiGenerationActivityPhase.Push
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
