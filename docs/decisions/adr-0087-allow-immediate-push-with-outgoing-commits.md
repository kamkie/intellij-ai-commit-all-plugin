---
status: proposed
date: 2026-06-11
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Claude
informed: Repository contributors
---

# Allow Immediate Push With Existing Outgoing Commits

## Context and Problem Statement

ADR 0047 allows the `Push` workflow to skip the IDE Push Commits dialog only when push safety is verified. The shipped commit-and-push implementation verifies, among other conditions, that the local branch head exactly matches the tracked upstream head before the commit (`REQ-PUSH-002`, `localMatchesTrackedUpstream` in `SafeImmediatePushDecisionPolicy`). Any mismatch falls back with `ForcePushStateUnverified` and opens the IDE Push Commits dialog.

Outgoing-only `Push` (no committable changes) deliberately skips this verification (`REQ-PUSH-003`, `requireTrackedUpstreamHeadMatch=false`) and pushes an already-ahead local branch silently.

The June 2026 IDE-log investigation (IntelliJ 2026.2 EAP and WebStorm 2026.1.3, plugin `0.1.0-beta.3`) found that 8 of 22 commit-and-push runs in IntelliJ and 5 of 11 in WebStorm fell back to the Push Commits dialog. Every verified fallback was caused by pre-existing unpushed local commits with zero incoming remote commits — the normal state after earlier `Commit`-only runs. The user experiences this as the dialog opening for no reason.

Should commit-and-push immediate push tolerate existing outgoing commits the same way outgoing-only push already does?

## Decision Drivers

* The exact-head-match requirement turns the most common mixed workflow (several `Commit` runs, then `Push`) into a dialog interruption.
* A normal non-force push to the tracked upstream cannot rewrite remote history; the server rejects non-fast-forward pushes.
* Outgoing-only `Push` already pushes an ahead local branch silently, so the plugin is inconsistent across the two `Push` paths.
* Push failures already surface through standard IDE push error UI and the plugin's push-completion observation (`REQ-PUSH-009`).
* Keep all other ADR 0047 safety conditions intact.

## Considered Options

* Align with outgoing-only push: drop the pre-commit head-match verification for commit-and-push
* Ancestry-aware check: allow immediate push only when the tracked upstream head is an ancestor of the local head
* Keep the exact head-match requirement (status quo)

## Decision Outcome

Chosen option: "Align with outgoing-only push: drop the pre-commit head-match verification for commit-and-push", because the verification adds no safety to a normal non-force push — the Git server already rejects non-fast-forward pushes and the rejection surfaces through standard IDE error UI — while it breaks the one-click `Push` promise in the most common local-ahead state, and because it removes the unexplained behavioral asymmetry between the commit-and-push and outgoing-only paths.

If accepted:

* Commit-and-push immediate push no longer requires the local branch head to match the tracked upstream head (`requireTrackedUpstreamHeadMatch` behavior aligns with the outgoing-only path).
* The `ForcePushStateUnverified` fallback reason is removed from `SafeImmediatePushDecisionPolicy` and from the `REQ-PUSH-005` reason set, because no remaining condition produces it.
* All other ADR 0047 conditions remain required: tracked upstream exists, target is the unambiguous tracked upstream, target is not a new branch or special ref, repository state is `NORMAL`, no unresolved conflicts, and push spec availability.
* The push remains a normal non-force push. Force push is never performed on this path.
* Non-fast-forward rejections (remote moved ahead) surface through the standard IDE push result UI, observed by the existing push-completion path (`REQ-PUSH-009`); the plugin does not add custom retry or custom error text (ADR 0016).

### Consequences

* Good, because `Push` after earlier `Commit`-only runs pushes silently instead of opening the Push Commits dialog with nothing to review.
* Good, because commit-and-push and outgoing-only push share one upstream policy, making behavior predictable and the policy easier to test.
* Good, because the diagnosed dialog fallbacks from the June 2026 investigation disappear without weakening force-push protection.
* Bad, because when the remote actually moved ahead, the user learns about the rejection after commit-and-push instead of seeing the dialog up front and the push must be retried after pull/rebase.
* Bad, because `REQ-PUSH-002` and `REQ-PUSH-005` and their tests must be reworked.

### Confirmation

Compliance is confirmed when:

* `SafeImmediatePushDecisionPolicy` no longer evaluates `localMatchesTrackedUpstream` for commit-and-push, and `ForcePushStateUnverified` no longer exists in the policy or logs.
* `docs/specification.md` `REQ-PUSH-002` drops the commit-and-push head-match clause and `REQ-PUSH-005` drops `ForcePushStateUnverified` from the fallback reason set, both sourcing this ADR.
* A local-ahead commit-and-push scenario takes the immediate push path in `SCN-PUSH-*` automated coverage; a diverged-remote scenario still surfaces the standard push failure UI.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.

## Pros and Cons of the Options

### Align with outgoing-only push: drop the pre-commit head-match verification for commit-and-push

* Good, because it removes the dominant cause of unexpected Push Commits dialogs found in field logs.
* Good, because it matches the already-accepted reasoning of `REQ-PUSH-003` for outgoing-only push.
* Good, because it simplifies the policy instead of adding a second verification mode.
* Bad, because a genuinely diverged remote is discovered at push time rather than before commit.

### Ancestry-aware check: allow immediate push only when the tracked upstream head is an ancestor of the local head

* Good, because it distinguishes local-ahead (safe) from diverged (needs attention) before committing.
* Bad, because it needs additional Git ancestry queries on the decision path that the cached repository info does not provide, adding latency and new failure modes.
* Bad, because the cached remote-branch hash can be stale, reintroducing spurious fallbacks the investigation diagnosed.
* Bad, because it keeps two different upstream policies between the commit-and-push and outgoing-only paths.

### Keep the exact head-match requirement (status quo)

* Good, because no implementation or specification change is needed.
* Bad, because the most common mixed workflow keeps hitting the Push Commits dialog with zero incoming commits.
* Bad, because the asymmetry with outgoing-only push remains unexplained to users.

## More Information

- Amends ADR 0047 verification semantics for commit-and-push; ADR 0047 fallback design otherwise remains in force. ADR 0069 (outgoing-only stop) is unaffected.
- Evidence: June 2026 IDE-log investigation; fallbacks at 2026-06-10 11:04 and 15:18 (`jit-ops-coordination`) reflog-verified as local-ahead-only states.
- Companion draft plan: `PLAN-workflow-stop-feedback-and-push-alignment` (task `T5-allow-immediate-push-with-outgoing-commits`). Implementation is blocked until this ADR is accepted and the plan is explicitly approved.

{After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date.}
