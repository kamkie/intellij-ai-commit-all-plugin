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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.ai.*
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.GitOutgoingCommitsService
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
    ): CompletableFuture<AiCommitAllWorkflowResult> = runner.start(
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
    private val scheduler: AiCommitAllWorkflowScheduler = IntellijAiCommitAllWorkflowScheduler,
) {
    private val activeWorkflowLock = Any()
    private var activeWorkflow: CompletableFuture<AiCommitAllWorkflowResult>? = null

    fun start(
        mode: AiCommitAllWorkflowMode,
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): CompletableFuture<AiCommitAllWorkflowResult> = synchronized(activeWorkflowLock) {
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

        val activity = dependencies.startActivity(AiGenerationActivityPhase.Ai)
        return scheduler.supplyBackground {
            prepareWorkflow(mode, workflowHandler, workflowUi)
        }.thenCompose { preparation ->
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
    ): AiCommitAllWorkflowPreparationResult {
        when (dependencies.checkReadiness()) {
            VcsOperationReadinessResult.Ready -> Unit

            VcsOperationReadinessResult.Frozen ->
                return AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.VcsFrozen)

            VcsOperationReadinessResult.BackgroundOperationRunning ->
                return AiCommitAllWorkflowPreparationResult.Stopped(
                    AiCommitAllWorkflowStopReason.VcsBackgroundOperationRunning,
                )
        }

        return when (val selectionResult = dependencies.prepareAllFilesSelection(workflowHandler, workflowUi)) {
            is CommitWorkflowSelectionResult.Prepared ->
                AiCommitAllWorkflowPreparationResult.Prepared(selectionResult.selection)

            CommitWorkflowSelectionResult.EmptySelection ->
                if (mode == AiCommitAllWorkflowMode.Push && dependencies.hasOutgoingCommitsToPush()) {
                    AiCommitAllWorkflowPreparationResult.PushOnly
                } else {
                    AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.EmptySelection)
                }

            CommitWorkflowSelectionResult.MissingWorkflow ->
                AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.MissingWorkflow)

            is CommitWorkflowSelectionResult.UnsupportedVcs ->
                AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.UnsupportedVcs)

            is CommitWorkflowSelectionResult.UnsupportedWorkflow ->
                AiCommitAllWorkflowPreparationResult.Stopped(AiCommitAllWorkflowStopReason.UnsupportedWorkflow)
        }
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
    ): CompletableFuture<AiCommitAllWorkflowResult> = scheduler.supplyEdt {
        dependencies.runAiGeneration(
            phase = AiGenerationActivityPhase.Ai,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = dataContext,
            inputEvent = inputEvent,
        )
    }.thenCompose { generationResult ->
        when (generationResult) {
            is AiCommitAllAiGenerationResult.AwaitingCompletion ->
                generationResult.completion.handle { completionResult, throwable ->
                    if (throwable != null) {
                        stopped(AiCommitAllWorkflowStopReason.AiCompletionFailed)
                    } else {
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
            activity.moveTo(AiGenerationActivityPhase.Commit)
            dependencies.executeCommit(workflowHandler)
                .toWorkflowResult(AiCommitAllWorkflowStopReason.CommitExecutionUnavailable)
        }

        AiCommitAllWorkflowMode.Push -> {
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

    private fun stopped(reason: AiCommitAllWorkflowStopReason): CompletableFuture<AiCommitAllWorkflowResult> = CompletableFuture.completedFuture(
        stoppedResult(reason),
    )

    private fun stoppedResult(reason: AiCommitAllWorkflowStopReason): AiCommitAllWorkflowResult {
        dependencies.reportStop(reason)
        return AiCommitAllWorkflowResult.Stopped(reason)
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

internal interface AiCommitAllWorkflowScheduler {
    fun <T> supplyBackground(action: () -> T): CompletableFuture<T>

    fun <T> supplyEdt(action: () -> T): CompletableFuture<T>
}

private object IntellijAiCommitAllWorkflowScheduler : AiCommitAllWorkflowScheduler {
    override fun <T> supplyBackground(action: () -> T): CompletableFuture<T> = CompletableFuture.supplyAsync(action, JobScheduler.getScheduler())

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
    try {
        complete(action())
    } catch (throwable: Throwable) {
        completeExceptionally(throwable)
    }
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

private class ProjectAiCommitAllWorkflowDependencies(private val project: Project) : AiCommitAllWorkflowDependencies {
    override fun startActivity(phase: AiGenerationActivityPhase): AiCommitAllWorkflowActivity = ProjectAiCommitAllWorkflowActivity(
        AiGenerationActivityStateService.getInstance(project).start(phase),
    )

    override fun checkReadiness(): VcsOperationReadinessResult = VcsOperationReadinessService.getInstance(project).checkAndReport()

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

    override fun executeCommit(workflowHandler: CommitWorkflowHandler): CommitWorkflowExecutionResult = CommitWorkflowExecutionService.getInstance(project)
        .executeCommit(workflowHandler)

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

    override fun hasOutgoingCommitsToPush(): Boolean = GitOutgoingCommitsService.getInstance(project).hasOutgoingCommitsToPush()

    override fun executePushOnly(
        dataContext: DataContext,
        inputEvent: InputEvent?,
    ): CommitWorkflowExecutionResult = PushOnlyWorkflowExecutionService.getInstance(project)
        .executePush(dataContext, inputEvent)

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
