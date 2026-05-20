---
status: accepted
date: 2026-05-20
accepted_at: 2026-05-20T04:30:12+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Replace Standard Commit-And-Push Toolbar Action

## Context and Problem Statement

`PLAN-three-section-ai-commit-push-control` introduced a startup activity that removes the standard `Commit and Push...` action from the Commit tool window primary actions while the plugin three-section control is registered (`REQ-ACT-004`, `REQ-INT-003`, archived `T-IDEA-011`).

The behavior is durable, user-visible, and re-shapes the Commit tool window toolbar, but it currently has no dedicated ADR. `docs/requested-features.md` correctly excludes plan-sourced emerged behavior from its "Shipped" inventory, which leaves this user-visible change without a decision record.

## Decision Drivers

* Avoid duplicating IDE actions: the plugin three-section `Push` section already covers the commit-and-push intent.
* Avoid splitting Push behavior across two unrelated toolbar actions that users could click in either order, since they implement different commit pipelines.
* Keep the change reversible: removing the plugin restores the standard IDE toolbar through the IntelliJ Platform extension lifecycle.
* Document a durable user-visible toolbar change because `requested-features.md` correctly excludes plan-sourced behavior from its "Shipped" inventory.

## Considered Options

* Replace the standard `Commit and Push...` action with the plugin three-section control.
* Keep both actions visible side by side in the Commit tool window.
* Hide the plugin control and keep the standard action.

## Decision Outcome

Chosen option: "Replace the standard `Commit and Push...` action with the plugin three-section control.", because the plugin `Push` section already implements commit-and-push with AI message generation; keeping both actions visible would invite users to interleave the two commit pipelines and would dilute the value of the plugin's one-click flow.

When the plugin is installed and enabled and the Commit tool window primary actions group is present, a startup activity must remove the standard `Commit and Push...` action from that toolbar group. Uninstalling or disabling the plugin must restore the standard action through the normal IntelliJ Platform mechanism: no persistent keymap or action changes outside the plugin lifecycle.

The plugin MUST NOT remove or re-bind any other standard Commit tool window actions through this path. Shortcut takeover for `CheckinProject` and `Vcs.Push` is governed by ADR 0054 and remains independent.

### Consequences

* Good, because users see a single, consistent commit-and-push entry point in the Commit tool window.
* Good, because the plugin can guarantee its safe-immediate-push path is the actual push path when the user clicks the toolbar.
* Good, because the change is no longer plan-sourced and now has a durable decision record.
* Bad, because users who learned the standard `Commit and Push...` action will need to recognize the plugin control as its replacement.
* Bad, because uninstall-time restoration relies on the plugin descriptor extension lifecycle; no persistent IDE configuration is changed.

### Confirmation

After acceptance, confirm the behavior through:

* `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllCommitToolbarCustomizer.kt` `AiCommitAllCommitToolbarStartupActivity` removes the standard action group entry.
* `src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllCommitToolbarCustomizerTest.kt` validates the removal.
* `docs/specification.md` `REQ-ACT-004` and `REQ-INT-003` source lines reference this ADR (currently sourced from `PLAN-three-section-ai-commit-push-control`).
* Manual sandbox row `T-IDEA-011`.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.

## Pros and Cons of the Options

### Replace the standard `Commit and Push...` action with the plugin three-section control.

* Good, because it makes the plugin `Push` section the canonical commit-and-push toolbar entry point.
* Good, because it avoids two toolbar actions for the same intent.
* Neutral, because the standard action remains reachable through the IDE menu, keymap, and shortcut takeover opt-out.

### Keep both actions visible side by side in the Commit tool window.

* Good, because users do not lose a familiar IDE action.
* Bad, because the two actions implement different commit pipelines (one with AI message generation, one without), encouraging interleaving.
* Bad, because the visual surface duplicates intent without distinguishing the two pipelines.

### Hide the plugin control and keep the standard action.

* Good, because the IDE toolbar would stay unchanged.
* Bad, because the plugin's primary value (one-click AI commit and push) becomes harder to discover.
* Bad, because users would need to learn keyboard shortcuts to reach the plugin behavior.

## More Information

- Records the durable toolbar customization introduced by `PLAN-three-section-ai-commit-push-control` (archived `T-IDEA-011`, `REQ-ACT-004`, `REQ-INT-003`).
- Existing implementation: `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllCommitToolbarCustomizer.kt` `AiCommitAllCommitToolbarStartupActivity`.
- Existing documentation: `docs/specification.md` `REQ-ACT-004` and `REQ-INT-003`; `config/intellij-platform/change-notes.html`.
- Shortcut takeover is governed separately by ADR 0054.
- After acceptance, update the ADR Implementation Tracker in `docs/decisions/README.md`, update the source lines of `docs/specification.md` `REQ-ACT-004` and `REQ-INT-003`, and add a row to `docs/requested-features.md` UI and Control section.
