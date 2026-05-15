# AGENTS.md

This is the AI entry point for the repository. Keep task context small and read the most specific governing artifact before editing.

## Scope

- Keep project plans, backlog details, product intent, and feature descriptions out of this file.
- Keep user-facing behavior in `README.md`.
- Keep implementation tasks and planning in `TASKS.md` or accepted task-specific plans.
- Keep durable agent workflow guidance in `.agents/references/`.

## Guidance Map

- Human guide for asking AI to work here: `docs/WORKING_WITH_AI.md`
- Development lifecycle: `docs/DEVELOPMENT_LIFECYCLE.md`
- Implementation backlog: `TASKS.md`
- Missing user input: `docs/decisions/OPEN_QUESTIONS.md`
- Execution loop: `.agents/references/execution.md`
- Planning: `.agents/references/planning.md`
- Code style: `.agents/references/code-style.md`
- Validation: `.agents/references/testing.md`
- Review priorities: `.agents/references/reviews.md`
- Release preparation: `.agents/references/releases.md`
- Documentation ownership: `.agents/references/documentation.md`
- Commit message template: `.gitmessage`
- Changelog: `CHANGELOG.md`
- Support policy: `SUPPORT.md`
- Proposals: `docs/proposals/`
- Decision records: `docs/decisions/`

## Priority Order

When instructions overlap, apply this project-specific order:

1. Current user request.
2. The most specific accepted task plan or governing document.
3. Platform, framework, and API constraints.
4. `README.md` and user-facing documentation.
5. General AI guidance and repository workflow rules.

## Working Rules

- Use the smallest task-shaped context that can safely answer the request.
- Identify the behavior and governing artifact before editing.
- Do not load every AI instruction file automatically; start from this file and then read only the specific mapped guidance needed for the current task.
- Follow `docs/decisions/README.md` for project decisions and repository rule changes.
- Update specs or docs before or alongside behavior changes when behavior changes.
- Run validation that matches the diff and risk.
- Review for bugs, missing validation, and API or IDE compatibility risk before handing off.
- Commit completed work only when the user asks for commits or the task scope explicitly requires it; when committing, follow `.gitmessage` and `.agents/references/execution.md`.
