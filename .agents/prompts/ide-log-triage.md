# IDE Log Triage

Analyze IntelliJ IDE logs for plugin-related errors, warnings, or runtime symptoms after the user grants permission or provides sanitized excerpts.

## Read First

- `AGENTS.md`
- `.agents/references/testing.md`
- `.agents/references/troubleshooting.md`
- `.agents/prompts/README.md`
- this prompt
- the user's symptom description, timestamp range, log folder permission, or sanitized log excerpt

Load plugin source, `plugin.xml`, Gradle files, or recent diffs only after identifying log lines that plausibly relate to this plugin or its dependencies.

## Output

Report:

- permission status and log source used
- IDE product, build, timestamp range, and log file names when available
- relevant errors, warnings, stack traces, plugin IDs, action IDs, notification IDs, or VCS/AI Assistant messages
- whether each finding is likely plugin-owned, platform-owned, dependency-owned, environment-owned, or unrelated
- next diagnostic step, reproduction command, or source area to inspect
- sanitized excerpts only, limited to the lines needed for evidence

When rotating logs for a future session, preserve existing logs unless the user explicitly asks to remove them.

## Non-Goals

- Do not inspect local IDE logs without explicit user permission for the relevant path.
- Do not include secrets, tokens, proprietary source paths beyond what is necessary, private commit messages, or large unrelated log blocks.
- Do not infer root cause from generic warnings without timestamp or symptom correlation.
- Do not modify plugin code from this prompt unless the user separately asks for a fix.
