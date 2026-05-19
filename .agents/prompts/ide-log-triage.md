# IDE Log Triage

Analyze IntelliJ IDE logs for plugin-related errors, warnings, or runtime symptoms.
Treat a direct user request for this prompt, by title or filename, as explicit permission to inspect the IntelliJ log folder named by the user.
When the user invokes this prompt without naming a folder, use the common IntelliJ IDEA 2026.1 log folder for this machine: `C:\Users\kamki\AppData\Local\JetBrains\IntelliJIdea2026.1\log`.

## Read First

- `AGENTS.md`
- `.agents/references/testing.md`
- `.agents/references/troubleshooting.md`
- `.agents/prompts/README.md`
- this prompt
- the user's symptom description, timestamp range, named log folder, default log folder, or sanitized log excerpt

Load plugin source, `plugin.xml`, Gradle files, or recent diffs only after identifying log lines that plausibly relate to this plugin or its dependencies.

## Output

Report:

- permission status, including whether permission came from direct prompt invocation, and log source used
- IDE product, build, timestamp range, and log file names when available
- relevant errors, warnings, stack traces, plugin IDs, action IDs, notification IDs, or VCS/AI Assistant messages
- whether each finding is likely plugin-owned, platform-owned, dependency-owned, environment-owned, or unrelated
- next diagnostic step, reproduction command, or source area to inspect
- sanitized excerpts only, limited to the lines needed for evidence

When rotating logs for a future session, preserve existing logs unless the user explicitly asks to remove them.

## Non-Goals

- Do not inspect local IDE logs outside the named or default prompt-authorized folder unless the user explicitly grants that additional path.
- Do not include secrets, tokens, proprietary source paths beyond what is necessary, private commit messages, or large unrelated log blocks.
- Do not infer root cause from generic warnings without timestamp or symptom correlation.
- Do not modify plugin code from this prompt unless the user separately asks for a fix.
