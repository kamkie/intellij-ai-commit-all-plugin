# Use Local Repository End-To-End Tests

Status: Accepted

Date: 2026-05-15

## Context

Real project examples and acceptance workflows were open as `Q-VAL-3`.

The user answered that end-to-end tests should be created if possible on local repositories.

This complements ADR 0020, which requires validation against current JetBrains IDE builds and support for both changelists and Git staging modes.

## Decision

Create end-to-end tests against local Git repositories where the IntelliJ test framework, Gradle sandbox, and CI environment make that practical.

End-to-end coverage should exercise realistic local repository states:

- Modified tracked files.
- Added and unversioned files.
- Deleted files.
- Moved or renamed files.
- Multiple changelists.
- Multiple Git roots.
- Ignored-file exclusion.
- Git staging enabled and disabled.
- Commit-only flow.
- Commit-and-push flow where a local remote can be configured safely.

When an end-to-end scenario cannot be automated reliably, keep a manual sandbox scenario and document why automation is deferred.

## Consequences

- `Q-VAL-3` is resolved.
- Acceptance testing should prefer reproducible local repositories over hand-maintained external projects.
- Tests must avoid pushing to real remotes; push scenarios should use local remotes or sandboxed repositories.
- CI can start with build and verifier checks, then add E2E coverage incrementally as the plugin scaffold and test harness mature.

## Alternatives Considered

- Use external real-world repositories as acceptance fixtures.
  - Why it was not chosen: external repositories add network dependency, churn, and licensing review overhead.
- Manual-only acceptance testing.
  - Why it was not chosen: the user wants end-to-end tests where possible.

## Follow-Up

- Remove `Q-VAL-3` from `OPEN_QUESTIONS.md`.
- Add local-repository E2E tasks to `TASKS.md`.
- Keep manual sandbox checks for cases that cannot be automated yet.
