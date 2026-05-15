# Use PLAN Prefix And Filename Stable IDs

Status: Accepted

Date: 2026-05-15

## Context

The repository already requires stable IDs for implementation plans and proposal documents.

Existing plan guidance used the shorter `P-<short-kebab-slug>` format. Existing proposal guidance required `proposal_id` in front matter, but proposal filenames could omit that stable ID, including archived proposal files.

The maintainer requested that proposal and plan files contain their stable IDs in both active and archived locations, and that plan IDs use `PLAN-<short-kebab-slug>`.

## Decision

Plan IDs must use the format `PLAN-<short-kebab-slug>`.

Every plan file under `.agents/plans/`, including `.agents/plans/archive/`, must contain a `Plan-ID` field and its filename must start with that exact `Plan-ID`.

Every proposal file under `docs/proposals/`, including `docs/proposals/archive/`, must contain a `proposal_id` front matter field and its filename must start with that exact `proposal_id`.

Proposal filenames keep their timestamp suffix for uniqueness, for example:

- `PROP-repository-analysis-2026-05-15T01-47.md`
- `PROP-workflow-duplication-review-2026-05-15T11-20.md`

## Consequences

- Active and archived plan and proposal files can be identified from filenames without opening the documents.
- `PLAN-` plan IDs are distinct from proposal IDs and less ambiguous than the older `P-` prefix.
- Existing plan references and archived filenames need to be renamed from `P-...` to `PLAN-...`.
- Documentation validation can catch plan or proposal files whose filenames omit their stable IDs.

## Alternatives Considered

- Keep `P-<short-kebab-slug>` for plan IDs.
    - Why it was not chosen: the maintainer requested the more explicit `PLAN-` prefix.
- Keep proposal IDs only in front matter.
    - Why it was not chosen: proposal files should be identifiable from active and archived filenames.
- Remove proposal timestamp suffixes.
    - Why it was not chosen: timestamps still prevent filename collisions when a topic receives multiple proposal documents.

## Follow-Up

- Rename existing archived plan and proposal files.
- Update plan, proposal, documentation, and commit-message guidance.
- Update documentation validation for `PLAN-` IDs and proposal filename prefixes.
