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
package pl.devopssolutions.aicommitall.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

internal class AiCommitAllCommitToolbarStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        AiCommitAllCommitToolbarCustomizer.removeStandardCommitAndPushAction()
    }
}

internal object AiCommitAllCommitToolbarCustomizer {
    fun removeStandardCommitAndPushAction(actionManager: ActionManager? = defaultActionManager()) {
        if (actionManager == null) {
            return
        }
        val primaryCommitActions = actionManager.getAction(PRIMARY_COMMIT_ACTIONS_GROUP_ID) as? DefaultActionGroup
            ?: return
        val commitAndPushAction = actionManager.getAction(IDE_COMMIT_AND_PUSH_ACTION_ID)

        primaryCommitActions.getChildren(actionManager)
            .filter { action ->
                action == commitAndPushAction || actionManager.getId(action) == IDE_COMMIT_AND_PUSH_ACTION_ID
            }
            .forEach { action -> primaryCommitActions.remove(action, actionManager) }
    }

    private fun defaultActionManager(): ActionManager? {
        if (ApplicationManager.getApplication() == null) {
            return null
        }
        return ActionManager.getInstance()
    }
}

internal const val PRIMARY_COMMIT_ACTIONS_GROUP_ID: String = "Vcs.Commit.PrimaryCommitActions"
