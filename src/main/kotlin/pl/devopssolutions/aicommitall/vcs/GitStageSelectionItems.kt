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

    private fun GitFileStatus.committablePath(): FilePath? =
        if (isIgnored() || isNotChanged()) {
            null
        } else {
            path
        }
}
