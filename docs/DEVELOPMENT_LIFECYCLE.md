# Development Lifecycle

Use this lifecycle for changes that are larger than a direct documentation edit. Direct one-off work can use the same gates with a shorter loop. Delegation is optional when the environment supports it; current no-delegation instructions and tool limits take precedence.

## 1. Frame The Work

- Identify the user-facing behavior, repository artifact, or workflow being changed.
- Check `docs/decisions/OPEN_QUESTIONS.md` for decisions that block the work.
- Use `TASKS.md` for backlog scope, not as proof that a decision has been made.
- Follow `docs/decisions/README.md` for ADR requirements, project decisions, and repository rule changes.

## 2. Plan When Needed

Create a plan in `.agents/plans/` when the work introduces new intended behavior, spans multiple areas, affects risky VCS, commit, push, AI generation, release, or compatibility behavior, depends on unresolved technical choices, or needs explicit task coordination.

When the same request clearly requires both an ADR and a later plan, draft the proposed ADR and companion draft plan together, then stop. The companion plan stays `Status: Draft` and blocked until ADR acceptance and later explicit plan approval.

After creating or updating a required plan, stop for user review. Implementation may start only after explicit user approval, `Status: Approved`, `Approved by:`, `Approved at:`, and a matching status-history entry are recorded.

Use `docs/proposals/` before planning when the task is to collect findings, duplications, simplifications, or improvement options for maintainer triage without immediate implementation.

Small docs-only changes do not need a plan. Direct one-off work does not need a plan when it stays narrow, the intended outcome is already decided by an accepted ADR, specification, owner document, or exact task ref, and no ADR, missing decision, risky workflow, or multi-step coordination gate is triggered. Delegating a one-off task does not by itself require a plan.

Use `.agents/references/planning.md` for required plan shape, readiness, approval, status history, worker metadata, execution graph, and task-packet shape. Use `.agents/references/orchestration.md` for delegation and worker coordination rules. Use `.agents/references/execution.md` for per-task implementation and commit rules.

The later release workflow takes over after implementation tasks and owns whole-release review, broader manual checks and tests, documentation update passes, and release artifact preparation.

## 3. Implement

- Keep the change scoped to the requested behavior.
- Prefer existing IntelliJ Platform, Gradle, and Kotlin conventions.
- Update docs before or alongside behavior changes.
- For direct one-off work, use the smallest owner context that can safely complete the request after ADR and plan gates are cleared. If the current thread is already large or the request is likely to trigger compaction, use a fresh delegated worker or read-only sidecar before loading broad context.
- For delegated one-off work, the main agent remains responsible for the final diff, validation evidence, review risks, and handoff. Write workers need explicit disjoint write scopes and compact briefs; review sidecars are read-only by default and may return compact summaries.
- For approved plan execution, implement only after explicit approval is recorded. Fresh task workers should follow the packet context budget, validation expectations, escalation triggers, and write scope.
- Publishing, signing, Marketplace metadata, and CI are in scope per ADR 0019; avoid unrelated operations work outside that scope.
- If a new question, missing decision, or unsafe assumption appears during planned implementation, follow the stop-and-update rules in `.agents/references/planning.md`.

## 4. Validate

Choose validation from `.agents/references/testing.md`.

For documentation-only changes, verify content, links, paths, and consistency with the repository's current state.

For code changes, prefer targeted build and sandbox checks before broader compatibility checks.

## 5. Review

Use `.agents/references/reviews.md` before handing off.

Focus on:

- Unintended commit or push behavior.
- AI Assistant integration failure paths.
- IntelliJ API compatibility.
- Missing validation evidence.
- Documentation that implies unsupported or unimplemented behavior.
- Delegated work that lacks final orchestrator review, has unclear write scopes, ignores a no-delegation instruction, or exceeds the available environment and tool limits.

Review-only sidecar delegation may be requested when the environment supports it. Keep that sidecar read-only, give it the files or diff to inspect, and reconcile its findings before handoff.

## 6. Handoff

Report:

- Files changed.
- Validation run.
- Validation not run and why.
- Remaining open questions or risks.
- For delegated work, compact worker results, blockers, review risks, and handoff notes.

## 7. Release Preparation

Release preparation starts after implementation work is complete and integrated on `main`, or when the user explicitly requests release work.

Use `.agents/references/releases.md` for release sequencing. The release orchestrator owns `CHANGELOG.md` updates, checks whether `SUPPORT.md` still matches the supported scope, runs the broader release validation, and prepares release artifacts.
