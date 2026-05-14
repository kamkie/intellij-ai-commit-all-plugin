# Resolve Plan Questions Before Implementation

Status: Accepted

Date: 2026-05-15

## Context

The repository uses plans for changes that span multiple files, change behavior, or depend on unresolved technical choices.

Starting implementation while plan questions or project decisions are still unresolved can cause agents to make hidden product, compatibility, validation, or workflow choices. Those choices are hard to review after code has already been written.

The user requested two workflow rules:

- When starting work on a plan, every question and decision must be answered and made.
- When a question arises while working on a plan, work must stop immediately and the appropriate document must be updated.

## Decision

Before implementation starts from an accepted plan, every plan question and required project decision must be answered, decided, or explicitly documented as an allowed assumption under the current user request.

Project decisions and repository rule changes must be recorded in `docs/decisions/` before or alongside the plan or implementation they affect.

If a new question, missing decision, or unsafe assumption appears during planned implementation, agents must stop implementation work immediately and update the appropriate document before continuing:

- Update the active plan for task-local questions, assumptions, validation changes, or implementation notes.
- Update `OPEN_QUESTIONS.md` for missing user input.
- Add or update an ADR in `docs/decisions/` for project decisions, repository rule changes, compatibility choices, user-facing behavior, validation expectations, or future maintenance policy.
- Update `TASKS.md` when backlog scope, dependencies, or task readiness changes.

Agents may resume only after the question is answered, decided, or explicitly documented as an allowed assumption.

## Consequences

- Planned implementation starts from explicit decisions instead of hidden assumptions.
- New uncertainty discovered during implementation is captured where future agents can see it.
- Some implementation work will pause until the user answers blocking questions.
- Plans and task documents may change during implementation when new information changes readiness or scope.

## Alternatives Considered

- Continue implementation with best-effort assumptions and document them at handoff.
    - Why it was not chosen: late documentation does not prevent hidden decisions from shaping implementation.
- Allow agents to decide every new question locally.
    - Why it was not chosen: some questions require user input or durable ADRs before implementation can safely continue.
- Keep unresolved questions only in the active plan.
    - Why it was not chosen: missing user input, durable decisions, and backlog dependency changes have separate owner documents.

## Follow-Up

- Update `.agents/references/planning.md` with the plan-readiness and stop-on-question rules.
- Update `.agents/references/execution.md` with the implementation stop condition.
- Update `.agents/plans/README.md` and `.agents/plans/PLAN_TEMPLATE.md` with accepted-plan readiness expectations.
- Update `docs/DEVELOPMENT_LIFECYCLE.md` and `docs/WORKING_WITH_AI.md` with the human-facing summary.
