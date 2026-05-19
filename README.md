# AI Commit All

[![CI](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml)
[![Plugin Verifier](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml)
[![CodeQL](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml)
[![Codecov](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin)
[![License](https://img.shields.io/github/license/kamkie/intellij-ai-commit-all-plugin)](LICENSE)

AI Commit All is an IntelliJ Platform plugin for preparing, committing, and pushing every non-ignored Git change through the IDE Commit tool window after JetBrains AI Assistant generates the commit message.

The Commit tool window gets a compact `<AI icon> AI | Commit | Push` control:

- `AI` includes every eligible Git change, generates a commit message, and stops before committing.
- `Commit` runs the `AI` section behavior, then commits all eligible Git changes through the standard IDE commit workflow.
- `Push` runs the `Commit` section behavior when committable changes exist, then pushes immediately when the Git state is safe and unambiguous, including protected branches when no force push is required; otherwise it falls back to the IDE commit-and-push executor. When there are no committable changes but local commits are waiting to push, `Push` uses the same safe immediate push path without opening the IDE Push dialog.

## Current Status

The executable Gradle/Kotlin IntelliJ plugin implementation is present for the first workflow slice. It is still an unreleased prerelease project and is not yet published to JetBrains Marketplace.

The current implementation:

- Targets the IntelliJ Platform 2026.1 release line.
- Supports Git projects, including multiple Git roots.
- Includes modified, added, deleted, moved or renamed, unversioned, and resolved-conflict paths exposed by IntelliJ VCS APIs.
- Supports both changelist-backed commit workflows and the Git staging-area commit workflow.
- Excludes ignored files through IntelliJ VCS APIs.
- Invokes JetBrains AI Assistant commit-message generation through the IntelliJ action system.
- Clears stale commit-message text before AI generation by default.
- Lets the `AI` section stop after AI generation without committing.
- Waits for AI generation to complete before the `Commit` or `Push` sections continue.
- Stops without committing or pushing when AI generation times out, produces an empty or unchanged message, or the user edits the message while generation is running.
- Executes commit and commit-and-push through the active IntelliJ commit workflow so IDE before-commit checks, confirmations, warnings, commit errors, and push errors stay in charge.
- Skips the Push Commits dialog when every affected Git root is on a normal tracked branch and the target is the standard tracked branch. Commit-and-push checks the branch against its tracked upstream before committing; outgoing-only push allows the local branch to be ahead of its tracked upstream. Protected branch settings do not force the dialog when the push is a normal non-force push.
- Keeps `Push` available for already-created outgoing commits even when there are no committable Git changes.
- Replaces the standard Commit tool window `Commit and Push...` toolbar button with the plugin's three-section control while keeping standard IDE commit, push, and shortcut delegation paths available.
- Uses the ADR 0053 violet AI, blue Commit, green Push segmented styling with cumulative hover and a snake-loop running indication on the active section.
- Uses the IDE commit shortcut for the `Commit` section and the IDE push shortcut for the `Push` section by default, with a settings opt-out.

Manual sandbox validation is tracked in [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).

## Requirements

- JDK 21.
- Node.js with `npx` for Markdown linting during documentation validation.
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

Generate the JaCoCo coverage report uploaded by CI to Codecov:

```powershell
.\gradlew.bat jacocoTestReport
```

Run source formatting checks:

```powershell
.\gradlew.bat spotlessCheck
```

Apply mechanical source formatting and Kotlin license-header fixes:

```powershell
.\gradlew.bat spotlessApply
```

Run documentation validation:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1
```

Run the sandbox IDE:

```powershell
.\gradlew.bat runIde
```

Pull-request CI validates the Gradle wrapper, source formatting, documentation, tests, JaCoCo coverage report generation, plugin structure, and plugin packaging without Marketplace or signing secrets. It uploads the packaged plugin ZIP as a GitHub Actions artifact and sends the JaCoCo XML test coverage report and Gradle JUnit XML test results to Codecov using GitHub Actions OIDC. The separate Plugin Verifier workflow checks the configured IDE matrix, and the CodeQL workflow scans Java/Kotlin code on pull requests, pushes to `main`, and a weekly schedule.

## Usage

1. Open a Git project in a supported JetBrains IDE.
2. Open the Commit tool window.
3. Make sure JetBrains AI Assistant is installed, enabled, and available for commit-message generation.
4. Create committable Git changes, or create local commits that are not pushed yet.
5. Use `AI` to generate a message without committing, `Commit` to generate and commit, or `Push` to generate, commit, and push. If only outgoing commits are present, use `Push` to push them immediately when the Git state is safe.

With the default shortcut setting enabled, the IDE's commit shortcut runs the plugin `Commit` workflow and the IDE's push shortcut runs the plugin `Push` workflow when the Commit tool window workflow is available. On the default Windows/Linux keymap, this makes `Ctrl+Shift+K` equivalent to clicking the `Push` section. Disable the shortcut setting to return those shortcuts to the standard IDE actions.

The control is hidden outside an active supported Git commit workflow. `AI` and `Commit` are disabled when no non-ignored committable Git files are available. `Push` stays enabled when outgoing Git commits are available to push, and otherwise follows the required workflow executor availability.

When the Git staging area commit workflow is active, the plugin stages eligible non-ignored paths before invoking AI Assistant so the IDE workflow can commit the intended content.

For `Push` with committable changes, missing upstreams, unresolved conflicts, non-normal Git states, ambiguous targets, or already-diverged local and upstream branches use the standard IDE commit-and-push executor and Push Commits dialog. Outgoing-only `Push` does not fall back to the IDE Push dialog when safe immediate push cannot be prepared.

## Settings

Open `Settings | Tools | AI Commit All` to configure:

- AI generation timeout, default `30000` ms.
- Completion check interval, default `500` ms.
- Clear commit message before AI generation, default enabled.
- Use AI Commit All for IDE commit and push shortcuts, default enabled.

Both timing values must be positive. Timeout and user-edit paths stop without committing or pushing.

## Known Limitations

- Git is the only supported VCS for the first implementation.
- The plugin relies on compatible IntelliJ Commit tool window APIs and fail-closed reflection boundaries for inclusion state.
- AI Assistant action discovery uses known action IDs and action presentation fallback; AI Assistant UI, sign-in, and runtime availability messages remain owned by JetBrains AI Assistant where available.
- Manual sandbox validation for final three-section control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior is not yet complete. See [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).
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
6. Verify GitHub secret scanning and push protection are enabled for the repository where available.
7. Start the `Release` workflow manually with the intended Marketplace channel, usually `default`.

Builds derive the Gradle project version and IntelliJ plugin descriptor version from Git metadata using Palantir `gradle-git-version`. Normal local and CI builds use `<latest-tag>-<commit-distance>-g<short-sha>`, tagged commits use `<latest-tag>-g<short-sha>`, and dirty working trees append `.dirty`. Set `GIT_VERSION` only when a packaging environment must override Git-derived version discovery.

The release workflow signs the plugin and calls `publishPlugin` only after manual dispatch and environment approval. Do not create tags or publish Marketplace updates unless release execution is explicitly requested.

## Project

- Plugin ID and base package: `pl.devopssolutions.aicommitall`.
- Vendor: DevOps Solutions Kamil Kiewisz, `https://devopssolutions.pl`, `kontakt@devopssolutions.pl`.
- License: Apache License 2.0.

Implementation guidance for future agents is in [AGENTS.md](AGENTS.md).

Guidance for working with AI agents on this repository is in [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md).

Human contribution guidance is in [CONTRIBUTING.md](CONTRIBUTING.md).

Notable changes are tracked in [CHANGELOG.md](CHANGELOG.md).

Support status and issue-reporting expectations are in [SUPPORT.md](SUPPORT.md).

Security reporting and release-secret handling are in [SECURITY.md](SECURITY.md).

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
