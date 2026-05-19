---
status: accepted
date: 2026-05-19
accepted_at: 2026-05-19T22:11:36+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Future maintainers and AI agents
---

# Archive Completed Tasks Outside Active Backlog

## Context and Problem Statement

`TASKS.md` currently contains the active backlog and a long completed task archive. The open backlog is now empty, but completed implementation, validation, release, bug, and idea tasks still dominate the file.

This makes `TASKS.md` less useful for its primary working purpose: quickly answering what remains open and what should happen next. The repository still needs to preserve completed task history because task IDs are stable references used by ADRs, plans, proposals, commits, and handoffs.

## Decision Drivers

* Keep `TASKS.md` small enough to scan for active work.
* Preserve stable `T-AREA-NNN` task IDs and historical task text.
* Keep completed task history in the repository instead of relying only on Git history.
* Avoid moving task history into ADRs, plans, proposals, or changelog entries where it does not belong.
* Make future backlog cleanup mechanical and low risk.
* Preserve the existing rule that completed tasks move only after work is finished, validated, and self-reviewed.

## Considered Options

* Move completed task history to `TASKS_ARCHIVE.md`
* Keep completed task history inside `TASKS.md`
* Delete completed task history after completion
* Archive completed tasks by plan or release-specific files

## Decision Outcome

Chosen option: "Move completed task history to `TASKS_ARCHIVE.md`", because `TASKS.md` should stay focused on active work while task history remains searchable, stable, and close to the backlog.

When accepted, implementation should:

* Create root `TASKS_ARCHIVE.md`.
* Move the existing `## Completed Task Archive` content from `TASKS.md` into `TASKS_ARCHIVE.md`.
* Keep completed task IDs, wording, grouping, and evidence links stable unless a small clarity fix is needed.
* Leave `TASKS.md` with the repository state summary, task notation rules, `## Open Backlog`, and a link to `TASKS_ARCHIVE.md`.
* Keep `_No open tasks._` in `TASKS.md` when no tasks remain.
* Add a short archive policy to `TASKS.md` or `TASKS_ARCHIVE.md` that says completed tasks move only after completion, validation, and self-review.
* Update artifact lookup guidance only if needed so `T-<AREA>-NNN` references search `TASKS.md` first and `TASKS_ARCHIVE.md` second.
* Do not update `CHANGELOG.md`, because this is internal task-history organization and does not affect public plugin behavior, public docs, support promises, or release artifacts.

### Consequences

* Good, because `TASKS.md` becomes a concise active backlog again.
* Good, because completed task history stays versioned and searchable.
* Good, because task IDs remain stable and usable as historical references.
* Good, because future backlog cleanup has a repeatable rule.
* Bad, because task lookup may require checking two files when a task is not active.
* Bad, because references that currently assume all tasks live in `TASKS.md` may need a small guidance update.

### Confirmation

After acceptance and implementation, confirm with:

* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1`
* `git diff --check`
* A scoped search for representative active and archived task IDs to confirm active IDs remain in `TASKS.md` and historical IDs are present in `TASKS_ARCHIVE.md`.

## Pros and Cons of the Options

### Move Completed Task History To `TASKS_ARCHIVE.md`

* Good, because it separates active backlog from historical ledger without losing task history.
* Good, because a single archive file is simple to search and maintain.
* Good, because the root location keeps task history easy to discover.
* Good, because existing task group headings can move without redesigning the task model.
* Neutral, because artifact lookup rules may need to mention the archive fallback.
* Bad, because contributors must remember that completed tasks may live outside `TASKS.md`.

### Keep Completed Task History Inside `TASKS.md`

* Good, because every task remains in one file.
* Good, because current artifact lookup guidance needs no update.
* Bad, because the active backlog is buried under historical content.
* Bad, because answering "what is next?" requires filtering through completed work.
* Bad, because each completed milestone makes the active task file heavier.

### Delete Completed Task History After Completion

* Good, because `TASKS.md` would stay very small.
* Bad, because stable task IDs would lose their durable repository home.
* Bad, because ADRs, plans, proposals, commits, and handoffs that reference task IDs would become harder to follow.
* Bad, because Git history is not a practical task lookup interface during normal maintenance.

### Archive Completed Tasks By Plan Or Release-Specific Files

* Good, because task history could be organized by originating work stream.
* Good, because large future releases could keep separate task ledgers.
* Bad, because lookup becomes fragmented across multiple files.
* Bad, because many completed tasks are not cleanly owned by one plan or release.
* Bad, because this repository does not yet need that level of archive structure.

## More Information

- Source request: "draft a way of archiving old tasks", followed by "implement that".
- Related guidance: `AGENTS.md` artifact lookup for `T-<AREA>-NNN` references and `TASKS.md` stable task ID rules.
- Implementation was intentionally blocked until this ADR was accepted.
