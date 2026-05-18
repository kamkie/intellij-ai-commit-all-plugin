package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
internal class CommitWorkflowSelectionService(private val project: Project) {
    fun prepareAllFilesSelection(
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
    ): CommitWorkflowSelectionResult {
        if (workflowHandler == null || workflowUi == null) {
            return CommitWorkflowSelectionResult.MissingWorkflow
        }

        val selectionService = GitChangeSelectionService.getInstance(project)
        val supportStatus = selectionService.supportStatus()
        if (supportStatus != GitVcsSupportStatus.Supported) {
            return CommitWorkflowSelectionResult.UnsupportedVcs(supportStatus)
        }

        val selection = selectionService.collectSelection()
        if (!selection.hasCommittableContent) {
            return CommitWorkflowSelectionResult.EmptySelection
        }

        val changeLists = CommitWorkflowSelectionItems.changeListsContaining(
            trackedChanges = selection.trackedChanges,
            changeLists = ChangeListManager.getInstance(project).changeLists,
        )
        if (changeLists.isEmpty() && selection.trackedChanges.isNotEmpty()) {
            return CommitWorkflowSelectionResult.UnsupportedWorkflow("No Git changelist owns the selected tracked changes.")
        }

        val activeChangeList = chooseActiveChangeList(changeLists)
        val inclusionItems = CommitWorkflowSelectionItems.inclusionItems(selection)

        if (!CommitWorkflowUiThreadAccess.run { workflowUi.activate() }) {
            return CommitWorkflowSelectionResult.UnsupportedWorkflow("The Commit tool window workflow could not be activated.")
        }

        val synchronized = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = workflowHandler,
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

    private fun chooseActiveChangeList(changeLists: List<LocalChangeList>): LocalChangeList =
        changeLists.firstOrNull() ?: ChangeListManager.getInstance(project).defaultChangeList

    companion object {
        fun getInstance(project: Project): CommitWorkflowSelectionService = project.service()
    }
}

internal object CommitWorkflowUiThreadAccess {
    fun <T> run(action: () -> T): T {
        val application = ApplicationManager.getApplication() ?: return action()
        if (application.isDispatchThread) {
            return action()
        }

        val result = AtomicReference<T>()
        val failure = AtomicReference<Throwable>()
        application.invokeAndWait {
            try {
                result.set(action())
            } catch (throwable: Throwable) {
                failure.set(throwable)
            }
        }
        failure.get()?.let { throwable -> throw throwable }
        return result.get()
    }
}
