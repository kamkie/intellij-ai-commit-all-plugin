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
import kotlin.test.assertFailsWith
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
    fun `ignores repeated initial not running signals until action starts`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.NotRunning,
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
                if (signal.callCount >= 4) "generated message" else "old message"
            },
            runningSignal = signal,
            options = testOptions(),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
    }

    @Test
    fun `waits through transient unavailable signal before observing running completion`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Unavailable,
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
        assertEquals(3, signal.callCount)
    }

    @Test
    fun `waits through transient stopped signal with unchanged message`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Running,
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
                if (signal.callCount >= 4) "generated message" else "old message"
            },
            runningSignal = signal,
            options = testOptions(),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
    }

    @Test
    fun `completes when generated text appears after a stopped signal before grace expires`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Running,
            AiGenerationRunningState.NotRunning,
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
        assertEquals(1_000, timeSource.nowMillis)
    }

    @Test
    fun `does not fail closed when generated text appears after repeated stopped signals`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Running,
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.NotRunning,
        )
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader {
                if (signal.callCount >= 4) "generated message" else "old message"
            },
            runningSignal = signal,
            options = testOptions(),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
    }

    @Test
    fun `continues stopped signal grace period without application focus`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Running,
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.NotRunning,
        )
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "old message" },
            runningSignal = signal,
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.UnchangedMessage("old message"), result)
        assertEquals(1_500, timeSource.nowMillis)
    }

    @Test
    fun `zero stopped signal grace period completes on the next stable stopped poll`() {
        val timeSource = MutableTimeSource()
        val signal = SequenceRunningSignal(
            AiGenerationRunningState.Running,
            AiGenerationRunningState.NotRunning,
            AiGenerationRunningState.NotRunning,
        )
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "old message" },
            runningSignal = signal,
            options = testOptions(stoppedSignalGracePeriod = Duration.ZERO),
        )

        assertEquals(AiGenerationCompletionResult.UnchangedMessage("old message"), result)
        assertEquals(1_000, timeSource.nowMillis)
    }

    @Test
    fun `checks completion evidence at exactly the configured timeout boundary`() {
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
            options = testOptions(timeout = Duration.ofMillis(1_000)),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
        assertEquals(1_000, timeSource.nowMillis)
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
    fun `times out when action never starts with unchanged prefilled message and unchanged acceptance is enabled`() {
        val timeSource = MutableTimeSource()
        val observer = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        )

        val result = observer.awaitCompletion(
            snapshot = AiCommitMessageSnapshot(
                originalMessage = "old message",
                acceptUnchangedPrefilledMessage = true,
            ),
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
        val timeSource = MutableTimeSource()
        val result = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        ).awaitCompletion(
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
    fun `completes when action stops with unchanged non-empty prefilled message and unchanged acceptance is enabled`() {
        val timeSource = MutableTimeSource()
        val result = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        ).awaitCompletion(
            snapshot = AiCommitMessageSnapshot(
                originalMessage = "old message",
                acceptUnchangedPrefilledMessage = true,
            ),
            messageReader = AiCommitMessageReader { "old message" },
            runningSignal = SequenceRunningSignal(
                AiGenerationRunningState.Running,
                AiGenerationRunningState.NotRunning,
            ),
            options = testOptions(),
        )

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("old message", completed.originalMessage)
        assertEquals("old message", completed.generatedMessage)
        assertEquals(
            AiGenerationCompletionEvidence.ActionNoLongerRunningAndUnchangedPrefilledMessage,
            completed.evidence,
        )
    }

    @Test
    fun `fails closed when action stops with empty message`() {
        val timeSource = MutableTimeSource()
        val result = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        ).awaitCompletion(
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
    fun `fails closed when action stops with unchanged empty prefilled message and unchanged acceptance is enabled`() {
        val timeSource = MutableTimeSource()
        val result = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        ).awaitCompletion(
            snapshot = AiCommitMessageSnapshot(
                originalMessage = "",
                acceptUnchangedPrefilledMessage = true,
            ),
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
    fun `returns no completion signal when running state stays unavailable through settling budget`() {
        val timeSource = MutableTimeSource()
        val signal = ConstantRunningSignal(AiGenerationRunningState.Unavailable)

        val result = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        ).awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "generated message" },
            runningSignal = signal,
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.NoCompletionSignal("generated message"), result)
        assertEquals(3, signal.callCount)
        assertEquals(1_000, timeSource.nowMillis)
    }

    @Test
    fun `returns no completion signal with blank latest message when unavailable signal settles`() {
        val timeSource = MutableTimeSource()

        val result = AiGenerationCompletionObserver(
            timeSource = timeSource,
            sleeper = AdvancingSleeper(timeSource),
        ).awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.Unavailable),
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.NoCompletionSignal(""), result)
    }

    @Test
    fun `user edit wins while running signal is unavailable`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "user message" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.Unavailable),
            userEditSignal = AiGenerationUserEditSignal { true },
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.UserEditedMessage("user message"), result)
    }

    @Test
    fun `user edit wins over simultaneously completed generated message`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "generated message" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.NotRunning),
            userEditSignal = AiGenerationUserEditSignal { true },
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.UserEditedMessage("generated message"), result)
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

    @Test
    fun `accepts zero stopped signal grace period`() {
        val options = testOptions(stoppedSignalGracePeriod = Duration.ZERO)

        assertEquals(Duration.ZERO, options.stoppedSignalGracePeriod)
    }

    @Test
    fun `rejects nonpositive timeout and check interval`() {
        assertFailsWith<IllegalArgumentException> {
            testOptions(timeout = Duration.ZERO)
        }
        assertFailsWith<IllegalArgumentException> {
            testOptions(checkInterval = Duration.ZERO)
        }
        assertFailsWith<IllegalArgumentException> {
            testOptions(stoppedSignalGracePeriod = Duration.ofMillis(-1))
        }
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
        var callCount = 0

        override fun state(): AiGenerationRunningState {
            callCount++
            return state
        }
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
        stoppedSignalGracePeriod: Duration = Duration.ofMillis(1_000),
    ): AiGenerationCompletionOptions = AiGenerationCompletionOptions(
        timeout = timeout,
        checkInterval = checkInterval,
        stoppedSignalGracePeriod = stoppedSignalGracePeriod,
    )
}
