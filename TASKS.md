# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The plugin has not been published to JetBrains Marketplace.

Completed task entries are preserved in `## Completed Task Archive` after the open backlog.

Notation:

- Every task starts with a stable `T-AREA-NNN` ID. Keep IDs stable when wording, status, or ordering changes. Do not renumber existing IDs.
- `resolves: Q-ID` means the task answers an open question.
- `depends on: Q-ID` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Bugs

- [x] T-BUG-002: `ai` `Staging + AI message generation` stage sometimes do not work the first time. it needs to recheck if in fact all files are included. and only then generate a commit message. (Plan `PLAN-confirm-staged-before-ai-generation`)
- [x] T-BUG-003: Correct ADR filename prefix from the former `ard-0000-<slug>.md` typo to `adr-0000-<slug>.md` across decision files and references.
- [ ] T-BUG-004: animations are not working. we need an alternative indicator for the commit push control.
- [ ] T-BUG-005: same as T-BUG-002 but for commit. commit should not implement there own logic for first stage but reuse `Staging + AI message generation. same for push stage button.
- [ ] T-BUG-006: reopens T-BUG-002. when there are multiple gradle modules, or multiple intellij products in repo

### Testing

- [ ] T-TEST-001: we need many more test cases for the three-section AI commit push control.
- [ ] T-TEST-002: we need to automate the test cases for the three-section AI commit push control as much as possible.

### Ideas

- [ ] T-IDEA-006: proposals file ergonomics improvements

### Gui changes

- [x] T-UI-002: move button to the right of the `Commit and Push...` button.
- [ ] T-UI-003: button corners do not match the rest of the buttons.

### Three-Section AI Commit Push Control

- [x] T-ACTIONS-009: Replace the current `AI Commit All` / `& Push` Commit tool window control with the ADR 0052 three-section `<AI icon> AI | Commit | Push` control. (Plan `PLAN-three-section-ai-commit-push-control`, ADR 0052)
- [x] T-AI-007: Wire the `AI` section to include every eligible non-ignored Git change, invoke AI message generation, and stop before commit. (Plan `PLAN-three-section-ai-commit-push-control`, ADR 0052)
- [x] T-COMMIT-006: Wire the `Commit` section to run the `AI` section behavior and then commit after successful AI generation and normal IDE commit checks. (Plan `PLAN-three-section-ai-commit-push-control`, ADR 0052)
- [x] T-COMMIT-007: Wire the `Push` section to run the `Commit` section behavior and then push with the ADR 0047 safe immediate push fallback. (Plan `PLAN-three-section-ai-commit-push-control`, ADR 0052, ADR 0047)
- [x] T-UI-001: Apply the ADR 0053 selected violet AI snake styling reference, including passive, cumulative hover, clicked/running, disabled, light, and dark states. (Plan `PLAN-three-section-ai-commit-push-control`, ADR 0053)
- [x] T-VAL-023: Add or refresh automated, documentation, and manual sandbox validation for `AI`, `Commit`, and `Push` section behavior and rendering. (Plan `PLAN-three-section-ai-commit-push-control`, ADR 0052, ADR 0053)

## Completed Task Archive

Archived as of orchestrated `AI Commit All` workflow implementation.

### Register Commit Tool Window Actions

- [x] T-ACTIONS-001: Create final SVG icon assets from AI-generated base concepts using IntelliJ Platform icon guidelines. (Plan `PLAN-commit-tool-window-actions`, Task 1)
- [x] T-ACTIONS-002: Add light and dark icon variants when needed. (Plan `PLAN-commit-tool-window-actions`, Task 1)
- [x] T-ACTIONS-004: Add split button to the Commit tool window primary actions group with `AI Commit All` and `& Push` segments. (Plan `PLAN-commit-tool-window-actions`, Task 2)
- [x] T-ACTIONS-005: Wire the `AI Commit All` split-button segment to the commit-only flow. (Plan `PLAN-commit-tool-window-actions`, Task 3)
- [x] T-ACTIONS-006: Wire the `& Push` split-button segment to the commit-and-push flow. (Plan `PLAN-commit-tool-window-actions`, Task 3)
- [x] T-ACTIONS-007: Ensure actions are visible only when a project has an active Git commit workflow. (Plan `PLAN-commit-tool-window-actions`, Task 4)
- [x] T-ACTIONS-008: Disable actions when no non-ignored committable files exist. (Plan `PLAN-commit-tool-window-actions`, Task 4)

### Include All Files

- [x] T-FILES-001: Read all tracked Git file changes from `ChangeListManager` across all changelists and Git roots, including modified, added, deleted, moved or renamed, and other committable change types. (Plan `PLAN-include-all-git-files`, Task 1)
- [x] T-FILES-002: Read all non-ignored unversioned Git file paths from `ChangeListManager` across all Git roots. (Plan `PLAN-include-all-git-files`, Task 2)
- [x] T-FILES-003: Include resolved conflict paths when IntelliJ exposes them as committable. (Plan `PLAN-include-all-git-files`, Task 1)
- [x] T-FILES-004: Activate the non-modal commit workflow. (Plan `PLAN-include-all-git-files`, Task 3)
- [x] T-FILES-005: Set commit state so every non-ignored eligible file is included across all Git roots. (Plan `PLAN-include-all-git-files`, Task 3)
- [x] T-FILES-006: Support changes spread across multiple changelists. (Plan `PLAN-include-all-git-files`, Tasks 1 and 4)
- [x] T-FILES-007: Support Git staging area enabled and disabled. (Plan `PLAN-include-all-git-files`, Task 4)

### Trigger AI Commit Message Generation

- [x] T-AI-001: Locate JetBrains AI Assistant's commit-message action through the IntelliJ action system. (Plan `PLAN-ai-assistant-message-generation`, Task 1)
- [x] T-AI-002: Prefer known action IDs if available. (Plan `PLAN-ai-assistant-message-generation`, Task 1)
- [x] T-AI-003: Fallback to searching `Vcs.MessageActionGroup` / commit toolbar actions by presentation text. (Plan `PLAN-ai-assistant-message-generation`, Task 1)
- [x] T-AI-004: Invoke the action with a data context containing project, commit workflow handler, commit UI, and commit message control. (Plan `PLAN-ai-assistant-message-generation`, Task 2)
- [x] T-AI-005: Show split-button progress or activity animation while AI generation is running. (Plan `PLAN-ai-generation-completion`, Task 4; ADR 0027)
- [x] T-AI-006: Let AI Assistant show its standard sign-in, unavailable, or generation failure messages where possible. (Plan `PLAN-ai-assistant-message-generation`, Task 3)

### Wait For AI Completion

- [x] T-WAIT-001: Capture the commit message before invoking AI. (Plan `PLAN-ai-generation-completion`, Task 1)
- [x] T-WAIT-002: Detect AI Assistant action or generation completion through a reliable action, callback, UI state, or commit-message-generation signal. (Plan `PLAN-ai-generation-completion`, Task 1)
- [x] T-WAIT-003: Use commit message field polling only as supporting evidence when no stronger completion signal is available, using the configured completion-check interval. (Plan `PLAN-ai-generation-completion`, Task 1)
- [x] T-WAIT-004: Treat generation as complete only after AI completion is detected and the message is non-empty and changed. (Plan `PLAN-ai-generation-completion`, Task 1)
- [x] T-WAIT-005: Add a Settings-configurable completion-check interval with a 500 ms default. (Plan `PLAN-ai-generation-completion`, Task 2)
- [x] T-WAIT-006: Add a Settings-configurable timeout with a 5 second default and report failure without committing. (Plan `PLAN-ai-generation-completion`, Task 2)
- [x] T-WAIT-007: Stop without committing or pushing if the user edits or clears the message during generation. (Plan `PLAN-ai-generation-completion`, Task 3)

### Commit And Push

- [x] T-COMMIT-001: Commit all included files through the current commit workflow. (Plan `PLAN-commit-and-push-execution`, Task 1)
- [x] T-COMMIT-002: For push flow, use Git's commit-and-push executor when available. (Plan `PLAN-commit-and-push-execution`, Task 2)
- [x] T-COMMIT-003: Respect existing before-commit checks. (Plan `PLAN-commit-and-push-execution`, Task 1)
- [x] T-COMMIT-004: Do not bypass commit confirmation/errors from the IDE. (Plan `PLAN-commit-and-push-execution`, Task 1)
- [x] T-COMMIT-005: Report unsupported non-Git project state or unavailable push executor using standard platform messages where available. (Plan `PLAN-commit-and-push-execution`, Task 3)

### Error Handling And UX

- [x] T-ERROR-001: Add notification group only for plugin-owned states that do not have platform-owned messages. (Plan `PLAN-error-handling-ux`, Task 1)
- [x] T-ERROR-002: Handle frozen changelist manager state. (Plan `PLAN-error-handling-ux`, Task 2)
- [x] T-ERROR-003: Handle background VCS operations already running. (Plan `PLAN-error-handling-ux`, Task 2)
- [x] T-ERROR-004: Handle AI timeout. (Plan `PLAN-error-handling-ux`, Task 3)
- [x] T-ERROR-005: Handle empty commit state. (Plan `PLAN-error-handling-ux`, Task 3)
- [x] T-ERROR-006: Handle commit failures without retry loops, forwarding platform errors where possible. (Plan `PLAN-error-handling-ux`, Task 4)
- [x] T-ERROR-007: Document any new plugin-owned notification text when implementation exposes an unavoidable non-standard error path. (Plan `PLAN-error-handling-ux`, Task 1 and Task 3)
- [x] T-ERROR-008: Add a new open question and placeholder if implementation reveals a risk not covered by standard IDE safeguards. (Plan `PLAN-error-handling-ux`, Task 5; no uncovered risk was found)

### Validation

- [x] T-VAL-001: Run Gradle build. (Plan `PLAN-scaffold-plugin-project`, Task 3)
- [x] T-VAL-002: Run plugin verifier for target IDE versions. (Plan `PLAN-validation-coverage`, Task 1 and `PLAN-marketplace-ci-release`, Task 4)
- [x] T-VAL-003: Keep manual sandbox Git project validation in the scenario record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-004: Record exact current IDE product names and build numbers used for manual validation. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-005: Cover modified tracked files through automated local Git validation and manual sandbox records. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-006: Cover unversioned files through automated local Git validation and manual sandbox records. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-007: Cover deleted files through automated local Git validation and manual sandbox records. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-008: Cover moved or renamed files through automated local Git validation and manual sandbox records. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-009: Keep multi-changelist sandbox validation in the scenario record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-010: Cover multiple Git roots through automated local Git validation and manual sandbox records. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-011: Cover ignored-file exclusion through automated local Git validation and manual sandbox records. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-012: Keep commit-only sandbox validation in the scenario record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-013: Cover local-only commit-and-push validation and keep sandbox validation in the scenario record. (Plan `PLAN-validation-coverage`, Tasks 2 and 3)
- [x] T-VAL-014: Keep light and dark icon rendering validation in the manual sandbox record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-015: Keep missing or disabled JetBrains AI Assistant validation in the manual sandbox record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-016: Keep AI Assistant unavailable or not-signed-in validation in the manual sandbox record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-017: Keep Git staging area enabled and disabled validation in the manual sandbox record. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-018: Record current stable JetBrains IDE builds available through All Products Pack. (Plan `PLAN-validation-coverage`, Task 3)
- [x] T-VAL-019: Add local-repository end-to-end tests where practical. (Plan `PLAN-validation-coverage`, Task 2)
- [x] T-VAL-020: Add local-repository E2E coverage for modified, added, deleted, moved or renamed, unversioned, ignored, multi-changelist, and multi-root states. (Plan `PLAN-validation-coverage`, Task 2)
- [x] T-VAL-021: Add local-repository E2E coverage for commit-only and commit-and-push using a local remote where safe. (Plan `PLAN-validation-coverage`, Task 2)
- [x] T-VAL-022: Keep manual sandbox scenarios for E2E cases that cannot be automated reliably yet. (Plan `PLAN-validation-coverage`, Task 3)

### Documentation

- [x] T-DOC-001: Update `README.md` with setup and usage instructions. (Plan `PLAN-user-documentation`, Task 1)
- [x] T-DOC-003: Document AI Assistant dependency and limitations. (Plan `PLAN-user-documentation`, Task 1)
- [x] T-DOC-004: Document how to run the sandbox IDE. (Plan `PLAN-user-documentation`, Task 1)
- [x] T-DOC-005: Document known unsupported cases. (Plan `PLAN-user-documentation`, Task 1)
- [x] T-DOC-007: Document source code location for users and contributors once Marketplace metadata exists. (Plan `PLAN-user-documentation`, Task 2)
- [x] T-DOC-008: Document the contributor-facing release and publication process after release automation is configured. (Plan `PLAN-user-documentation`, Task 2)

### Publishing, Signing, Marketplace, And CI

- [x] T-REL-001: Add Marketplace-ready plugin metadata. (Plan `PLAN-marketplace-ci-release`, Task 1)
- [x] T-REL-002: Add official source code link to plugin and Marketplace metadata. (Plan `PLAN-marketplace-ci-release`, Task 1)
- [x] T-REL-003: Configure plugin signing through IntelliJ Platform Gradle Plugin 2.x using local properties or CI secrets. (Plan `PLAN-marketplace-ci-release`, Task 2)
- [x] T-REL-004: Configure `publishPlugin` for official JetBrains Marketplace using a token supplied outside the repository. (Plan `PLAN-marketplace-ci-release`, Task 2)
- [x] T-REL-005: Configure or record first Marketplace upload handling when JetBrains requires manual initial plugin setup. (Plan `PLAN-marketplace-ci-release`, Task 2 and `PLAN-user-documentation`, Task 2)
- [x] T-REL-006: Add CI for build, tests, plugin structure verification, and plugin packaging. (Plan `PLAN-marketplace-ci-release`, Task 3)
- [x] T-REL-007: Add Plugin Verifier CI for target IDE versions. (Plan `PLAN-marketplace-ci-release`, Task 4)
- [x] T-REL-008: Add a gated/manual release workflow for signing and Marketplace publishing. (Plan `PLAN-marketplace-ci-release`, Task 4)
- [x] T-REL-009: Ensure pull-request CI does not require or expose Marketplace tokens, signing keys, or certificate passwords. (Plan `PLAN-marketplace-ci-release`, Task 3)

Archived as of `v0.1.0-alpha.1` release preparation.

### Confirmed Decisions

- [x] T-DEC-001: Choose the minimum supported IntelliJ IDE version: IntelliJ Platform 2026.1 release line. (ADR 0008)
- [x] T-DEC-002: Confirm target IDEs: all JetBrains IDEs with the VCS Commit tool window and compatible commit workflow APIs. (ADR 0008)
- [x] T-DEC-003: Decide whether the first version supports only Git or all VCS integrations: Git only. (ADR 0009)
- [x] T-DEC-004: Decide whether the first version supports projects with multiple VCS roots: yes, for multiple Git roots. (ADR 0009)
- [x] T-DEC-005: Decide whether the action commits automatically after AI generation or pauses for user review: commit automatically. (ADR 0010)
- [x] T-DEC-006: Decide what happens if the user edits or clears the message while AI generation is in progress: stop without committing or pushing. (ADR 0011)
- [x] T-DEC-007: Choose AI completion approach and timeout ownership: detect AI action completion, with timeout configurable in Settings and a 500 ms default supporting check interval. (ADR 0012)
- [x] T-DEC-008: Decide missing or disabled AI Assistant behavior: require JetBrains AI Assistant plugin dependency and fail installation/loading if unavailable. (ADR 0013)
- [x] T-DEC-009: Decide runtime unavailable-AI and non-AI fallback behavior: stop without committing or pushing, with button animation and standard IntelliJ notification. (ADR 0014)
- [x] T-DEC-010: Choose default AI generation timeout setting value: 5 seconds. (ADR 0012)
- [x] T-DEC-011: Choose icon direction: AI-generated base concepts adapted into IntelliJ-style SVG assets. (ADR 0015)
- [x] T-DEC-012: Choose notification and error message policy: reuse or forward standard IntelliJ messages where possible, and decide plugin-owned unexpected cases when concrete paths are reviewed. (ADR 0016)
- [x] T-DEC-013: Choose confirmation behavior: use standard IDE commit and push safeguards, and add a new question with a placeholder if development reveals an uncovered risk. (ADR 0017)
- [x] T-DEC-014: Choose split-button styling decision process: create a series of draft styles before final selection. (ADR 0025; resolves: Q-UX-5)
- [x] T-DEC-015: Choose final split-button styling from the draft series: use a generated placeholder graphic now. (ADR 0027; resolves: Q-UX-6)
- [x] T-DEC-016: Choose plugin ID, package name, vendor name, and vendor contact email. (ADR 0022)
- [x] T-DEC-017: Choose repository and plugin license: Apache-2.0. (ADR 0018)
- [x] T-DEC-018: Decide whether publishing, signing, marketplace metadata, and CI are in this phase: include them for official JetBrains Marketplace open-source publication. (ADR 0019)
- [x] T-DEC-019: Choose sandbox validation IDE versions: current stable JetBrains IDE builds available through All Products Pack. (ADR 0020)
- [x] T-DEC-020: Choose changelist and staging-area coverage: support and test both changelists and Git staging enabled and disabled. (ADR 0020)
- [x] T-DEC-021: Choose acceptance workflows: create local-repository end-to-end tests where practical. (ADR 0021)

### Scaffold Plugin Project

- [x] T-SCAFFOLD-001: Add Gradle Kotlin DSL project files. (Plan `PLAN-scaffold-plugin-project`, Task 1)
- [x] T-SCAFFOLD-002: Configure the IntelliJ Platform Gradle Plugin. (Plan `PLAN-scaffold-plugin-project`, Task 1)
- [x] T-SCAFFOLD-003: Add Kotlin/JVM configuration. (Plan `PLAN-scaffold-plugin-project`, Task 1)
- [x] T-SCAFFOLD-004: Add plugin descriptor at `src/main/resources/META-INF/plugin.xml`. (Plan `PLAN-scaffold-plugin-project`, Task 2)
- [x] T-SCAFFOLD-005: Identify JetBrains AI Assistant plugin dependency ID for IntelliJ Platform 2026.1: `com.intellij.ml.llm`. (Plan `PLAN-scaffold-plugin-project`, Task 2)
- [x] T-SCAFFOLD-006: Declare JetBrains AI Assistant as a required plugin dependency. (Plan `PLAN-scaffold-plugin-project`, Task 2)
- [x] T-SCAFFOLD-007: Add a base package for plugin code. (Plan `PLAN-scaffold-plugin-project`, Task 1)
- [x] T-SCAFFOLD-008: Verify `runIde` starts a sandbox IDE. (Plan `PLAN-scaffold-plugin-project`, Task 3)
- [x] T-SCAFFOLD-009: Add a top-level `LICENSE` file. (ADR 0018)
- [x] T-SCAFFOLD-010: Do not add `NOTICE` initially; add one later only if attribution needs or bundled dependencies require it. (ADR 0018)

### Commit Tool Window Actions

- [x] T-ACTIONS-003: Generate a placeholder split-button styling graphic covering normal, running, disabled, commit-only, and commit-and-push states: `docs/concepts/graphics/split-button-placeholder.png`. (ADR 0027)

### Documentation

- [x] T-DOC-002: Document supported IDE versions. (ADR 0008)
- [x] T-DOC-006: Document license in `README.md`. (ADR 0018)
- [x] T-DOC-009: Add root `CHANGELOG.md` for notable unreleased and released changes. (ADR 0029)
- [x] T-DOC-010: Add root `SUPPORT.md` for support status and issue-reporting expectations. (ADR 0029)
- [x] T-DOC-011: Add rule to avoid automatically loading every AI instruction file. (ADR 0031)
- [x] T-DOC-012: Add stable non-number-only plan IDs. (ADR 0032)
- [x] T-DOC-013: Add `docs/proposals/` with proposal rules, template, and archive marker. (ADR 0033)
- [x] T-DOC-014: Add stable proposal IDs. (ADR 0034)

### Publishing, Signing, Marketplace, And CI

- [x] T-REL-010: Add release guidance for changelog, support-policy, version tag, and release-precondition rules. (ADR 0029)
- [x] T-REL-011: Assign changelog maintenance to the orchestrator during orchestrated plan execution and release preparation. (ADR 0030)

### Bugs

- [x] T-BUG-001: plugin does not work when vsc changelists are disabled and git is in git stage mode

### Ideas

- [x] T-IDEA-002: docs/proposals/README.md does not need to know about archived proposals, docs/proposals/archive/README.md is doing that
- [x] T-IDEA-003: Add an ADR implementation tracker to `docs/decisions/README.md`. (ADR 0048)
- [x] T-IDEA-004: Add proposal implementation status tracking through per-finding status values and a README summary. (ADR 0049)
- [x] T-IDEA-005: Reengineer `docs/decisions/OPEN_QUESTIONS.md` as an active question register. (ADR 0050)
