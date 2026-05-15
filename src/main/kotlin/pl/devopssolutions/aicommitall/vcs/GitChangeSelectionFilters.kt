package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil

internal object GitChangeSelectionFilters {
    fun isGitChange(
        change: Change,
        vcsNameForPath: (FilePath) -> String?,
        isIgnored: (FilePath) -> Boolean,
    ): Boolean {
        val paths = ChangesUtil.iteratePaths(listOf(change)).toList()
        return paths.isNotEmpty() &&
            paths.all { path -> vcsNameForPath(path) == GIT_VCS_NAME && !isIgnored(path) }
    }

    fun isGitPath(
        path: FilePath,
        vcsManager: ProjectLevelVcsManager,
    ): Boolean = vcsManager.getVcsFor(path)?.name == GIT_VCS_NAME && !vcsManager.isIgnored(path)
}
