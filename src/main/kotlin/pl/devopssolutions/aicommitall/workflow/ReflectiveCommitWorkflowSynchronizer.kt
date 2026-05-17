package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.commit.CommitWorkflowHandler
import git4idea.index.GitStageCommitWorkflowHandler
import git4idea.index.GitStageTracker
import git4idea.util.GitFileUtils
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems
import java.lang.reflect.Method

private const val GIT_STAGE_CONFIRMATION_ATTEMPTS = 3

internal object ReflectiveCommitWorkflowSynchronizer {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): Boolean {
        synchronizeGitStageWorkflow(workflowHandler)?.let { synchronized ->
            return synchronized
        }

        if (inclusionItems.isEmpty()) {
            return false
        }

        val handlerClass = workflowHandler.javaClass
        val synchronizeInclusion = handlerClass.findMethod("synchronizeInclusion", List::class.java, List::class.java)
            ?: return false
        val setCommitState = handlerClass.findMethod(
            "setCommitState",
            LocalChangeList::class.java,
            Collection::class.java,
            java.lang.Boolean.TYPE,
        ) ?: return false

        return runCatching {
            synchronizeInclusion.invoke(workflowHandler, changeLists, unversionedFiles)
            setCommitState.invoke(workflowHandler, activeChangeList, inclusionItems, true)
        }.isSuccess
    }

    private fun synchronizeGitStageWorkflow(workflowHandler: CommitWorkflowHandler): Boolean? {
        val gitStageHandler = workflowHandler as? GitStageCommitWorkflowHandler ?: return null

        return runCatching {
            val project = gitStageHandler.workflow.project
            val tracker = GitStageTracker.getInstance(project)
            tracker.updateTrackerState()
            val currentState = tracker.state
            val pathsByRoot = GitStageSelectionItems.committablePathsByRoot(currentState)
            if (pathsByRoot.isEmpty()) {
                return@runCatching false
            }

            val refreshedState = confirmStagedState(
                project = project,
                tracker = tracker,
                pathsByRoot = pathsByRoot,
            ) ?: return@runCatching false
            val includedRoots = pathsByRoot.keys
            gitStageHandler.state = refreshedState
            gitStageHandler.ui.setTrackerState(refreshedState)
            gitStageHandler.ui.setIncludedRoots(includedRoots)
            true
        }.getOrDefault(false)
    }

    private fun confirmStagedState(
        project: Project,
        tracker: GitStageTracker,
        pathsByRoot: Map<VirtualFile, List<FilePath>>,
    ): GitStageTracker.State? {
        val expectedPaths = pathsByRoot.values.flatten()
        repeat(GIT_STAGE_CONFIRMATION_ATTEMPTS) {
            val refreshedState = runCatching {
                pathsByRoot.forEach { (root, paths) ->
                    GitFileUtils.addPaths(project, root, paths, true)
                }
                tracker.updateTrackerState()
                tracker.state
            }.getOrNull()

            if (refreshedState != null && GitStageSelectionItems.containsAllStagedPaths(refreshedState, expectedPaths)) {
                return refreshedState
            }
        }

        return null
    }

    private fun Class<*>.findMethod(name: String, vararg parameterTypes: Class<*>): Method? =
        methods.firstOrNull { method ->
            method.name == name &&
                method.parameterTypes.contentEquals(parameterTypes)
        }
}
