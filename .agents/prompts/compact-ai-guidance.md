# Compact AI Guidance

Compact standing AI instruction files without changing the current policy.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `.agents/prompts/README.md`
- this prompt
- if the user names one or more guidance files or prompts, only those named artifacts and their owner guides
- if the user does not name a guidance file, all live repository AI guidance:
    - `AGENTS.md`
    - `.agents/references/*.md`
    - `.agents/prompts/README.md`
    - `.agents/prompts/*.md`
    - `.agents/skills/*/SKILL.md`
    - `docs/WORKING_WITH_AI.md`
    - `docs/DEVELOPMENT_LIFECYCLE.md`

## Output

Look for duplicate or overlapping rules, guidance in the wrong owner file, stale references, broken anchors, verbose examples, and accumulated history such as dated decisions or "previously/now" migration prose.
Keep current rules, move guidance to the single best owner, replace duplicates with short cross-references, and do not touch archive content.
If ownership is unclear, flag the item instead of deleting it.

Summarize changed files, guidance moved, references fixed, and flagged-but-unchanged items.

## Non-Goals

- Do not change repository policy, validation requirements, task status, ADR status, or implementation behavior.
- Do not compact archive content unless the user names a specific archived artifact.
- Do not compact non-AI repository guidance unless needed to fix a concrete reference from the AI guidance scope.
- Do not bulk-load all repository guidance for unrelated tasks; a no-target invocation of this prompt is the broad AI-guidance compaction request.
