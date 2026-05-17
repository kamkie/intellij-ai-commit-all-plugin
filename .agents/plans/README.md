# Plans

This directory holds task-specific implementation plans for work that is too large or uncertain to execute directly.

## Use

- Create a plan when work spans multiple files or behavior areas.
- Use `PLAN_TEMPLATE.md` as the starting point.
- Give every plan a stable `Plan-ID` in the form `PLAN-<short-kebab-slug>`, such as `PLAN-scaffold-plugin-project`.
- Include the stable `Plan-ID` in the plan filename, such as `PLAN-scaffold-plugin-project.md`.
- Do not use a strictly number-based plan ID such as `PLAN-0001`; the ID should carry enough meaning to recognize the plan without its file path.
- Keep plans focused on one task or milestone.
- Link unresolved user input back to `docs/decisions/OPEN_QUESTIONS.md`, and move accepted project decisions or repository rule changes to `docs/decisions/`.
- Follow `.agents/references/planning.md` and `.agents/references/execution.md` for plan readiness, per-task commits, and orchestrator or task-worker execution.
- Creating or updating a plan is not approval to implement. Implementation may start only after explicit user review and approval, recorded in `Approved by:` when the plan becomes approved.
- Move closed plans to `archive/` only after the plan no longer needs active execution or release-preparation updates. Preserve the `Plan-ID`, filename, and close reason.

## Active Plans

- `PLAN-fastest-plan-execution` - Fastest Plan Execution ([PLAN-fastest-plan-execution.md](PLAN-fastest-plan-execution.md)).
- `PLAN-commit-tool-window-actions` - Commit Tool Window Actions ([PLAN-commit-tool-window-actions.md](PLAN-commit-tool-window-actions.md)).
- `PLAN-include-all-git-files` - Include All Git Files ([PLAN-include-all-git-files.md](PLAN-include-all-git-files.md)).
- `PLAN-ai-assistant-message-generation` - AI Assistant Message Generation ([PLAN-ai-assistant-message-generation.md](PLAN-ai-assistant-message-generation.md)).
- `PLAN-ai-generation-completion` - AI Generation Completion ([PLAN-ai-generation-completion.md](PLAN-ai-generation-completion.md)).
- `PLAN-commit-and-push-execution` - Commit And Push Execution ([PLAN-commit-and-push-execution.md](PLAN-commit-and-push-execution.md)).
- `PLAN-error-handling-ux` - Error Handling And UX ([PLAN-error-handling-ux.md](PLAN-error-handling-ux.md)).
- `PLAN-validation-coverage` - Validation Coverage ([PLAN-validation-coverage.md](PLAN-validation-coverage.md)).
- `PLAN-user-documentation` - User Documentation ([PLAN-user-documentation.md](PLAN-user-documentation.md)).
- `PLAN-marketplace-ci-release` - Marketplace, CI, And Release Automation ([PLAN-marketplace-ci-release.md](PLAN-marketplace-ci-release.md)).
- `PLAN-pre-release-adr-implementation` - Pre-Release ADR Implementation ([PLAN-pre-release-adr-implementation.md](PLAN-pre-release-adr-implementation.md)).

## Archived Plans

- `PLAN-scaffold-plugin-project` - Scaffold Plugin Project ([archive/PLAN-scaffold-plugin-project.md](archive/PLAN-scaffold-plugin-project.md)), archived as of 2026-05-15.

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
