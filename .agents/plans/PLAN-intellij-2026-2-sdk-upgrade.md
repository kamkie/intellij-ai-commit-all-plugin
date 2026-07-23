# Plan: IntelliJ 2026.2 SDK Upgrade

Plan-ID: PLAN-intellij-2026-2-sdk-upgrade

Status: In Progress

Workers: 1

Filename: `.agents/plans/PLAN-intellij-2026-2-sdk-upgrade.md`

## Readiness

- Plan readiness: Ready; the original plan and the bounded `T3R-regenerate-marketplace-change-notes`, `T5R-stabilize-pycharm-ui-startup`, `T5D-align-published-pycharm-documentation`, `T5R2-synchronize-pycharm-module-reload`, `T5R3-observe-pycharm-reload-at-startup`, `T5R4-observe-pycharm-loading-dialog`, `T5R5-await-pycharm-enable-attempt`, `T5R6-rebuild-pycharm-staging-workflow`, `T5R7-classify-2026-2-scheme-race`, `T5R8-cover-262-reflection-failures`, `T5R9-classify-2026-2-closed-index-storage`, `T5R10-cover-residual-reflection-branches`, `T5R11-simplify-reflection-null-guards`, `T5R12-handle-license-restart-transition`, `T5R13-relaunch-starter-after-license-restart`, and `T5R14-extend-license-restart-to-pycharm` remediation packets are explicitly approved.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-07-16T21:17:58+02:00
- Open questions: None; the maintainer directed that `PY-2026.2` remain required and be allowed to fail until JetBrains publishes it.
- Implementation progress: T1 through T4, T3R, T5R, T5D, T5R6, T5R7, T5R8, T5R9, T5R11, and T5R14 are complete. T5R14 committed the exact IU/PyCharm 2026.2 license-restart lifecycle as `ebe04440359812b75d05459b499e3cdf7ef5b6df`; synthetic and active-license PyCharm probes, full PyCharm 13/13, full IntelliJ IDEA 25/25, and focused/static checks passed. Push the reconciled head, then execute a fresh T5 exact-head gate.

## Status History

- 2026-07-16T21:02:39+02:00: none -> Draft by Codex <codex@openai.com>; companion plan created for proposed ADR 0089.
- 2026-07-16T21:17:58+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicit user approval recorded.
- 2026-07-16T21:18:00+02:00: Approved -> In Progress by Codex <codex@openai.com>; approved implementation started.
- 2026-07-16T22:15:11+02:00: In Progress -> Blocked by Codex <codex@openai.com>; T4 found stale generated Marketplace change notes and requires an explicitly approved remediation packet.
- 2026-07-16T22:25:25+02:00: Blocked -> In Progress by Kamil Kiewisz <kamkie@outlook.com>; explicit approval to execute T3R and continue the review-fix-validation loop recorded.
- 2026-07-23T10:43:36+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the maintainer's standing instruction to execute the normal change-review-fix loop without stopping, together with the request to check and merge PR #37, approves the bounded T5R remediation packet after the first published-PyCharm gate failure.
- 2026-07-23T11:45:36+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5D packet after final review found current public documentation that still described published PyCharm 2026.2 as unavailable.
- 2026-07-23T11:58:50+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R2 packet after hosted validation proved T5R opens a paid-product modal on an unlicensed Linux runner.
- 2026-07-23T12:27:22+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R3 packet after T5R2 proved every Driver-time state or listener has an unavoidable pre-reload gap.
- 2026-07-23T12:45:18+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R4 packet after T5R3 proved `PlatformTaskSupport` does not publish `ProgressManagerListener` for the startup loading dialog.
- 2026-07-23T12:57:24+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R5 packet after T5R4 proved the loading modal lives in the split frontend and cannot be observed from the backend fake-plugin process.
- 2026-07-23T13:14:12+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R6 packet after T5R5 proved completion of rejected Ultimate loading is deterministic but does not itself restore the staging workflow fixture.
- 2026-07-23T14:26:40+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R7 packet after exact-head hosted evidence proved Starter promotes one branch-262 `SchemeManagerImpl` concurrent-mutation diagnostic during the already-known Islands Dark scheme lifecycle.
- 2026-07-23T15:30:17+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R8 packet after delayed exact-head Codecov checks isolated the only remaining failure to uncovered branch-262 reflection failure paths.
- 2026-07-23T16:15:06+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R9 packet after exact-head hosted evidence proved Starter promotes a branch-262 stub-index storage race during PyCharm's dynamic plugin reload.
- 2026-07-23T17:00:15+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R10 packet after exact-head Codecov proved the aggregate project gate green but the patch gate remained below target on residual reflection-method branches.
- 2026-07-23T17:11:17+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same standing review-fix instruction approves the bounded T5R11 packet after T5R10 proved behavioral cases cannot execute compiler null guards dominated by prior missing-method checks.
- 2026-07-23T18:05:09+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the maintainer explicitly requests recurring deterministic handling for the exact license-required `Confirm Restart` transition that blocked the local IU lane.
- 2026-07-23T18:40:31+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same explicit recurring-handling request and normal fix loop approve T5R13 after runtime proof showed a real restart needs outer Starter context reacquisition.
- 2026-07-23T19:52:30+02:00: In Progress continued by Kamil Kiewisz <kamkie@outlook.com>; the same recurring-handling request and normal fix loop approve T5R14 after PyCharm 2026.2 reproduced the exact license restart dialog and write-intent lock outside T5R13's IU-only gate.

## Goal

Advance the minimum supported IntelliJ Platform from 2026.1 to 2026.2, migrate the Java and compatibility-sensitive VCS/Commit boundaries required by branch 262, and prepare the complete required product matrix while leaving the pull request draft until PyCharm 2026.2 is available and passes.

## Non-Goals

- Do not retain 2026.1 compatibility through a second artifact, version branch, shim, fallback, or feature flag.
- Do not suppress, conditionally skip, or mark the unavailable PyCharm 2026.2 lane as successful.
- Do not change Git-only or all-IDE scope, AI Assistant policy, commit/push behavior, Marketplace channels, signing, or release versioning.
- Do not upgrade Gradle, Kotlin, IntelliJ Platform Gradle Plugin, tests, or unrelated dependencies unless direct 2026.2 validation proves it necessary.
- Do not refactor unrelated VCS or workflow code.

## Assumptions

- Accepted ADR 0089 will supersede ADR 0008 and make branch 262 the minimum.
- IntelliJ IDEA `2026.2` is `IU-262.8665.258`, WebStorm `2026.2` is `WS-262.8665.259`, and AI Assistant `262.8665.258` is the exact build-SDK dependency.
- IntelliJ Platform Gradle Plugin `2.18.1` maps branch 262 to Java 25; Java and Kotlin targets must both use 25.
- `bundledModule(...)` is the supported build-classpath mechanism for the VCS/DVCS modules split out in 262.
- Existing fail-closed Commit and staging semantics remain authoritative.
- The PR may remain red solely because `PY-2026.2` is unpublished. Every other failure is an implementation defect.

## Open Questions

No open plan questions. ADR acceptance and plan approval are required lifecycle gates, not unresolved design questions.

## Proposed Changes

- `T1-platform-and-vcs-migration`: update the platform, Java, module, AI Assistant, and compatibility-sensitive Kotlin boundaries until the 262 plugin builds and tests pass.
- `T2-ci-and-release-matrix`: update CI, release, local validation, and workflow contract tests to JDK 25 and the required 2026.2 matrix.
- `T3-support-and-product-docs`: update specification, user/support/contributor docs, Marketplace description, issue template, and changelog.
- `T3R-regenerate-marketplace-change-notes`: regenerate the single stale Marketplace change-notes artifact created from the orchestrator-owned Unreleased changelog entry.
- `T4-available-product-validation`: validate all available 2026.2 products and record the expected PyCharm availability failure.
- `T5-pycharm-release-gate`: after PyCharm 2026.2 is published, rerun the unchanged required lane and full current-head readiness gate.
- `T5R-stabilize-pycharm-ui-startup`: synchronize the release-matrix harness with PyCharm's first-session product-plugin reconfiguration after the hosted lane exposed a Linux-only race.
- `T5D-align-published-pycharm-documentation`: replace the now-stale public availability wording and regenerate Marketplace change notes before final readiness.
- `T5R2-synchronize-pycharm-module-reload`: remove the paid-startup option and wait for PyCharm's normal module reload plus AI Commit All action re-registration before a scenario begins.
- `T5R3-observe-pycharm-reload-at-startup`: register the reload observer from the fake AI plugin descriptor so it captures `Loading Plugins` before the Driver can enter a scenario.
- `T5R4-observe-pycharm-loading-dialog`: install an AWT observer from an app-start listener and record the actual `Loading Plugins` dialog open/close lifecycle.
- `T5R5-await-pycharm-enable-attempt`: register an early `DynamicPluginEnabler` state listener in the fake AI plugin and wait until the Ultimate-module enable/load attempt has returned before a PyCharm scenario can begin.
- `T5R6-rebuild-pycharm-staging-workflow`: retain the exact enable-attempt barrier and deterministically create or rebuild the requested Git staging Commit workflow after PyCharm's rejected Ultimate-module reload.
- `T5R7-classify-2026-2-scheme-race`: extend the existing version-gated test-reporter mapping to the exact `SchemeManagerImpl`/`EditorColorsManagerImpl`/`FileStatusImpl` concurrent-mutation stack produced by the known Islands Dark startup lifecycle.
- `T5R8-cover-262-reflection-failures`: add behavioral unit coverage for invocation failure, missing nested boundary methods, and incompatible reflective results in the branch-262 Git staging access boundary.
- `T5R9-classify-2026-2-closed-index-storage`: extend the version-gated test-reporter mapping to the exact stub per-file-version `ClosedStorageException` stack produced while branch 262 tumbles indexes during dynamic plugin reload.
- `T5R10-cover-residual-reflection-branches`: add behavioral unit coverage for independently missing handler and nested-boundary methods in the branch-262 reflection boundary.
- `T5R11-simplify-reflection-null-guards`: preserve the same reflection diagnostics while constructing access only from explicitly proven non-null methods, removing unreachable compiler guards from the patch.
- `T5R12-handle-license-restart-transition`: handle the exact license-required restart lifecycle in the release-matrix harness before scenario actions, then re-establish IDE and plugin readiness without manual interaction or license bypass.
- `T5R13-relaunch-starter-after-license-restart`: treat only the exact marked restart/session-loss fingerprint as a completed preflight, close the stale Starter context, and run the scenario in a newly acquired IDE/Driver context.
- `T5R14-extend-license-restart-to-pycharm`: extend the proven exact restart/relaunch contract only to the observed PyCharm 2026.2 product, preserving fail-closed behavior for every near miss and unobserved product.

## Task Packets

### Task Packet: T1-platform-and-vcs-migration

Task id: T1-platform-and-vcs-migration

Lane: implementation

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `platform-docs-research`
- `plugin-test-tdd`

Goal:

- Produce a Java 25, `since-build=262` plugin that compiles, tests, and packages against IDEA 2026.2 without changing Commit, staging, or push behavior.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, `build.gradle.kts`, `gradle.properties`, `src/main/resources/META-INF/plugin.xml`, `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt`, and its matching test.
- Escalate to: exact IntelliJ 262 source, `.agents/references/code-style.md`, `.agents/references/testing.md`, and `.agents/references/reviews.md` when compile or validation evidence requires them.

Allowed inputs:

- The files in the write scope, accepted ADR 0089, JetBrains 262 source and documentation, and focused Gradle validation output.

Forbidden inputs:

- Unrelated archived plans, unrelated feature code, and prior worker transcripts beyond the orchestrator handoff.

Write scope:

- `build.gradle.kts`, `gradle.properties`, `src/main/resources/META-INF/plugin.xml`, `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt`, and `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`.

Dependencies:

- Accepted ADR 0089, approved plan, and no prior task.

Validation:

- Capture focused red evidence; run affected tests, full `test`, `buildPlugin`, `verifyPluginProjectConfiguration`, `spotlessCheck detekt`, self-review, and `git diff --check`; commit T1 before T2.

Escalation triggers:

- Escalate when no narrow 262 staging boundary preserves behavior, runtime modules alter product scope, or another dependency upgrade is required.

Stop conditions:

- A new product decision, behavior change, or broad compatibility abstraction is required.

Expected output:

- Passing 262 build foundation, regression evidence, task commit, risks, worker events, and orchestrator reconciliation.

Result summary:

- Status: completed
- Worker: `/root/t1_platform_vcs_migration`; corrective worker `/root/t1_integration_262_fix`.
- Changed files or reviewed diff: Initial `build.gradle.kts`, `gradle.properties`, `ReflectiveCommitWorkflowSynchronizer.kt`, and its test; corrective 262 Starter/VCS migration in `build.gradle.kts`, `ReleaseMatrixUiHarnessTest.kt`, and `FakeAiAssistantProbe.kt`.
- Validation evidence: Initial red compile captured missing 262 modules/internal handler; focused 25 tests passed; full 514 tests passed with 1 existing pending; `buildPlugin` and `verifyPluginProjectConfiguration` passed. Corrective validation passed `compileIntegrationTestKotlin`, `spotlessCheck`, `detekt`, `git diff --check`, a focused UI scenario, and the full IU 2026.2 UI lane with 21/21 tests.
- Self-review evidence from `.agents/references/reviews.md`: Exact-class reflective boundary fails closed; Commit/staging/push semantics remain unchanged; no proprietary AI compile dependency or product-scope expansion was added. The integration harness maps only exact, version-bounded upstream 2026.2 failures and synchronizes on observable VCS selection rather than sleeps or retries.
- Commit: `a7d4a5e635a1023b56f768d0bed915a278bce5b5`; corrective integration commit `bf3092218d3540650c27b23ccff2ad2ea04e8553`.
- Worker events: Initial worker preserved red job `20260716-213137-intellij-2026-2-t1-focused-red-f286f8` and completed focused/full green jobs. Corrective worker preserved the first cache and VCS-refresh failures, proved both alternating patterns with three focused reruns each, and completed full green job `20260717-013913-intellij-2026-2-t1-integration-ui-iu-final-r5`.
- Orchestrator reconciliation: Both workers' claims match their scoped commits, required commit metadata, generated `since-build=262` without `until-build`, and managed-job evidence; the final 21/21 run exercised the exact workspace-cache matcher while keeping unrelated failures red.
- Changelog/docs/spec/tasks updates: None in T1; compatibility docs remain assigned to T3 and changelog ownership remains with the orchestrator.
- Blockers: None.
- Review risks: Cross-product WebStorm smoke behavior remains for T4 validation.
- Handoff notes and next action: Dispatch T2 CI and release matrix.

### Task Packet: T2-ci-and-release-matrix

Task id: T2-ci-and-release-matrix

Lane: implementation

Required skills:

- `intellij-plugin-development`
- `plugin-test-tdd`
- `repository-documentation`

Goal:

- Move automation and local prerelease validation to JDK 25 and the required 2026.2 IDEA/PyCharm/WebStorm matrix without bypassing PyCharm.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, the seven workflows and prerelease script in the write scope, and the three CI contract tests.
- Escalate to: release/testing guidance and exact validation failures when required.

Allowed inputs:

- The files in the write scope, accepted ADR 0089, and their focused validation output.

Forbidden inputs:

- Plugin behavior source, unrelated workflows, and prior worker transcripts beyond the handoff.

Write scope:

- `.github/workflows/ci.yml`, `.github/workflows/codeql.yml`, `.github/workflows/dependency-submission.yml`, `.github/workflows/github-release.yml`, `.github/workflows/plugin-verifier.yml`, `.github/workflows/release.yml`, `.github/workflows/release-matrix-ui.yml`, `scripts/run-local-prerelease-validation.ps1`, and `src/test/kotlin/pl/devopssolutions/aicommitall/ci/`.

Dependencies:

- T1 committed and reconciled.

Validation:

- Run focused workflow tests and any shared suite, docs validation for executable docs, self-review that `PY-2026.2` is required with no skip or `continue-on-error`, and `git diff --check`; commit T2 before T3.

Escalation triggers:

- Escalate when a workflow tool cannot run on JDK 25 or an accepted product identifier is wrong.

Stop conditions:

- Passing CI would require hiding or weakening the PyCharm gate.

Expected output:

- Updated automation, contract-test evidence, expected unavailable-product behavior, task commit, events, and reconciliation.

Result summary:

- Status: completed
- Worker: `/root/t2_ci_release_matrix`
- Changed files or reviewed diff: Seven Gradle-running workflows, local prerelease validation script, and the three CI contract test classes.
- Validation evidence: Focused red produced 5 expected failures; focused green passed 25 tests; shared validation passed 516 tests with 1 existing pending plus `spotlessCheck`; docs, PowerShell syntax, YAML syntax, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: Every Gradle workflow uses JDK 25; IU/PY/WS 2026.2 remain required; no PyCharm skip or `continue-on-error` was added.
- Commit: `a29e97485a710c56306c637a8ce8578594f5992b`; corrective summary fix `ceb791e44ea0f1724f7c67450f438c6169ce8bdd`.
- Worker events: Started from clean `09e48eb`; red job `20260716-215419-intellij-2026-2-t2-contract-red-5b0258`; focused green `20260716-215608-intellij-2026-2-t2-contract-green-a29f69`; shared green `20260716-215741-intellij-2026-2-t2-shared-validation-9f67d8`; T4 later reproduced the final-summary `OrderedDictionary.ContainsKey` defect; fresh corrective worker proved `.Contains` with exact `-Resume`, 15 CI contract tests, syntax, and diff checks.
- Orchestrator reconciliation: Initial worker claims and corrective worker claims match their scoped commits and validation evidence; the only existing `continue-on-error` is the unrelated Detekt reporting flow.
- Changelog/docs/spec/tasks updates: Public compatibility changelog text suggested for orchestrator reconciliation after T3.
- Blockers: None for T3.
- Review risks: Missing PyCharm 2026.2 intentionally blocks the required CI/UI lanes until T5.
- Handoff notes and next action: Dispatch T3 support and product documentation.

### Task Packet: T3-support-and-product-docs

Task id: T3-support-and-product-docs

Lane: implementation

Required skills:

- `repository-documentation`
- `intellij-plugin-development`

Goal:

- Align all current compatibility statements with the accepted 2026.2 baseline and draft-readiness condition.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, the files in the write scope, and landed T1/T2 configuration.
- Escalate to: documentation/release guidance for generated artifacts when required.

Allowed inputs:

- The files in the write scope, accepted ADR 0089, and landed T1/T2 configuration.

Forbidden inputs:

- Unrelated archived plans, historical reports, and unrelated changelog sections.

Write scope:

- `README.md`, `docs/SUPPORT.md`, `docs/specification.md`, `docs/user-guide.md`, `docs/troubleshooting.md`, `CONTRIBUTING.md`, `.github/ISSUE_TEMPLATE/bug_report.yml`, and `config/intellij-platform/description.html`.

Dependencies:

- T2 committed and reconciled.

Validation:

- Run docs validation, relevant documentation/spec tests, self-review that no PyCharm pass is claimed, and `git diff --check`; commit T3 before T4.

Escalation triggers:

- Escalate when the Marketplace description cannot be reproduced or a new support/release decision appears.

Stop conditions:

- Correct wording contradicts ADR 0089 or landed configuration.

Expected output:

- Aligned docs, a suggested public compatibility changelog entry for the orchestrator, validation evidence, task commit, events, and reconciliation.

Result summary:

- Status: completed
- Worker: `/root/t3_support_product_docs`
- Changed files or reviewed diff: Eight approved user, support, specification, contributor, issue-template, and generated Marketplace description files.
- Validation evidence: Docs validation, Marketplace description parity, issue-template YAML parse, stale-version scan, scope check, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: No false PyCharm pass claim; compatibility/support wording matches branch 262, JDK 25, ADR 0089, and landed T1/T2 configuration.
- Commit: `a0e2d0122bf90c2af8e373f44ad78cddbabaa54b`
- Worker events: Started from clean `771c442`; mapped all eight scoped surfaces; generated Marketplace description from source docs; completed docs/parity/YAML/stale-version/scope validation.
- Orchestrator reconciliation: Worker claims match the committed eight-file diff, clean worktree, required commit metadata, documentation ownership, and validation evidence.
- Changelog/docs/spec/tasks updates: Specification and support/public docs aligned; orchestrator added the eligible Unreleased compatibility entry to `CHANGELOG.md`.
- Blockers: None for T4.
- Review risks: PyCharm 2026.2 availability remains the external readiness blocker; no product validation pass is claimed.
- Handoff notes and next action: Dispatch T4 available-product validation.

### Task Packet: T3R-regenerate-marketplace-change-notes

Task id: T3R-regenerate-marketplace-change-notes

Lane: implementation

Required skills:

- `repository-documentation`

Goal:

- Regenerate the Marketplace change-notes artifact from the approved Unreleased compatibility entry so the required generator check passes without changing release content.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, `CHANGELOG.md`, `config/intellij-platform/change-notes.html`, and `scripts/generate-intellij-platform-change-notes.ps1`.
- Escalate to: `.agents/references/documentation.md` and `.agents/references/releases.md` only if generator ownership or output scope is unclear.

Allowed inputs:

- The named changelog, generator, generated file, T4 blocked report, and validation output.

Forbidden inputs:

- Plugin source, build configuration, workflows, unrelated documentation, historical reports, and previous worker chat beyond the orchestrator handoff.

Write scope:

- `config/intellij-platform/change-notes.html`.

Dependencies:

- T3 committed and reconciled; T4 blocked report recorded; explicit approval of this remediation packet.

Validation:

- Run `scripts/generate-intellij-platform-change-notes.ps1 -Check`, docs validation, self-review that the generated output contains only current Unreleased public notes, and `git diff --check`; commit T3R before restarting T4.

Escalation triggers:

- Escalate if regeneration changes another file, includes released or internal plan content, or fails after a clean regeneration.

Stop conditions:

- Any content decision beyond reproducing the accepted `CHANGELOG.md` entry is required.

Expected output:

- Regenerated change notes, generator parity evidence, task commit, worker events, and orchestrator reconciliation.

Result summary:

- Status: completed
- Worker: `/root/t3r_change_notes`
- Changed files or reviewed diff: `config/intellij-platform/change-notes.html` only; two current public Unreleased entries generated, with released content unchanged.
- Validation evidence: Change-notes generator parity, docs validation, post-commit parity recheck, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: Exact one-file generated output; no internal plan content or released-note modification.
- Commit: `d736a9120b599fd04e8ab0a19dbd7f28d7b4fac6`
- Worker events: Started from clean `562af0c`; ran the generator; verified exact one-file scope; completed parity/docs/diff checks and post-commit parity.
- Orchestrator reconciliation: Worker claims match the committed five-line generated diff, clean worktree, required metadata, and validation output.
- Changelog/docs/spec/tasks updates: Generated Marketplace change notes now match `CHANGELOG.md`.
- Blockers: None.
- Review risks: None beyond the T4/T5 product validation risks.
- Handoff notes and next action: Restart T4 with a fresh validation worker.

### Task Packet: T4-available-product-validation

Task id: T4-available-product-validation

Lane: testing

Required skills:

- `intellij-plugin-development`
- `plugin-review`
- `repository-documentation`

Goal:

- Prove current head against every available 2026.2 gate, record PyCharm's external availability failure, and leave no other failure unresolved.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, current diff, testing/review guidance, and current product data.
- Escalate to: files required to attribute a validation failure.

Allowed inputs:

- Current-head repository state, validation output, JetBrains feeds, and available local IDEs.

Forbidden inputs:

- Unrelated archived evidence and implementation changes outside a separately dispatched remediation packet.

Write scope:

- `docs/validation/reports/2026-07-16-intellij-2026-2-upgrade.md`.

Dependencies:

- T3R committed and reconciled after its explicit approval.

Validation:

- Run full unit, coverage, formatting, Detekt, packaging, configuration, docs, and agent checks; verifier and UI checks for available IDEA/WebStorm 2026.2; manual staging/AI smoke where possible; invoke `PY-2026.2` and accept only product-unavailable resolution failure; review the full diff; commit evidence before T5.

Escalation triggers:

- Escalate on any non-PyCharm failure or a changed head.

Stop conditions:

- A non-PyCharm failure remains or the head changes without full revalidation.

Expected output:

- Current-head report, exact blocker, review findings, task commit, events, and reconciliation.

Result summary:

- Status: completed
- Worker: Final worker `/root/t4_final_after_262_remediation`; earlier stopped attempts `/root/t4_available_product_validation`, `/root/t4_available_product_rerun`, and `/root/t4_final_exact_head` supplied remediation evidence.
- Changed files or reviewed diff: Final exact-head evidence in `docs/validation/reports/2026-07-16-intellij-2026-2-upgrade.md`; full 33-file `origin/main..a3a6cb9` diff reviewed read-only.
- Validation evidence: On exact source `a3a6cb9`, fresh prerelease passed all seven gates with 516 tests and compatible IU/WS verifiers; forced `verifyPluginProjectConfiguration` passed; clean full IU UI rerun passed 21/21; WS smoke passed 13/13. Local and hosted PY lanes failed only while resolving unpublished `PyCharmProfessional` 2026.2, before verifier or IDE execution. Hosted build, CodeQL, Security, IU verifier, and WS verifier passed.
- Self-review evidence from `.agents/references/reviews.md`: Full branch review found no confirmed defect; PR has no review threads or current-head changes requested. The initial forced IU run's single staging JMX/restart failure and 13 derived port cascades were preserved, reproduced as focused 1/1 green, and cleared by a clean full 21/21 rerun.
- Commit: `8e4c78155b681f75521b45d3dd6b32d503ab8d40`.
- Worker events: Earlier workers exposed and fixed generated metadata, final-summary, and 262 integration failures. Final worker completed exact-head prerelease/configuration, preserved and triaged one IU infrastructure flake, passed focused/full IU and WS UI, exercised local PY resolution, reconciled hosted checks, and committed the report.
- Orchestrator reconciliation: Final worker claims match the report-only commit, exact managed-job logs, current hosted results, clean worktree, and required commit metadata. T4 is complete because every available product gate passed and PyCharm failed only at the approved external-availability boundary.
- Changelog/docs/spec/tasks updates: Final validation report records exact head, product feed data, all local/hosted gates, review state, the triaged infrastructure flake, and the expected PyCharm blocker.
- Blockers: PyCharm 2026.2 is unpublished; no non-PyCharm blocker remains.
- Review risks: Published PyCharm 2026.2 may expose a new compatibility issue during T5; no current available-product risk is unresolved.
- Handoff notes and next action: Keep PR #37 draft. After JetBrains publishes PyCharm 2026.2, dispatch T5 for its unchanged verifier/UI lanes and the full current-head readiness gate.

### Task Packet: T5-pycharm-release-gate

Task id: T5-pycharm-release-gate

Lane: testing

Required skills:

- `intellij-plugin-development`
- `plugin-review`
- `repository-documentation`

Goal:

- After PyCharm 2026.2 is published, prove its unchanged required lane and the full current-head readiness gate.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, T4 report, current PR head/checks/reviews, and product data.
- Escalate to: files required to attribute a validation failure.

Allowed inputs:

- Current-head repository/PR state, PyCharm 2026.2 metadata, and T4 evidence.

Forbidden inputs:

- Earlier-head approvals as current evidence and unrelated history.

Write scope:

- `docs/validation/reports/2026-07-16-intellij-2026-2-upgrade.md`; remediation requires a separate approved packet.

Dependencies:

- T4 committed and PyCharm 2026.2 published.

Validation:

- Run PyCharm verifier/UI and the complete current-head matrix; re-fetch head, checks, reviews, and threads; commit final evidence and complete the plan only after all gates pass.

Escalation triggers:

- Escalate when published PyCharm changes accepted identifiers, Java, modules, compatibility, or any current-head gate fails.

Stop conditions:

- PyCharm remains unavailable, a check fails, the head changes, or current-head review blocks readiness.

Expected output:

- Passing full-matrix evidence, final task commit, plan update, events, reconciliation, and PR readiness result.

Result summary:

- Status: ready to restart after the T5R14 reconciliation is pushed
- Worker: `/root/t5_pycharm_release_gate`
- Changed files or reviewed diff: Appended the first published-PyCharm gate evidence to `docs/validation/reports/2026-07-16-intellij-2026-2-upgrade.md`; source head remained unchanged.
- Validation evidence: On exact head `4ccc61b4196ff6932ec3daa97f2c8992758a0aa5`, full local prerelease passed all eight gates and every hosted check passed, including PyCharm UI 13/13, patch coverage 95.86%, and project coverage 90.51%. Local IU passed 10/23, then the next scenario stalled on the exact license-required `Confirm Restart` dialog; local PY/WS were not started.
- Self-review evidence from `.agents/references/reviews.md`: No production defect or compatibility change is confirmed; the failing IDE logs show IntelliJ declining an AI action because its target control stopped showing during dynamic plugin reconfiguration.
- Commit:
- Worker events: Initial worker stopped at the first published-PyCharm failure. Restarted workers preserved each later hosted failure. Worker `/root/t5_final_exact_head_r5` passed prerelease and every hosted gate, stopped local IU after 10/23 when the license restart modal blocked scenario 11, preserved exact log/thread/screenshot evidence, and stopped cleanly at `2026-07-23T18:05:09.4198028+02:00`.
- Orchestrator reconciliation: Exact local, origin, and PR head remained `4ccc61b4196ff6932ec3daa97f2c8992758a0aa5`. The T5R6 observer and terminal Ultimate callbacks completed before `LicenseManager` disabled Ultimate and `BackendMessagesService` displayed the exact modal. The worker performed no manual interaction and stopped the managed process cleanly.
- Changelog/docs/spec/tasks updates: T5D aligned README, contributor/support documentation, changelog, and generated Marketplace notes; the validation report now records the subsequent hosted scheme-race, coverage, and closed-index evidence through T5R9.
- Blockers: No remediation blocker remains; the complete current-head local, hosted, review, and readiness evidence still must pass together.
- Review risks: Re-fetch the pushed head and review state before readiness; the fresh T5 gate must confirm the exact restart handling does not hide an unrelated platform or scenario failure.
- Handoff notes and next action: Push the T5R14 implementation and reconciliation commits, then restart the complete T5 gate on that exact head.

### Task Packet: T5R-stabilize-pycharm-ui-startup

Task id: T5R-stabilize-pycharm-ui-startup

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Prevent IDE Starter from disabling paid product plugins during release-matrix startup so PyCharm 2026.2 does not rebuild the Commit UI after the test begins.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, the first hosted failure artifact, the matching local passing IDE logs, `ReleaseMatrixUiHarnessTest.kt`, and `FakeAiAssistantProbe.kt`.
- Escalate to: exact IntelliJ 262 plugin-enable APIs or bytecode only when required to prove the completion signal.

Allowed inputs:

- The preserved hosted/local PyCharm evidence, exact IntelliJ 262 APIs, and focused validation output.

Forbidden inputs:

- Production plugin behavior changes, retries, sleeps, weakened assertions, quarantines, product-scope changes, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`

Dependencies:

- First T5 attempt stopped on the preserved hosted failure; this bounded packet is approved by the maintainer's normal change-review-fix continuation directive and current merge request.

Validation:

- Preserve the hosted 10/13 failure; compile integration tests; run the three affected PyCharm scenarios and inspect their IDE JVM options/logs for the paid-plugin startup flag and absence of a late Ultimate-plugin transition; rerun them enough to establish the post-fix pattern; run the full PyCharm UI lane, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R before restarting T5.

Escalation triggers:

- Escalate if the completion signal requires production code, unsupported timing assumptions, another product decision, or changes outside the two-file scope.

Stop conditions:

- IDE Starter's official paid-plugin startup option does not prevent the late reconfiguration, or focused/full PyCharm validation still fails.

Expected output:

- Deterministic startup synchronization, exact before/after evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r_stabilize_pycharm_startup`; read-only diagnosis `/root/t5_ci_timing_diagnosis`.
- Changed files or reviewed diff: Added only `doNotDisablePaidPluginsOnStartup()` to `ReleaseMatrixUiHarnessTest.kt` before release-matrix plugin configuration.
- Validation evidence: `compileIntegrationTestKotlin` passed; two focused PyCharm runs passed 3/3 each; the full PyCharm 2026.2 lane passed 13/13 in 4 minutes 37 seconds; all 13 IDE logs contain `-Dide.do.not.disable.paid.plugins.on.startup=true` and none contains an Ultimate disable, applied re-enable, or dynamic-reconfiguration event; `spotlessCheck detekt` and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: The official IDE Starter option removes the startup lifecycle transition before tests begin; no assertion, timeout, retry, sleep, production code, or product-scope change was added.
- Commit: `234d91e18bda4b6028a594316ed1e2d90d57229c`
- Worker events: Read-only diagnosis classified the race and recommended the official Starter API; the fresh T5R worker completed one-file implementation and all focused/full validation.
- Orchestrator reconciliation: Worker claims match the one-line committed diff, 6/6 focused pattern, 13/13 full log, 13 property-bearing IDE logs, zero late-transition events, and required commit metadata.
- Changelog/docs/spec/tasks updates: No user-facing change; the task-local failure and remediation evidence is recorded in the active plan and validation report.
- Blockers: None.
- Review risks: Hosted Linux remains the final environment-specific proof.
- Handoff notes and next action: Hosted validation later proved the paid-startup option opens a modal on an unlicensed runner; supersede this approach through T5R2 before restarting T5.

### Task Packet: T5R2-synchronize-pycharm-module-reload

Task id: T5R2-synchronize-pycharm-module-reload

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Remove the paid-plugin startup option and deterministically wait until PyCharm's normal Ultimate-module reconfiguration and AI Commit All action re-registration are complete before each fake-AI scenario begins.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, hosted run 29993726119/job 89163423209 logs, the earlier hosted timing failures, `ReleaseMatrixUiHarnessTest.kt`, and `FakeAiAssistantProbe.kt`.
- Escalate to: exact IntelliJ 262 plugin/module identifier and loaded-state APIs or bytecode only when required to implement the completion signal.

Allowed inputs:

- Preserved pre-T5R timing failures, the T5R hosted modal/time-limit failure, exact IntelliJ 262 APIs, focused validation output, and current local logs.

Forbidden inputs:

- Production plugin behavior, paid licenses or paid-startup flags, dialog dismissal, retries, sleeps, weakened assertions, quarantines, product-scope changes, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`

Dependencies:

- T5R and hosted run 29993726119 preserved the paid-startup modal failure; this bounded replacement is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Remove `doNotDisablePaidPluginsOnStartup()`; compile integration tests; prove the barrier observes the PyCharm Ultimate-module loaded state followed by AI Commit All plugin/action availability; rerun the previously timing-sensitive and hosted-modal scenarios; run the full PyCharm lane enough to establish a post-fix pattern; run `spotlessCheck`, `detekt`, and `git diff --check`; require no paid-startup flag or paid modal in fresh IDE logs; commit T5R2 before restarting T5.

Escalation triggers:

- Escalate if the completion signal requires production code, dialog handling, a timing-only assumption, another product decision, or a file outside the two-file scope.

Stop conditions:

- Exact 262 APIs cannot prove module reload completion, focused/full PyCharm validation fails after the barrier, or a paid modal remains.

Expected output:

- Unlicensed-runner-safe deterministic reload synchronization, exact before/after evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: stopped after proving the two-file approach cannot be deterministic
- Worker: `/root/t5r2_pycharm_reload_barrier`
- Changed files or reviewed diff: Three candidate barriers were implemented and tested sequentially, then both scoped Kotlin files were restored exactly to T5R; no task diff or commit remains.
- Validation evidence: `compileIntegrationTestKotlin` passed for each candidate. Focused A1 job `20260723-121018-intellij-2026-2-t5r2-py-focused-a1-8198fa` proved loaded-only excludes a terminal failed reload; A2 `20260723-121723-intellij-2026-2-t5r2-py-focused-a2-0a25f7` proved Driver-time listeners initialize about seven seconds after `Loading Plugins` finishes; A3 `20260723-121951-intellij-2026-2-t5r2-py-focused-a3-96c067` proved the platform clears the non-load reason. Final `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: `!disabled && !modal` has an observed 349-millisecond pre-modal gap, and the internal dynamic-plugin lock still leaves an observed 225-millisecond pre-lock gap; accepting either would preserve the race.
- Commit: None.
- Worker events: The worker was recovered once after API diagnosis, tested three exact 262 predicates, stopped when the two-file scope could not provide an early observer, and restored its scope cleanly.
- Orchestrator reconciliation: Worker claims match all three managed-job logs, exact IDE timestamps, clean scoped files, removed generated artifacts, and the required escalation boundary.
- Changelog/docs/spec/tasks updates: Task-local diagnosis is recorded in this plan and validation report; no user-facing behavior or support policy changed.
- Blockers: An observer must be registered before Driver utility availability, requiring the fake AI test plugin descriptor.
- Review risks: The early listener must capture only the platform's `Loading Plugins` lifecycle, preserve failed and successful reload completion, and avoid production/plugin-runtime scope.
- Handoff notes and next action: Execute T5R3 with the additional fake-plugin descriptor scope.

### Task Packet: T5R3-observe-pycharm-reload-at-startup

Task id: T5R3-observe-pycharm-reload-at-startup

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Register a test-only progress listener when the fake AI plugin loads so PyCharm's startup `Loading Plugins` lifecycle is recorded before IDE Driver utility calls become available.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, T5R/T5R2 evidence, exact focused A1/A2/A3 IDE logs, the two Kotlin files, and the fake AI plugin descriptor.
- Escalate to: exact IntelliJ 262 application-listener descriptor syntax and `ProgressManagerListener` contract only.

Allowed inputs:

- Preserved hosted/local failures, exact IntelliJ 262 listener APIs, the test-only fake AI plugin, and focused validation output.

Forbidden inputs:

- Production plugin behavior or descriptor, paid licenses or paid-startup flags, dialog dismissal, retries, sleeps, weakened assertions, quarantines, product-matrix changes, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`
- `src/integrationTest/resources/fake-ai-assistant-plugin/META-INF/plugin.xml`

Dependencies:

- T5R2 restored its scope after proving Driver-time observation is too late; this bounded early-observer packet is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Remove `doNotDisablePaidPluginsOnStartup()`; register an early test-plugin application listener for the exact `Loading Plugins` lifecycle; wait for either Ultimate already loaded or the relevant loading task completed, then require AI Commit All plugin/action and fake action availability; compile integration tests; run the six previously timing-sensitive/modal scenarios at least twice; inspect fresh logs/evidence for both successful and rejected reload paths, no paid-startup flag, no paid modal, and no stale-control rejection; run the full PyCharm lane at least twice, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R3 before restarting T5.

Escalation triggers:

- Escalate if descriptor registration cannot initialize before the reload, task completion cannot be attributed to the Ultimate enable attempt, focused/full PyCharm validation fails, or any file outside the three-file scope is required.

Stop conditions:

- The listener misses the startup task, observes unrelated progress as readiness, or fresh runs retain a timing/modal failure.

Expected output:

- Early unlicensed-runner-safe reload observation, exact before/after evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: stopped after proving the proposed progress topic is not emitted
- Worker: `/root/t5r3_early_reload_observer`
- Changed files or reviewed diff: Registered the proposed listener, enabled it for test/headless modes, added harness/probe state, then restored all three scoped files exactly after the platform did not emit the required callback.
- Validation evidence: `compileIntegrationTestKotlin` passed. Focused A1 job `20260723-123504-intellij-2026-2-t5r3-py-focused-a1-c63bf4` passed 3/6 before staging retained `ChangesViewCommitWorkflowHandler`. Targeted job `20260723-124224-intellij-2026-2-t5r3-listener-proof-81beb2` passed 1/1 but logged `Loading Plugins` at `12:43:04.305`, listener construction only at `12:43:08.922`, and its first unrelated callback at `12:43:08.926`.
- Self-review evidence from `.agents/references/reviews.md`: The descriptor was packaged and active in integration/headless modes, but `PlatformTaskSupport` does not publish this loading operation through `ProgressManagerListener.TOPIC`; accepting the passing fallback would not prove rejected-reload synchronization.
- Commit: None.
- Worker events: The worker stopped at the packet boundary, restored the three files, removed generated artifacts, and left only orchestrator-owned plan/report changes.
- Orchestrator reconciliation: Exact timestamps, packaged descriptor, targeted log, clean scoped diff, and `git diff --check` agree with the worker report.
- Changelog/docs/spec/tasks updates: Task-local evidence is recorded in this plan and validation report; no user-facing behavior changed.
- Blockers: The early hook must observe the actual AWT modal rather than the absent progress topic.
- Review risks: The observer must install before the modal, match only `Loading Plugins`, never dismiss it, and record closure after successful or rejected reload.
- Handoff notes and next action: Execute T5R4 in the same three-file test-only scope.

### Task Packet: T5R4-observe-pycharm-loading-dialog

Task id: T5R4-observe-pycharm-loading-dialog

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Install a test-only AWT window observer from an early app-start listener and wait for the actual PyCharm `Loading Plugins` dialog to close before scenarios begin.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, T5R through T5R3 evidence, the targeted idea log, the two Kotlin files, and the fake AI plugin descriptor.
- Escalate to: exact IntelliJ 262 `AppLifecycleListener` callback and application-listener descriptor contract only.

Allowed inputs:

- Preserved hosted/local failures, exact 262 app lifecycle APIs, AWT window events, the test-only fake AI plugin, and focused validation output.

Forbidden inputs:

- Production plugin behavior or descriptor, paid licenses or paid-startup flags, dialog dismissal or interaction, retries, sleeps, weakened assertions, quarantines, product-matrix changes, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`
- `src/integrationTest/resources/fake-ai-assistant-plugin/META-INF/plugin.xml`

Dependencies:

- T5R3 restored its scope after proving the progress topic is not emitted; this bounded AWT observer packet is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Remove `doNotDisablePaidPluginsOnStartup()`; register an active-in-test/headless `AppLifecycleListener` that installs an AWT listener before reload; record only `Dialog` windows titled `Loading Plugins`, marking active on open and complete on close when Ultimate is loaded or enabled in configuration; wait for either Ultimate already loaded or relevant dialog completion, then require AI Commit All plugin/action and fake action; compile; prove observer installation precedes dialog opening in a targeted run; run the six timing/modal scenarios twice; inspect logs for successful/rejected reload paths, no paid flag/modal hang/stale-control rejection; run full PyCharm twice, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R4 before restarting T5.

Escalation triggers:

- Escalate if app lifecycle initialization occurs after the dialog, AWT close is not emitted, unrelated windows can satisfy readiness, focused/full validation fails, or any file outside the three-file scope is required.

Stop conditions:

- The listener misses the dialog, interacts with it, or leaves a timing-only readiness path.

Expected output:

- Early non-interacting loading-dialog observation, exact lifecycle evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: stopped after proving the split frontend dialog is not observable through backend AWT
- Worker: `/root/t5r4_loading_dialog_observer`
- Changed files or reviewed diff: Implemented the app-start AWT observer in the three scoped test-plugin files, then restored all three files exactly after the lifecycle proof missed the frontend dialog; no task diff or commit remains.
- Validation evidence: `compileIntegrationTestKotlin` passed. Targeted job `20260723-125313-intellij-2026-2-t5r4-py-lifecycle-proof-7af0e5` passed 1/1. The listener installed at `12:53:48.156`, before `PlatformTaskSupport` logged `Modal dialog is shown: Loading Plugins` at `12:53:49.636`, but the observer received neither `WINDOW_OPENED` nor `WINDOW_CLOSED`.
- Self-review evidence from `.agents/references/reviews.md`: Descriptor timing is early enough, but the remote-development split keeps the modal in the frontend process; accepting absence of a backend window callback as completion would preserve the race.
- Commit: None.
- Worker events: Worker started at `2026-07-23T12:46:59+02:00`, hit the explicit no-callback stop condition, restored its scope, and stopped cleanly at `2026-07-23T12:55:00+02:00`.
- Orchestrator reconciliation: Exact timestamps, packaged descriptor behavior, the targeted job log, clean scoped diff, and `git diff --check` agree with the worker report.
- Changelog/docs/spec/tasks updates: Task-local evidence is recorded in this plan and validation report; no user-facing behavior changed.
- Blockers: The barrier must observe the backend `DynamicPluginEnabler` completion boundary rather than a frontend dialog.
- Review risks: The listener must be installed before the enable call, filter the exact Ultimate module, retain failed-load completion, and require action availability after completion.
- Handoff notes and next action: Execute T5R5 in the same three-file test-only scope.

### Task Packet: T5R5-await-pycharm-enable-attempt

Task id: T5R5-await-pycharm-enable-attempt

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Register a test-only platform state listener before PyCharm's Ultimate-module enablement and do not enter a scenario until the corresponding dynamic load attempt has returned.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, T5R through T5R4 evidence, the two Kotlin files, and the fake AI plugin descriptor.
- Escalate to: exact IntelliJ 262 `DynamicPluginEnabler` and `PluginEnableStateChangedListener` bytecode/contracts only.

Allowed inputs:

- Preserved hosted/local failures, exact 262 dynamic-plugin enable APIs, the test-only fake AI plugin, and focused validation output.

Forbidden inputs:

- Production plugin behavior or descriptor, paid licenses or paid-startup flags, dialog observation/dismissal, retries, sleeps, weakened assertions, quarantines, product-matrix changes, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`
- `src/integrationTest/resources/fake-ai-assistant-plugin/META-INF/plugin.xml`

Dependencies:

- T5R4 restored its scope after proving a backend AWT listener cannot observe the split frontend modal; this bounded platform-callback packet is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Remove `doNotDisablePaidPluginsOnStartup()`; register an active-in-test/headless `AppLifecycleListener` that strongly retains a `PluginEnableStateChangedListener` through `DynamicPluginEnabler.addPluginStateChangedListener`; record completion only when `enabled=true` and the callback descriptors contain `com.intellij.modules.ultimate`; require listener installation and either Ultimate already loaded or that exact enable-attempt callback, followed by AI Commit All plugin/action and fake action availability; compile; prove installation precedes the enable attempt and the callback follows its load attempt; run the six timing/modal scenarios at least twice; inspect logs for successful/rejected reload paths, no paid flag/modal hang/stale-control rejection; run the full PyCharm lane at least twice, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R5 before restarting T5.

Escalation triggers:

- Escalate if the listener registers after the enable call, the callback fires before `DynamicPlugins.loadPlugins` returns, the exact Ultimate descriptor cannot be attributed, focused/full validation fails, or any file outside the three-file scope is required.

Stop conditions:

- The callback misses the enable attempt, unrelated plugin state can satisfy readiness, or fresh runs retain a timing/modal failure.

Expected output:

- Early unlicensed-runner-safe post-load-attempt synchronization, exact before/after evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: stopped after proving terminal rejected loading is not staging-workflow readiness
- Worker: `/root/t5r5_pycharm_enable_completion`
- Changed files or reviewed diff: Implemented the exact platform state listener and harness barrier in the three scoped test-plugin files, then restored all three exactly after the staging validation stop condition; no task diff or commit remains.
- Validation evidence: `compileIntegrationTestKotlin` passed. Callback proof job `20260723-130406-intellij-2026-2-t5r5-py-callback-proof-440677` passed 1/1: observer installed at `13:04:24.936`, enablement began at `13:04:26.437`, reconfiguration was rejected at `13:04:26.590`, and the exact callback arrived at `13:04:26.595`. Focused job `20260723-130525-intellij-2026-2-t5r5-py-focused-a1-25ced1` passed 5/6; narrow final-source job `20260723-130951-intellij-2026-2-t5r5-py-staging-repro-cd5335` reproduced the staging failure 0/1 with `ChangesViewCommitWorkflowHandler`.
- Self-review evidence from `.agents/references/reviews.md`: The callback is a valid post-load-attempt boundary and removes the original stale-control race, but accepting it alone would exclude the existing staging behavior because rejected Ultimate loading leaves the Commit workflow fixture in changelist mode.
- Commit: None.
- Worker events: Worker started at `2026-07-23T12:59:17+02:00`, preserved the callback proof and focused failure, restored its scope, and stopped cleanly at `2026-07-23T13:12:24+02:00`.
- Orchestrator reconciliation: Exact timestamps, both managed-job logs, clean scoped diff, removed Allure output, and `git diff --check` agree with the worker report.
- Changelog/docs/spec/tasks updates: Task-local evidence is recorded in this plan and validation report; no user-facing behavior changed.
- Blockers: After the callback, the staging-enabled scenario must deterministically create or rebuild `GitStageCommitWorkflowHandler`.
- Review risks: Fixture repair must use a real platform lifecycle/API, preserve both staging and changelist assertions, and not mask a plugin behavior defect.
- Handoff notes and next action: Execute T5R6 in the same three-file test-only scope, retaining the proven T5R5 barrier.

### Task Packet: T5R6-rebuild-pycharm-staging-workflow

Task id: T5R6-rebuild-pycharm-staging-workflow

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Retain T5R5's exact post-enable-attempt synchronization and make the release-matrix fixture deterministically enter the requested Git staging or changelist Commit workflow after PyCharm rejects Ultimate-module loading.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, T5R through T5R5 evidence, callback proof and staging-repro logs, the two Kotlin files, and the fake AI plugin descriptor.
- Escalate to: exact IntelliJ 262 `GitVcsApplicationSettings`, Commit tool-window/workflow manager, staging-setting listener, and workflow rebuild contracts only.

Allowed inputs:

- Preserved callback/staging failures, exact IntelliJ 262 test-fixture and Commit workflow APIs, the test-only fake AI plugin, and focused validation output.

Forbidden inputs:

- Production plugin behavior or descriptor, paid licenses or paid-startup flags, dialog observation/dismissal, retries, sleeps, weakened assertions, direct construction of fake workflow handlers, quarantines, product-matrix changes, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`
- `src/integrationTest/resources/fake-ai-assistant-plugin/META-INF/plugin.xml`

Dependencies:

- T5R5 restored its scope after proving the exact enable callback but reproducing persistent changelist workflow selection in the staging scenario; this bounded fixture-remediation packet is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Reapply T5R5 without the paid-startup option; prove the same exact callback ordering; diagnose the branch-262 supported lifecycle that applies `GitVcsApplicationSettings.stagingAreaEnabled` to an already-created or subsequently-created Commit workflow; invoke only that real lifecycle from the test fixture after terminal PyCharm enablement; require the handler to become `GitStageCommitWorkflowHandler` for enabled and not that class for disabled; run the narrow staging repro at least three times, the six timing/modal scenarios twice, both staging-enabled and staging-disabled commit flows, and the full PyCharm lane at least twice; inspect logs for no paid flag/modal hang/stale-control rejection; run `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R6 before restarting T5.

Escalation triggers:

- Escalate if no real 262 lifecycle can rebuild workflow selection, the fix requires production code or direct fake-handler construction, either staging mode fails, or any file outside the three-file scope is required.

Stop conditions:

- The fixture can pass only through timing, retries, weaker handler/selection assertions, or bypassing the real Commit workflow.

Expected output:

- Unlicensed-runner-safe enable synchronization plus deterministic real staging/changelist workflow selection, exact evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r6_pycharm_staging_workflow`
- Changed files or reviewed diff: Removed the paid-plugin startup flag, added an early test-plugin Ultimate enable-attempt listener and harness barrier, and changed the fake probe's staging setter to invoke the real `GitStageManagerKt.enableStagingArea(boolean)` lifecycle in the exact three-file scope.
- Validation evidence: `compileIntegrationTestKotlin` passed; the formerly deterministic narrow staging failure passed 3/3; the six timing-sensitive scenarios passed 6/6 twice; staging-disabled and staging-enabled commit flows passed 2/2; full PyCharm passed 13/13 twice; all 13 full-lane logs proved listener-before-enable and terminal callback ordering with zero paid-startup or stale-control hits; `spotlessCheck`, `detekt`, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: The diff is test-only, exercises the real Commit workflow and existing handler assertions, retains both staging modes, and adds no retries, sleeps, fake handlers, paid licensing, dialog handling, production changes, or unrelated refactor.
- Commit: `777cf177ca1ea7c54156c761b54ab1250fc002d4`
- Worker events: Worker started at `2026-07-23T13:16:16.8671783+02:00`, preserved the first formatting failures, completed the full repeated matrix, and stopped successfully at `2026-07-23T14:00:00.8295546+02:00`.
- Orchestrator reconciliation: The worker's claims match the three-file commit, required metadata, managed-job logs, clean task scope, static checks, and exact runtime bytecode for `GitStageManagerKt.enableStagingArea`.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; task-local evidence is recorded in this plan and validation report. T5D already aligned the changelog and generated Marketplace notes with the published release.
- Blockers: None; restart T5 on the exact integrated head.
- Review risks: Hosted Linux remains the decisive environment proof, followed by exact-head review and readiness gates.
- Handoff notes and next action: Integrate current `main`, restart T5, keep PR #37 draft until every exact-head gate passes.

### Task Packet: T5R7-classify-2026-2-scheme-race

Task id: T5R7-classify-2026-2-scheme-race

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Extend the existing branch-262 test-reporter compatibility mapping so Starter does not fail a successful scenario for the exact Islands Dark scheme-manager concurrent-mutation diagnostic observed before harness entry.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, the exact hosted job log/artifact stack, and the reporter/constants in `ReleaseMatrixUiHarnessTest.kt`.
- Escalate to: exact IntelliJ 262 `SchemeManagerImpl.findSchemeByName`, `EditorColorsManagerImpl.getSchemeForCurrentUITheme`, and `FileStatusImpl.getColor` stack contracts only.

Allowed inputs:

- Hosted artifact `8563226012`, the exact synthetic test name and stack, the existing branch-262 reporter mapping, and focused validation output.

Forbidden inputs:

- Production code/resources/descriptors, fallback color schemes, theme or user-setting mutation, arbitrary `ConcurrentModificationException` suppression, product skips, retries, sleeps, assertion weakening, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`

Dependencies:

- Restarted T5 preserved the exact hosted failure and restored a clean worktree; this bounded reporter-remediation packet is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Add a dedicated predicate that requires the exact `java.util.ConcurrentModificationException` synthetic name and all distinguishing `ArrayList$Itr`, `SchemeManagerImpl.findSchemeByName`, `EditorColorsManagerImpl.getSchemeForCurrentUITheme`, and `FileStatusImpl.getColor` stack frames; keep the existing exact `aicommitall.ide.version == 2026.2` gate; prove the captured hosted error is accepted while arbitrary concurrent modification and each distinguishing-frame near miss remain rejected; compile integration tests; rerun the previously failed PyCharm scenario at least three times and the full PyCharm smoke lane; run `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R7 before restarting T5.

Escalation triggers:

- Escalate if exact classification cannot be tested without a production change, another diagnostic appears, the scenario fails for plugin behavior, or a file outside the one-file scope is required.

Stop conditions:

- Passing requires a generic exception suppression, fallback/theme replacement, timing workaround, product skip, or weakened workflow assertion.

Expected output:

- Exact version-gated platform diagnostic classification, positive/negative proof, focused/full PyCharm evidence, task commit, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r7_scheme_race_classifier`
- Changed files or reviewed diff: Changed only `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`; refactored the existing exact IntelliJ 2026.2 reporter gate through a dedicated predicate and added an exact synthetic-name plus four-frame classifier and near-miss coverage.
- Validation evidence: `compileIntegrationTestKotlin` passed; the pure classifier test passed 1/1; the formerly failed PyCharm scenario passed 1/1 in three independent processes (`20260723-143215-intellij-2026-2-t5r7-py-empty-r1-56e6de`, `20260723-143315-intellij-2026-2-t5r7-py-empty-r2-248cbd`, and `20260723-143408-intellij-2026-2-t5r7-py-empty-r3-89db7e`); full PyCharm smoke passed 13/13 in `20260723-143500-intellij-2026-2-t5r7-py-full-637ff3`; `spotlessCheck`, `detekt`, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: The classifier requires exact IDE version `2026.2`, exact synthetic test name `java.util.ConcurrentModificationException: null`, and every distinguishing `ArrayList$Itr`, `SchemeManagerImpl`, `EditorColorsManagerImpl`, and `FileStatusImpl` frame. Production code/resources, fallback schemes, theme/user settings, product skips, retries, sleeps, and workflow assertions are unchanged.
- Commit: `c3bae72f614e8e4fb224aa93bbd82f0b1eade3be`
- Worker events: Worker started at `2026-07-23T14:30:58.3509502+02:00`, completed the bounded one-file packet and all validation, and stopped successfully at `2026-07-23T14:43:30.3744018+02:00`.
- Orchestrator reconciliation: The worker claims match the exact one-file diff, test counts, managed-job evidence, clean task scope, and required commit metadata.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; the task-local platform-diagnostic evidence is recorded in this plan and validation report. T5D already updated `CHANGELOG.md` and generated Marketplace notes for the PyCharm release.
- Blockers: None; restart T5 on the exact pushed head.
- Review risks: Hosted Linux and the full three-product exact-head gate remain required before readiness.
- Handoff notes and next action: Push T5R7 and this reconciliation, then dispatch a fresh T5 worker for the complete current-head gate.

### Task Packet: T5R8-cover-262-reflection-failures

Task id: T5R8-cover-262-reflection-failures

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `gh-fix-ci-security-quality`

Goal:

- Restore required patch and project coverage by testing the observable fail-closed behavior and diagnostics of the branch-262 Git staging reflection boundary.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, Codecov checks `89214533481` and `89214528762`, `ReflectiveCommitWorkflowSynchronizer.kt`, and `ReflectiveCommitWorkflowSynchronizerTest.kt`.
- Escalate to: local unit and aggregate JaCoCo XML for the changed file and exact branch-262 reflective method shapes only.

Allowed inputs:

- Exact Codecov summaries, local JaCoCo line/branch counters, existing reflection test fixtures, and the 2026.2 Git staging handler boundary already accepted by ADR 0089.

Forbidden inputs:

- Coverage threshold changes, exclusions, ignores, Codecov configuration changes, superficial source execution without behavioral assertions, production behavior changes, generic compatibility shims, retries, UI harness changes, and unrelated refactors.

Write scope:

- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`

Dependencies:

- T5 exact-head local and GitHub Actions gates passed; Codecov is the sole blocker and attributes all missing patch coverage to the named production file.

Validation:

- Add tests that prove invocation failure, missing nested boundary methods, and incompatible method results each return no access and emit the exact `gitStageCommitWorkflowAccess` diagnostic, including exception and cause where applicable. Run the targeted test, `jacocoTestReport`, inspect changed-file line/branch improvement, run the full unit suite, `verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and `git diff --check`; commit the bounded test change before restarting T5.

Escalation triggers:

- Escalate if the required failure path cannot be reached through the public/internal boundary, a production seam is required, or local coverage cannot explain the Codecov delta.

Stop conditions:

- Passing requires a threshold/exclusion/configuration change, non-behavioral coverage trick, production compatibility expansion, or a file outside the one-file scope.

Expected output:

- Exact behavioral tests, before/after changed-file coverage evidence, task commit, self-review, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r8_reflection_coverage`
- Changed files or reviewed diff: Changed only `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`; added seven atomic behavioral tests for handler invocation failure/cause, null workflow, null UI, missing nested methods, incompatible project, incompatible UI, and nested project invocation failure/cause.
- Validation evidence: Targeted reflection tests passed 32/32. For inserted production lines 330-457, unit coverage improved from 65/81 lines and 45/72 branches to 81/81 lines and 66/72 branches; the remaining six branches are compiler null guards dominated by required-method checks. Full unit tests passed 523 with one existing pending test; `jacocoTestReport`, `verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: Every new test asserts null access and the exact `gitStageCommitWorkflowAccess` diagnostic. No production, configuration, threshold, exclusion, mock, sleep, UI-harness, documentation, or changelog change was made.
- Commit: `c6cc0c390126d21a0a58633918a553639ae73bbe`
- Worker events: Worker started at `2026-07-23T15:32:06.0593512+02:00`, resolved a test-fixture JVM setter clash and Detekt `LargeClass` finding inside the same file, completed all validation, and stopped successfully at `2026-07-23T15:46:17.6317845+02:00`.
- Orchestrator reconciliation: Worker claims match the one-file test-only diff, local JaCoCo before/after counters, test counts, static checks, clean worktree, and required commit metadata.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; task-local coverage evidence is recorded in this plan and validation report. Existing changelog and Marketplace notes remain current.
- Blockers: None; restart T5 on the exact pushed head and require Codecov patch/project success.
- Review risks: Hosted aggregate coverage is calculated from unit plus PyCharm UI reports, so only the external checks can close the original Codecov finding.
- Handoff notes and next action: Push T5R8 and this reconciliation, then dispatch a fresh T5 worker for the complete current-head gate.

### Task Packet: T5R9-classify-2026-2-closed-index-storage

Task id: T5R9-classify-2026-2-closed-index-storage

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Extend the existing branch-262 test-reporter compatibility mapping so Starter does not fail a successful scenario for the exact stub-index storage race observed during PyCharm's dynamic plugin reload.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, hosted UI job `89226997462`, artifact `8566451340`, and the reporter/constants/classifier tests in `ReleaseMatrixUiHarnessTest.kt`.
- Escalate to: exact branch-262 `FileBasedIndexTumbler`, `PagedFileStorage`, per-file-version stub index, and unindexed-files scanner evidence only.

Allowed inputs:

- The two exact promoted failure names/stacks, all 13 hosted IDE logs, the existing branch-262 reporter mapping, and focused validation output.

Forbidden inputs:

- Production code/resources/descriptors, index or plugin-loading mutation, arbitrary `ClosedStorageException` suppression, product skips, retries as the fix, sleeps, assertion weakening, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`

Dependencies:

- Restarted T5 preserved artifact `8566451340` and restored a clean worktree; this bounded reporter-remediation packet is approved by the maintainer's standing normal review-fix continuation directive and current merge request.

Validation:

- Add a dedicated predicate under the exact `aicommitall.ide.version == 2026.2` gate. Require an exact `com.intellij.util.io.ClosedStorageException: storage is already closed; path ` prefix, the exact `/system/index/stubs/.perFileVersion/indexed_versions/indexed_versions_i` suffix, and all distinguishing `PagedFileStorage.doGetBufferWrapper`, `PersistentSubIndexerVersionEnumerator$MyEnumerator.enumerate`, `VfsAwareMapReduceIndex.getIndexingStateForFile`, and `UnindexedFilesScanner$ScanningSession.scanFiles` frames. Prove both captured forms are accepted while non-2026.2, arbitrary storage errors, wrong index paths, and every distinguishing-frame near miss remain rejected. Compile integration tests; run the two previously failed PyCharm scenarios in three independent processes and the full PyCharm smoke lane; run `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R9 before restarting T5.

Escalation triggers:

- Escalate if exact classification cannot be tested without a production change, a plugin frame appears, a scenario fails its intended commit behavior, another diagnostic appears, or a file outside the one-file scope is required.

Stop conditions:

- Passing requires generic exception suppression, index/plugin-loading mutation, timing workaround, product skip, retry-only mitigation, or weakened workflow assertion.

Expected output:

- Exact version-gated platform diagnostic classification, positive/negative proof, focused/full PyCharm evidence, task commit, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r9_closed_index_classifier`
- Changed files or reviewed diff: Changed only `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`; added one exact branch-262 classifier and synthetic positive/negative coverage.
- Validation evidence: The red classifier proof rejected the first captured diagnostic before implementation. The green pure classifier test passed 1/1. The two formerly failed PyCharm scenarios passed 6/6 across three independent IDE processes (`20260723-162217-intellij-2026-2-t5r9-py-two-scenarios-r1-50250f`, `20260723-162342-intellij-2026-2-t5r9-py-two-scenarios-r2-4f304c`, and `20260723-162530-intellij-2026-2-t5r9-py-two-scenarios-r3-de138d`), and the full PyCharm lane passed 13/13 in `20260723-162708-intellij-2026-2-t5r9-py-full-71bd67`. `compileIntegrationTestKotlin`, `spotlessCheck`, `detekt`, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: Classification remains inside the exact `2026.2` gate and requires the exact exception prefix, stub-index path suffix, and all four distinguishing platform frames. Tests leave non-2026.2, arbitrary storage errors, wrong paths, generic closed-storage errors, and every missing-frame near miss red. No production, index, plugin-loading, retry, sleep, skip, or workflow-assertion behavior changed.
- Commit: `ae5aa6dba8fe01ea4a4d0bea3fb816a33f25baf4`
- Worker events: Worker started at `2026-07-23T16:17:07.1735403+02:00`, completed the bounded one-file packet, and stopped successfully at `2026-07-23T16:35:13.7065745+02:00`.
- Orchestrator reconciliation: Worker claims match the one-file 110-line test-harness diff, managed-job results, clean worktree, and required contiguous commit metadata.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; this plan and the validation report record task-local evidence. Existing changelog and generated Marketplace notes remain current.
- Blockers: None; restart T5 on the exact pushed head and require the complete local, hosted, Codecov, review, and readiness gate.
- Review risks: Only the exact hosted branch-262 stack is classified; any new or near-miss diagnostic remains a failure and requires separate triage.
- Handoff notes and next action: Push T5R9 and this reconciliation, then dispatch a fresh T5 worker for the complete current-head gate.

### Task Packet: T5R10-cover-residual-reflection-branches

Task id: T5R10-cover-residual-reflection-branches

Lane: testing

Required skills:

- `kotlin-plugin-style`
- `plugin-test-tdd`

Goal:

- Raise exact-head patch coverage above its unchanged target by exercising the residual observable fail-closed combinations in the branch-262 Git staging reflection boundary.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, Codecov patch check `89243828900`, `build/reports/jacoco/test/jacocoTestReport.xml`, `ReflectiveCommitWorkflowSynchronizer.kt` lines 330-486, and the matching reflection-access tests and fixtures.
- Escalate to: the hosted aggregate report or exact Kotlin bytecode only if local JaCoCo cannot attribute the residual branches.

Allowed inputs:

- Current Codecov summary, local JaCoCo line/branch counters, the branch-262 reflection boundary, and its matching tests.

Forbidden inputs:

- Production changes, coverage thresholds/configuration/exclusions, Codecov configuration, generated-bytecode-only assertions, test disabling, arbitrary mocks, UI-harness changes, retries, and unrelated source.

Write scope:

- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`

Dependencies:

- T5R8 and T5R9 are committed; restarted T5 preserved the residual exact-head Codecov failure and restored a report-only worktree diff.

Validation:

- Preserve the exact JaCoCo before counters. Add the smallest behavioral parameterized or atomic cases that independently omit each handler method and each nested boundary method, asserting null access and the exact missing-method diagnostic. Run the focused reflection tests and `jacocoTestReport`; prove line/branch improvement on the changed reflection range sufficient to exceed the unchanged 90.27% patch target; run full `test`, `verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R10 before restarting T5.

Escalation triggers:

- Escalate if the target cannot be cleared by observable missing-method combinations, a production/configuration change appears necessary, or a new behavior defect is found.

Stop conditions:

- Passing requires threshold/exclusion/configuration changes, generated-bytecode-only tests, weakened diagnostics, or any file outside the one-file scope.

Expected output:

- Behavioral residual-branch coverage, exact before/after JaCoCo evidence, task commit, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: stopped cleanly; behavioral coverage cannot clear the target without a production simplification
- Worker: `/root/t5r10_residual_reflection_coverage`
- Changed files or reviewed diff: Temporarily added all six independently missing handler/nested-method cases in `ReflectiveCommitWorkflowSynchronizerTest.kt`, measured them, then restored the file exactly.
- Validation evidence: Focused reflection tests passed 9/9 and the full unit suite passed 525/525 with one pending. Exact JaCoCo counters for production lines 330-486 remained unchanged before and after: 650 covered/62 missed instructions, 69/7 branches, and 98/0 lines.
- Self-review evidence from `.agents/references/reviews.md`: Every temporary case asserted null access and the exact missing-method diagnostic. The zero coverage delta proves the residual paths are not additional observable method combinations.
- Commit: None; the one-file experiment was restored.
- Worker events: Worker started at `2026-07-23T17:03:51.6216378+02:00`, reached the packet stop condition, restored the worktree, and stopped cleanly at `2026-07-23T17:11:17.7303304+02:00`.
- Orchestrator reconciliation: The worker's zero-delta counters match the local JaCoCo XML. Six residual branches are null paths dominated by the preceding missing-method guard; the remaining same-name/wrong-signature branch can add only one hit.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; record the task-local evidence in the plan and validation report only.
- Blockers: The exact patch ratio is 101/113. One additional behavioral hit projects to 102/113 = 90.26549%, still below 90.27%; a behavior-preserving production-boundary simplification is required.
- Review risks: Do not add generated-bytecode-only tests or alter coverage configuration.
- Handoff notes and next action: Execute T5R11 to remove redundant dominated guards and cover the remaining reachable wrong-signature path.

### Task Packet: T5R11-simplify-reflection-null-guards

Task id: T5R11-simplify-reflection-null-guards

Lane: implementation

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`

Goal:

- Preserve the branch-262 reflection boundary's exact fail-closed behavior while eliminating redundant compiler null guards that cannot execute after the existing missing-method checks.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, T5R10 result, Codecov patch check `89243828900`, `ReflectiveCommitWorkflowSynchronizer.kt` lines 330-486, its matching reflection-access tests/fixtures, and local JaCoCo XML.
- Escalate to: compiled Kotlin bytecode only if source-level before/after counters do not explain the change.

Allowed inputs:

- Current reflection source/tests, T5R10 before/after evidence, exact diagnostics, and JaCoCo/Codecov counters.

Forbidden inputs:

- Coverage thresholds/configuration/exclusions, Codecov configuration, API/behavior changes, new reflection fallbacks, UI-harness changes, unrelated refactors, generated-bytecode-only tests, and assertion weakening.

Write scope:

- `src/main/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizer.kt`
- `src/test/kotlin/pl/devopssolutions/aicommitall/workflow/ReflectiveCommitWorkflowSynchronizerTest.kt`

Dependencies:

- T5R10 proved additional observable missing-method tests alone cannot clear the unchanged patch target and restored the worktree.

Validation:

- Capture current focused behavior and JaCoCo counters. Refactor the handler and nested-boundary method discovery so access objects are built only from explicitly proven non-null `Method` values, without later safe calls or `checkNotNull` guards for those values. Preserve exact diagnostics for all-missing and independently missing methods; add the same-name/wrong-signature rejection case. Run focused reflection tests, `jacocoTestReport`, and an exact diff-line coverage calculation proving the projected patch result exceeds 90.27%; run full `test`, `verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R11 before restarting T5.

Escalation triggers:

- Escalate if exact diagnostics change, the target still cannot be exceeded, or a file outside the two-file scope is required.

Stop conditions:

- Passing requires coverage/configuration changes, behavior/API changes, broader production refactoring, or tests of unreachable generated bytecode.

Expected output:

- Minimal behavior-preserving source/test refactor, exact diagnostic regression proof, improved patch projection, task commit, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r11_reflection_null_guards`
- Changed files or reviewed diff: Refactored only `ReflectiveCommitWorkflowSynchronizer.kt` and its matching test. Handler and nested method holders are constructed only after explicit non-null proof; tests cover each independently missing method and same-name/wrong-signature rejection.
- Validation evidence: Focused reflection tests passed 13/13 before and after the refactor. The full unit suite executed 530 tests with one existing skip and passed. Exact JaCoCo counters for the changed reflection range improved from 650 covered/62 missed instructions, 69/7 branches, and 98/0 lines to 665/15 instructions, 68/0 branches, and 106/0 lines. `jacocoTestReport`, `verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and `git diff --check` passed. The exact patch projection improved from 101/113 = 89.38053% to 116/121 = 95.86777%.
- Self-review evidence from `.agents/references/reviews.md`: Exact missing-method ordering, null access, method-invocation failure/cause, and incompatible-result diagnostics are preserved. The source refactor removes only post-guard null handling and adds no reflection fallback, API behavior, coverage configuration, or unrelated change.
- Commit: `df5964eb83a54b128dc3883b884cf5c33e1fe256`
- Worker events: Worker started at `2026-07-23T17:13:26.9670364+02:00`, completed the two-file packet, and stopped successfully at `2026-07-23T17:31:34.5457752+02:00`.
- Orchestrator reconciliation: Worker claims match the 58-line source and 135-line test diff, exact local counters, 13 focused cases, full/static validation, clean worktree, and required contiguous commit metadata. A temporary detached validation worktree was stopped and removed; no residual worktree remains.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; this plan and validation report record the coverage remediation. Existing changelog and Marketplace notes remain current.
- Blockers: None; restart T5 on the exact pushed head and require hosted patch/project coverage plus every other gate.
- Review risks: The local projection exceeds the target, but only hosted aggregate Codecov can close the original failure.
- Handoff notes and next action: Push T5R11 and reconciliation, then dispatch a fresh T5 worker for the complete exact-head gate.

### Task Packet: T5R12-handle-license-restart-transition

Task id: T5R12-handle-license-restart-transition

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `platform-docs-research`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Make release-matrix UI tests deterministically survive the exact IntelliJ 2026.2 license downgrade restart before entering a scenario, without manual interaction or bypassing platform license enforcement.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, the T5 r5 report evidence, managed job `20260723-174706-intellij-2026-2-t5-final-4ccc61b-ui-iu-f-d88113`, heartbeat screenshot `008_heartbeat/dialog0.png`, `runReleaseMatrixIdeWithFixture`, `FakeAiAssistantProbe`'s early app-lifecycle listener and existing subscription-dialog handling, and exact branch-262 Starter/Driver restart APIs or source.
- Escalate to: branch-262 `BackendMessagesService`, `DialogWrapper`, license transition, and Starter process-restart source only as needed to select a supported lifecycle.

Allowed inputs:

- The exact modal title/body/button, relevant IDE/Starter logs and thread dumps, current test-only lifecycle observers, official/source API evidence, and focused validation output.

Forbidden inputs:

- Production plugin code/resources, license bypass or spoofing, credentials, dismissing the dialog without applying the platform restart, generic dialog clicking, sleeps, retries as the fix, product skips, assertion weakening, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`

Dependencies:

- T5 r5 preserved exact local IU modal/log/thread evidence, stopped the managed job cleanly, and restored a report-only worktree diff; the maintainer explicitly requested recurring handling.

Validation:

- Add a red pure contract test proving only IntelliJ 2026.2's exact `Confirm Restart` title, full license-required body, and `Restart` action are accepted while near misses remain red. Implement the smallest supported test-only lifecycle that detects the transition before scenario work, invokes the real platform restart, waits for the restarted IDE/Driver/project, and reruns the existing plugin-action readiness barrier. Preserve an exact timeout/failure diagnostic for missing restart completion or any unexpected dialog. Run the formerly stalled IU scenario in three independent clean processes with evidence that at least one execution exercised the restart path, then the full IU lane; run a PyCharm smoke lane to prove no regression; run `compileIntegrationTestKotlin`, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R12 before restarting T5.

Escalation triggers:

- Escalate if Starter cannot follow a real IDE restart, the exact dialog cannot be observed before the EDT blocks, handling requires license bypass, the recurring path cannot be exercised deterministically, or a file outside the two-file scope is required.

Stop conditions:

- Passing requires manually assisted validation, generic dialog dismissal/clicking, license/configuration spoofing, a product skip, retry-only mitigation, sleeps, or production behavior changes.

Expected output:

- Exact dialog/restart lifecycle handling, positive/near-miss contract proof, repeated real IU evidence, full IU/PY validation, task commit, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: stopped cleanly; real restart succeeds but the retained Starter Driver cannot create a new remote session
- Worker: `/root/t5r12_license_restart`
- Changed files or reviewed diff: Temporarily implemented the exact two-file dialog/restart lifecycle and synthetic real-restart proof, then restored both files exactly after the stop condition.
- Validation evidence: Pure exact/near-miss contract passed 1/1 after a red unresolved-method proof; `compileIntegrationTestKotlin` passed. In managed job `20260723-183104-intellij-2026-2-t5r12-iu-synthetic-resta-2dbe60`, the exact handler clicked the real Restart action, IU process one shut down, IU process two started, and the persisted marker prevented a loop. The first post-restart remote call then failed with `Invoker.getRemoteCallResult: session must not be null`.
- Self-review evidence from `.agents/references/reviews.md`: No license state was altered or spoofed; the synthetic proof invoked the real platform restart. The worker did not treat a stale Driver connection as success or expand into custom Driver construction.
- Commit: None; all experimental code was restored.
- Worker events: Worker started at `2026-07-23T18:09:15.4932333+02:00`, reached the explicit Starter-cannot-follow-real-restart stop condition, cleaned the worktree, and stopped at `2026-07-23T18:40:31.2496910+02:00`.
- Orchestrator reconciliation: Worker claims match the managed job, idea log, two IDE process lifecycles, null-session test failure, clean worktree, and absence of active T5R12 jobs.
- Changelog/docs/spec/tasks updates: No user-facing behavior changed; record the task-local lifecycle evidence in this plan and the validation report.
- Blockers: Recovery must close the stale Starter context and acquire a new IDE process/Driver session outside the original `useDriverAndCloseIde` scope.
- Review risks: The expected restart/session-loss fingerprint must not mask unrelated process, JMX, Driver, or scenario failures.
- Handoff notes and next action: Execute T5R13 with an outer preflight/relaunch boundary; do not build a custom Driver or reuse the stale session.

### Task Packet: T5R13-relaunch-starter-after-license-restart

Task id: T5R13-relaunch-starter-after-license-restart

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `platform-docs-research`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Complete recurring license restart handling by ending the marked preflight context after the real restart and running the scenario once in a newly acquired Starter/Driver context.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, T5R12 result, managed job `20260723-183104-intellij-2026-2-t5r12-iu-synthetic-resta-2dbe60`, current `runReleaseMatrixIdeWithFixture`, early fake-plugin lifecycle observer, and exact Starter context/process cleanup source.
- Escalate to: branch-262 `IDETestContext`, `BackgroundRun`, `useDriverAndCloseIde`, and process/port cleanup source only as required to guarantee a fresh context.

Allowed inputs:

- The validated exact dialog/restart contract, marker/process/session-loss evidence, active-license alternative, current harness/fake plugin, and focused validation output.

Forbidden inputs:

- Production code/resources, custom Driver/JMX construction, license bypass/spoofing/credentials, generic exception swallowing, generic dialog clicking, sleeps, retry-only mitigation, product skips, assertion weakening, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`

Dependencies:

- T5R12 proved the real restart and one-shot marker behavior, proved the retained Driver's null-session boundary, and restored a clean worktree.

Validation:

- Red-test a pure outer lifecycle state machine: active license runs one context; exact marker plus exact restart-induced stale-session failure closes preflight and runs one fresh context; missing marker, wrong exception, restart loop, cleanup failure, or second-context failure remains red. Reintroduce the exact early handler and one-shot synthetic proof without changing license state. Implement an outer preflight that catches only the exact marked restart/session-loss fingerprint before the scenario block, fully closes the original context/process, waits through supported cleanup signals, then constructs a new Starter context and reruns project/plugin readiness before invoking the scenario exactly once. Run one synthetic real-restart lifecycle proof and the formerly stalled IU scenario in two additional clean active-license processes; require all three to pass. Run the full IU lane and PyCharm smoke; run `compileIntegrationTestKotlin`, `spotlessCheck`, `detekt`, and `git diff --check`; commit T5R13 before restarting T5.

Escalation triggers:

- Escalate if Starter cannot fully close the restarted preflight process/ports, a fresh context cannot be acquired without custom Driver construction, scenario execution cannot be proven exactly once, or a file outside the two-file scope is required.

Stop conditions:

- Passing requires swallowing a generic session failure, manually assisted validation, license/configuration spoofing, custom Driver/JMX construction, product skip, retry-only mitigation, sleeps, or production behavior changes.

Expected output:

- Exact outer preflight/relaunch lifecycle, positive/negative state-machine proof, real restart plus active-license repeated evidence, full IU/PY validation, task commit, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: stopped cleanly; IU recovery is proven, but PyCharm reproduced the same exact dialog outside the IU-only gate
- Worker: `/root/t5r13_starter_relaunch`
- Changed files or reviewed diff: Implemented the outer restart/relaunch lifecycle in `ReleaseMatrixUiHarnessTest.kt` and the exact dialog/marker/restart observer in `FakeAiAssistantProbe.kt`; the two-file diff remains uncommitted for T5R14.
- Validation evidence: Red compile proved the missing outer lifecycle; focused contract/state-machine tests passed 2/2. Synthetic IU job `20260723-192107-t5r13-iu-synthetic-license-relaunch-r4-7d9c6d` passed real restart, supported shutdown, process exit, release of ports 7777/10500/11111, fresh Starter/Driver acquisition, and exactly one scenario execution. Two active-license IU repetitions passed, and full IU job `20260723-192827-t5r13-full-iu-2026-2-c8e141` passed 25/25. PyCharm smoke job `20260723-194131-t5r13-py-2026-2-smoke-a5f146` preserved a failure where the exact license dialog held the write-intent permit; the unchanged focused PyCharm rerun `20260723-194934-t5r13-py-push-focused-unchanged-05e96a` passed 1/1, confirming intermittent timing.
- Self-review evidence from `.agents/references/reviews.md`: The handler requires the exact product/version/title/body/action, exact marker states, exact stale-session fingerprint, supported application exit, verified process/port cleanup, and exactly one fresh-context scenario. It does not alter license state, spoof configuration, click generic dialogs, swallow unrelated Driver failures, use sleeps/retries, or change production behavior.
- Commit: None; T5R14 must complete the observed PyCharm extension, formatting, and full validation before one task commit.
- Worker events: Started at `2026-07-23T18:44:56.2563487+02:00` and stopped at `2026-07-23T19:52:30.8506034+02:00` with no active managed job.
- Orchestrator reconciliation: Worker claims match the two-file diff, managed-job records, test XML, IU/PY idea logs, heartbeat screenshot, thread dumps, process/port evidence, and uncommitted worktree.
- Changelog/docs/spec/tasks updates: No user-facing plugin behavior changed; retain task-local evidence in this plan and the validation report.
- Blockers: The exact product gate must include observed PyCharm 2026.2 before the recurring license transition is handled across the required local lanes.
- Review risks: Product broadening must remain limited to observed IU/PY 2026.2 and preserve every negative contract and cleanup failure as red.
- Handoff notes and next action: Execute T5R14 in a fresh worker, format the retained two-file diff, prove synthetic PyCharm recovery, rerun full IU/PY, and commit.

### Task Packet: T5R14-extend-license-restart-to-pycharm

Task id: T5R14-extend-license-restart-to-pycharm

Lane: testing

Required skills:

- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `triage-flaky-test`

Goal:

- Extend the proven exact license restart/relaunch lifecycle from IU 2026.2 to the observed PyCharm 2026.2 product without weakening product, dialog, marker, session-loss, cleanup, or exactly-once boundaries.

Initial context budget:

- Read first: `AGENTS.md`, this plan's readiness/execution graph/this packet, the T5R13 result, the retained two-file diff, failed PyCharm job `20260723-194131-t5r13-py-2026-2-smoke-a5f146`, and focused rerun `20260723-194934-t5r13-py-push-focused-unchanged-05e96a`.
- Escalate to: the failed PyCharm idea log, heartbeat dialog screenshot, and thread dumps only as needed to verify the exact observed fingerprint.

Allowed inputs:

- The retained T5R13 implementation, validated IU lifecycle evidence, exact PyCharm dialog/thread-dump evidence, current product codes/builds, and focused/full validation output.

Forbidden inputs:

- Production code/resources, unobserved product codes, generic dialog matching/clicking, license bypass/spoofing/credentials, generic exception swallowing, custom Driver/JMX construction, sleeps, retry-only mitigation, product skips, assertion weakening, and unrelated source.

Write scope:

- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/fakeai/FakeAiAssistantProbe.kt`

Dependencies:

- T5R13 proved the exact IU restart/relaunch lifecycle and full IU lane, preserved the PyCharm failure, and left the two-file implementation uncommitted.

Validation:

- Red/green the exact product contract so IU and PyCharm 2026.2 match while a wrong product, version, title, body, or action remains rejected. Run a synthetic real PyCharm restart/relaunch proof with supported shutdown, process exit, port release, fresh Starter/Driver acquisition, and exactly one scenario execution. Run one clean active-license PyCharm focused scenario. Run the full IU lane and required PyCharm smoke. Run `spotlessApply`, then `compileIntegrationTestKotlin`, focused contract/state-machine tests, `spotlessCheck`, `detekt`, and `git diff --check`. Remove generated `allure-results/`, self-review the final two-file diff, and commit T5R14 before restarting T5.

Escalation triggers:

- Escalate if PyCharm requires a different dialog contract, restart/session fingerprint, unsupported process handling, a file outside the two-file scope, or any unobserved product expansion.

Stop conditions:

- Passing requires generic dialog or exception handling, manually assisted validation, license/configuration spoofing, custom Driver/JMX construction, product skip, retry-only mitigation, sleeps, production behavior changes, or acceptance of a second scenario execution.

Expected output:

- Exact IU/PyCharm product contract, synthetic and active PyCharm evidence, full IU/PY validation, formatted two-file task commit, events, reconciliation, and clean handoff to T5.

Result summary:

- Status: completed
- Worker: `/root/t5r14_recovery`
- Changed files or reviewed diff: Retained the T5R13 outer Starter restart/relaunch lifecycle in `ReleaseMatrixUiHarnessTest.kt` and `FakeAiAssistantProbe.kt`, and extended its exact product contract only to observed PyCharm 2026.2.
- Validation evidence: Synthetic PyCharm restart job `20260723-200137-t5r14-py-synthetic-license-relaunch-02a3b9` passed 1/1 with supported shutdown, process exit, port release, a fresh Starter/Driver context, and exactly one scenario execution. Active-license PyCharm job `20260723-200428-t5r14-py-active-license-focused-f8167d` passed 1/1 without recovery. Full PyCharm job `20260723-205028-t5r14-full-py-2026-2-terminal-preflight-f1ab7a` passed 13/13, and exact-current IntelliJ IDEA job `20260723-205950-t5r14-final-current-full-iu-2026-2-120b05` passed 25/25. Focused/static job `20260723-211218-t5r14-focused-static-final-92a26f` passed compilation, both contract/state-machine tests, `spotlessCheck`, and `detekt`; `git diff --check` also passed.
- Self-review evidence from `.agents/references/reviews.md`: IU and PyCharm require the exact 2026.2 product/version/title/body/action contract; wrong products and every dialog near miss remain red. Marker, stale-session, cleanup, and exactly-once boundaries remain explicit, generated `allure-results/` was removed, and the final commit contains only the two approved integration-test files. No production or user-facing plugin behavior changed.
- Commit: `ebe04440359812b75d05459b499e3cdf7ef5b6df`
- Worker events: The T5R14 worker sequence completed the five retained managed jobs, and the final recovery worker committed the clean two-file result; no T5R14 managed process remained active at handoff.
- Orchestrator reconciliation: The commit, managed-job status records, logs, test results, generated-artifact cleanup, and final two-file scope agree.
- Changelog/docs/spec/tasks updates: No changelog, specification, task, support, or user documentation change is required because the remediation affects only the release-matrix test harness and fake test plugin; this plan and the validation report own the evidence.
- Blockers: None within T5R14; the fresh T5 exact-head local, hosted, review, and readiness gate remains.
- Review risks: Preserve the exact IU/PyCharm 2026.2 boundary and fail closed for every unobserved product, dialog near miss, cleanup failure, unrelated Driver failure, or second scenario execution.
- Handoff notes and next action: Push `ebe0444` and this reconciliation, then dispatch a fresh T5 worker against the exact pushed head.

### Task Packet: T5D-align-published-pycharm-documentation

Task id: T5D-align-published-pycharm-documentation

Lane: docs

Required skills:

- `repository-documentation`
- `intellij-plugin-development`

Goal:

- Align every current/public compatibility statement with the published PyCharm 2026.2 release and regenerate Marketplace change notes from the corrected changelog.

Initial context budget:

- Read first: `AGENTS.md`, accepted ADR 0089, this plan's readiness and execution graph, this packet, the documentation ownership guidance, the five scoped files, and the Marketplace change-notes generator.
- Escalate to: only directly linked current/public compatibility documentation required to prove no stale availability statement remains.

Allowed inputs:

- JetBrains' published PyCharm 2026.2 product metadata, the accepted 2026.2 support decision, and current validation evidence.

Forbidden inputs:

- Historical ADR wording, production/test/workflow changes, unrelated documentation, and claims that final PR readiness has passed before current-head checks are terminal.

Write scope:

- `README.md`
- `CONTRIBUTING.md`
- `docs/SUPPORT.md`
- `CHANGELOG.md`
- `config/intellij-platform/change-notes.html` through the repository generator

Dependencies:

- PyCharm 2026.2 is published and the restarted T5 review identified the stale public wording.

Validation:

- Regenerate Marketplace change notes from `CHANGELOG.md`; run the documentation and Marketplace parity validators; search current/public documentation for stale PyCharm-unavailable claims; run `git diff --check`; do not stage, commit, or push.

Escalation triggers:

- Escalate if correcting the public availability statement requires a compatibility-policy change, historical ADR edit, or file outside the bounded scope.

Stop conditions:

- A scoped validator fails for a reason outside the documentation diff or source evidence contradicts the published release.

Expected output:

- Five-file documentation diff, generated Marketplace parity, validation evidence, events, reconciliation, and a clean handoff back to T5.

Result summary:

- Status: completed
- Worker: `/root/t5d_align_published_pycharm_docs`
- Changed files or reviewed diff: Updated the published-PyCharm compatibility statement in `README.md`, `CONTRIBUTING.md`, `docs/SUPPORT.md`, and `CHANGELOG.md`, then regenerated `config/intellij-platform/change-notes.html`.
- Validation evidence: Marketplace change-notes generator parity, `scripts/validate-docs.ps1`, stale PyCharm-unavailable wording search, and `git diff --check` passed.
- Self-review evidence from `.agents/references/reviews.md`: The five-file diff changes only current/public release-state wording, preserves the unchanged required product matrix, avoids a premature PR-readiness claim, and does not edit the historical ADR.
- Commit: `82abd9634effcc276b2d4821d8ee8b8657cd0ffe`
- Worker events: The sole active worker completed the bounded documentation packet without escalation.
- Orchestrator reconciliation: Worker claims match the exact five-file diff, generated changelog parity, required validation, clean task commit, and reserved plan/report scope.
- Changelog/docs/spec/tasks updates: Current public compatibility wording and generated Marketplace notes now agree that PyCharm 2026.2 is published and its local lanes pass.
- Blockers: None.
- Review risks: Final hosted and current-head readiness evidence remains owned by T5.
- Handoff notes and next action: Restart T5 on the documentation-aligned head.

## Execution Model

- Use one active worker at a time and a fresh sub-agent for each task packet.
- The orchestrator records a decision capsule, reserves each write scope, reconciles claims, and commits each completed task before the next.
- After T3, the orchestrator decides and applies any eligible `CHANGELOG.md` entry during reconciliation; workers may only suggest the text.
- T1 through T4, T3R, T5R, T5D, T5R6, T5R7, T5R8, T5R9, T5R11, T5R14, and the T1/T2 corrective work are complete. T5R2 through T5R5, T5R10, T5R12, and T5R13 stopped cleanly after producing bounded evidence; push the reconciled head, then restart T5.
- Follow `.agents/references/orchestration.md`; use the current upgrade worktree only.

## Long-Run Continuity

- Resume docs reread: after compaction, interruption, resume, or handoff, reread `AGENTS.md`; this plan's header, readiness, execution model, current packet, and result summary; `.agents/references/execution.md`; `.agents/references/orchestration.md`; `.agents/references/testing.md`; `.agents/references/reviews.md`; `.gitmessage` before commits; and the next owner files.
- Current task or wave: Push the reconciled T5R14 head, then run a fresh T5 exact-head gate.
- Completed commits: T1 `a7d4a5e635a1023b56f768d0bed915a278bce5b5`; T2 `a29e97485a710c56306c637a8ce8578594f5992b`; T3 `a0e2d0122bf90c2af8e373f44ad78cddbabaa54b`; T3R `d736a9120b599fd04e8ab0a19dbd7f28d7b4fac6`; T2 corrective `ceb791e44ea0f1724f7c67450f438c6169ce8bdd`; T1 integration corrective `bf3092218d3540650c27b23ccff2ad2ea04e8553`; T4 `8e4c78155b681f75521b45d3dd6b32d503ab8d40`; T5R `234d91e18bda4b6028a594316ed1e2d90d57229c`; T5D `82abd9634effcc276b2d4821d8ee8b8657cd0ffe`; T5R6 `777cf177ca1ea7c54156c761b54ab1250fc002d4`; T5R7 `c3bae72f614e8e4fb224aa93bbd82f0b1eade3be`; T5R8 `c6cc0c390126d21a0a58633918a553639ae73bbe`; T5R9 `ae5aa6dba8fe01ea4a4d0bea3fb816a33f25baf4`; T5R11 `df5964eb83a54b128dc3883b884cf5c33e1fe256`; T5R14 `ebe04440359812b75d05459b499e3cdf7ef5b6df`.
- Plan status and readiness: In Progress; the original plan and all bounded remediation packets through T5R14 are approved; T5R14 is complete, but T5 exact-head validation and readiness remain.
- Validation and self-review state: T5R14 passed synthetic and active-license PyCharm probes, full PyCharm 13/13, exact-current IntelliJ IDEA 25/25, focused contract/state-machine tests, compilation, formatting, Detekt, and diff checks. One upstream IU cyclic-extension initialization flake was preserved; its unchanged focused rerun and two later full IU runs passed.
- Worker event and reconciliation state: All workers through completed T5R14 worker `/root/t5r14_recovery` are reconciled; implementation commit `ebe0444` and this documentation-only reconciliation are ready to push.
- Changelog, docs, spec, task, or plan updates: Compatibility configuration, current public docs, changelog, and regenerated Marketplace notes are aligned with published 2026.2/JDK 25 state.
- Blockers or open questions: No open product question; the complete pushed-head local, hosted, review, and readiness gate remains required evidence.
- Next action: Keep PR #37 draft, push the T5R14 implementation and reconciliation commits, then dispatch a fresh T5 exact-head worker.
- Context handoff notes: Re-fetch the pushed head and review threads before readiness; preserve the exact IU/PY restart contract and supported fresh Starter context.

## Execution Graph

```mermaid
flowchart TD
    O1["O1[code]<br/>orchestrator"]
    W1["W1[code]<br/>T1 platform and VCS migration"]
    W2["W2[code]<br/>T2 CI and release matrix"]
    W3["W3[code]<br/>T3 support and product docs"]
    W3R["W3R[code]<br/>T3R regenerate change notes"]
    W4["W4[run-verify]<br/>T4 available-product validation"]
    G1["External gate<br/>PyCharm 2026.2 published"]
    W5["W5[run-verify]<br/>T5 PyCharm release gate"]
    W5R["W5R[run-verify]<br/>T5R PyCharm UI startup synchronization"]
    W5D["W5D[docs]<br/>T5D published PyCharm documentation"]
    W5R2["W5R2[run-verify]<br/>T5R2 PyCharm module reload barrier"]
    W5R3["W5R3[run-verify]<br/>T5R3 early reload observer"]
    W5R4["W5R4[run-verify]<br/>T5R4 loading-dialog observer"]
    W5R5["W5R5[run-verify]<br/>T5R5 enable-attempt completion"]
    W5R6["W5R6[run-verify]<br/>T5R6 staging-workflow rebuild"]
    W5R7["W5R7[run-verify]<br/>T5R7 scheme-race classification"]
    W5R8["W5R8[run-verify]<br/>T5R8 reflection failure coverage"]
    W5R9["W5R9[run-verify]<br/>T5R9 closed-index-storage classification"]
    W5R10["W5R10[run-verify]<br/>T5R10 residual reflection branches"]
    W5R11["W5R11[code]<br/>T5R11 simplify reflection null guards"]
    W5R12["W5R12[run-verify]<br/>T5R12 license restart lifecycle"]
    W5R13["W5R13[run-verify]<br/>T5R13 outer Starter relaunch"]
    W5R14["W5R14[run-verify]<br/>T5R14 PyCharm license restart"]
    O1 --> W1 --> W2 --> W3 --> W3R --> W4 --> G1 --> W5 --> W5R --> W5 --> W5D --> W5 --> W5R2 --> W5R3 --> W5R4 --> W5R5 --> W5R6 --> W5 --> W5R7 --> W5 --> W5R8 --> W5 --> W5R9 --> W5 --> W5R10 --> W5R11 --> W5 --> W5R12 --> W5R13 --> W5R14 --> W5 --> O1
```

## Validation

- Governance draft: run docs validation, agent-artifact validation, self-review, and `git diff --check`.
- T1 through T5 own implementation validation; drafting this plan authorizes none of it.

## Risks

- IntelliJ 262 internal and modular VCS APIs may require a narrower replacement than initial compile errors reveal.
- Compile-successful modules may still fail runtime class loading across products.
- The draft PR may remain red for an unknown period while PyCharm 2026.2 is unavailable.
- Published PyCharm may expose a new compatibility decision rather than a safe implementation detail.
- Java 25 changes every Gradle-running environment; local success does not prove hosted compatibility.

## Handoff Notes

- Research used `origin/main` at `3f3828826153d04f8719689e1102f5df6d29921f`.
- A property-only `buildPlugin` probe failed on Java target 25 versus Kotlin target 21.
- A diagnostic retry exposed missing VCS/DVCS modules and Kotlin-internal `GitStageCommitWorkflowHandler`; neither probe modified repository files.
- The PR stays draft until T5 completes on the current head.
