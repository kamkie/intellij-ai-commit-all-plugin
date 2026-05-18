---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Import Lightweight AI Guidance Model

## Context and Problem Statement

The repository needed AI-agent guidance before implementation work started. A mature source repository at `https://github.com/kamkie/technical-interview-demo` had useful guidance patterns, but it was a production Spring application with release, operations, API, and CI concerns that do not fit this IntelliJ Platform plugin repository.

This repository is still intentionally small, so importing the whole source guidance corpus would add process and unrelated assumptions.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Import Lightweight AI Guidance Model
* Import the full source guidance corpus.
* Keep only `AGENTS.md` without reference guides.
* Keep `AI_GUIDELINES_IMPORT_PROPOSAL.md` as the durable record.

## Decision Outcome

Chosen option: "Adopt Import Lightweight AI Guidance Model", because Import the AI guidance model, not the whole guidance corpus.

Import the AI guidance model, not the whole guidance corpus.

Use this lightweight structure:

- `AGENTS.md` as the short AI entry point.
- `docs/WORKING_WITH_AI.md` as the human-facing guide for asking AI agents to work in the repository.
- `docs/DEVELOPMENT_LIFECYCLE.md` for larger change flow.
- `.agents/references/` for focused execution, planning, testing, review, code style, and documentation guidance.
- `.agents/plans/` for task-specific implementation plans and a reusable plan template.
- `docs/decisions/` for ADRs.

Keep project plans, product intent, feature descriptions, and missing user input out of `AGENTS.md`. Put them in `README.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, plans, or ADRs as appropriate.

Do not import these source-repository artifacts unless a future accepted decision says otherwise:

- `.agents/archive/`
- `.agents/context/`
- `.agents/reports/`
- `.agents/skills/`
- Release, deployment, operations, OpenAPI, frontend, benchmark, and Spring-specific guides.
- Task prompts that assume the source repository's build, API, CI, or release process.

### Consequences

- Future agents have a compact, discoverable workflow without inheriting unrelated Spring or operations guidance.
- Guidance stays proportional to the repository's current size.
- The excluded folders and guides can still be added later through ADR-backed decisions.
- The standalone import proposal is obsolete after this ADR exists.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Import Lightweight AI Guidance Model

* Good, because Import the AI guidance model, not the whole guidance corpus.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Import the full source guidance corpus.

* Bad, because most of it targets a different technology stack and a more mature production lifecycle.

### Keep only `AGENTS.md` without reference guides.

* Bad, because focused reference files keep the entry point short while preserving useful guidance.

### Keep `AI_GUIDELINES_IMPORT_PROPOSAL.md` as the durable record.

* Bad, because accepted project decisions belong in `docs/decisions/`.

## More Information

- `AI_GUIDELINES_IMPORT_PROPOSAL.md` was deleted after this ADR was added.
- Keep excluded source-repository folders absent unless a future ADR accepts them.
