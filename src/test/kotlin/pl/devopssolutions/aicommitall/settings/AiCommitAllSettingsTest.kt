package pl.devopssolutions.aicommitall.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AiCommitAllSettingsTest {
    @Test
    fun `uses accepted AI completion defaults`() {
        val options = AiCommitAllSettings().completionOptions()

        assertEquals(5_000, options.timeout.toMillis())
        assertEquals(500, options.checkInterval.toMillis())
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
        assertEquals(5_000, options.timeout.toMillis())
        assertEquals(500, options.checkInterval.toMillis())
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
