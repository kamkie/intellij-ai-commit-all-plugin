---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Detect AI Completion With Configurable Timeout

## Context and Problem Statement

AI completion behavior previously depended on open questions about timeout and message stability.

The user answered `Q-AI-1` by saying the plugin should check whether the AI action is completed instead of waiting, with timeout configuration available in Settings.

This also replaces the earlier idea that the generated message must remain unchanged for a fixed stable interval before completion.

The user answered `Q-AI-2` with 500 ms as a good starting point for a configurable supporting check interval.

The user answered `Q-AI-5` with 5 seconds as a starting default for the configurable AI generation timeout.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Detect AI Completion With Configurable Timeout
* Use a fixed wait duration.
* Treat unchanged message text as completion.

## Decision Outcome

Chosen option: "Adopt Detect AI Completion With Configurable Timeout", because Prefer an explicit AI action or generation-completion signal over fixed waiting or message-stability timing.

Prefer an explicit AI action or generation-completion signal over fixed waiting or message-stability timing.

Treat AI generation as complete only when the plugin can determine that the AI Assistant action has completed and the resulting commit message is acceptable for the automated flow.

Use a configurable timeout as a fail-safe. The timeout should be exposed in the plugin's Settings page.

If completion cannot be detected before the configured timeout expires, stop without committing or pushing and report the timeout path.

Do not use a fixed stable-message interval as the primary completion criterion.

When supporting polling or debounce checks are needed, make the completion-check interval configurable and default it to 500 ms.

Default the configurable AI generation timeout to 5 seconds.

### Consequences

- The implementation should search for reliable IntelliJ action, callback, UI state, or commit-message-generation completion signals before falling back to polling.
- Polling the commit message field may still be used as supporting evidence, but it must not be the only completion rule when a better completion signal is available.
- Settings UI and persistent state are now in scope for the timeout configuration and completion-check interval.
- The initial default timeout is intentionally conservative and can be changed later if sandbox validation shows AI generation commonly needs more time.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Detect AI Completion With Configurable Timeout

* Good, because Prefer an explicit AI action or generation-completion signal over fixed waiting or message-stability timing.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use a fixed wait duration.

* Bad, because it is unreliable and can commit too early or wait longer than needed.

### Treat unchanged message text as completion.

* Bad, because message stability is only an indirect signal and can be wrong if generation pauses.

## More Information

- Remove `Q-AI-1` and `Q-AI-2` from `docs/decisions/OPEN_QUESTIONS.md`.
- Remove `Q-AI-5` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` to implement explicit completion detection, Settings-based timeout configuration, and a 500 ms default completion-check interval.
