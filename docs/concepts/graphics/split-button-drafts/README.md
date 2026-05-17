# Split Button Drafts

These drafts implement ADR 0025's draft-series requirement for detailed split-button styling before final selection.

Design-session premise: ADR 0052 is treated as accepted for this draft set. The draft graphics now explore the proposed three-section cumulative control rather than the older two-segment `AI Commit All` / `& Push` split button.

The common design intent is:

- Use the ADR 0052 structure: one compact `<icon> AI | Commit | Push` control.
- Stay close to the new reference direction: a compact IntelliJ run-widget-like control, small sparkle signal, restrained color, and clear state treatment.
- Keep both internal joins free of arrow graphics.
- Render each candidate as one segmented control: IntelliJ-style rounded outer corners, straight internal joins, compact icon/text padding, an AI icon in the first section, and an icon-forward `Push` section.
- Use related but contrasting section colors so `AI`, `Commit`, and `Push` read as a cumulative workflow without becoming three unrelated buttons.
- Treat all files here as review artifacts, not final plugin UI assets.

## Drafts

| Draft                                                                                 | Direction                                                                                                                                                                                          | Strength                                                             | Risk                                                                           |
|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|--------------------------------------------------------------------------------|
| [01 - Blue Steel](01-blue-steel.svg)                                                  | Blue AI base with steel commit and green push progression.                                                                                                                                         | Strong continuity with current icons.                                | Can read conservative in dark theme.                                           |
| [01b - Blue Steel Compact](01-blue-steel-compact.svg)                                 | Blue Steel with tighter internal section padding, shorter total control width, and per-section activity animation.                                                                                 | Closest to the requested compact divider spacing and phase feedback. | Needs review at actual toolbar scale to ensure labels do not feel crowded.     |
| [01c - Blue Steel Compact Snake](01-blue-steel-compact-snake.svg)                     | Chosen animation direction showing passive, hover, clicked/running, and disabled states with snake-loop activity on clicked phases.                                                                | Very clear phase ownership without changing fills.                   | May feel busy at small toolbar scale.                                          |
| [01d - Blue Steel Compact Magic Spin](01-blue-steel-compact-magic-spin.svg)           | Alternative activity animation using a rotating shimmer behind the active section.                                                                                                                 | Stronger AI-flavored motion and softer than a progress bar.          | Needs restraint to avoid feeling decorative.                                   |
| [01f - Blue Steel Compact Snake Violet AI](01-blue-steel-compact-snake-violet-ai.svg) | Snake-loop variant where the active AI section uses the JetBrains AI Assistant commit-message action violet (`#834DF0` light, `#A571E6` dark), with muted violet passive AI background/glyph/text. | Aligns the AI section with the platform AI Assistant action color.   | Violet may separate the AI section more strongly from the blue commit section. |
| [02 - Teal Graphite](02-teal-graphite.svg)                                            | Teal AI base with graphite commit and neutral push progression.                                                                                                                                    | Best neutral tooling feel.                                           | Push may need brighter hover treatment.                                        |
| [03 - Indigo Lime](03-indigo-lime.svg)                                                | Indigo AI base with lime push progression.                                                                                                                                                         | Highest cumulative-section distinction.                              | More saturated than typical IntelliJ controls.                                 |
| [04 - Slate Cyan](04-slate-cyan.svg)                                                  | Slate body with cyan push progression.                                                                                                                                                             | Strong dark-theme fit.                                               | Light theme needs careful border contrast.                                     |
| [05 - Violet Mint](05-violet-mint.svg)                                                | Muted violet AI base with mint push progression.                                                                                                                                                   | Good AI signal without using a gradient-heavy look.                  | Purple family should stay muted if selected.                                   |
| [05b - Violet Mint Alt](05-violet-mint-2.svg)                                         | Alternate violet and mint cumulative emphasis.                                                                                                                                                     | Tests a brighter final-step treatment.                               | May be too close to `05 - Violet Mint` to keep both.                           |

`01c - Blue Steel Compact Snake` is the chosen animation direction for this design session.

All listed drafts use the same compact run-widget-like one-control geometry after maintainer feedback on 2026-05-17 and the ADR 0052 design-session premise. Reviewers should compare color, icon fit, cumulative hover, per-section activity feedback, and disabled treatment rather than button shape.

## State Coverage

Each draft shows:

- AI-only normal state.
- AI-and-commit normal state.
- AI-commit-and-push normal state.
- AI-section hover state.
- Commit-section cumulative hover state.
- Push-section cumulative hover state.
- Staging-and-AI-message running state.
- Commit running state.
- Push running state.
- Disabled state.
- Light theme.
- Dark theme.

## Scoring Criteria

Use a 1-5 score during maintainer review:

| Criterion           | What To Check                                                                                                          |
|---------------------|------------------------------------------------------------------------------------------------------------------------|
| Legibility          | Labels and icons remain readable at toolbar scale.                                                                     |
| Theme contrast      | Light and dark variants keep enough contrast without feeling like different products.                                  |
| Segment distinction | `AI`, `Commit`, and `Push` sections are related but visually distinct.                                                 |
| IntelliJ fit        | Outer corner radius, hard divider, compact padding, border treatment, and state treatment fit IDE toolbar conventions. |
| Brand signal        | The AI sparkle is present without dominating the control.                                                              |
| Accessibility       | Disabled, running, and cumulative hover states remain distinguishable without relying only on color.                   |

## Suggested Review Flow

1. Remove any draft that fails legibility or IntelliJ fit.
2. Compare remaining drafts in light and dark theme pairs.
3. Pick the best segment contrast direction.
4. Create a final-selection ADR that supersedes ADR 0027 where needed.
