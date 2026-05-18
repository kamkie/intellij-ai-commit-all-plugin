---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T12:52:11+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Add Repository Local Agent Skills

## Context and Problem Statement

ADR 0001 intentionally excluded `.agents/skills/` while the repository was small and before the agent-skills ecosystem was relevant to the project. The repository now has enough repeated IntelliJ Platform, Kotlin, and test-driven bug-fix work that compact repo-local skills can improve consistency without loading every guidance file for every task.

Should the repository add project-scoped agent skills for IntelliJ plugin development, Kotlin style, and TDD-focused plugin testing?

## Decision Drivers

* Preserve ADR 0001's lightweight guidance model while allowing a narrowly scoped exception for skills.
* Keep `AGENTS.md` small and avoid loading all AI instruction files automatically.
* Capture project-specific IntelliJ Platform, Kotlin, and testing workflows in reusable, on-demand artifacts.
* Use the repository's actual validation commands rather than generic upstream skill commands.
* Keep durable repository rules in `.agents/references/`; use skills as task-specific workflow accelerators.

## Considered Options

* Add narrow repository-local skills.
* Keep only `.agents/references/`.
* Import broad published skills verbatim.

## Decision Outcome

Chosen option: "Add narrow repository-local skills", because it keeps context loading progressive while documenting repeated project-specific workflows in a discoverable format supported by modern agent tooling.

Add `.agents/skills/` with these initial skills:

- `intellij-plugin-development`: use when changing IntelliJ Platform plugin code, Gradle plugin configuration, `plugin.xml`, actions, services, VCS integration, threading, dumb-mode behavior, sandbox runs, or plugin compatibility checks.
- `kotlin-plugin-style`: use when writing or reviewing Kotlin in this plugin, especially IntelliJ Platform service/action code, nullable platform APIs, small compatibility boundaries, and existing package conventions.
- `plugin-test-tdd`: use when writing tests or fixing bugs in TDD style, especially when a regression test should fail before the production fix.

Keep each skill concise:

- The required `SKILL.md` is the source of trigger metadata and essential workflow guidance.
- Optional `agents/openai.yaml` may be generated for UI metadata.
- Do not add scripts, references, or assets unless a concrete repeated need appears.
- Do not duplicate long guidance already owned by `.agents/references/`; instead point agents to the specific reference file to read when relevant.
- Do not import broad published skills verbatim. Adapt only the useful workflow patterns to this repository.

### Consequences

* Good, because future agents can load IntelliJ/Kotlin/testing workflow guidance only when a matching task appears.
* Good, because skills can point to official IntelliJ Platform, Kotlin, and JUnit guidance while still using local commands.
* Good, because this supersedes ADR 0001's `.agents/skills/` exclusion only for the narrow project-local skills listed here.
* Bad, because repo-local skills add another guidance surface that must stay consistent with `.agents/references/`.
* Bad, because stale trigger descriptions could make agents select the wrong skill.

### Confirmation

Compliance is confirmed by reviewing `.agents/skills/*/SKILL.md`, validating skill metadata with the skill creation validator, and running `scripts/validate-docs.ps1` after adding or changing skill artifacts.

## Pros and Cons of the Options

### Add narrow repository-local skills

* Good, because it keeps `AGENTS.md` compact and uses progressive disclosure.
* Good, because it gives IntelliJ/Kotlin/TDD work a focused starting point without importing unrelated Spring, REST, operations, or frontend guidance.
* Good, because it aligns with JetBrains and Codex skill discovery layouts.
* Bad, because skills and reference guides can drift unless changes are reviewed together.

### Keep only `.agents/references/`

* Good, because the current guidance surface stays simple.
* Good, because ADR 0001 remains unchanged.
* Bad, because repeated IntelliJ/Kotlin/testing tasks still require agents to rediscover which reference files and external guidelines apply.
* Bad, because all task-specific invocation hints remain implicit in user prompts instead of discoverable skill metadata.

### Import broad published skills verbatim

* Good, because public skills contain useful patterns and examples.
* Bad, because broad skills often assume different build tools, frameworks, commands, or repository lifecycle rules.
* Bad, because importing them directly would conflict with ADR 0001's requirement to avoid unrelated guidance.

## More Information

- ADR 0001: [Import Lightweight AI Guidance Model](adr-0001-import-lightweight-ai-guidance-model.md)
- JetBrains supports shared project skills under `.agents/skills/<skill-name>/SKILL.md`.
- Codex supports project-scoped skills and `SKILL.md` metadata-based discovery.
