---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T01:20:58+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use VCS Shortcuts For AI Commit All With Opt Out

## Context and Problem Statement

`PROP-02-pre-release-ux` `E006` accepts evaluating whether `AI Commit All` should take over the standard IDE commit and push shortcuts before release. ADR 0052 now defines one three-section control with `AI`, `Commit`, and `Push` sections, so shortcut ownership needs to map to those sections rather than the older two-segment split button.

The current local IntelliJ Platform 2026.1.1 descriptors assign `CheckinProject` to the commit shortcut and `Vcs.Push` to the push shortcut. The proposal text also mentions shortcut wording such as `Ctrl+K` / `Ctrl+Shift+K` and macOS equivalents, so implementation should follow IDE action IDs and keymap equivalents rather than hard-coding shortcut text beyond fallback descriptor bindings.

## Decision Drivers

* Make the plugin feel first-class for users who rely on keyboard commit workflows.
* Preserve the accepted `AI`, `Commit`, and `Push` section semantics from ADR 0052.
* Preserve safe push behavior from ADR 0047.
* Avoid permanently rewriting user keymaps.
* Give users a clear opt-out path from shortcut takeover.
* Keep standard IDE actions reachable when shortcut takeover is disabled.

## Considered Options

* Take over commit and push shortcuts by default with settings opt-out
* Add plugin shortcuts only when users opt in
* Leave IDE commit and push shortcuts untouched

## Decision Outcome

Chosen option: "Take over commit and push shortcuts by default with settings opt-out", because shortcut takeover makes `AI Commit All` the primary commit workflow while the opt-out keeps standard IDE muscle memory recoverable.

If accepted, implementation should add keyboard-targetable plugin actions that dispatch to the same workflow sections as the visual control:

* The action using the IDE commit shortcut should run the `Commit` section behavior.
* The action using the IDE push shortcut should run the `Push` section behavior.
* The `AI` section should not receive a standard VCS shortcut unless a later decision assigns one.

Shortcut bindings should follow the IDE actions rather than fixed human-readable key text:

* `CheckinProject` is the source action for commit shortcut equivalence.
* `Vcs.Push` is the source action for push shortcut equivalence where that VCS action exists.
* Platform, OS, and keymap variants should be inherited or mirrored only when the IntelliJ action system can do so predictably.

Add a settings option under `Settings | Tools | AI Commit All`, enabled by default, for using `AI Commit All` on IDE commit and push shortcuts. When the option is disabled, pressing those shortcuts must execute the standard IDE action behavior rather than the plugin workflow. The implementation may achieve that by delegating to the original IDE action, by avoiding active plugin shortcut registration, or by another action-system approach that leaves user keymaps intact.

### Consequences

* Good, because keyboard users can trigger the AI-backed commit and push workflows without reaching for the mouse.
* Good, because the setting provides a clear restoration path for users who prefer standard IDE behavior.
* Good, because the decision maps shortcuts to `Commit` and `Push` sections without inventing a shortcut for the `AI` preparation-only section.
* Bad, because registering shortcut-equivalent plugin actions may expose IntelliJ action-system conflicts that need careful testing.
* Bad, because opt-out behavior must be verified in the sandbox, not only by descriptor tests.

### Confirmation

Compliance should be checked by implementation review and validation that covers:

* Shortcut action registration for the `Commit` and `Push` section workflows.
* Default enabled setting dispatching the commit shortcut to `Commit`.
* Default enabled setting dispatching the push shortcut to `Push`.
* Disabled setting preserving or delegating to standard IDE commit and push behavior.
* Documentation of the setting and user-visible shortcut behavior.
* Manual sandbox validation for the default Windows/Linux keymap and the macOS keymap equivalents available in the supported IDE line.

## Pros and Cons of the Options

### Take over commit and push shortcuts by default with settings opt-out

* Good, because it makes the plugin the default keyboard path for the workflow it replaces.
* Good, because it keeps the pre-release user experience aligned with the toolbar control.
* Good, because users can disable the takeover without manually editing every keymap entry.
* Bad, because shortcut conflicts and fallback delegation are more complex than static action registration.

### Add plugin shortcuts only when users opt in

* Good, because it avoids surprising users who expect standard IDE behavior.
* Good, because implementation could avoid some shortcut conflict risk.
* Bad, because new users may never discover the plugin keyboard workflow.
* Bad, because the accepted proposal asks for takeover with an opt-out, not a hidden opt-in.

### Leave IDE commit and push shortcuts untouched

* Good, because it is the least risky action-system change.
* Good, because standard IDE muscle memory remains untouched.
* Bad, because `PROP-02` `E006` remains unresolved.
* Bad, because the plugin would feel secondary even after becoming the primary Commit tool window control.

## More Information

- Source proposal: `docs/proposals/archive/PROP-02-pre-release-ux-2026-05-15T09-57.md` `E006`.
- Related behavior ADRs: ADR 0052 for the three-section control and ADR 0047 for safe push fallback.
- Implementation must wait for maintainer acceptance of this ADR under the ADR gating rule in `docs/decisions/README.md`.
