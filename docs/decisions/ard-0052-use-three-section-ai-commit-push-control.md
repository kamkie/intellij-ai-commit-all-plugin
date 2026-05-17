---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T23:40:43+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Three-Section AI Commit Push Control

## Context and Problem Statement

ADR 0006 chose a two-segment split button with `AI Commit All` and `& Push`. Recent visual review of `PROP-02-pre-release-ux` `E005` changed the intended control shape toward a compact IntelliJ toolbar control with three cumulative sections:

```text
<icon> AI | Commit | Push
```

This changes both presentation and behavior. The first section should run only the AI preparation work, the second should additionally commit, and the third should additionally push.

## Decision Drivers

* Keep the primary toolbar control compact and IntelliJ-like.
* Make the workflow ladder explicit: AI preparation, commit, then push.
* Allow users to generate or refresh an AI commit message without immediately committing.
* Preserve the all-files scope from ADR 0003.
* Preserve safe push behavior from ADR 0047 for the push section.
* Make hover states communicate cumulative execution: hovering later sections visually includes earlier prerequisite sections.

## Considered Options

* Use a three-section cumulative control: `<icon> AI | Commit | Push`
* Keep the two-segment `AI Commit All | & Push` split button
* Add separate full buttons for AI, Commit, and Push

## Decision Outcome

Chosen option: "Use a three-section cumulative control: `<icon> AI | Commit | Push`", because it makes each workflow level visible while preserving one compact control.

Replace the two-segment split-button structure from ADR 0006 with one three-section control:

* `AI` section: include every non-ignored committable file in the IDE commit workflow, stage or select files where the IDE workflow requires that, invoke AI message generation, and stop before commit.
* `Commit` section: perform the `AI` section behavior, then commit after successful AI generation and normal IDE commit checks.
* `Push` section: perform the `Commit` section behavior, then push after a successful commit.

The `Push` section label should use the word `Push` with a push icon. The `AI` section should include the AI icon and the text `AI`. The `Commit` section should use the text `Commit`.

Hover and active-state styling should be cumulative:

* Hovering `AI` visually selects only the `AI` section.
* Hovering `Commit` visually selects `AI` and `Commit`.
* Hovering `Push` visually selects `AI`, `Commit`, and `Push`.

This ADR supersedes ADR 0006 for commit/push control structure and refines ADR 0010 for the first section: the `AI` section does not auto-commit after generation. The `Commit` and `Push` sections still auto-continue after successful AI generation. ADR 0047 remains the push safety policy for the `Push` section.

### Consequences

* Good, because users can generate the message and prepare all files without being forced into an immediate commit.
* Good, because the cumulative labels make the workflow progression easier to scan than a two-segment split button.
* Good, because `Push` can carry both text and icon instead of relying on a small `& Push` segment.
* Bad, because the accepted action labels, plugin action structure, documentation, tests, and draft graphics need a follow-up implementation plan.
* Bad, because the implementation may need custom presentation logic if the IntelliJ action system cannot express cumulative segmented hover behavior with standard controls.

### Confirmation

Compliance should be checked by implementation review and validation that covers:

* `AI` section includes all eligible files and generates the message without committing.
* `Commit` section performs AI preparation and commits.
* `Push` section performs AI preparation, commits, and uses the accepted push behavior after commit.
* Hover states visually select cumulative sections as specified.
* Light, dark, disabled, running, and unavailable states remain readable.

## Pros and Cons of the Options

### Use a three-section cumulative control: `<icon> AI | Commit | Push`

* Good, because it gives the user an explicit preparation-only action.
* Good, because the control communicates workflow progression directly.
* Good, because cumulative hover can make section dependencies visible without extra explanatory text.
* Bad, because it changes accepted behavior and requires a new implementation plan.
* Bad, because standard IntelliJ split-button APIs may not provide this exact segmented behavior directly.

### Keep the two-segment `AI Commit All | & Push` split button

* Good, because it is already accepted and implemented.
* Good, because it maps cleanly to commit-only and commit-and-push executor paths.
* Bad, because it does not provide an AI-only preparation action.
* Bad, because the current visual iteration is moving away from the two-segment model.

### Add separate full buttons for AI, Commit, and Push

* Good, because each behavior would be independently clickable.
* Bad, because it would take more toolbar space.
* Bad, because it weakens the requested one-control visual model.
* Bad, because it makes cumulative hover and workflow progression harder to express.

## More Information

- Source request: maintainer asked for a three-section button with `<icon> AI | Commit | Push`.
- Related proposal: `docs/proposals/PROP-02-pre-release-ux-2026-05-15T09-57.md` `E005`.
- Runtime implementation is planned by `.agents/plans/PLAN-three-section-ai-commit-push-control.md`.
