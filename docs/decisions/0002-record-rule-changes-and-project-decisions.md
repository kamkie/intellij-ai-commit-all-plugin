# Record Rule Changes And Project Decisions

Status: Accepted

Date: 2026-05-14

## Context

Repository guidance now includes explicit rules for where project intent, tasks, open questions, plans, and AI workflow instructions belong.

Without a decision record requirement, repository rules and project decisions can drift across `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `.agents/references/`, and `docs/` without a durable explanation of why they changed.

## Decision

Every repository rule change must be recorded in `docs/decisions/` as a new ADR or as a superseding ADR before or alongside the rule edit.

Every project decision must be recorded in `docs/decisions/` as an ADR before or alongside the implementation it affects.

Routine task execution notes do not need ADRs unless they choose or change project direction, repository rules, compatibility, user behavior, validation expectations, or future maintenance policy.

## Consequences

- Future agents and maintainers have a durable record of rule changes and project decisions.
- Rule and decision changes require a little more documentation work.
- `docs/decisions/OPEN_QUESTIONS.md` remains the place for unresolved input, while `docs/decisions/` records accepted decisions.

## Alternatives Considered

- Keep ADRs only for durable architecture decisions.
  - Why it was not chosen: the repository also needs traceability for workflow rules and project direction choices.
- Record decisions only in `TASKS.md` or plans.
  - Why it was not chosen: task lists and plans can become stale after implementation.

## Follow-Up

- Apply this rule to future updates of `AGENTS.md`, `.agents/references/`, `docs/`, project scope, compatibility targets, and user-facing behavior choices.
