---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Gate ADR And Plan Implementation

## Context and Problem Statement

Repository rule changes, compatibility decisions, workflow changes, and other durable project decisions require ADR coverage.

Implementation plans already carry statuses, but the workflow guidance can still be interpreted as allowing implementation to start while a plan is only drafted or under review.

The maintainer requested two explicit gates: when a requested change requires an ADR, create the ADR first and stop until user review and explicit acceptance; and when work needs a plan, review and approve the plan before implementation starts.

## Decision Drivers

* Keep durable decisions reviewable before implementation changes depend on them.
* Prevent implementation from starting from unaccepted ADRs or unapproved plans.
* Make stop conditions explicit for agents and contributors.

## Considered Options

* Require explicit acceptance before continuing from ADRs and plans
* Allow ADRs and implementation changes in the same pass
* Allow implementation from draft plans

## Decision Outcome

Chosen option: "Require explicit acceptance before continuing from ADRs and plans", because it makes review and approval a hard prerequisite before implementation work changes repository behavior or workflow.

When a requested change requires creating an ADR, create the ADR first and stop. Do not update the governed implementation, workflow guidance, backlog, validation rules, or related behavior until the user has reviewed the ADR and explicitly accepted it.

When a requested change needs an implementation plan, create or update the plan first and stop. Implementation may start only after the user reviews and explicitly approves the plan.

### Consequences

* Good, because decision and plan review happens before dependent implementation work.
* Good, because agents have a clear stop condition for ADR-backed and plan-backed work.
* Bad, because some changes require an extra turn before implementation can proceed.

### Confirmation

Compliance is checked through documentation review and by verifying that ADR-backed changes and plan-backed implementation changes are not made before explicit acceptance or approval.

## Pros and Cons of the Options

### Require explicit acceptance before continuing from ADRs and plans

* Good, because it directly implements the requested review gates.
* Good, because it reduces accidental implementation from unreviewed decisions or plans.
* Bad, because it adds process latency for changes that are otherwise straightforward.

### Allow ADRs and implementation changes in the same pass

* Good, because it is faster for small rule changes.
* Bad, because it can land implementation before the durable decision has been reviewed.

### Allow implementation from draft plans

* Good, because exploratory work can start sooner.
* Bad, because implementation can diverge from the final reviewed plan.

## More Information

- Update `AGENTS.md`, `.agents/references/planning.md`, `.agents/references/execution.md`, and `docs/decisions/README.md`.
- Update validation or review guidance where practical to make the gate visible during handoff.
