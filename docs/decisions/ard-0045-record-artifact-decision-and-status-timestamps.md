---
status: accepted
date: 2026-05-15
accepted_at: 2026-05-15T11:33:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Record Artifact Decision And Status Timestamps

## Context and Problem Statement

The repository records durable decisions in ADRs, approved implementation work in plans, and advisory triage in proposals. Current artifacts record dates in several places, but they do not consistently record the exact time an ADR was accepted, a plan was approved, or a proposal finding was accepted.

Plans also expose only the current `Status:` and readiness summary. They do not preserve a timestamped history of status transitions such as `Draft`, `Approved`, `In Progress`, `Blocked`, `Implemented`, and `Closed`.

The maintainer requested that ADRs, plans, and proposals record the time when they were accepted or approved, and that plans record the time of every status change.

## Decision Drivers

* Make acceptance and approval moments auditable without reading conversation history.
* Preserve plan status transition history inside the plan file.
* Keep timestamp format consistent across ADRs, plans, and proposal trackers.
* Avoid rewriting historical artifacts unless they are materially updated.
* Keep validation practical and scoped to current artifact rules.

## Considered Options

* Record timestamp fields and plan status history
* Keep date-only artifact metadata
* Rely on Git history for timing

## Decision Outcome

Chosen option: "Record timestamp fields and plan status history", because acceptance, approval, and status transitions are workflow events that should be visible in the governing artifact itself.

If accepted, use ISO 8601 timestamps with timezone offsets for new event-time fields, for example `2026-05-15T10:30:00+02:00`.

ADRs:

* Keep the existing MADR `date:` field as the decision record date.
* Add `accepted_at: <timestamp>` to ADR front matter when `status: accepted`.
* When a proposed ADR is later accepted, set `accepted_at` to the actual acceptance time.

Plans:

* Add `Approved at: <timestamp>` in `## Readiness` when a plan reaches `Status: Approved` or later.
* Add a required `## Status History` section to plan files.
* Record every status change as a timestamped entry containing at least timestamp, from-status, to-status, actor when known, and short reason.
* The first entry should record initial creation as `none -> Draft` or equivalent.
* `Status:` remains the current canonical status and must match the latest status-history entry.

Proposals:

* For proposal finding YAML blocks, add `accepted_at: <timestamp>` when `decision: accepted`.
* For non-accepted decisions, use `decided_at: <timestamp>` when the finding is triaged as `rejected`, `deferred`, or another non-empty decision.
* Keep `updated:` for the last content or tracker update, but change new or materially updated proposal findings to use timestamps rather than date-only values.
* Historical archived proposals do not need retroactive timestamps unless materially updated for another reason.

After acceptance, update the relevant artifact templates, guidance, and `scripts/validate-docs.ps1` together.

### Consequences

* Good, because accepted ADRs show exactly when acceptance happened.
* Good, because plans preserve the full lifecycle history rather than only the latest status.
* Good, because proposal tracker decisions become easier to audit.
* Bad, because templates, guidance, existing active artifacts, and validation need coordinated updates.
* Bad, because timestamp fields add maintenance overhead whenever statuses or decisions change.

### Confirmation

Compliance will be checked through documentation review and `scripts/validate-docs.ps1` after templates and validation are updated.

Validation should require timestamp fields for current active artifacts according to the accepted rules while allowing historical archived artifacts to remain unchanged unless they are materially updated.

## Pros and Cons of the Options

### Record timestamp fields and plan status history

* Good, because acceptance and approval times are visible in the artifact that governs the work.
* Good, because plan status changes can be audited without reconstructing them from commits or chat.
* Good, because the same timestamp format can be used across artifact types.
* Bad, because every status transition and tracker decision needs a timestamp update.

### Keep date-only artifact metadata

* Good, because it avoids changing artifact format and validation.
* Bad, because the repository loses the exact acceptance or approval time.
* Bad, because plans still cannot show when each status transition happened.

### Rely on Git history for timing

* Good, because commits already have timestamps.
* Bad, because acceptance or approval may happen before the commit that records it.
* Bad, because reviewers would need to inspect history instead of reading the artifact.
* Bad, because multiple status or decision events can land in one commit.

## More Information

- Related plan lifecycle decision: ADR 0037.
- Related plan approval identity decision: ADR 0042.
- Related proposal tracker decision: ADR 0043.
- Follow-up implementation, after this ADR is accepted: update ADR guidance, proposal guidance, plan guidance, templates, active artifacts, and documentation validation.
