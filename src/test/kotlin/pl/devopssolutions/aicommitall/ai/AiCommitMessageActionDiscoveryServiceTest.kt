package pl.devopssolutions.aicommitall.ai

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
                "Vcs.GeneratedCommitMessageFallback" to fallbackAction,
            ),
            idsByPrefix = mapOf(
                "Vcs.LLM" to listOf("Vcs.LLMCommitMessageAction"),
                "Vcs." to listOf("Vcs.GeneratedCommitMessageFallback"),
            ),
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
    fun `falls back to registered VCS action presentation text`() {
        val fallbackAction = TestAction("Generate Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf(
                "Vcs.LLMRewordCommitAction" to TestAction("Reword Commit Message"),
                "Vcs.GeneratedCommitMessageFallback" to fallbackAction,
            ),
            idsByPrefix = mapOf(
                "Vcs." to listOf(
                    "Vcs.LLMRewordCommitAction",
                    "Vcs.GeneratedCommitMessageFallback",
                ),
            ),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(fallbackAction, result?.action)
        assertEquals("Vcs.GeneratedCommitMessageFallback", result?.actionId)
        assertEquals(AiCommitMessageActionSource.PresentationText, result?.source)
    }

    @Test
    fun `falls back to AI Assistant presentation text variants`() {
        val fallbackAction = TestAction("AI Assistant: Write Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf(
                "Vcs.AiAssistantCommitText" to fallbackAction,
            ),
            idsByPrefix = mapOf(
                "Vcs." to listOf("Vcs.AiAssistantCommitText"),
            ),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(fallbackAction, result?.action)
        assertEquals("Vcs.AiAssistantCommitText", result?.actionId)
        assertEquals(AiCommitMessageActionSource.PresentationText, result?.source)
    }

    @Test
    fun `uses matching action ids in presentation search before presentation text`() {
        val idMatchedAction = TestAction("Unexpected Localized Text")
        val textMatchedAction = TestAction("Generate Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf(
                "Vcs.LLMGenerateCommitMessageAction" to idMatchedAction,
                "Vcs.GeneratedCommitMessageFallback" to textMatchedAction,
            ),
            idsByPrefix = mapOf(
                "Vcs." to listOf(
                    "Vcs.LLMGenerateCommitMessageAction",
                    "Vcs.GeneratedCommitMessageFallback",
                ),
            ),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertSame(idMatchedAction, result?.action)
        assertEquals("Vcs.LLMGenerateCommitMessageAction", result?.actionId)
        assertEquals(AiCommitMessageActionSource.PresentationActionId, result?.source)
    }

    @Test
    fun `does not use the AI reword action as the commit message generator`() {
        val rewordAction = TestAction("Reword Commit Message")
        val lookup = TestActionLookup(
            actionsById = mapOf("Vcs.LLMRewordCommitAction" to rewordAction),
            idsByPrefix = mapOf(
                "Vcs.LLM" to listOf("Vcs.LLMRewordCommitAction"),
                "Vcs." to listOf("Vcs.LLMRewordCommitAction"),
            ),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertNull(result)
    }

    @Test
    fun `does not use conflict resolution presentation as the commit message generator`() {
        val conflictAction = TestAction("Generate Commit Message For Resolve Conflict")
        val lookup = TestActionLookup(
            actionsById = mapOf("Vcs.GenerateConflictCommitMessage" to conflictAction),
            idsByPrefix = mapOf(
                "Vcs." to listOf("Vcs.GenerateConflictCommitMessage"),
            ),
        )

        val result = AiCommitMessageActionDiscovery(lookup).findCommitMessageAction()

        assertNull(result)
    }

    private class TestAction(text: String) : AnAction(text) {
        override fun actionPerformed(event: AnActionEvent) = Unit
    }

    private class TestActionLookup(
        private val actionsById: Map<String, AnAction> = emptyMap(),
        private val idsByPrefix: Map<String, List<String>> = emptyMap(),
    ) : AiActionLookup {
        override fun getAction(actionId: String): AnAction? = actionsById[actionId]

        override fun getActionIdList(prefix: String): List<String> = idsByPrefix[prefix].orEmpty()
    }
}
