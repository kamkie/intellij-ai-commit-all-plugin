# AGENTS.md

This is the AI entry point for the repository. Keep task context small and read the most specific governing artifact before editing.

## Scope

- Keep project plans, backlog details, product intent, and feature descriptions out of this file.
- Keep user-facing behavior in `README.md`.
- Keep active implementation tasks and planning in `TASKS.md` or accepted task-specific plans.
- Keep completed task history in `TASKS_ARCHIVE.md`.
- Keep durable agent workflow guidance in `.agents/references/`.
- Keep reusable task-specific agent skills in `.agents/skills/`.

## Guidance Map

- Human guide for asking AI to work here: `docs/WORKING_WITH_AI.md`
- Development lifecycle: `docs/DEVELOPMENT_LIFECYCLE.md`
- Implementation backlog: `TASKS.md`
- Completed task archive: `TASKS_ARCHIVE.md`
- Missing user input: `docs/decisions/OPEN_QUESTIONS.md`
- Execution loop: `.agents/references/execution.md`
- Planning: `.agents/references/planning.md`
- Code style: `.agents/references/code-style.md`
- Validation: `.agents/references/testing.md`
- Review priorities: `.agents/references/reviews.md`
- Release preparation: `.agents/references/releases.md`
- Documentation ownership: `.agents/references/documentation.md`
- Task-specific skills: `.agents/skills/`
- Commit message template: `.gitmessage`
- Changelog: `CHANGELOG.md`
- Support policy: `SUPPORT.md`
- Proposals: `docs/proposals/`
- Decision records: `docs/decisions/`

## Artifact Lookup

- Treat `adr-NNNN-<slug>.md` or `adr-NNNN` references as decision records and search `docs/decisions/` first.
- Treat `PLAN-<short-kebab-slug>.md` or `PLAN-<short-kebab-slug>` references as plans and search `.agents/plans/` first, then `.agents/plans/archive/` when the active file is not found.
- Treat `PROP-<short-kebab-slug>-<YYYY-MM-DD>T<HH-MM>.md` or `PROP-<short-kebab-slug>` references as proposals and search `docs/proposals/` first, then `docs/proposals/archive/` when the active file is not found.
- Treat `T-<AREA>-NNN` references as task shortcuts and search `TASKS.md` first, then `TASKS_ARCHIVE.md`, for the exact task entry; use its surrounding section and linked artifacts before falling back to broader search.
- Prefer exact filename lookup when a full filename is supplied. If only an ID or prefix is supplied, use a scoped search in the owning directory before falling back to a repository-wide search.

## Priority Order

When instructions overlap, apply this project-specific order:

1. Current user request.
2. The most specific approved task plan or governing document.
3. Platform, framework, and API constraints.
4. `README.md` and user-facing documentation.
5. General AI guidance and repository workflow rules.

## Working Rules

- Use the smallest task-shaped context that can safely answer the request.
- Identify the behavior and governing artifact before editing.
- Do not load every AI instruction file automatically; start from this file and then read only the specific mapped guidance needed for the current task.
- Follow `docs/decisions/README.md` for project decisions and repository rule changes.
- When a requested change requires creating an ADR, create the ADR first and stop. Continue only after the user reviews and explicitly accepts it.
- When work needs an implementation plan, create or update the plan first and stop. Start implementation only after the user reviews and explicitly approves the plan.
- Update specs or docs before or alongside behavior changes when behavior changes.
- Run validation that matches the diff and risk.
- Review for bugs, missing validation, and API or IDE compatibility risk before handing off.
- Commit completed work only when the user asks for commits or the task scope explicitly requires it; when committing, follow `.gitmessage` and `.agents/references/execution.md`.
