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

    fun checkAndReport(): VcsOperationReadinessResult =
        guard.checkAndReport()

    companion object {
        fun getInstance(project: Project): VcsOperationReadinessService = project.service()
    }
}

internal class VcsOperationReadinessGuard(
    private val state: VcsOperationState,
    private val reporter: VcsOperationReadinessReporter,
) {
    fun checkAndReport(): VcsOperationReadinessResult =
        when {
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

    override fun isFrozenWithNotification(): Boolean =
        changeListManager.isFreezedWithNotification(null)

    override fun isBackgroundOperationRunning(): Boolean =
        vcsManager.isBackgroundVcsOperationRunning
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
