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
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.TimerListener
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import org.jetbrains.concurrency.Promise
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowMode
import pl.devopssolutions.aicommitall.workflow.AiCommitAllWorkflowResult
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import javax.swing.JComponent
import javax.swing.JPanel
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
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
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
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
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
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
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
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val project = testProject()
        val dataContext = testDataContext(project)

        action.actionPerformed(testEvent(dataContext))

        assertSame(project, starter.project)
        assertSame(dataContext, starter.dataContext)
        assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
    }

    @Test
    fun `fallback action invocation passes original input event to workflow starter`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val dataContext = testDataContext(testProject())
        val inputEvent = testMouseEvent(JPanel())

        action.actionPerformed(testEvent(dataContext, inputEvent))

        assertSame(inputEvent, starter.inputEvent)
    }

    @Test
    fun `action does not start without project`() {
        val starter = CapturingWorkflowStarter()
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )

        action.actionPerformed(testEvent(DataContext.EMPTY_CONTEXT))

        assertNull(starter.mode)
    }

    @Test
    fun `section activation rechecks availability at action time after stale disabled update`() {
        val starter = CapturingWorkflowStarter()
        val availabilityProvider = MutableAvailabilityProvider(
            availability = AiCommitAllWorkflowActionAvailability.Disabled,
        )
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = availabilityProvider,
            activityProvider = StaticActivityProvider(),
        )
        val project = testProject()
        val dataContext = testDataContext(project)
        val event = testEvent(dataContext)

        action.update(event)
        availabilityProvider.availability = AiCommitAllWorkflowActionAvailability.Enabled
        action.startSection(project, AiCommitAllControlSection.Push, dataContext, null)

        assertEquals(AiCommitAllWorkflowMode.Push, starter.mode)
        assertEquals(
            listOf(
                AiCommitAllWorkflowMode.Ai,
                AiCommitAllWorkflowMode.Commit,
                AiCommitAllWorkflowMode.Push,
                AiCommitAllWorkflowMode.Push,
            ),
            availabilityProvider.modes,
        )
    }

    @Test
    fun `section activation stops when action-time availability becomes disabled after stale enabled update`() {
        val starter = CapturingWorkflowStarter()
        val availabilityProvider = MutableAvailabilityProvider(
            availability = AiCommitAllWorkflowActionAvailability.Enabled,
        )
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = starter,
            availabilityProvider = availabilityProvider,
            activityProvider = StaticActivityProvider(),
        )
        val project = testProject()
        val dataContext = testDataContext(project)
        val event = testEvent(dataContext)

        action.update(event)
        availabilityProvider.availability = AiCommitAllWorkflowActionAvailability.Disabled
        val result = action.startSection(project, AiCommitAllControlSection.Push, dataContext, null)

        assertNull(result)
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
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        assertEquals(listOf("AI", "Commit", "Push"), control.sectionLabels())
        assertTrue(AiCommitAllControlSection.entries.all { section -> control.isSectionEnabledForTest(section) })
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
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        assertTrue(control.isSectionEnabledForTest(AiCommitAllControlSection.Ai))
        assertTrue(control.isSectionEnabledForTest(AiCommitAllControlSection.Commit))
        assertFalse(control.isSectionEnabledForTest(AiCommitAllControlSection.Push))
    }

    @Test
    fun `custom component highlights sections cumulatively`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()
        control.setHoverSectionForTest(AiCommitAllControlSection.Push)

        assertEquals(
            setOf(
                AiCommitAllControlSection.Ai,
                AiCommitAllControlSection.Commit,
                AiCommitAllControlSection.Push,
            ),
            control.highlightedSectionsForTest(),
        )
    }

    @Test
    fun `custom component keeps inactive divider passive while ai is running`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(runningSection = AiCommitAllControlSection.Ai),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        val (aiCommitDivider, commitPushDivider) = control.dividerColorsForTest()
        assertTrue(aiCommitDivider != commitPushDivider)
    }

    @Test
    fun `custom component uses matching active dividers when all sections are highlighted`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()
        control.setHoverSectionForTest(AiCommitAllControlSection.Push)

        val (aiCommitDivider, commitPushDivider) = control.dividerColorsForTest()
        assertEquals(aiCommitDivider, commitPushDivider)
    }

    @Test
    fun `custom component paints segmented control`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(runningSection = AiCommitAllControlSection.Push),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()
        control.setSize(control.preferredSize)
        val image = BufferedImage(control.width, control.height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        control.paint(graphics)
        graphics.dispose()

        assertTrue((image.getRGB(control.width / 2, control.height / 2) ushr 24) > 0)
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
    fun `action update stays visible and enabled when only one section is enabled`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(
                AiCommitAllWorkflowMode.Ai to AiCommitAllWorkflowActionAvailability.Hidden,
                AiCommitAllWorkflowMode.Commit to AiCommitAllWorkflowActionAvailability.Disabled,
                AiCommitAllWorkflowMode.Push to AiCommitAllWorkflowActionAvailability.Enabled,
            ),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        assertTrue(event.presentation.isVisible)
        assertTrue(event.presentation.isEnabled)
        assertFalse(control.isSectionEnabledForTest(AiCommitAllControlSection.Ai))
        assertFalse(control.isSectionEnabledForTest(AiCommitAllControlSection.Commit))
        assertTrue(control.isSectionEnabledForTest(AiCommitAllControlSection.Push))
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
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        assertTrue(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
        assertTrue(AiCommitAllControlSection.entries.all { section -> !control.isSectionEnabledForTest(section) })
        assertEquals(
            setOf(AiCommitAllControlSection.Ai, AiCommitAllControlSection.Commit),
            control.highlightedSectionsForTest(),
        )
    }

    @Test
    fun `custom component update refreshes running section state`() {
        val activityProvider = MutableActivityProvider()
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = activityProvider,
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)
        val control = action.createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        assertTrue(AiCommitAllControlSection.entries.all { section -> control.isSectionEnabledForTest(section) })

        activityProvider.runningSection = AiCommitAllControlSection.Push
        action.update(event)
        action.updateCustomComponent(control, event.presentation)

        assertTrue(AiCommitAllControlSection.entries.all { section -> !control.isSectionEnabledForTest(section) })
        assertEquals(
            setOf(
                AiCommitAllControlSection.Ai,
                AiCommitAllControlSection.Commit,
                AiCommitAllControlSection.Push,
            ),
            control.highlightedSectionsForTest(),
        )
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

    @Test
    fun `custom component falls back to hidden state without presentation state`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )

        val control = action.createCustomComponent(Presentation(), ActionPlaces.CHANGES_VIEW_TOOLBAR).asControl()

        assertFalse(control.isVisible)
        assertFalse(control.isEnabled)
        assertTrue(AiCommitAllControlSection.entries.all { section -> !control.isSectionEnabledForTest(section) })
    }

    @Test
    fun `custom component update ignores non-control components`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(),
            activityProvider = StaticActivityProvider(),
        )
        val component = JPanel()
        val presentation = Presentation()

        action.updateCustomComponent(component, presentation)

        assertTrue(component.isEnabled)
        assertTrue(component.isVisible)
    }

    @Test
    fun `custom component activation uses clicked component data context and mouse event`() {
        val dataManager = ComponentDataManager()
        withDataManagerApplication(dataManager) {
            val starter = CapturingWorkflowStarter()
            val action = AiCommitAllThreeSectionAction(
                workflowStarter = starter,
                availabilityProvider = StaticAvailabilityProvider(),
                activityProvider = StaticActivityProvider(),
            )
            val project = testProject()
            val dataContext = testDataContext(project)
            val event = testEvent(dataContext)
            action.update(event)
            val control = action
                .createCustomComponent(event.presentation, ActionPlaces.CHANGES_VIEW_TOOLBAR)
                .asControl()
            control.setSize(control.preferredSize)
            dataManager.contexts[control] = dataContext

            val inputEvent = testMouseEvent(control)
            control.dispatchEvent(inputEvent)

            assertTrue(dataManager.requestedComponents.isNotEmpty())
            assertTrue(dataManager.requestedComponents.all { component -> component === control })
            assertSame(project, starter.project)
            assertSame(dataContext, starter.dataContext)
            assertEquals(AiCommitAllWorkflowMode.Commit, starter.mode)
            assertSame(inputEvent, starter.inputEvent)
        }
    }

    @Test
    fun `action update hides when every section is unavailable`() {
        val action = AiCommitAllThreeSectionAction(
            workflowStarter = CapturingWorkflowStarter(),
            availabilityProvider = StaticAvailabilityProvider(
                defaultAvailability = AiCommitAllWorkflowActionAvailability.Hidden,
            ),
            activityProvider = StaticActivityProvider(),
        )
        val event = testEvent(testDataContext(testProject()))

        action.update(event)

        assertFalse(event.presentation.isVisible)
        assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun `availability policy enables push only when no committable content has outgoing commits`() {
        val availability = AiCommitAllWorkflowAvailabilityPolicy.availability(
            mode = AiCommitAllWorkflowMode.Push,
            hasCommittableContent = false,
            canExecuteCommit = { false },
            canExecuteCommitAndPush = { false },
            hasOutgoingCommitsToPush = { true },
        )

        assertEquals(AiCommitAllWorkflowActionAvailability.Enabled, availability)
    }

    @Test
    fun `availability policy keeps ai and commit disabled when only outgoing commits exist`() {
        val modes = listOf(AiCommitAllWorkflowMode.Ai, AiCommitAllWorkflowMode.Commit)

        val availabilities = modes.map { mode ->
            AiCommitAllWorkflowAvailabilityPolicy.availability(
                mode = mode,
                hasCommittableContent = false,
                canExecuteCommit = { true },
                canExecuteCommitAndPush = { true },
                hasOutgoingCommitsToPush = { true },
            )
        }

        assertEquals(
            listOf(
                AiCommitAllWorkflowActionAvailability.Disabled,
                AiCommitAllWorkflowActionAvailability.Disabled,
            ),
            availabilities,
        )
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

    private class MutableAvailabilityProvider(
        var availability: AiCommitAllWorkflowActionAvailability,
    ) : AiCommitAllWorkflowAvailabilityProvider {
        val modes = mutableListOf<AiCommitAllWorkflowMode>()

        override fun availability(
            project: Project,
            mode: AiCommitAllWorkflowMode,
            dataContext: DataContext,
        ): AiCommitAllWorkflowActionAvailability {
            modes += mode
            return availability
        }
    }

    private class StaticActivityProvider(
        private val runningSection: AiCommitAllControlSection? = null,
    ) : AiCommitAllWorkflowActivityProvider {
        override fun runningSection(project: Project): AiCommitAllControlSection? = runningSection
    }

    private class MutableActivityProvider : AiCommitAllWorkflowActivityProvider {
        var runningSection: AiCommitAllControlSection? = null

        override fun runningSection(project: Project): AiCommitAllControlSection? = runningSection
    }

    private fun JComponent.asControl(): AiCommitAllThreeSectionControl = this as AiCommitAllThreeSectionControl

    private fun testEvent(
        dataContext: DataContext,
        inputEvent: InputEvent? = null,
    ): AnActionEvent = AnActionEvent(
        dataContext,
        Presentation(),
        ActionPlaces.CHANGES_VIEW_TOOLBAR,
        ActionUiKind.NONE,
        inputEvent,
        0,
        TestActionManager,
    )

    private fun testMouseEvent(component: Component): MouseEvent = MouseEvent(
        component,
        MouseEvent.MOUSE_CLICKED,
        0L,
        0,
        component.width / 2,
        component.height / 2,
        1,
        false,
        MouseEvent.BUTTON1,
    )

    private fun testDataContext(project: Project): DataContext = DataContext { dataId ->
        when (dataId) {
            CommonDataKeys.PROJECT.name -> project
            else -> null
        }
    }

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

    private fun java.lang.reflect.Method.defaultReturnValue(): Any? = when (returnType) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        java.lang.Void.TYPE -> null
        else -> null
    }

    private fun withDataManagerApplication(
        dataManager: ComponentDataManager,
        block: () -> Unit,
    ) {
        if (ApplicationManager.getApplication() != null) {
            block()
            return
        }

        val disposable = Disposer.newDisposable("AI Commit All action test application")
        ApplicationManager.setApplication(testApplication(dataManager), disposable)
        try {
            block()
        } finally {
            Disposer.dispose(disposable)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private class ComponentDataManager : com.intellij.ide.DataManager() {
        val contexts = mutableMapOf<Component, DataContext>()
        val requestedComponents = mutableListOf<Component>()

        override fun getDataContext(): DataContext = DataContext.EMPTY_CONTEXT

        override fun getDataContextFromFocusAsync(): Promise<DataContext> {
            error("Focus data context is not needed for action tests.")
        }

        override fun getDataContext(component: Component): DataContext {
            requestedComponents += component
            return contexts[component] ?: DataContext.EMPTY_CONTEXT
        }

        override fun getDataContext(
            component: Component,
            x: Int,
            y: Int,
        ): DataContext = getDataContext(component)

        override fun getCustomizedData(
            dataId: String,
            dataContext: DataContext,
            dataProvider: DataProvider,
        ): Any? = dataProvider.getData(dataId) ?: runCatching { dataContext.getData(dataId) }.getOrNull()

        override fun customizeDataContext(
            dataContext: DataContext,
            dataSource: Any,
        ): DataContext {
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

    private fun testApplication(dataManager: ComponentDataManager): Application = Proxy.newProxyInstance(
        Application::class.java.classLoader,
        arrayOf(Application::class.java),
    ) { _, method, arguments ->
        when (method.name) {
            "getService" -> when (arguments?.firstOrNull()) {
                com.intellij.ide.DataManager::class.java -> dataManager
                ActionManager::class.java -> TestActionManager
                else -> null
            }

            "getServiceIfCreated" -> when (arguments?.firstOrNull()) {
                com.intellij.ide.DataManager::class.java -> dataManager
                ActionManager::class.java -> TestActionManager
                else -> null
            }

            "isUnitTestMode",
            "isHeadlessEnvironment",
            -> true

            "toString" -> "AI Commit All Action Test Application"

            "hashCode" -> System.identityHashCode(dataManager)

            "equals" -> false

            else -> method.defaultReturnValue()
        }
    } as Application

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
