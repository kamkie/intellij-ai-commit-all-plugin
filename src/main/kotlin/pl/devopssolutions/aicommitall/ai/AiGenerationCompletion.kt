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

        while (timeSource.nowMillis() - startedAtMillis <= options.timeout.toMillis()) {
            val signalState = runningSignal.state()
            val currentMessage = messageReader.readMessage()
            if (userEditSignal.isUserEdited()) {
                return AiGenerationCompletionResult.UserEditedMessage(
                    latestMessage = currentMessage,
                )
            }

            when (signalState) {
                AiGenerationRunningState.Running -> {
                    observedRunning = true
                }

                AiGenerationRunningState.NotRunning -> {
                    if (observedRunning || currentMessage.isUsableChangedMessage(snapshot)) {
                        return completionResult(
                            snapshot = snapshot,
                            currentMessage = currentMessage,
                        )
                    }
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

    private fun String.isUsableChangedMessage(snapshot: AiCommitMessageSnapshot): Boolean = isNotBlank() && this != snapshot.originalMessage

    private fun completionResult(
        snapshot: AiCommitMessageSnapshot,
        currentMessage: String,
    ): AiGenerationCompletionResult = when {
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
        fun capture(commitMessageUi: CommitMessageUi): AiCommitMessageSnapshot = AiCommitMessageSnapshot(commitMessageUi.text)
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
    override fun readText(readNow: () -> String): String {
        val application = ApplicationManager.getApplication() ?: return readNow()
        if (application.isDispatchThread) {
            return readNow()
        }

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
