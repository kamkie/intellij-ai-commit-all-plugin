# Working With AI

Use this guide when asking an AI agent to plan, implement, validate, or review work in this repository.

## Request Shape

For implementation tasks, prefer a compact request with the details that constrain the work:

```text
Task:
Goal:
Target IDE version:
Target artifacts:
Constraints:
Validation expected:
```

Example:

```text
Task: Scaffold the IntelliJ plugin project.
Goal: Add a Kotlin Gradle project that can run an IDE sandbox.
Target IDE version: 2026.1.
Target artifacts: build.gradle.kts, settings.gradle.kts, plugin.xml.
Constraints: Keep publishing, signing, and CI changes aligned with ADR 0019.
Validation expected: gradle buildPlugin and gradle runIde startup.
```

## Useful Task Types

- Planning: ask for a plan when the target IDE version, supported IDEs, or commit/push behavior is not settled.
- Implementation: name the user-facing behavior, target files, validation expected, and any decisions already made.
- Review: ask for bugs, unintended commit risk, AI Assistant integration risk, and missing validation.
- Documentation: state whether the change affects users, contributors, or AI agents.

## What AI Should Read

- Start with `AGENTS.md`.
- Use `TASKS.md` for backlog and scope boundaries.
- Use `OPEN_QUESTIONS.md` for missing user decisions.
- Use `docs/DEVELOPMENT_LIFECYCLE.md` for multi-step changes.
- Use `.agents/references/planning.md` before creating implementation plans.
- Use `.agents/references/execution.md` before implementation.
- Use `.agents/references/testing.md` before choosing validation.
- Use `.agents/references/reviews.md` for review tasks.
- Use `.agents/references/code-style.md` before editing Kotlin, Gradle, or plugin descriptor files.
- Use `.agents/references/documentation.md` before adding or changing docs.
- Use `docs/decisions/` for project decisions and repository rule changes.

## Constraints To State Explicitly

- Minimum supported IntelliJ Platform version, currently 2026.1.
- Target IDEs, currently all JetBrains IDEs with VCS commit UI.
- Git-only first-version behavior, including multiple Git roots.
- Split-button styling and icon choices beyond the accepted `AI Commit All` / `& Push` structure.
- Plugin ID and base package `pl.devopssolutions.aicommitall`; vendor `DevOps Solutions Kamil Kiewisz`.
- Apache-2.0 repository and plugin license.
- Open-source publication to the official JetBrains Marketplace, including signing, Marketplace metadata, and CI.
- Manual validation against current stable JetBrains IDE builds available through All Products Pack.
- End-to-end validation against local Git repositories where practical.
- Whether proprietary JetBrains AI Assistant APIs may be used directly. The default is no.

## Validation Expectations

Ask for validation that matches the change. For code changes, likely checks include:

- `gradle buildPlugin`
- `gradle verifyPlugin` when configured
- `gradle runIde` for sandbox validation
- IntelliJ Plugin Verifier for supported IDE ranges
- Manual sandbox checks for tracked, deleted, moved or renamed, unversioned, ignored-file exclusion, commit-only, commit-and-push, AI unavailable, and Git staging-area modes

For documentation-only changes, a focused content review and link/path check is usually enough.

## Commit Requests

When asking AI to commit completed work, expect Conventional Commit messages with the metadata trailer block defined in [.gitmessage](../.gitmessage).

Before AI starts implementation from an accepted plan, all plan questions and required decisions should be answered or documented as allowed assumptions. If a new question appears during planned implementation, AI should stop, update the appropriate planning, question, decision, or task document, and ask for the missing decision when needed.

For accepted multi-task plans, each named task is committed after implementation, targeted validation, and self-review. The later release workflow is responsible for whole-release review, manual checks and tests, documentation update passes, and release artifact preparation.

To use the same template locally:

```text
git config commit.template .gitmessage
```
