package pl.devopssolutions.aicommitall.vcs

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File
import java.nio.charset.Charset

internal class GitChangeSelectionFiltersTest {
    @Test
    fun `accepts git backed tracked changes`() {
        val change = modification("/repo/src/App.kt")

        val result = GitChangeSelectionFilters.isGitChange(
            change = change,
            vcsNameForPath = { GIT_VCS_NAME },
            isIgnored = { false },
        )

        assertTrue(result)
    }

    @Test
    fun `rejects non git tracked changes`() {
        val change = modification("/repo/src/App.kt")

        val result = GitChangeSelectionFilters.isGitChange(
            change = change,
            vcsNameForPath = { "Subversion" },
            isIgnored = { false },
        )

        assertFalse(result)
    }

    @Test
    fun `rejects mixed vcs move changes`() {
        val before = TestFilePath("/repo/src/Old.kt")
        val after = TestFilePath("/repo/src/New.kt")
        val change = Change(TestContentRevision(before), TestContentRevision(after), FileStatus.MODIFIED)

        val result = GitChangeSelectionFilters.isGitChange(
            change = change,
            vcsNameForPath = { path -> if (path.path.endsWith("Old.kt")) GIT_VCS_NAME else "Mercurial" },
            isIgnored = { false },
        )

        assertFalse(result)
    }

    @Test
    fun `rejects ignored tracked paths`() {
        val change = modification("/repo/generated.txt")

        val result = GitChangeSelectionFilters.isGitChange(
            change = change,
            vcsNameForPath = { GIT_VCS_NAME },
            isIgnored = { true },
        )

        assertFalse(result)
    }

    private fun modification(path: String): Change {
        val filePath = TestFilePath(path)
        return Change(TestContentRevision(filePath), TestContentRevision(filePath), FileStatus.MODIFIED)
    }

    private class TestContentRevision(private val filePath: FilePath) : ContentRevision {
        override fun getContent(): String? = null

        override fun getFile(): FilePath = filePath

        override fun getRevisionNumber(): VcsRevisionNumber = VcsRevisionNumber.NULL
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

        override fun isUnder(parent: FilePath, strict: Boolean): Boolean {
            val parentPath = parent.path.trimEnd('/')
            return rawPath.startsWith("$parentPath/") && (!strict || rawPath != parentPath)
        }

        override fun getParentPath(): FilePath? {
            val parent = ioFile.parent ?: return null
            return TestFilePath(parent.replace(File.separatorChar, '/'))
        }

        override fun isNonLocal(): Boolean = false
    }
}
