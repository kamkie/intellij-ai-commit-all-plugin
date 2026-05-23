---
status: superseded by adr-0080
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Orchestrator And Fresh Task Workers For Plans

## Context and Problem Statement

ADR 0023 requires each task in a multi-task accepted plan to be implemented, validated, reviewed, and committed before the next task starts.

ADR 0024 requires every plan question and required decision to be answered before implementation starts, and requires work to stop when a new question, missing decision, or unsafe assumption appears.

The user asked whether running a plan with an orchestrator and one forked subagent with clean context per task is a good idea, then asked to do that.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Orchestrator And Fresh Task Workers For Plans
* Use one long-running worker for the entire plan.
* Spawn multiple workers in parallel by default.
* Keep all work in the orchestrator even when delegation is available.

## Decision Outcome

Chosen option: "Adopt Use Orchestrator And Fresh Task Workers For Plans", because When the environment supports agent delegation, accepted multi-task plans should be executed with one orchestrator and one fresh task worker per named plan task.

When the environment supports agent delegation, accepted multi-task plans should be executed with one orchestrator and one fresh task worker per named plan task.

The orchestrator owns plan continuity:

- Confirm all plan questions and required decisions are answered before implementation starts.
- Select the next named plan task.
- Give each task worker only task-shaped context needed for that task.
- Stop implementation and update the owning document when a new question, missing decision, unsafe assumption, or scope conflict appears.
- Review worker output, validation evidence, self-review evidence, and commit metadata.
- Verify the task commit before starting the next task.

Each task worker owns only its assigned task:

- Implement the task according to `.agents/references/execution.md`.
- Run task-appropriate validation from `.agents/references/testing.md`.
- Self-review using `.agents/references/reviews.md`.
- Commit the completed task when the task scope requires it, or return the exact commit-ready diff and evidence when the environment prevents worker commits.
- Stop immediately and report when a new question, missing decision, unsafe assumption, or scope conflict appears.

Use a fresh task worker context for each task. Do not reuse worker context from previous plan tasks.

Run only one task worker at a time unless the accepted plan explicitly marks tasks as independent and gives them disjoint write scopes.

### Consequences

- The orchestrator keeps durable plan state while task workers stay focused and avoid stale context from prior tasks.
- Each plan task keeps separate implementation, validation, review, and commit evidence.
- New questions still stop implementation under ADR 0024.
- Parallel execution remains possible only when the plan proves tasks are independent and write scopes do not overlap.
- This rule depends on the agent environment. If delegation is unavailable, the single agent must follow the same per-task boundaries locally.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Orchestrator And Fresh Task Workers For Plans

* Good, because When the environment supports agent delegation, accepted multi-task plans should be executed with one orchestrator and one fresh task worker per named plan task.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use one long-running worker for the entire plan.

* Bad, because accumulated context can carry stale assumptions across tasks and blur task-specific validation evidence.

### Spawn multiple workers in parallel by default.

* Bad, because most plan tasks in this repository can touch shared Gradle, plugin descriptor, UI, or documentation files, so default parallel work risks conflicts.

### Keep all work in the orchestrator even when delegation is available.

* Bad, because fresh task workers can keep implementation context smaller while the orchestrator preserves plan continuity.

## More Information

- Update `.agents/references/planning.md` with orchestrator and fresh task worker guidance.
- Update `.agents/references/execution.md` with orchestrator and task worker responsibilities.
- Update `.agents/plans/README.md` and `.agents/plans/PLAN_TEMPLATE.md` with the execution model.
- Update `docs/DEVELOPMENT_LIFECYCLE.md` and `docs/WORKING_WITH_AI.md` with the human-facing summary.
