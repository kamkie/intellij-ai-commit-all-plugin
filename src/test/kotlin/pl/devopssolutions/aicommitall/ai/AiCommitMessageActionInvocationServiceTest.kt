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
package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.lang.reflect.Proxy
import javax.swing.JButton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class AiCommitMessageActionInvocationServiceTest {
    @Test
    fun `service forwards workflow context to action generation`() {
        val actionGeneration = CapturingActionGeneration()
        val project = testProxy<Project>()
        val workflowHandler = testProxy<CommitWorkflowHandler>()
        val workflowUi = testProxy<CommitWorkflowUi>()
        val parentDataContext = DataContext { dataId -> "parent:$dataId" }
        val inputEvent = testInputEvent()
        val service = AiCommitMessageActionInvocationService(
            project = project,
            actionGeneration = actionGeneration,
        )

        val result = service.invokeCommitMessageGeneration(
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
            inputEvent = inputEvent,
        )

        assertEquals(AiCommitMessageActionInvocationResult.MissingAction, result)
        assertSame(project, actionGeneration.project)
        assertSame(workflowHandler, actionGeneration.workflowHandler)
        assertSame(workflowUi, actionGeneration.workflowUi)
        assertSame(parentDataContext, actionGeneration.parentDataContext)
        assertSame(inputEvent, actionGeneration.inputEvent)
    }

    private class CapturingActionGeneration : AiCommitMessageActionGeneration {
        var project: Project? = null
        var workflowHandler: CommitWorkflowHandler? = null
        var workflowUi: CommitWorkflowUi? = null
        var parentDataContext: DataContext? = null
        var inputEvent: InputEvent? = null

        override fun invokeCommitMessageGeneration(
            project: Project,
            workflowHandler: CommitWorkflowHandler?,
            workflowUi: CommitWorkflowUi?,
            parentDataContext: DataContext,
            inputEvent: InputEvent?,
        ): AiCommitMessageActionInvocationResult {
            this.project = project
            this.workflowHandler = workflowHandler
            this.workflowUi = workflowUi
            this.parentDataContext = parentDataContext
            this.inputEvent = inputEvent
            return AiCommitMessageActionInvocationResult.MissingAction
        }
    }

    private companion object {
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

        private fun testInputEvent(): InputEvent = MouseEvent(
            JButton("AI"),
            MouseEvent.MOUSE_CLICKED,
            0L,
            0,
            1,
            1,
            1,
            false,
        )

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
