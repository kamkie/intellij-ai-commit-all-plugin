---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T11:21:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Add Multi-Agent Commit Attribution Trailers

## Context and Problem Statement

ADR 0007 and `.gitmessage` define AI-created commit metadata, and ADR 0026 permits orchestrated task-worker execution for approved multi-task plans. The current commit footer block does not record worker identity, orchestrator identity, or agent mode, so multi-agent commits cannot be audited from Git history alone.

`docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` finding S002 proposes explicit trailers for worker, orchestrator, and agent mode attribution.

## Decision Drivers

* Make multi-agent commit provenance auditable from Git history.
* Keep actor metadata out of Conventional Commits subjects.
* Preserve the existing contiguous project metadata footer block.
* Use a fixed agent-mode vocabulary that can be referenced by plans, graphs, and logs.
* Keep direct single-agent prompt commits unchanged unless they are part of orchestrated multi-agent execution.

## Considered Options

* Add worker, orchestrator, and agent-mode trailers
* Put worker and mode details in commit subjects or bodies
* Rely on chat transcripts and plan prose for attribution

## Decision Outcome

Chosen option: "Add worker, orchestrator, and agent-mode trailers", because commit history should preserve enough structured metadata to audit orchestrated work without relying on chat context.

Extend the AI-created commit metadata block with:

```text
Project-Worker: <worker-id>
Project-Orchestrator: <orchestrator-id>
Project-Agent-Mode: <mode>
```

`Project-Worker:` is required on every commit authored by a task worker. `Project-Orchestrator:` is required on every commit produced under orchestrated multi-agent execution, whether authored by the orchestrator or by a worker. `Project-Agent-Mode:` is required on every orchestrator and worker commit created in multi-agent execution.

Allowed `Project-Agent-Mode:` values are:

* `code`
* `fast-code`
* `setup`
* `advanced-chat`
* `run-verify`
* `niche`
* `chat`

Worker and orchestrator identifiers stay in trailers and must not be added to Conventional Commits subject lines. The new trailers remain contiguous with the existing project metadata footer block.

Implementation updates `.gitmessage` and the `## Commit Rules` section of `.agents/references/execution.md`.

### Consequences

* Good, because worker and orchestrator identity remain visible after chat context is gone.
* Good, because plans, logs, and execution graphs can share one agent-mode vocabulary.
* Good, because commit subjects stay focused on change intent.
* Bad, because multi-agent commits require more metadata and stricter review.

### Confirmation

Compliance is checked by documentation review and commit-message review after acceptance:

* `.gitmessage` lists the three new trailers.
* `.agents/references/execution.md` defines when each trailer is required.
* Multi-agent commits use only the allowed agent-mode values.
* Project metadata trailers remain contiguous.

## Pros and Cons of the Options

### Add worker, orchestrator, and agent-mode trailers

* Good, because attribution is structured and searchable.
* Good, because the fields can be validated manually and later by tooling.
* Good, because the mode vocabulary can be reused by execution graphs and logs.
* Bad, because commit authors must provide more exact metadata.

### Put worker and mode details in commit subjects or bodies

* Good, because no template footer changes are needed.
* Bad, because subjects become noisy and inconsistent.
* Bad, because free-form body text is harder to audit.

### Rely on chat transcripts and plan prose for attribution

* Good, because commit metadata stays smaller.
* Bad, because Git history no longer carries enough provenance for multi-agent execution.
* Bad, because chat context may be unavailable during later review.

## More Information

- Source proposal finding: `docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` S002.
- Related decisions: ADR 0007 and ADR 0026.
