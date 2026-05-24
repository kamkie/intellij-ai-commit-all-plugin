---
proposal_id: PROP-validation-docs-separation
generated_at: 2026-05-23T22-18
created_from: User request to propose content reengineering for clear separation of concerns in validation documentation.
purpose: Propose clearer ownership boundaries for validation scenario inventory, release checklists, and release evidence.
scope: Covers `docs/scenario-coverage.md`, `docs/validation/manual-sandbox.md`, related validation links, and the proposed ADR 0078 rename path.
---

# Validation Docs Separation Proposal

This proposal respects `AGENTS.md`, `docs/decisions/README.md`,
`docs/proposals/README.md`, ADR 0076, and proposed ADR 0078. It lists findings
for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Creation Context](#creation-context)
- [Progress Tracker](#progress-tracker)
- [Proposal Items](#proposal-items)
  - [New Features](#new-features)
  - [Errors And Mistakes](#errors-and-mistakes)
  - [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
    - [D001. Scenario file mixes registry, snapshots, and procedures](#d001-scenario-file-mixes-registry-snapshots-and-procedures)
    - [D002. Manual sandbox file duplicates coverage inventory](#d002-manual-sandbox-file-duplicates-coverage-inventory)
  - [Simplification Opportunities](#simplification-opportunities)
    - [S001. Add a validation directory map and ownership split](#s001-add-a-validation-directory-map-and-ownership-split)
    - [S002. Move current-cycle evidence into dated reports](#s002-move-current-cycle-evidence-into-dated-reports)
  - [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The current scenario document is doing three jobs: stable scenario registry,
  release evidence snapshot, and manual procedure owner.
- The current manual sandbox document is doing three different jobs: product
  matrix note, automated coverage summary, and partial release checklist.
- Reengineer validation docs around four owners: validation index, scenario
  register, release checklist, and dated release evidence reports.
- No scenario refs, requirement refs, validation requirements, or plugin behavior
  should change as part of this proposal.

## Creation Context

- Why this proposal exists: the maintainer accepted that better filenames are a
  useful start, then asked how the content itself should be reengineered for
  clear separation of concerns.
- How it was created: reviewed headings and cross-links in
  `docs/scenario-coverage.md`, `docs/validation/manual-sandbox.md`,
  `.agents/references/documentation.md`, README, contributor docs, support docs,
  user docs, and specification links.
- Scope guardrails: ADR 0076 keeps validation evidence separate from product
  explanation. Proposed ADR 0078 already proposes moving the two current files
  to `docs/validation/scenario-register.md` and
  `docs/validation/release-checklist.md`.

## Progress Tracker

| Id   | Title                                                   | Priority | Status | Decision |
|------|---------------------------------------------------------|----------|--------|----------|
| D001 | Scenario file mixes registry, snapshots, and procedures | 4        | done   | accepted |
| D002 | Manual sandbox file duplicates coverage inventory       | 4        | done   | accepted |
| S001 | Add a validation directory map and ownership split      | 4        | done   | accepted |
| S002 | Move current-cycle evidence into dated reports          | 4        | done   | accepted |

## Proposal Items

### New Features

_No tracked findings._

### Errors And Mistakes

_No tracked findings._

### Duplications To Remove Or Reduce

#### D001. Scenario file mixes registry, snapshots, and procedures

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T22:25:22+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-23T22:25:22+02:00 |

##### Context

- Evidence: `docs/scenario-coverage.md` starts as `Scenario Coverage`, then
  combines counting rules, project counts, IDEA UI automation evidence, the
  scenario registry, retained manual test details, and remaining automation
  candidates (`docs/scenario-coverage.md:1`,
  `docs/scenario-coverage.md:7`, `docs/scenario-coverage.md:16`,
  `docs/scenario-coverage.md:36`, `docs/scenario-coverage.md:59`,
  `docs/scenario-coverage.md:296`, `docs/scenario-coverage.md:394`).
- Impact: maintainers cannot quickly tell whether they are editing stable
  scenario identity, one release cycle's evidence, a manual execution procedure,
  or a future automation backlog.
- Non-goals:
  - Do not renumber or rewrite published scenario refs.
  - Do not remove existing automated or manual coverage.
  - Do not change requirements in `docs/specification.md`.
- Acceptance criteria:
  - The renamed scenario register owns only stable scenario identity,
      classification, execution mode, status, requirement/source refs, and
      primary evidence target.
  - Release-specific evidence and dated local results are absent from the
      scenario register.
  - Manual execution procedure detail is referenced from the register but
      owned elsewhere.

##### Recommended Change

After ADR acceptance, reshape `docs/validation/scenario-register.md` to contain:

1. Purpose and update rules.
2. status vocabulary and counting rules.
3. coverage counts.
4. scenario set summary.
5. scenario registry table.

Move the current `IDEA UI Automation Evidence`, `Retained Manual Test Case
Details`, and `Remaining Automation Candidates` sections out of the register.

##### Review Notes

- none

##### Follow-Up

- Artifact: amend proposed ADR 0078 or create an implementation plan after ADR
  acceptance.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  and link checking through documentation validation.

#### D002. Manual sandbox file duplicates coverage inventory

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T22:25:22+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-23T22:25:22+02:00 |

##### Context

- Evidence: `docs/validation/manual-sandbox.md` contains a current release
  matrix status, automated coverage added, a manual scenario table, a link back
  to scenario coverage, and a run command (`docs/validation/manual-sandbox.md:20`,
  `docs/validation/manual-sandbox.md:36`,
  `docs/validation/manual-sandbox.md:81`,
  `docs/validation/manual-sandbox.md:112`,
  `docs/validation/manual-sandbox.md:116`).
- Impact: the file repeats information already better owned by the scenario
  register, tests, and release reports, while the actual release checklist is
  hard to distinguish from historical notes.
- Non-goals:
  - Do not remove the requirement to perform manual validation before release
      readiness claims.
  - Do not weaken PyCharm, WebStorm, real AI Assistant, or platform error UI
      manual coverage.
- Acceptance criteria:
  - The release checklist owns only the current-cycle validation workflow,
      required inputs, manual gates, result recording shape, and report links.
  - Automated coverage is referenced by scenario ID or test lane, not copied as
      long explanatory sections.
  - Manual rows point to scenario refs in the scenario register.

##### Recommended Change

After ADR acceptance, reshape `docs/validation/release-checklist.md` to contain:

1. Purpose and release-readiness rule.
2. inputs for the current cycle: plugin artifact, IDE matrix, AI Assistant
   state, local fixture path, and report path.
3. required automated gates.
4. manual checklist grouped by product and risk area.
5. evidence recording instructions.
6. links to the scenario register and dated release reports.

Remove the long `Automated Coverage Added` narrative from this file.

##### Review Notes

- none

##### Follow-Up

- Artifact: amend proposed ADR 0078 or create an implementation plan after ADR
  acceptance.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  and manual link review.

### Simplification Opportunities

#### S001. Add a validation directory map and ownership split

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T22:25:22+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-23T22:25:22+02:00 |

##### Context

- Evidence: `.agents/references/documentation.md` currently names
  `docs/validation/` and `docs/scenario-coverage.md` together as validation
  evidence owners, but there is no `docs/validation/README.md` that explains
  the local split (`.agents/references/documentation.md:11`).
- Impact: maintainers must infer which validation document owns scenario IDs,
  release execution, reports, and automation follow-up.
- Non-goals:
  - Do not turn validation docs into user-facing product documentation.
  - Do not add a broad documentation site.
- Acceptance criteria:
  - `docs/validation/README.md` exists and lists the validation artifacts by
      ownership boundary.
  - README and user-facing docs either remove maintainer-only validation links
      or label them as maintainer validation artifacts.
  - `.agents/references/documentation.md` names the new owners.

##### Recommended Change

Introduce this ownership map:

| File                                           | Owner                                                                                                                    |
|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| `docs/validation/README.md`                    | Validation documentation map, update rules, and artifact ownership.                                                      |
| `docs/validation/scenario-register.md`         | Stable scenario refs, counts, execution mode, status, and evidence target.                                               |
| `docs/validation/release-checklist.md`         | Current release-cycle validation checklist and evidence recording instructions.                                          |
| `docs/validation/reports/<date-or-version>.md` | Dated release or local validation evidence, including commands, artifacts, IDE builds, results, skips, and report paths. |

##### Review Notes

- none

##### Follow-Up

- Artifact: amend proposed ADR 0078 and update documentation guidance after ADR
  acceptance.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  and `git diff --check`.

#### S002. Move current-cycle evidence into dated reports

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-23T22:25:22+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-23T22:25:22+02:00 |

##### Context

- Evidence: current artifact and local evidence notes are embedded in
  `docs/validation/manual-sandbox.md`, for example the prepared artifact and
  2026-05-22 IDEA automation result (`docs/validation/manual-sandbox.md:20`).
- Impact: a reusable checklist becomes stale whenever a release cycle completes,
  and historical evidence is hard to archive or compare across cycles.
- Non-goals:
  - Do not require a release report for every small local documentation edit.
  - Do not move CI logs or large generated reports into Git.
- Acceptance criteria:
  - The release checklist is reusable across release cycles.
  - Dated reports own one cycle's artifact path, IDE builds, commands, manual
      observations, skipped checks, and evidence paths.
  - The latest report can be linked from the release checklist or TASKS entry
      when release readiness is being claimed.

##### Recommended Change

Create `docs/validation/reports/` with a compact report format:

```markdown
# Release Validation Report: <version-or-cycle>

- Date:
- Artifact:
- IDE matrix:
- Automated gates:
- Manual checklist:
- Evidence paths:
- Skipped checks:
- Release-readiness conclusion:
```

Move the current artifact and local IDEA result notes from the reusable
checklist into the first report only when a release validation cycle is being
recorded.

##### Review Notes

- none

##### Follow-Up

- Artifact: amend proposed ADR 0078 and create the first report only when
  release validation evidence is being recorded.
- Validation: documentation validation, plus release-specific validation
  commands listed in the report.

### Smaller / Stylistic Items

- Use "Scenario Register" instead of "Scenario Coverage" in user-visible link
  labels when the file is kept in active navigation.
- Use "Release Validation Checklist" instead of "Manual Validation" in README
  links, and put it under a maintainer or validation subsection.
- Avoid "sandbox" in page titles unless the page is specifically about running
  the Gradle sandbox.

## Suggested Priority Order

1. `D001` - decide what stays in the scenario register before moving content.
2. `D002` - decide what stays in the release checklist before link updates.
3. `S001` - add the validation directory map and update guidance.
4. `S002` - introduce dated reports when a release validation cycle is recorded.

## Out Of Scope

- Plugin runtime behavior.
- Supported IDE family, Git-only support, or AI Assistant dependency policy.
- Renumbering scenario, task, requirement, or ADR refs.
- Executing release validation.
- Adding or rewriting automated tests.
- Marketplace description and release notes.
