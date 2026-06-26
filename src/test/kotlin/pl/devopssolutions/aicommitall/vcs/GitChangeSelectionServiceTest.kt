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
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitChangeSelectionServiceTest {
    @Test
    fun `support status reflects active vcs names`() {
        val supported = service(TestEnvironment(activeVcsNames = listOf(GIT_VCS_NAME)))
        val mixed = service(TestEnvironment(activeVcsNames = listOf(GIT_VCS_NAME, "Subversion")))

        assertEquals(GitVcsSupportStatus.Supported, supported.supportStatus())
        assertEquals(
            GitVcsSupportStatus.UnsupportedMixedVcs(listOf("Subversion")),
            mixed.supportStatus(),
        )
    }

    @Test
    fun `collect selection keeps distinct eligible git paths across all selection sources`() {
        val tracked = modification("/repo/src/App.kt")
        val ignoredTracked = modification("/repo/build/generated.txt")
        val nonGitTracked = modification("/external/Legacy.kt")
        val unversioned = TestFilePath("/repo/new-file.txt")
        val duplicateUnversioned = TestFilePath("/repo/new-file.txt")
        val ignoredUnversioned = TestFilePath("/repo/build/new-generated.txt")
        val nonGitUnversioned = TestFilePath("/external/new-file.txt")
        val resolvedConflict = TestFilePath("/repo/conflicted.txt")
        val duplicateConflict = TestFilePath("/repo/conflicted.txt")
        val stagingPath = TestFilePath("/repo/staged.txt")
        val environment = TestEnvironment(
            changeLists = listOf(
                TestChangeList(
                    "Default",
                    listOf(
                        tracked,
                        tracked,
                        ignoredTracked,
                        nonGitTracked,
                    ),
                ),
            ),
            unversionedFiles = listOf(
                unversioned,
                duplicateUnversioned,
                ignoredUnversioned,
                nonGitUnversioned,
            ),
            resolvedConflictPaths = listOf(resolvedConflict, duplicateConflict),
            stagingAreaPaths = listOf(stagingPath),
            ignoredPaths = setOf(
                "/repo/build/generated.txt",
                "/repo/build/new-generated.txt",
            ),
            vcsNamesByPath = mapOf(
                "/external/Legacy.kt" to "Subversion",
                "/external/new-file.txt" to "Subversion",
            ),
        )

        val selection = service(environment).collectSelection()

        assertEquals(listOf(tracked), selection.trackedChanges)
        assertEquals(listOf(unversioned), selection.unversionedFiles)
        assertEquals(listOf(resolvedConflict), selection.resolvedConflictPaths)
        assertEquals(listOf(stagingPath), selection.stagingAreaPaths)
    }

    @Test
    fun `collect tracked selection omits unversioned files while keeping conflicts and staging area paths`() {
        val tracked = modification("/repo/src/App.kt")
        val unversioned = TestFilePath("/repo/new-file.txt")
        val resolvedConflict = TestFilePath("/repo/conflicted.txt")
        val stagingPath = TestFilePath("/repo/staged.txt")
        val environment = TestEnvironment(
            changeLists = listOf(TestChangeList("Default", listOf(tracked))),
            unversionedFiles = listOf(unversioned),
            resolvedConflictPaths = listOf(resolvedConflict),
            stagingAreaPaths = listOf(stagingPath),
        )

        val selection = service(environment).collectTrackedSelection()

        assertEquals(listOf(tracked), selection.trackedChanges)
        assertEquals(emptyList(), selection.unversionedFiles)
        assertEquals(listOf(resolvedConflict), selection.resolvedConflictPaths)
        assertEquals(listOf(stagingPath), selection.stagingAreaPaths)
    }

    private class TestEnvironment(
        private val activeVcsNames: List<String> = listOf(GIT_VCS_NAME),
        private val changeLists: List<LocalChangeList> = emptyList(),
        private val unversionedFiles: List<FilePath> = emptyList(),
        private val resolvedConflictPaths: List<FilePath> = emptyList(),
        private val stagingAreaPaths: List<FilePath> = emptyList(),
        private val ignoredPaths: Set<String> = emptySet(),
        private val vcsNamesByPath: Map<String, String> = emptyMap(),
    ) : GitChangeSelectionEnvironment {
        override fun activeVcsNames(): List<String> = activeVcsNames

        override fun changeLists(): List<LocalChangeList> = changeLists

        override fun unversionedFiles(): List<FilePath> = unversionedFiles

        override fun resolvedConflictPaths(): List<FilePath> = resolvedConflictPaths

        override fun stagingAreaPaths(): List<FilePath> = stagingAreaPaths

        override fun vcsNameForPath(path: FilePath): String? = vcsNamesByPath[path.path] ?: GIT_VCS_NAME

        override fun isIgnored(path: FilePath): Boolean = path.path in ignoredPaths
    }

    private class TestChangeList(
        private val listName: String,
        private val listChanges: Collection<Change>,
    ) : LocalChangeList() {
        override fun getChanges(): Collection<Change> = listChanges

        override fun getName(): String = listName

        override fun getComment(): String? = null

        override fun isDefault(): Boolean = false

        override fun isReadOnly(): Boolean = false

        override fun getData(): Any? = null

        override fun copy(): LocalChangeList = TestChangeList(listName, listChanges)
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

    private companion object {
        private fun service(environment: TestEnvironment): GitChangeSelectionService {
            val project = testProject()
            return GitChangeSelectionService(project, environment)
        }

        private fun modification(path: String): Change {
            val filePath = TestFilePath(path)
            return Change(TestContentRevision(filePath), TestContentRevision(filePath), FileStatus.MODIFIED)
        }

        private fun testProject(): Project = Proxy.newProxyInstance(
            Project::class.java.classLoader,
            arrayOf(Project::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "toString" -> "Test Project"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> method.defaultReturnValue()
            }
        } as Project

        private fun java.lang.reflect.Method.defaultReturnValue(): Any? = when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Void.TYPE -> null
            else -> null
        }
    }
}
