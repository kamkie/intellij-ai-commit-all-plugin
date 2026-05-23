# Execution Guide

Use this guide for implementation work.

This file owns the direct one-off execution loop, the approved-plan task loop, and AI-facing commit-message rules. Use `.agents/references/orchestration.md` for delegated worker responsibilities, worker lanes, task packet dispatch, structured worker events, parallel synchronization, and result summaries.

The repository root `.gitmessage` is the authoritative commit-message template and example source.

## Choose The Path

Use the direct one-off loop for ad hoc user requests, `TASKS.md` items, narrow documentation edits, and small implementation tasks that do not require a plan. A narrow implementation of already-decided behavior may stay on the direct one-off path when an accepted ADR, specification, owner document, or exact task ref already defines the intended outcome and no missing decision or coordination gate is triggered.

Use the approved-plan task loop when working from an approved plan or a post-approval plan status. Create or update a plan before implementation when the work introduces new intended behavior, spans multiple areas, affects risky VCS, commit, push, AI generation, release, or compatibility behavior, depends on unresolved input, or needs explicit coordination across tasks or write scopes.

Use the proposal path when the request asks for findings, duplication analysis, simplification options, or maintainer triage before implementation.

If a request requires a new ADR or implementation plan, create or update the required artifact first and stop for the required acceptance or approval before implementation starts. If the request clearly requires both an ADR and a later implementation plan, create the proposed ADR and companion draft plan in the same step, then stop.

## Routing Matrix

| Path | Use When | Stop Or Escalate When |
|------|----------|-----------------------|
| Direct one-off | The request is narrow, the intended behavior is already decided by an accepted ADR, spec, owner doc, or task ref, and the likely write set is small. | A new decision, plan, unresolved input, broad coordination, or high-risk workflow surface appears. |
| Approved plan | The work starts from an approved plan or needs sequencing, task packets, disjoint write scopes, cross-area implementation, or broader validation. | The plan is not approved, approval metadata is missing, a required ADR is not accepted, or a new blocker appears. |
| Proposal | The user wants analysis, options, duplicated-rule findings, simplification ideas, or triage before implementation. | The user accepts a finding for implementation and the change needs an ADR, plan, or task. |

## Direct One-Off Loop

1. Frame the behavior: name the user-facing behavior, command, action, or workflow being changed.
2. Identify the owner artifact: find the source, descriptor, docs, task list, or reference guide that governs the behavior.
3. Check gates: follow ADR and plan requirements before editing governed implementation, workflow guidance, backlog, validation rules, or related behavior.
4. Load the smallest useful context: use `AGENTS.md` and the guidance map to choose only the needed owner documents.
5. Check delegation triggers before heavy context loading or edits: use `.agents/references/orchestration.md` when the thread is already large, has recently compacted, or the work may trigger compaction; the active agent still owns final integration and reporting.
6. Update docs or specs when behavior changes: keep `README.md`, `TASKS.md`, and agent guidance aligned with the implementation.
7. Implement the smallest change: stay within the requested scope and existing project shape.
8. Run targeted validation: choose checks from `.agents/references/testing.md` based on the diff.
9. Self-review: use `.agents/references/reviews.md` to check for behavior, compatibility, and validation gaps.
10. Report evidence: summarize changed files, validation run, and any remaining risk.

Direct one-off worker results are summarized in chat. Do not create durable `.agents/runs/` logs.

## Learning Capture

Treat learning capture as an intake check, not a mandatory persistent write
after every task. After task completion, validation failure, repeated retry, CI
failure, or user correction, check whether the work produced reusable
repository knowledge.

Persist a lesson only when all of these are true:

- The behavior was observed directly or reproduced.
- The fix or workflow was validated.
- The lesson is reusable beyond the current task.
- The lesson contains no secrets, credentials, personal data, raw logs,
  transient outage details, speculative explanation, one-off typo notes, or
  temporary debugging noise.
- The likely owner artifact has been checked for duplicates or overlapping
  guidance.

Choose the owner before editing:

- Use `.agents/references/` for durable AI-agent workflow guidance.
- Use `.agents/skills/` for reusable task-specific executable workflows.
- Use `.agents/prompts/` for narrow reusable prompt recipes.
- Use `.agents/plans/` or `TASKS.md` for active implementation sequencing.
- Use `docs/proposals/` for advisory findings that need maintainer triage.
- Use `docs/decisions/` for repository rule changes and accepted decisions.
- Use handoff notes, validation reports, `TASKS.md`, or no persistent artifact
  for temporary debugging observations unless they become reusable
  troubleshooting guidance.

Do not create `.ai/` memory files for this repository. If a lesson changes
repository rules, create the ADR or proposal required by `AGENTS.md` and
`docs/decisions/README.md`, then stop at the applicable gate. If a lesson is
task-local, mention it briefly in the handoff instead of creating persistent
documentation.

## Approved-Plan Task Loop

Before implementation starts from an approved plan, confirm the plan has `Status: Approved`, `Approved by:`, and `Approved at:` metadata, and that every plan question and required decision is answered, decided, or explicitly documented as an allowed assumption.

For approved multi-task plans, treat each named task as its own execution unit:

1. Use the assigned task packet as the task boundary.
2. Load only packet-approved context unless an escalation trigger fires.
3. Implement the task according to the direct execution loop where applicable.
4. Run task-appropriate validation from `.agents/references/testing.md`.
5. Self-review the task using `.agents/references/reviews.md`.
6. Return or record compact result evidence as required by the packet and `.agents/references/orchestration.md`.
7. Commit the completed task before starting the next plan task when commits are allowed in the environment.

Use `Project-Source: plan-task`, `Project-Plan:`, and `Project-Plan-Task:` commit metadata for approved plan-task commits.

Do not batch multiple plan tasks into one commit unless the approved plan or a later user request explicitly says those tasks are inseparable.

Per-task completion does not replace the later release workflow. Release preparation is expected to run after implementation tasks and should cover the full cross-task review, broader manual checks and tests, documentation updates, and release artifact preparation.

Delegated approved-plan execution, worker lanes, packet dispatch, parallel waves, plan result summaries, and plan/changelog handoff rules are defined in `.agents/references/orchestration.md`.

## Task Completion Timing

For `TASKS.md` items, keep the task in the open backlog while implementing. Do not move it to `TASKS_ARCHIVE.md` as part of the initial documentation alignment.

Move a task to the completed archive only after:

- The requested implementation or documentation change is finished.
- Task-appropriate validation from `.agents/references/testing.md` has passed or an explicit skipped-check reason is recorded.
- Self-review has checked for behavior, compatibility, documentation, and validation gaps.

After moving a `TASKS.md` task to `TASKS_ARCHIVE.md`, rerun documentation validation and `git diff --check` so the final task-state edit is also verified before handoff or commit.

## Context Rules

- Read only the context needed for the current task.
- Do not bulk-load every AI instruction file automatically; use `AGENTS.md` and the guidance map to choose the smallest governing read set.
- Classify artifact references by filename prefix before searching: `adr-NNNN` in `docs/decisions/`, `PLAN-<slug>` in `.agents/plans/` then `.agents/plans/archive/`, `PROP-<slug>` in `docs/proposals/` then `docs/proposals/archive/`, and `T-<AREA>-NNN` in `TASKS.md` then `TASKS_ARCHIVE.md`. Prefer exact filename lookup before scoped ref search, and fall back to repository-wide search only when scoped lookup fails or the reference is ambiguous.
- Broaden the read set only when targeted discovery shows another owner document is needed, the user asks for a broad audit, or validation requires cross-document consistency checks.
- For direct one-off work, do not load approved-plan orchestration details unless delegation, context pressure, or a coordination trigger makes `.agents/references/orchestration.md` relevant.
- Prefer existing IntelliJ Platform and Gradle plugin conventions over custom infrastructure.
- Publishing, signing, Marketplace metadata, and CI are in scope per ADR 0019; do not add unrelated release or operations files outside that scope.
- Follow `docs/decisions/README.md` for ADR requirements before changing governed implementation, workflow guidance, backlog, validation rules, or related behavior.
- If a requested change needs a plan instead of an ADR, create or update the plan first and stop. If it clearly needs both an ADR and a later plan, follow `docs/decisions/README.md` for the companion draft plan flow. Do not start implementation until the ADR is accepted when required and the user has reviewed and explicitly approved the plan.
- When an agent updates plan status during autonomous, orchestrated, or delegated implementation, record the status-history actor as the responsible agent identity in `Name <email>` form. Preserve `Approved by:` as the human approval identity unless the user explicitly changes it.

## Commit Rules

Commit completed work only when the user asks for a commit or the task scope explicitly requires it.

For approved multi-task plans, each completed plan task explicitly requires its own commit under ADR 0023.

When creating a commit for AI-authored work:

- Use Conventional Commits 1.0.0 style.
- Use `.gitmessage` as the authoritative template, rule set, and example source.
- Include the AI metadata trailer block required by `.gitmessage`.
- Keep all project metadata footers contiguous; do not put blank lines between trailer lines.
- Include `Validation:` with the command and result, or `not run` with a reason.
- When committing non-interactively, use a commit-message file or one final message paragraph for all trailer lines. Do not pass each footer as a separate `git commit -m` argument because Git inserts blank lines between message paragraphs.

Use `Project-Source: prompt` for direct ad hoc user requests, `task` for `TASKS.md` items, `plan` or `plan-task` for approved plan work, and `manual` only for human-authored commits outside the AI workflow. For `TASKS.md` work, include the `T-AREA-NNN` task ref in `Project-Task:`.

For orchestrated multi-agent commits:

- `Project-Worker: <worker-id>` is required on every commit authored by a task worker.
- `Project-Orchestrator: <orchestrator-id>` is required on every commit produced under orchestrated multi-agent execution, whether authored by the orchestrator or by a worker.
- `Project-Agent-Mode: <mode>` is required on every orchestrator and worker commit created in multi-agent execution.
- Allowed `Project-Agent-Mode:` values are `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, and `chat`.
- Worker and orchestrator identifiers must stay in trailers and must not be added to the Conventional Commits subject line.

## Stop Conditions

Stop and follow the ADR flow in `docs/decisions/README.md` when a request requires an ADR. That flow may include a companion draft plan when both artifacts are clearly required.

Stop after creating or updating a required plan and wait for explicit user approval before implementation starts.

Pause and ask for a decision when implementation depends on an unresolved product choice, such as direct dependency on proprietary AI Assistant APIs.

When a new question, missing decision, or unsafe assumption appears during planned implementation, stop work immediately and update the appropriate document before continuing: the active plan, `docs/decisions/OPEN_QUESTIONS.md`, `docs/decisions/`, or `TASKS.md`.
