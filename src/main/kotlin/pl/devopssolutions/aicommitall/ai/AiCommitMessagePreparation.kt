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

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi

internal object AiCommitMessagePreparation {
    fun prepareInitialSnapshot(
        commitMessageUi: CommitMessageUi,
        clearBeforeGeneration: Boolean,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
    ): AiCommitMessageSnapshot {
        if (clearBeforeGeneration) {
            CommitMessageTextCleaner.clear(
                commitMessageUi = commitMessageUi,
                parentDataContext = parentDataContext,
            )
        }

        return AiCommitMessageSnapshot.capture(
            commitMessageUi = commitMessageUi,
            acceptUnchangedPrefilledMessage = !clearBeforeGeneration,
        )
    }
}

internal object CommitMessageTextCleaner {
    fun clear(
        commitMessageUi: CommitMessageUi,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        textMutationAccess: CommitMessageTextMutationAccess = EdtCommitMessageTextMutationAccess,
    ) {
        textMutationAccess.mutateText {
            commitMessageUi.text = ""
            (commitMessageUi as? CommitMessageI)?.setCommitMessage("")
            VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(parentDataContext)?.setCommitMessage("")
            CommitMessageUiAccessors.editorDocument(commitMessageUi)?.clearText()
            VcsDataKeys.COMMIT_MESSAGE_DOCUMENT.getData(parentDataContext)?.clearText()
        }
    }

    private fun Document.clearText() {
        if (textLength > 0) {
            setText("")
        }
    }
}

internal fun interface CommitMessageTextMutationAccess {
    fun mutateText(mutateNow: () -> Unit)
}

private object EdtCommitMessageTextMutationAccess : CommitMessageTextMutationAccess {
    override fun mutateText(mutateNow: () -> Unit) {
        val application = ApplicationManager.getApplication() ?: return mutateNow()
        val writeMutation = Runnable {
            application.runWriteAction {
                mutateNow()
            }
        }

        if (application.isDispatchThread) {
            writeMutation.run()
        } else {
            application.invokeAndWait(writeMutation)
        }
    }
}
