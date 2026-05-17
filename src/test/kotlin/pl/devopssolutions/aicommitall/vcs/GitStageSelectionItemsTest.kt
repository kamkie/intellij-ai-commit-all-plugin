package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File
import java.nio.charset.Charset

internal class GitStageSelectionItemsTest {
    @Test
    fun `staging-area paths make a selection committable`() {
        val selection = GitChangeSelection(
            trackedChanges = emptyList(),
            stagingAreaPaths = listOf(TestFilePath("/repo/modified.txt")),
        )

        assertTrue(selection.hasCommittableContent)
    }

    @Test
    fun `keeps non ignored changed paths from git staging state`() {
        val modified = TestFilePath("/repo/modified.txt")
        val untracked = TestFilePath("/repo/untracked.txt")
        val ignored = TestFilePath("/repo/ignored.txt")
        val unchanged = TestFilePath("/repo/unchanged.txt")
        val state = stageState(
            gitStatus(' ', 'M', modified),
            gitStatus('?', '?', untracked),
            gitStatus('!', '!', ignored),
            gitStatus(' ', ' ', unchanged),
        )

        val result = GitStageSelectionItems.committablePaths(
            state = state,
            isGitPath = { true },
        )

        assertEquals(listOf(modified, untracked), result)
    }

    @Test
    fun `groups committable staging paths by git root`() {
        val firstRoot = LightVirtualFile("root-a")
        val secondRoot = LightVirtualFile("root-b")
        val firstPath = TestFilePath("/repo-a/modified.txt")
        val secondPath = TestFilePath("/repo-b/staged.txt")
        val ignored = TestFilePath("/repo-b/ignored.txt")
        val state = GitStageTracker.State(
            mapOf(
                firstRoot to GitStageTracker.RootState(
                    firstRoot,
                    true,
                    mapOf(firstPath to gitStatus(' ', 'M', firstPath)),
                ),
                secondRoot to GitStageTracker.RootState(
                    secondRoot,
                    true,
                    mapOf(
                        secondPath to gitStatus('A', ' ', secondPath),
                        ignored to gitStatus('!', '!', ignored),
                    ),
                ),
            ),
        )

        val result = GitStageSelectionItems.committablePathsByRoot(state)

        assertEquals(
            mapOf<VirtualFile, List<FilePath>>(
                firstRoot to listOf(firstPath),
                secondRoot to listOf(secondPath),
            ),
            result,
        )
    }

    @Test
    fun `confirms expected paths only when they are staged`() {
        val staged = TestFilePath("/repo/staged.txt")
        val unstaged = TestFilePath("/repo/unstaged.txt")
        val untracked = TestFilePath("/repo/untracked.txt")
        val state = stageState(
            gitStatus('M', ' ', staged),
            gitStatus(' ', 'M', unstaged),
            gitStatus('?', '?', untracked),
        )

        assertTrue(GitStageSelectionItems.containsAllStagedPaths(state, listOf(staged)))
        assertFalse(GitStageSelectionItems.containsAllStagedPaths(state, listOf(staged, unstaged)))
        assertEquals(
            listOf(unstaged, untracked),
            GitStageSelectionItems.missingStagedPaths(state, listOf(staged, unstaged, untracked)),
        )
    }

    @Test
    fun `matches refreshed staged paths by path text`() {
        val expected = TestFilePath("/repo/modified.txt")
        val refreshed = TestFilePath("/repo/modified.txt")
        val state = stageState(gitStatus('M', ' ', refreshed))

        assertTrue(GitStageSelectionItems.containsAllStagedPaths(state, listOf(expected)))
    }

    private fun stageState(vararg statuses: GitFileStatus): GitStageTracker.State {
        val root = LightVirtualFile("repo")
        return GitStageTracker.State(
            mapOf(
                root to GitStageTracker.RootState(
                    root,
                    true,
                    statuses.associateBy { status -> status.path },
                ),
            ),
        )
    }

    private fun gitStatus(
        index: Char,
        workTree: Char,
        path: FilePath,
    ): GitFileStatus = GitFileStatus(index, workTree, path, null)

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
