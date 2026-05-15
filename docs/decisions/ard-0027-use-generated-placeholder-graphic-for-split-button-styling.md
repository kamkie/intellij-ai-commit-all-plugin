---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Generated Placeholder Graphic For Split-Button Styling

## Context and Problem Statement

ADR 0006 chooses the split-button structure: primary `AI Commit All` segment and secondary `& Push` segment.

ADR 0015 chooses AI-generated base concepts adapted into IntelliJ-style SVG assets for icons.

ADR 0025 chooses a draft-based process for detailed split-button styling and leaves final draft selection in `Q-UX-6`.

The user answered `Q-UX-6`: generate placeholder graphic now.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Generated Placeholder Graphic For Split-Button Styling
* Wait for a polished multi-draft review before selecting a style.
* Commit raw generated bitmap output as the final UI asset.
* Use only default IntelliJ Platform split-button styling.

## Decision Outcome

Chosen option: "Adopt Use Generated Placeholder Graphic For Split-Button Styling", because Use a generated placeholder graphic as the selected split-button styling reference for now.

Use a generated placeholder graphic as the selected split-button styling reference for now.

The placeholder graphic is stored at `docs/concepts/graphics/split-button-placeholder.png`.

This is the current detailed styling owner for the split-button direction that ADR 0006 intentionally deferred.

The placeholder should cover:

- Normal enabled state.
- Running or AI-generation-in-progress state.
- Disabled state when there are no non-ignored committable files.
- Commit-only flow.
- Commit-and-push flow.
- Light and dark theme considerations.

The generated graphic is a visual placeholder, not a final committed implementation asset. Implementation must adapt the selected direction into IntelliJ Platform UI conventions and final SVG or UI resources where needed.

If implementation reveals that the generated placeholder cannot be represented cleanly in the IntelliJ Commit tool window, stop and record the concrete styling question before proceeding.

### Consequences

- `Q-UX-6` is closed.
- Split-button progress or activity animation is unblocked.
- The placeholder gives implementation a visual direction without treating raw generated bitmap output as final plugin UI.
- Final assets still need platform-quality adaptation and light/dark theme validation.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Generated Placeholder Graphic For Split-Button Styling

* Good, because Use a generated placeholder graphic as the selected split-button styling reference for now.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Wait for a polished multi-draft review before selecting a style.

* Bad, because the user requested generating the placeholder graphic now.

### Commit raw generated bitmap output as the final UI asset.

* Bad, because IntelliJ Platform UI should use native controls and platform-quality SVG or UI resources where assets are needed.

### Use only default IntelliJ Platform split-button styling.

* Bad, because the user requested a generated placeholder graphic for the detailed styling direction.

## More Information

- Remove `Q-UX-6` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` so progress/activity styling no longer depends on an open question.
- Store the generated placeholder graphic under `docs/concepts/graphics/` for immediate visual reference.
