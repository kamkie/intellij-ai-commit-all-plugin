# Plans

This directory holds task-specific implementation plans for work that is too large or uncertain to execute directly.

## Use

- Create a plan when work spans multiple files or behavior areas.
- Use `PLAN_TEMPLATE.md` as the starting point.
- Give every plan a stable `Plan-ID` in the form `PLAN-<short-kebab-slug>`, such as `PLAN-scaffold-plugin-project`.
- Include the stable `Plan-ID` in the plan filename, such as `PLAN-scaffold-plugin-project.md`.
- Include `Workers: 1` for sequential plans. Use `Workers: N (parallel, tasks: <task ids or labels>)` only when the approved plan marks those tasks independent and assigns disjoint write scopes.
- Do not use a strictly number-based plan ID such as `PLAN-0001`; the ID should carry enough meaning to recognize the plan without its file path.
- Keep plans focused on one task or milestone.
- Include an `## Execution Graph` section with a fenced Mermaid graph. Label orchestrators as `O<n>` and workers as `W<n>`, include planned worker modes, and encode task assignment and ordering.
- Link unresolved user input back to `docs/decisions/OPEN_QUESTIONS.md`, and move accepted project decisions or repository rule changes to `docs/decisions/`.
- Follow `.agents/references/planning.md` and `.agents/references/execution.md` for plan readiness, per-task commits, and orchestrator or task-worker execution.
- Creating or updating a plan is not approval to implement. Implementation may start only after explicit user review and approval, recorded in `Approved by:` when the plan becomes approved.
- Move closed plans to `archive/` only after the plan no longer needs active execution or release-preparation updates. Preserve the `Plan-ID`, filename, and close reason.

## Active Plans

- `PLAN-automate-manual-scenarios` - Automate Manual Scenarios ([PLAN-automate-manual-scenarios.md](PLAN-automate-manual-scenarios.md)).

## Archived Plans

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

Keep `Plan-ID` stable when the plan title, filename, status, or wording changes. If a plan is renamed, preserve the `Plan-ID` in the filename. If a plan is split, keep the original ID for the closest surviving plan and assign new meaningful IDs to new plans.

## Worker Count And Execution Graph

Every plan must include a `Workers:` metadata field near `Status:`.

Sequential plans use:

```text
Workers: 1
```

Parallel plans use:

```text
Workers: N (parallel, tasks: <task ids or labels>)
```

`N` is the maximum intended active worker count. Parallel worker counts are valid only when the plan also marks the referenced tasks independent and assigns disjoint write scopes under ADR 0026.

Every plan must also include an `## Execution Graph` section with a fenced Mermaid graph. Use `O<n>` labels for orchestrator nodes and `W<n>` labels for worker nodes. Each worker node must include its planned agent mode: `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, or `chat`. The graph must encode task assignment by plan task id or stable task label, and it must show sequence, wave, or handoff ordering. Parallel waves in the graph must match `Workers:` and the disjoint write scopes described in the plan.
