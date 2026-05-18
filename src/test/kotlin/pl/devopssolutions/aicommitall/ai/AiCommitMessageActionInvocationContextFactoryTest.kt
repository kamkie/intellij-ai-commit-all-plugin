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
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertSame

internal class AiCommitMessageActionInvocationContextFactoryTest {
    @Test
    fun `adds commit workflow data to the AI action data context`() {
        val project = testProxy<Project>()
        val workflowHandler = testProxy<CommitWorkflowHandler>()
        val commitMessageUi = TestCommitMessageControl()
        val workflowUi = testWorkflowUi(commitMessageUi)
        val commitMessageDocument = DocumentImpl("initial commit message")
        val parentDataContext = testDataContext(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT to commitMessageDocument)

        val data = AiCommitMessageActionInvocationContextFactory.collectData(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
        )

        assertSame(project, data.project)
        assertSame(workflowHandler, data.workflowHandler)
        assertSame(workflowUi, data.workflowUi)
        assertSame(commitMessageUi, data.commitMessageControl)
        assertSame(commitMessageDocument, data.commitMessageDocument)
    }

    @Test
    fun `preserves parent commit message control when workflow UI has only the public text accessor`() {
        val parentCommitMessageControl = TestCommitMessageControl()
        val parentDataContext = testDataContext(VcsDataKeys.COMMIT_MESSAGE_CONTROL to parentCommitMessageControl)

        val data = AiCommitMessageActionInvocationContextFactory.collectData(
            project = testProxy(),
            workflowHandler = testProxy(),
            workflowUi = testWorkflowUi(TestCommitMessageTextAccessor()),
            parentDataContext = parentDataContext,
        )

        assertSame(parentCommitMessageControl, data.commitMessageControl)
    }

    @Test
    fun `uses commit message UI editor document when available`() {
        val commitMessageDocument = DocumentImpl("generated message")

        val data = AiCommitMessageActionInvocationContextFactory.collectData(
            project = testProxy(),
            workflowHandler = testProxy(),
            workflowUi = testWorkflowUi(TestCommitMessageControl(commitMessageDocument)),
        )

        assertSame(commitMessageDocument, data.commitMessageDocument)
    }

    private class TestCommitMessageControl :
        CommitMessageUi,
        CommitMessageI {
        constructor()

        constructor(document: DocumentImpl) {
            testEditorField = TestEditorField(document)
        }

        private var text = ""
        private var testEditorField: TestEditorField? = null

        override fun getText(): String = text

        override fun setText(text: String?) {
            this.text = text.orEmpty()
        }

        override fun setCommitMessage(commitMessage: String) {
            text = commitMessage
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit

        fun getEditorField(): TestEditorField? = testEditorField
    }

    private class TestEditorField(private val document: DocumentImpl) {
        fun getDocument(): DocumentImpl = document
    }

    private class TestCommitMessageTextAccessor : CommitMessageUi {
        private var text = ""

        override fun getText(): String = text

        override fun setText(text: String?) {
            this.text = text.orEmpty()
        }

        override fun focus() = Unit

        override fun startLoading() = Unit

        override fun stopLoading() = Unit
    }

    private fun testWorkflowUi(commitMessageUi: CommitMessageUi): CommitWorkflowUi = testProxy(
        answers = mapOf(
            "getCommitMessageUi" to commitMessageUi,
        ),
    )

    private inline fun <reified T : Any> testProxy(answers: Map<String, Any?> = emptyMap()): T = Proxy.newProxyInstance(
        T::class.java.classLoader,
        arrayOf(T::class.java),
    ) { _, method, arguments ->
        when (method.name) {
            "toString" -> "Test ${T::class.java.simpleName}"
            "hashCode" -> System.identityHashCode(this)
            "equals" -> false
            else -> answers[method.name] ?: method.defaultReturnValue()
        }
    } as T

    private fun testDataContext(vararg values: Pair<DataKey<*>, Any>): DataContext {
        val data = values.associate { (key, value) -> key.name to value }
        return DataContext { dataId -> data[dataId] }
    }

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
