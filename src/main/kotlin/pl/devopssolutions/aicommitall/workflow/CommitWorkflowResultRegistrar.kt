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

        var successPendingAfterRefresh = false
        abstractHandler.workflow.addVcsCommitListener(
            object : com.intellij.vcs.commit.CommitterResultHandler {
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
            },
            listenerDisposable,
        )
        return CommitWorkflowResultRegistration(::disposeListener)
    }
}
