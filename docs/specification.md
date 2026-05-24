# Plugin Behavior Specification

Last updated: 2026-05-24

This document is the validation contract for the intended observable behavior of
the `AI Commit All` IntelliJ plugin. It states what the plugin must do and where
that behavior is validated. ADRs remain the decision authority; this
specification turns accepted decisions into stable requirements for tests,
manual sandbox checks, and future documentation.

## Purpose And Scope

- Describes user-observable IDE behavior and the compatibility policy needed to
  validate it.
- Keeps implementation classes, package structure, reflection mechanics, and
  test-only helpers out of requirement wording unless the detail is itself an
  observable compatibility contract.
- Does not replace user-facing setup, usage, troubleshooting, or support docs.
- Does not replace ADRs in `docs/decisions/`; ADRs remain the authority for
  accepted decisions and supersession history.
- Authority order when conflicts appear: ADRs > approved plans > this
  specification > user-facing docs > validation records.

## Document Conventions

- Each requirement has a stable `REQ-<AREA>-NNN` ref. Keep refs stable when
  wording changes; do not renumber existing refs.
- The keywords `MUST`, `MUST NOT`, `SHOULD`, `SHOULD NOT`, and `MAY` follow
  RFC 2119 / RFC 8174.
- `Source:` lists the originating ADR, accepted plan, task ref, or descriptor
  contract.
- `Implements:` lists the primary task ref from `TASKS_ARCHIVE.md` when one
  applies.
- `Validates:` lists scenario refs from
  [validation/scenario-register.md](validation/scenario-register.md),
  release checklist refs from
  [validation/release-checklist.md](validation/release-checklist.md),
  or descriptor validation where no scenario row owns the assertion.

## Validation References

- Scenario registry: [validation/scenario-register.md](validation/scenario-register.md).
- Release validation checklist:
  [validation/release-checklist.md](validation/release-checklist.md).
- Behavior decisions: `docs/decisions/adr-NNNN-*.md`.
- Completed implementation tasks: `TASKS_ARCHIVE.md`.

## 1. Plugin Identity And Load Contract

- REQ-ID-001: The plugin ID and base package MUST be `pl.devopssolutions.aicommitall`. Source: ADR 0022. Validates: SCN-CONTROL-AUT-001.
- REQ-ID-002: The plugin MUST declare platform, VCS, Git, and JetBrains AI Assistant dependencies so supported IDEs load it only with the required platform capabilities available. Source: ADR 0013. Validates: SCN-STAGE-MAN-011.
- REQ-ID-003: When JetBrains AI Assistant is missing or disabled, the IDE MUST refuse to load the plugin through the required-dependency mechanism; the plugin MUST NOT fall back to a non-AI commit-message path. Source: ADR 0013. Validates: T-VAL-015 (manual).
- REQ-ID-004: The plugin MUST target the IntelliJ Platform 2026.1 line. Source: ADR 0008. Validates: T-VAL-018 (manual).
- REQ-ID-005: The plugin MUST be licensed under Apache License 2.0 and MUST identify its vendor as `DevOps Solutions Kamil Kiewisz` with email `kontakt@devopssolutions.pl` and URL `https://devopssolutions.pl`. Source: ADR 0018, ADR 0022.
- REQ-ID-006: User-visible plugin, control, notification, and settings labels MUST use the accepted `AI Commit All` product name unless a more specific requirement defines the visible section label. Source: ADR 0005. Validates: SCN-CONTROL-AUT-001, SCN-SETTINGS-AUT-010.

## 2. Commit Tool Window Activation

- REQ-ACT-001: The three-section control MUST be hidden outside an active supported Git commit workflow in the Commit tool window. Source: ADR 0009. Validates: SCN-CONTROL-AUT-007, SCN-CONTROL-AUT-008.
- REQ-ACT-002: Git MUST be the only supported VCS for the first implementation. Non-Git VCS contexts MUST report `UnsupportedVcs` and MUST NOT start AI, commit, or push work. Source: ADR 0009. Validates: SCN-SELECT-*, SCN-WORKFLOW-*.
- REQ-ACT-003: The control MUST be available for both the changelist-backed commit workflow and the Git staging-area commit workflow. Source: ADR 0020. Validates: SCN-STAGE-MAN-001..003, T-VAL-017 (manual).
- REQ-ACT-004: The Commit tool window primary actions MUST show the plugin three-section control without also showing the standard `Commit and Push...` toolbar action. Source: ADR 0070. Validates: SCN-CONTROL-AUT-001, T-IDEA-011 (manual).
- REQ-ACT-005: The control MUST appear in the Commit tool window primary action order after the IDE commit-and-push executor. Source: plugin descriptor. Validates: SCN-CONTROL-AUT-001.

## 3. Three-Section Control

The control surface is `<AI icon> AI | Commit | Push`.

### 3.1 Sections And Routing

- REQ-UI-001: The control MUST expose exactly three sections in the order `AI`, `Commit`, `Push`, with their corresponding labels and the AI icon displayed for the `AI` section. Source: ADR 0015, ADR 0052. Validates: SCN-CONTROL-AUT-002..004.
- REQ-UI-002: Activating the `AI` section MUST start the `AI` workflow mode. Source: ADR 0052. Validates: SCN-CONTROL-AUT-002.
- REQ-UI-003: Activating the `Commit` section MUST start the `Commit` workflow mode. Source: ADR 0052. Validates: SCN-CONTROL-AUT-003.
- REQ-UI-004: Activating the `Push` section MUST start the `Push` workflow mode. Source: ADR 0052. Validates: SCN-CONTROL-AUT-004.
- REQ-UI-005: A fallback action invocation that cannot identify a specific section MUST default to `Commit` workflow mode. Source: PLAN-three-section-ai-commit-push-control. Validates: SCN-CONTROL-AUT-005.
- REQ-UI-006: The control MUST NOT start a workflow when no project is available. Source: ADR 0009. Validates: SCN-CONTROL-AUT-006.

### 3.2 Enabled, Disabled, And Running States

- REQ-UI-007: While no workflow is running and a supported Git workflow is present, section enablement MUST follow this matrix. When committable Git changes exist, `AI` is enabled, `Commit` is enabled only when the IDE commit operation is available, and `Push` is enabled only when the IDE commit-and-push operation is available. When no committable Git changes exist, `AI` and `Commit` are disabled but visible, while `Push` is enabled only when local outgoing commits exist and the push operation is available. Source: ADR 0047, ADR 0052. Validates: SCN-WORKFLOW-*.
- REQ-UI-008: While a plugin-owned workflow is running, every section MUST be disabled for further activation until the workflow completes. Source: PLAN-three-section-ai-commit-push-control. Validates: SCN-CONTROL-AUT-*.
- REQ-UI-009: The currently running section MUST display the running indication. The phase progression MUST follow the chosen workflow mode and MUST NOT advance past it: `Ai` mode keeps the indicator on `AI`; `Commit` mode advances `Ai` -> `Commit` and stops there; `Push` mode advances `Ai` -> `Commit` -> `Push`, where the move to `Push` happens when the push step starts after the commit completes. Source: ADR 0053. Validates: SCN-CONTROL-AUT-014.
- REQ-UI-010: `Push` MUST remain enabled when there are no committable Git changes but the project has outgoing local commits to push, subject to push availability. Source: ADR 0047. Validates: SCN-WORKFLOW-*.
- REQ-UI-011: Rapid repeated activation MUST NOT start multiple concurrent plugin workflows. A later activation while a workflow is in flight MUST leave the original workflow as the only active run. Source: PLAN-ai-generation-completion. Validates: SCN-WORKFLOW-*.

### 3.3 Visual Styling

- REQ-UI-012: The control MUST use the ADR 0053 violet AI, blue Commit, and green Push segmented styling. Source: ADR 0053. Validates: T-VAL-014 (manual).
- REQ-UI-013: Hover styling MUST be cumulative: hovering `AI` highlights only `AI`; hovering `Commit` highlights `AI` and `Commit`; hovering `Push` highlights `AI`, `Commit`, and `Push`. Source: ADR 0052. Validates: SCN-CONTROL-AUT-010..013.
- REQ-UI-014: Light and dark themes MUST both render the passive, hover, clicked, running, disabled, and divider-shade states described in ADR 0053. Source: ADR 0053. Validates: T-VAL-014 (manual).
- REQ-UI-015: Inactive section dividers MUST use the active-active, active-passive, and passive-passive shade variants from the styling reference. Source: ADR 0053. Validates: SCN-CONTROL-AUT-*.
- REQ-UI-016: The control's corner radius MUST match the IDE button arc so it aligns with neighbouring Commit toolbar buttons. Source: T-UI-003 (archived). Validates: T-VAL-014 (manual).
- REQ-UI-017: The control MUST expose accessible name and description text for assistive technology that conveys the active section and overall control purpose. Source: PLAN-three-section-ai-commit-push-control. Validates: SCN-CONTROL-AUT-*.

## 4. Change Selection

The selection is the set of files acted on by a workflow run.

- REQ-SEL-001: Selection MUST include every modified, added, deleted, moved, renamed, and unversioned non-ignored Git path that the IDE exposes as committable across all changelists and all Git roots. Source: ADR 0003. Implements: T-FILES-001, T-FILES-002, T-FILES-006, T-FILES-007. Validates: SCN-SELECT-*, T-VAL-005..010 (manual).
- REQ-SEL-002: Resolved-conflict paths MUST be included when the IDE exposes them as committable. Source: ADR 0003. Implements: T-FILES-003. Validates: SCN-SELECT-*.
- REQ-SEL-003: Ignored files MUST NOT be included. Source: ADR 0003. Validates: T-VAL-011 (manual), SCN-STAGE-AUT-015.
- REQ-SEL-004: When the Git staging area workflow is active, the workflow MUST stage every eligible unstaged non-ignored path before invoking AI generation and MUST confirm that every intended path is staged so the IDE commit operation commits the intended content. Source: ADR 0020. Validates: SCN-STAGE-AUT-001..026.
- REQ-SEL-005: Already-staged paths MUST remain staged when additional unstaged paths are selected for staging, and already-staged deleted or renamed paths MUST NOT be re-added as stale pathspecs after they are staged. Source: ADR 0020. Validates: SCN-STAGE-AUT-016..017, SCN-STAGE-AUT-021..022, SCN-STAGE-AUT-025..026.
- REQ-SEL-006: Multi-root and nested-module paths MUST be grouped per Git root and MUST NOT be merged or lost across roots. Source: ADR 0009. Validates: SCN-STAGE-AUT-004..005.
- REQ-SEL-007: Equivalent path spellings that differ only by slash direction MUST be treated as the same path so the selection is not duplicated. Source: PLAN-include-all-git-files. Validates: SCN-STAGE-AUT-006.
- REQ-SEL-008: The workflow MUST NOT invoke AI generation until staging state confirms that every expected path is staged; if confirmation cannot be reached within the bounded staging-confirmation window, the workflow MUST fail closed. Source: PLAN-confirm-staged-before-ai-generation. Validates: SCN-STAGE-AUT-007..014, SCN-STAGE-AUT-020.
- REQ-SEL-009: When the IDE changelist state is frozen, the workflow MUST stop before staging mutation and MUST report `VcsFrozen`. Source: ADR 0016. Validates: SCN-WORKFLOW-*.
- REQ-SEL-010: When a background VCS operation is already running, the workflow MUST stop before staging mutation and MUST report `VcsBackgroundOperationRunning`. Source: ADR 0016. Validates: SCN-WORKFLOW-*, SCN-STAGE-MAN-018.
- REQ-SEL-011: When the selection is empty after collection, the workflow MUST stop and MUST report `EmptySelection`. Source: ADR 0016. Validates: SCN-WORKFLOW-*, SCN-STAGE-MAN-017.

## 5. AI Section Behavior

- REQ-AI-001: The `AI` workflow MUST collect the selection per Section 4, activate the non-modal commit workflow, prepare the staging area when applicable, capture the current commit message as a snapshot, invoke AI message generation, wait for completion, and stop without committing. Source: ADR 0052. Implements: T-AI-007. Validates: SCN-AI-*, T-VAL-023 (manual).
- REQ-AI-002: AI Assistant action discovery MUST locate the dedicated commit-message generation action by preferring the stable commit-message action ID, accepting compatible VCS commit-message generation actions, and excluding reword, rewrite, and conflict-resolution actions. Source: PLAN-ai-assistant-message-generation. Implements: T-AI-001..003. Validates: SCN-AI-*.
- REQ-AI-003: AI Assistant invocation MUST receive the active project, commit workflow, and visible commit-message control needed to generate into the current commit message field. Source: PLAN-ai-assistant-message-generation. Implements: T-AI-004. Validates: SCN-AI-*.
- REQ-AI-004: When the AI Assistant action cannot be discovered or invoked, the workflow MUST stop and MUST report `MissingAiAction`. Source: ADR 0014. Validates: SCN-AI-*, T-VAL-016 (manual).
- REQ-AI-005: When `clearCommitMessageBeforeGeneration` is enabled, the commit-message control and underlying document MUST be cleared before AI generation begins. Source: PLAN-ai-generation-completion. Validates: SCN-SETTINGS-*.
- REQ-AI-006: AI generation activity MUST start in the `Ai` phase. The phase MUST advance according to the workflow mode as defined in REQ-UI-009 so the running indicator and workflow progress stay synchronized. Source: ADR 0053. Validates: SCN-CONTROL-AUT-014.
- REQ-AI-007: AI Assistant sign-in, unavailable, and other generation messages MUST be surfaced through AI Assistant's standard UI where the IDE supports it, without being replaced by plugin-owned text. Source: ADR 0014. Implements: T-AI-006. Validates: T-VAL-016 (manual).

### 5.1 Completion Detection

- REQ-AI-008: Completion detection MUST treat AI generation as complete only when the AI action signal reports stopped and the current commit message is non-empty and either changed relative to the captured snapshot or, after the plugin observed the action run and then stop, unchanged from a non-empty prefilled snapshot that was intentionally preserved because `clearCommitMessageBeforeGeneration` was disabled. Source: ADR 0012, ADR 0081. Implements: T-WAIT-001..004. Validates: SCN-AI-*.
- REQ-AI-009: Commit-message polling MUST run at the configured `completionCheckIntervalMillis` interval. Source: ADR 0012. Validates: SCN-SETTINGS-*.
- REQ-AI-010: When the user edits or clears the commit message during generation, the workflow MUST stop without committing or pushing and MUST report `UserEditedMessage`. Source: ADR 0011. Implements: T-WAIT-007. Validates: SCN-AI-*, SCN-STAGE-MAN-014.
- REQ-AI-011: When AI generation does not finish within `aiGenerationTimeoutMillis`, the workflow MUST stop without committing or pushing and MUST report `AiTimeout`. Source: ADR 0012. Implements: T-WAIT-006. Validates: SCN-AI-*, SCN-STAGE-MAN-012.
- REQ-AI-012: When AI generation completes but the resulting message is empty, the workflow MUST stop and MUST report `EmptyMessage`. Source: ADR 0014. Validates: SCN-AI-*, SCN-STAGE-MAN-013.
- REQ-AI-013: When AI generation completes but the message has not changed from the snapshot, the workflow MUST stop and MUST report `UnchangedMessage` unless the unchanged message is a non-empty prefilled snapshot accepted under REQ-AI-008. Unchanged empty snapshots MUST remain unsuccessful. Source: ADR 0014, ADR 0081. Validates: SCN-AI-*.
- REQ-AI-014: When the plugin cannot reliably determine whether AI Assistant generation is still running or has completed, the workflow MUST stop and MUST report `NoCompletionSignal`. This is distinct from `AiTimeout`, which is reported when the running-state signal remains readable but the timeout window elapses. Source: ADR 0012. Validates: SCN-AI-*.

## 6. Commit Section Behavior

- REQ-COM-001: The `Commit` workflow MUST perform every step required by Section 5 and then commit through the active IntelliJ commit workflow. Source: ADR 0010, ADR 0052. Implements: T-COMMIT-006. Validates: SCN-WORKFLOW-*, T-VAL-012 (manual).
- REQ-COM-002: The commit step MUST run the IDE before-commit checks; it MUST NOT bypass commit confirmations, warnings, or errors. Source: ADR 0017. Implements: T-COMMIT-003, T-COMMIT-004. Validates: SCN-WORKFLOW-*, SCN-STAGE-MAN-015.
- REQ-COM-003: When the commit executor is not available for the current workflow state, the workflow MUST stop and MUST report `CommitExecutionUnavailable`. Source: ADR 0016. Validates: SCN-WORKFLOW-*.
- REQ-COM-004: Commit failure paths MUST forward the IDE-provided platform message; the plugin MUST NOT replace them with custom retry loops or custom error text. Source: ADR 0016. Implements: T-ERROR-006. Validates: SCN-WORKFLOW-*.

## 7. Push Section Behavior

- REQ-PUSH-001: The `Push` workflow MUST perform every step required by Section 6 when committable changes exist, and then push the resulting commit. When no committable changes exist but local outgoing commits exist, `Push` MUST push the outgoing commits without performing an AI generation or commit step. Source: ADR 0047, ADR 0052. Implements: T-COMMIT-007. Validates: SCN-WORKFLOW-*, T-VAL-013 (manual).

### 7.1 Safe Immediate Push

- REQ-PUSH-002: Safe immediate push MUST be used, skipping the IDE Push Commits dialog, only when ALL of the following are verified for every affected Git repository: the current branch has a tracked upstream; the target push is unambiguous; the target is the tracked upstream; the target is not a new branch or special ref; the repository state is `NORMAL`; no unresolved conflicts are present in the affected commit scope; and, for commit-and-push, the local branch matches the tracked upstream before commit. Source: ADR 0047. Validates: SCN-PUSH-*.
- REQ-PUSH-003: Outgoing-only `Push` MUST NOT require local-matches-upstream verification, allowing an already-ahead local branch to push. Source: ADR 0047, alpha.9 fix. Validates: SCN-PUSH-*.
- REQ-PUSH-004: Protected branch settings that prohibit force push MUST NOT by themselves force the IDE Push dialog when the push is a normal non-force push. Source: ADR 0047. Validates: T-BUG-015 (manual).
- REQ-PUSH-005: When any verification in REQ-PUSH-002 fails for committable changes, push MUST fall back to the IDE commit-and-push executor and Push Commits dialog, and the fallback reason MUST be one of: `NoAffectedRepositories`, `MissingAffectedRepository`, `UnsafeRepositoryState`, `UnresolvedConflict`, `MissingTrackedUpstream`, `ForcePushStateUnverified`, `AmbiguousTarget`, `UnsupportedPushApi`. Source: ADR 0047. Validates: SCN-PUSH-*.
- REQ-PUSH-006: Outgoing-only `Push` MUST NOT open the IDE Push Commits dialog when safe immediate push cannot be prepared; instead the workflow MUST stop. Source: ADR 0069. Validates: SCN-PUSH-*.
- REQ-PUSH-007: A plugin-owned confirmation dialog MUST NOT be added for the safe immediate push path. Source: ADR 0047. Validates: SCN-PUSH-*.
- REQ-PUSH-008: When the push executor is unavailable, the workflow MUST stop and MUST report `PushExecutionUnavailable`. Source: ADR 0016. Validates: SCN-WORKFLOW-*.
- REQ-PUSH-009: The running indicator MUST remain active until safe immediate push observes a successful per-repository completion, cancellation, failed push result, or bounded missing-event timeout; it MUST NOT stop immediately after the push request is dispatched or treat a failed completed push as successful. Source: alpha.8 fix, PLAN-maintainability-stability-audit. Validates: SCN-CONTROL-AUT-014, SCN-PUSH-AUT-016..018.

## 8. Shortcut Takeover

- REQ-SHC-001: The plugin MUST register two shortcut-target actions: a commit shortcut action whose default keystroke mirrors `CheckinProject` (`Ctrl+K` on the default Windows/Linux keymap), and a push shortcut action whose default keystroke mirrors `Vcs.Push` (`Ctrl+Shift+K` on the default Windows/Linux keymap). Source: ADR 0054. Validates: SCN-SHORTCUT-*.
- REQ-SHC-002: When `useVcsShortcutsForAiCommitAll` is enabled by default and the Commit tool window workflow is available, the IDE commit shortcut MUST execute the plugin `Commit` workflow and the IDE push shortcut MUST execute the plugin `Push` workflow. Source: ADR 0054. Validates: SCN-SHORTCUT-*, ADR-0054-1, ADR-0054-2.
- REQ-SHC-003: When `useVcsShortcutsForAiCommitAll` is disabled or no plugin workflow is available, the IDE commit and push shortcuts MUST execute the standard IDE actions (`CheckinProject` and `Vcs.Push`). Source: ADR 0054. Validates: SCN-SHORTCUT-*, ADR-0054-3, ADR-0054-4.
- REQ-SHC-004: An action promoter MUST promote the plugin shortcut actions over the mirrored IDE actions only when shortcut takeover is available. Source: ADR 0054. Validates: SCN-SHORTCUT-*.
- REQ-SHC-005: The `AI` section MUST NOT receive a standard VCS shortcut. Source: ADR 0054. Validates: SCN-SHORTCUT-*.
- REQ-SHC-006: Plugin shortcut actions MUST NOT permanently overwrite the user's keymap; opt-out MUST restore the standard IDE behavior without manual keymap edits. Source: ADR 0054. Validates: SCN-SHORTCUT-*.
- REQ-SHC-007: When a plugin workflow is already running for the project, the IDE commit and push shortcuts MUST be no-ops while takeover is enabled: they MUST NOT start a second plugin workflow and MUST NOT delegate to the standard IDE action. The plugin shortcut action MUST report itself as disabled during this state. Source: PLAN-ai-generation-completion, ADR 0054. Validates: SCN-SHORTCUT-*.

## 9. Settings

Settings are application-scoped and exposed via `Settings | Tools | AI Commit All`.

| Key                                  | Type    | Default | Constraint       | Effect                                                                                 |
|--------------------------------------|---------|---------|------------------|----------------------------------------------------------------------------------------|
| `aiGenerationTimeoutMillis`          | Long    | `30000` | MUST be positive | Maximum wait time for AI generation before the workflow stops with `AiTimeout`.        |
| `completionCheckIntervalMillis`      | Long    | `500`   | MUST be positive | Polling interval for AI completion detection.                                          |
| `clearCommitMessageBeforeGeneration` | Boolean | `true`  | n/a              | When `true`, the commit message control and document are cleared before AI invocation. When `false`, a non-empty prefilled message may remain unchanged after reliable AI completion. |
| `useVcsShortcutsForAiCommitAll`      | Boolean | `true`  | n/a              | When `true`, the IDE commit and push shortcuts run the plugin workflows.               |

- REQ-SET-001: Both timing values MUST be validated as positive on apply. Non-positive values MUST be rejected or normalized to defaults; the plugin MUST NOT persist non-positive values. Source: ADR 0012, ADR 0068. Validates: SCN-SETTINGS-*.
- REQ-SET-002: Settings MUST persist across IDE restarts. Source: ADR 0054. Validates: SCN-SETTINGS-*.
- REQ-SET-003: A change to `useVcsShortcutsForAiCommitAll` MUST take effect for subsequent shortcut activations without requiring an IDE restart. Source: ADR 0054. Validates: SCN-SHORTCUT-*.
- REQ-SET-004: Default values MUST match the table above. Source: ADR 0012, ADR 0054, ADR 0068, PLAN-ai-generation-completion. Validates: SCN-SETTINGS-*.

## 10. Notifications And Stop Behavior

- REQ-ERR-001: A plugin-owned notification group named `AI Commit All` MUST exist with balloon display behavior and log-by-default behavior. Source: ADR 0014, ADR 0016, plugin descriptor. Validates: plugin descriptor.
- REQ-ERR-002: For workflow stop reasons covered by standard IntelliJ platform messages, the plugin MUST surface the standard platform text rather than custom text. Currently this applies to `EmptySelection` (`error.no.changes.to.commit`) and `EmptyMessage` (`error.no.commit.message`). Source: ADR 0016. Implements: T-ERROR-001, T-ERROR-005. Validates: SCN-WORKFLOW-*.
- REQ-ERR-003: For workflow stop reasons not covered by a standard platform message, the plugin MAY surface a plugin-owned warning notification through the `AI Commit All` group. The only such plugin-owned notification body is the AI timeout text: `AI Assistant did not finish generating a commit message before the configured timeout.` Source: ADR 0014. Implements: T-ERROR-004, T-ERROR-007. Validates: SCN-WORKFLOW-*.
- REQ-ERR-004: Workflow stop reasons `MissingWorkflow`, `UnsupportedVcs`, `UnsupportedWorkflow`, `MissingAiAction`, `AiCompletionFailed`, `UnchangedMessage`, `NoCompletionSignal`, `UserEditedMessage`, `CommitExecutionUnavailable`, `PushExecutionUnavailable`, `VcsFrozen`, and `VcsBackgroundOperationRunning` MUST NOT trigger plugin-owned notifications; their user-visible effect MUST be limited to existing IDE-owned UI or to no notification when the IDE has none for that state. Source: ADR 0016. Validates: SCN-WORKFLOW-*.
- REQ-ERR-005: When a stop occurs, the workflow MUST NOT commit, push, or modify the working copy beyond changes already made before the stop. Source: ADR 0011, ADR 0014. Validates: SCN-WORKFLOW-*, SCN-STAGE-MAN-011..018.

### 10.1 Workflow Stop Reasons

The complete set of workflow stop reasons is fixed. Implementations MUST report
exactly one of these values per stopped run, and MUST NOT add new values without
updating this specification.

| Stop Reason                     | Trigger                                                                        |
|---------------------------------|--------------------------------------------------------------------------------|
| `MissingWorkflow`               | Commit tool window workflow handler or UI was not present.                    |
| `VcsFrozen`                     | IDE changelist state is frozen at selection time.                             |
| `VcsBackgroundOperationRunning` | A background VCS operation is in progress.                                     |
| `EmptySelection`                | After selection collection, no eligible files and no outgoing commits exist.   |
| `UnsupportedVcs`                | The project's active VCS is not Git.                                           |
| `UnsupportedWorkflow`           | The active commit workflow type cannot be driven by the plugin.                |
| `MissingAiAction`               | AI Assistant commit-message action could not be located or invoked.            |
| `AiCompletionFailed`            | AI generation reported a failure condition during completion detection.        |
| `AiTimeout`                     | AI generation did not complete within `aiGenerationTimeoutMillis`.             |
| `EmptyMessage`                  | AI generation produced an empty commit message.                                |
| `UnchangedMessage`              | AI generation produced a message identical to the captured snapshot and the unchanged prefilled-message acceptance path did not apply. |
| `NoCompletionSignal`            | AI generation running-state could not be determined reliably.                  |
| `UserEditedMessage`             | The user edited or cleared the message during AI generation.                   |
| `CommitExecutionUnavailable`    | IDE commit executor is unavailable for the current workflow state.             |
| `PushExecutionUnavailable`      | IDE push executor is unavailable for the current workflow state.               |

## 11. IDE Action Registration Compatibility

- REQ-INT-001: The plugin MUST register the three-section control in the Commit tool window primary action group after the IDE commit-and-push executor. Source: plugin descriptor. Validates: SCN-CONTROL-AUT-001.
- REQ-INT-002: The plugin MUST register shortcut actions for the Commit and Push workflows, mirror `CheckinProject` and `Vcs.Push` via `use-shortcut-of`, and bind the default `$default` shortcuts to `control K` and `control shift K`. Source: ADR 0054. Validates: SCN-SHORTCUT-*.
- REQ-INT-003: The plugin MUST replace, not duplicate, the standard `Commit and Push...` toolbar action while the three-section control is registered. Source: ADR 0070. Validates: T-IDEA-011 (manual).

## 12. Compatibility Surface

- REQ-COMPAT-001: Plugin Verifier MUST run for at least IntelliJ IDEA, PyCharm, and WebStorm at the supported `2026.1.1` build. Source: ADR 0008, README CI section. Validates: T-VAL-002 (automated).
- REQ-COMPAT-002: When a supported IDE compatibility boundary cannot prove the requested inclusion state, the plugin MUST fail closed instead of treating the path as safely included. Source: PLAN-include-all-git-files. Validates: SCN-SELECT-*.
- REQ-COMPAT-003: Background work such as push preparation, outgoing-commit checks, and staging confirmation MUST run off the UI event thread; only commit-UI updates and AI Assistant invocation MUST run on the event dispatch thread. Source: alpha.6/alpha.8 fixes. Validates: SCN-WORKFLOW-*.

## 13. Requirement Traceability

Each behavior ADR, accepted plan, or descriptor contract that owns observable
behavior has at least one requirement here. New behavior MUST extend this
document by adding or updating a `REQ-` row before, or together with, the
behavior change.

### 13.1 ADR Sources

| ADR      | Requirement Refs                                                                                                     |
|----------|----------------------------------------------------------------------------------------------------------------------|
| ADR 0003 | REQ-SEL-001, REQ-SEL-002, REQ-SEL-003                                                                                |
| ADR 0005 | REQ-ID-006                                                                                                           |
| ADR 0008 | REQ-ID-004, REQ-COMPAT-001                                                                                           |
| ADR 0009 | REQ-ACT-001, REQ-ACT-002, REQ-SEL-006, REQ-UI-006                                                                    |
| ADR 0010 | REQ-COM-001                                                                                                          |
| ADR 0011 | REQ-AI-010, REQ-ERR-005                                                                                              |
| ADR 0012 | REQ-AI-008, REQ-AI-009, REQ-AI-011, REQ-AI-014, REQ-SET-001, REQ-SET-004                                             |
| ADR 0013 | REQ-ID-002, REQ-ID-003                                                                                               |
| ADR 0014 | REQ-AI-004, REQ-AI-007, REQ-AI-012, REQ-AI-013, REQ-ERR-001, REQ-ERR-003                                             |
| ADR 0015 | REQ-UI-001                                                                                                           |
| ADR 0016 | REQ-SEL-009, REQ-SEL-010, REQ-SEL-011, REQ-COM-003, REQ-COM-004, REQ-PUSH-008, REQ-ERR-001, REQ-ERR-002, REQ-ERR-004 |
| ADR 0017 | REQ-COM-002                                                                                                          |
| ADR 0018 | REQ-ID-005                                                                                                           |
| ADR 0020 | REQ-ACT-003, REQ-SEL-004, REQ-SEL-005                                                                                |
| ADR 0022 | REQ-ID-001, REQ-ID-005                                                                                               |
| ADR 0047 | REQ-UI-007, REQ-UI-010, REQ-PUSH-001..REQ-PUSH-007                                                                   |
| ADR 0052 | REQ-UI-001..REQ-UI-004, REQ-UI-013, REQ-AI-001, REQ-COM-001, REQ-PUSH-001                                            |
| ADR 0053 | REQ-UI-009, REQ-UI-012, REQ-UI-014, REQ-UI-015, REQ-AI-006                                                           |
| ADR 0054 | REQ-SHC-001..REQ-SHC-007, REQ-SET-002, REQ-SET-003, REQ-SET-004, REQ-INT-002                                         |
| ADR 0068 | REQ-SET-001, REQ-SET-004                                                                                             |
| ADR 0069 | REQ-PUSH-006                                                                                                         |
| ADR 0070 | REQ-ACT-004, REQ-INT-003                                                                                             |
| ADR 0081 | REQ-AI-008, REQ-AI-013                                                                                               |

### 13.2 Non-ADR Sources

| Source                                      | Requirement Refs                                                |
|---------------------------------------------|-----------------------------------------------------------------|
| PLAN-ai-assistant-message-generation        | REQ-AI-002, REQ-AI-003                                          |
| PLAN-ai-generation-completion               | REQ-UI-011, REQ-AI-005, REQ-SHC-007, REQ-SET-004                |
| PLAN-confirm-staged-before-ai-generation    | REQ-SEL-008                                                     |
| PLAN-include-all-git-files                  | REQ-SEL-007, REQ-COMPAT-002                                     |
| PLAN-maintainability-stability-audit        | REQ-PUSH-009                                                    |
| PLAN-three-section-ai-commit-push-control   | REQ-UI-005, REQ-UI-008, REQ-UI-017                              |
| T-UI-003 (archived)                         | REQ-UI-016                                                      |
| alpha.6/alpha.8 fixes                       | REQ-PUSH-009, REQ-COMPAT-003                                    |
| alpha.9 fix                                 | REQ-PUSH-003                                                    |
| plugin descriptor                           | REQ-ACT-005, REQ-ERR-001, REQ-INT-001                           |

## 14. Editing Rules

- Add new requirements with the next free `REQ-<AREA>-NNN` ref in the relevant
  section. Do not reuse retired refs.
- When behavior changes, update the requirement wording in place and add the
  new `Source:` ADR or accepted plan reference rather than removing still-valid
  older sources.
- When a requirement is retired, keep the ref, mark its text with `RETIRED:`,
  and record the retiring ADR or plan.
- Update `Validates:` lines when scenario refs are added in
  [validation/scenario-register.md](validation/scenario-register.md).
- Keep Section 13 in sync when adding requirements or source artifacts.
