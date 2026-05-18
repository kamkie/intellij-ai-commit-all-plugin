# Support

This project is a pre-release IntelliJ Platform plugin repository. No Marketplace-published release exists yet.

## Current Support Status

- Support is best-effort until the first official JetBrains Marketplace release.
- Current release candidate target: `v0.1.0-alpha.6`, an implementation prerelease.
- Current `main` branch state: AI Commit All workflow implementation and release automation are present but not Marketplace-published.
- Current planned IntelliJ Platform baseline: 2026.1.
- Current planned IDE scope: JetBrains IDEs with the VCS Commit tool window and compatible commit workflow APIs.
- Current planned VCS scope: Git only, including multiple Git roots.
- JetBrains AI Assistant is declared as a required plugin dependency.
- Manual sandbox validation remains required before release-readiness claims.
- Marketplace signing and publishing are configured through gated automation but have not been executed for a public release.

See [README.md](README.md) for the current supported scope decisions.

## Getting Help

Use repository issues or maintainer review channels for:

- Build, setup, or sandbox startup problems.
- Bugs in the plugin once implementation exists.
- Supported IDE, Git, changelist, staging-area, commit, push, or AI Assistant integration questions.
- Documentation gaps or unclear setup steps.

Include as much of this context as applies:

- IDE product name and build number.
- Plugin version, release tag, or commit SHA.
- Operating system.
- Whether Git staging area is enabled.
- Whether JetBrains AI Assistant is installed, enabled, and signed in.
- Exact steps to reproduce.
- Expected and actual behavior.
- Relevant logs or screenshots with secrets removed.

Tip: Use `Help | Show Log in Explorer` from the IDE to open the active logs folder. On Windows, IntelliJ Platform IDE logs are usually under `%LOCALAPPDATA%\JetBrains\<Product><Version>\log`, for example `%LOCALAPPDATA%\JetBrains\IntelliJIdea<Version>\log`.

## Out Of Scope

This project does not provide support for:

- JetBrains AI Assistant account, licensing, service availability, or model quality issues outside this plugin's integration behavior.
- Non-Git VCS support in the first version.
- IDE versions outside the documented supported range.
- Private forks or local changes that diverge from this repository.

## Security And Privacy

Do not post secrets, tokens, private repository contents, proprietary commit messages, or sensitive logs in public issues.

If a report involves sensitive information, open a minimal public issue that says a private report path is needed, or use the private security-reporting channel when one is available.
