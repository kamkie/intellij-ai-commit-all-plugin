---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Auto Commit After AI Generation

## Context and Problem Statement

The commit flow previously left open whether the plugin should automatically commit after AI Assistant generates the commit message, or pause for user review.

The user answered `Q-COMMIT-2` as `commit` and clarified that the built-in split button solves the commit-only versus commit-and-push flow choice.

Current state: ADR 0052 refines this behavior for the accepted three-section control. The `AI` section generates or refreshes the message and stops before commit, while the `Commit` and `Push` sections still auto-continue after successful AI generation.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Auto Commit After AI Generation
* Pause after AI generation for manual review.
* Reuse a separate IDE commit/push selected state.

## Decision Outcome

Chosen option: "Adopt Auto Commit After AI Generation", because After AI Assistant generates a commit message and the plugin determines generation is complete, the selected split-button flow should proceed automatically: - `AI Commit All` commits all included files.

After AI Assistant generates a commit message and the plugin determines generation is complete, the selected split-button flow should proceed automatically:

- `AI Commit All` commits all included files.
- `& Push` commits all included files and pushes after a successful commit.

The plugin should not pause for an extra user-review step by default.

This does not override fail-closed behavior for AI generation failure, timeout, empty message, unavailable AI Assistant, unsupported VCS state, before-commit failures, or unresolved user edits during generation.

### Consequences

- The split-button segment selected by the user is the confirmation of commit-only versus commit-and-push intent.
- AI completion detection and failure handling must be conservative because successful generation leads directly to commit execution.
- Before-commit checks, IDE warnings, and commit workflow errors still remain part of the normal IDE commit path.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Auto Commit After AI Generation

* Good, because After AI Assistant generates a commit message and the plugin determines generation is complete, the selected split-button flow should proceed automatically: - `AI Commit All` commits all included files.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Pause after AI generation for manual review.

* Bad, because the requested one-click flow should proceed to commit after AI generation.

### Reuse a separate IDE commit/push selected state.

* Bad, because ADR 0006 chose a split button for the flow decision.

## More Information

- Remove `Q-COMMIT-2` from `docs/decisions/OPEN_QUESTIONS.md`.
- Remove `Q-COMMIT-2` dependency markers from `TASKS.md`.
- See ADR 0011 for user edits or clears during AI generation.
