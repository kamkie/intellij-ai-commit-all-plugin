# Compact AI Guidance

Compact standing AI instruction files without changing the current policy.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- the specific guidance file or prompt the user asks to compact

## Output

Look for duplicate or overlapping rules, guidance in the wrong owner file, stale references, broken anchors, verbose examples, and accumulated history such as dated decisions or "previously/now" migration prose.
Keep current rules, move guidance to the single best owner, replace duplicates with short cross-references, and do not touch archive content.
If ownership is unclear, flag the item instead of deleting it.

Summarize changed files, guidance moved, references fixed, and flagged-but-unchanged items.

## Non-Goals

- Do not change repository policy, validation requirements, task status, ADR status, or implementation behavior.
- Do not compact archive content unless the user names a specific archived artifact.
- Do not bulk-load all repository guidance unless the user asks for a broad guidance audit.
