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
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.ui.EditorTextComponent
import com.intellij.vcs.commit.CommitMessageUi
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CommitMessageUserEditSignalTest {
    @Test
    fun `marks document change as user edit when current event comes from commit message editor`() {
        val editor = JTextArea()
        val document = TestDocument("old")
        val signal = DocumentCommitMessageUserEditSignal(
            document = document,
            detector = AwtCommitMessageUserEditDetector(editor) {
                KeyEvent(editor, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a')
            },
        )

        document.replaceString(0, document.textLength, "new")

        assertTrue(signal.isUserEdited())
        signal.close()
    }

    @Test
    fun `ignores document changes without editor input event`() {
        val editor = JTextArea()
        val document = TestDocument("old")
        val signal = DocumentCommitMessageUserEditSignal(
            document = document,
            detector = AwtCommitMessageUserEditDetector(editor) { null },
        )

        document.replaceString(0, document.textLength, "generated")

        assertFalse(signal.isUserEdited())
        signal.close()
    }

    @Test
    fun `ignores input events outside commit message editor`() {
        val editor = JTextArea()
        val outside = JTextArea()
        val document = TestDocument("old")
        val signal = DocumentCommitMessageUserEditSignal(
            document = document,
            detector = AwtCommitMessageUserEditDetector(editor) {
                KeyEvent(outside, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, 'a')
            },
        )

        document.replaceString(0, document.textLength, "new")

        assertFalse(signal.isUserEdited())
        signal.close()
    }

    @Test
    fun `accepts text actions from nested editor components`() {
        val editor = JPanel()
        val child = JTextArea()
        editor.add(child)
        val detector = AwtCommitMessageUserEditDetector(editor) {
            ActionEvent(child, ActionEvent.ACTION_PERFORMED, "paste")
        }

        assertTrue(detector.isUserEditEventInProgress())
    }

    @Test
    fun `factory creates active document signal when commit message ui exposes editor component`() {
        val document = TestDocument("old")
        val editorComponent = JPanel()
        val signal = CommitMessageUserEditSignalFactory.create(
            TestCommitMessageUi(TestEditorTextComponent(document, editorComponent)),
        )

        assertTrue(signal is DocumentCommitMessageUserEditSignal)
        signal.close()
    }

    @Test
    fun `factory returns not edited signal when editor field is missing`() {
        val signal = CommitMessageUserEditSignalFactory.create(TestCommitMessageUi(null))

        assertSame(ActiveCommitMessageUserEditSignal.NotEdited, signal)
        assertFalse(signal.isUserEdited())
        signal.close()
    }

    @Test
    fun `factory returns not edited signal when editor component is unavailable`() {
        val signal = CommitMessageUserEditSignalFactory.create(
            TestCommitMessageUi(DocumentOnlyEditorField(TestDocument("old"))),
        )

        assertSame(ActiveCommitMessageUserEditSignal.NotEdited, signal)
        assertFalse(signal.isUserEdited())
        signal.close()
    }

    private class TestCommitMessageUi(private val editorField: Any?) : CommitMessageUi {
        override fun getText(): String = ""

        override fun setText(text: String?) = Unit

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit

        fun getEditorField(): Any? = editorField
    }

    private class TestEditorTextComponent(
        private val document: Document,
        private val component: JComponent,
    ) : EditorTextComponent {
        private val listenerDisposables = mutableMapOf<DocumentListener, Disposable>()

        override fun getText(): String = document.text

        override fun getComponent(): JComponent = component

        override fun getDocument(): Document = document

        override fun addDocumentListener(listener: DocumentListener) {
            val disposable = Disposer.newDisposable("Test editor text component")
            listenerDisposables[listener] = disposable
            document.addDocumentListener(listener, disposable)
        }

        override fun removeDocumentListener(listener: DocumentListener) {
            listenerDisposables.remove(listener)?.let { disposable -> Disposer.dispose(disposable) }
            document.removeDocumentListener(listener)
        }
    }

    private class DocumentOnlyEditorField(private val document: Document) {
        fun getDocument(): Document = document
    }

    private class TestDocument(initialText: String) :
        UserDataHolderBase(),
        Document {
        private var text = initialText
        private val listeners = mutableListOf<DocumentListener>()
        private var stamp = 0L

        override fun getImmutableCharSequence(): CharSequence = text

        override fun getLineCount(): Int = 1

        override fun getLineNumber(offset: Int): Int = 0

        override fun getLineStartOffset(line: Int): Int = 0

        override fun getLineEndOffset(line: Int): Int = text.length

        override fun insertString(offset: Int, s: CharSequence) {
            replaceString(offset, offset, s)
        }

        override fun deleteString(startOffset: Int, endOffset: Int) {
            replaceString(startOffset, endOffset, "")
        }

        override fun setText(text: CharSequence) {
            replaceString(0, this.text.length, text)
        }

        override fun replaceString(
            startOffset: Int,
            endOffset: Int,
            s: CharSequence,
        ) {
            val old = text.substring(startOffset, endOffset)
            text = text.replaceRange(startOffset, endOffset, s)
            stamp++
            val event = TestDocumentEvent(
                document = this,
                offset = startOffset,
                oldFragment = old,
                newFragment = s,
                oldStamp = stamp - 1,
            )
            listeners.forEach { listener -> listener.documentChanged(event) }
        }

        override fun isWritable(): Boolean = true

        override fun getModificationStamp(): Long = stamp

        override fun createRangeMarker(
            startOffset: Int,
            endOffset: Int,
            surviveOnExternalChange: Boolean,
        ): RangeMarker = error("Not needed for user edit signal tests.")

        override fun createGuardedBlock(
            startOffset: Int,
            endOffset: Int,
        ): RangeMarker = error("Not needed for user edit signal tests.")

        override fun addDocumentListener(
            listener: DocumentListener,
            parentDisposable: Disposable,
        ) {
            listeners += listener
            Disposer.register(parentDisposable, Disposable { listeners -= listener })
        }

        override fun removeDocumentListener(listener: DocumentListener) {
            listeners -= listener
        }
    }

    private class TestDocumentEvent(
        document: Document,
        private val offset: Int,
        private val oldFragment: CharSequence,
        private val newFragment: CharSequence,
        private val oldStamp: Long,
    ) : DocumentEvent(document) {
        override fun getOffset(): Int = offset

        override fun getOldLength(): Int = oldFragment.length

        override fun getNewLength(): Int = newFragment.length

        override fun getOldFragment(): CharSequence = oldFragment

        override fun getNewFragment(): CharSequence = newFragment

        override fun getOldTimeStamp(): Long = oldStamp
    }
}
