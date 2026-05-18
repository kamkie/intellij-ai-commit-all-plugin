---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Stable Proposal IDs

## Context and Problem Statement

The repository already uses stable IDs for open questions, tasks, and plans.

`docs/proposals/` now owns repository analysis and proposal documents. Existing proposal rules require stable IDs for tracked findings, but the proposal document itself also needs an identifier that survives title, filename, index, and archive changes.

The user requested that proposals also need a stable ID.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Stable Proposal IDs
* Use the timestamped proposal filename as the only identifier.
* Use strictly numeric proposal IDs.
* Reuse per-finding IDs as proposal identifiers.

## Decision Outcome

Chosen option: "Adopt Use Stable Proposal IDs", because Every proposal must have a stable `proposal_id` in front matter and in its active or archived filename.

Every proposal must have a stable `proposal_id` in front matter and in its active or archived filename.

Use the format `PROP-<short-kebab-slug>`, for example:

- `PROP-repository-analysis`
- `PROP-workflow-duplication-review`
- `PROP-docs-cleanup`

The proposal ID should be human-readable enough to recognize the proposal without relying on the file path. Keep the `proposal_id` stable when the title, filename, status, wording, or archive location changes.

When a proposal is split, keep the original ID for the closest surviving proposal and assign new meaningful IDs to new proposals. Do not reuse a retired proposal ID for unrelated work.

Proposal filenames, index entries, handoffs, reviews, ADRs, tasks, and commit references that refer to proposal work should include the stable `proposal_id` when practical.

### Consequences

- Proposals can be referenced reliably without depending only on timestamped filenames.
- Proposal IDs remain distinct from plan IDs and per-finding IDs.
- Proposal front matter has one additional required key, and proposal filenames carry the same stable ID.
- Agents must choose meaningful proposal IDs and preserve them during archive moves or proposal edits.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Stable Proposal IDs

* Good, because Every proposal must have a stable `proposal_id` in front matter and in its active or archived filename.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use the timestamped proposal filename as the only identifier.

* Bad, because filenames can change during clarification or archive moves, and filename-only references are less readable in tasks, ADRs, and handoffs.

### Use strictly numeric proposal IDs.

* Bad, because human-readable slugs are easier to recognize and align with the existing plan ID approach.

### Reuse per-finding IDs as proposal identifiers.

* Bad, because finding IDs identify items inside one proposal, not the proposal document itself.

## More Information

- Update `docs/proposals/README.md`.
- Update `docs/proposals/PROPOSAL_TEMPLATE.md`.
- Update proposal archive and AI-facing documentation guidance.
- Update `TASKS.md` and `CHANGELOG.md`.
