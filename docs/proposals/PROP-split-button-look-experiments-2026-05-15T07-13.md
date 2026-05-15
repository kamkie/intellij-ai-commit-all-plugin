---
proposal_id: PROP-split-button-look-experiments
generated_at: 2026-05-15T07-13
purpose: Propose running structured experiments and using a decision tree to choose a new look for the AI Commit All split button, because the current look is not what the maintainer had in mind.
scope: Split-button visual styling under `src/main/resources/icons/`, `docs/concepts/graphics/`, and related ADRs 0006, 0015, 0025, and 0027.
---

# Split Button Look Experiments Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E1. Current split-button look does not match maintainer intent](#e1-current-split-button-look-does-not-match-maintainer-intent)
    - [E2. No structured experimentation process for visual styling](#e2-no-structured-experimentation-process-for-visual-styling)
    - [E3. No decision tree to converge on a final look](#e3-no-decision-tree-to-converge-on-a-final-look)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The maintainer has indicated the current split-button look (per ADR 0027 placeholder and the existing icon set under `src/main/resources/icons/`) is not what they had in mind and wants to experiment further before committing to a final design.
- Triage scope is the visual styling of the `AI Commit All` / `& Push` split button, including icon variants, label/affordance treatment, light/dark themes, and running/disabled states; and the *process* used to converge on a final look (experimentation + decision tree).
- This proposal performs no implementation; each finding is advisory until accepted via the normal ADR/plan flow defined in `docs/decisions/README.md` and `.agents/references/planning.md`.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id | Title                                                      | Priority | Status | Decision |
|----|------------------------------------------------------------|----------|--------|----------|
| E1 | Current split-button look does not match maintainer intent | 1        | open   |          |
| E2 | No structured experimentation process for visual styling   | 1        | open   |          |
| E3 | No decision tree to converge on a final look               | 2        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E1. Current split-button look does not match maintainer intent

- Evidence: ADR 0027 explicitly records "use a generated placeholder graphic now" for split-button styling, and the placeholder lives at `docs/concepts/graphics/split-button-placeholder.png`. The active icon assets under `src/main/resources/icons/aiCommitAll{,_dark}.svg` and `aiCommitAllPush{,_dark}.svg` were adapted from AI-generated base concepts per ADR 0015. The maintainer feedback ("look of the buttons is not what I had in mind") indicates the chosen placeholder/icons are not the intended final design.
- Impact: The split button is the plugin's primary user-facing affordance; a look that does not match maintainer intent risks low first-impression quality on the JetBrains Marketplace, weakens the `AI Commit All` brand established by ADR 0005, and may need to be reworked again after release.
- Proposal: Treat the current placeholder + icons as explicitly non-final and open a styling iteration. Capture the *missing intent* in `docs/decisions/OPEN_QUESTIONS.md` as a new `Q-UX-*` question (e.g., "What visual direction should the split button take? Flat monochrome glyph, accented AI-spark, badge-on-VCS-icon, text+icon combo, ...?") so subsequent ADRs can reference an answered question rather than re-deriving intent.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E2. No structured experimentation process for visual styling

- Evidence: ADR 0025 records the decision to "create a series of draft styles before final selection", but ADR 0027 short-circuited that by accepting a single generated placeholder. There is no record of multiple drafts compared side-by-side, no documented evaluation criteria, and no sandbox screenshots checked into `docs/concepts/graphics/` beyond the single placeholder PNG.
- Impact: Without an explicit experimentation process, every styling change becomes ad-hoc, hard to review, and hard to revisit. Iteration cost stays high because there is no shared baseline of candidates to compare against.
- Proposal: Define a lightweight visual-experimentation workflow:
    1. Produce a set of N candidate split-button styles (suggest N = 4–6) as SVG drafts under `docs/concepts/graphics/split-button-drafts/<NN>-<short-slug>/` containing: `light.svg`, `dark.svg`, `running.svg` (or animated frame), and a `README.md` describing the concept in one paragraph.
    2. Render each candidate inside a sandbox IDE screenshot (commit tool window, both themes) and store the screenshots next to the SVGs.
    3. Score each candidate against fixed criteria: legibility at 16 px, theme contrast (light + dark + high-contrast), distinguishability between `AI Commit All` and `& Push` segments, alignment with IntelliJ Platform icon guidelines, brand recognizability, and accessibility (color-independent meaning).
    4. Record the comparison table in a new proposal or an ADR draft and let the maintainer pick.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E3. No decision tree to converge on a final look

- Evidence: ADRs 0006, 0015, 0025, and 0027 describe styling decisions in isolation but do not chain into a single decision flow. There is no document that says "if X then choose A else go to step Y", which is the explicit shape the maintainer requested ("maybe use decision tree").
- Impact: Without an explicit decision tree, future styling iterations risk re-litigating already-settled sub-questions (e.g., monochrome vs. accent color, single icon vs. icon+label) and leave reviewers unsure which trade-off applies at each step.
- Proposal: Add a decision tree to the styling iteration document with branches covering the major axes of split-button design, used as a guided checklist during experimentation. Suggested top-level branches:
    1. **Brand prominence**
        - Is the AI nature of the action a primary signal? → emphasize an AI-specific glyph (spark/star/sparkle) on the icon.
        - Or is "commit + push" the primary signal? → reuse standard VCS commit/push glyphs with a small AI accent.
    2. **Icon vs. icon + label**
        - Toolbar density constraints allow a label? → use `[✨ AI Commit All] [▾]`.
        - Otherwise → icon-only with tooltip parity to ADR 0005 canonical labels.
    3. **Two-segment differentiation** (`AI Commit All` vs. `& Push`)
        - Different glyphs per segment? (e.g., commit checkmark vs. upward arrow on push).
        - Or shared glyph + modifier? (e.g., AI spark + small arrow on push segment).
    4. **Color usage**
        - Monochrome (IntelliJ recommended)? → single accent color from the platform palette, both themes derived.
        - Accent color allowed? → constrain to one accent + neutral, validate in light, dark, and high-contrast themes.
    5. **State variations** (normal / hover / running / disabled / commit-only vs. commit-and-push)
        - Running: animated spark vs. standard IntelliJ progress overlay (ADR 0027 already requires an animation here).
        - Disabled: 40% alpha vs. desaturated.
    6. **Validation gate**
        - Does the candidate pass the E2 scoring criteria (legibility, contrast, distinguishability, guideline alignment, brand, accessibility)? → promote to finalist.
        - Otherwise → discard or iterate.
          The tree itself should live alongside the drafts (e.g., `docs/concepts/graphics/split-button-drafts/DECISION_TREE.md`) and be referenced from the follow-up ADR that supersedes ADR 0027.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- If experimentation is accepted, supersede ADR 0027 with a new ADR that records the chosen styling and link both ways for history.
- Keep the canonical action labels from ADR 0005 (`AI Commit All`, `& Push`) regardless of icon choice; do not let visual iteration drift the wording.
- Consider exporting finalist SVGs through IntelliJ's icon guidelines (16 px base, 1 px stroke, pixel-aligned) to avoid blurry rendering on non-HiDPI displays.

## Suggested Priority Order

1. `E1` — capture the missing maintainer intent as an open question so subsequent work has a target.
2. `E2` — stand up the lightweight drafts/screenshots/scoring workflow; without it, E3 has nothing to evaluate.
3. `E3` — formalize the decision tree once at least one round of drafts exists, so the branches are grounded in real candidates.

## Out Of Scope

- Changing canonical action wording (`AI Commit All`, `& Push`); locked by ADR 0005.
- Changing the split-button structure itself (two segments + dropdown); locked by ADR 0006.
- Replacing the IntelliJ-style SVG icon direction with a different asset family (e.g., raster-only or external icon font); locked by ADR 0015.
- Implementing any new icons or animations; this proposal stops at triage and process definition.
