---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Resolve Plan Questions Before Implementation

## Context and Problem Statement

The repository uses plans for changes that span multiple files, change behavior, or depend on unresolved technical choices.

Starting implementation while plan questions or project decisions are still unresolved can cause agents to make hidden product, compatibility, validation, or workflow choices. Those choices are hard to review after code has already been written.

The user requested two workflow rules:

- When starting work on a plan, every question and decision must be answered and made.
- When a question arises while working on a plan, work must stop immediately and the appropriate document must be updated.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Resolve Plan Questions Before Implementation
* Continue implementation with best-effort assumptions and document them at handoff.
* Allow agents to decide every new question locally.
* Keep unresolved questions only in the active plan.

## Decision Outcome

Chosen option: "Adopt Resolve Plan Questions Before Implementation", because Before implementation starts from an accepted plan, every plan question and required project decision must be answered, decided, or explicitly documented as an allowed assumption under the current user request.

Before implementation starts from an accepted plan, every plan question and required project decision must be answered, decided, or explicitly documented as an allowed assumption under the current user request.

Project decisions and repository rule changes must be recorded in `docs/decisions/` before or alongside the plan or implementation they affect.

If a new question, missing decision, or unsafe assumption appears during planned implementation, agents must stop implementation work immediately and update the appropriate document before continuing:

- Update the active plan for task-local questions, assumptions, validation changes, or implementation notes.
- Update `docs/decisions/OPEN_QUESTIONS.md` for missing user input.
- Add or update an ADR in `docs/decisions/` for project decisions, repository rule changes, compatibility choices, user-facing behavior, validation expectations, or future maintenance policy.
- Update `TASKS.md` when backlog scope, dependencies, or task readiness changes.

Agents may resume only after the question is answered, decided, or explicitly documented as an allowed assumption.

### Consequences

- Planned implementation starts from explicit decisions instead of hidden assumptions.
- New uncertainty discovered during implementation is captured where future agents can see it.
- Some implementation work will pause until the user answers blocking questions.
- Plans and task documents may change during implementation when new information changes readiness or scope.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Resolve Plan Questions Before Implementation

* Good, because Before implementation starts from an accepted plan, every plan question and required project decision must be answered, decided, or explicitly documented as an allowed assumption under the current user request.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Continue implementation with best-effort assumptions and document them at handoff.

* Bad, because late documentation does not prevent hidden decisions from shaping implementation.

### Allow agents to decide every new question locally.

* Bad, because some questions require user input or durable ADRs before implementation can safely continue.

### Keep unresolved questions only in the active plan.

* Bad, because missing user input, durable decisions, and backlog dependency changes have separate owner documents.

## More Information

- Update `.agents/references/planning.md` with the plan-readiness and stop-on-question rules.
- Update `.agents/references/execution.md` with the implementation stop condition.
- Update `.agents/plans/README.md` and `.agents/plans/PLAN_TEMPLATE.md` with accepted-plan readiness expectations.
- Update `docs/DEVELOPMENT_LIFECYCLE.md` and `docs/WORKING_WITH_AI.md` with the human-facing summary.
