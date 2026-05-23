# Documentation Guide

Use this guide when adding or updating repository documentation.

## Owners

- `README.md`: concise user-facing landing page with product purpose, prerelease status, requirements, install path, quickstart, key settings, user-visible limitations, and links to deeper docs.
- `docs/user-guide.md`: task-oriented user manual for `AI`, `Commit`, `Push`, shortcuts, settings, changelist and staging-area behavior, and user-level safe push behavior.
- `docs/troubleshooting.md`: user-facing troubleshooting and FAQ owner for AI Assistant availability, AI timeout, hidden or disabled controls, push fallback, outgoing-only push stops, conflicts, background VCS operations, and report evidence.
- `docs/specification.md`: validation contract for intended observable plugin behavior, requirement refs, ADR sources, and validation links. Keep implementation classes, test-helper mechanics, and current code structure out unless they are observable compatibility policy.
- `docs/validation/` and `docs/scenario-coverage.md`: validation evidence owners. Record how behavior was checked, not product explanation.
- `TASKS.md`: active backlog, implementation decisions still pending, and validation tasks.
- `TASKS_ARCHIVE.md`: completed task history after work is finished, validated, and self-reviewed.
- `docs/decisions/OPEN_QUESTIONS.md`: missing user input and unresolved product or implementation choices.
- `CHANGELOG.md`: public release-note history for plugin behavior, public plugin docs, compatibility, support, and release pipeline changes.
- `docs/SUPPORT.md`: GitHub-recognized support resource with support status, supported-scope summary, issue-reporting expectations, privacy guidance, and links to troubleshooting without duplicating the full FAQ.
- `AGENTS.md`: short AI entry point, guidance map, priority order, and high-level workflow rules.
- `docs/WORKING_WITH_AI.md`: human-facing guide for asking AI agents to work on the repository; update it when workflow rules change, but do not treat it as a normal agent read-set owner.
- `docs/DEVELOPMENT_LIFECYCLE.md`: repository development lifecycle for larger changes.
- `docs/proposals/`: repository analysis and proposal documents for maintainer triage; every proposal needs a `proposal_id` ref; start from `docs/proposals/PROPOSAL_TEMPLATE.md` and follow `docs/proposals/README.md`.
- `docs/decisions/`: project decisions, repository rule changes, and ADR template.
- `.agents/references/`: focused AI guidance for planning, execution, code style, testing, reviews, and documentation.
- `.agents/references/troubleshooting.md`: repository-specific validation, Gradle, IntelliJ Plugin, sandbox, test, and IDE log troubleshooting.
- `.agents/plans/`: task-specific implementation plans and plan template; every active and archived plan needs a `Plan-ID` ref included in the filename.
- `.agents/prompts/`: reusable repository prompt recipes for named session starters; `.agents/prompts/README.md` owns the catalog and loading rules.
- `scripts/ai/validate-agent-artifacts.ps1`: validates `.agents/references/`, `.agents/skills/`, `.agents/prompts/`, `.agents/plans/`, catalog links, plan metadata, and concrete `.agents/...` file references.
- `LICENSE`: Apache-2.0 license text for the repository and plugin.
- `.gitmessage`: commit-message template, Conventional Commit type guidance, and AI metadata trailer schema.
- `.agents/references/releases.md`: release preparation, changelog update rules, support-policy checks, version tags, and release preconditions.

## Rules

- Keep docs proportional to the repo's current size.
- Do not copy Spring, REST, OpenAPI, release, deployment, operations, benchmark, or frontend guidance from other repositories.
- Keep implementation-status wording aligned with `README.md` and `TASKS.md`.
- Product and user documentation under `docs/` uses lowercase kebab-case, for example `docs/user-guide.md`, `docs/troubleshooting.md`, `docs/specification.md`, `docs/scenario-coverage.md`, and `docs/validation/manual-sandbox.md`.
- Directory index files use `README.md`.
- GitHub-recognized community health files keep canonical uppercase names even under `docs/`, for example `docs/SUPPORT.md`.
- Human and process governance documents keep their existing uppercase phrase names, for example `docs/WORKING_WITH_AI.md` and `docs/DEVELOPMENT_LIFECYCLE.md`.
- Registers, templates, ADRs, proposals, and active artifact naming rules keep their existing required formats.
- Assets and concept files use lowercase kebab-case, with numeric prefixes allowed for ordered variants.
- Prefer concrete commands and artifact names over generic process language.
- Do not load every AI instruction file automatically. Start from `AGENTS.md`, use the guidance map, and load only the owner documents needed for the current documentation change unless the task is a broad guidance audit or cross-document consistency review.
- Do not read `docs/WORKING_WITH_AI.md` as part of normal AI-agent workflow. It is for humans preparing requests, and should be updated only when rules, request shapes, or human-facing expectations for AI work change.
- Route artifact refs by filename prefix before broad search: `adr-NNNN` to `docs/decisions/`, `PLAN-<slug>` to `.agents/plans/` then `.agents/plans/archive/`, `PROP-<slug>` to `docs/proposals/` then `docs/proposals/archive/`, and `T-<AREA>-NNN` to `TASKS.md` then `TASKS_ARCHIVE.md`.
- Give open questions `Q-<AREA>-NNN` refs in `docs/decisions/OPEN_QUESTIONS.md`, for example `Q-UX-001`.
- Give every `TASKS.md` and `TASKS_ARCHIVE.md` item a `T-AREA-NNN` task ref, keep the ref stable when wording or ordering changes, and do not renumber existing task refs.
- Do not duplicate Markdown headings within `TASKS.md` or within `TASKS_ARCHIVE.md`; merge task rows under the existing matching heading, and preserve date, release, plan, or domain context as text inside that section.
- Give every plan a `Plan-ID` ref in the form `PLAN-<short-kebab-slug>`, include it in active and archived filenames, keep it stable when title, filename, status, or wording changes, and avoid strictly number-based plan refs.
- Give every plan `Workers:` metadata and an `## Execution Graph` section with a fenced Mermaid graph.
- Keep `.agents/prompts/` for narrow, reusable prompt recipes that are more concrete than `.agents/references/` guidance and not substantial enough to become `.agents/skills/`.
- Load repository prompts in two stages: identify the prompt from `.agents/prompts/README.md` by exact title, filename, or catalog entry, then load only the matching prompt and its declared read set.
- Keep each prompt's read set small, state expected output, and name explicit non-goals.
- Keep `.agents/skills/*/SKILL.md` front matter name in sync with the directory name, and include a `## Start` section for the first read set or startup workflow.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1` when adding or changing repository references, skills, prompts, plans, or concrete `.agents/...` links; it also runs through `scripts/validate-docs.ps1`.
- Do not use `.agents/prompts/` for active backlog items, implementation sequencing, durable policy, or executable workflow accelerators; use `TASKS.md`, `.agents/plans/`, `.agents/references/`, or `.agents/skills/` respectively.
- Give every proposal a `proposal_id` ref in the form `PROP-<short-kebab-slug>`, include it in active and archived filenames, keep it stable when title, filename, status, wording, or archive location changes, and do not reuse retired proposal refs.
- Use three-digit proposal finding refs such as `F001`, `E001`, `D001`, and `S001` for active proposal findings; archived proposals may keep historical refs unless materially updated.
- Keep new proposal finding decisions empty until maintainer triage. When a finding decision becomes non-empty, set the finding metadata table's `Decision at` field to an ISO 8601 timestamp with timezone offset.
- Mark blocked `TASKS.md` items with `depends on: Q-<AREA>-NNN`, and tasks that answer questions with `resolves: Q-<AREA>-NNN`.
- Follow `docs/decisions/README.md` for ADR requirements, project decisions, and repository rule changes.
- Update documentation before or alongside behavior changes that affect users, validation, supported IDEs, or AI agent workflow.
- Update `CHANGELOG.md` only for notable public plugin-facing changes: plugin source or runtime behavior, public plugin documentation, compatibility, support, security or privacy behavior, or CI and release pipeline behavior that affects the plugin artifact or publication.
- Do not update `CHANGELOG.md` for AI-agent documentation, `.agents/` skills or references, plans, proposals, ADR maintenance, scenario-coverage or test-case inventories, manual validation logs, or internal repository workflow changes unless they also change public plugin behavior, public docs, support promises, or release artifacts.
- In orchestrated plan execution and release preparation, `CHANGELOG.md` maintenance belongs to the orchestrator; task workers may suggest entries but do not own final changelog edits.
- Update `docs/SUPPORT.md` when supported IDE versions, supported VCS scope, plugin dependency requirements, Marketplace availability, or support channels change.
- Use `docs/proposals/` for analysis documents that list findings, duplications, simplifications, or improvement options for maintainer triage.
- Keep proposals advisory until accepted through ADRs, plans, or tasks.
