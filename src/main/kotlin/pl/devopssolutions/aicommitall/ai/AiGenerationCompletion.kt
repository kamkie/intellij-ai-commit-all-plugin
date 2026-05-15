package pl.devopssolutions.aicommitall.ai

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitMessageUi
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings
import java.lang.reflect.Field
import java.time.Duration
import java.util.concurrent.CompletableFuture

@Service(Service.Level.PROJECT)
internal class AiGenerationCompletionService {
    private val observer = AiGenerationCompletionObserver()

    fun captureInitialMessage(commitMessageUi: CommitMessageUi): AiCommitMessageSnapshot =
        AiCommitMessageSnapshot.capture(commitMessageUi)

    fun awaitCompletionAsync(
        snapshot: AiCommitMessageSnapshot,
        invocation: AiCommitMessageActionInvocationResult.Invoked,
        commitMessageUi: CommitMessageUi,
        options: AiGenerationCompletionOptions = AiCommitAllSettings.getInstance().completionOptions(),
    ): CompletableFuture<AiGenerationCompletionResult> =
        CompletableFuture.supplyAsync(
            {
                observer.awaitCompletion(
                    snapshot = snapshot,
                    messageReader = CommitMessageUiReader(commitMessageUi),
                    runningSignal = ReflectiveActionProgressRunningSignal(invocation.action),
                    userEditSignal = AiGenerationUserEditSignal.NotEdited,
                    options = options,
                )
            },
            JobScheduler.getScheduler(),
        )

    companion object {
        fun getInstance(project: Project): AiGenerationCompletionService = project.service()
    }
}

internal class AiGenerationCompletionObserver(
    private val timeSource: AiCompletionTimeSource = SystemAiCompletionTimeSource,
    private val sleeper: AiCompletionSleeper = ThreadAiCompletionSleeper,
) {
    fun awaitCompletion(
        snapshot: AiCommitMessageSnapshot,
        messageReader: AiCommitMessageReader,
        runningSignal: AiGenerationRunningSignal,
        userEditSignal: AiGenerationUserEditSignal = AiGenerationUserEditSignal.NotEdited,
        options: AiGenerationCompletionOptions = AiGenerationCompletionOptions.DEFAULT,
    ): AiGenerationCompletionResult {
        val startedAtMillis = timeSource.nowMillis()

        while (timeSource.nowMillis() - startedAtMillis <= options.timeout.toMillis()) {
            val signalState = runningSignal.state()
            val currentMessage = messageReader.readMessage()
            if (userEditSignal.isUserEdited()) {
                return AiGenerationCompletionResult.UserEditedMessage(
                    latestMessage = currentMessage,
                )
            }

            when (signalState) {
                AiGenerationRunningState.Running -> Unit
                AiGenerationRunningState.NotRunning -> {
                    return completionResult(
                        snapshot = snapshot,
                        currentMessage = currentMessage,
                    )
                }
                AiGenerationRunningState.Unavailable -> {
                    return AiGenerationCompletionResult.NoCompletionSignal(
                        latestMessage = currentMessage,
                    )
                }
            }

            sleeper.sleep(options.checkInterval)
        }

        return AiGenerationCompletionResult.Timeout(
            timeout = options.timeout,
            latestMessage = messageReader.readMessage(),
        )
    }

    private fun completionResult(
        snapshot: AiCommitMessageSnapshot,
        currentMessage: String,
    ): AiGenerationCompletionResult =
        when {
            currentMessage.isBlank() -> AiGenerationCompletionResult.EmptyMessage
            currentMessage == snapshot.originalMessage -> AiGenerationCompletionResult.UnchangedMessage(currentMessage)
            else -> AiGenerationCompletionResult.Completed(
                originalMessage = snapshot.originalMessage,
                generatedMessage = currentMessage,
                evidence = AiGenerationCompletionEvidence.ActionNoLongerRunningAndMessageChanged,
            )
        }
}

internal data class AiCommitMessageSnapshot(
    val originalMessage: String,
) {
    companion object {
        fun capture(commitMessageUi: CommitMessageUi): AiCommitMessageSnapshot =
            AiCommitMessageSnapshot(commitMessageUi.text)
    }
}

internal data class AiGenerationCompletionOptions(
    val timeout: Duration,
    val checkInterval: Duration,
) {
    init {
        require(!timeout.isNegative && !timeout.isZero) { "AI generation timeout must be positive." }
        require(!checkInterval.isNegative && !checkInterval.isZero) {
            "AI generation completion-check interval must be positive."
        }
    }

    companion object {
        val DEFAULT: AiGenerationCompletionOptions = AiGenerationCompletionOptions(
            timeout = Duration.ofSeconds(5),
            checkInterval = Duration.ofMillis(500),
        )
    }
}

internal sealed interface AiGenerationCompletionResult {
    data class Completed(
        val originalMessage: String,
        val generatedMessage: String,
        val evidence: AiGenerationCompletionEvidence,
    ) : AiGenerationCompletionResult

    data class Timeout(
        val timeout: Duration,
        val latestMessage: String,
    ) : AiGenerationCompletionResult

    data object EmptyMessage : AiGenerationCompletionResult

    data class UnchangedMessage(val message: String) : AiGenerationCompletionResult

    data class NoCompletionSignal(val latestMessage: String) : AiGenerationCompletionResult

    data class UserEditedMessage(val latestMessage: String) : AiGenerationCompletionResult
}

internal enum class AiGenerationCompletionEvidence {
    ActionNoLongerRunningAndMessageChanged,
}

internal fun interface AiCommitMessageReader {
    fun readMessage(): String
}

private class CommitMessageUiReader(private val commitMessageUi: CommitMessageUi) : AiCommitMessageReader {
    override fun readMessage(): String = commitMessageUi.text
}

internal interface AiGenerationRunningSignal {
    fun state(): AiGenerationRunningState
}

internal enum class AiGenerationRunningState {
    Running,
    NotRunning,
    Unavailable,
}

internal fun interface AiGenerationUserEditSignal {
    fun isUserEdited(): Boolean

    companion object {
        val NotEdited: AiGenerationUserEditSignal = AiGenerationUserEditSignal { false }
    }
}

internal class ReflectiveActionProgressRunningSignal(private val action: AnAction) : AiGenerationRunningSignal {
    override fun state(): AiGenerationRunningState =
        runCatching {
            val progressIndicatorField = action.javaClass.findField("progressIndicator")
                ?: return AiGenerationRunningState.Unavailable
            val progressIndicator = progressIndicatorField.get(action) as? ProgressIndicator
                ?: return AiGenerationRunningState.NotRunning

            if (progressIndicator.isRunning) {
                AiGenerationRunningState.Running
            } else {
                AiGenerationRunningState.NotRunning
            }
        }.getOrDefault(AiGenerationRunningState.Unavailable)

    private fun Class<*>.findField(name: String): Field? {
        var currentClass: Class<*>? = this
        while (currentClass != null) {
            currentClass.declaredFields.firstOrNull { field -> field.name == name }?.let { field ->
                field.isAccessible = true
                return field
            }
            currentClass = currentClass.superclass
        }
        return null
    }
}

internal interface AiCompletionTimeSource {
    fun nowMillis(): Long
}

private object SystemAiCompletionTimeSource : AiCompletionTimeSource {
    override fun nowMillis(): Long = System.currentTimeMillis()
}

internal interface AiCompletionSleeper {
    fun sleep(duration: Duration)
}

private object ThreadAiCompletionSleeper : AiCompletionSleeper {
    override fun sleep(duration: Duration) {
        Thread.sleep(duration.toMillis())
    }
}
