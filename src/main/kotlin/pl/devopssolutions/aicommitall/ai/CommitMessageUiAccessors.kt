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

import com.intellij.openapi.editor.Document
import com.intellij.ui.EditorTextComponent
import com.intellij.vcs.commit.CommitMessageUi
import java.lang.reflect.Method
import javax.swing.JComponent

internal object CommitMessageUiAccessors {
    fun editorDocument(commitMessageUi: CommitMessageUi): Document? = editorField(commitMessageUi)?.let { editorField ->
        (editorField as? EditorTextComponent)?.document
            ?: runCatching {
                editorField.javaClass.findMethod("getDocument")?.invoke(editorField) as? Document
            }.getOrNull()
    }

    fun editorComponent(commitMessageUi: CommitMessageUi): JComponent? = editorTextComponent(commitMessageUi)?.component

    private fun editorTextComponent(commitMessageUi: CommitMessageUi): EditorTextComponent? = editorField(commitMessageUi) as? EditorTextComponent

    private fun editorField(commitMessageUi: CommitMessageUi): Any? = runCatching {
        commitMessageUi.javaClass.findMethod("getEditorField")?.invoke(commitMessageUi)
    }.getOrNull()

    private fun Class<*>.findMethod(name: String): Method? = methods.firstOrNull { method ->
        method.name == name && method.parameterCount == 0
    }
}
