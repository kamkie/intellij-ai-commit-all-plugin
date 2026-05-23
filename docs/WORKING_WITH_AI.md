# Working With AI

Use this guide when asking an AI agent to plan, implement, validate, review, or release work in this repository.

This file is for humans preparing requests. It is not part of the normal AI-agent read set. Agents should start from `AGENTS.md` and load only the specific owner guidance needed for the task.

## Fast Request Shape

Most requests work best when they name the outcome, scope, constraints, and expected proof:

```text
Task:
Goal:
Target artifacts:
Constraints:
Validation expected:
```

Example:

```text
Task: Fix T-DETEKT-001.
Goal: Reduce the Detekt baseline without changing runtime behavior.
Target artifacts: config/detekt/baseline.xml and focused Kotlin files.
Constraints: Keep changes mechanical unless a finding exposes a real bug.
Validation expected: .\gradlew.bat detekt and targeted tests if code changes.
```

For very small requests, a task ID, filename, prompt name, or concrete bug report is enough. The agent should use `AGENTS.md` lookup rules to find the owning artifact.

## Common Request Patterns

Use these patterns to set the amount of coordination you want. Delegation is optional, not required. If you want one agent only, say: `Do not delegate this work. Use only the current agent session.` Environment and tool limits still apply; if subagents, shell access, network access, browser tools, or validation tools are unavailable, the agent should state the limit and use the safest supported path.

For direct one-off work, keep the request narrow and name the expected proof:

```text
Task: Make a direct one-off change to <task ID, file, bug, or behavior>.
Goal:
Scope:
Constraints: Do not delegate. <or> Delegation is allowed if useful.
Validation expected:
```

Use direct one-off work for small documentation updates, focused bug fixes, or narrow cleanup that does not need a new ADR or implementation plan. If the agent discovers that an ADR, plan, or missing decision is required, it should stop at that gate instead of implementing.

For delegated one-off work, make the main agent's orchestration role and write boundaries explicit:

```text
Task: Complete <goal>; delegation is allowed if useful and the environment supports it.
Orchestrator: The main agent owns final diff review, validation evidence, risks, and handoff.
Worker scopes: Read-only sidecars for <research/review/validation>; write workers only for <disjoint files or directories>.
Constraints:
Validation expected:
```

Use this when focused sidecar exploration, validation, review, or disjoint edits can help. Delegation does not bypass ADR gates, plan gates, validation requirements, or the main agent's final review.

For approved plan execution, name the approved plan and the specific task packet:

```text
Task: Execute <PLAN-slug> task <task ID or packet label>.
Plan status: Approved.
Required skills:
Initial context:
Escalation triggers:
Write scope:
Validation expected:
Expected output: changed files, validation evidence, blockers, review risks, handoff notes.
```

Implementation from a plan should start only after the plan has explicit approval recorded in the plan metadata and status history. Task workers should use the packet's context budget first and escalate only when the packet allows it or when a blocker requires broader review.

For review-only sidecar delegation, keep the request read-only:

```text
Task: Run a review-only sidecar for <diff, files, plan task, or behavior>.
Mode: Read-only; do not edit files, stage changes, commit, or push.
Inputs allowed:
Review focus:
Expected output: findings, missing validation, compatibility risks, and residual concerns.
```

Use this when you want a second pass on a diff, design, validation result, or plan task. The main agent should reconcile sidecar findings and report which risks remain.

## Choose The Request Type

- Design: ask for early exploration before deciding whether an idea needs a proposal, ADR, plan, task, documentation change, or implementation.
- Planning: ask for an implementation plan when work spans multiple behavior areas, files, or unresolved technical choices.
- Implementation: name whether the work is direct one-off, delegated one-off, or approved-plan execution; include the behavior or task ID, target files if known, constraints, and validation expected.
- Review: ask for bugs, regressions, missing validation, compatibility risk, or architecture concerns; state whether review-only sidecar delegation is allowed or prohibited.
- Documentation: state whether the change affects plugin users, contributors, repository workflow, or AI-agent guidance.
- Proposal: ask for a proposal when you want findings, duplication, simplification, or improvement options collected for later triage.
- Release: ask only after implementation is integrated and validation evidence is ready.

Design output can stay in chat when no durable record is needed. If design work chooses or changes durable project direction, repository rules, compatibility policy, user behavior, validation expectations, or future maintenance policy, expect the ADR flow in `docs/decisions/README.md`.

## Useful References To Name

Name stable IDs or files when they are relevant:

- `T-<AREA>-NNN` for backlog tasks in `TASKS.md` or `TASKS_ARCHIVE.md`.
- `adr-NNNN` for decisions in `docs/decisions/`.
- `PLAN-<slug>` for implementation plans in `.agents/plans/`.
- `PROP-<slug>` for proposals in `docs/proposals/`.
- Prompt filenames such as `backlog-triage.md` for reusable repository prompt recipes.
- Concrete files when the request is intentionally narrow.

Avoid asking the agent to load every guidance file. Ask for the work, the constraints, and the expected result; the agent should choose the smallest governing read set.

## Constraints To State

State any constraint that would change the implementation or validation path:

- Target IntelliJ Platform version, currently the 2026.1 line.
- Target JetBrains IDEs, currently IDEs with the VCS Commit tool window.
- Git-only behavior and multiple Git root expectations.
- JetBrains AI Assistant dependency and whether proprietary APIs may be used directly. The default is no.
- Three-section `AI | Commit | Push` behavior or styling constraints.
- Plugin ID, package, vendor, license, Marketplace, signing, or CI constraints.
- Manual sandbox validation scope, especially AI Assistant, Git staging area, commit-only, commit-and-push, and push behavior.
- Delegation preference: allow optional delegation, require read-only sidecars only, define disjoint write scopes for workers, or prohibit delegation with `Do not delegate this work`.
- Environment or tool limits: no subagents, no network, no browser tools, read-only filesystem, unavailable validation tools, locked files, or commands that must not be run.

## Expected Stops

The agent should stop instead of implementing when the requested work needs:

- A new or superseding repository decision: follow the ADR flow in `docs/decisions/README.md`.
- A multi-step implementation plan: create or update the plan first.
- Missing user input: record or point to `docs/decisions/OPEN_QUESTIONS.md`.
- Maintainer triage of findings or options: create a proposal instead of implementing.

Implementation from a plan should start only after you explicitly approve that plan.

Delegation cannot bypass these stops. A delegated one-off request or review-only sidecar request should still stop at the ADR, plan, missing-input, or proposal gate when that gate applies.

## Validation To Ask For

Ask for validation that matches the risk:

- Documentation or AI-guidance changes: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1` and `git diff --check`.
- Repository skills or prompts: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1`.
- Kotlin or Gradle changes: `.\gradlew.bat spotlessCheck`, focused tests, and broader tests when shared behavior changes.
- Detekt cleanup: `.\gradlew.bat detekt`.
- Plugin packaging or descriptor changes: `.\gradlew.bat buildPlugin` and `.\gradlew.bat verifyPluginStructure`.
- Compatibility-sensitive changes: plugin verifier and targeted sandbox checks.
- Runtime commit, push, AI Assistant, or UI workflow changes: targeted automated tests plus manual sandbox evidence where the live IDE owns the behavior.

If the agent skips validation, expect a concrete reason.

## Commit Requests

The agent should not commit unless you ask for a commit or the approved task scope explicitly requires one.

When asking for a commit, expect a Conventional Commit message with the metadata trailer block defined in [.gitmessage](../.gitmessage). For approved plans, expect plan status and validation evidence to be current before each task commit.
