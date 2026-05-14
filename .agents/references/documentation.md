# Documentation Guide

Use this guide when adding or updating repository documentation.

## Owners

- `README.md`: user-facing project description, setup, usage, supported IDE versions, limitations, and sandbox instructions after implementation exists.
- `TASKS.md`: backlog, implementation decisions still pending, and validation tasks.
- `OPEN_QUESTIONS.md`: missing user input and unresolved product or implementation choices.
- `AGENTS.md`: short AI entry point, guidance map, priority order, and high-level workflow rules.
- `docs/WORKING_WITH_AI.md`: human-facing guide for asking AI agents to work on the repository.
- `docs/DEVELOPMENT_LIFECYCLE.md`: repository development lifecycle for larger changes.
- `docs/decisions/`: project decisions, repository rule changes, and ADR template.
- `.agents/references/`: focused AI guidance for planning, execution, code style, testing, reviews, and documentation.
- `.agents/plans/`: task-specific implementation plans and plan template.

## Rules

- Keep docs proportional to the repo's current size.
- Do not copy Spring, REST, OpenAPI, release, deployment, operations, benchmark, or frontend guidance from other repositories.
- Do not imply plugin implementation has started until Gradle, Kotlin, or IntelliJ plugin scaffold files exist.
- Prefer concrete commands and artifact names over generic process language.
- Give open questions stable IDs in `OPEN_QUESTIONS.md`; mark blocked `TASKS.md` items with `depends on: Q-ID`, and tasks that answer questions with `resolves: Q-ID`.
- Record every repository rule change in `docs/decisions/` as a new or superseding ADR before or alongside the rule edit.
- Record every project decision in `docs/decisions/` as an ADR before or alongside the implementation it affects.
- Update documentation before or alongside behavior changes that affect users, validation, supported IDEs, or AI agent workflow.

## When To Add ADRs

Add decision records for every project decision, including:

- Minimum supported IntelliJ Platform version.
- Runtime-discovered AI Assistant action versus direct API dependency.
- Separate `AI Commit & Push All` button versus reusing an existing push-selected state.
- Repository rule or workflow changes.

Use the existing ADR structure for project decisions and repository rule changes.
