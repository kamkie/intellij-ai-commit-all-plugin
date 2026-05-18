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
