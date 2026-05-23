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
import com.intellij.vcs.commit.CommitMessageUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AiCommitMessagePreparationTest {
    @Test
    fun `clears stale commit message before capturing initial snapshot`() {
        val commitMessageUi = TestCommitMessageUi("stale message")

        val snapshot = AiCommitMessagePreparation.prepareInitialSnapshot(
            commitMessageUi = commitMessageUi,
            clearBeforeGeneration = true,
        )

        assertEquals("", commitMessageUi.text)
        assertEquals("", snapshot.originalMessage)
        assertFalse(snapshot.acceptUnchangedPrefilledMessage)
    }

    @Test
    fun `clears parent commit message control and document before capturing initial snapshot`() {
        val commitMessageUi = TestCommitMessageUi("stale message")
        val parentCommitMessageControl = TestCommitMessageControl("parent stale message")
        val parentCommitMessageDocument = TestDocument("document stale message")
        val parentDataContext = testDataContext(
            VcsDataKeys.COMMIT_MESSAGE_CONTROL to parentCommitMessageControl,
            VcsDataKeys.COMMIT_MESSAGE_DOCUMENT to parentCommitMessageDocument,
        )

        val snapshot = AiCommitMessagePreparation.prepareInitialSnapshot(
            commitMessageUi = commitMessageUi,
            clearBeforeGeneration = true,
            parentDataContext = parentDataContext,
        )

        assertEquals("", commitMessageUi.text)
        assertEquals("", parentCommitMessageControl.message)
        assertEquals("", parentCommitMessageDocument.text)
        assertEquals("", snapshot.originalMessage)
        assertFalse(snapshot.acceptUnchangedPrefilledMessage)
    }

    @Test
    fun `preserves stale commit message when clearing is disabled`() {
        val commitMessageUi = TestCommitMessageUi("stale message")
        val parentCommitMessageControl = TestCommitMessageControl("parent stale message")
        val parentCommitMessageDocument = TestDocument("document stale message")

        val snapshot = AiCommitMessagePreparation.prepareInitialSnapshot(
            commitMessageUi = commitMessageUi,
            clearBeforeGeneration = false,
            parentDataContext = testDataContext(
                VcsDataKeys.COMMIT_MESSAGE_CONTROL to parentCommitMessageControl,
                VcsDataKeys.COMMIT_MESSAGE_DOCUMENT to parentCommitMessageDocument,
            ),
        )

        assertEquals("stale message", commitMessageUi.text)
        assertEquals("parent stale message", parentCommitMessageControl.message)
        assertEquals("document stale message", parentCommitMessageDocument.text)
        assertEquals("stale message", snapshot.originalMessage)
        assertTrue(snapshot.acceptUnchangedPrefilledMessage)
    }

    private class TestCommitMessageControl(initialMessage: String) : CommitMessageI {
        var message = initialMessage
            private set

        override fun setCommitMessage(commitMessage: String) {
            message = commitMessage
        }
    }

    private class TestCommitMessageUi(initialText: String) : CommitMessageUi {
        private var currentText = initialText

        override fun getText(): String = currentText

        override fun setText(text: String?) {
            currentText = text.orEmpty()
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit
    }

    private class TestDocument(initialText: String) :
        UserDataHolderBase(),
        Document {
        private var text = initialText

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
            text = text.replaceRange(startOffset, endOffset, s)
        }

        override fun isWritable(): Boolean = true

        override fun getModificationStamp(): Long = 0L

        override fun createRangeMarker(
            startOffset: Int,
            endOffset: Int,
            surviveOnExternalChange: Boolean,
        ): RangeMarker = error("Not needed for commit message preparation tests.")

        override fun createGuardedBlock(
            startOffset: Int,
            endOffset: Int,
        ): RangeMarker = error("Not needed for commit message preparation tests.")

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
