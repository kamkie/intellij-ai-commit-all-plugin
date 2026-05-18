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
import java.io.File
import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `accepts non ignored git file paths`() {
        val path = TestFilePath("/repo/new-file.txt")

        val result = GitChangeSelectionFilters.isGitPath(
            path = path,
            vcsNameForPath = { GIT_VCS_NAME },
            isIgnored = { false },
        )

        assertTrue(result)
    }

    @Test
    fun `rejects ignored git file paths`() {
        val path = TestFilePath("/repo/build/output.txt")

        val result = GitChangeSelectionFilters.isGitPath(
            path = path,
            vcsNameForPath = { GIT_VCS_NAME },
            isIgnored = { true },
        )

        assertFalse(result)
    }

    @Test
    fun `rejects non git file paths`() {
        val path = TestFilePath("/repo/new-file.txt")

        val result = GitChangeSelectionFilters.isGitPath(
            path = path,
            vcsNameForPath = { "Mercurial" },
            isIgnored = { false },
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
