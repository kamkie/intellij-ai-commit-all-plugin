---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T11:21:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Require Single Branch Multi-Agent Execution Until Worktree Decision

## Context and Problem Statement

ADR 0003 defines all-files commit scope, ADR 0009 limits first-phase VCS support to Git, ADR 0023 requires one commit per plan task, and ADR 0026 permits parallel workers only for independent tasks with disjoint write scopes. No accepted rule currently authorizes per-worker git worktrees, defines merge-back order, or explains how worktrees preserve the one-commit-per-task boundary.

`docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` finding S007 proposes deciding the execution topology when a real approved plan needs worktrees.

## Decision Drivers

* Preserve ADR 0023's one-commit-per-task boundary.
* Avoid accidental worktree use without merge-back and validation rules.
* Keep current orchestrated execution understandable.
* Leave room for future worktree rules when a concrete plan proves the need.
* Avoid adding validation expectations for a topology the repository is not ready to use.

## Considered Options

* Require single-branch execution until a future worktree ADR
* Allow orchestrators to choose per-worker worktrees when tasks are disjoint
* Require per-worker worktrees for parallel execution

## Decision Outcome

Chosen option: "Require single-branch execution until a future worktree ADR", because the repository should not allow a topology that has no accepted merge-back, validation, conflict, or failed-worker handoff rules.

Orchestrated multi-agent execution stays on the current branch. Per-worker git worktrees are not authorized by default, even for parallel tasks, until a future accepted ADR tied to a concrete approved plan defines the worktree topology.

That future ADR must decide whether orchestrators may choose between a single branch and per-worker git worktrees, or whether single-branch execution remains required. If it allows worktrees, it must define:

* Worktree use only for parallel tasks that the approved plan marks independent with disjoint write scopes under ADR 0026.
* Merge-back sequencing.
* Whether validation runs inside each worker worktree, after merge-back, or both.
* How each plan task still lands as a distinct reviewed and validated unit under ADR 0023.
* How uncommitted worker diffs, failed worker tasks, and conflict resolution are handed back to the orchestrator.

Implementation updates `.agents/references/execution.md` to make single-branch execution the default and to require a future ADR before worktree use. `.agents/references/testing.md` does not need to change unless a later ADR changes validation expectations.

### Consequences

* Good, because the current topology remains explicit and conservative.
* Good, because worktree execution cannot accidentally weaken task commit boundaries.
* Good, because future worktree adoption must answer merge-back and validation details first.
* Bad, because parallel workers must coordinate in one worktree until a future ADR allows otherwise.
* Bad, because a future plan that would benefit from worktrees must pause for another ADR.

### Confirmation

Compliance is checked by documentation review after acceptance:

* `.agents/references/execution.md` says orchestrated execution uses the current branch unless a future accepted ADR authorizes worktrees.
* Any future plan that proposes per-worker worktrees links to an accepted topology ADR.
* No validation guidance claims worktree coverage before such an ADR exists.

## Pros and Cons of the Options

### Require single-branch execution until a future worktree ADR

* Good, because it matches the current documented execution model.
* Good, because it prevents under-specified worktree merge-back behavior.
* Good, because it leaves a clear path for future worktree adoption.
* Bad, because it limits parallel execution ergonomics in the meantime.

### Allow orchestrators to choose per-worker worktrees when tasks are disjoint

* Good, because disjoint parallel work could avoid local file conflicts.
* Bad, because merge-back order, validation timing, and failed-worker handoff remain undefined.
* Bad, because task commits could blur during merge-back without stronger rules.

### Require per-worker worktrees for parallel execution

* Good, because parallel workers would start from isolated working directories.
* Bad, because it imposes a more complex topology before the repository has rules for it.
* Bad, because simple disjoint documentation tasks would require unnecessary worktree overhead.

## More Information

- Source proposal finding: `docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` S007.
- Related decisions: ADR 0003, ADR 0009, ADR 0023, and ADR 0026.
