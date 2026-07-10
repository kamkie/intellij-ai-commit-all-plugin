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
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker
import git4idea.index.GitStageTrackerListener
import git4idea.util.GitFileUtils
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private const val DEFAULT_SYNTHETIC_STAGED_INDEX = 'M'

internal class GitStageConfirmation(
    private val attempts: Int,
    private val operations: GitStageConfirmationOperations,
    private val retryDelay: Duration = DEFAULT_RETRY_DELAY,
    private val sleeper: GitStageConfirmationSleeper = ThreadGitStageConfirmationSleeper,
) {
    fun confirm(
        pathsByRoot: Map<VirtualFile, List<FilePath>>,
        expectedPaths: Collection<FilePath> = pathsByRoot.values.flatten(),
        expectedPathsByRoot: Map<VirtualFile, List<FilePath>> = pathsByRoot,
    ): GitStageTracker.State? {
        val distinctExpectedPaths = expectedPaths
            .distinctBy { path -> path.normalizedPath() }
        val distinctExpectedPathsByRoot = expectedPathsByRoot
            .mapValues { (_, paths) ->
                paths.distinctBy { path -> path.normalizedPath() }
            }
            .filterValues { paths -> paths.isNotEmpty() }
        var pathsToStageByRoot = pathsByRoot
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

        logStarted(pathsToStageByRoot, distinctExpectedPaths)

        var confirmedState: GitStageTracker.State? = null
        var attempt = 0
        while (attempt < attempts && confirmedState == null) {
            val staged = stagePaths(
                pathsToStageByRoot = pathsToStageByRoot,
                attempt = attempt,
            )
            if (staged) {
                confirmedState = directlyConfirmedState(distinctExpectedPathsByRoot)
                if (confirmedState == null) {
                    val refreshedState = refreshAfterDirectConfirmation(
                        distinctExpectedPaths = distinctExpectedPaths,
                        attempt = attempt,
                    )

                    val expectedPathsAreStaged = refreshedState.confirmsExpectedPaths(distinctExpectedPaths)
                    logger.info(
                        "AI Commit All diagnostic: staging confirmation fallback attempt completed, " +
                            "attempt=${attempt + 1}/$attempts, " +
                            "refreshedState=${refreshedState != null}, confirmed=$expectedPathsAreStaged",
                    )
                    confirmedState = confirmedStateAfterFallback(
                        refreshedState = refreshedState,
                        distinctExpectedPaths = distinctExpectedPaths,
                        distinctExpectedPathsByRoot = distinctExpectedPathsByRoot,
                    )
                    if (confirmedState == null && refreshedState != null && refreshedState.hasStatusEntries()) {
                        pathsToStageByRoot = refreshedState.pathsToStageByRoot(distinctExpectedPaths)
                    }
                }
            }

            if (confirmedState == null && attempt < attempts - 1 && retryDelay.isPositive()) {
                sleeper.sleep(retryDelay)
            }
            attempt += 1
        }

        logFinished(confirmedState, attempt)
        return confirmedState
    }

    private fun logStarted(
        pathsToStageByRoot: Map<VirtualFile, List<FilePath>>,
        distinctExpectedPaths: Collection<FilePath>,
    ) {
        val pathsToStageCount = pathsToStageByRoot.values.sumOf { paths -> paths.size }
        logger.info(
            "AI Commit All diagnostic: staging confirmation started, " +
                "attempts=$attempts, roots=${pathsToStageByRoot.size}, " +
                "pathsToStage=$pathsToStageCount, expectedPaths=${distinctExpectedPaths.size}",
        )
    }

    private fun logFinished(
        confirmedState: GitStageTracker.State?,
        attemptsUsed: Int,
    ) {
        logger.info(
            "AI Commit All diagnostic: staging confirmation finished, " +
                "confirmed=${confirmedState != null}, attemptsUsed=$attemptsUsed",
        )
    }

    private fun GitStageTracker.State?.confirmsExpectedPaths(
        distinctExpectedPaths: Collection<FilePath>,
    ): Boolean = this != null && GitStageSelectionItems.containsAllStagedPaths(this, distinctExpectedPaths)

    private fun confirmedStateAfterFallback(
        refreshedState: GitStageTracker.State?,
        distinctExpectedPaths: Collection<FilePath>,
        distinctExpectedPathsByRoot: Map<VirtualFile, List<FilePath>>,
    ): GitStageTracker.State? = refreshedState?.let { state ->
        when {
            state.confirmsExpectedPaths(distinctExpectedPaths) -> state

            else -> confirmGitIndexState(distinctExpectedPathsByRoot)?.let { confirmationsByPath ->
                state.withGitIndexConfirmations(distinctExpectedPathsByRoot, confirmationsByPath)
            }
        }
    }

    private fun directlyConfirmedState(
        distinctExpectedPathsByRoot: Map<VirtualFile, List<FilePath>>,
    ): GitStageTracker.State? = confirmGitIndexState(distinctExpectedPathsByRoot)?.let { confirmationsByPath ->
        operations.currentTrackerState()
            .withGitIndexConfirmations(distinctExpectedPathsByRoot, confirmationsByPath)
    }

    private fun confirmGitIndexState(
        expectedPathsByRoot: Map<VirtualFile, List<FilePath>>,
    ): Map<String, GitIndexConfirmation>? = runCatching {
        val startedAtNanos = System.nanoTime()
        if (expectedPathsByRoot.isEmpty()) {
            return@runCatching null
        }

        var confirmedCount = 0
        val confirmationsByPath = mutableMapOf<String, GitIndexConfirmation>()
        val unconfirmedPaths = mutableListOf<FilePath>()
        expectedPathsByRoot.forEach { (root, paths) ->
            paths.forEach { path ->
                val confirmation = operations.confirmIndexPath(root, path)
                when (confirmation) {
                    GitIndexConfirmation.STAGED,
                    GitIndexConfirmation.HEAD_IDENTICAL,
                    -> {
                        confirmedCount += 1
                        confirmationsByPath[path.normalizedPath()] = confirmation
                    }

                    GitIndexConfirmation.UNCONFIRMED -> unconfirmedPaths += path
                }
            }
        }
        logger.info(
            "AI Commit All diagnostic: staging confirmation direct index check completed, " +
                "confirmed=${unconfirmedPaths.isEmpty()}, " +
                "confirmedPaths=$confirmedCount, unconfirmedPaths=${unconfirmedPaths.size}, " +
                "elapsedMs=${elapsedMillisSince(startedAtNanos)}",
        )
        confirmationsByPath.takeIf { unconfirmedPaths.isEmpty() }
    }.getOrElse { exception ->
        logger.info(
            "AI Commit All diagnostic: staging confirmation direct index check failed, " +
                "exception=${exception.javaClass.name}, " +
                "cause=${exception.cause?.javaClass?.name ?: "<none>"}",
        )
        null
    }

    private fun GitStageTracker.State.pathsToStageByRoot(
        expectedPaths: Collection<FilePath>,
    ): Map<VirtualFile, List<FilePath>> {
        val expectedPathTexts = expectedPaths.map { path -> path.normalizedPath() }.toSet()
        return GitStageSelectionItems.pathsToStageByRoot(this)
            .mapValues { (_, paths) ->
                paths.filter { path -> path.normalizedPath() in expectedPathTexts }
            }
            .filterValues { paths -> paths.isNotEmpty() }
    }

    private fun GitStageTracker.State.hasStatusEntries(): Boolean = rootStates.values
        .any { rootState -> rootState.statuses.isNotEmpty() }

    private fun stagePaths(
        pathsToStageByRoot: Map<VirtualFile, List<FilePath>>,
        attempt: Int,
    ): Boolean = runCatching {
        val startedAtNanos = System.nanoTime()
        pathsToStageByRoot.forEach { (root, paths) ->
            operations.stagePaths(root, paths)
        }
        logger.info(
            "AI Commit All diagnostic: staging confirmation git add completed, " +
                "attempt=${attempt + 1}/$attempts, roots=${pathsToStageByRoot.size}, " +
                "paths=${pathsToStageByRoot.values.sumOf { paths -> paths.size }}, " +
                "elapsedMs=${elapsedMillisSince(startedAtNanos)}",
        )
    }.fold(
        onSuccess = { true },
        onFailure = { exception ->
            logger.info(
                "AI Commit All diagnostic: staging confirmation git add failed, " +
                    "attempt=${attempt + 1}/$attempts, " +
                    "exception=${exception.javaClass.name}, " +
                    "cause=${exception.cause?.javaClass?.name ?: "<none>"}",
            )
            false
        },
    )

    private fun refreshAfterDirectConfirmation(
        distinctExpectedPaths: Collection<FilePath>,
        attempt: Int,
    ): GitStageTracker.State? = runCatching {
        val startedAtNanos = System.nanoTime()
        operations.reloadExternalFiles(distinctExpectedPaths)
        operations.markPathsDirty(distinctExpectedPaths)
        operations.waitForStatusRefresh()
        operations.refreshTrackerState().also {
            logger.info(
                "AI Commit All diagnostic: staging confirmation fallback IDE refresh completed, " +
                    "attempt=${attempt + 1}/$attempts, elapsedMs=${elapsedMillisSince(startedAtNanos)}",
            )
        }
    }.getOrElse { exception ->
        logger.info(
            "AI Commit All diagnostic: staging confirmation attempt failed, " +
                "attempt=${attempt + 1}/$attempts, " +
                "exception=${exception.javaClass.name}, " +
                "cause=${exception.cause?.javaClass?.name ?: "<none>"}",
        )
        null
    }

    private companion object {
        private const val DEFAULT_RETRY_DELAY_MILLIS = 250L
        val logger: Logger = Logger.getInstance(GitStageConfirmation::class.java)
        val DEFAULT_RETRY_DELAY: Duration = Duration.ofMillis(DEFAULT_RETRY_DELAY_MILLIS)
    }
}

private fun GitStageTracker.State.withGitIndexConfirmations(
    expectedPathsByRoot: Map<VirtualFile, List<FilePath>>,
    confirmationsByPath: Map<String, GitIndexConfirmation>,
): GitStageTracker.State {
    val updatedRootStates = rootStates.toMutableMap()
    expectedPathsByRoot.forEach { (root, paths) ->
        val rootState = updatedRootStates[root]
        val updatedStatuses = rootState?.statuses.orEmpty().toMutableMap()
        paths.forEach { path ->
            when (confirmationsByPath[path.normalizedPath()]) {
                GitIndexConfirmation.STAGED -> updatedStatuses.stageIndexConfirmedPath(path)

                GitIndexConfirmation.HEAD_IDENTICAL -> updatedStatuses.removeIndexConfirmedPath(path)

                GitIndexConfirmation.UNCONFIRMED,
                null,
                -> Unit
            }
        }
        updatedRootStates[root] = GitStageTracker.RootState(root, true, updatedStatuses)
    }
    return GitStageTracker.State(updatedRootStates)
}

private fun MutableMap<FilePath, GitFileStatus>.stageIndexConfirmedPath(path: FilePath) {
    val existingStatus = values.firstOrNull { status -> status.reports(path) }
    removeIndexConfirmedPath(path)
    val stagedStatus = existingStatus?.asIndexConfirmedStagedStatus()
        ?: GitFileStatus(DEFAULT_SYNTHETIC_STAGED_INDEX, ' ', path, null)
    this[stagedStatus.path] = stagedStatus
}

private fun MutableMap<FilePath, GitFileStatus>.removeIndexConfirmedPath(path: FilePath) {
    val matchingKeys = filter { (statusPath, status) ->
        statusPath.normalizedPath() == path.normalizedPath() || status.reports(path)
    }.keys
    matchingKeys.forEach { statusPath -> remove(statusPath) }
}

private fun GitFileStatus.asIndexConfirmedStagedStatus(): GitFileStatus = copy(
    stagedIndex(),
    ' ',
    path,
    origPath,
)

private fun GitFileStatus.stagedIndex(): Char = when {
    index.isStagedIndex() -> index
    workTree.isWorkTreeChange() -> if (workTree == '?') 'A' else workTree
    else -> DEFAULT_SYNTHETIC_STAGED_INDEX
}

private fun GitFileStatus.reports(expectedPath: FilePath): Boolean {
    val normalizedPath = expectedPath.normalizedPath()
    return path.normalizedPath() == normalizedPath || origPath?.normalizedPath() == normalizedPath
}

private fun Char.isStagedIndex(): Boolean = this != ' ' && this != '?'

private fun Char.isWorkTreeChange(): Boolean = this != ' '

private fun FilePath.normalizedPath(): String = path.replace('\\', '/')

internal interface GitStageConfirmationOperations {
    fun stagePaths(root: VirtualFile, paths: List<FilePath>)

    fun reloadExternalFiles(paths: Collection<FilePath>)

    fun markPathsDirty(paths: Collection<FilePath>) = Unit

    fun waitForStatusRefresh() = Unit

    fun refreshTrackerState(): GitStageTracker.State

    fun currentTrackerState(): GitStageTracker.State

    fun confirmIndexPath(root: VirtualFile, path: FilePath): GitIndexConfirmation = GitIndexConfirmation.UNCONFIRMED
}

internal enum class GitIndexConfirmation {
    STAGED,
    HEAD_IDENTICAL,
    UNCONFIRMED,
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

    override fun currentTrackerState(): GitStageTracker.State = tracker.state

    override fun confirmIndexPath(root: VirtualFile, path: FilePath): GitIndexConfirmation = when {
        hasStagedIndexContent(root, path) -> GitIndexConfirmation.STAGED
        hasAnyStatus(root, path) -> GitIndexConfirmation.UNCONFIRMED
        else -> GitIndexConfirmation.HEAD_IDENTICAL
    }

    private fun hasStagedIndexContent(root: VirtualFile, path: FilePath): Boolean {
        val handler = GitLineHandler(project, root, GitCommand.DIFF)
        handler.addParameters("--cached", "--quiet")
        handler.endOptions()
        handler.addRelativePaths(listOf(path))

        val result = Git.getInstance().runCommand(handler)
        return when (result.exitCode) {
            GIT_DIFF_NO_CHANGES -> false

            GIT_DIFF_HAS_CHANGES -> true

            else -> {
                result.throwOnError(GIT_DIFF_NO_CHANGES, GIT_DIFF_HAS_CHANGES)
                false
            }
        }
    }

    private fun hasAnyStatus(root: VirtualFile, path: FilePath): Boolean {
        val handler = GitLineHandler(project, root, GitCommand.STATUS)
        handler.addParameters("--porcelain=v1", "--untracked-files=all")
        handler.endOptions()
        handler.addRelativePaths(listOf(path))

        val result = Git.getInstance().runCommand(handler)
        result.throwOnError()
        return result.output.any { line -> line.isNotBlank() }
    }

    private fun CountDownLatch.awaitBounded(timeout: Duration) {
        try {
            await(timeout.toMillis(), TimeUnit.MILLISECONDS)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private companion object {
        private const val GIT_DIFF_NO_CHANGES = 0
        private const val GIT_DIFF_HAS_CHANGES = 1
        val STATUS_REFRESH_TIMEOUT: Duration = Duration.ofSeconds(2)
        val TRACKER_REFRESH_TIMEOUT: Duration = Duration.ofSeconds(2)
    }
}

private fun elapsedMillisSince(startedAtNanos: Long): Long = Duration.ofNanos(System.nanoTime() - startedAtNanos)
    .toMillis()
