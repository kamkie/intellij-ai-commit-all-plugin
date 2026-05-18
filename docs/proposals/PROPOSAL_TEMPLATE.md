---
proposal_id: PROP-<short-kebab-slug>
generated_at: YYYY-MM-DDTHH-MM
created_from: User request, task ID, review, audit, design pass, or other trigger.
purpose: One sentence describing what this document proposes.
scope: One sentence describing what part of the repository is covered.
---

# Proposal title

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/decisions/`, and `docs/proposals/README.md`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Creation Context](#creation-context)
- [Progress Tracker](#progress-tracker)
- [Proposal Items](#proposal-items)
    - [New Features](#new-features)
    - [Errors And Mistakes](#errors-and-mistakes)
        - [E001. Example error](#e001-example-error)
    - [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
    - [Simplification Opportunities](#simplification-opportunities)
    - [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- Replace this bullet with the main evidence-backed finding.
- Replace this bullet with the intended triage outcome.
- State clearly that no implementation is performed by the proposal.

## Creation Context

- Why this proposal exists: Describe the request, observation, review finding, open task, or repository friction that triggered this proposal.
- How it was created: List the main files, commands, reviews, interviews, or comparison sources used to produce the findings.
- Scope guardrails: Name the repository contracts, ADRs, plans, or user constraints that shaped what the proposal includes and excludes.

## Progress Tracker

Compact overview only. The metadata table inside each finding remains the source of truth; this table mirrors statuses at a glance. Tracker mirroring, status and decision vocabulary, and Proposal Implementation Summary updates live in `docs/proposals/README.md`.

| Id   | Title         | Priority | Status | Decision |
|------|---------------|----------|--------|----------|
| E001 | Example error | 1        | open   |          |

## Proposal Items

### New Features

Use this section for `F` findings, or write `_No tracked findings._`.

### Errors And Mistakes

Use this section for `E` findings: wrong, stale, misleading, broken, contract-violating, or risky repository content.

#### E001. Example error

| Field       | Value                     |
|-------------|---------------------------|
| Status      | open                      |
| Decision    |                           |
| Decision at |                           |
| Priority    | 1                         |
| Owner       |                           |
| Updated     | YYYY-MM-DDTHH:mm:ss+HH:mm |

##### Context

- Evidence: Cite exact files, line references, commands, or observable behavior.
- Impact: Explain the maintainer-visible problem if this stays unchanged.
- Non-goals:
    - List anything this item deliberately does not change.
- Acceptance criteria:
    - State what must be true when this item is done.
    - State what must remain unchanged.

##### Recommended Change

State the smallest concrete change to make if this finding is accepted.

##### Review Notes

- none

<!--
For answered reviewer questions, use:

- question: <reviewer question>
    - answer: <short answer>
    - rationale: <why this answer is correct or preferred>
    - effect: <status / decision / wording impact, or `no status or decision change`>
-->

##### Follow-Up

- Artifact: ADR, approved plan, task, direct docs edit, changed file, open question, or none.
- Validation: Command, review check, manual check, or none.

### Duplications To Remove Or Reduce

Use this section for `D` findings, or write `_No tracked findings._`.

### Simplification Opportunities

Use this section for `S` findings, or write `_No tracked findings._`.

### Smaller / Stylistic Items

- Add untracked minor notes here, or write `_None._`.

## Suggested Priority Order

1. `E001` - explain why this should happen first.

## Out Of Scope

- List files, behaviors, or decisions this proposal intentionally does not change.
