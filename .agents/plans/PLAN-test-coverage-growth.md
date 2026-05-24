# Plan: Test Coverage Growth

Plan-ID: PLAN-test-coverage-growth

Status: Draft

Workers: 1

Filename: `.agents/plans/PLAN-test-coverage-growth.md`

## Readiness

- Plan readiness: Drafted from a full current JaCoCo coverage pass and source/test layout review; extended with staged and unstaged move/rename coverage targets plus current source/test scans for complex uncovered workflow, VCS, AI, UI, reliability, and premature-abandonment edge cases; ready for maintainer review, not approved for implementation.
- Open questions: None blocking. Coverage targets are plan assumptions and can be adjusted during approval.
- Implementation progress: Not started.

## Status History

- 2026-05-24T23:01:33+02:00: none -> Draft by OpenAI Codex <codex@openai.com>; plan created from whole-codebase coverage analysis.

## Goal

Increase automated test coverage for the IntelliJ plugin codebase by adding focused behavior tests around the highest missed-line and missed-branch clusters, while preserving existing plugin behavior and commit/push safety guarantees.

Current baseline from `.\gradlew.bat test jacocoTestReport` on 2026-05-24:

| Metric      | Covered / Total | Coverage | Missed |
|-------------|-----------------|----------|--------|
| Line        | 2161 / 2981     | 72.5%    | 820    |
| Branch      | 751 / 1152      | 65.2%    | 401    |
| Instruction | 10280 / 14383   | 71.5%    | 4103   |

Package concentration:

| Package                                        | Line Coverage | Missed Lines | Branch Coverage | Missed Branches |
|------------------------------------------------|---------------|--------------|-----------------|-----------------|
| `pl.devopssolutions.aicommitall.workflow`      | 62.0%         | 337          | 62.0%           | 108             |
| `pl.devopssolutions.aicommitall.vcs`           | 65.6%         | 221          | 51.0%           | 149             |
| `pl.devopssolutions.aicommitall.ai`            | 73.2%         | 159          | 77.3%           | 56              |
| `pl.devopssolutions.aicommitall.actions`       | 88.0%         | 85           | 70.1%           | 69              |
| `pl.devopssolutions.aicommitall.settings`      | 91.4%         | 11           | 77.9%           | 19              |
| `pl.devopssolutions.aicommitall.notifications` | 68.4%         | 6            | 100.0%          | 0               |

Target outcome for this plan:

- Raise actual coverage to at least 78% line and 70% branch coverage.
- Stretch target: 80% line and 72% branch coverage if the additional tests stay deterministic and low-maintenance.
- Keep `verifyJacocoCoverageReport` thresholds unchanged in this plan unless the maintainer explicitly approves a validation-policy change after the measured result.

## Non-Goals

- Do not change user-visible plugin behavior.
- Do not bypass IDE commit, push, before-commit, or AI Assistant safeguards to make tests easier.
- Do not add brittle UI sleeps or real remote pushes.
- Do not raise `build.gradle.kts` coverage gates as part of this plan without explicit maintainer approval or a separate required decision.
- Do not replace manual release validation for live AI Assistant and Marketplace behavior.

## Assumptions

- Coverage growth should prioritize behavior risk and branch concentration over maximizing raw line count.
- Small internal test seams are acceptable when they preserve production defaults and remove static IntelliJ service coupling from otherwise valuable unit tests.
- Local Git repositories and deterministic fakes remain preferred over real remotes, live AI Assistant, or long-running sandbox scenarios.
- Current skipped asset-generation test remains skipped unless asset regeneration is explicitly requested.

## Open Questions

- None.

## Proposed Changes

1. Add or extend workflow tests for selection preparation, execution lifecycle, post-commit push handling, push-only execution, commit result registration, and transient readiness or selection states that should not make the plugin give up too early.
2. Add or extend VCS tests for outgoing-commit status, safe immediate push decisions, push completion tracking, Git selection service behavior, staged/unstaged file move or rename states, including moves or renames with destination content changes, listener/dispose races, and safe-push fallback decisions while repository state is still refreshing.
3. Add AI integration-boundary tests for commit-message invocation data, full action-event construction, completion service wiring, text access, completion edge states, user-edit precedence, late AI action availability, and transient progress-signal failures.
4. Add action and UI branch tests for availability, data context fallback, shortcut takeover boundaries, accessibility, geometry hit-testing, section-state behavior, and user-action routing when toolbar or shortcut update data was stale.
5. Run the full coverage gate and record the achieved coverage in the plan result summary before any optional threshold follow-up.

Additional complex cases identified from the current source/test scan:

- Workflow orchestration has tests for ordinary stop reasons, but not for activity cleanup, active-workflow reset, and phase transitions when background preparation, EDT AI invocation, commit completion, push completion, or push-only completion fails exceptionally.
- Commit execution tests cover happy paths and some invocation failures, but not every registered result-handler path: default commit success with a registered listener, listener disposal on executor/gate failure, post-commit immediate-push cancel/failure/no-success paths, asynchronous immediate-push failures, and fallback commit-and-push cancel/failure before after-refresh.
- Push completion tracking covers core success/failure/cancel/timeout results, but not empty repository waits, irrelevant repository events, duplicate completion events after a waiter is done, listener removal by parent disposal, dispose-time cancellation with partial results, or timeout-handle cancellation races.
- AI invocation context tests mostly cover collected data, leaving full `AnActionEvent` construction, cloned presentation isolation, input-event propagation, child data-context override versus stale parent data, and missing commit-message control/document combinations as higher-risk gaps.
- Action and control tests cover main routing and rendering smoke cases, but not boundary hit testing at dividers and outside bounds, no-enabled-section keyboard movement, animation start/stop on displayability changes, custom accessible-name override behavior, and shortcut promoter behavior when both plugin shortcut actions and source IDE actions are present in mixed order.

Premature-abandonment cases identified from the current source/test scan:

- `AiCommitMessageActionInvoker` treats a single missing AI action lookup as final; add red-first tests where the action appears on a later lookup before the timeout budget expires.
- `AiGenerationCompletionObserver` immediately returns `NoCompletionSignal` on `AiGenerationRunningState.Unavailable`; add red-first tests for transient progress-signal lookup/read failures followed by `Running` and a valid stopped generated message.
- `VcsOperationReadinessGuard` and workflow preparation are currently single-shot checks; add tests for frozen/background VCS and empty-selection states that clear on a controlled retry before the workflow reports a stop reason.
- `CommitWorkflowSelectionService` can stop on activation or synchronization failure after one attempt; add tests where the Commit tool window activation or reflective synchronization succeeds on a later deterministic attempt without bypassing platform safeguards.
- `SafeImmediatePushService` makes immediate push fallback decisions from one repository snapshot; add tests where upstream hashes, push specs, and outgoing commits are initially unavailable but become safe before falling back to the IDE commit-and-push executor.
- Shortcut and toolbar routing can rely on stale update-time availability; add tests where action execution rechecks fresh data and does not delegate to the IDE action or hide the plugin action solely because the previous update saw transient missing workflow data.

Expected write areas:

- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/`
- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/`
- `src/test/kotlin/pl/devopssolutions/aicommitall/validation/`
- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/`
- `src/test/kotlin/pl/devopssolutions/aicommitall/actions/`
- Narrow internal production seams under matching `src/main/kotlin/...` files only when needed for deterministic tests.

## Task Packets

### Task Packet: T1-workflow-selection-execution-and-result-registration

Task id: T1-workflow-selection-execution-and-result-registration

Lane: testing

Required skills:

- `plugin-test-tdd`
- `kotlin-plugin-style`

Goal:

- Add focused tests for `CommitWorkflowSelectionService`, `AiCommitAllWorkflowRunner`, `CommitWorkflowExecutionService`, `PushOnlyWorkflowExecutionService`, and `IntellijCommitWorkflowResultRegistrar`, the workflow files with the weakest current direct coverage and highest orchestration risk.

Initial context budget:

- Read first:
  - This plan header, readiness summary, execution graph, and this task packet.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowSelectionService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowSelectionResult.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowSelectionItems.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowResultRegistrar.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/AiCommitAllWorkflowCoordinator.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowExecutionService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/PushOnlyWorkflowExecutionService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/VcsOperationReadinessService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmation.kt`
  - Existing workflow tests in `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/`
- Escalate to:
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitChangeSelectionService.kt` only if a selection-service seam is needed.
  - `.agents/references/code-style.md` if adding shared test helpers.

Allowed inputs:

- Files named in `Read first`.
- Files named in `Escalate to` after an escalation trigger.

Forbidden inputs:

- Unrelated archived plans.
- Previous worker chat beyond the orchestrator handoff summary.
- Implementation evidence from unrelated task packets.

Write scope:

- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowSelectionServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowResultRegistrarTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/AiCommitAllWorkflowRunnerTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowExecutionServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/PushOnlyWorkflowExecutionServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/VcsOperationReadinessServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmationTest.kt`
- Existing workflow test helpers only if duplication becomes material.
- `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowSelectionService.kt` only for a narrow internal seam.
- `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/CommitWorkflowResultRegistrar.kt` only for a narrow internal seam.
- Matching workflow production files only for narrow internal seams that expose deterministic collaborators without changing execution order or platform safeguards.

Dependencies:

- None.

Validation:

- Run targeted workflow tests added or changed by this packet.
- Run `.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.workflow.*"` when practical.
- Run `.\gradlew.bat spotlessCheck` if Kotlin files changed.

Escalation triggers:

- Static IntelliJ service access prevents deterministic unit coverage.
- A fake `AbstractCommitWorkflowHandler` cannot be built without relying on unstable platform internals.
- Added seams would change observable commit workflow behavior.
- A test would need to bypass the default commit gate, before-commit checks, or platform commit executor semantics instead of observing the existing service boundary.

Stop conditions:

- Testing `IntellijCommitWorkflowResultRegistrar` requires a full IDE fixture or platform implementation detail that is less stable than the coverage value.
- Any proposed seam would alter production behavior or bypass platform commit safeguards.

Expected output:

- Tests for missing workflow, unsupported VCS, empty selection, no owning changelist, activation failure, synchronization failure, and prepared selection paths where feasible.
- Tests for commit result listener success, success-after-refresh, cancel, failure, disposal idempotence, and failed registration where feasible.
- Tests proving workflow activity closes and the active-workflow lock resets when preparation, AI invocation, AI completion, commit completion, commit-and-push completion, or push-only completion fails exceptionally; include a repeated-start-after-failure case.
- Red-first reliability tests for transient `VcsOperationReadinessResult.Frozen` and `BackgroundOperationRunning` results that become `Ready` within a bounded retry budget before workflow selection starts.
- Red-first reliability tests where the first selection collection is empty because VCS state has not refreshed, then a later controlled refresh exposes committable content before the plugin reports `EmptySelection`.
- Red-first reliability tests where commit workflow UI activation or reflective synchronization fails once and then succeeds, proving the plugin does not prematurely stop while the Commit tool window is still settling.
- Tests for push-only unavailable execution after empty selection with outgoing commits, so `PushExecutionUnavailable` is reported and activity still closes.
- Tests for default commit success through a registered result listener, result-listener disposal when the default executor or readiness gate throws, safe immediate push not starting on post-commit cancel/failure, synchronous and asynchronous immediate-push failures, and fallback commit-and-push cancel/failure before after-refresh.
- Tests for reflective workflow synchronization with unversioned files included, missing only `synchronizeInclusion`, missing only `setCommitState`, invocation failure from `setCommitState`, and diagnostic contents for each missing-method combination.
- Coverage result delta for workflow package.

Result summary:

- Status: pending
- Worker:
- Changed files or reviewed diff:
- Validation evidence:
- Blockers:
- Review risks:
- Handoff notes:

### Task Packet: T2-vcs-push-and-outgoing-coverage

Task id: T2-vcs-push-and-outgoing-coverage

Lane: testing

Required skills:

- `plugin-test-tdd`
- `kotlin-plugin-style`

Goal:

- Add behavior tests for the VCS package branch clusters: `SafeImmediatePushService`, `GitOutgoingCommitsStatus`, `GitPushCompletionTracker`, selection collection boundaries, and staged/unstaged move or rename selection states.

Initial context budget:

- Read first:
  - This plan header, readiness summary, execution graph, and this task packet.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/SafeImmediatePushService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitOutgoingCommitsService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitPushCompletionService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitChangeSelection.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitChangeSelectionService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitStageSelectionItems.kt`
  - Existing VCS tests in `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/`
  - `src/test/kotlin/pl/devopssolutions/aicommitall/validation/LocalGitRepositoryValidationTest.kt`
  - `src/test/kotlin/pl/devopssolutions/aicommitall/validation/LocalGitTestSupport.kt`
- Escalate to:
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt` only if move/rename staging behavior cannot be proven at the VCS selection boundary.
  - `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmationTest.kt` only if staged rename confirmation needs workflow-level coverage in addition to VCS selection coverage.

Allowed inputs:

- Files named in `Read first`.
- Files named in `Escalate to` after an escalation trigger.

Forbidden inputs:

- Real remotes or credentials.
- Live push targets outside temporary local repositories.
- Unrelated archived plans.

Write scope:

- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/SafeImmediatePushServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/GitOutgoingCommitsStatusTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/GitOutgoingCommitsServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/GitPushCompletionServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/GitChangeSelectionServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/vcs/GitStageSelectionItemsTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/validation/LocalGitRepositoryValidationTest.kt`
- Narrow internal production seams in matching VCS services only if deterministic tests need them.

Dependencies:

- None.

Validation:

- Run targeted VCS tests added or changed by this packet.
- Run `.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.vcs.*"` when practical.
- Run `.\gradlew.bat spotlessCheck` if Kotlin files changed.

Escalation triggers:

- A coverage target requires exercising private IntelliJ environment adapters instead of the existing injected environment interfaces.
- A local Git fixture is needed to prove path, staged-state, rename detection, move-with-content-change, or upstream behavior that fakes cannot model safely.

Stop conditions:

- A test would push to a real remote.
- A test depends on timing sleeps instead of deterministic scheduler handles.
- Additional coverage would mostly exercise platform internals rather than repository behavior.

Expected output:

- Tests for loader-failure, unchanged-cache, throttle, pending-refresh, and action-refresh branches in `GitOutgoingCommitsStatus`.
- Tests for empty repository waits, listener registration/removal by parent disposal, dispose cancellation with partial results, irrelevant repository events, duplicate completion events after completion, timeout-handle cancellation after immediate completion, and every successful push result type in `GitPushCompletionTracker`.
- Tests for affected-path extraction from before/after revisions, resolved conflicts, staging-area paths, duplicates, outgoing-only filtering, null push specs, unsafe filtered states, and push invocation failure propagation in `SafeImmediatePushService`.
- Tests for multi-repository affected selections that deduplicate repeated paths per repository, stop at the first missing repository without loading push states, and preserve stable push-spec ordering across multiple roots.
- Tests for outgoing-only push preparation when some repositories have null push specs, some have outgoing commits, and some throw while checking outgoing commits; unsafe or unavailable repositories must not be silently pushed.
- Red-first reliability tests for safe immediate push preparation where push support, push specs, tracked-upstream hash parity, or outgoing-commit data is transiently unavailable and then becomes safe inside a bounded retry or refresh cycle.
- Tests that distinguish a genuinely unsafe repository state from a not-yet-refreshed repository state, so the plugin falls back immediately for real divergence, conflicts, special refs, and missing upstream, but waits or retries when the environment reports a refreshable unknown state.
- Tests for staged rename, unstaged rename, staged move, unstaged move, staged rename with additional work-tree content changes, and unstaged move or rename with destination content changes. Cover both old and new path handling where IntelliJ or Git exposes both paths, and document the exact local Git short-status shapes used to map those scenarios into deterministic unit fixtures.
- Tests for `GitStageSelectionItems` that prove already-staged renames are not re-staged, partially staged renames or moves with content changes are staged by the destination path, committable path collection keeps rename destinations, and missing-staged-path confirmation fails closed when a rename destination remains unstaged.
- Coverage result delta for VCS package.

Result summary:

- Status: pending
- Worker:
- Changed files or reviewed diff:
- Validation evidence:
- Blockers:
- Review risks:
- Handoff notes:

### Task Packet: T3-ai-boundary-coverage

Task id: T3-ai-boundary-coverage

Lane: testing

Required skills:

- `plugin-test-tdd`
- `kotlin-plugin-style`

Goal:

- Cover AI integration-boundary paths with deterministic fakes, focusing on `AiGenerationCompletion.kt` and `AiCommitMessageActionInvocationContext.kt`.

Initial context budget:

- Read first:
  - This plan header, readiness summary, execution graph, and this task packet.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletion.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiCommitMessageActionDiscoveryService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiCommitMessageActionInvocationService.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiCommitMessageActionInvocationContext.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/ai/CommitMessageUiAccessors.kt`
  - Existing AI tests in `src/test/kotlin/pl/devopssolutions/aicommitall/ai/`
- Escalate to:
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/AiCommitAllWorkflowCoordinator.kt` only if workflow calls expose an uncovered AI result path that is better tested at runner level.

Allowed inputs:

- Files named in `Read first`.
- Files named in `Escalate to` after an escalation trigger.

Forbidden inputs:

- Proprietary AI Assistant classes.
- Live AI Assistant execution.
- Unrelated archived plans.

Write scope:

- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletionObserverTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletionServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/AiCommitMessageActionDiscoveryServiceTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/AiCommitMessageActionInvokerTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/AiCommitMessageActionInvocationContextFactoryTest.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/ai/CommitMessageUiReaderTest.kt`
- Narrow AI test helpers only when shared across these tests.

Dependencies:

- None.

Validation:

- Run targeted AI tests added or changed by this packet.
- Run `.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.ai.*"` when practical.
- Run `.\gradlew.bat spotlessCheck` if Kotlin files changed.

Escalation triggers:

- A proposed test duplicates an existing observer case without adding a new branch or behavior invariant.
- An uncovered line is a platform adapter that cannot be tested without a live AI Assistant dependency.

Stop conditions:

- A test requires compile-time dependency on proprietary AI Assistant APIs.
- A test relies on wall-clock sleeps instead of injected time source and sleeper fakes.

Expected output:

- Tests for service wrapper wiring, default option normalization, text cleaner branches, focus inactive handling, user-edit signal fallback, unavailable running-signal diagnostics, parent data-context fallback, and input-event propagation.
- Tests for `createInvocationContext` that assert the AI action event place, input event, cloned presentation isolation, project/workflow/UI data override stale parent data, and optional commit-message control/document keys are omitted when neither workflow UI nor parent context can provide them.
- Tests where user-edit detection wins over a simultaneously changed generated message, unavailable running signal with blank and nonblank messages returns the expected fail-closed result, focus returns after an inactive stopped-signal window, zero stopped-signal grace period completes immediately after a stable stop, and timeout boundary behavior is deterministic at exactly the configured timeout.
- Red-first reliability tests where `AiCommitMessageActionDiscovery` returns no action on the first lookup but finds the known action, a prefix match, or a presentation fallback on a later lookup within the AI invocation timeout budget.
- Red-first reliability tests where `ReflectiveActionProgressRunningSignal` reports `Unavailable` once because the progress indicator field is temporarily missing, unreadable, or throws, then reports `Running` and `NotRunning` with a generated message; these should expose any immediate `NoCompletionSignal` stop.
- Tests for initial `NotRunning` and unchanged message followed by delayed `Running`, delayed generated text after stop, and focus regaining during the stopped-signal grace window, proving the observer waits for credible completion evidence instead of giving up on the first quiet poll.
- Tests for `AiGenerationCompletionService.awaitCompletionAsync` that prove the user-edit signal is closed after normal, stopped, timeout, and exceptional observer paths if a lightweight injectable seam is needed.
- Coverage result delta for AI package.

Result summary:

- Status: pending
- Worker:
- Changed files or reviewed diff:
- Validation evidence:
- Blockers:
- Review risks:
- Handoff notes:

### Task Packet: T4-actions-and-small-package-coverage

Task id: T4-actions-and-small-package-coverage

Lane: testing

Required skills:

- `plugin-test-tdd`
- `kotlin-plugin-style`

Goal:

- Close low-risk branch gaps in action availability, control state, settings, notifications, and the plugin marker where tests can remain small and stable.

Initial context budget:

- Read first:
  - This plan header, readiness summary, execution graph, and this task packet.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllActions.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllShortcutActions.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt`
  - `src/main/kotlin/pl/devopssolutions/aicommitall/settings/AiCommitAllConfigurable.kt`
  - Existing action and settings tests in `src/test/kotlin/pl/devopssolutions/aicommitall/actions/` and `src/test/kotlin/pl/devopssolutions/aicommitall/settings/`
- Escalate to:
  - `src/main/resources/META-INF/plugin.xml` only if a registration assertion is affected.

Allowed inputs:

- Files named in `Read first`.
- Files named in `Escalate to` after an escalation trigger.

Forbidden inputs:

- Visual asset regeneration unless explicitly requested.
- Live IDE screenshots.
- Unrelated archived plans.

Write scope:

- Existing action tests under `src/test/kotlin/pl/devopssolutions/aicommitall/actions/`
- Existing settings tests under `src/test/kotlin/pl/devopssolutions/aicommitall/settings/`
- Small new tests for `AiCommitAllPluginMarker` or notification branches only if they add meaningful coverage.

Dependencies:

- None.

Validation:

- Run targeted action/settings tests added or changed by this packet.
- Run `.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.actions.*" --tests "pl.devopssolutions.aicommitall.settings.*"` when practical.
- Run `.\gradlew.bat spotlessCheck` if Kotlin files changed.

Escalation triggers:

- A control-rendering branch requires pixel assertions beyond existing deterministic paint helpers.
- A test would make generated asset dimensions or screenshots part of this plan.

Stop conditions:

- A proposed test is only asserting Kotlin data-class generated code without behavior value.
- A visual test is flaky across themes, fonts, or headless environments.

Expected output:

- Tests for action update branches with missing or partial providers, unknown running mode, mixed enabled/disabled availability, component fallback, keyboard/mouse paths not already covered, settings validation branch gaps, and small marker/notification lines where useful.
- Tests for toolbar custom component fallback when the presentation has no control state, non-control component update no-op, custom component data-context lookup using the actual clicked component, and workflow start receiving the original input event from mouse activation.
- Tests for shortcut takeover when commit and push plugin actions are both present with their source IDE actions in mixed order, source-action absence in the IntelliJ delegate, workflow-running actionPerformed no-op, project-missing update fallback, and setting toggles changing promoter suppress/promote results without stale state.
- Red-first reliability tests where shortcut `update` observes missing workflow data, but `actionPerformed` receives a fresh data context with workflow data and starts the plugin workflow instead of delegating to the IDE source action.
- Red-first reliability tests where the three-section toolbar presentation was hidden or disabled by stale availability, but the clicked custom component resolves a fresh enabled data context and starts the intended workflow section.
- Tests for control boundary hit-testing at section divider pixels and outside bounds, no-enabled-section keyboard movement no-op, focus request on mouse press, animation timer start/stop across `addNotify`, running-state removal, and `removeNotify`, custom accessible name/description overrides, and geometry behavior under very narrow or zero-size bounds.
- Tests for settings configurable after `disposeUIResources`, `apply` before `createComponent`, reset after rejected apply, max spinner values, and independent persistence when one checkbox component is absent.
- Coverage result delta for actions and smaller packages.

Result summary:

- Status: pending
- Worker:
- Changed files or reviewed diff:
- Validation evidence:
- Blockers:
- Review risks:
- Handoff notes:

### Task Packet: T5-coverage-verification-and-threshold-recommendation

Task id: T5-coverage-verification-and-threshold-recommendation

Lane: review

Required skills:

- `plugin-test-tdd`
- `repository-documentation`

Goal:

- Run the complete coverage validation, record measured results, and recommend whether a later threshold update is justified.

Initial context budget:

- Read first:
  - This plan header, readiness summary, execution graph, and this task packet.
  - `build.gradle.kts`
  - `README.md`
  - `build/reports/jacoco/test/jacocoTestReport.xml` after T1 through T4 complete.
- Escalate to:
  - `.agents/references/documentation.md` only if updating documentation.
  - `docs/decisions/README.md` only if the maintainer explicitly asks to raise coverage gates and that is treated as a validation-policy change.

Allowed inputs:

- Files named in `Read first`.
- Files named in `Escalate to` after an escalation trigger.

Forbidden inputs:

- Threshold changes without explicit maintainer approval.
- Changelog entries unless a public plugin-facing behavior or release artifact changes.

Write scope:

- This plan file result summaries and handoff notes.
- No `build.gradle.kts` or `README.md` edits unless explicitly approved after measured coverage results.

Dependencies:

- T1, T2, T3, and T4 complete or intentionally skipped with reasons.

Validation:

- Run `.\gradlew.bat test jacocoTestReport verifyJacocoCoverageReport`.
- Run `.\gradlew.bat spotlessCheck` if Kotlin files changed.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` if this plan or docs are updated.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1` if `.agents/` artifacts are updated.
- Run `git diff --check`.

Escalation triggers:

- Coverage target is missed after deterministic tests are added.
- Coverage gain comes mostly from low-value generated or platform-adapter code.
- The maintainer requests a threshold increase.

Stop conditions:

- Full test suite or coverage report is failing.
- A threshold update would require ADR or explicit decision that has not been accepted.

Expected output:

- Final line and branch coverage numbers.
- List of task packets completed, skipped, and why.
- Recommendation for future coverage gates, if any.
- Remaining manual or fixture coverage gaps.

Result summary:

- Status: pending
- Worker:
- Changed files or reviewed diff:
- Validation evidence:
- Blockers:
- Review risks:
- Handoff notes:

## Execution Model

- `Workers: 1`; execute packets sequentially unless the maintainer explicitly approves parallel sub-agent work later.
- T1 through T4 are independent enough to reorder, but T5 must run last.
- Approved-plan execution in this repository normally requires fresh sub-agent task workers. If the active tool contract does not authorize sub-agents at implementation time, stop before implementation and ask for explicit delegation approval.
- Keep production changes limited to internal test seams that preserve existing defaults.
- Commit each completed approved-plan task separately when commits are requested or required by the active execution rules.

## Execution Graph

```mermaid
flowchart TD
    O1["O1[code]<br/>orchestrator"]
  W1["W1[code]<br/>T1 workflow selection/execution/result"]
    W2["W2[code]<br/>T2 VCS push/outgoing"]
    W3["W3[code]<br/>T3 AI boundary"]
    W4["W4[code]<br/>T4 actions/small packages"]
    W5["W5[run-verify]<br/>T5 coverage verification"]
    O1 --> W1
    W1 --> W2
    W2 --> W3
    W3 --> W4
    W4 --> W5
    W5 --> O1
```

## Validation

Planning validation:

- `.\gradlew.bat test jacocoTestReport`
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`
- `git diff --check`

Implementation validation:

- Packet-specific targeted tests from `## Task Packets`.
- Final `.\gradlew.bat test jacocoTestReport verifyJacocoCoverageReport`.
- `.\gradlew.bat spotlessCheck` for Kotlin changes.
- `git diff --check`.

## Risks

- Some missed lines are IntelliJ environment adapters; tests that mock too much platform behavior may add maintenance cost without improving confidence.
- Coverage from private platform wrappers should not displace behavior tests for commit/push safety.
- Any internal seam added for tests must keep existing production constructors and service defaults unchanged.
- Raising coverage gates may be a validation-policy change; treat that as a separate explicit approval path.

## Handoff Notes

- Current high-value coverage targets by missed lines are `AiCommitAllWorkflowCoordinator.kt` (91), `AiGenerationCompletion.kt` (66), `CommitWorkflowSelectionService.kt` (58), `SafeImmediatePushService.kt` (55), `GitPushCompletionService.kt` (52), `ReflectiveCommitWorkflowSynchronizer.kt` (47), `GitOutgoingCommitsService.kt` (46), `GitStageConfirmation.kt` (45), `AiCommitAllActions.kt` (39), `AiCommitMessageActionInvocationContext.kt` (39), `GitChangeSelectionService.kt` (39), and `CommitWorkflowResultRegistrar.kt` (32).
- The current suite has broad workflow behavior coverage already (`287` passing, `1` pending), so avoid duplicating existing happy paths.
- A 2026-05-24 source/test scan found the best additional return in cross-phase exceptional paths, listener lifecycle races, parent/child data-context override behavior, and UI boundary conditions rather than more happy-path assertions.
- A follow-up reliability scan found single-shot stop decisions around AI action discovery, progress-signal availability, VCS readiness, selection collection, safe-push repository snapshots, and stale action-update data; these should be covered with red-first tests that fail if the plugin gives up before a bounded retry or fresh action-time read can succeed.
- The best expected return is from service edge cases, scheduler/timeout behavior, failure branches, lifecycle cleanup, and deterministic fakes around existing injection points.
