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

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitMessageUi
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings
import java.awt.KeyboardFocusManager
import java.lang.reflect.Field
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
internal class AiGenerationCompletionService {
    private val observer = AiGenerationCompletionObserver()

    fun captureInitialMessage(commitMessageUi: CommitMessageUi): AiCommitMessageSnapshot = AiCommitMessageSnapshot.capture(commitMessageUi)

    fun awaitCompletionAsync(
        snapshot: AiCommitMessageSnapshot,
        invocation: AiCommitMessageActionInvocationResult.Invoked,
        commitMessageUi: CommitMessageUi,
        options: AiGenerationCompletionOptions = AiCommitAllSettings.getInstance().completionOptions(),
    ): CompletableFuture<AiGenerationCompletionResult> {
        val userEditSignal = CommitMessageUserEditSignalFactory.create(commitMessageUi)

        return CompletableFuture.supplyAsync(
            {
                userEditSignal.use {
                    observer.awaitCompletion(
                        snapshot = snapshot,
                        messageReader = CommitMessageUiReader(commitMessageUi),
                        runningSignal = ReflectiveActionProgressRunningSignal(invocation.action),
                        userEditSignal = it,
                        options = options,
                    )
                }
            },
            JobScheduler.getScheduler(),
        )
    }

    companion object {
        fun getInstance(project: Project): AiGenerationCompletionService = project.service()
    }
}

internal class AiGenerationCompletionObserver(
    private val timeSource: AiCompletionTimeSource = SystemAiCompletionTimeSource,
    private val sleeper: AiCompletionSleeper = ThreadAiCompletionSleeper,
    private val focusState: AiCompletionFocusState = AwtAiCompletionFocusState,
) {
    fun awaitCompletion(
        snapshot: AiCommitMessageSnapshot,
        messageReader: AiCommitMessageReader,
        runningSignal: AiGenerationRunningSignal,
        userEditSignal: AiGenerationUserEditSignal = AiGenerationUserEditSignal.NotEdited,
        options: AiGenerationCompletionOptions = AiGenerationCompletionOptions.DEFAULT,
    ): AiGenerationCompletionResult {
        val startedAtMillis = timeSource.nowMillis()
        var observedRunning = false
        var stoppedWithUnusableMessageAtMillis: Long? = null
        var result: AiGenerationCompletionResult? = null

        while (result == null && timeSource.nowMillis() - startedAtMillis <= options.timeout.toMillis()) {
            val signalState = runningSignal.state()
            val currentMessage = messageReader.readMessage()
            if (userEditSignal.isUserEdited()) {
                result = AiGenerationCompletionResult.UserEditedMessage(
                    latestMessage = currentMessage,
                )
            }

            if (result == null) {
                when (signalState) {
                    AiGenerationRunningState.Running -> {
                        observedRunning = true
                        stoppedWithUnusableMessageAtMillis = null
                    }

                    AiGenerationRunningState.NotRunning -> {
                        if (shouldCompleteAfterStoppedSignal(
                                observedRunning = observedRunning,
                                currentMessage = currentMessage,
                                snapshot = snapshot,
                                stoppedWithUnusableMessageAtMillis = stoppedWithUnusableMessageAtMillis,
                                options = options,
                            )
                        ) {
                            result = completionResult(
                                snapshot = snapshot,
                                currentMessage = currentMessage,
                            )
                        } else {
                            stoppedWithUnusableMessageAtMillis = stoppedWithUnusableMessageAtMillisAfter(
                                observedRunning = observedRunning,
                                currentMessage = currentMessage,
                                snapshot = snapshot,
                                stoppedWithUnusableMessageAtMillis = stoppedWithUnusableMessageAtMillis,
                            )
                        }
                    }

                    AiGenerationRunningState.Unavailable -> {
                        result = AiGenerationCompletionResult.NoCompletionSignal(
                            latestMessage = currentMessage,
                        )
                    }
                }
            }

            if (result == null) {
                sleeper.sleep(options.checkInterval)
            }
        }

        return result ?: AiGenerationCompletionResult.Timeout(
            timeout = options.timeout,
            latestMessage = messageReader.readMessage(),
        )
    }

    private fun shouldCompleteAfterStoppedSignal(
        observedRunning: Boolean,
        currentMessage: String,
        snapshot: AiCommitMessageSnapshot,
        stoppedWithUnusableMessageAtMillis: Long?,
        options: AiGenerationCompletionOptions,
    ): Boolean {
        if (currentMessage.isUsableChangedMessage(snapshot)) {
            return true
        }

        return observedRunning &&
            isStableStoppedSignal(
                stoppedWithUnusableMessageAtMillis = stoppedWithUnusableMessageAtMillis,
                options = options,
            )
    }

    private fun String.isUsableChangedMessage(snapshot: AiCommitMessageSnapshot): Boolean = isNotBlank() && this != snapshot.originalMessage

    private fun String.isAcceptableUnchangedMessage(snapshot: AiCommitMessageSnapshot): Boolean = isNotBlank() &&
        this == snapshot.originalMessage &&
        snapshot.acceptUnchangedPrefilledMessage

    private fun isStableStoppedSignal(
        stoppedWithUnusableMessageAtMillis: Long?,
        options: AiGenerationCompletionOptions,
    ): Boolean = stoppedWithUnusableMessageAtMillis != null &&
        focusState.isApplicationFocused() &&
        timeSource.nowMillis() - stoppedWithUnusableMessageAtMillis >= options.stoppedSignalGracePeriod.toMillis()

    private fun stoppedWithUnusableMessageAtMillisAfter(
        observedRunning: Boolean,
        currentMessage: String,
        snapshot: AiCommitMessageSnapshot,
        stoppedWithUnusableMessageAtMillis: Long?,
    ): Long? {
        if (!observedRunning || currentMessage.isUsableChangedMessage(snapshot) || !focusState.isApplicationFocused()) {
            return null
        }

        return stoppedWithUnusableMessageAtMillis ?: timeSource.nowMillis()
    }

    private fun completionResult(
        snapshot: AiCommitMessageSnapshot,
        currentMessage: String,
    ): AiGenerationCompletionResult = when {
        currentMessage.isBlank() -> AiGenerationCompletionResult.EmptyMessage

        currentMessage.isAcceptableUnchangedMessage(snapshot) -> AiGenerationCompletionResult.Completed(
            originalMessage = snapshot.originalMessage,
            generatedMessage = currentMessage,
            evidence = AiGenerationCompletionEvidence.ActionNoLongerRunningAndUnchangedPrefilledMessage,
        )

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
    val acceptUnchangedPrefilledMessage: Boolean = false,
) {
    companion object {
        fun capture(
            commitMessageUi: CommitMessageUi,
            acceptUnchangedPrefilledMessage: Boolean = false,
        ): AiCommitMessageSnapshot = AiCommitMessageSnapshot(
            originalMessage = commitMessageUi.text,
            acceptUnchangedPrefilledMessage = acceptUnchangedPrefilledMessage,
        )
    }
}

internal data class AiGenerationCompletionOptions(
    val timeout: Duration,
    val checkInterval: Duration,
    val stoppedSignalGracePeriod: Duration = DEFAULT_STOPPED_SIGNAL_GRACE_PERIOD,
) {
    init {
        require(!timeout.isNegative && !timeout.isZero) { "AI generation timeout must be positive." }
        require(!checkInterval.isNegative && !checkInterval.isZero) {
            "AI generation completion-check interval must be positive."
        }
        require(!stoppedSignalGracePeriod.isNegative) {
            "AI generation stopped-signal grace period must not be negative."
        }
    }

    companion object {
        private val DEFAULT_STOPPED_SIGNAL_GRACE_PERIOD: Duration = Duration.ofSeconds(2)
        val DEFAULT: AiGenerationCompletionOptions = AiGenerationCompletionOptions(
            timeout = Duration.ofSeconds(30),
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
    ActionNoLongerRunningAndUnchangedPrefilledMessage,
}

internal fun interface AiCommitMessageReader {
    fun readMessage(): String
}

internal class CommitMessageUiReader(
    private val commitMessageUi: CommitMessageUi,
    private val textAccess: CommitMessageUiTextAccess = EdtCommitMessageUiTextAccess,
) : AiCommitMessageReader {
    override fun readMessage(): String = textAccess.readText { commitMessageUi.text }
}

internal fun interface CommitMessageUiTextAccess {
    fun readText(readNow: () -> String): String
}

private object EdtCommitMessageUiTextAccess : CommitMessageUiTextAccess {
    override fun readText(readNow: () -> String): String = ApplicationManager.getApplication()
        ?.takeUnless { application -> application.isDispatchThread }
        ?.let { application -> readTextOnEdt(application, readNow) }
        ?: readNow()

    private fun readTextOnEdt(
        application: com.intellij.openapi.application.Application,
        readNow: () -> String,
    ): String {
        val result = AtomicReference<String>()
        application.invokeAndWait {
            result.set(readNow())
        }
        return result.get()
    }
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
    override fun state(): AiGenerationRunningState = runCatching {
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

internal fun interface AiCompletionFocusState {
    fun isApplicationFocused(): Boolean

    companion object {
        val Focused: AiCompletionFocusState = AiCompletionFocusState { true }
    }
}

private object AwtAiCompletionFocusState : AiCompletionFocusState {
    override fun isApplicationFocused(): Boolean = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow != null
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
