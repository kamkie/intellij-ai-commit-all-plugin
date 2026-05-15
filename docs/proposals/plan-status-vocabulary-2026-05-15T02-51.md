---
proposal_id: PROP-plan-status-vocabulary
generated_at: 2026-05-15T02-51
purpose: Propose a compact implementation-plan status vocabulary and transition guidance.
scope: Covers `.agents/plans/`, `.agents/references/planning.md`, documentation validation, and related plan workflow guidance.
---

# Plan Status Vocabulary Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It lists workflow findings for maintainer triage only; it does not change plan status rules by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
    - [S1. Define a compact canonical plan status vocabulary](#s1-define-a-compact-canonical-plan-status-vocabulary)
    - [S2. Add transition guidance for plan statuses](#s2-add-transition-guidance-for-plan-statuses)
    - [S3. Validate plan status values](#s3-validate-plan-status-values)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- Before ADR 0037, plan guidance only documented `Draft`, `Accepted`, `Implemented`, and `Superseded`, which was too coarse for review, execution, release, rejection, and archival states.
- ADR 0037 accepted the compact canonical status set: `Draft`, `Approved`, `In Progress`, `Blocked`, `Implemented`, and `Closed`.
- `Closed` carries outcome traceability through `Close-Reason: Released`, `Rejected`, `Superseded`, `Deferred`, or `Archived`.
- The guidance now avoids ambiguous duplicates, especially `Accepted` versus `Approved`, `Implementing` versus `In Progress`, and `Defered` versus `Deferred`.
- Plan readiness, open questions, and implementation progress are now visible in a required `## Readiness` section instead of being overloaded into a large status list.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id | Title                                            | Priority | Status | Decision |
|----|--------------------------------------------------|----------|--------|----------|
| S1 | Define a compact canonical plan status vocabulary | 2        | done   | accepted |
| S2 | Add transition guidance for plan statuses         | 2        | done   | accepted |
| S3 | Validate plan status values                       | 3        | done   | accepted |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

_No tracked findings._

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

### S1. Define a compact canonical plan status vocabulary

- Evidence: Before ADR 0037, `.agents/plans/README.md` listed only `Draft`, `Accepted`, `Implemented`, and `Superseded` for plan lifecycle states. `.agents/plans/PLAN_TEMPLATE.md` started new plans at `Status: Draft`, and `.agents/plans/P-scaffold-plugin-project.md` moved directly from `Accepted` to `Implemented`.
- Impact: Plans that were under review, approved but not yet executing, actively executing, blocked, deferred, rejected, released, or archived had no clear status value. Agents and maintainers could encode these states inconsistently in notes instead of the `Status` field.
- Proposal: Adopt the canonical status set `Draft`, `Approved`, `In Progress`, `Blocked`, `Implemented`, and `Closed`. Require `Closed` plans to carry `Close-Reason: Released`, `Rejected`, `Superseded`, `Deferred`, or `Archived`. Treat `Accepted` as the old readiness status, `Implementing` as a non-canonical alias for `In Progress`, and `Defered` as a typo for `Deferred`.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-15
comment: Implemented by ADR 0037 with the compact status set.
```

### S2. Add transition guidance for plan statuses

- Evidence: Before ADR 0037, planning guidance explained when to create plans and how to execute multi-task plans, but it did not define status transitions, status owners, or when to move a plan from implementation to closure.
- Impact: A plan could be marked `Implemented` before release validation, or left `In Progress` after all tasks were committed. `Blocked`, `Deferred`, `Rejected`, and `Superseded` could also be used without enough context unless guidance required a reason and owner.
- Proposal: Add compact transition guidance to `.agents/plans/README.md` and `.agents/references/planning.md`. Require `Blocked` plans to link to a blocker such as `docs/decisions/OPEN_QUESTIONS.md`, an ADR, or a task. Require closed plans to include a close reason so release, rejection, supersession, deferral, and archival outcomes remain traceable.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-15
comment: Implemented by ADR 0037 and the updated plan guidance.
```

### S3. Validate plan status values

- Evidence: Before this implementation, `scripts/validate-docs.ps1` checked plan filenames for `Plan-ID`, but it did not validate plan `Status` values. The proposal workflow had a controlled status vocabulary and validation checks for proposal tracker consistency, so plan statuses could follow the same pattern.
- Impact: Status drift would be easy to introduce once more values existed, especially with near-duplicates such as `Approved` and `Accepted`, `Implementing` and `In Progress`, or spelling variants such as `Deferred` and `Defered`.
- Proposal: Extend documentation validation to check that every plan has a `Status` line and that the value is in the canonical vocabulary. If aliases are allowed, normalize them in guidance rather than accepting multiple spellings in files.

```yaml
status: done
decision: accepted
priority: 3
owner:
updated: 2026-05-15
comment: Implemented by validating plan status values, closed-plan reasons, and readiness sections.
```

## Smaller / Stylistic Items

- `Accepted` was replaced by `Approved` for live plan status guidance.
- Keep status labels in Title Case to match current plan files.
- Keep release workflow detail in `.agents/references/releases.md`; plan guidance only records `Released` as a close reason.

## Suggested Priority Order

1. `S1` - choose the vocabulary before changing templates or validation.
2. `S2` - define transitions so the new vocabulary is not just a larger list.
3. `S3` - add validation after the accepted vocabulary and transitions are stable.

## Out Of Scope

- No release workflow, plugin behavior, Gradle configuration, Kotlin code, or `plugin.xml` metadata is changed by this proposal.
