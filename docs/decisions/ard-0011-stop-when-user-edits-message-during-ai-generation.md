---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Stop When User Edits Message During AI Generation

## Context and Problem Statement

The behavior was open for cases where the user edits or clears the commit message while AI Assistant generation is in progress.

The user answered `Q-COMMIT-3` as `then it should stop`.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Stop When User Edits Message During AI Generation
* Continue and commit the user-edited message.
* Pause for confirmation after a user edit.

## Decision Outcome

Chosen option: "Adopt Stop When User Edits Message During AI Generation", because If the user edits or clears the commit message while AI generation is in progress, the plugin must stop the automated flow and must not commit or push.

If the user edits or clears the commit message while AI generation is in progress, the plugin must stop the automated flow and must not commit or push.

The plugin should treat the user edit as intentional intervention and fail closed.

### Consequences

- User edits during generation override the split-button automated commit flow.
- The implementation must detect message edits or clears while waiting for AI completion.
- The stop path should report a clear notification or status message once notification wording is decided.
- No retry loop should run automatically after this stop condition.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Stop When User Edits Message During AI Generation

* Good, because If the user edits or clears the commit message while AI generation is in progress, the plugin must stop the automated flow and must not commit or push.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Continue and commit the user-edited message.

* Bad, because edits during generation create ambiguous ownership of the final message.

### Pause for confirmation after a user edit.

* Bad, because the requested behavior is to stop.

## More Information

- Remove `Q-COMMIT-3` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` to implement the fail-closed stop path.
