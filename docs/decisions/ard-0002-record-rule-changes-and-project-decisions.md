---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Record Rule Changes And Project Decisions

## Context and Problem Statement

Repository guidance now includes explicit rules for where project intent, tasks, open questions, plans, and AI workflow instructions belong.

Without a decision record requirement, repository rules and project decisions can drift across `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `.agents/references/`, and `docs/` without a durable explanation of why they changed.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Record Rule Changes And Project Decisions
* Keep ADRs only for durable architecture decisions.
* Record decisions only in `TASKS.md` or plans.

## Decision Outcome

Chosen option: "Adopt Record Rule Changes And Project Decisions", because Every repository rule change must be recorded in `docs/decisions/` as a new ADR or as a superseding ADR before or alongside the rule edit.

Every repository rule change must be recorded in `docs/decisions/` as a new ADR or as a superseding ADR before or alongside the rule edit.

Every project decision must be recorded in `docs/decisions/` as an ADR before or alongside the implementation it affects.

Routine task execution notes do not need ADRs unless they choose or change project direction, repository rules, compatibility, user behavior, validation expectations, or future maintenance policy.

### Consequences

- Future agents and maintainers have a durable record of rule changes and project decisions.
- Rule and decision changes require a little more documentation work.
- `docs/decisions/OPEN_QUESTIONS.md` remains the place for unresolved input, while `docs/decisions/` records accepted decisions.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Record Rule Changes And Project Decisions

* Good, because Every repository rule change must be recorded in `docs/decisions/` as a new ADR or as a superseding ADR before or alongside the rule edit.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Keep ADRs only for durable architecture decisions.

* Bad, because the repository also needs traceability for workflow rules and project direction choices.

### Record decisions only in `TASKS.md` or plans.

* Bad, because task lists and plans can become stale after implementation.

## More Information

- Apply this rule to future updates of `AGENTS.md`, `.agents/references/`, `docs/`, project scope, compatibility targets, and user-facing behavior choices.
