---
status: accepted
date: 2026-05-25
accepted_at: 2026-05-25T01:48:18+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Reread Agent Entry Guidance After Compaction

## Context and Problem Statement

`AGENTS.md` already tells agents to keep context small and check compaction risk before broad exploration or edits. It does not yet say what an agent must reread after an actual context compaction, resume, or summarized handoff. Without an explicit recovery rule, an agent can continue from a lossy summary, miss newer user instructions, or apply stale workflow assumptions to governed repository work.

How should repository guidance require agents to recover task and rule context after compaction without encouraging broad rereads that increase context pressure again?

## Decision Drivers

* Preserve the smallest task-shaped context model.
* Make post-compaction recovery explicit enough to avoid stale or ghost task execution.
* Keep the current user request and repository entry point authoritative after a resume.
* Avoid bulk-loading all AI guidance after every compaction.
* Keep owner-document rereads proportional to the artifact being edited.
* Preserve ADR gates, plan gates, delegation rules, dirty-worktree care, and validation expectations.

## Considered Options

* Add a targeted after-compaction reread rule to `AGENTS.md` and execution guidance.
* Reread all repository guidance after every compaction.
* Rely on compaction summaries and existing context-discipline rules.
* Add no durable rule and handle rereads ad hoc in each task.

## Decision Outcome

Chosen option: "Add a targeted after-compaction reread rule to `AGENTS.md` and execution guidance", because it gives agents a clear recovery path while preserving the repository's narrow-context workflow.

If accepted:

* `AGENTS.md` `## Working Rules` must say that after context compaction, resume, or summarized handoff, agents reread the latest user request, `AGENTS.md`, and the most specific governing artifact needed for the next action before continuing.
* The rule must require agents to reconcile the resumed task against any local file changes, in-progress validation, active plan or ADR gates, and newer user instructions before editing or handing off.
* The rule must say not to bulk-load every guidance file after compaction; use the guidance map and artifact lookup rules to reread only the owner documents needed for the current artifact or next action.
* `.agents/references/execution.md` must include the same operational recovery rule in the execution loop.
* If compaction occurs during delegated or context-heavy work, `.agents/references/orchestration.md` may clarify that the orchestrator should reread the decision capsule, worker outputs, and only the owner artifacts needed to resume integration.
* Human-facing documentation does not need to change unless the implementation changes how users should ask for work.

This decision does not change the ADR gate, plan approval gate, approved-plan sub-agent requirement, standing delegation approval, validation command ownership, or the rule that agents must not revert unrelated user changes.

### Consequences

* Good, because resumed agents have a concrete checklist before continuing work.
* Good, because the latest user request is explicitly rechecked before final answers or edits.
* Good, because agents reread durable entry and owner guidance without reloading the whole guidance tree.
* Good, because post-compaction work is more likely to preserve governance gates and dirty-worktree constraints.
* Bad, because each resumed task pays a small reread cost even when the compaction summary is accurate.
* Bad, because the rule adds one more workflow step that agents can apply too broadly if wording is not tight.

### Confirmation

After acceptance, confirm implementation by checking:

* `AGENTS.md` `## Working Rules` contains the targeted after-compaction reread rule.
* `.agents/references/execution.md` includes the same operational recovery rule.
* `.agents/references/orchestration.md` is updated only if delegated or context-heavy recovery needs owner-specific detail.
* The wording preserves the "smallest task-shaped context" rule and does not require broad guidance rereads.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Add a targeted after-compaction reread rule to `AGENTS.md` and execution guidance

This option records the recovery rule at the entry point and in the execution loop, while leaving detailed owner context to the existing guidance map.

* Good, because it addresses the missing post-compaction behavior directly.
* Good, because it keeps rereads scoped to the next action.
* Good, because it reduces the risk of stale plan, ADR, or validation assumptions after summary handoff.
* Bad, because it requires a small governed documentation update after acceptance.

### Reread all repository guidance after every compaction

This option maximizes certainty by reloading all available AI guidance after a resume.

* Good, because agents are unlikely to miss a standing repository rule.
* Bad, because it conflicts with ADR 0031 and the repository's context-discipline model.
* Bad, because it can immediately recreate the context pressure that caused compaction.
* Bad, because broad rereads make simple resumed tasks slower and noisier.

### Rely on compaction summaries and existing context-discipline rules

This option leaves current guidance unchanged.

* Good, because no additional workflow rule is needed.
* Bad, because summaries may omit the newest user request, local worktree state, or a governing gate that matters to the next action.
* Bad, because agents do not have an explicit trigger for rereading `AGENTS.md` after compaction.

### Add no durable rule and handle rereads ad hoc in each task

This option lets each agent decide whether a reread is needed.

* Good, because it avoids adding process text.
* Bad, because post-compaction recovery remains inconsistent.
* Bad, because future agents may continue from stale summarized context when the task touches governed files.

## More Information

Related decisions:

* ADR 0031 says agents must not load all AI instruction files automatically.
* ADR 0073 optimizes AI execution paths and orchestration context.
* ADR 0075 calibrates execution routing and context discipline.
* ADR 0080 records standing sub-agent delegation approval and approved-plan worker requirements.

No companion implementation plan is used because the implementation is a bounded AI-agent documentation update after acceptance.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date.
