# Development Lifecycle

Use this lifecycle for changes that are larger than a direct documentation edit.

## 1. Frame The Work

- Identify the user-facing behavior, repository artifact, or workflow being changed.
- Check `docs/decisions/OPEN_QUESTIONS.md` for decisions that block the work.
- Use `TASKS.md` for backlog scope, not as proof that a decision has been made.
- Follow `docs/decisions/README.md` for project decisions and repository rule changes.

## 2. Plan When Needed

Create a plan in `.agents/plans/` when the work spans multiple files, changes behavior, or depends on unresolved technical choices.

Use `docs/proposals/` before planning when the task is to collect findings, duplications, simplifications, or improvement options for maintainer triage without immediate implementation.

A plan should state:

- Goal and non-goals.
- Open questions or assumptions.
- Files likely to change.
- Validation expected.
- Rollback or failure considerations when relevant.

Small docs-only changes do not need a plan.

Before implementation starts from an approved plan, follow the readiness, per-task execution, and delegation rules in `.agents/references/planning.md` and `.agents/references/execution.md`.

The later release workflow takes over after implementation tasks and owns whole-release review, broader manual checks and tests, documentation update passes, and release artifact preparation.

## 3. Implement

- Keep the change scoped to the requested behavior.
- Prefer existing IntelliJ Platform, Gradle, and Kotlin conventions once the project is scaffolded.
- Update docs before or alongside behavior changes.
- Publishing, signing, Marketplace metadata, and CI are in scope per ADR 0019; avoid unrelated operations work outside that scope.
- If a new question, missing decision, or unsafe assumption appears during planned implementation, follow the stop-and-update rules in `.agents/references/planning.md`.

## 4. Validate

Choose validation from `.agents/references/testing.md`.

For documentation-only changes, verify content, links, paths, and consistency with the repository's current state.

For code changes after scaffolding exists, prefer targeted build and sandbox checks before broader compatibility checks.

## 5. Review

Use `.agents/references/reviews.md` before handing off.

Focus on:

- Unintended commit or push behavior.
- AI Assistant integration failure paths.
- IntelliJ API compatibility.
- Missing validation evidence.
- Documentation that implies unsupported or unimplemented behavior.

## 6. Handoff

Report:

- Files changed.
- Validation run.
- Validation not run and why.
- Remaining open questions or risks.

## 7. Release Preparation

Release preparation starts after implementation work is complete and integrated on `main`, or when the user explicitly requests release work.

Use `.agents/references/releases.md` for release sequencing. The release orchestrator owns `CHANGELOG.md` updates, checks whether `SUPPORT.md` still matches the supported scope, runs the broader release validation, and prepares release artifacts.
