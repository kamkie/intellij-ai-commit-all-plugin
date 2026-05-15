# Plan: Validation Coverage

Plan-ID: PLAN-validation-coverage

Status: Draft

Filename: `.agents/plans/PLAN-validation-coverage.md`

## Readiness

- Plan readiness: Ready for user review; implementation requires explicit approval.
- Open questions: None known.
- Implementation progress: Not started.

## Goal

Create the automated and manual validation coverage needed for the implemented plugin workflow, including plugin verifier checks, local-repository end-to-end tests, and sandbox IDE scenario records.

## Non-Goals

- Do not implement product behavior solely inside tests.
- Do not push to real remotes during validation.
- Do not replace manual sandbox validation for scenarios that cannot be automated reliably yet.

## Assumptions

- Validation scope follows ADR 0020 and ADR 0021.
- Documentation-only validation should use `scripts/validate-docs.ps1`; plugin behavior validation should use Gradle, verifier, local repositories, and sandbox IDE checks.
- Exact IDE product names and build numbers must be recorded for manual validation.

## Open Questions

No open plan questions.

## Proposed Changes

- Task 1: Configure compatibility and packaging validation.
    - Covers `T-VAL-002`.
    - Add or document plugin verifier execution for target IDE versions.
- Task 2: Add local-repository end-to-end coverage.
    - Covers `T-VAL-019`, `T-VAL-020`, and `T-VAL-021`.
    - Cover modified, added, deleted, moved or renamed, unversioned, ignored, multi-changelist, multi-root, commit-only, and safe local-remote commit-and-push cases where practical.
- Task 3: Keep manual sandbox coverage for non-automated scenarios.
    - Covers `T-VAL-003`, `T-VAL-004`, `T-VAL-005`, `T-VAL-006`, `T-VAL-007`, `T-VAL-008`, `T-VAL-009`, `T-VAL-010`, `T-VAL-011`, `T-VAL-012`, `T-VAL-013`, `T-VAL-014`, `T-VAL-015`, `T-VAL-016`, `T-VAL-017`, `T-VAL-018`, and `T-VAL-022`.
    - Record exact current stable JetBrains IDE product names and build numbers available through All Products Pack.

## Execution Model

Use one orchestrator and one fresh task worker per named task when agent delegation is available. Task 1 and Task 2 can be parallelized only if an approved revision assigns disjoint Gradle/test write scopes.

Each named task should be implemented, validated, self-reviewed, and committed before the next task starts.

## Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` for validation documentation updates.
- Run `gradle buildPlugin`.
- Run newly added automated tests and plugin verifier tasks.
- Run sandbox IDE manual checks listed in this plan and record exact evidence.

## Risks

- IntelliJ test framework coverage for commit UI workflows may be limited or slow.
- Plugin verifier target availability may depend on local caches or network access.
- Manual validation can drift unless evidence records include exact IDE build numbers and scenario results.

## Handoff Notes

This plan can start once enough workflow implementation exists to test. Earlier execution may focus on validation infrastructure and leave scenario execution pending until behavior lands.
