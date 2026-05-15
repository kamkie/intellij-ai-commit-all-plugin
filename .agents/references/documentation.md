# Documentation Guide

Use this guide when adding or updating repository documentation.

## Owners

- `README.md`: user-facing project description, setup, usage, supported IDE versions, limitations, and sandbox instructions after implementation exists.
- `TASKS.md`: backlog, implementation decisions still pending, and validation tasks.
- `docs/decisions/OPEN_QUESTIONS.md`: missing user input and unresolved product or implementation choices.
- `CHANGELOG.md`: notable unreleased changes and released history.
- `SUPPORT.md`: support status, supported-scope summary, issue-reporting expectations, and privacy guidance.
- `AGENTS.md`: short AI entry point, guidance map, priority order, and high-level workflow rules.
- `docs/WORKING_WITH_AI.md`: human-facing guide for asking AI agents to work on the repository.
- `docs/DEVELOPMENT_LIFECYCLE.md`: repository development lifecycle for larger changes.
- `docs/proposals/`: repository analysis and proposal documents for maintainer triage; every proposal needs a stable `proposal_id`; start from `docs/proposals/PROPOSAL_TEMPLATE.md` and follow `docs/proposals/README.md`.
- `docs/decisions/`: project decisions, repository rule changes, and ADR template.
- `.agents/references/`: focused AI guidance for planning, execution, code style, testing, reviews, and documentation.
- `.agents/plans/`: task-specific implementation plans and plan template; every active and archived plan needs a stable `Plan-ID` included in the filename.
- `LICENSE`: Apache-2.0 license text for the repository and plugin.
- `.gitmessage`: commit-message template, Conventional Commit type guidance, and AI metadata trailer schema.
- `.agents/references/releases.md`: release preparation, changelog update rules, support-policy checks, version tags, and release preconditions.

## Rules

- Keep docs proportional to the repo's current size.
- Do not copy Spring, REST, OpenAPI, release, deployment, operations, benchmark, or frontend guidance from other repositories.
- Do not imply plugin implementation has started until Gradle, Kotlin, or IntelliJ plugin scaffold files exist.
- Prefer concrete commands and artifact names over generic process language.
- Do not load every AI instruction file automatically. Start from `AGENTS.md`, use the guidance map, and load only the owner documents needed for the current documentation change unless the task is a broad guidance audit or cross-document consistency review.
- Give open questions stable IDs in `docs/decisions/OPEN_QUESTIONS.md`.
- Give every `TASKS.md` item a stable task ID in the form `T-AREA-NNN`, keep the ID stable when wording or ordering changes, and do not renumber existing task IDs.
- Give every plan a stable `Plan-ID` in the form `PLAN-<short-kebab-slug>`, include it in active and archived filenames, keep it stable when title, filename, status, or wording changes, and avoid strictly number-based plan IDs.
- Give every proposal a stable `proposal_id` in the form `PROP-<short-kebab-slug>`, include it in active and archived filenames, keep it stable when title, filename, status, wording, or archive location changes, and do not reuse retired proposal IDs.
- Mark blocked `TASKS.md` items with `depends on: Q-ID`, and tasks that answer questions with `resolves: Q-ID`.
- Follow `docs/decisions/README.md` for project decisions and repository rule changes.
- Update documentation before or alongside behavior changes that affect users, validation, supported IDEs, or AI agent workflow.
- Update `CHANGELOG.md` for notable user-facing, contributor-facing, compatibility, support, release, or workflow changes.
- In orchestrated plan execution and release preparation, `CHANGELOG.md` maintenance belongs to the orchestrator; task workers may suggest entries but do not own final changelog edits.
- Update `SUPPORT.md` when supported IDE versions, supported VCS scope, plugin dependency requirements, Marketplace availability, or support channels change.
- Use `docs/proposals/` for analysis documents that list findings, duplications, simplifications, or improvement options for maintainer triage.
- Keep proposals advisory until accepted through ADRs, plans, or tasks.

## When To Add ADRs

See `docs/decisions/README.md` for required ADR topics, including:

- Minimum supported IntelliJ Platform version.
- Runtime-discovered AI Assistant action versus direct API dependency.
- Split-button commit/push presentation versus separate commit-and-push action.
- Repository rule or workflow changes.

Use the existing ADR structure for project decisions and repository rule changes.
