package pl.devopssolutions.aicommitall.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AiCommitAllSettingsTest {
    @Test
    fun `uses accepted AI completion defaults`() {
        val options = AiCommitAllSettings().completionOptions()

        assertEquals(30_000, options.timeout.toMillis())
        assertEquals(500, options.checkInterval.toMillis())
        assertEquals(true, AiCommitAllSettings().clearCommitMessageBeforeGeneration())
        assertEquals(true, AiCommitAllSettings().useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `normalizes invalid persisted values to defaults`() {
        val settings = AiCommitAllSettings()

        settings.loadState(
            AiCommitAllSettings.State(
                aiGenerationTimeoutMillis = 0,
                completionCheckIntervalMillis = -1,
            ),
        )

        val options = settings.completionOptions()
        assertEquals(30_000, options.timeout.toMillis())
        assertEquals(500, options.checkInterval.toMillis())
        assertEquals(true, settings.clearCommitMessageBeforeGeneration())
        assertEquals(true, settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `normalization preserves persisted runtime toggles`() {
        val settings = AiCommitAllSettings()

        settings.loadState(
            AiCommitAllSettings.State(
                aiGenerationTimeoutMillis = 0,
                completionCheckIntervalMillis = -1,
                clearCommitMessageBeforeGeneration = false,
                useVcsShortcutsForAiCommitAll = false,
            ),
        )

        assertEquals(false, settings.clearCommitMessageBeforeGeneration())
        assertEquals(false, settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `updates positive AI completion values`() {
        val settings = AiCommitAllSettings()

        settings.updateCompletionOptions(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
        )

        val options = settings.completionOptions()
        assertEquals(12_000, options.timeout.toMillis())
        assertEquals(250, options.checkInterval.toMillis())
    }

    @Test
    fun `updates clear commit message before generation setting`() {
        val settings = AiCommitAllSettings()

        settings.updateClearCommitMessageBeforeGeneration(false)

        assertEquals(false, settings.clearCommitMessageBeforeGeneration())
    }

    @Test
    fun `updates use vcs shortcuts setting`() {
        val settings = AiCommitAllSettings()

        settings.updateUseVcsShortcutsForAiCommitAll(false)

        assertEquals(false, settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `preserves clear commit message setting when updating AI completion values`() {
        val settings = AiCommitAllSettings()
        settings.updateClearCommitMessageBeforeGeneration(false)

        settings.updateCompletionOptions(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
        )

        assertEquals(false, settings.clearCommitMessageBeforeGeneration())
    }

    @Test
    fun `preserves vcs shortcut setting when updating AI completion values`() {
        val settings = AiCommitAllSettings()
        settings.updateUseVcsShortcutsForAiCommitAll(false)

        settings.updateCompletionOptions(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
        )

        assertEquals(false, settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `rejects non-positive AI generation timeout`() {
        val settings = AiCommitAllSettings()

        assertFailsWith<IllegalArgumentException> {
            settings.updateCompletionOptions(
                timeoutMillis = 0,
                checkIntervalMillis = 500,
            )
        }
    }

    @Test
    fun `rejects non-positive completion check interval`() {
        val settings = AiCommitAllSettings()

        assertFailsWith<IllegalArgumentException> {
            settings.updateCompletionOptions(
                timeoutMillis = 5_000,
                checkIntervalMillis = 0,
            )
        }
    }
}
