# Orchestration Guide

Use this guide when an agent delegates work, coordinates approved-plan workers, or runs parallel sidecar work.

Direct execution loops live in `.agents/references/execution.md`. Plan creation, readiness, status, and task-packet shape live in `.agents/references/planning.md`.

## Roles

The active agent is the orchestrator whenever work is delegated. The orchestrator owns the full outcome even when workers perform exploration, edits, validation, or review.

The orchestrator owns:

- Owning the critical path and keeping local work moving while sidecar work runs.
- Confirming ADR gates, plan gates, answered questions, and required decisions before implementation starts.
- Selecting worker lanes and dispatching only task-shaped context.
- Checking current worktree state before write delegation so worker scope does not collide with existing edits.
- Keeping worker write scopes explicit and disjoint when more than one worker may edit.
- Reserving write scopes before dispatch and changing them only through an explicit orchestrator decision.
- Logging structured worker events in the chat transcript.
- Waiting for active workers to finish or fail before advancing dependent work.
- Reviewing worker output, diffs, validation evidence, self-review evidence, blockers, and commit metadata.
- Reconciling worker claims against the final diff, validation output, and governing artifact before handoff.
- Integrating results into the final response and deciding whether commits are allowed or required.
- Updating governing plan state and `CHANGELOG.md` when required before dispatching the next dependent plan task.

A worker owns only its assigned packet or brief. A worker must stop and report when it finds a new question, missing decision, unsafe assumption, scope conflict, or validation blocker that prevents safe completion.

## Worker Lanes

Use these lanes for approved plans and one-off delegated work:

- `implementation`: edits production, test, documentation, or workflow files inside the assigned write scope.
- `testing`: owns tests, fixtures, validation investigation, or failure triage. Testing workers may edit files only when the packet or brief grants an explicit write scope.
- `review`: read-only by default. Review workers receive the task packet or brief, diff or files under review, relevant spec or ADR, and validation output.

Use a fresh worker context for each approved-plan task. Do not carry worker context from one plan task to the next.

## Approved-Plan Workers

For a multi-task plan with `Status: Approved`, use one orchestrator and one fresh task worker per plan task when the environment supports delegation.

Task packets are the dispatch contract. Dispatch the plan header or readiness summary, execution graph, assigned task packet, and explicitly named governing artifacts or source files. Do not dispatch the full approved plan by default.

A worker may load the full plan only when the packet allows it or when a blocker requires broader plan review. The worker must report that escalation in the handoff.

Task workers return compact result summaries by default. The orchestrator updates the governing plan file and `CHANGELOG.md` when required before dispatching the next dependent task. A task packet may still grant explicit plan-file write scope when keeping the plan update in the task commit is clearer and safe.

## One-Off Delegation

For direct one-off work, subagent delegation is allowed by default when the active environment and tool contract support it.

Do not request separate user opt-in before using sidecar agents or workers unless the current request, tool limits, active tool contract, or higher-priority instructions require it. If the active tool contract requires explicit delegation permission, either ask for that permission or keep the work local. Respect an explicit no-delegation instruction in the current request.

Check context pressure before starting substantive exploration or edits. Use a fresh worker or read-only sidecar when the current thread is already large, has recently compacted, or the task is likely to read enough files, plans, ADRs, logs, or validation output to trigger compaction. Do not keep that work in the main thread merely because it is a one-off request.

Choose the delegation shape based on estimated task size, current thread context load, parallel value, and integration cost:

- Keep the work local for tiny edits, one-file fixes, obvious commands, urgent blocking steps, or ambiguous tasks where the next action depends on the answer.
- Use read-only sidecars for focused codebase exploration, validation investigation, or review when they can run in parallel and reduce the main thread's context load.
- Use write workers for one-off implementation only when the task is bounded, the write scope is explicit and disjoint, and the orchestrator can cheaply review and integrate the result.
- Avoid delegation when describing the brief, waiting for the result, or reconciling the output would cost more than doing the work directly.

Prefer read-only sidecar agents for focused exploration, review, or validation when they can run in parallel with local work. Permit one-off write workers only with explicit, disjoint write scopes.

The main agent remains the orchestrator. It owns final diff review, validation evidence, risk reporting, handoff notes, and commit decisions.

One-off worker results are summarized in chat. They do not create plan-file result summaries unless the work is governed by an approved plan.

## Task Packets And Briefs

Approved-plan task packets must include:

- Task id and task label.
- Worker lane: `implementation`, `testing`, or `review`.
- Required skills.
- Goal.
- Initial context budget.
- Allowed inputs, including exact plan summary, governing artifacts, source files, specs, ADRs, or validation output the worker may read.
- Forbidden inputs, especially unrelated archived plans, unrelated prior worker chat, and implementation evidence from other packets.
- Write scope, or `read-only` for review packets.
- Dependencies and sequence or wave constraints.
- Validation or review checks.
- Escalation triggers.
- Stop conditions.
- Expected output, including changed files or reviewed diff, validation evidence, blockers, review risks, and handoff notes.

For one-off delegated work, use this compact brief shape when the task is small enough that a full plan packet would add overhead:

- Label.
- Lane: `implementation`, `testing`, or `review`.
- Goal.
- Read first: exact files, artifacts, diffs, commands, or validation output the worker may inspect.
- Forbidden inputs: unrelated archives, prior worker chat, broad scans, or other context that should stay out of scope.
- Write scope, or `read-only`.
- Escalate if: conditions that allow broader context or require orchestrator input.
- Stop if: missing decisions, unsafe assumptions, scope conflicts, validation blockers, or write-scope collisions.
- Output: changed files or reviewed diff, validation or review evidence, blockers, review risks, and handoff notes.

Keep one-off briefs narrower than approved-plan packets. Do not include full plans, broad archives, or general guidance bundles unless the brief names a concrete trigger.

## Parallel Synchronization

Run only one task worker at a time unless the approved plan explicitly marks tasks as independent, gives them disjoint write scopes, declares a parallel `Workers:` value, and shows the parallel wave in `## Execution Graph`.

For one-off delegated work, parallel write workers also require explicit, disjoint write scopes. Read-only sidecar workers may run in parallel when their findings can be reconciled by the orchestrator.

For any parallel worker wave, the orchestrator must wait for every worker in the current execution step to report success or failure before moving to the next step. The orchestrator must verify each worker's committed result or commit-ready diff before advancing.

Use the current branch for orchestrated multi-agent execution. Do not use per-worker git worktrees unless a later accepted ADR explicitly authorizes worktrees and defines merge-back, validation, failed-worker handoff, and conflict-resolution rules.

## Worker Events

The chat transcript is the event log destination. Do not create durable `.agents/runs/` logs unless a later accepted ADR defines ownership, retention, cleanup, and commit rules.

The orchestrator must log full structured worker events in chat for approved-plan workers and one-off write workers:

- Log `start`, `stop`, or `fail` for each worker.
- Log whenever the active worker count changes.
- Include ISO 8601 timestamp, event type, worker id, plan id or one-off work label, plan task id when applicable, agent mode, active worker count, and active worker ids.

Read-only one-off sidecars may use compact start and result summaries instead of full structured events when they have no write scope and no commit attribution. Compact summaries must still name the sidecar label, lane, active purpose, result, blockers, and review risks.

## Result Summaries

Worker result summaries should stay compact:

- Worker id or lane.
- Changed files or reviewed diff.
- Validation or review evidence.
- Blockers.
- Review risks.
- Handoff notes.

For approved plans, the orchestrator records compact task result summaries in the governing plan. Do not paste raw test output, raw worker transcripts, or bulky run logs into the plan.

For direct one-off work, summarize worker results in the final chat response unless the user asks for a separate record.

## Changelog Boundaries

In orchestrated plan execution and release preparation, `CHANGELOG.md` maintenance belongs to the orchestrator. Task workers may suggest entries but do not own final changelog edits unless their packet explicitly grants that scope.

Update `CHANGELOG.md` only for notable public plugin-facing changes: plugin source or runtime behavior, public plugin documentation, compatibility, support, security or privacy behavior, or CI and release pipeline behavior that affects the plugin artifact or publication.

Omit internal AI-agent docs, skills, plans, proposals, ADR maintenance, workflow governance, scenario registers, test-case inventories, manual validation logs, and test-only changes unless they also change public plugin behavior, public docs, support promises, or release artifacts.
