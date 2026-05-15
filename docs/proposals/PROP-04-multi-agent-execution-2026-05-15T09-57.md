---
proposal_id: PROP-04-multi-agent-execution
generated_at: 2026-05-15T09-57
purpose: Consolidate multi-agent execution rule findings into the lowest-priority active work stream for maintainer triage.
scope: Orchestrator and worker guidance, commit trailers, plan metadata, execution graphs, changelog cadence, logging, and worktree topology.
supersedes:
    - PROP-orchestrator-worker-rules S1-S7
---

# Multi-Agent Execution Work Stream

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It consolidates multi-agent execution findings for maintainer triage only; it does not implement changes by itself.

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

- This work stream is intentionally sorted after product UX and repository release hygiene because the current backlog is empty.
- It consolidates `PROP-orchestrator-worker-rules` `S1` through `S7`; the original `S8` author-empty decision rule moved to `PROP-01-proposal-governance`.
- Each finding should remain ADR-gated. None should update `.gitmessage`, plan templates, execution guidance, validation, or changelog rules before the relevant ADR is accepted.

## Progress Tracker

| Id   | Title                                                    | Priority | Status | Decision |
|------|----------------------------------------------------------|----------|--------|----------|
| S001 | Add a plan worker-count field                            | 2        | open   |          |
| S002 | Add worker, orchestrator, and agent-mode commit trailers | 3        | open   |          |
| S003 | Define orchestrator synchronization and logging          | 4        | open   |          |
| S004 | Clarify worker plan-file update responsibility           | 3        | open   |          |
| S005 | Define orchestrator changelog update cadence             | 3        | open   |          |
| S006 | Add a plan execution graph                               | 4        | open   |          |
| S007 | Decide worktree versus single-branch execution topology  | 5        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed, rejected, or superseded findings in place as history.

## Errors And Mistakes

_No tracked findings._

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

### S001. Add a plan worker-count field

- Evidence: `PROP-orchestrator-worker-rules` `S4` found that plans do not declare worker count even though ADR 0026 allows parallel workers only under explicit conditions.
- Impact: Reviewers cannot identify parallel execution intent at a glance.
- Proposal: Add an ADR-gated `Workers:` field to plan templates and validation, then backfill existing active plans where needed.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S4."
```

### S002. Add worker, orchestrator, and agent-mode commit trailers

- Evidence: `PROP-orchestrator-worker-rules` `S1` found no commit-message trailers for worker identity, orchestrator identity, or agent mode.
- Impact: Multi-agent commits cannot be audited by worker, orchestrator, or execution mode.
- Proposal: ADR-gate new trailers such as `Project-Worker`, `Project-Orchestrator`, and `Project-Agent-Mode`, then update `.gitmessage` and execution guidance.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S1."
```

### S003. Define orchestrator synchronization and logging

- Evidence: `PROP-orchestrator-worker-rules` `S2` found no durable rule for waiting on a parallel worker wave or logging worker start/stop/fail events.
- Impact: Multi-agent execution can be hard to reconstruct after the fact.
- Proposal: ADR-gate synchronization rules and choose a log destination, either chat transcript only or durable `.agents/runs/<plan-id>/` logs.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S2."
```

### S004. Clarify worker plan-file update responsibility

- Evidence: `PROP-orchestrator-worker-rules` `S5` found no accepted rule assigning plan-file status updates to workers or orchestrators after each task.
- Impact: Plan files can drift from actual execution state in multi-agent runs.
- Proposal: ADR-gate a handoff rule: the worker updates the plan with the task commit, or explicitly delegates the update to the orchestrator before the next task starts.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S5."
```

### S005. Define orchestrator changelog update cadence

- Evidence: `PROP-orchestrator-worker-rules` `S6` found that ADR 0030 assigns changelog ownership to the orchestrator but does not define per-task cadence.
- Impact: `CHANGELOG.md` can lag behind task execution and become harder to reconcile before release.
- Proposal: ADR-gate a cadence rule: after each worker handoff that produces user-visible or workflow-visible change, update the unreleased changelog before dispatching the next task.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S6."
```

### S006. Add a plan execution graph

- Evidence: `PROP-orchestrator-worker-rules` `S7` found no required execution graph that maps workers, orchestrators, modes, waves, and task assignments.
- Impact: Parallelism and task ownership must be inferred from prose.
- Proposal: ADR-gate an `Execution Graph` section in plan files after `S001` and `S002` settle the worker-count field and agent-mode vocabulary.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S7."
```

### S007. Decide worktree versus single-branch execution topology

- Evidence: `PROP-orchestrator-worker-rules` `S3` found no accepted rule for worker git worktrees, merge-back ordering, or per-worktree validation.
- Impact: Parallel worktree execution could accidentally weaken ADR 0023's one-commit-per-plan-task boundary.
- Proposal: Defer until a plan needs it, then ADR-gate when worktrees are allowed, how merge-back works, and how validation is recorded.

```yaml
status: open
decision:
priority: 5
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S3."
```

## Smaller / Stylistic Items

- Keep `PROP-01-proposal-governance S002` as the owner for author-empty proposal decisions; do not duplicate it here.
- Treat this work stream as optional unless a new approved multi-task plan needs parallel execution.

## Suggested Priority Order

1. `S001` - smallest useful metadata field for future plans.
2. `S002` - commit attribution before durable logging or graphs.
3. `S004` and `S005` - handoff ownership and changelog cadence.
4. `S003` - synchronization and logging after attribution is defined.
5. `S006` - execution graph after worker count and modes exist.
6. `S007` - worktree topology last, when an actual plan needs it.

## Out Of Scope

- Implementing worker orchestration tooling.
- Changing plugin source behavior.
- Changing proposal governance beyond the source findings already moved to `PROP-01-proposal-governance`.
- Updating `.gitmessage`, plan templates, validation scripts, or execution references before the relevant ADR is accepted.
