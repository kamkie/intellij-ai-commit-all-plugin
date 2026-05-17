---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T21:23:41+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Treat Task IDs As TASKS Lookup Shortcuts

## Context and Problem Statement

ADR 0044 defines artifact lookup shortcuts for ADR, plan, and proposal references. `TASKS.md` also uses stable task IDs in `T-<AREA>-NNN` form, but `AGENTS.md` does not explicitly say that those IDs should route agents to `TASKS.md`.

Without an explicit lookup rule, agents may start with broad repository search for task IDs, miss the task's surrounding section context, or treat the task ID like a standalone artifact filename.

## Decision Drivers

* Keep task context small by searching the task owner file first.
* Make `T-<AREA>-NNN` references as actionable as `ard-NNNN`, `PLAN-*`, and `PROP-*` references.
* Preserve `TASKS.md` as the implementation backlog owner.
* Avoid implying that task IDs correspond to separate task files.
* Keep broad repository search available when the task entry is missing or its context points elsewhere.

## Considered Options

* Treat task IDs as `TASKS.md` lookup shortcuts
* Keep the current guidance map only
* Create separate task files for each task ID

## Decision Outcome

Chosen option: "Treat task IDs as `TASKS.md` lookup shortcuts", because stable task IDs identify entries inside `TASKS.md`, and agents should inspect the owning backlog entry before falling back to broader search.

AI-facing guidance should instruct agents to treat `T-<AREA>-NNN` references, for example `T-BUG-001`, as shortcuts to `TASKS.md`.

Agents should search `TASKS.md` first for the exact task ID. After finding the task entry, agents should use the task's surrounding section, linked ADRs, proposals, plans, open questions, and evidence notes to identify the governing artifacts for the work.

When `TASKS.md` does not contain the referenced task ID, or when the task entry points to another artifact, scoped or repository-wide search remains allowed.

### Consequences

* Good, because task requests such as `do T-BUG-001` go directly to the backlog owner.
* Good, because agents retain surrounding task context such as section headings, status, dependencies, and linked decisions.
* Good, because the rule complements ADR 0044's prefix lookup model.
* Bad, because `AGENTS.md` gains one more artifact lookup rule.

### Confirmation

Compliance should be checked by documentation review when AI-facing guidance changes.

The implementation should update `AGENTS.md` `## Artifact Lookup` with the `T-<AREA>-NNN` lookup rule.

## Pros and Cons of the Options

### Treat task IDs as `TASKS.md` lookup shortcuts

* Good, because it uses the stable task ID convention already present in `TASKS.md`.
* Good, because it prevents unnecessary broad searches for common task requests.
* Good, because task IDs remain entries in the backlog rather than becoming new standalone artifacts.
* Bad, because agents need to distinguish task IDs from artifact filenames.

### Keep the current guidance map only

* Good, because `AGENTS.md` already names `TASKS.md` as the implementation backlog.
* Bad, because the guidance map does not explicitly say how to resolve `T-<AREA>-NNN` references.
* Bad, because agents can miss the exact task context before editing.

### Create separate task files for each task ID

* Good, because each task could be addressed by exact file lookup.
* Bad, because it fragments the backlog and conflicts with the current `TASKS.md` owner model.
* Bad, because the requested instruction only needs lookup routing, not a backlog restructure.

## More Information

- Extends ADR 0044's artifact lookup guidance.
- Related task ID convention: ADR 0028.
- Implementation evidence: `AGENTS.md` `## Artifact Lookup` routes `T-<AREA>-NNN` references to `TASKS.md` first.
