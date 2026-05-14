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
- Does it fail closed when AI Assistant is unavailable or times out?
- Does it avoid compile-time dependencies on non-public AI Assistant APIs unless explicitly approved?
- Does it behave predictably with Git staging area enabled and disabled?
- Is the target IDE version documented or otherwise accounted for?

## Review Output

For review requests, use this order:

1. Findings.
2. Open questions or assumptions.
3. Change summary, if useful.
4. Validation gaps or residual risk.
