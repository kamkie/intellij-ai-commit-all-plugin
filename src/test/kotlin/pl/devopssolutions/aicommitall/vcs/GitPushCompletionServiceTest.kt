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
package pl.devopssolutions.aicommitall.vcs

import git4idea.push.GitPushRepoResult
import git4idea.repo.GitRepository
import git4idea.update.GitUpdateResult
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GitPushCompletionServiceTest {
    @Test
    fun `await completion preserves successful push results for every repository`() {
        val timeoutScheduler = ManualGitPushCompletionTimeoutScheduler()
        val tracker = GitPushCompletionTracker(timeoutScheduler)
        val firstRepository = testRepository("first")
        val secondRepository = testRepository("second")
        val firstResult = pushResult(GitPushRepoResult.Type.SUCCESS)
        val secondResult = pushResult(GitPushRepoResult.Type.UP_TO_DATE)

        val completion = tracker.awaitCompletion(
            repositories = listOf(firstRepository, secondRepository),
            timeoutMillis = 1_000L,
        )

        tracker.completeRepositoryPush(firstRepository, firstResult)

        assertFalse(completion.isDone)

        tracker.completeRepositoryPush(secondRepository, secondResult)

        assertEquals(
            GitPushCompletionResult.Success(
                mapOf(
                    firstRepository to firstResult,
                    secondRepository to secondResult,
                ),
            ),
            completion.join(),
        )
        assertTrue(timeoutScheduler.scheduled.single().cancelled)
    }

    @Test
    fun `await completion reports failed push result while preserving per repository state`() {
        val timeoutScheduler = ManualGitPushCompletionTimeoutScheduler()
        val tracker = GitPushCompletionTracker(timeoutScheduler)
        val successfulRepository = testRepository("successful")
        val failedRepository = testRepository("failed")
        val successResult = pushResult(GitPushRepoResult.Type.SUCCESS)
        val failedResult = pushResult(GitPushRepoResult.Type.ERROR, error = "remote rejected")

        val completion = tracker.awaitCompletion(
            repositories = listOf(successfulRepository, failedRepository),
            timeoutMillis = 1_000L,
        )

        tracker.completeRepositoryPush(successfulRepository, successResult)
        tracker.completeRepositoryPush(failedRepository, failedResult)

        assertEquals(
            GitPushCompletionResult.Failed(
                mapOf(
                    successfulRepository to successResult,
                    failedRepository to failedResult,
                ),
            ),
            completion.join(),
        )
    }

    @Test
    fun `await completion reports cancelled push result explicitly`() {
        val timeoutScheduler = ManualGitPushCompletionTimeoutScheduler()
        val tracker = GitPushCompletionTracker(timeoutScheduler)
        val repository = testRepository("cancelled")
        val cancelledResult = pushResult(GitPushRepoResult.Type.NOT_PUSHED)

        val completion = tracker.awaitCompletion(
            repositories = listOf(repository),
            timeoutMillis = 1_000L,
        )

        tracker.completeRepositoryPush(repository, cancelledResult)

        assertEquals(
            GitPushCompletionResult.Cancelled(mapOf(repository to cancelledResult)),
            completion.join(),
        )
    }

    @Test
    fun `await completion times out with completed results and pending repositories`() {
        val timeoutScheduler = ManualGitPushCompletionTimeoutScheduler()
        val tracker = GitPushCompletionTracker(timeoutScheduler)
        val completedRepository = testRepository("completed")
        val pendingRepository = testRepository("pending")
        val completedResult = pushResult(GitPushRepoResult.Type.SUCCESS)

        val completion = tracker.awaitCompletion(
            repositories = listOf(completedRepository, pendingRepository),
            timeoutMillis = 1_000L,
        )

        tracker.completeRepositoryPush(completedRepository, completedResult)
        timeoutScheduler.runNextTimeout()

        assertEquals(
            GitPushCompletionResult.TimedOut(
                completedResults = mapOf(completedRepository to completedResult),
                pendingRepositories = setOf(pendingRepository),
            ),
            completion.join(),
        )
    }

    private class ManualGitPushCompletionTimeoutScheduler : GitPushCompletionTimeoutScheduler {
        val scheduled = mutableListOf<ManualGitPushCompletionTimeoutHandle>()

        override fun schedule(
            timeoutMillis: Long,
            onTimeout: () -> Unit,
        ): GitPushCompletionTimeoutHandle {
            val handle = ManualGitPushCompletionTimeoutHandle(timeoutMillis, onTimeout)
            scheduled += handle
            return handle
        }

        fun runNextTimeout() {
            scheduled.first { handle -> !handle.cancelled }.run()
        }
    }

    private class ManualGitPushCompletionTimeoutHandle(
        val timeoutMillis: Long,
        private val onTimeout: () -> Unit,
    ) : GitPushCompletionTimeoutHandle {
        var cancelled = false

        override fun cancel() {
            cancelled = true
        }

        fun run() {
            assertEquals(1_000L, timeoutMillis)
            assertFalse(cancelled)
            onTimeout()
        }
    }

    private companion object {
        private fun testRepository(name: String): GitRepository = Proxy.newProxyInstance(
            GitRepository::class.java.classLoader,
            arrayOf(GitRepository::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "toString" -> name
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> method.defaultReturnValue()
            }
        } as GitRepository

        private fun pushResult(
            type: GitPushRepoResult.Type,
            error: String? = null,
        ): GitPushRepoResult {
            val constructor = GitPushRepoResult::class.java.getDeclaredConstructor(
                GitPushRepoResult.Type::class.java,
                Int::class.javaPrimitiveType,
                String::class.java,
                String::class.java,
                String::class.java,
                List::class.java,
                String::class.java,
                GitUpdateResult::class.java,
            )
            constructor.isAccessible = true
            return constructor.newInstance(
                type,
                1,
                "refs/heads/main",
                "refs/remotes/origin/main",
                "origin",
                emptyList<String>(),
                error,
                null,
            ) as GitPushRepoResult
        }

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
