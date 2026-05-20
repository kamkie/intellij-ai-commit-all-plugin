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
package pl.devopssolutions.aicommitall.integration.fakeai

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi

class FakeLlmCommitMessageAction : AnAction() {
    @Suppress("unused")
    private var progressIndicator: ProgressIndicator? = null

    override fun actionPerformed(event: AnActionEvent) {
        writeGeneratedMessage(event)
    }

    private fun writeGeneratedMessage(event: AnActionEvent) {
        mutateCommitMessage {
            val dataContext = event.dataContext
            VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
                ?.commitMessageUi
                ?.writeText(GENERATED_MESSAGE)
            VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(dataContext)
                ?.setCommitMessage(GENERATED_MESSAGE)
            VcsDataKeys.COMMIT_MESSAGE_DOCUMENT.getData(dataContext)
                ?.writeText(GENERATED_MESSAGE)
        }
    }

    private fun mutateCommitMessage(action: () -> Unit) {
        val application = ApplicationManager.getApplication()
        val writeAction = Runnable {
            application?.runWriteAction(action) ?: action()
        }

        if (application == null || application.isDispatchThread) {
            writeAction.run()
        } else {
            application.invokeAndWait(writeAction)
        }
    }

    private fun CommitMessageUi.writeText(message: String) {
        text = message
        (this as? CommitMessageI)?.setCommitMessage(message)
    }

    private fun Document.writeText(message: String) {
        setText(message)
    }

    companion object {
        const val GENERATED_MESSAGE: String = "AI Commit All release matrix message"
    }
}
