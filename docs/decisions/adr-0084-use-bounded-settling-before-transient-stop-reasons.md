---
status: accepted
date: 2026-05-25
accepted_at: 2026-05-25T00:16:33+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Bounded Settling Before Transient Stop Reasons

## Context and Problem Statement

The specification currently treats several first-observed transient platform states as terminal stop
conditions: frozen VCS operations, background VCS work, empty selection snapshots, missing AI Assistant
actions, unavailable AI progress signals, and unknown push readiness. The implementation follows those
requirements literally, which keeps commit and push safety intact but also makes the plugin give up when
the IDE, VCS log, Commit tool window, or AI Assistant is still settling and would have become usable within
a short bounded window.

How should the plugin distinguish refreshable transient states from genuinely unsafe or unsupported states
without bypassing JetBrains-owned commit, push, and AI Assistant safeguards?

## Decision Drivers

* Preserve fail-closed behavior for commit, push, and AI Assistant uncertainty.
* Reduce false stops caused by eventually consistent IDE, VCS, and AI Assistant state.
* Keep behavior deterministic enough to cover with unit and integration-boundary tests.
* Avoid custom retry prompts, unbounded waits, or bypasses around standard IDE confirmations.
* Keep user-facing stop reasons stable unless a later accepted decision changes the notification contract.

## Considered Options

* Keep immediate stop on first transient state
* Use bounded settling for refreshable transient states
* Wait indefinitely or add custom prompts

## Decision Outcome

Chosen option: "Use bounded settling for refreshable transient states", because it improves reliability without weakening fail-closed safety or IDE-owned commit and push safeguards.

The plugin will treat a refreshable transient state as provisional until a bounded settling budget expires.
If the state clears inside that budget, the workflow continues through the existing safe path. If the state
does not clear, the existing final stop reason remains valid.

This applies to these classes of behavior:

* AI action discovery may retry within a bounded invocation window before reporting `MissingAiAction`.
* AI generation observation may tolerate transient unavailable progress-signal reads within the existing
  completion timeout before reporting `NoCompletionSignal`.
* VCS readiness and selection collection may refresh or retry frozen, background-operation, or empty
  snapshots before reporting `VcsFrozen`, `VcsBackgroundOperationRunning`, or `EmptySelection`, provided no
  staging or commit mutation has started.
* Commit tool window activation and reflective synchronization may retry boundedly before reporting
  `UnsupportedWorkflow`.
* Safe push preparation may distinguish refreshable unknown state from genuinely unsafe state and retry the
  refreshable case before falling back or stopping.
* Toolbar and shortcut action execution should prefer fresh action-time context over stale update-time
  visibility or enabled state when deciding whether to start the plugin workflow or delegate.

Terminal states remain terminal. The plugin must still stop or fall back immediately when it proves an
unsafe push, an unsupported workflow, a missing required dependency, a user-edited commit message, a failed
commit or push, or any other non-refreshable safety barrier.

### Consequences

* Good, because ordinary IDE and VCS settling should no longer cause avoidable plugin stops.
* Good, because the existing stop-reason vocabulary can stay stable while becoming final only after a
  bounded settling attempt.
* Good, because red-first tests can expose the premature-stop cases before implementation changes.
* Bad, because workflow start can take slightly longer when the IDE is busy or metadata is stale.
* Bad, because implementation needs narrow retry seams around several platform integration points.

### Confirmation

Compliance will be confirmed by updating `docs/specification.md` to distinguish provisional transient
states from final stop reasons, then adding red-first tests for each affected integration boundary before
production changes. Validation should include targeted tests for AI invocation, AI completion, VCS readiness,
selection, commit workflow activation and synchronization, push preparation, toolbar routing, shortcut
routing, plus the full test suite and formatting checks.

## Pros and Cons of the Options

### Keep immediate stop on first transient state

* Good, because it is simple and matches the current literal specification.
* Good, because it minimizes workflow latency when the first observed state is truly terminal.
* Bad, because it encodes a reliability bug in the specification: refreshable transient state is treated as
  proof that the workflow cannot proceed.
* Bad, because adding tests for late availability would fail by design unless the specification changes.

### Use bounded settling for refreshable transient states

* Good, because it improves reliability while preserving safe final stop reasons.
* Good, because it can reuse existing timeout and safety concepts instead of adding user-facing prompts.
* Good, because it is testable with deterministic fakes and bounded retry policies.
* Neutral, because timeout values should remain implementation details unless later product requirements
  make them user-facing settings.
* Bad, because it adds retry logic that must avoid masking genuinely unsupported or unsafe states.

### Wait indefinitely or add custom prompts

* Good, because it might recover from longer IDE or VCS operations.
* Bad, because unbounded waits can hang workflows and make validation brittle.
* Bad, because custom prompts would add UX and translation burden without improving the core safety model.
* Bad, because it risks competing with JetBrains-owned commit and push confirmations.

## More Information

This ADR was implemented by
[`PLAN-premature-stop-reliability`](../../.agents/plans/archive/PLAN-premature-stop-reliability.md).
