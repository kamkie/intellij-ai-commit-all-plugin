# Scenario Coverage

Last updated: 2026-05-18

This public project document tracks validation scenarios across the plugin. Use it for feature work, bug fixes, release validation, and coverage planning when a scenario needs an explicit owner, execution mode, status, and evidence target. Add future feature or bug sets as new `SCN-<AREA>` entries instead of creating one-off bug-specific coverage files.

## Counting Rules

- Count each row in the scenario registry as one scenario.
- Count a scenario as `Automated` when a repository test executes the primary assertion, even when the current result is red.
- Count a scenario as `Manual` when it needs the sandbox IDE, Commit tool window, JetBrains AI Assistant, platform commit checks, shortcut routing, push UI/runtime behavior, or product-specific IDE behavior.
- Keep scenario IDs stable after publishing them. Add new IDs for new behavior instead of renumbering existing rows.
- Update the coverage counts whenever rows are added, removed, or moved between automated and manual execution.

## Project Coverage Counts

| Total scenarios | Automated | Manual | Happy path | Failure path | Edge case |
|-----------------|-----------|--------|------------|--------------|-----------|
| 179 | 124 | 55 | 57 | 68 | 54 |

Automated status:

| Status | Count |
|--------|-------|
| Existing automated coverage | 123 |
| Known red automated coverage | 1 |

Manual status:

| Status | Count |
|--------|-------|
| Manual sandbox required | 55 |
| Manual scenarios completed in current cycle | 0 |

## Scenario Sets

| Set ID | Area | Requirement source | Total | Automated | Manual | Notes |
|--------|------|--------------------|-------|-----------|--------|-------|
| SCN-STAGE | Git staging-area workflow | `T-BUG-008` | 33 | 15 | 18 | Covers staging-area disappearance and workflow-stop regression paths. |
| SCN-CONTROL | Three-section control UI | `T-ACTIONS-009`, `T-UI-001`, `T-VAL-023` | 19 | 16 | 3 | Covers action routing, availability, running state, rendering, and toolbar placement. |
| SCN-SHORTCUT | IDE shortcut takeover | `ADR-0054`, `T-ACTIONS-009` | 10 | 6 | 4 | Covers commit and commit-and-push shortcut takeover and opt-out behavior. |
| SCN-AI | AI message generation | `T-AI-*`, `T-WAIT-*`, `ADR-0012`, `ADR-0014` | 40 | 33 | 7 | Covers AI action discovery, invocation context, completion evidence, activity state, and user-edit stops. |
| SCN-SELECT | Change selection and VCS support | `T-FILES-*`, `ADR-0020`, `ADR-0021` | 21 | 15 | 6 | Covers Git filtering, changelists, roots, resolved conflicts, ignored files, and unsupported VCS states. |
| SCN-WORKFLOW | Workflow orchestration and execution | `T-COMMIT-*`, `T-ERROR-*`, `T-VAL-*` | 32 | 24 | 8 | Covers AI, Commit, Push sequencing, executor availability, stop reporting, and VCS readiness. |
| SCN-PUSH | Safe immediate push and fallback | `ADR-0047`, `T-COMMIT-007`, `T-VAL-013` | 12 | 6 | 6 | Covers safe push policy, fallback reasons, and push error handling. |
| SCN-SETTINGS | Plugin settings | `T-WAIT-005`, `T-WAIT-006`, `ADR-0054` | 12 | 9 | 3 | Covers persisted settings, validation, defaults, and runtime effect checks. |

## Scenario Registry

| ID | Set | Path type | Execution | Status | Title | Evidence target |
|----|-----|-----------|-----------|--------|-------|-----------------|
| SCN-STAGE-AUT-001 | SCN-STAGE | Edge case | Automated | Known red | Staging-area-only paths are included in fallback inclusion items. | `CommitWorkflowSelectionItemsTest.keeps staging-area paths in fallback inclusion items` |
| SCN-STAGE-AUT-002 | SCN-STAGE | Happy path | Automated | Existing | Staging-area paths make a collected selection committable. | `GitStageSelectionItemsTest.staging-area paths make a selection committable` |
| SCN-STAGE-AUT-003 | SCN-STAGE | Edge case | Automated | Existing | Staging state keeps changed Git paths and excludes ignored or unchanged paths. | `GitStageSelectionItemsTest.keeps non ignored changed paths from git staging state` |
| SCN-STAGE-AUT-004 | SCN-STAGE | Edge case | Automated | Existing | Staging paths are grouped by Git root. | `GitStageSelectionItemsTest.groups committable staging paths by git root` |
| SCN-STAGE-AUT-005 | SCN-STAGE | Edge case | Automated | Existing | Nested module and product paths are preserved across multiple roots. | `GitStageSelectionItemsTest.groups nested Gradle module and IntelliJ product paths by git root` |
| SCN-STAGE-AUT-006 | SCN-STAGE | Edge case | Automated | Existing | Equivalent slash and backslash path text is deduplicated. | `GitStageSelectionItemsTest.deduplicates committable staging paths by normalized path text` |
| SCN-STAGE-AUT-007 | SCN-STAGE | Failure path | Automated | Existing | Expected paths are reported missing until the index status shows them staged. | `GitStageSelectionItemsTest.confirms expected paths only when they are staged` |
| SCN-STAGE-AUT-008 | SCN-STAGE | Happy path | Automated | Existing | Refreshed staged paths match expected paths by path text. | `GitStageSelectionItemsTest.matches refreshed staged paths by path text` |
| SCN-STAGE-AUT-009 | SCN-STAGE | Happy path | Automated | Existing | Staging confirmation retries until every expected path is staged. | `GitStageConfirmationTest.retries staging reload and tracker refresh until every expected path is staged` |
| SCN-STAGE-AUT-010 | SCN-STAGE | Failure path | Automated | Existing | Staging confirmation fails closed when paths never appear as staged. | `GitStageConfirmationTest.fails closed after the bounded retry count when staged paths never appear` |
| SCN-STAGE-AUT-011 | SCN-STAGE | Failure path | Automated | Existing | Staging command failure is retried before AI generation. | `GitStageConfirmationTest.retries after staging command failure before invoking AI generation` |
| SCN-STAGE-AUT-012 | SCN-STAGE | Failure path | Automated | Existing | External file reload failure is retried before trusting tracker state. | `GitStageConfirmationTest.retries after external file reload failure before trusting tracker state` |
| SCN-STAGE-AUT-013 | SCN-STAGE | Failure path | Automated | Existing | Tracker refresh failure is retried before confirming staged state. | `GitStageConfirmationTest.retries after tracker refresh failure and then confirms refreshed state` |
| SCN-STAGE-AUT-014 | SCN-STAGE | Edge case | Automated | Existing | Staging confirmation stops after the first confirmed staged state. | `GitStageConfirmationTest.stops retrying after the first confirmed staged state` |
| SCN-STAGE-AUT-015 | SCN-STAGE | Happy path | Automated | Existing | Local Git repositories cover modified, staged added, deleted, renamed, unversioned, and ignored states. | `LocalGitRepositoryValidationTest.local repositories cover committable file states without ignored files` |
| SCN-STAGE-MAN-001 | SCN-STAGE | Happy path | Manual | Manual sandbox required | `AI` stages every supported file-state path and starts generation. | Sandbox IDE result and `git status --porcelain --ignored` |
| SCN-STAGE-MAN-002 | SCN-STAGE | Happy path | Manual | Manual sandbox required | `Commit` stages every supported file-state path and creates one local commit. | Sandbox IDE result and `git show --name-status --oneline HEAD` |
| SCN-STAGE-MAN-003 | SCN-STAGE | Happy path | Manual | Manual sandbox required | `Push` stages every supported file-state path and pushes to a temporary local remote. | Sandbox IDE result and local/remote branch hashes |
| SCN-STAGE-MAN-004 | SCN-STAGE | Edge case | Manual | Manual sandbox required | Already staged files are not lost when unstaged files are added. | Sandbox IDE result and `git status --porcelain` |
| SCN-STAGE-MAN-005 | SCN-STAGE | Edge case | Manual | Manual sandbox required | All intended files already staged before the run remain visible and staged. | Sandbox IDE result and `git status --porcelain` |
| SCN-STAGE-MAN-006 | SCN-STAGE | Edge case | Manual | Manual sandbox required | No files staged before the run become staged without an empty-list flicker. | Sandbox IDE result and `git status --porcelain` |
| SCN-STAGE-MAN-007 | SCN-STAGE | Edge case | Manual | Manual sandbox required | Multiple Git roots and nested paths stay visible and staged. | Sandbox IDE result and `git status --porcelain` in each root |
| SCN-STAGE-MAN-008 | SCN-STAGE | Edge case | Manual | Manual sandbox required | Resolved-conflict paths marked resolved by the user stay included. | Sandbox IDE result and resolved-conflict path status |
| SCN-STAGE-MAN-009 | SCN-STAGE | Happy path | Manual | Manual sandbox required | Commit shortcut takeover follows the same staging-area behavior as `Commit`. | Sandbox IDE result and resulting commit hash |
| SCN-STAGE-MAN-010 | SCN-STAGE | Happy path | Manual | Manual sandbox required | Commit-and-push shortcut takeover follows the same staging-area behavior as `Push`. | Sandbox IDE result and local/remote branch hashes |
| SCN-STAGE-MAN-011 | SCN-STAGE | Failure path | Manual | Manual sandbox required | Missing or unavailable AI Assistant stops without commit or push after staging preparation. | AI Assistant state, notification/error, and unchanged git log |
| SCN-STAGE-MAN-012 | SCN-STAGE | Failure path | Manual | Manual sandbox required | AI generation timeout or no completion signal stops without commit or push. | Timeout setting, notification/error, and unchanged git log |
| SCN-STAGE-MAN-013 | SCN-STAGE | Failure path | Manual | Manual sandbox required | Empty or unchanged AI-generated message stops without commit or push. | Commit message text, notification/error, and unchanged git log |
| SCN-STAGE-MAN-014 | SCN-STAGE | Failure path | Manual | Manual sandbox required | User edits or clears the message during generation stops without commit or push. | Edited message text, notification/error, and unchanged git log |
| SCN-STAGE-MAN-015 | SCN-STAGE | Failure path | Manual | Manual sandbox required | Before-commit check failure or commit warning does not bypass IDE safeguards. | Failed check/warning evidence and unchanged git log |
| SCN-STAGE-MAN-016 | SCN-STAGE | Failure path | Manual | Manual sandbox required | Unsafe or ambiguous push target falls back without losing staged files. | Push target state, fallback behavior, and staged file list |
| SCN-STAGE-MAN-017 | SCN-STAGE | Failure path | Manual | Manual sandbox required | Empty change set stops before AI generation, commit, or push. | Empty Commit tool window state and unchanged git log |
| SCN-STAGE-MAN-018 | SCN-STAGE | Failure path | Manual | Manual sandbox required | Frozen or background-running VCS operation stops before staging mutation. | VCS operation state, notification/error, and unchanged git status |
| SCN-CONTROL-AUT-001 | SCN-CONTROL | Happy path | Automated | Existing | Three-section control is registered after the IDE Commit and Push action. | `PluginActionRegistrationTest.three-section control is placed after commit and push` |
| SCN-CONTROL-AUT-002 | SCN-CONTROL | Happy path | Automated | Existing | `AI` section starts AI workflow mode. | `AiCommitAllActionsTest.ai section starts ai workflow mode` |
| SCN-CONTROL-AUT-003 | SCN-CONTROL | Happy path | Automated | Existing | `Commit` section starts Commit workflow mode. | `AiCommitAllActionsTest.commit section starts commit workflow mode` |
| SCN-CONTROL-AUT-004 | SCN-CONTROL | Happy path | Automated | Existing | `Push` section starts Push workflow mode. | `AiCommitAllActionsTest.push section starts push workflow mode` |
| SCN-CONTROL-AUT-005 | SCN-CONTROL | Edge case | Automated | Existing | Fallback action invocation starts Commit workflow mode. | `AiCommitAllActionsTest.fallback action invocation starts commit workflow mode` |
| SCN-CONTROL-AUT-006 | SCN-CONTROL | Failure path | Automated | Existing | Action invocation without a project does not start a workflow. | `AiCommitAllActionsTest.action does not start without project` |
| SCN-CONTROL-AUT-007 | SCN-CONTROL | Happy path | Automated | Existing | Enabled availability makes the control visible and enabled. | `AiCommitAllActionsTest.action update applies enabled availability` |
| SCN-CONTROL-AUT-008 | SCN-CONTROL | Failure path | Automated | Existing | Disabled availability makes the control visible but disabled. | `AiCommitAllActionsTest.action update applies disabled availability` |
| SCN-CONTROL-AUT-009 | SCN-CONTROL | Failure path | Automated | Existing | Missing project hides and disables the control. | `AiCommitAllActionsTest.action update hides without project` |
| SCN-CONTROL-AUT-010 | SCN-CONTROL | Happy path | Automated | Existing | Custom component exposes `AI`, `Commit`, and `Push` in order. | `AiCommitAllActionsTest.custom component exposes three ordered sections` |
| SCN-CONTROL-AUT-011 | SCN-CONTROL | Edge case | Automated | Existing | Custom component disables only unavailable sections. | `AiCommitAllActionsTest.custom component disables unavailable section only` |
| SCN-CONTROL-AUT-012 | SCN-CONTROL | Edge case | Automated | Existing | Hover highlights sections cumulatively. | `AiCommitAllActionsTest.custom component highlights sections cumulatively` |
| SCN-CONTROL-AUT-013 | SCN-CONTROL | Edge case | Automated | Existing | Running AI keeps the inactive Commit/Push divider passive. | `AiCommitAllActionsTest.custom component keeps inactive divider passive while ai is running` |
| SCN-CONTROL-AUT-014 | SCN-CONTROL | Edge case | Automated | Existing | Fully highlighted sections use matching active dividers. | `AiCommitAllActionsTest.custom component uses matching active dividers when all sections are highlighted` |
| SCN-CONTROL-AUT-015 | SCN-CONTROL | Happy path | Automated | Existing | Segmented control paints nonblank output. | `AiCommitAllActionsTest.custom component paints segmented control` |
| SCN-CONTROL-AUT-016 | SCN-CONTROL | Edge case | Automated | Existing | Running activity disables all sections and highlights through the running section. | `AiCommitAllActionsTest.action update disables all sections while running` |
| SCN-CONTROL-MAN-001 | SCN-CONTROL | Edge case | Manual | Manual sandbox required | Light and dark themes render passive, disabled, cumulative hover, clicked, and running states. | Sandbox screenshots or visual confirmation |
| SCN-CONTROL-MAN-002 | SCN-CONTROL | Happy path | Manual | Manual sandbox required | The control appears in the real Commit tool window after the IDE `Commit and Push...` control. | Sandbox IDE toolbar observation |
| SCN-CONTROL-MAN-003 | SCN-CONTROL | Failure path | Manual | Manual sandbox required | The control hides or disables in real IDE states with no project, no commit workflow, empty changes, or unavailable executors. | Sandbox IDE visibility and enabled-state observations |
| SCN-SHORTCUT-AUT-001 | SCN-SHORTCUT | Happy path | Automated | Existing | Commit shortcut starts Commit workflow when takeover is enabled. | `AiCommitAllShortcutActionsTest.commit shortcut starts commit workflow when takeover is enabled` |
| SCN-SHORTCUT-AUT-002 | SCN-SHORTCUT | Happy path | Automated | Existing | Commit-and-push shortcut starts Push workflow when takeover is enabled. | `AiCommitAllShortcutActionsTest.push shortcut starts push workflow when takeover is enabled` |
| SCN-SHORTCUT-AUT-003 | SCN-SHORTCUT | Edge case | Automated | Existing | Shortcut action is disabled when takeover setting is off. | `AiCommitAllShortcutActionsTest.shortcut update is disabled when setting is off` |
| SCN-SHORTCUT-AUT-004 | SCN-SHORTCUT | Happy path | Automated | Existing | Shortcut delegates to the source IDE action when takeover setting is off. | `AiCommitAllShortcutActionsTest.shortcut delegates to source action when setting is off` |
| SCN-SHORTCUT-AUT-005 | SCN-SHORTCUT | Happy path | Automated | Existing | Promoter promotes the plugin shortcut and suppresses the matching source action. | `AiCommitAllShortcutActionsTest.promoter promotes available plugin shortcut and suppresses matching source action` |
| SCN-SHORTCUT-AUT-006 | SCN-SHORTCUT | Edge case | Automated | Existing | Promoter leaves IDE source actions alone when takeover setting is off. | `AiCommitAllShortcutActionsTest.promoter leaves source action alone when setting is off` |
| SCN-SHORTCUT-MAN-001 | SCN-SHORTCUT | Happy path | Manual | Manual sandbox required | IDE commit shortcut runs the plugin Commit workflow with takeover enabled. | Keymap name, generated message, commit hash |
| SCN-SHORTCUT-MAN-002 | SCN-SHORTCUT | Happy path | Manual | Manual sandbox required | IDE commit-and-push shortcut runs the plugin Push workflow with takeover enabled. | Keymap name, local remote path, commit and remote hashes |
| SCN-SHORTCUT-MAN-003 | SCN-SHORTCUT | Happy path | Manual | Manual sandbox required | Disabling takeover returns commit and commit-and-push shortcuts to standard IDE actions. | Keymap name, setting value, standard action behavior |
| SCN-SHORTCUT-MAN-004 | SCN-SHORTCUT | Edge case | Manual | Manual sandbox required | Missing or unusual keymap shortcuts do not make plugin actions intercept unrelated commands. | Keymap name and action lookup observation |
| SCN-AI-AUT-001 | SCN-AI | Happy path | Automated | Existing | Known AI Assistant commit-message action ID is preferred. | `AiCommitMessageActionDiscoveryServiceTest.prefers the known AI Assistant commit message action id` |
| SCN-AI-AUT-002 | SCN-AI | Edge case | Automated | Existing | Prefixed action IDs are used when the exact known ID is unavailable. | `AiCommitMessageActionDiscoveryServiceTest.uses prefixed action ids when the exact known id is unavailable` |
| SCN-AI-AUT-003 | SCN-AI | Edge case | Automated | Existing | Registered VCS action presentation text is used as a fallback. | `AiCommitMessageActionDiscoveryServiceTest.falls back to registered VCS action presentation text` |
| SCN-AI-AUT-004 | SCN-AI | Edge case | Automated | Existing | Matching action IDs win over generic presentation text during fallback search. | `AiCommitMessageActionDiscoveryServiceTest.uses matching action ids in presentation search before presentation text` |
| SCN-AI-AUT-005 | SCN-AI | Failure path | Automated | Existing | AI reword action is not accepted as the commit-message generator. | `AiCommitMessageActionDiscoveryServiceTest.does not use the AI reword action as the commit message generator` |
| SCN-AI-AUT-006 | SCN-AI | Happy path | Automated | Existing | AI action data context contains commit workflow data. | `AiCommitMessageActionInvocationContextFactoryTest.adds commit workflow data to the AI action data context` |
| SCN-AI-AUT-007 | SCN-AI | Edge case | Automated | Existing | Parent commit-message control is preserved when workflow UI exposes only public text access. | `AiCommitMessageActionInvocationContextFactoryTest.preserves parent commit message control when workflow UI has only the public text accessor` |
| SCN-AI-AUT-008 | SCN-AI | Edge case | Automated | Existing | Commit-message UI editor document is used when available. | `AiCommitMessageActionInvocationContextFactoryTest.uses commit message UI editor document when available` |
| SCN-AI-AUT-009 | SCN-AI | Happy path | Automated | Existing | Discovered AI action is invoked through the action system with commit workflow context. | `AiCommitMessageActionInvokerTest.invokes discovered action through the action system with commit workflow context` |
| SCN-AI-AUT-010 | SCN-AI | Failure path | Automated | Existing | Missing workflow returns before AI action discovery. | `AiCommitMessageActionInvokerTest.returns missing workflow before invoking action discovery` |
| SCN-AI-AUT-011 | SCN-AI | Failure path | Automated | Existing | Missing AI action returns without invoking the action system. | `AiCommitMessageActionInvokerTest.returns missing action without invoking the action system` |
| SCN-AI-AUT-012 | SCN-AI | Edge case | Automated | Existing | Stale commit message is cleared before capturing the AI initial snapshot. | `AiCommitMessagePreparationTest.clears stale commit message before capturing initial snapshot` |
| SCN-AI-AUT-013 | SCN-AI | Edge case | Automated | Existing | Stale commit message is preserved when clearing is disabled. | `AiCommitMessagePreparationTest.preserves stale commit message when clearing is disabled` |
| SCN-AI-AUT-014 | SCN-AI | Happy path | Automated | Existing | Running AI activity is tracked until its token closes. | `AiGenerationActivityStateServiceTest.tracks running activity until token closes` |
| SCN-AI-AUT-015 | SCN-AI | Edge case | Automated | Existing | Requested activity phase is tracked. | `AiGenerationActivityStateServiceTest.tracks requested activity phase` |
| SCN-AI-AUT-016 | SCN-AI | Edge case | Automated | Existing | Closing the activity token is idempotent. | `AiGenerationActivityStateServiceTest.closing activity token is idempotent` |
| SCN-AI-AUT-017 | SCN-AI | Happy path | Automated | Existing | Actions refresh when activity starts and finishes. | `AiGenerationActivityStateServiceTest.refreshes actions when activity starts and finishes` |
| SCN-AI-AUT-018 | SCN-AI | Edge case | Automated | Existing | Running activity applies animated disabled presentation. | `AiGenerationActivityStateServiceTest.applies animated disabled presentation while running` |
| SCN-AI-AUT-019 | SCN-AI | Happy path | Automated | Existing | Idle presentation is restored when no activity is running. | `AiGenerationActivityStateServiceTest.restores idle presentation when not running` |
| SCN-AI-AUT-020 | SCN-AI | Happy path | Automated | Existing | AI completion succeeds only after action stops and message changes. | `AiGenerationCompletionObserverTest.completes after action stops and message is changed` |
| SCN-AI-AUT-021 | SCN-AI | Failure path | Automated | Existing | AI completion times out while the action is still running. | `AiGenerationCompletionObserverTest.times out while action is still running` |
| SCN-AI-AUT-022 | SCN-AI | Failure path | Automated | Existing | AI completion fails closed when action stops with unchanged message. | `AiGenerationCompletionObserverTest.fails closed when action stops with unchanged message` |
| SCN-AI-AUT-023 | SCN-AI | Failure path | Automated | Existing | AI completion fails closed when action stops with an empty message. | `AiGenerationCompletionObserverTest.fails closed when action stops with empty message` |
| SCN-AI-AUT-024 | SCN-AI | Failure path | Automated | Existing | Message polling alone is not enough completion evidence. | `AiGenerationCompletionObserverTest.does not treat message polling alone as completion evidence` |
| SCN-AI-AUT-025 | SCN-AI | Failure path | Automated | Existing | User edits during generation fail closed. | `AiGenerationCompletionObserverTest.fails closed when user edits message during generation` |
| SCN-AI-AUT-026 | SCN-AI | Edge case | Automated | Existing | Document change from the commit-message editor is marked as user edit. | `CommitMessageUserEditSignalTest.marks document change as user edit when current event comes from commit message editor` |
| SCN-AI-AUT-027 | SCN-AI | Edge case | Automated | Existing | Document changes without editor input event are ignored. | `CommitMessageUserEditSignalTest.ignores document changes without editor input event` |
| SCN-AI-AUT-028 | SCN-AI | Edge case | Automated | Existing | Input events outside the commit-message editor are ignored. | `CommitMessageUserEditSignalTest.ignores input events outside commit message editor` |
| SCN-AI-AUT-029 | SCN-AI | Edge case | Automated | Existing | Text actions from nested editor components count as commit-message edits. | `CommitMessageUserEditSignalTest.accepts text actions from nested editor components` |
| SCN-AI-AUT-030 | SCN-AI | Happy path | Automated | Existing | Reflective action progress reports running when indicator is running. | `ReflectiveActionProgressRunningSignalTest.reports running when action progress indicator is running` |
| SCN-AI-AUT-031 | SCN-AI | Edge case | Automated | Existing | Reflective action progress reports not running when indicator is stopped. | `ReflectiveActionProgressRunningSignalTest.reports not running when action progress indicator is stopped` |
| SCN-AI-AUT-032 | SCN-AI | Edge case | Automated | Existing | Reflective action progress reports not running before indicator creation. | `ReflectiveActionProgressRunningSignalTest.reports not running when action progress indicator has not been created` |
| SCN-AI-AUT-033 | SCN-AI | Failure path | Automated | Existing | Reflective action progress reports unavailable without the progress field. | `ReflectiveActionProgressRunningSignalTest.reports unavailable when action has no progress indicator field` |
| SCN-AI-MAN-001 | SCN-AI | Happy path | Manual | Manual sandbox required | Signed-in AI Assistant generates a commit message from the active Commit tool window context. | Sandbox IDE generated message and AI Assistant UI state |
| SCN-AI-MAN-002 | SCN-AI | Failure path | Manual | Manual sandbox required | Missing or disabled AI Assistant dependency fails installation or plugin loading. | Plugin manager or IDE log evidence |
| SCN-AI-MAN-003 | SCN-AI | Failure path | Manual | Manual sandbox required | AI Assistant unavailable or signed out stops without commit or push. | AI Assistant state, notification/error, unchanged git log |
| SCN-AI-MAN-004 | SCN-AI | Edge case | Manual | Manual sandbox required | AI action discovery still works when JetBrains action IDs or presentation text differ within supported versions. | Product/build, discovered behavior, generated message |
| SCN-AI-MAN-005 | SCN-AI | Failure path | Manual | Manual sandbox required | Runtime AI generation timeout stops without commit or push. | Timeout setting, notification/error, unchanged git log |
| SCN-AI-MAN-006 | SCN-AI | Edge case | Manual | Manual sandbox required | Clear-before-generation setting changes real commit-message field behavior. | Setting value, before/after message text |
| SCN-AI-MAN-007 | SCN-AI | Failure path | Manual | Manual sandbox required | User edit during real AI generation stops the workflow without commit or push. | Edited message text, notification/error, unchanged git log |
| SCN-SELECT-AUT-001 | SCN-SELECT | Happy path | Automated | Existing | Git-backed tracked changes are accepted. | `GitChangeSelectionFiltersTest.accepts git backed tracked changes` |
| SCN-SELECT-AUT-002 | SCN-SELECT | Failure path | Automated | Existing | Non-Git tracked changes are rejected. | `GitChangeSelectionFiltersTest.rejects non git tracked changes` |
| SCN-SELECT-AUT-003 | SCN-SELECT | Edge case | Automated | Existing | Mixed-VCS move changes are rejected. | `GitChangeSelectionFiltersTest.rejects mixed vcs move changes` |
| SCN-SELECT-AUT-004 | SCN-SELECT | Edge case | Automated | Existing | Ignored tracked paths are rejected. | `GitChangeSelectionFiltersTest.rejects ignored tracked paths` |
| SCN-SELECT-AUT-005 | SCN-SELECT | Happy path | Automated | Existing | Non-ignored Git file paths are accepted. | `GitChangeSelectionFiltersTest.accepts non ignored git file paths` |
| SCN-SELECT-AUT-006 | SCN-SELECT | Edge case | Automated | Existing | Ignored Git file paths are rejected. | `GitChangeSelectionFiltersTest.rejects ignored git file paths` |
| SCN-SELECT-AUT-007 | SCN-SELECT | Failure path | Automated | Existing | Non-Git file paths are rejected. | `GitChangeSelectionFiltersTest.rejects non git file paths` |
| SCN-SELECT-AUT-008 | SCN-SELECT | Happy path | Automated | Existing | Projects with only Git VCS roots are supported. | `GitVcsSupportTest.supports projects with only Git VCS roots` |
| SCN-SELECT-AUT-009 | SCN-SELECT | Failure path | Automated | Existing | Projects without active VCS roots stop as unsupported. | `GitVcsSupportTest.stops projects without active VCS roots` |
| SCN-SELECT-AUT-010 | SCN-SELECT | Failure path | Automated | Existing | Projects with only non-Git VCS roots stop as unsupported. | `GitVcsSupportTest.stops projects with only non Git VCS roots` |
| SCN-SELECT-AUT-011 | SCN-SELECT | Failure path | Automated | Existing | Mixed VCS projects with unsupported root names stop as unsupported. | `GitVcsSupportTest.stops mixed VCS projects with unsupported root names` |
| SCN-SELECT-AUT-012 | SCN-SELECT | Happy path | Automated | Existing | All changelists containing selected tracked changes are kept. | `CommitWorkflowSelectionItemsTest.keeps all changelists that contain selected tracked changes` |
| SCN-SELECT-AUT-013 | SCN-SELECT | Happy path | Automated | Existing | Tracked, unversioned, and resolved-conflict items are included together. | `CommitWorkflowSelectionItemsTest.combines tracked unversioned and resolved-conflict items for inclusion` |
| SCN-SELECT-AUT-014 | SCN-SELECT | Happy path | Automated | Existing | Compatible changelist workflow handlers synchronize inclusion state. | `ReflectiveCommitWorkflowSynchronizerTest.synchronizes compatible commit workflow handlers` |
| SCN-SELECT-AUT-015 | SCN-SELECT | Failure path | Automated | Existing | Changelist workflow synchronization fails closed when inclusion methods are absent. | `ReflectiveCommitWorkflowSynchronizerTest.fails closed when workflow handler has no inclusion methods` |
| SCN-SELECT-MAN-001 | SCN-SELECT | Happy path | Manual | Manual sandbox required | Changelist-backed workflow includes modified, deleted, renamed, unversioned, and resolved paths while excluding ignored files. | Commit tool window before/after inclusion state |
| SCN-SELECT-MAN-002 | SCN-SELECT | Edge case | Manual | Manual sandbox required | Files across multiple changelists are included. | Changelist names and inclusion state |
| SCN-SELECT-MAN-003 | SCN-SELECT | Edge case | Manual | Manual sandbox required | Files across multiple Git roots are included in the real Commit tool window. | Root paths and inclusion state |
| SCN-SELECT-MAN-004 | SCN-SELECT | Failure path | Manual | Manual sandbox required | Unsupported or non-Git project state hides or stops the workflow without selection mutation. | Project VCS state and notification/error |
| SCN-SELECT-MAN-005 | SCN-SELECT | Edge case | Manual | Manual sandbox required | Changelists disabled and Git staging disabled fail closed or use the supported workflow without data loss. | IDE VCS settings and inclusion result |
| SCN-SELECT-MAN-006 | SCN-SELECT | Edge case | Manual | Manual sandbox required | Resolved conflict paths marked resolved by the user are included, while unresolved conflicts remain guarded by IDE behavior. | Resolved path status and Commit tool window state |
| SCN-WORKFLOW-AUT-001 | SCN-WORKFLOW | Happy path | Automated | Existing | AI mode prepares shared selection, invokes AI, and does not commit. | `AiCommitAllWorkflowRunnerTest.ai mode prepares the shared selection before AI generation and does not commit` |
| SCN-WORKFLOW-AUT-002 | SCN-WORKFLOW | Happy path | Automated | Existing | Commit mode reuses shared preparation and commits only after AI generation completes. | `AiCommitAllWorkflowRunnerTest.commit mode reuses the shared preparation and commits only after AI generation completes` |
| SCN-WORKFLOW-AUT-003 | SCN-WORKFLOW | Happy path | Automated | Existing | Push mode reuses shared preparation and pushes only after AI generation completes. | `AiCommitAllWorkflowRunnerTest.push mode reuses the shared preparation and pushes only after AI generation completes` |
| SCN-WORKFLOW-AUT-004 | SCN-WORKFLOW | Failure path | Automated | Existing | Selection failure stops before AI generation or commit execution. | `AiCommitAllWorkflowRunnerTest.selection failure stops before AI generation or commit execution` |
| SCN-WORKFLOW-AUT-005 | SCN-WORKFLOW | Happy path | Automated | Existing | Default commit starts through the workflow executor listener. | `CommitWorkflowExecutionServiceTest.starts default commit through workflow executor listener` |
| SCN-WORKFLOW-AUT-006 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit execution stops when workflow is missing. | `CommitWorkflowExecutionServiceTest.stops when workflow is missing` |
| SCN-WORKFLOW-AUT-007 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit execution stops when the default executor listener is absent. | `CommitWorkflowExecutionServiceTest.stops when workflow does not expose default executor listener` |
| SCN-WORKFLOW-AUT-008 | SCN-WORKFLOW | Failure path | Automated | Existing | Default commit execution failures are not swallowed. | `CommitWorkflowExecutionServiceTest.does not catch default commit execution failures` |
| SCN-WORKFLOW-AUT-009 | SCN-WORKFLOW | Happy path | Automated | Existing | Commit-and-push starts through the Git commit-and-push executor. | `CommitWorkflowExecutionServiceTest.starts commit and push through Git commit and push executor` |
| SCN-WORKFLOW-AUT-010 | SCN-WORKFLOW | Happy path | Automated | Existing | Safe immediate push starts through default commit and post-commit push listener. | `CommitWorkflowExecutionServiceTest.starts safe immediate push through default commit and post-commit push listener` |
| SCN-WORKFLOW-AUT-011 | SCN-WORKFLOW | Edge case | Automated | Existing | Commit-and-push falls back to Git executor when safe immediate push is unavailable. | `CommitWorkflowExecutionServiceTest.falls back to Git commit and push executor when safe immediate push is unavailable` |
| SCN-WORKFLOW-AUT-012 | SCN-WORKFLOW | Edge case | Automated | Existing | Commit-and-push falls back when post-commit listener cannot be registered. | `CommitWorkflowExecutionServiceTest.falls back to Git commit and push executor when post-commit listener cannot be registered` |
| SCN-WORKFLOW-AUT-013 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit-and-push stops when workflow is missing. | `CommitWorkflowExecutionServiceTest.stops commit and push when workflow is missing` |
| SCN-WORKFLOW-AUT-014 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit-and-push stops when Git executor is missing. | `CommitWorkflowExecutionServiceTest.stops commit and push when Git commit and push executor is missing` |
| SCN-WORKFLOW-AUT-015 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit-and-push stops when Git executor is disabled. | `CommitWorkflowExecutionServiceTest.stops commit and push when Git commit and push executor is disabled` |
| SCN-WORKFLOW-AUT-016 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit-and-push does not execute when executor becomes disabled before scheduled execution. | `CommitWorkflowExecutionServiceTest.does not execute commit and push when executor becomes disabled before scheduled execution` |
| SCN-WORKFLOW-AUT-017 | SCN-WORKFLOW | Failure path | Automated | Existing | Commit-and-push execution failures are not swallowed. | `CommitWorkflowExecutionServiceTest.does not catch commit and push execution failures` |
| SCN-WORKFLOW-AUT-018 | SCN-WORKFLOW | Failure path | Automated | Existing | Empty selection is reported with a standard VCS message. | `AiCommitAllWorkflowStopReporterTest.reports empty selection with standard VCS message` |
| SCN-WORKFLOW-AUT-019 | SCN-WORKFLOW | Failure path | Automated | Existing | AI timeout is reported with plugin-owned timeout message. | `AiCommitAllWorkflowStopReporterTest.reports AI timeout with plugin owned timeout message` |
| SCN-WORKFLOW-AUT-020 | SCN-WORKFLOW | Failure path | Automated | Existing | Empty generated commit message is reported with a standard VCS message. | `AiCommitAllWorkflowStopReporterTest.reports empty generated commit message with standard VCS message` |
| SCN-WORKFLOW-AUT-021 | SCN-WORKFLOW | Edge case | Automated | Existing | Platform-owned stop reasons are not reported by plugin-owned notifications. | `AiCommitAllWorkflowStopReporterTest.does not report stop reasons owned by platform workflow paths` |
| SCN-WORKFLOW-AUT-022 | SCN-WORKFLOW | Happy path | Automated | Existing | VCS operation guard is ready when VCS is not frozen or busy. | `VcsOperationReadinessServiceTest.is ready when VCS is not frozen or busy` |
| SCN-WORKFLOW-AUT-023 | SCN-WORKFLOW | Failure path | Automated | Existing | Frozen changelist manager stops the workflow. | `VcsOperationReadinessServiceTest.stops when changelist manager is frozen` |
| SCN-WORKFLOW-AUT-024 | SCN-WORKFLOW | Failure path | Automated | Existing | Background VCS operation stops and reports. | `VcsOperationReadinessServiceTest.stops and reports when background VCS operation is running` |
| SCN-WORKFLOW-MAN-001 | SCN-WORKFLOW | Happy path | Manual | Manual sandbox required | `AI` end-to-end generates a message and leaves git log unchanged. | Generated message, unchanged commit hash |
| SCN-WORKFLOW-MAN-002 | SCN-WORKFLOW | Happy path | Manual | Manual sandbox required | `Commit` end-to-end generates a message and creates one local commit. | Generated message and commit hash |
| SCN-WORKFLOW-MAN-003 | SCN-WORKFLOW | Happy path | Manual | Manual sandbox required | `Push` end-to-end generates, commits, and reaches push behavior. | Generated message, commit hash, push result |
| SCN-WORKFLOW-MAN-004 | SCN-WORKFLOW | Failure path | Manual | Manual sandbox required | IDE before-commit checks and warnings remain active and block when appropriate. | Check or warning evidence and unchanged git log |
| SCN-WORKFLOW-MAN-005 | SCN-WORKFLOW | Failure path | Manual | Manual sandbox required | Platform commit errors are surfaced without plugin masking. | Platform error and unchanged git log |
| SCN-WORKFLOW-MAN-006 | SCN-WORKFLOW | Failure path | Manual | Manual sandbox required | Platform push errors are surfaced or delegated without plugin masking. | Platform push error and branch hashes |
| SCN-WORKFLOW-MAN-007 | SCN-WORKFLOW | Failure path | Manual | Manual sandbox required | Unsupported commit workflow reflection boundary fails closed. | IDE build, workflow state, notification/error |
| SCN-WORKFLOW-MAN-008 | SCN-WORKFLOW | Failure path | Manual | Manual sandbox required | Real frozen or background-running VCS operation stops before mutation. | VCS state, notification/error, unchanged git status |
| SCN-PUSH-AUT-001 | SCN-PUSH | Happy path | Automated | Existing | Immediate push is allowed when every repository state is safe. | `SafeImmediatePushDecisionPolicyTest.allows immediate push when every repository state is safe` |
| SCN-PUSH-AUT-002 | SCN-PUSH | Failure path | Automated | Existing | Push falls back when no affected repository can be resolved. | `SafeImmediatePushDecisionPolicyTest.falls back when no affected repository can be resolved` |
| SCN-PUSH-AUT-003 | SCN-PUSH | Failure path | Automated | Existing | Push falls back when selected changes contain unresolved conflicts. | `SafeImmediatePushDecisionPolicyTest.falls back when selected changes contain unresolved conflicts` |
| SCN-PUSH-AUT-004 | SCN-PUSH | Failure path | Automated | Existing | Push falls back when a repository has no tracked upstream. | `SafeImmediatePushDecisionPolicyTest.falls back when a repository has no tracked upstream` |
| SCN-PUSH-AUT-005 | SCN-PUSH | Failure path | Automated | Existing | Push falls back when force-push safety cannot be proven. | `SafeImmediatePushDecisionPolicyTest.falls back when force-push safety cannot be proven` |
| SCN-PUSH-AUT-006 | SCN-PUSH | Failure path | Automated | Existing | Push falls back when a multi-root target is ambiguous. | `SafeImmediatePushDecisionPolicyTest.falls back when a multi-root target is ambiguous` |
| SCN-PUSH-MAN-001 | SCN-PUSH | Happy path | Manual | Manual sandbox required | Safe tracked-upstream local remote push completes without opening a real remote path. | Local remote path and branch hashes |
| SCN-PUSH-MAN-002 | SCN-PUSH | Failure path | Manual | Manual sandbox required | Missing upstream falls back to IDE commit-and-push behavior without data loss. | Push dialog/fallback evidence |
| SCN-PUSH-MAN-003 | SCN-PUSH | Failure path | Manual | Manual sandbox required | Diverged local/upstream state falls back instead of immediate push. | Local/upstream hashes and fallback evidence |
| SCN-PUSH-MAN-004 | SCN-PUSH | Failure path | Manual | Manual sandbox required | Protected, new, or special-ref target falls back to platform behavior. | Target state and fallback evidence |
| SCN-PUSH-MAN-005 | SCN-PUSH | Failure path | Manual | Manual sandbox required | Non-normal repository state falls back before immediate push. | Repository state and fallback evidence |
| SCN-PUSH-MAN-006 | SCN-PUSH | Failure path | Manual | Manual sandbox required | Real push failure is surfaced by platform push handling. | Push error and branch hashes |
| SCN-SETTINGS-AUT-001 | SCN-SETTINGS | Happy path | Automated | Existing | Accepted AI completion defaults are used. | `AiCommitAllSettingsTest.uses accepted AI completion defaults` |
| SCN-SETTINGS-AUT-002 | SCN-SETTINGS | Edge case | Automated | Existing | Invalid persisted values normalize to defaults. | `AiCommitAllSettingsTest.normalizes invalid persisted values to defaults` |
| SCN-SETTINGS-AUT-003 | SCN-SETTINGS | Happy path | Automated | Existing | Positive AI completion values are updated. | `AiCommitAllSettingsTest.updates positive AI completion values` |
| SCN-SETTINGS-AUT-004 | SCN-SETTINGS | Happy path | Automated | Existing | Clear-before-generation setting updates. | `AiCommitAllSettingsTest.updates clear commit message before generation setting` |
| SCN-SETTINGS-AUT-005 | SCN-SETTINGS | Happy path | Automated | Existing | VCS shortcut takeover setting updates. | `AiCommitAllSettingsTest.updates use vcs shortcuts setting` |
| SCN-SETTINGS-AUT-006 | SCN-SETTINGS | Edge case | Automated | Existing | Clear-before-generation setting is preserved when AI completion values update. | `AiCommitAllSettingsTest.preserves clear commit message setting when updating AI completion values` |
| SCN-SETTINGS-AUT-007 | SCN-SETTINGS | Edge case | Automated | Existing | Shortcut setting is preserved when AI completion values update. | `AiCommitAllSettingsTest.preserves vcs shortcut setting when updating AI completion values` |
| SCN-SETTINGS-AUT-008 | SCN-SETTINGS | Failure path | Automated | Existing | Non-positive AI generation timeout is rejected. | `AiCommitAllSettingsTest.rejects non-positive AI generation timeout` |
| SCN-SETTINGS-AUT-009 | SCN-SETTINGS | Failure path | Automated | Existing | Non-positive completion check interval is rejected. | `AiCommitAllSettingsTest.rejects non-positive completion check interval` |
| SCN-SETTINGS-MAN-001 | SCN-SETTINGS | Happy path | Manual | Manual sandbox required | Settings UI displays accepted defaults. | Settings dialog values |
| SCN-SETTINGS-MAN-002 | SCN-SETTINGS | Edge case | Manual | Manual sandbox required | Settings persist after IDE restart or project reopen. | Settings values before and after restart |
| SCN-SETTINGS-MAN-003 | SCN-SETTINGS | Edge case | Manual | Manual sandbox required | Timeout, clear-before-generation, and shortcut settings affect runtime behavior. | Setting values and observed workflow behavior |

## Manual Test Case Details

Use IntelliJ IDEA `IIU` first. Repeat representative happy-path and failure-path cases in `PCP` or `WS` when preparing a release validation report. Automated scenario rows are executable test cases in the repository test suite; manual scenario rows use the details below.

### SCN-STAGE Manual Cases

#### SCN-STAGE-MAN-001: AI Stages Every Supported File-State Path

- Preconditions: JetBrains AI Assistant is installed and signed in; Commit tool window uses `Staging area`; the temporary Git repo contains `modified.txt`, deleted `delete-me.txt`, `rename-source.txt -> rename-target.txt`, `unversioned.txt`, already staged `staged-added.txt`, unchanged `unchanged.txt`, and ignored `ignored.txt`.
- Steps: Open the Commit tool window; confirm `Staging area` is enabled; click `AI`; watch the staged file list until AI generation starts or the workflow stops; run `git status --porcelain --ignored`.
- Expected result: The staged file list never becomes empty after eligible files are present. Modified, deleted, renamed, unversioned, and already staged paths are staged or remain staged. Ignored and unchanged files are excluded. AI generation starts. No commit is created.

#### SCN-STAGE-MAN-002: Commit Stages And Commits Every Supported Path

- Preconditions: Same fixture as `SCN-STAGE-MAN-001`; commit message field is empty before the run.
- Steps: Open the Commit tool window; confirm `Staging area` is enabled; click `Commit`; wait for AI generation and commit flow to finish; run `git show --name-status --oneline HEAD`.
- Expected result: The staged file list never becomes empty before commit execution. The workflow does not stop after staging. One new commit is created. The commit contains modified, deleted, renamed, unversioned, and already staged paths. Ignored and unchanged files are absent.

#### SCN-STAGE-MAN-003: Push Commits And Pushes To A Temporary Local Remote

- Preconditions: Same fixture as `SCN-STAGE-MAN-001`; a temporary local bare remote is configured as the tracked upstream; local and upstream branch hashes match before the run.
- Steps: Open the Commit tool window; confirm `Staging area` is enabled; click `Push`; wait for AI generation, commit, and push to finish; compare local and remote branch hashes.
- Expected result: The staged file list never becomes empty before commit execution. One new commit is created and pushed to the local remote. No real remote is contacted.

#### SCN-STAGE-MAN-004: Already Staged Files Are Not Lost

- Preconditions: `already-staged.txt` is staged; `unstaged.txt` is modified but unstaged; `new-file.txt` is unversioned; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm `already-staged.txt` is already staged; click `AI`; watch the staged file list until AI generation starts or the workflow stops; run `git status --porcelain`.
- Expected result: The staged file list never becomes empty. `already-staged.txt`, `unstaged.txt`, and `new-file.txt` are staged. AI generation starts. No commit is created.

#### SCN-STAGE-MAN-005: All Intended Files Already Staged

- Preconditions: Modified, deleted, renamed, and unversioned fixture files are all staged before clicking the plugin control; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm every intended path is staged; click `AI`; watch the staged file list until AI generation starts or the workflow stops.
- Expected result: The plugin does not remove or temporarily hide the staged set. AI generation starts. No commit is created.

#### SCN-STAGE-MAN-006: No Files Staged Before Run

- Preconditions: Modified, deleted, renamed, and unversioned fixture files exist; none are staged; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm the staged list is empty and unstaged changes are visible; click `AI`; watch the staged file list until AI generation starts or the workflow stops.
- Expected result: Eligible paths move into the staged list without a stop after staging. The staged list does not become empty after files are staged. AI generation starts. No commit is created.

#### SCN-STAGE-MAN-007: Multiple Git Roots And Nested Paths

- Preconditions: Project has two Git roots; root A contains modified `modules/core/build.gradle.kts` and unversioned `products/idea/plugin/src/Main.kt`; root B contains staged `products/webstorm/plugin/src/Main.kt`; `Staging area` is enabled.
- Steps: Open the Commit tool window; click `AI`; watch the staged file list until AI generation starts or the workflow stops; run `git status --porcelain` in both roots.
- Expected result: Eligible files from both roots remain visible and staged. Nested module and product paths are preserved. AI generation starts. No commit is created.

#### SCN-STAGE-MAN-008: Resolved-Conflict Paths Stay Included

- Preconditions: A conflict was resolved by the user and marked resolved in the IDE; the resolved path is committable; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm the resolved-conflict path is visible; click `AI`; watch the staged file list until AI generation starts or the workflow stops.
- Expected result: The resolved-conflict path remains included and staged. AI generation starts. No unresolved-conflict path is committed.

#### SCN-STAGE-MAN-009: Commit Shortcut Takeover

- Preconditions: Same fixture as `SCN-STAGE-MAN-001`; shortcut takeover is enabled; the IDE commit shortcut is available in the current keymap.
- Steps: Trigger the IDE commit shortcut; wait for AI generation and commit flow to finish; inspect the resulting commit.
- Expected result: Shortcut routing uses the same staging-area preparation as the `Commit` section. One local commit is created with the expected paths and no staged-list disappearance.

#### SCN-STAGE-MAN-010: Commit-And-Push Shortcut Takeover

- Preconditions: Same fixture as `SCN-STAGE-MAN-003`; shortcut takeover is enabled; the IDE commit-and-push shortcut is available in the current keymap.
- Steps: Trigger the IDE commit-and-push shortcut; wait for AI generation, commit, and push to finish; compare local and remote branch hashes.
- Expected result: Shortcut routing uses the same staging-area preparation as the `Push` section. One commit is pushed to the temporary local remote and no real remote is contacted.

#### SCN-STAGE-MAN-011: AI Assistant Missing Or Unavailable

- Preconditions: Eligible files exist; `Staging area` is enabled; JetBrains AI Assistant is missing, disabled, unavailable, or signed out.
- Steps: Click `AI`; observe the IDE error or notification; inspect `git log --oneline -1` and `git status --porcelain`.
- Expected result: The workflow stops without commit or push. Platform-owned AI Assistant errors are preserved when available. Eligible staged files are not lost.

#### SCN-STAGE-MAN-012: AI Generation Timeout Or No Completion Signal

- Preconditions: Eligible files exist; `Staging area` is enabled; configure a short AI generation timeout or otherwise produce no completion signal.
- Steps: Click `Commit`; wait until the timeout or no-signal stop path fires; inspect `git log --oneline -1` and `git status --porcelain`.
- Expected result: The workflow stops without commit or push. Staged files are not lost.

#### SCN-STAGE-MAN-013: Empty Or Unchanged AI Message

- Preconditions: Eligible files exist; `Staging area` is enabled; AI generation leaves the message empty or unchanged from the initial snapshot.
- Steps: Click `Commit`; wait for the generation completion handling; inspect the commit message field and `git log --oneline -1`.
- Expected result: The workflow stops without commit or push. Staged files are not lost.

#### SCN-STAGE-MAN-014: User Edits Or Clears Message During Generation

- Preconditions: Eligible files exist; `Staging area` is enabled; AI generation can be started.
- Steps: Click `Commit`; edit or clear the commit message while generation is running; wait for workflow handling; inspect `git log --oneline -1`.
- Expected result: The workflow stops without commit or push because the user changed the message. Staged files are not lost.

#### SCN-STAGE-MAN-015: Before-Commit Check Or Commit Warning Failure

- Preconditions: Eligible files exist; `Staging area` is enabled; configure a before-commit check or warning that blocks commit completion.
- Steps: Click `Commit`; let AI generation complete; let the IDE commit workflow reach the blocking check or warning.
- Expected result: The IDE safeguard is not bypassed. No unintended commit or push is created. Staged files are not lost.

#### SCN-STAGE-MAN-016: Unsafe Or Ambiguous Push Target Fallback

- Preconditions: Eligible files exist; `Staging area` is enabled; push target is missing, unsafe, ambiguous, or not a matching tracked upstream.
- Steps: Click `Push`; let AI generation complete; observe push execution or fallback behavior; inspect staged files and local commits.
- Expected result: The workflow does not push to an unsafe target. If the IDE fallback is used, staged files remain visible and platform push safeguards stay active.

#### SCN-STAGE-MAN-017: Empty Change Set

- Preconditions: `Staging area` is enabled; the repository has no committable changes.
- Steps: Open the Commit tool window; click `AI`, `Commit`, and `Push` in separate attempts; inspect notifications and `git log --oneline -1`.
- Expected result: Each attempt stops before AI generation, commit, or push. No staged-list mutation or commit occurs.

#### SCN-STAGE-MAN-018: Frozen Or Background-Running VCS Operation

- Preconditions: `Staging area` is enabled; a VCS freeze or background VCS operation is active before clicking the plugin control.
- Steps: Click `AI`; observe the workflow result and notifications; inspect `git status --porcelain`.
- Expected result: The workflow stops before staging mutation. Existing staged files remain unchanged.

### Additional Manual Cases

| ID | Preconditions | Steps | Expected result |
|----|---------------|-------|-----------------|
| `SCN-CONTROL-MAN-001` | Sandbox IDE open with light and dark themes available. | Toggle themes and inspect passive, disabled, cumulative hover, clicked, and running states. | Each state is legible, non-overlapping, and matches the accepted segmented-control behavior. |
| `SCN-CONTROL-MAN-002` | Sandbox IDE open on a Git project with the Commit tool window visible. | Locate the plugin control relative to `Commit and Push...`. | The plugin control appears after the IDE `Commit and Push...` control. |
| `SCN-CONTROL-MAN-003` | Sandbox IDE states for no project, no commit workflow, empty changes, and unavailable executors are available. | Observe or trigger action update in each state. | The control hides or disables without starting a workflow. |
| `SCN-SHORTCUT-MAN-001` | Shortcut takeover enabled and IDE commit shortcut available. | Trigger the IDE commit shortcut. | The plugin Commit workflow runs and creates the expected commit after AI generation. |
| `SCN-SHORTCUT-MAN-002` | Shortcut takeover enabled, IDE commit-and-push shortcut available, and local remote configured. | Trigger the IDE commit-and-push shortcut. | The plugin Push workflow runs and pushes to the local remote only. |
| `SCN-SHORTCUT-MAN-003` | Shortcut takeover disabled. | Trigger IDE commit and commit-and-push shortcuts. | Standard IDE actions run instead of plugin workflows. |
| `SCN-SHORTCUT-MAN-004` | A keymap without expected shortcuts or with unusual bindings is active. | Inspect action behavior and trigger unrelated shortcuts. | Plugin actions do not intercept unrelated commands. |
| `SCN-AI-MAN-001` | AI Assistant installed, enabled, signed in, and available. | Click `AI` with eligible Git changes. | AI Assistant generates a commit message from the active Commit tool window context. |
| `SCN-AI-MAN-002` | AI Assistant dependency is missing or disabled in a sandbox profile. | Install or load the plugin. | Plugin loading fails through the required dependency behavior. |
| `SCN-AI-MAN-003` | AI Assistant installed but unavailable or signed out. | Click `AI`, `Commit`, or `Push`. | Workflow stops without commit or push and preserves platform-owned AI messaging where available. |
| `SCN-AI-MAN-004` | Supported IDE products with potentially different AI action IDs or labels are available. | Run `AI` in each product. | AI action discovery still invokes commit-message generation or fails closed. |
| `SCN-AI-MAN-005` | AI timeout setting is set low enough to trigger. | Click `Commit` and wait. | Workflow stops without commit or push on timeout. |
| `SCN-AI-MAN-006` | Clear-before-generation setting can be toggled. | Run `AI` once with clearing enabled and once disabled. | Commit-message field is cleared or preserved according to the setting. |
| `SCN-AI-MAN-007` | AI generation can be started and the message field is editable. | Click `Commit`, then edit or clear the message while AI is running. | Workflow stops without commit or push because the user edited the message. |
| `SCN-SELECT-MAN-001` | Changelist-backed Commit workflow is active with modified, deleted, renamed, unversioned, resolved, ignored, and unchanged files. | Click `AI` and inspect included files. | Eligible files are included and ignored or unchanged files are excluded. |
| `SCN-SELECT-MAN-002` | Multiple changelists contain eligible Git changes. | Click `AI` and inspect included files. | Files from all relevant changelists are included. |
| `SCN-SELECT-MAN-003` | Project has multiple Git roots with eligible changes. | Click `AI` and inspect included files per root. | Files from every supported root are included. |
| `SCN-SELECT-MAN-004` | Unsupported or non-Git project state is open. | Trigger plugin control or shortcut. | Workflow hides or stops without selection mutation. |
| `SCN-SELECT-MAN-005` | Changelists disabled and Git staging disabled state is available. | Trigger plugin workflow with eligible changes. | Workflow uses supported selection behavior or fails closed without data loss. |
| `SCN-SELECT-MAN-006` | Resolved and unresolved conflict examples are available. | Mark one conflict resolved and trigger plugin workflow. | Resolved paths can be included; unresolved conflicts remain guarded by IDE behavior. |
| `SCN-WORKFLOW-MAN-001` | Eligible changes and AI Assistant available. | Click `AI`. | A message is generated and no commit is created. |
| `SCN-WORKFLOW-MAN-002` | Eligible changes and AI Assistant available. | Click `Commit`. | A message is generated and one local commit is created. |
| `SCN-WORKFLOW-MAN-003` | Eligible changes, AI Assistant available, and local remote configured. | Click `Push`. | A message is generated, one commit is created, and push behavior runs. |
| `SCN-WORKFLOW-MAN-004` | A blocking before-commit check or warning is configured. | Click `Commit` and let AI finish. | IDE safeguard blocks as usual and no unintended commit or push occurs. |
| `SCN-WORKFLOW-MAN-005` | Commit operation can produce a platform commit error. | Click `Commit`. | Platform commit error is surfaced without plugin masking. |
| `SCN-WORKFLOW-MAN-006` | Push operation can produce a platform push error. | Click `Push`. | Platform push error is surfaced or delegated without plugin masking. |
| `SCN-WORKFLOW-MAN-007` | Unsupported Commit workflow API shape is available in a supported IDE build. | Trigger plugin workflow. | Reflection boundary fails closed without unintended mutation. |
| `SCN-WORKFLOW-MAN-008` | VCS freeze or background VCS operation is active. | Trigger plugin workflow. | Workflow stops before mutation and reports the standard or plugin-owned message. |
| `SCN-PUSH-MAN-001` | Local bare remote is configured as matching tracked upstream. | Click `Push`. | Commit is pushed to the local remote without contacting a real remote. |
| `SCN-PUSH-MAN-002` | Branch has no tracked upstream. | Click `Push`. | Workflow falls back to IDE commit-and-push behavior without data loss. |
| `SCN-PUSH-MAN-003` | Local branch and upstream have diverged. | Click `Push`. | Immediate push is not used; platform fallback handles the state. |
| `SCN-PUSH-MAN-004` | Push target is protected, new, special, or otherwise ambiguous. | Click `Push`. | Immediate push is not used and platform behavior remains in charge. |
| `SCN-PUSH-MAN-005` | Repository state is not normal. | Click `Push`. | Immediate push is not used. |
| `SCN-PUSH-MAN-006` | Real push failure can be produced safely against a local remote. | Click `Push`. | Push failure is surfaced and branch hashes show no unintended remote update. |
| `SCN-SETTINGS-MAN-001` | Settings dialog is available. | Open `Settings | Tools | AI Commit All`. | Defaults match documented and automated settings defaults. |
| `SCN-SETTINGS-MAN-002` | Settings can be changed and IDE restarted or project reopened. | Change settings, restart or reopen, and inspect values. | Settings persist. |
| `SCN-SETTINGS-MAN-003` | Timeout, clear-before-generation, and shortcut settings can be toggled. | Run workflows with each setting changed. | Runtime behavior follows the configured values. |

## Automation Candidates

Prefer automation for cases that can be isolated from the live Commit tool window:

- Promote `SCN-STAGE-MAN-004`, `SCN-STAGE-MAN-005`, and `SCN-STAGE-MAN-006` into unit coverage around staging-area selection once the production boundary exposes staged-set transitions cleanly.
- Keep `SCN-STAGE-MAN-001`, `SCN-STAGE-MAN-002`, and `SCN-STAGE-MAN-003` manual until a reliable IntelliJ fixture can drive the Git staging Commit UI and JetBrains AI Assistant path.
- Keep `SCN-STAGE-MAN-011` through `SCN-STAGE-MAN-018` manual unless the relevant platform or AI Assistant stop paths can be simulated without masking standard IDE behavior.
