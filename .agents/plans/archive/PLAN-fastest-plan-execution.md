# Plan: Fastest Plan Execution

Plan-ID: PLAN-fastest-plan-execution

Status: Closed

Close-Reason: Archived

Filename: `.agents/plans/archive/PLAN-fastest-plan-execution.md`

## Readiness

- Plan readiness: Closed; archived by user request.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-05-15T04:23:08+02:00
- Open questions: None.
- Implementation progress: Implemented; child plans executed in planned order with committed task-level outputs and final automated validation.

## Status History

- 2026-05-15T04:05:09+02:00: none -> Draft by Kamil Kiewisz <kamkie@outlook.com>; plan created.
- 2026-05-15T04:23:08+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicit user approval recorded.
- 2026-05-15T04:41:13+02:00: Approved -> In Progress by OpenAI Codex <codex@openai.com>; orchestrated implementation started.
- 2026-05-15T06:39:50+02:00: In Progress -> Implemented by OpenAI Codex <codex@openai.com>; planned changes completed and validated.

- 2026-05-17T22:40:44+02:00: Implemented -> Closed by Kamil Kiewisz <kamkie@outlook.com>; archived completed plan by user request.

## Goal

Run all active implementation plans in the fastest safe order by making cross-plan dependencies explicit, using parallel workers only where write scopes and behavior dependencies are disjoint, and preserving the repository rule that every plan task is validated, reviewed, and committed before the next dependent task starts.

## Non-Goals

- Do not implement child-plan behavior in this plan.
- Do not mark child plans `Approved` without explicit user approval.
- Do not bypass per-task validation, review, or commit requirements from `.agents/references/planning.md` and `.agents/references/execution.md`.
- Do not parallelize tasks that share commit workflow state, Gradle configuration, plugin descriptor entries, or user-facing behavior in a way that would create merge or safety risk.

## Assumptions

- The child plans remain the source of implementation details and task acceptance criteria.
- This plan is the source of cross-plan ordering and parallelization decisions.
- A later user request may explicitly approve this plan and all named child plans in one statement; until then, all implementation remains blocked.
- The orchestrator owns plan state, task dispatch, validation evidence, changelog maintenance, integration review, and commit verification.
- Parallel workers must receive disjoint file ownership and must stop if they discover a dependency on another worker's unmerged output.

## Open Questions

No open plan questions.

## Dependency Graph

Current child plans do not encode a full cross-plan dependency graph. Their dependencies are mostly implicit in goals and handoff notes. Use this graph during execution:

### Execution Graph

```mermaid
flowchart TD
    start["Approve PLAN-fastest-plan-execution and child plans"]
    files["PLAN-include-all-git-files"]
    ai["PLAN-ai-assistant-message-generation"]
    icons["PLAN-commit-tool-window-actions Task 1: icons"]
    verifier["PLAN-validation-coverage Task 1: verifier infrastructure"]
    completion["PLAN-ai-generation-completion"]
    actionShell["PLAN-commit-tool-window-actions Tasks 2-3: registration and routing shell"]
    notify["PLAN-error-handling-ux Task 1: notification surface"]
    commitPush["PLAN-commit-and-push-execution"]
    finalActions["PLAN-commit-tool-window-actions Task 4: final visibility and enablement"]
    finalErrors["PLAN-error-handling-ux Tasks 2-5: concrete workflow failures"]
    validation["PLAN-validation-coverage Tasks 2-3: E2E and sandbox coverage"]
    docsUsage["PLAN-user-documentation Task 1: usage and limitations"]
    releaseCi["PLAN-marketplace-ci-release"]
    docsRelease["PLAN-user-documentation Tasks 2-3: source, release, support, and changelog docs"]
    close["Final cross-plan validation and closure"]
    start --> files
    start --> ai
    start --> icons
    start --> verifier
    ai --> completion
    completion --> actionShell
    icons --> actionShell
    notify --> finalErrors
    files --> commitPush
    ai --> commitPush
    completion --> commitPush
    actionShell --> finalActions
    commitPush --> finalActions
    commitPush --> finalErrors
    completion --> finalErrors
    finalActions --> validation
    finalErrors --> validation
    commitPush --> validation
    verifier --> validation
    validation --> docsUsage
    validation --> releaseCi
    releaseCi --> docsRelease
    docsUsage --> docsRelease
    docsRelease --> close
```

### Wave Graph

```mermaid
flowchart LR
    wave0["Wave 0: approval and dispatch setup"]
    wave1["Wave 1: parallel foundations"]
    wave2["Wave 2: AI completion and workflow shell"]
    wave3["Wave 3: commit, push, final actions, final errors"]
    wave4["Wave 4: E2E validation and user docs"]
    wave5["Wave 5: Marketplace, CI, release docs"]
    wave6["Wave 6: final closure"]
    wave0 --> wave1 --> wave2 --> wave3 --> wave4 --> wave5 --> wave6
```

### Orchestrator And Workers Graph

Two views are shown:

- **Before** — current repository rules only (ADR 0026 + ADR 0030). Workers are anonymous, agent modes are implicit, no `Project-Worker` / `Project-Orchestrator` / `Project-Agent-Mode` trailers exist, no orchestrator start/stop log is required, no `Workers:` field is required on plans, and execution stays on a single branch.
- **After** — `docs/proposals/PROP-orchestrator-worker-rules-2026-05-15T05-31.md` findings S1–S4 adopted (ADRs A–D accepted). Workers carry stable ids and modes, the orchestrator emits structured start/stop log events, commit trailers identify worker/orchestrator/mode, plans declare an explicit `Workers:` count, and worktree topology is an allowed option under ADR 0026's disjoint-scope rule.

Conventions used in both graphs:

- `O[mode]` = orchestrator with its agent mode. The orchestrator owns plan state, dispatch, integration review, commit verification, and the changelog (ADR 0026, ADR 0030).
- `W#[mode]` = task worker with a worker id and its agent mode. Workers use fresh context per plan task (ADR 0026).
- Solid arrows show dispatch and result flow inside a wave; dashed arrows show wave-to-wave handoff.
- Mode vocabulary in the **After** view follows `PROP-orchestrator-worker-rules` finding S1 (`code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, `chat`); in the **Before** view the bracketed mode is informational only and is not encoded in commits or logs.

#### Before — Current Rules (PROP-orchestrator-worker-rules not yet accepted)

Under today's rules, ADR 0026 already allows parallel workers when the approved plan marks tasks independent with disjoint write scopes, but workers have no stable id, no mode is recorded in commits, and the orchestrator is not required to log start/stop events. Commits carry only the trailers defined by `.gitmessage` today (`Project-Source`, `Project-Plan`, `Project-Plan-Task`, etc.).

```mermaid
flowchart TD
    classDef orch fill:#1f3a5f,stroke:#0d1b33,color:#ffffff;
    classDef worker fill:#3a3a3a,stroke:#1a1a1a,color:#ffffff;
    classDef integ fill:#5a4a1f,stroke:#2a2310,color:#ffffff;

    subgraph B0["Wave 0: Approval And Dispatch Setup"]
        BO0["orchestrator<br/>approval, dispatch setup, ownership map"]
    end

    subgraph B1["Wave 1: Independent Foundations (parallel per ADR 0026)"]
        BO1["orchestrator"]
        BW1A["worker<br/>Track A — PLAN-include-all-git-files"]
        BW1B["worker<br/>Track B — PLAN-ai-assistant-message-generation"]
        BW1C["worker<br/>Track C — PLAN-commit-tool-window-actions Task 1"]
        BW1D["worker<br/>Track D — PLAN-validation-coverage Task 1"]
        BI1["orchestrator<br/>integration: build, docs validation, commit verification"]
        BO1 --> BW1A
        BO1 --> BW1B
        BO1 --> BW1C
        BO1 --> BW1D
        BW1A --> BI1
        BW1B --> BI1
        BW1C --> BI1
        BW1D --> BI1
    end

    subgraph B2["Wave 2: AI Completion And Workflow Shell"]
        BO2["orchestrator"]
        BW2A["worker<br/>PLAN-ai-generation-completion"]
        BW2B["worker<br/>PLAN-error-handling-ux Task 1"]
        BW2C["worker<br/>PLAN-commit-tool-window-actions Tasks 2–3"]
        BI2["orchestrator<br/>integration"]
        BO2 --> BW2A
        BO2 --> BW2B
        BO2 --> BW2C
        BW2A --> BI2
        BW2B --> BI2
        BW2C --> BI2
    end

    subgraph B3["Wave 3: Commit, Push, Final Actions, Final Errors"]
        BO3["orchestrator"]
        BW3A["worker<br/>PLAN-commit-and-push-execution"]
        BW3B["worker<br/>PLAN-commit-tool-window-actions Task 4"]
        BW3C["worker<br/>PLAN-error-handling-ux Tasks 2–5"]
        BI3["orchestrator<br/>integration: gradle buildPlugin, tests"]
        BO3 --> BW3A
        BO3 --> BW3B
        BO3 --> BW3C
        BW3A --> BI3
        BW3B --> BI3
        BW3C --> BI3
    end

    subgraph B4["Wave 4: E2E Validation And User Docs"]
        BO4["orchestrator (owns CHANGELOG.md)"]
        BW4A["worker<br/>PLAN-validation-coverage Tasks 2–3"]
        BW4B["worker<br/>PLAN-user-documentation Task 1"]
        BI4["orchestrator<br/>integration"]
        BO4 --> BW4A
        BO4 --> BW4B
        BW4A --> BI4
        BW4B --> BI4
    end

    subgraph B5["Wave 5: Marketplace, CI, Release Docs"]
        BO5["orchestrator"]
        BW5A["worker<br/>PLAN-marketplace-ci-release"]
        BW5B["worker<br/>PLAN-user-documentation Tasks 2–3"]
        BI5["orchestrator<br/>integration"]
        BO5 --> BW5A
        BO5 --> BW5B
        BW5A --> BI5
        BW5B --> BI5
    end

    subgraph B6["Wave 6: Final Closure"]
        BO6["orchestrator: full docs validation, gradle, verifier, plan status updates"]
    end

    BO0 -.-> BO1
    BI1 -.-> BO2
    BI2 -.-> BO3
    BI3 -.-> BO4
    BI4 -.-> BO5
    BI5 -.-> BO6

    class BO0,BO1,BO2,BO3,BO4,BO5,BO6,BI1,BI2,BI3,BI4,BI5 orch;
    class BW1A,BW1B,BW1C,BW1D,BW2A,BW2B,BW2C,BW3A,BW3B,BW3C,BW4A,BW4B,BW5A,BW5B worker;
```

Properties of the **Before** graph:

- Worker nodes are unlabeled actors. Their identity is not recoverable from commits or logs.
- Agent mode is not encoded anywhere; the orchestrator and workers may switch modes silently.
- No structured start/stop log is produced by the orchestrator; observability is limited to chat transcripts and the resulting commits.
- Parallel fan-out inside a wave is allowed only by ADR 0026 (independent tasks, disjoint write scopes) and is implicit in this plan's wave structure.
- Topology is single-branch. Worktrees are not an authorized execution option.

#### After — PROP-orchestrator-worker-rules Findings S1–S4 Adopted

Once ADRs A–D from `docs/proposals/PROP-orchestrator-worker-rules-2026-05-15T05-31.md` are accepted, every actor in the graph carries a stable id and an explicit mode, the orchestrator emits structured `start` / `stop` log events around each worker, and commits carry `Project-Worker` / `Project-Orchestrator` / `Project-Agent-Mode` trailers. Worktree usage becomes a documented option for waves whose tasks are independent with disjoint write scopes.

```mermaid
flowchart TD
    classDef orch fill:#1f3a5f,stroke:#0d1b33,color:#ffffff;
    classDef worker fill:#264d3b,stroke:#0f2419,color:#ffffff;
    classDef integ fill:#5a4a1f,stroke:#2a2310,color:#ffffff;

    subgraph W0["Wave 0: Approval And Dispatch Setup"]
        O0["O[code]<br/>orchestrator<br/>approval, dispatch setup, ownership map"]
    end

    subgraph W1["Wave 1: Independent Foundations (parallel, disjoint scopes)"]
        O1["O[code]<br/>orchestrator"]
        W1A["W1[code]<br/>Track A — PLAN-include-all-git-files"]
        W1B["W2[code]<br/>Track B — PLAN-ai-assistant-message-generation"]
        W1C["W3[code]<br/>Track C — PLAN-commit-tool-window-actions Task 1 (icons)"]
        W1D["W4[code]<br/>Track D — PLAN-validation-coverage Task 1 (verifier infra)"]
        I1["O[code]<br/>integration: build, docs validation, commit verification"]
        O1 --> W1A
        O1 --> W1B
        O1 --> W1C
        O1 --> W1D
        W1A --> I1
        W1B --> I1
        W1C --> I1
        W1D --> I1
    end

    subgraph W2["Wave 2: AI Completion And Workflow Shell"]
        O2["O[code]<br/>orchestrator"]
        W2A["W5[code]<br/>PLAN-ai-generation-completion"]
        W2B["W6[code]<br/>PLAN-error-handling-ux Task 1 (notification surface)"]
        W2C["W7[code]<br/>PLAN-commit-tool-window-actions Tasks 2–3 (registration shell)"]
        I2["O[code]<br/>integration: stop-path review, UI-thread check, per-task commits"]
        O2 --> W2A
        O2 --> W2B
        O2 --> W2C
        W2A --> I2
        W2B --> I2
        W2C --> I2
    end

    subgraph W3["Wave 3: Commit, Push, And Final Action Integration"]
        O3["O[code]<br/>orchestrator"]
        W3A["W8[code]<br/>PLAN-commit-and-push-execution"]
        W3B["W9[code]<br/>PLAN-commit-tool-window-actions Task 4 (final actions)"]
        W3C["W10[code]<br/>PLAN-error-handling-ux Tasks 2–5 (concrete failures)"]
        I3["O[run-verify]<br/>integration: gradle buildPlugin, tests, sandbox smoke"]
        O3 --> W3A
        O3 --> W3B
        O3 --> W3C
        W3A --> I3
        W3B --> I3
        W3C --> I3
    end

    subgraph W4["Wave 4: End-To-End Validation And User Docs"]
        O4["O[code]<br/>orchestrator (owns CHANGELOG.md)"]
        W4A["W11[code]<br/>PLAN-validation-coverage Tasks 2–3 (E2E, sandbox)"]
        W4B["W12[code]<br/>PLAN-user-documentation Task 1 (usage, limitations)"]
        I4["O[run-verify]<br/>integration: docs validation, gradle build, verifier"]
        O4 --> W4A
        O4 --> W4B
        W4A --> I4
        W4B --> I4
    end

    subgraph W5["Wave 5: Marketplace, CI, Release Automation, Release Docs"]
        O5["O[code]<br/>orchestrator"]
        W5A["W13[setup]<br/>PLAN-marketplace-ci-release"]
        W5B["W14[code]<br/>PLAN-user-documentation Tasks 2–3 (source, release, support)"]
        I5["O[run-verify]<br/>integration: CI syntax, secret boundaries, no publish"]
        O5 --> W5A
        O5 --> W5B
        W5A --> I5
        W5B --> I5
    end

    subgraph W6["Wave 6: Final Cross-Plan Closure"]
        O6["O[run-verify]<br/>orchestrator: full docs validation, gradle, verifier, plan status updates"]
    end

    O0 -.-> O1
    I1 -.-> O2
    I2 -.-> O3
    I3 -.-> O4
    I4 -.-> O5
    I5 -.-> O6

    class O0,O1,O2,O3,O4,O5,O6 orch;
    class W1A,W1B,W1C,W1D,W2A,W2B,W2C,W3A,W3B,W3C,W4A,W4B,W5A,W5B worker;
    class I1,I2,I3,I4,I5 integ;
```

Properties of the **After** graph (delta vs. **Before**):

- Worker ids `W1`–`W14` are illustrative labels for this plan's visualization. Real dispatch-time worker ids would appear in every commit via the new `Project-Worker: <worker-id>` trailer (Rule 3).
- Each orchestrator and worker node carries an explicit `[mode]` (`code`, `setup`, `run-verify`, …). The same value is recorded in commits via the new `Project-Agent-Mode: <mode>` trailer with a fixed vocabulary (Rule 5).
- Every commit also carries `Project-Orchestrator: <orchestrator-id>` so a worker's commit can be linked back to its dispatcher (Rule 4). None of these three trailers appears in the **Before** graph.
- The orchestrator emits a structured `start` / `stop` / `fail` log entry around each worker arrow (timestamp, worker id, plan id, plan task id, agent mode, active count). Destination is decided in ADR B (chat transcript vs `.agents/runs/<plan-id>/orchestrator.log`) (Rule 2).
- Synchronization is explicit: the orchestrator does not advance to the next wave until every worker arrow in the current wave has produced a verified commit or commit-ready diff (Rule 1).
- Topology is optional: each wave may run on a single branch or in per-worker git worktrees, but worktrees are allowed only when the approved plan marks the parallelized tasks as independent with disjoint write scopes (Rule 6, consistent with ADR 0026). The per-task commit rule from ADR 0023 still holds after merge-back.
- The plan itself declares the worker count up front (Rule 7). For this plan that would be `Workers: 14 (parallel by wave, tasks: as labeled W1–W14)` once `.agents/plans/PLAN_TEMPLATE.md` and `scripts/validate-docs.ps1` are updated.
- ADR 0026's parallel-execution condition and ADR 0030's "orchestrator alone touches `CHANGELOG.md`" rule are unchanged; the After view layers metadata and observability on top of them rather than replacing them.

The **After** graph is descriptive only at this stage: it relies on `Project-Worker`, `Project-Orchestrator`, `Project-Agent-Mode`, the orchestrator log, the `Workers:` plan field, and authorized worktree usage — none of which are active until ADRs A–D from `docs/proposals/PROP-orchestrator-worker-rules-2026-05-15T05-31.md` are accepted.

### Dependency Table

| Plan                                   | Upstream Dependencies                                                                                                            | Downstream Dependents                                                                                                      | Parallel Notes                                                                                                             |
|----------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `PLAN-include-all-git-files`           | None                                                                                                                             | `PLAN-commit-and-push-execution`, `PLAN-commit-tool-window-actions`, `PLAN-validation-coverage`, `PLAN-user-documentation` | Can run in parallel with AI action discovery if source package ownership is disjoint.                                      |
| `PLAN-ai-assistant-message-generation` | None                                                                                                                             | `PLAN-ai-generation-completion`, `PLAN-commit-and-push-execution`, `PLAN-validation-coverage`, `PLAN-user-documentation`   | Can run in parallel with file-selection work if source package ownership is disjoint.                                      |
| `PLAN-ai-generation-completion`        | `PLAN-ai-assistant-message-generation`                                                                                           | `PLAN-commit-and-push-execution`, `PLAN-error-handling-ux`, `PLAN-validation-coverage`, `PLAN-user-documentation`          | Should start after AI invocation has a concrete success/failure signal.                                                    |
| `PLAN-commit-and-push-execution`       | `PLAN-include-all-git-files`, `PLAN-ai-assistant-message-generation`, `PLAN-ai-generation-completion`                            | `PLAN-error-handling-ux`, `PLAN-validation-coverage`, `PLAN-user-documentation`                                            | Do not run before selection and completion gates exist.                                                                    |
| `PLAN-commit-tool-window-actions`      | Final routing depends on `PLAN-commit-and-push-execution`; icon work has no upstream dependency.                                 | `PLAN-validation-coverage`, `PLAN-user-documentation`                                                                      | Icon task can run early; action registration and enablement should wait for workflow services or a stable coordinator API. |
| `PLAN-error-handling-ux`               | Concrete failure branches depend on the workflow plans that expose them.                                                         | `PLAN-validation-coverage`, `PLAN-user-documentation`                                                                      | Notification setup can start early; final failure handling should run after core workflow paths exist.                     |
| `PLAN-validation-coverage`             | Full coverage depends on implemented workflow plans; verifier infrastructure can start earlier.                                  | `PLAN-user-documentation`, `PLAN-marketplace-ci-release`                                                                   | Split infrastructure from scenario execution.                                                                              |
| `PLAN-user-documentation`              | Depends on implemented behavior and, for release/source docs, Marketplace metadata.                                              | Release readiness                                                                                                          | Do not document behavior before it exists and is validated.                                                                |
| `PLAN-marketplace-ci-release`          | Core workflow should exist before publishable release workflow; CI and metadata can partially start earlier with disjoint files. | Release readiness                                                                                                          | Keep secrets out of repository and do not publish or tag unless separately requested.                                      |

## Proposed Execution Waves

### Wave 0: Approval And Dispatch Setup

- Confirm explicit approval for this plan and every child plan selected for execution.
- Update approved plan statuses before implementation starts.
- Assign file ownership for parallel workers before dispatch.
- Confirm `docs/decisions/OPEN_QUESTIONS.md` has no blockers.
- Create an integration checklist covering validation, changelog entries, support-policy updates, and final plan status updates.

### Wave 1: Independent Foundations

Run these in parallel only with disjoint ownership:

- Track A: Execute `PLAN-include-all-git-files`.
    - Suggested ownership: VCS selection services and related tests.
- Track B: Execute `PLAN-ai-assistant-message-generation`.
    - Suggested ownership: AI action discovery, invocation context, and related tests.
- Track C: Execute `PLAN-commit-tool-window-actions` Task 1 only.
    - Suggested ownership: final SVG icon resources.
- Track D: Execute early validation infrastructure from `PLAN-validation-coverage` Task 1 only if it does not touch files owned by another active worker.
    - Suggested ownership: plugin verifier configuration or validation documentation.

Integration checkpoint:

- Merge or review outputs in sequence.
- Run `gradle buildPlugin`.
- Run docs validation if any docs changed.
- Commit each completed child-plan task with the required plan metadata.

### Wave 2: AI Completion And Workflow Shell

Start after Wave 1 Track B completes and integration is green:

- Execute `PLAN-ai-generation-completion`.
- Execute `PLAN-error-handling-ux` Task 1 if notification setup is still independent.
- Continue `PLAN-commit-tool-window-actions` only for action registration that can compile against a stable workflow coordinator API.

Integration checkpoint:

- Validate timeout, unchanged message, empty message, and user-edit safety branches where practical.
- Confirm running activity does not block the IDE UI thread.
- Commit completed tasks before starting dependent execution work.

### Wave 3: Commit, Push, And Final Action Integration

Start after file selection and AI completion are integrated:

- Execute `PLAN-commit-and-push-execution`.
- Finish `PLAN-commit-tool-window-actions` routing, visibility, and enablement.
- Finish `PLAN-error-handling-ux` concrete workflow failures, including timeout, empty state, busy VCS, commit failures, and push failures.

Integration checkpoint:

- Run `gradle buildPlugin`.
- Run available automated tests.
- Manually smoke-test commit-only and commit-and-push in a local sandbox repository if practical at this stage.
- Confirm every stop path fails closed without unintended commit or push.

### Wave 4: End-To-End Validation And User Docs

Start after the runnable workflow is integrated:

- Execute remaining `PLAN-validation-coverage` tasks.
- Execute `PLAN-user-documentation` Task 1 for setup, usage, AI Assistant dependency, sandbox command, and known limitations.

Parallelism:

- Validation scenario execution and README drafting can overlap only if documentation uses implemented behavior and validation evidence already produced.
- Keep changelog maintenance with the orchestrator.

Integration checkpoint:

- Run docs validation.
- Run Gradle build and configured verifier/tests.
- Record exact sandbox IDE product names and build numbers for manual validation.

### Wave 5: Marketplace, CI, Release Automation, And Release Docs

Start after the workflow and validation coverage are stable:

- Execute `PLAN-marketplace-ci-release`.
- Execute `PLAN-user-documentation` Tasks 2 and 3 once Marketplace metadata and release automation exist.

Integration checkpoint:

- Validate CI workflow syntax where possible.
- Confirm pull-request CI does not require Marketplace or signing secrets.
- Confirm signing and publishing reference only local properties, environment variables, or CI secrets.
- Do not tag or publish unless the user explicitly requests release execution.

### Wave 6: Final Cross-Plan Closure

- Run full docs validation.
- Run full available Gradle validation.
- Run plugin verifier if configured.
- Review all child plans and update statuses to `Implemented` only after their planned changes are complete and validated.
- Leave release-wide artifact preparation, tagging, and Marketplace publication for a separate explicit release request.

## Parallelization Rules

- Parallelize across plans only when the dependency graph allows it and file ownership is disjoint.
- Do not run two workers that both edit `build.gradle.kts`, `src/main/resources/META-INF/plugin.xml`, `README.md`, `CHANGELOG.md`, or the same package subtree.
- Do not run commit execution, action routing, or final error handling before file selection and AI completion gates exist.
- Do not let task workers update `CHANGELOG.md`; the orchestrator owns final changelog entries.
- Prefer one worker per child-plan task. A worker may execute multiple tasks only when the approved child plan says they are inseparable or the orchestrator records why they are a single safe unit.

## Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` after plan, docs, changelog, or workflow documentation edits.
- Run `gradle buildPlugin` at every integration checkpoint that changes executable code, plugin descriptor metadata, Gradle configuration, or resources.
- Run newly added tests before committing their owning task.
- Run plugin verifier when configured and before release-readiness claims.
- Manually validate sandbox IDE scenarios from `.agents/references/testing.md` before closing workflow plans.

## Risks

- Cross-plan parallelism can create hidden API dependencies between workers; require workers to stop when they need another worker's unmerged output.
- IntelliJ commit workflow APIs may force a different sequence after implementation discovery; update this plan before continuing if that changes the dependency graph.
- Release automation can conflict with validation infrastructure in Gradle or CI files; assign ownership carefully.
- Documentation can overstate readiness if written before validation evidence exists.

## Handoff Notes

At present, the fastest safe path is parallel Wave 1 foundation work, then sequential integration through AI completion and commit execution, followed by validation, documentation, and release automation. Treat this plan as the orchestrator's map; keep child plans as the implementation contracts.
