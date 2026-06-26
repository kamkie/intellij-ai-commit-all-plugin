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
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionSource
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus
import java.io.File
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CommitWorkflowSelectionServiceTest {
    @Test
    fun `missing workflow is returned when handler or ui is absent`() {
        val service = CommitWorkflowSelectionService(testProject())
        val workflowHandler = testProxy<CommitWorkflowHandler>()
        val workflowUi = testProxy<CommitWorkflowUi>()

        val results = listOf(
            service.prepareAllFilesSelection(null, workflowUi),
            service.prepareAllFilesSelection(workflowHandler, null),
            service.prepareAllFilesSelection(null, null),
        )

        assertEquals(
            List(results.size) { CommitWorkflowSelectionResult.MissingWorkflow },
            results,
        )
    }

    @Test
    fun `unsupported vcs status stops before collecting selection`() {
        val selectionSource = CapturingSelectionSource(
            supportStatus = GitVcsSupportStatus.UnsupportedNoGitVcs,
        )
        val service = service(selectionSource = selectionSource)

        val result = service.prepareAllFilesSelection(testProxy(), testProxy())

        assertEquals(
            CommitWorkflowSelectionResult.UnsupportedVcs(GitVcsSupportStatus.UnsupportedNoGitVcs),
            result,
        )
        assertEquals(0, selectionSource.collectSelectionCallCount)
    }

    @Test
    fun `empty git selection stops before activating workflow ui`() {
        val selectionSource = CapturingSelectionSource(
            selection = GitChangeSelection(emptyList()),
        )
        val synchronizer = CapturingSelectionSynchronizer()
        val service = service(
            selectionSource = selectionSource,
            selectionSynchronizer = synchronizer,
        )

        val result = service.prepareAllFilesSelection(testProxy(), testProxy())

        assertEquals(CommitWorkflowSelectionResult.EmptySelection, result)
        assertEquals(1, selectionSource.collectSelectionCallCount)
        assertEquals(0, synchronizer.callCount)
    }

    @Test
    fun `tracked changes without owning changelist stop as unsupported workflow`() {
        val trackedChange = modification("/repo/src/App.kt")
        val service = service(
            selectionSource = CapturingSelectionSource(
                selection = GitChangeSelection(trackedChanges = listOf(trackedChange)),
            ),
            changeLists = emptyList(),
        )

        val result = service.prepareAllFilesSelection(testProxy(), testProxy())

        assertEquals(
            CommitWorkflowSelectionResult.UnsupportedWorkflow(
                "No Git changelist owns the selected tracked changes.",
            ),
            result,
        )
    }

    @Test
    fun `activation failure stops before synchronizing selection`() {
        val unversionedFile = TestFilePath("/repo/new-file.txt")
        val synchronizer = CapturingSelectionSynchronizer()
        val service = service(
            selectionSource = CapturingSelectionSource(
                selection = GitChangeSelection(
                    trackedChanges = emptyList(),
                    unversionedFiles = listOf(unversionedFile),
                ),
            ),
            changeLists = emptyList(),
            defaultChangeList = TestChangeList("Default", emptyList()),
            selectionSynchronizer = synchronizer,
            activateWorkflowUi = { false },
        )

        val result = service.prepareAllFilesSelection(testProxy(), testProxy())

        assertEquals(
            CommitWorkflowSelectionResult.UnsupportedWorkflow(
                "The Commit tool window workflow could not be activated.",
            ),
            result,
        )
        assertEquals(0, synchronizer.callCount)
    }

    @Test
    fun `successful synchronization returns prepared selection with collected inclusion state`() {
        val trackedChange = modification("/repo/src/App.kt")
        val unversionedFile = TestFilePath("/repo/new-file.txt")
        val selection = GitChangeSelection(
            trackedChanges = listOf(trackedChange),
            unversionedFiles = listOf(unversionedFile),
        )
        val changeList = TestChangeList("Feature", listOf(trackedChange))
        val synchronizer = CapturingSelectionSynchronizer(
            result = CommitWorkflowSynchronizationResult.Synchronized,
        )
        val service = service(
            selectionSource = CapturingSelectionSource(selection = selection),
            changeLists = listOf(changeList),
            selectionSynchronizer = synchronizer,
        )
        val workflowHandler = testProxy<CommitWorkflowHandler>()

        val result = service.prepareAllFilesSelection(workflowHandler, testProxy())

        assertEquals(CommitWorkflowSelectionResult.Prepared(selection), result)
        assertSame(workflowHandler, synchronizer.workflowHandler)
        assertEquals(listOf(changeList), synchronizer.changeLists)
        assertSame(changeList, synchronizer.activeChangeList)
        assertEquals(listOf(trackedChange, unversionedFile), synchronizer.inclusionItems)
        assertEquals(listOf(unversionedFile), synchronizer.unversionedFiles)
    }

    @Test
    fun `synchronizer failures are mapped to selection results`() {
        val cases = listOf(
            CommitWorkflowSynchronizationResult.StagingConfirmationFailed to
                CommitWorkflowSelectionResult.StagingConfirmationFailed,
            CommitWorkflowSynchronizationResult.Incompatible to
                CommitWorkflowSelectionResult.UnsupportedWorkflow(
                    "The active commit workflow does not expose compatible inclusion-state methods.",
                ),
        )

        cases.forEach { (synchronizationResult, expectedResult) ->
            val service = service(
                selectionSource = CapturingSelectionSource(
                    selection = GitChangeSelection(
                        trackedChanges = emptyList(),
                        unversionedFiles = listOf(TestFilePath("/repo/new-file.txt")),
                    ),
                ),
                changeLists = emptyList(),
                defaultChangeList = TestChangeList("Default", emptyList()),
                selectionSynchronizer = CapturingSelectionSynchronizer(synchronizationResult),
            )

            val result = service.prepareAllFilesSelection(testProxy(), testProxy())

            assertEquals(expectedResult, result)
        }
    }

    @Test
    fun `commit workflow ui thread access runs the action directly without an application`() {
        val result = CommitWorkflowUiThreadAccess.run { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `activation retry succeeds when commit workflow activation settles`() {
        var attempts = 0
        val sleeper = CapturingActivationSleeper()

        val activated = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(50),
            sleeper = sleeper,
        ).activate {
            attempts++
            attempts == 2
        }

        assertTrue(activated)
        assertEquals(2, attempts)
        assertEquals(listOf(Duration.ofMillis(50)), sleeper.delays)
    }

    @Test
    fun `activation retry returns false after bounded attempts`() {
        var attempts = 0
        val sleeper = CapturingActivationSleeper()

        val activated = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(50),
            sleeper = sleeper,
        ).activate {
            attempts++
            false
        }

        assertFalse(activated)
        assertEquals(3, attempts)
        assertEquals(listOf(Duration.ofMillis(50), Duration.ofMillis(50)), sleeper.delays)
    }

    @Test
    fun `activation retry does not sleep when retry interval is zero`() {
        var attempts = 0
        val sleeper = CapturingActivationSleeper()

        val activated = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ZERO,
            sleeper = sleeper,
        ).activate {
            attempts++
            false
        }

        assertFalse(activated)
        assertEquals(3, attempts)
        assertEquals(emptyList(), sleeper.delays)
    }

    @Test
    fun `activation retry rejects invalid settings`() {
        assertFailsWith<IllegalArgumentException> {
            CommitWorkflowActivationRetry(maxAttempts = 0, retryInterval = Duration.ZERO)
        }
        assertFailsWith<IllegalArgumentException> {
            CommitWorkflowActivationRetry(maxAttempts = 1, retryInterval = Duration.ofMillis(-1))
        }
    }

    private class CapturingSelectionSource(
        private val supportStatus: GitVcsSupportStatus = GitVcsSupportStatus.Supported,
        private val selection: GitChangeSelection = GitChangeSelection(emptyList()),
    ) : GitChangeSelectionSource {
        var collectSelectionCallCount = 0

        override fun supportStatus(): GitVcsSupportStatus = supportStatus

        override fun collectTrackedSelection(): GitChangeSelection {
            error("Tracked-only selection is not used by commit workflow selection preparation.")
        }

        override fun collectSelection(): GitChangeSelection {
            collectSelectionCallCount += 1
            return selection
        }
    }

    private class CapturingSelectionSynchronizer(
        private val result: CommitWorkflowSynchronizationResult =
            CommitWorkflowSynchronizationResult.Synchronized,
    ) : CommitWorkflowSelectionSynchronizer {
        var callCount = 0
        var workflowHandler: CommitWorkflowHandler? = null
        var changeLists: List<LocalChangeList>? = null
        var unversionedFiles: List<FilePath>? = null
        var activeChangeList: LocalChangeList? = null
        var inclusionItems: Collection<Any>? = null

        override fun synchronize(
            workflowHandler: CommitWorkflowHandler,
            changeLists: List<LocalChangeList>,
            unversionedFiles: List<FilePath>,
            activeChangeList: LocalChangeList,
            inclusionItems: Collection<Any>,
        ): CommitWorkflowSynchronizationResult {
            callCount += 1
            this.workflowHandler = workflowHandler
            this.changeLists = changeLists
            this.unversionedFiles = unversionedFiles
            this.activeChangeList = activeChangeList
            this.inclusionItems = inclusionItems
            return result
        }
    }

    private class CapturingActivationSleeper : CommitWorkflowActivationSleeper {
        val delays = mutableListOf<Duration>()

        override fun sleep(duration: Duration) {
            delays += duration
        }
    }

    private fun service(
        selectionSource: GitChangeSelectionSource = CapturingSelectionSource(),
        changeLists: List<LocalChangeList> = emptyList(),
        defaultChangeList: LocalChangeList = TestChangeList("Default", emptyList()),
        activationRetry: CommitWorkflowActivationRetry = CommitWorkflowActivationRetry(
            maxAttempts = 1,
            retryInterval = Duration.ZERO,
        ),
        selectionSynchronizer: CommitWorkflowSelectionSynchronizer = CapturingSelectionSynchronizer(),
        activateWorkflowUi: (CommitWorkflowUi) -> Boolean = { true },
    ): CommitWorkflowSelectionService = CommitWorkflowSelectionService(
        project = testProject(),
        dependencies = CommitWorkflowSelectionDependencies(
            selectionSource = { selectionSource },
            changeLists = { changeLists },
            defaultChangeList = { defaultChangeList },
            activationRetry = activationRetry,
            selectionSynchronizer = selectionSynchronizer,
            activateWorkflowUi = activateWorkflowUi,
        ),
    )

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
        private fun testProject(): Project = testProxy()

        private inline fun <reified T : Any> testProxy(): T = Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "toString" -> "Test ${T::class.java.simpleName}"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> method.defaultReturnValue()
            }
        } as T

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
