# AGENTS.md

## Project Intent

This repository is for an IntelliJ Platform plugin named `ai commit all files`.

Target behavior:

1. Add a button/action to the Commit tool window.
2. When invoked, include/select all changed files and unversioned files.
3. Trigger IntelliJ/JetBrains AI Assistant action `Generate commit message with AI Assistant`.
4. Wait until AI Assistant has finished generating the commit message.
5. Commit all included files.
6. Push after commit when the user chose the push flow.

## Current State

The repository is intentionally initialized only. Do not assume a Gradle/Kotlin plugin scaffold already exists unless it has been added after this file.

## Implementation Notes For Future Agents

- Before implementation, inspect the current IntelliJ Platform SDK APIs for the target IDE version.
- Prefer Kotlin and the official IntelliJ Platform Gradle Plugin when scaffolding is requested.
- The JetBrains AI Assistant implementation is proprietary and its action IDs/API may not be stable. Avoid compile-time dependencies on non-public AI Assistant classes unless the user explicitly chooses that path.
- Prefer invoking the AI commit-message action through IntelliJ's action system, with runtime discovery/fallbacks, then observe the commit message field until generation stabilizes.
- For commit/push, use the existing VCS commit workflow and Git commit-and-push executor where available.
- Keep changes scoped. Do not add publishing, CI, signing, or marketplace metadata unless requested.
