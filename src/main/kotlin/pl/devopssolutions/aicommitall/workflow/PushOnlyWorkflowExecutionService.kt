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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import pl.devopssolutions.aicommitall.vcs.SafeImmediateOutgoingPushSupport
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushService
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class PushOnlyWorkflowExecutionService(
    private val outgoingPushSupport: SafeImmediateOutgoingPushSupport,
    private val immediatePushExecutor: ImmediatePushExecutor = IntellijImmediatePushExecutor,
) {
    constructor(project: Project) : this(
        outgoingPushSupport = SafeImmediatePushService.getInstance(project),
    )

    fun executePush(): CommitWorkflowExecutionResult {
        val decision = outgoingPushSupport.prepareOutgoingCommits()
        if (decision is SafeImmediatePushDecision.Immediate) {
            return executeImmediatePush(decision.plan)
        }
        return CommitWorkflowExecutionResult.UnsupportedExecutor
    }

    private fun executeImmediatePush(pushPlan: SafeImmediatePushPlan): CommitWorkflowExecutionResult {
        val completion = CompletableFuture<Unit>()
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
        return CommitWorkflowExecutionResult.Started(completion)
    }

    companion object {
        fun getInstance(project: Project): PushOnlyWorkflowExecutionService = project.service()
    }
}

private inline fun completeExceptionallyOnFailure(
    completion: CompletableFuture<Unit>,
    action: () -> Unit,
) {
    val result = runCatching(action)
    result.exceptionOrNull()?.let(completion::completeExceptionally)
    result.getOrThrow()
}
