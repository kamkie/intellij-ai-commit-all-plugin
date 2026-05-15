---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Record Plan Approval Identity

## Context and Problem Statement

Implementation plans use `Status: Approved` to show that a user has reviewed the plan and allowed implementation to start.

The `## Readiness` section currently makes plan readiness, open questions, and implementation progress visible, but it does not identify who approved the plan.

The maintainer requested an `Approved by: <user>` entry for approved plans and asked that approval attribution use the configured Git identity when the configured repository user is the approver, matching ADR decision-maker metadata, unless another approver name is explicitly supplied.

## Decision Drivers

* Keep plan approval attributable to a concrete person.
* Avoid generic or ambiguous approval labels such as `user`, `maintainer`, or `none known`.
* Keep plan approval metadata consistent with ADR decision-maker identity rules.
* Preserve the existing explicit approval gate before implementation starts.

## Considered Options

* Record plan approval identity in readiness metadata
* Keep approval attribution only in conversation history
* Add approval identity only to plan front matter

## Decision Outcome

Chosen option: "Record plan approval identity in readiness metadata", because approval identity is part of implementation readiness after a plan has actually been approved and should be visible near the plan status, open questions, and implementation progress.

If accepted, approved plans and plans that have moved beyond approval must include an `Approved by:` line in the `## Readiness` section.

When a plan is approved by the configured repository user, `Approved by:` must use the configured Git identity in `Name <email>` form, resolved from `git config user.name` and `git config user.email` when approval is recorded.

When the current user request explicitly names another approver, `Approved by:` may use that explicit name instead.

Plans that are not approved yet must not claim approval. They may omit `Approved by:` or leave it empty.

### Consequences

* Good, because plan approval is visible and attributable without reading conversation history.
* Good, because approval metadata follows the same Git identity rule as ADR decision-maker metadata.
* Bad, because plan templates, existing plans, and validation need another conditional readiness field.

### Confirmation

Compliance will be checked through documentation review and `scripts/validate-docs.ps1` when plan files, planning guidance, or validation rules change.

## Pros and Cons of the Options

### Record plan approval identity in readiness metadata

* Good, because the approver appears next to readiness, questions, and progress.
* Good, because existing plan review and implementation gates can validate the field before work starts.
* Bad, because approved plan readiness sections need one more maintained line.

### Keep approval attribution only in conversation history

* Good, because it avoids changing plan file format.
* Bad, because approval context can be lost or hard to audit later.
* Bad, because agents would need to infer approval identity from chat history instead of reading the plan.

### Add approval identity only to plan front matter

* Good, because it keeps approval metadata machine-readable.
* Bad, because the repository already uses `## Readiness` for plan readiness signals.
* Bad, because it separates approval identity from the approval-dependent readiness summary.

## More Information

- If accepted, update `.agents/plans/README.md`, `.agents/plans/PLAN_TEMPLATE.md`, `.agents/references/planning.md`, existing plan files, and `scripts/validate-docs.ps1`.
- Use the wording `Open questions: None.` rather than `Open questions: None known.` when no questions are open.
