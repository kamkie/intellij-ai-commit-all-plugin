# Documentation Guide

Use this guide when adding or updating repository documentation.

## Owners

- `README.md`: user-facing project description, setup, usage, supported IDE versions, limitations, and sandbox instructions after implementation exists.
- `TASKS.md`: backlog, implementation decisions still pending, and validation tasks.
- `AGENTS.md`: short AI entry point, project intent, priority order, and high-level rules.
- `docs/WORKING_WITH_AI.md`: human-facing guide for asking AI agents to work on the repository.
- `.agents/references/`: focused AI guidance for execution, code style, testing, reviews, and documentation.

## Rules

- Keep docs proportional to the repo's current size.
- Do not copy Spring, REST, OpenAPI, release, deployment, operations, benchmark, or frontend guidance from other repositories.
- Do not imply plugin implementation has started until Gradle, Kotlin, or IntelliJ plugin scaffold files exist.
- Prefer concrete commands and artifact names over generic process language.
- Update documentation before or alongside behavior changes that affect users, validation, supported IDEs, or AI agent workflow.

## When To Add ADRs

Add decision records only for durable project choices, such as:

- Minimum supported IntelliJ Platform version.
- Runtime-discovered AI Assistant action versus direct API dependency.
- Separate `AI Commit & Push All` button versus reusing an existing push-selected state.

Do not add ADR structure until a durable decision needs to be recorded.
