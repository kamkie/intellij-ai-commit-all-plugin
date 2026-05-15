# Validate Current Products, Changelists, And Staging

Status: Accepted

Date: 2026-05-15

## Context

Manual sandbox IDE versions were open as `Q-VAL-1`, and Git staging-area coverage was open as `Q-VAL-2`.

The user answered:

- `Q-VAL-1`: use current IDE versions; the user has an All Products Pack subscription.
- `Q-VAL-2`: yes, changelists and staging both need to be supported and tested.

ADR 0008 already sets the minimum supported IntelliJ Platform release line to 2026.1 and targets all JetBrains IDEs with the VCS Commit tool window and compatible commit workflow APIs.

## Decision

Use current stable JetBrains IDE builds available through the user's All Products Pack for manual sandbox validation.

Record the exact IDE product names and build numbers at validation time. At minimum, validation should include IntelliJ IDEA and representative non-IDEA JetBrains IDEs with compatible Git commit workflows. Broaden coverage across the current product set when the scaffold and CI make that practical.

The implementation must support and test both:

- IntelliJ changelists, including changes spread across multiple changelists.
- Git staging area enabled and disabled.

## Consequences

- `Q-VAL-1` and `Q-VAL-2` are resolved.
- Validation tasks no longer wait for a separate IDE-version answer or staging-area decision.
- Test reports must name the current IDE builds used instead of relying on the word "current".
- Implementation should avoid assumptions that only work with one changelist or only one staging-area mode.

## Alternatives Considered

- Test only IntelliJ IDEA.
  - Why it was not chosen: ADR 0008 targets all compatible JetBrains IDEs, and the user has access to the product pack.
- Defer Git staging-area support.
  - Why it was not chosen: the user explicitly requested staging and changelist support and tests.

## Follow-Up

- Remove `Q-VAL-1` and `Q-VAL-2` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` validation dependencies.
- See ADR 0021 for local-repository end-to-end acceptance workflows.
