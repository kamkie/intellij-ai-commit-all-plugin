---
proposal_id: PROP-plugin-default-settings
generated_at: 2026-05-15T07-08
purpose: Propose changing a few default plugin behaviors (clear commit message before AI generation, suppress the push confirmation window, and take over Ctrl+K / Ctrl+Shift+K shortcuts) for maintainer triage.
scope: Plugin runtime defaults and keymap bindings under `src/main/kotlin/pl/devopssolutions/aicommitall/` and `src/main/resources/META-INF/plugin.xml`, plus related settings UI.
---

# Plugin Default Settings Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
  - [E1. Commit message is not cleared before AI generation](#e1-commit-message-is-not-cleared-before-ai-generation)
  - [E2. Push confirmation window is shown on commit-and-push flow](#e2-push-confirmation-window-is-shown-on-commit-and-push-flow)
  - [E3. Ctrl+K and Ctrl+Shift+K shortcuts are not taken over by plugin actions](#e3-ctrlk-and-ctrlshiftk-shortcuts-are-not-taken-over-by-plugin-actions)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The plugin currently reuses standard IntelliJ commit/push defaults; this proposal collects three opinionated default-behavior changes the maintainer requested for triage.
- Triage scope is plugin runtime defaults (commit-message handling, push window suppression) and keymap bindings (Ctrl+K, Ctrl+Shift+K).
- This proposal performs no implementation; each finding is advisory until accepted via the normal ADR/plan flow defined in `docs/decisions/README.md` and `.agents/references/planning.md`.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id | Title                                                          | Priority | Status   | Decision |
|----|----------------------------------------------------------------|----------|----------|----------|
| E1 | Commit message is not cleared before AI generation             | 1        | deferred | deferred |
| E2 | Push confirmation window is shown on commit-and-push flow      | 1        | deferred | deferred |
| E3 | Ctrl+K and Ctrl+Shift+K shortcuts are not taken over by plugin | 2        | deferred | deferred |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E1. Commit message is not cleared before AI generation

- Evidence: `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletion.kt` and `AiCommitMessageActionInvocationContext.kt` capture the existing commit message before invoking AI and use it as a baseline for change detection; there is no step that clears the field first. A stale, manually-typed or previously-generated message can therefore remain visible (and influence AI Assistant) at the start of generation.
- Impact: Users see a confusing mix of old and new content while AI works; "message changed" detection has to rely on field diffing against a stale baseline; AI Assistant may be biased by leftover text. Conflicts with the user's expectation that `AI Commit All` always produces a fresh message.
- Proposal: Make "clear commit message before invoking AI generation" the default behavior for both the `AI Commit All` and `& Push` actions. Expose an `AiCommitAllSettings` toggle (e.g., `clearCommitMessageBeforeGeneration`, default `true`) so users who prefer the current behavior can opt out. Ensure the user-edit-detection signal treats this programmatic clear as non-user input.

```yaml
status: deferred
decision: deferred
priority: 1
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-02-pre-release-ux E001`."
```

### E2. Push confirmation window is shown on commit-and-push flow

- Evidence: ADR 0017 ("Use Standard IDE Confirmation Barriers") and the current `& Push` implementation route through Git's commit-and-push executor, which by default opens the IntelliJ "Push Commits" dialog. The user explicitly requests that the push window not be shown for the plugin's one-click flow.
- Impact: An extra confirmation dialog defeats the "one-click AI commit and push" value proposition; users who already accepted the split-button `& Push` action get prompted again. This is the most common friction reported for similar plugins.
- Proposal: Default the `& Push` action to skip the Push Commits dialog and push immediately to the current tracked branch when conditions are safe (single Git root or all roots have a tracked upstream, no force-push needed, no unresolved conflicts). Fall back to the standard dialog when conditions are not safe. Expose an `AiCommitAllSettings` toggle (e.g., `showPushDialog`, default `false`) and document the interaction with ADR 0017 — likely requires a new ADR that narrows ADR 0017 for the `& Push` action specifically.

```yaml
status: deferred
decision: deferred
priority: 1
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-02-pre-release-ux E002`."
```

### E3. Ctrl+K and Ctrl+Shift+K shortcuts are not taken over by plugin actions

- Evidence: `src/main/resources/META-INF/plugin.xml` registers the `AI Commit All` and `& Push` actions in the Commit tool window but does not declare `<keyboard-shortcut>` entries. Ctrl+K and Ctrl+Shift+K remain bound to IntelliJ's built-in `CheckinProject` (Commit) and `Vcs.Push` actions.
- Impact: Users who install the plugin still have to click the split button or remap shortcuts manually to get the AI flow on their primary muscle-memory keys. Taking over the shortcuts gives the AI flow first-class status, matching the request.
- Proposal: Register first/default keyboard shortcuts for the plugin actions:
  - `AI Commit All` → `ctrl K` (Windows/Linux) and `meta K` (macOS), replacing the default `CheckinProject` binding for users who keep the plugin's keymap.
  - `& Push` (AI Commit All & Push) → `ctrl shift K` / `meta shift K`, replacing the default `Vcs.Push` binding.
      Implement via `<keyboard-shortcut keymap="$default" first-keystroke="…"/>` in `plugin.xml` and provide an override entry that removes the original action's shortcut where the IntelliJ Platform allows it (`<remove-shortcut>` or via a custom `KeymapExtension`). Because this overrides well-established IDE shortcuts, treat acceptance as a user-visible behavioral change that requires an ADR and a `README.md` note, plus an `AiCommitAllSettings` option (e.g., `overrideCommitShortcuts`, default `true`) so users can opt out without manually editing the keymap.

```yaml
status: deferred
decision: deferred
priority: 2
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-02-pre-release-ux E006`."
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- Group the three new toggles under a single "Defaults" section in `AiCommitAllConfigurable` to keep the Settings page tidy.
- If E3 is accepted, add a one-line note to `README.md` and `CHANGELOG.md` calling out the shortcut takeover so users are not surprised.

## Suggested Priority Order

1. `E1` — cheapest and most visible quality-of-life fix; unblocks cleaner AI prompts.
2. `E2` — biggest UX win for the `& Push` flow; needs ADR follow-up to ADR 0017.
3. `E3` — most invasive (overrides IDE-wide shortcuts); requires its own ADR and clear opt-out.

## Out Of Scope

- Changing default behavior for non-Git VCS (out per ADR 0009).
- Reworking the split-button styling or icons (covered by ADR 0027).
- Adding new commit content rules or AI prompt customization.
- Implementing the changes themselves; this proposal stops at triage.
