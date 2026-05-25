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

import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionPopupMenu
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.actionSystem.DataMap
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.DataSink
import com.intellij.openapi.actionSystem.DataSnapshotProvider
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.TimerListener
import com.intellij.openapi.actionSystem.UiDataProvider
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import org.jetbrains.concurrency.Promise
import java.awt.event.MouseEvent
import java.lang.reflect.Proxy
import javax.swing.JButton
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class AiCommitMessageActionInvocationContextFactoryTest {
    @Test
    fun `creates AI action event with toolbar place input event and isolated presentation`() {
        withDataManagerApplication {
            val action = TestAction("Generate Commit Message")
            val inputEvent = testInputEvent()

            val context = AiCommitMessageActionInvocationContextFactory.createInvocationContext(
                actionReference = AiCommitMessageActionReference(
                    action = action,
                    actionId = "Vcs.LLMCommitMessageAction",
                    source = AiCommitMessageActionSource.KnownActionId,
                ),
                project = testProxy(),
                workflowHandler = testProxy(),
                workflowUi = testWorkflowUi(TestCommitMessageTextAccessor()),
                inputEvent = inputEvent,
            )

            assertSame(context.dataContext, context.event.dataContext)
            assertEquals(ActionPlaces.CHANGES_VIEW_TOOLBAR, context.event.place)
            assertSame(inputEvent, context.event.inputEvent)
            assertFalse(action.templatePresentation === context.event.presentation)

            context.event.presentation.text = "Mutated Presentation"

            assertEquals("Generate Commit Message", action.templatePresentation.text)
        }
    }

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
    fun `workflow data overrides stale parent data while preserving unrelated parent keys`() {
        withDataManagerApplication {
            val currentProject = testProxy<Project>()
            val currentWorkflowHandler = testProxy<CommitWorkflowHandler>()
            val currentWorkflowUi = testWorkflowUi(TestCommitMessageTextAccessor())
            val staleProject = testProxy<Project>()
            val staleWorkflowHandler = testProxy<CommitWorkflowHandler>()
            val staleWorkflowUi = testWorkflowUi(TestCommitMessageTextAccessor())
            val customKey = DataKey.create<String>("aicommitall.test.parent")
            val parentDataContext = testDataContext(
                CommonDataKeys.PROJECT to staleProject,
                VcsDataKeys.COMMIT_WORKFLOW_HANDLER to staleWorkflowHandler,
                VcsDataKeys.COMMIT_WORKFLOW_UI to staleWorkflowUi,
                customKey to "parent value",
            )

            val dataContext = AiCommitMessageActionInvocationContextFactory.createDataContext(
                project = currentProject,
                workflowHandler = currentWorkflowHandler,
                workflowUi = currentWorkflowUi,
                parentDataContext = parentDataContext,
            )

            assertSame(currentProject, dataContext.getData(CommonDataKeys.PROJECT))
            assertSame(currentWorkflowHandler, dataContext.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER))
            assertSame(currentWorkflowUi, dataContext.getData(VcsDataKeys.COMMIT_WORKFLOW_UI))
            assertEquals("parent value", dataContext.getData(customKey))
        }
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
    fun `omits optional commit message keys when workflow UI and parent context cannot provide them`() {
        withDataManagerApplication {
            val dataContext = AiCommitMessageActionInvocationContextFactory.createDataContext(
                project = testProxy(),
                workflowHandler = testProxy(),
                workflowUi = testWorkflowUi(TestCommitMessageTextAccessor()),
            )

            assertNull(dataContext.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL))
            assertNull(dataContext.getData(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT))
        }
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

    private class TestAction(text: String) : AnAction(text) {
        override fun actionPerformed(event: AnActionEvent) = Unit
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

    private fun testInputEvent(): MouseEvent = MouseEvent(
        JButton("AI"),
        MouseEvent.MOUSE_CLICKED,
        0L,
        0,
        1,
        1,
        1,
        false,
    )

    private fun withDataManagerApplication(block: () -> Unit) {
        if (ApplicationManager.getApplication() != null) {
            block()
            return
        }

        val disposable = Disposer.newDisposable("Test application")
        ApplicationManager.setApplication(
            testApplication(
                dataManager = TestDataManager(),
                actionManager = TestActionManager(),
            ),
            disposable,
        )
        try {
            block()
        } finally {
            Disposer.dispose(disposable)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private class TestDataManager : DataManager() {
        override fun getDataContext(): DataContext = DataContext.EMPTY_CONTEXT

        override fun getDataContextFromFocusAsync(): Promise<DataContext> {
            error("Focus data context is not needed for invocation context tests.")
        }

        override fun getDataContext(component: java.awt.Component): DataContext = DataContext.EMPTY_CONTEXT

        override fun getDataContext(
            component: java.awt.Component,
            x: Int,
            y: Int,
        ): DataContext = DataContext.EMPTY_CONTEXT

        override fun getCustomizedData(
            dataId: String,
            dataContext: DataContext,
            dataProvider: DataProvider,
        ): Any? = dataProvider.getData(dataId) ?: runCatching { dataContext.getData(dataId) }.getOrNull()

        override fun customizeDataContext(
            dataContext: DataContext,
            dataSource: Any,
        ): DataContext {
            if (dataSource is DataSnapshotProvider) {
                val data = mutableMapOf<String, Any?>()
                dataSource.dataSnapshot(MapDataSink(data))
                return DataContext { dataId ->
                    if (data.containsKey(dataId)) {
                        data[dataId]
                    } else {
                        runCatching { dataContext.getData(dataId) }.getOrNull()
                    }
                }
            }

            val dataProvider = dataSource as DataProvider
            return DataContext { dataId -> getCustomizedData(dataId, dataContext, dataProvider) }
        }

        override fun <T : Any> saveInDataContext(
            dataContext: DataContext?,
            key: Key<T>,
            data: T?,
        ) = Unit

        override fun <T : Any> loadFromDataContext(
            dataContext: DataContext,
            key: Key<T>,
        ): T? = null
    }

    private class MapDataSink(
        private val values: MutableMap<String, Any?>,
    ) : DataSink {
        override fun <T : Any> set(
            key: DataKey<T>,
            data: T?,
        ) {
            values[key.name] = data
        }

        override fun <T : Any> setNull(key: DataKey<T>) {
            values[key.name] = null
        }

        override fun <T : Any> lazy(
            key: DataKey<T>,
            data: () -> T?,
        ) {
            values[key.name] = data()
        }

        override fun <T : Any> lazyValue(
            key: DataKey<T>,
            data: (DataMap) -> T?,
        ) = Unit

        override fun <T : Any> lazyNull(key: DataKey<T>) {
            values[key.name] = null
        }

        override fun uiDataSnapshot(provider: UiDataProvider) = Unit

        override fun dataSnapshot(provider: DataSnapshotProvider) {
            provider.dataSnapshot(this)
        }

        override fun uiDataSnapshot(provider: DataProvider) = Unit
    }

    @Suppress("OVERRIDE_DEPRECATION")
    private class TestActionManager : ActionManager() {
        override fun createActionPopupMenu(
            place: String,
            group: ActionGroup,
        ): ActionPopupMenu = error("Action popup menus are not needed for invocation context tests.")

        override fun createActionToolbar(
            place: String,
            group: ActionGroup,
            horizontal: Boolean,
        ): ActionToolbar = error("Action toolbars are not needed for invocation context tests.")

        override fun getAction(actionId: String): AnAction? = null

        override fun getId(action: AnAction): String? = null

        override fun registerAction(
            actionId: String,
            action: AnAction,
        ) = Unit

        override fun registerAction(
            actionId: String,
            action: AnAction,
            pluginId: PluginId?,
        ) = Unit

        override fun unregisterAction(actionId: String) = Unit

        override fun replaceAction(
            actionId: String,
            newAction: AnAction,
        ) = Unit

        override fun getActionIds(idPrefix: String): Array<String> = emptyArray()

        override fun getActionIdList(idPrefix: String): List<String> = emptyList()

        override fun isGroup(actionId: String): Boolean = false

        override fun getActionOrStub(actionId: String): AnAction? = null

        override fun addTimerListener(listener: TimerListener) = Unit

        override fun removeTimerListener(listener: TimerListener) = Unit

        override fun tryToExecute(
            action: AnAction,
            inputEvent: java.awt.event.InputEvent?,
            contextComponent: java.awt.Component?,
            place: String?,
            now: Boolean,
        ): ActionCallback = ActionCallback.DONE

        override fun getKeyboardShortcut(actionId: String): KeyboardShortcut? = null
    }

    private fun testApplication(
        dataManager: DataManager,
        actionManager: ActionManager,
    ): Application = Proxy.newProxyInstance(
        Application::class.java.classLoader,
        arrayOf(Application::class.java),
    ) { _, method, arguments ->
        when (method.name) {
            "getService" -> when (arguments?.firstOrNull()) {
                DataManager::class.java -> dataManager
                ActionManager::class.java -> actionManager
                else -> null
            }

            "getServiceIfCreated" -> when (arguments?.firstOrNull()) {
                DataManager::class.java -> dataManager
                ActionManager::class.java -> actionManager
                else -> null
            }

            "isUnitTestMode",
            "isHeadlessEnvironment",
            -> true

            "toString" -> "Test Application"

            "hashCode" -> System.identityHashCode(dataManager)

            "equals" -> false

            else -> method.defaultReturnValue()
        }
    } as Application

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
