---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T11:21:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Define Orchestrator Synchronization And Chat Logging

## Context and Problem Statement

ADR 0026 allows parallel workers only when an approved plan marks tasks independent and assigns disjoint write scopes. The current guidance does not define the synchronization point for a parallel worker wave, and it does not require structured worker start, stop, failure, or active-count logging.

`docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` finding S003 proposes explicit synchronization and logging rules.

## Decision Drivers

* Preserve ADR 0026's narrow parallel execution exception.
* Make worker-wave completion unambiguous.
* Require enough structured logging to reconstruct active worker state during orchestrated execution.
* Avoid adding durable run-log files until the repository has a concrete retention and cleanup need.
* Keep the first logging rule implementable in existing chat-based execution.

## Considered Options

* Require synchronization and structured chat logging
* Require synchronization and durable `.agents/runs/` logs
* Keep synchronization and logging implicit

## Decision Outcome

Chosen option: "Require synchronization and structured chat logging", because it makes orchestrated execution auditable in the existing workflow without adding a durable run-log directory before retention and cleanup needs are proven.

The orchestrator must wait for every worker in the current execution step to report success or failure before moving to the next step. The orchestrator must verify each worker's committed result or commit-ready diff before advancing. Parallel workers remain allowed only for independent tasks with disjoint write scopes under ADR 0026.

The orchestrator must log a structured chat-transcript event for each worker `start`, `stop`, and `fail`. The orchestrator must also log whenever the active worker count changes.

Each log entry includes:

* ISO 8601 timestamp.
* Event type.
* Worker id.
* Plan id.
* Plan task id.
* Agent mode.
* Active worker count.
* Active worker ids.

This decision intentionally chooses the chat transcript as the log destination. It does not create `.agents/runs/`, does not define retention for durable run logs, and does not commit run logs. A later ADR may introduce durable logs if a concrete approved plan needs them.

Implementation updates `.agents/references/execution.md` with the synchronization and chat logging requirements.

### Consequences

* Good, because each parallel wave has an explicit wait and verification boundary.
* Good, because active worker count changes are visible during execution.
* Good, because no committed run-log lifecycle is needed yet.
* Bad, because chat transcript logs are less durable than repository files.
* Bad, because durable log rules would still need a future ADR if the repository needs committed run records.

### Confirmation

Compliance is checked by documentation review and orchestrated-work handoff review after acceptance:

* `.agents/references/execution.md` defines the synchronization boundary.
* `.agents/references/execution.md` lists required structured chat log fields.
* Parallel execution handoffs show all workers in a wave reaching `stop` or `fail` before the next step starts.

## Pros and Cons of the Options

### Require synchronization and structured chat logging

* Good, because it fits current agent execution without adding new repository directories.
* Good, because the orchestrator's wait and verify steps become explicit.
* Good, because later durable logging can reuse the same fields.
* Bad, because chat transcript retention is outside the repository.

### Require synchronization and durable `.agents/runs/` logs

* Good, because run logs would be preserved in a repository-owned location.
* Bad, because the repository would need ownership, retention, cleanup, and commit rules for generated run files.
* Bad, because durable log files add workflow overhead before there is a proven need.

### Keep synchronization and logging implicit

* Good, because no guidance changes are needed.
* Bad, because parallel worker waves remain hard to audit.
* Bad, because active worker count and failure timing can be lost.

## More Information

- Source proposal finding: `docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` S003.
- Related decisions: ADR 0023, ADR 0024, ADR 0026, and the proposed multi-agent commit attribution ADR.
