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
package pl.devopssolutions.aicommitall.ai

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class AiGenerationCompletionObserverTest {
    @Test
    fun `completes after action stops and message is changed`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Running,
            AiGenerationRunningState.Running,
            AiGenerationRunningState.NotRunning,
        )
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader {
                if (signal.callCount >= 3) "generated message" else "old message"
            },
            runningSignal = signal,
            options = testOptions(),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("old message", completed.originalMessage)
        assertEquals("generated message", completed.generatedMessage)
        assertEquals(
            AiGenerationCompletionEvidence.ActionNoLongerRunningAndMessageChanged,
            completed.evidence,
        )
    }

    @Test
    fun `ignores initial not running signal until action starts`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.Running,
            AiGenerationRunningState.NotRunning,
        )
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader {
                if (signal.callCount >= 3) "generated message" else "old message"
            },
            runningSignal = signal,
            options = testOptions(),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
    }

    @Test
    fun `times out when action never starts and message does not change`() {
        val timeSource = MutableTimeSource()
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "old message" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.NotRunning),
            options = testOptions(timeout = Duration.ofMillis(1_000)),
        )

        val timeout = assertIs<AiGenerationCompletionResult.Timeout>(result)
        assertEquals(Duration.ofMillis(1_000), timeout.timeout)
        assertEquals("old message", timeout.latestMessage)
    }

    @Test
    fun `times out while action is still running`() {
        val timeSource = MutableTimeSource()
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "old message" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.Running),
            options = testOptions(timeout = Duration.ofMillis(1_000)),
        )

        val timeout = assertIs<AiGenerationCompletionResult.Timeout>(result)
        assertEquals(Duration.ofMillis(1_000), timeout.timeout)
        assertEquals("old message", timeout.latestMessage)
    }

    @Test
    fun `fails closed when action stops with unchanged message`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "old message" },
            runningSignal = SequenceRunningSignal(
                AiGenerationRunningState.Running,
                AiGenerationRunningState.NotRunning,
            ),
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.UnchangedMessage("old message"), result)
    }

    @Test
    fun `fails closed when action stops with empty message`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "" },
            runningSignal = SequenceRunningSignal(
                AiGenerationRunningState.Running,
                AiGenerationRunningState.NotRunning,
            ),
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.EmptyMessage, result)
    }

    @Test
    fun `does not treat message polling alone as completion evidence`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "generated message" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.Unavailable),
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.NoCompletionSignal("generated message"), result)
    }

    @Test
    fun `fails closed when user edits message during generation`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "user message" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.Running),
            userEditSignal = AiGenerationUserEditSignal { true },
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.UserEditedMessage("user message"), result)
    }

    private class SequenceRunningSignal(
        private vararg val states: AiGenerationRunningState,
    ) : AiGenerationRunningSignal {
        var callCount = 0

        override fun state(): AiGenerationRunningState {
            val state = states.getOrElse(callCount) { states.last() }
            callCount++
            return state
        }
    }

    private class ConstantRunningSignal(
        private val state: AiGenerationRunningState,
    ) : AiGenerationRunningSignal {
        override fun state(): AiGenerationRunningState = state
    }

    private class MutableTimeSource : AiCompletionTimeSource {
        var nowMillis = 0L

        override fun nowMillis(): Long = nowMillis
    }

    private class AdvancingSleeper(private val timeSource: MutableTimeSource) : AiCompletionSleeper {
        override fun sleep(duration: Duration) {
            timeSource.nowMillis += duration.toMillis()
        }
    }

    private fun testOptions(
        timeout: Duration = Duration.ofMillis(5_000),
        checkInterval: Duration = Duration.ofMillis(500),
    ): AiGenerationCompletionOptions = AiGenerationCompletionOptions(
        timeout = timeout,
        checkInterval = checkInterval,
    )
}
