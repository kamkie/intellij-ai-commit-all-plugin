---
proposal_id: PROP-agent-learning-memory
generated_at: 2026-05-23T23-12
created_from: User request to adapt a JetBrains scratch AGENTS.md learning draft to repository conventions without creating an ADR.
purpose: Propose a repository-aligned way to capture reusable AI-agent learning without adding a parallel memory system.
scope: Covers the scratch learning draft, `AGENTS.md`, `.agents/references/`, `.agents/skills/`, `.agents/prompts/`, and proposal governance.
---

# Agent Learning Memory Proposal

This proposal respects `AGENTS.md`, `.agents/references/documentation.md`,
`docs/proposals/README.md`, and ADR 0079. It records proposal findings and
implementation evidence; the accepted repository rule change is owned by
ADR 0079 and `.agents/references/execution.md`.

## Table of Contents

- [Summary](#summary)
- [Creation Context](#creation-context)
- [Progress Tracker](#progress-tracker)
- [Proposal Items](#proposal-items)
  - [New Features](#new-features)
    - [F001. Add a governed agent learning capture workflow](#f001-add-a-governed-agent-learning-capture-workflow)
  - [Errors And Mistakes](#errors-and-mistakes)
  - [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
    - [D001. Avoid a parallel `.ai` memory tree](#d001-avoid-a-parallel-ai-memory-tree)
  - [Simplification Opportunities](#simplification-opportunities)
    - [S001. Narrow mandatory retrospectives to validated learning candidates](#s001-narrow-mandatory-retrospectives-to-validated-learning-candidates)
  - [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The scratch draft is directionally useful, but its proposed `.ai/` memory
  tree and continuous autonomous writes do not match this repository's current
  documentation ownership model.
- Reusable AI-agent knowledge should route through the existing governed
  owners: `.agents/references/`, `.agents/skills/`, `.agents/prompts/`,
  `.agents/plans/`, `TASKS.md`, proposals, and ADRs.
- ADR 0079 accepted the validation-gated learning capture workflow: identify
  reusable lessons, deduplicate them, choose the existing owner artifact, and
  apply the normal proposal, ADR, plan, or direct-doc path.
- The implemented workflow does not create `.ai/` files or change `AGENTS.md`.
  It adds the durable AI-agent rule to `.agents/references/execution.md`.

## Creation Context

- Why this proposal exists: the maintainer asked for a draft matching this
  repository's conventions after reviewing a generic autonomous-agent learning
  scratch.
- How it was created: compared the scratch draft's reflection and memory
  sections (`scratch_25.md:61`, `scratch_25.md:81`, `scratch_25.md:226`) with
  the repository entry-point ownership map (`AGENTS.md:11`, `AGENTS.md:12`,
  `AGENTS.md:13`, `AGENTS.md:60`) and proposal governance
  (`docs/proposals/README.md:3`, `docs/proposals/README.md:5`,
  `docs/proposals/README.md:148`).
- Scope guardrails: the initial proposal created no ADR because repository rule
  changes follow the explicit ADR gate in `AGENTS.md:66`. Later maintainer
  acceptance created and accepted ADR 0079. No implementation plan was created,
  because the accepted implementation is a bounded AI-agent documentation edit.

## Progress Tracker

| Id   | Title                                                            | Priority | Status | Decision |
|------|------------------------------------------------------------------|----------|--------|----------|
| F001 | Add a governed agent learning capture workflow                   | 4        | done   | accepted |
| D001 | Avoid a parallel `.ai` memory tree                               | 2        | done   | accepted |
| S001 | Narrow mandatory retrospectives to validated learning candidates | 2        | done   | accepted |

## Proposal Items

### New Features

#### F001. Add a governed agent learning capture workflow

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:18:40+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:25:54+02:00 |

##### Context

- Evidence: the scratch draft expects retrospective learning after task
  completion, failures, retries, and user correction (`scratch_25.md:61`,
  `scratch_25.md:69`). The repository already has focused owners for durable
  agent workflow, task-specific skills, and prompt recipes (`AGENTS.md:11`,
  `AGENTS.md:12`, `AGENTS.md:13`) plus a rule to keep context small
  (`AGENTS.md:60`).
- Impact: without a repo-shaped learning path, agents may either skip useful
  operational lessons or store them in an unmanaged location.
- Non-goals:
  - Do not approve a new standing rule without an ADR.
  - Do not make agents write persistent documentation after every task.
  - Do not store raw logs, secrets, personal data, or speculative root causes.
- Acceptance criteria:
  - The learning workflow names the existing owner artifacts for each kind of
      reusable knowledge.
  - The workflow requires strong evidence before persisting a lesson.
  - The workflow preserves the smallest-context rule and avoids broad guidance
      reads by default.
  - The workflow states when an ADR, proposal, plan, task, or direct docs edit
      is required.

##### Recommended Change

If accepted later, add a repository-specific learning capture section to the
appropriate governed artifact, likely `.agents/references/execution.md` or a
new focused `.agents/references/learning.md`. A draft section could be:

```markdown
## Learning Capture

After a task completion, validation failure, repeated retry, CI failure, or user
correction, check whether the result produced reusable repository knowledge.

Persist a learning only when:

- the behavior was observed directly or reproduced;
- the fix or workflow was validated;
- the lesson is reusable beyond the current task;
- the lesson contains no secrets, credentials, personal data, raw logs, or
  speculative explanation;
- an existing owner artifact has been checked for duplicates.

Choose the owner before editing:

- `.agents/references/` for durable AI-agent workflow guidance.
- `.agents/skills/` for reusable task-specific executable workflows.
- `.agents/prompts/` for narrow reusable prompt recipes.
- `.agents/plans/` or `TASKS.md` for active implementation sequencing.
- `docs/proposals/` for advisory findings that need maintainer triage.
- `docs/decisions/` for accepted repository rule changes and ADRs.

When the lesson changes repository rules, create the ADR or proposal required
by `AGENTS.md` and stop at the applicable gate. When the lesson is only
task-local, mention it in the handoff instead of creating persistent docs.
```

##### Review Notes

- none

##### Follow-Up

- Artifact: accepted `adr-0079-use-governed-agent-learning-capture.md` and
  `.agents/references/execution.md` `Learning Capture` section.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`.

### Errors And Mistakes

_No tracked findings._

### Duplications To Remove Or Reduce

#### D001. Avoid a parallel `.ai` memory tree

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:18:40+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:25:54+02:00 |

##### Context

- Evidence: the scratch draft proposes `.ai/learnings.md`,
  `.ai/patterns.md`, `.ai/failures.md`, and `.ai/workflows.md`
  (`scratch_25.md:81`, `scratch_25.md:86`, `scratch_25.md:97`,
  `scratch_25.md:98`, `scratch_25.md:99`, `scratch_25.md:100`). This
  repository already assigns durable agent workflow to `.agents/references/`,
  reusable skills to `.agents/skills/`, and prompt recipes to
  `.agents/prompts/` (`AGENTS.md:11`, `AGENTS.md:12`, `AGENTS.md:13`).
- Impact: a second memory tree would fragment governance and make future agents
  decide between overlapping sources.
- Non-goals:
  - Do not remove any current `.agents/` artifact.
  - Do not create new memory files as part of this proposal.
  - Do not block future dedicated learning documentation if it goes through
      the repository decision path.
- Acceptance criteria:
  - The adapted draft does not introduce `.ai/`.
  - Each memory category from the scratch draft maps to an existing governed
      owner or to no persistent artifact.
  - Temporary debugging observations are not kept as durable documentation
      unless they become reusable troubleshooting guidance.

##### Recommended Change

Use this owner mapping instead of the scratch draft's `.ai/` files:

| Scratch category                        | Repository owner                                                                                                                                                  |
|-----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Stable reusable heuristics              | `.agents/references/` owner document, or a proposal/ADR first when the heuristic changes rules                                                                    |
| Proven implementation patterns          | `.agents/skills/` for reusable workflows, or `.agents/references/code-style.md` / `.agents/references/testing.md` for durable guidance                            |
| Temporary debugging observations        | Handoff summary, validation report, `TASKS.md`, or no persistent artifact unless the finding is reusable                                                          |
| Project-specific operational procedures | `.agents/references/execution.md`, `.agents/references/planning.md`, `.agents/references/testing.md`, `.agents/references/troubleshooting.md`, or a focused skill |

##### Review Notes

- none

##### Follow-Up

- Artifact: accepted `adr-0079-use-governed-agent-learning-capture.md` and
  `.agents/references/execution.md` `Learning Capture` section.
- Validation: proposal review plus agent artifact validation.

### Simplification Opportunities

#### S001. Narrow mandatory retrospectives to validated learning candidates

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T23:18:40+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-23T23:25:54+02:00 |

##### Context

- Evidence: the scratch draft says the agent must perform a retrospective after
  several common events and continuously update memory files
  (`scratch_25.md:69`, `scratch_25.md:299`). The repository already requires
  focused context and gated ADR or plan flow for repository rule changes
  (`AGENTS.md:60`, `AGENTS.md:66`, `AGENTS.md:67`).
- Impact: a mandatory write-heavy loop could create noisy documentation churn
  and slow small tasks without producing validated reusable knowledge.
- Non-goals:
  - Do not remove final self-review or validation obligations.
  - Do not prevent agents from reporting useful lessons in handoff notes.
  - Do not persist speculative lessons.
- Acceptance criteria:
  - Retrospectives are lightweight unless a validated reusable lesson exists.
  - Persistent updates require a clear owner, evidence, and deduplication.
  - Repository rule changes still stop at the ADR gate.

##### Recommended Change

Rewrite the scratch retrospective rule as an intake check:

1. Identify the root cause only when the task involved a failure, correction,
   repeated retry, CI issue, or meaningful workflow discovery.
2. Decide whether the lesson is reusable and validated.
3. Search the likely owner artifact for duplicates.
4. If persistence is in scope and allowed by the current request, update the
   owner artifact and run required validation.
5. If persistence needs maintainer triage, create a proposal or ADR as required
   and stop at that gate.
6. If the lesson is task-local, mention it briefly in the handoff and do not
   create durable documentation.

##### Review Notes

- none

##### Follow-Up

- Artifact: accepted `adr-0079-use-governed-agent-learning-capture.md` and
  `.agents/references/execution.md` `Learning Capture` section.
- Validation: proposal review and agent artifact validation.

### Smaller / Stylistic Items

- Use `AI-agent` consistently when describing repository automation guidance.
- Keep learning guidance in imperative, task-shaped bullets instead of broad
  motivational language.

## Suggested Priority Order

1. `D001` - completed by excluding a parallel `.ai/` tree in ADR 0079 and the
   execution guide.
2. `S001` - completed by defining learning capture as a validation-gated intake
   check instead of a mandatory write-heavy retrospective.
3. `F001` - completed by adding the governed workflow to
   `.agents/references/execution.md`.

## Out Of Scope

- Creating any additional ADR for agent learning or repository memory.
- Updating `AGENTS.md`, `.agents/skills/`, or `.agents/prompts/`.
- Creating `.ai/` files or any other new memory tree.
- Changing plugin runtime behavior, tests, release process, or user-facing
  documentation.
- Updating `CHANGELOG.md`, because this proposal is internal AI-agent workflow
  triage and does not change public plugin behavior.
