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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `keeps already staged and unstaged paths from git staging state`() {
        val alreadyStaged = TestFilePath("/repo/already-staged.txt")
        val unstaged = TestFilePath("/repo/unstaged.txt")
        val untracked = TestFilePath("/repo/untracked.txt")
        val state = stageState(
            gitStatus('M', ' ', alreadyStaged),
            gitStatus(' ', 'M', unstaged),
            gitStatus('?', '?', untracked),
        )

        val result = GitStageSelectionItems.committablePaths(
            state = state,
            isGitPath = { true },
        )

        assertEquals(listOf(alreadyStaged, unstaged, untracked), result)
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
    fun `paths to stage skip already staged deletion rename and modification`() {
        val root = LightVirtualFile("repo")
        val stagedDeletion = TestFilePath("/repo/delete-me.txt")
        val renameSource = TestFilePath("/repo/rename-source.txt")
        val renameTarget = TestFilePath("/repo/rename-target.txt")
        val stagedModification = TestFilePath("/repo/staged-modified.txt")
        val partiallyStaged = TestFilePath("/repo/partially-staged.txt")
        val unstagedDeletion = TestFilePath("/repo/unstaged-delete.txt")
        val untracked = TestFilePath("/repo/untracked.txt")
        val state = GitStageTracker.State(
            mapOf(
                root to GitStageTracker.RootState(
                    root,
                    true,
                    mapOf(
                        stagedDeletion to gitStatus('D', ' ', stagedDeletion),
                        renameTarget to gitStatus('R', ' ', renameTarget, renameSource),
                        stagedModification to gitStatus('M', ' ', stagedModification),
                        partiallyStaged to gitStatus('M', 'M', partiallyStaged),
                        unstagedDeletion to gitStatus(' ', 'D', unstagedDeletion),
                        untracked to gitStatus('?', '?', untracked),
                    ),
                ),
            ),
        )

        val result = GitStageSelectionItems.pathsToStageByRoot(state)

        assertEquals(
            mapOf<VirtualFile, List<FilePath>>(
                root to listOf(partiallyStaged, unstagedDeletion, untracked),
            ),
            result,
        )
    }

    @Test
    fun `groups nested Gradle module and IntelliJ product paths by git root`() {
        val firstRoot = LightVirtualFile("repo-a")
        val secondRoot = LightVirtualFile("repo-b")
        val coreModuleBuild = TestFilePath("/repo-a/modules/core/build.gradle.kts")
        val ideaProductSource = TestFilePath("/repo-a/products/idea/plugin/src/Main.kt")
        val webstormProductSource = TestFilePath("/repo-b/products/webstorm/plugin/src/Main.kt")
        val state = GitStageTracker.State(
            mapOf(
                firstRoot to GitStageTracker.RootState(
                    firstRoot,
                    true,
                    mapOf(
                        coreModuleBuild to gitStatus(' ', 'M', coreModuleBuild),
                        ideaProductSource to gitStatus('?', '?', ideaProductSource),
                    ),
                ),
                secondRoot to GitStageTracker.RootState(
                    secondRoot,
                    true,
                    mapOf(webstormProductSource to gitStatus('A', ' ', webstormProductSource)),
                ),
            ),
        )

        val result = GitStageSelectionItems.committablePathsByRoot(state)

        assertEquals(
            mapOf<VirtualFile, List<FilePath>>(
                firstRoot to listOf(coreModuleBuild, ideaProductSource),
                secondRoot to listOf(webstormProductSource),
            ),
            result,
        )
    }

    @Test
    fun `deduplicates committable staging paths by normalized path text`() {
        val slashPath = TestFilePath("/repo/products/idea/plugin/src/Main.kt")
        val backslashPath = TestFilePath("\\repo\\products\\idea\\plugin\\src\\Main.kt")
        val state = stageState(
            gitStatus(' ', 'M', slashPath),
            gitStatus(' ', 'M', backslashPath),
        )

        val result = GitStageSelectionItems.committablePaths(
            state = state,
            isGitPath = { true },
        )

        assertEquals(listOf(slashPath), result)
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

    @Test
    fun `confirms staged paths across nested module and product directories in multiple roots`() {
        val firstRoot = LightVirtualFile("repo-a")
        val secondRoot = LightVirtualFile("repo-b")
        val ideaProductSource = TestFilePath("/repo-a/products/idea/plugin/src/Main.kt")
        val coreModuleSource = TestFilePath("/repo-b/modules/core/src/Main.kt")
        val state = GitStageTracker.State(
            mapOf(
                firstRoot to GitStageTracker.RootState(
                    firstRoot,
                    true,
                    mapOf(ideaProductSource to gitStatus('M', ' ', ideaProductSource)),
                ),
                secondRoot to GitStageTracker.RootState(
                    secondRoot,
                    true,
                    mapOf(coreModuleSource to gitStatus('A', ' ', coreModuleSource)),
                ),
            ),
        )

        assertTrue(
            GitStageSelectionItems.containsAllStagedPaths(
                state,
                listOf(
                    TestFilePath("\\repo-a\\products\\idea\\plugin\\src\\Main.kt"),
                    TestFilePath("/repo-b/modules/core/src/Main.kt"),
                ),
            ),
        )
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
        origPath: FilePath? = null,
    ): GitFileStatus = GitFileStatus(index, workTree, path, origPath)

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
