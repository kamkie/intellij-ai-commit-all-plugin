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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import git4idea.index.GitFileStatus
import git4idea.index.GitStageTracker
import java.io.File
import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitStagingAreaSelectionCollectorTest {
    @Test
    fun `collects distinct committable git paths from staging tracker state`() {
        val root = LightVirtualFile("repo")
        val modified = TestFilePath("/repo/src/Main.kt")
        val duplicateModified = TestFilePath("\\repo\\src\\Main.kt")
        val untracked = TestFilePath("/repo/src/New.kt")
        val ignoredByFilter = TestFilePath("/repo/build/output.txt")
        val state = GitStageTracker.State(
            mapOf(
                root to GitStageTracker.RootState(
                    root,
                    true,
                    linkedMapOf(
                        modified to GitFileStatus('M', ' ', modified, null),
                        duplicateModified to GitFileStatus(' ', 'M', duplicateModified, null),
                        untracked to GitFileStatus('?', '?', untracked, null),
                        ignoredByFilter to GitFileStatus('A', ' ', ignoredByFilter, null),
                    ),
                ),
            ),
        )

        val result = GitStagingAreaSelectionCollector.collect(
            stateProvider = { state },
            isGitPath = { path -> !path.path.contains("/build/") },
        )

        assertEquals(listOf(modified, untracked), result)
    }

    @Test
    fun `reports diagnostic and fails closed when staging area state cannot be collected`() {
        val diagnostics = CapturingGitChangeSelectionCompatibilityDiagnostics()

        val result = GitStagingAreaSelectionCollector.collect(
            stateProvider = { error("tracker state unavailable") },
            isGitPath = { true },
            diagnostics = diagnostics,
        )

        assertEquals(emptyList(), result)
        assertEquals(
            listOf(
                GitChangeSelectionCompatibilityDiagnostic(
                    sourceClassName = GitChangeSelectionService::class.java.name,
                    methodName = "collectStagingAreaPaths",
                    reason = "staging area paths could not be collected",
                    exceptionClassName = IllegalStateException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `reports diagnostic cause when staging area collection wraps a platform failure`() {
        val diagnostics = CapturingGitChangeSelectionCompatibilityDiagnostics()
        val cause = IllegalStateException("tracker disposed")

        val result = GitStagingAreaSelectionCollector.collect(
            stateProvider = { throw IllegalArgumentException("tracker state unavailable", cause) },
            isGitPath = { true },
            diagnostics = diagnostics,
        )

        assertEquals(emptyList(), result)
        assertEquals(
            IllegalStateException::class.java.name,
            diagnostics.events.single().causeClassName,
        )
    }

    @Test
    fun `default diagnostics fails closed and logs without rethrowing when collection wraps a cause`() {
        val cause = IllegalStateException("tracker disposed")

        val result = GitStagingAreaSelectionCollector.collect(
            stateProvider = { throw IllegalArgumentException("tracker state unavailable", cause) },
            isGitPath = { true },
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun `default diagnostics fails closed and logs without rethrowing when collection has no cause`() {
        val result = GitStagingAreaSelectionCollector.collect(
            stateProvider = { error("tracker state unavailable") },
            isGitPath = { true },
        )

        assertEquals(emptyList(), result)
    }

    private class CapturingGitChangeSelectionCompatibilityDiagnostics : GitChangeSelectionCompatibilityDiagnostics {
        val events = mutableListOf<GitChangeSelectionCompatibilityDiagnostic>()

        override fun report(diagnostic: GitChangeSelectionCompatibilityDiagnostic) {
            events += diagnostic
        }
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
