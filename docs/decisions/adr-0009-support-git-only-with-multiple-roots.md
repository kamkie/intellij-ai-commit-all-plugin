---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Support Git Only With Multiple Roots

## Context and Problem Statement

The first-version VCS scope and multiple-root support were open questions.

The user answered `Q-SCOPE-3` as `only git for now` and `Q-SCOPE-4` as `yes`.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Support Git Only With Multiple Roots
* Support every VCS exposed through the IntelliJ commit workflow.
* Support only a single Git root.

## Decision Outcome

Chosen option: "Adopt Support Git Only With Multiple Roots", because The first implementation supports Git only.

The first implementation supports Git only.

Projects with multiple Git roots are in scope.

Non-Git VCS integrations are out of scope for the first implementation and should fail closed with a clear unsupported-state notification or disabled action state.

### Consequences

- Implementation may use Git-specific commit-and-push executor behavior where needed.
- File selection still uses IntelliJ Platform VCS and commit workflow APIs where practical, but behavior is validated against Git.
- Multi-root behavior must include all non-ignored committable files across supported Git roots.
- Non-Git and mixed unsupported VCS projects must not be committed accidentally.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Support Git Only With Multiple Roots

* Good, because The first implementation supports Git only.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Support every VCS exposed through the IntelliJ commit workflow.

* Bad, because the user scoped the first version to Git only.

### Support only a single Git root.

* Bad, because the user explicitly accepted multiple roots.

## More Information

- Remove `Q-SCOPE-3` and `Q-SCOPE-4` from `docs/decisions/OPEN_QUESTIONS.md`.
- Remove `Q-SCOPE-3` and `Q-SCOPE-4` dependency markers from `TASKS.md`.
- Update documentation to state Git-only first-version behavior and multiple Git root support.
