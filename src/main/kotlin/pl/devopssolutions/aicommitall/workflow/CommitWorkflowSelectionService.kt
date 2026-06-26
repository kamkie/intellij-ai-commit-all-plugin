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
package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionService
import pl.devopssolutions.aicommitall.vcs.GitChangeSelectionSource
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference

@Service(Service.Level.PROJECT)
internal class CommitWorkflowSelectionService @JvmOverloads constructor(
    private val project: Project,
    private val dependencies: CommitWorkflowSelectionDependencies = CommitWorkflowSelectionDependencies(),
) {
    fun prepareAllFilesSelection(
        workflowHandler: CommitWorkflowHandler?,
        workflowUi: CommitWorkflowUi?,
    ): CommitWorkflowSelectionResult = CommitWorkflowSelectionInput.create(workflowHandler, workflowUi)
        ?.let(::prepareAllFilesSelection)
        ?: CommitWorkflowSelectionResult.MissingWorkflow

    private fun prepareAllFilesSelection(input: CommitWorkflowSelectionInput): CommitWorkflowSelectionResult {
        val selectionService = dependencies.selectionSource(project)
        val supportStatus = selectionService.supportStatus()
        return if (supportStatus == GitVcsSupportStatus.Supported) {
            prepareSupportedSelection(input, selectionService)
        } else {
            CommitWorkflowSelectionResult.UnsupportedVcs(supportStatus)
        }
    }

    private fun prepareSupportedSelection(
        input: CommitWorkflowSelectionInput,
        selectionService: GitChangeSelectionSource,
    ): CommitWorkflowSelectionResult {
        val selection = selectionService.collectSelection()
        return if (selection.hasCommittableContent) {
            prepareCommittableSelection(input, selection)
        } else {
            CommitWorkflowSelectionResult.EmptySelection
        }
    }

    private fun prepareCommittableSelection(
        input: CommitWorkflowSelectionInput,
        selection: GitChangeSelection,
    ): CommitWorkflowSelectionResult {
        val changeLists = CommitWorkflowSelectionItems.changeListsContaining(
            trackedChanges = selection.trackedChanges,
            changeLists = dependencies.changeLists(project),
        )
        return if (changeLists.isEmpty() && selection.trackedChanges.isNotEmpty()) {
            CommitWorkflowSelectionResult.UnsupportedWorkflow("No Git changelist owns the selected tracked changes.")
        } else {
            synchronizeSelection(input, selection, changeLists)
        }
    }

    private fun synchronizeSelection(
        input: CommitWorkflowSelectionInput,
        selection: GitChangeSelection,
        changeLists: List<LocalChangeList>,
    ): CommitWorkflowSelectionResult {
        val activeChangeList = chooseActiveChangeList(changeLists)
        val inclusionItems = CommitWorkflowSelectionItems.inclusionItems(selection)
        logger.info(
            "AI Commit All diagnostic: activating commit workflow UI, " +
                "trackedChanges=${selection.trackedChanges.size}, " +
                "unversionedFiles=${selection.unversionedFiles.size}, " +
                "resolvedConflicts=${selection.resolvedConflictPaths.size}, " +
                "stagingAreaPaths=${selection.stagingAreaPaths.size}, " +
                "changeLists=${changeLists.size}, inclusionItems=${inclusionItems.size}",
        )
        val activated = dependencies.activationRetry.activate {
            dependencies.activateWorkflowUi(input.workflowUi)
        }
        logger.info("AI Commit All diagnostic: commit workflow UI activation result, activated=$activated")

        return if (activated) {
            synchronizedSelectionResult(
                input = input,
                selection = selection,
                changeLists = changeLists,
                activeChangeList = activeChangeList,
                inclusionItems = inclusionItems,
            )
        } else {
            CommitWorkflowSelectionResult.UnsupportedWorkflow("The Commit tool window workflow could not be activated.")
        }
    }

    private fun synchronizedSelectionResult(
        input: CommitWorkflowSelectionInput,
        selection: GitChangeSelection,
        changeLists: List<LocalChangeList>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): CommitWorkflowSelectionResult {
        val synchronizationResult = dependencies.selectionSynchronizer.synchronize(
            workflowHandler = input.workflowHandler,
            changeLists = changeLists,
            unversionedFiles = selection.unversionedFiles,
            activeChangeList = activeChangeList,
            inclusionItems = inclusionItems,
        )
        logger.info(
            "AI Commit All diagnostic: commit workflow selection synchronization finished, " +
                "synchronized=${synchronizationResult == CommitWorkflowSynchronizationResult.Synchronized}, " +
                "result=$synchronizationResult, inclusionItems=${inclusionItems.size}",
        )

        return when (synchronizationResult) {
            CommitWorkflowSynchronizationResult.Synchronized ->
                CommitWorkflowSelectionResult.Prepared(selection)

            CommitWorkflowSynchronizationResult.StagingConfirmationFailed ->
                CommitWorkflowSelectionResult.StagingConfirmationFailed

            CommitWorkflowSynchronizationResult.Incompatible ->
                CommitWorkflowSelectionResult.UnsupportedWorkflow(
                    "The active commit workflow does not expose compatible inclusion-state methods.",
                )
        }
    }

    private fun chooseActiveChangeList(changeLists: List<LocalChangeList>): LocalChangeList = changeLists.firstOrNull()
        ?: dependencies.defaultChangeList(project)

    companion object {
        private val logger: Logger = Logger.getInstance(CommitWorkflowSelectionService::class.java)

        fun getInstance(project: Project): CommitWorkflowSelectionService = project.service()
    }
}

internal class CommitWorkflowSelectionDependencies(
    val selectionSource: (Project) -> GitChangeSelectionSource = GitChangeSelectionService::getInstance,
    val changeLists: (Project) -> List<LocalChangeList> = { currentProject ->
        ChangeListManager.getInstance(currentProject).changeLists
    },
    val defaultChangeList: (Project) -> LocalChangeList = { currentProject ->
        ChangeListManager.getInstance(currentProject).defaultChangeList
    },
    val activationRetry: CommitWorkflowActivationRetry = CommitWorkflowActivationRetry.DEFAULT,
    val selectionSynchronizer: CommitWorkflowSelectionSynchronizer =
        ReflectiveCommitWorkflowSelectionSynchronizer,
    val activateWorkflowUi: (CommitWorkflowUi) -> Boolean = { workflowUi ->
        CommitWorkflowUiThreadAccess.run { workflowUi.activate() }
    },
)

internal fun interface CommitWorkflowSelectionSynchronizer {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): CommitWorkflowSynchronizationResult
}

private object ReflectiveCommitWorkflowSelectionSynchronizer : CommitWorkflowSelectionSynchronizer {
    override fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): CommitWorkflowSynchronizationResult = ReflectiveCommitWorkflowSynchronizer.synchronize(
        workflowHandler = workflowHandler,
        changeLists = changeLists,
        unversionedFiles = unversionedFiles,
        activeChangeList = activeChangeList,
        inclusionItems = inclusionItems,
    )
}

internal class CommitWorkflowActivationRetry(
    private val maxAttempts: Int,
    private val retryInterval: Duration,
    private val sleeper: CommitWorkflowActivationSleeper = ThreadCommitWorkflowActivationSleeper,
) {
    init {
        require(maxAttempts > 0) { "Commit workflow activation attempts must be positive." }
        require(!retryInterval.isNegative) { "Commit workflow activation retry interval must not be negative." }
    }

    fun activate(activateNow: () -> Boolean): Boolean {
        repeat(maxAttempts) { attemptIndex ->
            if (activateNow()) {
                return true
            }

            if (attemptIndex < maxAttempts - 1 && !retryInterval.isZero) {
                sleeper.sleep(retryInterval)
            }
        }

        return false
    }

    companion object {
        val DEFAULT: CommitWorkflowActivationRetry = CommitWorkflowActivationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(50),
        )
    }
}

internal fun interface CommitWorkflowActivationSleeper {
    fun sleep(duration: Duration)
}

private object ThreadCommitWorkflowActivationSleeper : CommitWorkflowActivationSleeper {
    override fun sleep(duration: Duration) {
        try {
            Thread.sleep(duration.toMillis())
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

private data class CommitWorkflowSelectionInput(
    val workflowHandler: CommitWorkflowHandler,
    val workflowUi: CommitWorkflowUi,
) {
    companion object {
        fun create(
            workflowHandler: CommitWorkflowHandler?,
            workflowUi: CommitWorkflowUi?,
        ): CommitWorkflowSelectionInput? = if (workflowHandler != null && workflowUi != null) {
            CommitWorkflowSelectionInput(workflowHandler, workflowUi)
        } else {
            null
        }
    }
}

internal object CommitWorkflowUiThreadAccess {
    fun <T> run(action: () -> T): T = ApplicationManager.getApplication()
        ?.takeUnless { application -> application.isDispatchThread }
        ?.let { application -> runOnEdt(application, action) }
        ?: action()

    private fun <T> runOnEdt(
        application: com.intellij.openapi.application.Application,
        action: () -> T,
    ): T {
        val result = AtomicReference<T>()
        val failure = AtomicReference<Throwable>()
        application.invokeAndWait {
            runCatching(action)
                .onSuccess(result::set)
                .onFailure(failure::set)
        }
        failure.get()?.let { throwable -> throw throwable }
        return result.get()
    }
}
