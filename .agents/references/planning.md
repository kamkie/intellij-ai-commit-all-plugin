# Planning Guide

Use this guide when a task needs an implementation plan before editing.

## When To Plan

Create or update a plan when:

- The work changes plugin behavior.
- The work touches multiple areas, such as Gradle, plugin descriptor, Kotlin code, and docs.
- A user decision from `OPEN_QUESTIONS.md` blocks implementation.
- The change may affect commit selection, AI message generation, commit execution, or push behavior.

Do not create a plan for small documentation cleanup unless the user asks for one.

## Plan Location

Store active plans in `.agents/plans/`.

Use `.agents/plans/PLAN_TEMPLATE.md` as the starting shape. Name plan files after the task, for example:

```text
.agents/plans/scaffold-plugin-project.md
.agents/plans/commit-tool-window-actions.md
```

## Plan Content

A useful plan should include:

- Goal.
- Non-goals.
- Assumptions.
- Open questions.
- Proposed changes.
- Validation.
- Risks and fallback behavior.

## Planning Rules

- Keep plans short enough to maintain.
- Do not duplicate the full backlog from `TASKS.md`.
- Move unresolved user decisions to `OPEN_QUESTIONS.md`.
- Record durable decisions in `docs/decisions/` only after they are accepted.
- Update or delete stale plans when implementation makes them obsolete.

## Before Implementation

Before editing code from a plan:

- Confirm blocking questions are answered or explicitly assumed.
- Identify files likely to change.
- Choose validation that matches the risk.
- Keep the first implementation step small enough to review.
