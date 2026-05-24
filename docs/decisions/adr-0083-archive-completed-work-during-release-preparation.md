---
status: accepted
date: 2026-05-24
accepted_at: 2026-05-24T23:19:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Archive Completed Work During Release Preparation

## Context and Problem Statement

Release preparation already owns cross-task review, validation, changelog
updates, support-policy checks, artifact preparation, and publication readiness.
Completed tasks, closed plans, and retired proposals can still remain in active
indexes when release preparation starts, which makes the release boundary harder
to inspect and leaves active artifacts carrying work that no longer needs
execution.

Should release preparation require a completed-work archive sweep before a
release is tagged or published?

## Decision Drivers

* Keep active backlog, plan, and proposal indexes focused on unfinished work.
* Make the release boundary reflect the repository's current state.
* Preserve completed-work history in the existing archive owners.
* Reuse the existing archive-readiness rules instead of creating a separate
  release-only archival process.
* Avoid archiving work that lacks validation, self-review, terminal status, or
  required closeout evidence.

## Considered Options

* Require release preparation to archive all archive-ready completed work.
* Leave completed-work archiving as ad hoc maintenance.
* Archive only completed tasks during release preparation.

## Decision Outcome

Chosen option: "Require release preparation to archive all archive-ready completed work", because release preparation is the natural repository boundary where active work indexes should be cleared of finished execution artifacts.

If accepted:

* Release preparation must run a full `archive-completed-work.md` sweep before
  final release handoff, tagging, or publication.
* The sweep must cover completed `TASKS.md` entries, closed plans in
  `.agents/plans/`, completed or retired proposals in `docs/proposals/`, and
  their indexes.
* Archive operations must preserve stable task, plan, proposal, and finding refs
  and continue to use the existing archive locations.
* Existing archive-readiness rules still apply. Do not archive tasks without
  validation or self-review evidence, plans without `Status: Closed` and
  `Close-Reason`, or proposals with non-terminal implementation rows or
  untriaged findings.
* Any completed-looking item that cannot be archived must be resolved before
  release or recorded in the release handoff as a release blocker or explicit
  skipped archive with the missing evidence.
* The release guide should list completed-work archiving in release preparation
  coverage and pre-tag checks.

### Consequences

* Good, because release preparation leaves active work indexes easier to scan.
* Good, because completed work history stays preserved under the existing
  archive owners.
* Good, because the release handoff will expose incomplete closeout evidence
  instead of silently carrying stale active artifacts.
* Bad, because release preparation gains one more required documentation
  maintenance step.
* Bad, because incomplete closeout metadata can block a release until it is
  corrected or explicitly documented.

### Confirmation

After acceptance and implementation, confirm by checking:

* `.agents/references/releases.md` requires a full completed-work archive sweep
  during release preparation.
* The release guidance references `archive-completed-work.md` or equivalent
  archive mechanics for tasks, plans, and proposals.
* The guidance preserves the existing archive-readiness gates for validation,
  self-review, terminal plan status, proposal tracker state, and stable refs.
* Release handoff guidance records unresolved or skipped archive candidates.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  passes.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`
  passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Require Release Preparation To Archive All Archive-Ready Completed Work

This option makes completed-work archiving a release-preparation gate while
retaining the repository's existing archive rules.

* Good, because it creates a predictable cleanup boundary.
* Good, because it keeps active task, plan, and proposal indexes current before
  release.
* Good, because it does not weaken validation, self-review, plan closeout, or
  proposal tracker requirements.
* Bad, because release preparation can be blocked by missing closeout evidence.

### Leave Completed-Work Archiving As Ad Hoc Maintenance

This option keeps the current behavior where archive sweeps happen only when a
maintainer or prompt request asks for them.

* Good, because it avoids adding release-preparation work.
* Bad, because completed artifacts can remain active across release boundaries.
* Bad, because stale active indexes make release readiness harder to audit.

### Archive Only Completed Tasks During Release Preparation

This option would require release prep to clean `TASKS.md` while leaving plans
and proposals to separate maintenance.

* Good, because it covers the most visible active backlog.
* Bad, because completed plans and retired proposals are also active work
  surfaces.
* Bad, because release handoff would still need a separate stale-plan and
  stale-proposal audit.

## More Information

This ADR extends ADR 0029 release guidance and ADR 0066 completed-task archive
guidance. It does not change product behavior, changelog eligibility, release
versioning, Marketplace publication mechanics, or the existing archive
readiness rules.

No companion implementation plan is used because the implementation is a
bounded release-guidance documentation edit after ADR acceptance.

After this ADR is accepted, update the ADR Implementation Tracker in
`docs/decisions/README.md` with implementation status, evidence, and last
updated date.
