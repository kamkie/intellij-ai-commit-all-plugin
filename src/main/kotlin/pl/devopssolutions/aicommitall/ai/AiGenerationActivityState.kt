package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.AnimatedIcon
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.Icon

@Service(Service.Level.PROJECT)
internal class AiGenerationActivityStateService {
    private val running = AtomicBoolean(false)

    fun start(): AiGenerationActivityToken {
        running.set(true)
        return AiGenerationActivityToken(this)
    }

    fun finish() {
        running.set(false)
    }

    fun isRunning(): Boolean = running.get()

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

internal class AiGenerationActivityToken internal constructor(
    private val service: AiGenerationActivityStateService,
) : AutoCloseable {
    private val closed = AtomicBoolean(false)

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            service.finish()
        }
    }
}
