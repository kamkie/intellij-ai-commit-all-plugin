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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
internal class CommitWorkflowSelectionService(private val project: Project) {
    fun prepareAllFilesSelection(
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
    ): CommitWorkflowSelectionResult = CommitWorkflowSelectionInput.create(workflowHandler, workflowUi)
        ?.let(::prepareAllFilesSelection)
        ?: CommitWorkflowSelectionResult.MissingWorkflow

    private fun prepareAllFilesSelection(input: CommitWorkflowSelectionInput): CommitWorkflowSelectionResult {
        val selectionService = GitChangeSelectionService.getInstance(project)
        val supportStatus = selectionService.supportStatus()
        return if (supportStatus == GitVcsSupportStatus.Supported) {
            prepareSupportedSelection(input, selectionService)
        } else {
            CommitWorkflowSelectionResult.UnsupportedVcs(supportStatus)
        }
    }

    private fun prepareSupportedSelection(
        input: CommitWorkflowSelectionInput,
        selectionService: GitChangeSelectionService,
    ): CommitWorkflowSelectionResult {
        val selection = selectionService.collectSelection()
        return if (selection.hasCommittableContent) {
            prepareCommittableSelection(input, selection)
        } else {
            CommitWorkflowSelectionResult.EmptySelection
        }
    }

    private fun prepareCommittableSelection(
        input: CommitWorkflowSelectionInput,
        selection: GitChangeSelection,
    ): CommitWorkflowSelectionResult {
        val changeLists = CommitWorkflowSelectionItems.changeListsContaining(
            trackedChanges = selection.trackedChanges,
            changeLists = ChangeListManager.getInstance(project).changeLists,
        )
        return if (changeLists.isEmpty() && selection.trackedChanges.isNotEmpty()) {
            CommitWorkflowSelectionResult.UnsupportedWorkflow("No Git changelist owns the selected tracked changes.")
        } else {
            synchronizeSelection(input, selection, changeLists)
        }
    }

    private fun synchronizeSelection(
        input: CommitWorkflowSelectionInput,
        selection: GitChangeSelection,
        changeLists: List<LocalChangeList>,
    ): CommitWorkflowSelectionResult {
        val activeChangeList = chooseActiveChangeList(changeLists)
        val inclusionItems = CommitWorkflowSelectionItems.inclusionItems(selection)

        return if (CommitWorkflowUiThreadAccess.run { input.workflowUi.activate() }) {
            synchronizedSelectionResult(
                input = input,
                selection = selection,
                changeLists = changeLists,
                activeChangeList = activeChangeList,
                inclusionItems = inclusionItems,
            )
        } else {
            CommitWorkflowSelectionResult.UnsupportedWorkflow("The Commit tool window workflow could not be activated.")
        }
    }

    private fun synchronizedSelectionResult(
        input: CommitWorkflowSelectionInput,
        selection: GitChangeSelection,
        changeLists: List<LocalChangeList>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): CommitWorkflowSelectionResult {
        val synchronized = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = input.workflowHandler,
            changeLists = changeLists,
            unversionedFiles = selection.unversionedFiles,
            activeChangeList = activeChangeList,
            inclusionItems = inclusionItems,
        )

        return if (synchronized) {
            CommitWorkflowSelectionResult.Prepared(selection)
        } else {
            CommitWorkflowSelectionResult.UnsupportedWorkflow(
                "The active commit workflow does not expose compatible inclusion-state methods.",
            )
        }
    }

    private fun chooseActiveChangeList(changeLists: List<LocalChangeList>): LocalChangeList = changeLists.firstOrNull()
        ?: ChangeListManager.getInstance(project).defaultChangeList

    companion object {
        fun getInstance(project: Project): CommitWorkflowSelectionService = project.service()
    }
}

private data class CommitWorkflowSelectionInput(
    val workflowHandler: CommitWorkflowHandler,
    val workflowUi: CommitWorkflowUi,
) {
    companion object {
        fun create(
            workflowHandler: CommitWorkflowHandler?,
            workflowUi: CommitWorkflowUi?,
        ): CommitWorkflowSelectionInput? = if (workflowHandler != null && workflowUi != null) {
            CommitWorkflowSelectionInput(workflowHandler, workflowUi)
        } else {
            null
        }
    }
}

internal object CommitWorkflowUiThreadAccess {
    fun <T> run(action: () -> T): T = ApplicationManager.getApplication()
        ?.takeUnless { application -> application.isDispatchThread }
        ?.let { application -> runOnEdt(application, action) }
        ?: action()

    private fun <T> runOnEdt(
        application: com.intellij.openapi.application.Application,
        action: () -> T,
    ): T {
        val result = AtomicReference<T>()
        val failure = AtomicReference<Throwable>()
        application.invokeAndWait {
            runCatching(action)
                .onSuccess(result::set)
                .onFailure(failure::set)
        }
        failure.get()?.let { throwable -> throw throwable }
        return result.get()
    }
}
