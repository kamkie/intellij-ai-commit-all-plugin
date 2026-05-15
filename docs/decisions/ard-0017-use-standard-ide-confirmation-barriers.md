---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Standard IDE Confirmation Barriers

## Context and Problem Statement

Confirmation behavior for risky commit paths was open.

The user answered `Q-UX-4`: there is no need for additional plugin-specific checks for now. If a need appears during development, add a question and continue with a placeholder.

The plugin already uses the IDE commit workflow so before-commit checks, commit validation, warnings, and action errors should remain active.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Use Standard IDE Confirmation Barriers
* Add custom confirmation prompts for commit or push.
* Block implementation until every possible risky case is enumerated.

## Decision Outcome

Chosen option: "Adopt Use Standard IDE Confirmation Barriers", because Do not add plugin-specific confirmation dialogs or extra safety checks in the first implementation.

Do not add plugin-specific confirmation dialogs or extra safety checks in the first implementation.

Use the standard IntelliJ commit workflow as the confirmation and validation boundary:

- Keep before-commit checks active.
- Let IDE commit warnings, validation errors, and push errors appear through their normal UI.
- Do not bypass commit or push executor confirmation behavior.
- Do not add custom "are you sure" prompts unless a concrete implementation path proves the standard workflow does not cover the risk.

If development reveals a path that may require an additional plugin-owned confirmation or check, add a new open question with a stable ID in `docs/decisions/OPEN_QUESTIONS.md`, leave a placeholder task or implementation branch, and continue with the rest of the work.

### Consequences

- `Q-UX-4` is resolved without adding custom confirmation UX.
- Implementation can proceed through the standard commit and commit-and-push executors.
- Reviews should verify that normal IDE commit and push safeguards are still invoked.
- Any future plugin-specific confirmation must be tied to a concrete discovered risk and documented before implementation.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Use Standard IDE Confirmation Barriers

* Good, because Do not add plugin-specific confirmation dialogs or extra safety checks in the first implementation.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Add custom confirmation prompts for commit or push.

* Bad, because the user does not see a need now, and custom prompts would duplicate the IDE flow.

### Block implementation until every possible risky case is enumerated.

* Bad, because unknown risks should become focused questions only when development reveals them.

## More Information

- Remove `Q-UX-4` from `docs/decisions/OPEN_QUESTIONS.md`.
- Remove `depends on: Q-UX-4` markers from `TASKS.md`.
- Historical note: `Q-UX-5` was later resolved by ADR 0025, and final placeholder styling was selected by ADR 0027.
