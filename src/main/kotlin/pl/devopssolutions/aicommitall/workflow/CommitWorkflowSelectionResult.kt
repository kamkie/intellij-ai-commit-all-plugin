package pl.devopssolutions.aicommitall.workflow

import pl.devopssolutions.aicommitall.vcs.GitChangeSelection

internal sealed interface CommitWorkflowSelectionResult {
    data class Prepared(val selection: GitChangeSelection) : CommitWorkflowSelectionResult

    data object EmptySelection : CommitWorkflowSelectionResult

    data object MissingWorkflow : CommitWorkflowSelectionResult

    data class UnsupportedWorkflow(val reason: String) : CommitWorkflowSelectionResult
}
