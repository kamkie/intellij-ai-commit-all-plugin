---
status: accepted
date: 2026-05-23
accepted_at: 2026-05-23T23:25:54+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Use Governed Agent Learning Capture

## Context and Problem Statement

The repository received a generic autonomous-agent learning draft that proposed
mandatory retrospectives and a new `.ai/` memory tree. The intent is useful:
agents should preserve validated operational lessons and avoid repeated
mistakes. The proposed storage and always-write workflow conflict with the
repository's existing governed AI-agent artifact model.

How should the repository capture reusable AI-agent learning while preserving
the current `.agents/` ownership model, small-context rule, proposal triage, and
ADR gates?

## Decision Drivers

* Keep durable AI-agent workflow guidance in `.agents/references/`.
* Keep reusable task-specific workflows in `.agents/skills/`.
* Keep reusable prompt recipes in `.agents/prompts/`.
* Avoid a parallel `.ai/` memory tree that duplicates existing owners.
* Persist only validated, reusable lessons with clear evidence.
* Avoid documentation churn from mandatory write-backs after routine tasks.
* Preserve the ADR gate for repository rule and workflow changes.
* Preserve the smallest task-shaped context rule.

## Considered Options

* Use governed agent learning capture in existing owners.
* Add the scratch draft's `.ai/` memory tree.
* Keep learning only in chat handoffs.
* Make every task end with a persistent retrospective.

## Decision Outcome

Chosen option: "Use governed agent learning capture in existing owners", because
it preserves the repository's current artifact ownership model while still
giving agents a concrete path for validated reusable lessons.

If accepted:

* Do not create `.ai/` memory files for this repository.
* Treat learning capture as an intake check, not a mandatory write after every
  task.
* Persist a lesson only when it is observed directly or reproduced, validated,
  reusable beyond the current task, free of secrets or personal data, and not a
  duplicate of an existing owner artifact.
* Map reusable lessons to existing owners:
  `.agents/references/` for durable AI-agent workflow guidance,
  `.agents/skills/` for reusable task-specific workflows,
  `.agents/prompts/` for narrow prompt recipes, `.agents/plans/` or `TASKS.md`
  for active implementation sequencing, `docs/proposals/` for advisory triage,
  and `docs/decisions/` for repository rule changes.
* Keep temporary debugging observations in handoff notes, validation reports,
  `TASKS.md`, or no persistent artifact unless they become reusable
  troubleshooting guidance.
* Require agents to choose the owner artifact before editing and search the
  likely owner for duplicates.
* Keep the existing rule that repository workflow changes require ADR review
  and acceptance before implementation.
* Add the implementation guidance to `.agents/references/execution.md`, or to a
  focused `.agents/references/learning.md` if the implementation proves large
  enough to deserve a separate owner.

### Consequences

* Good, because the repository gains a clear learning intake workflow without a
  second memory hierarchy.
* Good, because high-confidence lessons can become durable guidance while
  speculative or task-local observations stay out of permanent docs.
* Good, because future agents have a concrete owner mapping instead of choosing
  between overlapping storage locations.
* Bad, because agents still need judgment to decide whether a lesson is
  reusable and validated.
* Bad, because the workflow adds a small review step after failures, repeated
  retries, CI issues, or user corrections.

### Confirmation

After acceptance, confirm implementation by checking:

* `.agents/references/execution.md` or a focused `.agents/references/learning.md`
  contains a `Learning Capture` section.
* The guidance says not to create a `.ai/` memory tree.
* The guidance requires evidence, validation, deduplication, and owner selection
  before persistent learning edits.
* The guidance maps lesson types to existing repository artifacts.
* The guidance preserves the ADR gate for repository rule changes.
* `PROP-agent-learning-memory` findings are updated with implementation
  evidence.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`
  passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Use governed agent learning capture in existing owners

This option turns learning into a validation-gated intake workflow and stores
durable knowledge only in existing repository-owned artifacts.

* Good, because it follows `AGENTS.md` and `.agents/references/documentation.md`.
* Good, because it avoids duplicate memory locations.
* Good, because it supports future improvement without requiring a write-heavy
  retrospective loop.
* Bad, because it is more constrained than the scratch draft's generic memory
  model.

### Add the scratch draft's `.ai/` memory tree

This option would add `.ai/learnings.md`, `.ai/patterns.md`,
`.ai/failures.md`, and `.ai/workflows.md`.

* Good, because it gives learning documents obvious names.
* Bad, because it duplicates `.agents/references/`, `.agents/skills/`, and
  `.agents/prompts/`.
* Bad, because agents would need to decide between overlapping governance
  sources.
* Bad, because it would increase metadata churn in a repository that already has
  explicit artifact owners.

### Keep learning only in chat handoffs

This option avoids durable repository changes and leaves lessons in session
summaries.

* Good, because it avoids documentation churn.
* Bad, because useful project-specific knowledge may be lost after compaction or
  future sessions.
* Bad, because repeated failures would remain more likely.

### Make every task end with a persistent retrospective

This option would require agents to write a persistent learning note after each
task.

* Good, because it maximizes capture opportunities.
* Bad, because routine tasks would create low-value documentation churn.
* Bad, because it risks storing speculative or one-off observations.
* Bad, because it conflicts with the repository's smallest-context and
  smallest-owner editing rules.

## More Information

This ADR accepts the direction of `PROP-agent-learning-memory` while preserving
the repository's existing AI-agent documentation owners.

No companion implementation plan is used because the implementation is a
bounded AI-agent documentation edit.

After this ADR is accepted, update the ADR Implementation Tracker in
`docs/decisions/README.md` with implementation status, evidence, and last
updated date.
