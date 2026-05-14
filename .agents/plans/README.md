# Plans

This directory holds task-specific implementation plans for work that is too large or uncertain to execute directly.

## Use

- Create a plan when work spans multiple files or behavior areas.
- Use `PLAN_TEMPLATE.md` as the starting point.
- Give every plan a stable `Plan-ID` in the form `P-<short-kebab-slug>`, such as `P-scaffold-plugin-project`.
- Do not use a strictly number-based plan ID such as `P-0001`; the ID should carry enough meaning to recognize the plan without its file path.
- Keep plans focused on one task or milestone.
- Link unresolved user input back to `OPEN_QUESTIONS.md`.
- Move accepted project decisions and repository rule changes to `docs/decisions/`.
- Do not start implementation from an accepted plan until every plan question and required decision is answered, decided, or explicitly documented as an allowed assumption.
- For multi-task implementation, prefer an orchestrator with one fresh task worker per named task when the environment supports agent delegation.

## Lifecycle

- Draft: the plan is being shaped and may contain unanswered questions.
- Accepted: the plan is approved or clear enough to implement, with every required question and decision answered, decided, or explicitly documented as an allowed assumption.
- Implemented: the code/docs have been changed and validated.
- Superseded: another plan or decision record replaced it.

Update the status in the plan file instead of leaving stale instructions.

Keep `Plan-ID` stable when the plan title, filename, status, or wording changes. If a plan is split, keep the original ID for the closest surviving plan and assign new meaningful IDs to new plans.
