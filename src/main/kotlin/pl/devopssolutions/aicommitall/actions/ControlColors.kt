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

import com.intellij.ui.JBColor
import java.awt.Color

internal object ControlColors {
    val border = color(ControlColorDefaults.BORDER_LIGHT, ControlColorDefaults.BORDER_DARK)
    val activeDivider = translucentColor(
        lightRgb = ControlColorDefaults.ACTIVE_DIVIDER_LIGHT,
        lightAlpha = ControlColorDefaults.ACTIVE_DIVIDER_LIGHT_ALPHA,
        darkRgb = ControlColorDefaults.ACTIVE_DIVIDER_DARK,
        darkAlpha = ControlColorDefaults.ACTIVE_DIVIDER_DARK_ALPHA,
    )
    val activePassiveDivider = translucentColor(
        lightRgb = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_LIGHT,
        lightAlpha = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_LIGHT_ALPHA,
        darkRgb = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_DARK,
        darkAlpha = ControlColorDefaults.ACTIVE_PASSIVE_DIVIDER_DARK_ALPHA,
    )
    val passiveDivider = translucentColor(
        lightRgb = ControlColorDefaults.PASSIVE_DIVIDER_LIGHT,
        lightAlpha = ControlColorDefaults.PASSIVE_DIVIDER_LIGHT_ALPHA,
        darkRgb = ControlColorDefaults.PASSIVE_DIVIDER_DARK,
        darkAlpha = ControlColorDefaults.PASSIVE_DIVIDER_DARK_ALPHA,
    )
    val disabledDivider = color(ControlColorDefaults.DISABLED_DIVIDER_LIGHT, ControlColorDefaults.DISABLED_DIVIDER_DARK)
    val activeForeground = JBColor(Color.WHITE, Color.WHITE)
    val disabledForeground = color(
        ControlColorDefaults.DISABLED_FOREGROUND_LIGHT,
        ControlColorDefaults.DISABLED_FOREGROUND_DARK,
    )
    val aiCommitSnake = color(ControlColorDefaults.AI_COMMIT_SNAKE_LIGHT, ControlColorDefaults.AI_COMMIT_SNAKE_DARK)
    val pushSnake = color(ControlColorDefaults.PUSH_SNAKE_LIGHT, ControlColorDefaults.PUSH_SNAKE_DARK)
    val pushIconHighlighted = color(
        ControlColorDefaults.PUSH_ICON_HIGHLIGHTED_LIGHT,
        ControlColorDefaults.PUSH_ICON_HIGHLIGHTED_DARK,
    )

    fun sectionFill(
        state: AiCommitAllControlState,
        section: AiCommitAllControlSection,
        highlighted: Boolean,
    ): Color = if (!state.isSectionEnabled(section) && state.runningSection == null) {
        disabledFill(section)
    } else if (highlighted) {
        activeFill(section)
    } else {
        passiveFill(section)
    }

    fun sectionForeground(
        state: AiCommitAllControlState,
        section: AiCommitAllControlSection,
        highlighted: Boolean,
    ): Color = if (!state.isSectionEnabled(section) && state.runningSection == null) {
        disabledForeground
    } else if (highlighted) {
        activeForeground
    } else {
        passiveForeground(section)
    }

    fun divider(
        state: AiCommitAllControlState,
        leftSection: AiCommitAllControlSection,
        rightSection: AiCommitAllControlSection,
        highlighted: Set<AiCommitAllControlSection>,
    ): Color {
        if (state.runningSection == null &&
            (!state.isSectionEnabled(leftSection) || !state.isSectionEnabled(rightSection))
        ) {
            return disabledDivider
        }

        val leftHighlighted = highlighted.contains(leftSection)
        val rightHighlighted = highlighted.contains(rightSection)
        return when {
            leftHighlighted && rightHighlighted -> activeDivider
            leftHighlighted || rightHighlighted -> activePassiveDivider
            else -> passiveDivider
        }
    }

    private fun passiveFill(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Ai -> color(
            ControlColorDefaults.AI_PASSIVE_FILL_LIGHT,
            ControlColorDefaults.AI_PASSIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Commit -> color(
            ControlColorDefaults.COMMIT_PASSIVE_FILL_LIGHT,
            ControlColorDefaults.COMMIT_PASSIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_PASSIVE_FILL_LIGHT,
            ControlColorDefaults.PUSH_PASSIVE_FILL_DARK,
        )
    }

    private fun activeFill(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Ai -> color(
            ControlColorDefaults.AI_ACTIVE_FILL_LIGHT,
            ControlColorDefaults.AI_ACTIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Commit -> color(
            ControlColorDefaults.COMMIT_ACTIVE_FILL_LIGHT,
            ControlColorDefaults.COMMIT_ACTIVE_FILL_DARK,
        )

        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_ACTIVE_FILL_LIGHT,
            ControlColorDefaults.PUSH_ACTIVE_FILL_DARK,
        )
    }

    private fun disabledFill(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_DISABLED_FILL_LIGHT,
            ControlColorDefaults.PUSH_DISABLED_FILL_DARK,
        )

        else -> color(
            ControlColorDefaults.DEFAULT_DISABLED_FILL_LIGHT,
            ControlColorDefaults.DEFAULT_DISABLED_FILL_DARK,
        )
    }

    private fun passiveForeground(section: AiCommitAllControlSection): Color = when (section) {
        AiCommitAllControlSection.Ai -> color(
            ControlColorDefaults.AI_PASSIVE_FOREGROUND_LIGHT,
            ControlColorDefaults.AI_PASSIVE_FOREGROUND_DARK,
        )

        AiCommitAllControlSection.Commit -> color(
            ControlColorDefaults.COMMIT_PASSIVE_FOREGROUND_LIGHT,
            ControlColorDefaults.COMMIT_PASSIVE_FOREGROUND_DARK,
        )

        AiCommitAllControlSection.Push -> color(
            ControlColorDefaults.PUSH_PASSIVE_FOREGROUND_LIGHT,
            ControlColorDefaults.PUSH_PASSIVE_FOREGROUND_DARK,
        )
    }

    private fun color(
        lightRgb: Int,
        darkRgb: Int,
    ): Color = JBColor(Color(lightRgb), Color(darkRgb))

    private fun translucentColor(
        lightRgb: Int,
        lightAlpha: Int,
        darkRgb: Int,
        darkAlpha: Int,
    ): Color = JBColor(
        Color((lightAlpha shl COLOR_ALPHA_SHIFT) or lightRgb, true),
        Color((darkAlpha shl COLOR_ALPHA_SHIFT) or darkRgb, true),
    )
}

private object ControlColorDefaults {
    const val BORDER_LIGHT = 0xD1D5DB
    const val BORDER_DARK = 0x4B5563
    const val ACTIVE_DIVIDER_LIGHT = 0xE9F0FF
    const val ACTIVE_DIVIDER_LIGHT_ALPHA = 110
    const val ACTIVE_DIVIDER_DARK = 0xE9F0FF
    const val ACTIVE_DIVIDER_DARK_ALPHA = 90
    const val ACTIVE_PASSIVE_DIVIDER_LIGHT = 0xDDE8FF
    const val ACTIVE_PASSIVE_DIVIDER_LIGHT_ALPHA = 150
    const val ACTIVE_PASSIVE_DIVIDER_DARK = 0xDBEAFE
    const val ACTIVE_PASSIVE_DIVIDER_DARK_ALPHA = 90
    const val PASSIVE_DIVIDER_LIGHT = 0xCBD5E1
    const val PASSIVE_DIVIDER_LIGHT_ALPHA = 190
    const val PASSIVE_DIVIDER_DARK = 0x4B5563
    const val PASSIVE_DIVIDER_DARK_ALPHA = 190
    const val DISABLED_DIVIDER_LIGHT = 0xC3CBD8
    const val DISABLED_DIVIDER_DARK = 0x4B5563
    const val DISABLED_FOREGROUND_LIGHT = 0x6B7280
    const val DISABLED_FOREGROUND_DARK = 0x8792A1
    const val AI_COMMIT_SNAKE_LIGHT = 0xD9EAFF
    const val AI_COMMIT_SNAKE_DARK = 0xD9EAFF
    const val PUSH_SNAKE_LIGHT = 0xD9FFE3
    const val PUSH_SNAKE_DARK = 0xD9FFE3
    const val PUSH_ICON_HIGHLIGHTED_LIGHT = 0xD9FFE3
    const val PUSH_ICON_HIGHLIGHTED_DARK = 0xD9FFE3
    const val AI_PASSIVE_FILL_LIGHT = 0xF0E9FF
    const val AI_PASSIVE_FILL_DARK = 0x342A47
    const val COMMIT_PASSIVE_FILL_LIGHT = 0xEAF1FF
    const val COMMIT_PASSIVE_FILL_DARK = 0x28394E
    const val PUSH_PASSIVE_FILL_LIGHT = 0xE8F5EC
    const val PUSH_PASSIVE_FILL_DARK = 0x263C32
    const val AI_ACTIVE_FILL_LIGHT = 0x834DF0
    const val AI_ACTIVE_FILL_DARK = 0xA571E6
    const val COMMIT_ACTIVE_FILL_LIGHT = 0x315FAE
    const val COMMIT_ACTIVE_FILL_DARK = 0x2F5AA0
    const val PUSH_ACTIVE_FILL_LIGHT = 0x238449
    const val PUSH_ACTIVE_FILL_DARK = 0x2E9D50
    const val PUSH_DISABLED_FILL_LIGHT = 0xDCEAE2
    const val PUSH_DISABLED_FILL_DARK = 0x2F3A35
    const val DEFAULT_DISABLED_FILL_LIGHT = 0xE4E8F0
    const val DEFAULT_DISABLED_FILL_DARK = 0x303641
    const val AI_PASSIVE_FOREGROUND_LIGHT = 0x6F4BB8
    const val AI_PASSIVE_FOREGROUND_DARK = 0xB99BE8
    const val COMMIT_PASSIVE_FOREGROUND_LIGHT = 0x315FAE
    const val COMMIT_PASSIVE_FOREGROUND_DARK = 0x9DB6E3
    const val PUSH_PASSIVE_FOREGROUND_LIGHT = 0x238449
    const val PUSH_PASSIVE_FOREGROUND_DARK = 0x8AF0A1
}

private const val COLOR_ALPHA_SHIFT = 24
