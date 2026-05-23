# Archive Completed Work

Move completed task entries from `TASKS.md` to `TASKS_ARCHIVE.md`, or report whether named work is ready to archive.

Use this prompt when implementation or documentation work appears finished and the remaining action is task-state cleanup, archive readiness review, or a mechanical task archive edit.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `.agents/references/execution.md`
- `.agents/references/testing.md`
- `.agents/prompts/README.md`
- this prompt
- `TASKS.md`
- `TASKS_ARCHIVE.md`
- the named task refs, current diff, validation output, closeout note, plan result summary, or completion evidence supplied by the user

Load related plans, ADRs, proposals, source files, specs, support docs, changelog, validation reports, or `docs/decisions/OPEN_QUESTIONS.md` only when the task entry or supplied evidence references them.

Use `change-closeout.md` first when the user needs a broader handoff or commit-readiness check before archiving.
Use `backlog-triage.md` instead when the request is mostly stale, duplicate, blocked, or misplaced backlog analysis.

## Archive Rules

- Archive only when the requested work is finished, task-appropriate validation has passed or has an explicit skipped-check reason, and self-review has checked behavior, compatibility, documentation, and validation gaps.
- Preserve stable `T-AREA-NNN` refs. Do not renumber tasks.
- Move only clearly completed, superseded, rejected, or no-longer-needed items. Do not infer product decisions or close unresolved questions.
- Keep active umbrella tasks open when their child tasks or stated completion criteria remain open.
- Preserve existing task wording, grouping, linked artifacts, and validation evidence unless a small clarity fix is needed.
- In `TASKS_ARCHIVE.md`, follow the surrounding archive style, use `[x]`, and add a concise `Archived as of <date> <reason>.` note only when starting a new archive group.
- Do not duplicate Markdown headings within `TASKS.md` or `TASKS_ARCHIVE.md`; merge moved entries under an existing matching heading when one exists.
- After a task archive edit, run documentation validation and `git diff --check` before handoff.

## Output

If the user asks for report-only output, return an archive-readiness note with:

- task refs reviewed
- ready-to-archive items and evidence
- blocked or not-ready items and missing evidence
- owner artifacts that would change if edits are requested
- validation needed after an archive edit

If the user asks to edit, make the mechanical task-state changes and summarize:

- tasks moved, retained, or left unchanged
- archive headings or notes added
- evidence preserved or added
- validation commands run and results
- skipped checks and remaining risk, if any

## Non-Goals

- Do not implement unfinished work.
- Do not mark tasks complete without completion, validation, and self-review evidence.
- Do not change task scope, priorities, product behavior, ADR status, plan status, or open-question status unless the user explicitly asks and repository rules allow it.
- Do not create commits unless the user asks for a commit or an approved plan requires one.
