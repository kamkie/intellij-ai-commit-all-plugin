# Testing And Validation

Use validation that matches the change. Documentation-only changes do not require plugin builds unless they alter executable examples or build instructions.

## Success Criteria

For non-trivial work, connect the requested outcome to concrete validation before handoff. Use the smallest check set that can prove the changed behavior without relying on unrelated source-repository assumptions.

- For bug fixes, include or identify a reproduction when practical, then verify the fix against that scenario. If reproduction is impractical, explain the substitute evidence.
- For refactors, preserve behavior with before-and-after validation when feasible, or run the narrowest existing tests that cover the refactored path.
- For new validation rules, prove both the accepted path and the rejected path when the risk warrants it.
- For skipped checks, record the exact reason, such as unavailable IDE sandbox, missing configured task, documentation-only scope, or external dependency.

## Build Checks

Use the repository Gradle wrapper in validation examples: `.\gradlew.bat <task>` for PowerShell on Windows. `./gradlew <task>` is the equivalent for Unix-like shells and CI scripts.

- `.\gradlew.bat spotlessCheck` for Kotlin and Gradle Kotlin DSL formatting and Kotlin source license-header enforcement.
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` for Markdown linting, documentation structure, links, refs, ADR numbering and index, and proposal tracker checks.
- `npx --yes markdownlint-cli2@0.22.1` only when isolating Markdown lint failures outside the full docs validation script.
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1` for repository agent-artifact checks when `.agents/references/`, `.agents/skills/`, `.agents/prompts/`, or `.agents/plans/` changed.
- `.\gradlew.bat jacocoTestReport` for the JaCoCo XML coverage report uploaded to Codecov by CI.
- `.\gradlew.bat jacocoAggregateReport` for the merged unit and release-matrix UI coverage report. It merges every `*.exec` under `build/jacoco` (the unit `test.exec` plus the per-IDE-product release-matrix exec files) against the instrumented production classes, so coverage for the IntelliJ platform adapter classes that unit tests cannot reach is captured from the Starter-launched IDE. Each release-matrix UI matrix job attaches the JaCoCo agent to its IDE process and uploads its `releaseMatrixUiTest.exec` as an artifact; a dedicated `merge-coverage` job then downloads every product's exec, builds one merged report, and uploads it to Codecov under the `aggregate` flag. Pass `-Paicommitall.integrationCoverage=false` to skip the agent if it ever destabilizes the lane. The unit `verifyJacocoCoverageReport` gate is intentionally left independent of this report so pull-request CI does not depend on the heavy UI lane.
- `.\gradlew.bat test` for JUnit XML test result reports under `build/test-results/test/`, uploaded to Codecov by CI.
- `.\gradlew.bat buildPlugin` for packaging and basic compile validation.
- `.\gradlew.bat verifyPlugin` when configured.
- Plugin signing and signature verification once signing configuration exists.
- IntelliJ Plugin Verifier for the supported IDE version range once compatibility targets are chosen.
- CI workflow validation for pull-request checks without Marketplace or signing secrets once CI exists.

## Sandbox Checks

Use `.\gradlew.bat runIde` for manual sandbox testing.

Use current stable JetBrains IDE builds available through the user's All Products Pack. Record exact product names and build numbers in validation reports.

When a problem is reported from manual testing in a real IDE or Gradle sandbox IDE, ask whether the user wants the agent to analyze the relevant IDE logs before inferring the cause. Request sanitized log excerpts or explicit permission to inspect the local logs folder, and remind the user not to share secrets, tokens, proprietary source content, or private commit messages.

Use `.agents/references/troubleshooting.md` for validation, Gradle, plugin packaging, sandbox IDE, test, and IDE-log failure diagnosis.

Create end-to-end tests against local Git repositories where the IntelliJ test framework, Gradle sandbox, and CI environment make that practical. Keep manual sandbox coverage for scenarios that cannot be automated reliably yet.

Manual scenarios for this plugin:

- Modified tracked file is included.
- Deleted tracked file is included.
- Moved or renamed tracked file is included.
- Unversioned file is included.
- Ignored file is excluded.
- Files from all changelists in the supported VCS scope are included.
- Commit-only flow commits selected files after AI message generation.
- Commit-and-push flow pushes after a successful commit when that flow is selected.
- Existing before-commit checks, commit warnings, and push errors remain active through the IDE workflow.
- JetBrains AI Assistant dependency missing or disabled fails installation/loading.
- AI Assistant present but unavailable or not signed in stops without commit or push.
- Standard IntelliJ, Git, VCS, push, and AI Assistant errors are surfaced or forwarded without being masked by custom plugin text.
- Git staging area enabled.
- Git staging area disabled.
- Local-repository E2E coverage for commit selection, changelists, staging modes, commit-only, and local-remote commit-and-push where safe.
- Empty change set.
- User edits or clears the message while AI generation is in progress.
- Release workflow only through a gated/manual path with secrets supplied outside the repository.

## Review Checks

- Confirm no validation relies on source repo assumptions from unrelated Spring, REST, OpenAPI, release, or operations workflows.
- Confirm failures are reported without committing.
- Confirm platform-owned errors are not replaced by less precise plugin-owned notifications.
- Confirm implementation does not bypass IDE commit or push safeguards.
- Confirm changelists and Git staging enabled/disabled paths remain covered.
- Confirm local-repository E2E tests do not push to real remotes.
- Confirm publishing and signing secrets are not committed or required for pull-request checks.
- Confirm timeout paths do not leave the user with an unintended commit.

## Flaky Test Triage

Use `.agents/skills/triage-flaky-test/SKILL.md` when a test alternates between pass and fail without relevant source changes.

- Preserve the first failure output and exact command before rerunning.
- Rerun the narrowest failing test first, then the smallest enclosing task.
- Diagnose shared state, fixture lifecycle, temp directories, real remotes, sleeps, timing assumptions, platform background work, and environment differences before changing assertions.
- Prefer deterministic setup, cleanup, and synchronization over retries.

## Reporting

When handing off, state:

- Commands run.
- Manual release-validation scenarios tested, if any.
- Checks not run and why.
- Residual IDE compatibility risk.
