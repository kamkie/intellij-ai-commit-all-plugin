package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.util.Disposer
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowHandler

internal fun interface CommitWorkflowResultRegistrar {
    fun register(
        workflowHandler: CommitWorkflowHandler,
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration?
}

internal interface CommitWorkflowResultHandler {
    fun onSuccess()

    fun onCancel()

    fun onFailure()
}

internal fun interface CommitWorkflowResultRegistration {
    fun dispose()
}

internal object IntellijCommitWorkflowResultRegistrar : CommitWorkflowResultRegistrar {
    override fun register(
        workflowHandler: CommitWorkflowHandler,
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration? {
        val abstractHandler = workflowHandler as? AbstractCommitWorkflowHandler<*, *>
            ?: return null
        val listenerDisposable = Disposer.newDisposable("AI Commit All commit result")
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
            object : com.intellij.vcs.commit.CommitterResultHandler {
                override fun onSuccess() {
                    disposeListener()
                    resultHandler.onSuccess()
                }

                override fun onCancel() {
                    disposeListener()
                    resultHandler.onCancel()
                }

                override fun onFailure() {
                    disposeListener()
                    resultHandler.onFailure()
                }
            },
            listenerDisposable,
        )
        return CommitWorkflowResultRegistration(::disposeListener)
    }
}
