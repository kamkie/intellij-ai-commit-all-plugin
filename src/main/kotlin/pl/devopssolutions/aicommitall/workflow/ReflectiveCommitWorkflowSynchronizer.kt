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

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.commit.CommitWorkflowHandler
import git4idea.index.GitStageCommitWorkflowHandler
import git4idea.index.GitStageTracker
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems
import java.lang.reflect.Method

private const val GIT_STAGE_CONFIRMATION_ATTEMPTS = 10

internal object ReflectiveCommitWorkflowSynchronizer {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): Boolean = synchronizeGitStageWorkflow(workflowHandler)
        ?: synchronizeCommitWorkflow(
            workflowHandler = workflowHandler,
            changeLists = changeLists,
            unversionedFiles = unversionedFiles,
            activeChangeList = activeChangeList,
            inclusionItems = inclusionItems,
        )

    private fun synchronizeCommitWorkflow(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): Boolean = inclusionItems.isNotEmpty() &&
        workflowHandler.javaClass.commitWorkflowMethods()?.synchronize(
            workflowHandler = workflowHandler,
            changeLists = changeLists,
            unversionedFiles = unversionedFiles,
            activeChangeList = activeChangeList,
            inclusionItems = inclusionItems,
        ) == true

    private fun Class<*>.commitWorkflowMethods(): CommitWorkflowMethods? {
        val synchronizeInclusion = findMethod("synchronizeInclusion", List::class.java, List::class.java)
        val setCommitState = findMethod(
            "setCommitState",
            LocalChangeList::class.java,
            Collection::class.java,
            java.lang.Boolean.TYPE,
        )
        return if (synchronizeInclusion != null && setCommitState != null) {
            CommitWorkflowMethods(synchronizeInclusion, setCommitState)
        } else {
            null
        }
    }

    private fun synchronizeGitStageWorkflow(workflowHandler: CommitWorkflowHandler): Boolean? {
        val gitStageHandler = workflowHandler as? GitStageCommitWorkflowHandler ?: return null

        return runCatching {
            val project = gitStageHandler.workflow.project
            val tracker = GitStageTracker.getInstance(project)
            tracker.updateTrackerState()
            val currentState = tracker.state
            val expectedPathsByRoot = GitStageSelectionItems.committablePathsByRoot(currentState)
            if (expectedPathsByRoot.isEmpty()) {
                return@runCatching false
            }
            val pathsToStageByRoot = GitStageSelectionItems.pathsToStageByRoot(currentState)

            val refreshedState = confirmStagedState(
                project = project,
                tracker = tracker,
                pathsByRoot = pathsToStageByRoot,
                expectedPaths = expectedPathsByRoot.values.flatten(),
            ) ?: return@runCatching false
            val includedRoots = expectedPathsByRoot.keys
            CommitWorkflowUiThreadAccess.run {
                gitStageHandler.state = refreshedState
                gitStageHandler.ui.setTrackerState(refreshedState)
                gitStageHandler.ui.setIncludedRoots(includedRoots)
            }
            true
        }.getOrDefault(false)
    }

    private fun confirmStagedState(
        project: Project,
        tracker: GitStageTracker,
        pathsByRoot: Map<VirtualFile, List<FilePath>>,
        expectedPaths: Collection<FilePath>,
    ): GitStageTracker.State? = GitStageConfirmation(
        attempts = GIT_STAGE_CONFIRMATION_ATTEMPTS,
        operations = IntellijGitStageConfirmationOperations(project, tracker),
    ).confirm(
        pathsByRoot = pathsByRoot,
        expectedPaths = expectedPaths,
    )

    private fun Class<*>.findMethod(
        name: String,
        vararg parameterTypes: Class<*>,
    ): Method? = methods.firstOrNull { method ->
        method.name == name &&
            method.parameterTypes.contentEquals(parameterTypes)
    }
}

private data class CommitWorkflowMethods(
    val synchronizeInclusion: Method,
    val setCommitState: Method,
) {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): Boolean = runCatching {
        CommitWorkflowUiThreadAccess.run {
            synchronizeInclusion.invoke(workflowHandler, changeLists, unversionedFiles)
            setCommitState.invoke(workflowHandler, activeChangeList, inclusionItems, true)
        }
    }.isSuccess
}
