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

        val trackedChanges = changeListManager.changeLists
            .asSequence()
            .flatMap { changeList -> changeList.changes.asSequence() }
            .filter { change -> isEligibleGitChange(change, vcsManager) }
            .distinct()
            .toList()

        val resolvedConflictPaths = changeListManager.resolvedConflictPaths
            .filter { path -> GitChangeSelectionFilters.isGitPath(path, vcsManager) }
            .distinctBy { path -> path.path }

        return GitChangeSelection(
            trackedChanges = trackedChanges,
            resolvedConflictPaths = resolvedConflictPaths,
        )
    }

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
