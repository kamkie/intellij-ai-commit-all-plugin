package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class AiCommitMessageActionDiscoveryServiceTest {
    @Test
    fun `prefers the known AI Assistant commit message action id`() {
        val knownAction = TestAction("Known")
        val fallbackAction = TestAction("Generate Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf(
                "Vcs.LLMCommitMessageAction" to knownAction,
                "Vcs.MessageActionGroup" to TestActionGroup(fallbackAction),
            ),
            idsByPrefix = mapOf("Vcs.LLM" to listOf("Vcs.LLMCommitMessageAction")),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(knownAction, result?.action)
        assertEquals("Vcs.LLMCommitMessageAction", result?.actionId)
        assertEquals(AiCommitMessageActionSource.KnownActionId, result?.source)
    }

    @Test
    fun `uses prefixed action ids when the exact known id is unavailable`() {
        val action = TestAction("Generate Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf(
                "Vcs.LLMRewordCommitAction" to TestAction("Reword Commit Message"),
                "Vcs.LLMGenerateCommitMessageAction" to action,
            ),
            idsByPrefix = mapOf(
                "Vcs.LLM" to listOf(
                    "Vcs.LLMRewordCommitAction",
                    "Vcs.LLMGenerateCommitMessageAction",
                ),
            ),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(action, result?.action)
        assertEquals("Vcs.LLMGenerateCommitMessageAction", result?.actionId)
        assertEquals(AiCommitMessageActionSource.ActionIdPrefix, result?.source)
    }

    @Test
    fun `falls back to the VCS message action group presentation text`() {
        val fallbackAction = TestAction("Generate Commit Message")
        val group = TestActionGroup(
            TestAction("Reword Commit Message"),
            TestActionGroup(fallbackAction),
        )
        val lookup = TestActionLookup(actionsById = mapOf("Vcs.MessageActionGroup" to group))

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(fallbackAction, result?.action)
        assertNull(result?.actionId)
        assertEquals(AiCommitMessageActionSource.GroupPresentation, result?.source)
    }

    @Test
    fun `uses matching action ids inside the VCS message action group before presentation text`() {
        val idMatchedAction = TestAction("Unexpected Localized Text")
        val textMatchedAction = TestAction("Generate Commit Message")
        val group = TestActionGroup(idMatchedAction, textMatchedAction)
        val lookup = TestActionLookup(
            actionsById = mapOf("Vcs.MessageActionGroup" to group),
            idsByAction = mapOf(idMatchedAction to "Vcs.LLMGenerateCommitMessageAction"),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(idMatchedAction, result?.action)
        assertEquals("Vcs.LLMGenerateCommitMessageAction", result?.actionId)
        assertEquals(AiCommitMessageActionSource.GroupActionId, result?.source)
    }

    @Test
    fun `does not use the AI reword action as the commit message generator`() {
        val rewordAction = TestAction("Reword Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf("Vcs.MessageActionGroup" to TestActionGroup(rewordAction)),
            idsByAction = mapOf(rewordAction to "Vcs.LLMRewordCommitAction"),
            idsByPrefix = mapOf("Vcs.LLM" to listOf("Vcs.LLMRewordCommitAction")),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertNull(result)
    }

    private class TestAction(text: String) : AnAction(text) {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class TestActionGroup(vararg children: AnAction) : ActionGroup() {
        private val children = children.toList()

        override fun getChildren(event: AnActionEvent?): Array<AnAction> = children.toTypedArray()
    }

    private class TestActionLookup(
        private val actionsById: Map<String, AnAction> = emptyMap(),
        private val idsByAction: Map<AnAction, String> = actionsById.entries.associate { (id, action) -> action to id },
        private val idsByPrefix: Map<String, List<String>> = emptyMap(),
    ) : AiActionLookup {
        override fun getAction(actionId: String): AnAction? = actionsById[actionId]

        override fun getActionIdList(prefix: String): List<String> = idsByPrefix[prefix].orEmpty()

        override fun getId(action: AnAction): String? = idsByAction[action]
    }
}
