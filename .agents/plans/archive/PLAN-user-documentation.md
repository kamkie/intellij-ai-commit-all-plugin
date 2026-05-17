# Plan: User Documentation

Plan-ID: PLAN-user-documentation

Status: Closed

Close-Reason: Archived

Filename: `.agents/plans/archive/PLAN-user-documentation.md`

## Readiness

- Plan readiness: Closed; archived by user request.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-05-15T04:23:08+02:00
- Open questions: None.
- Implementation progress: Implemented through committed plan tasks; documentation validation passed.

## Status History

- 2026-05-15T03:55:19+02:00: none -> Draft by Kamil Kiewisz <kamkie@outlook.com>; plan created.
- 2026-05-15T04:23:08+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicit user approval recorded.
- 2026-05-15T04:41:13+02:00: Approved -> In Progress by OpenAI Codex <codex@openai.com>; orchestrated implementation started.
- 2026-05-15T06:39:50+02:00: In Progress -> Implemented by OpenAI Codex <codex@openai.com>; planned changes completed and validated.

- 2026-05-17T22:40:44+02:00: Implemented -> Closed by Kamil Kiewisz <kamkie@outlook.com>; archived completed plan by user request.

## Goal

Update user-facing and contributor-facing documentation so it accurately describes the implemented plugin workflow, setup, sandbox execution, limitations, source location, and release process.

## Non-Goals

- Do not claim Marketplace availability, usable workflow support, or release automation before those are implemented and validated.
- Do not duplicate backlog details from `TASKS.md`.
- Do not change support promises without updating `SUPPORT.md` when needed.

## Assumptions

- `README.md` owns user-facing setup, usage, dependency, supported IDE, and limitation content.
- Release and publication process documentation should be added after release automation is configured.
- Source code location documentation depends on Marketplace metadata being present.

## Open Questions

No open plan questions.

## Proposed Changes

- Task 1: Update setup, usage, dependency, and sandbox docs.
    - Covers `T-DOC-001`, `T-DOC-003`, `T-DOC-004`, and `T-DOC-005`.
    - Update `README.md` with setup, usage, AI Assistant dependency, sandbox IDE command, and known unsupported cases.
- Task 2: Document source and release process when release surfaces exist.
    - Covers `T-DOC-007` and `T-DOC-008`.
    - Add source code location once Marketplace metadata exists and contributor release/publication process after release automation is configured.
- Task 3: Align support and changelog docs.
    - Covers documentation side effects from the implementation plans.
    - Update `SUPPORT.md` or `CHANGELOG.md` only when implemented behavior, support scope, or contributor workflow changes make that notable.

## Execution Model

Use one orchestrator and one fresh task worker per named task when agent delegation is available. Task 1 should wait until the workflow behavior is implemented enough to document accurately.

Each named task should be implemented, validated, self-reviewed, and committed before the next task starts.

## Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`.
- Review user-facing claims against implemented behavior and validation evidence.
- Confirm local commands and paths in documentation match the current Gradle scaffold.

## Risks

- Documentation can overstate plugin readiness if updated before behavior is implemented.
- Marketplace and source metadata wording can become stale if release automation changes later.
- Duplicating implementation details in README would increase maintenance cost.

## Handoff Notes

Prefer documentation updates alongside or immediately after the behavior they describe. Keep `TASKS.md` as the backlog owner and `README.md` as the user-facing behavior owner.
