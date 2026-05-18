---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Store Open Questions With Decisions

## Context and Problem Statement

The repository previously stored unresolved user input in root `OPEN_QUESTIONS.md`.

During implementation of `PROP-repository-analysis`, the maintainer accepted the smaller cleanup item to move open questions under `docs/decisions/`.

Open questions are not accepted decisions, but they are closely tied to decision flow because they block plans, tasks, ADRs, and implementation assumptions.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Store Open Questions With Decisions
* Keep `OPEN_QUESTIONS.md` at the repository root.
* Merge open questions into `docs/decisions/README.md`.
* Turn each open question into a proposed ADR.

## Decision Outcome

Chosen option: "Adopt Store Open Questions With Decisions", because Store unresolved user input and missing project decisions in `docs/decisions/OPEN_QUESTIONS.md`.

Store unresolved user input and missing project decisions in `docs/decisions/OPEN_QUESTIONS.md`.

Numbered files in `docs/decisions/` remain ADRs. `docs/decisions/OPEN_QUESTIONS.md` is not an ADR and must not be included in ADR numbering.

When a question is resolved, update `docs/decisions/OPEN_QUESTIONS.md` alongside the ADR, task, plan, or implementation artifact that records the answer.

### Consequences

- Current guidance should point agents and maintainers to `docs/decisions/OPEN_QUESTIONS.md`.
- The root-level `OPEN_QUESTIONS.md` file is removed.
- ADR tooling and indexes must ignore non-numbered Markdown files in `docs/decisions/`.
- Keeping open questions near ADRs makes decision-blocking input easier to find, but authors must still keep questions separate from accepted decisions.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Store Open Questions With Decisions

* Good, because Store unresolved user input and missing project decisions in `docs/decisions/OPEN_QUESTIONS.md`.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Keep `OPEN_QUESTIONS.md` at the repository root.

* Bad, because the accepted proposal cleanup requested moving it under `docs/decisions/`.

### Merge open questions into `docs/decisions/README.md`.

* Bad, because open questions need stable IDs and a focused owner file.

### Turn each open question into a proposed ADR.

* Bad, because questions are unresolved input, while ADRs record accepted, proposed, rejected, or superseded decisions.

## More Information

- Update `AGENTS.md`, AI guidance, planning guidance, proposal guidance, and historical path references.
- Add documentation validation so ADR checks ignore `docs/decisions/OPEN_QUESTIONS.md`.
