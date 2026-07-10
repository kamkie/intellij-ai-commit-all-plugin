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

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.FilePath

internal fun classifyGitStatusSnapshot(
    rootPath: String,
    expectedPaths: Collection<FilePath>,
    porcelainOutput: String,
): Map<String, GitIndexConfirmation> {
    val statuses = parseGitStatusSnapshot(porcelainOutput)
    return expectedPaths.associate { expectedPath ->
        val relativePath = FileUtil.getRelativePath(rootPath, expectedPath.path, '/')
            ?: error("Expected path is outside Git root: ${expectedPath.path}")
        val normalizedRelativePath = relativePath.replace('\\', '/')
        val matchingStatuses = statuses.filter { status -> normalizedRelativePath in status.paths }
        val confirmation = when {
            matchingStatuses.isEmpty() -> GitIndexConfirmation.HEAD_IDENTICAL

            matchingStatuses.any { status -> status.index != ' ' && status.index != '?' } -> {
                GitIndexConfirmation.STAGED
            }

            else -> GitIndexConfirmation.UNCONFIRMED
        }
        expectedPath.path.replace('\\', '/') to confirmation
    }
}

private fun parseGitStatusSnapshot(porcelainOutput: String): List<GitStatusSnapshotEntry> {
    val records = porcelainOutput.split('\u0000').filter { record -> record.isNotEmpty() }
    val statuses = mutableListOf<GitStatusSnapshotEntry>()
    var recordIndex = 0
    while (recordIndex < records.size) {
        val record = records[recordIndex]
        require(record.length >= GIT_STATUS_PATH_OFFSET) { "Invalid Git status record" }
        val index = record[0]
        val workTree = record[1]
        val paths = mutableSetOf(record.substring(GIT_STATUS_PATH_OFFSET).replace('\\', '/'))
        if (index.isRenameOrCopy() || workTree.isRenameOrCopy()) {
            recordIndex += 1
            val originalPath = records.getOrNull(recordIndex)
                ?: error("Git status rename record is missing its original path")
            paths += originalPath.replace('\\', '/')
        }
        statuses += GitStatusSnapshotEntry(index, paths)
        recordIndex += 1
    }
    return statuses
}

private data class GitStatusSnapshotEntry(
    val index: Char,
    val paths: Set<String>,
)

private fun Char.isRenameOrCopy(): Boolean = this == 'R' || this == 'C'

private const val GIT_STATUS_PATH_OFFSET = 3
