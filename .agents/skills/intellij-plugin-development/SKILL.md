---
name: intellij-plugin-development
description: IntelliJ Platform plugin development workflow for this repository. Use when changing Gradle IntelliJ Platform configuration, plugin.xml, actions, services, VCS/commit/push workflow code, Commit tool window UI controls, notifications, threading, dumb-mode compatibility, sandbox runs, buildPlugin, runIde, verifyPlugin, or IDE compatibility behavior.
---

# IntelliJ Plugin Development

## Start

- Read `AGENTS.md` if it is not already loaded.
- Read `.agents/references/code-style.md` for plugin implementation constraints.
- Read `.agents/references/testing.md` before selecting validation.
- Read the relevant ADR, task, or approved plan before changing governed behavior.
- Use `docs/decisions/README.md` for ADR requirements when the request may change repository rules, supported IDE scope, plugin dependency policy, commit/push behavior, or validation expectations.

## Implementation

- Prefer IntelliJ Platform APIs and the IDE commit workflow over custom process or Git command execution.
- Keep action classes small. Put workflow state, VCS preparation, and UI state in focused collaborators.
- Preserve standard IntelliJ, Git, VCS, push, and AI Assistant errors. Add plugin-owned text only for paths without a platform-owned message.
- Avoid compile-time dependencies on proprietary JetBrains AI Assistant classes unless an accepted ADR approves that direction.
- Apply code-style guidance for service lifetime, EDT/background work, action threading, and DumbAware boundaries.
- Isolate compatibility-sensitive IntelliJ APIs behind narrow functions or classes and document the target IDE assumption in the governing ADR, task, or plan.

## Validation

- Run the smallest relevant targeted test first, for example `.\gradlew.bat test --tests "package.ClassTest"`.
- Run `.\gradlew.bat test` when behavior spans shared workflow, services, settings, or VCS logic.
- Run `.\gradlew.bat buildPlugin` for plugin descriptor, Gradle, packaging, dependency, or compatibility-boundary changes.
- Run `.\gradlew.bat verifyPlugin` when configured and relevant.
- Run `.\gradlew.bat runIde` for manual sandbox checks when behavior cannot be trusted from automated tests alone.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1` for docs, ADRs, plans, proposals, changelog, or agent guidance.
- Run `git diff --check` before handoff.

Report commands run, manual sandbox scenarios tested, skipped checks with reasons, and remaining IDE compatibility risk.
