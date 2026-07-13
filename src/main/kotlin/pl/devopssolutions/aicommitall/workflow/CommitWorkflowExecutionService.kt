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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushFallbackReason
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushSupport
import java.util.concurrent.CompletableFuture

private const val GIT_COMMIT_AND_PUSH_EXECUTOR_ID = "Git.Commit.And.Push.Executor"
private const val NO_SELECTION_FALLBACK_REASON = "NoSelection"
private const val UNSUPPORTED_HANDLER_FALLBACK_REASON = "UnsupportedHandler"
private const val RESULT_LISTENER_UNAVAILABLE_FALLBACK_REASON = "ResultListenerUnavailable"

@Service(Service.Level.PROJECT)
internal class CommitWorkflowExecutionService(
    private val scheduler: CommitWorkflowExecutionScheduler = IntellijCommitWorkflowExecutionScheduler,
    private val postCommitPushScheduler: CommitWorkflowExecutionScheduler = IntellijPostCommitPushScheduler,
    private val safeImmediatePushSupport: SafeImmediatePushSupport = FallbackSafeImmediatePushSupport,
    private val immediatePushExecutor: ImmediatePushExecutor = IntellijImmediatePushExecutor,
    private val commitResultRegistrar: CommitWorkflowResultRegistrar = IntellijCommitWorkflowResultRegistrar,
    private val defaultCommitExecutionGate: DefaultCommitExecutionGate = IntellijDefaultCommitExecutionGate,
    private val defaultCommitUiHandoff: DefaultCommitUiHandoff = IntellijDefaultCommitUiHandoff,
) {
    fun canExecuteCommit(workflowHandler: CommitWorkflowHandler?): Boolean = workflowHandler is CommitExecutorListener

    fun executeCommit(workflowHandler: CommitWorkflowHandler?): CommitWorkflowExecutionResult {
        val execution = workflowHandler.defaultCommitExecution()
        logger.info(
            "AI Commit All diagnostic: commit execution requested, " +
                "state=${execution.diagnosticName()}",
        )
        return when (execution) {
            DefaultCommitExecutionState.MissingWorkflow -> CommitWorkflowExecutionResult.MissingWorkflow
            DefaultCommitExecutionState.UnsupportedExecutor -> CommitWorkflowExecutionResult.UnsupportedExecutor
            is DefaultCommitExecutionState.Ready -> executeDefaultCommit(execution)
        }
    }

    private fun executeDefaultCommit(
        execution: DefaultCommitExecutionState.Ready,
    ): CommitWorkflowExecutionResult {
        val completion = CompletableFuture<Unit>()
        logger.info(
            "AI Commit All diagnostic: default commit execution scheduled, " +
                "workflowHandler=${execution.workflowHandler.javaClass.name}",
        )
        completion.whenComplete { _, throwable ->
            logger.info(
                "AI Commit All diagnostic: default commit execution completed, " +
                    "outcome=${throwable?.diagnosticOutcome() ?: "completed"}",
            )
        }
        scheduler.schedule {
            logger.info("AI Commit All diagnostic: default commit scheduler callback started")
            completeExceptionallyOnFailure(completion) {
                defaultCommitExecutionGate.runWhenReady(execution.workflowHandler) {
                    logger.info("AI Commit All diagnostic: default commit readiness gate opened")
                    completeExceptionallyOnFailure(completion) {
                        defaultCommitUiHandoff.apply(execution.workflowHandler)
                    }
                    logger.info("AI Commit All diagnostic: default commit UI handoff applied")
                    val registration = registerCompletion(execution.workflowHandler, completion)
                    logger.info(
                        "AI Commit All diagnostic: invoking default commit executor, " +
                            "resultListenerRegistered=${registration != null}",
                    )
                    completeExceptionallyOnFailure(completion, registration) {
                        execution.executorListener.executorCalled(null)
                        if (registration == null) {
                            completion.complete(Unit)
                        }
                    }
                }
            }
        }
        return CommitWorkflowExecutionResult.Started(completion)
    }

    fun canExecuteCommitAndPush(workflowHandler: CommitWorkflowHandler?): Boolean = workflowHandler?.let { handler ->
        handler.getExecutor(GIT_COMMIT_AND_PUSH_EXECUTOR_ID)
            ?.let(handler::isExecutorEnabled)
    } == true

    fun executeCommitAndPush(
        workflowHandler: CommitWorkflowHandler?,
        selection: GitChangeSelection? = null,
        safeImmediatePushSupport: SafeImmediatePushSupport = this.safeImmediatePushSupport,
        onPushStarted: () -> Unit = {},
    ): CommitWorkflowExecutionResult {
        val execution = workflowHandler.commitAndPushExecution()
        logger.info(
            "AI Commit All diagnostic: commit-and-push execution requested, " +
                "state=${execution.diagnosticName()}, hasSelection=${selection != null}",
        )
        return when (execution) {
            CommitAndPushExecutionState.MissingWorkflow -> CommitWorkflowExecutionResult.MissingWorkflow

            CommitAndPushExecutionState.UnsupportedExecutor -> CommitWorkflowExecutionResult.UnsupportedExecutor

            CommitAndPushExecutionState.DisabledExecutor -> CommitWorkflowExecutionResult.DisabledExecutor

            is CommitAndPushExecutionState.Ready -> executeCommitAndPush(
                execution = execution,
                selection = selection,
                safeImmediatePushSupport = safeImmediatePushSupport,
                onPushStarted = onPushStarted,
            )
        }
    }

    private fun executeCommitAndPush(
        execution: CommitAndPushExecutionState.Ready,
        selection: GitChangeSelection?,
        safeImmediatePushSupport: SafeImmediatePushSupport,
        onPushStarted: () -> Unit,
    ): CommitWorkflowExecutionResult {
        val completion = CompletableFuture<Unit>()
        logger.info(
            "AI Commit All diagnostic: commit-and-push execution scheduled, " +
                "workflowHandler=${execution.workflowHandler.javaClass.name}, " +
                "executor=${execution.executor.id}",
        )
        completion.whenComplete { _, throwable ->
            logger.info(
                "AI Commit All diagnostic: commit-and-push execution completed, " +
                    "outcome=${throwable?.diagnosticOutcome() ?: "completed"}",
            )
        }
        scheduler.schedule {
            logger.info("AI Commit All diagnostic: commit-and-push scheduler callback started")
            if (execution.workflowHandler.isExecutorEnabled(execution.executor)) {
                val immediatePushAttempt = executeImmediatePushWhenSafe(
                    workflowHandler = execution.workflowHandler,
                    selection = selection,
                    safeImmediatePushSupport = safeImmediatePushSupport,
                    onPushStarted = onPushStarted,
                    completion = completion,
                )
                logger.info(
                    "AI Commit All diagnostic: commit-and-push execution path selected, " +
                        immediatePushAttempt.diagnosticSummary(),
                )
                if (immediatePushAttempt !is ImmediatePushAttempt.Started) {
                    val registration = registerCommitAndPushCompletion(
                        workflowHandler = execution.workflowHandler,
                        completion = completion,
                        onPushStarted = onPushStarted,
                    )
                    logger.info(
                        "AI Commit All diagnostic: invoking commit-and-push executor, " +
                            "resultListenerRegistered=${registration != null}",
                    )
                    completeExceptionallyOnFailure(completion, registration) {
                        execution.workflowHandler.execute(execution.executor)
                        if (registration == null) {
                            completion.complete(Unit)
                        }
                    }
                }
            } else {
                logger.info("AI Commit All diagnostic: commit-and-push executor became disabled before invocation")
                completion.complete(Unit)
            }
        }
        return CommitWorkflowExecutionResult.Started(completion)
    }

    internal fun executeImmediatePushWhenSafe(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection?,
        safeImmediatePushSupport: SafeImmediatePushSupport,
        onPushStarted: () -> Unit,
        completion: CompletableFuture<Unit>,
    ): ImmediatePushAttempt {
        if (selection == null) {
            return ImmediatePushAttempt.Fallback(NO_SELECTION_FALLBACK_REASON)
        }
        val executorListener = workflowHandler as? CommitExecutorListener
        val decision = safeImmediatePushSupport.prepare(selection)
        return if (executorListener == null) {
            ImmediatePushAttempt.Fallback(UNSUPPORTED_HANDLER_FALLBACK_REASON)
        } else {
            when (decision) {
                is SafeImmediatePushDecision.Fallback -> ImmediatePushAttempt.Fallback(decision.reason.name)

                is SafeImmediatePushDecision.Immediate -> startImmediatePush(
                    workflowHandler = workflowHandler,
                    executorListener = executorListener,
                    pushPlan = decision.plan,
                    onPushStarted = onPushStarted,
                    completion = completion,
                )
            }
        }
    }

    private fun startImmediatePush(
        workflowHandler: CommitWorkflowHandler,
        executorListener: CommitExecutorListener,
        pushPlan: SafeImmediatePushPlan,
        onPushStarted: () -> Unit,
        completion: CompletableFuture<Unit>,
    ): ImmediatePushAttempt {
        val registration = registerPostCommitPush(
            workflowHandler = workflowHandler,
            pushPlan = pushPlan,
            onPushStarted = onPushStarted,
            completion = completion,
        ) ?: return ImmediatePushAttempt.Fallback(RESULT_LISTENER_UNAVAILABLE_FALLBACK_REASON)
        logger.info("AI Commit All diagnostic: immediate push commit result listener registered")
        completeExceptionallyOnFailure(completion, registration) {
            defaultCommitExecutionGate.runWhenReady(workflowHandler) {
                logger.info("AI Commit All diagnostic: immediate push commit readiness gate opened")
                completeExceptionallyOnFailure(completion, registration) {
                    logger.info("AI Commit All diagnostic: invoking default commit executor before immediate push")
                    executorListener.executorCalled(null)
                }
            }
        }
        return ImmediatePushAttempt.Started
    }

    private fun registerCompletion(
        workflowHandler: CommitWorkflowHandler,
        completion: CompletableFuture<Unit>,
    ): CommitWorkflowResultRegistration? = commitResultRegistrar.register(
        workflowHandler = workflowHandler,
        resultHandler = CompletionResultHandler(completion),
    )

    private fun registerPostCommitPush(
        workflowHandler: CommitWorkflowHandler,
        pushPlan: SafeImmediatePushPlan,
        onPushStarted: () -> Unit,
        completion: CompletableFuture<Unit>,
    ): CommitWorkflowResultRegistration? = commitResultRegistrar.register(
        workflowHandler = workflowHandler,
        resultHandler = PostCommitPushResultHandler(
            pushPlan = pushPlan,
            pushScheduler = postCommitPushScheduler,
            immediatePushExecutor = immediatePushExecutor,
            onPushStarted = onPushStarted,
            completion = completion,
        ),
    )

    private fun registerCommitAndPushCompletion(
        workflowHandler: CommitWorkflowHandler,
        completion: CompletableFuture<Unit>,
        onPushStarted: () -> Unit,
    ): CommitWorkflowResultRegistration? = commitResultRegistrar.register(
        workflowHandler = workflowHandler,
        resultHandler = CommitAndPushResultHandler(
            completion = completion,
            onPushStarted = onPushStarted,
        ),
    )

    companion object {
        private val logger: Logger = Logger.getInstance(CommitWorkflowExecutionService::class.java)

        fun getInstance(project: Project): CommitWorkflowExecutionService = project.service()
    }
}

private sealed interface DefaultCommitExecutionState {
    data object MissingWorkflow : DefaultCommitExecutionState

    data object UnsupportedExecutor : DefaultCommitExecutionState

    data class Ready(
        val workflowHandler: CommitWorkflowHandler,
        val executorListener: CommitExecutorListener,
    ) : DefaultCommitExecutionState
}

private fun CommitWorkflowHandler?.defaultCommitExecution(): DefaultCommitExecutionState = when (this) {
    null -> DefaultCommitExecutionState.MissingWorkflow
    !is CommitExecutorListener -> DefaultCommitExecutionState.UnsupportedExecutor
    else -> DefaultCommitExecutionState.Ready(workflowHandler = this, executorListener = this)
}

private fun DefaultCommitExecutionState.diagnosticName(): String = when (this) {
    DefaultCommitExecutionState.MissingWorkflow -> "MissingWorkflow"

    DefaultCommitExecutionState.UnsupportedExecutor -> "UnsupportedExecutor"

    is DefaultCommitExecutionState.Ready ->
        "Ready(workflowHandler=${workflowHandler.javaClass.name})"
}

private sealed interface CommitAndPushExecutionState {
    data object MissingWorkflow : CommitAndPushExecutionState

    data object UnsupportedExecutor : CommitAndPushExecutionState

    data object DisabledExecutor : CommitAndPushExecutionState

    data class Ready(
        val workflowHandler: CommitWorkflowHandler,
        val executor: CommitExecutor,
    ) : CommitAndPushExecutionState
}

private fun CommitWorkflowHandler?.commitAndPushExecution(): CommitAndPushExecutionState {
    val executor = this?.getExecutor(GIT_COMMIT_AND_PUSH_EXECUTOR_ID)
    return when {
        this == null -> CommitAndPushExecutionState.MissingWorkflow
        executor == null -> CommitAndPushExecutionState.UnsupportedExecutor
        !isExecutorEnabled(executor) -> CommitAndPushExecutionState.DisabledExecutor
        else -> CommitAndPushExecutionState.Ready(workflowHandler = this, executor = executor)
    }
}

private fun CommitAndPushExecutionState.diagnosticName(): String = when (this) {
    CommitAndPushExecutionState.MissingWorkflow -> "MissingWorkflow"

    CommitAndPushExecutionState.UnsupportedExecutor -> "UnsupportedExecutor"

    CommitAndPushExecutionState.DisabledExecutor -> "DisabledExecutor"

    is CommitAndPushExecutionState.Ready ->
        "Ready(workflowHandler=${workflowHandler.javaClass.name}, executor=${executor.id})"
}

internal sealed interface ImmediatePushAttempt {
    data object Started : ImmediatePushAttempt

    data class Fallback(val reason: String) : ImmediatePushAttempt
}

internal fun ImmediatePushAttempt.diagnosticSummary(): String = when (this) {
    ImmediatePushAttempt.Started -> "immediatePushStarted=true"
    is ImmediatePushAttempt.Fallback -> "immediatePushStarted=false, fallbackReason=$reason"
}

private class CompletionResultHandler(
    private val completion: CompletableFuture<Unit>,
) : CommitWorkflowResultHandler {
    override fun onSuccess() {
        commitWorkflowResultLogger.info("AI Commit All diagnostic: default commit result callback, result=success")
        completion.complete(Unit)
    }

    override fun onCancel() {
        commitWorkflowResultLogger.info("AI Commit All diagnostic: default commit result callback, result=cancel")
        completion.complete(Unit)
    }

    override fun onFailure() {
        commitWorkflowResultLogger.info("AI Commit All diagnostic: default commit result callback, result=failure")
        completion.complete(Unit)
    }
}

private class CommitAndPushResultHandler(
    private val completion: CompletableFuture<Unit>,
    private val onPushStarted: () -> Unit,
) : CommitWorkflowResultHandler {
    override val waitForAfterRefreshOnSuccess: Boolean = true

    private var commitSucceeded = false

    override fun onSuccess() {
        commitWorkflowResultLogger.info(
            "AI Commit All diagnostic: commit-and-push result callback, result=success, waitingForAfterRefresh=true",
        )
        commitSucceeded = true
        onPushStarted()
    }

    override fun onCancel() {
        commitWorkflowResultLogger.info("AI Commit All diagnostic: commit-and-push result callback, result=cancel")
        completion.complete(Unit)
    }

    override fun onFailure() {
        commitWorkflowResultLogger.info("AI Commit All diagnostic: commit-and-push result callback, result=failure")
        completion.complete(Unit)
    }

    override fun onAfterRefresh() {
        commitWorkflowResultLogger.info(
            "AI Commit All diagnostic: commit-and-push after-refresh callback, commitSucceeded=$commitSucceeded",
        )
        if (commitSucceeded) {
            completion.complete(Unit)
        }
    }
}

private class PostCommitPushResultHandler(
    private val pushPlan: SafeImmediatePushPlan,
    private val pushScheduler: CommitWorkflowExecutionScheduler,
    private val immediatePushExecutor: ImmediatePushExecutor,
    private val onPushStarted: () -> Unit,
    private val completion: CompletableFuture<Unit>,
) : CommitWorkflowResultHandler {
    override fun onSuccess() {
        commitWorkflowResultLogger.info(
            "AI Commit All diagnostic: immediate push commit result callback, result=success, schedulingPush=true",
        )
        onPushStarted()
        pushScheduler.schedule {
            commitWorkflowResultLogger.info("AI Commit All diagnostic: immediate push scheduler callback started")
            executePush()
        }
    }

    private fun executePush() {
        completeExceptionallyOnFailure(completion) {
            commitWorkflowResultLogger.info("AI Commit All diagnostic: immediate push invoked")
            immediatePushExecutor.push(pushPlan)
                .whenComplete { _, throwable ->
                    commitWorkflowResultLogger.info(
                        "AI Commit All diagnostic: immediate push future completed, " +
                            "outcome=${throwable?.diagnosticOutcome() ?: "completed"}",
                    )
                    if (throwable != null) {
                        completion.completeExceptionally(throwable)
                    } else {
                        completion.complete(Unit)
                    }
                }
        }
    }

    override fun onCancel() {
        commitWorkflowResultLogger.info(
            "AI Commit All diagnostic: immediate push commit result callback, result=cancel",
        )
        completion.complete(Unit)
    }

    override fun onFailure() {
        commitWorkflowResultLogger.info(
            "AI Commit All diagnostic: immediate push commit result callback, result=failure",
        )
        completion.complete(Unit)
    }
}

private object FallbackSafeImmediatePushSupport : SafeImmediatePushSupport {
    override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision {
        val reason = SafeImmediatePushFallbackReason.UnsupportedPushApi
        return SafeImmediatePushDecision.Fallback(reason)
    }
}

internal fun interface CommitWorkflowExecutionScheduler {
    fun schedule(action: () -> Unit)
}

internal fun interface ImmediatePushExecutor {
    fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit>
}

internal fun interface DefaultCommitExecutionGate {
    fun runWhenReady(
        workflowHandler: CommitWorkflowHandler,
        action: () -> Unit,
    )
}

internal fun interface DefaultCommitUiHandoff {
    fun apply(workflowHandler: CommitWorkflowHandler)
}

private object IntellijCommitWorkflowExecutionScheduler : CommitWorkflowExecutionScheduler {
    override fun schedule(action: () -> Unit) {
        val application = ApplicationManager.getApplication()
        if (application == null) {
            action()
        } else {
            application.invokeLater(action)
        }
    }
}

private object IntellijPostCommitPushScheduler : CommitWorkflowExecutionScheduler {
    override fun schedule(action: () -> Unit) {
        JobScheduler.getScheduler().execute(action)
    }
}

private object IntellijDefaultCommitExecutionGate : DefaultCommitExecutionGate {
    override fun runWhenReady(
        workflowHandler: CommitWorkflowHandler,
        action: () -> Unit,
    ) {
        val project = (workflowHandler as? AbstractCommitWorkflowHandler<*, *>)
            ?.workflow
            ?.project
        if (project == null || project.isDisposed) {
            action()
            return
        }

        DumbService.getInstance(project).smartInvokeLater(action)
    }
}

private object IntellijDefaultCommitUiHandoff : DefaultCommitUiHandoff {
    override fun apply(workflowHandler: CommitWorkflowHandler) {
        check(
            ReflectiveCommitWorkflowSynchronizer.prepareCommitUi(
                workflowHandler = workflowHandler,
                workflowUi = null,
                consumeConfirmedHandoff = true,
            ),
        ) {
            "Confirmed git-stage Commit UI handoff is unavailable before commit execution"
        }
    }
}

internal object IntellijImmediatePushExecutor : ImmediatePushExecutor {
    override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> {
        var completion: CompletableFuture<Unit>? = null
        ProgressManager.getInstance().runProcess(
            {
                completion = pushPlan.push()
            },
            EmptyProgressIndicator(),
        )
        return completion ?: CompletableFuture.completedFuture(Unit)
    }
}

internal sealed interface CommitWorkflowExecutionResult {
    data class Started(
        val completion: CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit),
    ) : CommitWorkflowExecutionResult

    data object MissingWorkflow : CommitWorkflowExecutionResult

    data object UnsupportedExecutor : CommitWorkflowExecutionResult

    data object DisabledExecutor : CommitWorkflowExecutionResult
}

private inline fun completeExceptionallyOnFailure(
    completion: CompletableFuture<Unit>,
    registration: CommitWorkflowResultRegistration? = null,
    action: () -> Unit,
) {
    val result = runCatching(action)
    result.exceptionOrNull()?.let { failure ->
        registration?.dispose()
        completion.completeExceptionally(failure)
    }
    result.getOrThrow()
}

private fun Throwable.diagnosticOutcome(): String = "exception=${javaClass.name}, " +
    "cause=${cause?.javaClass?.name ?: "<none>"}"

private val commitWorkflowResultLogger: Logger = Logger.getInstance(CommitWorkflowExecutionService::class.java)
