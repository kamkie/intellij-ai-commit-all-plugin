package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.LocalChangeList
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection

internal object CommitWorkflowSelectionItems {
    fun changeListsContaining(
        trackedChanges: List<Change>,
        changeLists: List<LocalChangeList>,
    ): List<LocalChangeList> {
        val trackedSet = trackedChanges.toSet()
        return changeLists.filter { changeList ->
            changeList.changes.any { change -> change in trackedSet }
        }
    }

    fun inclusionItems(selection: GitChangeSelection): List<Any> =
        selection.trackedChanges +
            selection.unversionedFiles +
            selection.resolvedConflictPaths +
            selection.stagingAreaPaths
}
