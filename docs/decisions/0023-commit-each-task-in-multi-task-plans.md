# Commit Each Task In Multi-Task Plans

Status: Accepted

Date: 2026-05-15

## Context

The repository already has an execution loop for implementation work, targeted validation guidance, review guidance, and commit-message rules.

Multi-task plans need a clear stopping point for each task so completed work does not accumulate into broad commits that are harder to validate, review, or trace back to a plan task.

The user requested a rule that every task in a multi-task plan be fully implemented according to `.agents/references/execution.md`, tested using `.agents/references/testing.md`, reviewed using `.agents/references/reviews.md`, and committed. The user also clarified that a future release workflow will take over after implementation tasks for full review, manual checks and tests, documentation updates, and release artifact preparation.

## Decision

When an accepted plan contains multiple implementation tasks, each named task is its own execution unit.

For each task, agents must:

- Implement the task according to `.agents/references/execution.md`.
- Run task-appropriate validation selected from `.agents/references/testing.md`.
- Self-review using `.agents/references/reviews.md`.
- Commit the completed task before starting the next plan task.

These commits use `Project-Source: plan-task` and include `Project-Plan:` and `Project-Plan-Task:` metadata.

Agents must not batch multiple plan tasks into one commit unless the accepted plan or a later user request explicitly says those tasks are inseparable.

Per-task completion does not replace the release workflow. Release preparation remains responsible for the full cross-task review, broader manual checks and tests, documentation update pass, and release artifact preparation.

## Consequences

- Multi-task plans produce smaller, traceable commits tied to named plan tasks.
- Each task carries its own validation and review evidence.
- Implementation agents have a clear rule that a named plan task is a task scope that explicitly requires a commit.
- Release workflow remains the place for whole-release review and artifact preparation instead of being duplicated inside every implementation task.
- Plans need clear task names suitable for commit metadata.

## Alternatives Considered

- Commit only after the entire plan is complete.
    - Why it was not chosen: broad plan-level commits make validation, review evidence, and rollback harder to trace to individual tasks.
- Require full release checks after every implementation task.
    - Why it was not chosen: release-wide checks are still needed, but running the full release workflow after every task would duplicate work and slow implementation unnecessarily.
- Leave commits optional unless the user explicitly asks each time.
    - Why it was not chosen: the user requested a durable rule for multi-task plans, making each completed plan task a scope that requires a commit.

## Follow-Up

- Update `.agents/references/execution.md` with the per-task implementation, validation, review, and commit rule.
- Update `.agents/references/planning.md` so multi-task plans use named tasks suitable for commit metadata.
- Update `docs/DEVELOPMENT_LIFECYCLE.md` with the human-facing lifecycle summary.
