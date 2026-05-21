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

import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class FakeLlmCommitMessageAction : AnAction() {
    @Suppress("unused")
    @Volatile
    private var progressIndicator: ProgressIndicator? = null

    override fun actionPerformed(event: AnActionEvent) {
        recordInvocation()
        val indicator = EmptyProgressIndicator().also { progress ->
            progress.start()
        }
        progressIndicator = indicator
        val behavior = currentBehavior()
        if (behavior == FakeLlmCommitMessageBehavior.NeverFinishes) {
            scheduleProgressCleanup(indicator)
            return
        }
        JobScheduler.getScheduler().schedule(
            {
                try {
                    writeCommitMessage(event, messageForBehavior(event, behavior))
                } finally {
                    indicator.stop()
                    progressIndicator = null
                }
            },
            FAKE_GENERATION_DELAY_MILLIS,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun scheduleProgressCleanup(indicator: ProgressIndicator) {
        JobScheduler.getScheduler().schedule(
            {
                indicator.stop()
                if (progressIndicator === indicator) {
                    progressIndicator = null
                }
            },
            FAKE_TIMEOUT_CLEANUP_DELAY_MILLIS,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun messageForBehavior(
        event: AnActionEvent,
        behavior: FakeLlmCommitMessageBehavior,
    ): String = when (behavior) {
        FakeLlmCommitMessageBehavior.Generated -> GENERATED_MESSAGE
        FakeLlmCommitMessageBehavior.Empty -> ""
        FakeLlmCommitMessageBehavior.Unchanged -> currentCommitMessage(event)
        FakeLlmCommitMessageBehavior.NeverFinishes -> GENERATED_MESSAGE
    }

    private fun currentCommitMessage(event: AnActionEvent): String {
        val dataContext = event.dataContext
        return VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
            ?.commitMessageUi
            ?.text
            ?: VcsDataKeys.COMMIT_MESSAGE_DOCUMENT.getData(dataContext)
                ?.text
            ?: ""
    }

    private fun writeCommitMessage(
        event: AnActionEvent,
        message: String,
    ) {
        mutateCommitMessage {
            val dataContext = event.dataContext
            VcsDataKeys.COMMIT_WORKFLOW_UI.getData(dataContext)
                ?.commitMessageUi
                ?.writeText(message)
            VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(dataContext)
                ?.setCommitMessage(message)
            VcsDataKeys.COMMIT_MESSAGE_DOCUMENT.getData(dataContext)
                ?.writeText(message)
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
        private val invocationCounter = AtomicInteger()

        @Volatile
        private var behavior: FakeLlmCommitMessageBehavior = FakeLlmCommitMessageBehavior.Generated
        private const val FAKE_GENERATION_DELAY_MILLIS: Long = 750
        private const val FAKE_TIMEOUT_CLEANUP_DELAY_MILLIS: Long = 60_000

        fun setBehavior(nextBehavior: FakeLlmCommitMessageBehavior) {
            behavior = nextBehavior
        }

        fun reset() {
            behavior = FakeLlmCommitMessageBehavior.Generated
            invocationCounter.set(0)
        }

        fun invocationCount(): Int = invocationCounter.get()

        fun recordInvocation() {
            invocationCounter.incrementAndGet()
        }

        private fun currentBehavior(): FakeLlmCommitMessageBehavior = behavior
    }
}

class FakeUnavailableLlmCommitMessageAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        FakeLlmCommitMessageAction.recordInvocation()
    }
}

enum class FakeLlmCommitMessageBehavior {
    Generated,
    Empty,
    Unchanged,
    NeverFinishes,
}
