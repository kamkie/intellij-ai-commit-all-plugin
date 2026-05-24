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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.FilePath
import git4idea.index.GitStageTracker

internal object GitStagingAreaSelectionCollector {
    fun collect(
        stateProvider: () -> GitStageTracker.State,
        isGitPath: (FilePath) -> Boolean,
        diagnostics: GitChangeSelectionCompatibilityDiagnostics = IntelliJGitChangeSelectionCompatibilityDiagnostics,
    ): List<FilePath> = runCatching {
        GitStageSelectionItems.committablePaths(
            state = stateProvider(),
            isGitPath = isGitPath,
        )
    }.getOrElse { exception ->
        diagnostics.report(
            GitChangeSelectionCompatibilityDiagnostic(
                sourceClassName = GitChangeSelectionService::class.java.name,
                methodName = "collectStagingAreaPaths",
                reason = "staging area paths could not be collected",
                exceptionClassName = exception.javaClass.name,
                causeClassName = exception.cause?.javaClass?.name,
            ),
        )
        emptyList()
    }
}

internal fun interface GitChangeSelectionCompatibilityDiagnostics {
    fun report(diagnostic: GitChangeSelectionCompatibilityDiagnostic)
}

internal data class GitChangeSelectionCompatibilityDiagnostic(
    val sourceClassName: String,
    val methodName: String,
    val reason: String,
    val exceptionClassName: String? = null,
    val causeClassName: String? = null,
)

private object IntelliJGitChangeSelectionCompatibilityDiagnostics : GitChangeSelectionCompatibilityDiagnostics {
    private val logger = Logger.getInstance(GitChangeSelectionService::class.java)

    override fun report(diagnostic: GitChangeSelectionCompatibilityDiagnostic) {
        logger.warn(diagnostic.toLogMessage())
    }

    private fun GitChangeSelectionCompatibilityDiagnostic.toLogMessage(): String = buildString {
        append("Git change-selection compatibility diagnostic: ")
        append("class=").append(sourceClassName)
        append(", method=").append(methodName)
        append(", reason=").append(reason)
        exceptionClassName?.let { exceptionClass ->
            append(", exception=").append(exceptionClass)
        }
        causeClassName?.let { causeClass ->
            append(", cause=").append(causeClass)
        }
    }
}
