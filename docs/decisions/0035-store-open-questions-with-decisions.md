# Store Open Questions With Decisions

Status: Accepted

Date: 2026-05-15

## Context

The repository previously stored unresolved user input in root `OPEN_QUESTIONS.md`.

During implementation of `PROP-repository-analysis`, the maintainer accepted the smaller cleanup item to move open questions under `docs/decisions/`.

Open questions are not accepted decisions, but they are closely tied to decision flow because they block plans, tasks, ADRs, and implementation assumptions.

## Decision

Store unresolved user input and missing project decisions in `docs/decisions/OPEN_QUESTIONS.md`.

Numbered files in `docs/decisions/` remain ADRs. `docs/decisions/OPEN_QUESTIONS.md` is not an ADR and must not be included in ADR numbering.

When a question is resolved, update `docs/decisions/OPEN_QUESTIONS.md` alongside the ADR, task, plan, or implementation artifact that records the answer.

## Consequences

- Current guidance should point agents and maintainers to `docs/decisions/OPEN_QUESTIONS.md`.
- The root-level `OPEN_QUESTIONS.md` file is removed.
- ADR tooling and indexes must ignore non-numbered Markdown files in `docs/decisions/`.
- Keeping open questions near ADRs makes decision-blocking input easier to find, but authors must still keep questions separate from accepted decisions.

## Alternatives Considered

- Keep `OPEN_QUESTIONS.md` at the repository root.
  - Why it was not chosen: the accepted proposal cleanup requested moving it under `docs/decisions/`.
- Merge open questions into `docs/decisions/README.md`.
  - Why it was not chosen: open questions need stable IDs and a focused owner file.
- Turn each open question into a proposed ADR.
  - Why it was not chosen: questions are unresolved input, while ADRs record accepted, proposed, rejected, or superseded decisions.

## Follow-Up

- Update `AGENTS.md`, AI guidance, planning guidance, proposal guidance, and historical path references.
- Add documentation validation so ADR checks ignore `docs/decisions/OPEN_QUESTIONS.md`.
