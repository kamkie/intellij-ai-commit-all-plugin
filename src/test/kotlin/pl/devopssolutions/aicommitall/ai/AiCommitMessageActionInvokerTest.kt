package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.awt.event.InputEvent
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class AiCommitMessageActionInvokerTest {
    @Test
    fun `invokes discovered action through the action system with commit workflow context`() {
        val actionReference = AiCommitMessageActionReference(
            action = TestAction(),
            actionId = "Vcs.LLMCommitMessageAction",
            source = AiCommitMessageActionSource.KnownActionId,
        )
        val actionSystemInvoker = CapturingActionSystemInvoker()
        val project = testProxy<Project>()
        val workflowHandler = testProxy<CommitWorkflowHandler>()
        val workflowUi = testWorkflowUi()

        val result = AiCommitMessageActionInvoker(
            actionFinder = StaticActionFinder(actionReference),
            actionSystemInvoker = actionSystemInvoker,
        ).invokeCommitMessageGeneration(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
        )

        val invoked = assertIs<AiCommitMessageActionInvocationResult.Invoked>(result)
        assertEquals("Vcs.LLMCommitMessageAction", invoked.actionId)
        assertEquals(AiCommitMessageActionSource.KnownActionId, invoked.source)
        assertSame(actionReference, actionSystemInvoker.actionReference)
        assertSame(project, actionSystemInvoker.dataContext?.getData(CommonDataKeys.PROJECT))
        assertSame(workflowHandler, actionSystemInvoker.dataContext?.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER))
        assertSame(workflowUi, actionSystemInvoker.dataContext?.getData(VcsDataKeys.COMMIT_WORKFLOW_UI))
    }

    @Test
    fun `returns missing workflow before invoking action discovery`() {
        val actionFinder = StaticActionFinder(
            AiCommitMessageActionReference(
                action = TestAction(),
                actionId = "Vcs.LLMCommitMessageAction",
                source = AiCommitMessageActionSource.KnownActionId,
            ),
        )
        val actionSystemInvoker = CapturingActionSystemInvoker()

        val result = AiCommitMessageActionInvoker(
            actionFinder = actionFinder,
            actionSystemInvoker = actionSystemInvoker,
        ).invokeCommitMessageGeneration(
            project = testProxy(),
            workflowHandler = null,
            workflowUi = testWorkflowUi(),
        )

        assertEquals(AiCommitMessageActionInvocationResult.MissingWorkflow, result)
        assertEquals(0, actionFinder.callCount)
        assertNull(actionSystemInvoker.actionReference)
    }

    @Test
    fun `returns missing action without invoking the action system`() {
        val actionSystemInvoker = CapturingActionSystemInvoker()

        val result = AiCommitMessageActionInvoker(
            actionFinder = StaticActionFinder(null),
            actionSystemInvoker = actionSystemInvoker,
        ).invokeCommitMessageGeneration(
            project = testProxy(),
            workflowHandler = testProxy(),
            workflowUi = testWorkflowUi(),
        )

        assertEquals(AiCommitMessageActionInvocationResult.MissingAction, result)
        assertNull(actionSystemInvoker.actionReference)
    }

    private class TestAction : AnAction("Generate Commit Message") {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class StaticActionFinder(
        private val actionReference: AiCommitMessageActionReference?,
    ) : AiCommitMessageActionFinder {
        var callCount = 0

        override fun findCommitMessageAction(event: AnActionEvent?): AiCommitMessageActionReference? {
            callCount++
            return actionReference
        }
    }

    private class CapturingActionSystemInvoker : AiActionSystemInvoker {
        var actionReference: AiCommitMessageActionReference? = null
        var dataContext: DataContext? = null
        var inputEvent: InputEvent? = null

        override fun invoke(
            actionReference: AiCommitMessageActionReference,
            dataContext: DataContext,
            inputEvent: InputEvent?,
        ) {
            this.actionReference = actionReference
            this.dataContext = dataContext
            this.inputEvent = inputEvent
        }
    }

    private class TestCommitMessageUi : CommitMessageUi {
        private var text = ""

        override fun getText(): String = text

        override fun setText(text: String?) {
            this.text = text.orEmpty()
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit
    }

    private fun testWorkflowUi(): CommitWorkflowUi =
        testProxy(
            answers = mapOf(
                "getCommitMessageUi" to TestCommitMessageUi(),
            ),
        )

    private inline fun <reified T : Any> testProxy(answers: Map<String, Any?> = emptyMap()): T =
        Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "Test ${T::class.java.simpleName}"
                "hashCode" -> System.identityHashCode(this)
                "equals" -> false
                else -> answers[method.name] ?: method.defaultReturnValue()
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
