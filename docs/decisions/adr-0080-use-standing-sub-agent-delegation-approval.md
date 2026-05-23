---
status: accepted
date: 2026-05-24
accepted_at: 2026-05-24T00:33:31+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Use Standing Sub-Agent Delegation Approval

## Context and Problem Statement

The repository guidance encourages context discipline and delegated workers or
read-only sidecars when they materially help. A tool contract may require
explicit user approval before spawning sub-agents, and that led an agent to keep
a broad documentation task local after treating absent per-task approval as a
blocker.

The maintainer has now explicitly approved sub-agent use for this repository,
asked that agents never treat presumed lack of approval as a reason to act as
orchestrator only, and clarified that plan execution should always run tasks in
sub-agents. How should the repository make that approval durable without
requiring repeated per-task delegation permission, and how should approved-plan
execution behave when sub-agents are unavailable?

## Decision Drivers

* Preserve the current context-discipline and delegation model.
* Avoid repeated per-task approval prompts when the maintainer has already
  granted repository-local delegation approval.
* Keep newer user constraints, higher-priority instructions, and active tool
  contracts authoritative.
* Keep direct one-off delegation value-based instead of mandatory for every
  task.
* Require approved-plan tasks to run in sub-agents, preserving orchestrator and
  worker separation.
* Refuse approved-plan execution when sub-agents are unavailable instead of
  silently falling back to orchestrator-only execution.
* Preserve the main agent's responsibility for final integration, validation,
  risk reporting, and handoff.

## Considered Options

* Treat repository-local sub-agent approval as standing authorization and
  require sub-agents for approved-plan tasks.
* Require explicit delegation approval in each user request.
* Keep local packet mode as a fallback for approved-plan tasks.
* Require sub-agent delegation for every eligible task, including direct
  one-off work.

## Decision Outcome

Chosen option: "Treat repository-local sub-agent approval as standing authorization and require sub-agents for approved-plan tasks", because it records the maintainer's explicit instruction, preserves value-based delegation for direct one-off work, and keeps approved-plan execution from collapsing into orchestrator-only implementation.

If accepted:

* `AGENTS.md` records that this repository has standing maintainer approval for
  sub-agent delegation.
* Agents must not treat presumed lack of delegation approval as the reason for
  keeping work local in this repository.
* Direct one-off work may still stay local when delegation adds no material
  value, increases coordination cost, conflicts with the current request,
  conflicts with higher-priority instructions, or is blocked by the active tool
  contract.
* Approved-plan execution must dispatch each executable plan task, or each task
  in an approved parallel wave, to a sub-agent worker.
* Parallel approved-plan waves still require independent tasks with disjoint
  write scopes.
* Local packet mode is not an allowed fallback for approved-plan task execution.
* If sub-agents are unavailable, unauthorized by the active tool contract, or
  explicitly forbidden for the current approved-plan execution request, the
  agent must refuse to execute the plan task, report the blocker, and leave
  implementation unstarted.
* Newer user instructions can narrow or forbid delegation for direct one-off
  work. For approved-plan execution, no-delegation instructions block execution
  unless a later accepted repository decision changes this policy.
* `.agents/references/orchestration.md` explains how standing approval interacts
  with one-off delegation, approved-plan workers, and local packet mode.
* `docs/WORKING_WITH_AI.md` may mention that users can still opt out of
  delegation or restrict it to read-only sidecars for direct one-off requests,
  but approved plan execution requires sub-agents.

### Consequences

* Good, because future agents have a durable repository-local answer when
  deciding whether delegation is authorized.
* Good, because broad audits and context-heavy work can use sidecars without
  spending turns on permission checks.
* Good, because approved plans keep a hard separation between orchestrator and
  task worker roles.
* Good, because unavailable sub-agents produce an explicit blocker instead of a
  lower-isolation local execution path.
* Good, because explicit no-delegation instructions remain effective for direct
  one-off work.
* Bad, because agents still need judgment to decide whether delegation is worth
  the coordination overhead outside approved plans.
* Bad, because approved-plan execution can be blocked in environments that do
  not expose sub-agent tools.

### Confirmation

After acceptance, confirm implementation by checking:

* `AGENTS.md` states the standing delegation approval.
* `.agents/references/orchestration.md` says presumed lack of approval is not a
  local-execution reason in this repository.
* `.agents/references/orchestration.md` says approved-plan tasks must use
  sub-agent workers and must stop when sub-agents are unavailable.
* `.agents/references/planning.md` and `.agents/references/execution.md` do not
  present local packet mode as an approved-plan execution fallback.
* `docs/WORKING_WITH_AI.md` preserves request controls for no delegation and
  read-only sidecars while noting that approved-plan execution requires
  sub-agents.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  passes.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`
  passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Treat repository-local sub-agent approval as standing authorization and require sub-agents for approved-plan tasks

This option records the maintainer's explicit repository instruction, lets
agents decide direct one-off delegation based on task value and active
constraints, and requires approved-plan tasks to run in sub-agent workers.

* Good, because it removes repeated permission friction.
* Good, because it matches the existing orchestration model that prefers
  delegation for context-heavy or parallelizable work.
* Good, because it preserves hard worker separation for approved plans.
* Good, because it does not force delegation when direct one-off local work is
  cheaper or clearer.
* Bad, because agents must still recognize active tool-contract limitations and
  newer user constraints.
* Bad, because approved-plan execution blocks when sub-agents are unavailable.

### Require explicit delegation approval in each user request

This option keeps the prior conservative reading: no delegation unless the
current user request asks for it.

* Good, because it is maximally explicit per task.
* Bad, because it ignores the maintainer's standing repository instruction.
* Bad, because broad or context-heavy work may stay in the main thread only due
  to avoidable permission uncertainty.

### Keep local packet mode as a fallback for approved-plan tasks

This option would keep the current local packet mode fallback when delegation is
unavailable, not permitted, or not useful enough.

* Good, because plan implementation could still proceed in environments without
  sub-agent tools.
* Good, because it preserves a lower-overhead fallback for small plan tasks.
* Bad, because it conflicts with the maintainer's instruction that plan tasks
  should always run in sub-agents.
* Bad, because it allows approved-plan execution to lose fresh-context worker
  isolation silently.

### Require sub-agent delegation for every eligible task, including direct one-off work

This option would make delegation mandatory whenever a task could be split.

* Good, because it maximizes fresh-context isolation.
* Bad, because small tasks would pay unnecessary coordination cost.
* Bad, because delegation quality depends on task shape, write scope, and
  available tools.
* Bad, because it could conflict with explicit no-delegation requests.

## More Information

This ADR supersedes ADR 0026 while preserving its orchestrator, fresh task
worker, sequential-by-default, and disjoint-write-scope rules. The changed
policy is that approved-plan task workers are now required instead of
conditional on environment support.

No companion implementation plan is used because the implementation is a
bounded AI-agent documentation update after acceptance.

After this ADR is accepted, update the ADR Implementation Tracker in
`docs/decisions/README.md` with implementation status, evidence, and last
updated date.
