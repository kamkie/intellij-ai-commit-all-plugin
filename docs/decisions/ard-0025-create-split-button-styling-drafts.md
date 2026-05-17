---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Create Split-Button Styling Drafts

## Context and Problem Statement

`Q-UX-5` asked what detailed styling the split button should use beyond default IntelliJ Platform styling.

The split-button structure is already decided in ADR 0006: primary `AI Commit All` segment and secondary `& Push` segment in the Commit tool window.

Icon direction is already decided in ADR 0015: use AI-generated base concepts adapted into IntelliJ-style SVG assets.

The user answered `Q-UX-5`: make a series of drafts.

Current state: ADR 0053 selects `docs/concepts/graphics/split-button-drafts/01-blue-steel-compact-snake-violet-ai.svg` as the final styling reference for the ADR 0052 three-section control.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Create Split-Button Styling Drafts
* Use default IntelliJ Platform split-button styling only.
* Choose one detailed style immediately without drafts.
* Let implementation decide styling opportunistically.

## Decision Outcome

Chosen option: "Adopt Create Split-Button Styling Drafts", because Detailed split-button styling should be chosen through a series of draft styles before final implementation.

Detailed split-button styling should be chosen through a series of draft styles before final implementation.

The draft series should cover at least:

- Normal enabled state.
- Running or AI-generation-in-progress state.
- Disabled state when there are no non-ignored committable files.
- Commit-only flow.
- Commit-and-push flow.
- Light and dark theme rendering.

The draft series must stay compatible with IntelliJ Platform UI conventions and the accepted split-button structure from ADR 0006.

Final implementation should not invent a detailed style independently while coding the button. It should use the selected draft or stop and update `docs/decisions/OPEN_QUESTIONS.md` if no draft has been selected.

### Consequences

- `Q-UX-5` is closed as a decision about the styling selection process.
- Final detailed styling remains blocked until the draft series is prepared and one draft is selected.
- Progress or activity animation work should depend on the selected final draft because the running state is part of the styling decision.
- The implementation backlog needs a task to prepare draft styles and a remaining open question for final draft selection.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Create Split-Button Styling Drafts

* Good, because Detailed split-button styling should be chosen through a series of draft styles before final implementation.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use default IntelliJ Platform split-button styling only.

* Bad, because the user asked to make a series of drafts for the detailed styling question.

### Choose one detailed style immediately without drafts.

* Bad, because the requested next step is to prepare draft options before final selection.

### Let implementation decide styling opportunistically.

* Bad, because visual styling should be reviewed as a concrete draft before it becomes implementation behavior.

## More Information

- Remove `Q-UX-5` from `docs/decisions/OPEN_QUESTIONS.md`.
- Add a new open question for selecting the final split-button styling draft.
- Update `TASKS.md` with draft preparation, final selection, and progress-state dependencies.
- Update `README.md` with the accepted draft-selection approach.
