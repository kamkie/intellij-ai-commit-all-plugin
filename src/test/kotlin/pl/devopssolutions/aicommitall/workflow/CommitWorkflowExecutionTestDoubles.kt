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

import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.vcs.commit.AmendCommitHandler
import com.intellij.vcs.commit.CommitExecutorListener
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowHandlerState
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushSupport
import java.util.concurrent.CompletableFuture

internal class CapturingScheduler : CommitWorkflowExecutionScheduler {
    private val actions = mutableListOf<() -> Unit>()

    val scheduledActionCount: Int
        get() = actions.size

    override fun schedule(action: () -> Unit) {
        actions += action
    }

    fun runScheduledActions() {
        val scheduledActions = actions.toList()
        actions.clear()
        scheduledActions.forEach { action -> action() }
    }
}

internal class CapturingDefaultCommitExecutionGate : DefaultCommitExecutionGate {
    private val actions = mutableListOf<() -> Unit>()

    val readyActionCount: Int
        get() = actions.size

    override fun runWhenReady(
        workflowHandler: CommitWorkflowHandler,
        action: () -> Unit,
    ) {
        actions += action
    }

    fun runReadyActions() {
        val readyActions = actions.toList()
        actions.clear()
        readyActions.forEach { action -> action() }
    }
}

internal class ThrowingDefaultCommitExecutionGate(
    private val failure: RuntimeException,
) : DefaultCommitExecutionGate {
    override fun runWhenReady(
        workflowHandler: CommitWorkflowHandler,
        action: () -> Unit,
    ): Unit = throw failure
}

internal class TestSafeImmediatePushSupport(
    private val decision: SafeImmediatePushDecision,
) : SafeImmediatePushSupport {
    var prepareCallCount = 0

    override fun prepare(selection: GitChangeSelection): SafeImmediatePushDecision {
        prepareCallCount++
        return decision
    }
}

internal class CapturingSafeImmediatePushPlan : SafeImmediatePushPlan {
    private val completion = CompletableFuture<Unit>()
    var pushCallCount = 0

    override fun push(): CompletableFuture<Unit> {
        pushCallCount++
        return completion
    }

    fun completePush() {
        completion.complete(Unit)
    }

    fun failPush(failure: RuntimeException) {
        completion.completeExceptionally(failure)
    }
}

internal class CapturingImmediatePushExecutor : ImmediatePushExecutor {
    var pushCallCount = 0

    override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> {
        pushCallCount++
        return pushPlan.push()
    }
}

internal class ThrowingImmediatePushExecutor(
    private val failure: RuntimeException,
) : ImmediatePushExecutor {
    override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> = throw failure
}

internal class CapturingCommitResultRegistrar(
    private val registered: Boolean = true,
) : CommitWorkflowResultRegistrar {
    var registerCallCount = 0
    var resultHandler: CommitWorkflowResultHandler? = null
    private val registrations = mutableListOf<CapturingCommitWorkflowResultRegistration>()

    val disposeCallCount: Int
        get() = registrations.sumOf { registration -> registration.disposeCallCount }

    override fun register(
        workflowHandler: CommitWorkflowHandler,
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration? {
        registerCallCount++
        this.resultHandler = resultHandler
        return if (registered) {
            CapturingCommitWorkflowResultRegistration()
                .also(registrations::add)
        } else {
            null
        }
    }
}

internal class CapturingCommitWorkflowResultRegistration : CommitWorkflowResultRegistration {
    var disposeCallCount = 0

    override fun dispose() {
        disposeCallCount += 1
    }
}

internal class CapturingCommitWorkflowHandler(
    private val commitAndPushExecutor: CommitExecutor? = null,
    var commitAndPushEnabled: Boolean = false,
    private val defaultCommitFailure: RuntimeException? = null,
    private val executeFailure: RuntimeException? = null,
) : CommitWorkflowHandler,
    CommitExecutorListener {
    var executorCallCount = 0
    var executor: CommitExecutor? = null
    var executeCallCount = 0
    var executedExecutor: CommitExecutor? = null
    val requestedExecutorIds = mutableListOf<String>()

    override val amendCommitHandler: AmendCommitHandler
        get() = error("Not needed for execution tests.")

    override fun executorCalled(executor: CommitExecutor?) {
        executorCallCount++
        this.executor = executor
        defaultCommitFailure?.let { failure -> throw failure }
    }

    override fun getExecutor(executorId: String): CommitExecutor? {
        requestedExecutorIds += executorId
        return if (executorId == "Git.Commit.And.Push.Executor") {
            commitAndPushExecutor
        } else {
            null
        }
    }

    override fun isExecutorEnabled(executor: CommitExecutor): Boolean {
        val isCommitAndPushExecutor = executor === commitAndPushExecutor
        return isCommitAndPushExecutor && commitAndPushEnabled
    }

    override fun execute(executor: CommitExecutor) {
        executeCallCount++
        executedExecutor = executor
        executeFailure?.let { failure -> throw failure }
    }

    override fun getState(): CommitWorkflowHandlerState {
        val state = CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
        return state
    }
}

internal object UnsupportedCommitWorkflowHandler : CommitWorkflowHandler {
    override val amendCommitHandler: AmendCommitHandler
        get() = error("Not needed for execution tests.")

    override fun getExecutor(executorId: String): CommitExecutor? = null

    override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

    override fun execute(executor: CommitExecutor) = error("Not needed for execution tests.")

    override fun getState(): CommitWorkflowHandlerState {
        val state = CommitWorkflowHandlerState(isAmend = false, isSkipCommitChecks = false)
        return state
    }
}

internal object TestCommitAndPushExecutor : CommitExecutor {
    override fun getActionText(): String = "Commit and Push"
}

internal fun CommitWorkflowExecutionResult.asStarted(): CommitWorkflowExecutionResult.Started {
    val started = this as CommitWorkflowExecutionResult.Started
    return started
}
