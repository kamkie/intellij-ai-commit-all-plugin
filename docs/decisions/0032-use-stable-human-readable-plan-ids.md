# Use Stable Human-Readable Plan IDs

Status: Accepted

Date: 2026-05-15

## Context

The repository already uses stable IDs for open questions and tasks.

Plans are active execution artifacts and can be referenced by commits, reviews, handoffs, and later release preparation. File paths and titles can change as a plan is clarified, split, renamed, or moved.

The user requested that plans have stable IDs, but not IDs that are strictly number based.

## Decision

Every plan must have a stable `Plan-ID`.

Use the format `P-<short-kebab-slug>`, for example:

- `P-scaffold-plugin-project`
- `P-commit-tool-window-actions`
- `P-local-repository-e2e-tests`

Do not use strictly number-based plan IDs such as `P-0001`.

The plan ID should be human-readable enough to recognize the plan without relying on the file path. Keep the `Plan-ID` stable when the title, filename, status, or wording changes.

When a plan is split, keep the original ID for the closest surviving plan and assign new meaningful IDs to new plans. Do not reuse a retired plan ID for unrelated work.

Commits, reviews, handoffs, and release notes that refer to plan work should include the stable `Plan-ID` when practical.

## Consequences

- Plans can be referenced reliably without depending only on filenames or list order.
- Plan IDs remain readable during handoffs and commit metadata.
- Plans need one extra metadata line.
- Agents must choose meaningful IDs instead of allocating numeric sequences.

## Alternatives Considered

- Use strictly numeric plan IDs.
  - Why it was not chosen: the user explicitly requested IDs that are not strictly number based.
- Use only the plan filename.
  - Why it was not chosen: filenames can change when plans are clarified or moved.
- Use only the plan title.
  - Why it was not chosen: titles can change as scope is refined.

## Follow-Up

- Update `.agents/plans/PLAN_TEMPLATE.md`.
- Update `.agents/plans/README.md`.
- Update `.agents/references/planning.md` and `.agents/references/documentation.md`.
- Update `.gitmessage`, `docs/WORKING_WITH_AI.md`, `TASKS.md`, and `CHANGELOG.md`.
