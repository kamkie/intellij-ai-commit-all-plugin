# Planning Guide

Use this guide when a task needs an implementation plan before editing.

This file owns plan creation, readiness, status rules, and task-packet shape. Use `.agents/references/execution.md` for canonical route selection, the approved-plan task execution loop, and commit rules. Use `.agents/references/orchestration.md` for orchestrator responsibilities, worker lanes, task packet dispatch, structured worker events, parallel synchronization, decision capsules, and result summaries.

## When To Plan

Create or update a plan when:

- The work introduces new intended plugin behavior or changes behavior not already decided by an accepted ADR, specification, owner document, or exact task ref.
- The work touches multiple areas, such as Gradle, plugin descriptor, Kotlin code, and docs.
- A user decision from `docs/decisions/OPEN_QUESTIONS.md` blocks implementation.
- The change may affect commit selection, AI message generation, commit execution, or push behavior.
- The change needs explicit sequencing, task packets, disjoint write scopes, or broader validation coordination.

Do not create a plan for small documentation cleanup unless the user asks for one.

Narrow implementation of already-decided behavior may use the direct one-off loop in `.agents/references/execution.md` when the intended outcome is already governed, the write set is small, and no ADR, missing-input, or coordination gate is triggered.

## Routing

Canonical route selection lives in the routing matrix in `.agents/references/execution.md`.

Use this guide after the route is `Plan`, or when checking whether a direct request has plan triggers. Plan triggers include new intended behavior, multi-area work, risky VCS, commit, push, AI generation, release, compatibility, unresolved decisions, task packets, disjoint write scopes, worker coordination, or broader validation.

When a requested change needs a plan instead of an ADR, create or update the plan first and stop. When a requested change clearly needs both an ADR and a later plan, follow `docs/decisions/README.md` and create a companion draft plan with the proposed ADR. Do not start implementation until the required ADR is accepted and the user has reviewed and explicitly approved the plan.

## Plan Location

Store active plans in `.agents/plans/`.

Use `.agents/plans/PLAN_TEMPLATE.md` as the starting shape. Name plan files with the `Plan-ID` ref as the filename prefix, for example:

```text
.agents/plans/PLAN-scaffold-plugin-project.md
.agents/plans/PLAN-commit-tool-window-actions.md
```

## Plan Content

A useful plan should include:

- `Plan-ID` ref in the form `PLAN-<short-kebab-slug>`, not a strictly number-based ref.
- Compact `Status`: `Draft`, `Approved`, `In Progress`, `Blocked`, `Implemented`, or `Closed`.
- `Workers:` metadata. Use `Workers: 1` for sequential plans and `Workers: N (parallel, tasks: <task refs or labels>)` only for approved independent tasks with disjoint write scopes.
- `Readiness` section that summarizes plan readiness, approval identity and timestamp when approved, open questions, and implementation progress.
- `Status History` section that records every status transition with timestamp, from-status, to-status, actor identity and action source, and short reason.
- Goal.
- Non-goals.
- Assumptions.
- Open questions.
- Proposed changes, split into named implementation tasks when the work has multiple tasks.
- Task packets for worker-owned tasks in approved multi-task plans.
- `Execution Graph` section with a fenced Mermaid graph that labels orchestrator nodes as `O<n>`, worker nodes as `W<n>`, includes worker agent modes, and shows task assignment plus sequence or wave ordering.
- Validation.
- Risks and fallback behavior.

## Planning Rules

- Keep plans short enough to maintain.
- Give every plan a human-readable `Plan-ID` ref such as `PLAN-scaffold-plugin-project`; avoid strictly number-based refs such as `PLAN-0001`.
- Include the `Plan-ID` ref in the plan filename for active and archived plans.
- Include `Workers:` metadata near the plan status.
- Keep `Plan-ID` stable when plan title, filename, status, or wording changes.
- Include `## Execution Graph` in every plan. Sequential plans may use a compact graph; parallel plans must show waves that match `Workers:` and ADR 0080 disjoint write scopes.
- For approved multi-task plans, include `## Task Packets` with a packet for each worker-owned task, or link to child packet files when the long-plan split rule applies.
- Use only canonical plan statuses from `.agents/plans/README.md`; `Closed` plans must include a `Close-Reason`.
- Treat `Approved` as an explicit user approval state, not an agent-assumed readiness label.
- Do not implement from a plan until the user has reviewed it, explicitly approved it, the plan status is `Approved`, and `Approved by:` records the approver.
- For approved and post-approval plans, set `Approved by:` to the configured Git identity in `Name <email>` form unless the current user request explicitly supplies another approver name.
- For approved and post-approval plans, set `Approved at:` to the ISO 8601 timestamp with timezone offset when approval was recorded.
- Draft or otherwise unapproved plans must not claim approval; omit `Approved by:` or leave it empty.
- Companion plans drafted with proposed ADRs must remain `Status: Draft`, must name the proposed ADR, and must state in `## Readiness` that implementation is blocked on ADR acceptance and later explicit plan approval.
- Every plan status change must append a `## Status History` entry in the form `<timestamp>: <from-status> -> <to-status> by <actor Name <email>>; <reason>`. `Status:` remains canonical and must match the latest status-history entry.
- Status-history actors are separate from `Approved by:`. Use the actor that caused the specific transition.
- For direct human commands that record a human decision or requested state change, use the configured Git identity in `Name <email>` form unless the current request explicitly supplies another human identity.
- For autonomous, orchestrated, or delegated implementation work performed by Codex or another agent, use the responsible agent identity in `Name <email>` form, matching the identity style used in AI-created commit trailers.
- Do not reuse the plan approver identity for later implementation status changes unless the later status change is itself a direct human command.
- Do not duplicate the full backlog from `TASKS.md`.
- Move unresolved user decisions to `docs/decisions/OPEN_QUESTIONS.md`.
- Before implementation starts from an `Approved` plan, every plan question and required project decision must be answered, explicitly decided, or recorded as a documented assumption that the current user request allows.
- Before implementation starts from an `Approved` plan, sub-agent workers must be available and authorized by the active tool contract. If they are not, approved-plan execution is blocked.
- Follow `docs/decisions/README.md` for ADR requirements before recording project decisions or repository rule changes in a plan.
- Update or delete stale plans when implementation makes them obsolete.
- For multi-task plans, name tasks clearly enough to use in `Project-Plan-Task:` commit metadata.
- Reference the `Plan-ID` ref in implementation handoffs, review notes, and commit metadata when work comes from a plan.
- When plan tasks come from `TASKS.md`, reference the `T-AREA-NNN` task ref alongside the task name.
- For multi-task plans, each task or approved parallel wave must be fully implemented, validated through `.agents/references/testing.md`, self-reviewed through `.agents/references/reviews.md`, and committed before the next dependent task or wave starts.
- Leave release-wide review, broader manual checks and tests, documentation update passes, and release artifact preparation to the later release workflow unless the plan is specifically a release plan.
- Use `.agents/references/orchestration.md` for delegated plan execution rules, including worker lanes, packet dispatch, parallel synchronization, worker events, result summaries, branch topology, and plan/changelog handoffs.

## Task Packets

Use task packets as the default dispatch contract for approved multi-task plans. The orchestrator owns the full approved plan; workers own only the packet assigned to them.

Each task packet must include:

- Task id and task label.
- Worker lane: `implementation`, `exploration`, `testing`, or `review`.
- Required skills.
- Goal.
- Initial context budget.
- Allowed inputs, including the exact plan summary, governing artifacts, source files, specs, ADRs, or validation output the worker may read.
- Forbidden inputs, especially unrelated archived plans, unrelated prior worker chat, and implementation evidence from other packets.
- Write scope, or `read-only` for exploration and review packets.
- Dependencies and sequence or wave constraints.
- Validation or review checks.
- Escalation triggers.
- Stop conditions.
- Expected output, including changed files or reviewed diff, validation evidence, blockers, review risks, and handoff notes.

Keep ordinary task packets inline in the parent plan. Use child packet files only when the parent plan would become difficult to scan, such as plans with more than six worker-owned tasks, multiple parallel waves, or expected parent-plan length above roughly 200 lines after packeting. Child packet files must preserve task packet refs and stay linked from the parent plan.

Keep the parent plan focused on approval, readiness, dependencies, execution graph, packet index, and compact task result summaries. Do not paste raw test output, raw worker transcripts, or bulky run logs into the plan.

Local packet mode is not an approved-plan execution fallback. Approved-plan tasks must run in sub-agent workers as defined in `.agents/references/orchestration.md`.

## Before Implementation

Before editing from a plan:

- Confirm the plan has `Status: Approved` from explicit user approval.
- Confirm `Approved by:` records the approver.
- Confirm `Approved at:` records the approval timestamp.
- Confirm every required ADR is accepted, including any ADR that produced a companion draft plan.
- Confirm every plan question and required decision is answered, decided, or explicitly assumed under the current request.
- Confirm sub-agent workers are available and authorized by the active tool contract; otherwise stop before implementation and report the blocker.
- Update the plan status to `In Progress` when implementation starts and keep `## Readiness` current.
- Append the matching `Approved -> In Progress` entry to `## Status History` with the status-history actor selected by action source.
- Identify files likely to change.
- Choose validation that matches the risk.
- Keep the first implementation step small enough to review.

## Questions During Implementation

If a new question, missing decision, or unsafe assumption appears while implementing from a plan:

- Stop implementation work immediately.
- Set the plan status to `Blocked` if the question prevents progress, and make the blocker visible in `## Readiness` or `## Open Questions`.
- Append the matching status-history entry when the plan status changes.
- Update the appropriate document before continuing: the active plan for task-local questions, `docs/decisions/OPEN_QUESTIONS.md` for missing user input, `docs/decisions/` for project decisions or repository rule changes, and `TASKS.md` when backlog scope or dependencies change.
- Ask the user for the decision when the answer cannot be safely inferred from the current request and governing documents.
- Resume only after the question is answered, decided, or explicitly documented as an allowed assumption.
