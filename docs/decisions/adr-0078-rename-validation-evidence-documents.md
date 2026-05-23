---
status: accepted
date: 2026-05-23
accepted_at: 2026-05-23T22:25:22+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Rename Validation Evidence Documents

## Context and Problem Statement

ADR 0076 separated user documentation, the behavior specification, and validation
evidence, but the retained validation evidence names are still vague:
`docs/scenario-coverage.md` sounds like a coverage summary even though the file
is primarily a stable scenario registry, while
`docs/validation/manual-sandbox.md` sounds like a sandbox note even though the
file is used as a release-cycle checklist and manual evidence target.

How should the repository name and shape these validation evidence documents so
contributors can quickly tell which file owns stable scenario IDs, reusable
release validation instructions, and current-cycle validation evidence?

## Decision Drivers

* Make validation artifact names describe their maintenance role, not just their
  historical origin.
* Keep validation evidence under `docs/validation/` where possible.
* Separate stable scenario inventory from release-cycle execution notes.
* Keep reusable checklists separate from dated local or release evidence.
* Preserve stable scenario refs such as `SCN-*`, `T-VAL-*`, and `ADR-*`.
* Keep public user docs free of maintainer-only validation inventory details.
* Avoid changing plugin behavior, support scope, release policy, or validation
  requirements.

## Considered Options

* Move to `docs/validation/scenario-register.md` and
  `docs/validation/release-checklist.md`.
* Keep current filenames and only change page titles.
* Move both files into a broader `docs/release/` directory.
* Merge both files into one validation document.

## Decision Outcome

Chosen option: "Move to `docs/validation/scenario-register.md` and `docs/validation/release-checklist.md`", because it gives each document a clear owner and puts validation evidence in one directory without changing the underlying scenario refs or release validation expectations.

If accepted:

* Add `docs/validation/README.md` as the validation documentation map.
* Rename `docs/scenario-coverage.md` to
  `docs/validation/scenario-register.md`.
* Rename `docs/validation/manual-sandbox.md` to
  `docs/validation/release-checklist.md`.
* Use `docs/validation/reports/` for dated local, release-candidate, or release
  validation evidence reports.
* Use `Scenario Register` as the page title for the scenario file.
* Use `Release Validation Checklist` as the page title for the release-cycle
  validation file.
* Keep the scenario register focused on stable scenario refs, counts, execution
  mode, status, requirement/source refs, and evidence targets.
* Keep the release checklist focused on reusable release-readiness gates,
  required inputs, manual checklist items, and evidence recording instructions.
* Keep dated artifact paths, product build snapshots, command results, skipped
  checks, and release-readiness conclusions in validation reports instead of
  reusable checklist text.
* Keep scenario IDs, task refs, ADR refs, counts, and evidence targets stable.
* Update README, user guide, troubleshooting, support, specification, task,
  guidance, and proposal references to the new paths where active links are
  maintained.
* Keep archived proposal references historically accurate unless they need an
  active navigation link.

### Consequences

* Good, because `scenario-register.md` describes the file's primary function:
  stable scenario IDs, execution mode, status, and evidence targets.
* Good, because `release-checklist.md` describes the file's primary function:
  current release-cycle validation scope, IDE matrix, manual scenarios, and
  evidence to record.
* Good, because `docs/validation/` becomes the single obvious location for
  validation evidence.
* Good, because README and user docs can label these as maintainer validation
  artifacts instead of ordinary usage docs.
* Bad, because existing links to the two current filenames must be updated.

### Confirmation

After acceptance, confirm implementation by checking:

* `docs/validation/scenario-register.md` exists and
  `docs/scenario-coverage.md` does not.
* `docs/validation/release-checklist.md` exists and
  `docs/validation/manual-sandbox.md` does not.
* `docs/validation/README.md` maps the validation documentation owners.
* Dated current-cycle evidence is preserved under `docs/validation/reports/`.
* `.agents/references/documentation.md` names the new validation evidence
  owners.
* Active links in README, contributor docs, support docs, user docs,
  troubleshooting docs, specification, and task docs resolve to the new paths.
* The scenario register links to the release checklist for retained manual
  evidence, and the release checklist links back to the scenario register.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Move to `docs/validation/scenario-register.md` and `docs/validation/release-checklist.md`

This option renames the two validation evidence files and keeps them under the
same validation directory.

* Good, because the file names name the ownership boundary directly.
* Good, because the scenario file no longer sits at the top of `docs/` beside
  user-facing pages.
* Good, because the release checklist name makes the current-cycle nature of the
  manual evidence file clearer.
* Bad, because link churn is required.

### Keep current filenames and only change page titles

This option would improve headings while avoiding file moves.

* Good, because it minimizes churn.
* Bad, because contributors would still see unclear filenames in links,
  task refs, and validation guidance.

### Move both files into a broader `docs/release/` directory

This option would treat both files as release-operation documents.

* Good, because the manual checklist is release-oriented.
* Bad, because the scenario registry is also used during feature work, bug
  fixes, and coverage planning, not only during releases.

### Merge both files into one validation document

This option would combine scenario inventory and release-cycle evidence.

* Good, because there would be one validation file to find.
* Bad, because the stable scenario registry and current-cycle manual evidence
  have different update rhythms and audiences.
* Bad, because the merged file would be harder to scan and more likely to
  accumulate stale release notes.

## More Information

This ADR refines the validation evidence naming from ADR 0076 and accepts the
content separation proposed in `PROP-validation-docs-separation` without
changing ADR 0076's user documentation, troubleshooting, support, or
specification ownership model.

No companion implementation plan is proposed because the implementation is a
bounded documentation rename, heading cleanup, and link update. Implementation
is still blocked until this ADR is accepted.

After this ADR is accepted, update the ADR Implementation Tracker in
`docs/decisions/README.md` with implementation status, evidence, and last
updated date.
