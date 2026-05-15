package pl.devopssolutions.aicommitall.workflow

import pl.devopssolutions.aicommitall.vcs.GitChangeSelection
import pl.devopssolutions.aicommitall.vcs.GitVcsSupportStatus

internal sealed interface CommitWorkflowSelectionResult {
    data class Prepared(val selection: GitChangeSelection) : CommitWorkflowSelectionResult

    data object EmptySelection : CommitWorkflowSelectionResult

    data object MissingWorkflow : CommitWorkflowSelectionResult

    data class UnsupportedVcs(val supportStatus: GitVcsSupportStatus) : CommitWorkflowSelectionResult

    data class UnsupportedWorkflow(val reason: String) : CommitWorkflowSelectionResult
}
