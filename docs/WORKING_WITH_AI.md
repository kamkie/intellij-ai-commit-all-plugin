# Working With AI

Use this guide when asking an AI agent to plan, implement, validate, review, or release work in this repository.

This file is for humans preparing requests. It is not part of the normal AI-agent read set. Do not paste it into ordinary work requests; the repository AI entry point is `AGENTS.md`.

## Request Shape

Most requests only need the outcome, boundary, constraints, and proof:

```text
Task:
Goal:
Scope or target artifacts:
Constraints:
Validation expected:
```

For small requests, a ref, file path, prompt name, or concrete bug report is enough. A ref is a durable artifact name such as `T-BUG-013`, `adr-0074`, or `PLAN-<slug>`.

Prefer naming what must be true after the work over naming every file to read. The normal AI workflow already knows how to find ADRs, plans, tasks, prompts, and owner docs.

## Work Modes

Use the smallest mode that fits the request:

- Direct one-off: narrow docs, focused bugs, cleanup, or simple commands.
- Direct one-off for decided behavior: use this for narrow implementation when an accepted ADR, specification, owner document, or exact task ref already defines the intended outcome.
- Delegated one-off: use this when the task is context-heavy, the current thread is already large or recently compacted, or parallel exploration, review, or validation would reduce risk. Read-only sidecars fit most cases; write workers need explicit, disjoint scopes and a compact brief.
- Approved plan execution: name the `PLAN-<slug>` and task packet. Implementation starts only after plan approval is recorded and every required ADR is accepted.
- Review-only sidecar: ask for a read-only second pass over a diff, file set, plan task, validation output, or behavior.
- Proposal: use when you want findings, duplication, simplification, or improvement options for maintainer triage before committing to implementation.
- Release: use after implementation is integrated and validation evidence is ready.

Say `Do not delegate this work. Use only the current agent session.` when you want one agent only. Delegation never bypasses ADR gates, plan gates, validation, or final orchestrator review.

Say `Use subagents/delegation as needed to avoid context compaction.` when the current thread is large, recently compacted, or the task is likely to require broad exploration. For write delegation, name the intended file or directory ownership when you know it.

## Lifecycle Requests

Use these request shapes to make the development stage explicit:

- Design-only: `Run a design-only pass for <UI/concept/draft>. Compare variants, note visual risks, and do not implement production plugin UI.`
- Proposal: `Create a proposal for <problem>. I want findings and options for triage, not implementation.`
- ADR: `Draft an ADR for <decision>. Stop after the ADR unless a companion draft plan is clearly required.`
- Planning: `Create or update PLAN-<slug> for <goal>. Do not implement until I approve the plan.`
- Direct implementation: `Implement <ref or behavior>. Keep it direct if existing ADRs/specs already decide the behavior.`
- Delegated one-off: `Use read-only sidecars for exploration/review. Keep writes in <files or dirs>. Stop if scope expands.`
- Approved plan execution: `I accept adr-NNNN and approve PLAN-<slug>. Execute the plan and commit each approved plan task.`
- Validation: `Run validation for <risk or artifact>. Report commands, results, skipped checks, and remaining risk.`
- Review: `Review <diff/files/ref> for <risk>. Findings first; do not edit.`
- Release: `Check release readiness for <version or boundary>. Include changelog, support, package, signing, CI, tag, and Marketplace readiness.`

## Gates

Some requests stop at a gate before implementation. Use explicit approval language when you want to move forward:

- A new or superseding repository decision. The ADR flow lives in `docs/decisions/README.md`.
- A companion implementation plan. When both an ADR and later plan are clearly required, ask for both as drafts.
- A plan without a required ADR. Small implementation of already-decided behavior can stay direct when no risky workflow or coordination gate applies.
- Missing maintainer input. Name the choice directly when you can.
- Proposal triage. Keep proposal findings separate from implementation until you accept a direction.

Useful approval phrases:

- `I accept adr-NNNN.`
- `I approve PLAN-<slug>; execute it.`
- `Do not implement yet; update the plan only.`
- `Proceed as a direct one-off unless a gate triggers.`

## Refs To Name

Name refs or files when they are relevant:

- `T-<AREA>-NNN` for backlog tasks in `TASKS.md` or `TASKS_ARCHIVE.md`.
- `adr-NNNN` for decisions in `docs/decisions/`.
- `PLAN-<slug>` for implementation plans in `.agents/plans/`.
- `PROP-<slug>` for proposals in `docs/proposals/`.
- Prompt filenames such as `backlog-triage.md` for reusable repository prompt recipes.
- Concrete files when the request is intentionally narrow.

Avoid asking for every guidance file to be loaded. Ask for the work, constraints, and expected result.

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
- Context protection: say `Use subagents/delegation as needed to avoid context compaction.`
- One-off worker boundary: name the goal, read-first files, forbidden inputs, write scope, escalation triggers, stop conditions, and expected output when you want delegated implementation.
- Environment or tool limits: no subagents, no network, no browser tools, read-only filesystem, unavailable validation tools, locked files, or commands that must not be run.

For delegated one-off writes, a compact human brief can be:

```text
Goal:
Read first:
Forbidden inputs:
Write scope:
Escalate if:
Stop if:
Expected output:
```

## Validation To Ask For

Ask for validation that matches the risk:

- Documentation or AI-guidance changes: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1` and `git diff --check`.
- Repository refs, skills, prompts, or plans: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1`.
- Kotlin or Gradle changes: `.\gradlew.bat spotlessCheck`, focused tests, and broader tests when shared behavior changes.
- Detekt cleanup: `.\gradlew.bat detekt`.
- Plugin packaging or descriptor changes: `.\gradlew.bat buildPlugin` and `.\gradlew.bat verifyPluginStructure`.
- Compatibility-sensitive changes: plugin verifier and targeted sandbox checks.
- Runtime commit, push, AI Assistant, or UI workflow changes: targeted automated tests plus manual sandbox evidence where the live IDE owns the behavior.

If validation is skipped, ask for the concrete reason.

## Review Requests

For review, ask for the risk you care about:

- Bugs, regressions, missing validation, compatibility risk, or architecture concerns.
- Commit selection, AI generation, commit execution, push behavior, staging mode, or multi-root risk.
- Documentation that implies unsupported behavior.
- Read-only sidecar review when you want a second pass without edits. Read-only sidecars may return compact findings; write workers and approved-plan workers need fuller validation and handoff evidence.

Review findings should lead the answer. Summaries and change explanations are secondary.

## Commit Requests

Ask for a commit explicitly when you want one. Approved plan tasks may already require commits.

When asking for a commit, expect a Conventional Commit message with the metadata trailer block defined in [.gitmessage](../.gitmessage). For approved plans, expect plan status and validation evidence to be current before each task commit.
