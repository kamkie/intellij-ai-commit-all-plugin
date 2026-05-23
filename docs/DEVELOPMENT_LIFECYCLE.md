# Development Lifecycle

Use this shared lifecycle for larger repository changes. Small direct requests can use the same gates with a shorter loop.

`docs/WORKING_WITH_AI.md` gives humans request examples. AI execution mechanics live in `.agents/references/execution.md`, `.agents/references/planning.md`, `.agents/references/orchestration.md`, `.agents/references/testing.md`, and `.agents/references/releases.md`.

## Core Rules

- Choose the path early: direct one-off, bug triage, design, proposal, ADR, plan, approved-plan execution, release, or closeout.
- Use direct one-off work when the change is narrow, the intended behavior is already decided, and no gate is triggered.
- Stop for an ADR when work chooses or changes durable project direction, repository rules, compatibility policy, validation expectations, user-facing behavior, or maintenance policy.
- Stop for a plan when work needs sequencing, task packets, disjoint write scopes, broader validation coordination, risky VCS/commit/push/AI/release/compatibility changes, or unresolved choices.
- Use proposals for findings, duplication analysis, simplification options, and maintainer triage before choosing a direction.
- Match validation to the risk and report skipped checks with a concrete reason.
- Commit only when the user asks or an approved plan requires it.

## 1. Intake

Clarify what kind of work is being requested before loading broad context.

Identify:

- The outcome or behavior being changed.
- Stable refs, such as `T-<AREA>-NNN`, `adr-NNNN`, `PLAN-<slug>`, `PROP-<slug>`, a prompt filename, or a concrete file path.
- The intended boundary: analysis, design, proposal, ADR, plan, implementation, validation, review, commit, or release.
- Constraints that affect execution, such as direct one-off no-delegation, read-only review, no commits, no network, specific validation, manual sandbox scope, exact write scope, or unavailable sub-agents for approved-plan execution.

For bug reports, first classify observed behavior, expected behavior, triggering action, affected workflow, missing information, and likely owner area. Use `bug-report-triage.md` when a report needs classification before a fix.

For screenshots, concept images, or UI drafts, record what state is shown and what feedback is wanted before turning the result into an ADR, plan, or implementation request.

## 2. Orient

Use orientation when the next safe action is unclear, the worktree may be dirty, or active artifacts may affect the work.

Check only what is needed:

- Worktree status and existing user edits.
- Active task, ADR, proposal, plan, or open-question refs named by the request.
- Owner docs for the artifact or behavior being changed.
- Relevant prompt or skill only when the request names it or the stage calls for it.

Use `repository-state-snapshot.md` for a broad AI-assisted status report. Do not use orientation as a reason to load every guidance file.

## 3. Explore Before Deciding

Use the lightest pre-implementation artifact that fits the uncertainty.

Use design when visual direction, UI state coverage, icons, graphics, or interaction variants need review before production implementation. `design-draft-session.md` owns bounded design-only sessions.

Use proposals when the right answer is not yet selected and the maintainer needs findings, options, duplicate analysis, simplification opportunities, or tradeoffs. Keep proposal findings advisory until accepted through an ADR, plan, task, or direct request.

Use backlog triage when `TASKS.md`, `TASKS_ARCHIVE.md`, or open questions need stale, blocked, duplicate, or misplaced work reviewed.

Use compatibility, CI, IDE-log, manual-sandbox, or toolchain prompts when the question is narrow enough for those prompt recipes and not substantial enough to become a skill or plan.

## 4. Decide

Record durable decisions before implementation changes governed behavior.

Follow `docs/decisions/README.md` when a change affects project direction, repository rules, compatibility, validation expectations, user-facing behavior, or maintenance policy.

When an ADR is required:

- Draft the ADR and stop.
- If the same request clearly requires a later implementation plan, draft the proposed ADR and companion draft plan together, then stop.
- Keep companion plans `Status: Draft` until the ADR is accepted and the plan is separately approved.
- Continue only after the user explicitly accepts the ADR.

Use `adr-impact-check.md` when it is unclear whether a request needs an ADR, plan, task update, open question, or documentation update.

## 5. Plan

Plan before implementation when the work is too broad or risky for direct one-off execution.

Create or update a plan in `.agents/plans/` when work:

- Introduces new intended plugin behavior or changes behavior not already decided.
- Touches multiple areas, such as Gradle, plugin metadata, Kotlin code, tests, and docs.
- Affects commit selection, AI message generation, commit execution, push behavior, release, or compatibility.
- Depends on unresolved user input or technical choices.
- Needs sequencing, task packets, disjoint write scopes, worker coordination, or broader validation.

A plan must remain unimplemented until explicit user approval is recorded. Before execution starts, confirm the readiness fields required by planning guidance, including approval, ADR prerequisites, worker metadata, task packets, and execution graph.

Small documentation cleanup and narrow implementation of already-decided behavior can stay on the direct one-off path when no gate is triggered.

## 6. Implement

Implement only after ADR and plan gates are clear.

- Use the owner artifact and smallest relevant read set.
- Keep the change scoped to the requested behavior.
- Update specs, README, support docs, tasks, or AI guidance before or alongside behavior changes when those artifacts are affected.
- Escalate to ADR, plan, open question, or proposal when a new decision, missing input, or unsafe assumption appears.
- Treat each task packet as the task boundary.
- Follow packet-approved context, write scope, dependencies, validation, escalation triggers, and stop conditions.
- Commit each completed plan task, or each task in the current approved wave, before starting the next dependent task or wave when commits are allowed and required by the approved plan.

## 7. Validate

Choose validation based on the diff and risk.

Common validation levels:

- Documentation-only changes: docs validation, agent-artifact validation when `.agents/` artifacts changed, link/path/ref checks, and `git diff --check`.
- Kotlin or Gradle changes: formatting, focused tests, and relevant build tasks.
- Plugin behavior changes: focused tests plus packaging, descriptor, compatibility, or sandbox checks as risk requires.
- Commit, push, AI Assistant, staging, changelist, or multi-root changes: targeted tests plus manual sandbox coverage when automation is insufficient.
- Release changes: release checklist, changelog, support, package, signing, CI, tag, and Marketplace readiness checks.

## 8. Review And Close Out

Review before handoff, before task completion, and before commit.

Check for:

- Bugs, regressions, compatibility issues, missing tests, and missing validation.
- Commit selection, AI generation, commit execution, push behavior, staging, changelist, and multi-root risks.
- Documentation that implies unsupported or unimplemented behavior.
- Drift between implementation, specs, README, support docs, tasks, plans, ADRs, and workflow guidance.

## 9. Commit

Commit only when the user asks or an approved plan requires it.

Before committing:

- Confirm the diff matches the requested scope.
- Confirm validation and review evidence are current.
- Confirm task, plan, ADR, docs, prompt, skill, changelog, or support updates are complete or explicitly not applicable.
- Preserve unrelated user changes.

Use `.gitmessage` for commit-message rules and metadata trailers.

## 10. Release

Release preparation starts after implementation is complete and integrated, or when the user explicitly requests release work.

Use `docs/validation/release-checklist.md` for release validation sequencing.

The release pass owns:

- Changelog readiness for public plugin-facing changes.
- Support policy alignment.
- Packaging, signing, CI, tags, Marketplace readiness, and compatibility checks.
- Release blockers from tasks, open questions, ADR implementation gaps, proposal implementation gaps, validation failures, or known risks.

Do not publish, sign, tag, or push a release unless the user explicitly asks and required prerequisites are satisfied.
