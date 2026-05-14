---
generated_at: YYYY-MM-DDTHH-MM
purpose: One sentence describing what this document proposes.
scope: One sentence describing what part of the repository is covered.
---

# Proposal title

This proposal respects `AGENTS.md`, `TASKS.md`, `OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E1. Example error](#e1-example-error)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- Replace this bullet with the main finding.
- Replace this bullet with the intended triage scope.
- State clearly that no implementation is performed by the proposal.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id | Title         | Priority | Status | Decision |
|----|---------------|----------|--------|----------|
| E1 | Example error | 1        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E1. Example error

- Evidence: Cite files, line references, commands, or observable facts.
- Impact: Explain why it matters.
- Proposal: State the concrete change to make.

```yaml
status: open
decision:
priority: 1
owner:
updated: YYYY-MM-DD
comment:
```

## Duplications To Remove Or Reduce

Add `D<n>` findings here, or write `_No tracked findings._`.

## Simplification Opportunities

Add `S<n>` findings here, or write `_No tracked findings._`.

## Smaller / Stylistic Items

- Add untracked minor notes here.

## Suggested Priority Order

1. `E1` - explain why this should happen first.

## Out Of Scope

- List files, behaviors, or decisions this proposal intentionally does not change.
