---
name: triage-flaky-test
description: Flaky-test triage workflow for this IntelliJ plugin repository. Use when a Gradle, JUnit, IntelliJ fixture, local Git repository, sandbox, or CI test alternates between pass and fail without relevant source changes.
---

# Triage Flaky Test

## Start

- Read `.agents/references/testing.md`.
- Read `.agents/references/troubleshooting.md`.
- Preserve the first failing command, stack trace, test report path, operating system, JDK, Gradle task, and relevant IDE or sandbox log path before rerunning anything.
- Identify whether the failure is deterministic, order-dependent, timing-dependent, environment-dependent, or data-dependent.
- Do not delete, weaken, ignore, or relax assertions until the root cause is understood.

## Reproduce

1. Rerun the narrowest failing test by class or method.
2. Rerun the same narrow command enough times to see whether it fails consistently.
3. If the narrow command passes, rerun the smallest enclosing Gradle task that failed.
4. If order dependence is suspected, run the neighboring tests or the full test class.
5. Record the exact pass/fail pattern rather than summarizing it as flaky.

## Diagnosis

- Check shared mutable state, leaked disposables, unclosed projects, reused temp directories, global services, static caches, and system properties.
- Check local Git repository tests for real remotes, branch name assumptions, staging mode assumptions, line-ending differences, path separator assumptions, and leftover repository state.
- Check IntelliJ Platform tests for fixture lifecycle, write-action boundaries, EDT usage, background task completion, dumb-mode state, indexing assumptions, and missing waits for platform events.
- Check async code for sleeps, uncontrolled timers, race-prone callbacks, missing cancellation checks, and assertions that run before observable state is stable.
- Check CI-only failures for JDK version, Gradle cache state, filesystem case sensitivity, network access, locale, time zone, and parallelism.

## Fix

- Prefer deterministic setup and cleanup over retries.
- Isolate temp directories, repository state, services, clocks, and background work per test.
- Replace sleeps with controlled callbacks, latches, polling with bounded timeout helpers, or IntelliJ Platform test utilities.
- Add regression coverage that proves the fixed invariant when the root cause is a product or test-helper bug.
- Keep any retry or quarantine as a temporary, documented mitigation with a linked follow-up task.

## Output

- Report the first failure, reproduction commands, pass/fail pattern, suspected root cause, files changed, and validation commands.
- If unresolved, leave the exact next diagnostic step and the reason broad validation remains unreliable.
