---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Validate Current Products, Changelists, And Staging

## Context and Problem Statement

Manual sandbox IDE versions were open as `Q-VAL-1`, and Git staging-area coverage was open as `Q-VAL-2`.

The user answered:

- `Q-VAL-1`: use current IDE versions; the user has an All Products Pack subscription.
- `Q-VAL-2`: yes, changelists and staging both need to be supported and tested.

ADR 0008 already sets the minimum supported IntelliJ Platform release line to 2026.1 and targets all JetBrains IDEs with the VCS Commit tool window and compatible commit workflow APIs.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Validate Current Products, Changelists, And Staging
* Test only IntelliJ IDEA.
* Defer Git staging-area support.

## Decision Outcome

Chosen option: "Adopt Validate Current Products, Changelists, And Staging", because Use current stable JetBrains IDE builds available through the user's All Products Pack for manual sandbox validation.

Use current stable JetBrains IDE builds available through the user's All Products Pack for manual sandbox validation.

Record the exact IDE product names and build numbers at validation time. At minimum, validation should include IntelliJ IDEA and representative non-IDEA JetBrains IDEs with compatible Git commit workflows. Broaden coverage across the current product set when the scaffold and CI make that practical.

The implementation must support and test both:

- IntelliJ changelists, including changes spread across multiple changelists.
- Git staging area enabled and disabled.

### Consequences

- `Q-VAL-1` and `Q-VAL-2` are resolved.
- Validation tasks no longer wait for a separate IDE-version answer or staging-area decision.
- Test reports must name the current IDE builds used instead of relying on the word "current".
- Implementation should avoid assumptions that only work with one changelist or only one staging-area mode.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Validate Current Products, Changelists, And Staging

* Good, because Use current stable JetBrains IDE builds available through the user's All Products Pack for manual sandbox validation.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Test only IntelliJ IDEA.

* Bad, because ADR 0008 targets all compatible JetBrains IDEs, and the user has access to the product pack.

### Defer Git staging-area support.

* Bad, because the user explicitly requested staging and changelist support and tests.

## More Information

- Remove `Q-VAL-1` and `Q-VAL-2` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` validation dependencies.
- See ADR 0021 for local-repository end-to-end acceptance workflows.
