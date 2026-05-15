package pl.devopssolutions.aicommitall.workflow

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.vcs.commit.CommitWorkflowHandler
import java.lang.reflect.Method

internal object ReflectiveCommitWorkflowSynchronizer {
    fun synchronize(
        workflowHandler: CommitWorkflowHandler,
        changeLists: List<LocalChangeList>,
        unversionedFiles: List<FilePath>,
        activeChangeList: LocalChangeList,
        inclusionItems: Collection<Any>,
    ): Boolean {
        val handlerClass = workflowHandler.javaClass
        val synchronizeInclusion = handlerClass.findMethod("synchronizeInclusion", List::class.java, List::class.java)
            ?: return false
        val setCommitState = handlerClass.findMethod(
            "setCommitState",
            LocalChangeList::class.java,
            Collection::class.java,
            java.lang.Boolean.TYPE,
        ) ?: return false

        return runCatching {
            synchronizeInclusion.invoke(workflowHandler, changeLists, unversionedFiles)
            setCommitState.invoke(workflowHandler, activeChangeList, inclusionItems, true)
        }.isSuccess
    }

    private fun Class<*>.findMethod(name: String, vararg parameterTypes: Class<*>): Method? =
        methods.firstOrNull { method ->
            method.name == name &&
                method.parameterTypes.contentEquals(parameterTypes)
        }
}
