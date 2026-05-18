package pl.devopssolutions.aicommitall.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionOptions
import java.time.Duration
import com.intellij.openapi.components.State as PersistentState

@Service(Service.Level.APP)
@PersistentState(
    name = "AiCommitAllSettings",
    storages = [Storage("aiCommitAll.xml")],
)
internal class AiCommitAllSettings : PersistentStateComponent<AiCommitAllSettings.State> {
    private var settingsState = State()

    override fun getState(): State = settingsState

    override fun loadState(state: State) {
        settingsState = state.normalized()
    }

    fun completionOptions(): AiGenerationCompletionOptions =
        settingsState.normalized().toCompletionOptions()

    fun clearCommitMessageBeforeGeneration(): Boolean =
        settingsState.normalized().clearCommitMessageBeforeGeneration

    fun useVcsShortcutsForAiCommitAll(): Boolean =
        settingsState.normalized().useVcsShortcutsForAiCommitAll

    fun updateCompletionOptions(timeoutMillis: Long, checkIntervalMillis: Long) {
        require(timeoutMillis > 0) { "AI generation timeout must be positive." }
        require(checkIntervalMillis > 0) { "AI generation completion-check interval must be positive." }
        settingsState = settingsState.normalized().copy(
            aiGenerationTimeoutMillis = timeoutMillis,
            completionCheckIntervalMillis = checkIntervalMillis,
        )
    }

    fun updateClearCommitMessageBeforeGeneration(enabled: Boolean) {
        settingsState = settingsState.normalized().copy(
            clearCommitMessageBeforeGeneration = enabled,
        )
    }

    fun updateUseVcsShortcutsForAiCommitAll(enabled: Boolean) {
        settingsState = settingsState.normalized().copy(
            useVcsShortcutsForAiCommitAll = enabled,
        )
    }

    data class State(
        var aiGenerationTimeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
        var completionCheckIntervalMillis: Long = DEFAULT_CHECK_INTERVAL_MILLIS,
        var clearCommitMessageBeforeGeneration: Boolean = DEFAULT_CLEAR_COMMIT_MESSAGE_BEFORE_GENERATION,
        var useVcsShortcutsForAiCommitAll: Boolean = DEFAULT_USE_VCS_SHORTCUTS_FOR_AI_COMMIT_ALL,
    ) {
        fun normalized(): State =
            State(
                aiGenerationTimeoutMillis = aiGenerationTimeoutMillis.takeIf { it > 0 } ?: DEFAULT_TIMEOUT_MILLIS,
                completionCheckIntervalMillis = completionCheckIntervalMillis.takeIf { it > 0 }
                    ?: DEFAULT_CHECK_INTERVAL_MILLIS,
                clearCommitMessageBeforeGeneration = clearCommitMessageBeforeGeneration,
                useVcsShortcutsForAiCommitAll = useVcsShortcutsForAiCommitAll,
            )

        fun toCompletionOptions(): AiGenerationCompletionOptions =
            AiGenerationCompletionOptions(
                timeout = Duration.ofMillis(aiGenerationTimeoutMillis),
                checkInterval = Duration.ofMillis(completionCheckIntervalMillis),
            )
    }

    companion object {
        private val defaults = AiGenerationCompletionOptions.DEFAULT
        const val SETTINGS_ID: String = "pl.devopssolutions.aicommitall.settings"
        const val DISPLAY_NAME: String = "AI Commit All"
        val DEFAULT_TIMEOUT_MILLIS: Long = defaults.timeout.toMillis()
        val DEFAULT_CHECK_INTERVAL_MILLIS: Long = defaults.checkInterval.toMillis()
        const val DEFAULT_CLEAR_COMMIT_MESSAGE_BEFORE_GENERATION: Boolean = true
        const val DEFAULT_USE_VCS_SHORTCUTS_FOR_AI_COMMIT_ALL: Boolean = true

        fun getInstance(): AiCommitAllSettings = service()
    }
}
