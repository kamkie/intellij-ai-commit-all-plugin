package pl.devopssolutions.aicommitall.ai

import com.intellij.vcs.commit.CommitMessageUi
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommitMessageUiReaderTest {
    @Test
    fun `reads commit message through text access boundary`() {
        val textAccess = CapturingCommitMessageUiTextAccess()
        val reader = CommitMessageUiReader(
            commitMessageUi = TestCommitMessageUi("generated message"),
            textAccess = textAccess,
        )

        val message = reader.readMessage()

        assertEquals("generated message", message)
        assertEquals(1, textAccess.readCallCount)
        assertEquals("generated message", textAccess.observedMessage)
    }

    private class CapturingCommitMessageUiTextAccess : CommitMessageUiTextAccess {
        var readCallCount = 0
        var observedMessage: String? = null

        override fun readText(readNow: () -> String): String {
            readCallCount++
            return readNow().also { message -> observedMessage = message }
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
}
