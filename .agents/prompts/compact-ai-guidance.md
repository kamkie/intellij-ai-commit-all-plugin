# Compact AI Guidance

Compact live AI guidance without changing current policy, artifact gates, validation requirements, task status, or implementation behavior.
Use this prompt when standing instructions need deduplication, owner cleanup, stale-reference repair, or context-load reduction.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `.agents/prompts/README.md`
- this prompt

Target selection:

- Treat this prompt's own title or filename as prompt invocation, not as the target to compact.
- If the user names one or more target guidance files or prompts after invoking this prompt, read only those target artifacts and their owner guides.
- If the user invokes this prompt without a target file, inspect live AI guidance:
    - `AGENTS.md`
    - `.agents/references/*.md`
    - `.agents/prompts/README.md`
    - `.agents/prompts/*.md`
    - `.agents/skills/*/SKILL.md`
    - `docs/DEVELOPMENT_LIFECYCLE.md`
  - `docs/WORKING_WITH_AI.md`
- Do not inspect archive content unless the user names a specific archived artifact.

## Compacting Rules

- Preserve the current rule before shortening it. When rules conflict, identify the governing owner before editing.
- Keep each rule in one best owner:
    - `AGENTS.md` for the entry point, guidance map, priority order, and high-level working rules.
    - `.agents/references/` for durable AI workflow guidance.
    - `.agents/prompts/` for narrow session recipes.
    - `.agents/skills/` for reusable task workflows with startup instructions.
    - `docs/WORKING_WITH_AI.md` for human request guidance, not AI execution rules.
    - `docs/DEVELOPMENT_LIFECYCLE.md` for lifecycle summary and contributor-facing development flow.
- Move misplaced guidance to its owner. Leave a short cross-reference only when normal reading order would otherwise lose needed context.
- Remove duplicate rules, overlapping examples, dated decision history, "previously/now" migration prose, and verbose examples that do not clarify current behavior.
- Repair stale filenames, outdated artifact references, and broken Markdown links or anchors.
- Remove AI-facing execution instructions from human-facing docs; keep human-facing request examples and concise pointers to the owning AI guidance when useful.
- Do not compact archive content unless the user explicitly names it.
- If ownership or current policy is unclear, leave the item unchanged and flag it in the result.

## Output

Make the compacting edits unless the user asks for report-only output.

Summarize:

- changed files
- guidance moved, including source and destination owner
- duplicate, stale, or historical prose removed
- references or anchors fixed
- AI-facing instructions removed from human-facing docs
- flagged-but-unchanged items and why they were left in place
- validation run and results

## Non-Goals

- Do not change repository policy, validation requirements, task status, ADR status, or implementation behavior.
- Do not compact archive content unless the user names a specific archived artifact.
- Do not compact non-AI repository guidance unless needed to fix a concrete reference from the AI guidance scope.
- Do not bulk-load all repository guidance for unrelated tasks; a no-target invocation of this prompt is the broad AI-guidance compaction request.
