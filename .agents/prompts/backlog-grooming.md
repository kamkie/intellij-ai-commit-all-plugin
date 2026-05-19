# Backlog Grooming

Review backlog, archive, and open-question artifacts for stale, duplicate, blocked, or misplaced work.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `TASKS.md`
- `TASKS_ARCHIVE.md`
- `docs/decisions/OPEN_QUESTIONS.md`
- `.agents/prompts/README.md`
- this prompt

Load related ADRs, proposals, plans, or source files only when a task, archived task, or open question references them.

## Output

Produce a report in the current response unless the user asks for edits.
Group findings as:

- active tasks that appear completed, obsolete, duplicated, blocked, underspecified, or missing stable IDs
- archive entries that should stay archived, need correction, or conflict with active tasks
- open questions that are answered, stale, missing blockers, or missing owner evidence
- tasks that should link to ADRs, proposals, plans, validation evidence, or open questions
- suggested edits with the exact owning artifact and validation command

If the user asks to edit, keep changes mechanical and owner-specific: preserve task IDs, do not renumber historical entries, and do not invent product decisions.

## Non-Goals

- Do not implement backlog tasks.
- Do not close open questions unless the answer is explicit in accepted artifacts or the user directly supplies the answer.
- Do not archive active work without clear evidence that it is completed, superseded, rejected, or no longer needed.
- Do not create new product direction without an ADR, proposal, plan, or explicit user instruction.
