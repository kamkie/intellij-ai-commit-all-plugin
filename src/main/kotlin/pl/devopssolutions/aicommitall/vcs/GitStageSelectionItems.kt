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
package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker

internal object GitStageSelectionItems {
    fun committablePaths(
        state: GitStageTracker.State,
        isGitPath: (FilePath) -> Boolean,
    ): List<FilePath> = state.rootStates.values
        .asSequence()
        .flatMap { rootState -> rootState.statuses.values.asSequence() }
        .mapNotNull { status -> status.committablePath() }
        .filter(isGitPath)
        .distinctBy { path -> path.normalizedPath() }
        .toList()

    fun committablePathsByRoot(state: GitStageTracker.State): Map<VirtualFile, List<FilePath>> = state.rootStates
        .mapValues { (_, rootState) ->
            rootState.statuses.values
                .mapNotNull { status -> status.committablePath() }
                .distinctBy { path -> path.normalizedPath() }
        }
        .filterValues { paths -> paths.isNotEmpty() }

    fun pathsToStageByRoot(state: GitStageTracker.State): Map<VirtualFile, List<FilePath>> = state.rootStates
        .mapValues { (_, rootState) ->
            rootState.statuses.values
                .mapNotNull { status -> status.pathToStage() }
                .distinctBy { path -> path.normalizedPath() }
        }
        .filterValues { paths -> paths.isNotEmpty() }

    fun containsAllStagedPaths(
        state: GitStageTracker.State,
        expectedPaths: Collection<FilePath>,
    ): Boolean = missingStagedPaths(state, expectedPaths).isEmpty()

    fun missingStagedPaths(
        state: GitStageTracker.State,
        expectedPaths: Collection<FilePath>,
    ): List<FilePath> {
        val stagedPaths = state.rootStates.values
            .asSequence()
            .flatMap { rootState -> rootState.statuses.values.asSequence() }
            .mapNotNull { status -> status.stagedPath() }
            .map { path -> path.normalizedPath() }
            .toSet()

        return expectedPaths
            .distinctBy { path -> path.normalizedPath() }
            .filter { path -> path.normalizedPath() !in stagedPaths }
    }
}

private fun GitFileStatus.committablePath(): FilePath? = if (isIgnored() || isNotChanged()) {
    null
} else {
    path
}

private fun GitFileStatus.pathToStage(): FilePath? = if (isExcludedFromStageMutation() || isAlreadyFullyStaged()) {
    null
} else {
    path
}

private fun GitFileStatus.stagedPath(): FilePath? = if (canConfirmStagedPath()) {
    path
} else {
    null
}

private fun GitFileStatus.isExcludedFromStageMutation(): Boolean = isIgnored() || isNotChanged() || isConflicted()

private fun GitFileStatus.isAlreadyFullyStaged(): Boolean = hasStagedIndex() && workTree == ' '

private fun GitFileStatus.canConfirmStagedPath(): Boolean = !isExcludedFromStageMutation() && hasStagedIndex()

private fun GitFileStatus.hasStagedIndex(): Boolean = index != ' ' && index != '?'

private fun FilePath.normalizedPath(): String = path.replace('\\', '/')
