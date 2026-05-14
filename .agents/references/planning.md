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
- Proposed changes, split into named implementation tasks when the work has multiple tasks.
- Validation.
- Risks and fallback behavior.

## Planning Rules

- Keep plans short enough to maintain.
- Do not duplicate the full backlog from `TASKS.md`.
- Move unresolved user decisions to `OPEN_QUESTIONS.md`.
- Before implementation starts from an accepted plan, every plan question and required project decision must be answered, explicitly decided, or recorded as a documented assumption that the current user request allows.
- Record project decisions and repository rule changes in `docs/decisions/` before or alongside the plan or implementation they affect.
- Follow `docs/decisions/README.md` for project decisions and repository rule changes.
- Update or delete stale plans when implementation makes them obsolete.
- For multi-task plans, name tasks clearly enough to use in `Project-Plan-Task:` commit metadata.
- For multi-task plans, each task must be fully implemented, validated through `.agents/references/testing.md`, self-reviewed through `.agents/references/reviews.md`, and committed before the next task starts.
- Leave release-wide review, broader manual checks and tests, documentation update passes, and release artifact preparation to the later release workflow unless the plan is specifically a release plan.

## Before Implementation

Before editing code from a plan:

- Confirm every plan question and required decision is answered, decided, or explicitly assumed under the current request.
- Identify files likely to change.
- Choose validation that matches the risk.
- Keep the first implementation step small enough to review.

## Questions During Implementation

If a new question, missing decision, or unsafe assumption appears while implementing from a plan:

- Stop implementation work immediately.
- Update the appropriate document before continuing: the active plan for task-local questions, `OPEN_QUESTIONS.md` for missing user input, `docs/decisions/` for project decisions or repository rule changes, and `TASKS.md` when backlog scope or dependencies change.
- Ask the user for the decision when the answer cannot be safely inferred from the current request and governing documents.
- Resume only after the question is answered, decided, or explicitly documented as an allowed assumption.
