# Development Lifecycle

Use this lifecycle for changes that are larger than a direct documentation edit.

## 1. Frame The Work

- Identify the user-facing behavior, repository artifact, or workflow being changed.
- Check `OPEN_QUESTIONS.md` for decisions that block the work.
- Use `TASKS.md` for backlog scope, not as proof that a decision has been made.
- Record project decisions and repository rule changes in `docs/decisions/` before or alongside the affected implementation or rule edit.

## 2. Plan When Needed

Create a plan in `.agents/plans/` when the work spans multiple files, changes behavior, or depends on unresolved technical choices.

A plan should state:

- Goal and non-goals.
- Open questions or assumptions.
- Files likely to change.
- Validation expected.
- Rollback or failure considerations when relevant.

Small docs-only changes do not need a plan.

## 3. Implement

- Keep the change scoped to the requested behavior.
- Prefer existing IntelliJ Platform, Gradle, and Kotlin conventions once the project is scaffolded.
- Update docs before or alongside behavior changes.
- Avoid adding release, publishing, signing, marketplace, or CI work unless requested.

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
