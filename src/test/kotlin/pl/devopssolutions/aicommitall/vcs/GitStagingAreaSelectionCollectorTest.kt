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
package pl.devopssolutions.aicommitall.vcs

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GitStagingAreaSelectionCollectorTest {
    @Test
    fun `reports diagnostic and fails closed when staging area state cannot be collected`() {
        val diagnostics = CapturingGitChangeSelectionCompatibilityDiagnostics()

        val result = GitStagingAreaSelectionCollector.collect(
            stateProvider = { error("tracker state unavailable") },
            isGitPath = { true },
            diagnostics = diagnostics,
        )

        assertEquals(emptyList(), result)
        assertEquals(
            listOf(
                GitChangeSelectionCompatibilityDiagnostic(
                    sourceClassName = GitChangeSelectionService::class.java.name,
                    methodName = "collectStagingAreaPaths",
                    reason = "staging area paths could not be collected",
                    exceptionClassName = IllegalStateException::class.java.name,
                ),
            ),
            diagnostics.events,
        )
    }

    private class CapturingGitChangeSelectionCompatibilityDiagnostics : GitChangeSelectionCompatibilityDiagnostics {
        val events = mutableListOf<GitChangeSelectionCompatibilityDiagnostic>()

        override fun report(diagnostic: GitChangeSelectionCompatibilityDiagnostic) {
            events += diagnostic
        }
    }
}
