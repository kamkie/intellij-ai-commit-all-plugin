# Mark Task Dependencies On Open Questions

Status: Accepted

Date: 2026-05-14

## Context

`TASKS.md` tracks implementation work while `docs/decisions/OPEN_QUESTIONS.md` tracks unresolved user input and project decisions.

Some backlog items cannot be implemented safely until specific open questions are answered. Without explicit links, future agents may treat blocked tasks as ready work and make assumptions about commit behavior, IDE support, metadata, validation, or UX.

## Decision

Give each open question in `docs/decisions/OPEN_QUESTIONS.md` a stable question ID.

Mark task items in `TASKS.md` with:

- `resolves: Q-ID` when the task is to answer or close an open question.
- `depends on: Q-ID` when the task should not be implemented until the question is answered or explicitly assumed in an accepted plan or ADR.

Use comma-separated IDs when a task depends on multiple questions.

## Consequences

- Blocked work is visible in the backlog.
- Future implementation plans can trace assumptions back to unresolved questions.
- `docs/decisions/OPEN_QUESTIONS.md` IDs should remain stable even when question wording is clarified.
- When a question is resolved, related `TASKS.md` dependency markers should be updated or removed alongside the ADR, plan, or implementation that resolves it.

## Alternatives Considered

- Keep dependencies implicit by section ordering.
  - Why it was not chosen: implicit dependencies are easy to miss when agents work on individual tasks.
- Duplicate full question text in each task.
  - Why it was not chosen: it would make the backlog noisy and easier to desynchronize.

## Follow-Up

- Add question IDs to `docs/decisions/OPEN_QUESTIONS.md`.
- Annotate `TASKS.md` task dependencies and resolution tasks.
- Document the convention in repository documentation guidance.
