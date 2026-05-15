---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use PLAN Prefix And Filename Stable IDs

## Context and Problem Statement

The repository already requires stable IDs for implementation plans and proposal documents.

Existing plan guidance used the shorter `P-<short-kebab-slug>` format. Existing proposal guidance required `proposal_id` in front matter, but proposal filenames could omit that stable ID, including archived proposal files.

The maintainer requested that proposal and plan files contain their stable IDs in both active and archived locations, and that plan IDs use `PLAN-<short-kebab-slug>`.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use PLAN Prefix And Filename Stable IDs
* Keep `P-<short-kebab-slug>` for plan IDs.
* Keep proposal IDs only in front matter.
* Remove proposal timestamp suffixes.

## Decision Outcome

Chosen option: "Adopt Use PLAN Prefix And Filename Stable IDs", because Plan IDs must use the format `PLAN-<short-kebab-slug>`.

Plan IDs must use the format `PLAN-<short-kebab-slug>`.

Every plan file under `.agents/plans/`, including `.agents/plans/archive/`, must contain a `Plan-ID` field and its filename must start with that exact `Plan-ID`.

Every proposal file under `docs/proposals/`, including `docs/proposals/archive/`, must contain a `proposal_id` front matter field and its filename must start with that exact `proposal_id`.

Proposal filenames keep their timestamp suffix for uniqueness, for example:

- `PROP-repository-analysis-2026-05-15T01-47.md`
- `PROP-workflow-duplication-review-2026-05-15T11-20.md`

### Consequences

- Active and archived plan and proposal files can be identified from filenames without opening the documents.
- `PLAN-` plan IDs are distinct from proposal IDs and less ambiguous than the older `P-` prefix.
- Existing plan references and archived filenames need to be renamed from `P-...` to `PLAN-...`.
- Documentation validation can catch plan or proposal files whose filenames omit their stable IDs.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use PLAN Prefix And Filename Stable IDs

* Good, because Plan IDs must use the format `PLAN-<short-kebab-slug>`.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Keep `P-<short-kebab-slug>` for plan IDs.

* Bad, because the maintainer requested the more explicit `PLAN-` prefix.

### Keep proposal IDs only in front matter.

* Bad, because proposal files should be identifiable from active and archived filenames.

### Remove proposal timestamp suffixes.

* Bad, because timestamps still prevent filename collisions when a topic receives multiple proposal documents.

## More Information

- Rename existing archived plan and proposal files.
- Update plan, proposal, documentation, and commit-message guidance.
- Update documentation validation for `PLAN-` IDs and proposal filename prefixes.
