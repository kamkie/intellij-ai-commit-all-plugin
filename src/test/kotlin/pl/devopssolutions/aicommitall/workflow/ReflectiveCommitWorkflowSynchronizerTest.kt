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

import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.vcs.commit.AmendCommitHandler
import com.intellij.vcs.commit.CommitWorkflowHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ReflectiveCommitWorkflowSynchronizerTest {
    @Test
    fun `synchronizes compatible commit workflow handlers`() {
        val handler = CompatibleHandler()
        val changeList = TestChangeList("Default")
        val items = listOf(Any(), Any())

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = items,
        )

        assertTrue(result)
        assertEquals(listOf(changeList), handler.synchronizedChangeLists)
        assertEquals(0, handler.synchronizedUnversionedCount)
        assertEquals(changeList, handler.activeChangeList)
        assertEquals(items, handler.inclusionItems)
        assertTrue(handler.replaceInclusion)
    }

    @Test
    fun `fails closed when workflow handler has no inclusion methods`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = IncompatibleHandler(),
            changeLists = listOf(changeList),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
        )

        assertFalse(result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = IncompatibleHandler::class.java.name,
                    methodName = "commitWorkflowMethods",
                    reason = "required methods missing",
                    missingMethodNames = listOf("synchronizeInclusion", "setCommitState"),
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `fails closed before reflection when inclusion items are empty`() {
        val handler = CompatibleHandler()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = emptyList(),
        )

        assertFalse(result)
        assertEquals(null, handler.synchronizedChangeLists)
        assertEquals(null, handler.activeChangeList)
    }

    @Test
    fun `fails closed when workflow handler has only synchronize inclusion method`() {
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = MissingSetCommitStateHandler(),
            changeLists = listOf(changeList),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
        )

        assertFalse(result)
    }

    @Test
    fun `fails closed when workflow synchronization throws`() {
        val diagnostics = CapturingCommitWorkflowCompatibilityDiagnostics()
        val changeList = TestChangeList("Default")

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = ThrowingHandler(),
            changeLists = listOf(changeList),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = listOf(Any()),
            diagnostics = diagnostics,
        )

        assertFalse(result)
        assertEquals(
            listOf(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = ThrowingHandler::class.java.name,
                    methodName = "synchronize",
                    reason = "method invocation failed",
                    exceptionClassName = java.lang.reflect.InvocationTargetException::class.java.name,
                    causeClassName = IllegalStateException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `succeeds when compatible workflow synchronization fails once then settles`() {
        val handler = TransientThrowingHandler()
        val changeList = TestChangeList("Default")
        val items = listOf(Any())

        val result = ReflectiveCommitWorkflowSynchronizer.synchronize(
            workflowHandler = handler,
            changeLists = listOf(changeList),
            unversionedFiles = emptyList(),
            activeChangeList = changeList,
            inclusionItems = items,
        )

        assertTrue(result)
        assertEquals(2, handler.synchronizeCallCount)
        assertEquals(1, handler.setCommitStateCallCount)
        assertEquals(changeList, handler.activeChangeList)
        assertEquals(items, handler.inclusionItems)
    }

    private open class TestCommitWorkflowHandler : CommitWorkflowHandler {
        override val amendCommitHandler: AmendCommitHandler
            get() = error("Not needed for reflection tests.")

        override fun getExecutor(executorId: String): CommitExecutor? = null

        override fun isExecutorEnabled(executor: CommitExecutor): Boolean = false

        override fun execute(executor: CommitExecutor) = Unit
    }

    private class CompatibleHandler : TestCommitWorkflowHandler() {
        var synchronizedChangeLists: List<LocalChangeList>? = null
        var synchronizedUnversionedCount: Int? = null
        var activeChangeList: LocalChangeList? = null
        var inclusionItems: Collection<Any>? = null
        var replaceInclusion: Boolean = false

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizedChangeLists = changeLists
            synchronizedUnversionedCount = unversionedFiles.size
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            activeChangeList = changeList
            inclusionItems = items
            this.replaceInclusion = replaceInclusion
        }
    }

    private class IncompatibleHandler : TestCommitWorkflowHandler()

    private class MissingSetCommitStateHandler : TestCommitWorkflowHandler() {
        var synchronizedInputCount = 0

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizedInputCount = changeLists.size + unversionedFiles.size
        }
    }

    private class ThrowingHandler : TestCommitWorkflowHandler() {
        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            error("synchronization failed for ${changeLists.size} lists and ${unversionedFiles.size} files")
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            error("setCommitState should not run for ${changeList.name}, ${items.size}, $replaceInclusion")
        }
    }

    private class TransientThrowingHandler : TestCommitWorkflowHandler() {
        var synchronizeCallCount = 0
        var setCommitStateCallCount = 0
        var activeChangeList: LocalChangeList? = null
        var inclusionItems: Collection<Any>? = null

        fun synchronizeInclusion(changeLists: List<LocalChangeList>, unversionedFiles: List<*>) {
            synchronizeCallCount++
            if (synchronizeCallCount == 1) {
                error("transient synchronization failure")
            }
        }

        fun setCommitState(changeList: LocalChangeList, items: Collection<Any>, replaceInclusion: Boolean) {
            setCommitStateCallCount++
            activeChangeList = changeList
            inclusionItems = items
        }
    }

    private class CapturingCommitWorkflowCompatibilityDiagnostics : CommitWorkflowCompatibilityDiagnostics {
        val events = mutableListOf<CommitWorkflowCompatibilityDiagnostic>()

        override fun report(diagnostic: CommitWorkflowCompatibilityDiagnostic) {
            events += diagnostic
        }
    }

    private class TestChangeList(private val listName: String) : LocalChangeList() {
        override fun getChanges(): Collection<Change> = emptyList()

        override fun getName(): String = listName

        override fun getComment(): String? = null

        override fun isDefault(): Boolean = false

        override fun isReadOnly(): Boolean = false

        override fun getData(): Any? = null

        override fun copy(): LocalChangeList = TestChangeList(listName)
    }
}
