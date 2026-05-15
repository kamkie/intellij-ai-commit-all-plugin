# Stop When User Edits Message During AI Generation

Status: Accepted

Date: 2026-05-14

## Context

The behavior was open for cases where the user edits or clears the commit message while AI Assistant generation is in progress.

The user answered `Q-COMMIT-3` as `then it should stop`.

## Decision

If the user edits or clears the commit message while AI generation is in progress, the plugin must stop the automated flow and must not commit or push.

The plugin should treat the user edit as intentional intervention and fail closed.

## Consequences

- User edits during generation override the split-button automated commit flow.
- The implementation must detect message edits or clears while waiting for AI completion.
- The stop path should report a clear notification or status message once notification wording is decided.
- No retry loop should run automatically after this stop condition.

## Alternatives Considered

- Continue and commit the user-edited message.
  - Why it was not chosen: edits during generation create ambiguous ownership of the final message.
- Pause for confirmation after a user edit.
  - Why it was not chosen: the requested behavior is to stop.

## Follow-Up

- Remove `Q-COMMIT-3` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` to implement the fail-closed stop path.
