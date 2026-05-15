package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CommitMessageI
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.CommitMessageUi
import com.intellij.vcs.commit.CommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import java.awt.event.InputEvent
import java.lang.reflect.Method

internal object AiCommitMessageActionInvocationContextFactory {
    fun createInvocationContext(
        actionReference: AiCommitMessageActionReference,
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
        inputEvent: InputEvent? = null,
    ): AiCommitMessageActionInvocationContext {
        val dataContext = createDataContext(
            project = project,
            workflowHandler = workflowHandler,
            workflowUi = workflowUi,
            parentDataContext = parentDataContext,
        )
        val presentation = actionReference.action.templatePresentation.clone()
        val event = AnActionEvent.createEvent(
            actionReference.action,
            dataContext,
            presentation,
            ActionPlaces.CHANGES_VIEW_TOOLBAR,
            ActionUiKind.NONE,
            inputEvent,
        )

        return AiCommitMessageActionInvocationContext(
            dataContext = dataContext,
            event = event,
        )
    }

    internal fun createDataContext(
        project: Project,
        workflowHandler: CommitWorkflowHandler,
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext = DataContext.EMPTY_CONTEXT,
    ): DataContext {
        val data = linkedMapOf<String, Any>()
        data.put(CommonDataKeys.PROJECT, project)
        data.put(VcsDataKeys.COMMIT_WORKFLOW_HANDLER, workflowHandler)
        data.put(VcsDataKeys.COMMIT_WORKFLOW_UI, workflowUi)

        resolveCommitMessageControl(workflowUi, parentDataContext)?.let { commitMessageControl ->
            data.put(VcsDataKeys.COMMIT_MESSAGE_CONTROL, commitMessageControl)
        }
        resolveCommitMessageDocument(workflowUi, parentDataContext)?.let { commitMessageDocument ->
            data.put(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT, commitMessageDocument)
        }

        return LayeredDataContext(
            data = data,
            parentDataContext = parentDataContext,
        )
    }

    private fun <T : Any> MutableMap<String, Any>.put(key: DataKey<T>, value: T) {
        put(key.name, value)
    }

    private fun resolveCommitMessageControl(
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
    ): CommitMessageI? =
        workflowUi.commitMessageUi as? CommitMessageI
            ?: parentDataContext.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL)

    private fun resolveCommitMessageDocument(
        workflowUi: CommitWorkflowUi,
        parentDataContext: DataContext,
    ): Document? =
        workflowUi.commitMessageUi.findEditorDocument()
            ?: parentDataContext.getData(VcsDataKeys.COMMIT_MESSAGE_DOCUMENT)

    private fun CommitMessageUi.findEditorDocument(): Document? =
        runCatching {
            val editorField = javaClass.findMethod("getEditorField")?.invoke(this)
            editorField?.javaClass?.findMethod("getDocument")?.invoke(editorField) as? Document
        }.getOrNull()

    private fun Class<*>.findMethod(name: String): Method? =
        methods.firstOrNull { method ->
            method.name == name && method.parameterCount == 0
        }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    private class LayeredDataContext(
        private val data: Map<String, Any>,
        private val parentDataContext: DataContext,
    ) : DataContext {
        override fun getData(dataId: String): Any? =
            data[dataId] ?: parentDataContext.getData(dataId)
    }
}

internal data class AiCommitMessageActionInvocationContext(
    val dataContext: DataContext,
    val event: AnActionEvent,
)
