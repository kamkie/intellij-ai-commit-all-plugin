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
