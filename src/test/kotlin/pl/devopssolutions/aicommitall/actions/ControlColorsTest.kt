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
package pl.devopssolutions.aicommitall.actions

import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

internal class ControlColorsTest {
    @Test
    fun `section colors cover enabled highlighted and disabled states`() {
        val enabledState = state()
        val disabledPushState = state(
            AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
        )
        val runningState = state(
            AiCommitAllControlSection.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            runningSection = AiCommitAllControlSection.Commit,
        )

        AiCommitAllControlSection.entries.forEach { section ->
            assertNotEquals(
                ControlColors.sectionFill(enabledState, section, highlighted = false).rgb,
                ControlColors.sectionFill(enabledState, section, highlighted = true).rgb,
            )
            assertNotEquals(
                ControlColors.sectionForeground(enabledState, section, highlighted = false).rgb,
                ControlColors.sectionForeground(enabledState, section, highlighted = true).rgb,
            )
        }

        assertNotEquals(
            ControlColors.sectionFill(disabledPushState, AiCommitAllControlSection.Push, highlighted = false).rgb,
            ControlColors.sectionFill(runningState, AiCommitAllControlSection.Push, highlighted = false).rgb,
        )
        assertSame(
            ControlColors.disabledForeground,
            ControlColors.sectionForeground(disabledPushState, AiCommitAllControlSection.Push, highlighted = false),
        )
    }

    @Test
    fun `divider colors cover disabled active passive and mixed highlights`() {
        val enabledState = state()
        val disabledCommitState = state(
            AiCommitAllControlSection.Commit to AiCommitAllWorkflowActionAvailability.Disabled,
        )

        assertSame(
            ControlColors.disabledDivider,
            ControlColors.divider(
                state = disabledCommitState,
                leftSection = AiCommitAllControlSection.Ai,
                rightSection = AiCommitAllControlSection.Commit,
                highlighted = emptySet(),
            ),
        )
        assertSame(
            ControlColors.activeDivider,
            ControlColors.divider(
                state = enabledState,
                leftSection = AiCommitAllControlSection.Ai,
                rightSection = AiCommitAllControlSection.Commit,
                highlighted = setOf(AiCommitAllControlSection.Ai, AiCommitAllControlSection.Commit),
            ),
        )
        assertSame(
            ControlColors.activePassiveDivider,
            ControlColors.divider(
                state = enabledState,
                leftSection = AiCommitAllControlSection.Commit,
                rightSection = AiCommitAllControlSection.Push,
                highlighted = setOf(AiCommitAllControlSection.Commit),
            ),
        )
        assertSame(
            ControlColors.passiveDivider,
            ControlColors.divider(
                state = enabledState,
                leftSection = AiCommitAllControlSection.Commit,
                rightSection = AiCommitAllControlSection.Push,
                highlighted = emptySet(),
            ),
        )
    }

    @Test
    fun `named chrome and icon colors are initialized`() {
        val colors = listOf(
            ControlColors.border,
            ControlColors.aiCommitSnake,
            ControlColors.pushSnake,
            ControlColors.pushIconHighlighted,
        )

        assertEquals(colors.size, colors.filterIsInstance<Color>().size)
    }

    private fun state(
        vararg overrides: Pair<AiCommitAllControlSection, AiCommitAllWorkflowActionAvailability>,
        runningSection: AiCommitAllControlSection? = null,
    ): AiCommitAllControlState {
        val overrideMap = overrides.toMap()
        return AiCommitAllControlState(
            sections = AiCommitAllControlSection.entries.associateWith { section ->
                overrideMap[section] ?: AiCommitAllWorkflowActionAvailability.Enabled
            },
            runningSection = runningSection,
        )
    }
}
