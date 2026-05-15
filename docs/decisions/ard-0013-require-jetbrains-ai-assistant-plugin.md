---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Require JetBrains AI Assistant Plugin

## Context and Problem Statement

The behavior was open for cases where JetBrains AI Assistant is missing, disabled, unavailable, or the user is not signed in.

The user answered `Q-AI-3` by saying this plugin should depend on IntelliJ AI Assistant and fail to install when that dependency is not available.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Require JetBrains AI Assistant Plugin
* Treat AI Assistant as optional and show a runtime notification when absent.
* Depend directly on proprietary AI Assistant classes.

## Decision Outcome

Chosen option: "Adopt Require JetBrains AI Assistant Plugin", because Declare JetBrains AI Assistant as a required plugin dependency.

Declare JetBrains AI Assistant as a required plugin dependency.

If JetBrains AI Assistant is missing or disabled, this plugin should not install, load, or enable as an operational plugin.

The first implementation must identify the correct JetBrains AI Assistant plugin dependency ID for the targeted 2026.1 platform line and declare it in plugin metadata.

This dependency decision does not approve compile-time use of non-public AI Assistant implementation classes. Prefer invoking AI Assistant through the IntelliJ action system unless a later ADR explicitly accepts direct API usage.

Runtime states that can still occur with the dependency present, such as not signed in or AI Assistant service unavailable, are covered by ADR 0014.

### Consequences

- AI Assistant absence is handled at installation/loading time instead of as an optional runtime path.
- The plugin metadata must include a required AI Assistant dependency once the scaffold exists.
- Validation must cover the dependency failure path.
- Runtime unavailable or not-signed-in cases still need clear handling; ADR 0014 covers the fail-closed behavior and ADR 0016 covers notification and error message policy.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Require JetBrains AI Assistant Plugin

* Good, because Declare JetBrains AI Assistant as a required plugin dependency.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Treat AI Assistant as optional and show a runtime notification when absent.

* Bad, because the user requested a required dependency and install failure when missing.

### Depend directly on proprietary AI Assistant classes.

* Bad, because a plugin dependency is enough for installation/loading; direct non-public API usage remains a separate compatibility risk.

## More Information

- Remove `Q-AI-3` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` to declare the required AI Assistant dependency and test missing-dependency behavior.
- See ADR 0014 for non-AI fallback behavior when AI Assistant exists but cannot generate a message.
