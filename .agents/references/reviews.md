# Review Guide

Use this guide when reviewing changes in this repository.

## Findings First

Lead with concrete findings ordered by severity. Reference file paths and lines where possible. Keep summaries secondary.

## Plugin Risk Priority

Review in this order:

1. Incorrect commit selection or unintended commit.
2. AI Assistant invocation failures.
3. Committing before AI generation is complete.
4. Push behavior mismatch.
5. IntelliJ API compatibility or internal API risk.
6. Missing sandbox validation.

## Questions To Ask

- Could this commit files the user did not expect?
- Does it preserve the IDE's normal before-commit checks and error handling?
- Does it reuse or forward platform-owned errors instead of replacing them with plugin-specific wording?
- Does it avoid adding plugin-specific confirmation prompts unless a concrete uncovered risk was documented?
- Does it fail closed when AI Assistant is unavailable, not signed in, or times out?
- Does plugin metadata require JetBrains AI Assistant so missing or disabled AI Assistant fails at installation/loading time?
- Does it avoid compile-time dependencies on non-public AI Assistant APIs unless explicitly approved?
- Does it behave predictably with Git staging area enabled and disabled?
- Does it preserve support for changes spread across multiple changelists?
- Do local-repository E2E tests avoid real remotes and destructive repository state?
- Does publishing/signing/CI keep Marketplace tokens, certificates, private keys, and passwords out of the repository?
- Is the target IDE version documented or otherwise accounted for?
- Are material assumptions stated, resolved, or harmless to behavior, write scope, validation, ADR gates, and plan gates?
- Is the implementation the simplest shape that satisfies the user request and accepted governing artifacts?
- Did the change avoid speculative features, single-use abstractions, unnecessary configurability, and generic defensive code that the request did not need?
- Does every changed line trace to the user request, governing artifact, validation fix, or cleanup caused by the current change?
- Were unrelated dead code, formatting churn, style drift, or drive-by refactors left out unless explicitly requested?
- Are success criteria and validation evidence strong enough for the claimed fix, refactor, or documentation rule change?

## Review Output

For review requests, use this order:

1. Findings.
2. Open questions or assumptions.
3. Change summary, if useful.
4. Validation gaps or residual risk.
