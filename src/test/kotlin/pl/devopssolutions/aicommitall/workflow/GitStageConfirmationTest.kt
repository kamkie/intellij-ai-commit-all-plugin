package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker
import java.io.File
import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

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
    fun `does not stage reload or refresh when there are no expected paths`() {
        val operations = CapturingOperations()

        val result = confirmation(operations, attempts = 3)
            .confirm(emptyMap())

        assertNull(result)
        assertEquals(emptyList(), operations.events)
    }

    @Test
    fun `stages every root on every retry before refreshing the tracker`() {
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
                "stage:repo-a:/repo-a/modified.txt",
                "stage:repo-b:/repo-b/added.txt",
                "reload:/repo-a/modified.txt,/repo-b/added.txt",
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

    private fun confirmation(
        operations: CapturingOperations,
        attempts: Int,
    ): GitStageConfirmation =
        GitStageConfirmation(
            attempts = attempts,
            operations = operations,
        )

    private class CapturingOperations(
        vararg states: GitStageTracker.State,
    ) : GitStageConfirmationOperations {
        private val states = ArrayDeque(states.toList())
        val events = mutableListOf<String>()
        val failStageCalls = mutableSetOf<Int>()
        val failReloadCalls = mutableSetOf<Int>()
        val failRefreshCalls = mutableSetOf<Int>()
        val reloadedPaths = mutableListOf<List<FilePath>>()
        var stageCallCount = 0
        var reloadCallCount = 0
        var refreshCallCount = 0

        override fun stagePaths(root: VirtualFile, paths: List<FilePath>) {
            stageCallCount += 1
            events += "stage:${root.name}:${pathEventText(paths)}"
            if (stageCallCount in failStageCalls) {
                error("staging failed")
            }
        }

        override fun reloadExternalFiles(paths: Collection<FilePath>) {
            reloadCallCount += 1
            val pathList = paths.toList()
            reloadedPaths += pathList
            events += "reload:${pathEventText(pathList)}"
            if (reloadCallCount in failReloadCalls) {
                error("reload failed")
            }
        }

        override fun refreshTrackerState(): GitStageTracker.State {
            refreshCallCount += 1
            events += "refresh"
            if (refreshCallCount in failRefreshCalls) {
                error("tracker refresh failed")
            }
            return states.removeFirstOrNull() ?: GitStageTracker.State(emptyMap())
        }
    }

    private fun stageState(
        root: VirtualFile,
        vararg statuses: GitFileStatus,
    ): GitStageTracker.State =
        GitStageTracker.State(
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
    ): GitFileStatus = GitFileStatus(index, workTree, path, null)

    private companion object {
        fun pathEventText(paths: Collection<FilePath>): String =
            paths.joinToString(",") { path -> path.path }
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
