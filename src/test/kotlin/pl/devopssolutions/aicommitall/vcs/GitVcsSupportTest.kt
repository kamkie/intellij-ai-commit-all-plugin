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

internal class GitVcsSupportTest {
    @Test
    fun `supports projects with only Git VCS roots`() {
        val result = GitVcsSupport.status(listOf(GIT_VCS_NAME, GIT_VCS_NAME))

        assertEquals(GitVcsSupportStatus.Supported, result)
    }

    @Test
    fun `stops projects without active VCS roots`() {
        val result = GitVcsSupport.status(emptyList())

        assertEquals(GitVcsSupportStatus.UnsupportedNoGitVcs, result)
    }

    @Test
    fun `stops projects with only non Git VCS roots`() {
        val result = GitVcsSupport.status(listOf("Mercurial"))

        assertEquals(GitVcsSupportStatus.UnsupportedNoGitVcs, result)
    }

    @Test
    fun `stops mixed VCS projects with unsupported root names`() {
        val result = GitVcsSupport.status(listOf(GIT_VCS_NAME, "Subversion", "Mercurial"))

        assertEquals(
            GitVcsSupportStatus.UnsupportedMixedVcs(
                unsupportedVcsNames = listOf("Mercurial", "Subversion"),
            ),
            result,
        )
    }
}
