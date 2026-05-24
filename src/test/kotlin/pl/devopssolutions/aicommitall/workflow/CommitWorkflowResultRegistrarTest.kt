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
package pl.devopssolutions.aicommitall.workflow

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CommitWorkflowResultRegistrarTest {
    @Test
    fun `listener disposes immediately before reporting default success`() {
        val events = mutableListOf<String>()
        val handler = CapturingResultHandler(events)
        val listener = DisposableCommitWorkflowResultListener(
            resultHandler = handler,
            disposeListener = { events += "dispose" },
        )

        listener.onSuccess()

        assertEquals(listOf("dispose", "success"), events)
    }

    @Test
    fun `listener waits for after-refresh before disposing deferred success`() {
        val events = mutableListOf<String>()
        val handler = CapturingResultHandler(events, waitForAfterRefresh = true)
        val listener = DisposableCommitWorkflowResultListener(
            resultHandler = handler,
            disposeListener = { events += "dispose" },
        )

        listener.onSuccess()

        assertEquals(listOf("success"), events)

        listener.onAfterRefresh()

        assertEquals(listOf("success", "dispose", "afterRefresh"), events)
    }

    @Test
    fun `listener ignores after-refresh before a deferred success`() {
        val events = mutableListOf<String>()
        val handler = CapturingResultHandler(events, waitForAfterRefresh = true)
        val listener = DisposableCommitWorkflowResultListener(
            resultHandler = handler,
            disposeListener = { events += "dispose" },
        )

        listener.onAfterRefresh()

        assertEquals(emptyList(), events)
    }

    @Test
    fun `listener disposes before reporting cancel or failure`() {
        val cases = listOf(
            "cancel" to { listener: DisposableCommitWorkflowResultListener -> listener.onCancel() },
            "failure" to { listener: DisposableCommitWorkflowResultListener -> listener.onFailure() },
        )

        cases.forEach { (expectedEvent, complete) ->
            val events = mutableListOf<String>()
            val handler = CapturingResultHandler(events)
            val listener = DisposableCommitWorkflowResultListener(
                resultHandler = handler,
                disposeListener = { events += "dispose" },
            )

            complete(listener)

            assertEquals(listOf("dispose", expectedEvent), events)
        }
    }

    @Test
    fun `registration disposes its listener only once`() {
        var disposeCount = 0
        val registration = DisposableCommitWorkflowResultRegistration {
            disposeCount += 1
        }

        registration.dispose()
        registration.dispose()

        assertEquals(1, disposeCount)
    }

    private class CapturingResultHandler(
        private val events: MutableList<String>,
        private val waitForAfterRefresh: Boolean = false,
    ) : CommitWorkflowResultHandler {
        override val waitForAfterRefreshOnSuccess: Boolean = waitForAfterRefresh

        override fun onSuccess() {
            events += "success"
        }

        override fun onCancel() {
            events += "cancel"
        }

        override fun onFailure() {
            events += "failure"
        }

        override fun onAfterRefresh() {
            events += "afterRefresh"
        }
    }
}
