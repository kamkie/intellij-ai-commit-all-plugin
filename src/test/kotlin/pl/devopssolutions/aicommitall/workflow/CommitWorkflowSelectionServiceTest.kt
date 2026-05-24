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

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CommitWorkflowSelectionServiceTest {
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

    private class CapturingActivationSleeper : CommitWorkflowActivationSleeper {
        val delays = mutableListOf<Duration>()

        override fun sleep(duration: Duration) {
            delays += duration
        }
    }
}
