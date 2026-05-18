---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T19:36:57+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Safe Immediate Push Fallback

## Context and Problem Statement

ADR 0017 chose standard IDE confirmation barriers for the first implementation and explicitly avoided bypassing commit or push executor confirmation behavior.

`PROP-02-pre-release-ux` `E002` accepts a narrower pre-release UX direction: the `& Push` split-button segment should push without opening the standard Push Commits dialog only when repository state is safe and unambiguous, and should fall back to the standard dialog otherwise.

This decision defines whether the plugin may add that safe immediate-push path before Marketplace publication.

## Decision Drivers

* Preserve IDE-owned safeguards for risky or ambiguous push states.
* Keep the `& Push` segment useful as a one-click workflow when the push target is already clear.
* Avoid custom confirmation dialogs that duplicate IntelliJ UI.
* Keep multi-root, missing-upstream, conflict, and force-push cases conservative.
* Make the behavior testable before implementation starts.

## Considered Options

* Push immediately only when safe, otherwise use the standard push dialog
* Always use the standard push dialog
* Push immediately for all `& Push` executions

## Decision Outcome

Chosen option: "Push immediately only when safe, otherwise use the standard push dialog", because the plugin can reduce one-click friction in simple Git states while preserving standard IDE confirmation and error handling for ambiguous or risky push states.

If accepted, the `& Push` flow may skip the standard Push Commits dialog only when implementation can verify all of these conditions:

* Every affected Git repository has a tracked upstream branch.
* No affected repository requires force push.
* No unresolved conflicts are present in the affected commit scope.
* The target push operation is unambiguous across all affected Git roots.
* The plugin can still surface standard IntelliJ, Git, VCS, and push errors without replacing them with less precise custom messages.

If any condition cannot be verified, the plugin must fall back to the existing standard push dialog path.

The implementation must not add a plugin-owned confirmation dialog for this path. If implementation reveals a risk not covered by these conditions or by the standard dialog fallback, record a new open question before continuing that part of the work.

### Consequences

* Good, because simple `& Push` executions become closer to one click after AI commit-message generation.
* Good, because ambiguous multi-root and missing-upstream cases keep the existing IDE confirmation path.
* Good, because ADR 0017 remains valid for unsafe or unverifiable states.
* Bad, because implementation must inspect push safety state before choosing the executor path.
* Bad, because some users will still see the standard push dialog when the plugin cannot prove the state is safe.

### Confirmation

Compliance should be checked by implementation review and targeted validation that covers at least:

* Safe single-root push with a tracked upstream.
* Missing upstream fallback to the standard push dialog.
* Multi-root ambiguity fallback to the standard push dialog.
* Conflict or force-push-required fallback where such states can be produced safely.
* Error propagation through standard IntelliJ, Git, VCS, and push UI.

## Pros and Cons of the Options

### Push immediately only when safe, otherwise use the standard push dialog

* Good, because it preserves the one-click value of the `& Push` segment in the common safe case.
* Good, because it leaves risky states on the standard IDE path.
* Neutral, because exact safe-state detection depends on IntelliJ Platform APIs available in the supported IDE line.
* Bad, because it introduces branching behavior that needs focused tests and sandbox validation.

### Always use the standard push dialog

* Good, because it is the simplest behavior and already aligns with ADR 0017.
* Good, because IntelliJ continues to own all push confirmation UI.
* Bad, because it leaves the accepted `PROP-02` friction point unresolved.

### Push immediately for all `& Push` executions

* Good, because it gives the fastest possible `& Push` path.
* Bad, because it would bypass useful IDE confirmation for missing upstreams, multi-root ambiguity, force push, and other risky states.
* Bad, because it conflicts with the safety boundary in ADR 0017.

## More Information

- Narrows ADR 0017 only for safe, verifiable `& Push` states.
- Source proposal: `docs/proposals/archive/PROP-02-pre-release-ux-2026-05-15T09-57.md` `E002`.
- Implementation should happen from an approved plan because this changes plugin behavior and push execution.
