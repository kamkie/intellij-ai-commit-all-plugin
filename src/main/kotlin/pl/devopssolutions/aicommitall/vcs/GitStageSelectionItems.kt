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
        .distinctBy { path -> path.path }
        .toList()

    fun committablePathsByRoot(state: GitStageTracker.State): Map<VirtualFile, List<FilePath>> =
        state.rootStates
            .mapValues { (_, rootState) ->
                rootState.statuses.values
                    .mapNotNull { status -> status.committablePath() }
                    .distinctBy { path -> path.path }
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

    private fun GitFileStatus.committablePath(): FilePath? =
        if (isIgnored() || isNotChanged()) {
            null
        } else {
            path
        }

    private fun GitFileStatus.stagedPath(): FilePath? =
        if (isIgnored() || isNotChanged() || isConflicted() || index == ' ' || index == '?') {
            null
        } else {
            path
        }

    private fun FilePath.normalizedPath(): String = path.replace('\\', '/')
}
