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
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler
import pl.devopssolutions.aicommitall.vcs.*
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class CommitWorkflowExecutionService(
    private val scheduler: CommitWorkflowExecutionScheduler = IntellijCommitWorkflowExecutionScheduler,
    private val postCommitPushScheduler: CommitWorkflowExecutionScheduler = IntellijPostCommitPushScheduler,
    private val safeImmediatePushSupport: SafeImmediatePushSupport = FallbackSafeImmediatePushSupport,
    private val commitResultRegistrar: CommitWorkflowResultRegistrar = IntellijCommitWorkflowResultRegistrar,
) {
    fun canExecuteCommit(workflowHandler: CommitWorkflowHandler?): Boolean = workflowHandler is CommitExecutorListener

    fun executeCommit(workflowHandler: CommitWorkflowHandler?): CommitWorkflowExecutionResult {
        if (workflowHandler == null) {
            return CommitWorkflowExecutionResult.MissingWorkflow
        }

        val executorListener = workflowHandler as? CommitExecutorListener
            ?: return CommitWorkflowExecutionResult.UnsupportedExecutor
        val completion = CompletableFuture<Unit>()
        scheduler.schedule {
            val registration = registerCompletion(workflowHandler, completion)
            try {
                executorListener.executorCalled(null)
                if (registration == null) {
                    completion.complete(Unit)
                }
            } catch (throwable: Throwable) {
                registration?.dispose()
                completion.completeExceptionally(throwable)
                throw throwable
            }
        }
        return CommitWorkflowExecutionResult.Started(completion)
    }

    fun canExecuteCommitAndPush(workflowHandler: CommitWorkflowHandler?): Boolean {
        if (workflowHandler == null) {
            return false
        }

        val executor = workflowHandler.getExecutor(GIT_COMMIT_AND_PUSH_EXECUTOR_ID)
            ?: return false
        return workflowHandler.isExecutorEnabled(executor)
    }

    fun executeCommitAndPush(
        workflowHandler: CommitWorkflowHandler?,
        selection: GitChangeSelection? = null,
        safeImmediatePushSupport: SafeImmediatePushSupport = this.safeImmediatePushSupport,
        onPushStarted: () -> Unit = {},
    ): CommitWorkflowExecutionResult {
        if (workflowHandler == null) {
            return CommitWorkflowExecutionResult.MissingWorkflow
        }

        val executor = workflowHandler.getExecutor(GIT_COMMIT_AND_PUSH_EXECUTOR_ID)
            ?: return CommitWorkflowExecutionResult.UnsupportedExecutor
        if (!workflowHandler.isExecutorEnabled(executor)) {
            return CommitWorkflowExecutionResult.DisabledExecutor
        }

        val completion = CompletableFuture<Unit>()
        scheduler.schedule {
            if (workflowHandler.isExecutorEnabled(executor)) {
                val immediatePushStarted = selection != null &&
                    executeImmediatePushWhenSafe(
                        workflowHandler = workflowHandler,
                        selection = selection,
                        safeImmediatePushSupport = safeImmediatePushSupport,
                        onPushStarted = onPushStarted,
                        completion = completion,
                    )
                if (!immediatePushStarted) {
                    val registration = registerCommitAndPushCompletion(
                        workflowHandler = workflowHandler,
                        completion = completion,
                        onPushStarted = onPushStarted,
                    )
                    try {
                        workflowHandler.execute(executor)
                        if (registration == null) {
                            completion.complete(Unit)
                        }
                    } catch (throwable: Throwable) {
                        registration?.dispose()
                        completion.completeExceptionally(throwable)
                        throw throwable
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
        val executorListener = workflowHandler as? CommitExecutorListener
            ?: return false
        val decision = safeImmediatePushSupport.prepare(selection)
        if (decision !is SafeImmediatePushDecision.Immediate) {
            return false
        }

        val registration = registerPostCommitPush(
            workflowHandler = workflowHandler,
            pushPlan = decision.plan,
            onPushStarted = onPushStarted,
            completion = completion,
        )
            ?: return false

        try {
            executorListener.executorCalled(null)
        } catch (throwable: Throwable) {
            registration.dispose()
            completion.completeExceptionally(throwable)
            throw throwable
        }
        return true
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

        private const val GIT_COMMIT_AND_PUSH_EXECUTOR_ID = "Git.Commit.And.Push.Executor"
    }
}

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
        try {
            pushPlan.push()
                .whenComplete { _, throwable ->
                    if (throwable != null) {
                        completion.completeExceptionally(throwable)
                    } else {
                        completion.complete(Unit)
                    }
                }
        } catch (throwable: Throwable) {
            completion.completeExceptionally(throwable)
            throw throwable
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

internal sealed interface CommitWorkflowExecutionResult {
    data class Started(
        val completion: CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit),
    ) : CommitWorkflowExecutionResult

    data object MissingWorkflow : CommitWorkflowExecutionResult

    data object UnsupportedExecutor : CommitWorkflowExecutionResult

    data object DisabledExecutor : CommitWorkflowExecutionResult
}
