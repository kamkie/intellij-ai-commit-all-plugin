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

import pl.devopssolutions.aicommitall.vcs.SafeImmediateOutgoingPushSupport
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushDecision
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushFallbackReason
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

        val result = service.executePush()

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

        val result = service.executePush()

        assertSame(CommitWorkflowExecutionResult.UnsupportedExecutor, result)
    }

    @Test
    fun `does not catch immediate outgoing push invocation failures`() {
        val failure = IllegalStateException("push failed")
        val service = PushOnlyWorkflowExecutionService(
            outgoingPushSupport = TestOutgoingPushSupport(
                SafeImmediatePushDecision.Immediate(CapturingSafeImmediatePushPlan()),
            ),
            immediatePushExecutor = ThrowingImmediatePushExecutor(failure),
        )

        val thrown = assertFailsWith<IllegalStateException> {
            service.executePush()
        }

        assertSame(failure, thrown)
    }

    @Test
    fun `immediate outgoing push completion fails when push future fails`() {
        val pushPlan = CapturingSafeImmediatePushPlan()
        val immediatePushExecutor = CapturingImmediatePushExecutor()
        val service = PushOnlyWorkflowExecutionService(
            outgoingPushSupport = TestOutgoingPushSupport(
                SafeImmediatePushDecision.Immediate(pushPlan),
            ),
            immediatePushExecutor = immediatePushExecutor,
        )

        val result = service.executePush().asStarted()

        pushPlan.failPush(IllegalStateException("push failed"))

        assertEquals(1, immediatePushExecutor.pushCallCount)
        assertTrue(result.completion.isCompletedExceptionally)
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

        fun failPush(failure: RuntimeException) {
            completion.completeExceptionally(failure)
        }
    }

    private class CapturingImmediatePushExecutor : ImmediatePushExecutor {
        var pushCallCount = 0

        override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> {
            pushCallCount++
            return pushPlan.push()
        }
    }

    private class ThrowingImmediatePushExecutor(
        private val failure: RuntimeException,
    ) : ImmediatePushExecutor {
        override fun push(pushPlan: SafeImmediatePushPlan): CompletableFuture<Unit> = throw failure
    }

    private fun CommitWorkflowExecutionResult.asStarted(): CommitWorkflowExecutionResult.Started {
        val started = this as CommitWorkflowExecutionResult.Started
        return started
    }
}
