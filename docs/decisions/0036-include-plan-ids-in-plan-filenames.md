# Include Plan IDs In Plan Filenames

Status: Accepted

Date: 2026-05-15

## Context

ADR 0032 requires every implementation plan to have a stable, human-readable `Plan-ID`, but existing plan filename guidance still allowed filenames that omitted the ID.

The user requested that plans include the plan ID in the filename, consistent with other repository documents that expose stable identifiers in filenames or indexes.

## Decision

Every implementation plan file under `.agents/plans/` must include its stable `Plan-ID` in the filename.

Use the `Plan-ID` as the filename prefix, for example `P-scaffold-plugin-project.md`.

Keep the `Plan-ID` stable when the plan title, filename, status, or wording changes. When renaming a plan, preserve the `Plan-ID` in the filename.

## Consequences

- Plan files are easier to match to commit metadata, handoffs, and review notes.
- Renaming a plan for wording or status changes no longer hides the stable identifier.
- Documentation validation can catch plan files that omit their `Plan-ID`.

## Alternatives Considered

- Keep plan IDs only inside plan content.
  - Why it was not chosen: filenames remain weaker references during file browsing, diffs, and handoffs.
- Duplicate both the ID and full title in every filename.
  - Why it was not chosen: the existing `Plan-ID` is already human-readable enough to stand alone as the filename prefix.

## Follow-Up

- Rename the active scaffold plan to include `P-scaffold-plugin-project`.
- Update plan guidance and documentation validation.
