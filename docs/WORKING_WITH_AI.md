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

## Choose The Request Type

- Design: ask for early exploration before deciding whether an idea needs a proposal, ADR, plan, task, documentation change, or implementation.
- Planning: ask for an implementation plan when work spans multiple behavior areas, files, or unresolved technical choices.
- Implementation: name the behavior or task ID, the target files if known, constraints, and validation expected.
- Review: ask for bugs, regressions, missing validation, compatibility risk, or architecture concerns.
- Documentation: state whether the change affects plugin users, contributors, repository workflow, or AI-agent guidance.
- Proposal: ask for a proposal when you want findings, duplication, simplification, or improvement options collected for later triage.
- Release: ask only after implementation is integrated and validation evidence is ready.

Design output can stay in chat when no durable record is needed. If design work chooses or changes durable project direction, repository rules, compatibility policy, user behavior, validation expectations, or future maintenance policy, expect an ADR and a stop for explicit acceptance.

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

## Expected Stops

The agent should stop instead of implementing when the requested work needs:

- A new or changed repository decision: create an ADR first.
- A multi-step implementation plan: create or update the plan first.
- Missing user input: record or point to `docs/decisions/OPEN_QUESTIONS.md`.
- Maintainer triage of findings or options: create a proposal instead of implementing.

Implementation from a plan should start only after you explicitly approve that plan.

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
