---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Include Plan IDs In Plan Filenames

## Context and Problem Statement

ADR 0032 requires every implementation plan to have a stable, human-readable `Plan-ID`, but existing plan filename guidance still allowed filenames that omitted the ID.

The user requested that plans include the plan ID in the filename, consistent with other repository documents that expose stable identifiers in filenames or indexes.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Include Plan IDs In Plan Filenames
* Keep plan IDs only inside plan content.
* Duplicate both the ID and full title in every filename.

## Decision Outcome

Chosen option: "Adopt Include Plan IDs In Plan Filenames", because Every implementation plan file under `.agents/plans/`, including `.agents/plans/archive/`, must include its stable `Plan-ID` in the filename.

Every implementation plan file under `.agents/plans/`, including `.agents/plans/archive/`, must include its stable `Plan-ID` in the filename.

Use the `Plan-ID` as the filename prefix, for example `PLAN-scaffold-plugin-project.md`.

Keep the `Plan-ID` stable when the plan title, filename, status, or wording changes. When renaming a plan, preserve the `Plan-ID` in the filename.

### Consequences

- Plan files are easier to match to commit metadata, handoffs, and review notes.
- Renaming a plan for wording or status changes no longer hides the stable identifier.
- Documentation validation can catch plan files that omit their `Plan-ID`.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Include Plan IDs In Plan Filenames

* Good, because Every implementation plan file under `.agents/plans/`, including `.agents/plans/archive/`, must include its stable `Plan-ID` in the filename.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Keep plan IDs only inside plan content.

* Bad, because filenames remain weaker references during file browsing, diffs, and handoffs.

### Duplicate both the ID and full title in every filename.

* Bad, because the existing `Plan-ID` is already human-readable enough to stand alone as the filename prefix.

## More Information

- Rename the active scaffold plan to include `PLAN-scaffold-plugin-project`.
- Update plan guidance and documentation validation.
