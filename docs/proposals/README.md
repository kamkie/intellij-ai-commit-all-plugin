# Proposals

Use this directory for repository analysis and proposal documents that list new features, findings, duplications, simplifications, or improvement options for maintainer triage.

Proposals are advisory until accepted through the normal repository flow. A proposal does not replace an ADR, approved plan, task update, implementation, validation, or release work.

## Index

### Active Proposals

Active proposals still have at least one non-terminal implementation status row or at least one untriaged finding in their `Progress Tracker`.

- None.

### Completed Proposals

Completed proposals have no non-terminal implementation status rows and no untriaged findings in their `Progress Tracker`. Keep them listed here until they are moved to `archive/`.

- `PROP-validation-docs-separation` - [Validation Docs Separation Proposal](PROP-validation-docs-separation-2026-05-23T22-18.md) - completed on 2026-05-23.

Index entries should include the proposal ref, title, file link, and current status date when applicable.

### Proposal Implementation Summary

This summary lists accepted findings whose implementation status is not terminal. The per-finding metadata table remains the source of truth.

Implementation evidence can be a task, approved plan, ADR, changed file, commit, validation result, blocker, open question, or clear open-intake note. Do not create a `TASKS.md` entry solely to satisfy this summary when another evidence path is clearer.

| Proposal | Finding | Title  | Priority | Status | Evidence                                                        |
|----------|---------|--------|----------|--------|-----------------------------------------------------------------|
| _None_   | _None_  | _None_ | _None_   | _None_ | _No accepted findings with non-terminal implementation status._ |

## When To Use A Proposal

Create a proposal when a task asks for any of:

- Repository, folder, or file analysis with recommendations.
- Errors, mistakes, duplications, simplifications, removals, or improvement opportunities.
- New feature or workflow ideas that need maintainer triage before planning.
- A review document that mixes findings with per-item decisions.
- A triage document the maintainer will revisit and mark inline.

Do not create a proposal for:

- Accepted project decisions; use `docs/decisions/`.
- Implementation plans; use `.agents/plans/`.
- Backlog items; use `TASKS.md`.
- Missing user input; use `docs/decisions/OPEN_QUESTIONS.md`.
- One-shot explanations or status updates that do not need triage.

## File Naming

Proposal files must live under `docs/proposals/` or `docs/proposals/archive/`.

Use this filename shape:

```text
<proposal_id>-<YYYY-MM-DD>T<HH-MM>.md
```

Rules:

- Start every active and archived proposal filename with its `proposal_id` ref.
- Use ASCII and hyphen-separated words; keep the required `PROP-` prefix uppercase.
- Use local creation time with no seconds and no timezone.
- Keep the proposal ref short and descriptive, for example `PROP-repository-analysis`, `PROP-workflow-duplication-review`, or `PROP-docs-cleanup`.
- Do not overwrite an older proposal with the same proposal ref or topic. Create a new timestamped file and link to older context when needed.

## Proposal Refs

Every proposal must have a `proposal_id` ref in its front matter and filename.

Use the format `PROP-<short-kebab-slug>`, for example:

- `PROP-repository-analysis`
- `PROP-workflow-duplication-review`
- `PROP-docs-cleanup`

Rules:

- Use an uppercase `PROP-` prefix and a lowercase ASCII kebab-case slug.
- Keep the ref readable enough to identify the proposal without relying on title, filename, or index position.
- Keep the ref stable when the title, filename, status, wording, or archive location changes.
- Preserve the `proposal_id` as the filename prefix for active and archived proposal files.
- When a proposal is split, keep the original ref for the closest surviving proposal and assign new meaningful refs to new proposals.
- Do not reuse a retired proposal ref for unrelated work.

Use proposal refs in README index entries, handoffs, reviews, ADRs, tasks, or commit references that refer to proposal work.

## Required Front Matter

Every proposal must start with:

```yaml
---
proposal_id: PROP-<short-kebab-slug>
generated_at: <YYYY-MM-DDTHH-MM>
created_from: <user request, task ref, review, audit, design pass, or other trigger>
purpose: <one sentence describing what this document proposes>
scope: <one sentence describing what part of the repository is covered>
---
```

Optional keys such as `author`, `related`, or `supersedes` may be added, but they must not replace the five required keys.

Archived proposals may keep historical front matter unless materially updated.

## Required Structure

Use this order:

1. `# <Title>`
2. Short intro paragraph naming the repository contract anchors the proposal respects.
3. `## Table of Contents`
4. `## Summary`
5. `## Creation Context`
6. `## Progress Tracker`
7. `## Proposal Items`, containing finding groups when present:
    - `### New Features`
    - `### Errors And Mistakes`
    - `### Duplications To Remove Or Reduce`
    - `### Simplification Opportunities`
    - `### Smaller / Stylistic Items`
8. `## Suggested Priority Order`
9. `## Out Of Scope`

Use one `#` H1 only. Use `####` headings for individual tracked findings.

## Finding Refs

Tracked findings must use three-digit refs:

- `F001`, `F002`, and so on for new features or larger new capabilities.
- `E001`, `E002`, and so on for errors, mistakes, or rule violations.
- `D001`, `D002`, and so on for duplications.
- `S001`, `S002`, and so on for simplifications, removals, or reorganizations.

Number findings sequentially per letter, starting at `1`, in the order they appear.

Each ref must appear in:

- The finding heading, for example `#### E001. Missing support policy`.
- The `Progress Tracker` table.
- Any cross-reference inside the proposal.

Do not reuse a ref for a different finding after publication.

New proposal findings must start with an empty `Decision` field in both the progress tracker and the per-finding metadata table. Only maintainer triage may set a decision.

## Progress Tracker

Every proposal with tracked findings must contain one compact table under `## Progress Tracker`:

| Id   | Title           | Priority | Status | Decision |
|------|-----------------|----------|--------|----------|
| E001 | Example finding | 1        | open   |          |

Rules:

- The table lists every `F*`, `E*`, `D*`, and `S*` finding in document order.
- The table must not include refs without matching sections.
- `### Smaller / Stylistic Items` bullets are not tracked in this table.
- The per-finding metadata table is the source of truth; mirror `Status`, `Decision`, and `Priority` into the progress tracker after edits.

## Per-Finding Layout

Each tracked finding should use:

````markdown
#### <Id>. <Short title>

| Field       | Value                     |
|-------------|---------------------------|
| Status      | open                      |
| Decision    |                           |
| Decision at |                           |
| Priority    | <1-6>                     |
| Owner       |                           |
| Updated     | <YYYY-MM-DDTHH:mm:ss+HH:mm> |

##### Context

- Evidence: <observable facts, file paths, line references>
- Impact: <why it matters>
- Non-goals:
    - <thing this item deliberately does not do>
- Acceptance criteria:
    - <what must be true when this item is done>

##### Recommended Change

<Concrete action>

##### Review Notes

- none

##### Follow-Up

- Artifact: <ADR, approved plan, task, direct docs edit, changed file, open question, or none>
- Validation: <command, review check, manual check, or none>
````

Rules:

- `Evidence`, `Recommended Change`, and `Follow-Up` are required.
- `Impact` should be present unless the finding is very small.
- Use exactly one metadata table per tracked finding.
- Keep metadata fields in the order shown above.
- Leave `Decision` and `Decision at` empty until maintainer triage sets `Decision`.
- Set `Decision at` to an ISO 8601 timestamp with timezone offset when `Decision` is non-empty.
- Use `Review Notes` for reviewer questions, requested changes, or decision rationale; use `- none` when there are no notes.

## Status Vocabulary

Use these values for `Status`:

- `open` - implementation has not started.
- `planned` - implementation is covered by an approved plan or explicit open task.
- `in-progress` - implementation work has started.
- `blocked` - implementation is waiting on unresolved input, dependency, or external condition.
- `done` - implemented and landed.
- `not-required` - no separate implementation is required.

Use these values for `Decision`:

- empty - not triaged yet.
- `accepted` - will be done as proposed.
- `rejected` - will not be done.
- `deferred` - revisit later.

Rejected findings should use `Decision` `rejected` and `Status` `not-required`. Deferred findings should use `Decision` `deferred` and either `Status` `blocked` or `Status` `not-required`.

Accepted findings with non-terminal implementation status must have a visible evidence path in the Proposal Implementation Summary. The evidence path does not have to be a `TASKS.md` task.

Short lowercase tags may be used only in archived proposals when the standard vocabulary does not fit.

## Priority Scale

- `1` - minutes of work, no risk, no dependencies.
- `2` - clear documentation or clarity improvement, low risk.
- `3` - small functional or tooling fix.
- `4` - larger content or compatibility work.
- `5` - tooling or workflow improvement with dependencies.
- `6` - broad reorganization, rename, or churn-heavy cleanup.

Use the same priority in the progress tracker and the per-finding metadata table.

## Editing Workflow

1. Edit the finding's metadata table.
2. Mirror `Status`, `Decision`, and `Priority` into the `Progress Tracker` row.
3. Bump `Updated` to the current timestamp.
4. Leave `done` or `rejected` findings in place as history.
5. When setting any non-empty `Decision`, set `Decision at` to the decision timestamp.
6. Keep `Decision at` empty while `Decision` is empty.
7. Add accepted findings with non-terminal implementation status to the Proposal Implementation Summary with an evidence path.
8. Remove accepted findings with terminal implementation status from the Proposal Implementation Summary.
9. When no non-terminal implementation statuses and no untriaged findings remain, move the proposal from `Active Proposals` to `Completed Proposals` in this README and append the completion date.
10. Move fully retired proposals to `archive/` only when their proposal ref, finding refs, and filename can be preserved.

## Template

Start from [PROPOSAL_TEMPLATE.md](PROPOSAL_TEMPLATE.md) for new proposals.
