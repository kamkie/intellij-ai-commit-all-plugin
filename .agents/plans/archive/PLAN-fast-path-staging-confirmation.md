# Plan: Fast-Path Staging Confirmation

Plan-ID: PLAN-fast-path-staging-confirmation

Status: Closed

Close-Reason: Archived

Workers: 1

Filename: `.agents/plans/archive/PLAN-fast-path-staging-confirmation.md`

## Readiness

- Plan readiness: T1 is implemented, validated, self-reviewed, reconciled, and archived at the user's request.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-07-10T15:33:34+02:00
- Open questions: No.
- Implementation progress: Complete; the plan no longer has active execution or release-preparation work.

## Status History

- 2026-07-10T15:07:47+02:00: none -> Draft by Codex <codex@openai.com>; replaced the rejected timeout/cancellation approach with a staging-path fix.
- 2026-07-10T15:32:00+02:00: Draft -> Draft by Codex <codex@openai.com>; corrected the AI handoff after verifying that the installed JetBrains AI action reads included changes from `CommitWorkflowUi`, not from the Git index or handler state.
- 2026-07-10T15:33:34+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicitly requested implementation in a new Codex task.
- 2026-07-10T15:37:15+02:00: Approved -> In Progress by Codex <codex@openai.com>; started orchestrated implementation of T1 in the approved new task.
- 2026-07-10T16:22:39+02:00: In Progress -> Implemented by Codex <codex@openai.com>; reconciled W1 implementation, TDD, focused real-IDE integration, full validation, and self-review evidence.
- 2026-07-10T16:22:40+02:00: Implemented -> Closed by Codex <codex@openai.com>; archived the completed plan at the user's request; Close-Reason: Archived.

## Goal

Continue from successful Git staging without blocking background preparation on IntelliJ changelist and staging-tracker refresh callbacks when the Git index already proves every selected path is staged or HEAD-identical. On the EDT turn that invokes JetBrains AI, first apply the index-confirmed tracker state and included roots to the Commit UI, verify that the UI exposes the expected staged changes, and only then invoke AI generation.

## Non-Goals

- Cancel or time out an AI action merely because its EDT callback is delayed.
- Run the AI Assistant action outside the EDT or change its modality policy.
- Claim to fix EDT starvation caused by IntelliJ or another plugin.
- Change which files are selected, staged, committed, or pushed.
- Weaken fail-closed handling when the Git index cannot confirm all expected paths.

## Assumptions

- `git add` completion followed by direct per-path Git index confirmation is stronger staging evidence than waiting for eventually consistent IDE tracker callbacks.
- The installed JetBrains AI Assistant `Vcs.LLMCommitMessageAction` reads `CommitWorkflowUi.includedChanges` and `includedUnversionedFiles`; assigning only `GitStageCommitWorkflowHandler.state` is insufficient because that setter does not update `GitStageCommitPanel`.
- The index-confirmed tracker state and included roots therefore form a required Commit UI model handoff, not a best-effort visual refresh.
- `ChangeListManager` and `GitStageTracker` refresh callbacks remain useful for eventual IDE consistency, but they do not need to block background preparation after direct index confirmation succeeds.
- If immediate index confirmation is incomplete or fails, the existing bounded IDE refresh and retry path remains the fallback.

## Open Questions

- None.

## Proposed Changes

### T1-fast-path-staging-confirmation

- Add regression tests proving that successful direct index confirmation skips `waitForStatusRefresh()` and `refreshTrackerState()` while still returning an index-confirmed state.
- Reorder staging confirmation so `GitFileUtils.addPaths` is followed by immediate Git index confirmation for all expected paths.
- When every path is `STAGED` or `HEAD_IDENTICAL`, synthesize the required tracker state from the current tracker snapshot and carry it, its included roots, and the expected staged paths into the AI-phase EDT handoff.
- In the same EDT callback that invokes `Vcs.LLMCommitMessageAction`, first assign the confirmed handler state, call `GitStageCommitPanel.setTrackerState(...)` and `setIncludedRoots(...)`, and verify that `CommitWorkflowUi.includedChanges` exposes every expected staged path. Invoke AI only after that required model handoff succeeds; fail closed otherwise.
- Keep unrelated visual repaint or eventual tracker refresh work asynchronous and best-effort, with diagnostics, after the required inclusion model is current.
- Preserve the current blocking refresh/retry path when any path remains `UNCONFIRMED` or the direct Git check fails.
- Add step timing diagnostics for `git add`, direct index confirmation, the fallback IDE refresh, the EDT Commit UI model handoff, and the included-path verification so future logs distinguish them.
- Extend the fake AI action to record the paths returned by `CommitWorkflowUi` at invocation, and add a staging-enabled integration assertion proving AI receives the intended modified, deleted, renamed, and multi-root changes through that API.
- Update `REQ-SEL-008` to state that direct post-stage index confirmation may precede IDE tracker refresh but that the Commit UI inclusion model MUST be current before AI invocation, and add an `Unreleased` changelog entry.

## Task Packets

### Task Packet: T1-fast-path-staging-confirmation

Task id: T1-fast-path-staging-confirmation

Lane: implementation

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `repository-documentation`

Goal:

- A staging run whose Git index immediately confirms every expected path reaches AI preparation without waiting for IntelliJ status/tracker refresh callbacks; unconfirmed paths retain the existing bounded fallback and fail-closed behavior.

Initial context budget:

- Read first:
  - This plan's header, readiness summary, execution graph, and this task packet.
  - `AGENTS.md`.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmation.kt`.
  - `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmationTest.kt`.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt`.
  - `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/AiCommitAllWorkflowCoordinator.kt`.
  - `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeLlmCommitMessageAction.kt`.
  - `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`.
  - `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`.
  - `docs/specification.md` requirements `REQ-SEL-004`, `REQ-SEL-005`, and `REQ-SEL-008`.
- Escalate to:
  - `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitStageSelectionItems.kt` only if staged-path predicates require adjustment.
  - IntelliJ 2026.x GitStageTracker sources only if reading the current snapshot has compatibility ambiguity.

Allowed inputs:

- Files and artifacts named in `Read first`.
- Files named in `Escalate to` only after the matching trigger fires.
- The retained IDEA trace showing about 4.3 seconds of bounded refresh waits before direct index confirmation succeeded.

Forbidden inputs:

- Unrelated archived plans.
- Previous worker chat beyond the orchestrator handoff summary.
- AI completion, commit execution, push, release, or Marketplace implementation files other than the explicitly named AI-phase coordinator and fake-action integration seam.

Write scope:

- `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmation.kt`.
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmationTest.kt`.
- `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt`.
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`.
- `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/AiCommitAllWorkflowCoordinator.kt`.
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/AiCommitAllWorkflowRunnerTest.kt`.
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeLlmCommitMessageAction.kt`.
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`.
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`.
- Escalation-only source/test files explicitly named above.
- `docs/specification.md`.
- `CHANGELOG.md`.
- This plan's status, result summary, and continuity sections.

Dependencies:

- Plan status must be `Approved` with `Approved by:` and `Approved at:` recorded.
- Diagnostic commit `0a3820f` must remain in the implementation base.

Validation:

- Red first: a test expecting direct index-confirmed staging to avoid both blocking IDE refresh calls.
- Red first: a test proving AI is not invoked until the required Commit UI model handoff exposes every expected staged path.
- Green: fast-path staged, HEAD-identical, multi-root, and mixed-path cases; fallback unconfirmed, Git error, retry, and final failure cases.
- `./gradlew.bat test --tests "*GitStageConfirmationTest"`.
- `./gradlew.bat test`.
- `./gradlew.bat spotlessCheck detekt`.
- `./gradlew.bat buildPlugin`.
- The focused staging-enabled release-matrix integration test, including an assertion on the exact paths observed by the fake `Vcs.LLMCommitMessageAction` through `CommitWorkflowUi`.
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`.
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`.
- `git diff --check`.
- Manual sandbox scenario when practical: trigger Commit and switch away immediately; verify the plugin no longer spends the two bounded IDE-refresh waits before scheduling AI, the required Commit UI model handoff completes before AI invocation, and the generated message reflects the selected staged content.
- Self-review per `.agents/references/reviews.md`, prioritizing incomplete staging, HEAD-identical paths, multi-root selection, and fail-closed fallback.

Escalation triggers:

- Load IntelliJ 2026.x GitStageTracker sources if a current tracker snapshot cannot be safely used as the base for index-confirmed state synthesis.
- Load `GitStageSelectionItems.kt` if immediate Git index checks cannot distinguish staged, HEAD-identical, and worktree-only paths with the existing operations.
- Stop and report if the required Commit UI model cannot be updated and verified on the same EDT turn as AI invocation without waiting for another refresh callback.
- Stop and report if a test shows the fast path can commit less than the selected scope.
- Stop and report if the change requires modifying commit execution or push behavior.

Stop conditions:

- The plan is not explicitly approved.
- Direct index confirmation cannot prove every expected path before AI generation.
- `CommitWorkflowUi.includedChanges` does not expose every expected staged path immediately before the AI action is invoked.
- The fallback path would become less bounded or less fail-closed.
- Implementation would require proprietary AI Assistant APIs.

Expected output:

- Focused staging implementation and regression tests.
- TDD red/green evidence and full validation results.
- Self-review evidence for staging correctness and fallback safety.
- One plan-task commit with worker/orchestrator metadata.
- Structured worker events and orchestrator reconciliation.
- Updated specification, changelog, plan status, and result summary.
- Any blockers, residual IDE compatibility risk, and manual sandbox gap.

Result summary:

- Status: done
- Worker: W1 `/root/t1_fast_path_staging` (implementation, mode code).
- Changed files or reviewed diff: `GitStageConfirmation.kt`, `ReflectiveCommitWorkflowSynchronizer.kt`, `AiCommitAllWorkflowCoordinator.kt`, their focused unit tests, the fake AI action/probe, the focused release-matrix harness test, `docs/specification.md`, and `CHANGELOG.md`; plan lifecycle and catalog archived by the orchestrator.
- Validation evidence: red tests captured the old blocking refresh path and absent/fail-open UI handoff; focused green suites passed 80 tests; full `test` passed 491 tests with 1 skip; `spotlessCheck detekt`, `buildPlugin`, docs validation, agent-artifact validation, and `git diff --check` passed; focused IU 2026.1.2 staging integration passed with exact equality of 8 expected and 8 `CommitWorkflowUi` paths.
- Self-review evidence: W1 and orchestrator checked incomplete staging, HEAD-identical paths, rename source and target, unversioned files, multi-root selection, bounded fallback, same-EDT AI ordering, and platform compatibility; no remaining findings.
- Commit: this T1 plan-task commit with `Project-Worker: W1`, `Project-Orchestrator: O1`, and plan-task metadata.
- Worker events: structured W1 start and stop events recorded in the orchestrator chat; active worker count returned to zero before reconciliation.
- Orchestrator reconciliation: final diff stayed within the packet, worker validation claims matched result XML/artifacts, IntelliJ IDEA 2026.1.2 bytecode confirmed tracker state retains every configured Git root, and the changelog/archive decisions were applied by O1.
- Changelog/docs/spec/tasks updates: `REQ-SEL-008` and traceability updated; an Unreleased Fixed entry added; plan closed and archived; no `TASKS.md` change applies because T1 exists only in this approved plan.
- Blockers: none.
- Review risks: the required Git staging UI APIs remain a 2026.x IntelliJ compatibility boundary covered by the real-IDE test; manual focus-switch sandbox was not run; a separately starved EDT remains outside this fix.
- Handoff notes and next action: no plan work remains. Release preparation stays separate and was not requested.

## Execution Model

- `Workers: 1`; after approval, one fresh implementation sub-agent executes T1 under the orchestrator.
- If sub-agents are unavailable or forbidden, stop before implementation rather than executing this approved-plan task locally.
- Use the current branch; the worker owns only the task packet write scope and the orchestrator owns plan status and reconciliation.
- Commit T1 after implementation, validation, and self-review with `Project-Source: plan-task`, `Project-Plan: PLAN-fast-path-staging-confirmation`, and `Project-Plan-Task: T1-fast-path-staging-confirmation`.

## Long-Run Continuity

- Resume docs reread: after compaction, interruption, resume, or handoff, reread `AGENTS.md`; this plan's header, `## Readiness`, `## Execution Model`, current task packet, and result summary; `.agents/references/execution.md`; `.agents/references/orchestration.md`; `.agents/references/testing.md`; `.agents/references/reviews.md`; and `.gitmessage` before committing.
- Current task or wave: none; T1 complete.
- Completed commits: diagnostic baseline originally executed from `0a3820f`; PR rebase replaced it with patch-equivalent `544cade` from `main`, followed by this T1 plan-task commit.
- Plan status and readiness: Closed and archived; Close-Reason: Archived.
- Validation and self-review state: complete; required automated checks passed, with the manual focus-switch sandbox skipped because Starter coverage proves the plugin-owned boundary but cannot reproduce external EDT starvation.
- Worker event state: W1 stopped successfully; active worker count zero.
- Orchestrator reconciliation state: complete; no findings or unresolved scope conflicts.
- Changelog, docs, spec, task, or plan updates: Unreleased changelog and `REQ-SEL-008` updated; plan and catalog archived; no task-list change applies.
- Blockers or open questions: none.
- Next action: optional later release preparation; do not treat this archive as a release action.
- Context handoff notes: direct index confirmation now removes plugin-owned refresh waiting, but the required same-EDT Commit UI handoff still waits for its EDT turn and cannot solve a separately starved EDT.

## Execution Graph

```mermaid
flowchart TD
    O1["O1[code]<br/>orchestrator"]
    W1["W1[code]<br/>T1-fast-path-staging-confirmation"]
    O1 --> W1
```

## Validation

- Validate plan structure, links, and Markdown with the repository documentation and agent-artifact scripts.
- During implementation, run the task packet's targeted TDD, full Gradle, formatting, static-analysis, packaging, docs, and diff checks.
- Keep the focus-switch reproduction as manual evidence; automated tests prove the plugin skips its blocking waits but cannot reproduce Windows/IDE scheduling.

## Risks

- Staging correctness: a stale tracker base must be fully amended with direct index confirmations for every expected path before continuation.
- HEAD-identical paths: they must remain satisfied without creating synthetic staged content.
- Fallback regression: any unconfirmed path must still take the bounded refresh/retry path and eventually stop rather than invoking AI.
- AI visibility: the installed AI action reads the Commit UI inclusion model, so handler-only state assignment or a later best-effort `setTrackerState` callback would produce stale or empty AI input.
- Residual EDT delay: eliminating plugin-owned refresh waits saves the bounded refresh time but cannot make a starved EDT execute sooner; the historical 134-second EDT queue delay remains a separate problem and the diagnostics originally authored in `0a3820f` and retained as `544cade` remain necessary.

## Handoff Notes

- No ADR is required: `REQ-SEL-008` already authorizes index-confirmed state and asynchronous best-effort visual refresh; this plan moves that confirmation earlier.
- This plan deliberately does not cancel delayed AI work. It also does not claim that Git index confirmation alone is visible to JetBrains AI; the required Commit UI model handoff is part of the fix.
- Implementation completed with exact-path evidence from the fake `Vcs.LLMCommitMessageAction` through `CommitWorkflowUi`; manual focus-switch reproduction remains a separate sandbox gap.
