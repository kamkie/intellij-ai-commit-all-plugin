# Plans

This directory holds task-specific implementation plans for work that is too large or uncertain to execute directly.

## Use

- Create a plan when work spans multiple files or behavior areas.
- Use `PLAN_TEMPLATE.md` as the starting point.
- Give every plan a stable `Plan-ID` in the form `P-<short-kebab-slug>`, such as `P-scaffold-plugin-project`.
- Include the stable `Plan-ID` in the plan filename, such as `P-scaffold-plugin-project.md`.
- Do not use a strictly number-based plan ID such as `P-0001`; the ID should carry enough meaning to recognize the plan without its file path.
- Keep plans focused on one task or milestone.
- Link unresolved user input back to `docs/decisions/OPEN_QUESTIONS.md`, and move accepted project decisions or repository rule changes to `docs/decisions/`.
- Follow `.agents/references/planning.md` and `.agents/references/execution.md` for plan readiness, per-task commits, and orchestrator or task-worker execution.
- Move closed plans to `archive/` only after the plan no longer needs active execution or release-preparation updates. Preserve the `Plan-ID`, filename, and close reason.

## Active Plans

_none yet_

## Archived Plans

- `P-scaffold-plugin-project` - Scaffold Plugin Project ([archive/P-scaffold-plugin-project.md](archive/P-scaffold-plugin-project.md)), archived as of 2026-05-15.

## Lifecycle

Use the smallest status set that preserves traceability:

- Draft: the plan is being shaped and may contain unanswered questions.
- Approved: the plan is ready to implement; all open questions and required decisions are answered, explicitly decided, moved to an owner document, or documented as allowed assumptions.
- In Progress: implementation has started.
- Blocked: implementation cannot proceed; link the blocker in `## Readiness`, `## Open Questions`, or the relevant owner document.
- Implemented: planned changes are complete and task validation is done; release workflow may still remain.
- Closed: no further plan work is expected; include `Close-Reason: Released`, `Rejected`, `Superseded`, `Deferred`, or `Archived`.

`Accepted` and `Implementing` are not plan statuses; use `Approved` and `In Progress`. Use `Deferred`, not `Defered`.

Update the status in the plan file instead of leaving stale instructions.

Every plan must keep a short `## Readiness` section near the top with:

- Plan readiness.
- Open questions.
- Implementation progress.

Keep `Plan-ID` stable when the plan title, filename, status, or wording changes. If a plan is renamed, preserve the `Plan-ID` in the filename. If a plan is split, keep the original ID for the closest surviving plan and assign new meaningful IDs to new plans.
