---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T20:47:58+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Track ADR Implementation Status

## Context and Problem Statement

The repository records whether an ADR is proposed, accepted, rejected, deprecated, or superseded, but that MADR status describes the decision lifecycle rather than whether the accepted decision has been implemented.

After several accepted ADRs required follow-up documentation, validation, or runtime changes, it became hard to answer whether every accepted ADR had actually landed without manually reading plans, proposals, tasks, and commits.

This decision defines the repository-owned way to track ADR implementation status.

## Decision Drivers

* Keep MADR `status` focused on decision lifecycle.
* Make accepted-but-not-implemented ADRs visible in one place.
* Avoid scattering implementation tracking across `TASKS.md`, plans, and per-ADR notes.
* Preserve stable ADR references for handoffs, audits, and release checks.
* Keep the tracker easy to validate automatically.

## Considered Options

* Add a central ADR implementation tracker to `docs/decisions/README.md`
* Add implementation status fields to every ADR front matter block
* Track ADR implementation only through `TASKS.md`
* Track ADR implementation only through plans and commits

## Decision Outcome

Chosen option: "Add a central ADR implementation tracker to `docs/decisions/README.md`", because ADR implementation state is cross-cutting release-readiness metadata and should be visible beside the ADR index without overloading MADR decision status or depending on temporary backlog entries.

If accepted, `docs/decisions/README.md` should contain an `ADR Implementation Tracker` section after the ADR index. That tracker is the source of truth for implementation state of ADRs.

The tracker should use these implementation statuses:

* `not-required` - the ADR itself is the durable artifact, or the decision only governs future work and needs no separate repository change.
* `pending` - implementation is required but not yet covered by an approved plan, task, or landed change.
* `planned` - implementation is covered by an approved plan or explicit open task.
* `in-progress` - implementation has started but has not landed completely.
* `implemented` - the required code, documentation, validation, or workflow changes have landed.
* `blocked` - implementation is waiting on unresolved input, dependency, or external condition.

Each tracker row should include the ADR link, implementation status, evidence, and last updated date. Evidence should point to the relevant task, plan, commit, file, validation, blocker, open question, or state `not required` when no separate implementation exists. A `TASKS.md` entry is not required when another evidence path is clearer.

The existing ADR index should remain the decision-lifecycle index. The ADR implementation tracker should not replace MADR `status`, proposal finding status, plan status, or task checkboxes.

### Consequences

* Good, because maintainers and agents can answer whether accepted ADRs are implemented without reading every artifact.
* Good, because MADR `status` remains compatible with standard ADR meaning.
* Good, because release-readiness review can check accepted ADR implementation directly.
* Bad, because accepting or implementing ADRs requires one more tracker row update.
* Bad, because existing ADRs need an initial backfill.

### Confirmation

Compliance should be checked by documentation validation and review:

* `scripts/validate-docs.ps1` should require one implementation tracker row for every ADR listed in the ADR index.
* Tracker implementation status values should be limited to the accepted vocabulary.
* New accepted ADRs that require follow-up work should not remain without an evidence path such as a task, plan, direct implementation evidence, or explicit blocker.
* Release-preparation review should check for accepted ADRs whose implementation status is not `implemented` or `not-required`.

## Pros and Cons of the Options

### Add a central ADR implementation tracker to `docs/decisions/README.md`

* Good, because `docs/decisions/README.md` already owns the ADR index.
* Good, because one central table can show implementation status without touching every ADR for routine progress changes.
* Good, because validation can compare the index and tracker rows.
* Bad, because the README table becomes larger.

### Add implementation status fields to every ADR front matter block

* Good, because implementation status lives next to the decision text.
* Good, because each ADR is self-contained.
* Bad, because it overloads ADR metadata with execution state.
* Bad, because routine implementation progress would churn many ADR files.
* Bad, because it could be confused with MADR `status`.

### Track ADR implementation only through `TASKS.md`

* Good, because the current open backlog already uses task checkboxes.
* Bad, because `TASKS.md` is not guaranteed to remain the durable status owner.
* Bad, because completed task history is less direct than a per-ADR implementation view.
* Bad, because accepted ADRs without open tasks become hard to audit.

### Track ADR implementation only through plans and commits

* Good, because implementation evidence already exists there.
* Bad, because status must be reconstructed from multiple artifacts.
* Bad, because there is no single answer to whether all accepted ADRs are implemented.

## More Information

- Source task: `TASKS.md` `T-IDEA-003`.
- Related decision lifecycle guidance: `docs/decisions/README.md`.
- Related timestamp rule: ADR 0045.
- Follow-up implementation, after this ADR is accepted: update `docs/decisions/README.md`, `docs/decisions/ADR_TEMPLATE.md` if needed, `scripts/validate-docs.ps1`, and `TASKS.md`; then backfill implementation tracker rows for existing ADRs.
