---
proposal_id: PROP-01-proposal-governance
generated_at: 2026-05-15T09-57
purpose: Consolidate proposal-format governance work into the first active work stream for maintainer triage.
scope: Proposal IDs, proposal finding IDs, tracker decisions, proposal template rules, proposal README rules, and proposal validation.
supersedes:
  - PROP-proposal-id-and-markdown-formatting
  - PROP-orchestrator-worker-rules S8
---

# Proposal Governance Work Stream

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It consolidates proposal-governance findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
    - [S001. Normalize active proposal finding IDs](#s001-normalize-active-proposal-finding-ids)
    - [S002. Require author-empty proposal decisions](#s002-require-author-empty-proposal-decisions)
    - [S003. Update proposal docs and validation together](#s003-update-proposal-docs-and-validation-together)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- This is the first work stream because proposal triage should not depend on ambiguous tracker metadata.
- The work stream consolidates the fixed-width finding-ID request from `PROP-proposal-id-and-markdown-formatting` with the author-empty decision rule from `PROP-orchestrator-worker-rules` `S8`.
- The resulting governance change should be ADR-gated before any durable rule, template, or validation update lands.

## Progress Tracker

| Id   | Title                                        | Priority | Status | Decision |
|------|----------------------------------------------|----------|--------|----------|
| S001 | Normalize active proposal finding IDs        | 1        | open   |          |
| S002 | Require author-empty proposal decisions      | 1        | open   |          |
| S003 | Update proposal docs and validation together | 2        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed, rejected, or superseded findings in place as history.
- Do not set `decision: accepted` when authoring a new finding; leave it empty for maintainer triage.

## Errors And Mistakes

_No tracked findings._

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

### S001. Normalize active proposal finding IDs

- Evidence: Earlier active proposals used variable-width IDs such as `E1` and `E10`, while `PROP-proposal-id-and-markdown-formatting` already used `E001` style IDs. The mixed forms make tables harder to scan and sort.
- Impact: Proposal trackers are noisy to maintain, cross-references are harder to compare, and generated proposal files drift between styles.
- Proposal: Use zero-padded IDs in consolidated and future active proposals: `E001`, `D001`, and `S001`. Preserve historical IDs in archived source proposals, but use the consolidated ID in new ADRs, plans, reviews, and handoffs.

```yaml
status: open
decision: accepted
priority: 1
owner:
updated: 2026-05-15
comment: "Source: PROP-proposal-id-and-markdown-formatting E001."
```

### S002. Require author-empty proposal decisions

- Evidence: `PROP-orchestrator-worker-rules` pre-filled several tracker decisions as `accepted` even though the proposal itself states proposals are advisory until maintainer triage.
- Impact: Reviewers cannot tell whether `decision: accepted` records maintainer intent or the proposal author's recommendation.
- Proposal: Require every new proposal finding to start with an empty `decision` field in both the progress tracker and YAML tracker block. Only maintainer triage should fill the decision.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment: "Source: PROP-orchestrator-worker-rules S8."
```

### S003. Update proposal docs and validation together

- Evidence: Proposal behavior is split across `docs/proposals/README.md`, `docs/proposals/PROPOSAL_TEMPLATE.md`, and `scripts/validate-docs.ps1`.
- Impact: Updating only one surface creates rule drift; agents may follow the template while validation accepts something else.
- Proposal: After the ADR for `S001` and `S002` is accepted, update the proposal README, template, and validation script in one change. If the validation script enforces three-digit IDs, update only active proposal files that are materially touched and leave archived history intact.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment: "Sources: PROP-proposal-id-and-markdown-formatting E002 and S001; PROP-orchestrator-worker-rules S8."
```

## Smaller / Stylistic Items

- Keep consolidated proposal files sorted by work-stream number in `docs/proposals/README.md`.
- Prefer referencing consolidated IDs such as `PROP-01-proposal-governance S001` over superseded source IDs in new work.

## Suggested Priority Order

1. `S002` - clear the author-versus-maintainer decision boundary first.
2. `S001` - normalize IDs in active proposal work after the decision boundary is clear.
3. `S003` - update docs and validation only after the rule is accepted.

## Out Of Scope

- Implementing repository automation, plugin runtime behavior, or multi-agent execution rules.
- Reformatting archived proposal bodies solely for style.
- Retroactively changing historical commit messages or ADRs that cite old finding IDs.
