# Orchestrator Maintains Changelog

Status: Accepted

Date: 2026-05-15

## Context

ADR 0026 defines the orchestrator and fresh task worker model for accepted multi-task plans.

ADR 0029 adds `CHANGELOG.md` and `.agents/references/releases.md`.

When plan task workers implement individual tasks, they may see local changes clearly but lack whole-plan context for deciding whether a changelog entry is notable, how it should be grouped, and whether wording duplicates another task's entry.

The user requested a rule that the changelog must be maintained by the orchestrator.

## Decision

The orchestrator owns `CHANGELOG.md` maintenance during orchestrated plan execution and release preparation.

Task workers may suggest changelog entries in handoff evidence, but they do not own final changelog edits, category selection, placement, or notability decisions.

During orchestrated plan execution, the orchestrator decides after each task review whether the task produced a notable unreleased change and updates `CHANGELOG.md` before starting the next task when an entry is needed.

During release preparation, the release orchestrator owns moving accepted `Unreleased` entries into the versioned release section.

## Consequences

- `CHANGELOG.md` stays coherent across multiple plan tasks.
- Task workers remain focused on their assigned task and evidence.
- Changelog wording is less likely to duplicate entries or expose internal task noise.
- The orchestrator has one more explicit review responsibility before starting the next task.

## Alternatives Considered

- Let each task worker edit `CHANGELOG.md`.
  - Why it was not chosen: workers may lack whole-plan context and can create duplicate or inconsistent entries.
- Update `CHANGELOG.md` only during release preparation.
  - Why it was not chosen: notable changes are easier to capture while the orchestrator is reviewing each completed task.
- Let commit messages replace changelog maintenance.
  - Why it was not chosen: commit metadata is detailed provenance, while `CHANGELOG.md` is curated user, contributor, compatibility, and release history.

## Follow-Up

- Update `.agents/references/planning.md` with orchestrator changelog responsibility.
- Update `.agents/references/releases.md` with changelog ownership rules.
- Update documentation guidance and human-facing AI workflow docs.
- Update `TASKS.md` and `CHANGELOG.md`.
