# Code Style

Use this guide for Kotlin, Gradle, IntelliJ Platform plugin descriptors, and plugin implementation code.

## Kotlin And IntelliJ Platform

- Prefer Kotlin for plugin source code.
- Follow IntelliJ Platform SDK conventions for actions, services, threading, disposables, notifications, and VCS integration.
- Keep action classes small and explicit.
- Keep UI-facing action text and descriptions stable and clear.
- Use extension points and action registration through `plugin.xml` or the Gradle IntelliJ Platform conventions selected by the scaffold.

## AI Assistant Integration

- Avoid compile-time dependencies on proprietary JetBrains AI Assistant classes unless the user explicitly approves that direction.
- Prefer invoking AI Assistant through the IntelliJ action system.
- Discover known action IDs at runtime where possible, then fall back to action groups or presentation text when needed.
- Treat AI Assistant absence, disabled state, sign-in requirements, and timeout as normal runtime outcomes.

## VCS And Commit Workflow

- Prefer existing VCS commit workflow APIs over custom Git command execution.
- Use the IDE commit workflow so before-commit checks, errors, and confirmations remain intact.
- Use Git commit-and-push executor behavior where available for push flow.
- Do not bypass the IDE's commit UI state unless there is an explicit product decision and test coverage.

## Design Discipline

- Do not add broad abstractions before there is real repetition.
- Keep compatibility boundaries visible. If an IntelliJ API differs across target IDE versions, isolate that decision and document it.
- Do not introduce publishing, signing, CI, marketplace metadata, or release automation as part of feature implementation unless requested.
