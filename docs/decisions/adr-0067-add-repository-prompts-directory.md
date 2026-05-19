---
status: accepted
date: 2026-05-19
accepted_at: 2026-05-19T22:50:49+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Future maintainers and AI agents
---

# Add Repository Prompts Directory

## Context and Problem Statement

The repository has durable AI guidance in `.agents/references/`, task-specific implementation plans in `.agents/plans/`, reusable agent skills in `.agents/skills/`, and active backlog items in `TASKS.md`. Another local repository uses `.agents/tasks/` for reusable prompt recipes, but this repository already gives "task" a specific meaning through `TASKS.md` and stable `T-<AREA>-NNN` task IDs.

Should this repository add a separate home for narrow reusable prompt recipes, and if so should it use `.agents/prompts/` instead of `.agents/tasks/`?

## Decision Drivers

* Avoid confusing reusable prompt recipes with active backlog tasks in `TASKS.md`.
* Preserve ADR 0001's lightweight guidance model and progressive context loading.
* Keep durable policy in `.agents/references/`, implementation plans in `.agents/plans/`, and executable workflow accelerators in `.agents/skills/`.
* Provide a discoverable home for named session starters that are more concrete than reference guides but too small or ad hoc to justify a skill.
* Avoid importing another repository's prompt corpus verbatim.

## Considered Options

* Add `.agents/prompts/` for repository prompt recipes.
* Add `.agents/tasks/` for repository task prompts.
* Keep only `.agents/references/`, `.agents/plans/`, and `.agents/skills/`.
* Put prompt recipes into `.agents/references/` or `.agents/skills/`.

## Decision Outcome

Chosen option: "Add `.agents/prompts/` for repository prompt recipes", because `.agents/prompts/` names the artifact by what it is and avoids colliding with `TASKS.md`, task IDs, and plan execution language.

When accepted, implementation should:

* Create `.agents/prompts/README.md` as the catalog and owner for repository prompt rules.
* Use prompt files only for named, repository-specific session starters that are more concrete than `.agents/references/` guidance and not substantial enough to become `.agents/skills/`.
* Keep prompt loading two-stage: identify a prompt by exact title, filename, or catalog entry, then load only the matching prompt and its declared read set.
* Require each prompt to declare a small read set, expected output shape, and any explicit non-goals.
* Keep backlog items in `TASKS.md`, implementation sequencing in `.agents/plans/`, durable policy in `.agents/references/`, and repeatable executable workflows in `.agents/skills/`.
* Add only prompts with repeated near-term value, such as `toolchain-upgrade.md` or `evaluate-ai-guidance.md`, after adapting them to this repository's artifact names and validation rules.
* Update `AGENTS.md` and `.agents/references/documentation.md` only after acceptance so the new artifact type is discoverable.
* Do not update `CHANGELOG.md`, because repository prompts are internal AI-agent workflow guidance and do not affect public plugin behavior, public docs, support promises, or release artifacts.

### Consequences

* Good, because prompt recipes get a precise name and do not overload "task".
* Good, because prompt recipes can improve repeatable repository sessions without turning every session starter into a skill.
* Good, because `AGENTS.md` can remain compact and point to the catalog only when prompt lookup is relevant.
* Bad, because `.agents/prompts/` adds another guidance surface that must stay aligned with references, skills, plans, and task rules.
* Bad, because weak prompt boundaries could become stale mini-guides unless the catalog enforces narrow ownership.

### Confirmation

After acceptance and implementation, confirm with:

* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1`
* `git diff --check`
* Manual review that `AGENTS.md`, `.agents/references/documentation.md`, and `.agents/prompts/README.md` agree on prompt ownership and lookup rules.

## Pros and Cons of the Options

### Add `.agents/prompts/` For Repository Prompt Recipes

* Good, because the directory name describes reusable prompt recipes directly.
* Good, because it avoids ambiguity with `TASKS.md`, `TASKS_ARCHIVE.md`, and `T-<AREA>-NNN` lookup rules.
* Good, because it gives agents a narrow place to look for named session starters without broad-loading all guidance files.
* Neutral, because this is a new repository artifact type and needs routing guidance in `AGENTS.md` and documentation ownership rules.
* Bad, because prompt recipes can drift if they duplicate reference or skill guidance instead of pointing to it.

### Add `.agents/tasks/` For Repository Task Prompts

* Good, because it matches the source repository's existing naming pattern.
* Good, because "task prompt" is understandable when read in isolation.
* Bad, because "task" already means active backlog item and stable task ID in this repository.
* Bad, because it makes artifact lookup and user requests such as "look at tasks" less precise.

### Keep Only Existing Agent Guidance Artifacts

* Good, because the current guidance model remains simpler.
* Good, because `.agents/references/`, `.agents/plans/`, and `.agents/skills/` already cover most repeated work.
* Bad, because small repeatable session prompts either remain informal in chat history or get forced into heavier artifacts.
* Bad, because future agents may rediscover the same setup for periodic guidance audits, toolchain scans, or docs-only sessions.

### Put Prompt Recipes Into `.agents/references/` Or `.agents/skills/`

* Good, because it avoids adding another directory.
* Good, because existing discovery paths remain enough.
* Bad, because `.agents/references/` should own durable policy, not one-off task recipes.
* Bad, because `.agents/skills/` should own reusable workflows with clear trigger metadata, not every narrow session prompt.
* Bad, because mixing prompt recipes into those owners weakens the current artifact boundaries.

## More Information

- ADR 0001: [Import Lightweight AI Guidance Model](adr-0001-import-lightweight-ai-guidance-model.md)
- ADR 0062: [Add Repository Local Agent Skills](adr-0062-add-repository-local-agent-skills.md)
- `TASKS.md` and `TASKS_ARCHIVE.md` are the task backlog and task history homes; `.agents/prompts/` would not replace them.
- Source discussion: review of `D:\Projects\Jit\technical-interview-demo\.agents\tasks` and the follow-up suggestion to use `.agents/prompts` to avoid a naming clash with `TASKS.md`.
