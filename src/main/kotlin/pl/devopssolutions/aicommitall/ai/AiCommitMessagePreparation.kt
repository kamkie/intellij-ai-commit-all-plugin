package pl.devopssolutions.aicommitall.ai

import com.intellij.vcs.commit.CommitMessageUi

internal object AiCommitMessagePreparation {
    fun prepareInitialSnapshot(
        commitMessageUi: CommitMessageUi,
        clearBeforeGeneration: Boolean,
    ): AiCommitMessageSnapshot {
        if (clearBeforeGeneration) {
            commitMessageUi.text = ""
        }

        return AiCommitMessageSnapshot.capture(commitMessageUi)
    }
}
