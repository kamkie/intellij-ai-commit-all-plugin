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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.vcs.commit.CommitWorkflowHandler
import git4idea.index.GitStageCommitWorkflowHandler
import git4idea.index.GitStageTracker
import pl.devopssolutions.aicommitall.vcs.GitStageSelectionItems
import java.lang.reflect.Method
import java.time.Duration

private const val GIT_STAGE_CONFIRMATION_ATTEMPTS = 10

internal object ReflectiveCommitWorkflowSynchronizer {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
        diagnostics: CommitWorkflowCompatibilityDiagnostics = IntelliJCommitWorkflowCompatibilityDiagnostics,
        synchronizationRetry: CommitWorkflowSynchronizationRetry = CommitWorkflowSynchronizationRetry.DEFAULT,
    ): CommitWorkflowSynchronizationResult = synchronizeGitStageWorkflow(workflowHandler, diagnostics)
        ?: synchronizeCommitWorkflow(
            workflowHandler = workflowHandler,
            changeLists = changeLists,
            unversionedFiles = unversionedFiles,
            activeChangeList = activeChangeList,
            inclusionItems = inclusionItems,
            diagnostics = diagnostics,
            synchronizationRetry = synchronizationRetry,
        )

    private fun synchronizeCommitWorkflow(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
        diagnostics: CommitWorkflowCompatibilityDiagnostics,
        synchronizationRetry: CommitWorkflowSynchronizationRetry,
    ): CommitWorkflowSynchronizationResult {
        val synchronized = inclusionItems.isNotEmpty() &&
            workflowHandler.javaClass.commitWorkflowMethods(diagnostics)?.synchronize(
                workflowHandler = workflowHandler,
                changeLists = changeLists,
                unversionedFiles = unversionedFiles,
                activeChangeList = activeChangeList,
                inclusionItems = inclusionItems,
                diagnostics = diagnostics,
                synchronizationRetry = synchronizationRetry,
            ) == true
        return if (synchronized) {
            CommitWorkflowSynchronizationResult.Synchronized
        } else {
            CommitWorkflowSynchronizationResult.Incompatible
        }
    }

    private fun Class<*>.commitWorkflowMethods(
        diagnostics: CommitWorkflowCompatibilityDiagnostics,
    ): CommitWorkflowMethods? {
        val synchronizeInclusion = findMethod("synchronizeInclusion", List::class.java, List::class.java)
        val setCommitState = findMethod(
            "setCommitState",
            LocalChangeList::class.java,
            Collection::class.java,
            java.lang.Boolean.TYPE,
        )
        if (synchronizeInclusion != null && setCommitState != null) {
            return CommitWorkflowMethods(synchronizeInclusion, setCommitState)
        }

        val missingMethods = listOfNotNull(
            "synchronizeInclusion".takeIf { synchronizeInclusion == null },
            "setCommitState".takeIf { setCommitState == null },
        )
        diagnostics.report(
            CommitWorkflowCompatibilityDiagnostic(
                sourceClassName = name,
                methodName = "commitWorkflowMethods",
                reason = "required methods missing",
                missingMethodNames = missingMethods,
            ),
        )
        return null
    }

    private fun synchronizeGitStageWorkflow(
        workflowHandler: CommitWorkflowHandler,
        diagnostics: CommitWorkflowCompatibilityDiagnostics,
    ): CommitWorkflowSynchronizationResult? {
        val gitStageHandler = workflowHandler as? GitStageCommitWorkflowHandler ?: return null

        return runCatching {
            val project = gitStageHandler.workflow.project
            val tracker = GitStageTracker.getInstance(project)
            tracker.updateTrackerState()
            val currentState = tracker.state
            val expectedPathsByRoot = GitStageSelectionItems.committablePathsByRoot(currentState)
            if (expectedPathsByRoot.isEmpty()) {
                return@runCatching CommitWorkflowSynchronizationResult.Incompatible
            }
            val pathsToStageByRoot = GitStageSelectionItems.pathsToStageByRoot(currentState)

            val refreshedState = confirmStagedState(
                project = project,
                tracker = tracker,
                pathsByRoot = pathsToStageByRoot,
                expectedPathsByRoot = expectedPathsByRoot,
                expectedPaths = expectedPathsByRoot.values.flatten(),
            ) ?: run {
                diagnostics.report(
                    CommitWorkflowCompatibilityDiagnostic(
                        sourceClassName = gitStageHandler.javaClass.name,
                        methodName = "synchronizeGitStageWorkflow",
                        reason = "staging state confirmation failed",
                    ),
                )
                return@runCatching CommitWorkflowSynchronizationResult.StagingConfirmationFailed
            }
            val includedRoots = expectedPathsByRoot.keys
            CommitWorkflowUiThreadAccess.run {
                gitStageHandler.state = refreshedState
                gitStageHandler.ui.setTrackerState(refreshedState)
                gitStageHandler.ui.setIncludedRoots(includedRoots)
            }
            CommitWorkflowSynchronizationResult.Synchronized
        }.getOrElse { exception ->
            diagnostics.report(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = gitStageHandler.javaClass.name,
                    methodName = "synchronizeGitStageWorkflow",
                    reason = "git stage workflow synchronization failed",
                    exceptionClassName = exception.javaClass.name,
                    causeClassName = exception.cause?.javaClass?.name,
                ),
            )
            CommitWorkflowSynchronizationResult.Incompatible
        }
    }

    private fun confirmStagedState(
        project: Project,
        tracker: GitStageTracker,
        pathsByRoot: Map<VirtualFile, List<FilePath>>,
        expectedPathsByRoot: Map<VirtualFile, List<FilePath>>,
        expectedPaths: Collection<FilePath>,
    ): GitStageTracker.State? = GitStageConfirmation(
        attempts = GIT_STAGE_CONFIRMATION_ATTEMPTS,
        operations = IntellijGitStageConfirmationOperations(project, tracker),
    ).confirm(
        pathsByRoot = pathsByRoot,
        expectedPaths = expectedPaths,
        expectedPathsByRoot = expectedPathsByRoot,
    )

    private fun Class<*>.findMethod(
        name: String,
        vararg parameterTypes: Class<*>,
    ): Method? = methods.firstOrNull { method ->
        method.name == name &&
            method.parameterTypes.contentEquals(parameterTypes)
    }
}

internal sealed interface CommitWorkflowSynchronizationResult {
    data object Synchronized : CommitWorkflowSynchronizationResult

    data object StagingConfirmationFailed : CommitWorkflowSynchronizationResult

    data object Incompatible : CommitWorkflowSynchronizationResult
}

private data class CommitWorkflowMethods(
    val synchronizeInclusion: Method,
    val setCommitState: Method,
) {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
        diagnostics: CommitWorkflowCompatibilityDiagnostics,
        synchronizationRetry: CommitWorkflowSynchronizationRetry,
    ): Boolean {
        val failure = synchronizationRetry.run {
            CommitWorkflowUiThreadAccess.run {
                synchronizeInclusion.invoke(workflowHandler, changeLists, unversionedFiles)
                setCommitState.invoke(workflowHandler, activeChangeList, inclusionItems, true)
            }
        }
        if (failure != null) {
            diagnostics.report(
                CommitWorkflowCompatibilityDiagnostic(
                    sourceClassName = workflowHandler.javaClass.name,
                    methodName = "synchronize",
                    reason = "method invocation failed",
                    exceptionClassName = failure.javaClass.name,
                    causeClassName = failure.cause?.javaClass?.name,
                ),
            )
        }
        return failure == null
    }
}

internal class CommitWorkflowSynchronizationRetry(
    private val maxAttempts: Int,
    private val retryInterval: Duration,
    private val sleeper: CommitWorkflowSynchronizationSleeper = ThreadCommitWorkflowSynchronizationSleeper,
) {
    init {
        require(maxAttempts > 0) { "Commit workflow synchronization attempts must be positive." }
        require(!retryInterval.isNegative) { "Commit workflow synchronization retry interval must not be negative." }
    }

    fun run(action: () -> Unit): Throwable? {
        var lastFailure: Throwable? = null
        repeat(maxAttempts) { attemptIndex ->
            val failure = runCatching(action).exceptionOrNull()
            if (failure == null) {
                return null
            }

            lastFailure = failure
            if (attemptIndex < maxAttempts - 1 && !retryInterval.isZero) {
                sleeper.sleep(retryInterval)
            }
        }
        return lastFailure
    }

    companion object {
        val DEFAULT: CommitWorkflowSynchronizationRetry = CommitWorkflowSynchronizationRetry(
            maxAttempts = 3,
            retryInterval = Duration.ofMillis(50),
        )
    }
}

internal fun interface CommitWorkflowSynchronizationSleeper {
    fun sleep(duration: Duration)
}

private object ThreadCommitWorkflowSynchronizationSleeper : CommitWorkflowSynchronizationSleeper {
    override fun sleep(duration: Duration) {
        try {
            Thread.sleep(duration.toMillis())
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}

internal fun interface CommitWorkflowCompatibilityDiagnostics {
    fun report(diagnostic: CommitWorkflowCompatibilityDiagnostic)
}

internal data class CommitWorkflowCompatibilityDiagnostic(
    val sourceClassName: String,
    val methodName: String,
    val reason: String,
    val missingMethodNames: List<String> = emptyList(),
    val exceptionClassName: String? = null,
    val causeClassName: String? = null,
)

private object IntelliJCommitWorkflowCompatibilityDiagnostics : CommitWorkflowCompatibilityDiagnostics {
    private val logger = Logger.getInstance(ReflectiveCommitWorkflowSynchronizer::class.java)

    override fun report(diagnostic: CommitWorkflowCompatibilityDiagnostic) {
        logger.warn(diagnostic.toLogMessage())
    }

    private fun CommitWorkflowCompatibilityDiagnostic.toLogMessage(): String = buildString {
        append("Commit workflow compatibility diagnostic: ")
        append("class=").append(sourceClassName)
        append(", method=").append(methodName)
        append(", reason=").append(reason)
        if (missingMethodNames.isNotEmpty()) {
            append(", missingMethods=").append(missingMethodNames.joinToString(","))
        }
        exceptionClassName?.let { exceptionClass ->
            append(", exception=").append(exceptionClass)
        }
        causeClassName?.let { causeClass ->
            append(", cause=").append(causeClass)
        }
    }
}
