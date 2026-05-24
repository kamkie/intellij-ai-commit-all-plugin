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

@Service(Service.Level.PROJECT)
internal class CommitWorkflowExecutionService(
    private val scheduler: CommitWorkflowExecutionScheduler = IntellijCommitWorkflowExecutionScheduler,
    private val postCommitPushScheduler: CommitWorkflowExecutionScheduler = IntellijPostCommitPushScheduler,
    private val safeImmediatePushSupport: SafeImmediatePushSupport = FallbackSafeImmediatePushSupport,
    private val immediatePushExecutor: ImmediatePushExecutor = IntellijImmediatePushExecutor,
    private val commitResultRegistrar: CommitWorkflowResultRegistrar = IntellijCommitWorkflowResultRegistrar,
    private val defaultCommitExecutionGate: DefaultCommitExecutionGate = IntellijDefaultCommitExecutionGate,
) {
    fun canExecuteCommit(workflowHandler: CommitWorkflowHandler?): Boolean = workflowHandler is CommitExecutorListener

    fun executeCommit(workflowHandler: CommitWorkflowHandler?): CommitWorkflowExecutionResult = when (
        val execution = workflowHandler.defaultCommitExecution()
    ) {
        DefaultCommitExecutionState.MissingWorkflow -> CommitWorkflowExecutionResult.MissingWorkflow
        DefaultCommitExecutionState.UnsupportedExecutor -> CommitWorkflowExecutionResult.UnsupportedExecutor
        is DefaultCommitExecutionState.Ready -> executeDefaultCommit(execution)
    }

    private fun executeDefaultCommit(
        execution: DefaultCommitExecutionState.Ready,
    ): CommitWorkflowExecutionResult {
        val completion = CompletableFuture<Unit>()
        scheduler.schedule {
            completeExceptionallyOnFailure(completion) {
                defaultCommitExecutionGate.runWhenReady(execution.workflowHandler) {
                    val registration = registerCompletion(execution.workflowHandler, completion)
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
    ): CommitWorkflowExecutionResult = when (val execution = workflowHandler.commitAndPushExecution()) {
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

    private fun executeCommitAndPush(
        execution: CommitAndPushExecutionState.Ready,
        selection: GitChangeSelection?,
        safeImmediatePushSupport: SafeImmediatePushSupport,
        onPushStarted: () -> Unit,
    ): CommitWorkflowExecutionResult {
        val completion = CompletableFuture<Unit>()
        scheduler.schedule {
            if (execution.workflowHandler.isExecutorEnabled(execution.executor)) {
                val immediatePushStarted = selection != null &&
                    executeImmediatePushWhenSafe(
                        workflowHandler = execution.workflowHandler,
                        selection = selection,
                        safeImmediatePushSupport = safeImmediatePushSupport,
                        onPushStarted = onPushStarted,
                        completion = completion,
                    )
                if (!immediatePushStarted) {
                    val registration = registerCommitAndPushCompletion(
                        workflowHandler = execution.workflowHandler,
                        completion = completion,
                        onPushStarted = onPushStarted,
                    )
                    completeExceptionallyOnFailure(completion, registration) {
                        execution.workflowHandler.execute(execution.executor)
                        if (registration == null) {
                            completion.complete(Unit)
                        }
                    }
                }
            } else {
                completion.complete(Unit)
            }
        }
        return CommitWorkflowExecutionResult.Started(completion)
    }

    private fun executeImmediatePushWhenSafe(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
        safeImmediatePushSupport: SafeImmediatePushSupport,
        onPushStarted: () -> Unit,
        completion: CompletableFuture<Unit>,
    ): Boolean {
        val execution = immediatePushExecution(
            workflowHandler = workflowHandler,
            selection = selection,
            safeImmediatePushSupport = safeImmediatePushSupport,
            onPushStarted = onPushStarted,
            completion = completion,
        )
        if (execution != null) {
            completeExceptionallyOnFailure(completion, execution.registration) {
                defaultCommitExecutionGate.runWhenReady(workflowHandler) {
                    completeExceptionallyOnFailure(completion, execution.registration) {
                        execution.executorListener.executorCalled(null)
                    }
                }
            }
        }
        return execution != null
    }

    private fun immediatePushExecution(
        workflowHandler: CommitWorkflowHandler,
        selection: GitChangeSelection,
        safeImmediatePushSupport: SafeImmediatePushSupport,
        onPushStarted: () -> Unit,
        completion: CompletableFuture<Unit>,
    ): ImmediatePushExecution? {
        val executorListener = workflowHandler as? CommitExecutorListener
        val pushPlan = (safeImmediatePushSupport.prepare(selection) as? SafeImmediatePushDecision.Immediate)?.plan
        return if (executorListener != null && pushPlan != null) {
            registerPostCommitPush(
                workflowHandler = workflowHandler,
                pushPlan = pushPlan,
                onPushStarted = onPushStarted,
                completion = completion,
            )?.let { registration ->
                ImmediatePushExecution(
                    executorListener = executorListener,
                    registration = registration,
                )
            }
        } else {
            null
        }
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

private data class ImmediatePushExecution(
    val executorListener: CommitExecutorListener,
    val registration: CommitWorkflowResultRegistration,
)

private class CompletionResultHandler(
    private val completion: CompletableFuture<Unit>,
) : CommitWorkflowResultHandler {
    override fun onSuccess() {
        completion.complete(Unit)
    }

    override fun onCancel() {
        completion.complete(Unit)
    }

    override fun onFailure() {
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
        commitSucceeded = true
        onPushStarted()
    }

    override fun onCancel() {
        completion.complete(Unit)
    }

    override fun onFailure() {
        completion.complete(Unit)
    }

    override fun onAfterRefresh() {
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
        onPushStarted()
        pushScheduler.schedule {
            executePush()
        }
    }

    private fun executePush() {
        completeExceptionallyOnFailure(completion) {
            immediatePushExecutor.push(pushPlan)
                .whenComplete { _, throwable ->
                    if (throwable != null) {
                        completion.completeExceptionally(throwable)
                    } else {
                        completion.complete(Unit)
                    }
                }
        }
    }

    override fun onCancel() {
        completion.complete(Unit)
    }

    override fun onFailure() {
        completion.complete(Unit)
    }
}

private object FallbackSafeImmediatePushSupport : SafeImmediatePushSupport {
    override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision = SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.UnsupportedPushApi)
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
