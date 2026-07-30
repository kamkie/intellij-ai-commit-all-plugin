# Plan: Transitive Build Dependency Remediation

Plan-ID: PLAN-transitive-build-dependency-remediation

Status: In Progress

Workers: 1

Filename: `.agents/plans/PLAN-transitive-build-dependency-remediation.md`

## Readiness

- Plan readiness: Approved and resumed after the maintainer authorized a release-line version-matching correction.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-07-30T20:39:23+02:00
- Open questions: None.
- Implementation progress: T1 recovery will remove patch-coordinate coupling from the 2026.2 UI harness, then repeat the required release-matrix validation.

## Status History

- 2026-07-30T18:58:43+02:00: none -> Draft by Codex <codex@openai.com>; investigation established the alert families, upstream dependency paths, and compatibility-sensitive remediation boundary.
- 2026-07-30T20:39:23+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; the maintainer explicitly approved PR #42 and authorized autonomous implementation.
- 2026-07-30T20:39:24+02:00: Approved -> In Progress by Codex <codex@openai.com>; autonomous approved-plan execution started after confirming PR #41 was merged.
- 2026-07-30T21:17:23+02:00: In Progress -> Blocked by Codex <codex@openai.com>; repeated full IntelliJ UI validation failed on unrelated, nondeterministic JetBrains theme and Windows workspace-cache infrastructure races.
- 2026-07-30T21:30:08+02:00: Blocked -> In Progress by Kamil Kiewisz <kamkie@outlook.com>; the maintainer authorized stable release-line matching instead of mutable patch-version hardcoding and resumed autonomous execution.

## Goal

Reduce the repository's 19 open transitive Dependabot alerts wherever patched versions can be applied safely to build and integration-test configurations, while preserving IntelliJ IDE Starter compatibility and reporting upstream-only alerts accurately.

## Investigation Findings

- GitHub reports 19 open Gradle alerts in `settings.gradle.kts`: 7 high and 12 medium.
- The alert families are:
  - 9 Netty alerts, patched in `4.2.16.Final`.
  - 7 Jackson 2 alerts, requiring patched releases through `2.21.5`.
  - 1 Jackson 3 alert, patched in `3.1.5`.
  - 1 OpenTelemetry API alert, patched in `1.62.0`.
  - 1 LZ4 alert, patched in `1.11.1`.
- The dependencies are build or integration-test tooling and are not packaged in the distributed plugin.
- JetBrains IDE Starter `262.8665.337`, used by IntelliJ IDEA 2026.2.0.1, still declares Netty `4.2.15.Final`, Jackson 2 `2.19.0`, Jackson 3 `3.1.4`, OpenTelemetry `1.48.0`, and LZ4 `1.11.0`.
- JetBrains IntelliJ Platform Gradle Plugin `2.18.1` also reaches Jackson 2 `2.20.2` through its own settings/build plugin classpath. Ordinary project dependency constraints cannot replace that settings-plugin dependency.
- GitHub's Gradle dependency-submission action supports configuration filters, but filtering out build or test dependencies would hide executable supply-chain dependencies rather than remediate them. This plan does not use filtering or alert dismissal as a fix.
- The first T1 UI run exposed an existing harness regression introduced when `platformVersion` changed from `2026.2` to `2026.2.0.1`: exact `ideVersion == "2026.2"` predicates disabled both license-restart setup and known 2026.2 platform-error classification even though the IDE still reports the stable `2026.2` release line.

Primary evidence:

- [Dependabot alerts](https://github.com/kamkie/intellij-ai-commit-all-plugin/security/dependabot)
- [IDE Starter 262.8665.337 POM](https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/tools/ide-starter-squashed/262.8665.337/ide-starter-squashed-262.8665.337.pom)
- [IntelliJ Platform Gradle Plugin 2.18.1 POM](https://plugins.gradle.org/m2/org/jetbrains/intellij/platform/intellij-platform-gradle-plugin/2.18.1/intellij-platform-gradle-plugin-2.18.1.pom)
- [Gradle dependency-submission inputs](https://github.com/gradle/actions/blob/main/dependency-submission/action.yml)

## Non-Goals

- Do not change plugin runtime behavior or the IntelliJ 2026.2 support baseline.
- Do not add dependencies to the packaged plugin.
- Do not exclude configurations merely to suppress GitHub alerts.
- Do not dismiss alerts through the GitHub API.
- Do not force versions across every Gradle configuration.
- Do not upgrade unrelated dependencies or refactor the build.

## Assumptions

- Upgrade PR #41 lands before implementation, so dependency probes use IntelliJ IDEA 2026.2.0.1 build `262.8665.337`.
- Patched minimum versions are preferred over newer unrelated releases.
- An alert remains open when its only safe remedy is an upstream JetBrains update.
- A constraint is retained only when dependency insight proves its scope and the full relevant validation remains compatible.

## Open Questions

None. Any new incompatibility or broader remediation choice stops implementation and returns the plan to maintainer review.

## Proposed Changes

### T1: Prove and remediate safe project configurations

1. Capture sequential `dependencyInsight` evidence for every alert family on the integration-test runtime and any other affected project configuration.
2. Add the smallest constraints limited to configurations that execute IntelliJ IDE Starter or integration tests.
3. Evaluate these patched minimums independently so one incompatible family does not block safer fixes:
   - Netty BOM `4.2.16.Final`.
   - Jackson 3 BOM `3.1.5`.
   - LZ4 `1.11.1`.
   - OpenTelemetry BOM `1.62.0`.
   - Jackson 2 BOM `2.21.5` only where it does not attempt to alter the settings-plugin classpath.
4. Remove any constraint that does not affect the vulnerable resolution path or that destabilizes IDE Starter.
5. Preserve unresolved settings-plugin alerts as upstream findings; do not hide or dismiss them.
6. Match the stable 2026.2 release line independently of mutable patch-qualified artifact coordinates, cover base and patch-qualified inputs plus adjacent-line near misses, and rerun the stopped UI validation.

Expected implementation files:

- `build.gradle.kts`
- `gradle.properties` only if named version properties make the retained constraints clearer
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- This plan and `.agents/plans/README.md` for lifecycle and result metadata

## Task Packets

### Task Packet: T1-prove-and-remediate-safe-project-configurations

Task id: T1-prove-and-remediate-safe-project-configurations

Lane: implementation

Required skills:

- `gh-fix-ci-security-quality`
- `intellij-plugin-development`
- `kotlin-plugin-style`
- `plugin-test-tdd`
- `platform-docs-research`
- `managed-jobs`

Goal:

- Apply only compatibility-proven patched versions to affected project configurations, validate them, and distinguish remediated alerts from upstream-only settings-plugin alerts.

Initial context budget:

- Read first:
  - This plan's header, readiness summary, execution graph, and this task packet.
  - `AGENTS.md`.
  - `build.gradle.kts`.
  - `gradle.properties`.
  - `settings.gradle.kts`.
  - `.github/workflows/dependency-submission.yml`.
  - `docs/decisions/adr-0089-advance-minimum-intellij-platform-to-2026-2.md`.
  - `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`.
  - The exact failure evidence in `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260730-205453-dependency-remediation-ui-iu-3ad25e.log`.
  - `.agents/references/testing.md`.
  - `.agents/references/reviews.md`.
- Escalate to:
  - JetBrains or Gradle primary dependency metadata for a conflicting resolution.
  - `.agents/references/troubleshooting.md` for a validation failure.

Allowed inputs:

- Files and artifacts named in `Read first`.
- Current GitHub Dependabot alert data and dependency-graph SBOM.
- Files and sources named in `Escalate to` only after an escalation trigger fires.

Forbidden inputs:

- Unrelated archived plans.
- Previous worker chat beyond the orchestrator handoff summary.
- Unrelated source or product documentation.

Write scope:

- `build.gradle.kts`
- `gradle.properties`
- `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`
- `.agents/plans/PLAN-transitive-build-dependency-remediation.md`
- `.agents/plans/README.md`

Dependencies:

- PR #41 merged into `main`.
- Explicit maintainer approval recorded in this plan.

Validation:

- Run sequential `dependencyInsight` checks for Netty, Jackson 2, Jackson 3, OpenTelemetry, and LZ4 before and after constraints.
- Prove a red regression test for patch-qualified 2026.2 coordinates, then green coverage for base and patch-qualified 2026.2 inputs while adjacent release lines remain rejected.
- Run `.\gradlew.bat --no-daemon spotlessCheck test buildPlugin verifyPlugin`.
- Run the existing IntelliJ IDEA full and PyCharm/WebStorm smoke release-matrix UI lanes on the 2026.2 release line.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1`.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1`.
- Run `git diff --check`.
- Re-run the dependency-submission workflow on the pushed branch and inspect the generated snapshot before merge.
- After merge and GitHub reprocessing, re-query open Dependabot alerts and record which findings remain upstream-only.
- Self-review that no constraint enters the packaged plugin, no configuration is filtered from dependency submission, and no alert is dismissed.

Escalation triggers:

- Escalate if a patched family changes production compile or runtime configurations.
- Escalate if IDE Starter, Plugin Verifier, or a release-matrix UI lane fails after a constraint.
- Escalate if the dependency graph reports a settings/build plugin path that project constraints cannot control.
- Escalate if a candidate requires a version newer than the first patched release.
- Escalate if stable release-line matching would accept a different IntelliJ release line or require parsing an undocumented version format.

Stop conditions:

- A constraint would alter the packaged plugin.
- Safe remediation requires dependency-submission filtering or alert dismissal.
- A JetBrains tooling dependency is binary-incompatible with its patched version.
- A new durable dependency or support policy decision is required.

Expected output:

- A minimal build-configuration diff containing only compatible constraints.
- Before-and-after dependency resolution evidence for every retained constraint.
- Validation and self-review evidence.
- A task commit and pushed pull-request head.
- A result summary distinguishing remediated and upstream-only alert families.
- No changelog entry unless implementation unexpectedly changes public plugin behavior.

Result summary:

- Status: W2 recovery validated; commit, push, and branch dependency-submission inspection pending.
- Worker: W2 (`code`) completed local recovery validation.
- Changed files or reviewed diff: `build.gradle.kts` and `gradle.properties` add integration-test-only BOM platforms for Netty `4.2.16.Final`, Jackson 2 `2.21.5`, Jackson 3 `3.1.5`, and OpenTelemetry `1.62.0`, plus an LZ4 `1.11.1` constraint. `ReleaseMatrixUiHarnessTest.kt` uses one delimiter-aware release-line predicate for the license-restart setup and known 2026.2 platform-error classifier.
- Validation evidence:
  - Sequential `integrationTestRuntimeClasspath` dependency insight changed Netty `4.2.15.Final -> 4.2.16.Final`, Jackson 2 `2.19.0 -> 2.21.5`, Jackson 3 `3.1.4 -> 3.1.5`, OpenTelemetry `1.48.0 -> 1.62.0`, and LZ4 `1.11.0 -> 1.11.1`; all five candidates were effective and retained.
  - RED: patch-qualified 2026.2 classification failed in managed job `20260730-213506-dependency-remediation-red-release-line-34659a`. GREEN: base and patch-qualified inputs passed while `2026.1`, `2026.20`, `2026.3`, and `2025.2` remained rejected in job `20260730-213628-dependency-remediation-green-release-lin-85181b`.
  - `runtimeClasspath` remains empty, and the built plugin ZIP contains only the plugin-owned JAR under `lib/`, so none of the constrained families enters production or packaged-plugin runtime. `.\gradlew.bat --no-daemon spotlessCheck test buildPlugin verifyPlugin` passed in job `20260730-214507-dependency-remediation-w2-gradle-gate-re-0fdfa2` with 529 tests passing, one pending, and Plugin Verifier reporting compatibility with `IU-262.8665.337`.
  - Sequential release-matrix validation passed: IU full, 26 tests in job `20260730-215009-dependency-remediation-w2-ui-iu-full-842667`; PY smoke, 13 tests in job `20260730-220902-dependency-remediation-w2-ui-py-smoke-f6ac69`; WS smoke, 13 tests in job `20260730-222601-dependency-remediation-w2-ui-ws-smoke-c056a4`. Restart-enabled IU/PY scenarios recorded one restart PID, accepted shutdown, process exit, released ports, and continuation in a fresh context without loops.
- Self-review evidence from `.agents/references/reviews.md`: constraints are scoped only to `integrationTestImplementation`, the release-matrix task executes that source set's runtime classpath, main runtime remains empty, no packaged-plugin configuration or workflow filter changed, no alert was dismissed, and the diff contains no unrelated cleanup.
- Commit: Pending final documentation validation and commit.
- Worker events: W2 started from `038ac8e`; preserved W1's dependency diff, proved the release-line regression red/green, and completed all local validation.
- Orchestrator reconciliation: Pending W2 handoff.
- Changelog/docs/spec/tasks updates: Not applicable; the change affects build-time integration-test resolution only.
- Blockers: None.
- Review risks: GitHub dependency submission must still prove the project path is remediated while settings-plugin paths remain visible. Current open alerts are all attributed to `settings.gradle.kts`; those settings-plugin Netty, Jackson 2/3, OpenTelemetry, and LZ4 copies are upstream-only and intentionally preserved without filtering or dismissal.
- Handoff notes and next action: Validate plan artifacts, commit and push the complete task, then dispatch and inspect branch dependency submission.

## Execution Model

- Use one fresh implementation worker per initial or recovery attempt after explicit approval.
- The orchestrator owns plan status, worker reconciliation, GitHub alert rechecks, and final handoff.
- If sub-agents are unavailable or unauthorized, stop before implementation instead of executing this approved-plan task locally.
- Keep all resolution probes sequential to avoid Gradle daemon and cache contention.
- Use the current branch only.

## Long-Run Continuity

- Resume docs reread:
  - After context compaction, interruption, resume, or handoff, reread `AGENTS.md`; this plan's header, `## Readiness`, `## Long-Run Continuity`, `## Execution Model`, current task packet, and current result summary; `.agents/references/execution.md`; `.agents/references/orchestration.md`; `.agents/references/testing.md`; `.agents/references/reviews.md`; `.gitmessage` before any commit; and the next action's exact owner files.
- Current task or wave: T1-prove-and-remediate-safe-project-configurations recovery has passed local validation.
- Completed commits: None.
- Plan status and readiness: In Progress; patch-independent 2026.2 release-line handling authorized by Kamil Kiewisz <kamkie@outlook.com>.
- Validation and self-review state: All five dependency constraints resolve at their first patched versions on `integrationTestRuntimeClasspath`; production runtime is unchanged; formatting, unit tests, packaging, Plugin Verifier, IU full UI, PY smoke UI, and WS smoke UI pass.
- Worker event state: W2 recovery completed local validation.
- Orchestrator reconciliation state: Pending W2 handoff after commit, push, and branch dependency-submission inspection.
- Changelog, docs, spec, task, or plan updates: Build configuration, version properties, and this plan evidence only; changelog, public docs, specification, and tasks are not affected.
- Blockers or open questions: None.
- Next action: Commit and push T1, then inspect branch dependency-submission output.
- Context handoff notes: Preserve upstream-only settings-plugin alerts rather than filtering or dismissing them; avoid hardcoding mutable patch details; do not commit or push until required UI validation passes.

## Execution Graph

```mermaid
flowchart TD
    O1["O1[code]<br/>orchestrator"]
    W1["W1[code]<br/>T1: prove and remediate safe project configurations"]
    W2["W2[code]<br/>T1 recovery: stable release-line handling and validation"]
    O1 --> W1
    W1 --> O1
    O1 --> W2
    W2 --> O1
```

## Validation

- Plan-only PR:
  - `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1`
  - `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1`
  - `git diff --check`
- Implementation validation is defined in task packet T1.

## Risks

- Jackson 2 and OpenTelemetry require larger minor-version jumps than the other alert families and may be binary-incompatible with JetBrains tooling.
- Settings-plugin dependencies cannot be governed by ordinary project constraints, so a truthful result may retain some alerts until JetBrains publishes an updated Gradle plugin.
- Dependency resolution passing is insufficient; IDE Starter and release-matrix UI execution must also pass.
- GitHub alert closure occurs asynchronously after an updated default-branch dependency snapshot.

## Handoff Notes

- Upgrade PR #41 is intentionally separate from this security plan.
- The investigation found no direct vulnerable dependency in the packaged plugin.
- Do not represent a reduced alert count as complete remediation while upstream-only alerts remain.
