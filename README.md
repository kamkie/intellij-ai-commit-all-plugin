# AI Commit All

[![CI](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml)
[![Plugin Verifier](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml)
[![CodeQL](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml)
[![Security](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/security.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/security.yml)
[![Codecov](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin)
[![License](https://img.shields.io/github/license/kamkie/intellij-ai-commit-all-plugin)](LICENSE)

An IntelliJ Platform plugin that turns the Commit tool window into a one-click AI commit flow. JetBrains AI Assistant writes the message, then the plugin commits — or commits and pushes — every non-ignored Git change.

## What It Does

The Commit tool window gains a compact three-section control: `AI | Commit | Push`.

| Section  | Behavior                                                                                                                                                                                                                                                                                         |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AI`     | Includes every eligible Git change and asks AI Assistant to generate a commit message. Stops before committing.                                                                                                                                                                                  |
| `Commit` | Runs the `AI` step, then commits through the standard IDE commit workflow.                                                                                                                                                                                                                       |
| `Push`   | Runs the `Commit` step when there are changes, then pushes. Skips the Push Commits dialog when the Git state is safe (normal tracked branch, no force push); otherwise falls back to the IDE commit-and-push executor and dialog. When only outgoing commits exist, `Push` pushes them directly. |

Eligible changes include modified, added, deleted, moved or renamed, unversioned, and resolved-conflict paths. Ignored files are excluded. Multiple Git roots and both changelist-backed and staging-area commit workflows are supported.

## Status

Unreleased prerelease — **not yet published to JetBrains Marketplace**. Latest tag: `v0.1.0-alpha.9`. Builds against IntelliJ Platform `2026.1.1`.

- Pending release notes: [CHANGELOG.md](CHANGELOG.md) → `Unreleased`.
- Full behavior contract: [docs/specification.md](docs/specification.md).
- Manual sandbox validation status: [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).

## Requirements

- A JetBrains IDE on the **2026.1** IntelliJ Platform line with the non-modal Commit tool window.
- **JetBrains AI Assistant**, installed and signed in. Required by plugin dependency `com.intellij.ml.llm`.
- A Git working copy.

If AI Assistant is missing or disabled, the IDE refuses to load this plugin — it does not silently fall back to a non-AI commit message.

## Install

Until the first Marketplace release, install from a local build:

```powershell
.\gradlew.bat buildPlugin
```

In the IDE, open `Settings | Plugins`, click the gear icon, choose `Install Plugin from Disk…`, and select `build/distributions/ai-commit-all-<version>.zip`.

For day-to-day plugin development, run a sandbox IDE instead:

```powershell
.\gradlew.bat runIde
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full build, test, and validation command set.

## Usage

1. Open a Git project in a supported JetBrains IDE.
2. Make sure JetBrains AI Assistant is enabled and signed in.
3. Open the Commit tool window. Either create committable changes, or have local commits ready to push.
4. Click `AI`, `Commit`, or `Push` on the segmented control.

### Keyboard shortcuts

With the default shortcut setting enabled, the IDE's commit and push shortcuts trigger the plugin instead of the standard IDE actions whenever the Commit tool window workflow is available:

| Shortcut (default Windows/Linux keymap) | Action          |
|-----------------------------------------|-----------------|
| `Ctrl+K`                                | Plugin `Commit` |
| `Ctrl+Shift+K`                          | Plugin `Push`   |

Disable the setting to return both shortcuts to the standard IDE actions.

### When the control hides or disables

- Hidden entirely outside an active Git commit workflow.
- `AI` and `Commit` disable when there are no non-ignored committable files.
- `Push` stays enabled when outgoing commits are available to push, even if nothing else is committable.

### Generation guardrails

The plugin clears stale message text before generation (configurable), then waits for AI Assistant to finish. It stops without committing or pushing when generation times out, returns an empty or unchanged message, or the user edits the message while generation is running.

Commit and commit-and-push run through the active IntelliJ commit workflow, so IDE before-commit checks, confirmations, warnings, and commit/push error handling remain in charge.

### Push fallback

`Push` only takes the immediate-push path for normal tracked branches in a safe state. Missing upstream, unresolved conflicts, ambiguous targets, divergence from upstream, or any force-push requirement falls back to the IDE commit-and-push executor and Push Commits dialog. Outgoing-only `Push` does **not** fall back to the IDE Push dialog — when safe immediate push cannot be prepared, it stops.

When the Git staging-area commit workflow is active, eligible non-ignored paths are staged before AI Assistant is invoked, so the resulting commit matches what the AI saw.

## Settings

Open `Settings | Tools | AI Commit All`:

| Setting                                             | Default |
|-----------------------------------------------------|---------|
| AI generation timeout (ms)                          | `30000` |
| Completion check interval (ms)                      | `500`   |
| Clear commit message before AI generation           | enabled |
| Use AI Commit All for IDE commit and push shortcuts | enabled |

Both timing values must be positive.

## Limitations

User-visible:

- Git only. No Mercurial, Subversion, or Perforce.
- The first Marketplace upload still requires manual JetBrains setup; nothing has been published yet.
- Manual sandbox validation of the final UI is incomplete — see [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md).

Internal:

- Inclusion state for the Commit tool window uses fail-closed reflection on IntelliJ APIs that are not part of the stable public surface.
- AI Assistant action discovery uses known action IDs with a presentation-based fallback. AI Assistant sign-in, UI, and runtime-availability messaging remain owned by AI Assistant.

## Documentation

- [CONTRIBUTING.md](CONTRIBUTING.md) — build commands, validation, pull-request expectations.
- [CHANGELOG.md](CHANGELOG.md) — release notes.
- [SUPPORT.md](SUPPORT.md) — support scope and issue reporting.
- [SECURITY.md](SECURITY.md) — vulnerability reporting and release-secret handling.
- [docs/specification.md](docs/specification.md) — full behavior specification.
- [AGENTS.md](AGENTS.md), [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md) — guidance for AI-agent work in this repository.

## Project

- Plugin ID and base package: `pl.devopssolutions.aicommitall`
- Vendor: DevOps Solutions Kamil Kiewisz · `kontakt@devopssolutions.pl` · [devopssolutions.pl](https://devopssolutions.pl)
- Canonical source: [github.com/kamkie/intellij-ai-commit-all-plugin](https://github.com/kamkie/intellij-ai-commit-all-plugin)

## License

[Apache License 2.0](LICENSE).
