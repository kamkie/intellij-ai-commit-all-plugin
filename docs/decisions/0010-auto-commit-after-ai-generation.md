# Auto Commit After AI Generation

Status: Accepted

Date: 2026-05-14

## Context

The commit flow previously left open whether the plugin should automatically commit after AI Assistant generates the commit message, or pause for user review.

The user answered `Q-COMMIT-2` as `commit` and clarified that the built-in split button solves the commit-only versus commit-and-push flow choice.

## Decision

After AI Assistant generates a commit message and the plugin determines generation is complete, the selected split-button flow should proceed automatically:

- `AI Commit All` commits all included files.
- `& Push` commits all included files and pushes after a successful commit.

The plugin should not pause for an extra user-review step by default.

This does not override fail-closed behavior for AI generation failure, timeout, empty message, unavailable AI Assistant, unsupported VCS state, before-commit failures, or unresolved user edits during generation.

## Consequences

- The split-button segment selected by the user is the confirmation of commit-only versus commit-and-push intent.
- AI completion detection and failure handling must be conservative because successful generation leads directly to commit execution.
- Before-commit checks, IDE warnings, and commit workflow errors still remain part of the normal IDE commit path.

## Alternatives Considered

- Pause after AI generation for manual review.
  - Why it was not chosen: the requested one-click flow should proceed to commit after AI generation.
- Reuse a separate IDE commit/push selected state.
  - Why it was not chosen: ADR 0006 chose a split button for the flow decision.

## Follow-Up

- Remove `Q-COMMIT-2` from `OPEN_QUESTIONS.md`.
- Remove `Q-COMMIT-2` dependency markers from `TASKS.md`.
- See ADR 0011 for user edits or clears during AI generation.
