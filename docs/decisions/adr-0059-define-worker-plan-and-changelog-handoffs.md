---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T11:21:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Define Worker Plan And Changelog Handoffs

## Context and Problem Statement

ADR 0023 requires one commit per task in multi-task plans, ADR 0026 defines orchestrator and task-worker responsibilities, and ADR 0030 makes the orchestrator responsible for changelog maintenance. The current guidance does not unambiguously assign who updates the governing plan file after a worker finishes a task, and it does not spell out the changelog cadence at the worker-handoff boundary.

`docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` findings S004 and S005 propose aligning plan status and changelog review at the same handoff point.

## Decision Drivers

* Keep plan task state aligned with actual execution state.
* Preserve ADR 0023's one-commit-per-task boundary.
* Preserve ADR 0030's orchestrator ownership of final changelog wording.
* Make worker-to-orchestrator handoffs explicit.
* Capture notable changes while the task context is fresh.

## Considered Options

* Require explicit plan and changelog handoffs at each worker boundary
* Let only the orchestrator update plan files and changelog
* Let each worker independently update both plan files and changelog
* Defer plan and changelog updates until release preparation

## Decision Outcome

Chosen option: "Require explicit plan and changelog handoffs at each worker boundary", because the worker sees task completion details while the orchestrator owns cross-task continuity and final changelog coherence.

After completing a plan task, the worker updates the governing plan file for that task in the same commit as the task work. If the worker cannot or should not update the plan file, the worker must explicitly hand off that responsibility to the orchestrator within the same execution step. When responsibility is handed off, the orchestrator updates the plan file before dispatching the next dependent task.

The handoff must be recorded in the chat transcript or in durable orchestrator logs if a later accepted ADR introduces them. The plan file must reflect the completed, failed, blocked, or otherwise current task state before dependent work starts.

After every worker handoff for a task that produces a user-visible, contributor-visible, workflow-visible, compatibility, support, release, or validation-policy change, the orchestrator updates the next unreleased `CHANGELOG.md` section before dispatching the next task. Purely internal tasks with no notable external or workflow effect may be grouped into one later entry. When grouping is chosen, the orchestrator records the reason in the chat transcript or durable orchestrator log if one exists.

The changelog edit should ride along in the same task commit when feasible and when it does not break the one-commit-per-task boundary. The orchestrator may supply or review the changelog wording before that commit is created. If a separate orchestrator commit is needed, it must occur before the next task starts and must use the multi-agent attribution trailers if those trailers have been accepted.

Implementation updates `.agents/references/execution.md`, `.agents/references/planning.md`, and `.agents/references/releases.md`.

### Consequences

* Good, because plan task status cannot drift silently after worker completion.
* Good, because changelog review happens while task context is still fresh.
* Good, because the orchestrator keeps final changelog responsibility.
* Bad, because each task handoff has more explicit bookkeeping.
* Bad, because separate orchestrator commits may be needed when a worker already committed task work.

### Confirmation

Compliance is checked by documentation review and task handoff review after acceptance:

* `.agents/references/execution.md` defines plan-file handoff responsibility.
* `.agents/references/planning.md` describes worker and orchestrator plan-file update ownership.
* `.agents/references/releases.md` describes changelog cadence at task handoff.
* Multi-task execution evidence shows plan status and notable changelog decisions before dependent work starts.

## Pros and Cons of the Options

### Require explicit plan and changelog handoffs at each worker boundary

* Good, because ownership is clear at the point where drift can happen.
* Good, because worker task knowledge and orchestrator continuity are both used.
* Good, because changelog grouping decisions are recorded when entries are deferred.
* Bad, because handoffs require more careful transcript or log evidence.

### Let only the orchestrator update plan files and changelog

* Good, because one actor owns all durable execution notes.
* Bad, because workers may finish tasks without updating the task state they know best.
* Bad, because the orchestrator may need to reconstruct implementation details after the fact.

### Let each worker independently update both plan files and changelog

* Good, because each task commit can include all related documentation.
* Bad, because changelog wording and grouping can become inconsistent across workers.
* Bad, because it conflicts with ADR 0030's orchestrator ownership of final changelog decisions.

### Defer plan and changelog updates until release preparation

* Good, because implementation commits stay smaller.
* Bad, because plan state can become stale during execution.
* Bad, because notable changes are easier to miss after task context has expired.

## More Information

- Source proposal findings: `docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` S004 and S005.
- Related decisions: ADR 0023, ADR 0024, ADR 0026, ADR 0030, and the proposed multi-agent commit attribution ADR.
