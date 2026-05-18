---
name: plugin-test-tdd
description: TDD workflow for IntelliJ plugin tests in this repository. Use when fixing bugs, adding regression coverage, expanding test cases, validating VCS/commit/push workflow behavior, testing settings or services, proving IntelliJ Platform integration behavior, or when the user asks for tests that fail before the fix.
---

# Plugin Test TDD

## Start

- Read `.agents/references/testing.md`.
- Read `.agents/references/code-style.md` when adding Kotlin test helpers or production seams.
- Identify the user-visible behavior or workflow invariant before naming test cases.

## Red First

1. Choose the smallest boundary that would have caught the bug: pure unit, service collaborator, local Git repository workflow, IntelliJ light/heavy fixture, or manual sandbox scenario.
2. Add one or more tests that fail for the current code and prove the missing behavior.
3. Run the targeted test command before fixing production code, for example:

```powershell
.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.package.ClassTest"
```

4. Record the red result in the handoff or commit message validation trailer.

## Test Design

- Test behavior, not private method structure.
- Give each test one clear reason to fail.
- Map every new regression test to the bug or workflow invariant it protects.
- For manual sandbox checks, larger workflows, or bug reports that need translation into coverage, capture a compact case first: traceability link, title, preconditions, exact test data or repository state, action steps, and expected result.
- Keep each structured case atomic: one observable outcome, no unrelated assertions, and under 10 manual steps when a human must execute it.
- Shape automated tests as Arrange-Act-Assert; put repeated setup in fixtures or preconditions, but keep important assumptions visible at the call site.
- Cover success, failure, retry/timeout, empty input, duplicate input, multi-root, staging-enabled, staging-disabled, and user-edit stop paths when those cases are relevant to the bug.
- Prefer deterministic fakes and captured calls for platform seams that are expensive to initialize.
- Keep fixture setup small and explicit; avoid hidden global state, order dependencies, and data that matters only by convention.
- Use local repositories only; never push to a real remote from automated tests.
- Do not use sleeps for async behavior. Use controlled callbacks, latches, polling with timeout helpers, or platform test utilities.
- Avoid OS-specific assumptions about separators, case sensitivity, default encoding, and line endings.
- For IntelliJ Platform tests, prefer the lightest fixture that proves the behavior. Use heavier IDE or sandbox coverage only when the bug is in platform wiring.
- When a test depends on mocks or environment assumptions, name the limitation in the handoff so the remaining confidence gap is visible.

## Green And Refactor

- Implement the smallest production change that makes the red tests pass.
- Run the targeted test again.
- Run `.\gradlew.bat test` for shared workflow changes.
- Run `.\gradlew.bat buildPlugin` when plugin packaging, descriptors, Gradle config, or compatibility boundaries changed.
- Run docs validation when tests required ADR, task, changelog, plan, or agent guidance updates.
- End with `git diff --check`.

Report the red command, green commands, any broader checks skipped, and residual manual sandbox coverage.
