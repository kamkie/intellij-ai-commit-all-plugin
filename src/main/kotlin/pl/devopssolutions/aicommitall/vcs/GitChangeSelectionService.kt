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
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import git4idea.index.GitStageTracker

@Service(Service.Level.PROJECT)
internal class GitChangeSelectionService @JvmOverloads constructor(
    project: Project,
    private val environment: GitChangeSelectionEnvironment = IntellijGitChangeSelectionEnvironment(project),
) : GitChangeSelectionSource {
    override fun supportStatus(): GitVcsSupportStatus = GitVcsSupport.status(environment.activeVcsNames())

    override fun collectTrackedSelection(): GitChangeSelection = GitChangeSelection(
        trackedChanges = collectTrackedChanges(environment.changeLists()),
        resolvedConflictPaths = collectResolvedConflictPaths(),
        stagingAreaPaths = environment.stagingAreaPaths(),
    )

    override fun collectSelection(): GitChangeSelection = GitChangeSelection(
        trackedChanges = collectTrackedChanges(environment.changeLists()),
        unversionedFiles = collectUnversionedFiles(),
        resolvedConflictPaths = collectResolvedConflictPaths(),
        stagingAreaPaths = environment.stagingAreaPaths(),
    )

    private fun collectTrackedChanges(
        changeLists: List<LocalChangeList>,
    ): List<Change> = changeLists
        .asSequence()
        .flatMap { changeList -> changeList.changes.asSequence() }
        .filter(::isEligibleGitChange)
        .distinct()
        .toList()

    private fun collectUnversionedFiles() = environment.unversionedFiles()
        .filter(::isEligibleGitPath)
        .distinctBy { path -> path.path }

    private fun collectResolvedConflictPaths() = environment.resolvedConflictPaths()
        .filter(::isEligibleGitPath)
        .distinctBy { path -> path.path }

    private fun isEligibleGitChange(change: Change): Boolean = GitChangeSelectionFilters.isGitChange(
        change = change,
        vcsNameForPath = environment::vcsNameForPath,
        isIgnored = environment::isIgnored,
    )

    private fun isEligibleGitPath(path: FilePath): Boolean = GitChangeSelectionFilters.isGitPath(
        path = path,
        vcsNameForPath = environment::vcsNameForPath,
        isIgnored = environment::isIgnored,
    )

    companion object {
        fun getInstance(project: Project): GitChangeSelectionService = project.service()
    }
}

internal interface GitChangeSelectionSource {
    fun supportStatus(): GitVcsSupportStatus

    fun collectTrackedSelection(): GitChangeSelection

    fun collectSelection(): GitChangeSelection
}

internal interface GitChangeSelectionEnvironment {
    fun activeVcsNames(): List<String>

    fun changeLists(): List<LocalChangeList>

    fun unversionedFiles(): List<FilePath>

    fun resolvedConflictPaths(): List<FilePath>

    fun stagingAreaPaths(): List<FilePath>

    fun vcsNameForPath(path: FilePath): String?

    fun isIgnored(path: FilePath): Boolean
}

private class IntellijGitChangeSelectionEnvironment(private val project: Project) : GitChangeSelectionEnvironment {
    override fun activeVcsNames(): List<String> {
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        return vcsManager.getAllActiveVcss().map { vcs -> vcs.name }
    }

    override fun changeLists(): List<LocalChangeList> = ChangeListManager.getInstance(project).changeLists

    override fun unversionedFiles(): List<FilePath> = ChangeListManager.getInstance(project).unversionedFilesPaths

    override fun resolvedConflictPaths(): List<FilePath> = ChangeListManager.getInstance(project).resolvedConflictPaths

    override fun stagingAreaPaths(): List<FilePath> {
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        return GitStagingAreaSelectionCollector.collect(
            stateProvider = { GitStageTracker.getInstance(project).state },
            isGitPath = { path -> GitChangeSelectionFilters.isGitPath(path, vcsManager) },
        )
    }

    override fun vcsNameForPath(path: FilePath): String? = ProjectLevelVcsManager.getInstance(project)
        .getVcsFor(path)
        ?.name

    override fun isIgnored(path: FilePath): Boolean = ProjectLevelVcsManager.getInstance(project).isIgnored(path)
}
