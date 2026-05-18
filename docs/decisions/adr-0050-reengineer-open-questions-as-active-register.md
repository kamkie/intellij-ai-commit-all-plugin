---
status: accepted
date: 2026-05-17
accepted_at: 2026-05-17T21:18:25+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Reengineer Open Questions As Active Register

## Context and Problem Statement

ADR 0035 moved unresolved user input from the repository root into `docs/decisions/OPEN_QUESTIONS.md` and kept it separate from numbered ADRs.

The file currently contains only a short empty-state message. That makes it lightweight, but it does not define a durable shape for future questions, blockers, evidence paths, or validation.

This decision defines whether to retire the file or reengineer it before more question-tracking drift appears.

## Decision Drivers

* Keep unresolved user input easy to find before implementation starts.
* Preserve ADR 0035's distinction between open questions and accepted decisions.
* Avoid broken links across agent guidance, lifecycle docs, ADRs, proposals, and historical references.
* Make the empty state explicit without treating it as a backlog.
* Give future questions stable IDs, blocker links, and update rules that validation can check.

## Considered Options

* Reengineer `docs/decisions/OPEN_QUESTIONS.md` as an active question register
* Retire `docs/decisions/OPEN_QUESTIONS.md` and move open questions into `docs/decisions/README.md`
* Retire `docs/decisions/OPEN_QUESTIONS.md` and require proposed ADRs for every missing input item
* Keep the current minimal empty-state file

## Decision Outcome

Chosen option: "Reengineer `docs/decisions/OPEN_QUESTIONS.md` as an active question register", because unresolved input still needs a focused owner, but the file should carry enough structure to make future blockers auditable and automatically validated.

Under this decision, `docs/decisions/OPEN_QUESTIONS.md` remains the repository owner for unresolved user input that blocks an ADR, plan, proposal finding, task, implementation branch, or release decision.

The file should contain:

* A short purpose statement.
* A `## Active Questions` section.
* Either `_No open questions._` or a table of active questions.
* A `## Editing Rules` section that explains ID, blocker, answer, and removal behavior.

Active question rows should include:

* `ID` using `Q-<AREA>-NNN`, for example `Q-UX-001`.
* `Question` as the missing input.
* `Blocks` with at least one evidence path such as an ADR, plan, proposal finding, task, or file.
* `Needed For` as a short reason the answer is required.
* `Updated` as `YYYY-MM-DD`.

Resolved questions should not remain as historical rows in `OPEN_QUESTIONS.md`. When a question is answered, record the answer in the ADR, plan, proposal, task, or implementation artifact that uses it, then remove the active row. If there are no rows left, restore `_No open questions._`.

### Consequences

* Good, because agents retain one focused place to check blockers.
* Good, because empty state remains explicit and cheap to maintain.
* Good, because future questions have stable IDs and visible blocker evidence.
* Good, because the file can stay separate from ADR implementation and proposal implementation trackers.
* Bad, because adding or resolving questions requires one more small table update.
* Bad, because historical question wording is not preserved in this file after resolution.

### Confirmation

Compliance should be checked by documentation validation and review:

* `scripts/validate-docs.ps1` should validate the `OPEN_QUESTIONS.md` structure.
* Question IDs should match `Q-<AREA>-NNN` and be unique.
* The file should contain either `_No open questions._` or active question table rows, not both.
* Active rows should include non-empty `Question`, `Blocks`, `Needed For`, and valid `Updated` date fields.
* Resolved-question answers should be reviewed in the artifact that consumes the answer.

## Pros and Cons of the Options

### Reengineer `docs/decisions/OPEN_QUESTIONS.md` as an active question register

* Good, because it keeps the focused owner chosen by ADR 0035.
* Good, because structured rows make blockers easier to audit.
* Good, because validation can prevent ambiguous question entries.
* Bad, because this preserves a file that is often empty.

### Retire `docs/decisions/OPEN_QUESTIONS.md` and move open questions into `docs/decisions/README.md`

* Good, because the decision README would contain all decision-adjacent tracking.
* Bad, because ADR 0035 already rejected merging open questions into the ADR README.
* Bad, because unresolved input would compete with the ADR index and ADR implementation tracker.

### Retire `docs/decisions/OPEN_QUESTIONS.md` and require proposed ADRs for every missing input item

* Good, because every unresolved decision-shaped question would have a durable artifact.
* Bad, because not every missing input item is ready to be an ADR.
* Bad, because temporary blockers would create noisy proposed ADRs.

### Keep the current minimal empty-state file

* Good, because it requires no immediate maintenance.
* Bad, because the next question can reintroduce ad hoc formatting.
* Bad, because validation cannot distinguish a useful blocker entry from ambiguous prose.

## More Information

- Source task: `TASKS.md` `T-IDEA-005`.
- Supersedes the operational details of ADR 0035 while preserving its owner-file decision.
- Related artifact lookup guidance: ADR 0044.
- Implementation evidence: `docs/decisions/OPEN_QUESTIONS.md`, `docs/decisions/README.md`, `scripts/validate-docs.ps1`, `.agents/references/documentation.md`, and `TASKS.md`.
