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
package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.ui.EditorTextComponent
import com.intellij.vcs.commit.CommitMessageUi
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommitMessageUiReaderTest {
    @Test
    fun `reads commit message through text access boundary`() {
        val textAccess = CapturingCommitMessageUiTextAccess()
        val reader = CommitMessageUiReader(
            commitMessageUi = TestPublicCommitMessageUi("generated message"),
            textAccess = textAccess,
        )

        val message = reader.readMessage()

        assertEquals("generated message", message)
        assertEquals(1, textAccess.readCallCount)
        assertEquals("generated message", textAccess.observedMessage)
    }

    @Test
    fun `reads commit message through default text access boundary outside an application`() {
        val reader = CommitMessageUiReader(TestPublicCommitMessageUi("generated message"))

        val message = reader.readMessage()

        assertEquals("generated message", message)
    }

    @Test
    fun `clears public commit message text through default mutation access outside an application`() {
        val commitMessageUi = TestPublicCommitMessageUi("stale message")

        CommitMessageTextCleaner.clear(commitMessageUi = commitMessageUi)

        assertEquals("", commitMessageUi.text)
    }

    @Test
    fun `clears public commit message text through mutation access when no optional controls exist`() {
        val commitMessageUi = TestPublicCommitMessageUi("stale message")
        val mutationAccess = CapturingCommitMessageTextMutationAccess()

        CommitMessageTextCleaner.clear(
            commitMessageUi = commitMessageUi,
            textMutationAccess = mutationAccess,
        )

        assertEquals("", commitMessageUi.text)
        assertEquals(1, mutationAccess.mutateCallCount)
    }

    @Test
    fun `clears commit message controls and non-empty documents while skipping empty documents`() {
        val editorDocument = CountingDocument("editor stale message")
        val parentDocument = CountingDocument("")
        val commitMessageUi = TestCommitMessageUi(
            initialText = "ui stale message",
            editorField = TestEditorTextComponent(editorDocument),
        )
        val parentCommitMessageControl = TestCommitMessageControl("parent stale message")
        val mutationAccess = CapturingCommitMessageTextMutationAccess()

        CommitMessageTextCleaner.clear(
            commitMessageUi = commitMessageUi,
            parentDataContext = testDataContext(
                VcsDataKeys.COMMIT_MESSAGE_CONTROL to parentCommitMessageControl,
                VcsDataKeys.COMMIT_MESSAGE_DOCUMENT to parentDocument,
            ),
            textMutationAccess = mutationAccess,
        )

        assertEquals("", commitMessageUi.text)
        assertEquals("", commitMessageUi.commitMessage)
        assertEquals("", parentCommitMessageControl.commitMessage)
        assertEquals("", editorDocument.currentText())
        assertEquals(1, editorDocument.setTextCallCount)
        assertEquals("", parentDocument.currentText())
        assertEquals(0, parentDocument.setTextCallCount)
        assertEquals(1, mutationAccess.mutateCallCount)
    }

    private class CapturingCommitMessageUiTextAccess : CommitMessageUiTextAccess {
        var readCallCount = 0
        var observedMessage: String? = null

        override fun readText(readNow: () -> String): String {
            readCallCount++
            return readNow().also { message -> observedMessage = message }
        }
    }

    private class CapturingCommitMessageTextMutationAccess : CommitMessageTextMutationAccess {
        var mutateCallCount = 0

        override fun mutateText(mutateNow: () -> Unit) {
            mutateCallCount++
            mutateNow()
        }
    }

    private class TestPublicCommitMessageUi(initialText: String) : CommitMessageUi {
        private var currentText = initialText

        override fun getText(): String = currentText

        override fun setText(text: String?) {
            currentText = text.orEmpty()
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit
    }

    private class TestCommitMessageUi(
        initialText: String,
        private val editorField: EditorTextComponent? = null,
    ) : CommitMessageUi,
        CommitMessageI {
        private var currentText = initialText
        var commitMessage = initialText
            private set

        override fun getText(): String = currentText

        override fun setText(text: String?) {
            currentText = text.orEmpty()
        }

        override fun setCommitMessage(commitMessage: String) {
            this.commitMessage = commitMessage
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit

        fun getEditorField(): EditorTextComponent? = editorField
    }

    private class TestCommitMessageControl(initialMessage: String) : CommitMessageI {
        var commitMessage = initialMessage
            private set

        override fun setCommitMessage(commitMessage: String) {
            this.commitMessage = commitMessage
        }
    }

    private class TestEditorTextComponent(
        private val document: Document,
        private val component: JComponent = JPanel(),
    ) : EditorTextComponent {
        override fun getText(): String = document.immutableCharSequence.toString()

        override fun getComponent(): JComponent = component

        override fun getDocument(): Document = document

        override fun addDocumentListener(listener: DocumentListener) = Unit

        override fun removeDocumentListener(listener: DocumentListener) = Unit
    }

    private class CountingDocument(initialText: String) :
        UserDataHolderBase(),
        Document {
        private var currentText = initialText
        var setTextCallCount = 0
            private set

        override fun getImmutableCharSequence(): CharSequence = currentText

        fun currentText(): String = currentText

        override fun getLineCount(): Int = 1

        override fun getLineNumber(offset: Int): Int = 0

        override fun getLineStartOffset(line: Int): Int = 0

        override fun getLineEndOffset(line: Int): Int = currentText.length

        override fun insertString(offset: Int, s: CharSequence) {
            replaceString(offset, offset, s)
        }

        override fun deleteString(startOffset: Int, endOffset: Int) {
            replaceString(startOffset, endOffset, "")
        }

        override fun setText(text: CharSequence) {
            setTextCallCount++
            replaceString(0, currentText.length, text)
        }

        override fun replaceString(
            startOffset: Int,
            endOffset: Int,
            s: CharSequence,
        ) {
            currentText = currentText.replaceRange(startOffset, endOffset, s)
        }

        override fun isWritable(): Boolean = true

        override fun getModificationStamp(): Long = 0L

        override fun createRangeMarker(
            startOffset: Int,
            endOffset: Int,
            surviveOnExternalChange: Boolean,
        ): RangeMarker = error("Not needed for commit message reader tests.")

        override fun createGuardedBlock(
            startOffset: Int,
            endOffset: Int,
        ): RangeMarker = error("Not needed for commit message reader tests.")

        override fun addDocumentListener(
            listener: DocumentListener,
            parentDisposable: Disposable,
        ) = Unit

        override fun removeDocumentListener(listener: DocumentListener) = Unit
    }

    private fun testDataContext(vararg values: Pair<DataKey<*>, Any>): DataContext {
        val data = values.associate { (key, value) -> key.name to value }
        return DataContext { dataId -> data[dataId] }
    }
}
