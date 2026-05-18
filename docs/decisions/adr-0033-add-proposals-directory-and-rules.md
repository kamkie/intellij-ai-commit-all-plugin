---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Add Proposals Directory And Rules

## Context and Problem Statement

The repository has ADRs for durable decisions, plans for accepted implementation work, `TASKS.md` for backlog items, and `docs/decisions/OPEN_QUESTIONS.md` for missing input.

It did not yet have a home for analysis documents that list findings, duplications, simplifications, and improvement options for maintainer triage without immediately making decisions or implementation changes.

The user requested a `docs/proposals/` directory with README and proposal rules.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Add Proposals Directory And Rules
* Store proposals at the repository root.
* Store proposals in `.agents/plans/`.
* Store proposal findings directly in `TASKS.md`.

## Decision Outcome

Chosen option: "Adopt Add Proposals Directory And Rules", because Add `docs/proposals/` as the owner for repository analysis and proposal documents.

Add `docs/proposals/` as the owner for repository analysis and proposal documents.

Proposal documents are advisory triage artifacts. They do not replace:

- ADRs for accepted decisions.
- `.agents/plans/` for implementation plans.
- `TASKS.md` for backlog work.
- `docs/decisions/OPEN_QUESTIONS.md` for missing user input.
- `CHANGELOG.md` for released or notable unreleased history.

Each proposal must follow the rules in `docs/proposals/README.md`, including:

- Timestamped filename under `docs/proposals/`.
- Required YAML front matter.
- Table of contents.
- Summary.
- Progress tracker.
- Per-finding IDs and YAML tracker blocks.
- Status and priority vocabulary.
- Active and completed proposal indexing in `docs/proposals/README.md`.

Add `docs/proposals/PROPOSAL_TEMPLATE.md` as the starting point for new proposal files.

### Consequences

- Repository analysis work has a stable home.
- Maintainers can triage findings inline without turning every finding into an immediate task or ADR.
- Proposal rules add another documentation owner that agents must route correctly.
- Completed proposal tracking requires keeping `docs/proposals/README.md` aligned with proposal tracker state.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Add Proposals Directory And Rules

* Good, because Add `docs/proposals/` as the owner for repository analysis and proposal documents.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Store proposals at the repository root.

* Bad, because root-level proposal files add clutter and are harder to discover as a set.

### Store proposals in `.agents/plans/`.

* Bad, because proposals are triage artifacts, while plans are accepted implementation artifacts.

### Store proposal findings directly in `TASKS.md`.

* Bad, because proposals may contain analysis, rejected ideas, deferred items, and grouped evidence that would make the backlog noisy.

## More Information

- Add `docs/proposals/README.md`.
- Add `docs/proposals/PROPOSAL_TEMPLATE.md`.
- Add `docs/proposals/archive/README.md`.
- Update documentation routing, AI guidance, `TASKS.md`, and `CHANGELOG.md`.
