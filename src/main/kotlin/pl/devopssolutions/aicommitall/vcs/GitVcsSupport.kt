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

internal object GitVcsSupport {
    fun status(activeVcsNames: Iterable<String>): GitVcsSupportStatus {
        val names = activeVcsNames.toSet()
        return when {
            names.isEmpty() ->
                GitVcsSupportStatus.UnsupportedNoGitVcs

            names.all { name -> name == GIT_VCS_NAME } ->
                GitVcsSupportStatus.Supported

            GIT_VCS_NAME !in names ->
                GitVcsSupportStatus.UnsupportedNoGitVcs

            else ->
                GitVcsSupportStatus.UnsupportedMixedVcs(
                    unsupportedVcsNames = names
                        .filterNot { name -> name == GIT_VCS_NAME }
                        .sorted(),
                )
        }
    }
}

internal sealed interface GitVcsSupportStatus {
    data object Supported : GitVcsSupportStatus

    data object UnsupportedNoGitVcs : GitVcsSupportStatus

    data class UnsupportedMixedVcs(
        val unsupportedVcsNames: List<String>,
    ) : GitVcsSupportStatus
}
