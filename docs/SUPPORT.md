# Support

This project is a pre-release IntelliJ Platform plugin repository. No
Marketplace-published release exists yet.

For user-facing problem paths and FAQ entries, see
[Troubleshooting And FAQ](troubleshooting.md). This page owns support status,
reporting expectations, privacy guidance, and out-of-scope support cases, and
it does not repeat the troubleshooting FAQ.

## Current Support Status

- Support is best-effort until the first official JetBrains Marketplace release.
- Current release candidate target: `v0.1.0-alpha.9`, an implementation
  prerelease.
- Current `main` branch state: AI Commit All workflow implementation and release
  automation are present but not Marketplace-published.
- Current IntelliJ Platform target: `2026.1`; automated compatibility validation
  currently uses `2026.1.1`.
- Current IDE scope: JetBrains IDEs with the VCS Commit tool window and
  compatible commit workflow APIs. The release verifier gate covers IntelliJ
  IDEA, PyCharm, and WebStorm `2026.1.1`.
- Current VCS scope: Git only, including multiple Git roots.
- JetBrains AI Assistant is declared as a required plugin dependency.
- Manual sandbox validation remains required before release-readiness claims for
  final control rendering, staging-area modes, shortcut takeover, AI Assistant
  unavailable states, and full commit/push UI behavior.
- Marketplace signing and publishing are configured through gated automation.
  The release workflow runs only from `main` at the requested annotated release
  tag, validates documentation, formatting, Detekt, tests, coverage, plugin
  structure, packaging, and the supported IDE verifier matrix before signing and
  publishing.

See [README](../README.md) for the current supported scope decisions and
[Manual Sandbox Validation](validation/manual-sandbox.md) for current manual
coverage status.

## Supported Prerelease Scope

Support covers repository-local validation and prerelease plugin behavior for:

- Git repositories using the non-modal IntelliJ Commit tool window.
- Changelist-backed commit workflows and the Git staging-area commit workflow.
- Modified, added, deleted, moved or renamed, unversioned, and resolved-conflict
  paths exposed by IntelliJ VCS APIs.
- JetBrains AI Assistant commit-message generation invoked through the IntelliJ
  action system.
- The `AI`, `Commit`, and `Push` sections of the Commit tool window control.
- Default IDE commit and push shortcut takeover, plus the settings opt-out.

Support for Marketplace installation and update behavior starts after the first
Marketplace publication.

## Getting Help

Use repository issues or maintainer review channels for:

- Build, setup, or sandbox startup problems.
- Bugs in the prerelease plugin implementation.
- Supported IDE, Git, changelist, staging-area, commit, push, or AI Assistant
  integration questions.
- Release workflow, packaging, signing, or verifier failures that occur in this
  repository.
- Documentation gaps or unclear setup steps.

Before filing a workflow bug, check
[Troubleshooting And FAQ](troubleshooting.md) for common states such as missing
AI Assistant, AI timeout, disabled controls, push fallback, outgoing-only push
stops, conflicts, and background VCS operations.

Include as much of this context as applies:

- IDE product name and build number.
- Plugin version, release tag, or commit SHA.
- Operating system.
- Whether Git staging area is enabled.
- Whether JetBrains AI Assistant is installed, enabled, and signed in.
- Exact steps to reproduce.
- Expected and actual behavior.
- Relevant logs or screenshots with secrets removed.

For workflow bugs, also include the section used (`AI`, `Commit`, `Push`, or a
shortcut), whether the project had committable changes or outgoing commits, and
any branch or upstream state needed to understand push behavior.

Use `Help | Show Log in Explorer` from the IDE to open the active logs folder.
On Windows, IntelliJ Platform IDE logs are usually under
`%LOCALAPPDATA%\JetBrains\<Product><Version>\log`, for example
`%LOCALAPPDATA%\JetBrains\IntelliJIdea<Version>\log`.

## Out Of Scope

This project does not provide support for:

- JetBrains AI Assistant account, licensing, service availability, or model
  quality issues outside this plugin's integration behavior.
- Non-Git VCS support in the first version.
- IDE versions outside the documented supported range.
- Private forks or local changes that diverge from this repository.

## Security And Privacy

Do not post secrets, tokens, private repository contents, proprietary commit
messages, private remote URLs, or sensitive logs in public issues.

If a report involves sensitive information, open a minimal public issue that
says a private report path is needed, or use the private security-reporting
channel when one is available.

Report vulnerabilities, suspected secret exposure, or sensitive security details
through [SECURITY.md](../SECURITY.md), not through public issues.
