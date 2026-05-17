# AI Commit All

AI Commit All is an IntelliJ Platform plugin for committing every non-ignored Git change through the IDE Commit tool window after JetBrains AI Assistant generates the commit message.

The Commit tool window gets an `AI Commit All` split-button group:

- `AI Commit All` generates a commit message and commits all eligible Git changes.
- `& Push` generates a commit message, commits all eligible Git changes, and pushes immediately only when the Git state is safe and unambiguous; otherwise it falls back to the IDE commit-and-push executor.

## Current Status

The executable Gradle/Kotlin IntelliJ plugin implementation is present for the first workflow slice. It is still an unreleased prerelease project and is not yet published to JetBrains Marketplace.

The current implementation:

- Targets the IntelliJ Platform 2026.1 release line.
- Supports Git projects, including multiple Git roots.
- Includes modified, added, deleted, moved or renamed, unversioned, and resolved-conflict paths exposed by IntelliJ VCS APIs.
- Excludes ignored files through IntelliJ VCS APIs.
- Invokes JetBrains AI Assistant commit-message generation through the IntelliJ action system.
- Waits for AI generation to complete before committing.
- Stops without committing or pushing when AI generation times out, produces an empty or unchanged message, or the user edits the message while generation is running.
- Executes commit and commit-and-push through the active IntelliJ commit workflow so IDE before-commit checks, confirmations, warnings, commit errors, and push errors stay in charge.
- Skips the Push Commits dialog only when every affected Git root is on a normal tracked branch, the local branch exactly matches its tracked upstream before the commit, and the target is the standard tracked branch.

Accepted follow-up direction: ADR 0052 and ADR 0053 replace this with a planned three-section `<AI icon> AI | Commit | Push` control using the selected violet AI snake styling reference. That runtime replacement is not implemented yet; the usage instructions below describe the current two-segment prerelease implementation.

Manual sandbox validation is tracked in [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).

## Requirements

- JDK 21.
- Git for local repository validation and development fixtures.
- JetBrains IDE with the 2026.1 IntelliJ Platform line and the non-modal Commit tool window.
- JetBrains AI Assistant, required by plugin dependency `com.intellij.ml.llm`.

If JetBrains AI Assistant is missing or disabled, the IDE should reject or fail plugin loading through the required dependency instead of falling back to a non-AI commit message.

## Build And Test

Build the plugin package:

```powershell
.\gradlew.bat buildPlugin
```

Run automated tests:

```powershell
.\gradlew.bat test
```

Run documentation validation:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1
```

Run the sandbox IDE:

```powershell
.\gradlew.bat runIde
```

Pull-request CI runs `test`, `verifyPluginStructure`, and `buildPlugin` without Marketplace or signing secrets. The separate Plugin Verifier workflow checks the configured IDE matrix.

## Usage

1. Open a Git project in a supported JetBrains IDE.
2. Open the Commit tool window.
3. Make sure JetBrains AI Assistant is installed, enabled, and available for commit-message generation.
4. Create committable Git changes.
5. Use `AI Commit All` for commit-only, or `& Push` for commit-and-push.

The actions are hidden outside an active supported Git commit workflow. They are disabled when no non-ignored committable Git files are available or when the required workflow executor is unavailable.

For `& Push`, missing upstreams, unresolved conflicts, non-normal Git states, ambiguous targets, protected targets, or already-diverged local and upstream branches use the standard IDE commit-and-push executor and Push Commits dialog.

## Settings

Open `Settings | Tools | AI Commit All` to configure:

- AI generation timeout, default `5000` ms.
- Completion check interval, default `500` ms.

Both values must be positive. Timeout and user-edit paths stop without committing or pushing.

## Known Limitations

- Git is the only supported VCS for the first implementation.
- The plugin relies on compatible IntelliJ Commit tool window APIs and fail-closed reflection boundaries for inclusion state.
- AI Assistant action discovery uses known action IDs and action presentation fallback; AI Assistant UI, sign-in, and runtime availability messages remain owned by JetBrains AI Assistant where available.
- Manual sandbox validation for icon rendering, staging-area modes, AI Assistant unavailable states, and full commit/push UI behavior is not yet complete. See [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).
- Marketplace signing and publishing automation is configured but has not been executed for a public release. The first Marketplace upload may still require manual JetBrains setup.

## Source Repository

The canonical source repository is [github.com/kamkie/intellij-ai-commit-all-plugin](https://github.com/kamkie/intellij-ai-commit-all-plugin). Marketplace-facing plugin metadata points users and contributors to that source location.

## Release And Publication

Release publication is intentionally manual and gated:

1. Complete implementation, documentation, validation, and changelog review on `main`.
2. Run local validation for the release candidate, including `test`, `verifyPluginStructure`, `buildPlugin`, and `verifyPlugin`.
3. Confirm manual sandbox validation evidence is current where release scope depends on it.
4. Configure the GitHub Environment named `jetbrains-marketplace` with required reviewer protection.
5. Add GitHub Actions secrets `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, and `PUBLISH_TOKEN`.
6. Start the `Release` workflow manually with the intended Marketplace channel, usually `default`.

The release workflow signs the plugin and calls `publishPlugin` only after manual dispatch and environment approval. Do not create tags or publish Marketplace updates unless release execution is explicitly requested.

## Project

- Plugin ID and base package: `pl.devopssolutions.aicommitall`.
- Vendor: DevOps Solutions Kamil Kiewisz, `https://devopssolutions.pl`, `kontakt@devopssolutions.pl`.
- License: Apache License 2.0.

Implementation guidance for future agents is in [AGENTS.md](AGENTS.md).

Guidance for working with AI agents on this repository is in [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md).

Notable changes are tracked in [CHANGELOG.md](CHANGELOG.md).

Support status and issue-reporting expectations are in [SUPPORT.md](SUPPORT.md).

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
