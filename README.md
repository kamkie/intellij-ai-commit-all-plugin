# AI Commit All

[![CI](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/ci.yml)
[![Plugin Verifier](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/plugin-verifier.yml)
[![CodeQL](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/codeql.yml)
[![Security](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/security.yml/badge.svg)](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/workflows/security.yml)
[![Codecov](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/kamkie/intellij-ai-commit-all-plugin)
[![License](https://img.shields.io/github/license/kamkie/intellij-ai-commit-all-plugin)](LICENSE)

**AI Commit All** (`pl.devopssolutions.aicommitall`) is an IntelliJ Platform plugin that adds an `AI | Commit | Push` control to the Commit tool window. It uses JetBrains AI Assistant to draft the commit message, then can commit or commit-and-push eligible Git changes.

## Status

GitHub prerelease. The plugin has not been published to JetBrains
Marketplace yet. Latest tag: `v0.1.0-beta.6`.

## Requirements

- A JetBrains IDE on the `2026.1` IntelliJ Platform line with the non-modal Commit tool window.
- Git as the active VCS.
- JetBrains AI Assistant installed, enabled, and signed in.

If AI Assistant is missing or disabled, the IDE refuses to load the plugin through the required dependency.

## Quick Start

Build the plugin ZIP locally:

```powershell
.\gradlew.bat buildPlugin
```

In the IDE, open `Settings | Plugins`, click the gear icon, choose `Install
Plugin from Disk...`, and select
`build/distributions/ai-commit-all-<version>.zip`.

Then:

1. Open a Git project in a supported JetBrains IDE.
2. Open the Commit tool window.
3. Use the three-section control:

| Section | Result |
|---------|--------|
| `AI` | Generate a commit message without committing. |
| `Commit` | Generate the message, then commit. |
| `Push` | Commit and push, or push safe outgoing commits when nothing new needs committing. |

When the push state is safe, `Push` pushes silently without the IDE Push Commits dialog, including when the local branch already has unpushed commits from earlier `Commit` runs. Commit-and-push falls back to the dialog only for states such as a missing tracked upstream, an ambiguous push target, unresolved conflicts, an abnormal repository state, or an unsupported push API. The push is always a normal non-force push; a remote that moved ahead surfaces as a standard push failure after the commit.

## Key Settings

Open `Settings | Tools | AI Commit All`.

- AI generation timeout.
- Completion check interval.
- Clear commit message before AI generation.
- Use AI Commit All for IDE commit and push shortcuts.

## User Limits

- Git is the only supported VCS.
- There is no non-AI fallback message generator.
- Safe push behavior is intentionally conservative.
- Current reviewed screenshots and animation are linked from the user guide; concept graphics are references only.

## Documentation

- [User Guide](docs/user-guide.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Support](docs/SUPPORT.md)
- [Specification](docs/specification.md)
- [Validation](docs/validation/README.md) - maintainer validation map, scenario register, and release checklist.
- [Contributing](CONTRIBUTING.md)
- [Working With AI](docs/WORKING_WITH_AI.md) - guidance for humans preparing AI-assisted repository work requests.
- [Changelog](CHANGELOG.md)
- [Security](SECURITY.md)
