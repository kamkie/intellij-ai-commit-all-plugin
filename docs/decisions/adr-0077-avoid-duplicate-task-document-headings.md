---
status: accepted
date: 2026-05-23
accepted_at: 2026-05-23T21:56:10+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Avoid Duplicate Task Document Headings

## Context and Problem Statement

`TASKS.md` and `TASKS_ARCHIVE.md` are lookup surfaces for active and completed
task refs. Repeated Markdown headings in those files make scoped search results
harder to interpret, create ambiguous insertion points for future task moves,
and make the archive feel less like one navigable register.

How should task documentation preserve readable grouping without duplicating
section headings inside the same file?

## Decision Drivers

* Keep task lookup predictable for `T-<AREA>-NNN` refs.
* Keep archive insertion points unambiguous.
* Keep archive date notes inside merged sections instead of using them to
  distinguish repeated headings.
* Preserve existing task refs, task wording, and validation evidence while
  improving navigation.
* Keep the rule simple enough for agents and maintainers to apply during normal
  backlog and archive edits.

## Considered Options

* Forbid duplicate headings in task documents.
* Allow duplicate headings when separated by archive date notes.
* Apply the rule only to `TASKS.md`.
* Replace headings with a table-only task register.

## Decision Outcome

Chosen option: "Forbid duplicate headings in task documents", because the task documents are registers, and repeated section headings inside one register make future edits and task lookup less reliable.

`TASKS.md` and `TASKS_ARCHIVE.md` must not contain duplicate Markdown section
headings within the same file. New task rows must be merged under an existing
matching heading, and date, release, plan, or domain context must be preserved
as text inside that section instead of by creating another heading.

This decision does not change task refs, completion criteria, archive rules, or
the distinction between active tasks and completed task history.

### Consequences

* Good, because contributors and agents have one clear destination for each task
  category in active backlog files.
* Good, because archive headings become stable navigation anchors instead of
  repeated labels.
* Good, because future task moves are less likely to add another ambiguous
  section.
* Bad, because existing repeated archive headings may need a cleanup pass.
* Bad, because some archived groupings need context text inside merged sections
  to preserve their historical meaning.

### Confirmation

Confirm implementation by checking:

* `.agents/references/documentation.md` states that `TASKS.md` and
  `TASKS_ARCHIVE.md` must not duplicate Markdown headings within the same file.
* `TASKS.md` and `TASKS_ARCHIVE.md` have no duplicate headings within each file,
  or a follow-up cleanup task exists if the archive cleanup is intentionally
  staged.
* Documentation validation passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Forbid Duplicate Headings In Task Documents

This option makes heading uniqueness part of the task-document maintenance
rules.

* Good, because it keeps task insertion points deterministic.
* Good, because it avoids multiple identical anchors in rendered Markdown.
* Good, because it supports scoped search and small-context task lookup.
* Bad, because it requires a cleanup when existing archive sections repeat a
  heading.

### Allow Duplicate Headings When Separated By Archive Date Notes

This option keeps the current archive style and relies on surrounding text for
context.

* Good, because it preserves the current archive layout with minimal edits.
* Good, because each historical batch can keep short headings like
  `Documentation` or `Testing`.
* Bad, because repeated headings still create ambiguous anchors and insertion
  points.
* Bad, because agents must load surrounding context to know which repeated
  heading is meant.

### Apply The Rule Only To TASKS.md

This option keeps active backlog navigation strict but leaves the archive loose.

* Good, because active work gets the strongest navigation guarantee.
* Good, because it avoids archive cleanup.
* Bad, because `TASKS_ARCHIVE.md` is also a lookup surface and can grow much
  larger than `TASKS.md`.
* Bad, because moving a completed task from active backlog to archive can still
  create duplicate archive headings.

### Replace Headings With A Table-Only Task Register

This option removes section headings as grouping owners.

* Good, because a single table can be sorted and searched consistently.
* Good, because duplicate headings disappear by construction.
* Bad, because a table-only register would be a larger documentation migration.
* Bad, because the current task files rely on readable sections for area and
  historical context.

## More Information

Related artifacts:

* `TASKS.md`
* `TASKS_ARCHIVE.md`
* `.agents/references/documentation.md`
* `scripts/validate-docs.ps1`

The accepted implementation updates the ADR Implementation Tracker in
`docs/decisions/README.md` with implementation status, evidence, and last
updated date, implements the rule in task-document guidance, and validates the
rule in documentation checks.
