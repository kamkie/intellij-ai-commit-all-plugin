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
package pl.devopssolutions.aicommitall.integration.fakeai

import com.intellij.ide.DataManager
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import java.awt.Component
import java.awt.Container
import java.awt.Frame
import java.awt.Graphics2D
import java.awt.KeyboardFocusManager
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import javax.swing.JComponent

object FakeAiAssistantProbe {
    @JvmStatic
    fun isCommitMessageActionRegistered(): Boolean = ActionManager.getInstance().getAction("Vcs.LLMCommitMessageAction") != null

    @JvmStatic
    fun primaryCommitActionsContain(actionId: String): Boolean {
        val actionManager = ActionManager.getInstance()
        val primaryCommitActions = actionManager.getAction(PRIMARY_COMMIT_ACTIONS_GROUP_ID) as? DefaultActionGroup
            ?: return false
        val targetAction = actionManager.getAction(actionId)

        return primaryCommitActions.getChildren(actionManager).any { child ->
            child == targetAction || actionManager.getId(child) == actionId
        }
    }

    @JvmStatic
    fun primaryCommitActionIds(): List<String> {
        val actionManager = ActionManager.getInstance()
        val primaryCommitActions = actionManager.getAction(PRIMARY_COMMIT_ACTIONS_GROUP_ID) as? DefaultActionGroup
            ?: return emptyList()

        return primaryCommitActions.getChildren(actionManager).map { action ->
            actionManager.getId(action)
                ?: action.templatePresentation.text
                ?: action.javaClass.name
        }
    }

    @JvmStatic
    fun openCommitToolWindow(project: Project): Boolean = runOnEdt {
        performToolWindowActivationAction()
        val frame = WindowManager.getInstance().getFrame(project)
        frame?.bringToFrontForInput()
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(COMMIT_TOOL_WINDOW_ID)
            ?: return@runOnEdt false
        toolWindow.activate(null, true)
        true
    }

    @JvmStatic
    fun isIdeFrameAndAiCommitAllControlVisible(project: Project): Boolean = runOnEdt {
        val frame = WindowManager.getInstance().getFrame(project) ?: return@runOnEdt false
        frame.isShowing &&
            frame.isVisible &&
            frame.extendedState and Frame.ICONIFIED == 0 &&
            frame.isForegroundWindowForInput() &&
            findAiCommitAllControl(project)?.isShowing == true
    }

    @JvmStatic
    fun activateCommitToolWindow(project: Project): Boolean = openCommitToolWindow(project)

    @JvmStatic
    fun isAiCommitAllControlShowing(project: Project): Boolean = runOnEdt {
        findAiCommitAllControl(project)?.isShowing == true
    }

    @JvmStatic
    fun aiCommitAllControlAccessibleName(project: Project): String? = runOnEdt {
        findAiCommitAllControl(project)?.accessibleContext?.accessibleName
    }

    @JvmStatic
    fun aiCommitAllControlAccessibleDescription(project: Project): String? = runOnEdt {
        findAiCommitAllControl(project)?.accessibleContext?.accessibleDescription
    }

    @JvmStatic
    fun isAiCommitAllControlEnabled(project: Project): Boolean = runOnEdt {
        findAiCommitAllControl(project)?.isEnabled == true
    }

    @JvmStatic
    fun activateAiCommitAllSection(
        project: Project,
        section: String,
    ): Boolean = clickAiCommitAllSection(project, section)

    @JvmStatic
    fun clickAiCommitAllSection(
        project: Project,
        section: String,
    ): Boolean = runOnEdt {
        val control = findAiCommitAllControl(project) ?: return@runOnEdt false
        if (!control.isShowing) {
            return@runOnEdt false
        }
        control.requestFocusInWindow()
        control.dispatchClick(section)
        true
    }

    @JvmStatic
    fun writeAiCommitAllControlScreenshots(
        project: Project,
        outputDirectory: String,
    ): List<String> = runOnEdt {
        val control = requireNotNull(findAiCommitAllControl(project)) {
            "AI Commit All three-section control is not showing."
        }
        val directory = Path.of(outputDirectory)
        Files.createDirectories(directory)

        listOf(false, true).map { dark ->
            writeControlScreenshot(
                control = control,
                outputFile = directory.resolve("ai-commit-all-control-${if (dark) "dark" else "light"}.png"),
                dark = dark,
            )
        }
    }

    @JvmStatic
    fun generatedCommitMessageThroughDataContext(): String {
        val action = requireNotNull(ActionManager.getInstance().getAction("Vcs.LLMCommitMessageAction")) {
            "Vcs.LLMCommitMessageAction is not registered."
        }
        val document = EditorFactory.getInstance().createDocument("")
        val commitMessageControl = CapturingCommitMessageControl()
        val dataContext = SimpleDataContext.builder()
            .add(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT, document)
            .add(VcsDataKeys.COMMIT_MESSAGE_CONTROL, commitMessageControl)
            .build()
        val event = AnActionEvent.createEvent(
            action,
            dataContext,
            action.templatePresentation.clone(),
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            null,
        )

        runOnEdt {
            ActionUtil.performAction(action, event)
        }

        val generatedMessage = awaitGeneratedCommitMessage(document, commitMessageControl)
        check(document.text == commitMessageControl.message) {
            "Fake AI action wrote different commit messages through document and control APIs."
        }
        return generatedMessage
    }

    @JvmStatic
    fun registeredKeyboardShortcutText(actionId: String): String? = ActionManager.getInstance()
        .getKeyboardShortcut(actionId)
        ?.toString()

    @JvmStatic
    fun setUseVcsShortcutsForAiCommitAll(enabled: Boolean) {
        val settings = aiCommitAllSettingsInstance()
        settings.javaClass
            .getDeclaredMethod("updateUseVcsShortcutsForAiCommitAll", java.lang.Boolean.TYPE)
            .invoke(settings, enabled)
    }

    @JvmStatic
    fun useVcsShortcutsForAiCommitAll(): Boolean {
        val settings = aiCommitAllSettingsInstance()
        return settings.javaClass
            .getDeclaredMethod("useVcsShortcutsForAiCommitAll")
            .invoke(settings) as Boolean
    }

    @JvmStatic
    fun isShortcutActionEnabled(
        project: Project,
        actionId: String,
    ): Boolean = runOnEdt {
        val action = ActionManager.getInstance().getAction(actionId) ?: return@runOnEdt false
        val event = AnActionEvent.createEvent(
            action,
            commitWorkflowDataContext(project),
            action.templatePresentation.clone(),
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            null,
        )
        ActionUtil.performDumbAwareUpdate(action, event, false)
        event.presentation.isEnabled
    }

    @JvmStatic
    fun performAction(
        project: Project,
        actionId: String,
    ): Boolean = runOnEdt {
        val action = ActionManager.getInstance().getAction(actionId) ?: return@runOnEdt false
        val event = AnActionEvent.createEvent(
            action,
            commitWorkflowDataContext(project),
            action.templatePresentation.clone(),
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            null,
        )
        ActionUtil.performAction(action, event)
        true
    }

    @JvmStatic
    fun hasOutgoingCommitsToPush(project: Project): Boolean {
        val serviceClass = aiCommitAllPluginClass("pl.devopssolutions.aicommitall.vcs.GitOutgoingCommitsService")
        val companion = serviceClass.getDeclaredField("Companion").get(null)
        val service = companion.javaClass.getDeclaredMethod("getInstance", Project::class.java).invoke(companion, project)
        return service.javaClass.getDeclaredMethod("hasOutgoingCommitsToPush").invoke(service) as Boolean
    }

    @JvmStatic
    fun hasCommittableContent(project: Project): Boolean {
        val serviceClass = aiCommitAllPluginClass("pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService")
        val companion = serviceClass.getDeclaredField("Companion").get(null)
        val service = companion.javaClass.getDeclaredMethod("getInstance", Project::class.java).invoke(companion, project)
        val selection = service.javaClass.getDeclaredMethod("collectSelection").invoke(service)
        return selection.javaClass.getDeclaredMethod("getHasCommittableContent").invoke(selection) as Boolean
    }

    @JvmStatic
    fun setGitStagingAreaEnabled(enabled: Boolean) {
        val settings = gitVcsApplicationSettings()
        settings.javaClass.getDeclaredMethod("setStagingAreaEnabled", java.lang.Boolean.TYPE)
            .invoke(settings, enabled)
    }

    @JvmStatic
    fun isGitStagingAreaEnabled(): Boolean {
        val settings = gitVcsApplicationSettings()
        return settings.javaClass.getDeclaredMethod("isStagingAreaEnabled").invoke(settings) as Boolean
    }

    @JvmStatic
    fun commitWorkflowHandlerClassName(project: Project): String? = runOnEdt {
        val control = findAiCommitAllControl(project) ?: return@runOnEdt null
        VcsDataKeys.COMMIT_WORKFLOW_HANDLER.getData(DataManager.getInstance().getDataContext(control))
            ?.javaClass
            ?.name
    }

    @JvmStatic
    fun commitMessageText(project: Project): String? = runOnEdt {
        val control = findAiCommitAllControl(project) ?: return@runOnEdt null
        VcsDataKeys.COMMIT_WORKFLOW_UI.getData(DataManager.getInstance().getDataContext(control))
            ?.commitMessageUi
            ?.text
    }

    private fun writeControlScreenshot(
        control: JComponent,
        outputFile: Path,
        dark: Boolean,
    ): String {
        val previousDarkMode = !JBColor.isBright()
        JBColor.setDark(dark)
        try {
            val image = control.renderImage()
            check(image.hasNonblankContent()) {
                "AI Commit All control screenshot for dark=$dark is blank."
            }
            ImageIO.write(image, "png", outputFile.toFile())
            return outputFile.toAbsolutePath().toString()
        } finally {
            JBColor.setDark(previousDarkMode)
        }
    }

    private fun JComponent.renderImage(): BufferedImage {
        check(width > 0 && height > 0) {
            "AI Commit All control has invalid size ${width}x$height."
        }
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics() as Graphics2D
        try {
            paint(graphics)
        } finally {
            graphics.dispose()
        }
        return image
    }

    private fun JComponent.dispatchClick(section: String) {
        val clickPoint = sectionClickPoint(section)
        listOf(MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED).forEach { eventId ->
            dispatchEvent(
                MouseEvent(
                    this,
                    eventId,
                    System.currentTimeMillis(),
                    0,
                    clickPoint.x,
                    clickPoint.y,
                    1,
                    false,
                    MouseEvent.BUTTON1,
                ),
            )
        }
    }

    private fun awaitGeneratedCommitMessage(
        document: com.intellij.openapi.editor.Document,
        commitMessageControl: CapturingCommitMessageControl,
    ): String {
        val deadline = System.currentTimeMillis() + FAKE_GENERATION_TIMEOUT_MILLIS
        while (System.currentTimeMillis() <= deadline) {
            val message = document.text
            if (message.isNotBlank() && message == commitMessageControl.message) {
                return message
            }
            Thread.sleep(FAKE_GENERATION_POLL_MILLIS)
        }
        error("Fake AI action did not write a commit message.")
    }

    private fun JComponent.sectionClickPoint(section: String): Point {
        val xRatio = when (section) {
            "AI" -> 0.13
            "Commit" -> 0.45
            "Push" -> 0.82
            else -> error("Unknown AI Commit All control section: $section")
        }
        return Point((width * xRatio).toInt(), height / 2)
    }

    private fun BufferedImage.hasNonblankContent(): Boolean {
        var opaquePixels = 0
        val colors = mutableSetOf<Int>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = getRGB(x, y)
                if ((pixel ushr ALPHA_SHIFT) > 0) {
                    opaquePixels++
                    colors += pixel
                }
            }
        }
        return opaquePixels > 0 && colors.size > 1
    }

    private fun findAiCommitAllControl(project: Project): JComponent? {
        val frame = WindowManager.getInstance().getFrame(project) ?: return null
        return frame.descendants()
            .filterIsInstance<JComponent>()
            .firstOrNull { component ->
                component.name == CONTROL_COMPONENT_NAME ||
                    component.javaClass.name == CONTROL_CLASS_NAME ||
                    component.accessibleContext?.accessibleName == CONTROL_ACCESSIBLE_NAME
            }
    }

    private fun Component.descendants(): Sequence<Component> = sequence {
        yield(this@descendants)
        if (this@descendants is Container) {
            components.forEach { child ->
                yieldAll(child.descendants())
            }
        }
    }

    private fun <T> runOnEdt(action: () -> T): T {
        val application = ApplicationManager.getApplication()
        if (application == null || application.isDispatchThread) {
            return action()
        }

        val value = AtomicReference<T>()
        val throwable = AtomicReference<Throwable>()
        application.invokeAndWait {
            try {
                value.set(action())
            } catch (error: Throwable) {
                throwable.set(error)
            }
        }
        throwable.get()?.let { error -> throw error }
        return value.get()
    }

    private fun commitWorkflowDataContext(project: Project): DataContext {
        val control = findAiCommitAllControl(project)
        return if (control == null) {
            projectDataContext(project)
        } else {
            DataManager.getInstance().getDataContext(control)
        }
    }

    private fun performToolWindowActivationAction() {
        val action = ActionManager.getInstance().getAction(ACTIVATE_COMMIT_TOOL_WINDOW_ACTION_ID) ?: return
        val event = AnActionEvent.createEvent(
            action,
            DataContext.EMPTY_CONTEXT,
            action.templatePresentation.clone(),
            ActionPlaces.UNKNOWN,
            ActionUiKind.NONE,
            null,
        )
        ActionUtil.performAction(action, event)
    }

    private fun Frame.bringToFrontForInput() {
        if (extendedState and Frame.ICONIFIED != 0) {
            extendedState = extendedState and Frame.ICONIFIED.inv()
        }
        toFront()
        requestFocus()
        requestFocusInWindow()
    }

    private fun Window.isForegroundWindowForInput(): Boolean {
        val focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow
        return isActive || isFocused || focusedWindow == this || focusedWindow?.owner == this
    }

    private fun aiCommitAllSettingsInstance(): Any {
        val settingsClass = aiCommitAllPluginClass("pl.devopssolutions.aicommitall.settings.AiCommitAllSettings")
        val companion = settingsClass.getDeclaredField("Companion").get(null)
        return companion.javaClass.getDeclaredMethod("getInstance").invoke(companion)
    }

    private fun aiCommitAllPluginClass(className: String): Class<*> {
        val plugin = requireNotNull(PluginManagerCore.getPlugin(PluginId.getId(AI_COMMIT_ALL_PLUGIN_ID))) {
            "AI Commit All plugin descriptor was not found."
        }
        return Class.forName(className, true, plugin.pluginClassLoader)
    }

    private fun gitVcsApplicationSettings(): Any {
        val gitPlugin = requireNotNull(PluginManagerCore.getPlugin(PluginId.getId(GIT_PLUGIN_ID))) {
            "Git plugin descriptor was not found."
        }
        val settingsClass = Class.forName(
            "git4idea.config.GitVcsApplicationSettings",
            true,
            gitPlugin.pluginClassLoader,
        )
        return settingsClass.getDeclaredMethod("getInstance").invoke(null)
    }

    private fun projectDataContext(project: Project): DataContext = SimpleDataContext.builder()
        .add(CommonDataKeys.PROJECT, project)
        .build()

    private class CapturingCommitMessageControl : CommitMessageI {
        var message: String = ""
            private set

        override fun setCommitMessage(comment: String) {
            message = comment
        }
    }

    private const val ALPHA_SHIFT = 24
    private const val COMMIT_TOOL_WINDOW_ID = "Commit"
    private const val ACTIVATE_COMMIT_TOOL_WINDOW_ACTION_ID = "ActivateCommitToolWindow"
    private const val CONTROL_ACCESSIBLE_NAME = "AI Commit All"
    private const val CONTROL_CLASS_NAME =
        "pl.devopssolutions.aicommitall.actions.AiCommitAllThreeSectionControl"
    private const val CONTROL_COMPONENT_NAME = "AI Commit All three-section control"
    private const val PRIMARY_COMMIT_ACTIONS_GROUP_ID = "Vcs.Commit.PrimaryCommitActions"
    private const val AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID = "pl.devopssolutions.aicommitall.actions.CommitShortcut"
    private const val AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID = "pl.devopssolutions.aicommitall.actions.PushShortcut"
    private const val AI_COMMIT_ALL_PLUGIN_ID = "pl.devopssolutions.aicommitall"
    private const val GIT_PLUGIN_ID = "Git4Idea"
    private const val FAKE_GENERATION_TIMEOUT_MILLIS = 5_000L
    private const val FAKE_GENERATION_POLL_MILLIS = 100L
}
