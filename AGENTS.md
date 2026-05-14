# AGENTS.md

This is the AI entry point for the repository. Keep task context small and read the most specific governing artifact before editing.

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

## Guidance Map

- Human guide for asking AI to work here: `docs/WORKING_WITH_AI.md`
- Build and implementation backlog: `TASKS.md`
- Execution loop: `.agents/references/execution.md`
- Code style: `.agents/references/code-style.md`
- Validation: `.agents/references/testing.md`
- Review priorities: `.agents/references/reviews.md`
- Documentation ownership: `.agents/references/documentation.md`

## Priority Order

When instructions overlap, apply this project-specific order:

1. Current user request.
2. `TASKS.md` and accepted implementation plans.
3. IntelliJ Platform API constraints and plugin descriptor behavior.
4. `README.md` and user-facing plugin behavior.
5. ADRs and AI guidance.

## Working Rules

- Use the smallest task-shaped context that can safely answer the request.
- Identify the behavior and governing artifact before editing.
- Update specs or docs before or alongside behavior changes when behavior changes.
- Run validation that matches the diff and risk.
- Review for bugs, missing validation, and API or IDE compatibility risk before handing off.
- Commit completed work only when the user asks for commits or the task scope explicitly requires it.

## Implementation Notes For Future Agents

- Before implementation, inspect the current IntelliJ Platform SDK APIs for the target IDE version.
- Prefer Kotlin and the official IntelliJ Platform Gradle Plugin when scaffolding is requested.
- The JetBrains AI Assistant implementation is proprietary and its action IDs/API may not be stable. Avoid compile-time dependencies on non-public AI Assistant classes unless the user explicitly chooses that path.
- Prefer invoking the AI commit-message action through IntelliJ's action system, with runtime discovery/fallbacks, then observe the commit message field until generation stabilizes.
- For commit/push, use the existing VCS commit workflow and Git commit-and-push executor where available.
- Keep changes scoped. Do not add publishing, CI, signing, or marketplace metadata unless requested.
