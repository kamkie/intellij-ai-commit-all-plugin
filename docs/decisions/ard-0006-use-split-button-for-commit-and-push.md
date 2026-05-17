---
status: superseded by ard-0052
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Split Button For Commit And Push

## Context and Problem Statement

The commit and push presentation was previously unresolved. Options included one action, separate commit and commit-and-push actions, or reuse of the IDE's existing Commit / Commit and Push choice.

The user decided the control should be a split button with `AI Commit All` and `& Push` segments. Detailed visual styling was deferred at the time of this decision.

Current state: ADR 0052 supersedes this two-segment control structure with a three-section `<icon> AI | Commit | Push` control. ADR 0053 supersedes the later placeholder styling reference from ADR 0027.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Split Button For Commit And Push
* One `AI Commit All` action that reuses an existing IDE push-selected state.
* Separate `AI Commit All` and `AI Commit All & Push` buttons.

## Decision Outcome

Chosen option: "Adopt Use Split Button For Commit And Push", because Use a split button in the Commit tool window for the plugin's commit controls.

Use a split button in the Commit tool window for the plugin's commit controls.

- Primary segment: `AI Commit All`.
- Secondary push segment: `& Push`.

The primary segment starts the AI-generated commit flow and commits all non-ignored committable files.

The `& Push` segment starts the same AI-generated commit flow and then pushes after a successful commit.

Use IntelliJ Platform default split-button behavior for the control structure. Detailed icon and styling choices were decided later by ADR 0015 and ADR 0027.

Historical implementation guidance followed ADR 0015 for final icon assets and ADR 0027 for split-button styling adaptation. New implementation work should follow ADR 0052 and ADR 0053.

Do not implement separate full-width `AI Commit All` and `AI Commit All & Push` buttons unless a later ADR supersedes this decision.

### Consequences

- The commit and push choice is visible in one compact control.
- The push path shares the same `all files` scope and AI message-generation behavior as the primary commit path.
- Implementation should model the two outcomes as distinct executor paths behind one split-button UI.
- Styling can be adjusted later without reopening the control structure decision.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Split Button For Commit And Push

* Good, because Use a split button in the Commit tool window for the plugin's commit controls.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### One `AI Commit All` action that reuses an existing IDE push-selected state.

* Bad, because the split-button design makes the push choice explicit in the plugin control.

### Separate `AI Commit All` and `AI Commit All & Push` buttons.

* Bad, because the user chose a split button instead of separate buttons.

## More Information

- Remove the open question for commit/push presentation.
- Update `TASKS.md` to implement the split button instead of separate actions.
- Historical note: icon direction was later decided by ADR 0015, split-button styling was later decided by ADR 0027, and the control structure was superseded by ADR 0052.
