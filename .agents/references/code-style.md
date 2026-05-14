# Code Style

Use this guide for Kotlin, Gradle, IntelliJ Platform plugin descriptors, and plugin implementation code.

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

- Do not add broad abstractions before there is real repetition.
- Keep compatibility boundaries visible. If an IntelliJ API differs across target IDE versions, isolate that decision and document it.
- Use `pl.devopssolutions.aicommitall` as the base package and plugin ID namespace per ADR 0022.
- Use Apache-2.0 for repository and plugin materials per ADR 0018; do not commit third-party assets unless their license is known and compatible.
- Publishing, signing, CI, Marketplace metadata, and release automation are in scope per ADR 0019; keep credentials out of the repository and use local properties or CI secrets.
