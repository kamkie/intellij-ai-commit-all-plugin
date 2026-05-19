# Repository State Snapshot

Summarize the current repository state across worktree changes, tasks, open questions, ADRs, proposals, plans, and prompt or skill guidance.
Use this when the user asks what state the repository is in, what task should be next, or whether governance artifacts are clean.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `.agents/references/execution.md`
- `.agents/references/planning.md`
- `TASKS.md`
- `TASKS_ARCHIVE.md`
- `docs/decisions/OPEN_QUESTIONS.md`
- `docs/decisions/README.md`
- `docs/proposals/README.md`
- `.agents/prompts/README.md`
- this prompt

Load specific ADRs, proposals, plans, task archives, source files, or validation logs only when an artifact status depends on them.

## Output

Produce a repository-state report with:

- permission boundary and whether the report includes uncommitted changes
- current branch, worktree status, and notable untracked or modified files
- active tasks, recently archived tasks, stale or blocked tasks, and task IDs needing attention
- open questions and whether active tasks are blocked by them
- ADR, proposal, and plan status, including missing index or archive work
- prompt, skill, or guidance status only when relevant to the current request
- recommended next task or top 1-3 options with the evidence for each
- suggested owning artifact edits and validation commands, without applying them unless asked

## Non-Goals

- Do not implement tasks or archive work unless the user explicitly asks for edits.
- Do not close open questions without accepted evidence or explicit user input.
- Do not infer ADR or proposal acceptance from discussion alone.
- Do not run heavy builds or tests for a status snapshot unless the user asks for validation.
