# Working With AI

Use this guide when asking an AI agent to plan, implement, validate, review, or release work in this repository.

This file is for humans preparing requests. It is not part of the normal AI-agent read set. Agents should start from `AGENTS.md` and load only the specific owner guidance needed for the task.

## Request Shape

Most requests only need the outcome, boundary, constraints, and proof:

```text
Task:
Goal:
Scope or target artifacts:
Constraints:
Validation expected:
```

For small requests, a ref, file path, prompt name, or concrete bug report is enough. A ref is a durable artifact name such as `T-BUG-013`, `adr-0074`, or `PLAN-<slug>`. The agent should use `AGENTS.md` lookup rules to find the owning artifact.

Prefer naming what must be true after the work over naming every file the agent should read. The repository guidance already tells agents how to find ADRs, plans, tasks, prompts, and owner docs.

## Work Modes

Use the smallest mode that fits the request:

- Direct one-off: narrow docs, focused bugs, cleanup, or simple commands. The agent should implement directly after clearing ADR, plan, proposal, and missing-input gates.
- Delegated one-off: useful when the task is context-heavy, the current thread is already large, or parallel exploration, review, or validation would reduce risk. Read-only sidecars fit most cases; write workers need explicit, disjoint scopes.
- Approved plan execution: name the `PLAN-<slug>` and task packet. Implementation starts only after plan approval is recorded and every required ADR is accepted.
- Review-only sidecar: ask for a read-only second pass over a diff, file set, plan task, validation output, or behavior.
- Proposal: use when you want findings, duplication, simplification, or improvement options for maintainer triage before committing to implementation.
- Release: use after implementation is integrated and validation evidence is ready.

Say `Do not delegate this work. Use only the current agent session.` when you want one agent only. Delegation never bypasses ADR gates, plan gates, validation, or final orchestrator review.

## Gates

Expect the agent to stop instead of implementing when the request needs:

- A new or superseding repository decision. The ADR flow lives in `docs/decisions/README.md`.
- A companion implementation plan. When both an ADR and later plan are clearly required, the agent may draft the proposed ADR and companion draft plan together, then stop.
- A plan without a required ADR. The agent should create or update the plan first, then wait for explicit approval.
- Missing maintainer input. The agent should record or point to `docs/decisions/OPEN_QUESTIONS.md`.
- Proposal triage. The agent should create or update a proposal instead of implementing findings.

Implementation from a plan should start only after you explicitly approve that plan and any required ADR is accepted.

## Refs To Name

Name refs or files when they are relevant:

- `T-<AREA>-NNN` for backlog tasks in `TASKS.md` or `TASKS_ARCHIVE.md`.
- `adr-NNNN` for decisions in `docs/decisions/`.
- `PLAN-<slug>` for implementation plans in `.agents/plans/`.
- `PROP-<slug>` for proposals in `docs/proposals/`.
- Prompt filenames such as `backlog-triage.md` for reusable repository prompt recipes.
- Concrete files when the request is intentionally narrow.

Avoid asking the agent to load every guidance file. Ask for the work, constraints, and expected result; the agent should choose the smallest governing read set.

## Constraints To State

State constraints that would change implementation, validation, or coordination:

- Target IntelliJ Platform version, currently the 2026.1 line.
- Target JetBrains IDEs, currently IDEs with the VCS Commit tool window.
- Git-only behavior and multiple Git root expectations.
- JetBrains AI Assistant dependency and whether proprietary APIs may be used directly. The default is no.
- Three-section `AI | Commit | Push` behavior or styling constraints.
- Plugin ID, package, vendor, license, Marketplace, signing, or CI constraints.
- Manual sandbox validation scope, especially AI Assistant, Git staging area, commit-only, commit-and-push, and push behavior.
- Delegation preference: optional delegation, read-only sidecars only, disjoint write scopes, or no delegation.
- Environment or tool limits: no subagents, no network, no browser tools, read-only filesystem, unavailable validation tools, locked files, or commands that must not be run.

## Validation To Ask For

Ask for validation that matches the risk:

- Documentation or AI-guidance changes: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1` and `git diff --check`.
- Repository refs, skills, prompts, or plans: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1`.
- Kotlin or Gradle changes: `.\gradlew.bat spotlessCheck`, focused tests, and broader tests when shared behavior changes.
- Detekt cleanup: `.\gradlew.bat detekt`.
- Plugin packaging or descriptor changes: `.\gradlew.bat buildPlugin` and `.\gradlew.bat verifyPluginStructure`.
- Compatibility-sensitive changes: plugin verifier and targeted sandbox checks.
- Runtime commit, push, AI Assistant, or UI workflow changes: targeted automated tests plus manual sandbox evidence where the live IDE owns the behavior.

If the agent skips validation, expect a concrete reason.

## Review Requests

For review, ask for the risk you care about:

- Bugs, regressions, missing validation, compatibility risk, or architecture concerns.
- Commit selection, AI generation, commit execution, push behavior, staging mode, or multi-root risk.
- Documentation that implies unsupported behavior.
- Read-only sidecar review when you want a second pass without edits.

Review findings should lead the answer. Summaries and change explanations are secondary.

## Commit Requests

The agent should not commit unless you ask for a commit or the approved task scope explicitly requires one.

When asking for a commit, expect a Conventional Commit message with the metadata trailer block defined in [.gitmessage](../.gitmessage). For approved plans, expect plan status and validation evidence to be current before each task commit.
