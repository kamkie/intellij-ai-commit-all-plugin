---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Stop On Runtime AI Failure With Standard Notification

## Context and Problem Statement

JetBrains AI Assistant is a required plugin dependency, but runtime failure states can still occur after the dependency is present.

Examples include the user not being signed in, AI Assistant service unavailability, generation failure, timeout, or an empty or invalid generated message.

The user selected the fail-closed option for `Q-AI-4`: stop without committing. The user also requested button animation and standard IntelliJ notification for this path.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Stop On Runtime AI Failure With Standard Notification
* Fall back to a manual or existing commit-message flow.
* Use custom failure UI.

## Decision Outcome

Chosen option: "Adopt Stop On Runtime AI Failure With Standard Notification", because Do not fall back to a non-AI or manual commit-message flow.

Do not fall back to a non-AI or manual commit-message flow.

When AI Assistant is present but cannot complete message generation successfully at runtime, the plugin must stop without committing or pushing.

During AI generation, show progress or activity state through the split button using IntelliJ-standard button/progress animation where available.

For runtime AI failure paths, report the stop with a standard IntelliJ notification.

Notification and error message policy is covered by ADR 0016. Detailed split-button styling remains a separate UX decision.

### Consequences

- Runtime AI failures are fail-closed.
- No generated message means no automated commit or push.
- The implementation needs a visible in-progress state while AI generation is running.
- The implementation should use IntelliJ Platform notification APIs instead of custom notification UI.
- Exact notification copy and detailed styling can be decided later without changing the fail-closed behavior.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Stop On Runtime AI Failure With Standard Notification

* Good, because Do not fall back to a non-AI or manual commit-message flow.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Fall back to a manual or existing commit-message flow.

* Bad, because the user selected stop without committing.

### Use custom failure UI.

* Bad, because the user requested standard IntelliJ notification.

## More Information

- Remove `Q-AI-4` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` to add button animation/progress state and standard IntelliJ notifications for runtime AI failures.
- Use ADR 0016 for notification and error message policy.
- Keep detailed split-button styling open.
