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
package pl.devopssolutions.aicommitall.settings

import com.intellij.openapi.options.ConfigurationException
import java.awt.Container
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JSpinner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AiCommitAllConfigurableTest {
    @Test
    fun `createComponent renders current settings`() {
        val settings = configuredSettings(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
            clearCommitMessageBeforeGeneration = false,
            useVcsShortcutsForAiCommitAll = false,
        )
        val component = AiCommitAllConfigurable(settings).createComponent()
        val ui = component.settingsUi()

        assertEquals(12_000, ui.timeoutMillis)
        assertEquals(250, ui.checkIntervalMillis)
        assertFalse(ui.clearCommitMessageBeforeGeneration)
        assertFalse(ui.useVcsShortcutsForAiCommitAll)
    }

    @Test
    fun `isModified tracks each configurable setting`() {
        val settings = configuredSettings(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
            clearCommitMessageBeforeGeneration = false,
            useVcsShortcutsForAiCommitAll = false,
        )
        val configurable = AiCommitAllConfigurable(settings)
        val ui = configurable.createComponent().settingsUi()

        assertFalse(configurable.isModified())

        val mutations = mapOf<String, SettingsUi.() -> Unit>(
            "timeout" to { timeoutMillis = 13_000 },
            "check interval" to { checkIntervalMillis = 500 },
            "clear commit message toggle" to { clearCommitMessageBeforeGeneration = true },
            "VCS shortcut toggle" to { useVcsShortcutsForAiCommitAll = true },
        )

        mutations.forEach { (settingName, mutate) ->
            configurable.reset()

            ui.mutate()

            assertTrue(
                configurable.isModified(),
                "$settingName change should make configurable modified.",
            )
        }
    }

    @Test
    fun `isModified is false when settings ui is unavailable`() {
        val settings = configuredSettings(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
            clearCommitMessageBeforeGeneration = false,
            useVcsShortcutsForAiCommitAll = false,
        )
        val configurable = AiCommitAllConfigurable(settings)

        assertFalse(configurable.isModified())

        configurable.createComponent()
        assertFalse(configurable.isModified())

        configurable.disposeUIResources()
        assertFalse(configurable.isModified())
    }

    @Test
    fun `apply persists every setting field`() {
        val settings = AiCommitAllSettings()
        val configurable = AiCommitAllConfigurable(settings)
        val ui = configurable.createComponent().settingsUi()

        ui.timeoutMillis = 45_000
        ui.checkIntervalMillis = 750
        ui.clearCommitMessageBeforeGeneration = false
        ui.useVcsShortcutsForAiCommitAll = false

        configurable.apply()

        val completionOptions = settings.completionOptions()
        assertEquals(45_000, completionOptions.timeout.toMillis())
        assertEquals(750, completionOptions.checkInterval.toMillis())
        assertFalse(settings.clearCommitMessageBeforeGeneration())
        assertFalse(settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `apply rejects non-positive timeout without persisting changes`() {
        val settings = AiCommitAllSettings()
        val configurable = AiCommitAllConfigurable(settings)
        val ui = configurable.createComponent().settingsUi()

        ui.timeoutMillis = 0
        ui.checkIntervalMillis = 750
        ui.clearCommitMessageBeforeGeneration = false
        ui.useVcsShortcutsForAiCommitAll = false

        assertFailsWith<ConfigurationException> {
            configurable.apply()
        }

        assertEquals(
            AiCommitAllSettings.DEFAULT_TIMEOUT_MILLIS,
            settings.completionOptions().timeout.toMillis(),
        )
        assertEquals(
            AiCommitAllSettings.DEFAULT_CHECK_INTERVAL_MILLIS,
            settings.completionOptions().checkInterval.toMillis(),
        )
        assertTrue(settings.clearCommitMessageBeforeGeneration())
        assertTrue(settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `apply rejects non-positive check interval without persisting changes`() {
        val settings = AiCommitAllSettings()
        val configurable = AiCommitAllConfigurable(settings)
        val ui = configurable.createComponent().settingsUi()

        ui.timeoutMillis = 45_000
        ui.checkIntervalMillis = -1
        ui.clearCommitMessageBeforeGeneration = false
        ui.useVcsShortcutsForAiCommitAll = false

        assertFailsWith<ConfigurationException> {
            configurable.apply()
        }

        assertEquals(
            AiCommitAllSettings.DEFAULT_TIMEOUT_MILLIS,
            settings.completionOptions().timeout.toMillis(),
        )
        assertEquals(
            AiCommitAllSettings.DEFAULT_CHECK_INTERVAL_MILLIS,
            settings.completionOptions().checkInterval.toMillis(),
        )
        assertTrue(settings.clearCommitMessageBeforeGeneration())
        assertTrue(settings.useVcsShortcutsForAiCommitAll())
    }

    @Test
    fun `reset restores every setting field`() {
        val settings = configuredSettings(
            timeoutMillis = 12_000,
            checkIntervalMillis = 250,
            clearCommitMessageBeforeGeneration = false,
            useVcsShortcutsForAiCommitAll = false,
        )
        val configurable = AiCommitAllConfigurable(settings)
        val ui = configurable.createComponent().settingsUi()

        ui.timeoutMillis = 1
        ui.checkIntervalMillis = 1
        ui.clearCommitMessageBeforeGeneration = true
        ui.useVcsShortcutsForAiCommitAll = true

        settings.updateCompletionOptions(
            timeoutMillis = 45_000,
            checkIntervalMillis = 750,
        )
        settings.updateClearCommitMessageBeforeGeneration(true)
        settings.updateUseVcsShortcutsForAiCommitAll(true)
        configurable.reset()

        assertEquals(45_000, ui.timeoutMillis)
        assertEquals(750, ui.checkIntervalMillis)
        assertTrue(ui.clearCommitMessageBeforeGeneration)
        assertTrue(ui.useVcsShortcutsForAiCommitAll)
    }

    private data class SettingsUi(
        private val timeoutSpinner: JSpinner,
        private val checkIntervalSpinner: JSpinner,
        private val clearCommitMessageCheckBox: JCheckBox,
        private val useVcsShortcutsCheckBox: JCheckBox,
    ) {
        var timeoutMillis: Long
            get() = (timeoutSpinner.value as Number).toLong()
            set(value) {
                timeoutSpinner.value = value
            }

        var checkIntervalMillis: Long
            get() = (checkIntervalSpinner.value as Number).toLong()
            set(value) {
                checkIntervalSpinner.value = value
            }

        var clearCommitMessageBeforeGeneration: Boolean
            get() = clearCommitMessageCheckBox.isSelected
            set(value) {
                clearCommitMessageCheckBox.isSelected = value
            }

        var useVcsShortcutsForAiCommitAll: Boolean
            get() = useVcsShortcutsCheckBox.isSelected
            set(value) {
                useVcsShortcutsCheckBox.isSelected = value
            }
    }

    private fun configuredSettings(
        timeoutMillis: Long,
        checkIntervalMillis: Long,
        clearCommitMessageBeforeGeneration: Boolean,
        useVcsShortcutsForAiCommitAll: Boolean,
    ): AiCommitAllSettings = AiCommitAllSettings().apply {
        updateCompletionOptions(timeoutMillis, checkIntervalMillis)
        updateClearCommitMessageBeforeGeneration(clearCommitMessageBeforeGeneration)
        updateUseVcsShortcutsForAiCommitAll(useVcsShortcutsForAiCommitAll)
    }

    private fun JComponent.settingsUi(): SettingsUi = SettingsUi(
        timeoutSpinner = spinnerForLabel("AI generation timeout (ms)"),
        checkIntervalSpinner = spinnerForLabel("Completion check interval (ms)"),
        clearCommitMessageCheckBox = checkBox("Clear commit message before AI generation"),
        useVcsShortcutsCheckBox = checkBox("Use AI Commit All for IDE commit and push shortcuts"),
    )

    private fun Container.spinnerForLabel(labelText: String): JSpinner {
        val components = descendants().toList()
        val labelIndex = components.indexOfFirst { component ->
            component is JLabel && component.text == labelText
        }

        require(labelIndex >= 0) { "Label `$labelText` was not found." }

        return components.drop(labelIndex + 1)
            .filterIsInstance<JSpinner>()
            .firstOrNull()
            ?: error("Spinner for label `$labelText` was not found.")
    }

    private fun Container.checkBox(text: String): JCheckBox = descendants()
        .filterIsInstance<JCheckBox>()
        .firstOrNull { checkBox -> checkBox.text == text }
        ?: error("Check box `$text` was not found.")

    private fun Container.descendants(): Sequence<java.awt.Component> = sequence {
        for (component in components) {
            yield(component)
            if (component is Container) {
                yieldAll(component.descendants())
            }
        }
    }
}
