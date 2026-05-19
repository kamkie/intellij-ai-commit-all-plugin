# Repository Prompts

`.agents/prompts/README.md` owns the catalog, loading mechanism, and maintenance rules for reusable repository prompt recipes.
Use these prompts only for named repository-specific session starters that are more concrete than `.agents/references/` guidance and not substantial enough to become `.agents/skills/`.

## Loading Mechanism

Use repository prompts through a two-stage load:

1. Identify the requested prompt by exact title, filename, or catalog entry.
2. Load only the matching prompt, then follow that prompt's declared read set.

Rules:

- do not bulk-load `.agents/prompts/` to discover intent
- use this catalog or a targeted search only when the requested prompt name is ambiguous
- if more than one prompt matches, ask which prompt to run unless the requested outcome clearly selects one
- treat a prompt's `Read first` section as the prompt-local initial context
- load extra references, docs, plans, reports, or skills only when the prompt or current request gives a concrete trigger
- prompts can narrow or shape context for a session, but they do not override the current user request, `AGENTS.md`, accepted ADRs, approved plans, or executable contract artifacts

## Rules

- keep prompts narrow, single-purpose, and self-contained
- name the smallest useful read set; avoid broad repository scans
- bound outputs by naming the expected report, summary, plan, or artifact location
- keep durable policy in `.agents/references/`, not prompts
- keep implementation sequencing in `.agents/plans/`, not prompts
- keep active backlog and stable task IDs in `TASKS.md` and `TASKS_ARCHIVE.md`, not prompts
- keep executable or strongly repeatable workflows in `.agents/skills/`, not prompts
- do not add metadata preambles; keep catalog metadata here
- update this README when adding, renaming, moving, or removing prompts

## Current Prompts

| Prompt                                          | Use When                                                                                                                                                     |
|-------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Compact AI Guidance](compact-ai-guidance.md)   | Standing AI instruction files need duplicate, stale, or misplaced guidance compacted without changing current policy.                                        |
| [Evaluate AI Guidance](evaluate-ai-guidance.md) | A report-only assessment of repository AI guidance, lifecycle coverage, ownership, duplication, or context-load cost is needed.                              |
| [Toolchain Upgrade](toolchain-upgrade.md)       | CI actions, Java dependencies, Gradle plugins/tools, plugin verifier targets, Gradle wrapper versions, or release tool upgrades need an implementation plan. |
