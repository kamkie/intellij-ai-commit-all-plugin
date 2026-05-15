---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use MADR ADR Format And ARD Filenames

## Context and Problem Statement

The repository already stores project decisions and repository rule changes in `docs/decisions/`.

The previous ADR files used a local template with `Status:`, `Date:`, `Context`, `Decision`, `Consequences`, `Alternatives Considered`, and `Follow-Up` sections.

The maintainer requested that all ADR files strictly conform to the MADR 4.0.0 ADR template and use the `ard-0000-<slug>.md` filename format.

## Decision Drivers

* Keep decision records aligned with a published ADR template.
* Make ADR filenames predictable and easy to validate.
* Preserve existing decision history while changing only the document structure and filenames.

## Considered Options

* Adopt MADR 4.0.0 structure and `ard-0000-<slug>.md` filenames
* Keep the local ADR template
* Use `adr-0000-<slug>.md` filenames

## Decision Outcome

Chosen option: "Adopt MADR 4.0.0 structure and `ard-0000-<slug>.md` filenames", because it implements the maintainer's requested ADR structure and filename convention.

All ADR files under `docs/decisions/` must use the MADR 4.0.0 template structure and the `ard-0000-<slug>.md` filename format.

`ADR_TEMPLATE.md` should track the MADR 4.0.0 template.

Documentation validation should reject ADR files that do not use the required filename shape, required MADR front matter, required MADR headings, or chosen-option line.

### Consequences

* Good, because all decision records now share one published structure.
* Good, because filename and structure compliance can be checked locally.
* Bad, because the migration rewrites every existing ADR file and creates a large documentation diff.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when ADR files change.

## Pros and Cons of the Options

### Adopt MADR 4.0.0 structure and `ard-0000-<slug>.md` filenames

* Good, because it matches the maintainer's requested template and filename convention.
* Good, because it gives future ADRs a single external structure to follow.
* Bad, because existing ADR history must be mechanically rewritten.

### Keep the local ADR template

* Good, because it would avoid churn in existing decision files.
* Bad, because it would not satisfy the requested MADR 4.0.0 conformance.

### Use `adr-0000-<slug>.md` filenames

* Good, because `adr` is the common architectural decision record abbreviation.
* Bad, because the maintainer explicitly requested `ard-0000-<slug>.md`.

## More Information

- Update `docs/decisions/ADR_TEMPLATE.md`.
- Rename existing ADR files to `ard-0000-<slug>.md`.
- Update `docs/decisions/README.md` and local references.
- Extend `scripts/validate-docs.ps1` to enforce the new format.
