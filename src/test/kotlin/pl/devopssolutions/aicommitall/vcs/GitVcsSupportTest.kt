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
