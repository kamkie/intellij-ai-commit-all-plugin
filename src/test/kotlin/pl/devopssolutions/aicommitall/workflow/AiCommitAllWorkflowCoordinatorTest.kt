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
import com.intellij.openapi.project.Project
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import javax.swing.JTextArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class AiCommitAllWorkflowCoordinatorTest {
    @Test
    fun `start forwards mode context and input event to workflow starter`() {
        val expectedResult = CompletableFuture.completedFuture<AiCommitAllWorkflowResult>(
            AiCommitAllWorkflowResult.Started,
        )
        val starter = CapturingWorkflowStarter(expectedResult)
        val dataContext = DataContext.EMPTY_CONTEXT
        val inputEvent = testInputEvent()

        val result = AiCommitAllWorkflowCoordinator(
            project = testProject(),
            starter = starter,
        ).start(
            mode = AiCommitAllWorkflowMode.Push,
            dataContext = dataContext,
            inputEvent = inputEvent,
        )

        assertSame(expectedResult, result)
        assertEquals(AiCommitAllWorkflowMode.Push, starter.mode)
        assertSame(dataContext, starter.dataContext)
        assertSame(inputEvent, starter.inputEvent)
    }

    @Test
    fun `start forwards default null input event`() {
        val starter = CapturingWorkflowStarter(
            CompletableFuture.completedFuture(AiCommitAllWorkflowResult.Started),
        )
        val dataContext = DataContext.EMPTY_CONTEXT

        AiCommitAllWorkflowCoordinator(
            project = testProject(),
            starter = starter,
        ).start(
            mode = AiCommitAllWorkflowMode.Commit,
            dataContext = dataContext,
        )

        assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
        assertSame(dataContext, starter.dataContext)
        assertNull(starter.inputEvent)
    }

    private class CapturingWorkflowStarter(
        private val result: CompletableFuture<AiCommitAllWorkflowResult>,
    ) : AiCommitAllWorkflowStarter {
        var mode: AiCommitAllWorkflowMode? = null
        var dataContext: DataContext? = null
        var inputEvent: InputEvent? = null

        override fun start(
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
            inputEvent: InputEvent?,
        ): CompletableFuture<AiCommitAllWorkflowResult> {
            this.mode = mode
            this.dataContext = dataContext
            this.inputEvent = inputEvent
            return result
        }
    }

    private companion object {
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

        private fun testInputEvent(): InputEvent = KeyEvent(
            JTextArea(),
            KeyEvent.KEY_TYPED,
            0L,
            0,
            KeyEvent.VK_UNDEFINED,
            'a',
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
