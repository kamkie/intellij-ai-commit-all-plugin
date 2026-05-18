---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Reuse Standard IntelliJ Error Messages

## Context and Problem Statement

Notification and error text for skipped commits, AI failures, timeout, empty change sets, unsupported push, and other failure paths was open.

The user answered `Q-UX-3`: when possible, the plugin should use the same messages as the standard IntelliJ flow, forward action errors, or let standard IntelliJ actions show their own errors. Unexpected cases should be decided when the concrete code paths are revealed and reviewed.

ADR 0014 already decided that runtime AI failures stop without committing or pushing and use standard IntelliJ notifications.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Reuse Standard IntelliJ Error Messages
* Define every notification string before implementation.
* Wrap all errors in plugin-specific notifications.

## Decision Outcome

Chosen option: "Adopt Reuse Standard IntelliJ Error Messages", because Do not invent custom notification text for paths where IntelliJ Platform, Git, VCS commit workflow, push executor, or JetBrains AI Assistant actions already provide user-facing errors or notifications.

Do not invent custom notification text for paths where IntelliJ Platform, Git, VCS commit workflow, push executor, or JetBrains AI Assistant actions already provide user-facing errors or notifications.

Implementation should prefer, in this order:

- Let the standard IntelliJ action or workflow surface its own message.
- Forward the standard action or workflow error without rewriting it.
- Use the plugin notification group only for plugin-owned states that have no platform-owned message.

For plugin-owned states that need new wording, choose the exact message only after the implementation exposes the concrete code path, then document the decision before or alongside the code that introduces that message.

### Consequences

- `Q-UX-3` is resolved without a fixed list of custom notification strings.
- Backlog items no longer wait on notification text before implementation.
- Reviews should check that the plugin does not mask platform errors with less precise custom text.
- New plugin-owned notification messages require a small documented decision when their code paths are introduced.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Reuse Standard IntelliJ Error Messages

* Good, because Do not invent custom notification text for paths where IntelliJ Platform, Git, VCS commit workflow, push executor, or JetBrains AI Assistant actions already provide user-facing errors or notifications.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Define every notification string before implementation.

* Bad, because the preferred behavior is to reuse IntelliJ's standard flow, and some branches are not concrete until the API integration is implemented.

### Wrap all errors in plugin-specific notifications.

* Bad, because this would duplicate or weaken standard IntelliJ error handling.

## More Information

- Remove `Q-UX-3` from `docs/decisions/OPEN_QUESTIONS.md`.
- Remove `depends on: Q-UX-3` markers from `TASKS.md`.
- See ADR 0017 for confirmation behavior.
- Historical note: `Q-UX-5` was later resolved by ADR 0025, and final placeholder styling was selected by ADR 0027.
- Implementation note: the plugin-owned AI timeout notification uses title `AI Commit All` and content `AI Assistant did not finish generating a commit message before the configured timeout.`
