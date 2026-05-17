# Plan: Three-Section AI Commit Push Control

Plan-ID: PLAN-three-section-ai-commit-push-control

Status: In Progress

Filename: `.agents/plans/PLAN-three-section-ai-commit-push-control.md`

## Readiness

- Plan readiness: Ready; explicit implementation approval recorded from the maintainer request.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Approved at: 2026-05-17T23:59:08+02:00
- Open questions: None.
- Implementation progress: Tasks 1 and 2 completed; Task 3 pending.

## Status History

- 2026-05-17T23:40:43+02:00: none -> Draft by Codex <codex@openai.com>; plan created after ADR 0052 and ADR 0053 acceptance.
- 2026-05-17T23:59:08+02:00: Draft -> Approved by Kamil Kiewisz <kamkie@outlook.com>; explicit user approval recorded from implementation request.
- 2026-05-17T23:59:08+02:00: Approved -> In Progress by Codex <codex@openai.com>; implementation started.
- 2026-05-18T00:04:28+02:00: In Progress -> In Progress by Codex <codex@openai.com>; Task 1 routing implementation completed.
- 2026-05-18T00:12:13+02:00: In Progress -> In Progress by Codex <codex@openai.com>; Task 2 styling implementation completed.

## Goal

Replace the current two-segment `AI Commit All` / `& Push` Commit tool window control with the ADR 0052 three-section cumulative control:

```text
<AI icon> AI | Commit | Push
```

The implementation should apply the ADR 0053 selected visual reference, including the violet passive and active AI section and snake-loop running indication.

## Non-Goals

- Do not change the product name `AI Commit All`.
- Do not weaken all-files scope, Git-only scope, AI Assistant dependency behavior, commit safeguards, or safe push fallback behavior.
- Do not publish or release the plugin as part of this plan.

## Assumptions

- IntelliJ Platform action APIs may not directly provide the requested three-section cumulative hover behavior, so a small custom toolbar control or presentation wrapper may be needed.
- The `AI` section should reuse the existing all-files inclusion and AI message-generation path, then stop without committing.
- The `Commit` and `Push` sections should reuse existing commit and safe push execution services rather than duplicating workflow logic.

## Open Questions

- None. Record any newly discovered API limitation or visual ambiguity in `docs/decisions/OPEN_QUESTIONS.md` or a follow-up ADR before changing the accepted behavior.

## Proposed Changes

- Task 1: Update action registration and routing for `T-ACTIONS-009`, `T-AI-007`, `T-COMMIT-006`, and `T-COMMIT-007`.
    - Replace the current two-segment action presentation with three cumulative sections.
    - Keep visibility and enablement tied to the supported Git commit workflow.
    - Route `AI` to all-file inclusion plus AI message generation only.
    - Route `Commit` to the `AI` section behavior plus commit.
    - Route `Push` to the `Commit` section behavior plus ADR 0047 safe push behavior.
- Task 2: Apply selected styling for `T-UI-001`.
    - Use `docs/concepts/graphics/split-button-drafts/01-blue-steel-compact-snake-violet-ai.svg` as the reference.
    - Preserve passive, cumulative hover, clicked/running, disabled, light, and dark state behavior from ADR 0053.
    - Use snake-loop activity on the active running section.
- Task 3: Update tests, manual validation records, and user-facing docs for `T-VAL-023`.
    - Update automated coverage for AI-only, commit, and push routing.
    - Update README usage only after the runtime behavior is implemented.
    - Refresh manual sandbox validation for light/dark rendering, disabled state, running indication, and all three section paths.

## Execution Model

- This plan can be split into action-routing, styling, and validation/documentation tasks after approval.
- Avoid parallel edits to the same UI registration or workflow service files unless ownership is split explicitly.

## Validation

- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
- `.\gradlew.bat test`
- `.\gradlew.bat buildPlugin`
- Manual sandbox review in a supported 2026.1 JetBrains IDE for light theme, dark theme, disabled state, `AI`, `Commit`, and `Push`.

## Risks

- IntelliJ Platform split-button APIs may not expose cumulative hover sections without custom painting.
- The custom control must still preserve platform accessibility, focus, disabled, and toolbar-density behavior.
- AI-only stop behavior changes the previous auto-commit assumption and needs careful regression coverage around user edits, timeouts, empty messages, and unavailable AI Assistant.

## Handoff Notes

- ADR 0052 and ADR 0053 are accepted and govern the implementation.
- ADR 0006 and ADR 0027 are superseded for future work, but the current runtime still implements their historical two-segment behavior until this plan is approved and implemented.
