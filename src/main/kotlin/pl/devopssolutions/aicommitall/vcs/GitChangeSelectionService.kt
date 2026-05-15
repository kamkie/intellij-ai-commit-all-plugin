package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager

@Service(Service.Level.PROJECT)
internal class GitChangeSelectionService(private val project: Project) {
    fun collectTrackedSelection(): GitChangeSelection {
        val changeListManager = ChangeListManager.getInstance(project)
        val vcsManager = ProjectLevelVcsManager.getInstance(project)

        return GitChangeSelection(
            trackedChanges = collectTrackedChanges(changeListManager, vcsManager),
            resolvedConflictPaths = collectResolvedConflictPaths(changeListManager, vcsManager),
        )
    }

    fun collectSelection(): GitChangeSelection {
        val changeListManager = ChangeListManager.getInstance(project)
        val vcsManager = ProjectLevelVcsManager.getInstance(project)

        return GitChangeSelection(
            trackedChanges = collectTrackedChanges(changeListManager, vcsManager),
            unversionedFiles = collectUnversionedFiles(changeListManager, vcsManager),
            resolvedConflictPaths = collectResolvedConflictPaths(changeListManager, vcsManager),
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
