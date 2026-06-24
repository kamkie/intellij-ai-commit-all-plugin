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

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems
import java.io.File
import java.nio.charset.Charset
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GitStageConfirmationTest {
    @Test
    fun `reloads external files before reading refreshed tracker state`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(confirmed)

        val result = confirmation(operations, attempts = 1)
            .confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/modified.txt",
                "reload:/repo/modified.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `marks staged paths dirty and waits for status refresh before tracker refresh`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(confirmed)

        val result = confirmation(operations, attempts = 1)
            .confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:/repo/modified.txt",
                "reload:/repo/modified.txt",
                "dirty:/repo/modified.txt",
                "wait-status",
                "refresh",
            ),
            operations.refreshBoundaryEvents,
        )
    }

    @Test
    fun `retries staging reload and tracker refresh until every expected path is staged`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val untracked = TestFilePath("/repo/untracked.txt")
        val notYetStaged = stageState(
            root,
            gitStatus(' ', 'M', modified),
            gitStatus('?', '?', untracked),
        )
        val confirmed = stageState(
            root,
            gitStatus('M', ' ', modified),
            gitStatus('A', ' ', untracked),
        )
        val operations = CapturingOperations(notYetStaged, confirmed)

        val result = confirmation(operations, attempts = 3)
            .confirm(mapOf(root to listOf(modified, untracked)))

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/modified.txt,/repo/untracked.txt",
                "reload:/repo/modified.txt,/repo/untracked.txt",
                "refresh",
                "stage:repo:/repo/modified.txt,/repo/untracked.txt",
                "reload:/repo/modified.txt,/repo/untracked.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `retries after transient empty tracker state before treating staged paths as missing`() {
        val root = LightVirtualFile("repo")
        val alreadyStaged = TestFilePath("/repo/already-staged.txt")
        val empty = GitStageTracker.State(emptyMap())
        val confirmed = stageState(root, gitStatus('M', ' ', alreadyStaged))
        val operations = CapturingOperations(empty, confirmed)

        val result = confirmation(operations, attempts = 3)
            .confirm(mapOf(root to listOf(alreadyStaged)))

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/already-staged.txt",
                "reload:/repo/already-staged.txt",
                "refresh",
                "stage:repo:/repo/already-staged.txt",
                "reload:/repo/already-staged.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `confirms head-identical expected path absent from refreshed initialized state`() {
        val root = LightVirtualFile("repo")
        val headIdentical = TestFilePath("/repo/line-endings-only.txt")
        val refreshed = stageState(root)
        val operations = CapturingOperations(refreshed)

        val result = confirmation(operations, attempts = 3)
            .confirm(mapOf(root to listOf(headIdentical)))

        assertSame(refreshed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/line-endings-only.txt",
                "reload:/repo/line-endings-only.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `re-stages only reported paths while a head-identical path stays satisfied`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val headIdentical = TestFilePath("/repo/line-endings-only.txt")
        val partial = stageState(root, gitStatus(' ', 'M', modified))
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(partial, confirmed)

        val result = confirmation(operations, attempts = 3)
            .confirm(mapOf(root to listOf(modified, headIdentical)))

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/modified.txt,/repo/line-endings-only.txt",
                "reload:/repo/modified.txt,/repo/line-endings-only.txt",
                "refresh",
                "stage:repo:/repo/modified.txt",
                "reload:/repo/modified.txt,/repo/line-endings-only.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `confirms already staged paths without re-staging them`() {
        val root = LightVirtualFile("repo")
        val stagedDeletion = TestFilePath("/repo/delete-me.txt")
        val confirmed = stageState(root, gitStatus('D', ' ', stagedDeletion))
        val operations = CapturingOperations(confirmed)

        val result = confirmation(operations, attempts = 1)
            .confirm(
                pathsByRoot = emptyMap(),
                expectedPaths = listOf(stagedDeletion),
            )

        assertSame(confirmed, result)
        assertEquals(0, operations.stageCallCount)
        assertEquals(
            listOf(
                "reload:/repo/delete-me.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `waits between failed confirmation attempts for delayed staging tracker updates`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val notYetStaged = stageState(root, gitStatus(' ', 'M', modified))
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(notYetStaged, confirmed)
        val sleeper = CapturingSleeper()

        val result = confirmation(
            operations = operations,
            attempts = 3,
            sleeper = sleeper,
        ).confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(listOf(RETRY_DELAY), sleeper.delays)
    }

    @Test
    fun `fails closed after the bounded retry count when staged paths never appear`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val notStaged = stageState(root, gitStatus(' ', 'M', modified))
        val operations = CapturingOperations(notStaged, notStaged, notStaged, notStaged)

        val result = confirmation(operations, attempts = 3)
            .confirm(mapOf(root to listOf(modified)))

        assertNull(result)
        assertEquals(3, operations.stageCallCount)
        assertEquals(3, operations.reloadCallCount)
        assertEquals(3, operations.refreshCallCount)
    }

    @Test
    fun `returns index confirmed staged state when git index shows staged content`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val stale = stageState(root, gitStatus(' ', 'M', modified))
        val operations = CapturingOperations(stale)
        operations.indexConfirmations[modified.normalizedPath()] = GitIndexConfirmation.STAGED

        val result = confirmation(operations, attempts = 1)
            .confirm(
                pathsByRoot = mapOf(root to listOf(modified)),
                expectedPathsByRoot = mapOf(root to listOf(modified)),
            )

        val confirmed = result ?: error("Expected index-confirmed tracker state.")
        assertTrue(GitStageSelectionItems.containsAllStagedPaths(confirmed, listOf(modified)))
        assertEquals(listOf("index:repo:/repo/modified.txt"), operations.indexConfirmationEvents)
    }

    @Test
    fun `does not wait for tracker resync after git index confirms staged content`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val stale = stageState(root, gitStatus(' ', 'M', modified))
        val operations = CapturingOperations(stale)
        operations.indexConfirmations[modified.normalizedPath()] = GitIndexConfirmation.STAGED

        val result = confirmation(operations, attempts = 1)
            .confirm(
                pathsByRoot = mapOf(root to listOf(modified)),
                expectedPathsByRoot = mapOf(root to listOf(modified)),
            )

        val confirmed = result ?: error("Expected index-confirmed tracker state.")
        assertTrue(GitStageSelectionItems.containsAllStagedPaths(confirmed, listOf(modified)))
        assertEquals(
            listOf(
                "stage:/repo/modified.txt",
                "reload:/repo/modified.txt",
                "dirty:/repo/modified.txt",
                "wait-status",
                "refresh",
            ),
            operations.refreshBoundaryEvents,
        )
        assertEquals(1, operations.refreshCallCount)
    }

    @Test
    fun `returns index confirmed staged state when refreshed tracker has no statuses`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val empty = GitStageTracker.State(emptyMap())
        val operations = CapturingOperations(empty)
        operations.indexConfirmations[modified.normalizedPath()] = GitIndexConfirmation.STAGED

        val result = confirmation(operations, attempts = 1)
            .confirm(
                pathsByRoot = mapOf(root to listOf(modified)),
                expectedPathsByRoot = mapOf(root to listOf(modified)),
            )

        val confirmed = result ?: error("Expected index-confirmed tracker state.")
        assertTrue(GitStageSelectionItems.containsAllStagedPaths(confirmed, listOf(modified)))
        assertEquals(setOf(root), confirmed.rootStates.keys)
        assertEquals(1, operations.refreshCallCount)
    }

    @Test
    fun `returns initialized state when git index reports no status for expected path`() {
        val root = LightVirtualFile("repo")
        val headIdentical = TestFilePath("/repo/line-endings-only.txt")
        val stale = stageState(root, gitStatus(' ', 'M', headIdentical))
        val operations = CapturingOperations(stale)
        operations.indexConfirmations[headIdentical.normalizedPath()] = GitIndexConfirmation.HEAD_IDENTICAL

        val result = confirmation(operations, attempts = 1)
            .confirm(
                pathsByRoot = mapOf(root to listOf(headIdentical)),
                expectedPathsByRoot = mapOf(root to listOf(headIdentical)),
            )

        val confirmed = result ?: error("Expected index-confirmed tracker state.")
        assertTrue(GitStageSelectionItems.containsAllStagedPaths(confirmed, listOf(headIdentical)))
        assertEquals(emptyMap(), confirmed.rootStates.getValue(root).statuses)
        assertEquals(listOf("index:repo:/repo/line-endings-only.txt"), operations.indexConfirmationEvents)
    }

    @Test
    fun `fails closed when git index fallback still reports only worktree changes`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val notStaged = stageState(root, gitStatus(' ', 'M', modified))
        val operations = CapturingOperations(notStaged)
        operations.indexConfirmations[modified.normalizedPath()] = GitIndexConfirmation.UNCONFIRMED

        val result = confirmation(operations, attempts = 1)
            .confirm(
                pathsByRoot = mapOf(root to listOf(modified)),
                expectedPathsByRoot = mapOf(root to listOf(modified)),
            )

        assertNull(result)
        assertEquals(listOf("index:repo:/repo/modified.txt"), operations.indexConfirmationEvents)
    }

    @Test
    fun `fails closed when every tracker refresh attempt fails`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val operations = CapturingOperations()
        operations.failRefreshCalls += 1
        operations.failRefreshCalls += 2

        val result = confirmation(operations, attempts = 2)
            .confirm(mapOf(root to listOf(modified)))

        assertNull(result)
        assertEquals(2, operations.refreshCallCount)
    }

    @Test
    fun `does not stage reload or refresh when there are no expected paths`() {
        val operations = CapturingOperations()

        val result = confirmation(operations, attempts = 3)
            .confirm(emptyMap())

        assertNull(result)
        assertEquals(emptyList(), operations.events)
    }

    @Test
    fun `does not stage reload or refresh when attempts are exhausted before starting`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(confirmed)

        val result = confirmation(operations, attempts = 0)
            .confirm(mapOf(root to listOf(modified)))

        assertNull(result)
        assertEquals(emptyList(), operations.events)
    }

    @Test
    fun `retries only remaining unstaged roots after refreshed tracker state`() {
        val firstRoot = LightVirtualFile("repo-a")
        val secondRoot = LightVirtualFile("repo-b")
        val firstPath = TestFilePath("/repo-a/modified.txt")
        val secondPath = TestFilePath("/repo-b/added.txt")
        val partial = GitStageTracker.State(
            mapOf(
                firstRoot to GitStageTracker.RootState(
                    firstRoot,
                    true,
                    mapOf(firstPath to gitStatus('M', ' ', firstPath)),
                ),
                secondRoot to GitStageTracker.RootState(
                    secondRoot,
                    true,
                    mapOf(secondPath to gitStatus('?', '?', secondPath)),
                ),
            ),
        )
        val confirmed = GitStageTracker.State(
            mapOf(
                firstRoot to GitStageTracker.RootState(
                    firstRoot,
                    true,
                    mapOf(firstPath to gitStatus('M', ' ', firstPath)),
                ),
                secondRoot to GitStageTracker.RootState(
                    secondRoot,
                    true,
                    mapOf(secondPath to gitStatus('A', ' ', secondPath)),
                ),
            ),
        )
        val operations = CapturingOperations(partial, confirmed)

        val result = confirmation(operations, attempts = 3)
            .confirm(
                linkedMapOf(
                    firstRoot to listOf(firstPath),
                    secondRoot to listOf(secondPath),
                ),
            )

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo-a:/repo-a/modified.txt",
                "stage:repo-b:/repo-b/added.txt",
                "reload:/repo-a/modified.txt,/repo-b/added.txt",
                "refresh",
                "stage:repo-b:/repo-b/added.txt",
                "reload:/repo-a/modified.txt,/repo-b/added.txt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `refreshes paths to stage after a staged move source becomes stale`() {
        val root = LightVirtualFile("repo")
        val moveSource = TestFilePath("/repo/proposals/draft.md")
        val moveTarget = TestFilePath("/repo/proposals/archive/draft.md")
        val remaining = TestFilePath("/repo/PLANNING.md")
        val partiallyConfirmed = stageState(
            root,
            gitStatus('R', ' ', moveTarget, moveSource),
            gitStatus(' ', 'M', remaining),
        )
        val confirmed = stageState(
            root,
            gitStatus('R', ' ', moveTarget, moveSource),
            gitStatus('M', ' ', remaining),
        )
        val operations = CapturingOperations(partiallyConfirmed, confirmed)

        val result = confirmation(operations, attempts = 2)
            .confirm(
                pathsByRoot = mapOf(root to listOf(moveSource, moveTarget, remaining)),
                expectedPaths = listOf(moveSource, moveTarget, remaining),
            )

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/proposals/draft.md,/repo/proposals/archive/draft.md,/repo/PLANNING.md",
                "reload:/repo/proposals/draft.md,/repo/proposals/archive/draft.md,/repo/PLANNING.md",
                "refresh",
                "stage:repo:/repo/PLANNING.md",
                "reload:/repo/proposals/draft.md,/repo/proposals/archive/draft.md,/repo/PLANNING.md",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `reloads each expected external file only once by normalized path`() {
        val root = LightVirtualFile("repo")
        val slashPath = TestFilePath("/repo/products/idea/plugin/src/Main.kt")
        val backslashPath = TestFilePath("\\repo\\products\\idea\\plugin\\src\\Main.kt")
        val confirmed = stageState(root, gitStatus('M', ' ', slashPath))
        val operations = CapturingOperations(confirmed)

        val result = confirmation(operations, attempts = 1)
            .confirm(mapOf(root to listOf(slashPath, backslashPath)))

        assertSame(confirmed, result)
        assertEquals(
            listOf<List<FilePath>>(
                listOf(slashPath),
            ),
            operations.reloadedPaths,
        )
    }

    @Test
    fun `stages each path only once by normalized path`() {
        val root = LightVirtualFile("repo")
        val slashPath = TestFilePath("/repo/products/idea/plugin/src/Main.kt")
        val backslashPath = TestFilePath("\\repo\\products\\idea\\plugin\\src\\Main.kt")
        val confirmed = stageState(root, gitStatus('M', ' ', slashPath))
        val operations = CapturingOperations(confirmed)

        val result = confirmation(operations, attempts = 1)
            .confirm(mapOf(root to listOf(slashPath, backslashPath)))

        assertSame(confirmed, result)
        assertEquals(
            listOf(
                "stage:repo:/repo/products/idea/plugin/src/Main.kt",
                "reload:/repo/products/idea/plugin/src/Main.kt",
                "refresh",
            ),
            operations.events,
        )
    }

    @Test
    fun `retries after staging command failure before invoking AI generation`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(confirmed)
        operations.failStageCalls += 1

        val result = confirmation(operations, attempts = 2)
            .confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(2, operations.stageCallCount)
        assertEquals(1, operations.reloadCallCount)
        assertEquals(1, operations.refreshCallCount)
    }

    @Test
    fun `retries after external file reload failure before trusting tracker state`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(confirmed)
        operations.failReloadCalls += 1

        val result = confirmation(operations, attempts = 2)
            .confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(2, operations.stageCallCount)
        assertEquals(2, operations.reloadCallCount)
        assertEquals(1, operations.refreshCallCount)
    }

    @Test
    fun `retries after tracker refresh failure and then confirms refreshed state`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val operations = CapturingOperations(confirmed)
        operations.failRefreshCalls += 1

        val result = confirmation(operations, attempts = 2)
            .confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(2, operations.stageCallCount)
        assertEquals(2, operations.reloadCallCount)
        assertEquals(2, operations.refreshCallCount)
    }

    @Test
    fun `stops retrying after the first confirmed staged state`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val confirmed = stageState(root, gitStatus('M', ' ', modified))
        val unused = stageState(root, gitStatus(' ', 'M', modified))
        val operations = CapturingOperations(confirmed, unused, unused)

        val result = confirmation(operations, attempts = 3)
            .confirm(mapOf(root to listOf(modified)))

        assertSame(confirmed, result)
        assertEquals(1, operations.stageCallCount)
        assertEquals(1, operations.reloadCallCount)
        assertEquals(1, operations.refreshCallCount)
    }

    @Test
    fun `does not wait after the final failed confirmation attempt`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/modified.txt")
        val notStaged = stageState(root, gitStatus(' ', 'M', modified))
        val operations = CapturingOperations(notStaged, notStaged)
        val sleeper = CapturingSleeper()

        val result = confirmation(
            operations = operations,
            attempts = 2,
            sleeper = sleeper,
        ).confirm(mapOf(root to listOf(modified)))

        assertNull(result)
        assertEquals(listOf(RETRY_DELAY), sleeper.delays)
    }

    private fun confirmation(
        operations: CapturingOperations,
        attempts: Int,
        sleeper: GitStageConfirmationSleeper = CapturingSleeper(),
    ): GitStageConfirmation = GitStageConfirmation(
        attempts = attempts,
        operations = operations,
        retryDelay = RETRY_DELAY,
        sleeper = sleeper,
    )

    private class CapturingSleeper : GitStageConfirmationSleeper {
        val delays = mutableListOf<Duration>()

        override fun sleep(duration: Duration) {
            delays += duration
        }
    }

    private class CapturingOperations(
        vararg states: GitStageTracker.State,
    ) : GitStageConfirmationOperations {
        private val states = ArrayDeque(states.toList())
        val events = mutableListOf<String>()
        val refreshBoundaryEvents = mutableListOf<String>()
        val failStageCalls = mutableSetOf<Int>()
        val failReloadCalls = mutableSetOf<Int>()
        val failRefreshCalls = mutableSetOf<Int>()
        val indexConfirmations = mutableMapOf<String, GitIndexConfirmation>()
        val indexConfirmationEvents = mutableListOf<String>()
        val reloadedPaths = mutableListOf<List<FilePath>>()
        var stageCallCount = 0
        var reloadCallCount = 0
        var refreshCallCount = 0

        override fun stagePaths(root: VirtualFile, paths: List<FilePath>) {
            stageCallCount += 1
            events += "stage:${root.name}:${pathEventText(paths)}"
            refreshBoundaryEvents += "stage:${pathEventText(paths)}"
            if (stageCallCount in failStageCalls) {
                error("staging failed")
            }
        }

        override fun reloadExternalFiles(paths: Collection<FilePath>) {
            reloadCallCount += 1
            val pathList = paths.toList()
            reloadedPaths += pathList
            events += "reload:${pathEventText(pathList)}"
            refreshBoundaryEvents += "reload:${pathEventText(pathList)}"
            if (reloadCallCount in failReloadCalls) {
                error("reload failed")
            }
        }

        override fun markPathsDirty(paths: Collection<FilePath>) {
            refreshBoundaryEvents += "dirty:${pathEventText(paths)}"
        }

        override fun waitForStatusRefresh() {
            refreshBoundaryEvents += "wait-status"
        }

        override fun refreshTrackerState(): GitStageTracker.State {
            refreshCallCount += 1
            events += "refresh"
            refreshBoundaryEvents += "refresh"
            if (refreshCallCount in failRefreshCalls) {
                error("tracker refresh failed")
            }
            return states.removeFirstOrNull() ?: GitStageTracker.State(emptyMap())
        }

        override fun confirmIndexPath(root: VirtualFile, path: FilePath): GitIndexConfirmation {
            indexConfirmationEvents += "index:${root.name}:${path.path}"
            return indexConfirmations[path.normalizedPath()] ?: GitIndexConfirmation.UNCONFIRMED
        }
    }

    private fun stageState(
        root: VirtualFile,
        vararg statuses: GitFileStatus,
    ): GitStageTracker.State = GitStageTracker.State(
        mapOf(
            root to GitStageTracker.RootState(
                root,
                true,
                statuses.associateBy { status -> status.path },
            ),
        ),
    )

    private fun gitStatus(
        index: Char,
        workTree: Char,
        path: FilePath,
        origPath: FilePath? = null,
    ): GitFileStatus = GitFileStatus(index, workTree, path, origPath)

    private companion object {
        val RETRY_DELAY: Duration = Duration.ofMillis(250)

        fun pathEventText(paths: Collection<FilePath>): String = paths.joinToString(",") { path -> path.path }

        fun FilePath.normalizedPath(): String = path.replace('\\', '/')
    }

    private class TestFilePath(private val rawPath: String) : FilePath {
        override fun getVirtualFile(): VirtualFile? = null

        override fun getVirtualFileParent(): VirtualFile? = null

        override fun getIOFile(): File = File(rawPath)

        override fun getName(): String = ioFile.name

        override fun getPresentableUrl(): String = rawPath

        override fun getCharset(): Charset = Charsets.UTF_8

        override fun getCharset(project: Project?): Charset = Charsets.UTF_8

        override fun getFileType(): FileType = PlainTextFileType.INSTANCE

        override fun getPath(): String = rawPath

        override fun isDirectory(): Boolean = false

        override fun isUnder(parent: FilePath, strict: Boolean): Boolean = false

        override fun getParentPath(): FilePath? = null

        override fun isNonLocal(): Boolean = false
    }
}
