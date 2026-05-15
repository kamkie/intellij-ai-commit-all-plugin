---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Do Not Load All AI Instruction Files Automatically

## Context and Problem Statement

The repository has several AI-facing guidance files under `AGENTS.md`, `.agents/references/`, `.agents/plans/`, and human-facing workflow docs.

Loading all instruction files by default increases context size, raises the chance of stale or irrelevant guidance influencing the task, and makes small tasks slower to execute. The repository already encourages small task-shaped context, but the user requested an explicit rule not to load all AI instruction files automatically.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Do Not Load All AI Instruction Files Automatically
* Load every AI instruction file at task start.
* Load only `AGENTS.md` and never follow references.
* Rely on the existing small-context rule only.

## Decision Outcome

Chosen option: "Adopt Do Not Load All AI Instruction Files Automatically", because AI agents must not automatically load every AI instruction file.

AI agents must not automatically load every AI instruction file.

Agents should:

- Start with `AGENTS.md`.
- Use the guidance map and the current task to identify the smallest governing read set.
- Load only the specific owner documents needed for the task, such as planning, execution, testing, review, documentation, release, or code-style guidance.
- Use targeted search when ownership is unclear instead of bulk-reading all guidance files.

Agents may broaden the read set only when:

- The user asks for a broad guidance audit or repository-rule review.
- Targeted discovery shows another owner document is needed.
- Validation requires cross-document consistency checks.
- A new or conflicting rule must be reconciled across guidance files.

### Consequences

- AI context stays smaller and more task-specific.
- Agents are less likely to follow irrelevant guidance from unrelated workflows.
- Cross-document audits remain possible when explicitly requested or technically required.
- `AGENTS.md` remains the entry point and guidance map, not a trigger to preload every mapped file.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Do Not Load All AI Instruction Files Automatically

* Good, because AI agents must not automatically load every AI instruction file.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Load every AI instruction file at task start.

* Bad, because this is inefficient and can mix unrelated guidance into small tasks.

### Load only `AGENTS.md` and never follow references.

* Bad, because specific tasks need owner documents such as execution, testing, reviews, documentation, or release guidance.

### Rely on the existing small-context rule only.

* Bad, because the user requested an explicit no-bulk-loading rule.

## More Information

- Update `AGENTS.md` with the explicit rule.
- Update `.agents/references/execution.md` and `.agents/references/documentation.md`.
- Update `docs/WORKING_WITH_AI.md`.
- Update `TASKS.md` and `CHANGELOG.md`.
