package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.InvokeAfterUpdateMode
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcsUtil.VcsFileUtil
import git4idea.index.GitStageTracker
import git4idea.index.GitStageTrackerListener
import git4idea.util.GitFileUtils
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class GitStageConfirmation(
    private val attempts: Int,
    private val operations: GitStageConfirmationOperations,
    private val retryDelay: Duration = Duration.ofMillis(250),
    private val sleeper: GitStageConfirmationSleeper = ThreadGitStageConfirmationSleeper,
) {
    fun confirm(pathsByRoot: Map<VirtualFile, List<FilePath>>): GitStageTracker.State? {
        val expectedPaths = pathsByRoot.values
            .flatten()
            .distinctBy { path -> path.normalizedPath() }
        if (attempts <= 0 || expectedPaths.isEmpty()) {
            return null
        }

        repeat(attempts) { attempt ->
            val refreshedState = runCatching {
                pathsByRoot.forEach { (root, paths) ->
                    operations.stagePaths(root, paths)
                }
                operations.reloadExternalFiles(expectedPaths)
                operations.markPathsDirty(expectedPaths)
                operations.waitForStatusRefresh()
                operations.refreshTrackerState()
            }.getOrNull()

            if (refreshedState != null && GitStageSelectionItems.containsAllStagedPaths(refreshedState, expectedPaths)) {
                return refreshedState
            }

            if (attempt < attempts - 1 && !retryDelay.isNegative && !retryDelay.isZero) {
                sleeper.sleep(retryDelay)
            }
        }

        return null
    }

    private fun FilePath.normalizedPath(): String = path.replace('\\', '/')
}

internal interface GitStageConfirmationOperations {
    fun stagePaths(root: VirtualFile, paths: List<FilePath>)

    fun reloadExternalFiles(paths: Collection<FilePath>)

    fun markPathsDirty(paths: Collection<FilePath>) = Unit

    fun waitForStatusRefresh() = Unit

    fun refreshTrackerState(): GitStageTracker.State
}

internal fun interface GitStageConfirmationSleeper {
    fun sleep(duration: Duration)
}

private object ThreadGitStageConfirmationSleeper : GitStageConfirmationSleeper {
    override fun sleep(duration: Duration) {
        try {
            Thread.sleep(duration.toMillis())
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
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

    override fun markPathsDirty(paths: Collection<FilePath>) {
        VcsFileUtil.markFilesDirty(project, paths.toList())
    }

    override fun waitForStatusRefresh() {
        val latch = CountDownLatch(1)
        ChangeListManager.getInstance(project).invokeAfterUpdate(
            { latch.countDown() },
            InvokeAfterUpdateMode.SILENT_CALLBACK_POOLED,
            "AI Commit All staging refresh",
            ModalityState.nonModal(),
        )
        latch.awaitBounded(STATUS_REFRESH_TIMEOUT)
    }

    override fun refreshTrackerState(): GitStageTracker.State {
        val disposable = Disposer.newDisposable()
        val latch = CountDownLatch(1)
        tracker.addListener(
            object : GitStageTrackerListener {
                override fun update() {
                    latch.countDown()
                }
            },
            disposable,
        )
        tracker.updateTrackerState()
        return try {
            latch.awaitBounded(TRACKER_REFRESH_TIMEOUT)
            tracker.state
        } finally {
            Disposer.dispose(disposable)
        }
    }

    private fun CountDownLatch.awaitBounded(timeout: Duration) {
        try {
            await(timeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private companion object {
        val STATUS_REFRESH_TIMEOUT: Duration = Duration.ofSeconds(2)
        val TRACKER_REFRESH_TIMEOUT: Duration = Duration.ofSeconds(2)
    }
}
