# Proposals

Use this directory for repository analysis and proposal documents that list findings, duplications, simplifications, or improvement options for maintainer triage.

Proposals are advisory until accepted through the normal repository flow. A proposal does not replace an ADR, approved plan, task update, implementation, validation, or release work.

## Index

### Active Proposals

Active proposals still have at least one `open` row in their `Progress Tracker`.

- `PROP-orchestrator-worker-rules` - Orchestrator And Worker Rules For Multi-Agent Execution ([PROP-orchestrator-worker-rules-2026-05-15T05-31.md](PROP-orchestrator-worker-rules-2026-05-15T05-31.md)), created 2026-05-15.
- `PROP-repo-hygiene-automation` - Repository Hygiene Automation Proposal ([PROP-repo-hygiene-automation-2026-05-15T06-45.md](PROP-repo-hygiene-automation-2026-05-15T06-45.md)), created 2026-05-15.
- `PROP-remove-tasks-md-when-empty` - Remove TASKS.md When All Tasks Are Finished Proposal ([PROP-remove-tasks-md-when-empty-2026-05-15T06-49.md](PROP-remove-tasks-md-when-empty-2026-05-15T06-49.md)), created 2026-05-15.

### Completed Proposals

Completed proposals have no `open` rows in their `Progress Tracker`. Keep them listed here until they are moved to `archive/`.

_none yet_

### Archived Proposals

Archived proposals have completed or otherwise retired tracker rows and live under `archive/`.

- `PROP-repository-analysis` - Repository Analysis Proposal ([archive/PROP-repository-analysis-2026-05-15T01-47.md](archive/PROP-repository-analysis-2026-05-15T01-47.md)), archived as of 2026-05-15.
- `PROP-plan-status-vocabulary` - Plan Status Vocabulary Proposal ([archive/PROP-plan-status-vocabulary-2026-05-15T02-51.md](archive/PROP-plan-status-vocabulary-2026-05-15T02-51.md)), archived as of 2026-05-15.

Index entries should include the proposal ID, title, file link, and current status date when applicable.

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

Tracked findings must use stable IDs:

- `E<n>` for errors, mistakes, or rule violations.
- `D<n>` for duplications.
- `S<n>` for simplifications, removals, or reorganizations.

Number findings sequentially per letter, starting at `1`, in the order they appear.

Each ID must appear in:

- The finding heading, for example `### E1. Missing support policy`.
- The `Progress Tracker` table.
- Any cross-reference inside the proposal.

Do not reuse an ID for a different finding after publication.

## Progress Tracker

Every proposal with tracked findings must contain one compact table under `## Progress Tracker`:

| Id | Title           | Priority | Status | Decision |
|----|-----------------|----------|--------|----------|
| E1 | Example finding | 1        | open   |          |

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
updated: <YYYY-MM-DD>
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

## Status Vocabulary

Use these values for `status` and `decision`:

- `open` - not reviewed yet.
- `in-progress` - work has started.
- `accepted` - will be done as proposed.
- `rejected` - will not be done.
- `deferred` - revisit later.
- `done` - implemented and landed.

Short lowercase tags may be used only when the standard vocabulary does not fit, and the `comment` must explain them.

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
3. Bump `updated` to the current date.
4. Leave `done` or `rejected` findings in place as history.
5. When no `open` rows remain, move the proposal from `Active Proposals` to `Completed Proposals` in this README and append the completion date.
6. Move fully retired proposals to `archive/` only when their proposal ID, finding IDs, and filename can be preserved.

## Template

Start from [PROPOSAL_TEMPLATE.md](PROPOSAL_TEMPLATE.md) for new proposals.
