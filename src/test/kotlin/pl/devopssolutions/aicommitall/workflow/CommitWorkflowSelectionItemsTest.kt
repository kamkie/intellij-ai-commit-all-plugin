package pl.devopssolutions.aicommitall.workflow

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
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import kotlin.test.Test
import kotlin.test.assertEquals
import java.io.File
import java.nio.charset.Charset

internal class CommitWorkflowSelectionItemsTest {
    @Test
    fun `keeps all changelists that contain selected tracked changes`() {
        val firstChange = modification("/repo/first.txt")
        val secondChange = modification("/repo/second.txt")
        val firstList = TestChangeList("first", listOf(firstChange))
        val secondList = TestChangeList("second", listOf(secondChange))
        val unrelatedList = TestChangeList("third", emptyList())

        val result = CommitWorkflowSelectionItems.changeListsContaining(
            trackedChanges = listOf(firstChange, secondChange),
            changeLists = listOf(firstList, unrelatedList, secondList),
        )

        assertEquals(listOf(firstList, secondList), result)
    }

    @Test
    fun `combines tracked unversioned and resolved-conflict items for inclusion`() {
        val trackedChange = modification("/repo/tracked.txt")
        val unversioned = TestFilePath("/repo/new.txt")
        val resolvedConflict = TestFilePath("/repo/conflict.txt")

        val result = CommitWorkflowSelectionItems.inclusionItems(
            GitChangeSelection(
                trackedChanges = listOf(trackedChange),
                unversionedFiles = listOf(unversioned),
                resolvedConflictPaths = listOf(resolvedConflict),
            ),
        )

        assertEquals(listOf(trackedChange, unversioned, resolvedConflict), result)
    }

    @Test
    fun `keeps staging-area paths in fallback inclusion items`() {
        val stagedPath = TestFilePath("/repo/staged-only.txt")

        val result = CommitWorkflowSelectionItems.inclusionItems(
            GitChangeSelection(
                trackedChanges = emptyList(),
                stagingAreaPaths = listOf(stagedPath),
            ),
        )

        assertEquals(listOf(stagedPath), result)
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
