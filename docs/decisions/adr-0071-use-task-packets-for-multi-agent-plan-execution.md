---
status: accepted
date: 2026-05-21
accepted_at: 2026-05-21T20:56:11+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Task Packets For Multi-Agent Plan Execution

## Context and Problem Statement

ADR 0026 requires an orchestrator and fresh task workers for approved multi-task plans, and ADR 0058 requires structured chat logging for worker events. The current planning guidance still tells the orchestrator to give each worker the approved plan, which conflicts with the rule that workers should receive only task-shaped context.

`PROP-multi-agent-context-packets` accepted five findings: add task packet dispatch contracts, define optional review and testing worker lanes, add compact orchestration records, correct full-plan dispatch, and split long plans into parent plans plus task packets. The problem is how to keep long plan execution auditable without loading the full plan, prior worker evidence, and accumulated chat context into every worker.

## Decision Drivers

* Preserve ADR 0026's orchestrator model and disjoint write-scope rule.
* Preserve ADR 0058's structured worker event logging without adding unowned run-log files.
* Reduce context pressure during long approved plan execution.
* Keep the full approved plan available to the orchestrator as the source of task order, dependencies, status, and approval evidence.
* Give workers enough context to implement, test, or review a specific task without inheriting unrelated task evidence.
* Keep plan templates maintainable for small plans.

## Considered Options

* Use task packets with role lanes and compact plan summaries
* Keep full-plan dispatch and chat-only records
* Add committed `.agents/runs/` records for every worker event
* Split every multi-task plan into child task-packet files

## Decision Outcome

Chosen option: "Use task packets with role lanes and compact plan summaries", because it resolves the full-plan dispatch contradiction while preserving approved plans, structured chat evidence, and one-commit-per-task execution.

Approved multi-task plans must define task packets for worker-owned tasks. A task packet is the default worker dispatch contract and includes the task id, goal, worker mode or lane, allowed inputs, forbidden inputs, write scope, dependencies, validation, stop conditions, and expected output.

The orchestrator owns the full approved plan. By default, a task worker receives only the plan header or readiness summary, the assigned task packet, and explicitly named governing artifacts or source files. The worker may load the full plan only when the task packet allows it or when a blocker requires broader plan review; the worker must report that escalation in the handoff.

Implementation workers remain the default lane. Optional testing and review lanes may be used when the task risk or validation design justifies the overhead. Review workers are read-only by default and receive the task packet, diff, relevant spec or ADR, and validation output. Testing workers own tests or validation investigation only, with an explicit write scope when they may edit tests.

Structured chat events remain required for worker `start`, `stop`, `fail`, and active-count changes under ADR 0058. The durable repository record is a compact plan-owned task result summary, not raw chat logs and not `.agents/runs/` files. Each task result summary records the worker id or lane, changed files or reviewed diff, validation evidence, blockers, review risks, and handoff notes. Plans must summarize or link task results instead of absorbing raw test output or full worker transcripts.

Long plans should keep the parent plan focused on approval, readiness, dependencies, execution graph, packet index, and compact task results. Use inline task packets for ordinary plans. Use child packet files only when the plan would become difficult to scan, such as plans with more than six worker-owned tasks, multiple parallel waves, or expected parent-plan length above roughly 200 lines after packeting. Child packet files must preserve stable task packet ids and remain referenced from the parent plan.

This decision does not authorize per-worker git worktrees, does not weaken one-commit-per-task rules, does not move backlog ownership out of `TASKS.md`, and does not create committed run-log directories.

### Consequences

* Good, because workers get a small, explicit contract instead of the full approved plan by default.
* Good, because the full plan remains the orchestrator-owned source of approval, order, dependencies, and state.
* Good, because high-risk work can use independent review or testing context without making those lanes mandatory for every task.
* Good, because durable evidence stays compact and repository-owned without introducing `.agents/runs/` retention and cleanup rules.
* Bad, because approved plans need more structure before implementation can start.
* Bad, because the orchestrator must maintain packet references and compact result summaries.

### Confirmation

After acceptance, confirm implementation through documentation review and validation:

* `.agents/references/planning.md` defines task packet requirements, long-plan split triggers, and worker dispatch defaults.
* `.agents/references/execution.md` defines worker role lanes, full-plan escalation, compact task result summaries, and chat event continuity under ADR 0058.
* `.agents/plans/README.md` documents inline and child task packet expectations.
* `.agents/plans/PLAN_TEMPLATE.md` includes a compact task packet section and result summary shape.
* `docs/proposals/README.md` and `PROP-multi-agent-context-packets` show accepted findings progressing from `open` to implemented states after the guidance lands.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.

## Pros and Cons of the Options

### Use task packets with role lanes and compact plan summaries

This option keeps approved plans as orchestrator-owned contracts while giving workers packet-level input.

* Good, because it directly implements `PROP-multi-agent-context-packets` F001, F002, F003, E001, and S001.
* Good, because it resolves the existing contradiction between task-shaped context and full-plan dispatch.
* Good, because it gives testing and review workers clear boundaries without requiring them for every task.
* Good, because it keeps durable records compact and avoids new generated log-file ownership.
* Bad, because it adds template and plan-authoring overhead.

### Keep full-plan dispatch and chat-only records

This option keeps the current workflow unchanged.

* Good, because it requires no template or guidance edits.
* Good, because every worker can see the complete approved plan without asking for broader context.
* Bad, because it preserves the context-pressure problem documented by `PROP-multi-agent-context-packets`.
* Bad, because it leaves planning guidance internally inconsistent.
* Bad, because long task execution evidence remains primarily in chat or a growing plan file.

### Add committed `.agents/runs/` records for every worker event

This option creates durable run logs with one record per worker action.

* Good, because worker events would be repository-owned and easier to inspect after chat history is lost.
* Good, because the same event fields from ADR 0058 could become a file format.
* Bad, because the repository would need retention, cleanup, commit, and privacy rules for generated logs.
* Bad, because it adds file churn before there is a proven need for committed run logs.
* Bad, because it does not by itself reduce worker input context.

### Split every multi-task plan into child task-packet files

This option requires every worker-owned task packet to live outside the parent plan.

* Good, because worker dispatch can point at a single small child file.
* Good, because parent plans stay short.
* Bad, because small plans would pay unnecessary file-management overhead.
* Bad, because task packet files could become backlog fragments if ownership rules are not kept tight.
* Bad, because cross-file plan review becomes harder for simple sequential work.

## More Information

- Source proposal: `docs/proposals/archive/PROP-multi-agent-context-packets-2026-05-21T00-06.md`.
- Related decisions: ADR 0023, ADR 0024, ADR 0026, ADR 0056, ADR 0058, ADR 0059, ADR 0060, and ADR 0061.
- After acceptance, update the ADR index status and implementation tracker in `docs/decisions/README.md`, then implement the guidance and template edits in `.agents/references/planning.md`, `.agents/references/execution.md`, `.agents/plans/README.md`, and `.agents/plans/PLAN_TEMPLATE.md`.
