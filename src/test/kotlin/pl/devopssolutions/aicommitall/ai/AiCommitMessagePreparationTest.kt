package pl.devopssolutions.aicommitall.ai

import com.intellij.vcs.commit.CommitMessageUi
import kotlin.test.Test
import kotlin.test.assertEquals

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
    }

    @Test
    fun `preserves stale commit message when clearing is disabled`() {
        val commitMessageUi = TestCommitMessageUi("stale message")

        val snapshot = AiCommitMessagePreparation.prepareInitialSnapshot(
            commitMessageUi = commitMessageUi,
            clearBeforeGeneration = false,
        )

        assertEquals("stale message", commitMessageUi.text)
        assertEquals("stale message", snapshot.originalMessage)
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
