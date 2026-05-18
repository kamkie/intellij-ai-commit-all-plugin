package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import git4idea.index.GitStageTracker
import git4idea.util.GitFileUtils
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems

internal class GitStageConfirmation(
    private val attempts: Int,
    private val operations: GitStageConfirmationOperations,
) {
    fun confirm(pathsByRoot: Map<VirtualFile, List<FilePath>>): GitStageTracker.State? {
        val expectedPaths = pathsByRoot.values
            .flatten()
            .distinctBy { path -> path.normalizedPath() }
        if (attempts <= 0 || expectedPaths.isEmpty()) {
            return null
        }

        repeat(attempts) {
            val refreshedState = runCatching {
                pathsByRoot.forEach { (root, paths) ->
                    operations.stagePaths(root, paths)
                }
                operations.reloadExternalFiles(expectedPaths)
                operations.refreshTrackerState()
            }.getOrNull()

            if (refreshedState != null && GitStageSelectionItems.containsAllStagedPaths(refreshedState, expectedPaths)) {
                return refreshedState
            }
        }

        return null
    }

    private fun FilePath.normalizedPath(): String = path.replace('\\', '/')
}

internal interface GitStageConfirmationOperations {
    fun stagePaths(root: VirtualFile, paths: List<FilePath>)

    fun reloadExternalFiles(paths: Collection<FilePath>)

    fun refreshTrackerState(): GitStageTracker.State
}

internal class IntellijGitStageConfirmationOperations(
    private val project: Project,
    private val tracker: GitStageTracker,
) : GitStageConfirmationOperations {
    override fun stagePaths(root: VirtualFile, paths: List<FilePath>) {
        GitFileUtils.addPaths(project, root, paths, true)
    }

    override fun reloadExternalFiles(paths: Collection<FilePath>) {
        LocalFileSystem.getInstance()
            .refreshIoFiles(paths.map { path -> path.ioFile }, false, false, null)
    }

    override fun refreshTrackerState(): GitStageTracker.State {
        tracker.updateTrackerState()
        return tracker.state
    }
}
