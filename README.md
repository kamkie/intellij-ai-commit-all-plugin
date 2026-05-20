# AI Commit All

[![CI](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml)
[![Plugin Verifier](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml)
[![CodeQL](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml)
[![Security](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/security.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/security.yml)
[![Codecov](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin)
[![License](https://img.shields.io/github/license/kamkie/intellij-ai-commit-all-plugin)](LICENSE)

AI Commit All is an IntelliJ Platform plugin for preparing, committing, and pushing every non-ignored Git change through the IDE Commit tool window after JetBrains AI Assistant generates the commit message.

The Commit tool window gets a compact `<AI icon> AI | Commit | Push` control:

- `AI` includes every eligible Git change, generates a commit message, and stops before committing.
- `Commit` runs the `AI` section behavior, then commits all eligible Git changes through the standard IDE commit workflow.
- `Push` runs the `Commit` section behavior when committable changes exist, then pushes immediately when the Git state is safe and unambiguous, including protected branches when no force push is required; otherwise it falls back to the IDE commit-and-push executor. When there are no committable changes but local commits are waiting to push, `Push` uses the same safe immediate push path without opening the IDE Push dialog.

## Current Status

This is an unreleased prerelease project and is not yet published to JetBrains Marketplace. The latest tagged candidate is `v0.1.0-alpha.9`; changes listed under `Unreleased` in [CHANGELOG.md](CHANGELOG.md) have not been Marketplace-published.

The plugin targets the IntelliJ Platform 2026.1 release line and currently builds against `2026.1.1`. The full behavior specification used for requirement validation is in [docs/specification.md](docs/specification.md). Manual sandbox validation is tracked in [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).

## Requirements

- JetBrains IDE on the 2026.1 IntelliJ Platform line with the non-modal Commit tool window.
- JetBrains AI Assistant, required by plugin dependency `com.intellij.ml.llm`.
- A Git working copy.

If JetBrains AI Assistant is missing or disabled, the IDE should reject or fail plugin loading through the required dependency instead of falling back to a non-AI commit message.

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
- Manual sandbox validation for final three-section control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior is not yet complete. The current manual validation matrix records IntelliJ IDEA, PyCharm, and WebStorm `2026.1.1` targets in [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).
- Marketplace signing and publishing automation is configured but has not been executed for a public release. The first Marketplace upload may still require manual JetBrains setup.

## Source Repository

The canonical source repository is [github.com/kamkie/intellij-ai-commit-all-plugin](https://github.com/kamkie/intellij-ai-commit-all-plugin). Marketplace-facing plugin metadata points users and contributors to that source location.

## Project

- Plugin ID and base package: `pl.devopssolutions.aicommitall`.
- Vendor: DevOps Solutions Kamil Kiewisz, `https://devopssolutions.pl`, `kontakt@devopssolutions.pl`.
- License: Apache License 2.0.

Human contribution guidance, including build, test, and validation commands, is in [CONTRIBUTING.md](CONTRIBUTING.md).

Notable changes are tracked in [CHANGELOG.md](CHANGELOG.md).

Support status and issue-reporting expectations are in [SUPPORT.md](SUPPORT.md).

Security reporting and release-secret handling are in [SECURITY.md](SECURITY.md).

Implementation guidance for future agents is in [AGENTS.md](AGENTS.md). Guidance for working with AI agents on this repository is in [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md).

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
