# Repository Quality Audit

Assess the repository for errors, mistakes, duplication, simplification opportunities, missing validation, and maintainability risks.
Use this when the user asks for a broad repository analysis or wants findings saved as a proposal for maintainer triage.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `.agents/references/reviews.md`
- `.agents/references/testing.md`
- `docs/proposals/README.md`
- `.agents/prompts/README.md`
- this prompt

Load source, build files, workflows, README, support docs, ADRs, plans, tasks, or archives only when they are needed to support a specific finding.

## Output

If the user asks for a proposal, create a proposal under `docs/proposals/` using `docs/proposals/PROPOSAL_TEMPLATE.md`.
Otherwise, produce a report in the current response.

Evaluate:

- correctness, user-facing behavior, and plugin compatibility risks
- duplicated or contradictory guidance
- obsolete, stale, or misplaced documentation
- missing validation, review, release, support, or CI coverage
- unnecessary complexity or simpler owner-specific alternatives
- security, privacy, or local-environment risks when visible from repository artifacts

Report shape:

- scope, method, and artifacts inspected
- findings grouped as errors and mistakes, duplications, simplifications, and smaller items
- owner file, evidence, impact, recommended change, and validation for each finding
- items intentionally out of scope
- whether the result should become a proposal, task, ADR, direct docs edit, or no action

## Non-Goals

- Do not implement findings during the audit unless the user separately asks.
- Do not make broad repository scans the default for narrow questions.
- Do not replace code review for a specific diff; use the repository review workflow for direct review requests.
- Do not set proposal decisions or create implementation plans without explicit user direction.
