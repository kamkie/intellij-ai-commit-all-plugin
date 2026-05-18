---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use AI-Generated IntelliJ-Style Icon Bases

## Context and Problem Statement

The icon direction for plugin actions was open.

The user answered `Q-UX-2`: icons should use AI-generated icon bases and be inspired by default IntelliJ icons and guidelines.

JetBrains SDK guidance says plugins should reuse existing platform icons where possible, and custom icons should follow the IntelliJ Platform icon style and file requirements.

Sources:

- https://plugins.jetbrains.com/docs/intellij/icons.html
- https://plugins.jetbrains.com/docs/intellij/icons-style.html

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use AI-Generated IntelliJ-Style Icon Bases
* Use only stock IntelliJ Platform icons.
* Commit raw AI-generated raster icons.

## Decision Outcome

Chosen option: "Adopt Use AI-Generated IntelliJ-Style Icon Bases", because Use AI-generated icon concepts as base artwork for this plugin's custom icons.

Use AI-generated icon concepts as base artwork for this plugin's custom icons.

Final committed icons must be adapted to IntelliJ Platform conventions:

- Use clean SVG assets, not raw raster AI output.
- Follow IntelliJ's flat, simple, geometric icon style.
- Use action-icon sizing and placement appropriate for the target UI location.
- Provide dark-theme variants when the base icon does not work well in dark theme.
- Support New UI icon requirements where applicable.
- Avoid copying JetBrains proprietary icon artwork directly; use default IntelliJ icons and guidelines as style references.

Prefer existing platform icons when they already communicate the exact action clearly. Use custom AI-derived icons when the plugin needs a distinct AI Commit All identity.

### Consequences

- Icon implementation includes a design/export step, not only code registration.
- AI-generated images are treated as concept input that must be redrawn or cleaned into platform-quality SVG assets before commit.
- Icon validation must include light and dark theme rendering, and New UI behavior where the icon is used.
- Detailed split-button styling remains a separate open UX question.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use AI-Generated IntelliJ-Style Icon Bases

* Good, because Use AI-generated icon concepts as base artwork for this plugin's custom icons.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use only stock IntelliJ Platform icons.

* Bad, because the user requested AI-generated icon bases.

### Commit raw AI-generated raster icons.

* Bad, because IntelliJ plugin icons should be clean SVG assets that render well at small sizes and across themes.

## More Information

- Remove `Q-UX-2` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` with icon asset creation and validation tasks.
