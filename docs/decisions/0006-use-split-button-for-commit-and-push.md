# Use Split Button For Commit And Push

Status: Accepted

Date: 2026-05-14

## Context

The commit and push presentation was previously unresolved. Options included one action, separate commit and commit-and-push actions, or reuse of the IDE's existing Commit / Commit and Push choice.

The user decided the control should be a split button with `AI Commit All` and `& Push` segments. Detailed visual styling was deferred at the time of this decision.

Current state: ADR 0027 later selected the generated placeholder graphic in `docs/concepts/graphics/split-button-placeholder.png` as the current detailed styling reference.

## Decision

Use a split button in the Commit tool window for the plugin's commit controls.

- Primary segment: `AI Commit All`.
- Secondary push segment: `& Push`.

The primary segment starts the AI-generated commit flow and commits all non-ignored committable files.

The `& Push` segment starts the same AI-generated commit flow and then pushes after a successful commit.

Use IntelliJ Platform default split-button behavior for the control structure. Detailed icon and styling choices were decided later by ADR 0015 and ADR 0027.

Current implementation guidance should also follow ADR 0015 for final icon assets and ADR 0027 for split-button styling adaptation.

Do not implement separate full-width `AI Commit All` and `AI Commit All & Push` buttons unless a later ADR supersedes this decision.

## Consequences

- The commit and push choice is visible in one compact control.
- The push path shares the same `all files` scope and AI message-generation behavior as the primary commit path.
- Implementation should model the two outcomes as distinct executor paths behind one split-button UI.
- Styling can be adjusted later without reopening the control structure decision.

## Alternatives Considered

- One `AI Commit All` action that reuses an existing IDE push-selected state.
  - Why it was not chosen: the split-button design makes the push choice explicit in the plugin control.
- Separate `AI Commit All` and `AI Commit All & Push` buttons.
  - Why it was not chosen: the user chose a split button instead of separate buttons.

## Follow-Up

- Remove the open question for commit/push presentation.
- Update `TASKS.md` to implement the split button instead of separate actions.
- Historical note: icon direction was later decided by ADR 0015, and split-button styling was later decided by ADR 0027.
