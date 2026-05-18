# Proposals

Use this directory for repository analysis and proposal documents that list findings, duplications, simplifications, or improvement options for maintainer triage.

Proposals are advisory until accepted through the normal repository flow. A proposal does not replace an ADR, approved plan, task update, implementation, validation, or release work.

## Index

### Active Proposals

Active proposals still have at least one non-terminal implementation status row or at least one untriaged finding in their `Progress Tracker`.

- `PROP-03-repository-quality-lifecycle` - Repository Quality And Lifecycle Work Stream ([PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md](PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md)), created 2026-05-15.

### Completed Proposals

Completed proposals have no non-terminal implementation status rows and no untriaged findings in their `Progress Tracker`. Keep them listed here until they are moved to `archive/`.

- None.

Index entries should include the proposal ID, title, file link, and current status date when applicable.

### Proposal Implementation Summary

This summary lists accepted findings whose implementation status is not terminal. The per-finding YAML tracker remains the source of truth.

Implementation evidence can be a task, approved plan, ADR, changed file, commit, validation result, blocker, open question, or clear open-intake note. Do not create a `TASKS.md` entry solely to satisfy this summary when another evidence path is clearer.

| Proposal                                                                                         | Finding | Title                                                    | Priority | Status  | Evidence                                                                                          |
|--------------------------------------------------------------------------------------------------|---------|----------------------------------------------------------|----------|---------|---------------------------------------------------------------------------------------------------|
| [PROP-03-repository-quality-lifecycle](PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md) | E001    | Add Dependabot configuration                             | 1        | open    | Open intake; no task or plan selected.                                                            |
| [PROP-03-repository-quality-lifecycle](PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md) | E002    | Add CodeQL analysis                                      | 1        | open    | Open intake; no task or plan selected.                                                            |
| [PROP-03-repository-quality-lifecycle](PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md) | E005    | Add security policy and secret-scanning guidance         | 2        | open    | Open intake; no task or plan selected.                                                            |
| [PROP-03-repository-quality-lifecycle](PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md) | E006    | Add contributor intake files                             | 3        | open    | Open intake; no task or plan selected.                                                            |
| [PROP-03-repository-quality-lifecycle](PROP-03-repository-quality-lifecycle-2026-05-15T09-57.md) | E007    | Add CODEOWNERS after reviewer identity is known          | 3        | open    | Open intake; reviewer GitHub handle still needs to be supplied.                                    |

## When To Use A Proposal

Create a proposal when a task asks for any of:

- Repository, folder, or file analysis with recommendations.
- Errors, mistakes, duplications, simplifications, removals, or improvement opportunities.
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

- Start every active and archived proposal filename with its stable `proposal_id`.
- Use ASCII and hyphen-separated words; keep the required `PROP-` prefix uppercase.
- Use local creation time with no seconds and no timezone.
- Keep the proposal ID short and descriptive, for example `PROP-repository-analysis`, `PROP-workflow-duplication-review`, or `PROP-docs-cleanup`.
- Do not overwrite an older proposal with the same proposal ID or topic. Create a new timestamped file and link to older context when needed.

## Proposal IDs

Every proposal must have a stable `proposal_id` in its front matter and filename.

Use the format `PROP-<short-kebab-slug>`, for example:

- `PROP-repository-analysis`
- `PROP-workflow-duplication-review`
- `PROP-docs-cleanup`

Rules:

- Use an uppercase `PROP-` prefix and a lowercase ASCII kebab-case slug.
- Keep the ID readable enough to identify the proposal without relying on title, filename, or index position.
- Keep the ID stable when the title, filename, status, wording, or archive location changes.
- Preserve the `proposal_id` as the filename prefix for active and archived proposal files.
- When a proposal is split, keep the original ID for the closest surviving proposal and assign new meaningful IDs to new proposals.
- Do not reuse a retired proposal ID for unrelated work.

Use proposal IDs in README index entries, handoffs, reviews, ADRs, tasks, or commit references that refer to proposal work.

## Required Front Matter

Every proposal must start with:

```yaml
---
proposal_id: PROP-<short-kebab-slug>
generated_at: <YYYY-MM-DDTHH-MM>
purpose: <one sentence describing what this document proposes>
scope: <one sentence describing what part of the repository is covered>
---
```

Optional keys such as `author`, `related`, or `supersedes` may be added, but they must not replace the four required keys.

## Required Structure

Use this order:

1. `# <Title>`
2. Short intro paragraph naming the repository contract anchors the proposal respects.
3. `## Table of Contents`
4. `## Summary`
5. `## Progress Tracker`
6. `## How To Edit The Trackers`
7. Finding groups, when present:
    - `## Errors And Mistakes`
    - `## Duplications To Remove Or Reduce`
    - `## Simplification Opportunities`
    - `## Smaller / Stylistic Items`
8. `## Suggested Priority Order`
9. `## Out Of Scope`

Use one `#` H1 only. Use `###` headings for individual tracked findings.

## Finding IDs

Tracked findings must use stable three-digit IDs:

- `E001`, `E002`, and so on for errors, mistakes, or rule violations.
- `D001`, `D002`, and so on for duplications.
- `S001`, `S002`, and so on for simplifications, removals, or reorganizations.

Number findings sequentially per letter, starting at `1`, in the order they appear.

Each ID must appear in:

- The finding heading, for example `### E001. Missing support policy`.
- The `Progress Tracker` table.
- Any cross-reference inside the proposal.

Do not reuse an ID for a different finding after publication.

New proposal findings must start with an empty `decision` field in both the progress tracker and the per-finding YAML block. Only maintainer triage may set a decision.

## Progress Tracker

Every proposal with tracked findings must contain one compact table under `## Progress Tracker`:

| Id   | Title           | Priority | Status | Decision |
|------|-----------------|----------|--------|----------|
| E001 | Example finding | 1        | open   |          |

Rules:

- The table lists every `E*`, `D*`, and `S*` finding in document order.
- The table must not include IDs without matching sections.
- `## Smaller / Stylistic Items` bullets are not tracked in this table.
- The per-finding YAML block is the source of truth; mirror `status`, `decision`, and `priority` into the table after edits.

## Per-Finding Layout

Each tracked finding should use:

````markdown
### <Id>. <Short title>

- Evidence: <observable facts, file paths, line references>
- Impact: <why it matters>
- Proposal: <concrete action>

```yaml
status: open
decision:
priority: <1-6>
owner:
updated: <YYYY-MM-DDTHH:mm:ss+HH:mm>
accepted_at:
decided_at:
comment:
```
````

Rules:

- `Evidence` and `Proposal` are required.
- `Impact` should be present unless the finding is very small.
- Use exactly one fenced `yaml` tracker block per tracked finding.
- Keep tracker keys in the order shown above.
- Quote YAML values that contain `:` or other special characters.
- Use `comment: |` for multi-line comments.
- Set `accepted_at` to an ISO 8601 timestamp with timezone offset when `decision: accepted`.
- Set `decided_at` to an ISO 8601 timestamp with timezone offset when `decision` is a non-empty value other than `accepted`.
- Keep `accepted_at` and `decided_at` empty until maintainer triage sets `decision`.

## Status Vocabulary

Use these values for `status`:

- `open` - implementation has not started.
- `planned` - implementation is covered by an approved plan or explicit open task.
- `in-progress` - implementation work has started.
- `blocked` - implementation is waiting on unresolved input, dependency, or external condition.
- `done` - implemented and landed.
- `not-required` - no separate implementation is required.

Use these values for `decision`:

- empty - not triaged yet.
- `accepted` - will be done as proposed.
- `rejected` - will not be done.
- `deferred` - revisit later.

Rejected findings should use `decision: rejected` and `status: not-required`. Deferred findings should use `decision: deferred` and either `status: blocked` or `status: not-required`.

Accepted findings with non-terminal implementation status must have a visible evidence path in the Proposal Implementation Summary. The evidence path does not have to be a `TASKS.md` task.

Short lowercase tags may be used only in archived proposals when the standard vocabulary does not fit, and the `comment` must explain them.

## Priority Scale

- `1` - minutes of work, no risk, no dependencies.
- `2` - clear documentation or clarity improvement, low risk.
- `3` - small functional or tooling fix.
- `4` - larger content or compatibility work.
- `5` - tooling or workflow improvement with dependencies.
- `6` - broad reorganization, rename, or churn-heavy cleanup.

Use the same priority in the tracker table and the per-finding YAML block.

## Editing Workflow

1. Edit the finding's YAML tracker block.
2. Mirror `status`, `decision`, and `priority` into the `Progress Tracker` row.
3. Bump `updated` to the current timestamp.
4. Leave `done` or `rejected` findings in place as history.
5. When setting `decision: accepted`, set `accepted_at` to the decision timestamp.
6. When setting a non-empty decision other than `accepted`, set `decided_at` to the decision timestamp.
7. Add accepted findings with non-terminal implementation status to the Proposal Implementation Summary with an evidence path.
8. Remove accepted findings with terminal implementation status from the Proposal Implementation Summary.
9. When no non-terminal implementation statuses and no untriaged findings remain, move the proposal from `Active Proposals` to `Completed Proposals` in this README and append the completion date.
10. Move fully retired proposals to `archive/` only when their proposal ID, finding IDs, and filename can be preserved.

## Template

Start from [PROPOSAL_TEMPLATE.md](PROPOSAL_TEMPLATE.md) for new proposals.
