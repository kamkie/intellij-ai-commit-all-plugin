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

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GitOutgoingCommitsStatusTest {
    @Test
    fun `service refreshes cached outgoing status after repository change callbacks`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val environment = CapturingOutgoingCommitsEnvironment(
            loaderResults = listOf(true),
        )
        val service = GitOutgoingCommitsService(
            project = testProject(),
            environment = environment,
            scheduler = scheduler,
            actionRefresh = actionRefresh,
        )

        environment.repositoryRefreshes.single().invoke()

        assertFalse(service.cachedHasOutgoingCommitsToPush())
        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertTrue(service.cachedHasOutgoingCommitsToPush())
        assertEquals(1, actionRefresh.count)
    }

    @Test
    fun `service refreshes cached outgoing status after push completion callbacks`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val environment = CapturingOutgoingCommitsEnvironment(
            loaderResults = listOf(true, false),
        )
        val service = GitOutgoingCommitsService(
            project = testProject(),
            environment = environment,
            scheduler = scheduler,
            actionRefresh = actionRefresh,
        )

        assertTrue(service.hasOutgoingCommitsToPush())

        environment.pushCompletionRefreshes.single().invoke()
        scheduler.runNext()

        assertFalse(service.cachedHasOutgoingCommitsToPush())
        assertEquals(2, actionRefresh.count)
    }

    @Test
    fun `cached status returns last known value and refreshes in background`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertTrue(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, actionRefresh.count)
        assertEquals(0, scheduler.pendingCount)
    }

    @Test
    fun `cached status coalesces repeated refresh requests`() {
        val scheduler = CapturingRefreshScheduler()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = CountingActionRefresh(),
            nowMillis = { 0L },
        )

        status.cachedHasOutgoingCommitsToPush()
        status.cachedHasOutgoingCommitsToPush()

        assertEquals(1, scheduler.pendingCount)
    }

    @Test
    fun `cached status throttles refreshes until interval elapses`() {
        var now = 0L
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            refreshIntervalMillis = 2_000L,
            nowMillis = { now },
        )

        status.cachedHasOutgoingCommitsToPush()
        scheduler.runNext()

        now = 1_999L
        assertTrue(status.cachedHasOutgoingCommitsToPush())
        assertEquals(0, scheduler.pendingCount)

        now = 2_000L
        assertTrue(status.cachedHasOutgoingCommitsToPush())

        assertEquals(1, scheduler.pendingCount)
        assertEquals(1, actionRefresh.count)
    }

    @Test
    fun `background refresh keeps cached value when loader fails`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        var loadCalls = 0
        val status = GitOutgoingCommitsStatus(
            loader = {
                loadCalls += 1
                error("outgoing commits unavailable")
            },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        status.cachedHasOutgoingCommitsToPush()
        scheduler.runNext()

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, loadCalls)
        assertEquals(0, actionRefresh.count)
        assertEquals(0, scheduler.pendingCount)
    }

    @Test
    fun `background refresh does not refresh actions when cache is unchanged`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val status = GitOutgoingCommitsStatus(
            loader = { false },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        status.cachedHasOutgoingCommitsToPush()
        scheduler.runNext()

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(0, actionRefresh.count)
    }

    @Test
    fun `synchronous status refreshes cache without scheduling background work`() {
        val scheduler = CapturingRefreshScheduler()
        val status = GitOutgoingCommitsStatus(
            loader = { true },
            scheduler = scheduler,
            actionRefresh = CountingActionRefresh(),
            nowMillis = { 0L },
        )

        assertTrue(status.hasOutgoingCommitsToPush())
        assertTrue(status.cachedHasOutgoingCommitsToPush())

        assertEquals(0, scheduler.pendingCount)
    }

    @Test
    fun `synchronous status refreshes actions only when cache changes`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val values = ArrayDeque(listOf(false, false, true))
        val status = GitOutgoingCommitsStatus(
            loader = { values.removeFirst() },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        assertFalse(status.hasOutgoingCommitsToPush())
        assertFalse(status.hasOutgoingCommitsToPush())
        assertTrue(status.hasOutgoingCommitsToPush())

        assertEquals(1, actionRefresh.count)
        assertEquals(0, scheduler.pendingCount)
    }

    @Test
    fun `requested refresh bypasses throttle and updates stale cached value`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val values = ArrayDeque(listOf(true, false))
        val status = GitOutgoingCommitsStatus(
            loader = { values.removeFirst() },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        assertTrue(status.hasOutgoingCommitsToPush())
        assertTrue(status.cachedHasOutgoingCommitsToPush())
        assertEquals(0, scheduler.pendingCount)

        status.requestRefresh()
        scheduler.runNext()

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(2, actionRefresh.count)
    }

    @Test
    fun `pending refresh runs after an in progress refresh loader failure`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val values = ArrayDeque<() -> Boolean>(
            listOf(
                { error("outgoing commits unavailable") },
                { true },
            ),
        )
        val status = GitOutgoingCommitsStatus(
            loader = { values.removeFirst().invoke() },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        status.cachedHasOutgoingCommitsToPush()
        status.requestRefresh()

        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, scheduler.pendingCount)
        assertEquals(0, actionRefresh.count)

        scheduler.runNext()

        assertTrue(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, actionRefresh.count)
    }

    @Test
    fun `requested refresh runs after refresh already in progress`() {
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val values = ArrayDeque(listOf(true, false))
        val status = GitOutgoingCommitsStatus(
            loader = { values.removeFirst() },
            scheduler = scheduler,
            actionRefresh = actionRefresh,
            nowMillis = { 0L },
        )

        status.cachedHasOutgoingCommitsToPush()
        status.requestRefresh()

        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertTrue(status.cachedHasOutgoingCommitsToPush())
        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertFalse(status.cachedHasOutgoingCommitsToPush())
        assertEquals(2, actionRefresh.count)
    }

    private class CapturingRefreshScheduler : GitOutgoingCommitsRefreshScheduler {
        private val pendingRefreshes = mutableListOf<() -> Unit>()
        val pendingCount: Int
            get() = pendingRefreshes.size

        override fun schedule(refresh: () -> Unit) {
            pendingRefreshes += refresh
        }

        fun runNext() {
            pendingRefreshes.removeAt(0).invoke()
        }
    }

    private class CountingActionRefresh : GitOutgoingCommitsActionRefresh {
        var count: Int = 0

        override fun refreshActions() {
            count++
        }
    }

    private class CapturingOutgoingCommitsEnvironment(
        loaderResults: List<Boolean>,
    ) : GitOutgoingCommitsEnvironment {
        private val results = ArrayDeque(loaderResults)
        private var lastResult = loaderResults.lastOrNull() ?: false
        val repositoryRefreshes = mutableListOf<() -> Unit>()
        val pushCompletionRefreshes = mutableListOf<() -> Unit>()

        override fun subscribeToRepositoryChanges(
            parentDisposable: Disposable,
            refresh: () -> Unit,
        ) {
            repositoryRefreshes += refresh
        }

        override fun subscribeToPushCompletion(
            parentDisposable: Disposable,
            refresh: () -> Unit,
        ) {
            pushCompletionRefreshes += refresh
        }

        override fun hasOutgoingCommitsToPush(): Boolean {
            if (results.isNotEmpty()) {
                lastResult = results.removeFirst()
            }
            return lastResult
        }
    }

    private fun testProject(): Project = Proxy.newProxyInstance(
        Project::class.java.classLoader,
        arrayOf(Project::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "toString" -> "Test Project"
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            else -> method.defaultReturnValue()
        }
    } as Project

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
