# CI Failure Triage

Analyze a GitHub Actions, local CI, Gradle, lint, docs validation, or packaging failure and identify the narrowest useful fix path.

## Read First

- `AGENTS.md`
- `.agents/references/testing.md`
- `.agents/references/troubleshooting.md`
- `.agents/prompts/README.md`
- this prompt
- the failing command output, CI log excerpt, workflow run link, job name, or failing check supplied by the user

Load workflow files, Gradle files, docs validation scripts, source files, tests, or plugin metadata only after identifying the failing check class.
Use official or primary-source documentation only when current tool behavior, version compatibility, or API semantics need verification.

## Output

Return a triage note with:

- failing check, command, job, or task name
- first meaningful error and why later errors are likely secondary or independent
- failure class: docs validation, markdownlint, agent artifact validation, formatting, compilation, test, plugin packaging, plugin verification, dependency resolution, signing, environment, or unknown
- narrow local reproduction command
- likely owning files
- smallest safe fix path
- validation sequence after the fix
- remaining risk if the failure cannot be reproduced locally

If the user asks for implementation and the cause is clear, fix the issue and run the matching validation.

## Non-Goals

- Do not rewrite CI pipelines broadly while triaging a single failure.
- Do not update dependencies or tool versions unless the failure requires it and the repository governance path allows it.
- Do not ignore, quarantine, or weaken tests to make CI pass.
- Do not require signing, Marketplace secrets, or real remote pushes for pull-request validation.
