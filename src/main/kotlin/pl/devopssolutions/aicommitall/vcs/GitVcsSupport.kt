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
