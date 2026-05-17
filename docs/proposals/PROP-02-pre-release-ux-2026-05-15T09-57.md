---
proposal_id: PROP-02-pre-release-ux
generated_at: 2026-05-15T09-57
purpose: Consolidate pre-release plugin behavior and split-button visual UX findings into one active work stream for maintainer triage.
scope: Plugin runtime defaults, commit-and-push behavior, keymap bindings, split-button styling, icon drafts, screenshots, and follow-up UX ADRs.
supersedes:
    - PROP-plugin-default-settings
    - PROP-split-button-look-experiments
---

# Pre-Release UX Work Stream

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It consolidates pre-release user-experience findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E001. Clear the commit message before AI generation](#e001-clear-the-commit-message-before-ai-generation)
    - [E002. Skip the push dialog only when safe](#e002-skip-the-push-dialog-only-when-safe)
    - [E003. Capture split-button design intent](#e003-capture-split-button-design-intent)
    - [E004. Run split-button visual draft experiments](#e004-run-split-button-visual-draft-experiments)
    - [E005. Add a split-button decision tree and final-selection ADR](#e005-add-a-split-button-decision-tree-and-final-selection-adr)
    - [E006. Evaluate shortcut takeover with opt-out](#e006-evaluate-shortcut-takeover-with-opt-out)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- This work stream owns user-visible behavior and first-impression polish before public Marketplace publication.
- It consolidates plugin default settings and split-button visual experiments because both affect the first run of `AI Commit All`.
- Behavior changes that narrow accepted ADRs, especially push confirmation behavior and shortcut ownership, need new ADRs before implementation.

## Progress Tracker

| Id   | Title                                                    | Priority | Status  | Decision |
|------|----------------------------------------------------------|----------|---------|----------|
| E001 | Clear the commit message before AI generation            | 1        | done    | accepted |
| E002 | Skip the push dialog only when safe                      | 1        | done    | accepted |
| E003 | Capture split-button design intent                       | 1        | done    | accepted |
| E004 | Run split-button visual draft experiments                | 2        | done    | accepted |
| E005 | Add a split-button decision tree and final-selection ADR | 2        | done    | accepted |
| E006 | Evaluate shortcut takeover with opt-out                  | 3        | blocked | accepted |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current timestamp.
- Use `status` for implementation progress and `decision` for maintainer triage.
- Update the Proposal Implementation Summary in `docs/proposals/README.md` for accepted findings with non-terminal implementation status and an evidence path. A `TASKS.md` entry is optional when another evidence path is clearer.
- Leave completed, rejected, or superseded findings in place as history.

## Errors And Mistakes

### E001. Clear the commit message before AI generation

- Evidence: `PROP-plugin-default-settings` `E1` identified that the plugin captures the existing commit message as a baseline but does not clear stale content before invoking AI generation.
- Impact: Users can see old text during generation, and AI Assistant may be biased by leftover commit-message content.
- Proposal: Default both `AI Commit All` and `& Push` to clear the commit message before invoking AI generation. Add a settings toggle defaulting to enabled, and ensure the programmatic clear is not treated as a user edit.

```yaml
status: done
decision: accepted
priority: 1
owner:
updated: 2026-05-18T01:09:34+02:00
accepted_at: 2026-05-15T11:31:11+02:00
comment: "Implemented by clearing stale commit-message text before AI generation by default, with a settings toggle to preserve existing text."
```

### E002. Skip the push dialog only when safe

- Evidence: `PROP-plugin-default-settings` `E2` identified that the current `& Push` flow routes through the standard Push Commits dialog.
- Impact: A second confirmation step weakens the one-click value of the split-button `& Push` action.
- Proposal: Default `& Push` to push immediately only when the repository state is safe: tracked upstreams are available, no force push is needed, no unresolved conflicts exist, and multi-root behavior is unambiguous. Fall back to the standard dialog otherwise. Record the narrowed behavior in a new ADR that explicitly relates to ADR 0017.

```yaml
status: done
decision: accepted
priority: 1
owner:
updated: 2026-05-17T20:24:11+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Safe immediate push implemented for tracked-upstream Git states with standard commit-and-push fallback."
```

### E003. Capture split-button design intent

- Evidence: `PROP-split-button-look-experiments` `E1` records maintainer feedback that the current placeholder/icon look is not the intended final design.
- Impact: Styling work will keep drifting unless the missing visual intent is captured before new assets are produced.
- Proposal: Add a `Q-UX-*` entry in `docs/decisions/OPEN_QUESTIONS.md` or equivalent ADR draft prompt that asks for the intended visual direction. Treat current placeholder graphics as non-final until the question is answered.

```yaml
status: done
decision: accepted
priority: 1
owner:
updated: 2026-05-17T19:47:19+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Source: PROP-split-button-look-experiments E1."
```

### E004. Run split-button visual draft experiments

- Evidence: `PROP-split-button-look-experiments` `E2` identified that ADR 0025 called for multiple draft styles, but ADR 0027 accepted a single generated placeholder.
- Impact: Maintainer review has no side-by-side draft set with screenshots, state coverage, or scoring criteria.
- Proposal: Produce four to six draft split-button styles under `docs/concepts/graphics/split-button-drafts/`, including light, dark, running, disabled, commit-only, and commit-and-push states. Capture sandbox screenshots and score them for legibility, theme contrast, segment distinction, IntelliJ guideline fit, brand signal, and accessibility.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-17T21:55:22+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Source: PROP-split-button-look-experiments E2."
command: "Make the drafts closer to the IntelliJ run-widget reference: one rounded toolbar-like body, a text primary segment, an icon-forward push segment, a straight divider, minimal middle gap, and hover examples for both segments."
```

### E005. Add a split-button decision tree and final-selection ADR

- Evidence: `PROP-split-button-look-experiments` `E3` identified no structured decision flow from current ADRs 0006, 0015, 0025, and 0027 to a final look.
- Impact: Reviewers must re-litigate the same choices across icon family, color use, state treatment, and segment differentiation.
- Proposal: Add a compact decision tree beside the draft set, then create a follow-up ADR that records the selected final direction and supersedes ADR 0027 where appropriate.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-17T23:40:43+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Source: PROP-split-button-look-experiments E3. Decision tree revised and final style selected by accepted ADR 0053."
```

### E006. Evaluate shortcut takeover with opt-out

- Evidence: `PROP-plugin-default-settings` `E3` proposed taking over `Ctrl+K` / `Ctrl+Shift+K` and macOS equivalents for plugin actions.
- Impact: Shortcut takeover would make the plugin feel first-class but would override established IDE muscle memory.
- Proposal: Defer until `E001` and `E002` settle. If accepted, record a dedicated ADR, document the user-visible change, and provide an opt-out setting or clear keymap restoration path.

```yaml
status: blocked
decision: accepted
priority: 3
owner:
updated: 2026-05-18T01:05:37+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Source: PROP-plugin-default-settings E3. Proposed ADR 0054 created; implementation waits for maintainer acceptance and must include a settings opt-out."
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- Keep product label `AI Commit All` from ADR 0005; ADR 0052 now governs the three-section `AI`, `Commit`, and `Push` control labels.
- Keep ADR 0052's three-section control structure; ADR 0006 is superseded for future implementation.
- Group any accepted runtime toggles under a small Settings section rather than scattering them.

## Suggested Priority Order

1. `E001` - clear stale text before generation.
2. `E002` - remove push-dialog friction only with safe fallback behavior.
3. `E003` - capture visual intent before producing new assets.
4. `E004` - produce and screenshot candidate styles.
5. `E005` - record final style selection through a decision tree and ADR.
6. `E006` - revisit shortcut ownership after runtime behavior is settled.

## Out Of Scope

- Non-Git VCS behavior, governed by ADR 0009.
- Replacing JetBrains AI Assistant integration.
- Marketplace release tagging or publication.
- Repository hygiene and contributor-process automation.
