---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T23:40:43+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Select Violet AI Snake Three-Section Control Style

## Context and Problem Statement

ADR 0027 selected a generated placeholder graphic as the temporary split-button styling reference. The design session for the ADR 0052 three-section control produced compact SVG drafts under `docs/concepts/graphics/split-button-drafts/`.

The project needs a durable final styling decision for the accepted `<icon> AI | Commit | Push` control before the design can be applied to runtime UI assets, user-facing documentation, tests, and validation records.

## Decision Drivers

* Preserve the compact IntelliJ toolbar-control shape from the selected Blue Steel compact direction.
* Make the `AI`, `Commit`, and `Push` sections visually cumulative without arrow joins.
* Use the JetBrains AI Assistant visual language for the `AI` section.
* Keep passive, hover, clicked/running, and disabled states readable in light and dark themes.
* Use an activity indication that identifies the active section without covering labels or relying only on fill color.

## Considered Options

* Select `01-blue-steel-compact-snake-violet-ai.svg`.
* Select `01-blue-steel-compact-snake.svg`.
* Select `01-blue-steel-compact.svg`.
* Select `01-blue-steel-compact-magic-spin.svg`.
* Keep ADR 0027's generated placeholder as the styling owner.

## Decision Outcome

Chosen option: "Select `01-blue-steel-compact-snake-violet-ai.svg`", because it keeps the chosen compact snake-loop interaction while aligning the active `AI` section with the JetBrains AI Assistant commit-message action color.

`docs/concepts/graphics/split-button-drafts/01-blue-steel-compact-snake-violet-ai.svg` is the final styling reference for the three-section control.

The selected styling has these required traits:

* One compact segmented control with the visible structure `<AI icon> AI | Commit | Push`.
* Passive state keeps each section visually passive while preserving its color family: muted violet background/text/icon for `AI`, blue steel for `Commit`, and green for `Push`.
* Hover changes section backgrounds only; there is no extra hover filter or translucent overlay.
* Hover is cumulative: `AI` hover affects only `AI`, `Commit` hover affects `AI` and `Commit`, and `Push` hover affects all three sections.
* Clicked/running states use the snake-loop border segment around the active phase section.
* The `AI` active-section background uses the JetBrains AI Assistant commit-message action violet: `#834DF0` in light theme and `#A571E6` in dark theme. Passive `AI` background/text/icon use muted violet values from the same color family.
* The `Commit` section stays in the blue steel family and the `Push` section stays green.
* The draft covers passive, `Staging + AI` hover, `Commit` hover, `Push` hover, `Staging + AI` clicked/running, `Commit` clicked/running, `Push` clicked/running, and disabled states in both light and dark themes.

This ADR supersedes ADR 0027 as the detailed split-button styling owner. It depends on ADR 0052 for the three-section control structure and behavior.

### Consequences

* Good, because the `AI` section uses the same violet family as the JetBrains AI Assistant commit-message action icon.
* Good, because the snake-loop animation gives clear phase ownership while keeping labels readable.
* Good, because passive, hover, running, and disabled states are explicitly captured before implementation.
* Bad, because the violet `AI` section is more visually distinct from the blue `Commit` section and needs toolbar-scale review.
* Bad, because implementation may need custom segmented control drawing to match cumulative hover and per-section running indication.

### Confirmation

Compliance should be checked by:

* Design review against `docs/concepts/graphics/split-button-drafts/01-blue-steel-compact-snake-violet-ai.svg`.
* Light and dark theme rendering review at actual Commit tool window toolbar scale.
* Verification that hover styling changes only backgrounds.
* Verification that clicked/running states show snake-loop activity on the currently running section.
* Documentation validation after the final style is applied to repository-facing docs.
* Runtime UI validation after the design is implemented outside the draft graphics.

## Pros and Cons of the Options

### Select `01-blue-steel-compact-snake-violet-ai.svg`

* Good, because it matches the user's final choice from the design session.
* Good, because the `AI` active-section color follows the AI Assistant commit-message action icon color.
* Good, because it keeps the chosen snake-loop activity model.
* Bad, because it introduces a stronger purple-to-blue contrast between `AI` and `Commit`.

### Select `01-blue-steel-compact-snake.svg`

* Good, because it has the same state coverage and snake-loop behavior.
* Good, because the blue family keeps the workflow visually quieter.
* Bad, because it does not use the JetBrains AI Assistant violet for the `AI` section.

### Select `01-blue-steel-compact.svg`

* Good, because the progress strip is restrained and simple.
* Bad, because the selected snake-loop animation communicates the active section more directly.

### Select `01-blue-steel-compact-magic-spin.svg`

* Good, because it has a stronger AI-flavored motion.
* Bad, because the rotating shimmer can feel decorative and less operational for `Commit` and `Push`.

### Keep ADR 0027's generated placeholder as the styling owner

* Good, because it avoids another design decision.
* Bad, because it describes the older two-segment placeholder direction and no longer matches the selected three-section control.

## More Information

- Selected draft: `docs/concepts/graphics/split-button-drafts/01-blue-steel-compact-snake-violet-ai.svg`.
- Related structure decision: `docs/decisions/adr-0052-use-three-section-ai-commit-push-control.md`.
- Superseded styling decision: `docs/decisions/adr-0027-use-generated-placeholder-graphic-for-split-button-styling.md`.
- Runtime implementation was completed by `.agents/plans/archive/PLAN-three-section-ai-commit-push-control.md`.
- Local AI Assistant icon evidence: the commit-message action declares `com.intellij.ml.llm.core.AIAssistantBrandingIcons.LogoColored`, whose local SVG resources use `#834DF0` for light theme and `#A571E6` for dark theme.
