# Support Git Only With Multiple Roots

Status: Accepted

Date: 2026-05-14

## Context

The first-version VCS scope and multiple-root support were open questions.

The user answered `Q-SCOPE-3` as `only git for now` and `Q-SCOPE-4` as `yes`.

## Decision

The first implementation supports Git only.

Projects with multiple Git roots are in scope.

Non-Git VCS integrations are out of scope for the first implementation and should fail closed with a clear unsupported-state notification or disabled action state.

## Consequences

- Implementation may use Git-specific commit-and-push executor behavior where needed.
- File selection still uses IntelliJ Platform VCS and commit workflow APIs where practical, but behavior is validated against Git.
- Multi-root behavior must include all non-ignored committable files across supported Git roots.
- Non-Git and mixed unsupported VCS projects must not be committed accidentally.

## Alternatives Considered

- Support every VCS exposed through the IntelliJ commit workflow.
  - Why it was not chosen: the user scoped the first version to Git only.
- Support only a single Git root.
  - Why it was not chosen: the user explicitly accepted multiple roots.

## Follow-Up

- Remove `Q-SCOPE-3` and `Q-SCOPE-4` from `OPEN_QUESTIONS.md`.
- Remove `Q-SCOPE-3` and `Q-SCOPE-4` dependency markers from `TASKS.md`.
- Update documentation to state Git-only first-version behavior and multiple Git root support.
