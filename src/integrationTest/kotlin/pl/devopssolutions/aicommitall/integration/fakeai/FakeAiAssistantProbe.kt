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

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.DataManager
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.plugins.DynamicPluginEnabler
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginEnableStateChangedListener
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginModuleId
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.LicensingFacade
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.commit.CommitMessageUi
import java.awt.AWTEvent
import java.awt.Component
import java.awt.Container
import java.awt.Dialog
import java.awt.Dialog.ModalityType
import java.awt.Frame
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.AWTEventListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.text.JTextComponent

object FakeAiAssistantProbe {
    private val logger = Logger.getInstance(FakeAiAssistantProbe::class.java)
    private val licenseRestartObserverInstalled = AtomicBoolean()
    private val licenseRestartHandled = AtomicBoolean()
    private val syntheticLicenseRestartDialogShown = AtomicBoolean()
    private val licenseRestartDiagnostic = AtomicReference("No modal dialog has been observed.")
    private val ultimateEnableObserverRegistrationStarted = AtomicBoolean()
    private val ultimateEnableObserverInstalled = AtomicBoolean()
    private val ultimateEnableAttemptCompleted = AtomicBoolean()
    private val pluginEnableStateChangedListener = object : PluginEnableStateChangedListener {
        override fun stateChanged(
            pluginDescriptors: Collection<IdeaPluginDescriptor>,
            enable: Boolean,
        ) {
            val pluginIds = pluginDescriptors.map { descriptor -> descriptor.pluginId.idString }
            val isUltimateEnableAttempt = enable && ULTIMATE_MODULE_ID in pluginIds
            logger.info(
                "AI Commit All test plugin enablement callback: enabled=$enable, " +
                    "pluginIds=$pluginIds, ultimateEnableAttempt=$isUltimateEnableAttempt",
            )
            if (isUltimateEnableAttempt) {
                ultimateEnableAttemptCompleted.set(true)
            }
        }
    }
    private var manageSubscriptionsDialogCloserInstalled = false
    private val manageSubscriptionsDialogCloser = AWTEventListener { event ->
        if (event.id == WindowEvent.WINDOW_OPENED) {
            ((event as? WindowEvent)?.window as? Dialog)?.let(::closeManageSubscriptionsDialog)
        }
    }
    private val licenseRestartObserver = AWTEventListener { event ->
        if (event.id == WindowEvent.WINDOW_OPENED) {
            ((event as? WindowEvent)?.window as? Dialog)?.let { dialog ->
                ApplicationManager.getApplication().invokeLater {
                    handleLicenseRestartDialog(dialog)
                }
            }
        }
    }

    @JvmStatic
    fun isCommitMessageActionRegistered(): Boolean = ActionManager.getInstance().getAction("Vcs.LLMCommitMessageAction") != null

    @JvmStatic
    fun isAiCommitAllPluginEnabled(): Boolean = PluginManagerCore.isLoaded(PluginId.getId(AI_COMMIT_ALL_PLUGIN_ID))

    @JvmStatic
    fun isAiCommitAllThreeSectionActionRegistered(): Boolean = ActionManager
        .getInstance()
        .getAction(AI_COMMIT_ALL_THREE_SECTION_ACTION_ID) != null

    @JvmStatic
    fun installLicenseRestartObserver() {
        if (licenseRestartObserverInstalled.compareAndSet(false, true)) {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                licenseRestartObserver,
                AWTEvent.WINDOW_EVENT_MASK,
            )
            logger.info("AI Commit All test plugin license restart observer installed")
        }
        continueRestartedLicensePreflightIfNeeded()
        Window.getWindows().filterIsInstance<Dialog>().forEach(::handleLicenseRestartDialog)
        ApplicationManager.getApplication().invokeLater(::showSyntheticLicenseRestartDialogIfRequested)
    }

    @JvmStatic
    fun isIntellij2026Point2LicenseRestartDialog(
        productCode: String,
        ideVersion: String,
        title: String,
        body: String,
        action: String,
    ): Boolean = productCode in LICENSE_RESTART_PRODUCT_CODES &&
        isInReleaseLine(ideVersion, INTELLIJ_2026_2_RELEASE_LINE) &&
        title == LICENSE_RESTART_DIALOG_TITLE &&
        body == LICENSE_RESTART_DIALOG_BODY &&
        action == LICENSE_RESTART_ACTION

    @JvmStatic
    fun licenseRestartHandlingDiagnostic(): String = licenseRestartDiagnostic.get()

    @JvmStatic
    fun isIdeLicenseActive(): Boolean {
        val licensingFacade = LicensingFacade.getInstance() ?: return false
        val licensedTo = licensingFacade.licensedTo ?: return false
        val productReleaseDate = Date(ApplicationInfo.getInstance().buildDate.timeInMillis)
        return licensedTo.isNotBlank() && licensingFacade.isApplicableForProduct(productReleaseDate)
    }

    @JvmStatic
    fun installUltimateEnableAttemptObserver() {
        if (ultimateEnableObserverRegistrationStarted.compareAndSet(false, true)) {
            DynamicPluginEnabler.addPluginStateChangedListener(pluginEnableStateChangedListener)
            ultimateEnableObserverInstalled.set(true)
            logger.info("AI Commit All test plugin enablement observer installed")
        }
    }

    @JvmStatic
    fun isUltimateEnableAttemptObserverInstalled(): Boolean = ultimateEnableObserverInstalled.get()

    @JvmStatic
    fun isUltimateModuleLoaded(): Boolean = PluginManagerCore.isLoaded(PluginId.getId(ULTIMATE_MODULE_ID))

    @JvmStatic
    fun isUltimateEnableAttemptCompleted(): Boolean = ultimateEnableAttemptCompleted.get()

    @JvmStatic
    fun isProjectSmart(project: Project): Boolean = !DumbService.getInstance(project).isDumb

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
        frame?.restoreIfMinimized()
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
    fun setAiCompletionOptions(
        timeoutMillis: Long,
        checkIntervalMillis: Long,
    ) {
        val settings = aiCommitAllSettingsInstance()
        settings.javaClass
            .getDeclaredMethod("updateCompletionOptions", java.lang.Long.TYPE, java.lang.Long.TYPE)
            .invoke(settings, timeoutMillis, checkIntervalMillis)
    }

    @JvmStatic
    fun setClearCommitMessageBeforeGeneration(enabled: Boolean) {
        val settings = aiCommitAllSettingsInstance()
        settings.javaClass
            .getDeclaredMethod("updateClearCommitMessageBeforeGeneration", java.lang.Boolean.TYPE)
            .invoke(settings, enabled)
    }

    @JvmStatic
    fun setFakeAiBehavior(behaviorName: String) {
        FakeLlmCommitMessageAction.setBehavior(
            when (behaviorName) {
                "generated" -> FakeLlmCommitMessageBehavior.Generated
                "empty" -> FakeLlmCommitMessageBehavior.Empty
                "unchanged" -> FakeLlmCommitMessageBehavior.Unchanged
                "never-finishes" -> FakeLlmCommitMessageBehavior.NeverFinishes
                else -> error("Unknown fake AI behavior: $behaviorName")
            },
        )
    }

    @JvmStatic
    fun fakeAiInvocationCount(): Int = FakeLlmCommitMessageAction.invocationCount()

    @JvmStatic
    fun commitUiPathsAtFakeAiInvocation(): List<String> = FakeLlmCommitMessageAction.commitUiPathsAtInvocation()

    @JvmStatic
    fun unregisterFakeAiAction() {
        val actionManager = ActionManager.getInstance()
        if (actionManager.getAction(FAKE_AI_ACTION_ID) != null) {
            actionManager.unregisterAction(FAKE_AI_ACTION_ID)
        }
    }

    @JvmStatic
    fun replaceFakeAiActionWithUnavailableSignal() {
        registerFakeAiAction(FakeUnavailableLlmCommitMessageAction())
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
        ActionUtil.updateAction(action, event)
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
    fun stagingAreaSelectionPaths(project: Project): List<String> {
        val serviceClass = aiCommitAllPluginClass("pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService")
        val companion = serviceClass.getDeclaredField("Companion").get(null)
        val service = companion.javaClass.getDeclaredMethod("getInstance", Project::class.java).invoke(companion, project)
        val selection = service.javaClass.getDeclaredMethod("collectSelection").invoke(service)

        @Suppress("UNCHECKED_CAST")
        val paths = selection.javaClass.getDeclaredMethod("getStagingAreaPaths").invoke(selection) as List<FilePath>
        return paths.map { path -> path.path.replace('\\', '/') }.sorted()
    }

    @JvmStatic
    fun gitSelectionPaths(project: Project): List<String> {
        val serviceClass = aiCommitAllPluginClass("pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService")
        val companion = serviceClass.getDeclaredField("Companion").get(null)
        val service = companion.javaClass.getDeclaredMethod("getInstance", Project::class.java).invoke(companion, project)
        val selection = service.javaClass.getDeclaredMethod("collectSelection").invoke(service)

        @Suppress("UNCHECKED_CAST")
        val trackedChanges = selection.javaClass.getDeclaredMethod("getTrackedChanges").invoke(selection) as List<Change>

        @Suppress("UNCHECKED_CAST")
        val unversionedFiles = selection.javaClass
            .getDeclaredMethod("getUnversionedFiles")
            .invoke(selection) as List<FilePath>
        return buildList {
            trackedChanges.forEach { change ->
                change.beforeRevision?.file?.let(::add)
                change.afterRevision?.file?.let(::add)
            }
            addAll(unversionedFiles)
        }.map { path -> path.path.replace('\\', '/') }.distinct().sorted()
    }

    @JvmStatic
    fun resetReleaseMatrixSettings() {
        runOnEdt {
            installManageSubscriptionsDialogCloser()
        }
        registerFakeAiAction(FakeLlmCommitMessageAction())
        FakeLlmCommitMessageAction.reset()
        setAiCompletionOptions(
            timeoutMillis = DEFAULT_AI_COMPLETION_TIMEOUT_MILLIS,
            checkIntervalMillis = DEFAULT_AI_COMPLETION_CHECK_INTERVAL_MILLIS,
        )
        setClearCommitMessageBeforeGeneration(true)
        setUseVcsShortcutsForAiCommitAll(true)
        setGitStagingAreaEnabled(false)
    }

    private fun installManageSubscriptionsDialogCloser() {
        if (!manageSubscriptionsDialogCloserInstalled) {
            Toolkit.getDefaultToolkit().addAWTEventListener(
                manageSubscriptionsDialogCloser,
                AWTEvent.WINDOW_EVENT_MASK,
            )
            manageSubscriptionsDialogCloserInstalled = true
        }
        Window.getWindows()
            .filterIsInstance<Dialog>()
            .forEach(::closeManageSubscriptionsDialog)
    }

    private fun closeManageSubscriptionsDialog(dialog: Dialog) {
        if (dialog.isShowing && dialog.title == MANAGE_SUBSCRIPTIONS_DIALOG_TITLE) {
            dialog.dispose()
        }
    }

    private fun handleLicenseRestartDialog(dialog: Dialog) {
        if (!dialog.isShowing || dialog.modalityType == ModalityType.MODELESS) {
            return
        }

        val applicationInfo = ApplicationInfo.getInstance()
        val bodyFragments = dialog.descendants()
            .mapNotNull { component ->
                when (component) {
                    is JLabel -> component.text
                    is JTextComponent -> component.text
                    else -> null
                }
            }
            .map(::normalizeDialogText)
            .filter(String::isNotBlank)
            .toList()
        val body = bodyFragments.firstOrNull { fragment -> fragment == LICENSE_RESTART_DIALOG_BODY }
            ?: normalizeDialogText(bodyFragments.joinToString(separator = " "))
        val actionButtons = dialog.descendants()
            .filterIsInstance<AbstractButton>()
            .filter(AbstractButton::isShowing)
            .toList()
        val actionTexts = actionButtons.map { button -> normalizeDialogText(button.text.orEmpty()) }
        val restartButtons = actionButtons.filter { button ->
            normalizeDialogText(button.text.orEmpty()) == LICENSE_RESTART_ACTION
        }
        val isSyntheticLifecycleProof = dialog.descendants()
            .filterIsInstance<JComponent>()
            .any { component ->
                component.getClientProperty(SYNTHETIC_LICENSE_RESTART_PROOF_COMPONENT_KEY) == true
            }
        val source = if (isSyntheticLifecycleProof) {
            SYNTHETIC_LICENSE_RESTART_SOURCE
        } else {
            PLATFORM_LICENSE_RESTART_SOURCE
        }
        val diagnostic = "source=$source, product=${applicationInfo.build.productCode}, " +
            "version=${applicationInfo.shortVersion}, title=${dialog.title}, body=$body, actions=$actionTexts"
        val restartAction = restartButtons.singleOrNull()?.text.orEmpty()

        if (
            !isIntellij2026Point2LicenseRestartDialog(
                productCode = applicationInfo.build.productCode,
                ideVersion = applicationInfo.shortVersion,
                title = dialog.title,
                body = body,
                action = restartAction,
            )
        ) {
            licenseRestartDiagnostic.set("Observed modal did not match the exact license restart contract: $diagnostic")
            return
        }
        if (!licenseRestartHandled.compareAndSet(false, true)) {
            return
        }

        val markerPath = licenseRestartMarkerPath()
            ?: error("The exact license restart dialog was shown without a configured restart marker path: $diagnostic")
        val existingMarker = readLicenseRestartMarker(markerPath)
        if (existingMarker != null) {
            writeLicenseRestartMarker(
                markerPath,
                existingMarker + ("state" to LICENSE_RESTART_LOOP_STATE),
            )
            licenseRestartDiagnostic.set("Refusing a second license restart: $diagnostic")
            return
        }

        val marker = linkedMapOf(
            "state" to LICENSE_RESTART_REQUESTED_STATE,
            "source" to source,
            "product" to applicationInfo.build.productCode,
            "version" to applicationInfo.shortVersion,
            "title" to dialog.title,
            "body" to body,
            "action" to restartAction,
            "jmxPort" to requiredLicenseRestartSystemProperty(JMX_PORT_PROPERTY),
            "rmiPort" to requiredLicenseRestartSystemProperty(RMI_PORT_PROPERTY),
            "rpcPort" to requiredLicenseRestartSystemProperty(RPC_PORT_PROPERTY),
            "originalPid" to ProcessHandle.current().pid().toString(),
        )
        writeLicenseRestartMarker(markerPath, marker)
        licenseRestartDiagnostic.set("Invoking the exact platform Restart action: $diagnostic")
        logger.info("AI Commit All test plugin invoking exact license Restart action: $diagnostic")
        restartButtons.single().doClick()
    }

    private fun continueRestartedLicensePreflightIfNeeded() {
        val markerPath = licenseRestartMarkerPath() ?: return
        val marker = readLicenseRestartMarker(markerPath) ?: return
        val productCode = ApplicationInfo.getInstance().build.productCode
        if (!marker.hasExactLicenseRestartContract(LICENSE_RESTART_REQUESTED_STATE, productCode)) {
            return
        }

        val restartedMarker = marker +
            ("state" to LICENSE_RESTART_STARTED_STATE) +
            ("restartPid" to ProcessHandle.current().pid().toString())
        writeLicenseRestartMarker(markerPath, restartedMarker)
        licenseRestartDiagnostic.set("License restart process is waiting for host cleanup: $restartedMarker")
        ApplicationManager.getApplication().executeOnPooledThread {
            markerPath.parent.fileSystem.newWatchService().use { watchService ->
                val shutdownRequestPath = licenseRestartShutdownRequestPath(markerPath)
                markerPath.parent.register(watchService, ENTRY_CREATE)
                if (acceptLicenseRestartShutdown(markerPath)) {
                    return@executeOnPooledThread
                }
                while (true) {
                    val key = watchService.take()
                    val shutdownRequested = key.pollEvents().any { event ->
                        event.context() == shutdownRequestPath.fileName
                    }
                    check(key.reset()) {
                        "License restart marker watch key became invalid: $markerPath"
                    }
                    if (shutdownRequested && acceptLicenseRestartShutdown(markerPath)) {
                        return@executeOnPooledThread
                    }
                }
            }
        }
    }

    private fun acceptLicenseRestartShutdown(markerPath: Path): Boolean {
        if (!Files.isRegularFile(licenseRestartShutdownRequestPath(markerPath))) {
            return false
        }
        val marker = readLicenseRestartMarker(markerPath) ?: return false
        val productCode = ApplicationInfo.getInstance().build.productCode
        if (!marker.hasExactLicenseRestartContract(LICENSE_RESTART_SHUTDOWN_REQUESTED_STATE, productCode)) {
            return false
        }
        writeLicenseRestartMarker(
            markerPath,
            marker + ("state" to LICENSE_RESTART_SHUTDOWN_ACCEPTED_STATE),
        )
        licenseRestartDiagnostic.set("License restart process accepted host cleanup: $marker")
        ApplicationManager.getApplication().invokeLater {
            ApplicationManager.getApplication().exit()
        }
        return true
    }

    private fun licenseRestartShutdownRequestPath(markerPath: Path): Path = markerPath.resolveSibling("${markerPath.fileName}.shutdown-requested")

    private fun showSyntheticLicenseRestartDialogIfRequested() {
        if (System.getProperty(SYNTHETIC_LICENSE_RESTART_PROOF_PROPERTY) != "true") {
            return
        }
        val applicationInfo = ApplicationInfo.getInstance()
        if (
            applicationInfo.build.productCode !in LICENSE_RESTART_PRODUCT_CODES ||
            !isInReleaseLine(applicationInfo.shortVersion, INTELLIJ_2026_2_RELEASE_LINE)
        ) {
            return
        }
        val markerPath = licenseRestartMarkerPath() ?: return
        if (!Files.exists(markerPath) && syntheticLicenseRestartDialogShown.compareAndSet(false, true)) {
            SyntheticLicenseRestartDialog().show()
        }
    }

    private fun normalizeDialogText(text: String): String = text
        .replace(HTML_TAG_REGEX, " ")
        .replace(WHITESPACE_REGEX, " ")
        .trim()

    private fun licenseRestartMarkerPath(): Path? = System
        .getProperty(LICENSE_RESTART_MARKER_PROPERTY)
        ?.takeIf(String::isNotBlank)
        ?.let(Path::of)

    private fun requiredLicenseRestartSystemProperty(name: String): String = requireNotNull(
        System.getProperty(name)?.takeIf(String::isNotBlank),
    ) {
        "The exact license restart dialog requires IDE system property '$name'."
    }

    private fun readLicenseRestartMarker(markerPath: Path): Map<String, String>? {
        if (!Files.isRegularFile(markerPath)) {
            return null
        }
        return Files.readAllLines(markerPath)
            .mapNotNull { line ->
                val separator = line.indexOf('=')
                if (separator <= 0) null else line.substring(0, separator) to line.substring(separator + 1)
            }
            .toMap()
    }

    private fun writeLicenseRestartMarker(
        markerPath: Path,
        marker: Map<String, String>,
    ) {
        markerPath.parent?.let(Files::createDirectories)
        val nextMarkerPath = markerPath.resolveSibling("${markerPath.fileName}.next")
        Files.write(
            nextMarkerPath,
            LICENSE_RESTART_MARKER_KEYS.mapNotNull { key ->
                marker[key]?.let { value -> "$key=$value" }
            },
        )
        Files.move(nextMarkerPath, markerPath, ATOMIC_MOVE, REPLACE_EXISTING)
    }

    private fun isInReleaseLine(version: String?, releaseLine: String): Boolean = version == releaseLine || version?.startsWith("$releaseLine.") == true

    private fun Map<String, String>.hasExactLicenseRestartContract(
        state: String,
        productCode: String,
    ): Boolean = this["state"] == state &&
        this["source"] in LICENSE_RESTART_SOURCES &&
        productCode in LICENSE_RESTART_PRODUCT_CODES &&
        this["product"] == productCode &&
        isInReleaseLine(this["version"], INTELLIJ_2026_2_RELEASE_LINE) &&
        this["title"] == LICENSE_RESTART_DIALOG_TITLE &&
        this["body"] == LICENSE_RESTART_DIALOG_BODY &&
        this["action"] == LICENSE_RESTART_ACTION &&
        this["jmxPort"]?.toIntOrNull() in 1..65535 &&
        this["rmiPort"]?.toIntOrNull() in 1..65535 &&
        this["rpcPort"]?.toIntOrNull() in 1..65535 &&
        this["originalPid"]?.toLongOrNull()?.let { pid -> pid > 0 } == true &&
        (
            state == LICENSE_RESTART_REQUESTED_STATE ||
                this["restartPid"]?.toLongOrNull()?.let { pid -> pid > 0 } == true
            )

    @JvmStatic
    fun setGitStagingAreaEnabled(enabled: Boolean) = runOnEdt {
        val gitModule = requireNotNull(
            PluginManagerCore.getPluginSet().findEnabledModule(
                PluginModuleId.getId(GIT_BACKEND_MODULE_ID, PluginModuleId.JETBRAINS_NAMESPACE),
            ),
        ) {
            "Git settings module descriptor was not found."
        }
        val stageManagerClass = Class.forName(
            GIT_STAGE_MANAGER_CLASS_NAME,
            true,
            gitModule.pluginClassLoader,
        )
        stageManagerClass.getDeclaredMethod("enableStagingArea", java.lang.Boolean.TYPE)
            .invoke(null, enabled)
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
        commitMessageUi(project)?.text
    }

    @JvmStatic
    fun setCommitMessageText(
        project: Project,
        message: String,
    ): Boolean = runOnEdt {
        val commitMessageUi = commitMessageUi(project) ?: return@runOnEdt false
        commitMessageUi.text = message
        (commitMessageUi as? CommitMessageI)?.setCommitMessage(message)
        true
    }

    @JvmStatic
    fun dispatchUserCommitMessageEdit(
        project: Project,
        message: String,
    ): Boolean = runOnEdt {
        val commitMessageUi = commitMessageUi(project) ?: return@runOnEdt false
        val editorComponent = commitMessageUi.editorComponent() ?: return@runOnEdt false
        val eventQueue = IdeEventQueue.getInstance()
        val disposable = Disposer.newDisposable("AI Commit All release matrix user edit dispatch")
        var edited = false
        val dispatcher = object : IdeEventQueue.NonLockedEventDispatcher {
            override fun dispatch(e: AWTEvent): Boolean {
                if (e is KeyEvent && e.source === editorComponent && e.keyChar == USER_EDIT_SENTINEL_KEY) {
                    edited = true
                    commitMessageUi.text = message
                    (commitMessageUi as? CommitMessageI)?.setCommitMessage(message)
                    return true
                }
                return false
            }
        }

        try {
            eventQueue.addDispatcher(dispatcher, disposable)
            commitMessageUi.focus()
            editorComponent.requestFocusInWindow()
            eventQueue.dispatchEvent(
                KeyEvent(
                    editorComponent,
                    KeyEvent.KEY_TYPED,
                    System.currentTimeMillis(),
                    0,
                    KeyEvent.VK_UNDEFINED,
                    USER_EDIT_SENTINEL_KEY,
                ),
            )
        } finally {
            Disposer.dispose(disposable)
        }

        edited && commitMessageUi.text == message
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

    private fun commitMessageUi(project: Project): CommitMessageUi? {
        val control = findAiCommitAllControl(project) ?: return null
        return VcsDataKeys.COMMIT_WORKFLOW_UI.getData(DataManager.getInstance().getDataContext(control))
            ?.commitMessageUi
    }

    private fun CommitMessageUi.editorComponent(): Component? {
        val editorField = javaClass.findNoArgumentMethod("getEditorField")?.invoke(this) ?: return null
        return editorField.javaClass.findNoArgumentMethod("getComponent")?.invoke(editorField) as? Component
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

    private fun Class<*>.findNoArgumentMethod(name: String) = methods.firstOrNull { method ->
        method.name == name && method.parameterCount == 0
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

    private fun Frame.restoreIfMinimized() {
        if (extendedState and Frame.ICONIFIED != 0) {
            extendedState = extendedState and Frame.ICONIFIED.inv()
        }
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

    private fun registerFakeAiAction(action: AnAction) {
        val actionManager = ActionManager.getInstance()
        if (actionManager.getAction(FAKE_AI_ACTION_ID) != null) {
            actionManager.unregisterAction(FAKE_AI_ACTION_ID)
        }
        actionManager.registerAction(FAKE_AI_ACTION_ID, action)
    }

    private fun gitVcsApplicationSettings(): Any {
        val settingsClassName = "git4idea.config.GitVcsApplicationSettings"
        val gitModule = requireNotNull(
            PluginManagerCore.getPluginSet().findEnabledModule(
                PluginModuleId.getId(GIT_BACKEND_MODULE_ID, PluginModuleId.JETBRAINS_NAMESPACE),
            ),
        ) {
            "Git settings module descriptor was not found."
        }
        val settingsClass = Class.forName(
            settingsClassName,
            true,
            gitModule.pluginClassLoader,
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
    private const val AI_COMMIT_ALL_THREE_SECTION_ACTION_ID = "pl.devopssolutions.aicommitall.actions.ThreeSectionControl"
    private const val AI_COMMIT_ALL_COMMIT_SHORTCUT_ACTION_ID = "pl.devopssolutions.aicommitall.actions.CommitShortcut"
    private const val AI_COMMIT_ALL_PUSH_SHORTCUT_ACTION_ID = "pl.devopssolutions.aicommitall.actions.PushShortcut"
    private const val AI_COMMIT_ALL_PLUGIN_ID = "pl.devopssolutions.aicommitall"
    private const val ULTIMATE_MODULE_ID = "com.intellij.modules.ultimate"
    private const val GIT_BACKEND_MODULE_ID = "intellij.vcs.git.backend"
    private const val GIT_STAGE_MANAGER_CLASS_NAME = "git4idea.index.GitStageManagerKt"
    private const val MANAGE_SUBSCRIPTIONS_DIALOG_TITLE = "Manage Subscriptions"
    private const val LICENSE_RESTART_MARKER_PROPERTY = "aicommitall.license.restart.marker"
    private const val SYNTHETIC_LICENSE_RESTART_PROOF_PROPERTY = "aicommitall.license.restart.synthetic.proof"
    private const val SYNTHETIC_LICENSE_RESTART_PROOF_COMPONENT_KEY =
        "aicommitall.license.restart.synthetic.proof.component"
    private const val INTELLIJ_2026_2_RELEASE_LINE = "2026.2"
    private const val LICENSE_RESTART_DIALOG_TITLE = "Confirm Restart"
    private const val LICENSE_RESTART_DIALOG_BODY =
        "Application restart is necessary to disable features requiring license. " +
            "Click 'Restart' to continue using the product without these features."
    private const val LICENSE_RESTART_ACTION = "Restart"
    private const val BACK_TO_SUBSCRIPTIONS_ACTION = "Back to Subscriptions"
    private const val PLATFORM_LICENSE_RESTART_SOURCE = "platform-license-dialog"
    private const val SYNTHETIC_LICENSE_RESTART_SOURCE = "synthetic-lifecycle-proof"
    private const val LICENSE_RESTART_REQUESTED_STATE = "restart-requested"
    private const val LICENSE_RESTART_STARTED_STATE = "restart-started"
    private const val LICENSE_RESTART_SHUTDOWN_REQUESTED_STATE = "shutdown-requested"
    private const val LICENSE_RESTART_SHUTDOWN_ACCEPTED_STATE = "shutdown-accepted"
    private const val LICENSE_RESTART_LOOP_STATE = "restart-loop"
    private const val JMX_PORT_PROPERTY = "com.sun.management.jmxremote.port"
    private const val RMI_PORT_PROPERTY = "com.sun.management.jmxremote.rmi.port"
    private const val RPC_PORT_PROPERTY = "rpc.port"
    private const val FAKE_GENERATION_TIMEOUT_MILLIS = 30_000L
    private const val FAKE_GENERATION_POLL_MILLIS = 100L
    private const val DEFAULT_AI_COMPLETION_TIMEOUT_MILLIS = 30_000L
    private const val DEFAULT_AI_COMPLETION_CHECK_INTERVAL_MILLIS = 500L
    private const val FAKE_AI_ACTION_ID = "Vcs.LLMCommitMessageAction"
    private const val USER_EDIT_SENTINEL_KEY = '\u001D'
    private val LICENSE_RESTART_SOURCES = setOf(
        PLATFORM_LICENSE_RESTART_SOURCE,
        SYNTHETIC_LICENSE_RESTART_SOURCE,
    )
    private val LICENSE_RESTART_PRODUCT_CODES = setOf("IU", "PY")
    private val LICENSE_RESTART_MARKER_KEYS = listOf(
        "state",
        "source",
        "product",
        "version",
        "title",
        "body",
        "action",
        "jmxPort",
        "rmiPort",
        "rpcPort",
        "originalPid",
        "restartPid",
    )
    private val HTML_TAG_REGEX = Regex("<[^>]+>")
    private val WHITESPACE_REGEX = Regex("\\s+")
}

private class SyntheticLicenseRestartDialog : DialogWrapper(false) {
    private val message = JBLabel(LICENSE_RESTART_DIALOG_BODY).apply {
        putClientProperty(SYNTHETIC_LICENSE_RESTART_PROOF_COMPONENT_KEY, true)
    }

    init {
        title = LICENSE_RESTART_DIALOG_TITLE
        setOKButtonText(LICENSE_RESTART_ACTION)
        setCancelButtonText(BACK_TO_SUBSCRIPTIONS_ACTION)
        init()
    }

    override fun createCenterPanel(): JComponent = message

    override fun doOKAction() {
        close(OK_EXIT_CODE)
        ApplicationManagerEx.getApplicationEx().restart(true)
    }

    companion object {
        private const val LICENSE_RESTART_DIALOG_TITLE = "Confirm Restart"
        private const val LICENSE_RESTART_DIALOG_BODY =
            "Application restart is necessary to disable features requiring license. " +
                "Click 'Restart' to continue using the product without these features."
        private const val LICENSE_RESTART_ACTION = "Restart"
        private const val BACK_TO_SUBSCRIPTIONS_ACTION = "Back to Subscriptions"
        private const val SYNTHETIC_LICENSE_RESTART_PROOF_COMPONENT_KEY =
            "aicommitall.license.restart.synthetic.proof.component"
    }
}

class FakeAiAssistantAppLifecycleListener : AppLifecycleListener {
    override fun appStarted() {
        FakeAiAssistantProbe.installLicenseRestartObserver()
        FakeAiAssistantProbe.installUltimateEnableAttemptObserver()
    }
}
