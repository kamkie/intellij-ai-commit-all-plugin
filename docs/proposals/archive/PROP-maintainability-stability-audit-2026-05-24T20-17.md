---
proposal_id: PROP-maintainability-stability-audit
generated_at: 2026-05-24T20-17
created_from: User request to analyze the repository and propose maintainability, efficiency, stability, test coverage, and code quality improvements.
purpose: Propose evidence-backed repository improvements for maintainer triage after a local code, test, CI, and documentation audit.
scope: Covers plugin runtime code, tests, CI, Gradle validation, and governed repository documentation.
---

# Maintainability Stability Audit

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/specification.md`, `docs/validation/scenario-register.md`, `docs/decisions/`, and `docs/proposals/README.md`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Creation Context](#creation-context)
- [Progress Tracker](#progress-tracker)
- [Proposal Items](#proposal-items)
  - [New Features](#new-features)
    - [F001. Add package-level coverage targets for critical workflow and VCS code](#f001-add-package-level-coverage-targets-for-critical-workflow-and-vcs-code)
    - [F002. Extend deterministic release-matrix UI automation beyond IDEA](#f002-extend-deterministic-release-matrix-ui-automation-beyond-idea)
  - [Errors And Mistakes](#errors-and-mistakes)
    - [E001. Pull-request CI does not run the full documentation validator](#e001-pull-request-ci-does-not-run-the-full-documentation-validator)
    - [E002. Safe immediate push completion ignores the Git push result](#e002-safe-immediate-push-completion-ignores-the-git-push-result)
    - [E003. Compatibility-boundary failures are often silent](#e003-compatibility-boundary-failures-are-often-silent)
  - [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
  - [Simplification Opportunities](#simplification-opportunities)
    - [S001. Split the three-section Swing control into reviewable units](#s001-split-the-three-section-swing-control-into-reviewable-units)
    - [S002. Move custom Gradle verification tasks out of the root build script](#s002-move-custom-gradle-verification-tasks-out-of-the-root-build-script)
  - [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The strongest stability finding is the safe immediate push completion path: it waits for Git push completion events but currently discards `GitPushRepoResult`, so workflow state cannot distinguish successful pushes from failed completed pushes.
- The strongest process finding is PR CI documentation validation: release CI runs the full docs validator, but normal CI runs only the agent-artifact validator despite the repository validation guide naming `scripts/validate-docs.ps1` as the full documentation check.
- Test coverage is healthy overall, with `.\gradlew.bat test jacocoTestReport verifyJacocoCoverageReport` passing at 276 tests, 1 pending test, 71.2% line coverage, and 64.2% branch coverage; the remaining useful coverage work is package- and class-targeted, not broad test volume.
- Several recommendations are maintainability cleanup rather than urgent fixes: split the large Swing control, extract custom Gradle tasks if build logic keeps growing, and make compatibility fallback diagnostics easier to read.
- No implementation is performed by this proposal.

## Creation Context

- Why this proposal exists: the maintainer asked for a repository analysis with proposed changes to improve maintainability, efficiency, stability, test coverage, code quality, and other relevant areas.
- How it was created: local audit of `src/main`, `src/test`, `src/integrationTest`, `.github/workflows`, `build.gradle.kts`, `TASKS.md`, `docs/specification.md`, and `docs/validation/scenario-register.md`; local validation command `.\gradlew.bat test jacocoTestReport verifyJacocoCoverageReport` passed.
- Scope guardrails: the proposal follows the repository review priority order for commit selection, AI invocation, commit timing, push behavior, IntelliJ compatibility, and missing validation. It does not supersede the open release-validation task `T-VAL-024`.

## Progress Tracker

Compact overview only. The metadata table inside each finding remains the source of truth; this table mirrors statuses at a glance. Tracker mirroring, status and decision vocabulary, and Proposal Implementation Summary updates live in `docs/proposals/README.md`.

| Id   | Title                                                                 | Priority | Status       | Decision |
|------|-----------------------------------------------------------------------|----------|--------------|----------|
| F001 | Add package-level coverage targets for critical workflow and VCS code | 3        | not-required | rejected |
| F002 | Extend deterministic release-matrix UI automation beyond IDEA         | 5        | done         | accepted |
| E001 | Pull-request CI does not run the full documentation validator         | 2        | done         | accepted |
| E002 | Safe immediate push completion ignores the Git push result            | 1        | done         | accepted |
| E003 | Compatibility-boundary failures are often silent                      | 4        | done         | accepted |
| S001 | Split the three-section Swing control into reviewable units           | 6        | done         | accepted |
| S002 | Move custom Gradle verification tasks out of the root build script    | 6        | done         | accepted |

## Proposal Items

### New Features

#### F001. Add package-level coverage targets for critical workflow and VCS code

| Field       | Value                     |
|-------------|---------------------------|
| Status      | not-required              |
| Decision    | rejected                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 3                         |
| Owner       |                           |
| Updated     | 2026-05-24T20:53:28+02:00 |

##### Context

- Evidence: `build.gradle.kts` currently enforces only overall JaCoCo thresholds: 68% line and 62% branch coverage (`build.gradle.kts:374`, `build.gradle.kts:379`, `build.gradle.kts:380`).
- Evidence: local validation passed, but package coverage is uneven: `workflow` is 61.4% line / 62.4% branch and `vcs` is 57.8% line / 46.6% branch from `build/reports/jacoco/test/jacocoTestReport.xml`.
- Evidence: class-level coverage shows some high-risk integration boundaries at 0% unit coverage, including `GitPushCompletionService`, `CommitWorkflowSelectionService`, `GitChangeSelectionService`, and `ProjectAiCommitAllWorkflowDependencies`.
- Impact: overall thresholds can pass while the most consequential workflow and VCS packages remain below the repository average.
- Non-goals:
  - Do not require unit tests for every IntelliJ service adapter when release-matrix UI automation is the better proof.
  - Do not replace manual live AI Assistant validation required by `T-VAL-024`.
- Acceptance criteria:
  - Critical packages have explicit coverage reporting or thresholds, or a documented reason why package-level thresholds are not useful.
  - New tests target behavior risk, not line count.
  - Existing overall JaCoCo gates remain in place.

##### Recommended Change

Add package-level JaCoCo reporting or a small verification task for `pl.devopssolutions.aicommitall.workflow`, `pl.devopssolutions.aicommitall.vcs`, and `pl.devopssolutions.aicommitall.ai`. Start with report-only output or modest thresholds, then raise them after adding targeted tests for push completion, selection preparation, and project dependency wiring.

##### Review Notes

- none

##### Follow-Up

- Artifact: task or approved plan if accepted.
- Validation: `.\gradlew.bat test jacocoTestReport verifyJacocoCoverageReport` plus any new package-threshold task.

#### F002. Extend deterministic release-matrix UI automation beyond IDEA

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 5                         |
| Owner       |                           |
| Updated     | 2026-05-24T22:18:22+02:00 |

##### Context

- Evidence: the Gradle release-matrix UI task rejects non-IDEA products with the message `releaseMatrixUiTest currently supports IDEA only` (`build.gradle.kts:418`).
- Evidence: the GitHub release-matrix UI workflow repeats that limitation in its input description and job name (`.github/workflows/release-matrix-ui.yml:7`, `.github/workflows/release-matrix-ui.yml:21`, `.github/workflows/release-matrix-ui.yml:41`).
- Evidence: the release checklist asks maintainers to use IDEA first and repeat representative coverage in PyCharm and WebStorm where practical (`docs/validation/release-checklist.md:39`, `docs/validation/release-checklist.md:44`, `docs/validation/release-checklist.md:45`, `docs/validation/release-checklist.md:46`).
- Impact: product-specific Commit tool window, keymap, AI Assistant, and VCS differences remain manual until `T-VAL-024` is executed.
- Non-goals:
  - Do not block the first Marketplace publication solely on automating every product-specific manual case.
  - Do not remove the manual matrix from the release checklist.
- Acceptance criteria:
  - The deterministic UI lane can run at least a small smoke subset in PyCharm and WebStorm, or the repository documents why those products must remain manual.
  - The existing IDEA lane remains the full deterministic harness.

##### Recommended Change

Add a product-parameterized smoke subset for PyCharm and WebStorm that verifies plugin load, Commit tool window visibility, control placement, shortcut routing, and one fake-AI stop path. Keep the deep commit/push fixture lane in IDEA unless cross-product fixture stability proves practical.

##### Review Notes

- none

##### Follow-Up

- Artifact: approved plan if accepted because this changes validation scope and likely touches Gradle, CI, and integration tests.
- Validation: product matrix `releaseMatrixUiTest` runs plus `verifyPlugin` for `IU`, `PY`, and `WS`.

### Errors And Mistakes

#### E001. Pull-request CI does not run the full documentation validator

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-24T21:32:42+02:00 |

##### Context

- Evidence: normal CI labels a step `Validate documentation and agent artifacts`, but it runs only `scripts/ai/validate-agent-artifacts.ps1` (`.github/workflows/ci.yml:45`, `.github/workflows/ci.yml:47`).
- Evidence: release CI runs `scripts/validate-docs.ps1` before publishing (`.github/workflows/release.yml:86`, `.github/workflows/release.yml:88`).
- Evidence: the full docs validator runs Markdown linting and then invokes the agent-artifact validator (`scripts/validate-docs.ps1:37`, `scripts/validate-docs.ps1:57`, `scripts/validate-docs.ps1:59`, `scripts/validate-docs.ps1:68`).
- Evidence: repository testing guidance names `scripts/validate-docs.ps1` as the documentation-structure, link, ADR, and proposal tracker check (`.agents/references/testing.md:10`).
- Impact: PRs can merge Markdown, link, ADR, task, open-question, or proposal tracker regressions that release CI catches only later.
- Non-goals:
  - Do not remove the agent-artifact validator; the full docs validator already calls it.
  - Do not force plugin builds for documentation-only changes.
- Acceptance criteria:
  - PR CI runs the full documentation validator.
  - Agent-artifact validation remains covered.
  - Release CI keeps the same or stronger documentation gate.

##### Recommended Change

Change the CI documentation step to run `scripts/validate-docs.ps1`. If runtime is a concern, keep a separate path-filtered agent-artifact-only job only for quick feedback, but make full docs validation required before merge.

##### Review Notes

- none

##### Follow-Up

- Artifact: direct CI edit or small task if accepted.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` and `.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.ci.GitHubActionsWorkflowTest"`.

#### E002. Safe immediate push completion ignores the Git push result

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 1                         |
| Owner       |                           |
| Updated     | 2026-05-24T21:28:30+02:00 |

##### Context

- Evidence: `GitPushCompletionListener.onCompleted` receives `GitPushRepoResult` but discards it and forwards only the repository (`src/main/java/pl/devopssolutions/aicommitall/vcs/GitPushCompletionListener.java:34`, `src/main/java/pl/devopssolutions/aicommitall/vcs/GitPushCompletionListener.java:36`, `src/main/java/pl/devopssolutions/aicommitall/vcs/GitPushCompletionListener.java:38`).
- Evidence: `GitPushCompletionService` completes waiting futures with `Unit` when all repositories have completed, with no success/failure state (`src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitPushCompletionService.kt:41`, `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitPushCompletionService.kt:73`, `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitPushCompletionService.kt:88`).
- Evidence: the safe immediate push plan waits on that completion future before the workflow finishes (`src/main/kotlin/pl/devopssolutions/aicommitall/vcs/SafeImmediatePushService.kt:211`, `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/SafeImmediatePushService.kt:213`, `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/SafeImmediatePushService.kt:217`).
- Impact: the running indicator can end as if push completion were successful even when the platform reports a failed push result, and a missing completion event can leave the workflow future unresolved until project disposal.
- Non-goals:
  - Do not replace platform-owned push error UI.
  - Do not add a plugin-specific push confirmation dialog.
- Acceptance criteria:
  - Push completion represents success, cancellation, and failure according to available platform result data.
  - Missing completion events are bounded by a timeout or another platform-backed completion signal.
  - Immediate push tests cover success, failed push result, multi-repository partial completion, and missing-event timeout.

##### Recommended Change

Change `GitPushCompletionListener` and `GitPushCompletionService` to preserve `GitPushRepoResult` per repository. Complete the workflow normally only when all repositories succeed; complete exceptionally or with an explicit failure result when any repository fails or times out, while leaving platform push errors visible through IntelliJ.

##### Review Notes

- none

##### Follow-Up

- Artifact: approved plan or focused bug task if accepted.
- Validation: targeted `GitPushCompletionService` tests, `SafeImmediatePushServiceTest`, `CommitWorkflowExecutionServiceTest`, and a local-remote integration or release-matrix push-failure scenario where practical.

#### E003. Compatibility-boundary failures are often silent

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-24T21:40:37+02:00 |

##### Context

- Evidence: AI generation running-state detection reflects into a private `progressIndicator` field and maps any failure to `Unavailable` (`src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletion.kt:331`, `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletion.kt:333`, `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletion.kt:343`).
- Evidence: commit workflow synchronization uses reflection and returns false on failures (`src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt:61`, `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt:79`, `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt:103`, `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt:138`, `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt:143`).
- Evidence: staging-area collection also hides failures behind an empty list (`src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitChangeSelectionService.kt:80`, `src/main/kotlin/pl/devopssolutions/aicommitall/vcs/GitChangeSelectionService.kt:87`).
- Impact: fail-closed behavior is correct for safety, but silent compatibility failures make IDE-version drift hard to diagnose from user reports or validation logs.
- Non-goals:
  - Do not surface noisy plugin-owned notifications for platform-owned errors.
  - Do not change the fail-closed safety policy.
- Acceptance criteria:
  - Compatibility-boundary failures are logged or recorded at debug/info level with class, method, and exception context.
  - User-facing behavior remains unchanged unless an accepted ADR or spec change says otherwise.
  - Tests verify diagnostics are emitted without changing stop reasons.

##### Recommended Change

Introduce a narrow diagnostics boundary for reflection and platform-compatibility fallbacks. Use IntelliJ logging for maintainer-visible diagnostics and keep the existing stop behavior and platform-owned user messages intact.

##### Review Notes

- none

##### Follow-Up

- Artifact: small task or approved plan if accepted.
- Validation: targeted unit tests for diagnostic emission and `verifyPlugin` across supported IDE products.

### Duplications To Remove Or Reduce

_No tracked findings._

### Simplification Opportunities

#### S001. Split the three-section Swing control into reviewable units

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 6                         |
| Owner       |                           |
| Updated     | 2026-05-24T22:31:10+02:00 |

##### Context

- Evidence: `AiCommitAllThreeSectionControl.kt` contains the component, model, interaction handler, renderer, geometry, drawing constants, and test peer in one file (`src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt:47`, `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt:159`, `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt:200`, `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt:296`, `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt:490`, `src/main/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllThreeSectionControl.kt:885`).
- Evidence: the asset generator test is also a large multi-purpose test/helper and is skipped unless an environment flag is set (`src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllControlAssetGeneratorTest.kt:38`, `src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllControlAssetGeneratorTest.kt:41`, `src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllControlAssetGeneratorTest.kt:47`).
- Impact: UI rendering changes require reviewers to scan unrelated input, geometry, color, animation, accessibility, and asset-generation concerns in the same files.
- Non-goals:
  - Do not redesign the control.
  - Do not change user-visible labels, colors, shortcuts, or rendering behavior as part of the split.
- Acceptance criteria:
  - Model, interaction, geometry, rendering, and asset-generation helpers are separated enough that a renderer-only change has a narrow diff.
  - Existing UI behavior and asset outputs remain byte-for-byte identical unless a deliberate visual update is accepted.

##### Recommended Change

Split the control into package-private Kotlin files such as `AiCommitAllThreeSectionControl.kt`, `ThreeSectionControlModel.kt`, `ThreeSectionControlInteraction.kt`, `ThreeSectionControlRenderer.kt`, and `ThreeSectionControlGeometry.kt`. Extract the asset generator's rendering helpers into a small test support object so the skipped generator test is mostly orchestration.

##### Review Notes

- none

##### Follow-Up

- Artifact: approved refactor plan if accepted, because the file is high-visibility UI code.
- Validation: existing `AiCommitAllThreeSectionControlTest`, asset-generator dimensions or hash checks, and release-matrix screenshots if visuals are intentionally touched.

#### S002. Move custom Gradle verification tasks out of the root build script

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T20:53:28+02:00 |
| Priority    | 6                         |
| Owner       |                           |
| Updated     | 2026-05-24T22:41:45+02:00 |

##### Context

- Evidence: the root `build.gradle.kts` owns plugin configuration, dependencies, versioning, release-matrix wiring, and custom XML-parsing verification task implementations (`build.gradle.kts:11`, `build.gradle.kts:134`, `build.gradle.kts:182`, `build.gradle.kts:279`, `build.gradle.kts:463`).
- Impact: the root build script is becoming both declarative build configuration and a home for reusable verification code.
- Non-goals:
  - Do not introduce build indirection until it reduces real complexity.
  - Do not change validation behavior.
- Acceptance criteria:
  - Root build configuration remains easy to scan.
  - Custom tasks have focused tests or at least stay covered by existing Gradle validation.
  - Configuration cache remains enabled and passing.

##### Recommended Change

If another custom Gradle task is added, move existing task classes into `buildSrc` or an included convention plugin. Keep task registration and project-specific thresholds in `build.gradle.kts`.

##### Review Notes

- none

##### Follow-Up

- Artifact: direct refactor or approved plan if accepted.
- Validation: `.\gradlew.bat spotlessCheck test jacocoTestReport verifyJacocoCoverageReport verifyPluginStructure buildPlugin`.

### Smaller / Stylistic Items

- Consider replacing the skipped asset-generation test with a dedicated Gradle task if asset refreshes become a common maintainer workflow. The current guarded test is acceptable for infrequent refreshes.
- Keep `T-VAL-024` as the only active backlog item until release validation is done; the findings above should not distract from Marketplace readiness unless the maintainer explicitly accepts one.

## Suggested Priority Order

1. `E002` - push completion state is the highest behavior risk because it affects the user-visible commit-and-push flow.
2. `E001` - PR documentation validation is cheap and prevents avoidable release-gate failures.
3. `E003` - diagnostics reduce future compatibility-debugging time without changing behavior.
4. `F002` - cross-product UI automation is valuable but heavier and partly covered by manual release validation.
5. `S001` and `S002` - useful cleanup once behavior and release gates are stable.

`F001` was rejected during maintainer triage and is not part of the accepted implementation order.

## Out Of Scope

- Implementing any code, CI, Gradle, or documentation changes from these findings.
- Changing product behavior, plugin defaults, AI Assistant integration policy, safe-push policy, supported IDE versions, or Marketplace release timing.
- Replacing `T-VAL-024` or the existing manual release checklist.
