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

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.ai.AiCommitMessageActionInvocationResult
import pl.devopssolutions.aicommitall.ai.AiCommitMessageActionInvocationService
import pl.devopssolutions.aicommitall.ai.AiCommitMessagePreparation
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityPhase
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityStateService
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityToken
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionResult
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionService
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.GitOutgoingCommitsService
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushService
import java.awt.event.InputEvent
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class AiCommitAllWorkflowCoordinator @JvmOverloads constructor(
    project: Project,
    private val starter: AiCommitAllWorkflowStarter = AiCommitAllWorkflowRunnerStarter(
        AiCommitAllWorkflowRunner(ProjectAiCommitAllWorkflowDependencies(project)),
    ),
) {
    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> = starter.start(
        mode = mode,
        dataContext = dataContext,
        inputEvent = inputEvent,
    )

    companion object {
        fun getInstance(project: Project): AiCommitAllWorkflowCoordinator = project.service()
    }
}

internal interface AiCommitAllWorkflowStarter {
    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult>
}

private class AiCommitAllWorkflowRunnerStarter(
    private val runner: AiCommitAllWorkflowRunner,
) : AiCommitAllWorkflowStarter {
    override fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CompletableFuture<AiCommitAllWorkflowResult> = runner.start(
        mode = mode,
        dataContext = dataContext,
        inputEvent = inputEvent,
    )
}

internal class AiCommitAllWorkflowRunner(
    private val dependencies: AiCommitAllWorkflowDependencies,
    private val scheduler: AiCommitAllWorkflowScheduler = IntellijAiCommitAllWorkflowScheduler,
) {
    private val activeWorkflowLock = Any()
    private var activeWorkflow: CompletableFuture<AiCommitAllWorkflowResult>? = null

    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> = synchronized(activeWorkflowLock) {
        val runningWorkflow = activeWorkflow?.takeIf { future -> !future.isDone }
        if (runningWorkflow != null) {
            logger.info(
                "AI Commit All diagnostic: workflow start requested while workflow is active, " +
                    "requestedMode=$mode",
            )
            runningWorkflow
        } else {
            logger.info("AI Commit All diagnostic: workflow start requested, mode=$mode")
            startNewWorkflow(mode, dataContext, inputEvent).also { future ->
                activeWorkflow = future
                future.whenComplete { _, _ -> clearActiveWorkflow(future) }
            }
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
            logger.info(
                "AI Commit All diagnostic: workflow stopped before preparation, " +
                    "mode=$mode, reason=${AiCommitAllWorkflowStopReason.MissingWorkflow}, " +
                    "hasWorkflowHandler=${workflowHandler != null}, hasWorkflowUi=${workflowUi != null}",
            )
            return stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
        }
        logger.info(
            "AI Commit All diagnostic: workflow context resolved, " +
                "mode=$mode, workflowHandler=${workflowHandler.javaClass.name}, " +
                "workflowUi=${workflowUi.javaClass.name}",
        )

        val activity = dependencies.startActivity(AiGenerationActivityPhase.Ai)
        return scheduler.supplyBackground {
            prepareWorkflow(mode, workflowHandler, workflowUi)
        }.thenCompose { preparation ->
            logger.info(
                "AI Commit All diagnostic: workflow preparation completed, " +
                    "mode=$mode, result=${preparation.diagnosticName()}",
            )
            when (preparation) {
                is AiCommitAllWorkflowPreparationResult.Prepared ->
                    startAiGeneration(
                        mode = mode,
                        workflowHandler = workflowHandler,
                        workflowUi = workflowUi,
                        selection = preparation.selection,
                        dataContext = dataContext,
                        inputEvent = inputEvent,
                        activity = activity,
                    )

                AiCommitAllWorkflowPreparationResult.PushOnly ->
                    executePushOnly(
                        dataContext = dataContext,
                        inputEvent = inputEvent,
                        activity = activity,
                    )

                is AiCommitAllWorkflowPreparationResult.Stopped ->
                    stopped(preparation.reason)
            }
        }.whenComplete { _, _ ->
            activity.close()
        }
    }

    private fun prepareWorkflow(
        mode: AiCommitAllWorkflowMode,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
    ): AiCommitAllWorkflowPreparationResult = dependencies.checkReadiness()
        .stopReason()
        ?.let(AiCommitAllWorkflowPreparationResult::Stopped)
        ?: selectionPreparation(
            mode = mode,
            selectionResult = prepareAllFilesSelectionWithSettling(workflowHandler, workflowUi),
            hasOutgoingCommitsToPush = dependencies::hasOutgoingCommitsToPush,
        )

    private fun prepareAllFilesSelectionWithSettling(
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
    ): CommitWorkflowSelectionResult {
        var selectionResult: CommitWorkflowSelectionResult = CommitWorkflowSelectionResult.EmptySelection
        repeat(WORKFLOW_SELECTION_SETTLING_ATTEMPTS) { attemptIndex ->
            selectionResult = dependencies.prepareAllFilesSelection(workflowHandler, workflowUi)
            if (selectionResult != CommitWorkflowSelectionResult.EmptySelection || dependencies.isDisposed()) {
                return selectionResult
            }

            if (attemptIndex < WORKFLOW_SELECTION_SETTLING_ATTEMPTS - 1 && !dependencies.isDisposed()) {
                WORKFLOW_SELECTION_SETTLING_SLEEPER.sleep(WORKFLOW_SELECTION_SETTLING_RETRY_INTERVAL)
            }
        }
        return selectionResult
    }

    private fun executePushOnly(
        dataContext: DataContext,
        inputEvent: InputEvent?,
        activity: AiCommitAllWorkflowActivity,
    ): CompletableFuture<AiCommitAllWorkflowResult> {
        activity.moveTo(AiGenerationActivityPhase.Push)
        return scheduler.supplyBackground {
            dependencies.executePushOnly(dataContext, inputEvent)
        }.thenCompose { executionResult ->
            executionResult.toWorkflowResult(AiCommitAllWorkflowStopReason.PushExecutionUnavailable)
        }
    }

    private fun startAiGeneration(
        mode: AiCommitAllWorkflowMode,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        selection: GitChangeSelection,
        dataContext: DataContext,
        inputEvent: InputEvent?,
        activity: AiCommitAllWorkflowActivity,
    ): CompletableFuture<AiCommitAllWorkflowResult> {
        logger.info(
            "AI Commit All diagnostic: AI generation phase scheduled, " +
                "mode=$mode, selection=${selection.diagnosticSummary()}",
        )
        return scheduler.supplyEdt {
            dependencies.runAiGeneration(
                phase = AiGenerationActivityPhase.Ai,
                workflowHandler = workflowHandler,
                workflowUi = workflowUi,
                parentDataContext = dataContext,
                inputEvent = inputEvent,
            )
        }.thenCompose { generationResult ->
            logger.info(
                "AI Commit All diagnostic: AI generation invocation completed, " +
                    "mode=$mode, result=${generationResult.diagnosticName()}",
            )
            when (generationResult) {
                is AiCommitAllAiGenerationResult.AwaitingCompletion ->
                    generationResult.completion.handle { completionResult, throwable ->
                        if (throwable != null) {
                            logger.info(
                                "AI Commit All diagnostic: AI completion future failed, " +
                                    "mode=$mode, exception=${throwable.javaClass.name}, " +
                                    "cause=${throwable.cause?.javaClass?.name ?: "<none>"}",
                            )
                            stopped(AiCommitAllWorkflowStopReason.AiCompletionFailed)
                        } else {
                            logger.info(
                                "AI Commit All diagnostic: AI completion result, " +
                                    "mode=$mode, result=${completionResult.diagnosticName()}",
                            )
                            completeAfterAiGeneration(
                                mode = mode,
                                workflowHandler = workflowHandler,
                                selection = selection,
                                completionResult = completionResult,
                                activity = activity,
                            )
                        }
                    }.thenCompose { result -> result }

                is AiCommitAllAiGenerationResult.Stopped ->
                    stopped(generationResult.reason)
            }
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
        activity: AiCommitAllWorkflowActivity,
    ): CompletableFuture<AiCommitAllWorkflowResult> = when (completionResult) {
        is AiGenerationCompletionResult.Completed ->
            executeCompletedWorkflow(mode, workflowHandler, selection, activity)

        is AiGenerationCompletionResult.Timeout ->
            stopped(AiCommitAllWorkflowStopReason.AiTimeout)

        AiGenerationCompletionResult.EmptyMessage ->
            stopped(AiCommitAllWorkflowStopReason.EmptyMessage)

        is AiGenerationCompletionResult.UnchangedMessage ->
            stopped(AiCommitAllWorkflowStopReason.UnchangedMessage)

        is AiGenerationCompletionResult.NoCompletionSignal ->
            stopped(AiCommitAllWorkflowStopReason.NoCompletionSignal)

        is AiGenerationCompletionResult.UserEditedMessage ->
            stopped(AiCommitAllWorkflowStopReason.UserEditedMessage)
    }

    private fun executeCompletedWorkflow(
        mode: AiCommitAllWorkflowMode,
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
        activity: AiCommitAllWorkflowActivity,
    ): CompletableFuture<AiCommitAllWorkflowResult> = when (mode) {
        AiCommitAllWorkflowMode.Ai ->
            CompletableFuture.completedFuture(AiCommitAllWorkflowResult.Started)

        AiCommitAllWorkflowMode.Commit -> {
            logger.info("AI Commit All diagnostic: executing completed workflow, mode=$mode")
            activity.moveTo(AiGenerationActivityPhase.Commit)
            dependencies.executeCommit(workflowHandler)
                .toWorkflowResult(AiCommitAllWorkflowStopReason.CommitExecutionUnavailable)
        }

        AiCommitAllWorkflowMode.Push -> {
            logger.info("AI Commit All diagnostic: executing completed workflow, mode=$mode")
            activity.moveTo(AiGenerationActivityPhase.Commit)
            dependencies.executeCommitAndPush(
                workflowHandler = workflowHandler,
                selection = selection,
                onPushStarted = { activity.moveTo(AiGenerationActivityPhase.Push) },
            )
                .toWorkflowResult(AiCommitAllWorkflowStopReason.PushExecutionUnavailable)
        }
    }

    private fun CommitWorkflowExecutionResult.toWorkflowResult(
        unavailableReason: AiCommitAllWorkflowStopReason,
    ): CompletableFuture<AiCommitAllWorkflowResult> = when (this) {
        is CommitWorkflowExecutionResult.Started ->
            completion.thenApply { AiCommitAllWorkflowResult.Started }

        CommitWorkflowExecutionResult.MissingWorkflow ->
            stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)

        CommitWorkflowExecutionResult.UnsupportedExecutor ->
            stopped(unavailableReason)

        CommitWorkflowExecutionResult.DisabledExecutor ->
            stopped(unavailableReason)
    }

    private fun stopped(reason: AiCommitAllWorkflowStopReason): CompletableFuture<AiCommitAllWorkflowResult> {
        val result = stoppedResult(reason)
        return CompletableFuture.completedFuture(result)
    }

    private fun stoppedResult(reason: AiCommitAllWorkflowStopReason): AiCommitAllWorkflowResult {
        logger.info("AI Commit All diagnostic: workflow stopped, reason=$reason")
        dependencies.reportStop(reason)
        return AiCommitAllWorkflowResult.Stopped(reason)
    }

    private companion object {
        val logger: Logger = Logger.getInstance(AiCommitAllWorkflowRunner::class.java)
        private const val WORKFLOW_SELECTION_SETTLING_ATTEMPTS = 3
        private val WORKFLOW_SELECTION_SETTLING_RETRY_INTERVAL: Duration = Duration.ofMillis(50)
        private val WORKFLOW_SELECTION_SETTLING_SLEEPER: WorkflowSelectionSettlingSleeper =
            ThreadWorkflowSelectionSettlingSleeper
    }
}

private fun interface WorkflowSelectionSettlingSleeper {
    fun sleep(duration: Duration)
}

private object ThreadWorkflowSelectionSettlingSleeper : WorkflowSelectionSettlingSleeper {
    override fun sleep(duration: Duration) {
        if (duration.isZero) {
            return
        }

        try {
            Thread.sleep(duration.toMillis())
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

private sealed interface AiCommitAllWorkflowPreparationResult {
    data class Prepared(
        val selection: GitChangeSelection,
    ) : AiCommitAllWorkflowPreparationResult

    data object PushOnly : AiCommitAllWorkflowPreparationResult

    data class Stopped(
        val reason: AiCommitAllWorkflowStopReason,
    ) : AiCommitAllWorkflowPreparationResult
}

private fun AiCommitAllWorkflowPreparationResult.diagnosticName(): String = when (this) {
    is AiCommitAllWorkflowPreparationResult.Prepared ->
        "Prepared(${selection.diagnosticSummary()})"

    AiCommitAllWorkflowPreparationResult.PushOnly -> "PushOnly"

    is AiCommitAllWorkflowPreparationResult.Stopped ->
        "Stopped(reason=$reason)"
}

private fun VcsOperationReadinessResult.stopReason(): AiCommitAllWorkflowStopReason? = when (this) {
    VcsOperationReadinessResult.Ready -> null

    VcsOperationReadinessResult.Frozen -> AiCommitAllWorkflowStopReason.VcsFrozen

    VcsOperationReadinessResult.BackgroundOperationRunning ->
        AiCommitAllWorkflowStopReason.VcsBackgroundOperationRunning
}

private fun selectionPreparation(
    mode: AiCommitAllWorkflowMode,
    selectionResult: CommitWorkflowSelectionResult,
    hasOutgoingCommitsToPush: () -> Boolean,
): AiCommitAllWorkflowPreparationResult = when (selectionResult) {
    is CommitWorkflowSelectionResult.Prepared ->
        AiCommitAllWorkflowPreparationResult.Prepared(selectionResult.selection)

    CommitWorkflowSelectionResult.EmptySelection ->
        emptySelectionPreparation(mode, hasOutgoingCommitsToPush)

    CommitWorkflowSelectionResult.MissingWorkflow ->
        AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)

    is CommitWorkflowSelectionResult.UnsupportedVcs ->
        AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.UnsupportedVcs)

    is CommitWorkflowSelectionResult.UnsupportedWorkflow ->
        AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.UnsupportedWorkflow)
}

private fun emptySelectionPreparation(
    mode: AiCommitAllWorkflowMode,
    hasOutgoingCommitsToPush: () -> Boolean,
): AiCommitAllWorkflowPreparationResult = if (mode == AiCommitAllWorkflowMode.Push && hasOutgoingCommitsToPush()) {
    AiCommitAllWorkflowPreparationResult.PushOnly
} else {
    AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.EmptySelection)
}

internal interface AiCommitAllWorkflowScheduler {
    fun <T> supplyBackground(action: () -> T): CompletableFuture<T>

    fun <T> supplyEdt(action: () -> T): CompletableFuture<T>
}

private object IntellijAiCommitAllWorkflowScheduler : AiCommitAllWorkflowScheduler {
    override fun <T> supplyBackground(action: () -> T): CompletableFuture<T> {
        val scheduler = JobScheduler.getScheduler()
        return CompletableFuture.supplyAsync(action, scheduler)
    }

    override fun <T> supplyEdt(action: () -> T): CompletableFuture<T> {
        val application = ApplicationManager.getApplication()
        if (application == null || application.isDispatchThread) {
            return completedFrom(action)
        }

        val future = CompletableFuture<T>()
        application.invokeLater {
            future.completeFrom(action)
        }
        return future
    }
}

private fun <T> completedFrom(action: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    future.completeFrom(action)
    return future
}

private fun <T> CompletableFuture<T>.completeFrom(action: () -> T) {
    runCatching(action)
        .onSuccess(::complete)
        .onFailure(::completeExceptionally)
}

internal interface AiCommitAllWorkflowActivity : AutoCloseable {
    fun moveTo(phase: AiGenerationActivityPhase)
}

internal interface AiCommitAllWorkflowDependencies {
    fun startActivity(phase: AiGenerationActivityPhase): AiCommitAllWorkflowActivity

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
        onPushStarted: () -> Unit,
    ): CommitWorkflowExecutionResult

    fun hasOutgoingCommitsToPush(): Boolean = false

    fun isDisposed(): Boolean = false

    fun executePushOnly(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult = CommitWorkflowExecutionResult.UnsupportedExecutor

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

private fun AiCommitAllAiGenerationResult.diagnosticName(): String = when (this) {
    is AiCommitAllAiGenerationResult.AwaitingCompletion -> "AwaitingCompletion"
    is AiCommitAllAiGenerationResult.Stopped -> "Stopped(reason=$reason)"
}

private fun AiGenerationCompletionResult?.diagnosticName(): String = when (this) {
    is AiGenerationCompletionResult.Completed ->
        "Completed(evidence=$evidence)"

    is AiGenerationCompletionResult.Timeout ->
        "Timeout(timeout=$timeout)"

    AiGenerationCompletionResult.EmptyMessage -> "EmptyMessage"

    is AiGenerationCompletionResult.UnchangedMessage -> "UnchangedMessage"

    is AiGenerationCompletionResult.NoCompletionSignal -> "NoCompletionSignal"

    is AiGenerationCompletionResult.UserEditedMessage -> "UserEditedMessage"

    null -> "MissingCompletionResult"
}

private fun GitChangeSelection.diagnosticSummary(): String = "trackedChanges=${trackedChanges.size}, " +
    "unversionedFiles=${unversionedFiles.size}, " +
    "resolvedConflicts=${resolvedConflictPaths.size}, " +
    "stagingAreaPaths=${stagingAreaPaths.size}"

private class ProjectAiCommitAllWorkflowDependencies(private val project: Project) : AiCommitAllWorkflowDependencies {
    override fun startActivity(phase: AiGenerationActivityPhase): AiCommitAllWorkflowActivity {
        val activity = AiGenerationActivityStateService.getInstance(project).start(phase)
        return ProjectAiCommitAllWorkflowActivity(activity)
    }

    override fun checkReadiness(): VcsOperationReadinessResult {
        val readinessService = VcsOperationReadinessService.getInstance(project)
        return readinessService.checkAndReport()
    }

    override fun prepareAllFilesSelection(
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
    ): CommitWorkflowSelectionResult = CommitWorkflowSelectionService.getInstance(project)
        .prepareAllFilesSelection(workflowHandler, workflowUi)

    override fun runAiGeneration(
        phase: AiGenerationActivityPhase,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
        inputEvent: InputEvent?,
    ): AiCommitAllAiGenerationResult {
        val completionService = AiGenerationCompletionService.getInstance(project)
        val snapshot = AiCommitMessagePreparation.prepareInitialSnapshot(
            commitMessageUi = workflowUi.commitMessageUi,
            clearBeforeGeneration = AiCommitAllSettings.getInstance()
                .clearCommitMessageBeforeGeneration(),
            parentDataContext = parentDataContext,
        )
        return when (
            val invocation = AiCommitMessageActionInvocationService.getInstance(project)
                .invokeCommitMessageGeneration(
                    workflowHandler = workflowHandler,
                    workflowUi = workflowUi,
                    parentDataContext = parentDataContext,
                    inputEvent = inputEvent,
                )
        ) {
            is AiCommitMessageActionInvocationResult.Invoked ->
                AiCommitAllAiGenerationResult.AwaitingCompletion(
                    completionService.awaitCompletionAsync(
                        snapshot = snapshot,
                        invocation = invocation,
                        commitMessageUi = workflowUi.commitMessageUi,
                    ),
                )

            AiCommitMessageActionInvocationResult.MissingAction ->
                AiCommitAllAiGenerationResult.Stopped(AiCommitAllWorkflowStopReason.MissingAiAction)

            AiCommitMessageActionInvocationResult.MissingWorkflow ->
                AiCommitAllAiGenerationResult.Stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)
        }
    }

    override fun executeCommit(workflowHandler: CommitWorkflowHandler): CommitWorkflowExecutionResult {
        val executionService = CommitWorkflowExecutionService.getInstance(project)
        return executionService.executeCommit(workflowHandler)
    }

    override fun executeCommitAndPush(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
        onPushStarted: () -> Unit,
    ): CommitWorkflowExecutionResult = CommitWorkflowExecutionService.getInstance(project)
        .executeCommitAndPush(
            workflowHandler = workflowHandler,
            selection = selection,
            safeImmediatePushSupport = SafeImmediatePushService.getInstance(project),
            onPushStarted = onPushStarted,
        )

    override fun hasOutgoingCommitsToPush(): Boolean {
        val outgoingCommitsService = GitOutgoingCommitsService.getInstance(project)
        return outgoingCommitsService.hasOutgoingCommitsToPush()
    }

    override fun isDisposed(): Boolean = project.isDisposed

    override fun executePushOnly(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult = PushOnlyWorkflowExecutionService.getInstance(project)
        .executePush()

    override fun reportStop(reason: AiCommitAllWorkflowStopReason) {
        AiCommitAllWorkflowStopReporter.getInstance(project).report(reason)
    }
}

internal enum class AiCommitAllWorkflowMode {
    Ai,
    Commit,
    Push,
}

private class ProjectAiCommitAllWorkflowActivity(
    private val token: AiGenerationActivityToken,
) : AiCommitAllWorkflowActivity {
    override fun moveTo(phase: AiGenerationActivityPhase) {
        token.moveTo(phase)
    }

    override fun close() {
        token.close()
    }
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
