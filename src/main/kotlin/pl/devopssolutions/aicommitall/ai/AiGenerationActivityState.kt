package pl.devopssolutions.aicommitall.ai

import com.intellij.ide.ActivityTracker
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Icon

@Service(Service.Level.PROJECT)
internal class AiGenerationActivityStateService {
    private var actionRefresh: AiGenerationActivityActionRefresh = IntellijAiGenerationActivityActionRefresh
    private val running = AtomicBoolean(false)
    private val runningPhase = AtomicReference<AiGenerationActivityPhase?>()

    fun start(phase: AiGenerationActivityPhase = AiGenerationActivityPhase.Ai): AiGenerationActivityToken {
        running.set(true)
        runningPhase.set(phase)
        actionRefresh.refreshActions()
        return AiGenerationActivityToken(this)
    }

    fun moveTo(phase: AiGenerationActivityPhase) {
        if (running.get()) {
            runningPhase.set(phase)
            actionRefresh.refreshActions()
        }
    }

    fun finish() {
        running.set(false)
        runningPhase.set(null)
        actionRefresh.refreshActions()
    }

    fun isRunning(): Boolean = running.get()

    fun runningPhase(): AiGenerationActivityPhase? = runningPhase.get()

    internal fun replaceActionRefreshForTest(actionRefresh: AiGenerationActivityActionRefresh) {
        this.actionRefresh = actionRefresh
    }

    fun applyToPresentation(
        presentation: Presentation,
        idleIcon: Icon?,
        enabledWhenIdle: Boolean,
    ) {
        if (isRunning()) {
            presentation.setEnabled(false)
            presentation.setIcon(AnimatedIcon.Default.INSTANCE)
        } else {
            presentation.setEnabled(enabledWhenIdle)
            presentation.setIcon(idleIcon)
        }
    }

    companion object {
        fun getInstance(project: Project): AiGenerationActivityStateService = project.service()
    }
}

internal fun interface AiGenerationActivityActionRefresh {
    fun refreshActions()
}

private object IntellijAiGenerationActivityActionRefresh : AiGenerationActivityActionRefresh {
    override fun refreshActions() {
        ActivityTracker.getInstance().inc()
    }
}

internal enum class AiGenerationActivityPhase {
    Ai,
    Commit,
    Push,
}

internal class AiGenerationActivityToken internal constructor(
    private val service: AiGenerationActivityStateService,
) : AutoCloseable {
    private val closed = AtomicBoolean(false)

    fun moveTo(phase: AiGenerationActivityPhase) {
        if (!closed.get()) {
            service.moveTo(phase)
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            service.finish()
        }
    }
}
