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
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.NotRunning),
            options = testOptions(),
        )

        assertEquals(AiGenerationCompletionResult.UnchangedMessage("old message"), result)
    }

    @Test
    fun `fails closed when action stops with empty message`() {
        val result = AiGenerationCompletionObserver().awaitCompletion(
            snapshot = AiCommitMessageSnapshot("old message"),
            messageReader = AiCommitMessageReader { "" },
            runningSignal = ConstantRunningSignal(AiGenerationRunningState.NotRunning),
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
    ): AiGenerationCompletionOptions =
        AiGenerationCompletionOptions(
            timeout = timeout,
            checkInterval = checkInterval,
        )
}
