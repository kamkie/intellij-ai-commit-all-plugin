# Plan: Commit Tool Window Actions

Plan-ID: PLAN-commit-tool-window-actions

Status: In Progress

Filename: `.agents/plans/PLAN-commit-tool-window-actions.md`

## Readiness

- Plan readiness: Approved; ready for implementation.
- Approved by: Kamil Kiewisz <kamkie@outlook.com>
- Open questions: None.
- Implementation progress: Orchestrated execution started.

## Goal

Add the `AI Commit All` split button to the Commit tool window primary actions, with commit-only and commit-and-push segments that are visible and enabled only when the current project can run the supported Git commit workflow.

## Non-Goals

- Do not implement the all-files selection engine beyond calling the API supplied by `PLAN-include-all-git-files`.
- Do not implement AI Assistant invocation or commit execution beyond delegating to the planned workflow services.
- Do not introduce custom confirmation dialogs.

## Assumptions

- Use action labels from ADR 0005 and split-button behavior from ADR 0006 and ADR 0027.
- Final icons are SVG resources under the plugin resource tree and follow IntelliJ Platform icon guidance.
- Visibility and enablement should use IntelliJ VCS and commit workflow state, not shell Git inspection.

## Open Questions

No open plan questions.

## Proposed Changes

- Task 1: Produce final IntelliJ-style action icons.
    - Covers `T-ACTIONS-001` and `T-ACTIONS-002`.
    - Convert the generated concept direction into small SVG icon assets, with dark variants only when contrast requires them.
- Task 2: Register the split-button actions.
    - Covers `T-ACTIONS-004`.
    - Add action classes, action IDs, group placement, presentation text, icons, and plugin descriptor entries needed for the Commit tool window primary actions group.
- Task 3: Route split-button segments into workflow entry points.
    - Covers `T-ACTIONS-005` and `T-ACTIONS-006`.
    - Route `AI Commit All` to commit-only mode and `& Push` to commit-and-push mode without duplicating the downstream workflow implementation.
- Task 4: Implement visibility and enablement rules.
    - Covers `T-ACTIONS-007` and `T-ACTIONS-008`.
    - Hide outside active Git commit workflow contexts and disable when no non-ignored committable files are available.

## Execution Model

Use one orchestrator and one fresh task worker per named task when agent delegation is available. Do not run these tasks in parallel because they share action classes, plugin descriptor registration, and resources.

Each named task should be implemented, validated, self-reviewed, and committed before the next task starts.

## Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` if docs or plans are updated during execution.
- Run `gradle buildPlugin`.
- Run IDE inspections or targeted tests for action update logic when available.
- Manually verify action visibility, disabled state, split-button text, and light/dark icon rendering in a sandbox IDE with the Commit tool window.

## Risks

- Commit tool window action group IDs or component context may differ across target IDEs.
- Split-button registration may require adapting to platform APIs that are not obvious from static code alone.
- Icon assets can look acceptable in one theme and fail contrast in another; test both light and dark themes before closing the plan.

## Handoff Notes

Implementation should keep the action layer thin. If action placement or split-button APIs require a new product or compatibility decision, stop and record it before editing the governed behavior further.
