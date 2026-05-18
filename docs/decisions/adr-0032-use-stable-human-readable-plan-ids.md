---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Stable Human-Readable Plan IDs

## Context and Problem Statement

The repository already uses stable IDs for open questions and tasks.

Plans are active execution artifacts and can be referenced by commits, reviews, handoffs, and later release preparation. File paths and titles can change as a plan is clarified, split, renamed, or moved.

The user requested that plans have stable IDs, but not IDs that are strictly number based.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Stable Human-Readable Plan IDs
* Use strictly numeric plan IDs.
* Use only the plan filename.
* Use only the plan title.

## Decision Outcome

Chosen option: "Adopt Use Stable Human-Readable Plan IDs", because Every plan must have a stable `Plan-ID`.

Every plan must have a stable `Plan-ID`.

Use the format `PLAN-<short-kebab-slug>`, for example:

- `PLAN-scaffold-plugin-project`
- `PLAN-commit-tool-window-actions`
- `PLAN-local-repository-e2e-tests`

Do not use strictly number-based plan IDs such as `PLAN-0001`.

The plan ID should be human-readable enough to recognize the plan without relying on the file path. Keep the `Plan-ID` stable when the title, filename, status, or wording changes.

When a plan is split, keep the original ID for the closest surviving plan and assign new meaningful IDs to new plans. Do not reuse a retired plan ID for unrelated work.

Commits, reviews, handoffs, and release notes that refer to plan work should include the stable `Plan-ID` when practical.

### Consequences

- Plans can be referenced reliably without depending only on filenames or list order.
- Plan IDs remain readable during handoffs and commit metadata.
- Plans need one extra metadata line.
- Agents must choose meaningful IDs instead of allocating numeric sequences.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Stable Human-Readable Plan IDs

* Good, because Every plan must have a stable `Plan-ID`.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Use strictly numeric plan IDs.

* Bad, because the user explicitly requested IDs that are not strictly number based.

### Use only the plan filename.

* Bad, because filenames can change when plans are clarified or moved.

### Use only the plan title.

* Bad, because titles can change as scope is refined.

## More Information

- Update `.agents/plans/PLAN_TEMPLATE.md`.
- Update `.agents/plans/README.md`.
- Update `.agents/references/planning.md` and `.agents/references/documentation.md`.
- Update `.gitmessage`, `docs/WORKING_WITH_AI.md`, `TASKS.md`, and `CHANGELOG.md`.
