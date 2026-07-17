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
