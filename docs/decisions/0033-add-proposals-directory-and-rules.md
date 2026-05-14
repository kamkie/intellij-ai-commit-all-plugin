# Add Proposals Directory And Rules

Status: Accepted

Date: 2026-05-15

## Context

The repository has ADRs for durable decisions, plans for accepted implementation work, `TASKS.md` for backlog items, and `OPEN_QUESTIONS.md` for missing input.

It did not yet have a home for analysis documents that list findings, duplications, simplifications, and improvement options for maintainer triage without immediately making decisions or implementation changes.

The user requested a `docs/proposals/` directory with README and proposal rules.

## Decision

Add `docs/proposals/` as the owner for repository analysis and proposal documents.

Proposal documents are advisory triage artifacts. They do not replace:

- ADRs for accepted decisions.
- `.agents/plans/` for implementation plans.
- `TASKS.md` for backlog work.
- `OPEN_QUESTIONS.md` for missing user input.
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

## Consequences

- Repository analysis work has a stable home.
- Maintainers can triage findings inline without turning every finding into an immediate task or ADR.
- Proposal rules add another documentation owner that agents must route correctly.
- Completed proposal tracking requires keeping `docs/proposals/README.md` aligned with proposal tracker state.

## Alternatives Considered

- Store proposals at the repository root.
    - Why it was not chosen: root-level proposal files add clutter and are harder to discover as a set.
- Store proposals in `.agents/plans/`.
    - Why it was not chosen: proposals are triage artifacts, while plans are accepted implementation artifacts.
- Store proposal findings directly in `TASKS.md`.
    - Why it was not chosen: proposals may contain analysis, rejected ideas, deferred items, and grouped evidence that would make the backlog noisy.

## Follow-Up

- Add `docs/proposals/README.md`.
- Add `docs/proposals/PROPOSAL_TEMPLATE.md`.
- Add `docs/proposals/archive/README.md`.
- Update documentation routing, AI guidance, `TASKS.md`, and `CHANGELOG.md`.
