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

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.ai.AiGenerationActivityPhase
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionEvidence
import pl.devopssolutions.aicommitall.ai.AiGenerationCompletionResult
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import java.awt.event.InputEvent
import java.lang.reflect.Proxy
import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

internal class AiCommitAllWorkflowRunnerTest {
    @Test
    fun `ai mode prepares the shared selection before AI generation and does not commit`() {
        val dependencies = CapturingWorkflowDependencies()

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Ai, testDataContext())
            .join()

        assertEquals(AiCommitAllWorkflowResult.Started, result)
        assertEquals(listOf("readiness", "prepare", "ai:Ai"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `commit mode reuses the shared preparation and commits only after AI generation completes`() {
        val completion = CompletableFuture<AiGenerationCompletionResult>()
        val dependencies = CapturingWorkflowDependencies(aiCompletion = completion)

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Commit, testDataContext())

        assertFalse(result.isDone)
        assertEquals(listOf("readiness", "prepare", "ai:Ai"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)

        completion.complete(completedAiGeneration())

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(listOf("readiness", "prepare", "ai:Ai", "commit"), dependencies.events)
        assertEquals(
            listOf(AiGenerationActivityPhase.Ai, AiGenerationActivityPhase.Commit),
            dependencies.activityPhases,
        )
        assertEquals(1, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `push mode reuses the shared preparation and pushes only after AI generation completes`() {
        val completion = CompletableFuture<AiGenerationCompletionResult>()
        val dependencies = CapturingWorkflowDependencies(aiCompletion = completion)

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())

        assertFalse(result.isDone)
        assertEquals(listOf("readiness", "prepare", "ai:Ai"), dependencies.events)
        assertEquals(0, dependencies.pushCallCount)

        completion.complete(completedAiGeneration())

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(listOf("readiness", "prepare", "ai:Ai", "push"), dependencies.events)
        assertEquals(
            listOf(AiGenerationActivityPhase.Ai, AiGenerationActivityPhase.Commit),
            dependencies.activityPhases,
        )
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(1, dependencies.pushCallCount)
        assertSame(dependencies.selection, dependencies.pushedSelection)
    }

    @Test
    fun `commit and push modes start activity on ai phase while generation is pending`() {
        listOf(AiCommitAllWorkflowMode.Commit, AiCommitAllWorkflowMode.Push).forEach { mode ->
            val completion = CompletableFuture<AiGenerationCompletionResult>()
            val dependencies = CapturingWorkflowDependencies(aiCompletion = completion)

            val result = runner(dependencies)
                .start(mode, testDataContext())

            assertFalse(result.isDone)
            assertEquals(listOf(AiGenerationActivityPhase.Ai), dependencies.activityStarts)
        }
    }

    @Test
    fun `push mode moves activity to push only after commit reports push start`() {
        val completion = CompletableFuture<AiGenerationCompletionResult>()
        val pushCompletion = CompletableFuture<Unit>()
        val dependencies = CapturingWorkflowDependencies(
            aiCompletion = completion,
            pushResult = CommitWorkflowExecutionResult.Started(pushCompletion),
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())

        completion.complete(completedAiGeneration())

        assertFalse(result.isDone)
        assertEquals(
            listOf(AiGenerationActivityPhase.Ai, AiGenerationActivityPhase.Commit),
            dependencies.activityPhases,
        )

        dependencies.onPushStarted?.invoke()

        assertFalse(result.isDone)
        assertEquals(
            listOf(AiGenerationActivityPhase.Ai, AiGenerationActivityPhase.Commit, AiGenerationActivityPhase.Push),
            dependencies.activityPhases,
        )

        pushCompletion.complete(Unit)

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(1, dependencies.activityFinishCount)
    }

    @Test
    fun `repeated starts return active workflow until it finishes`() {
        val completion = CompletableFuture<AiGenerationCompletionResult>()
        val dependencies = CapturingWorkflowDependencies(aiCompletion = completion)
        val runner = runner(dependencies)

        val first = runner.start(AiCommitAllWorkflowMode.Commit, testDataContext())
        val second = runner.start(AiCommitAllWorkflowMode.Push, testDataContext())

        assertSame(first, second)
        assertEquals(listOf("readiness", "prepare", "ai:Ai"), dependencies.events)

        completion.complete(completedAiGeneration())

        assertEquals(AiCommitAllWorkflowResult.Started, first.join())
        assertEquals(listOf("readiness", "prepare", "ai:Ai", "commit"), dependencies.events)

        val third = runner.start(AiCommitAllWorkflowMode.Push, testDataContext())

        assertFalse(first === third)
        assertEquals(AiCommitAllWorkflowResult.Started, third.join())
        assertEquals(
            listOf("readiness", "prepare", "ai:Ai", "commit", "readiness", "prepare", "ai:Ai", "push"),
            dependencies.events,
        )
    }

    @Test
    fun `workflow maps unusable AI completion results to stop reasons without execution`() {
        val cases = listOf(
            AiGenerationCompletionResult.Timeout(Duration.ofSeconds(5), "draft") to
                AiCommitAllWorkflowStopReason.AiTimeout,
            AiGenerationCompletionResult.EmptyMessage to
                AiCommitAllWorkflowStopReason.EmptyMessage,
            AiGenerationCompletionResult.UnchangedMessage("same message") to
                AiCommitAllWorkflowStopReason.UnchangedMessage,
            AiGenerationCompletionResult.NoCompletionSignal("generated message") to
                AiCommitAllWorkflowStopReason.NoCompletionSignal,
            AiGenerationCompletionResult.UserEditedMessage("user message") to
                AiCommitAllWorkflowStopReason.UserEditedMessage,
        )

        cases.forEach { (completionResult, expectedReason) ->
            val dependencies = CapturingWorkflowDependencies(
                aiCompletion = CompletableFuture.completedFuture(completionResult),
            )

            val result = runner(dependencies)
                .start(AiCommitAllWorkflowMode.Commit, testDataContext())
                .join()

            assertEquals(AiCommitAllWorkflowResult.Stopped(expectedReason), result)
            assertEquals(
                listOf("readiness", "prepare", "ai:Ai", "stop:${expectedReason.name}"),
                dependencies.events,
            )
            assertEquals(0, dependencies.commitCallCount)
            assertEquals(0, dependencies.pushCallCount)
        }
    }

    @Test
    fun `push mode stops without pushing when AI completion future fails`() {
        val completion = CompletableFuture<AiGenerationCompletionResult>()
        completion.completeExceptionally(IllegalStateException("AI completion failed"))
        val dependencies = CapturingWorkflowDependencies(aiCompletion = completion)

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.AiCompletionFailed),
            result,
        )
        assertEquals(listOf("readiness", "prepare", "ai:Ai", "stop:AiCompletionFailed"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `workflow stops before execution when AI generation cannot start`() {
        val dependencies = CapturingWorkflowDependencies(
            aiGenerationResult = AiCommitAllAiGenerationResult.Stopped(AiCommitAllWorkflowStopReason.MissingAiAction),
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.MissingAiAction),
            result,
        )
        assertEquals(listOf("readiness", "prepare", "ai:Ai", "stop:MissingAiAction"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `selection failure stops before AI generation or commit execution`() {
        val dependencies = CapturingWorkflowDependencies(
            selectionResult = CommitWorkflowSelectionResult.EmptySelection,
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.EmptySelection),
            result,
        )
        assertEquals(listOf("readiness", "prepare", "stop:EmptySelection"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `missing workflow stops before readiness check`() {
        val dependencies = CapturingWorkflowDependencies()

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Commit, DataContext.EMPTY_CONTEXT)
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.MissingWorkflow),
            result,
        )
        assertEquals(listOf("stop:MissingWorkflow"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `frozen vcs state stops before selection preparation`() {
        val dependencies = CapturingWorkflowDependencies(
            readinessResult = VcsOperationReadinessResult.Frozen,
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Commit, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.VcsFrozen),
            result,
        )
        assertEquals(listOf("readiness", "stop:VcsFrozen"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `background vcs operation stops before selection preparation`() {
        val dependencies = CapturingWorkflowDependencies(
            readinessResult = VcsOperationReadinessResult.BackgroundOperationRunning,
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.VcsBackgroundOperationRunning),
            result,
        )
        assertEquals(listOf("readiness", "stop:VcsBackgroundOperationRunning"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `commit mode stops when commit execution is unavailable after AI completion`() {
        val dependencies = CapturingWorkflowDependencies(
            commitResult = CommitWorkflowExecutionResult.UnsupportedExecutor,
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Commit, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.CommitExecutionUnavailable),
            result,
        )
        assertEquals(
            listOf("readiness", "prepare", "ai:Ai", "commit", "stop:CommitExecutionUnavailable"),
            dependencies.events,
        )
        assertEquals(1, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `push mode stops when push execution is unavailable after AI completion`() {
        val dependencies = CapturingWorkflowDependencies(
            pushResult = CommitWorkflowExecutionResult.DisabledExecutor,
        )

        val result = runner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.PushExecutionUnavailable),
            result,
        )
        assertEquals(
            listOf("readiness", "prepare", "ai:Ai", "push", "stop:PushExecutionUnavailable"),
            dependencies.events,
        )
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(1, dependencies.pushCallCount)
    }

    @Test
    fun `workflow preparation is scheduled off the caller thread before AI generation returns to EDT`() {
        val dependencies = CapturingWorkflowDependencies()
        val scheduler = CapturingWorkflowScheduler()

        val result = AiCommitAllWorkflowRunner(dependencies, scheduler)
            .start(AiCommitAllWorkflowMode.Ai, testDataContext())

        assertFalse(result.isDone)
        assertEquals(emptyList(), dependencies.events)
        assertEquals(1, scheduler.backgroundActionCount)
        assertEquals(0, scheduler.edtActionCount)

        scheduler.runNextBackground()

        assertFalse(result.isDone)
        assertEquals(listOf("readiness", "prepare"), dependencies.events)
        assertEquals(0, scheduler.backgroundActionCount)
        assertEquals(1, scheduler.edtActionCount)

        scheduler.runNextEdt()

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(listOf("readiness", "prepare", "ai:Ai"), dependencies.events)
    }

    @Test
    fun `workflow activity starts before background preparation and finishes after completion`() {
        val dependencies = CapturingWorkflowDependencies()
        val scheduler = CapturingWorkflowScheduler()

        val result = AiCommitAllWorkflowRunner(dependencies, scheduler)
            .start(AiCommitAllWorkflowMode.Commit, testDataContext())

        assertFalse(result.isDone)
        assertEquals(listOf(AiGenerationActivityPhase.Ai), dependencies.activityStarts)
        assertEquals(0, dependencies.activityFinishCount)

        scheduler.runNextBackground()

        assertFalse(result.isDone)
        assertEquals(0, dependencies.activityFinishCount)

        scheduler.runNextEdt()

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(1, dependencies.activityFinishCount)
    }

    private class CapturingWorkflowDependencies(
        val selection: GitChangeSelection = GitChangeSelection(emptyList()),
        private val aiCompletion: CompletableFuture<AiGenerationCompletionResult> =
            CompletableFuture.completedFuture(completedAiGeneration()),
        private val selectionResult: CommitWorkflowSelectionResult =
            CommitWorkflowSelectionResult.Prepared(selection),
        private val aiGenerationResult: AiCommitAllAiGenerationResult? = null,
        private val readinessResult: VcsOperationReadinessResult = VcsOperationReadinessResult.Ready,
        private val commitResult: CommitWorkflowExecutionResult = CommitWorkflowExecutionResult.Started(),
        private val pushResult: CommitWorkflowExecutionResult = CommitWorkflowExecutionResult.Started(),
    ) : AiCommitAllWorkflowDependencies {
        val events = mutableListOf<String>()
        var commitCallCount = 0
        var pushCallCount = 0
        var pushedSelection: GitChangeSelection? = null
        val activityStarts = mutableListOf<AiGenerationActivityPhase>()
        val activityPhases = mutableListOf<AiGenerationActivityPhase>()
        var activityFinishCount = 0
        var onPushStarted: (() -> Unit)? = null

        override fun startActivity(phase: AiGenerationActivityPhase): AiCommitAllWorkflowActivity {
            activityStarts += phase
            activityPhases += phase
            return object : AiCommitAllWorkflowActivity {
                override fun moveTo(phase: AiGenerationActivityPhase) {
                    activityPhases += phase
                }

                override fun close() {
                    activityFinishCount += 1
                }
            }
        }

        override fun checkReadiness(): VcsOperationReadinessResult {
            events += "readiness"
            return readinessResult
        }

        override fun prepareAllFilesSelection(
            workflowHandler: CommitWorkflowHandler,
            workflowUi: CommitWorkflowUi,
        ): CommitWorkflowSelectionResult {
            events += "prepare"
            return selectionResult
        }

        override fun runAiGeneration(
            phase: AiGenerationActivityPhase,
            workflowHandler: CommitWorkflowHandler,
            workflowUi: CommitWorkflowUi,
            parentDataContext: DataContext,
            inputEvent: InputEvent?,
        ): AiCommitAllAiGenerationResult {
            events += "ai:${phase.name}"
            return aiGenerationResult ?: AiCommitAllAiGenerationResult.AwaitingCompletion(aiCompletion)
        }

        override fun executeCommit(workflowHandler: CommitWorkflowHandler): CommitWorkflowExecutionResult {
            events += "commit"
            commitCallCount++
            return commitResult
        }

        override fun executeCommitAndPush(
            workflowHandler: CommitWorkflowHandler,
            selection: GitChangeSelection,
            onPushStarted: () -> Unit,
        ): CommitWorkflowExecutionResult {
            events += "push"
            pushCallCount++
            pushedSelection = selection
            this.onPushStarted = onPushStarted
            return pushResult
        }

        override fun reportStop(reason: AiCommitAllWorkflowStopReason) {
            events += "stop:${reason.name}"
        }
    }

    private companion object {
        private fun runner(dependencies: CapturingWorkflowDependencies): AiCommitAllWorkflowRunner = AiCommitAllWorkflowRunner(dependencies, ImmediateWorkflowScheduler)

        private fun completedAiGeneration(): AiGenerationCompletionResult = AiGenerationCompletionResult.Completed(
            originalMessage = "",
            generatedMessage = "Generated commit message",
            evidence = AiGenerationCompletionEvidence.ActionNoLongerRunningAndMessageChanged,
        )

        private fun testDataContext(
            workflowHandler: CommitWorkflowHandler = testProxy(),
            workflowUi: CommitWorkflowUi = testProxy(),
        ): DataContext {
            val data = mapOf(
                VcsDataKeys.COMMIT_WORKFLOW_HANDLER.name to workflowHandler,
                VcsDataKeys.COMMIT_WORKFLOW_UI.name to workflowUi,
            )
            return DataContext { dataId -> data[dataId] }
        }

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

        private fun <T> completedFrom(action: () -> T): CompletableFuture<T> {
            val future = CompletableFuture<T>()
            future.completeFrom(action)
            return future
        }

        private fun <T> CompletableFuture<T>.completeFrom(action: () -> T) {
            try {
                complete(action())
            } catch (throwable: Throwable) {
                completeExceptionally(throwable)
            }
        }
    }

    private object ImmediateWorkflowScheduler : AiCommitAllWorkflowScheduler {
        override fun <T> supplyBackground(action: () -> T): CompletableFuture<T> = completedFrom(action)

        override fun <T> supplyEdt(action: () -> T): CompletableFuture<T> = completedFrom(action)
    }

    private class CapturingWorkflowScheduler : AiCommitAllWorkflowScheduler {
        private val backgroundActions = ArrayDeque<() -> Unit>()
        private val edtActions = ArrayDeque<() -> Unit>()

        val backgroundActionCount: Int
            get() = backgroundActions.size

        val edtActionCount: Int
            get() = edtActions.size

        override fun <T> supplyBackground(action: () -> T): CompletableFuture<T> = queued(backgroundActions, action)

        override fun <T> supplyEdt(action: () -> T): CompletableFuture<T> = queued(edtActions, action)

        fun runNextBackground() {
            backgroundActions.removeFirst()()
        }

        fun runNextEdt() {
            edtActions.removeFirst()()
        }

        private fun <T> queued(
            actions: ArrayDeque<() -> Unit>,
            action: () -> T,
        ): CompletableFuture<T> {
            val future = CompletableFuture<T>()
            actions += {
                future.completeFrom(action)
            }
            return future
        }
    }
}
