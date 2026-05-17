package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionPopupMenu
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUiKind
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
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import java.awt.Component
import java.awt.event.InputEvent
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import javax.swing.JButton
import javax.swing.JComponent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AiCommitAllActionsTest {
    @Test
    fun `ai section starts ai workflow mode`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(starter)
        val project = testProject()
        val dataContext = testDataContext(project)

        action.startSection(project, AiCommitAllControlSection.Ai, dataContext, null)

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Ai, starter.mode)
    }

    @Test
    fun `commit section starts commit workflow mode`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(starter)
        val project = testProject()
        val dataContext = testDataContext(project)

        action.startSection(project, AiCommitAllControlSection.Commit, dataContext, null)

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
    }

    @Test
    fun `push section starts push workflow mode`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(starter)
        val project = testProject()
        val dataContext = testDataContext(project)

        action.startSection(project, AiCommitAllControlSection.Push, dataContext, null)

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Push, starter.mode)
    }

    @Test
    fun `fallback action invocation starts commit workflow mode`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(starter)
        val project = testProject()
        val dataContext = testDataContext(project)

        action.actionPerformed(testEvent(dataContext))

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
    }

    @Test
    fun `action does not start without project`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(starter)

        action.actionPerformed(testEvent(DataContext.EMPTY_CONTEXT))

        assertNull(starter.mode)
    }

    @Test
    fun `action update applies enabled availability`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertTrue(event.presentation.isVisible)
        assertTrue(event.presentation.isEnabled)
    }

    @Test
    fun `custom component exposes three ordered sections`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val buttons = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).sectionButtons()

        assertEquals(listOf("AI", "Commit", "Push"), buttons.map { button -> button.text })
        assertTrue(buttons.all { button -> button.isEnabled })
    }

    @Test
    fun `custom component disables unavailable section only`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(
                AiCommitAllWorkflowMode.Push to AiCommitAllWorkflowActionAvailability.Disabled,
            ),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val buttons = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).sectionButtons()

        assertTrue(buttons[0].isEnabled)
        assertTrue(buttons[1].isEnabled)
        assertFalse(buttons[2].isEnabled)
    }

    @Test
    fun `action update applies disabled availability`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(
                defaultAvailability = AiCommitAllWorkflowActionAvailability.Disabled,
            ),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertTrue(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun `action update disables all sections while running`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(runningSection = AiCommitAllControlSection.Commit),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val buttons = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).sectionButtons()

        assertTrue(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
        assertTrue(buttons.all { button -> !button.isEnabled })
    }

    @Test
    fun `action update hides without project`() {
        val provider = CapturingAvailabilityProvider()
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = provider,
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(DataContext.EMPTY_CONTEXT)

        action.update(event)

        assertFalse(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
        assertTrue(provider.modes.isEmpty())
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
        private val defaultAvailability: AiCommitAllWorkflowActionAvailability =
            AiCommitAllWorkflowActionAvailability.Enabled,
        private val overrides: Map<AiCommitAllWorkflowMode, AiCommitAllWorkflowActionAvailability> = emptyMap(),
    ) : AiCommitAllWorkflowAvailabilityProvider {
        val modes = mutableListOf<AiCommitAllWorkflowMode>()

        override fun availability(
            project: Project,
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
        ): AiCommitAllWorkflowActionAvailability {
            modes += mode
            return overrides[mode] ?: defaultAvailability
        }
    }

    private class StaticAvailabilityProvider(
        vararg overrides: Pair<AiCommitAllWorkflowMode, AiCommitAllWorkflowActionAvailability>,
        defaultAvailability: AiCommitAllWorkflowActionAvailability = AiCommitAllWorkflowActionAvailability.Enabled,
    ) : CapturingAvailabilityProvider(defaultAvailability, overrides.toMap())

    private class StaticActivityProvider(
        private val runningSection: AiCommitAllControlSection? = null,
    ) : AiCommitAllWorkflowActivityProvider {
        override fun runningSection(project: Project): AiCommitAllControlSection? = runningSection
    }

    private fun JComponent.sectionButtons(): List<JButton> =
        components.filterIsInstance<JButton>()

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
