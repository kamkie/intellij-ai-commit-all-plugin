# Planning Guide

Use this guide when a task needs an implementation plan before editing.

## When To Plan

Create or update a plan when:

- The work changes plugin behavior.
- The work touches multiple areas, such as Gradle, plugin descriptor, Kotlin code, and docs.
- A user decision from `docs/decisions/OPEN_QUESTIONS.md` blocks implementation.
- The change may affect commit selection, AI message generation, commit execution, or push behavior.

Do not create a plan for small documentation cleanup unless the user asks for one.

When a requested change needs a plan, create or update the plan first and stop. Do not start implementation until the user has reviewed and explicitly approved the plan.

## Plan Location

Store active plans in `.agents/plans/`.

Use `.agents/plans/PLAN_TEMPLATE.md` as the starting shape. Name plan files with the stable `Plan-ID` as the filename prefix, for example:

```text
.agents/plans/PLAN-scaffold-plugin-project.md
.agents/plans/PLAN-commit-tool-window-actions.md
```

## Plan Content

A useful plan should include:

- Stable `Plan-ID` in the form `PLAN-<short-kebab-slug>`, not a strictly number-based ID.
- Compact `Status`: `Draft`, `Approved`, `In Progress`, `Blocked`, `Implemented`, or `Closed`.
- `Readiness` section that summarizes plan readiness, approval identity when approved, open questions, and implementation progress.
- Goal.
- Non-goals.
- Assumptions.
- Open questions.
- Proposed changes, split into named implementation tasks when the work has multiple tasks.
- Validation.
- Risks and fallback behavior.

## Planning Rules

- Keep plans short enough to maintain.
- Give every plan a stable, human-readable `Plan-ID` such as `PLAN-scaffold-plugin-project`; avoid strictly number-based IDs such as `PLAN-0001`.
- Include the stable `Plan-ID` in the plan filename for active and archived plans.
- Keep `Plan-ID` stable when plan title, filename, status, or wording changes.
- Use only canonical plan statuses from `.agents/plans/README.md`; `Closed` plans must include a `Close-Reason`.
- Treat `Approved` as an explicit user approval state, not an agent-assumed readiness label.
- Do not implement from a plan until the user has reviewed it, explicitly approved it, the plan status is `Approved`, and `Approved by:` records the approver.
- For approved and post-approval plans, set `Approved by:` to the configured Git identity in `Name <email>` form unless the current user request explicitly supplies another approver name.
- Draft or otherwise unapproved plans must not claim approval; omit `Approved by:` or leave it empty.
- Do not duplicate the full backlog from `TASKS.md`.
- Move unresolved user decisions to `docs/decisions/OPEN_QUESTIONS.md`.
- Before implementation starts from an `Approved` plan, every plan question and required project decision must be answered, explicitly decided, or recorded as a documented assumption that the current user request allows.
- Record project decisions and repository rule changes in `docs/decisions/` before or alongside the plan or implementation they affect.
- Follow `docs/decisions/README.md` for project decisions and repository rule changes.
- Update or delete stale plans when implementation makes them obsolete.
- For multi-task plans, name tasks clearly enough to use in `Project-Plan-Task:` commit metadata.
- Reference the stable `Plan-ID` in implementation handoffs, review notes, and commit metadata when work comes from a plan.
- When plan tasks come from `TASKS.md`, reference the stable `T-AREA-NNN` task ID alongside the task name.
- For multi-task plans, each task must be fully implemented, validated through `.agents/references/testing.md`, self-reviewed through `.agents/references/reviews.md`, and committed before the next task starts.
- Leave release-wide review, broader manual checks and tests, documentation update passes, and release artifact preparation to the later release workflow unless the plan is specifically a release plan.
- For multi-task plan execution, use an orchestrator plus one fresh task worker per plan task when the environment supports agent delegation.
- The orchestrator owns plan state, task sequencing, question handling, validation evidence, review evidence, changelog maintenance, and commit verification.
- Each task worker gets only the task-shaped context needed for its assigned plan task, not accumulated context from previous tasks.
- Do not run task workers in parallel unless the `Approved` plan explicitly identifies independent tasks with disjoint write scopes.

## Before Implementation

Before editing code from a plan:

- Confirm the plan has `Status: Approved` from explicit user approval.
- Confirm `Approved by:` records the approver.
- Confirm every plan question and required decision is answered, decided, or explicitly assumed under the current request.
- Update the plan status to `In Progress` when implementation starts and keep `## Readiness` current.
- Identify files likely to change.
- Choose validation that matches the risk.
- Keep the first implementation step small enough to review.

## Questions During Implementation

If a new question, missing decision, or unsafe assumption appears while implementing from a plan:

- Stop implementation work immediately.
- Set the plan status to `Blocked` if the question prevents progress, and make the blocker visible in `## Readiness` or `## Open Questions`.
- Update the appropriate document before continuing: the active plan for task-local questions, `docs/decisions/OPEN_QUESTIONS.md` for missing user input, `docs/decisions/` for project decisions or repository rule changes, and `TASKS.md` when backlog scope or dependencies change.
- Ask the user for the decision when the answer cannot be safely inferred from the current request and governing documents.
- Resume only after the question is answered, decided, or explicitly documented as an allowed assumption.

## Orchestrated Execution

When using delegated agents for an `Approved` multi-task plan:

- Keep one orchestrator responsible for the whole plan.
- Start one fresh task worker for the current named plan task.
- Give the worker `AGENTS.md`, the approved plan, the current task name, relevant ADRs, relevant source files, expected validation, and commit metadata requirements.
- Have the worker stop and report immediately if the task reveals a new question, missing decision, unsafe assumption, or scope conflict.
- Have the worker report suggested `CHANGELOG.md` entries for notable task outcomes, but keep final changelog edits with the orchestrator.
- Have the orchestrator update the owning document and obtain the missing decision before resuming.
- Have the orchestrator review worker output, confirm validation and self-review evidence, maintain `CHANGELOG.md` for notable changes, and verify the task commit before starting the next task.
- Have the orchestrator update the plan status to `Implemented` when all planned changes are complete and validated.
