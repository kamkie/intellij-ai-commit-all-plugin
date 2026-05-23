# Proposal Consolidation

Review or edit proposal documents so the active proposal set has a clear source of truth, refs, consistent trackers, and accurate supersession notes.
Use this when proposals are duplicated, partially superseded, missing refs, or need maintainer decisions reflected after explicit user triage.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `docs/proposals/README.md`
- `docs/proposals/PROPOSAL_TEMPLATE.md`
- `.agents/prompts/README.md`
- this prompt
- the specific proposal files, proposal refs, or finding refs named by the user

Load ADRs, plans, tasks, archives, source files, or old proposals only when a named proposal or finding references them.

## Output

Produce a proposal-consolidation report or, when the user asks for edits, update only the owning proposal artifacts.

Report:

- active proposal source of truth for the requested topic
- duplicate, stale, or superseded proposal content
- proposal ref, filename, front matter, and index issues
- progress tracker consistency with per-finding metadata
- accepted findings whose non-terminal implementation status needs README summary evidence
- proposed edits by exact artifact
- validation commands required after edits

When editing:

- preserve proposal refs and finding refs
- keep `Decision` and `Decision at` empty unless the user explicitly supplies a decision
- update `Decision at` only when setting a non-empty decision
- update `docs/proposals/README.md` when index or implementation summary status changes

## Non-Goals

- Do not implement proposal findings.
- Do not invent proposal decisions, owners, evidence, or product direction.
- Do not create an ADR or plan unless the user separately asks or repository rules require stopping for one.
- Do not merge unrelated proposal topics into one broad document.
