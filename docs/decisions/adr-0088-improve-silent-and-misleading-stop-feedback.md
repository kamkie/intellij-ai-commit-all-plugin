---
status: accepted
date: 2026-06-11
accepted_at: 2026-06-11T23:25:00+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Claude
informed: Repository contributors
---

# Improve Silent And Misleading Stop Feedback

## Context and Problem Statement

ADR 0016 limits plugin-owned notifications: stop reasons covered by standard IntelliJ messages reuse them (`REQ-ERR-002`), and most other stop reasons stay silent (`REQ-ERR-004`), with the AI timeout warning as the only plugin-owned notification body (`REQ-ERR-003`, ADR 0014).

The June 2026 IDE-log investigation (plugin `0.1.0-beta.3`) diagnosed two stop paths where this policy harms users:

1. When Git staging-area confirmation fails after all bounded attempts, the workflow stops with `UnsupportedWorkflow`, which `REQ-ERR-004` keeps silent. The user watches ~9 seconds of staging activity that ends with no commit, no message, and no log pointer — perceived as a "never ending stage" hang. Three such silent stops occurred in one week.
2. When JetBrains AI Assistant's generation times out internally (`com.intellij.ml.llm.InlayGenerationTimeout`, typically on large diffs of 39–234 paths), the commit message stays empty and the workflow stops with `EmptyMessage`, surfacing the standard "empty commit message" error. The text is technically true but misleading: the cause is AI generation not finishing, not a message problem the user created. Nine such stops occurred in one week.

How should these two diagnosed stop paths report themselves so the user knows what failed and what to do?

## Decision Drivers

* A silent stop after visible staging activity is indistinguishable from a hang; the maintainer had to mine IDE logs to learn why commits failed.
* The empty-message error sends users to inspect their message instead of retrying generation or reducing the change size.
* ADR 0016's goal — do not duplicate or replace IDE-owned UI — remains valid; both gaps concern states where the IDE shows nothing or shows a misleading platform text.
* Stop reasons are a fixed specification set (`docs/specification.md` Section 10.1); distinguishing a new failure cause requires a specification change.
* Diagnostics already exist internally (`ReflectiveCommitWorkflowSynchronizer` logs "staging state confirmation failed"); only the user-facing surface is missing.

## Considered Options

* Add a distinct staging-confirmation stop reason with a plugin-owned warning, and report AI-caused empty messages with the AI-generation warning
* Notify on every `UnsupportedWorkflow` stop without adding a stop reason
* Keep the current notification policy (status quo)

## Decision Outcome

Chosen option: "Add a distinct staging-confirmation stop reason with a plugin-owned warning, and report AI-caused empty messages with the AI-generation warning", because both diagnosed paths are states where the IDE owns no truthful message, which is exactly the case `REQ-ERR-003` reserves for plugin-owned notifications, and because a distinct stop reason keeps `UnsupportedWorkflow` meaningful for genuinely unsupported workflow types.

If accepted:

* A new workflow stop reason `StagingConfirmationFailed` is added to the fixed set in `docs/specification.md` Section 10.1: triggered when staging-state confirmation fails after the bounded confirmation window; final after bounded settling. The staging-confirmation failure path stops with this reason instead of `UnsupportedWorkflow`.
* `StagingConfirmationFailed` MUST surface a plugin-owned warning notification through the `AI Commit All` group, stating that staging could not be confirmed for the selected changes and suggesting a refresh/retry. The notification SHOULD include the unconfirmed path count from the existing diagnostics.
* `EmptyMessage` stops that follow an observed AI generation run MUST surface the plugin-owned AI-generation warning (extending the ADR 0014 timeout text with a hint that generation did not produce a message and that large changes are a known trigger) instead of the standard `error.no.commit.message` text. Empty messages caused by user edits remain governed by `UserEditedMessage` (unchanged).
* `REQ-ERR-002`, `REQ-ERR-003`, and `REQ-ERR-004` are updated accordingly; all other stop reasons keep their current silent or standard-message behavior.
* This narrows ADR 0016 for these two stop paths only; no plugin-owned confirmation dialogs are added (ADR 0017 unchanged).

### Consequences

* Good, because a failed staging confirmation becomes a visible, explained stop instead of a perceived hang.
* Good, because AI-caused empty messages point the user at generation (retry, smaller change) instead of at their commit message.
* Good, because `UnsupportedWorkflow` regains a single meaning: the workflow type itself cannot be driven.
* Bad, because the fixed stop-reason set, stop reporter, tests, and specification rows must change together.
* Bad, because two new plugin-owned notification bodies must be maintained and localized consistently with the existing AI timeout text.

### Confirmation

Compliance is confirmed when:

* `docs/specification.md` Section 10.1 lists `StagingConfirmationFailed`; `REQ-ERR-002..004` reference this ADR; `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.
* The staging-confirmation failure path reports `StagingConfirmationFailed` with a warning notification in `SCN-STAGE-AUT-*` coverage.
* An empty message after observed AI generation produces the AI-generation warning in `SCN-AI-*` coverage, while a user-cleared message still reports `UserEditedMessage` silently.

## Pros and Cons of the Options

### Add a distinct staging-confirmation stop reason with a plugin-owned warning, and report AI-caused empty messages with the AI-generation warning

* Good, because each diagnosed failure gets a truthful, actionable message at the moment it happens.
* Good, because the stop-reason taxonomy stays precise for future log triage.
* Bad, because it is the largest specification change of the options.

### Notify on every `UnsupportedWorkflow` stop without adding a stop reason

* Good, because it is a one-line reporter change.
* Bad, because genuinely unsupported workflow types (for example, non-Git or modal commit flows) would warn on every attempt, where silence plus IDE-owned UI is correct.
* Bad, because one stop reason would keep covering two unrelated failure causes, making future diagnostics ambiguous.

### Keep the current notification policy (status quo)

* Good, because no change is needed.
* Bad, because the most damaging field failure — silent staging stops — remains a perceived hang that requires IDE-log archaeology to diagnose.

## More Information

- Narrows ADR 0016 for the two diagnosed paths; extends the ADR 0014 plugin-owned AI warning. Stop-reason settling discipline from ADR 0084 is unchanged.
- Evidence: June 2026 IDE-log investigation; silent staging stops at 2026-06-08 22:51 and 2026-06-11 00:54 (×2); `InlayGenerationTimeout`-correlated `EmptyMessage` stops on 2026-06-03, 2026-06-04, 2026-06-08, and 2026-06-10.
- Companion draft plan: `PLAN-workflow-stop-feedback-and-push-alignment` (tasks `T3-staging-confirmation-failure-stop-reason`, `T4-empty-ai-message-timeout-notification`). Implementation is blocked until this ADR is accepted and the plan is explicitly approved.
- The staging-confirmation root cause itself (HEAD-identical phantom paths) is fixed separately by plan task `T2`, which needs no ADR; this ADR only governs how residual failures report themselves.

{After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date.}
