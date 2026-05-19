# Execution Guide

Use this loop for implementation work.

This file also owns AI-facing commit-message rules. The repository root `.gitmessage` is the authoritative commit-message template and example source.

## Loop

1. Frame the behavior: name the user-facing behavior, command, action, or workflow being changed.
2. Identify the owner artifact: find the source, descriptor, docs, or task list that governs the behavior.
3. Update docs or specs when behavior changes: keep `README.md`, `TASKS.md`, and agent guidance aligned with the implementation.
4. Implement the smallest change: stay within the requested scope and existing project shape.
5. Run targeted validation: choose checks from `.agents/references/testing.md` based on the diff.
6. Self-review: use `.agents/references/reviews.md` to check for behavior, compatibility, and validation gaps.
7. Report evidence: summarize changed files, validation run, and any remaining risk.

## Task Completion Timing

For `TASKS.md` items, keep the task in the open backlog while implementing. Do not move it to `TASKS_ARCHIVE.md` as part of the initial documentation alignment.

Move a task to the completed archive only after:

- The requested implementation or documentation change is finished.
- Task-appropriate validation from `.agents/references/testing.md` has passed or an explicit skipped-check reason is recorded.
- Self-review has checked for behavior, compatibility, documentation, and validation gaps.

After moving a `TASKS.md` task to `TASKS_ARCHIVE.md`, rerun documentation validation and `git diff --check` so the final task-state edit is also verified before handoff or commit.

## Multi-Task Plans

When working from a plan with `Status: Approved` that contains multiple implementation tasks, treat each named task as its own execution unit:

- Fully implement the task according to this execution loop.
- Run task-appropriate validation from `.agents/references/testing.md`.
- Self-review the task using `.agents/references/reviews.md`.
- Commit the completed task before starting the next plan task.

Use `Project-Source: plan-task`, `Project-Plan:`, and `Project-Plan-Task:` commit metadata for these commits.

Do not batch multiple plan tasks into one commit unless the approved plan or a later user request explicitly says those tasks are inseparable.

Per-task completion does not replace the later release workflow. Release preparation is expected to run after implementation tasks and should cover the full cross-task review, broader manual checks and tests, documentation updates, and release artifact preparation.

## Orchestrator And Task Workers

When a multi-task plan with `Status: Approved` is executed with delegated agents, use one orchestrator and one fresh task worker per plan task.

The orchestrator owns:

- Confirming all plan questions and required decisions are answered before implementation starts.
- Selecting the next named plan task.
- Giving the task worker only task-shaped context needed for that task.
- Handling new questions or missing decisions by stopping implementation and updating the owning document.
- Reviewing worker output, validation evidence, self-review evidence, and commit metadata.
- Starting the next task only after the current task is committed.

The task worker owns only its assigned task:

- Implement the task according to this execution loop.
- Run task-appropriate validation from `.agents/references/testing.md`.
- Self-review using `.agents/references/reviews.md`.
- Update the governing plan file for the assigned task in the same commit as the task work. If that is unsafe or inappropriate, explicitly hand the plan-file update back to the orchestrator within the same execution step.
- Commit the completed task when the task scope requires it, or return the exact commit-ready diff and evidence when the environment prevents worker commits.
- Stop immediately and report if a new question, missing decision, unsafe assumption, or scope conflict appears.

Use a fresh task worker context for each plan task. Do not carry worker context from one plan task to the next.

Run only one task worker at a time unless the approved plan explicitly marks tasks as independent, gives them disjoint write scopes, declares a parallel `Workers:` value, and shows the parallel wave in `## Execution Graph`.

Use the current branch for orchestrated multi-agent execution. Do not use per-worker git worktrees unless a later accepted ADR explicitly authorizes worktrees and defines merge-back, validation, failed-worker handoff, and conflict-resolution rules.

For a parallel worker wave, the orchestrator must wait for every worker in the current execution step to report success or failure before moving to the next step. The orchestrator must verify each worker's committed result or commit-ready diff before advancing.

The orchestrator must log structured worker events in the chat transcript:

- Log `start`, `stop`, or `fail` for each worker.
- Log whenever the active worker count changes.
- Include ISO 8601 timestamp, event type, worker id, plan id, plan task id, agent mode, active worker count, and active worker ids.

The chat transcript is the log destination. Do not create `.agents/runs/` logs unless a later accepted ADR defines ownership, retention, cleanup, and commit rules.

Before dispatching the next dependent task, the orchestrator must ensure the plan file reflects the completed, failed, blocked, or otherwise current task state. If a task produces a public plugin-facing change, the orchestrator updates the next unreleased `CHANGELOG.md` section before dispatching the next task. Public plugin-facing changes include plugin source or runtime behavior, public plugin documentation, compatibility, support, security or privacy behavior, and CI, signing, publishing, or release workflow changes that affect the plugin artifact or publication. Omit internal AI-agent docs, skills, plans, proposals, ADR maintenance, workflow governance, scenario-coverage registers, test-case inventories, manual validation logs, and test-only changes unless they also change public plugin behavior, public docs, support promises, or release artifacts.

## Context Rules

- Read only the context needed for the current task.
- Do not bulk-load every AI instruction file automatically; use `AGENTS.md` and the guidance map to choose the smallest governing read set.
- Classify artifact references by filename prefix before searching: `adr-NNNN` in `docs/decisions/`, `PLAN-<slug>` in `.agents/plans/` then `.agents/plans/archive/`, `PROP-<slug>` in `docs/proposals/` then `docs/proposals/archive/`, and `T-<AREA>-NNN` in `TASKS.md` then `TASKS_ARCHIVE.md`. Prefer exact filename lookup before scoped ID search, and fall back to repository-wide search only when scoped lookup fails or the reference is ambiguous.
- Broaden the read set only when targeted discovery shows another owner document is needed, the user asks for a broad audit, or validation requires cross-document consistency checks.
- Prefer existing IntelliJ Platform and Gradle plugin conventions over custom infrastructure.
- Publishing, signing, Marketplace metadata, and CI are in scope per ADR 0019; do not add unrelated release or operations files outside that scope.
- If the repo is still unscaffolded, do not assume Gradle, Kotlin, or plugin descriptor files exist.
- If a requested change requires creating an ADR, create the ADR first and stop. Do not update the governed implementation, workflow guidance, backlog, validation rules, or related behavior until the user has reviewed and explicitly accepted the ADR.
- If a requested change needs a plan, create or update the plan first and stop. Do not start implementation until the user has reviewed and explicitly approved the plan.
- Before implementing from an `Approved` plan, confirm the approval was explicit, `Approved by:` records the approver, and every plan question and required decision is answered, decided, or explicitly documented as an allowed assumption.
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

Use `Project-Source: prompt` for direct ad hoc user requests, `task` for `TASKS.md` items, `plan` or `plan-task` for approved plan work, and `manual` only for human-authored commits outside the AI workflow. For `TASKS.md` work, include the stable `T-AREA-NNN` task ID in `Project-Task:`.

For orchestrated multi-agent commits:

- `Project-Worker: <worker-id>` is required on every commit authored by a task worker.
- `Project-Orchestrator: <orchestrator-id>` is required on every commit produced under orchestrated multi-agent execution, whether authored by the orchestrator or by a worker.
- `Project-Agent-Mode: <mode>` is required on every orchestrator and worker commit created in multi-agent execution.
- Allowed `Project-Agent-Mode:` values are `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, and `chat`.
- Worker and orchestrator identifiers must stay in trailers and must not be added to the Conventional Commits subject line.

## Stop Conditions

Stop after creating a required ADR and wait for explicit user acceptance before changing the governed artifacts.

Stop after creating or updating a required plan and wait for explicit user approval before implementation starts.

Pause and ask for a decision when implementation depends on an unresolved product choice, such as direct dependency on proprietary AI Assistant APIs.

When a new question, missing decision, or unsafe assumption appears during planned implementation, stop work immediately and update the appropriate document before continuing: the active plan, `docs/decisions/OPEN_QUESTIONS.md`, `docs/decisions/`, or `TASKS.md`.
