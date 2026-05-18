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
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import java.awt.Component
import java.awt.event.InputEvent
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class AiCommitAllShortcutActionsTest {
    @Test
    fun `commit shortcut starts commit workflow when takeover is enabled`() {
        val starter = CapturingWorkflowStarter()
        val action = testShortcutAction(
            section = AiCommitAllControlSection.Commit,
            sourceActionId = IDE_COMMIT_ACTION_ID,
            starter = starter,
        )
        val project = testProject()
        val dataContext = testDataContext(project)

        action.actionPerformed(testEvent(dataContext))

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
    }

    @Test
    fun `push shortcut starts push workflow when takeover is enabled`() {
        val starter = CapturingWorkflowStarter()
        val action = testShortcutAction(
            section = AiCommitAllControlSection.Push,
            sourceActionId = IDE_COMMIT_AND_PUSH_ACTION_ID,
            starter = starter,
        )
        val project = testProject()

        action.actionPerformed(testEvent(testDataContext(project)))

        assertSame(project, starter.project)
        assertEquals(AiCommitAllWorkflowMode.Push, starter.mode)
    }

    @Test
    fun `shortcut update is disabled when setting is off`() {
        val action = testShortcutAction(
            section = AiCommitAllControlSection.Commit,
            sourceActionId = IDE_COMMIT_ACTION_ID,
            settingsEnabled = false,
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertTrue(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun `shortcut delegates to source action when setting is off`() {
        val starter = CapturingWorkflowStarter()
        val delegate = CapturingStandardActionDelegate()
        val action = testShortcutAction(
            section = AiCommitAllControlSection.Commit,
            sourceActionId = IDE_COMMIT_ACTION_ID,
            starter = starter,
            settingsEnabled = false,
            delegate = delegate,
        )
        val event = testEvent(testDataContext(testProject()))

        action.actionPerformed(event)

        assertEquals(null, starter.mode)
        assertEquals(IDE_COMMIT_ACTION_ID, delegate.sourceActionId)
        assertSame(event, delegate.event)
    }

    @Test
    fun `promoter promotes available plugin shortcut and suppresses matching source action`() {
        val sourceAction = testSourceAction()
        val shortcutAction = testShortcutAction(
            section = AiCommitAllControlSection.Commit,
            sourceActionId = IDE_COMMIT_ACTION_ID,
        )
        val idProvider = MapActionIdProvider(sourceAction to IDE_COMMIT_ACTION_ID)
        val promoter = AiCommitAllShortcutActionPromoter(
            settingsProvider = StaticShortcutSettingsProvider(enabled = true),
            actionIdProvider = idProvider,
        )
        val actions = listOf<AnAction>(sourceAction, shortcutAction)
        val context = testDataContext(testProject())

        assertEquals(listOf(shortcutAction), promoter.promote(actions, context))
        assertEquals(listOf(sourceAction), promoter.suppress(actions, context))
    }

    @Test
    fun `promoter leaves source action alone when setting is off`() {
        val sourceAction = testSourceAction()
        val shortcutAction = testShortcutAction(
            section = AiCommitAllControlSection.Commit,
            sourceActionId = IDE_COMMIT_ACTION_ID,
            settingsEnabled = false,
        )
        val promoter = AiCommitAllShortcutActionPromoter(
            settingsProvider = StaticShortcutSettingsProvider(enabled = false),
            actionIdProvider = MapActionIdProvider(sourceAction to IDE_COMMIT_ACTION_ID),
        )
        val actions = listOf<AnAction>(sourceAction, shortcutAction)

        assertEquals(emptyList(), promoter.promote(actions, testDataContext(testProject())))
        assertEquals(emptyList(), promoter.suppress(actions, testDataContext(testProject())))
    }

    private fun testShortcutAction(
        section: AiCommitAllControlSection,
        sourceActionId: String,
        starter: CapturingWorkflowStarter = CapturingWorkflowStarter(),
        settingsEnabled: Boolean = true,
        availability: AiCommitAllWorkflowActionAvailability = AiCommitAllWorkflowActionAvailability.Enabled,
        delegate: StandardVcsShortcutActionDelegate = CapturingStandardActionDelegate(),
    ): AiCommitAllShortcutAction =
        object : AiCommitAllShortcutAction(
            section = section,
            sourceActionId = sourceActionId,
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(availability),
            settingsProvider = StaticShortcutSettingsProvider(settingsEnabled),
            standardActionDelegate = delegate,
        ) {}

    private class CapturingWorkflowStarter : AiCommitAllWorkflowStarter {
        var project: Project? = null
        var mode: AiCommitAllWorkflowMode? = null
        var dataContext: DataContext? = null

        override fun start(
            project: Project,
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
            inputEvent: InputEvent?,
        ): CompletableFuture<AiCommitAllWorkflowResult> {
            this.project = project
            this.mode = mode
            this.dataContext = dataContext
            return CompletableFuture.completedFuture(AiCommitAllWorkflowResult.Started)
        }
    }

    private class StaticAvailabilityProvider(
        private val availability: AiCommitAllWorkflowActionAvailability,
    ) : AiCommitAllWorkflowAvailabilityProvider {
        override fun availability(
            project: Project,
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
        ): AiCommitAllWorkflowActionAvailability = availability
    }

    private class StaticShortcutSettingsProvider(
        private val enabled: Boolean,
    ) : AiCommitAllShortcutSettingsProvider {
        override fun useVcsShortcutsForAiCommitAll(): Boolean = enabled
    }

    private class CapturingStandardActionDelegate : StandardVcsShortcutActionDelegate {
        var sourceActionId: String? = null
        var event: AnActionEvent? = null

        override fun perform(sourceActionId: String, event: AnActionEvent) {
            this.sourceActionId = sourceActionId
            this.event = event
        }
    }

    private class MapActionIdProvider(vararg entries: Pair<AnAction, String>) : AiCommitAllActionIdProvider {
        private val ids = entries.toMap()

        override fun id(action: AnAction): String? = ids[action]
    }

    private fun testSourceAction(): AnAction =
        object : DumbAwareAction() {
            override fun actionPerformed(event: AnActionEvent) = Unit
        }

    private fun testEvent(dataContext: DataContext): AnActionEvent =
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
        ): ActionPopupMenu = error("Not needed for shortcut tests.")

        override fun createActionToolbar(
            place: String,
            group: ActionGroup,
            horizontal: Boolean,
        ): ActionToolbar = error("Not needed for shortcut tests.")

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
