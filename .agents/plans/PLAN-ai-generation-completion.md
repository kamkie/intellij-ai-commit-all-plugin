# Plan: AI Generation Completion

Plan-ID: PLAN-ai-generation-completion

Status: Implemented

Filename: `.agents/plans/PLAN-ai-generation-completion.md`

## Readiness

- Plan readiness: Approved; ready for implementation.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-05-15T04:23:08+02:00
- Open questions: None.
- Implementation progress: Implemented through committed plan tasks; automated validation passed.

## Status History

- 2026-05-15T03:55:19+02:00: none -> Draft by Kamil Kiewisz <kamkie@outlook.com>; plan created.
- 2026-05-15T04:23:08+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicit user approval recorded.
- 2026-05-15T04:41:13+02:00: Approved -> In Progress by OpenAI Codex <codex@openai.com>; orchestrated implementation started.
- 2026-05-15T06:39:50+02:00: In Progress -> Implemented by OpenAI Codex <codex@openai.com>; planned changes completed and validated.

## Goal

Wait safely for AI-generated commit messages before commit execution, expose timeout and polling settings, show running activity, and stop without committing when completion fails or the user edits the message.

## Non-Goals

- Do not invoke AI Assistant directly beyond consuming the invocation result from `PLAN-ai-assistant-message-generation`.
- Do not execute commits or pushes.
- Do not replace AI Assistant or IntelliJ error messages with plugin-owned messages unless no platform-owned message exists.

## Assumptions

- Completion behavior follows ADR 0011, ADR 0012, ADR 0014, and ADR 0027.
- Default timeout is 5 seconds and default supporting check interval is 500 ms.
- Commit is allowed only after AI completion is detected and the message is non-empty and changed from the captured pre-generation value.

## Open Questions

No open plan questions.

## Proposed Changes

- Task 1: Capture message state and observe completion.
    - Covers `T-WAIT-001`, `T-WAIT-002`, `T-WAIT-003`, and `T-WAIT-004`.
    - Capture the original message, prefer reliable completion signals, and use message-field polling only as supporting evidence.
- Task 2: Add settings for timeout and completion checks.
    - Covers `T-WAIT-005` and `T-WAIT-006`.
    - Add persistent Settings UI/state for timeout and completion-check interval with accepted defaults and validation.
- Task 3: Handle user edits during generation.
    - Covers `T-WAIT-007`.
    - Stop without committing or pushing when the user edits or clears the commit message while generation is running.
- Task 4: Show split-button activity while running.
    - Covers `T-AI-005`.
    - Surface running state in the action presentation or compatible UI layer without blocking the IDE UI thread.

## Execution Model

Use one orchestrator and one fresh task worker per named task when agent delegation is available. Do not run these tasks in parallel because settings, state observation, and UI activity all interact with the same workflow lifecycle.

Each named task should be implemented, validated, self-reviewed, and committed before the next task starts.

## Validation

- Run `gradle buildPlugin`.
- Add focused tests for timeout, unchanged message, empty message, user-edited message, and successful changed-message completion where practical.
- Manually test running activity and timeout behavior in a sandbox IDE.
- Confirm timeout and user-edit paths leave no commit or push behind.

## Risks

- Polling the message field can race with user edits or AI streaming updates.
- Completion signals may not be public or stable; if only polling is available, document the limitation before approval or implementation continues.
- Settings UI must avoid accepting zero or negative intervals that would cause busy loops.

## Handoff Notes

This plan owns the safety gate between AI generation and commit execution. Implementation should fail closed whenever completion evidence is ambiguous.
