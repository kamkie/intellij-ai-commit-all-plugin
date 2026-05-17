# Split Button Decision Tree

Use this decision tree after reviewing the draft series.

## 1. Does the draft preserve the accepted structure?

- Yes: continue.
- No: reject unless a new ADR supersedes ADR 0006.

Required structure:

- Left segment: `AI Commit All`.
- Right segment: `& Push`.
- One compact split-button control in the Commit tool window.
- No arrow graphic in the center join.

## 2. Is the segment distinction strong enough?

- If the two segments blur together, prefer a draft with stronger hue or value separation.
- If the push segment looks like a separate full primary action, reduce saturation or contrast.
- If the right segment dominates the left, reject or rebalance.

## 3. Does the draft work in both themes?

- If only the light variant works, revise before selection.
- If only the dark variant works, revise before selection.
- If disabled state relies only on color, revise before selection.

## 4. Does the running state communicate activity without layout shift?

- Prefer a subtle pulse, progress stripe, or status tint that preserves button dimensions.
- Reject running treatments that resize labels, move the divider, or obscure text.

## 5. Does the draft fit IntelliJ toolbar density?

- Prefer 6px or smaller radii, compact padding, crisp borders, and simple icons.
- Reject oversized hero-style controls, decorative gradients, or card-like frames.

## 6. Final Selection

When a draft is selected:

1. Record the choice in a follow-up ADR.
2. State whether the new ADR supersedes ADR 0027 or only refines it.
3. Link the selected draft file.
4. List required production asset or UI implementation changes.
5. Define light, dark, running, disabled, commit-only, and commit-and-push validation.
