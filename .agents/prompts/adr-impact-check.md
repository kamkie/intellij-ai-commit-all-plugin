# ADR Impact Check

Decide whether a requested change needs an ADR, plan, task update, open question, or documentation update before implementation starts.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `docs/decisions/README.md`
- `.agents/references/planning.md`
- `.agents/prompts/README.md`
- this prompt
- the user request, task, proposal finding, bug report, or draft change being assessed

Load `TASKS.md`, `TASKS_ARCHIVE.md`, `docs/decisions/OPEN_QUESTIONS.md`, existing ADRs, proposals, or plans only when the requested change references them or the impact check depends on them.

## Output

Return a short governance decision with:

- requested change summary
- affected behavior, repository rule, validation policy, compatibility target, workflow, or documentation owner
- whether an ADR is required, with the specific reason
- whether an implementation plan is required before code or docs change
- whether `TASKS.md`, `TASKS_ARCHIVE.md`, `OPEN_QUESTIONS.md`, proposal trackers, README, support docs, changelog, prompts, skills, or references need updates
- explicit stop or continue recommendation
- minimal validation expected after the eventual change

When an ADR or plan is required, return the stop recommendation from `docs/decisions/README.md` or `.agents/references/planning.md`.

## Non-Goals

- Do not implement the assessed change from this prompt.
- Do not create broad repository audits unless the user asks for one.
- Do not treat routine typo fixes, local wording cleanup, or non-policy prompt edits as ADR-worthy unless they change future workflow rules.
- Do not bypass accepted ADRs, approved plans, or current user instructions.
