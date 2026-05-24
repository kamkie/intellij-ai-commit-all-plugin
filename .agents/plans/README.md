# Plans

This directory holds task-specific implementation plans for work that is too large or uncertain to execute directly.

## Use

- Create a plan when work spans multiple files or behavior areas.
- Use `PLAN_TEMPLATE.md` as the starting point.
- Give every plan a `Plan-ID` ref in the form `PLAN-<short-kebab-slug>`, such as `PLAN-scaffold-plugin-project`.
- Include the `Plan-ID` ref in the plan filename, such as `PLAN-scaffold-plugin-project.md`.
- Include `Workers: 1` for sequential plans. Use `Workers: N (parallel, tasks: <task refs or labels>)` only when the approved plan marks those tasks independent and assigns disjoint write scopes.
- Do not use a strictly number-based plan ref such as `PLAN-0001`; the ref should carry enough meaning to recognize the plan without its file path.
- Keep plans focused on one task or milestone.
- For approved multi-task plans, define task packets for worker-owned tasks. Use inline packets by default.
- Include an `## Execution Graph` section with a fenced Mermaid graph. Label orchestrators as `O<n>` and workers as `W<n>`, include planned worker modes, and encode task assignment and ordering.
- Link unresolved user input back to `docs/decisions/OPEN_QUESTIONS.md`, and move accepted project decisions or repository rule changes to `docs/decisions/`.
- Follow `.agents/references/planning.md` for plan readiness and task-packet shape, `.agents/references/orchestration.md` for worker orchestration, packet dispatch, parallel synchronization, worker events, and result summaries, and `.agents/references/execution.md` for approved-plan task execution and per-task commits.
- Creating or updating a plan is not approval to implement. Implementation may start only after explicit user review and approval, recorded in `Approved by:` when the plan becomes approved.
- A plan drafted as a companion to a proposed ADR must stay `Status: Draft`, name the proposed ADR, and state that implementation is blocked until ADR acceptance and later explicit plan approval.
- Move closed plans to `archive/` only after the plan no longer needs active execution or release-preparation updates. Preserve the `Plan-ID`, filename, and close reason.
- Do not list closed plans under Active Plans, and do not use "ready to archive" as a catalog state; move archive-ready closed plans to Archived Plans in the same change that archives the file.

## Active Plans

- `PLAN-premature-stop-reliability` - Premature Stop Reliability ([PLAN-premature-stop-reliability.md](PLAN-premature-stop-reliability.md)), drafted as of 2026-05-25.
- `PLAN-test-coverage-growth` - Test Coverage Growth ([PLAN-test-coverage-growth.md](PLAN-test-coverage-growth.md)), drafted as of 2026-05-24.

## Archived Plans

- `PLAN-user-documentation-rebuild` - User Documentation Rebuild ([archive/PLAN-user-documentation-rebuild.md](archive/PLAN-user-documentation-rebuild.md)), archived as of 2026-05-24.
- `PLAN-unchanged-prefilled-ai-message` - Unchanged Prefilled AI Message ([archive/PLAN-unchanged-prefilled-ai-message.md](archive/PLAN-unchanged-prefilled-ai-message.md)), archived as of 2026-05-24.
- `PLAN-markdown-list-indent-two-spaces` - Markdown List Indent Two Spaces ([archive/PLAN-markdown-list-indent-two-spaces.md](archive/PLAN-markdown-list-indent-two-spaces.md)), archived as of 2026-05-24.
- `PLAN-github-release-for-tags` - GitHub Release For Tags ([archive/PLAN-github-release-for-tags.md](archive/PLAN-github-release-for-tags.md)), archived as of 2026-05-24.
- `PLAN-detekt-baseline-cleanup` - Detekt Baseline Cleanup ([archive/PLAN-detekt-baseline-cleanup.md](archive/PLAN-detekt-baseline-cleanup.md)), archived as of 2026-05-24.
- `PLAN-maintainability-stability-audit` - Maintainability Stability Audit ([archive/PLAN-maintainability-stability-audit.md](archive/PLAN-maintainability-stability-audit.md)), archived as of 2026-05-24.
- `PLAN-documentation-release-followups` - Documentation Release Follow-Ups ([archive/PLAN-documentation-release-followups.md](archive/PLAN-documentation-release-followups.md)), archived as of 2026-05-24.
- `PLAN-execution-context-discipline` - Execution Context Discipline ([archive/PLAN-execution-context-discipline.md](archive/PLAN-execution-context-discipline.md)), archived as of 2026-05-23.
- `PLAN-ai-execution-orchestration-optimization` - AI Execution Orchestration Optimization ([archive/PLAN-ai-execution-orchestration-optimization.md](archive/PLAN-ai-execution-orchestration-optimization.md)), archived as of 2026-05-23.
- `PLAN-release-matrix-ui-automation` - Release Matrix UI Automation ([archive/PLAN-release-matrix-ui-automation.md](archive/PLAN-release-matrix-ui-automation.md)), archived as of 2026-05-22.
- `PLAN-automate-manual-scenarios` - Automate Manual Scenarios ([archive/PLAN-automate-manual-scenarios.md](archive/PLAN-automate-manual-scenarios.md)), archived as of 2026-05-18.
- `PLAN-unified-formatting-linting-toolchain` - Unified Formatting And Linting Toolchain ([archive/PLAN-unified-formatting-linting-toolchain.md](archive/PLAN-unified-formatting-linting-toolchain.md)), archived as of 2026-05-18.
- `PLAN-confirm-staged-before-ai-generation` - Confirm Staged Before AI Generation ([archive/PLAN-confirm-staged-before-ai-generation.md](archive/PLAN-confirm-staged-before-ai-generation.md)), archived as of 2026-05-18.
- `PLAN-three-section-ai-commit-push-control` - Three-Section AI Commit Push Control ([archive/PLAN-three-section-ai-commit-push-control.md](archive/PLAN-three-section-ai-commit-push-control.md)), archived as of 2026-05-18.
- `PLAN-scaffold-plugin-project` - Scaffold Plugin Project ([archive/PLAN-scaffold-plugin-project.md](archive/PLAN-scaffold-plugin-project.md)), archived as of 2026-05-15.
- `PLAN-ai-assistant-message-generation` - AI Assistant Message Generation ([archive/PLAN-ai-assistant-message-generation.md](archive/PLAN-ai-assistant-message-generation.md)), archived as of 2026-05-17.
- `PLAN-ai-generation-completion` - AI Generation Completion ([archive/PLAN-ai-generation-completion.md](archive/PLAN-ai-generation-completion.md)), archived as of 2026-05-17.
- `PLAN-commit-and-push-execution` - Commit And Push Execution ([archive/PLAN-commit-and-push-execution.md](archive/PLAN-commit-and-push-execution.md)), archived as of 2026-05-17.
- `PLAN-commit-tool-window-actions` - Commit Tool Window Actions ([archive/PLAN-commit-tool-window-actions.md](archive/PLAN-commit-tool-window-actions.md)), archived as of 2026-05-17.
- `PLAN-error-handling-ux` - Error Handling And UX ([archive/PLAN-error-handling-ux.md](archive/PLAN-error-handling-ux.md)), archived as of 2026-05-17.
- `PLAN-fastest-plan-execution` - Fastest Plan Execution ([archive/PLAN-fastest-plan-execution.md](archive/PLAN-fastest-plan-execution.md)), archived as of 2026-05-17.
- `PLAN-include-all-git-files` - Include All Git Files ([archive/PLAN-include-all-git-files.md](archive/PLAN-include-all-git-files.md)), archived as of 2026-05-17.
- `PLAN-marketplace-ci-release` - Marketplace, CI, And Release Automation ([archive/PLAN-marketplace-ci-release.md](archive/PLAN-marketplace-ci-release.md)), archived as of 2026-05-17.
- `PLAN-pre-release-adr-implementation` - Pre-Release ADR Implementation ([archive/PLAN-pre-release-adr-implementation.md](archive/PLAN-pre-release-adr-implementation.md)), archived as of 2026-05-17.
- `PLAN-user-documentation` - User Documentation ([archive/PLAN-user-documentation.md](archive/PLAN-user-documentation.md)), archived as of 2026-05-17.
- `PLAN-validation-coverage` - Validation Coverage ([archive/PLAN-validation-coverage.md](archive/PLAN-validation-coverage.md)), archived as of 2026-05-17.

## Lifecycle

Use the smallest status set that preserves traceability:

- Draft: the plan is being shaped and may contain unanswered questions.
- Approved: the plan is ready to implement; all open questions and required decisions are answered, explicitly decided, moved to an owner document, or documented as allowed assumptions, and `Approved by:` records the approver.
- In Progress: implementation has started.
- Blocked: implementation cannot proceed; link the blocker in `## Readiness`, `## Open Questions`, or the relevant owner document.
- Implemented: planned changes are complete and task validation is done; release workflow may still remain.
- Closed: no further plan work is expected; include `Close-Reason: Released`, `Rejected`, `Superseded`, `Deferred`, or `Archived`.

`Accepted` and `Implementing` are not plan statuses; use `Approved` and `In Progress`. Use `Deferred`, not `Defered`.

Update the status in the plan file instead of leaving stale instructions.

Every plan must keep a short `## Readiness` section near the top with:

- Plan readiness.
- Approval identity for approved and post-approval plans.
- Open questions.
- Implementation progress.

Approved and post-approval plans with `Status: Approved`, `In Progress`, `Blocked`, `Implemented`, or `Closed` must include non-empty `Approved by:` and `Approved at:` entries in `## Readiness`.

Use the configured Git identity in `Name <email>` form when the configured repository user approved the plan, resolved from `git config user.name` and `git config user.email` when approval is recorded. Use another approver name only when the current user request explicitly supplies it.

Draft or otherwise unapproved plans must not claim approval. They may omit `Approved by:` or leave it empty.

Every plan must keep a `## Status History` section. Each status change must append a timestamped entry in this shape:

```text
- 2026-05-15T10:30:00+02:00: Draft -> Approved by Project Maintainer <maintainer@example.com>; explicit user approval recorded.
```

Status-history actors are separate from `Approved by:` and must identify who or what caused that specific transition. For direct human commands that record a decision or requested state change, use the configured Git identity in `Name <email>` form unless the request explicitly supplies another human identity. For autonomous, orchestrated, or delegated implementation work performed by Codex or another agent, use the responsible agent identity in `Name <email>` form, matching AI-created commit trailers.

Do not copy the plan approver identity into later implementation status-history entries unless the later status change is itself a direct human command.

The latest status-history entry must match the current `Status:` value. Use `none -> Draft` for the first creation entry when no previous status existed.

Keep `Plan-ID` stable when the plan title, filename, status, or wording changes. If a plan is renamed, preserve the `Plan-ID` in the filename. If a plan is split, keep the original ref for the closest surviving plan and assign new meaningful refs to new plans.

## Worker Count And Execution Graph

Every plan must include a `Workers:` metadata field near `Status:`.

Sequential plans use:

```text
Workers: 1
```

Parallel plans use:

```text
Workers: N (parallel, tasks: <task refs or labels>)
```

`N` is the maximum intended active worker count. Parallel worker counts are valid only when the plan also marks the referenced tasks independent and assigns disjoint write scopes under ADR 0080 and the orchestration rules in `.agents/references/orchestration.md`.

Every plan must also include an `## Execution Graph` section with a fenced Mermaid graph. Use `O<n>` labels for orchestrator nodes and `W<n>` labels for worker nodes. Each worker node must include its planned agent mode: `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, or `chat`. The graph must encode task assignment by plan task id or task label, and it must show sequence, wave, or handoff ordering. Parallel waves in the graph must match `Workers:` and the disjoint write scopes described in the plan.

## Task Packets

Approved multi-task plans must include task packets for worker-owned tasks. A task packet is the default worker dispatch contract and must include:

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

Use `Initial context budget` to name exact `Read first` and `Escalate to` artifacts. Do not seed packets with broad default guidance bundles when exact files, specs, ADRs, source paths, commands, or validation outputs are enough. Exploration and review packets must keep `Write scope: read-only`.

Use inline task packets for ordinary plans. Use child packet files only when the parent plan would become difficult to scan, such as plans with more than six worker-owned tasks, multiple parallel waves, or expected parent-plan length above roughly 200 lines after packeting. Child packet files must preserve task packet refs and stay linked from the parent plan.

The parent plan remains the source of approval, readiness, dependencies, execution graph, packet index, and compact task result summaries. Task result summaries should record worker id or lane, changed files or reviewed diff, validation evidence, blockers, review risks, and handoff notes. Do not paste raw test output, raw worker transcripts, or bulky run logs into the plan.

Approved-plan task execution requires sub-agent workers under ADR 0080. If sub-agents are unavailable, unauthorized by the active tool contract, or explicitly forbidden for approved-plan execution, stop before implementation and report the blocker instead of running the task locally.
