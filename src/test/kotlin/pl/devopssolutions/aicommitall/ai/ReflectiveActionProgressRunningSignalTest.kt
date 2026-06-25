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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReflectiveActionProgressRunningSignalTest {
    @Test
    fun `reports running when action progress indicator is running`() {
        val signal = ReflectiveActionProgressRunningSignal(
            TestAction(progressIndicator(running = true)),
        )

        assertEquals(AiGenerationRunningState.Running, signal.state())
    }

    @Test
    fun `reports not running when action progress indicator is stopped`() {
        val signal = ReflectiveActionProgressRunningSignal(
            TestAction(progressIndicator(running = false)),
        )

        assertEquals(AiGenerationRunningState.NotRunning, signal.state())
    }

    @Test
    fun `reports not running when action progress indicator has not been created`() {
        val signal = ReflectiveActionProgressRunningSignal(TestAction(null))

        assertEquals(AiGenerationRunningState.NotRunning, signal.state())
    }

    @Test
    fun `reports unavailable when progress indicator state cannot be read`() {
        val diagnostics = CapturingAiCompletionCompatibilityDiagnostics()
        val signal = ReflectiveActionProgressRunningSignal(
            action = TestAction(throwingProgressIndicator()),
            diagnostics = diagnostics,
        )

        assertEquals(AiGenerationRunningState.Unavailable, signal.state())
        assertEquals(
            listOf(
                AiCompletionCompatibilityDiagnostic(
                    sourceClassName = TestAction::class.java.name,
                    methodName = "state",
                    memberName = "ProgressIndicator.isRunning",
                    reason = "progress indicator state read failed",
                    exceptionClassName = IllegalStateException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `reports unavailable when action has no progress indicator field`() {
        val diagnostics = CapturingAiCompletionCompatibilityDiagnostics()
        val signal = ReflectiveActionProgressRunningSignal(
            action = ActionWithoutProgressIndicator(),
            diagnostics = diagnostics,
        )

        assertEquals(AiGenerationRunningState.Unavailable, signal.state())
        assertEquals(
            listOf(
                AiCompletionCompatibilityDiagnostic(
                    sourceClassName = ActionWithoutProgressIndicator::class.java.name,
                    methodName = "state",
                    memberName = "progressIndicator",
                    reason = "field missing",
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `reports unavailable when action progress indicator field has incompatible type`() {
        val diagnostics = CapturingAiCompletionCompatibilityDiagnostics()
        val signal = ReflectiveActionProgressRunningSignal(
            action = ActionWithIncompatibleProgressIndicator(),
            diagnostics = diagnostics,
        )

        assertEquals(AiGenerationRunningState.Unavailable, signal.state())
        assertEquals(
            listOf(
                AiCompletionCompatibilityDiagnostic(
                    sourceClassName = ActionWithIncompatibleProgressIndicator::class.java.name,
                    methodName = "state",
                    memberName = "progressIndicator",
                    reason = "field value has incompatible type",
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `reports unavailable when progress indicator rejects state reads as unsupported`() {
        val diagnostics = CapturingAiCompletionCompatibilityDiagnostics()
        val signal = ReflectiveActionProgressRunningSignal(
            action = TestAction(unsupportedProgressIndicator()),
            diagnostics = diagnostics,
        )

        assertEquals(AiGenerationRunningState.Unavailable, signal.state())
        assertEquals(
            listOf(
                AiCompletionCompatibilityDiagnostic(
                    sourceClassName = TestAction::class.java.name,
                    methodName = "state",
                    memberName = "ProgressIndicator.isRunning",
                    reason = "progress indicator state read failed",
                    exceptionClassName = UnsupportedOperationException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    @Test
    fun `finds progress indicator field declared on a superclass`() {
        val signal = ReflectiveActionProgressRunningSignal(
            InheritingAction(progressIndicator(running = true)),
        )

        assertEquals(AiGenerationRunningState.Running, signal.state())
    }

    @Test
    fun `default diagnostics report a missing progress indicator field without throwing`() {
        val signal = ReflectiveActionProgressRunningSignal(ActionWithoutProgressIndicator())

        assertEquals(AiGenerationRunningState.Unavailable, signal.state())
    }

    @Test
    fun `default diagnostics report a failed progress indicator read without throwing`() {
        val signal = ReflectiveActionProgressRunningSignal(TestAction(throwingProgressIndicator()))

        assertEquals(AiGenerationRunningState.Unavailable, signal.state())
    }

    private class TestAction(
        @Suppress("unused")
        private val progressIndicator: ProgressIndicator?,
    ) : AnAction() {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class ActionWithoutProgressIndicator : AnAction() {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private open class BaseActionWithProgressIndicator(
        @Suppress("unused")
        private val progressIndicator: ProgressIndicator?,
    ) : AnAction() {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class InheritingAction(
        progressIndicator: ProgressIndicator?,
    ) : BaseActionWithProgressIndicator(progressIndicator)

    private class ActionWithIncompatibleProgressIndicator(
        @Suppress("unused")
        private val progressIndicator: Any = "not a progress indicator",
    ) : AnAction() {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class CapturingAiCompletionCompatibilityDiagnostics : AiCompletionCompatibilityDiagnostics {
        val events = mutableListOf<AiCompletionCompatibilityDiagnostic>()

        override fun report(diagnostic: AiCompletionCompatibilityDiagnostic) {
            events += diagnostic
        }
    }

    private fun progressIndicator(running: Boolean): ProgressIndicator = Proxy.newProxyInstance(
        ProgressIndicator::class.java.classLoader,
        arrayOf(ProgressIndicator::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "isRunning" -> running
            "toString" -> "Test ProgressIndicator"
            "hashCode" -> running.hashCode()
            "equals" -> false
            else -> method.defaultReturnValue()
        }
    } as ProgressIndicator

    private fun throwingProgressIndicator(): ProgressIndicator = Proxy.newProxyInstance(
        ProgressIndicator::class.java.classLoader,
        arrayOf(ProgressIndicator::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "isRunning" -> error("Progress state unavailable")
            "toString" -> "Throwing ProgressIndicator"
            "hashCode" -> 1
            "equals" -> false
            else -> method.defaultReturnValue()
        }
    } as ProgressIndicator

    private fun unsupportedProgressIndicator(): ProgressIndicator = Proxy.newProxyInstance(
        ProgressIndicator::class.java.classLoader,
        arrayOf(ProgressIndicator::class.java),
    ) { _, method, _ ->
        when (method.name) {
            "isRunning" -> throw UnsupportedOperationException("Progress state unsupported")
            "toString" -> "Unsupported ProgressIndicator"
            "hashCode" -> 2
            "equals" -> false
            else -> method.defaultReturnValue()
        }
    } as ProgressIndicator

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
