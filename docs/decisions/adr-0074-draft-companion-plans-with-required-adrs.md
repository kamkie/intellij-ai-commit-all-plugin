---
status: accepted
date: 2026-05-23
accepted_at: 2026-05-23T17:28:32+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Draft Companion Plans With Required ADRs

## Context and Problem Statement

The current workflow requires an agent to create a required ADR first and stop before creating any follow-up implementation plan. That preserves decision review, but it creates an extra round trip when the initial request already makes it clear that the eventual accepted decision will also need a plan before implementation.

The user asked that when requested work requires an ADR and later a plan, the agent should create them in one go.

## Decision Drivers

* Preserve the ADR acceptance gate and plan approval gate.
* Reduce avoidable handoff latency when the need for both artifacts is clear at the start.
* Keep unaccepted ADR decisions from being treated as implementation authorization.
* Keep draft plans visibly blocked on the proposed ADR until the ADR is accepted.
* Avoid drafting speculative plans when the required decision could materially change the implementation shape.

## Considered Options

* Draft the required ADR and companion plan together when both are clearly required.
* Keep the strict ADR-first stop before any plan draft.
* Allow a companion plan only after ADR acceptance.
* Treat the companion plan as approved when the ADR is accepted.

## Decision Outcome

Chosen option: "Draft the required ADR and companion plan together when both are clearly required", because it keeps the existing gates while avoiding a predictable second artifact-drafting turn.

If accepted, update repository guidance so that:

1. When a request clearly requires both an ADR and an implementation plan, the agent may draft the proposed ADR and a companion draft plan in the same work step.
2. The ADR must remain `status: proposed` until explicit user acceptance.
3. The companion plan must remain `Status: Draft` and its readiness section must state that implementation is blocked on ADR acceptance and later explicit plan approval.
4. The agent must stop after drafting both artifacts. Implementation, governed guidance edits, backlog changes, validation-rule changes, and related behavior changes remain blocked.
5. The user may later accept the ADR and approve the plan in one explicit command, but both decisions must be recorded separately in their owning artifacts.
6. If the ADR outcome changes during review, the companion plan must be updated before implementation starts.
7. If the need for a plan is uncertain, or if plan contents depend on the chosen ADR option, draft only the ADR and stop.
8. If the user explicitly requests ADR-only drafting, do not create a companion plan.

This decision does not change the requirement that accepted ADRs update the ADR index and implementation tracker, that approved plans record `Approved by:` and `Approved at:`, or that implementation starts only after required acceptance and approval are recorded.

### Consequences

* Good, because predictable ADR-plus-plan work can be prepared in one review package.
* Good, because acceptance and approval remain separate explicit gates.
* Good, because companion plans make expected implementation and validation visible earlier.
* Bad, because agents must judge whether the plan is clear enough before the ADR is accepted.
* Bad, because rejected or materially changed ADRs may leave draft companion plans that need cleanup.

### Confirmation

After acceptance, confirm implementation by checking that:

* `AGENTS.md` allows drafting a required ADR and companion draft plan together when both are clearly required.
* `docs/decisions/README.md` documents the companion-plan exception while preserving the ADR acceptance gate.
* `.agents/references/planning.md` and `.agents/references/execution.md` keep implementation blocked until ADR acceptance and plan approval are recorded.
* `.agents/plans/README.md` makes companion plans draft and blocked until explicit approval.
* Documentation validation passes.

## Pros and Cons of the Options

### Draft the required ADR and companion plan together when both are clearly required

This option changes artifact drafting order without changing implementation gates.

* Good, because it reduces unnecessary review cycles.
* Good, because it gives maintainers both the decision and expected implementation shape at the same time.
* Good, because the companion plan can record assumptions and validation while the decision is still under review.
* Bad, because a proposed ADR with multiple viable outcomes can make a companion plan premature.

### Keep the strict ADR-first stop before any plan draft

This option preserves the current conservative workflow unchanged.

* Good, because agents never spend time drafting a plan for an unaccepted decision.
* Good, because there is no risk of mistaking a draft plan for implementation approval.
* Bad, because it creates an avoidable second drafting turn when the plan need is already obvious.

### Allow a companion plan only after ADR acceptance

This is the current practical result when a plan is known to be required.

* Good, because the plan can be based on an accepted decision.
* Bad, because it still requires a separate plan-drafting turn after acceptance.

### Treat the companion plan as approved when the ADR is accepted

This option collapses the ADR and plan gates.

* Good, because it is the fastest path from decision to implementation.
* Bad, because it conflicts with the repository's explicit plan approval gate.
* Bad, because ADR acceptance does not necessarily mean the maintainer accepts the implementation sequence, validation scope, worker model, or task boundaries.

## More Information

Related decisions:

* `adr-0041` gates ADR and plan implementation.
* `adr-0042` records plan approval identity.
* `adr-0073` separates direct execution, planned execution, and orchestration guidance.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date, then update the affected workflow guidance.
