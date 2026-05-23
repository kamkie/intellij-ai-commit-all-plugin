---
proposal_id: PROP-execution-loop-efficiency
generated_at: 2026-05-23T23-41
created_from: User request to analyze repository AI instruction files and propose execution-loop improvements for plans and one-off tasks.
purpose: Propose targeted AI execution-loop improvements for context management, subagents, and orchestrator responsibility.
scope: Covers `AGENTS.md`, `.agents/references/execution.md`, `.agents/references/planning.md`, `.agents/references/orchestration.md`, `.agents/plans/`, and human-facing AI workflow summaries.
---

# Execution Loop Efficiency Proposal

This proposal respects `AGENTS.md`, `docs/decisions/README.md`, `docs/proposals/README.md`, ADR 0073, and ADR 0075. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Creation Context](#creation-context)
- [Progress Tracker](#progress-tracker)
- [Proposal Items](#proposal-items)
    - [New Features](#new-features)
        - [F001. Add a read-only exploration lane](#f001-add-a-read-only-exploration-lane)
        - [F002. Add an orchestrator decision capsule for context-heavy work](#f002-add-an-orchestrator-decision-capsule-for-context-heavy-work)
    - [Errors And Mistakes](#errors-and-mistakes)
        - [E001. Parallel plan waves are weakened by sequential task wording](#e001-parallel-plan-waves-are-weakened-by-sequential-task-wording)
    - [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
        - [D001. Routing matrix duplication can drift between execution and planning guidance](#d001-routing-matrix-duplication-can-drift-between-execution-and-planning-guidance)
    - [Simplification Opportunities](#simplification-opportunities)
        - [S001. Move one-off context and delegation preflight ahead of context loading](#s001-move-one-off-context-and-delegation-preflight-ahead-of-context-loading)
        - [S002. Define local packet mode when delegation is unavailable](#s002-define-local-packet-mode-when-delegation-is-unavailable)
    - [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The current AI workflow is already much better than the older full-plan execution path: ADR 0073 split direct execution, approved-plan execution, and orchestration, while ADR 0075 calibrated routing, packet budgets, one-off briefs, and event logging.
- The remaining efficiency issues are narrower: some guidance still checks delegation after context loading, parallel plan waves are not fully reflected in sequential task wording, and read-only exploration is mentioned but not modeled as a first-class lane.
- The proposal keeps existing gates intact: ADR gates, plan approval, task packets, one commit per approved plan task, disjoint write scopes, and orchestrator-owned final review.
- The accepted findings are now implemented as clarifications and consolidation under ADR 0073 and ADR 0075.

## Creation Context

- Why this proposal exists: the user asked to analyze repository AI instruction files and propose changes in the plan and one-off execution loops to make them more efficient and capable, especially for context management, subagents, and orchestrator responsibility.
- How it was created: reviewed `AGENTS.md`, `.agents/references/execution.md`, `.agents/references/planning.md`, `.agents/references/orchestration.md`, `.agents/plans/README.md`, `.agents/plans/PLAN_TEMPLATE.md`, `.gitmessage`, `docs/WORKING_WITH_AI.md`, `docs/DEVELOPMENT_LIFECYCLE.md`, ADR 0073, ADR 0075, and the archived multi-agent context-packet proposal.
- Scope guardrails: this proposal does not weaken user approval gates, ADR requirements, plan approval requirements, one-task-one-commit rules for approved plans, single-branch topology, disjoint write scopes, validation requirements, or final orchestrator accountability.

## Progress Tracker

Compact overview only. The metadata table inside each finding remains the source of truth; this table mirrors statuses at a glance. Tracker mirroring, status and decision vocabulary, and Proposal Implementation Summary updates live in `docs/proposals/README.md`.

| Id   | Title                                                                  | Priority | Status | Decision |
|------|------------------------------------------------------------------------|----------|--------|----------|
| F001 | Add a read-only exploration lane                                       | 3        | done   | accepted |
| F002 | Add an orchestrator decision capsule for context-heavy work            | 3        | done   | accepted |
| E001 | Parallel plan waves are weakened by sequential task wording            | 3        | done   | accepted |
| D001 | Routing matrix duplication can drift between execution and planning    | 2        | done   | accepted |
| S001 | Move one-off context and delegation preflight ahead of context loading | 2        | done   | accepted |
| S002 | Define local packet mode when delegation is unavailable                | 2        | done   | accepted |

## Proposal Items

### New Features

#### F001. Add a read-only exploration lane

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:58:51+02:00 |
| Priority    | 3                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:58:51+02:00 |

##### Context

- Evidence: `.agents/references/orchestration.md:28` defines worker lanes, and `.agents/references/orchestration.md:33` and `.agents/references/orchestration.md:34` currently list `testing` and `review` after `implementation`; `.agents/references/orchestration.md:59` separately recommends read-only sidecars for focused codebase exploration. Exploration is useful but has no lane contract.
- Impact: Agents must fit codebase discovery into `review` or informal one-off sidecars. That makes exploration output less predictable and can mix fact-finding, validation, and review judgments in one context.
- Non-goals:
    - Do not make exploration workers mandatory for small tasks.
    - Do not allow exploration workers to edit files, update plans, or approve decisions.
    - Do not replace review or testing lanes.
- Acceptance criteria:
    - `.agents/references/orchestration.md` defines an `exploration` lane as read-only by default.
    - Exploration briefs require exact read-first inputs, forbidden broad scans, escalation triggers, and output with evidence-backed file or line references.
    - Planning and execution guidance allow exploration lanes for context-heavy one-off work and approved-plan preflight without dispatching full plans by default.

##### Recommended Change

Add a read-only `exploration` lane for repository fact-finding, source-map discovery, validation-log triage, or artifact lookup. The lane should return compact facts, relevant paths, confidence or uncertainty, and recommended next read sets, not implementation recommendations unless requested.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `.agents/references/orchestration.md`, `.agents/references/planning.md`, `.agents/plans/README.md`, `.agents/plans/PLAN_TEMPLATE.md`, `docs/DEVELOPMENT_LIFECYCLE.md`, and `docs/WORKING_WITH_AI.md`.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`, `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`, and `git diff --check`.

#### F002. Add an orchestrator decision capsule for context-heavy work

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:58:51+02:00 |
| Priority    | 3                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:58:51+02:00 |

##### Context

- Evidence: `.agents/references/orchestration.md:11` starts a detailed orchestrator ownership list, including write-scope checks, worker events, output review, and final reconciliation through `.agents/references/orchestration.md:22`. Result summaries are defined later in `.agents/references/orchestration.md:128`, but there is no compact preflight capsule that records route, gate status, read set, delegation decision, write scopes, and validation plan before context-heavy work starts.
- Impact: The orchestrator role is correct but operationally late. Agents can make route and context decisions implicitly, then report only final evidence. That reduces auditability when a broad one-off task or multi-worker plan later needs to explain why it loaded context locally, delegated, or avoided delegation.
- Non-goals:
    - Do not require a verbose capsule for tiny commands or one-file edits.
    - Do not create durable `.agents/runs/` logs.
    - Do not duplicate full task packets or plan result summaries.
- Acceptance criteria:
    - Orchestration guidance defines when a capsule is required: delegated work, context-pressure risk, broad audits, parallel waves, or write workers.
    - The capsule shape is compact: path, gates, read set, delegation plan or local reason, reserved write scopes, validation plan, and current blocker status.
    - The capsule lives in chat or the plan handoff, not in a new durable run-log tree.

##### Recommended Change

Add an optional but recommended "orchestrator decision capsule" before substantive context-heavy work. Use it to make the active agent's route, context, subagent, and validation decisions visible without loading or persisting bulky run records.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `.agents/references/orchestration.md`, `.agents/references/execution.md`, and `docs/DEVELOPMENT_LIFECYCLE.md`.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`, `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`, and `git diff --check`.

### Errors And Mistakes

#### E001. Parallel plan waves are weakened by sequential task wording

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:58:51+02:00 |
| Priority    | 3                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:58:51+02:00 |

##### Context

- Evidence: `.agents/references/planning.md:90` says each multi-task plan task must be fully implemented, validated, self-reviewed, and committed before the next task starts. `.agents/references/execution.md:78` and `docs/DEVELOPMENT_LIFECYCLE.md:112` repeat similar sequential wording. In contrast, `.agents/references/orchestration.md:103` allows parallel task workers when the plan marks independent tasks, disjoint write scopes, `Workers: N`, and a parallel execution graph, while `.agents/references/orchestration.md:107` defines synchronization for a parallel worker wave.
- Impact: The repository supports parallel approved-plan waves, but the core planning and execution loops still read as strictly task-by-task. Agents may over-serialize independent plan work, or they may run parallel workers while appearing to violate the "before the next task starts" rule.
- Non-goals:
    - Do not remove the one-commit-per-approved-plan-task rule.
    - Do not permit parallel write work without approved disjoint write scopes.
    - Do not allow dependent tasks to start before predecessor validation and review gates pass.
- Acceptance criteria:
    - Execution and planning guidance define the execution unit as either one task or one approved parallel wave.
    - Sequential plans still require task completion before the next task starts.
    - Parallel plans require every task in the current wave to finish, be validated, be self-reviewed or reviewed, and be committed or integrated according to commit rules before a dependent wave starts.

##### Recommended Change

Replace strict "before starting the next task" wording with "before starting the next dependent task or wave" and add a short parallel-wave closeout rule. The closeout should preserve per-task commits while allowing independent workers in the same approved wave to run concurrently.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `.agents/references/execution.md`, `.agents/references/planning.md`, `.agents/plans/PLAN_TEMPLATE.md`, and `docs/DEVELOPMENT_LIFECYCLE.md`.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`, `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`, and `git diff --check`.

### Duplications To Remove Or Reduce

#### D001. Routing matrix duplication can drift between execution and planning guidance

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:58:51+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:58:51+02:00 |

##### Context

- Evidence: `.agents/references/execution.md:19` and `.agents/references/planning.md:21` both define a `## Routing Matrix`. The content is currently aligned, and the duplication was useful during ADR 0075 implementation, but future edits must keep the two matrices synchronized.
- Impact: Routing drift is a high-leverage AI workflow bug. If execution says a task can stay direct while planning says it needs a plan, agents will either over-plan small work or bypass intended plan gates.
- Non-goals:
    - Do not remove plan-specific "When To Plan" guidance.
    - Do not hide the routing decision from agents creating plans.
- Acceptance criteria:
    - One file is named as the canonical owner for route selection.
    - The other file keeps only a short pointer plus owner-specific triggers.
    - Documentation validation or review catches stale concrete links after the edit.

##### Recommended Change

Make `.agents/references/execution.md` the canonical owner for the route matrix, then keep `.agents/references/planning.md` focused on "When To Plan" and plan readiness. Planning can link to the canonical matrix and list only extra plan-specific triggers.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented by making `.agents/references/execution.md` the canonical route-selection owner and replacing the duplicate planning matrix with a route pointer in `.agents/references/planning.md`.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`, `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`, and `git diff --check`.

### Simplification Opportunities

#### S001. Move one-off context and delegation preflight ahead of context loading

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:58:51+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:58:51+02:00 |

##### Context

- Evidence: `AGENTS.md:61` says to check thread size and compaction risk before broad exploration or edits. `.agents/references/orchestration.md:54` also says to check context pressure before substantive exploration or edits. However, the direct one-off loop in `.agents/references/execution.md:32` loads the smallest useful context before `.agents/references/execution.md:33` checks delegation triggers.
- Impact: The direct loop says "before heavy context loading" but places the delegation decision after a context-loading step. That ordering is easy to follow literally, especially during broad audits, and can push context into the main thread before deciding whether a read-only sidecar would be cheaper.
- Non-goals:
    - Do not require delegation for tiny direct tasks.
    - Do not ask the user for delegation permission when local execution is small and safe.
    - Do not load orchestration guidance for every one-off task.
- Acceptance criteria:
    - The direct one-off loop includes an early preflight before optional owner-doc loading.
    - The preflight checks route, gates, context-pressure proxies, delegation permission or limits, and likely read set.
    - Context-pressure proxies are concrete enough to apply when exact token or thread-size metrics are unavailable.

##### Recommended Change

Reorder the direct one-off loop so route, gate, context-pressure, and delegation preflight happen before loading anything beyond `AGENTS.md` and the first owner pointer. Define context-pressure proxies such as broad audit scope, multiple owner documents, archived plans or ADRs, long logs, validation output, recent compaction, or multiple independent research questions.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `.agents/references/execution.md`, with matching human-facing context in `docs/DEVELOPMENT_LIFECYCLE.md` and `docs/WORKING_WITH_AI.md`.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`, `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`, and `git diff --check`.

#### S002. Define local packet mode when delegation is unavailable

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:53:57+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-24T00:33:31+02:00 |

##### Context

- Evidence: `.agents/references/orchestration.md:40` says approved multi-task plans use one orchestrator and one fresh task worker per plan task when the environment supports delegation. `.agents/plans/PLAN_TEMPLATE.md:128` repeats that pattern. `.agents/references/orchestration.md:52` handles one-off tool-contract limits by allowing the agent to ask for permission or keep work local, but the approved-plan path does not explicitly define how to preserve packet boundaries when delegation is unavailable or not authorized.
- Impact: When subagents are unavailable, disabled, or not permitted by the active tool contract, agents may either ask the user unnecessarily or abandon packet boundaries. The efficient fallback should be obvious: run the same packet locally with the same read-first, escalation, write-scope, stop, validation, and result-summary rules.
- Non-goals:
    - Do not pretend local packet execution has the same isolation as a fresh worker.
    - Do not remove fresh-worker usage when delegation is available and permitted.
    - Do not change plan approval or commit rules.
- Acceptance criteria:
    - Orchestration and plan template guidance define "local packet mode" as the fallback for approved-plan task packets and one-off briefs.
    - Local packet mode keeps packet-approved context, escalation triggers, write scope, validation, and result summary requirements.
    - Handoffs state when delegation was unavailable, not authorized, or skipped because local execution was cheaper.

##### Recommended Change

Document local packet mode: the active agent remains the orchestrator but executes the assigned packet locally, preserving task boundaries and reporting that no fresh worker was used. Ask for delegation permission only when local packet execution would create material context or coordination risk.

##### Review Notes

- Superseded for approved-plan task execution by ADR 0080; local packet mode now
  applies only to direct one-off work.

##### Follow-Up

- Artifact: Originally implemented as a fallback clarification; ADR 0080 later
  narrowed local packet mode to direct one-off work and requires sub-agent
  workers for approved-plan task execution.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`, `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`, and `git diff --check`.

### Smaller / Stylistic Items

- Consider adding a short note that `Project-Agent-Mode` is repository attribution metadata, not necessarily the same as the current tool provider's role names. `.gitmessage:44`, `.agents/references/execution.md:135`, and `.agents/plans/README.md:108` list fixed mode values that can be confused with concrete runtime tool roles.
- Consider a one-line "tiny task exemption" under the decision capsule guidance so obvious one-command or one-file tasks do not pay checklist overhead.
- Keep any examples short. Longer examples should stay in proposal or archived-plan artifacts instead of the hot-path reference files.

## Suggested Priority Order

1. `S001` - done; context/delegation preflight now happens before optional broad context loading.
2. `S002` - done; superseded for approved-plan task execution by ADR 0080, with local packet mode retained only for direct one-off work.
3. `E001` - done; parallel-wave capability is reconciled with sequential task wording.
4. `F001` - done; read-only exploration is now a worker lane.
5. `F002` - done; the orchestrator decision capsule is documented for context-heavy or delegated work.
6. `D001` - done; route selection is canonical in `.agents/references/execution.md`.

## Out Of Scope

- Creating a new ADR for these clarifications.
- Editing `.agents/prompts/` or `.agents/skills/`.
- Changing plugin runtime behavior, tests, Gradle configuration, CI, Marketplace release behavior, or public user documentation.
- Committing the proposal.
