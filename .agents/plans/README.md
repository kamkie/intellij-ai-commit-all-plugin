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

## Lifecycle

- Draft: the plan is being shaped and may contain unanswered questions.
- Accepted: the plan is approved or clear enough to implement under `.agents/references/planning.md`.
- Implemented: the code/docs have been changed and validated.
- Superseded: another plan or decision record replaced it.

Update the status in the plan file instead of leaving stale instructions.

Keep `Plan-ID` stable when the plan title, filename, status, or wording changes. If a plan is renamed, preserve the `Plan-ID` in the filename. If a plan is split, keep the original ID for the closest surviving plan and assign new meaningful IDs to new plans.
