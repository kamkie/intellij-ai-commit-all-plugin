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

        val result = AiCommitAllWorkflowRunner(dependencies)
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

        val result = AiCommitAllWorkflowRunner(dependencies)
            .start(AiCommitAllWorkflowMode.Commit, testDataContext())

        assertFalse(result.isDone)
        assertEquals(listOf("readiness", "prepare", "ai:Commit"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)

        completion.complete(completedAiGeneration())

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(listOf("readiness", "prepare", "ai:Commit", "commit"), dependencies.events)
        assertEquals(1, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `push mode reuses the shared preparation and pushes only after AI generation completes`() {
        val completion = CompletableFuture<AiGenerationCompletionResult>()
        val dependencies = CapturingWorkflowDependencies(aiCompletion = completion)

        val result = AiCommitAllWorkflowRunner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())

        assertFalse(result.isDone)
        assertEquals(listOf("readiness", "prepare", "ai:Push"), dependencies.events)
        assertEquals(0, dependencies.pushCallCount)

        completion.complete(completedAiGeneration())

        assertEquals(AiCommitAllWorkflowResult.Started, result.join())
        assertEquals(listOf("readiness", "prepare", "ai:Push", "push"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(1, dependencies.pushCallCount)
        assertSame(dependencies.selection, dependencies.pushedSelection)
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

            val result = AiCommitAllWorkflowRunner(dependencies)
                .start(AiCommitAllWorkflowMode.Commit, testDataContext())
                .join()

            assertEquals(AiCommitAllWorkflowResult.Stopped(expectedReason), result)
            assertEquals(
                listOf("readiness", "prepare", "ai:Commit", "stop:${expectedReason.name}"),
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

        val result = AiCommitAllWorkflowRunner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.AiCompletionFailed),
            result,
        )
        assertEquals(listOf("readiness", "prepare", "ai:Push", "stop:AiCompletionFailed"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `workflow stops before execution when AI generation cannot start`() {
        val dependencies = CapturingWorkflowDependencies(
            aiGenerationResult = AiCommitAllAiGenerationResult.Stopped(AiCommitAllWorkflowStopReason.MissingAiAction),
        )

        val result = AiCommitAllWorkflowRunner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.MissingAiAction),
            result,
        )
        assertEquals(listOf("readiness", "prepare", "ai:Push", "stop:MissingAiAction"), dependencies.events)
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(0, dependencies.pushCallCount)
    }

    @Test
    fun `selection failure stops before AI generation or commit execution`() {
        val dependencies = CapturingWorkflowDependencies(
            selectionResult = CommitWorkflowSelectionResult.EmptySelection,
        )

        val result = AiCommitAllWorkflowRunner(dependencies)
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

        val result = AiCommitAllWorkflowRunner(dependencies)
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

        val result = AiCommitAllWorkflowRunner(dependencies)
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

        val result = AiCommitAllWorkflowRunner(dependencies)
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

        val result = AiCommitAllWorkflowRunner(dependencies)
            .start(AiCommitAllWorkflowMode.Commit, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.CommitExecutionUnavailable),
            result,
        )
        assertEquals(
            listOf("readiness", "prepare", "ai:Commit", "commit", "stop:CommitExecutionUnavailable"),
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

        val result = AiCommitAllWorkflowRunner(dependencies)
            .start(AiCommitAllWorkflowMode.Push, testDataContext())
            .join()

        assertEquals(
            AiCommitAllWorkflowResult.Stopped(AiCommitAllWorkflowStopReason.PushExecutionUnavailable),
            result,
        )
        assertEquals(
            listOf("readiness", "prepare", "ai:Push", "push", "stop:PushExecutionUnavailable"),
            dependencies.events,
        )
        assertEquals(0, dependencies.commitCallCount)
        assertEquals(1, dependencies.pushCallCount)
    }

    private class CapturingWorkflowDependencies(
        val selection: GitChangeSelection = GitChangeSelection(emptyList()),
        private val aiCompletion: CompletableFuture<AiGenerationCompletionResult> =
            CompletableFuture.completedFuture(completedAiGeneration()),
        private val selectionResult: CommitWorkflowSelectionResult =
            CommitWorkflowSelectionResult.Prepared(selection),
        private val aiGenerationResult: AiCommitAllAiGenerationResult? = null,
        private val readinessResult: VcsOperationReadinessResult = VcsOperationReadinessResult.Ready,
        private val commitResult: CommitWorkflowExecutionResult = CommitWorkflowExecutionResult.Started,
        private val pushResult: CommitWorkflowExecutionResult = CommitWorkflowExecutionResult.Started,
    ) : AiCommitAllWorkflowDependencies {
        val events = mutableListOf<String>()
        var commitCallCount = 0
        var pushCallCount = 0
        var pushedSelection: GitChangeSelection? = null

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
        ): CommitWorkflowExecutionResult {
            events += "push"
            pushCallCount++
            pushedSelection = selection
            return pushResult
        }

        override fun reportStop(reason: AiCommitAllWorkflowStopReason) {
            events += "stop:${reason.name}"
        }
    }

    private companion object {
        private fun completedAiGeneration(): AiGenerationCompletionResult =
            AiGenerationCompletionResult.Completed(
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

        private inline fun <reified T : Any> testProxy(): T =
            Proxy.newProxyInstance(
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

        private fun java.lang.reflect.Method.defaultReturnValue(): Any? =
            when (returnType) {
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
