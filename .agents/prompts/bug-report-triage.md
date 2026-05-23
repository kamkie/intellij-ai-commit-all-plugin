# Bug Report Triage

Classify a reported plugin problem, identify the likely owner area, and choose the next safe work path before implementation.

## Read First

- `AGENTS.md`
- `.agents/references/testing.md`
- `.agents/references/troubleshooting.md`
- `.agents/prompts/README.md`
- this prompt
- the bug report, screenshots, log paths, validation output, task ref, or affected file paths supplied by the user

Load source files, tests, plugin metadata, IDE logs, ADRs, plans, tasks, or support docs only when the report points to them or the likely owner area depends on them.
Use `ide-log-triage.md` when the next step is mostly log analysis.
Use `manual-sandbox-validation.md` when the next step is reproducing behavior in an IntelliJ sandbox.

## Output

Return a triage note with:

- observed behavior and expected behavior
- triggering action, IDE or plugin context, and affected workflow when known
- likely owner area: UI, commit selection, AI generation, commit execution, push behavior, settings, Gradle/build, compatibility, docs, or unknown
- repro status: reproduced, likely reproducible, insufficient information, or cannot reproduce locally
- missing information that would change the diagnosis
- whether the next step is direct one-off work, ADR impact check, plan, manual validation, log triage, or no repo change
- smallest useful investigation or fix path
- validation needed after the fix
- privacy or log-sanitization concerns, if any

If the user asks for implementation and the report is sufficiently scoped, proceed only when ADR and plan gates are satisfied.

## Non-Goals

- Do not infer product decisions or support promises from one bug report.
- Do not inspect local IDE logs unless the user has explicitly approved the relevant log folder or provided sanitized excerpts.
- Do not broaden a narrow bug report into unrelated cleanup.
- Do not create tasks, ADRs, or plans unless repository rules or the user request require them.
