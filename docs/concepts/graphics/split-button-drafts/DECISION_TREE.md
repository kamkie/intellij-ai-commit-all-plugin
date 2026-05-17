# Split Button Decision Tree

Use this decision tree after reviewing the draft series.

Design-session premise: ADR 0052 is treated as accepted for this draft set. Apply this tree to the three-section cumulative `<icon> AI | Commit | Push` control.

## 1. Does the draft preserve the accepted structure?

- Yes: continue.
- No: reject unless a new ADR supersedes ADR 0052.

Required structure:

- Left section: AI icon plus `AI`.
- Middle section: `Commit`.
- Right section: `Push` plus a compact push icon.
- One compact cumulative control in the Commit tool window.
- IntelliJ-style rounded outer corners with no rounding at the internal segment join.
- Full-height dividers between `AI`, `Commit`, and `Push`, with compact internal margins.
- No arrow graphic in either internal join.

## 2. Is the segment distinction strong enough?

- If the three sections blur together, prefer a draft with stronger hue, value, or divider separation.
- If `Push` looks like a separate full primary action, reduce section fill and rebalance it as the final cumulative step.
- If `AI` dominates the whole control, reduce the brand signal so `Commit` and `Push` remain legible.
- If the right section dominates the workflow, reject or rebalance.

## 3. Does the draft work in both themes?

- If only the light variant works, revise before selection.
- If only the dark variant works, revise before selection.
- If disabled state relies only on color, revise before selection.

## 4. Does the running state communicate activity without layout shift?

- Prefer a subtle pulse, progress stripe, or status tint that preserves button dimensions.
- Reject running treatments that resize labels, move the divider, or obscure text.

## 5. Are all hover targets clear?

- Prefer cumulative hover examples: `AI` highlights only `AI`, `Commit` highlights `AI` plus `Commit`, and `Push` highlights all three sections.
- Reject hover treatments that make any section look detached or change the button width.

## 6. Does the draft fit IntelliJ toolbar density?

- Prefer IntelliJ run-widget-like geometry, rounded outer corners, tight icon/text padding, crisp borders, simple icons, and equal visual density across the three sections.
- Reject independently rounded section geometry, oversized hero-style controls, decorative gradients, card-like frames, or sections that read as separate full buttons.

## 7. Final Selection

When a draft is selected:

1. Record the choice in a follow-up ADR.
2. State whether the new ADR supersedes ADR 0027 or only refines it.
3. Link the selected draft file.
4. List required production asset or UI implementation changes.
5. Define light, dark, running, disabled, AI-only, AI-and-commit, and AI-commit-and-push validation.
