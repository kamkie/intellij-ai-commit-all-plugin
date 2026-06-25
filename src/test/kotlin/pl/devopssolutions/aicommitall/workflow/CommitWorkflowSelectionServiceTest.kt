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

import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.lang.reflect.Proxy
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CommitWorkflowSelectionServiceTest {
    @Test
    fun `missing workflow is returned when handler or ui is absent`() {
        val service = CommitWorkflowSelectionService(testProject())
        val workflowHandler = testProxy<CommitWorkflowHandler>()
        val workflowUi = testProxy<CommitWorkflowUi>()

        val results = listOf(
            service.prepareAllFilesSelection(null, workflowUi),
            service.prepareAllFilesSelection(workflowHandler, null),
            service.prepareAllFilesSelection(null, null),
        )

        assertEquals(
            List(results.size) { CommitWorkflowSelectionResult.MissingWorkflow },
            results,
        )
    }

    @Test
    fun `commit workflow ui thread access runs the action directly without an application`() {
        val result = CommitWorkflowUiThreadAccess.run { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `activation retry succeeds when commit workflow activation settles`() {
        var attempts = 0
        val sleeper = CapturingActivationSleeper()

        val activated = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(50),
            sleeper = sleeper,
        ).activate {
            attempts++
            attempts == 2
        }

        assertTrue(activated)
        assertEquals(2, attempts)
        assertEquals(listOf(Duration.ofMillis(50)), sleeper.delays)
    }

    @Test
    fun `activation retry returns false after bounded attempts`() {
        var attempts = 0
        val sleeper = CapturingActivationSleeper()

        val activated = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(50),
            sleeper = sleeper,
        ).activate {
            attempts++
            false
        }

        assertFalse(activated)
        assertEquals(3, attempts)
        assertEquals(listOf(Duration.ofMillis(50), Duration.ofMillis(50)), sleeper.delays)
    }

    @Test
    fun `activation retry does not sleep when retry interval is zero`() {
        var attempts = 0
        val sleeper = CapturingActivationSleeper()

        val activated = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ZERO,
            sleeper = sleeper,
        ).activate {
            attempts++
            false
        }

        assertFalse(activated)
        assertEquals(3, attempts)
        assertEquals(emptyList(), sleeper.delays)
    }

    @Test
    fun `activation retry rejects invalid settings`() {
        assertFailsWith<IllegalArgumentException> {
            CommitWorkflowActivationRetry(maxAttempts = 0, retryInterval = Duration.ZERO)
        }
        assertFailsWith<IllegalArgumentException> {
            CommitWorkflowActivationRetry(maxAttempts = 1, retryInterval = Duration.ofMillis(-1))
        }
    }

    private class CapturingActivationSleeper : CommitWorkflowActivationSleeper {
        val delays = mutableListOf<Duration>()

        override fun sleep(duration: Duration) {
            delays += duration
        }
    }

    private companion object {
        private fun testProject(): Project = testProxy()

        private inline fun <reified T : Any> testProxy(): T = Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "toString" -> "Test ${T::class.java.simpleName}"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> method.defaultReturnValue()
            }
        } as T

        private fun java.lang.reflect.Method.defaultReturnValue(): Any? = when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Void.TYPE -> null
            else -> null
        }
    }
}
