---
status: proposed
date: 2026-05-23
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Separate User Docs, Intent Specification, And Validation Evidence

## Context and Problem Statement

The current documentation grew during implementation and now mixes several audiences and responsibilities. `README.md` includes user setup, contributor validation, workflow details, and status notes; `docs/specification.md` was created to validate plugin behavior but also contains implementation mechanics; `docs/requested-features.md` duplicates traceability that is already available through ADRs, tasks, and specification refs while also looking like a user-facing feature list; troubleshooting content is scattered across README, support, specification, and validation records.

How should the repository rebuild user-facing and validation documentation so intended behavior, implementation detail, user guidance, and validation evidence each have a clear owner?

## Decision Drivers

* Keep user-facing documentation understandable without requiring ADR, task, test, or implementation knowledge.
* Preserve `docs/specification.md` as a validation contract for intended observable behavior.
* Move implementation mechanics out of the specification unless they are themselves observable behavior or compatibility policy.
* Keep validation evidence separate from product explanation.
* Avoid maintaining a separate requested-feature inventory when ADRs, tasks, and specification refs already preserve traceability.
* Avoid doing Marketplace description and change-notes work before the documentation model is clean.
* Preserve links from ADRs, task refs, scenarios, and validation records.

## Considered Options

* Rebuild documentation around explicit ownership boundaries.
* Keep the current documentation structure and only polish README.
* Make `docs/specification.md` the complete user and validation reference.
* Split everything into many small pages immediately.

## Decision Outcome

Chosen option: "Rebuild documentation around explicit ownership boundaries", because the current problem is ownership drift across multiple artifacts, and a small but explicit documentation model lets the repository rewrite user docs, validation docs, and specification content without duplicating responsibilities.

If accepted, use this documentation ownership model:

* `README.md` is the concise landing page: product purpose, prerelease status, requirements, install path, quickstart, key settings, user-visible limitations, and links to deeper docs.
* `docs/user-guide.md` is the task-oriented user manual: `AI`, `Commit`, `Push`, shortcuts, settings, changelist and staging-area behavior, safe push behavior at user level, and screenshots or animation when available.
* `docs/troubleshooting.md` is the user-facing troubleshooting and FAQ owner: missing or disabled AI Assistant, AI timeout, disabled or hidden controls, push fallback, outgoing-only push stops, conflicts, background VCS operations, and what evidence to include in reports.
* `docs/specification.md` remains the validation contract for intended observable plugin behavior. It should state what the plugin must do and why, with requirement refs, ADR sources, and validation links. It should not describe implementation classes, reflection mechanics, test helper behavior, or current code structure unless those details are observable compatibility policy.
* `docs/validation/` and `docs/scenario-coverage.md` remain evidence owners. They record how behavior was checked, not product explanation.
* `docs/requested-features.md` is retired and deleted during the rebuild. Any useful traceability must move to `docs/decisions/README.md`, `docs/specification.md`, or task/archive refs instead of remaining in a standalone feature inventory.
* `CONTRIBUTING.md` owns contributor setup, local validation, pull-request expectations, and developer workflow links. It should not be the primary user installation or usage guide.
* `SUPPORT.md` moves from the repository root to `docs/SUPPORT.md`. It remains the GitHub-recognized support resource and owns support scope, issue-reporting expectations, privacy boundaries, and out-of-scope support cases. It may link to troubleshooting but should not duplicate the full FAQ.
* `config/intellij-platform/description.html` and `config/intellij-platform/change-notes.html` are deferred until the cleaned user-facing docs exist; later Marketplace text should be derived from the rebuilt README and user guide.

Use this `docs/` naming convention:

* Product and user documentation uses lowercase kebab-case, for example `docs/user-guide.md`, `docs/troubleshooting.md`, `docs/specification.md`, `docs/scenario-coverage.md`, and `docs/validation/manual-sandbox.md`.
* Directory index files use `README.md`.
* GitHub-recognized community health files keep their canonical uppercase names even under `docs/`, for example `docs/SUPPORT.md`.
* Human and process governance documents keep their existing uppercase phrase names, for example `docs/WORKING_WITH_AI.md` and `docs/DEVELOPMENT_LIFECYCLE.md`.
* Registers and templates keep their existing uppercase or underscore names, for example `docs/decisions/OPEN_QUESTIONS.md`, `docs/decisions/ADR_TEMPLATE.md`, and `docs/proposals/PROPOSAL_TEMPLATE.md`.
* ADRs and proposals keep their existing required ID-prefixed formats.
* Assets and concept files use lowercase kebab-case, with numeric prefixes allowed for ordered variants.

This decision does not change plugin runtime behavior, supported IDE scope, Git-only scope, AI Assistant dependency policy, release policy, validation requirements, or Marketplace publication status.

### Consequences

* Good, because README can become a focused landing page instead of a mixed user and contributor document.
* Good, because users get a dedicated guide and troubleshooting path.
* Good, because the specification can become a cleaner intended-behavior contract for tests and validation.
* Good, because implementation notes extracted from the specification can be preserved in contributor, ADR, compatibility, or validation owners instead of being lost.
* Good, because retiring `docs/requested-features.md` removes a duplicate traceability surface.
* Good, because Marketplace documentation can be handled later from stable source material.
* Bad, because this is a broad documentation rewrite touching several cross-linked artifacts.
* Bad, because temporarily there will be both old wording and draft target structure until the companion plan is implemented.

### Confirmation

After acceptance and companion-plan approval, confirm implementation by checking:

* `README.md` is concise and links to deeper user, troubleshooting, support, and contributor docs.
* `docs/user-guide.md` exists and owns full user workflow explanation.
* `docs/troubleshooting.md` exists and owns FAQ and problem-path guidance.
* `docs/specification.md` separates intended observable behavior from implementation mechanics.
* `docs/requested-features.md` is deleted, and no active user or contributor documentation links to it.
* New or moved `docs/` files follow the naming convention above.
* `CONTRIBUTING.md` no longer depends on README as the primary build and validation owner.
* `docs/SUPPORT.md` exists, GitHub-recognized support links still work, and the page links to troubleshooting without duplicating the whole FAQ.
* `TASKS.md` documentation tasks are updated or replaced to match the new documentation plan.
* `T-DOC-018` and `T-DOC-019` remain deferred until the rebuilt user docs are available.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Rebuild documentation around explicit ownership boundaries

This option creates a small documentation architecture before rewriting content.

* Good, because it resolves the root ownership problem rather than polishing symptoms.
* Good, because it gives each artifact a clear audience and maintenance rule.
* Good, because it removes the standalone requested-feature inventory instead of redefining it.
* Good, because it keeps validation, support, and user docs connected without making them duplicates.
* Good, because it supports a later Marketplace pass from stable source docs.
* Bad, because it requires a companion plan and coordinated rewrite.

### Keep the current documentation structure and only polish README

This option would make the most visible document better first.

* Good, because it is fast and low risk.
* Good, because it can address the immediate README backlog items.
* Bad, because it leaves the specification mixing intention and implementation detail.
* Bad, because it keeps troubleshooting and contributor material scattered.
* Bad, because later Marketplace work would inherit the same unclear source of truth.

### Make `docs/specification.md` the complete user and validation reference

This option would centralize behavior explanation in one file.

* Good, because there is one canonical behavior file.
* Good, because validation links remain close to requirements.
* Bad, because users should not have to read requirement refs, ADR sources, and scenario links to understand the plugin.
* Bad, because adding FAQ, screenshots, and setup guidance would make the specification less useful as a validation contract.

### Split everything into many small pages immediately

This option would create a larger documentation site shape now.

* Good, because each topic could have a precise owner.
* Good, because future Marketplace and support content could reuse focused pages.
* Bad, because the repository is still prerelease and does not need a large docs tree yet.
* Bad, because too many pages would increase link and maintenance overhead before the content model is proven.

## More Information

Related current artifacts:

* `README.md`
* `CONTRIBUTING.md`
* `SUPPORT.md` (to move to `docs/SUPPORT.md`)
* `docs/specification.md`
* `docs/requested-features.md` (to retire)
* `docs/validation/manual-sandbox.md`
* `docs/scenario-coverage.md`
* `TASKS.md` T-DOC-017 through T-DOC-023

Companion draft plan: `PLAN-user-documentation-rebuild`.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with the accepted implementation status, then implement the companion plan only after explicit plan approval.
