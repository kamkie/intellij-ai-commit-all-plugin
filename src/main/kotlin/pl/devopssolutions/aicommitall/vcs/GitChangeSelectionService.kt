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
package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import git4idea.index.GitStageTracker

@Service(Service.Level.PROJECT)
internal class GitChangeSelectionService(private val project: Project) {
    fun supportStatus(): GitVcsSupportStatus {
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        return GitVcsSupport.status(vcsManager.getAllActiveVcss().map { vcs -> vcs.name })
    }

    fun collectTrackedSelection(): GitChangeSelection {
        val changeListManager = ChangeListManager.getInstance(project)
        val vcsManager = ProjectLevelVcsManager.getInstance(project)

        return GitChangeSelection(
            trackedChanges = collectTrackedChanges(changeListManager, vcsManager),
            resolvedConflictPaths = collectResolvedConflictPaths(changeListManager, vcsManager),
            stagingAreaPaths = collectStagingAreaPaths(vcsManager),
        )
    }

    fun collectSelection(): GitChangeSelection {
        val changeListManager = ChangeListManager.getInstance(project)
        val vcsManager = ProjectLevelVcsManager.getInstance(project)

        return GitChangeSelection(
            trackedChanges = collectTrackedChanges(changeListManager, vcsManager),
            unversionedFiles = collectUnversionedFiles(changeListManager, vcsManager),
            resolvedConflictPaths = collectResolvedConflictPaths(changeListManager, vcsManager),
            stagingAreaPaths = collectStagingAreaPaths(vcsManager),
        )
    }

    private fun collectTrackedChanges(
        changeListManager: ChangeListManager,
        vcsManager: ProjectLevelVcsManager,
    ): List<Change> = changeListManager.changeLists
        .asSequence()
        .flatMap { changeList -> changeList.changes.asSequence() }
        .filter { change -> isEligibleGitChange(change, vcsManager) }
        .distinct()
        .toList()

    private fun collectUnversionedFiles(
        changeListManager: ChangeListManager,
        vcsManager: ProjectLevelVcsManager,
    ) = changeListManager.unversionedFilesPaths
        .filter { path -> GitChangeSelectionFilters.isGitPath(path, vcsManager) }
        .distinctBy { path -> path.path }

    private fun collectResolvedConflictPaths(
        changeListManager: ChangeListManager,
        vcsManager: ProjectLevelVcsManager,
    ) = changeListManager.resolvedConflictPaths
        .filter { path -> GitChangeSelectionFilters.isGitPath(path, vcsManager) }
        .distinctBy { path -> path.path }

    private fun collectStagingAreaPaths(
        vcsManager: ProjectLevelVcsManager,
    ) = GitStagingAreaSelectionCollector.collect(
        stateProvider = { GitStageTracker.getInstance(project).state },
        isGitPath = { path -> GitChangeSelectionFilters.isGitPath(path, vcsManager) },
    )

    private fun isEligibleGitChange(
        change: Change,
        vcsManager: ProjectLevelVcsManager,
    ): Boolean = GitChangeSelectionFilters.isGitChange(
        change = change,
        vcsNameForPath = { path -> vcsManager.getVcsFor(path)?.name },
        isIgnored = vcsManager::isIgnored,
    )

    companion object {
        fun getInstance(project: Project): GitChangeSelectionService = project.service()
    }
}
