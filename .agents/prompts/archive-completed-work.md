# Archive Completed Work

Move completed task entries from `TASKS.md` to `TASKS_ARCHIVE.md`, closed plans from `.agents/plans/` to `.agents/plans/archive/`, or retired proposals from `docs/proposals/` to `docs/proposals/archive/`. When evidence is incomplete, report whether named work is ready to archive.

Use this prompt when implementation, documentation, planning, or proposal work appears finished and the remaining action is archive readiness review or a mechanical archive edit.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `.agents/references/execution.md`
- `.agents/references/testing.md`
- `.agents/prompts/README.md`
- this prompt
- `TASKS.md`
- `TASKS_ARCHIVE.md`
- the named task refs, plan refs, proposal refs, current diff, validation output, closeout note, plan result summary, proposal tracker status, or completion evidence supplied by the user

When archiving plans, also read `.agents/references/planning.md`, `.agents/plans/README.md`, `.agents/plans/archive/README.md`, and the named plan files.

When archiving proposals, also read `docs/proposals/README.md`, `docs/proposals/archive/README.md`, and the named proposal files.

Load related plans, ADRs, proposals, source files, specs, support docs, changelog, validation reports, or `docs/decisions/OPEN_QUESTIONS.md` only when the work entry or supplied evidence references them.

Use `change-closeout.md` first when the user needs a broader handoff or commit-readiness check before archiving.
Use `backlog-triage.md` instead when the request is mostly stale, duplicate, blocked, or misplaced backlog analysis.

## Archive Rules

- Archive only when the requested work is finished, task-appropriate validation has passed or has an explicit skipped-check reason, and self-review has checked behavior, compatibility, documentation, and validation gaps.
- Preserve stable `T-AREA-NNN` refs. Do not renumber tasks.
- Preserve stable `PLAN-<short-kebab-slug>` refs, plan filenames, plan status history, close reasons, and validation history.
- Preserve stable `PROP-<short-kebab-slug>` refs, proposal filenames, proposal front matter, and finding refs.
- Move only clearly completed, superseded, rejected, or no-longer-needed items. Do not infer product decisions or close unresolved questions.
- Keep active umbrella tasks open when their child tasks or stated completion criteria remain open.
- Move plans only after they have `Status: Closed`, include a `Close-Reason`, and no longer need active execution or release-preparation updates. If a plan is only `Implemented`, report the missing closeout unless the current request explicitly asks to close it and the evidence supports that status change.
- Move proposals only after they are implemented, superseded, or otherwise retired, have no non-terminal implementation status rows, and have no untriaged findings in their `Progress Tracker`.
- Preserve existing task wording, grouping, linked artifacts, and validation evidence unless a small clarity fix is needed.
- In `TASKS_ARCHIVE.md`, follow the surrounding archive style, use `[x]`, and add a concise `Archived as of <date> <reason>.` note only when starting a new archive group.
- Do not duplicate Markdown headings within `TASKS.md` or `TASKS_ARCHIVE.md`; merge moved entries under an existing matching heading when one exists.
- For plan archives, move the file to `.agents/plans/archive/`, move its index entry from Active Plans to Archived Plans in `.agents/plans/README.md`, and preserve the filename.
- For proposal archives, move the file to `docs/proposals/archive/`, remove or move its completed-proposal index entry in `docs/proposals/README.md`, add an archive index entry in `docs/proposals/archive/README.md`, and preserve the filename.
- After an archive edit, run documentation validation, agent-artifact validation when `.agents/` artifacts changed, and `git diff --check` before handoff.

## Output

If the user asks for report-only output, return an archive-readiness note with:

- task refs, plan refs, and proposal refs reviewed
- ready-to-archive items and evidence
- blocked or not-ready items and missing evidence
- owner artifacts that would change if edits are requested
- validation needed after an archive edit

If the user asks to edit, make the mechanical task-state changes and summarize:

- tasks, plans, and proposals moved, retained, or left unchanged
- archive headings, notes, or index entries added
- evidence preserved or added
- validation commands run and results
- skipped checks and remaining risk, if any

## Non-Goals

- Do not implement unfinished work.
- Do not mark tasks complete without completion, validation, and self-review evidence.
- Do not close plans or retire proposals without terminal status, validation or explicit skipped-check evidence, and self-review evidence.
- Do not change task scope, priorities, product behavior, ADR status, plan status, or open-question status unless the user explicitly asks and repository rules allow it.
- Do not create commits unless the user asks for a commit or an approved plan requires one.
