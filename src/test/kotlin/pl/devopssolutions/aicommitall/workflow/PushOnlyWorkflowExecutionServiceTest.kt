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

import com.intellij.openapi.actionSystem.DataContext
import pl.devopssolutions.aicommitall.vcs.SafeImmediateOutgoingPushSupport
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushFallbackReason
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class PushOnlyWorkflowExecutionServiceTest {
    @Test
    fun `executes immediate outgoing push without invoking IDE push action`() {
        val pushPlan = CapturingSafeImmediatePushPlan()
        val idePushAction = CapturingPushOnlyIdeAction()
        val service = PushOnlyWorkflowExecutionService(
            outgoingPushSupport = TestOutgoingPushSupport(
                SafeImmediatePushDecision.Immediate(pushPlan),
            ),
            idePushAction = idePushAction,
        )

        val result = service.executePush(DataContext.EMPTY_CONTEXT, inputEvent = null)

        val started = result.asStarted()
        assertEquals(1, pushPlan.pushCallCount)
        assertEquals(0, idePushAction.executeCallCount)
        assertFalse(started.completion.isDone)

        pushPlan.completePush()

        assertTrue(started.completion.isDone)
    }

    @Test
    fun `falls back to IDE push action when immediate outgoing push is unavailable`() {
        val ideResult = CommitWorkflowExecutionResult.Started()
        val idePushAction = CapturingPushOnlyIdeAction(executeResult = ideResult)
        val service = PushOnlyWorkflowExecutionService(
            outgoingPushSupport = TestOutgoingPushSupport(
                SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.AmbiguousTarget),
            ),
            idePushAction = idePushAction,
        )

        val result = service.executePush(DataContext.EMPTY_CONTEXT, inputEvent = null)

        assertSame(ideResult, result)
        assertEquals(1, idePushAction.executeCallCount)
    }

    private class TestOutgoingPushSupport(
        private val decision: SafeImmediatePushDecision,
    ) : SafeImmediateOutgoingPushSupport {
        override fun prepareOutgoingCommits(): SafeImmediatePushDecision = decision
    }

    private class CapturingSafeImmediatePushPlan : SafeImmediatePushPlan {
        private val completion = CompletableFuture<Unit>()
        var pushCallCount = 0

        override fun push(): CompletableFuture<Unit> {
            pushCallCount++
            return completion
        }

        fun completePush() {
            completion.complete(Unit)
        }
    }

    private class CapturingPushOnlyIdeAction(
        private val executeResult: CommitWorkflowExecutionResult = CommitWorkflowExecutionResult.Started(),
    ) : PushOnlyIdeAction {
        var executeCallCount = 0

        override fun canExecute(dataContext: DataContext): Boolean = true

        override fun execute(
            dataContext: DataContext,
            inputEvent: java.awt.event.InputEvent?,
        ): CommitWorkflowExecutionResult {
            executeCallCount++
            return executeResult
        }
    }

    private fun CommitWorkflowExecutionResult.asStarted(): CommitWorkflowExecutionResult.Started = this as CommitWorkflowExecutionResult.Started
}
