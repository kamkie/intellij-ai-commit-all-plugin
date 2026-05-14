# Canonical Product And Action Labels

Status: Accepted

Date: 2026-05-14

## Context

The repository used both `AI Commit All Files` and `AI Commit All` for the product and action name. The push flow was also described with several candidate labels, including `AI Commit & Push All`, `AI Commit & Push`, and `AI Commit All & Push`.

The project needs stable labels before implementation so `README.md`, `plugin.xml`, action classes, tests, and future screenshots use consistent names.

## Decision

Use these canonical labels:

- Product and plugin display name: `AI Commit All`.
- Primary commit action: `AI Commit All`.
- Split-button push segment: `& Push`.

Do not use `AI Commit All Files` as the product or action label.

Do not use `AI Commit All & Push` or `AI Commit & Push` as standalone button labels unless a later ADR replaces the split-button design.

Use ADR 0003 to define what `All` means instead of adding `Files` to the UI label.

## Consequences

- UI labels are shorter and align better with IntelliJ action naming.
- The push segment relies on the primary `AI Commit All` label for scope and adds only the push distinction.
- Future implementation and documentation should use these exact labels unless a later ADR supersedes this decision.

## Alternatives Considered

- `AI Commit All Files`.
  - Why it was not chosen: it is longer and duplicates scope detail that belongs in documentation.
- `AI Commit & Push All`.
  - Why it was not chosen: the split-button design uses `& Push` as the secondary segment label.
- `AI Commit & Push`.
  - Why it was not chosen: the split-button design uses `& Push` as the secondary segment label.
- `AI Commit All & Push`.
  - Why it was not chosen: the user changed the design to a split button with `AI Commit All` and `& Push` segments.

## Follow-Up

- Update README and tasks to use the canonical labels.
- Remove the open question for exact action labels.
