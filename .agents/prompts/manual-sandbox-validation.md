# Manual Sandbox Validation

Prepare and report manual IntelliJ sandbox validation for plugin behavior that cannot be proven well enough by automated tests alone.

## Read First

- `AGENTS.md`
- `.agents/references/testing.md`
- `.agents/references/troubleshooting.md`
- `.agents/prompts/README.md`
- this prompt
- the changed files, plan, task, bug report, or scenario that triggered manual validation

Load `.agents/references/releases.md` only when release readiness, signing, Marketplace packaging, or public changelog impact is in scope.

## Output

Produce a compact validation note in the current response unless the user asks for a tracked artifact.
Include:

- exact IDE product name and build number, or state that they still need to be collected
- sandbox command, usually `.\gradlew.bat runIde`
- plugin build or install path used
- manual scenario table with preconditions, action, expected result, actual result, status, and evidence
- logs inspected, if any, with user permission status and sanitized relevant lines only
- automated checks run before or after the sandbox pass
- failures, follow-up diagnostics, and residual manual coverage risk

Prefer the existing manual scenario list in `.agents/references/testing.md` for commit, push, changelist, staging, AI Assistant availability, shortcut, and error-surfacing flows.

## Non-Goals

- Do not replace automated regression tests with manual validation when practical automation exists.
- Do not inspect local IDE logs unless the user has explicitly approved the relevant log folder or provided sanitized excerpts.
- Do not run Marketplace signing, publishing, or real remote pushes from this prompt.
- Do not update `CHANGELOG.md` unless a separate release or public behavior request requires it.
