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
package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.vcs.FilePath
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

    fun inclusionItems(selection: GitChangeSelection): List<Any> = selection.trackedChanges +
        selection.unversionedFiles +
        selection.resolvedConflictPaths +
        selection.stagingAreaPaths

    fun selectedPaths(selection: GitChangeSelection): List<FilePath> = buildList {
        selection.trackedChanges.forEach { change ->
            change.beforeRevision?.file?.let(::add)
            change.afterRevision?.file?.let(::add)
        }
        addAll(selection.unversionedFiles)
        addAll(selection.resolvedConflictPaths)
        addAll(selection.stagingAreaPaths)
    }.distinctBy { path -> path.path.replace('\\', '/') }
}
