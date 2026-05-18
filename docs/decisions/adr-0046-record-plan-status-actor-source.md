---
status: accepted
date: 2026-05-15
accepted_at: 2026-05-15T13:19:38+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Record Plan Status Actor Source

## Context and Problem Statement

Plans record status transitions as timestamped `## Status History` entries with an actor in `Name <email>` form. Current guidance requires approval identity to use the configured Git identity, but it does not distinguish direct human commands from autonomous or delegated agent work when recording later status transitions.

This can make autonomous implementation entries look human-authored. For example, an orchestrated implementation start or completion may be recorded as `by Kamil Kiewisz <kamkie@outlook.com>` even though Codex or another agent performed the transition after approval.

The maintainer requested that autonomous Codex or other agent changes use the agent identity, like AI-created commit trailers, while direct human commands such as `accept`, `approve`, or "change plan state but do not start implementation yet" continue to use the configured Git user identity.

## Decision Drivers

* Preserve a useful audit trail for plan status changes.
* Distinguish human approvals and direct human commands from autonomous agent execution.
* Keep plan status actor identity consistent with AI-created commit attribution.
* Avoid attributing agent-performed implementation lifecycle changes to the maintainer.
* Keep the rule simple enough for agents to apply during plan updates.

## Considered Options

* Record plan status actors by action source
* Always use the configured Git identity
* Always use the currently executing agent identity

## Decision Outcome

Chosen option: "Record plan status actors by action source", because the status-history actor should identify who or what caused that specific transition.

If accepted, plan status-history entries use these attribution rules:

* For direct human commands that only record a human decision or requested state change, use the configured Git identity in `Name <email>` form unless the current request explicitly supplies another human identity.
* For autonomous, orchestrated, or delegated implementation work performed by Codex or another agent, use the responsible agent identity in `<agent-name> <agent-email>` form, matching the identity style used in AI-created commit trailers.
* When a human approves or accepts a plan and an agent later starts or completes implementation, record the approval transition with the human identity and the implementation transitions with the agent identity.
* Do not reuse the plan approver identity for later implementation status changes unless the later status change is itself a direct human command rather than agent-performed work.

After acceptance, update plan guidance, execution guidance if needed, plan templates, validation where practical, and any active plan status-history entries that are known to have attributed autonomous agent work to the maintainer.

### Consequences

* Good, because plan history will show whether a status change came from a human decision or an agent action.
* Good, because autonomous implementation entries will align with AI-created commit attribution.
* Good, because the configured Git identity remains the source for direct human approval and acceptance commands.
* Bad, because agents must decide whether a status change is a direct human command or an autonomous agent action.
* Bad, because existing active plan histories may need targeted correction after this rule is accepted.

### Confirmation

Compliance will be checked through documentation review of the plan and execution guidance after acceptance.

Where validation can check deterministic structure, `scripts/validate-docs.ps1` should continue to require actor presence and valid identity shape. Human versus agent source classification may require review because it depends on the request context.

## Pros and Cons of the Options

### Record plan status actors by action source

* Good, because it records human approvals and agent execution as different events when they are different events.
* Good, because it supports direct commands that intentionally update only plan state.
* Good, because it avoids implying that the maintainer performed autonomous implementation work.
* Bad, because it depends on agents preserving enough context to classify the transition.

### Always use the configured Git identity

* Good, because it is simple and already available.
* Bad, because it attributes autonomous agent work to the maintainer.
* Bad, because it conflicts with AI-created commit attribution.

### Always use the currently executing agent identity

* Good, because autonomous edits are clearly agent-authored.
* Bad, because direct human approvals and acceptance commands would lose the human decision-maker attribution.
* Bad, because it would conflict with existing plan approval identity rules.

## More Information

- Related plan approval identity decision: ADR 0042.
- Related plan status timestamp decision: ADR 0045.
- Related commit attribution guidance: `.gitmessage`.
- Related plan guidance owner: `.agents/references/planning.md`.
