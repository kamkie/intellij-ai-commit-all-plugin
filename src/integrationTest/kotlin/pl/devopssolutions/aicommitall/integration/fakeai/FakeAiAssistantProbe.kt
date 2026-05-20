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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import java.awt.Component
import java.awt.Container
import java.awt.Graphics2D
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
    fun activateCommitToolWindow(project: Project): Boolean = runOnEdt {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(COMMIT_TOOL_WINDOW_ID)
            ?: return@runOnEdt false
        toolWindow.show()
        true
    }

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

        ActionUtil.performAction(action, event)

        check(document.text == commitMessageControl.message) {
            "Fake AI action wrote different commit messages through document and control APIs."
        }
        check(document.text.isNotBlank()) {
            "Fake AI action did not write a commit message."
        }
        return document.text
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

    private class CapturingCommitMessageControl : CommitMessageI {
        var message: String = ""
            private set

        override fun setCommitMessage(comment: String) {
            message = comment
        }
    }

    private const val ALPHA_SHIFT = 24
    private const val COMMIT_TOOL_WINDOW_ID = "Commit"
    private const val CONTROL_ACCESSIBLE_NAME = "AI Commit All"
    private const val CONTROL_CLASS_NAME =
        "pl.devopssolutions.aicommitall.actions.AiCommitAllThreeSectionControl"
    private const val CONTROL_COMPONENT_NAME = "AI Commit All three-section control"
    private const val PRIMARY_COMMIT_ACTIONS_GROUP_ID = "Vcs.Commit.PrimaryCommitActions"
}
