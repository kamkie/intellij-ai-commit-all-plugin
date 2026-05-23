---
status: proposed
date: 2026-05-23
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Calibrate AI Execution Routing And Context Discipline

## Context and Problem Statement

ADR 0073 split direct execution, approved-plan execution, and orchestration into clearer owner files. A follow-up review found remaining friction in the hot path: `AGENTS.md` still implies one-off delegation needs user authorization, planning guidance can push small covered fixes into plans, task-packet templates still encourage broad default guidance reads, one-off worker briefs are underspecified, orchestrator duties are not operational enough for write delegation, event logging is heavy for read-only sidecars, and validation checks packet field presence more than packet quality.

How should the repository refine AI execution guidance so one-off work stays fast, delegated work stays bounded, and approved-plan execution remains auditable?

## Decision Drivers

* Keep direct one-off work cheap when existing ADRs, specs, or owner docs already define the desired behavior.
* Preserve ADR gates, plan approval gates, one-commit-per-plan-task rules, current-branch topology, and disjoint write scopes.
* Make one-off subagent use practical without requiring full approved-plan packet ceremony.
* Keep the active agent accountable as orchestrator whenever delegation is used.
* Reduce broad default context reads in task packets and worker briefs.
* Add validation that catches stale template placeholders and unsafe packet shapes without making simple drafts brittle.
* Keep human-facing request guidance aligned with AI-facing execution rules.

## Considered Options

* Calibrate routing, one-off briefs, packet budgets, orchestrator duties, event tiers, and validation.
* Keep ADR 0073 guidance unchanged.
* Require implementation plans for every behavior or code change.
* Allow free-form delegation without packet, brief, event, or validation structure.

## Decision Outcome

Chosen option: "Calibrate routing, one-off briefs, packet budgets, orchestrator duties, event tiers, and validation", because it preserves the accepted orchestration model while removing the remaining context and coordination overhead from small, well-governed work.

If accepted, implement these guidance changes:

1. Align `AGENTS.md` with ADR 0073 by saying one-off delegation is allowed when the environment supports it and no higher-priority or user instruction forbids it.
2. Add a routing matrix to `.agents/references/execution.md` and `.agents/references/planning.md`:
    * direct one-off for narrow changes covered by existing ADRs, specs, owner docs, or task refs;
    * plan for new intended behavior, multi-area changes, risky VCS, commit, push, AI generation, release, or compatibility work, unresolved decisions, or coordination-heavy work;
    * proposal for analysis, findings, simplification options, or maintainer triage before implementation.
3. Clarify that changing plugin behavior does not automatically require a new plan when the desired behavior is already decided and the implementation is narrow enough for a direct one-off loop.
4. Replace broad default task-packet context in `.agents/plans/PLAN_TEMPLATE.md` with explicit `Read first` and `Escalate to` guidance, so packets name exact source files and owner artifacts instead of defaulting to multiple guidance files.
5. Add a compact one-off worker brief shape to `.agents/references/orchestration.md` with label, lane, goal, read-first context, forbidden inputs, write scope, escalation triggers, stop conditions, and expected output.
6. Extend orchestrator responsibilities to include critical-path ownership, checking current worktree state before write delegation, reserving disjoint write scopes, and reconciling worker claims against the final diff and validation output.
7. Tier worker-event logging:
    * full structured `start`, `stop`, and `fail` events remain required for approved-plan workers and one-off write workers;
    * read-only one-off sidecars may use compact start and result summaries when no write scope or commit attribution is involved.
8. Extend `scripts/ai/validate-agent-artifacts.ps1` to catch deterministic packet-quality issues, including unchanged template placeholders, non-read-only write scope for `Lane: review`, and approved task packets whose context budget lacks concrete files, artifacts, or escalation conditions.
9. Update `docs/WORKING_WITH_AI.md` and `docs/DEVELOPMENT_LIFECYCLE.md` so human request shapes match the calibrated direct one-off, delegated one-off, approved-plan, and review-sidecar paths.

This decision does not change the ADR gate, plan approval gate, one-commit-per-approved-plan-task rule, single-branch topology, changelog ownership, or the requirement that parallel write workers have disjoint write scopes.

### Consequences

* Good, because agents can use sidecars for context-heavy one-off work without reading approved-plan orchestration details into the main thread.
* Good, because small fixes with existing decisions can stay direct instead of creating unnecessary plans.
* Good, because task packets become more executable and less likely to trigger broad context loading.
* Good, because orchestrator accountability becomes concrete enough to review delegated write work.
* Good, because validation catches packet/template drift before approved-plan execution starts.
* Bad, because execution routing rules become more nuanced and require careful wording to avoid bypassing plan gates.
* Bad, because event-tiering adds one more distinction for agents to apply correctly.

### Confirmation

After acceptance and plan approval, confirm implementation by checking:

* `AGENTS.md` no longer contradicts `.agents/references/orchestration.md` on default one-off delegation.
* `.agents/references/execution.md` and `.agents/references/planning.md` include the same routing matrix.
* `.agents/references/orchestration.md` includes the one-off worker brief shape, expanded orchestrator duties, and tiered event logging.
* `.agents/plans/PLAN_TEMPLATE.md` no longer encourages broad default packet context.
* `scripts/ai/validate-agent-artifacts.ps1` rejects stale packet placeholders and invalid review packet write scopes.
* `docs/WORKING_WITH_AI.md` and `docs/DEVELOPMENT_LIFECYCLE.md` describe the calibrated routing without making delegation mandatory.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1` passes.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Calibrate routing, one-off briefs, packet budgets, orchestrator duties, event tiers, and validation

This option is a focused follow-up to ADR 0073. It keeps the split owner model and improves the places where agents still pay too much context or coordination cost.

* Good, because it resolves the `AGENTS.md` and orchestration default-delegation mismatch.
* Good, because it gives one-off delegated work a lighter contract than approved-plan packets.
* Good, because it keeps final responsibility with the orchestrator instead of outsourcing integration judgment.
* Good, because validator checks can enforce the most mechanical packet-quality rules.
* Bad, because it touches several AI guidance and validation files.

### Keep ADR 0073 guidance unchanged

This option avoids further workflow churn.

* Good, because current guidance is functional and validated.
* Good, because no additional ADR or implementation work is needed.
* Bad, because the entry point still conflicts with default one-off delegation.
* Bad, because plan packets still invite broad default context reads.
* Bad, because simple covered changes can still be over-routed into planning.

### Require implementation plans for every behavior or code change

This option prioritizes maximum traceability.

* Good, because every behavior change has an explicit plan artifact.
* Good, because plan packets would make worker scope and validation visible.
* Bad, because it makes small decided fixes slower and increases context load.
* Bad, because it conflicts with the repository's direct one-off path for narrow work.

### Allow free-form delegation without packet, brief, event, or validation structure

This option prioritizes speed and flexibility.

* Good, because agents could spin up sidecars with minimal ceremony.
* Good, because exploratory work would be quick to start.
* Bad, because write scopes, context boundaries, and stop conditions would become implicit.
* Bad, because final integration and auditability would depend too much on chat memory.
* Bad, because it weakens the accepted orchestration and validation model.

## More Information

Related decisions:

* ADR 0023 requires one commit per task in approved multi-task plans.
* ADR 0026 requires one orchestrator and fresh task workers for plans.
* ADR 0058 defines orchestrator synchronization and chat logging.
* ADR 0059 defines worker plan and changelog handoffs.
* ADR 0061 keeps multi-agent execution on the current branch.
* ADR 0071 defines task packets for multi-agent plan execution.
* ADR 0072 extends agent artifact validation.
* ADR 0073 split execution and orchestration ownership and allowed one-off delegation by default.
* ADR 0074 requires companion draft plans when a requested change clearly needs both an ADR and later implementation plan.

Companion draft plan: `PLAN-execution-context-discipline`.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date, then implement the companion plan only after explicit plan approval.
