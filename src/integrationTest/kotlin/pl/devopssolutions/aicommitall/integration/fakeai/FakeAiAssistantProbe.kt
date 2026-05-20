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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys

object FakeAiAssistantProbe {
    @JvmStatic
    fun isCommitMessageActionRegistered(): Boolean = ActionManager.getInstance().getAction("Vcs.LLMCommitMessageAction") != null

    @JvmStatic
    fun generatedCommitMessageThroughDataContext(): String {
        val action = requireNotNull(ActionManager.getInstance().getAction("Vcs.LLMCommitMessageAction")) {
            "Vcs.LLMCommitMessageAction is not registered."
        }
        val document = EditorFactory.getInstance().createDocument("")
        val commitMessageControl = CapturingCommitMessageControl()
        val dataContext = SimpleDataContext.builder()
            .add(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT, document)
            .add(VcsDataKeys.COMMIT_MESSAGE_CONTROL, commitMessageControl)
            .build()
        val event = AnActionEvent.createEvent(
            action,
            dataContext,
            action.templatePresentation.clone(),
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            null,
        )

        ActionUtil.performAction(action, event)

        check(document.text == commitMessageControl.message) {
            "Fake AI action wrote different commit messages through document and control APIs."
        }
        check(document.text.isNotBlank()) {
            "Fake AI action did not write a commit message."
        }
        return document.text
    }

    private class CapturingCommitMessageControl : CommitMessageI {
        var message: String = ""
            private set

        override fun setCommitMessage(comment: String) {
            message = comment
        }
    }
}
