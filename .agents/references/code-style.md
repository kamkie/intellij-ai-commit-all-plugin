# Code Style

Use this guide for Kotlin, Gradle, IntelliJ Platform plugin descriptors, and plugin implementation code.

## Mechanical Formatting

- Follow `.editorconfig` for editor defaults across Kotlin, Gradle Kotlin DSL, Markdown, YAML, and PowerShell.
- Run `.\gradlew.bat spotlessCheck` before handoff when Kotlin or Gradle Kotlin DSL files change.
- Run `.\gradlew.bat spotlessApply` only for mechanical formatting and license-header fixes; keep those changes separate from behavior edits when practical.
- Spotless with ktlint is the only Kotlin and Gradle Kotlin DSL formatter. Do not add a second Kotlin formatter or license-header tool without a superseding ADR.
- Kotlin source files under `src/` must carry the Apache-2.0 header enforced by Spotless.
- Markdown is checked with `markdownlint-cli2@0.22.1` through `scripts/validate-docs.ps1`; use 2-space nested-list and continuation indentation, one space after list markers, one blank line around headings and block elements, and no hard-wrapped prose by default.
- Keep Markdown tables with leading/trailing pipes and consistent columns. Let IntelliJ's Markdown table formatter preserve alignment, but do not rely on table padding as semantic content.

## Kotlin And IntelliJ Platform

- Prefer Kotlin for plugin source code.
- Follow IntelliJ Platform SDK conventions for actions, services, threading, disposables, notifications, and VCS integration.
- Keep action classes small and explicit.
- Keep UI-facing action text and descriptions stable and clear.
- Use AI-generated icon bases only as concept artwork; final plugin icons must be clean IntelliJ-style SVG assets following JetBrains icon guidance.
- Use extension points and action registration through `plugin.xml` or the Gradle IntelliJ Platform conventions selected by the scaffold.
- Reuse or forward standard IntelliJ, Git, VCS, push, and AI Assistant messages where possible; add plugin-owned notification text only for concrete paths without a platform-owned message.

## AI Assistant Integration

- Declare JetBrains AI Assistant as a required plugin dependency per ADR 0013.
- Avoid compile-time dependencies on proprietary JetBrains AI Assistant classes unless the user explicitly approves that direction.
- Prefer invoking AI Assistant through the IntelliJ action system.
- Discover known action IDs at runtime where possible, then fall back to action groups or presentation text when needed.
- Treat AI Assistant absence, disabled state, sign-in requirements, and timeout as normal runtime outcomes.

## VCS And Commit Workflow

- Prefer existing VCS commit workflow APIs over custom Git command execution.
- Use the IDE commit workflow so before-commit checks, errors, and confirmations remain intact.
- Use Git commit-and-push executor behavior where available for push flow.
- Do not add plugin-specific confirmation dialogs unless a concrete uncovered risk is documented as a new open question or ADR.
- Do not bypass the IDE's commit UI state unless there is an explicit product decision and test coverage.

## Design Discipline

- Prefer the smallest implementation that satisfies the user request and accepted governing artifacts.
- Do not add broad abstractions before there is real repetition.
- Do not add speculative features, single-use abstractions, optional configurability, generalized extension points, or generic defensive handling unless the user requested it or an accepted ADR, plan, specification, or task requires it.
- Keep changed lines traceable to the user request, the governing artifact, validation fixes, or cleanup caused by the current change.
- Clean up imports, variables, functions, and comments made obsolete by the current change. Mention pre-existing dead code, style drift, or unrelated cleanup opportunities in the handoff or review instead of editing them unless requested.
- Keep compatibility boundaries visible. If an IntelliJ API differs across target IDE versions, isolate that decision and document it.
- Keep documented handling for AI Assistant absence, disabled state, sign-in requirements, timeouts, IDE errors, VCS failures, commit failures, push failures, and compatibility failures. The simplicity rule does not justify removing required failure handling.
- Use `pl.devopssolutions.aicommitall` as the base package and plugin ID namespace per ADR 0022.
- Use Apache-2.0 for repository and plugin materials per ADR 0018; do not commit third-party assets unless their license is known and compatible.
- Publishing, signing, CI, Marketplace metadata, and release automation are in scope per ADR 0019; keep credentials out of the repository and use local properties or CI secrets.
