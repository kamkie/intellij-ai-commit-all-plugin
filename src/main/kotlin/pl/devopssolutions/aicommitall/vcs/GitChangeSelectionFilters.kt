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

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil

internal object GitChangeSelectionFilters {
    fun isGitChange(
        change: Change,
        vcsNameForPath: (FilePath) -> String?,
        isIgnored: (FilePath) -> Boolean,
    ): Boolean {
        val paths = ChangesUtil.iteratePaths(listOf(change)).toList()
        return paths.isNotEmpty() &&
            paths.all { path -> vcsNameForPath(path) == GIT_VCS_NAME && !isIgnored(path) }
    }

    fun isGitPath(
        path: FilePath,
        vcsNameForPath: (FilePath) -> String?,
        isIgnored: (FilePath) -> Boolean,
    ): Boolean = vcsNameForPath(path) == GIT_VCS_NAME && !isIgnored(path)

    fun isGitPath(
        path: FilePath,
        vcsManager: ProjectLevelVcsManager,
    ): Boolean = isGitPath(
        path = path,
        vcsNameForPath = { candidate -> vcsManager.getVcsFor(candidate)?.name },
        isIgnored = vcsManager::isIgnored,
    )
}
