# Plan: Marketplace, CI, And Release Automation

Plan-ID: PLAN-marketplace-ci-release

Status: Draft

Filename: `.agents/plans/PLAN-marketplace-ci-release.md`

## Readiness

- Plan readiness: Ready for user review; implementation requires explicit approval.
- Open questions: None known.
- Implementation progress: Not started.

## Goal

Prepare the plugin for official JetBrains Marketplace publication with Marketplace metadata, signing, publishing configuration, CI validation, plugin verifier checks, and a gated manual release workflow that keeps all secrets outside the repository.

## Non-Goals

- Do not publish to Marketplace or create release tags unless the user explicitly requests release execution later.
- Do not commit Marketplace tokens, signing keys, certificate passwords, or private credentials.
- Do not require release secrets for pull-request CI.

## Assumptions

- Publishing, signing, Marketplace metadata, and CI are in scope per ADR 0019.
- Plugin identity and vendor metadata follow ADR 0022.
- Release preparation follows `.agents/references/releases.md` only after implementation work is complete and integrated.

## Open Questions

No open plan questions.

## Proposed Changes

- Task 1: Add Marketplace-ready metadata.
    - Covers `T-REL-001` and `T-REL-002`.
    - Add official source code link, publishable description fields, tags, and other Marketplace metadata supported by the IntelliJ Platform Gradle Plugin.
- Task 2: Configure signing and publishing.
    - Covers `T-REL-003`, `T-REL-004`, and `T-REL-005`.
    - Configure signing and `publishPlugin` through local properties or CI secrets, and document any required manual first-upload step.
- Task 3: Add pull-request and packaging CI.
    - Covers `T-REL-006` and `T-REL-009`.
    - Add CI for build, tests, plugin structure verification, and packaging without requiring or exposing secrets.
- Task 4: Add verifier and gated release workflow.
    - Covers `T-REL-007` and `T-REL-008`.
    - Add Plugin Verifier CI for target IDE versions and a gated/manual signing and Marketplace publishing workflow.

## Execution Model

Use one orchestrator and one fresh task worker per named task when agent delegation is available. Do not run signing, publishing, and CI tasks in parallel unless an approved revision assigns disjoint files and secret surfaces.

Each named task should be implemented, validated, self-reviewed, and committed before the next task starts.

## Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` for docs and workflow references.
- Run `gradle buildPlugin`.
- Run available Gradle verification tasks, including plugin verifier once configured.
- Validate CI workflow syntax and confirm pull-request jobs do not require secrets.
- For release workflow dry runs, confirm secret names are referenced but secret values are absent from the repository.

## Risks

- Marketplace first-upload requirements may require manual setup that cannot be fully automated.
- Signing configuration can accidentally expose credentials if not restricted to environment variables, local properties, or CI secrets.
- Plugin verifier matrix can be slow or brittle if target IDE versions are too broad for routine PR checks.

## Handoff Notes

This plan should normally follow core workflow implementation and validation. It prepares release automation but does not perform an actual release or Marketplace publication.
