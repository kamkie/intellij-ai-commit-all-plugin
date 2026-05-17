# Plan: Error Handling And UX

Plan-ID: PLAN-error-handling-ux

Status: Implemented

Filename: `.agents/plans/PLAN-error-handling-ux.md`

## Readiness

- Plan readiness: Approved; ready for implementation.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-05-15T04:23:08+02:00
- Open questions: None.
- Implementation progress: Implemented through committed plan tasks; no newly discovered uncovered risks require a new open question.

## Status History

- 2026-05-15T03:55:19+02:00: none -> Draft by Kamil Kiewisz <kamkie@outlook.com>; plan created.
- 2026-05-15T04:23:08+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicit user approval recorded.
- 2026-05-15T04:41:13+02:00: Approved -> In Progress by OpenAI Codex <codex@openai.com>; orchestrated implementation started.
- 2026-05-15T06:39:50+02:00: In Progress -> Implemented by OpenAI Codex <codex@openai.com>; planned changes completed and validated.

## Goal

Define and implement plugin-owned error handling only where IntelliJ, Git, VCS, push, or AI Assistant do not already provide the user-facing message, while making every failure path stop without unintended commit or push.

## Non-Goals

- Do not replace platform-owned messages with plugin-specific text.
- Do not add retry loops for commit or push failures.
- Do not add custom confirmation prompts unless a concrete uncovered risk is documented first.

## Assumptions

- Error ownership follows ADR 0014, ADR 0016, and ADR 0017.
- Plugin-owned notifications need a notification group only for states without a platform-owned message.
- New uncovered safety risks should be recorded in `docs/decisions/OPEN_QUESTIONS.md` before implementation continues.

## Open Questions

No open plan questions.

## Proposed Changes

- Task 1: Add the minimum plugin-owned notification surface.
    - Covers `T-ERROR-001` and `T-ERROR-007`.
    - Add a notification group and document any unavoidable plugin-owned text introduced by implementation.
- Task 2: Handle VCS busy or frozen states.
    - Covers `T-ERROR-002` and `T-ERROR-003`.
    - Detect frozen changelist manager state and background VCS operations already running; stop without committing.
- Task 3: Handle workflow stop conditions.
    - Covers `T-ERROR-004` and `T-ERROR-005`.
    - Report AI timeout and empty commit state through the best available platform or plugin-owned message.
- Task 4: Forward commit and push failures.
    - Covers `T-ERROR-006`.
    - Preserve standard failure messages and avoid retry loops.
- Task 5: Record newly discovered uncovered risks.
    - Covers `T-ERROR-008`.
    - Add an open question and placeholder task when implementation reveals a risk not covered by standard IDE safeguards.

## Execution Model

Use one orchestrator and one fresh task worker per named task when agent delegation is available. Do not run these tasks in parallel because failure handling must align across the full workflow.

Each named task should be implemented, validated, self-reviewed, and committed before the next task starts.

## Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` if notification text or open questions are documented.
- Run `gradle buildPlugin`.
- Add tests for timeout, empty state, busy VCS, and forwarded failure branches where practical.
- Manually verify standard IntelliJ, Git, VCS, push, and AI Assistant messages are not masked.

## Risks

- Some platform-owned error states may be hard to reproduce reliably in tests.
- Overly broad exception handling can hide precise platform diagnostics.
- Adding plugin-owned text without documentation would make support behavior harder to maintain.

## Handoff Notes

Keep this plan close to the implemented workflow. If it is executed before the core workflow plans, expect some tasks to remain blocked until the concrete failure paths exist.
