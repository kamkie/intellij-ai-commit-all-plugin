---
name: kotlin-plugin-style
description: Kotlin style guidance for this IntelliJ Platform plugin. Use when writing, reviewing, refactoring, or testing Kotlin files under src/main/kotlin or src/test/kotlin, especially action, service, UI, VCS workflow, settings, nullable platform API, coroutine/threading, and compatibility-boundary code.
---

# Kotlin Plugin Style

## Start

- Read `.agents/references/code-style.md`.
- Match the surrounding package, visibility, naming, and test style before introducing new patterns.
- Prefer the official Kotlin coding conventions unless this repository already uses a more specific local pattern.

## Kotlin Rules

- Keep classes and functions small enough that IntelliJ Platform responsibilities stay visible.
- Prefer `val`, immutable collections, expression bodies for simple expressions, and explicit data shapes for workflow state.
- Avoid `!!`; handle Java platform types with explicit null checks, early returns, or narrow helper functions.
- Avoid `lateinit` outside tests or framework-required lifecycle code.
- Declare explicit return types on public or internal APIs that cross module, platform, or test boundaries.
- Keep extension functions close to the domain they clarify. Do not use them to hide platform side effects.
- Prefer sealed types or simple data classes for finite workflow states only when they remove branching ambiguity.
- Add comments only where IntelliJ Platform lifecycle, threading, reflection, or compatibility behavior is not obvious.

## IntelliJ Kotlin Boundaries

- Keep services final by default and avoid heavy constructor work.
- Acquire dependent services inside methods instead of storing unnecessary service references.
- Do not block EDT with VFS, PSI, indexing, Git, process, network, or file-system traversal.
- Use platform disposables, listeners, message bus subscriptions, and action update threading according to IntelliJ Platform expectations.
- Keep reflection boundaries narrow, named, and covered by tests when direct API access is unavailable.
- Keep UI strings stable and user-facing text in action presentations, settings, or notifications precise.

## Tests

- Use readable Kotlin test names that match the surrounding style; backtick names are acceptable in tests.
- Keep fixture helpers explicit. A helper should remove noise without hiding the behavior under test.
- Prefer deterministic fake collaborators over sleeps, real clocks, random IDs, or order-dependent global state.
