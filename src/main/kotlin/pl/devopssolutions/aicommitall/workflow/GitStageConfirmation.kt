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

import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
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
    private val retryDelay: Duration = DEFAULT_RETRY_DELAY,
    private val sleeper: GitStageConfirmationSleeper = ThreadGitStageConfirmationSleeper,
) {
    fun confirm(
        pathsByRoot: Map<VirtualFile, List<FilePath>>,
        expectedPaths: Collection<FilePath> = pathsByRoot.values.flatten(),
    ): GitStageTracker.State? {
        val distinctExpectedPaths = expectedPaths
            .distinctBy { path -> path.normalizedPath() }
        val pathsToStageByRoot = pathsByRoot
            .mapValues { (_, paths) ->
                paths.distinctBy { path -> path.normalizedPath() }
            }
            .filterValues { paths -> paths.isNotEmpty() }
        if (attempts <= 0 || distinctExpectedPaths.isEmpty()) {
            logger.info(
                "AI Commit All diagnostic: staging confirmation skipped, " +
                    "attempts=$attempts, expectedPaths=${distinctExpectedPaths.size}",
            )
            return null
        }

        val pathsToStageCount = pathsToStageByRoot.values.sumOf { paths -> paths.size }
        logger.info(
            "AI Commit All diagnostic: staging confirmation started, " +
                "attempts=$attempts, roots=${pathsToStageByRoot.size}, " +
                "pathsToStage=$pathsToStageCount, expectedPaths=${distinctExpectedPaths.size}",
        )

        var confirmedState: GitStageTracker.State? = null
        var attempt = 0
        while (attempt < attempts && confirmedState == null) {
            val refreshedState = refreshAfterStaging(
                pathsToStageByRoot = pathsToStageByRoot,
                distinctExpectedPaths = distinctExpectedPaths,
                attempt = attempt,
            )

            val expectedPathsAreStaged = refreshedState != null &&
                GitStageSelectionItems.containsAllStagedPaths(refreshedState, distinctExpectedPaths)
            logger.info(
                "AI Commit All diagnostic: staging confirmation attempt completed, " +
                    "attempt=${attempt + 1}/$attempts, " +
                    "refreshedState=${refreshedState != null}, confirmed=$expectedPathsAreStaged",
            )
            if (expectedPathsAreStaged) {
                confirmedState = refreshedState
            }

            if (confirmedState == null && attempt < attempts - 1 && retryDelay.isPositive()) {
                sleeper.sleep(retryDelay)
            }
            attempt += 1
        }

        logger.info(
            "AI Commit All diagnostic: staging confirmation finished, " +
                "confirmed=${confirmedState != null}, attemptsUsed=$attempt",
        )
        return confirmedState
    }

    private fun refreshAfterStaging(
        pathsToStageByRoot: Map<VirtualFile, List<FilePath>>,
        distinctExpectedPaths: Collection<FilePath>,
        attempt: Int,
    ): GitStageTracker.State? = runCatching {
        pathsToStageByRoot.forEach { (root, paths) ->
            operations.stagePaths(root, paths)
        }
        operations.reloadExternalFiles(distinctExpectedPaths)
        operations.markPathsDirty(distinctExpectedPaths)
        operations.waitForStatusRefresh()
        operations.refreshTrackerState()
    }.getOrElse { exception ->
        logger.info(
            "AI Commit All diagnostic: staging confirmation attempt failed, " +
                "attempt=${attempt + 1}/$attempts, " +
                "exception=${exception.javaClass.name}, " +
                "cause=${exception.cause?.javaClass?.name ?: "<none>"}",
        )
        null
    }

    private fun FilePath.normalizedPath(): String = path.replace('\\', '/')

    private companion object {
        private const val DEFAULT_RETRY_DELAY_MILLIS = 250L
        val logger: Logger = Logger.getInstance(GitStageConfirmation::class.java)
        val DEFAULT_RETRY_DELAY: Duration = Duration.ofMillis(DEFAULT_RETRY_DELAY_MILLIS)
    }
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
