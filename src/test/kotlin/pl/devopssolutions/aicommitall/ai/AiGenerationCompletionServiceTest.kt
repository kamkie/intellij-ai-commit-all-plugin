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
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.ui.EditorTextComponent
import com.intellij.vcs.commit.CommitMessageUi
import java.lang.reflect.Proxy
import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

internal class AiGenerationCompletionServiceTest {
    @Test
    fun `captures initial commit message snapshot from the UI`() {
        val service = AiGenerationCompletionService()

        val snapshot = service.captureInitialMessage(TestCommitMessageUi("existing message"))

        assertEquals(AiCommitMessageSnapshot("existing message"), snapshot)
    }

    @Test
    fun `awaitCompletionAsync wires commit message reader and closes user edit signal after completion`() {
        val document = CountingDocument("generated message")
        val commitMessageUi = TestCommitMessageUi(
            text = "generated message",
            editorField = TestEditorTextComponent(document),
        )
        val service = AiGenerationCompletionService()

        val result = service.awaitCompletionAsync(
            snapshot = AiCommitMessageSnapshot("old message"),
            invocation = testInvocation(progressIndicator = progressIndicator(false)),
            commitMessageUi = commitMessageUi,
            options = immediateOptions(),
        ).get(2, TimeUnit.SECONDS)

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
        assertEquals(0, document.listenerCount)
    }

    @Test
    fun `awaitCompletionAsync closes user edit signal after observed running completion`() {
        val document = CountingDocument("generated message")
        val commitMessageUi = TestCommitMessageUi(
            text = "generated message",
            editorField = TestEditorTextComponent(document),
        )
        val service = AiGenerationCompletionService()

        val result = service.awaitCompletionAsync(
            snapshot = AiCommitMessageSnapshot("old message"),
            invocation = testInvocation(progressIndicator = progressIndicator(true, false)),
            commitMessageUi = commitMessageUi,
            options = immediateOptions(stoppedSignalGracePeriod = Duration.ZERO),
        ).get(2, TimeUnit.SECONDS)

        val completed = assertIs<AiGenerationCompletionResult.Completed>(result)
        assertEquals("generated message", completed.generatedMessage)
        assertEquals(0, document.listenerCount)
    }

    @Test
    fun `awaitCompletionAsync closes user edit signal after unavailable running signal`() {
        val document = CountingDocument("generated message")
        val commitMessageUi = TestCommitMessageUi(
            text = "generated message",
            editorField = TestEditorTextComponent(document),
        )
        val service = AiGenerationCompletionService()

        val result = service.awaitCompletionAsync(
            snapshot = AiCommitMessageSnapshot("old message"),
            invocation = testInvocation(progressIndicator = "not a progress indicator"),
            commitMessageUi = commitMessageUi,
            options = immediateOptions(stoppedSignalGracePeriod = Duration.ZERO),
        ).get(2, TimeUnit.SECONDS)

        assertEquals(AiGenerationCompletionResult.NoCompletionSignal("generated message"), result)
        assertEquals(0, document.listenerCount)
    }

    @Test
    fun `awaitCompletionAsync closes user edit signal after timeout`() {
        val document = CountingDocument("old message")
        val commitMessageUi = TestCommitMessageUi(
            text = "old message",
            editorField = TestEditorTextComponent(document),
        )
        val service = AiGenerationCompletionService()

        val result = service.awaitCompletionAsync(
            snapshot = AiCommitMessageSnapshot("old message"),
            invocation = testInvocation(progressIndicator = progressIndicator(false)),
            commitMessageUi = commitMessageUi,
            options = immediateOptions(timeout = Duration.ofMillis(1)),
        ).get(2, TimeUnit.SECONDS)

        assertIs<AiGenerationCompletionResult.Timeout>(result)
        assertEquals(0, document.listenerCount)
    }

    @Test
    fun `awaitCompletionAsync closes user edit signal after observer exception`() {
        val document = CountingDocument("old message")
        val commitMessageUi = TestCommitMessageUi(
            text = "old message",
            editorField = TestEditorTextComponent(document),
            failOnRead = true,
        )
        val service = AiGenerationCompletionService()

        val future = service.awaitCompletionAsync(
            snapshot = AiCommitMessageSnapshot("old message"),
            invocation = testInvocation(progressIndicator = progressIndicator(false)),
            commitMessageUi = commitMessageUi,
            options = immediateOptions(),
        )

        assertFailsWith<ExecutionException> {
            future.get(2, TimeUnit.SECONDS)
        }
        assertEquals(0, document.listenerCount)
    }

    private class TestAction(
        @Suppress("unused")
        private val progressIndicator: Any?,
    ) : AnAction("Generate Commit Message") {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class TestCommitMessageUi(
        private var text: String,
        private val editorField: EditorTextComponent? = null,
        private val failOnRead: Boolean = false,
    ) : CommitMessageUi {
        override fun getText(): String {
            check(!failOnRead) { "Commit message read failed." }
            return text
        }

        override fun setText(text: String?) {
            this.text = text.orEmpty()
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit

        fun getEditorField(): EditorTextComponent? = editorField
    }

    private class TestEditorTextComponent(
        private val document: CountingDocument,
        private val component: JComponent = JPanel(),
    ) : EditorTextComponent {
        override fun getText(): String = document.immutableCharSequence.toString()

        override fun getComponent(): JComponent = component

        override fun getDocument(): Document = document

        override fun addDocumentListener(listener: DocumentListener) {
            val disposable = Disposer.newDisposable("Test commit message editor")
            document.addDocumentListener(listener, disposable)
        }

        override fun removeDocumentListener(listener: DocumentListener) {
            document.removeDocumentListener(listener)
        }
    }

    private class CountingDocument(initialText: String) :
        UserDataHolderBase(),
        Document {
        private var currentText = initialText
        private val listeners = mutableListOf<DocumentListener>()
        private var stamp = 0L

        val listenerCount: Int
            get() = listeners.size

        override fun getImmutableCharSequence(): CharSequence = currentText

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
            replaceString(0, currentText.length, text)
        }

        override fun replaceString(
            startOffset: Int,
            endOffset: Int,
            s: CharSequence,
        ) {
            val old = currentText.substring(startOffset, endOffset)
            currentText = currentText.replaceRange(startOffset, endOffset, s)
            stamp++
            val event = TestDocumentEvent(
                document = this,
                offset = startOffset,
                oldFragment = old,
                newFragment = s,
                oldStamp = stamp - 1,
            )
            listeners.toList().forEach { listener -> listener.documentChanged(event) }
        }

        override fun isWritable(): Boolean = true

        override fun getModificationStamp(): Long = stamp

        override fun createRangeMarker(
            startOffset: Int,
            endOffset: Int,
            surviveOnExternalChange: Boolean,
        ): RangeMarker = error("Not needed for AI completion service tests.")

        override fun createGuardedBlock(
            startOffset: Int,
            endOffset: Int,
        ): RangeMarker = error("Not needed for AI completion service tests.")

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

    private fun testInvocation(
        progressIndicator: Any?,
    ): AiCommitMessageActionInvocationResult.Invoked = AiCommitMessageActionInvocationResult.Invoked(
        action = TestAction(progressIndicator),
        actionId = "Vcs.LLMCommitMessageAction",
        source = AiCommitMessageActionSource.KnownActionId,
    )

    private fun progressIndicator(vararg runningStates: Boolean): ProgressIndicator {
        var callCount = 0
        return Proxy.newProxyInstance(
            ProgressIndicator::class.java.classLoader,
            arrayOf(ProgressIndicator::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "isRunning" -> runningStates.getOrElse(callCount++) { runningStates.last() }
                "toString" -> "Test ProgressIndicator"
                "hashCode" -> runningStates.contentHashCode()
                "equals" -> false
                else -> method.defaultReturnValue()
            }
        } as ProgressIndicator
    }

    private fun immediateOptions(
        timeout: Duration = Duration.ofSeconds(1),
        stoppedSignalGracePeriod: Duration = Duration.ofMillis(1),
    ): AiGenerationCompletionOptions = AiGenerationCompletionOptions(
        timeout = timeout,
        checkInterval = Duration.ofNanos(1),
        stoppedSignalGracePeriod = stoppedSignalGracePeriod,
    )

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
