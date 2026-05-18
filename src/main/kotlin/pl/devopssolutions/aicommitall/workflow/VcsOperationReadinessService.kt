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

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsBundle
import com.intellij.openapi.vcs.changes.ChangeListManager
import pl.devopssolutions.aicommitall.notifications.AiCommitAllNotificationService
import pl.devopssolutions.aicommitall.settings.AiCommitAllSettings

@Service(Service.Level.PROJECT)
internal class VcsOperationReadinessService(private val project: Project) {
    private val guard = VcsOperationReadinessGuard(
        state = IntellijVcsOperationState(project),
        reporter = IntellijVcsOperationReadinessReporter(project),
    )

    fun checkAndReport(): VcsOperationReadinessResult = guard.checkAndReport()

    companion object {
        fun getInstance(project: Project): VcsOperationReadinessService = project.service()
    }
}

internal class VcsOperationReadinessGuard(
    private val state: VcsOperationState,
    private val reporter: VcsOperationReadinessReporter,
) {
    fun checkAndReport(): VcsOperationReadinessResult = when {
        state.isFrozenWithNotification() ->
            VcsOperationReadinessResult.Frozen

        state.isBackgroundOperationRunning() -> {
            reporter.notifyBackgroundOperationRunning()
            VcsOperationReadinessResult.BackgroundOperationRunning
        }

        else ->
            VcsOperationReadinessResult.Ready
    }
}

internal interface VcsOperationState {
    fun isFrozenWithNotification(): Boolean

    fun isBackgroundOperationRunning(): Boolean
}

internal fun interface VcsOperationReadinessReporter {
    fun notifyBackgroundOperationRunning()
}

private class IntellijVcsOperationState(project: Project) : VcsOperationState {
    private val changeListManager = ChangeListManager.getInstance(project)
    private val vcsManager = ProjectLevelVcsManager.getInstance(project)

    override fun isFrozenWithNotification(): Boolean = changeListManager.isFreezedWithNotification(null)

    override fun isBackgroundOperationRunning(): Boolean = vcsManager.isBackgroundVcsOperationRunning
}

private class IntellijVcsOperationReadinessReporter(private val project: Project) : VcsOperationReadinessReporter {
    override fun notifyBackgroundOperationRunning() {
        AiCommitAllNotificationService.getInstance(project)
            .notifyWarning(
                title = AiCommitAllSettings.DISPLAY_NAME,
                content = VcsBundle.message("message.text.background.tasks"),
            )
    }
}

internal enum class VcsOperationReadinessResult {
    Ready,
    Frozen,
    BackgroundOperationRunning,
}
