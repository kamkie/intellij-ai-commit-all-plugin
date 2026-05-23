---
name: repository-documentation
description: Repository documentation workflow for this IntelliJ plugin. Use when adding, updating, reviewing, or triaging README.md, SUPPORT.md, TASKS.md, CHANGELOG.md, docs/decisions/, docs/proposals/, docs/WORKING_WITH_AI.md, docs/DEVELOPMENT_LIFECYCLE.md, .agents/references/, .agents/plans/, .agents/prompts/, .agents/skills/, AGENTS.md, or other governed contributor and AI-agent documentation.
---

# Repository Documentation

## Start

- Read `AGENTS.md` if it is not already loaded.
- Read `.agents/references/documentation.md` before editing governed docs.
- Read the specific owner guide named by `.agents/references/documentation.md` for the artifact being edited.
- Use the smallest owner-specific context set. Do not load every guidance file by default.

## Editing

- Follow `.agents/references/documentation.md` for owner mapping, ADR and plan gates, refs, proposal decision handling, changelog boundaries, and proportionality rules.
- Keep docs proportional and repository-specific. Do not copy generic Spring, REST, OpenAPI, deployment, operations, benchmark, frontend, or unrelated guidance from external repositories.

## Validation

- Run the validation commands required by `.agents/references/documentation.md`.
- Report validation commands, skipped checks with reasons, and any remaining governance risk.
