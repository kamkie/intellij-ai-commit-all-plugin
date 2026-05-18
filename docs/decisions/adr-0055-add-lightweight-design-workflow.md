---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T01:20:58+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Add Lightweight Design Workflow

## Context and Problem Statement

`docs/WORKING_WITH_AI.md` currently names task types for planning, implementation, review, documentation, proposal, and release work. Those task types are useful once work is being triaged, planned, executed, or reviewed, but they do not give maintainers a lightweight way to ask an AI agent to shape new product, UX, API, architecture, or repository ideas before deciding whether the idea should become a proposal, ADR, plan, task, or implementation request.

The existing proposal and planning workflows are intentionally durable and gated. They are too heavy for early design exploration where the expected output is a short set of options, tradeoffs, open questions, and a recommended next artifact.

## Decision Drivers

* Make early design exploration explicit without forcing every idea into a proposal or plan.
* Keep repository decision, workflow, and implementation gates intact.
* Preserve small task context for AI agents.
* Give maintainers a clearer request shape for new work that is not ready for implementation.
* Avoid creating another durable artifact type unless persistence is explicitly needed.

## Considered Options

* Add a lightweight Design task type without a default persisted artifact
* Use the existing Proposal workflow for design exploration
* Use the existing Planning workflow for design exploration
* Add a new persisted design-note directory

## Decision Outcome

Chosen option: "Add a lightweight Design task type without a default persisted artifact", because it gives maintainers a low-ceremony way to shape new work while preserving the existing ADR, proposal, plan, and implementation gates.

If accepted, update `docs/WORKING_WITH_AI.md` to add a `Design` task type for early exploration of new product behavior, UX, APIs, architecture, repository structure, or workflow ideas. A design request should usually ask the AI agent for:

* The design goal and non-goals.
* Relevant constraints and existing decisions.
* One to three viable approaches.
* Tradeoffs, risks, and unanswered questions.
* A recommendation when the user asks for one or the tradeoff is clear.
* The likely next artifact: no artifact, proposal, ADR, implementation plan, task update, documentation change, or implementation request.

The Design workflow must not replace existing gates:

* If the design chooses or changes durable project direction, repository rules, compatibility policy, user behavior, validation expectations, or future maintenance policy, create or update an ADR and stop for explicit acceptance.
* If the design leads to multi-file implementation, behavior changes, or unresolved technical choices, create or update an implementation plan and stop for explicit approval.
* If the user wants a durable triage artifact for findings, duplication, simplification, or improvement options without immediate implementation, use `docs/proposals/`.
* If the user asks for direct implementation and the work is small enough not to need a plan or ADR, proceed under the existing implementation workflow.

### Consequences

* Good, because maintainers can ask AI to explore new ideas without prematurely creating a plan or proposal.
* Good, because the output stays compact and can remain in chat when no durable record is needed.
* Good, because the next step is explicit instead of implied.
* Bad, because agents must still recognize when a design discussion crosses into ADR or plan territory.
* Bad, because chat-only design output can be lost unless the user asks to persist it as a proposal, plan, decision, task, or documentation update.

### Confirmation

Compliance should be checked by documentation review after acceptance:

* `docs/WORKING_WITH_AI.md` includes the new Design task type.
* The Design guidance says it is for early shaping before proposal, ADR, plan, or implementation.
* The Design guidance explicitly preserves ADR and plan gates.
* `scripts/validate-docs.ps1` passes when the guidance update lands.

## Pros and Cons of the Options

### Add a lightweight Design task type without a default persisted artifact

* Good, because it fills the gap between brainstorming and formal planning.
* Good, because it keeps lightweight work lightweight.
* Good, because durable follow-up artifacts remain available when needed.
* Bad, because the boundary between design and decision can require judgment.

### Use the existing Proposal workflow for design exploration

* Good, because proposals already provide a durable place for improvement options and triage.
* Good, because proposal IDs and implementation tracking already exist.
* Bad, because not every early design exploration needs a persisted proposal.
* Bad, because proposal structure is heavier than a quick design pass.

### Use the existing Planning workflow for design exploration

* Good, because plans already identify files, validation, questions, and rollback considerations.
* Good, because approved plans provide a clear implementation gate.
* Bad, because plans imply implementation readiness.
* Bad, because plan approval overhead is too heavy for early design shaping.

### Add a new persisted design-note directory

* Good, because design notes would be discoverable outside chat.
* Good, because design history could be reviewed later.
* Bad, because a new artifact type adds naming, lifecycle, validation, and archival rules.
* Bad, because the current need is lightweight design, not another durable process.

## More Information

- Implementation landed in `docs/WORKING_WITH_AI.md` after maintainer acceptance.
