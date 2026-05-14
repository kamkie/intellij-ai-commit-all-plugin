# Create Split-Button Styling Drafts

Status: Accepted

Date: 2026-05-15

## Context

`Q-UX-5` asked what detailed styling the split button should use beyond default IntelliJ Platform styling.

The split-button structure is already decided in ADR 0006: primary `AI Commit All` segment and secondary `& Push` segment in the Commit tool window.

Icon direction is already decided in ADR 0015: use AI-generated base concepts adapted into IntelliJ-style SVG assets.

The user answered `Q-UX-5`: make a series of drafts.

## Decision

Detailed split-button styling should be chosen through a series of draft styles before final implementation.

The draft series should cover at least:

- Normal enabled state.
- Running or AI-generation-in-progress state.
- Disabled state when there are no non-ignored committable files.
- Commit-only flow.
- Commit-and-push flow.
- Light and dark theme rendering.

The draft series must stay compatible with IntelliJ Platform UI conventions and the accepted split-button structure from ADR 0006.

Final implementation should not invent a detailed style independently while coding the button. It should use the selected draft or stop and update `OPEN_QUESTIONS.md` if no draft has been selected.

## Consequences

- `Q-UX-5` is closed as a decision about the styling selection process.
- Final detailed styling remains blocked until the draft series is prepared and one draft is selected.
- Progress or activity animation work should depend on the selected final draft because the running state is part of the styling decision.
- The implementation backlog needs a task to prepare draft styles and a remaining open question for final draft selection.

## Alternatives Considered

- Use default IntelliJ Platform split-button styling only.
  - Why it was not chosen: the user asked to make a series of drafts for the detailed styling question.
- Choose one detailed style immediately without drafts.
  - Why it was not chosen: the requested next step is to prepare draft options before final selection.
- Let implementation decide styling opportunistically.
  - Why it was not chosen: visual styling should be reviewed as a concrete draft before it becomes implementation behavior.

## Follow-Up

- Remove `Q-UX-5` from `OPEN_QUESTIONS.md`.
- Add a new open question for selecting the final split-button styling draft.
- Update `TASKS.md` with draft preparation, final selection, and progress-state dependencies.
- Update `README.md` with the accepted draft-selection approach.
