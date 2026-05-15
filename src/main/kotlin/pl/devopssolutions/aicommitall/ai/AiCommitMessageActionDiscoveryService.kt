package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import java.util.Locale

@Service(Service.Level.APP)
internal class AiCommitMessageActionDiscoveryService {
    private val discovery = AiCommitMessageActionDiscovery(IntellijAiActionLookup)

    fun findCommitMessageAction(event: AnActionEvent? = null): AiCommitMessageActionReference? =
        discovery.findCommitMessageAction(event)

    companion object {
        fun getInstance(): AiCommitMessageActionDiscoveryService = service()
    }
}

internal class AiCommitMessageActionDiscovery(
    private val actionLookup: AiActionLookup,
) {
    fun findCommitMessageAction(event: AnActionEvent? = null): AiCommitMessageActionReference? =
        findKnownAction()
            ?: findPrefixedAction()
            ?: findGroupedAction(event)

    private fun findKnownAction(): AiCommitMessageActionReference? =
        knownActionIds.firstNotNullOfOrNull { actionId ->
            actionLookup.getAction(actionId)?.let { action ->
                AiCommitMessageActionReference(
                    action = action,
                    actionId = actionId,
                    source = AiCommitMessageActionSource.KnownActionId,
                )
            }
        }

    private fun findPrefixedAction(): AiCommitMessageActionReference? =
        actionIdPrefixes.asSequence()
            .flatMap { prefix -> actionLookup.getActionIdList(prefix).asSequence() }
            .filter(::looksLikeCommitMessageActionId)
            .distinct()
            .mapNotNull { actionId ->
                actionLookup.getAction(actionId)?.let { action ->
                    AiCommitMessageActionReference(
                        action = action,
                        actionId = actionId,
                        source = AiCommitMessageActionSource.ActionIdPrefix,
                    )
                }
            }
            .firstOrNull()

    private fun findGroupedAction(event: AnActionEvent?): AiCommitMessageActionReference? =
        fallbackGroupIds.asSequence()
            .mapNotNull { groupId -> actionLookup.getAction(groupId) as? ActionGroup }
            .flatMap { group -> group.childrenRecursive(event).asSequence() }
            .mapNotNull { action -> action.toPresentationFallbackReference() }
            .firstOrNull()

    private fun ActionGroup.childrenRecursive(event: AnActionEvent?): List<AnAction> {
        val visited = mutableSetOf<AnAction>()
        val result = mutableListOf<AnAction>()

        fun visit(action: AnAction) {
            if (!visited.add(action)) {
                return
            }

            if (action is ActionGroup) {
                action.getChildrenOrEmpty(event).forEach(::visit)
            } else {
                result.add(action)
            }
        }

        getChildrenOrEmpty(event).forEach(::visit)
        return result
    }

    private fun ActionGroup.getChildrenOrEmpty(event: AnActionEvent?): Array<AnAction> =
        runCatching { getChildren(event) }.getOrDefault(emptyArray())

    private fun AnAction.toPresentationFallbackReference(): AiCommitMessageActionReference? {
        val actionId = actionLookup.getId(this)
        if (actionId != null && looksLikeCommitMessageActionId(actionId)) {
            return AiCommitMessageActionReference(
                action = this,
                actionId = actionId,
                source = AiCommitMessageActionSource.GroupActionId,
            )
        }

        val presentationText = listOfNotNull(templatePresentation.text, templatePresentation.description)
            .joinToString(separator = " ")
        if (!looksLikeCommitMessagePresentation(presentationText)) {
            return null
        }

        return AiCommitMessageActionReference(
            action = this,
            actionId = actionId,
            source = AiCommitMessageActionSource.GroupPresentation,
        )
    }

    companion object {
        private val knownActionIds = listOf("Vcs.LLMCommitMessageAction")
        private val actionIdPrefixes = listOf("Vcs.LLM")
        private val fallbackGroupIds = listOf("Vcs.MessageActionGroup")
        private val rejectedFallbackTerms = listOf("reword", "rewrite", "resolve conflict", "conflict")
        private val generationFallbackTerms = listOf("generate", "write", "create", "suggest", "ai", "assistant", "llm")

        private fun looksLikeCommitMessageActionId(actionId: String): Boolean {
            val normalized = actionId.lowercase(Locale.ROOT)
            return "reword" !in normalized &&
                "commitmessage" in normalized &&
                ("llm" in normalized || "ai" in normalized)
        }

        private fun looksLikeCommitMessagePresentation(text: String): Boolean {
            val normalized = text.lowercase(Locale.ROOT)
            return "commit" in normalized &&
                "message" in normalized &&
                rejectedFallbackTerms.none { rejectedTerm -> rejectedTerm in normalized } &&
                generationFallbackTerms.any { generationTerm -> generationTerm in normalized }
        }
    }
}

internal data class AiCommitMessageActionReference(
    val action: AnAction,
    val actionId: String?,
    val source: AiCommitMessageActionSource,
)

internal enum class AiCommitMessageActionSource {
    KnownActionId,
    ActionIdPrefix,
    GroupActionId,
    GroupPresentation,
}

internal interface AiActionLookup {
    fun getAction(actionId: String): AnAction?

    fun getActionIdList(prefix: String): List<String>

    fun getId(action: AnAction): String?
}

private object IntellijAiActionLookup : AiActionLookup {
    override fun getAction(actionId: String): AnAction? =
        ActionManager.getInstance().getAction(actionId)

    override fun getActionIdList(prefix: String): List<String> =
        ActionManager.getInstance().getActionIdList(prefix).toList()

    override fun getId(action: AnAction): String? =
        ActionManager.getInstance().getId(action)
}
