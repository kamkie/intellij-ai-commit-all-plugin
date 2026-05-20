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

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private typealias ImmediatePushDecision = SafeImmediatePushDecision.Immediate

internal class SafeImmediatePushServiceTest {
    @Test
    fun `prepare falls back when selection has no affected paths`() {
        val environment = CapturingSafeImmediatePushEnvironment()
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(GitChangeSelection(emptyList()))

        assertFallback(SafeImmediatePushFallbackReason.NoAffectedRepositories, decision)
        assertEquals(emptyList(), environment.pathLookups)
    }

    @Test
    fun `prepare falls back when an affected path has no repository`() {
        val environment = CapturingSafeImmediatePushEnvironment()
        val service = SafeImmediatePushService(testProject(), environment)
        val path = TestFilePath("/repo/untracked.txt")

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        assertFallback(SafeImmediatePushFallbackReason.MissingAffectedRepository, decision)
        assertEquals(listOf<FilePath>(path), environment.pathLookups)
    }

    @Test
    fun `prepare falls back when Git push support is unavailable`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            pushSupportAvailable = false,
            repositoriesByPath = mapOf(path.path to repository),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        assertFallback(SafeImmediatePushFallbackReason.UnsupportedPushApi, decision)
        assertEquals(emptyList(), environment.pushStateRequests)
    }

    @Test
    fun `prepare falls back when selected changes contain unresolved conflicts`() {
        val path = TestFilePath("/repo/conflict.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = listOf(conflictedChange(path)),
            ),
        )

        assertFallback(SafeImmediatePushFallbackReason.UnresolvedConflict, decision)
    }

    @Test
    fun `prepare creates immediate push plan for safe affected repositories`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushSpec = SafeImmediatePushSpecHandle("spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(pushSpec)),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )
        val completion = decision.asImmediate().plan.push()

        assertEquals(setOf(repository), environment.awaitedRepositories.single().toSet())
        assertEquals(mapOf(repository to pushSpec), environment.pushedSpecs.single())
        assertFalse(completion.isDone)

        environment.completePush()

        assertTrue(completion.isDone)
    }

    @Test
    fun `immediate push plan completes exceptionally when push invocation fails`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushFailure = IllegalStateException("push failed")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
            pushFailure = pushFailure,
        )
        val service = SafeImmediatePushService(testProject(), environment)
        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        val thrown = assertFailsWith<IllegalStateException> {
            decision.asImmediate().plan.push()
        }

        assertSame(pushFailure, thrown)
        assertTrue(environment.pushCompletion.isCompletedExceptionally)
    }

    @Test
    fun `prepare outgoing commits falls back when project is disposed`() {
        val environment = CapturingSafeImmediatePushEnvironment()
        val service = SafeImmediatePushService(testProject(disposed = true), environment)

        val decision = service.prepareOutgoingCommits()

        assertFallback(SafeImmediatePushFallbackReason.NoAffectedRepositories, decision)
        assertEquals(0, environment.allRepositoriesCallCount)
    }

    @Test
    fun `prepare outgoing commits pushes only repositories with outgoing commits`() {
        val firstRepository = SafeImmediatePushRepositoryHandle("first")
        val secondRepository = SafeImmediatePushRepositoryHandle("second")
        val thirdRepository = SafeImmediatePushRepositoryHandle("third")
        val firstSpec = SafeImmediatePushSpecHandle("first-spec")
        val secondSpec = SafeImmediatePushSpecHandle("second-spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(firstRepository, secondRepository, thirdRepository),
            pushStates = mapOf(
                firstRepository to pushState(firstSpec, localMatchesTrackedUpstream = false),
                secondRepository to pushState(secondSpec),
                thirdRepository to pushState(pushSpec = null),
            ),
            outgoingCommits = mapOf(
                firstRepository to true,
                secondRepository to false,
            ),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepareOutgoingCommits()
        decision.asImmediate().plan.push()

        assertEquals(setOf(firstRepository), environment.awaitedRepositories.single().toSet())
        assertEquals(mapOf(firstRepository to firstSpec), environment.pushedSpecs.single())
    }

    @Test
    fun `prepare outgoing commits falls back when no repository has outgoing commits`() {
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(repository),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
            outgoingCommits = mapOf(repository to false),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepareOutgoingCommits()

        assertFallback(SafeImmediatePushFallbackReason.NoAffectedRepositories, decision)
    }

    private class CapturingSafeImmediatePushEnvironment(
        private val pushSupportAvailable: Boolean = true,
        private val repositoriesByPath: Map<String, SafeImmediatePushRepositoryHandle> = emptyMap(),
        private val allRepositories: List<SafeImmediatePushRepositoryHandle> = emptyList(),
        private val pushStates: Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushRepositoryPushState> =
            emptyMap(),
        private val outgoingCommits: Map<SafeImmediatePushRepositoryHandle, Boolean> = emptyMap(),
        private val pushFailure: RuntimeException? = null,
    ) : SafeImmediatePushEnvironment {
        val pathLookups = mutableListOf<FilePath>()
        val pushStateRequests = mutableListOf<SafeImmediatePushRepositoryHandle>()
        val awaitedRepositories = mutableListOf<Collection<SafeImmediatePushRepositoryHandle>>()
        val pushedSpecs = mutableListOf<Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>>()
        val pushCompletion: CompletableFuture<Unit> = CompletableFuture()
        var allRepositoriesCallCount = 0

        override fun repositoryForPath(path: FilePath): SafeImmediatePushRepositoryHandle? {
            pathLookups += path
            return repositoriesByPath[path.path]
        }

        override fun repositories(): List<SafeImmediatePushRepositoryHandle> {
            allRepositoriesCallCount += 1
            return allRepositories
        }

        override fun isPushSupportAvailable(): Boolean = pushSupportAvailable

        override fun pushState(
            repository: SafeImmediatePushRepositoryHandle,
        ): SafeImmediatePushRepositoryPushState {
            pushStateRequests += repository
            return pushStates.getValue(repository)
        }

        override fun hasOutgoingCommits(
            repository: SafeImmediatePushRepositoryHandle,
            pushSpec: SafeImmediatePushSpecHandle,
        ): Boolean = outgoingCommits.getValue(repository)

        override fun awaitPushCompletion(
            repositories: Collection<SafeImmediatePushRepositoryHandle>,
        ): CompletableFuture<Unit> {
            awaitedRepositories += repositories
            return pushCompletion
        }

        override fun push(pushSpecs: Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>) {
            pushedSpecs += pushSpecs
            pushFailure?.let { failure -> throw failure }
        }

        fun completePush() {
            pushCompletion.complete(Unit)
        }
    }

    private fun pushState(
        pushSpec: SafeImmediatePushSpecHandle?,
        hasTrackedUpstream: Boolean = true,
        localMatchesTrackedUpstream: Boolean = true,
        targetIsTrackingBranch: Boolean = true,
        targetMatchesTrackedUpstream: Boolean = true,
        targetIsNewBranch: Boolean = false,
        targetIsSpecialRef: Boolean = false,
        repositoryStateIsNormal: Boolean = true,
    ): SafeImmediatePushRepositoryPushState = SafeImmediatePushRepositoryPushState(
        repositoryState = SafeImmediatePushRepositoryState(
            hasTrackedUpstream = hasTrackedUpstream,
            localMatchesTrackedUpstream = localMatchesTrackedUpstream,
            targetIsTrackingBranch = targetIsTrackingBranch,
            targetMatchesTrackedUpstream = targetMatchesTrackedUpstream,
            pushSpecAvailable = pushSpec != null,
            targetIsNewBranch = targetIsNewBranch,
            targetIsSpecialRef = targetIsSpecialRef,
            repositoryStateIsNormal = repositoryStateIsNormal,
        ),
        pushSpec = pushSpec,
    )

    private fun assertFallback(
        reason: SafeImmediatePushFallbackReason,
        decision: SafeImmediatePushDecision,
    ) {
        assertEquals(reason, (decision as SafeImmediatePushDecision.Fallback).reason)
    }

    private fun SafeImmediatePushDecision.asImmediate() = this as ImmediatePushDecision

    private fun conflictedChange(path: FilePath): Change = Change(
        TestContentRevision(path),
        TestContentRevision(path),
        FileStatus.MERGED_WITH_CONFLICTS,
    )

    private class TestContentRevision(private val filePath: FilePath) : ContentRevision {
        override fun getContent(): String? = null

        override fun getFile(): FilePath = filePath

        override fun getRevisionNumber(): VcsRevisionNumber = VcsRevisionNumber.NULL
    }

    private fun testProject(disposed: Boolean = false): Project = Proxy.newProxyInstance(
        Project::class.java.classLoader,
        arrayOf(Project::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "isDisposed" -> disposed
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

    private class TestFilePath(private val rawPath: String) : FilePath {
        override fun getVirtualFile(): VirtualFile? = null

        override fun getVirtualFileParent(): VirtualFile? = null

        override fun getIOFile(): File = File(rawPath)

        override fun getName(): String = ioFile.name

        override fun getPresentableUrl(): String = rawPath

        override fun getCharset(): Charset = Charsets.UTF_8

        override fun getCharset(project: Project?): Charset = Charsets.UTF_8

        override fun getFileType(): FileType = PlainTextFileType.INSTANCE

        override fun getPath(): String = rawPath

        override fun isDirectory(): Boolean = false

        override fun isUnder(parent: FilePath, strict: Boolean): Boolean = false

        override fun getParentPath(): FilePath? = null

        override fun isNonLocal(): Boolean = false
    }
}
