---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Canonical Product And Action Labels

## Context and Problem Statement

The repository used both `AI Commit All Files` and `AI Commit All` for the product and action name. The push flow was also described with several candidate labels, including `AI Commit & Push All`, `AI Commit & Push`, and `AI Commit All & Push`.

The project needs stable labels before implementation so `README.md`, `plugin.xml`, action classes, tests, and future screenshots use consistent names.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Canonical Product And Action Labels
* `AI Commit All Files`.
* `AI Commit & Push All`.
* `AI Commit & Push`.
* `AI Commit All & Push`.

## Decision Outcome

Chosen option: "Adopt Canonical Product And Action Labels", because Use these canonical labels: - Product and plugin display name: `AI Commit All`.

Use these canonical labels:

- Product and plugin display name: `AI Commit All`.
- Primary commit action: `AI Commit All`.
- Split-button push segment: `& Push`.

Do not use `AI Commit All Files` as the product or action label.

Do not use `AI Commit All & Push` or `AI Commit & Push` as standalone button labels unless a later ADR replaces the split-button design.

Use ADR 0003 to define what `All` means instead of adding `Files` to the UI label.

### Consequences

- UI labels are shorter and align better with IntelliJ action naming.
- The push segment relies on the primary `AI Commit All` label for scope and adds only the push distinction.
- Future implementation and documentation should use these exact labels unless a later ADR supersedes this decision.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Canonical Product And Action Labels

* Good, because Use these canonical labels: - Product and plugin display name: `AI Commit All`.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### `AI Commit All Files`.

* Bad, because it is longer and duplicates scope detail that belongs in documentation.

### `AI Commit & Push All`.

* Bad, because the split-button design uses `& Push` as the secondary segment label.

### `AI Commit & Push`.

* Bad, because the split-button design uses `& Push` as the secondary segment label.

### `AI Commit All & Push`.

* Bad, because the user changed the design to a split button with `AI Commit All` and `& Push` segments.

## More Information

- Update README and tasks to use the canonical labels.
- Remove the open question for exact action labels.
