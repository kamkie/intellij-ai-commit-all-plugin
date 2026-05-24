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

internal class ThreeSectionControlModel {
    var state: AiCommitAllControlState = AiCommitAllControlState.Hidden
    var hoverSection: AiCommitAllControlSection? = null
    var keyboardSection: AiCommitAllControlSection = AiCommitAllControlSection.Commit
    var snakeOffset: Float = 0f

    fun updateState(nextState: AiCommitAllControlState) {
        state = nextState
        val currentHover = hoverSection
        if (currentHover != null && !state.isSectionEnabled(currentHover)) {
            hoverSection = null
        }
        if (!state.isSectionEnabled(keyboardSection)) {
            keyboardSection = firstEnabledSection() ?: AiCommitAllControlSection.Commit
        }
    }

    fun highlightedSections(): Set<AiCommitAllControlSection> {
        val activeSection = state.runningSection ?: hoverSection ?: return emptySet()
        return controlSections
            .filter { section -> section.ordinal <= activeSection.ordinal }
            .toSet()
    }

    fun accessibleDescription(): String {
        state.runningSection?.let { section ->
            return "AI, Commit, and Push sections; ${section.label} is running"
        }
        val enabledSections = controlSections
            .filter { section -> state.isSectionEnabled(section) }
            .joinToString(", ") { section -> section.label }
        return if (enabledSections.isBlank()) {
            "AI, Commit, and Push sections; no sections are enabled"
        } else {
            "AI, Commit, and Push sections"
        }
    }

    private fun firstEnabledSection(): AiCommitAllControlSection? = controlSections.firstOrNull(state::isSectionEnabled)
}

internal val controlSections = AiCommitAllControlSection.entries

internal fun Int.floorMod(other: Int): Int = ((this % other) + other) % other

internal val AiCommitAllControlSection.toolTipText: String
    get() = when (this) {
        AiCommitAllControlSection.Ai -> "Generate an AI commit message for all Git changes."
        AiCommitAllControlSection.Commit -> "Generate an AI commit message and commit all Git changes."
        AiCommitAllControlSection.Push -> "Generate an AI commit message, commit all Git changes, and push."
    }
