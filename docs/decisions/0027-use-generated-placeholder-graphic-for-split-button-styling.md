# Use Generated Placeholder Graphic For Split-Button Styling

Status: Accepted

Date: 2026-05-15

## Context

ADR 0006 chooses the split-button structure: primary `AI Commit All` segment and secondary `& Push` segment.

ADR 0015 chooses AI-generated base concepts adapted into IntelliJ-style SVG assets for icons.

ADR 0025 chooses a draft-based process for detailed split-button styling and leaves final draft selection in `Q-UX-6`.

The user answered `Q-UX-6`: generate placeholder graphic now.

## Decision

Use a generated placeholder graphic as the selected split-button styling reference for now.

The placeholder graphic is stored at `docs/concepts/graphics/split-button-placeholder.png`.

The placeholder should cover:

- Normal enabled state.
- Running or AI-generation-in-progress state.
- Disabled state when there are no non-ignored committable files.
- Commit-only flow.
- Commit-and-push flow.
- Light and dark theme considerations.

The generated graphic is a visual placeholder, not a final committed implementation asset. Implementation must adapt the selected direction into IntelliJ Platform UI conventions and final SVG or UI resources where needed.

If implementation reveals that the generated placeholder cannot be represented cleanly in the IntelliJ Commit tool window, stop and record the concrete styling question before proceeding.

## Consequences

- `Q-UX-6` is closed.
- Split-button progress or activity animation is unblocked.
- The placeholder gives implementation a visual direction without treating raw generated bitmap output as final plugin UI.
- Final assets still need platform-quality adaptation and light/dark theme validation.

## Alternatives Considered

- Wait for a polished multi-draft review before selecting a style.
    - Why it was not chosen: the user requested generating the placeholder graphic now.
- Commit raw generated bitmap output as the final UI asset.
    - Why it was not chosen: IntelliJ Platform UI should use native controls and platform-quality SVG or UI resources where assets are needed.
- Use only default IntelliJ Platform split-button styling.
    - Why it was not chosen: the user requested a generated placeholder graphic for the detailed styling direction.

## Follow-Up

- Remove `Q-UX-6` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` so progress/activity styling no longer depends on an open question.
- Store the generated placeholder graphic under `docs/concepts/graphics/` for immediate visual reference.
