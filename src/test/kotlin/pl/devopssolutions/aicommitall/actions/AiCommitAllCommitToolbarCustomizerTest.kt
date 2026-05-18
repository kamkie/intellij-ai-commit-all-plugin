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

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionPopupMenu
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.TimerListener
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.ActionCallback
import java.awt.Component
import java.awt.event.InputEvent
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertSame

internal class AiCommitAllCommitToolbarCustomizerTest {
    @Test
    fun `customizer removes standard commit and push action from primary commit toolbar group`() {
        val commitAndPushAction = TestAction()
        val pluginAction = TestAction()
        val group = DefaultActionGroup(commitAndPushAction, pluginAction)
        val actionManager = TestActionManager(
            actions = mapOf(
                PRIMARY_COMMIT_ACTIONS_GROUP_ID to group,
                IDE_COMMIT_AND_PUSH_ACTION_ID to commitAndPushAction,
                AI_COMMIT_ALL_THREE_SECTION_ACTION_ID to pluginAction,
            ),
        )

        AiCommitAllCommitToolbarCustomizer.removeStandardCommitAndPushAction(actionManager)

        val children = group.getChildren(actionManager).toList()
        assertFalse(commitAndPushAction in children)
        assertContains(children, pluginAction)
        assertSame(commitAndPushAction, actionManager.getAction(IDE_COMMIT_AND_PUSH_ACTION_ID))
    }

    private class TestAction : AnAction() {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    @Suppress("OVERRIDE_DEPRECATION")
    private class TestActionManager(
        private val actions: Map<String, AnAction>,
    ) : com.intellij.openapi.actionSystem.ActionManager() {
        private val ids = actions.entries.associate { (id, action) -> action to id }

        override fun createActionPopupMenu(
            place: String,
            group: ActionGroup,
        ): ActionPopupMenu = error("Not needed for toolbar customizer tests.")

        override fun createActionToolbar(
            place: String,
            group: ActionGroup,
            horizontal: Boolean,
        ): ActionToolbar = error("Not needed for toolbar customizer tests.")

        override fun getAction(actionId: String): AnAction? = actions[actionId]

        override fun getId(action: AnAction): String? = ids[action]

        override fun registerAction(
            actionId: String,
            action: AnAction,
        ) = Unit

        override fun registerAction(
            actionId: String,
            action: AnAction,
            pluginId: PluginId?,
        ) = Unit

        override fun unregisterAction(actionId: String) = Unit

        override fun replaceAction(
            actionId: String,
            newAction: AnAction,
        ) = Unit

        override fun getActionIds(idPrefix: String): Array<String> = emptyArray()

        override fun getActionIdList(idPrefix: String): MutableList<String> = mutableListOf()

        override fun isGroup(actionId: String): Boolean = actions[actionId] is ActionGroup

        override fun getActionOrStub(actionId: String): AnAction? = actions[actionId]

        override fun addTimerListener(listener: TimerListener) = Unit

        override fun removeTimerListener(listener: TimerListener) = Unit

        override fun tryToExecute(
            action: AnAction,
            inputEvent: InputEvent?,
            contextComponent: Component?,
            place: String?,
            now: Boolean,
        ): ActionCallback = ActionCallback.DONE

        override fun getKeyboardShortcut(actionId: String): KeyboardShortcut? = null
    }
}
