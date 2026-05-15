package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change

internal const val GIT_VCS_NAME = "Git"

internal data class GitChangeSelection(
    val trackedChanges: List<Change>,
    val unversionedFiles: List<FilePath> = emptyList(),
    val resolvedConflictPaths: List<FilePath> = emptyList(),
) {
    val hasCommittableContent: Boolean
        get() = trackedChanges.isNotEmpty() || unversionedFiles.isNotEmpty() || resolvedConflictPaths.isNotEmpty()
}
