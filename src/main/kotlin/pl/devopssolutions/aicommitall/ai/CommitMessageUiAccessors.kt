package pl.devopssolutions.aicommitall.ai

import com.intellij.openapi.editor.Document
import com.intellij.ui.EditorTextComponent
import com.intellij.vcs.commit.CommitMessageUi
import java.lang.reflect.Method
import javax.swing.JComponent

internal object CommitMessageUiAccessors {
    fun editorDocument(commitMessageUi: CommitMessageUi): Document? =
        editorField(commitMessageUi)?.let { editorField ->
            (editorField as? EditorTextComponent)?.document
                ?: runCatching {
                    editorField.javaClass.findMethod("getDocument")?.invoke(editorField) as? Document
                }.getOrNull()
        }

    fun editorComponent(commitMessageUi: CommitMessageUi): JComponent? =
        editorTextComponent(commitMessageUi)?.component

    private fun editorTextComponent(commitMessageUi: CommitMessageUi): EditorTextComponent? =
        editorField(commitMessageUi) as? EditorTextComponent

    private fun editorField(commitMessageUi: CommitMessageUi): Any? =
        runCatching {
            commitMessageUi.javaClass.findMethod("getEditorField")?.invoke(commitMessageUi)
        }.getOrNull()

    private fun Class<*>.findMethod(name: String): Method? =
        methods.firstOrNull { method ->
            method.name == name && method.parameterCount == 0
        }
}
