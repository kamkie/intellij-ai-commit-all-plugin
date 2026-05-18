---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Local Repository End-To-End Tests

## Context and Problem Statement

Real project examples and acceptance workflows were open as `Q-VAL-3`.

The user answered that end-to-end tests should be created if possible on local repositories.

This complements ADR 0020, which requires validation against current JetBrains IDE builds and support for both changelists and Git staging modes.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Local Repository End-To-End Tests
* Use external real-world repositories as acceptance fixtures.
* Manual-only acceptance testing.

## Decision Outcome

Chosen option: "Adopt Use Local Repository End-To-End Tests", because Create end-to-end tests against local Git repositories where the IntelliJ test framework, Gradle sandbox, and CI environment make that practical.

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

### Consequences

- `Q-VAL-3` is resolved.
- Acceptance testing should prefer reproducible local repositories over hand-maintained external projects.
- Tests must avoid pushing to real remotes; push scenarios should use local remotes or sandboxed repositories.
- CI can start with build and verifier checks, then add E2E coverage incrementally as the plugin scaffold and test harness mature.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Local Repository End-To-End Tests

* Good, because Create end-to-end tests against local Git repositories where the IntelliJ test framework, Gradle sandbox, and CI environment make that practical.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use external real-world repositories as acceptance fixtures.

* Bad, because external repositories add network dependency, churn, and licensing review overhead.

### Manual-only acceptance testing.

* Bad, because the user wants end-to-end tests where possible.

## More Information

- Remove `Q-VAL-3` from `docs/decisions/OPEN_QUESTIONS.md`.
- Add local-repository E2E tasks to `TASKS.md`.
- Keep manual sandbox checks for cases that cannot be automated yet.
