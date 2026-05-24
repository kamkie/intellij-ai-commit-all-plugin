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
package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import java.util.*

@Service(Service.Level.APP)
internal class AiCommitMessageActionDiscoveryService : AiCommitMessageActionFinder {
    private val discovery = AiCommitMessageActionDiscovery(IntellijAiActionLookup)

    override fun findCommitMessageAction(event: AnActionEvent?): AiCommitMessageActionReference? = discovery.findCommitMessageAction(event)

    companion object {
        fun getInstance(): AiCommitMessageActionDiscoveryService = service()
    }
}

internal interface AiCommitMessageActionFinder {
    fun findCommitMessageAction(event: AnActionEvent? = null): AiCommitMessageActionReference?
}

internal class AiCommitMessageActionDiscovery(
    private val actionLookup: AiActionLookup,
) : AiCommitMessageActionFinder {
    override fun findCommitMessageAction(event: AnActionEvent?): AiCommitMessageActionReference? = findKnownAction()
        ?: findPrefixedAction()
        ?: findPresentationAction()

    private fun findKnownAction(): AiCommitMessageActionReference? = knownActionIds.firstNotNullOfOrNull { actionId ->
        actionLookup.getAction(actionId)?.let { action ->
            AiCommitMessageActionReference(
                action = action,
                actionId = actionId,
                source = AiCommitMessageActionSource.KnownActionId,
            )
        }
    }

    private fun findPrefixedAction(): AiCommitMessageActionReference? = actionIdPrefixes.asSequence()
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

    private fun findPresentationAction(): AiCommitMessageActionReference? = presentationFallbackActionIdPrefixes.asSequence()
        .flatMap { prefix -> actionLookup.getActionIdList(prefix).asSequence() }
        .distinct()
        .mapNotNull { actionId ->
            actionLookup.getAction(actionId)?.toPresentationFallbackReference(actionId)
        }
        .firstOrNull()

    private fun AnAction.toPresentationFallbackReference(actionId: String): AiCommitMessageActionReference? {
        if (looksLikeCommitMessageActionId(actionId)) {
            return AiCommitMessageActionReference(
                action = this,
                actionId = actionId,
                source = AiCommitMessageActionSource.PresentationActionId,
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
            source = AiCommitMessageActionSource.PresentationText,
        )
    }

    companion object {
        private val knownActionIds = listOf("Vcs.LLMCommitMessageAction")
        private val actionIdPrefixes = listOf("Vcs.LLM")
        private val presentationFallbackActionIdPrefixes = listOf("Vcs.")
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
    PresentationActionId,
    PresentationText,
}

internal interface AiActionLookup {
    fun getAction(actionId: String): AnAction?

    fun getActionIdList(prefix: String): List<String>
}

private object IntellijAiActionLookup : AiActionLookup {
    override fun getAction(actionId: String): AnAction? = ActionManager.getInstance().getAction(actionId)

    override fun getActionIdList(prefix: String): List<String> = ActionManager.getInstance().getActionIdList(prefix).toList()
}
