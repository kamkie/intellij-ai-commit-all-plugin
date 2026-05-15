package pl.devopssolutions.aicommitall.ai

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.Disposer
import com.intellij.vcs.commit.CommitMessageUi
import java.awt.AWTEvent
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.InputMethodEvent
import java.awt.event.KeyEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

internal object CommitMessageUserEditSignalFactory {
    fun create(commitMessageUi: CommitMessageUi): ActiveCommitMessageUserEditSignal {
        val document = CommitMessageUiAccessors.editorDocument(commitMessageUi)
            ?: return ActiveCommitMessageUserEditSignal.NotEdited
        val component = CommitMessageUiAccessors.editorComponent(commitMessageUi)
            ?: return ActiveCommitMessageUserEditSignal.NotEdited

        return DocumentCommitMessageUserEditSignal(
            document = document,
            detector = AwtCommitMessageUserEditDetector(component),
        )
    }
}

internal interface ActiveCommitMessageUserEditSignal : AiGenerationUserEditSignal, AutoCloseable {
    override fun close()

    companion object {
        val NotEdited: ActiveCommitMessageUserEditSignal =
            object : ActiveCommitMessageUserEditSignal {
                override fun isUserEdited(): Boolean = false

                override fun close() = Unit
            }
    }
}

internal class DocumentCommitMessageUserEditSignal(
    private val document: Document,
    private val detector: CommitMessageUserEditDetector,
) : ActiveCommitMessageUserEditSignal {
    private val edited = AtomicBoolean(false)
    private val disposable = Disposer.newDisposable("AI Commit All commit message user edit signal")
    private val closed = AtomicBoolean(false)

    private val listener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            if (detector.isUserEditEventInProgress()) {
                edited.set(true)
            }
        }
    }

    init {
        document.addDocumentListener(listener, disposable)
    }

    override fun isUserEdited(): Boolean = edited.get()

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            Disposer.dispose(disposable)
        }
    }
}

internal fun interface CommitMessageUserEditDetector {
    fun isUserEditEventInProgress(): Boolean
}

internal class AwtCommitMessageUserEditDetector(
    private val editorComponent: Component,
    private val currentEventProvider: () -> AWTEvent? = { IdeEventQueue.getInstance().trueCurrentEvent },
) : CommitMessageUserEditDetector {
    override fun isUserEditEventInProgress(): Boolean {
        val event = currentEventProvider() ?: return false
        if (!event.canEditText()) {
            return false
        }

        val sourceComponent = event.source as? Component ?: return false
        return sourceComponent === editorComponent ||
                SwingUtilities.isDescendingFrom(sourceComponent, editorComponent)
    }

    private fun AWTEvent.canEditText(): Boolean =
        this is KeyEvent ||
                this is InputMethodEvent ||
                this is ActionEvent
}
