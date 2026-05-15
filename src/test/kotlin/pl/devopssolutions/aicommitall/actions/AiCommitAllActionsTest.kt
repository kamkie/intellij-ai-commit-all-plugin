package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionPopupMenu
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.TimerListener
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.ui.AnimatedIcon
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import java.awt.Component
import java.awt.event.InputEvent
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import javax.swing.Icon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AiCommitAllActionsTest {
    @Test
    fun `commit action starts commit workflow mode`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllCommitAction(starter)
        val project = testProject()
        val dataContext = testDataContext(project)

        action.actionPerformed(testEvent(dataContext))

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
    }

    @Test
    fun `commit and push action starts commit and push workflow mode`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllCommitAndPushAction(starter)
        val project = testProject()
        val dataContext = testDataContext(project)

        action.actionPerformed(testEvent(dataContext))

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.CommitAndPush, starter.mode)
    }

    @Test
    fun `action does not start without project`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllCommitAction(starter)

        action.actionPerformed(testEvent(DataContext.EMPTY_CONTEXT))

        assertNull(starter.mode)
    }

    @Test
    fun `action update applies enabled availability`() {
        val action = AiCommitAllCommitAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(AiCommitAllWorkflowActionAvailability.Enabled),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertTrue(event.presentation.isVisible)
        assertTrue(event.presentation.isEnabled)
    }

    @Test
    fun `action update applies disabled availability`() {
        val action = AiCommitAllCommitAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(AiCommitAllWorkflowActionAvailability.Disabled),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertTrue(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun `action update applies running activity presentation`() {
        val action = AiCommitAllCommitAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(AiCommitAllWorkflowActionAvailability.Enabled),
            activityProvider = StaticActivityProvider(running = true),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertTrue(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
        assertSame(AnimatedIcon.Default.INSTANCE, event.presentation.icon)
    }

    @Test
    fun `action update hides without project`() {
        val provider = CapturingAvailabilityProvider(AiCommitAllWorkflowActionAvailability.Enabled)
        val action = AiCommitAllCommitAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = provider,
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(DataContext.EMPTY_CONTEXT)

        action.update(event)

        assertFalse(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
        assertNull(provider.mode)
    }

    private class CapturingWorkflowStarter : AiCommitAllWorkflowStarter {
        var project: Project? = null
        var mode: AiCommitAllWorkflowMode? = null
        var dataContext: DataContext? = null
        var inputEvent: InputEvent? = null

        override fun start(
            project: Project,
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
            inputEvent: InputEvent?,
        ): CompletableFuture<AiCommitAllWorkflowResult> {
            this.project = project
            this.mode = mode
            this.dataContext = dataContext
            this.inputEvent = inputEvent
            return CompletableFuture.completedFuture(AiCommitAllWorkflowResult.Started)
        }
    }

    private open class CapturingAvailabilityProvider(
        private val availability: AiCommitAllWorkflowActionAvailability,
    ) : AiCommitAllWorkflowAvailabilityProvider {
        var project: Project? = null
        var mode: AiCommitAllWorkflowMode? = null
        var dataContext: DataContext? = null

        override fun availability(
            project: Project,
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
        ): AiCommitAllWorkflowActionAvailability {
            this.project = project
            this.mode = mode
            this.dataContext = dataContext
            return availability
        }
    }

    private class StaticAvailabilityProvider(
        availability: AiCommitAllWorkflowActionAvailability,
    ) : CapturingAvailabilityProvider(availability)

    private class StaticActivityProvider(
        private val running: Boolean = false,
    ) : AiCommitAllWorkflowActivityProvider {
        override fun applyActivityState(
            project: Project,
            presentation: Presentation,
            idleIcon: Icon?,
            enabledWhenIdle: Boolean,
        ) {
            if (running) {
                presentation.setEnabled(false)
                presentation.setIcon(AnimatedIcon.Default.INSTANCE)
            } else {
                presentation.setEnabled(enabledWhenIdle)
                presentation.setIcon(idleIcon)
            }
        }
    }

    private fun testEvent(
        dataContext: DataContext,
    ): AnActionEvent =
        AnActionEvent(
            dataContext,
            Presentation(),
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            null,
            0,
            TestActionManager,
        )

    private fun testDataContext(project: Project): DataContext =
        DataContext { dataId ->
            when (dataId) {
                CommonDataKeys.PROJECT.name -> project
                else -> null
            }
        }

    private fun testProject(): Project =
        Proxy.newProxyInstance(
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

    @Suppress("OVERRIDE_DEPRECATION")
    private object TestActionManager : ActionManager() {
        override fun createActionPopupMenu(
            place: String,
            group: ActionGroup,
        ): ActionPopupMenu = error("Not needed for action routing tests.")

        override fun createActionToolbar(
            place: String,
            group: ActionGroup,
            horizontal: Boolean,
        ): ActionToolbar = error("Not needed for action routing tests.")

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

        override fun getActionIdList(idPrefix: String): MutableList<String> = mutableListOf()

        override fun isGroup(actionId: String): Boolean = false

        override fun getActionOrStub(actionId: String): AnAction? = null

        override fun addTimerListener(listener: TimerListener) = Unit

        override fun removeTimerListener(listener: TimerListener) = Unit

        override fun tryToExecute(
            action: AnAction,
            inputEvent: InputEvent?,
            contextComponent: Component?,
            place: String?,
            now: Boolean,
        ): ActionCallback = ActionCallback.DONE

        override fun getKeyboardShortcut(actionId: String): KeyboardShortcut? = null
    }
}
