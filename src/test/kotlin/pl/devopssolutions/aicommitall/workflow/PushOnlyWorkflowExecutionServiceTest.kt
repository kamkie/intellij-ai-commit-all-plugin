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
        val immediatePushExecutor = CapturingImmediatePushExecutor()
        val service = PushOnlyWorkflowExecutionService(
            outgoingPushSupport = TestOutgoingPushSupport(
                SafeImmediatePushDecision.Immediate(pushPlan),
            ),
            immediatePushExecutor = immediatePushExecutor,
        )

        val result = service.executePush(DataContext.EMPTY_CONTEXT, inputEvent = null)

        val started = result.asStarted()
        assertEquals(1, immediatePushExecutor.pushCallCount)
        assertEquals(1, pushPlan.pushCallCount)
        assertFalse(started.completion.isDone)

        pushPlan.completePush()

        assertTrue(started.completion.isDone)
    }

    @Test
    fun `does not invoke IDE push action when immediate outgoing push is unavailable`() {
        val service = PushOnlyWorkflowExecutionService(
            outgoingPushSupport = TestOutgoingPushSupport(
                SafeImmediatePushDecision.Fallback(SafeImmediatePushFallbackReason.AmbiguousTarget),
            ),
        )

        val result = service.executePush(DataContext.EMPTY_CONTEXT, inputEvent = null)

        assertSame(CommitWorkflowExecutionResult.UnsupportedExecutor, result)
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

    private class CapturingImmediatePushExecutor : ImmediatePushExecutor {
        var pushCallCount = 0

        override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> {
            pushCallCount++
            return pushPlan.push()
        }
    }

    private fun CommitWorkflowExecutionResult.asStarted(): CommitWorkflowExecutionResult.Started = this as CommitWorkflowExecutionResult.Started
}
