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
import com.intellij.openapi.options.SearchableConfigurable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

internal class AiCommitAllConfigurable(
    private val settings: AiCommitAllSettings = AiCommitAllSettings.getInstance(),
) : SearchableConfigurable {
    private var panel: JPanel? = null
    private var timeoutSpinner: JSpinner? = null
    private var checkIntervalSpinner: JSpinner? = null
    private var clearCommitMessageCheckBox: JCheckBox? = null
    private var useVcsShortcutsCheckBox: JCheckBox? = null

    override fun getId(): String = AiCommitAllSettings.SETTINGS_ID

    override fun getDisplayName(): String = AiCommitAllSettings.DISPLAY_NAME

    override fun createComponent(): JComponent {
        val completionOptions = settings.completionOptions()
        timeoutSpinner = millisSpinner(completionOptions.timeout.toMillis())
        checkIntervalSpinner = millisSpinner(completionOptions.checkInterval.toMillis())
        clearCommitMessageCheckBox = JCheckBox(
            "Clear commit message before AI generation",
            settings.clearCommitMessageBeforeGeneration(),
        )
        useVcsShortcutsCheckBox = JCheckBox(
            "Use AI Commit All for IDE commit and push shortcuts",
            settings.useVcsShortcutsForAiCommitAll(),
        )

        panel = JPanel(GridBagLayout()).apply {
            add(
                requireNotNull(useVcsShortcutsCheckBox),
                constraints(row = 0, column = 0, width = 2),
            )
            add(
                requireNotNull(clearCommitMessageCheckBox),
                constraints(row = 1, column = 0, width = 2),
            )
            add(
                JLabel("AI generation timeout (ms)"),
                constraints(row = 2, column = 0),
            )
            add(
                requireNotNull(timeoutSpinner),
                constraints(row = 2, column = 1),
            )
            add(
                JLabel("Completion check interval (ms)"),
                constraints(row = 3, column = 0),
            )
            add(
                requireNotNull(checkIntervalSpinner),
                constraints(row = 3, column = 1),
            )
        }

        return requireNotNull(panel)
    }

    override fun isModified(): Boolean {
        val timeoutMillis = timeoutSpinner?.longValue()
        val checkIntervalMillis = checkIntervalSpinner?.longValue()
        val clearCommitMessageSelected = clearCommitMessageCheckBox?.isSelected
        val useVcsShortcutsSelected = useVcsShortcutsCheckBox?.isSelected
        val completionOptions = settings.completionOptions()
        return timeoutMillis != null &&
            checkIntervalMillis != null &&
            clearCommitMessageSelected != null &&
            useVcsShortcutsSelected != null &&
            (
                timeoutMillis != completionOptions.timeout.toMillis() ||
                    checkIntervalMillis != completionOptions.checkInterval.toMillis() ||
                    clearCommitMessageSelected != settings.clearCommitMessageBeforeGeneration() ||
                    useVcsShortcutsSelected != settings.useVcsShortcutsForAiCommitAll()
                )
    }

    override fun apply() {
        val timeoutMillis = timeoutSpinner?.longValue()
        val checkIntervalMillis = checkIntervalSpinner?.longValue()

        if (timeoutMillis != null && checkIntervalMillis != null) {
            if (timeoutMillis <= 0) {
                throw ConfigurationException("AI generation timeout must be positive.")
            }
            if (checkIntervalMillis <= 0) {
                throw ConfigurationException("Completion check interval must be positive.")
            }

            settings.updateCompletionOptions(
                timeoutMillis = timeoutMillis,
                checkIntervalMillis = checkIntervalMillis,
            )

            clearCommitMessageCheckBox?.isSelected?.let { clearCommitMessageBeforeGeneration ->
                settings.updateClearCommitMessageBeforeGeneration(
                    enabled = clearCommitMessageBeforeGeneration,
                )
                useVcsShortcutsCheckBox?.isSelected?.let { useVcsShortcutsForAiCommitAll ->
                    settings.updateUseVcsShortcutsForAiCommitAll(
                        enabled = useVcsShortcutsForAiCommitAll,
                    )
                }
            }
        }
    }

    override fun reset() {
        val completionOptions = settings.completionOptions()
        timeoutSpinner?.value = completionOptions.timeout.toMillis()
        checkIntervalSpinner?.value = completionOptions.checkInterval.toMillis()
        clearCommitMessageCheckBox?.isSelected = settings.clearCommitMessageBeforeGeneration()
        useVcsShortcutsCheckBox?.isSelected = settings.useVcsShortcutsForAiCommitAll()
    }

    override fun disposeUIResources() {
        panel = null
        timeoutSpinner = null
        checkIntervalSpinner = null
        clearCommitMessageCheckBox = null
        useVcsShortcutsCheckBox = null
    }

    private fun millisSpinner(value: Long): JSpinner = JSpinner(
        SpinnerNumberModel(
            value,
            MIN_MILLIS,
            MAX_MILLIS,
            STEP_MILLIS,
        ),
    )

    private fun JSpinner.longValue(): Long = (value as Number).toLong()

    private fun constraints(row: Int, column: Int, width: Int = 1): GridBagConstraints = GridBagConstraints().apply {
        gridx = column
        gridy = row
        gridwidth = width
        anchor = GridBagConstraints.WEST
        fill = if (column == 1 || width > 1) GridBagConstraints.HORIZONTAL else GridBagConstraints.NONE
        weightx = if (column == 1 || width > 1) 1.0 else 0.0
        insets = Insets(CONTROL_INSET, CONTROL_INSET, CONTROL_INSET, CONTROL_INSET)
    }

    companion object {
        private const val MIN_MILLIS = 1L
        private const val MAX_MILLIS = Int.MAX_VALUE.toLong()
        private const val STEP_MILLIS = 100L
        private const val CONTROL_INSET = 4
    }
}
