---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T21:01:37+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Track Proposal Implementation Status

## Context and Problem Statement

Proposal findings already carry both `status` and `decision` fields, but the current proposal guidance uses one shared vocabulary for both fields. That makes it hard to distinguish maintainer triage from implementation progress.

Accepted proposal findings can remain open after triage, and maintainers need a direct way to see which accepted proposal findings still need implementation without scanning every proposal file by hand.

This decision defines how proposal implementation status should be tracked.

## Decision Drivers

* Preserve proposals as advisory artifacts until triaged.
* Keep maintainer triage separate from implementation progress.
* Avoid creating a second source of truth for every proposal finding.
* Make accepted-but-not-finished proposal findings visible from `docs/proposals/README.md`.
* Keep validation practical for proposal tracker edits.

## Considered Options

* Use per-finding proposal trackers as the source of truth and add a README implementation summary
* Add a central tracker row for every proposal finding
* Track accepted proposal implementation only through `TASKS.md`
* Keep the current shared status and decision vocabulary

## Decision Outcome

Chosen option: "Use per-finding proposal trackers as the source of truth and add a README implementation summary", because each proposal finding already owns detailed evidence, priority, decision, timestamps, and comments, while a compact README summary can make accepted unfinished work visible without duplicating the whole tracker.

If accepted, proposal trackers should use separate meanings for `decision` and `status`:

* `decision` records maintainer triage. Valid standard values are empty, `accepted`, `rejected`, and `deferred`.
* `status` records implementation progress. Valid standard values are `open`, `planned`, `in-progress`, `blocked`, `done`, and `not-required`.

`docs/proposals/README.md` should add a `Proposal Implementation Summary` section. The summary should list accepted findings whose implementation status is not terminal. Terminal proposal implementation statuses are `done` and `not-required`.

The per-finding YAML tracker remains the source of truth. The README summary is a maintained overview for currently actionable accepted proposal work.

Rejected findings should use `decision: rejected` and `status: not-required`. Deferred findings should use `decision: deferred` and either `status: blocked` or `status: not-required`, depending on whether future implementation is still expected.

### Consequences

* Good, because a proposal finding can be accepted without pretending it has already landed.
* Good, because accepted unfinished proposal work becomes visible from the proposal README.
* Good, because detailed finding evidence stays in the proposal file where it belongs.
* Bad, because proposal tracker edits require one more consistency check between per-finding YAML and the README summary.
* Bad, because existing active proposals need vocabulary cleanup and summary backfill.

### Confirmation

Compliance should be checked by documentation validation and review:

* `scripts/validate-docs.ps1` should validate separate proposal `status` and `decision` vocabularies.
* Proposal progress-table rows should continue to mirror per-finding YAML.
* Accepted findings with non-terminal implementation status should appear in the README implementation summary.
* Accepted findings with terminal implementation status should not appear in the README implementation summary.
* Proposal README active and completed lists should be based on implementation status, not only on the literal `open` value.

## Pros and Cons of the Options

### Use per-finding proposal trackers as the source of truth and add a README implementation summary

* Good, because it builds on the existing proposal structure.
* Good, because it avoids duplicating every finding in a repository-wide table.
* Good, because the README can show accepted unfinished work directly.
* Bad, because the summary still needs validation to prevent drift.

### Add a central tracker row for every proposal finding

* Good, because all proposal implementation state would be visible in one table.
* Bad, because it duplicates each proposal's detailed progress tracker.
* Bad, because large proposal sets would make `docs/proposals/README.md` noisy.

### Track accepted proposal implementation only through `TASKS.md`

* Good, because implementation work can already be expressed as tasks.
* Bad, because proposals are not guaranteed to map one-to-one with tasks.
* Bad, because completed or rejected proposal findings become harder to audit after backlog cleanup.

### Keep the current shared status and decision vocabulary

* Good, because it requires no immediate documentation churn.
* Bad, because `accepted` can mean either a decision or a status.
* Bad, because accepted-but-not-implemented findings remain hard to distinguish from untriaged findings.

## More Information

- Source task: `TASKS.md` `T-IDEA-004`.
- Related proposal governance decisions: ADR 0033, ADR 0034, ADR 0043, and ADR 0045.
- Related ADR implementation tracker decision: ADR 0048.
- Follow-up implementation, after this ADR is accepted: update `docs/proposals/README.md`, `docs/proposals/PROPOSAL_TEMPLATE.md`, active proposal files, `scripts/validate-docs.ps1`, and `TASKS.md`.
