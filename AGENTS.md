# AGENTS.md

This is the AI entry point for the repository. Keep task context small and read the most specific governing artifact before editing.

## Scope

- Keep project plans, backlog details, product intent, and feature descriptions out of this file.
- Keep user-facing behavior in `README.md`.
- Keep active implementation tasks and planning in `TASKS.md` or accepted task-specific plans.
- Keep completed task history in `TASKS_ARCHIVE.md`.
- Keep durable agent workflow guidance in `.agents/references/`.
- Keep reusable task-specific agent skills in `.agents/skills/`.
- Keep reusable prompt recipes in `.agents/prompts/`.

## Guidance Map

- Development lifecycle: `docs/DEVELOPMENT_LIFECYCLE.md`
- Behavior specification: `docs/specification.md`
- Implementation backlog: `TASKS.md`
- Completed task archive: `TASKS_ARCHIVE.md`
- Missing user input: `docs/decisions/OPEN_QUESTIONS.md`
- Implementation execution loop: `.agents/references/execution.md`
- Planning: `.agents/references/planning.md`
- Orchestration and delegation: `.agents/references/orchestration.md`
- Code style: `.agents/references/code-style.md`
- Validation: `.agents/references/testing.md`
- Review priorities: `.agents/references/reviews.md`
- Release preparation: `.agents/references/releases.md`
- Documentation ownership: `.agents/references/documentation.md`
- Troubleshooting: `.agents/references/troubleshooting.md`
- Task-specific skills: `.agents/skills/`
- Repository prompts: `.agents/prompts/`
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
- Treat named repository prompt references as prompt recipes and search `.agents/prompts/README.md` first, then load only the matching prompt file from `.agents/prompts/`.
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
- Before broad exploration or edits, check thread size and compaction risk. When the active environment and tool contract support delegation, and no higher-priority instruction or current no-delegation request forbids it, use delegated workers or read-only sidecars to avoid context compaction; otherwise keep context narrow and warn if compaction risk is high.
- Identify the behavior and governing artifact before editing.
- Never bulk-load AI guidance by default. Start from this file, then read only the mapped owner docs needed for the current task; broaden only for explicit broad audits, cross-document consistency checks, or validation failures that require it.
- For reusable operational lessons, follow `.agents/references/execution.md` Learning Capture before adding persistent guidance.
- Do not read `docs/WORKING_WITH_AI.md` during normal agent workflow; it owns human-facing guidance for asking AI to work here.
- Follow `docs/decisions/README.md` for ADR requirements, project decisions, and repository rule changes.
- When a requested change requires creating an ADR, create the ADR and stop. If the request clearly also requires a later implementation plan, create the proposed ADR and companion draft plan in the same step, then stop. Continue only after the user reviews and explicitly accepts the ADR; implementation from the plan still requires explicit plan approval.
- When work needs an implementation plan, create or update the plan first and stop. Start implementation only after the user reviews and explicitly approves the plan.
- For plugin behavior changes, use `docs/specification.md` as the requirements owner; add or update `REQ-` rows and traceability before or alongside implementation.
- Update specs or docs before or alongside behavior changes when behavior changes.
- Run validation that matches the diff and risk.
- Review for bugs, missing validation, and API or IDE compatibility risk before handing off.
- Before handoff, confirm the requested change is complete; specs, docs, tasks, plans, changelog, and support updates are done or explicitly not applicable; validation evidence is current; skipped checks have reasons; and self-review is done.
- Commit completed work only when the user asks for commits or the task scope explicitly requires it; when committing, follow `.gitmessage` and `.agents/references/execution.md`.
