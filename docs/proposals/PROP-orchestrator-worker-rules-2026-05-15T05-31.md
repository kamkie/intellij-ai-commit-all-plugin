---
proposal_id: PROP-orchestrator-worker-rules
generated_at: 2026-05-15T05-31
purpose: Propose orchestrator and worker rules for multi-agent execution of approved multi-task plans, expressed as ADR-gated findings for maintainer triage.
scope: AI workflow guidance, commit-message schema, plan template, and the docs validation script; no source code under `src/`.
---

# Orchestrator And Worker Rules For Multi-Agent Execution

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself. Per `AGENTS.md` ("create the ADR first and stop") and ADR 0002, every change here is gated on new ADRs being authored and accepted before any guidance, template, `.gitmessage`, or validation script edit is made.

This proposal supersedes the earlier draft at `.agents/prompts/PROMPT-orchestrator-worker-rules.md`.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
    - [S1. Commit-message schema extension for multi-agent execution (ADR A)](#s1-commit-message-schema-extension-for-multi-agent-execution-adr-a)
    - [S2. Orchestrator synchronization and logging (ADR B)](#s2-orchestrator-synchronization-and-logging-adr-b)
    - [S3. Execution topology — git worktrees vs single branch (ADR C)](#s3-execution-topology--git-worktrees-vs-single-branch-adr-c)
    - [S4. Plan worker-count field (ADR D)](#s4-plan-worker-count-field-adr-d)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- Propose a coherent set of orchestrator/worker rules for multi-agent execution of accepted multi-task plans, layered on top of ADR 0023, ADR 0024, and ADR 0026 without weakening them.
- Group the seven user-requested rules into four ADRs (A–D) so commit-schema, orchestrator behavior, execution topology, and plan-level worker declaration can each be reviewed independently.
- No implementation is performed by this proposal; it is advisory until each ADR is authored and accepted and the dependent guidance, template, and validation updates are merged.

## Progress Tracker

| Id | Title                                                     | Priority | Status | Decision |
|----|-----------------------------------------------------------|----------|--------|----------|
| S1 | Commit-message schema extension for multi-agent execution | 3        | open   |          |
| S2 | Orchestrator synchronization and logging                  | 4        | open   |          |
| S3 | Execution topology — git worktrees vs single branch       | 5        | open   |          |
| S4 | Plan worker-count field                                   | 2        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

_No tracked findings._

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

### S1. Commit-message schema extension for multi-agent execution (ADR A)

- Evidence: `.gitmessage` and `.agents/references/execution.md` define a contiguous project metadata footer block (`Project-Source`, `Project-Plan`, `Project-Plan-Task`, `Project-Task`, `Validation`) but record no worker identity, no dispatching orchestrator identity, and no agent mode. ADR 0007 is the only prior commit-message ADR. ADR 0026 allows delegated worker commits but provides no way to attribute them in commit history.
- Impact: When multi-agent execution is used, commits cannot be traced back to a specific worker, a specific orchestrator, or the agent mode that produced them, which blocks per-worker audit, blame, and post-hoc validation.
- Proposal: Author one ADR (ADR A) that:
    - Adds `Project-Worker: <worker-id>` (Rule 3): required on every commit authored by a worker; lives in the contiguous footer block.
    - Adds `Project-Orchestrator: <orchestrator-id>` (Rule 4): required on every commit produced under an orchestrator in multi-agent mode, whether the orchestrator or a worker authors it; identifiers stay in trailers only and never in the Conventional Commits subject line.
    - Adds `Project-Agent-Mode: <mode>` (Rule 5): required on every orchestrator and worker commit, with an enumerated vocabulary (`code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, `chat`); free-form values are rejected.
    - References ADR 0007 as the prior commit-message ADR.
    - After acceptance, update `.gitmessage` (template + example block) and the `## Commit Rules` section of `.agents/references/execution.md` in a single follow-up change.

```yaml
status: open
decision: accepted
priority: 3
owner:
updated: 2026-05-15
comment:
```

### S2. Orchestrator synchronization and logging (ADR B)

- Evidence: `.agents/references/execution.md` (`Orchestrator And Task Workers`) and ADR 0026 say the orchestrator "starts the next task only after the current task is committed" and "runs only one task worker at a time unless the approved plan explicitly marks tasks as independent and gives them disjoint write scopes". There is no rule for waiting on multiple parallel workers in the same step, and no rule that the orchestrator must log start/stop events for workers.
- Impact: In the allowed parallel case, the orchestrator's synchronization point is implicit, and observability of which workers were active at which time is limited to ad hoc chat transcripts. This makes it hard to reconstruct what happened during a multi-worker wave.
- Proposal: Author one ADR (ADR B) that:
    - Codifies orchestrator synchronization (Rule 1): the orchestrator must wait for every worker in the current execution step to report completion (success or failure) and have its commit or commit-ready diff verified before proceeding; the ADR 0026 parallel exception is preserved.
    - Codifies orchestrator logging (Rule 2): the orchestrator must log a `start` / `stop` / `fail` event per worker, plus an event whenever the active worker count changes; each entry includes ISO 8601 timestamp, event type, worker id, plan id, plan task id, agent mode, active worker count, and the list of active worker ids.
    - Decides log destination explicitly: chat transcript only, or a durable file under `.agents/runs/<plan-id>/orchestrator.log`. If a durable file is chosen, the ADR also defines ownership and lifecycle of `.agents/runs/`.
    - References ADR 0026 and does not weaken its parallel-execution exception.
    - After acceptance, extend `.agents/references/execution.md` (`Orchestrator And Task Workers`) and, if file-backed, document `.agents/runs/` in `.agents/references/execution.md` and `AGENTS.md` guidance map.

```yaml
status: open
decision: accepted
priority: 4
owner:
updated: 2026-05-15
comment:
```

### S3. Execution topology — git worktrees vs single branch (ADR C)

- Evidence: ADR 0009 (git-only with multiple roots), ADR 0003 (all-files commit scope), ADR 0023 (commit-per-plan-task), and ADR 0026 together describe single-branch sequential execution with a narrow parallel exception. No accepted decision authorizes per-worker git worktrees, and no rule defines merge-back ordering, per-worktree validation, or how the commit-per-plan-task guarantee is preserved across worktrees.
- Impact: Without an explicit decision, choosing worktrees for parallel workers either (a) silently violates ADR 0023's per-task commit boundary after merge-back, or (b) blocks the use of worktrees at all, depending on the agent's interpretation. The resulting ambiguity is worse than either rule.
- Proposal: Author one ADR (ADR C) that:
    - Codifies execution topology (Rule 6): the orchestrator may choose between running workers in separate git worktrees or in a single branch.
    - Restricts worktree usage to the cases where the approved plan marks the parallelized tasks as independent with disjoint write scopes, consistent with ADR 0026.
    - Defines merge-back sequencing, per-worktree validation timing (per worktree vs after merge), and preserves the one-commit-per-plan-task rule from ADR 0023 after merge-back.
    - References ADR 0003, ADR 0009, ADR 0023, and ADR 0026.
    - After acceptance, update `.agents/references/execution.md` (`Orchestrator And Task Workers`) with the topology choice and, if needed, `.agents/references/testing.md` for per-worktree validation.

```yaml
status: open
decision: accepted
priority: 5
owner:
updated: 2026-05-15
comment:
```

### S4. Plan worker-count field (ADR D)

- Evidence: `.agents/plans/PLAN_TEMPLATE.md` and `.agents/plans/README.md` do not require a worker-count field. ADR 0026 requires plans to mark tasks independent before parallel execution, but the per-plan worker count is implicit. `scripts/validate-docs.ps1` does not validate any such field. `PLAN-fastest-plan-execution.md` already describes 14 illustrative workers in its Before/After graph without a corresponding plan front-matter field.
- Impact: Plans cannot be reviewed for parallel execution intent at a glance, and the validation script cannot catch plans that are silently assumed to be multi-worker. This is the lowest-cost change that immediately tightens ADR 0026.
- Proposal: Author one ADR (ADR D) that:
    - Codifies a required plan field (Rule 7): every plan declares `Workers: 1` (default, sequential) or `Workers: N (parallel, tasks: T-…, T-…)`.
    - Requires `scripts/validate-docs.ps1` to validate presence and shape of the field on every plan file.
    - Requires `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, and `.agents/references/planning.md` to describe the field and its semantics.
    - References ADR 0026 and ADR 0032 / ADR 0036 / ADR 0038 for plan-id conventions.
    - After acceptance, update `PLAN_TEMPLATE.md`, `.agents/plans/README.md`, `.agents/references/planning.md`, and `scripts/validate-docs.ps1` in a single follow-up change, and backfill the field on existing plans (including `PLAN-fastest-plan-execution.md`).

```yaml
status: open
decision: accepted
priority: 2
owner:
updated: 2026-05-15
comment:
```

## Smaller / Stylistic Items

- The After view of the `Orchestrator And Workers Graph` in `PLAN-fastest-plan-execution.md` already illustrates 14 workers; once S4 is accepted, the plan should declare `Workers: 14 (parallel by wave, tasks: as labeled W1–W14)` to make the graph and the front matter consistent.
- Each ADR must follow `docs/decisions/ADR_TEMPLATE.md`, use the `ard-NNNN-...md` filename pattern (ADR 0039), and record decision-maker identity (ADR 0040). Plan approvals stay subject to ADR 0041 and ADR 0042.
- `CHANGELOG.md` should record any accepted workflow change under the next unreleased section, per the release-preparation guidance.

## Suggested Priority Order

1. `S4` — smallest blast radius (one ADR plus a plan-template field, README note, planning reference, and validation script update); unblocks plan-level worker count for every later change.
2. `S1` — commit-message schema; small, localized to `.gitmessage` and one `execution.md` section, and makes S2/S3 trails observable.
3. `S2` — orchestrator synchronization and logging; depends on S1 to record agent mode and worker identity in commits, and on a destination decision for the log.
4. `S3` — execution topology; largest cross-document impact (touches ADR 0023's commit boundary), so do it last and after S2 has clarified what the orchestrator must observe across worktrees.

## Out Of Scope

- Implementing any orchestrator runtime, daemon, or process supervisor.
- Changes to source code under `src/`.
- Any modification to existing accepted ADRs; new behavior must be introduced via new ADRs (A–D) that reference the existing ones.
- Touching `.gitmessage`, `.agents/references/execution.md`, `.agents/references/planning.md`, `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, `scripts/validate-docs.ps1`, or `CHANGELOG.md` before the matching ADR is accepted.
- Release tagging, Marketplace publishing, or CI changes triggered by this proposal.
