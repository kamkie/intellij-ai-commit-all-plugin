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

internal class GitOutgoingCommitsServiceTest {
    @Test
    fun `synchronous status loads through environment and refreshes actions when cache changes`() {
        val environment = CapturingGitOutgoingCommitsEnvironment(true)
        val scheduler = CapturingRefreshScheduler()
        val actionRefresh = CountingActionRefresh()
        val service = GitOutgoingCommitsService(
            project = testProject(),
            environment = environment,
            scheduler = scheduler,
            actionRefresh = actionRefresh,
        )

        assertTrue(service.hasOutgoingCommitsToPush())

        assertEquals(1, environment.loadCallCount)
        assertEquals(1, actionRefresh.count)
        assertEquals(0, scheduler.pendingCount)
    }

    @Test
    fun `repository change schedules cached outgoing commits refresh`() {
        val environment = CapturingGitOutgoingCommitsEnvironment(true)
        val scheduler = CapturingRefreshScheduler()
        val service = GitOutgoingCommitsService(
            project = testProject(),
            environment = environment,
            scheduler = scheduler,
            actionRefresh = CountingActionRefresh(),
        )

        environment.fireRepositoryChange()

        assertEquals(1, scheduler.pendingCount)
        assertFalse(service.cachedHasOutgoingCommitsToPush())

        scheduler.runNext()

        assertTrue(service.cachedHasOutgoingCommitsToPush())
    }

    @Test
    fun `push completion schedules cached outgoing commits refresh`() {
        val environment = CapturingGitOutgoingCommitsEnvironment(true)
        val scheduler = CapturingRefreshScheduler()
        val service = GitOutgoingCommitsService(
            project = testProject(),
            environment = environment,
            scheduler = scheduler,
            actionRefresh = CountingActionRefresh(),
        )

        environment.firePushCompletion()

        assertEquals(1, scheduler.pendingCount)

        scheduler.runNext()

        assertTrue(service.cachedHasOutgoingCommitsToPush())
    }

    @Test
    fun `service subscribes to repository changes and push completion using itself as disposable`() {
        val environment = CapturingGitOutgoingCommitsEnvironment(false)
        val service = GitOutgoingCommitsService(
            project = testProject(),
            environment = environment,
            scheduler = CapturingRefreshScheduler(),
            actionRefresh = CountingActionRefresh(),
        )

        assertEquals(service, environment.repositoryChangeParent)
        assertEquals(service, environment.pushCompletionParent)
    }

    private class CapturingGitOutgoingCommitsEnvironment(
        vararg values: Boolean,
    ) : GitOutgoingCommitsEnvironment {
        private val values = ArrayDeque(values.toList())
        private var repositoryChangeRefresh: (() -> Unit)? = null
        private var pushCompletionRefresh: (() -> Unit)? = null

        var loadCallCount = 0
        var repositoryChangeParent: Disposable? = null
        var pushCompletionParent: Disposable? = null

        override fun subscribeToRepositoryChanges(parentDisposable: Disposable, refresh: () -> Unit) {
            repositoryChangeParent = parentDisposable
            repositoryChangeRefresh = refresh
        }

        override fun subscribeToPushCompletion(parentDisposable: Disposable, refresh: () -> Unit) {
            pushCompletionParent = parentDisposable
            pushCompletionRefresh = refresh
        }

        override fun hasOutgoingCommitsToPush(): Boolean {
            loadCallCount += 1
            return values.removeFirst()
        }

        fun fireRepositoryChange() {
            requireNotNull(repositoryChangeRefresh).invoke()
        }

        fun firePushCompletion() {
            requireNotNull(pushCompletionRefresh).invoke()
        }
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
