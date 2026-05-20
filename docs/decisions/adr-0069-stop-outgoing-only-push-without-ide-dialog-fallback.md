---
status: accepted
date: 2026-05-20
accepted_at: 2026-05-20T04:30:12+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Stop Outgoing-Only Push Without IDE Dialog Fallback

## Context and Problem Statement

ADR 0047 lets the `Push` section take the safe immediate push fast path when every safety condition is verified for every affected Git repository, and otherwise requires fallback to the standard IDE Push Commits dialog.

The outgoing-only `Push` case — no committable changes, but local outgoing commits exist — was added after ADR 0047 (`T-IDEA-010`, alpha.9 release line). In that case, when safe immediate push cannot be prepared (for example because the upstream is missing or the target is ambiguous), opening the Push Commits dialog has poor UX: there is no commit to author and the dialog is dominated by file-tree controls the user does not need. The shipped implementation already stops the workflow instead of opening the dialog, but ADR 0047 does not explicitly carve this case out, leaving `REQ-PUSH-006` sourced from an "Unreleased fix".

## Decision Drivers

* Keep the `Push` section button useful when only outgoing commits exist.
* Avoid opening the standard Push Commits dialog when there is no commit-authoring work the dialog provides.
* Preserve ADR 0047 fallback semantics for the commit-and-push case.
* Avoid surprising users who click `Push` on outgoing-only state and see an unrelated commit dialog.
* Match the behavior already shipped in `v0.1.0-alpha.9`.

## Considered Options

* Stop outgoing-only `Push` when safe immediate push cannot be prepared.
* Fall back to the IDE Push Commits dialog for outgoing-only `Push`, matching ADR 0047 for the commit-and-push case.
* Add a plugin-owned modal confirmation when safe immediate outgoing push cannot be prepared.

## Decision Outcome

Chosen option: "Stop outgoing-only `Push` when safe immediate push cannot be prepared.", because the IDE Push Commits dialog provides no useful workflow when there is nothing to commit, and a plugin-owned modal would duplicate IDE UI that the plugin is otherwise required to avoid.

This decision narrows ADR 0047 only for outgoing-only `Push`. Commit-and-push fallback to the IDE Push Commits dialog (`REQ-PUSH-005`) is unchanged. ADR 0047 otherwise remains in force.

The plugin must still surface relevant errors through the standard IntelliJ notification and log path so the user is not silently abandoned when safe immediate push cannot be prepared. The plugin MUST NOT add a plugin-owned confirmation dialog for this path (consistent with ADR 0017 and ADR 0047).

### Consequences

* Good, because outgoing-only `Push` keeps a clean one-click model when safe immediate push is available.
* Good, because users are not redirected into a commit dialog they cannot use.
* Good, because the ADR matches the shipped behavior and removes the "Unreleased fix" source line in `REQ-PUSH-006`.
* Bad, because outgoing-only `Push` can now end without forward progress when the repository state is unsafe, requiring the user to remediate before retrying.
* Bad, because the contrast between commit-and-push fallback (opens dialog) and outgoing-only-push fallback (stops) needs to be documented for users.

### Confirmation

After acceptance, confirm the behavior through:

* `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/PushOnlyWorkflowExecutionService.kt` returns `CommitWorkflowExecutionResult.UnsupportedExecutor` when the decision is not `Immediate`.
* `docs/specification.md` `REQ-PUSH-006` source line references this ADR (currently: "Unreleased fix").
* `docs/requested-features.md` Push Behavior section reflects this carve-out.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.

## Pros and Cons of the Options

### Stop outgoing-only `Push` when safe immediate push cannot be prepared.

* Good, because it avoids opening the IDE Push Commits dialog when there is nothing to commit.
* Good, because the `Push` section stays a one-click control when conditions are safe and inert when they are not.
* Neutral, because the user must remediate unsafe state through standard Git tools, then retry.

### Fall back to the IDE Push Commits dialog for outgoing-only `Push`, matching ADR 0047 for the commit-and-push case.

* Good, because it would be uniform with ADR 0047 across all `Push` cases.
* Bad, because the dialog adds no value when there is no committable work.
* Bad, because the dialog can mislead users into thinking the plugin staged extra work.

### Add a plugin-owned modal confirmation when safe immediate outgoing push cannot be prepared.

* Good, because the user would get explicit feedback before the workflow stops.
* Bad, because ADR 0017 disallows plugin-owned confirmation dialogs that duplicate IDE UI.
* Bad, because the plugin still cannot recover unsafe state through a confirmation step.

## More Information

- Narrows ADR 0047 only for outgoing-only `Push`.
- Source request: archived `T-IDEA-010` (`Push` enabled for outgoing commits) and the Unreleased fix that prevents the IDE Push dialog for outgoing-only `Push`.
- Existing implementation: `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/PushOnlyWorkflowExecutionService.kt`.
- Existing documentation: `docs/specification.md` `REQ-PUSH-006`; `README.md` "Push fallback" paragraph; `config/intellij-platform/change-notes.html`.
- After acceptance, update the ADR Implementation Tracker in `docs/decisions/README.md`, update the source line of `docs/specification.md` `REQ-PUSH-006`, and update `docs/requested-features.md` Push Behavior section to reflect this narrowing.
