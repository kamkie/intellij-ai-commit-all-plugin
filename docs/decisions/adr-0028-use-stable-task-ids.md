---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Stable Task IDs

## Context and Problem Statement

`docs/decisions/OPEN_QUESTIONS.md` already uses stable question IDs such as `Q-UX-6`.

`TASKS.md` tracks backlog and implementation work, but task entries did not have stable identifiers. Referencing tasks by section and line position is fragile because the backlog is expected to change as plans, decisions, implementation, and validation evolve.

The user requested that tasks also have a name or ID like questions.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Stable Task IDs
* Use task names only.
* Use section numbers and list positions.
* Use one global sequence for all tasks.

## Decision Outcome

Chosen option: "Adopt Use Stable Task IDs", because Every task item in `TASKS.md` must have a stable task ID.

Every task item in `TASKS.md` must have a stable task ID.

Use the format `T-AREA-NNN`, where:

- `T` marks the identifier as a task.
- `AREA` is a short uppercase area label, such as `DEC`, `SCAFFOLD`, `ACTIONS`, `FILES`, `AI`, `WAIT`, `COMMIT`, `ERROR`, `VAL`, `DOC`, or `REL`.
- `NNN` is a zero-padded sequence number within that area.

Task IDs must remain stable when task wording, status, or ordering changes. Do not renumber existing task IDs. When a task is split, keep the original ID for the closest surviving task and give new IDs to new task items. When a task is removed because it is obsolete, do not reuse its ID for unrelated work.

Plans, reviews, commit metadata, and handoff notes should reference task IDs when they refer to `TASKS.md` work.

### Consequences

- Tasks can be referenced as reliably as open questions.
- Plan tasks and commits can point to concrete backlog items without relying on line numbers.
- `TASKS.md` is slightly more verbose.
- Future backlog edits must allocate new IDs without reusing or renumbering existing ones.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Stable Task IDs

* Good, because Every task item in `TASKS.md` must have a stable task ID.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use task names only.

* Bad, because names can change as tasks are clarified.

### Use section numbers and list positions.

* Bad, because positions change whenever tasks are inserted, completed, or regrouped.

### Use one global sequence for all tasks.

* Bad, because area-prefixed IDs are easier to scan in a backlog grouped by implementation area.

## More Information

- Add stable task IDs to existing `TASKS.md` items.
- Update documentation guidance with the task ID convention.
- Update commit metadata guidance so `Project-Task:` includes stable task IDs for task-sourced work.
- Update plan and human-facing AI guidance to reference task IDs when planning or requesting task-sourced work.
