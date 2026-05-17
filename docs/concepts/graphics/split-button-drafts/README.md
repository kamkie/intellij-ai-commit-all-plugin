# Split Button Drafts

These drafts implement ADR 0025's draft-series requirement for detailed `AI Commit All` split-button styling before final selection.

The common design intent is:

- Keep the accepted ADR 0006 structure: left `AI Commit All` segment and right `& Push` segment.
- Stay close to the existing concept graphic language: compact IntelliJ-like controls, small sparkle signal, restrained color, and clear state treatment.
- Keep the middle join free of arrow graphics.
- Use related but contrasting left and right segment colors so the push action is visible without becoming a separate full button.
- Treat all files here as review artifacts, not final plugin UI assets.

## Drafts

| Draft | Direction | Strength | Risk |
|-------|-----------|----------|------|
| [01 - Blue Steel](01-blue-steel.svg) | Quiet blue primary segment with green push segment. | Strongest continuity with current icons. | Can read conservative in dark theme. |
| [02 - Teal Graphite](02-teal-graphite.svg) | Teal commit segment with graphite push segment. | Best neutral tooling feel. | Push segment may need brighter hover treatment. |
| [03 - Indigo Lime](03-indigo-lime.svg) | Indigo commit segment with lime-green push segment. | Highest segment distinction. | More saturated than typical IntelliJ controls. |
| [04 - Slate Cyan](04-slate-cyan.svg) | Slate commit segment with cyan push segment. | Strong dark-theme fit. | Lighter theme needs careful border contrast. |
| [05 - Violet Mint](05-violet-mint.svg) | Muted violet commit segment with mint push segment. | Good AI signal without using a gradient-heavy look. | Purple family should stay muted if selected. |

## State Coverage

Each draft shows:

- Commit-only normal state.
- Commit-and-push normal state.
- Running state.
- Disabled state.
- Light theme.
- Dark theme.

## Scoring Criteria

Use a 1-5 score during maintainer review:

| Criterion | What To Check |
|-----------|---------------|
| Legibility | Labels and icons remain readable at toolbar scale. |
| Theme contrast | Light and dark variants keep enough contrast without feeling like different products. |
| Segment distinction | Left and right actions are related but visually distinct. |
| IntelliJ fit | Shape, border, density, and state treatment fit IDE toolbar conventions. |
| Brand signal | The AI sparkle is present without dominating the control. |
| Accessibility | Disabled and running states remain distinguishable without relying only on color. |

## Suggested Review Flow

1. Remove any draft that fails legibility or IntelliJ fit.
2. Compare remaining drafts in light and dark theme pairs.
3. Pick the best segment contrast direction.
4. Create a final-selection ADR that supersedes ADR 0027 where needed.
