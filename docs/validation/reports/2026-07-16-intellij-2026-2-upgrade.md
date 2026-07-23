# Validation Report: IntelliJ Platform 2026.2 Upgrade

- Date: 2026-07-17
- Source ref: `codex/intellij-2026-2-upgrade`
- Source SHA: `a3a6cb9ca416a03c207c38e84bde90db606be620`
- Base SHA: `3f3828826153d04f8719689e1102f5df6d29921f`
- Pull request: [#37](https://github.com/kamkie/intellij-ai-commit-all-plugin/pull/37), draft
- Operating system: Windows
- Java runtime: Zulu OpenJDK `25.0.3+9-LTS`
- Artifact: `build/distributions/ai-commit-all-0.1.0-beta.9+19.a3a6cb9ca4.dirty.zip`; the dirty suffix records the pre-existing report-only worktree modification, while the saved validation context confirms source HEAD `a3a6cb9`.
- T4 conclusion: Complete. Every local gate for the available 2026.2 products passed; hosted build, security, analysis, and available-product verifier checks also passed on the current head. PyCharm 2026.2 remains unpublished and its required local and hosted lanes fail only during product resolution, before Plugin Verifier or IDE execution. The pull request remains draft for T5.

## Product Metadata

JetBrains' [official release feed](https://data.services.jetbrains.com/products/releases?code=IIU,PCP,WS&latest=true&type=release) returned the following stable releases when rechecked at 2026-07-17T02:42:45+02:00:

| Product       | Feed code | Latest stable version | Build          | Date       | T4 state               |
|---------------|-----------|-----------------------|----------------|------------|------------------------|
| IntelliJ IDEA | `IIU`     | 2026.2                | `262.8665.258` | 2026-07-16 | Available and passed   |
| PyCharm       | `PCP`     | 2026.1.4              | `261.26222.68` | 2026-07-03 | Unavailable at 2026.2  |
| WebStorm      | `WS`      | 2026.2                | `262.8665.259` | 2026-07-16 | Available and passed   |

## Fresh Exact-Head Prerelease Validation

The complete available-product prerelease command ran from scratch through the managed-jobs controller:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/run-local-prerelease-validation.ps1 -PluginVerifierIdeVersions IU-2026.2,WS-2026.2
```

- Job: `20260717-015843-intellij-2026-2-t4-final-a3a6cb9-prerele-98494b`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-015843-intellij-2026-2-t4-final-a3a6cb9-prerele-98494b.log`
- Structured status: `build/reports/local-prerelease-validation/status.json`
- Result: Passed in 8 minutes 54 seconds with exit code 0.

The saved context records HEAD `a3a6cb9`, script version 3, and only `IU-2026.2,WS-2026.2` as the available-product verifier inputs.

| Gate | Result | Evidence |
|------|--------|----------|
| Marketplace change notes and description | Passed | Generated-artifact parity checks completed in 0.633 and 0.612 seconds. |
| Documentation and agent artifacts | Passed | Documentation completed in 10.783 seconds; agent artifacts completed in 1.462 seconds. |
| Formatting, Detekt, tests, coverage, structure, and packaging | Passed | The combined Gradle gate completed in 56.006 seconds; 516 tests passed with one existing pending test. |
| Plugin Verifier, IntelliJ IDEA 2026.2 | Passed | `IU-262.8665.258` classified the plugin as compatible; two existing experimental `GitPushListener.onCompleted` usages remain. |
| Plugin Verifier, WebStorm 2026.2 | Passed | `WS-262.8665.259` classified the plugin as compatible; the same two experimental usages remain. |
| Final validation summary | Passed | All seven saved steps completed and printed successfully. |

`verifyPluginProjectConfiguration` also passed independently with every task rerun:

```powershell
.\gradlew.bat --no-configuration-cache --rerun-tasks verifyPluginProjectConfiguration
```

- Job: `20260717-020801-intellij-2026-2-t4-a3a6cb9-project-confi-8d1829`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-020801-intellij-2026-2-t4-a3a6cb9-project-confi-8d1829.log`
- Result: Passed in 11 seconds with exit code 0; six tasks executed.

The packaged descriptor has `since-build="262"`, no upper bound, and plugin version `0.1.0-beta.9+19.ga3a6cb9ca4.dirty`.

## Release-Matrix UI Validation

### IntelliJ IDEA 2026.2

The first forced full run preserved one infrastructure failure before the clean reruns:

```powershell
.\gradlew.bat --no-configuration-cache --rerun-tasks releaseMatrixUiTest -PideProducts=IU -PideVersion=2026.2
```

- Job: `20260717-020832-intellij-2026-2-t4-a3a6cb9-ui-iu-full-fr-349ebb`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-020832-intellij-2026-2-t4-a3a6cb9-ui-iu-full-fr-349ebb.log`
- Result: Failed after five scenarios passed. `stagingAreaAiInvocationSeesExactMultiRootCommitUiPaths()` lost its JMX connection while the test IDE was being restarted; the relaunched IDE retained port 7777, and 13 later failures were derived `Proposed port 7777 is not available` cascades rather than independent scenario failures.

The first sandbox log, XML test report, process identity, and port evidence were preserved before rerunning. The managed job completed, its test IDE exited, and ports 7777, 10500, and 11111 were verified free. No source changed between the failed attempt and the reruns.

The first real failure then passed in isolation:

```powershell
.\gradlew.bat --no-configuration-cache releaseMatrixUiTest -PideProducts=IU -PideVersion=2026.2 --tests pl.devopssolutions.aicommitall.integration.ReleaseMatrixUiHarnessTest.stagingAreaAiInvocationSeesExactMultiRootCommitUiPaths
```

- Job: `20260717-022101-intellij-2026-2-t4-a3a6cb9-ui-iu-staging-c4bd07`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-022101-intellij-2026-2-t4-a3a6cb9-ui-iu-staging-c4bd07.log`
- Result: 1/1 passed in 32 seconds; build passed in 37 seconds.

The clean full rerun then passed every scenario, including the staging scenario after the same five predecessors that preceded the failed attempt:

```powershell
.\gradlew.bat --no-configuration-cache releaseMatrixUiTest -PideProducts=IU -PideVersion=2026.2
```

- Job: `20260717-022337-intellij-2026-2-t4-a3a6cb9-ui-iu-full-re-aa90c1`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-022337-intellij-2026-2-t4-a3a6cb9-ui-iu-full-re-aa90c1.log`
- Result: 21/21 passed in 9 minutes 27 seconds; build passed in 9 minutes 30 seconds.

The focused and same-sequence full passes classify the initial restart/JMX/port sequence as a non-reproducible test-infrastructure flake, not a plugin failure.

### WebStorm 2026.2

The required WebStorm smoke subset passed:

```powershell
.\gradlew.bat --no-configuration-cache releaseMatrixUiTest -PideProducts=WS -PideVersion=2026.2
```

- Job: `20260717-023428-intellij-2026-2-t4-a3a6cb9-ui-ws-smoke-7d5b28`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-023428-intellij-2026-2-t4-a3a6cb9-ui-ws-smoke-7d5b28.log`
- Result: 13/13 passed in 5 minutes 25 seconds; build passed in 5 minutes 35 seconds.

Both UI lanes used the test-only AI Assistant substitute and temporary local Git repositories/bare remotes. They exercised AI, staging-enabled and staging-disabled commit, commit-and-push, outgoing-only push, shortcut takeover, empty/edited message stops, and missing-AI behavior without contacting or mutating a real remote.

## Expected PyCharm Availability Failure

The unchanged required PyCharm lane was invoked locally:

```powershell
.\gradlew.bat --no-configuration-cache releaseMatrixUiTest -PideProducts=PY -PideVersion=2026.2
```

- Job: `20260717-024104-intellij-2026-2-t4-a3a6cb9-ui-py-resolut-b455f0`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260717-024104-intellij-2026-2-t4-a3a6cb9-ui-py-resolut-b455f0.log`
- Result: Expected failure in 3 seconds with exit code 1: `Couldn't resolve PyCharmProfessional download URL for version: '2026.2'`.

The failure occurs while Gradle determines `releaseMatrixUiTest` dependencies, before an IDE launches. It is recorded as an external availability failure, not as a skipped or passing PyCharm test.

## Hosted Current-Head Evidence

GitHub Actions completed on PR head `a3a6cb9`:

| Workflow or job | Run | Result | Evidence |
|-----------------|-----|--------|----------|
| Security | [29543339406](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339406) | Passed | Trivy filesystem scan passed. |
| CodeQL | [29543339379](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339379) | Passed | Java/Kotlin analysis passed. |
| CI: Build and verify | [29543339393](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339393) | Passed | Formatting, docs, agent artifacts, Detekt, tests, coverage, structure, and packaging passed on JDK 25. |
| CI: UI coverage | [29543339393](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339393) | Expected unavailable-product failure | Dependency resolution stopped on the unpublished PyCharm 2026.2 build before the IDE or coverage test started; failure evidence upload still ran. |
| Plugin Verifier: IDEA | [29543339439](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339439) | Passed | `IU-2026.2` passed. |
| Plugin Verifier: WebStorm | [29543339439](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339439) | Passed | `WS-2026.2` passed. |
| Plugin Verifier: PyCharm | [29543339439](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29543339439) | Expected unavailable-product failure | `Couldn't resolve PyCharmProfessional download URL for version: '2026.2'` before Plugin Verifier execution. |

The required PyCharm failures remain red; no `continue-on-error`, skip, compatibility artifact, or alternate version weakens them. PR #37 is open and draft with an unstable merge state caused only by these two expected PyCharm-dependent failures.

## Full-Diff Review

The complete `origin/main..a3a6cb9` branch diff was reviewed: 33 files, 1,267 insertions, and 84 deletions. No confirmed production, CI, documentation, or compatibility defect remains.

- The production Git staging compatibility boundary is narrowly reflective and fails closed when required branch-262 members are unavailable.
- `IU-262.8665.258` and `WS-262.8665.259` both classify the artifact as compatible. The two `GitPushListener.onCompleted` experimental API usages are existing, explicit residual risk rather than a new incompatibility.
- The release-matrix harness version-gates exact known IntelliJ 2026.2 platform diagnostics. Theme colors remain platform-owned: the plugin and fake AI test plugin bundle no replacement color scheme, and IntelliJ retains its Darcula fallback.
- Compatibility docs, support policy, changelog, generated Marketplace notes/description, CI, and packaged `since-build=262` metadata agree on the 2026.2/JDK 25 baseline.
- `git diff --check origin/main..HEAD` passed.

Real JetBrains AI Assistant smoke was not possible because it requires an interactive signed-in user session. The release-matrix tests instead validate the full plugin workflow deterministically with the test substitute; this is the only skipped manual surface.

## Review And Readiness

The final re-fetch confirmed local HEAD, `origin/codex/intellij-2026-2-upgrade`, and PR #37 head all at `a3a6cb9`. The PR has no review decision and no review threads. Its only review is an earlier-head `COMMENTED` `LGTM` on `1072f42`, not a current-head approval.

Earlier T4 attempts found and preserved three non-PyCharm defects that were repaired in separately dispatched packets: stale generated Marketplace notes, the prerelease summary's `OrderedDictionary.ContainsKey` call, and unmigrated branch-262 integration-test APIs. This final run proves those repairs on the exact current head.

T4 is complete and committed evidence can advance to the external T5 gate. PyCharm 2026.2 publication is the only remaining compatibility-matrix blocker. Until JetBrains publishes it, the two required hosted checks remain red and PR #37 must remain draft; no release-readiness or PyCharm-pass claim is made.

## T5 PyCharm Release Gate

T5 started on 2026-07-23 against unchanged source head `e276ec09b1b91e4b64871b7a15c5c00579a0451a`. At the start of the gate, local HEAD, `origin/codex/intellij-2026-2-upgrade`, and PR #37 all matched that SHA. The PR remained open and draft, had no review threads or review decision, and retained only the earlier-head `COMMENTED` `LGTM` on `1072f42`.

JetBrains' [official release feed](https://data.services.jetbrains.com/products/releases?code=IIU,PCP,WS&latest=true&type=release) now reports PyCharm 2026.2 as build `262.8665.309`, released on 2026-07-21.

The unchanged PyCharm Plugin Verifier lane passed locally:

```powershell
.\gradlew.bat --no-configuration-cache --rerun-tasks verifyPlugin -PpluginVerifierIdeVersions=PY-2026.2
```

- Job: `20260723-101531-intellij-2026-2-t5-e276ec0-py-verifier-0fc151`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260723-101531-intellij-2026-2-t5-e276ec0-py-verifier-0fc151.log`
- Result: Passed in 5 minutes 31 seconds with exit code 0. `PY-262.8665.309` classified the plugin as compatible; only the same two existing experimental `GitPushListener.onCompleted` usages remain.

The unchanged PyCharm UI smoke lane also passed locally:

```powershell
.\gradlew.bat --no-configuration-cache --rerun-tasks releaseMatrixUiTest -PideProducts=PY -PideVersion=2026.2
```

- Job: `20260723-102223-intellij-2026-2-t5-e276ec0-ui-py-smoke-321a31`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260723-102223-intellij-2026-2-t5-e276ec0-ui-py-smoke-321a31.log`
- Result: 13/13 passed in 6 minutes 53 seconds; build passed in 7 minutes 30 seconds.

The hosted PyCharm verifier rerun passed on the same source head:

- Workflow: [Plugin Verifier run 29545915028, attempt 2](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29545915028/attempts/2)
- Job: [Verify against PY-2026.2](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29545915028/job/89152433982)
- Result: Passed against `PY-2026.2` on `e276ec09b1b91e4b64871b7a15c5c00579a0451a`.

The hosted PyCharm UI coverage rerun exposed the first failing T5 gate on the same source head:

- Workflow: [CI run 29545915006, attempt 2](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29545915006/attempts/2)
- Job: [UI coverage](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29545915006/job/89152436004)
- Command: `xvfb-run -a ./gradlew --no-configuration-cache releaseMatrixUiTest -PideProducts=PY -PideVersion=2026.2`
- Result: 10/13 passed and 3 failed in 8 minutes 6 seconds; the Gradle build failed after 12 minutes 31 seconds.
- First failure: `stagingAreaAiInvocationSeesExactMultiRootCommitUiPaths()` timed out after one minute with `invocations=0, paths=[]`.
- Additional failures: `missingAiActionStopsWithoutCommitOrPush()` could not activate the Push section, and `emptyGeneratedMessageStopsWithoutCommitOrPush()` timed out after 30 seconds with `invocations=0`.
- The workflow uploaded its release-matrix failure evidence successfully.

An unchanged hosted diagnostic rerun established the timing-dependent pattern:

- Workflow: [CI run 29545915006, attempt 3](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29545915006/attempts/3)
- Job: [UI coverage](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29545915006/job/89155652183)
- Result: 12/13 passed; only `stagingAreaAiInvocationSeesExactMultiRootCommitUiPaths()` repeated its `invocations=0, paths=[]` timeout. The missing-action and empty-message scenarios passed without source changes.
- Pattern: attempt 2 passed 10/13, attempt 3 passed 12/13, and the exact local lane passed 13/13.

The failing IDE logs identify an environment-dependent PyCharm startup race. In both failed staging executions, the workflow starts before PyCharm enables `com.intellij.modules.ultimate`; dynamic plugin reconfiguration then rebuilds the Commit UI while the AI phase is queued. IntelliJ subsequently refuses `FakeLlmCommitMessageAction` because the original `AiCommitAllThreeSectionControl` is no longer showing, so the fake invocation count correctly remains zero. The local Windows logs contain the same paid-plugin enable attempt before workflow activation, which leaves the control stable. T5 therefore requires the bounded `T5R-stabilize-pycharm-ui-startup` test-harness remediation before the full gate can be repeated.

The complete three-product prerelease gate was started after both local PyCharm checks passed, but was stopped as soon as the hosted UI failure became available:

- Job: `20260723-103035-intellij-2026-2-t5-e276ec0-full-prerelea-05c2e8`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260723-103035-intellij-2026-2-t5-e276ec0-full-prerelea-05c2e8.log`
- Result: Stopped during documentation validation, after the generated Marketplace parity checks passed. It is not readiness evidence.

The approved T5R remediation added IDE Starter's `doNotDisablePaidPluginsOnStartup()` option on source head `234d91e18bda4b6028a594316ed1e2d90d57229c`. Focused PyCharm executions passed 3/3 twice and a full PyCharm lane passed 13/13. Every fresh local IDE process contained `-Dide.do.not.disable.paid.plugins.on.startup=true`, and none logged the late Ultimate-module disable, re-enable, or dynamic-reconfiguration sequence.

The complete local gate then passed on the same source head:

- Full prerelease validation: job `20260723-110647-intellij-2026-2-t5-234d91e-full-prerelea-ace7fe`; all documentation, agent-artifact, formatting, static-analysis, test, coverage, structure, packaging, configuration, and Marketplace parity gates passed. IU, PY, and WS Plugin Verifier targets all classified the plugin as compatible.
- IntelliJ IDEA UI: job `20260723-111820-intellij-2026-2-t5-234d91e-ui-iu-full-d05dc5`; 21/21 passed.
- PyCharm UI: job `20260723-112708-intellij-2026-2-t5-234d91e-ui-py-smoke-df2d7d`; 13/13 passed.
- WebStorm UI: job `20260723-113317-intellij-2026-2-t5-234d91e-ui-ws-smoke-f86c50`; 13/13 passed.

Hosted checks on that source head passed build, CodeQL, security, Detekt, and all three Plugin Verifier targets. The hosted UI lane exposed a different environment-specific failure:

- Workflow: [CI run 29993726119](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29993726119)
- Job: [UI coverage](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/29993726119/job/89163423209)
- Result: Cancelled at the job's 45-minute limit.
- `commitShortcutCreatesLocalCommitWhenTakeoverEnabled()`, `pushSectionCommitsAndPushesToTemporaryBareRemote()`, and `pushShortcutPushesOutgoingOnlyLocalCommitWhenTakeoverEnabled()` each reached the IDE Starter 10-minute timeout because a modal dialog was shown. `startsIdeWithPluginFakeAiDependencyAndGitFixture()` also failed because the fake AI action did not write a message.
- The modal stack ends at `MessageDialogBuilder$Message.show -> com.intellij.ide.S.M.sQ.S`. The obfuscated platform class does not expose the title, but the failure appears only after requesting paid plugins before startup on the unlicensed hosted runner.
- Job cancellation skipped the failure-evidence upload; the GitHub job log preserves the failure.

T5D commit `82abd9634effcc276b2d4821d8ee8b8657cd0ffe` aligned README, contributor/support documentation, changelog, and generated Marketplace notes with the published PyCharm release. On that local head, Marketplace change-note and description parity, documentation validation, agent-artifact validation, and both worktree and branch `git diff --check` gates passed.

T5R2 removed the paid-startup option while testing three Driver-time reload barriers. All candidates compiled, but focused PyCharm executions proved that none can deterministically distinguish terminal completion from the pre-reload window:

- A1 job `20260723-121018-intellij-2026-2-t5r2-py-focused-a1-8198fa`: requiring `com.intellij.modules.ultimate` to be loaded passed one process but excluded a valid terminal failure path. In the next process, PyCharm enabled the plugin in configuration, entered `Loading Plugins`, and rejected dynamic reconfiguration because `intellij.database.dialects.hsql` referenced unavailable extension point `com.intellij.database.jdbcSourceLoader`; Ultimate remained unloaded while AI Commit All and fake actions were valid.
- A2 job `20260723-121723-intellij-2026-2-t5r2-py-focused-a2-0a25f7`: a `ProgressManagerListener` installed through the Driver probe missed the entire lifecycle. `Loading Plugins` finished at approximately `12:17:44.646`; the Driver utility became callable around `12:17:51.681`.
- A3 job `20260723-121951-intellij-2026-2-t5r2-py-focused-a3-96c067`: the platform cleared the failed plugin's non-load reason, leaving `loaded=false`, `disabled=false`, `nonLoadReason=null`, and no modal progress.

The remaining current-state candidates retain measured gaps: configuration becomes enabled about 349 milliseconds before the modal loading task begins, and the dynamic-plugin implementation's internal lock is acquired only after another approximately 225-millisecond interval. Treating either state as ready would preserve the original race. T5R2 therefore restored its two Kotlin files exactly to T5R and produced no commit.

T5R3 registered a test-only `ProgressManagerListener` from the fake AI plugin descriptor before trying to use the recorded lifecycle. The exact descriptor and callback contracts were confirmed from PyCharm 2026.2, and the listener was explicitly enabled in integration/headless modes. Compilation passed, but the focused evidence proved the platform operation does not publish this topic:

- Focused A1 job `20260723-123504-intellij-2026-2-t5r3-py-focused-a1-c63bf4` passed 3/6 scenarios before the staging case remained on `ChangesViewCommitWorkflowHandler`.
- Targeted listener job `20260723-124224-intellij-2026-2-t5r3-listener-proof-81beb2` passed 1/1, but `Loading Plugins` was shown at `12:43:04.305`, the listener was not constructed until `12:43:08.922`, and its first callback at `12:43:08.926` was the unrelated `Version Control: Processing Changed Files` task.
- The fake plugin descriptor and observer class were packaged correctly. The absence of a loading callback therefore belongs to `PlatformTaskSupport`, not descriptor packaging or a title mismatch.

T5R3 restored all three scoped files and produced no commit.

T5R4 then installed an `AppLifecycleListener` early enough to attach a backend AWT window observer before the reload. `compileIntegrationTestKotlin` passed, and targeted job `20260723-125313-intellij-2026-2-t5r4-py-lifecycle-proof-7af0e5` passed 1/1. The listener installed at `12:53:48.156`, before `PlatformTaskSupport` reported `Modal dialog is shown: Loading Plugins` at `12:53:49.636`, but neither `WINDOW_OPENED` nor `WINDOW_CLOSED` reached the observer. The remote-development split owns that modal in the frontend process, so the backend fake plugin cannot use its AWT lifecycle as a barrier. T5R4 restored all three scoped files and produced no commit.

The exact branch-262 `DynamicPluginEnabler` bytecode exposes a stronger backend boundary: it calls `DynamicPlugins.loadPlugins(...)` and only after that call returns notifies each `PluginEnableStateChangedListener` through `stateChanged(descriptors, true)`.

T5R5 registered that listener through the fake plugin's early app-lifecycle hook. Compilation passed, and callback proof job `20260723-130406-intellij-2026-2-t5r5-py-callback-proof-440677` passed 1/1 with exact ordering: observer installation at `13:04:24.936`, Ultimate enablement at `13:04:26.437`, dynamic reconfiguration rejection at `13:04:26.590`, and the exact `enabled=true`, `com.intellij.modules.ultimate` callback at `13:04:26.595`. There was no paid-startup flag, subscription modal, or stale-control rejection.

The six-scenario focused job `20260723-130525-intellij-2026-2-t5r5-py-focused-a1-25ced1` passed 5/6. Its staging scenario timed out because rejected Ultimate loading left the requested staging-enabled fixture on `ChangesViewCommitWorkflowHandler`. Narrow final-source job `20260723-130951-intellij-2026-2-t5r5-py-staging-repro-cd5335` reproduced the same state 0/1. The callback is therefore a valid completion signal, but completion of a rejected reload is not by itself readiness for the staging workflow. T5R5 restored all three scoped files and produced no commit.

T5R6 retained that exact callback barrier and changed the test probe's staging setter from direct preference mutation to the same real lifecycle used by IntelliJ's staging actions: `GitStageManagerKt.enableStagingArea(boolean)`, which updates `GitVcsApplicationSettings` and synchronously publishes `CommitModeManager.SETTINGS.settingsChanged()`. The paid-startup option was removed. The implementation is confined to the release-matrix harness, fake AI test probe, and fake plugin descriptor.

Validation on T5R6 commit `777cf177ca1ea7c54156c761b54ab1250fc002d4` passed:

- `compileIntegrationTestKotlin`.
- The formerly deterministic narrow staging failure in three independent IDE processes: jobs `20260723-132614-intellij-2026-2-t5r6-py-staging-r1-f586fd`, `20260723-132809-intellij-2026-2-t5r6-py-staging-r2-00bb1e`, and `20260723-132946-intellij-2026-2-t5r6-py-staging-r3-50acf9`, each 1/1.
- The six timing-sensitive scenarios twice: jobs `20260723-133128-intellij-2026-2-t5r6-py-focused-a1-760440` and `20260723-133535-intellij-2026-2-t5r6-py-focused-a2-7cb684`, each 6/6.
- Both staging-disabled and staging-enabled local commit flows: job `20260723-133919-intellij-2026-2-t5r6-py-staging-commit-m-7fef77`, 2/2.
- The full PyCharm lane twice: jobs `20260723-134129-intellij-2026-2-t5r6-py-full-r1-9786d5` and `20260723-134822-intellij-2026-2-t5r6-py-full-r2-b9bbbb`, each 13/13 in 6 minutes 28 seconds.
- All 13 full-lane IDE logs show the observer installed before Ultimate enablement and an exact terminal callback, with zero paid-startup-option or stale-control-rejection hits.
- `spotlessCheck`, `detekt`, and `git diff --check`.

T5R6 is locally complete. PR #37 must remain draft while current `main` is integrated and the complete exact-head local, hosted, review, and readiness gates are repeated.

Current `main` was integrated in merge commit `5d45025f9cc55573a90980b03a9165a99fbd9d73`, adding only the `actions/setup-node` 6-to-7 update from PR #38. On that exact pushed head:

- Full local prerelease job `20260723-140615-intellij-2026-2-t5-final-5d45025-prerele-f770ef` passed all eight gates, including IU/PY/WS compatibility.
- Hosted build, Security, CodeQL, Detekt, and all three Plugin Verifier jobs passed.
- Hosted UI coverage job [89201220085](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30005459240/job/89201220085) ran all 13 PyCharm smoke scenarios. Twelve passed. `emptyGeneratedMessageStopsWithoutCommitOrPush()` completed its plugin workflow but Starter promoted one synthetic platform error from a fresh IDE process.
- Failure artifact `ai-commit-all-ui-coverage-failure-evidence` (`8563226012`) records `java.util.ConcurrentModificationException` at `ArrayList$Itr.checkForComodification -> SchemeManagerImpl.findSchemeByName -> EditorColorsManagerImpl.getSchemeForCurrentUITheme -> FileStatusImpl.getColor`, with no plugin frame.
- In that process the T5R6 observer installed at `12:19:09.065`, the `Loading Plugins` operation began at `12:19:13.890`, the already-known Islands Dark missing-scheme diagnostics and concurrent modification appeared around `12:19:15.157`, and the first terminal Ultimate callback arrived only at `12:19:18.442`. The error therefore occurs during platform scheme mutation before the test harness can enter.
- Across the 13 hosted IDE logs, the existing exact Islands Dark diagnostic appeared in ten processes; the concurrent modification appeared once.

The release-matrix reporter already version-gates the exact Islands Dark branch-262 diagnostic. T5R7 must add the paired concurrent-mutation classification only when the synthetic name and the complete distinguishing SchemeManager/EditorColors/FileStatus stack match. Arbitrary concurrent modification, near-miss stacks, fallback color schemes, and production/theme changes remain forbidden. PR #37 stays draft until T5R7 and the complete exact-head gate pass.

T5R7 completed on commit `c3bae72f614e8e4fb224aa93bbd82f0b1eade3be`. It changes only the release-matrix integration harness and retains the existing exact `2026.2` gate. The new classifier accepts the captured hosted diagnostic only when the synthetic test name is exactly `java.util.ConcurrentModificationException: null` and its details contain all four distinguishing frames:

- `java.util.ArrayList$Itr.checkForComodification`
- `com.intellij.configurationStore.schemeManager.SchemeManagerImpl.findSchemeByName`
- `com.intellij.openapi.editor.colors.impl.EditorColorsManagerImpl.getSchemeForCurrentUITheme`
- `com.intellij.openapi.vcs.FileStatusImpl.getColor`

Synthetic coverage proves the captured form is accepted while a non-2026.2 IDE, an arbitrary test name, a generic concurrent modification, and every single-frame near miss remain rejected. `compileIntegrationTestKotlin` and the pure classifier test passed. The formerly failed PyCharm scenario passed in three independent IDE processes (`20260723-143215-intellij-2026-2-t5r7-py-empty-r1-56e6de`, `20260723-143315-intellij-2026-2-t5r7-py-empty-r2-248cbd`, and `20260723-143408-intellij-2026-2-t5r7-py-empty-r3-89db7e`), followed by a full 13/13 PyCharm smoke pass in `20260723-143500-intellij-2026-2-t5r7-py-full-637ff3`. `spotlessCheck`, `detekt`, and `git diff --check` also passed.

No production code or resources changed. T5R7 does not bundle fallback colors, clone or register a color scheme, modify a theme or user setting, retry or sleep, skip a product, or weaken a workflow assertion. PR #37 remains draft until the complete exact-head local, hosted, review, and readiness gates pass.

The complete T5 restart on exact head `a0bd6c7389c1d8f5bdb03c87e3e870f8d4acb2c3` passed every local and GitHub Actions gate:

- Full prerelease job `20260723-145004-intellij-2026-2-t5-final-a0bd6c7-prerele-4e7eb4` passed all eight gates, including IU/PY/WS compatibility.
- IntelliJ IDEA job `20260723-150158-intellij-2026-2-t5-final-a0bd6c7-ui-iu-f-ba901b` passed 22/22, comprising the original 21 full scenarios plus T5R7's untagged pure classifier test.
- PyCharm job `20260723-151129-intellij-2026-2-t5-final-a0bd6c7-ui-py-s-33c9e4` passed 13/13.
- WebStorm job `20260723-151907-intellij-2026-2-t5-final-a0bd6c7-ui-ws-s-9ded55` passed 13/13.
- Hosted [CI run 30008445405](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30008445405), [CodeQL run 30008445456](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30008445456), [Plugin Verifier run 30008445472](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30008445472), and [Security run 30008445377](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30008445377) all passed. This includes hosted PyCharm UI coverage and IU/PY/WS Plugin Verifier.

Two delayed Codecov checks then failed. `codecov/patch` check `89214533481` reported 65.48% diff coverage against a 90.27% target with 39 changed lines not fully covered. `codecov/project` check `89214528762` reported 89.58%, down 0.69% from base `753fda8`. Codecov attributed every missing line to `ReflectiveCommitWorkflowSynchronizer.kt`: 19 missing lines and 20 partial branches in the branch-262 reflection boundary.

T5R8 commit `c6cc0c390126d21a0a58633918a553639ae73bbe` adds seven unit tests for observable fail-closed behavior: handler invocation failure and cause, null workflow, null UI, missing nested methods, incompatible project, incompatible UI, and nested project invocation failure and cause. Every case requires null access and the exact `gitStageCommitWorkflowAccess` diagnostic.

For inserted production lines 330-457, unit coverage improved from 65/81 lines and 45/72 branches to 81/81 lines and 66/72 branches. The remaining six branches are compiler null guards dominated by required-method checks. Targeted tests passed 32/32; the full unit suite passed 523 with one existing pending test; `jacocoTestReport`, `verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and `git diff --check` passed. No production, threshold, exclusion, Codecov configuration, UI-harness, documentation, or changelog behavior changed. PR #37 remains draft until hosted aggregate Codecov and the complete exact-head gate confirm the remediation.

The complete T5 restart on exact head `37abdab54c635e1a680d05d5e73f49ac45f8d558` passed the full local prerelease gate in job `20260723-155138-intellij-2026-2-t5-final-37abdab-prerele-67fcb4`. Hosted build, Security, CodeQL, Detekt, and all three Plugin Verifier jobs also passed. Hosted PyCharm UI coverage job [89226997462](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30012892593/job/89226997462) ran all 13 scenarios: 11 passed and two otherwise-successful commit scenarios promoted the same platform-only `ClosedStorageException`.

Failure artifact `8566451340` showed both failures during nested `FileBasedIndexTumbler` plugin-loaded/unloaded cycles approximately 13 seconds before plugin workflow entry. Each stack named the branch-262 stub per-file-version storage path and contained:

- `com.intellij.util.io.PagedFileStorage.doGetBufferWrapper`
- `com.intellij.util.indexing.impl.perFileVersion.PersistentSubIndexerVersionEnumerator$MyEnumerator.enumerate`
- `com.intellij.util.indexing.impl.storage.VfsAwareMapReduceIndex.getIndexingStateForFile`
- `com.intellij.util.indexing.UnindexedFilesScanner$ScanningSession.scanFiles`

Neither stack contained a plugin frame. Both plugin workflows subsequently created the expected commit, reached commit count two, and left a clean Git state. Raw closed-storage text appeared in four of the 13 IDE logs but was promoted by Starter in only the two failed scenarios.

T5R9 commit `ae5aa6dba8fe01ea4a4d0bea3fb816a33f25baf4` changes only the release-matrix integration harness. Inside the existing exact `2026.2` gate, the new classifier requires the precise `ClosedStorageException` prefix, exact `/system/index/stubs/.perFileVersion/indexed_versions/indexed_versions_i` path suffix, and all four distinguishing frames above.

Synthetic proof accepts both captured promoted forms and rejects non-2026.2, arbitrary exceptions, wrong index paths, generic closed-storage failures, and each single-frame near miss. The red classifier test rejected the captured diagnostic before implementation; the green classifier passed 1/1. The two formerly failed scenarios then passed 6/6 across three independent IDE processes (`20260723-162217-intellij-2026-2-t5r9-py-two-scenarios-r1-50250f`, `20260723-162342-intellij-2026-2-t5r9-py-two-scenarios-r2-4f304c`, and `20260723-162530-intellij-2026-2-t5r9-py-two-scenarios-r3-de138d`), followed by a full 13/13 PyCharm smoke pass in `20260723-162708-intellij-2026-2-t5r9-py-full-71bd67`. `compileIntegrationTestKotlin`, `spotlessCheck`, `detekt`, and `git diff --check` passed.

No production, index, plugin-loading, retry, sleep, product-skip, or workflow-assertion behavior changed. PR #37 remains draft until the complete exact-head local, hosted, Codecov, review, and readiness gates pass.

### Final Exact-Head Restart After T5R9

T5 restarted on exact source, origin, and PR head `489a901b8158fc9c1b6f39a3232ab1bb0b1b6ac3`.
Local prerelease job `20260723-164340-intellij-2026-2-t5-final-489a901-prerele-5f10f9`
passed all eight gates: Marketplace change-note and description parity,
documentation validation, agent-artifact validation, build/tests/coverage/plugin
structure/packaging, and Plugin Verifier compatibility for IU, PY, and WS
2026.2.

Hosted exact-head evidence passed:

- CI build job
  [89239305940](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016945943/job/89239305940).
- UI coverage job
  [89240451739](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016945943/job/89240451739),
  including all 13 PyCharm scenarios and the aggregate coverage upload.
- Plugin Verifier jobs for
  [IU-2026.2](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016946292/job/89239306843),
  [PY-2026.2](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016946292/job/89239306898),
  and
  [WS-2026.2](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016946292/job/89239306889).
- Security job
  [89239305984](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016946042/job/89239305984),
  plus the Detekt and Trivy check runs.
- `codecov/project` check `89243823410`: 90.31%, up 0.03 percentage
  points from base `753fda8`; Codecov reported all tests successful.

The required `codecov/patch` check `89243828900` failed at 89.38% against
the 90.27% target. Its GitHub check output attributes all 12 not-fully-covered
diff lines to `ReflectiveCommitWorkflowSynchronizer.kt`: three missing lines
and nine partial lines. CodeQL job
[89239305957](https://github.com/kamkie/intellij-ai-commit-all-plugin/actions/runs/30016945909/job/89239305957)
was still running when the first failed current-head gate triggered T5's stop
condition.

Local IU job
`20260723-165443-intellij-2026-2-t5-final-489a901-ui-iu-f-75d764` was stopped
cleanly after its first 5 of 23 scenarios passed. The local PY and WS UI lanes
were not started because the hosted patch-coverage failure had already made the
current head ineligible for readiness.

The full `origin/main...HEAD` diff review found no correctness, security,
commit-selection, AI-invocation, push, platform-compatibility, or documentation
finding apart from the failed coverage gate. The PR remained draft, open, and
mergeable but blocked; `reviewDecision` was empty, there were no review requests
or review threads, and the only review was the maintainer's earlier-head
`COMMENTED` `LGTM` on `1072f42`. T5 remains incomplete until patch coverage and
the complete exact-head local, hosted, review, and readiness gates pass together.

T5R10 tested whether additional observable missing-method combinations could
close the patch deficit without touching production. Six temporary cases omitted
each handler method and each nested boundary method independently. Focused tests
passed 9/9 and the full suite passed 525/525 with one pending test, but exact
JaCoCo counters for production lines 330-486 remained unchanged at 650 covered
and 62 missed instructions, 69 covered and 7 missed branches, and 98 covered
and 0 missed lines. The worker restored the test file and produced no commit.

Six residual branches are compiler null paths dominated by the preceding
missing-method guard. The remaining reachable same-name/wrong-signature lookup
path can add only one hit; from Codecov's exact 101/113 patch ratio, that would
project to 102/113 = 90.26549%, still below the unchanged 90.27% target. The
next bounded remediation must therefore simplify only the redundant post-guard
null handling while preserving exact diagnostics and supported behavior.

T5R11 commit `df5964eb83a54b128dc3883b884cf5c33e1fe256`
implements that bounded simplification. Handler and nested method holders are
now constructed only after explicit non-null proof, so successful access no
longer carries later safe calls or `checkNotNull` guards for already-proven
methods. Behavioral tests cover each independently missing method and reject a
same-name method with the wrong signature.

Focused reflection tests passed 13/13 before and after the refactor. The full
unit suite executed 530 tests with one existing skip and passed. Exact JaCoCo
counters for the changed reflection range improved from 650 covered and 62
missed instructions, 69 covered and 7 missed branches, and 98 covered and 0
missed lines to 665/15 instructions, 68/0 branches, and 106/0 lines. The exact
patch projection improves from 101/113 = 89.38053% to 116/121 = 95.86777%,
above the unchanged 90.27% target. `jacocoTestReport`,
`verifyJacocoCoverageReport`, `spotlessCheck`, `detekt`, and
`git diff --check` passed.

Exact missing-method ordering and invocation/incompatible-result diagnostics
remain unchanged. No reflection fallback, public behavior, coverage
configuration, UI harness, documentation, specification, or changelog behavior
changed. Hosted aggregate Codecov remains the decisive confirmation.
