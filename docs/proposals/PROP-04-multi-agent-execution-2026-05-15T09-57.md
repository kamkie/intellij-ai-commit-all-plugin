---
proposal_id: PROP-04-multi-agent-execution
generated_at: 2026-05-15T09-57
purpose: Consolidate multi-agent execution rule findings into a self-contained active work stream for maintainer triage.
scope: Orchestrator and worker guidance, commit trailers, plan metadata, execution graphs, changelog cadence, logging, and worktree topology.
supersedes:
    - PROP-orchestrator-worker-rules S1-S7
---

# Multi-Agent Execution Work Stream

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It is the active authoritative source for the changes proposed by `S001` through `S007`; superseded proposals are provenance only and are not needed to decide or implement these findings. This proposal is advisory and does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
    - [S001. Add a plan worker-count field](#s001-add-a-plan-worker-count-field)
    - [S002. Add worker, orchestrator, and agent-mode commit trailers](#s002-add-worker-orchestrator-and-agent-mode-commit-trailers)
    - [S003. Define orchestrator synchronization and logging](#s003-define-orchestrator-synchronization-and-logging)
    - [S004. Clarify worker plan-file update responsibility](#s004-clarify-worker-plan-file-update-responsibility)
    - [S005. Define orchestrator changelog update cadence](#s005-define-orchestrator-changelog-update-cadence)
    - [S006. Add a plan execution graph](#s006-add-a-plan-execution-graph)
    - [S007. Decide worktree versus single-branch execution topology](#s007-decide-worktree-versus-single-branch-execution-topology)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- This work stream owns the repository rules needed to make orchestrated and parallel agent work auditable without weakening ADR 0023, ADR 0024, ADR 0026, or ADR 0030.
- The current baseline permits an orchestrator with one fresh task worker per plan task, and permits parallel workers only when an approved plan marks tasks independent and assigns disjoint write scopes.
- The proposed changes add explicit worker count, commit attribution, synchronization, logging, plan-update ownership, changelog cadence details, execution graphs, and optional worktree topology rules.
- Each finding is self-contained. The superseded `PROP-orchestrator-worker-rules` proposal only explains provenance; it is not the decision source for active work.
- Each finding remains ADR-gated. Do not update `.gitmessage`, plan templates, validation scripts, execution guidance, planning guidance, release guidance, or changelog rules before the relevant ADR is accepted.
- `PROP-orchestrator-worker-rules S8` is intentionally excluded from this work stream because the author-empty proposal decision rule belongs to `PROP-01-proposal-governance`.

## Progress Tracker

| Id   | Title                                                    | Priority | Status | Decision |
|------|----------------------------------------------------------|----------|--------|----------|
| S001 | Add a plan worker-count field                            | 2        | open   | accepted |
| S002 | Add worker, orchestrator, and agent-mode commit trailers | 3        | open   | accepted |
| S003 | Define orchestrator synchronization and logging          | 4        | open   | accepted |
| S004 | Clarify worker plan-file update responsibility           | 3        | open   | accepted |
| S005 | Define orchestrator changelog update cadence             | 3        | open   | accepted |
| S006 | Add a plan execution graph                               | 4        | open   | accepted |
| S007 | Decide worktree versus single-branch execution topology  | 5        | open   | accepted |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current timestamp.
- Use `status` for implementation progress and `decision` for maintainer triage.
- Update the Proposal Implementation Summary in `docs/proposals/README.md` for accepted findings with non-terminal implementation status and an evidence path. A `TASKS.md` entry is optional when another evidence path is clearer.
- Leave completed, rejected, or superseded findings in place as history.

## Errors And Mistakes

_No tracked findings._

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

### S001. Add a plan worker-count field

- Evidence: `.agents/plans/PLAN_TEMPLATE.md` and `.agents/plans/README.md` do not require a `Workers:` field. ADR 0026 permits parallel execution only when the approved plan explicitly marks tasks independent and gives them disjoint write scopes, but the worker count is not declared in plan metadata. `scripts/validate-docs.ps1` validates plan structure but does not validate worker-count metadata. Archived `PLAN-fastest-plan-execution` already illustrates multiple workers without a corresponding plan field.
- Impact: Reviewers cannot identify planned parallel execution at a glance, and validation cannot catch plans that imply multiple workers without declaring them.
- Proposal: Author an ADR that defines `Workers:` as a required plan field.
    - Sequential plans use `Workers: 1`.
    - Parallel plans use `Workers: N (parallel, tasks: <task ids or labels>)`, where `N` is the maximum intended active worker count for the plan and the task list identifies the parallelized work.
    - Parallel values are valid only when the plan also marks the tasks independent and assigns disjoint write scopes under ADR 0026.
    - Update `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, `.agents/references/planning.md`, and `scripts/validate-docs.ps1`.
    - Backfill the field on every active and archived plan file that validation scans. If `PLAN-fastest-plan-execution` remains the example for multi-worker planning, backfill it with the worker count shown in its execution graph.
    - Validation should require presence of the field and should reject malformed values; deeper checks against the execution graph can be deferred to `S006`.

```yaml
status: open
decision: accepted
priority: 2
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S4; active decision details are maintained here."
```

### S002. Add worker, orchestrator, and agent-mode commit trailers

- Evidence: `.gitmessage` defines `Project-Source`, `Project-Plan`, `Project-Plan-Task`, `Project-Task`, `Project-Prompt`, `Co-authored-by`, `Refs`, and `Validation` metadata, but it does not record worker identity, orchestrator identity, or agent mode. ADR 0026 allows delegated task-worker commits without defining commit-history attribution for the worker or dispatcher.
- Impact: Multi-agent commits cannot be audited by worker, orchestrator, or execution mode. Post-hoc review must infer who did the work and which agent mode was used from chat context or plan prose.
- Proposal: Author an ADR that extends the AI-created commit metadata block with these trailers.
    - `Project-Worker: <worker-id>` is required on every commit authored by a task worker.
    - `Project-Orchestrator: <orchestrator-id>` is required on every commit produced under orchestrated multi-agent execution, whether authored by the orchestrator or by a worker.
    - `Project-Agent-Mode: <mode>` is required on every orchestrator and worker commit created in multi-agent execution.
    - Allowed mode values are `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, and `chat`; free-form values are rejected.
    - Worker and orchestrator identifiers stay in trailers and must not be added to the Conventional Commits subject line.
    - The new trailers remain contiguous with the existing project metadata footer block.
    - Update `.gitmessage` and the `## Commit Rules` section of `.agents/references/execution.md` after the ADR is accepted.
    - The ADR should reference ADR 0007 as the prior commit-message decision and ADR 0026 as the delegated-worker execution decision.

```yaml
status: open
decision: accepted
priority: 3
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S1; active decision details are maintained here."
```

### S003. Define orchestrator synchronization and logging

- Evidence: ADR 0026 and `.agents/references/execution.md` say the orchestrator starts the next task only after the current task is committed, and allows more than one worker only when an approved plan marks tasks independent with disjoint write scopes. They do not define a synchronization point for a parallel worker wave, and they do not require structured worker start, stop, or failure logging.
- Impact: In allowed parallel execution, it can be unclear when a wave is complete, which worker outputs were verified, and which workers were active at a given time.
- Proposal: Author an ADR that defines synchronization and logging rules for orchestrated multi-agent execution.
    - The orchestrator must wait for every worker in the current execution step to report success or failure before moving to the next step.
    - The orchestrator must verify each worker's committed result or commit-ready diff before advancing.
    - The ADR 0026 parallel exception stays narrow: parallel workers are still allowed only for independent tasks with disjoint write scopes.
    - The orchestrator must log a `start`, `stop`, or `fail` event for each worker.
    - The orchestrator must also log whenever the active worker count changes.
    - Each log entry includes ISO 8601 timestamp, event type, worker id, plan id, plan task id, agent mode, active worker count, and the list of active worker ids.
    - The ADR must choose the log destination: chat transcript only, or a durable file under `.agents/runs/<plan-id>/orchestrator.log`.
    - If durable logs are chosen, the ADR must define `.agents/runs/` ownership, retention, cleanup, and whether files are committed.
    - Update `.agents/references/execution.md`; if durable logs are chosen, also update `AGENTS.md` guidance map and any validation or documentation references needed for `.agents/runs/`.

```yaml
status: open
decision: accepted
priority: 4
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S2; active decision details are maintained here."
```

### S004. Clarify worker plan-file update responsibility

- Evidence: ADR 0023, ADR 0026, `.agents/references/execution.md`, `.agents/references/planning.md`, and `.agents/plans/PLAN_TEMPLATE.md` describe task boundaries and plan status tracking, but they do not unambiguously assign who updates the governing plan file after each worker finishes a task.
- Impact: Plan task checkboxes, status fields, progress notes, and status history can drift from actual execution state when neither the worker nor the orchestrator owns the update.
- Proposal: Author an ADR that defines plan-file update responsibility at worker handoff.
    - After completing a plan task, the worker updates the governing plan file for that task in the same commit as the task work.
    - If the worker cannot or should not update the plan file, the worker must explicitly hand off that responsibility to the orchestrator within the same execution step.
    - When responsibility is handed off, the orchestrator updates the plan file before dispatching the next task.
    - The handoff must be recorded in the chat transcript or in the durable orchestrator log if `S003` chooses one.
    - The plan file must reflect the completed, failed, blocked, or otherwise current task state before dependent work starts.
    - When the plan-file edit is a separate orchestrator commit, it must use the commit attribution trailers from `S002` if those have been accepted.
    - Update `.agents/references/execution.md` and `.agents/references/planning.md`.
    - The ADR should reference ADR 0023, ADR 0026, and the accepted plan status lifecycle rules.

```yaml
status: open
decision: accepted
priority: 3
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S5; active decision details are maintained here."
```

### S005. Define orchestrator changelog update cadence

- Evidence: ADR 0030 and `.agents/references/releases.md` already make the orchestrator responsible for `CHANGELOG.md` during orchestrated plan execution and release preparation. `.agents/references/execution.md` does not spell out the worker-handoff cadence, grouping exemption, or commit-attribution expectations for those changelog edits.
- Impact: `CHANGELOG.md` can lag behind task execution or become harder to reconcile with plan-task history when the cadence is only implied by release guidance.
- Proposal: Author an ADR or ADR 0030 follow-up that makes the changelog cadence explicit at the execution handoff boundary.
    - After every worker handoff for a task that produces a user-visible, contributor-visible, workflow-visible, compatibility, support, release, or validation-policy change, the orchestrator updates the next unreleased `CHANGELOG.md` section before dispatching the next task.
    - Purely internal tasks with no notable external or workflow effect may be grouped into one later entry.
    - When grouping is chosen, the orchestrator records the reason in the chat transcript or in the durable orchestrator log if `S003` chooses one.
    - The changelog edit rides along in the same task commit when feasible and when it does not break the one-commit-per-task boundary.
    - If a separate orchestrator commit is needed, it uses the commit attribution trailers from `S002` if those have been accepted.
    - Align this rule with `S004` so plan status and changelog review happen at the same handoff point.
    - Update `.agents/references/execution.md` and `.agents/references/releases.md`.
    - The ADR or follow-up should reference ADR 0030, ADR 0023, and ADR 0026.

```yaml
status: open
decision: accepted
priority: 3
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S6; active decision details are maintained here."
```

### S006. Add a plan execution graph

- Evidence: `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, and `.agents/references/planning.md` do not require an execution graph in plan files. ADR 0026 defines orchestrator and worker roles, and ADR 0023 defines the one-commit-per-task boundary, but plans do not have to visualize who executes which task, in which wave, under which orchestrator, and in which agent mode.
- Impact: Reviewers must infer parallelism, ownership, worker modes, and task sequencing from prose. Multi-agent plans are harder to validate against ADR 0026, the `Workers:` field from `S001`, commit trailers from `S002`, and logs from `S003`.
- Proposal: Author an ADR that adds an `Execution Graph` section to plan files.
    - Every plan includes an `Execution Graph` section.
    - The graph uses Mermaid or another fenced text format accepted by the ADR.
    - Each orchestrator node is labeled as `O<n>`.
    - Each worker node is labeled as `W<n>`.
    - Each worker node includes its planned agent mode from the `S002` vocabulary: `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, or `chat`.
    - The graph encodes task assignment by plan task id or stable task label.
    - The graph encodes wave or sequence ordering and orchestrator handoff edges.
    - Parallel waves shown in the graph must match the `Workers:` field from `S001` and the disjoint write scopes required by ADR 0026.
    - Update `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, `.agents/references/planning.md`, and `scripts/validate-docs.ps1`.
    - Validation must require the section in every plan file; deeper structural validation of nodes and edges may be deferred.
    - Backfill existing plan files that validation scans. `PLAN-fastest-plan-execution` is the known example whose existing graph can be brought into the required shape.

```yaml
status: open
decision: accepted
priority: 4
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S7; active decision details are maintained here."
```

### S007. Decide worktree versus single-branch execution topology

- Evidence: ADR 0003 defines all-files commit scope, ADR 0009 limits first-phase VCS support to Git, ADR 0023 requires one commit per plan task, and ADR 0026 permits parallel workers only when an approved plan marks tasks independent with disjoint write scopes. No accepted rule authorizes per-worker git worktrees, defines merge-back order, defines per-worktree validation timing, or explains how to preserve the one-commit-per-task boundary across worktrees.
- Impact: Parallel worktree execution can be interpreted inconsistently. One agent might use worktrees and accidentally weaken commit boundaries during merge-back; another might avoid worktrees even when they would be safe.
- Proposal: Defer implementation until a real approved plan needs worktrees, then author an ADR that chooses the allowed topology.
    - The ADR decides whether orchestrators may choose between a single branch and per-worker git worktrees, or whether this repository requires single-branch execution only.
    - If worktrees are allowed, they are allowed only for parallel tasks that the approved plan marks independent with disjoint write scopes under ADR 0026.
    - The ADR defines merge-back sequencing.
    - The ADR defines whether validation runs inside each worker worktree, after merge-back, or both.
    - The ADR defines how each plan task still lands as a distinct reviewed and validated unit under ADR 0023.
    - The ADR defines how uncommitted worker diffs, failed worker tasks, and conflict resolution are handed back to the orchestrator.
    - Update `.agents/references/execution.md`; if validation expectations change, also update `.agents/references/testing.md`.
    - The ADR should reference ADR 0003, ADR 0009, ADR 0023, and ADR 0026.

```yaml
status: open
decision: accepted
priority: 5
owner:
updated: 2026-05-18T01:43:37+02:00
accepted_at: 2026-05-18T01:43:37+02:00
comment: "Supersedes PROP-orchestrator-worker-rules S3; active decision details are maintained here."
```

## Smaller / Stylistic Items

- Keep `PROP-01-proposal-governance S002` as the owner for author-empty proposal decisions; do not duplicate that rule here.
- Treat this work stream as optional unless a new approved multi-task plan needs parallel execution, stronger attribution, or durable multi-agent auditability.
- Keep the archived `PROP-orchestrator-worker-rules` proposal as historical context only; active ADRs and implementation plans should cite this proposal for S001-S007 details.

## Suggested Priority Order

1. `S001` - add worker-count metadata first because later graph and topology rules depend on it.
2. `S002` - add commit attribution before durable logging, handoff commits, or worktree merge-back rules rely on actor identity.
3. `S004` and `S005` - define plan-status and changelog handoff ownership together.
4. `S003` - define synchronization and logging after attribution vocabulary is settled.
5. `S006` - require execution graphs after worker count and agent-mode vocabulary exist.
6. `S007` - decide worktree topology last, when an actual plan needs it.

## Out Of Scope

- Implementing worker orchestration tooling, daemons, process supervision, or runtime automation.
- Changing plugin source behavior.
- Changing proposal governance beyond the source finding already moved to `PROP-01-proposal-governance`.
- Updating `.gitmessage`, plan templates, validation scripts, execution references, planning references, release references, or changelog rules before the relevant ADR is accepted.
- Marketplace publication, release tagging, or CI changes that are not directly required by an accepted ADR from this work stream.
