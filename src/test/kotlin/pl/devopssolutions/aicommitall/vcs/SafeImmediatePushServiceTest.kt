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
import git4idea.push.GitPushRepoResult
import git4idea.repo.GitRepository
import git4idea.update.GitUpdateResult
import java.io.File
import java.lang.reflect.Proxy
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
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
    fun `prepare extracts before and after revision paths from moved changes`() {
        val beforePath = TestFilePath("/repo/src/OldName.kt")
        val afterPath = TestFilePath("/repo/src/NewName.kt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(
                beforePath.path to repository,
                afterPath.path to repository,
            ),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = listOf(trackedChange(beforePath, afterPath)),
            ),
        )
        decision.asImmediate().plan.push()

        assertEquals(listOf<FilePath>(beforePath, afterPath), environment.pathLookups)
        assertEquals(
            mapOf(repository to SafeImmediatePushSpecHandle("spec")),
            environment.pushedSpecs.single(),
        )
    }

    @Test
    fun `prepare includes resolved conflict and staging area paths in affected repositories`() {
        val resolvedConflictPath = TestFilePath("/repo/resolved-conflict.txt")
        val stagingAreaPath = TestFilePath("/repo/staged-only.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushSpec = SafeImmediatePushSpecHandle("spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(
                resolvedConflictPath.path to repository,
                stagingAreaPath.path to repository,
            ),
            pushStates = mapOf(repository to pushState(pushSpec)),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                resolvedConflictPaths = listOf(resolvedConflictPath),
                stagingAreaPaths = listOf(stagingAreaPath),
            ),
        )
        decision.asImmediate().plan.push()

        assertEquals(listOf<FilePath>(resolvedConflictPath, stagingAreaPath), environment.pathLookups)
        assertEquals(mapOf(repository to pushSpec), environment.pushedSpecs.single())
    }

    @Test
    fun `prepare deduplicates repeated affected paths before resolving repositories`() {
        val path = TestFilePath("/repo/duplicated.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushSpec = SafeImmediatePushSpecHandle("spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(pushSpec)),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = listOf(trackedChange(path, path)),
                unversionedFiles = listOf(TestFilePath(path.path)),
                resolvedConflictPaths = listOf(TestFilePath(path.path)),
                stagingAreaPaths = listOf(TestFilePath(path.path)),
            ),
        )
        decision.asImmediate().plan.push()

        assertEquals(listOf<FilePath>(path), environment.pathLookups)
        assertEquals(mapOf(repository to pushSpec), environment.pushedSpecs.single())
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
    fun `prepare stops at first missing affected repository before loading push states`() {
        val firstPath = TestFilePath("/repo/known.txt")
        val missingPath = TestFilePath("/outside/missing.txt")
        val skippedPath = TestFilePath("/repo/skipped.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(
                firstPath.path to repository,
                skippedPath.path to repository,
            ),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(firstPath, missingPath, skippedPath),
            ),
        )

        assertFallback(SafeImmediatePushFallbackReason.MissingAffectedRepository, decision)
        assertEquals(listOf<FilePath>(firstPath, missingPath), environment.pathLookups)
        assertEquals(emptyList(), environment.pushStateRequests)
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
    fun `prepare preserves stable push spec ordering across multiple affected roots`() {
        val firstPath = TestFilePath("/repo-a/modified.txt")
        val secondPath = TestFilePath("/repo-b/modified.txt")
        val duplicateFirstPath = TestFilePath(firstPath.path)
        val firstRepository = SafeImmediatePushRepositoryHandle("first")
        val secondRepository = SafeImmediatePushRepositoryHandle("second")
        val firstSpec = SafeImmediatePushSpecHandle("first-spec")
        val secondSpec = SafeImmediatePushSpecHandle("second-spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(
                firstPath.path to firstRepository,
                secondPath.path to secondRepository,
            ),
            pushStates = mapOf(
                firstRepository to pushState(firstSpec),
                secondRepository to pushState(secondSpec),
            ),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(firstPath, secondPath, duplicateFirstPath),
            ),
        )
        decision.asImmediate().plan.push()

        assertEquals(listOf<FilePath>(firstPath, secondPath), environment.pathLookups)
        assertEquals(
            listOf(firstRepository, secondRepository),
            environment.pushedSpecs.single().keys.toList(),
        )
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
    fun `prepare retries refreshable unavailable push metadata before falling back`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushSpec = SafeImmediatePushSpecHandle("spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStateSequences = mapOf(
                repository to listOf(
                    pushState(pushSpec = null),
                    pushState(pushSpec),
                ),
            ),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )
        decision.asImmediate().plan.push()

        assertEquals(listOf(repository, repository), environment.pushStateRequests)
        assertEquals(mapOf(repository to pushSpec), environment.pushedSpecs.single())
    }

    @Test
    fun `prepare falls back after refreshable push metadata never settles`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStateSequences = mapOf(
                repository to listOf(
                    pushState(pushSpec = null),
                    pushState(pushSpec = null),
                    pushState(pushSpec = null),
                    pushState(SafeImmediatePushSpecHandle("late-spec")),
                ),
            ),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        assertFallback(SafeImmediatePushFallbackReason.UnsupportedPushApi, decision)
        assertEquals(listOf(repository, repository, repository), environment.pushStateRequests)
        assertEquals(emptyList(), environment.pushedSpecs)
    }

    @Test
    fun `prepare falls back immediately when repository state is unsafe even if later metadata is safe`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushSpec = SafeImmediatePushSpecHandle("spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStateSequences = mapOf(
                repository to listOf(
                    pushState(pushSpec, repositoryStateIsNormal = false),
                    pushState(pushSpec),
                ),
            ),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        assertFallback(SafeImmediatePushFallbackReason.UnsafeRepositoryState, decision)
        assertEquals(listOf(repository), environment.pushStateRequests)
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
    fun `immediate push plan completes exceptionally when push completion reports failure`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val failedResult = GitPushCompletionResult.Failed(
            mapOf(testRepository("repo") to pushResult(GitPushRepoResult.Type.ERROR, error = "remote rejected")),
        )
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
        )
        val service = SafeImmediatePushService(testProject(), environment)
        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        val completion = decision.asImmediate().plan.push()
        environment.completePush(failedResult)

        val thrown = assertCompletionFailsWith<SafeImmediatePushFailedException>(completion)
        assertSame(failedResult, thrown.result)
    }

    @Test
    fun `immediate push plan completes exceptionally when push completion reports cancellation`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val cancelledResult = GitPushCompletionResult.Cancelled(
            mapOf(testRepository("repo") to pushResult(GitPushRepoResult.Type.NOT_PUSHED)),
        )
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
        )
        val service = SafeImmediatePushService(testProject(), environment)
        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        val completion = decision.asImmediate().plan.push()
        environment.completePush(cancelledResult)

        val thrown = assertCompletionFailsWith<SafeImmediatePushCancelledException>(completion)
        assertSame(cancelledResult, thrown.result)
    }

    @Test
    fun `immediate push plan completes exceptionally when push completion times out`() {
        val path = TestFilePath("/repo/modified.txt")
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val timedOutResult = GitPushCompletionResult.TimedOut(
            completedResults = emptyMap(),
            pendingRepositories = setOf(testRepository("repo")),
        )
        val environment = CapturingSafeImmediatePushEnvironment(
            repositoriesByPath = mapOf(path.path to repository),
            pushStates = mapOf(repository to pushState(SafeImmediatePushSpecHandle("spec"))),
        )
        val service = SafeImmediatePushService(testProject(), environment)
        val decision = service.prepare(
            GitChangeSelection(
                trackedChanges = emptyList(),
                unversionedFiles = listOf(path),
            ),
        )

        val completion = decision.asImmediate().plan.push()
        environment.completePush(timedOutResult)

        val thrown = assertCompletionFailsWith<SafeImmediatePushTimedOutException>(completion)
        assertSame(timedOutResult, thrown.result)
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
        val thirdSpec = SafeImmediatePushSpecHandle("third-spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(firstRepository, secondRepository, thirdRepository),
            pushStates = mapOf(
                firstRepository to pushState(firstSpec, localMatchesTrackedUpstream = false),
                secondRepository to pushState(secondSpec),
                thirdRepository to pushState(thirdSpec),
            ),
            outgoingCommits = mapOf(
                firstRepository to true,
                secondRepository to false,
                thirdRepository to false,
            ),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepareOutgoingCommits()
        decision.asImmediate().plan.push()

        assertEquals(setOf(firstRepository), environment.awaitedRepositories.single().toSet())
        assertEquals(mapOf(firstRepository to firstSpec), environment.pushedSpecs.single())
    }

    @Test
    fun `prepare outgoing commits does not push a safe subset while another repository has unavailable metadata`() {
        val outgoingRepository = SafeImmediatePushRepositoryHandle("outgoing")
        val unavailableRepository = SafeImmediatePushRepositoryHandle("unavailable")
        val outgoingSpec = SafeImmediatePushSpecHandle("outgoing-spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(outgoingRepository, unavailableRepository),
            pushStates = mapOf(
                outgoingRepository to pushState(outgoingSpec, localMatchesTrackedUpstream = false),
                unavailableRepository to pushState(pushSpec = null),
            ),
            outgoingCommits = mapOf(outgoingRepository to true),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepareOutgoingCommits()

        assertFallback(SafeImmediatePushFallbackReason.UnsupportedPushApi, decision)
        assertEquals(
            listOf(
                outgoingRepository,
                unavailableRepository,
                outgoingRepository,
                unavailableRepository,
                outgoingRepository,
                unavailableRepository,
            ),
            environment.pushStateRequests,
        )
        assertEquals(
            listOf(
                outgoingRepository to outgoingSpec,
                outgoingRepository to outgoingSpec,
                outgoingRepository to outgoingSpec,
            ),
            environment.outgoingCommitRequests,
        )
        assertEquals(emptyList(), environment.pushedSpecs)
    }

    @Test
    fun `prepare outgoing commits falls back when an outgoing repository state is unsafe`() {
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(repository),
            pushStates = mapOf(
                repository to pushState(
                    SafeImmediatePushSpecHandle("spec"),
                    localMatchesTrackedUpstream = false,
                    repositoryStateIsNormal = false,
                ),
            ),
            outgoingCommits = mapOf(repository to true),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepareOutgoingCommits()

        assertFallback(SafeImmediatePushFallbackReason.UnsafeRepositoryState, decision)
        assertEquals(emptyList(), environment.pushedSpecs)
    }

    @Test
    fun `prepare outgoing commits propagates outgoing checks that fail before push planning completes`() {
        val outgoingRepository = SafeImmediatePushRepositoryHandle("outgoing")
        val failingRepository = SafeImmediatePushRepositoryHandle("failing")
        val failure = IllegalStateException("outgoing commits unavailable")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(outgoingRepository, failingRepository),
            pushStates = mapOf(
                outgoingRepository to pushState(
                    SafeImmediatePushSpecHandle("outgoing-spec"),
                    localMatchesTrackedUpstream = false,
                ),
                failingRepository to pushState(SafeImmediatePushSpecHandle("failing-spec")),
            ),
            outgoingCommits = mapOf(outgoingRepository to true),
            outgoingFailures = mapOf(failingRepository to failure),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val thrown = assertFailsWith<IllegalStateException> {
            service.prepareOutgoingCommits()
        }

        assertSame(failure, thrown)
        assertEquals(
            listOf(
                outgoingRepository to SafeImmediatePushSpecHandle("outgoing-spec"),
                failingRepository to SafeImmediatePushSpecHandle("failing-spec"),
            ),
            environment.outgoingCommitRequests,
        )
        assertEquals(emptyList(), environment.pushedSpecs)
    }

    @Test
    fun `prepare outgoing commits retries refreshable unavailable push metadata before stopping`() {
        val repository = SafeImmediatePushRepositoryHandle("repo")
        val pushSpec = SafeImmediatePushSpecHandle("spec")
        val environment = CapturingSafeImmediatePushEnvironment(
            allRepositories = listOf(repository),
            pushStateSequences = mapOf(
                repository to listOf(
                    pushState(pushSpec = null),
                    pushState(pushSpec),
                ),
            ),
            outgoingCommits = mapOf(repository to true),
        )
        val service = SafeImmediatePushService(testProject(), environment)

        val decision = service.prepareOutgoingCommits()
        decision.asImmediate().plan.push()

        assertEquals(listOf(repository, repository), environment.pushStateRequests)
        assertEquals(mapOf(repository to pushSpec), environment.pushedSpecs.single())
    }

    @Test
    fun `metadata settling sleeper does not block dispatch thread`() {
        val delays = mutableListOf<Long>()
        val sleeper = DispatchAwareSafeImmediatePushMetadataSettlingSleeper(
            isDispatchThread = { true },
            sleepMillis = { delay -> delays += delay },
        )

        sleeper.sleep(Duration.ofMillis(50))

        assertEquals(emptyList(), delays)
    }

    @Test
    fun `metadata settling sleeper waits outside dispatch thread`() {
        val delays = mutableListOf<Long>()
        val sleeper = DispatchAwareSafeImmediatePushMetadataSettlingSleeper(
            isDispatchThread = { false },
            sleepMillis = { delay -> delays += delay },
        )

        sleeper.sleep(Duration.ofMillis(50))

        assertEquals(listOf(50L), delays)
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
        private val pushStateSequences: Map<
            SafeImmediatePushRepositoryHandle,
            List<SafeImmediatePushRepositoryPushState>,
            > = emptyMap(),
        private val outgoingCommits: Map<SafeImmediatePushRepositoryHandle, Boolean> = emptyMap(),
        private val outgoingFailures: Map<SafeImmediatePushRepositoryHandle, RuntimeException> = emptyMap(),
        private val pushFailure: RuntimeException? = null,
    ) : SafeImmediatePushEnvironment {
        val pathLookups = mutableListOf<FilePath>()
        val pushStateRequests = mutableListOf<SafeImmediatePushRepositoryHandle>()
        val outgoingCommitRequests =
            mutableListOf<Pair<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>>()
        val awaitedRepositories = mutableListOf<Collection<SafeImmediatePushRepositoryHandle>>()
        val pushedSpecs = mutableListOf<Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>>()
        val pushCompletion: CompletableFuture<GitPushCompletionResult> = CompletableFuture()
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
            val stateSequence = pushStateSequences[repository]
            if (stateSequence != null) {
                val sequenceIndex = pushStateRequests.count { request -> request == repository } - 1
                return stateSequence.getOrElse(sequenceIndex) { stateSequence.last() }
            }
            return pushStates.getValue(repository)
        }

        override fun hasOutgoingCommits(
            repository: SafeImmediatePushRepositoryHandle,
            pushSpec: SafeImmediatePushSpecHandle,
        ): Boolean {
            outgoingCommitRequests += repository to pushSpec
            outgoingFailures[repository]?.let { failure -> throw failure }
            return outgoingCommits.getValue(repository)
        }

        override fun awaitPushCompletion(
            repositories: Collection<SafeImmediatePushRepositoryHandle>,
        ): CompletableFuture<GitPushCompletionResult> {
            awaitedRepositories += repositories
            return pushCompletion
        }

        override fun push(pushSpecs: Map<SafeImmediatePushRepositoryHandle, SafeImmediatePushSpecHandle>) {
            pushedSpecs += pushSpecs
            pushFailure?.let { failure -> throw failure }
        }

        fun completePush(
            result: GitPushCompletionResult = GitPushCompletionResult.Success(emptyMap()),
        ) {
            pushCompletion.complete(result)
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

    private inline fun <reified T : Throwable> assertCompletionFailsWith(
        completion: CompletableFuture<Unit>,
    ): T {
        val thrown = assertFailsWith<CompletionException> {
            completion.join()
        }
        val cause = thrown.cause
        assertTrue(cause is T)
        return cause
    }

    private fun trackedChange(
        beforePath: FilePath,
        afterPath: FilePath,
        status: FileStatus = FileStatus.MODIFIED,
    ): Change = Change(
        TestContentRevision(beforePath),
        TestContentRevision(afterPath),
        status,
    )

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
