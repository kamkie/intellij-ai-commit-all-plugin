package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.util.Disposer
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.CommitterResultHandler
import com.intellij.vcs.commit.CommitWorkflowHandler
import pl.devopssolutions.aicommitall.vcs.SafeImmediatePushPlan

internal fun interface PostCommitPushRegistrar {
    fun register(
        workflowHandler: CommitWorkflowHandler,
        pushPlan: SafeImmediatePushPlan,
    ): PostCommitPushRegistration?
}

internal fun interface PostCommitPushRegistration {
    fun dispose()
}

internal object IntellijPostCommitPushRegistrar : PostCommitPushRegistrar {
    override fun register(
        workflowHandler: CommitWorkflowHandler,
        pushPlan: SafeImmediatePushPlan,
    ): PostCommitPushRegistration? {
        val abstractHandler = workflowHandler as? AbstractCommitWorkflowHandler<*, *>
            ?: return null
        val listenerDisposable = Disposer.newDisposable("AI Commit All immediate push")
        if (!Disposer.tryRegister(abstractHandler, listenerDisposable)) {
            Disposer.dispose(listenerDisposable)
            return null
        }
        var listenerDisposed = false

        fun disposeListener() {
            if (!listenerDisposed) {
                listenerDisposed = true
                Disposer.dispose(listenerDisposable)
            }
        }

        abstractHandler.workflow.addVcsCommitListener(
            object : CommitterResultHandler {
                override fun onSuccess() {
                    disposeListener()
                    pushPlan.push()
                }

                override fun onCancel() {
                    disposeListener()
                }

                override fun onFailure() {
                    disposeListener()
                }
            },
            listenerDisposable,
        )
        return PostCommitPushRegistration(::disposeListener)
    }
}
