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

import com.intellij.openapi.util.Disposer
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitterResultHandler

internal fun interface CommitWorkflowResultRegistrar {
    fun register(
        workflowHandler: CommitWorkflowHandler,
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration?
}

internal interface CommitWorkflowResultHandler {
    val waitForAfterRefreshOnSuccess: Boolean
        get() = false

    fun onSuccess()

    fun onCancel()

    fun onFailure()

    fun onAfterRefresh() = Unit
}

internal fun interface CommitWorkflowResultRegistration {
    fun dispose()
}

internal object IntellijCommitWorkflowResultRegistrar : CommitWorkflowResultRegistrar {
    override fun register(
        workflowHandler: CommitWorkflowHandler,
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration? = (workflowHandler as? AbstractCommitWorkflowHandler<*, *>)
        ?.registerResultHandler(resultHandler)

    private fun AbstractCommitWorkflowHandler<*, *>.registerResultHandler(
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration? {
        val listenerDisposable = Disposer.newDisposable("AI Commit All commit result")
        return if (Disposer.tryRegister(this, listenerDisposable)) {
            registerDisposableResultHandler(listenerDisposable, resultHandler)
        } else {
            Disposer.dispose(listenerDisposable)
            null
        }
    }

    private fun AbstractCommitWorkflowHandler<*, *>.registerDisposableResultHandler(
        listenerDisposable: com.intellij.openapi.Disposable,
        resultHandler: CommitWorkflowResultHandler,
    ): CommitWorkflowResultRegistration {
        val registration = DisposableCommitWorkflowResultRegistration {
            Disposer.dispose(listenerDisposable)
        }
        workflow.addVcsCommitListener(
            DisposableCommitWorkflowResultListener(
                resultHandler = resultHandler,
                disposeListener = registration::dispose,
            ),
            listenerDisposable,
        )
        return registration
    }
}

internal class DisposableCommitWorkflowResultRegistration(
    private val disposeListener: () -> Unit,
) : CommitWorkflowResultRegistration {
    private var disposed = false

    override fun dispose() {
        if (!disposed) {
            disposed = true
            disposeListener()
        }
    }
}

internal class DisposableCommitWorkflowResultListener(
    private val resultHandler: CommitWorkflowResultHandler,
    private val disposeListener: () -> Unit,
) : CommitterResultHandler {
    private var successPendingAfterRefresh = false

    override fun onSuccess() {
        if (!resultHandler.waitForAfterRefreshOnSuccess) {
            disposeListener()
        }
        successPendingAfterRefresh = resultHandler.waitForAfterRefreshOnSuccess
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

    override fun onAfterRefresh() {
        if (successPendingAfterRefresh) {
            disposeListener()
            resultHandler.onAfterRefresh()
        }
    }
}
