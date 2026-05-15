# Build Tasks

Repository state: documentation, planning, and the initial executable Gradle/Kotlin IntelliJ plugin scaffold are present. Runtime `AI Commit All` workflow implementation is still pending.

Completed task entries are preserved in `## Completed Task Archive` after the open backlog.

Notation:

- Every task starts with a stable `T-AREA-NNN` ID. Keep IDs stable when wording, status, or ordering changes. Do not renumber existing IDs.
- `resolves: Q-ID` means the task answers an open question.
- `depends on: Q-ID` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### 1. Register Commit Tool Window Actions

- [ ] T-ACTIONS-001: Create final SVG icon assets from AI-generated base concepts using IntelliJ Platform icon guidelines.
- [ ] T-ACTIONS-002: Add light and dark icon variants when needed.
- [ ] T-ACTIONS-004: Add split button to the Commit tool window primary actions group with `AI Commit All` and `& Push` segments.
- [ ] T-ACTIONS-005: Wire the `AI Commit All` split-button segment to the commit-only flow.
- [ ] T-ACTIONS-006: Wire the `& Push` split-button segment to the commit-and-push flow.
- [ ] T-ACTIONS-007: Ensure actions are visible only when a project has an active Git commit workflow.
- [ ] T-ACTIONS-008: Disable actions when no non-ignored committable files exist.

### 2. Include All Files

- [ ] T-FILES-001: Read all tracked Git file changes from `ChangeListManager` across all changelists and Git roots, including modified, added, deleted, moved or renamed, and other committable change types.
- [ ] T-FILES-002: Read all non-ignored unversioned Git file paths from `ChangeListManager` across all Git roots.
- [ ] T-FILES-003: Include resolved conflict paths when IntelliJ exposes them as committable.
- [ ] T-FILES-004: Activate the non-modal commit workflow.
- [ ] T-FILES-005: Set commit state so every non-ignored eligible file is included across all Git roots.
- [ ] T-FILES-006: Support changes spread across multiple changelists.
- [ ] T-FILES-007: Support Git staging area enabled and disabled.

### 3. Trigger AI Commit Message Generation

- [ ] T-AI-001: Locate JetBrains AI Assistant's commit-message action through the IntelliJ action system.
- [ ] T-AI-002: Prefer known action IDs if available.
- [ ] T-AI-003: Fallback to searching `Vcs.MessageActionGroup` / commit toolbar actions by presentation text.
- [ ] T-AI-004: Invoke the action with a data context containing project, commit workflow handler, commit UI, and commit message control.
- [ ] T-AI-005: Show split-button progress or activity animation while AI generation is running. (ADR 0027)
- [ ] T-AI-006: Let AI Assistant show its standard sign-in, unavailable, or generation failure messages where possible.

### 4. Wait For AI Completion

- [ ] T-WAIT-001: Capture the commit message before invoking AI.
- [ ] T-WAIT-002: Detect AI Assistant action or generation completion through a reliable action, callback, UI state, or commit-message-generation signal.
- [ ] T-WAIT-003: Use commit message field polling only as supporting evidence when no stronger completion signal is available, using the configured completion-check interval.
- [ ] T-WAIT-004: Treat generation as complete only after AI completion is detected and the message is non-empty and changed.
- [ ] T-WAIT-005: Add a Settings-configurable completion-check interval with a 500 ms default.
- [ ] T-WAIT-006: Add a Settings-configurable timeout with a 5 second default and report failure without committing.
- [ ] T-WAIT-007: Stop without committing or pushing if the user edits or clears the message during generation.

### 5. Commit And Push

- [ ] T-COMMIT-001: Commit all included files through the current commit workflow.
- [ ] T-COMMIT-002: For push flow, use Git's commit-and-push executor when available.
- [ ] T-COMMIT-003: Respect existing before-commit checks.
- [ ] T-COMMIT-004: Do not bypass commit confirmation/errors from the IDE.
- [ ] T-COMMIT-005: Report unsupported non-Git project state or unavailable push executor using standard platform messages where available.

### 6. Error Handling And UX

- [ ] T-ERROR-001: Add notification group only for plugin-owned states that do not have platform-owned messages.
- [ ] T-ERROR-002: Handle frozen changelist manager state.
- [ ] T-ERROR-003: Handle background VCS operations already running.
- [ ] T-ERROR-004: Handle AI timeout.
- [ ] T-ERROR-005: Handle empty commit state.
- [ ] T-ERROR-006: Handle commit failures without retry loops, forwarding platform errors where possible.
- [ ] T-ERROR-007: Document any new plugin-owned notification text when implementation exposes an unavoidable non-standard error path.
- [ ] T-ERROR-008: Add a new open question and placeholder if implementation reveals a risk not covered by standard IDE safeguards.

### 7. Validation

- [ ] T-VAL-002: Run plugin verifier for target IDE versions.
- [ ] T-VAL-003: Manually test in sandbox IDE with Git project.
- [ ] T-VAL-004: Record exact current IDE product names and build numbers used for manual validation.
- [ ] T-VAL-005: Test with modified tracked files.
- [ ] T-VAL-006: Test with unversioned files.
- [ ] T-VAL-007: Test with deleted files.
- [ ] T-VAL-008: Test with moved or renamed files.
- [ ] T-VAL-009: Test with files in multiple changelists.
- [ ] T-VAL-010: Test with files across multiple Git roots.
- [ ] T-VAL-011: Test that ignored files are excluded.
- [ ] T-VAL-012: Test with Commit only.
- [ ] T-VAL-013: Test with Commit and Push.
- [ ] T-VAL-014: Test icon rendering in light and dark themes.
- [ ] T-VAL-015: Test install/load failure when JetBrains AI Assistant dependency is missing or disabled.
- [ ] T-VAL-016: Test with AI Assistant present but runtime unavailable or not signed in.
- [ ] T-VAL-017: Test with Git staging area enabled and disabled.
- [ ] T-VAL-018: Test in current stable JetBrains IDE builds available through All Products Pack.
- [ ] T-VAL-019: Add local-repository end-to-end tests where practical.
- [ ] T-VAL-020: Add local-repository E2E coverage for modified, added, deleted, moved or renamed, unversioned, ignored, multi-changelist, and multi-root states.
- [ ] T-VAL-021: Add local-repository E2E coverage for commit-only and commit-and-push using a local remote where safe.
- [ ] T-VAL-022: Keep manual sandbox scenarios for E2E cases that cannot be automated reliably yet.

### 8. Documentation

- [ ] T-DOC-001: Update `README.md` with setup and usage instructions.
- [ ] T-DOC-003: Document AI Assistant dependency and limitations.
- [ ] T-DOC-004: Document how to run the sandbox IDE.
- [ ] T-DOC-005: Document known unsupported cases.
- [ ] T-DOC-007: Document source code location for users and contributors once Marketplace metadata exists.
- [ ] T-DOC-008: Document the contributor-facing release and publication process after release automation is configured.

### 9. Publishing, Signing, Marketplace, And CI

- [ ] T-REL-001: Add Marketplace-ready plugin metadata.
- [ ] T-REL-002: Add official source code link to plugin and Marketplace metadata.
- [ ] T-REL-003: Configure plugin signing through IntelliJ Platform Gradle Plugin 2.x using local properties or CI secrets.
- [ ] T-REL-004: Configure `publishPlugin` for official JetBrains Marketplace using a token supplied outside the repository.
- [ ] T-REL-005: Configure or record first Marketplace upload handling when JetBrains requires manual initial plugin setup.
- [ ] T-REL-006: Add CI for build, tests, plugin structure verification, and plugin packaging.
- [ ] T-REL-007: Add Plugin Verifier CI for target IDE versions.
- [ ] T-REL-008: Add a gated/manual release workflow for signing and Marketplace publishing.
- [ ] T-REL-009: Ensure pull-request CI does not require or expose Marketplace tokens, signing keys, or certificate passwords.

## Completed Task Archive

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

### Validation

- [x] T-VAL-001: Run Gradle build. (Plan `PLAN-scaffold-plugin-project`, Task 3)

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
