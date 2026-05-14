# Build Tasks

Notation:

- `resolves: Q-ID` means the task answers an open question.
- `depends on: Q-ID` means the task should wait until that question is answered or explicitly assumed in an accepted plan or ADR.

## 1. Confirm Blocking Decisions

- [x] Choose the minimum supported IntelliJ IDE version: IntelliJ Platform 2026.1 release line. (ADR 0008)
- [x] Confirm target IDEs: all JetBrains IDEs with the VCS Commit tool window and compatible commit workflow APIs. (ADR 0008)
- [x] Decide whether the first version supports only Git or all VCS integrations: Git only. (ADR 0009)
- [x] Decide whether the first version supports projects with multiple VCS roots: yes, for multiple Git roots. (ADR 0009)
- [x] Decide whether the action commits automatically after AI generation or pauses for user review: commit automatically. (ADR 0010)
- [x] Decide what happens if the user edits or clears the message while AI generation is in progress: stop without committing or pushing. (ADR 0011)
- [x] Choose AI completion approach and timeout ownership: detect AI action completion, with timeout configurable in Settings and a 500 ms default supporting check interval. (ADR 0012)
- [x] Decide missing or disabled AI Assistant behavior: require JetBrains AI Assistant plugin dependency and fail installation/loading if unavailable. (ADR 0013)
- [x] Decide runtime unavailable-AI and non-AI fallback behavior: stop without committing or pushing, with button animation and standard IntelliJ notification. (ADR 0014)
- [x] Choose default AI generation timeout setting value: 5 seconds. (ADR 0012)
- [x] Choose icon direction: AI-generated base concepts adapted into IntelliJ-style SVG assets. (ADR 0015)
- [x] Choose notification and error message policy: reuse or forward standard IntelliJ messages where possible, and decide plugin-owned unexpected cases when concrete paths are reviewed. (ADR 0016)
- [x] Choose confirmation behavior: use standard IDE commit and push safeguards, and add a new question with a placeholder if development reveals an uncovered risk. (ADR 0017)
- [ ] Choose split-button styling. (resolves: Q-UX-5)
- [x] Choose plugin ID, package name, vendor name, and vendor contact email. (ADR 0022)
- [x] Choose repository and plugin license: Apache-2.0. (ADR 0018)
- [x] Decide whether publishing, signing, marketplace metadata, and CI are in this phase: include them for official JetBrains Marketplace open-source publication. (ADR 0019)
- [x] Choose sandbox validation IDE versions: current stable JetBrains IDE builds available through All Products Pack. (ADR 0020)
- [x] Choose changelist and staging-area coverage: support and test both changelists and Git staging enabled and disabled. (ADR 0020)
- [x] Choose acceptance workflows: create local-repository end-to-end tests where practical. (ADR 0021)

## 2. Scaffold Plugin Project

- [ ] Add Gradle Kotlin DSL project files.
- [ ] Configure the IntelliJ Platform Gradle Plugin.
- [ ] Add Kotlin/JVM configuration.
- [ ] Add plugin descriptor at `src/main/resources/META-INF/plugin.xml`.
- [ ] Identify JetBrains AI Assistant plugin dependency ID for IntelliJ Platform 2026.1.
- [ ] Declare JetBrains AI Assistant as a required plugin dependency.
- [ ] Add a base package for plugin code.
- [ ] Verify `runIde` starts a sandbox IDE.
- [x] Add a top-level `LICENSE` file. (ADR 0018)
- [x] Do not add `NOTICE` initially; add one later only if attribution needs or bundled dependencies require it. (ADR 0018)

## 3. Register Commit Tool Window Actions

- [ ] Create final SVG icon assets from AI-generated base concepts using IntelliJ Platform icon guidelines.
- [ ] Add light and dark icon variants when needed.
- [ ] Add split button to the Commit tool window primary actions group with `AI Commit All` and `& Push` segments.
- [ ] Wire the `AI Commit All` split-button segment to the commit-only flow.
- [ ] Wire the `& Push` split-button segment to the commit-and-push flow.
- [ ] Ensure actions are visible only when a project has an active Git commit workflow.
- [ ] Disable actions when no non-ignored committable files exist.

## 4. Include All Files

- [ ] Read all tracked Git file changes from `ChangeListManager` across all changelists and Git roots, including modified, added, deleted, moved or renamed, and other committable change types.
- [ ] Read all non-ignored unversioned Git file paths from `ChangeListManager` across all Git roots.
- [ ] Include resolved conflict paths when IntelliJ exposes them as committable.
- [ ] Activate the non-modal commit workflow.
- [ ] Set commit state so every non-ignored eligible file is included across all Git roots.
- [ ] Support changes spread across multiple changelists.
- [ ] Support Git staging area enabled and disabled.

## 5. Trigger AI Commit Message Generation

- [ ] Locate JetBrains AI Assistant's commit-message action through the IntelliJ action system.
- [ ] Prefer known action IDs if available.
- [ ] Fallback to searching `Vcs.MessageActionGroup` / commit toolbar actions by presentation text.
- [ ] Invoke the action with a data context containing project, commit workflow handler, commit UI, and commit message control.
- [ ] Show split-button progress or activity animation while AI generation is running. (depends on: Q-UX-5)
- [ ] Let AI Assistant show its standard sign-in, unavailable, or generation failure messages where possible.

## 6. Wait For AI Completion

- [ ] Capture the commit message before invoking AI.
- [ ] Detect AI Assistant action or generation completion through a reliable action, callback, UI state, or commit-message-generation signal.
- [ ] Use commit message field polling only as supporting evidence when no stronger completion signal is available, using the configured completion-check interval.
- [ ] Treat generation as complete only after AI completion is detected and the message is non-empty and changed.
- [ ] Add a Settings-configurable completion-check interval with a 500 ms default.
- [ ] Add a Settings-configurable timeout with a 5 second default and report failure without committing.
- [ ] Stop without committing or pushing if the user edits or clears the message during generation.

## 7. Commit And Push

- [ ] Commit all included files through the current commit workflow.
- [ ] For push flow, use Git's commit-and-push executor when available.
- [ ] Respect existing before-commit checks.
- [ ] Do not bypass commit confirmation/errors from the IDE.
- [ ] Report unsupported non-Git project state or unavailable push executor using standard platform messages where available.

## 8. Error Handling And UX

- [ ] Add notification group only for plugin-owned states that do not have platform-owned messages.
- [ ] Handle frozen changelist manager state.
- [ ] Handle background VCS operations already running.
- [ ] Handle AI timeout.
- [ ] Handle empty commit state.
- [ ] Handle commit failures without retry loops, forwarding platform errors where possible.
- [ ] Document any new plugin-owned notification text when implementation exposes an unavoidable non-standard error path.
- [ ] Add a new open question and placeholder if implementation reveals a risk not covered by standard IDE safeguards.

## 9. Validation

- [ ] Run Gradle build.
- [ ] Run plugin verifier for target IDE versions.
- [ ] Manually test in sandbox IDE with Git project.
- [ ] Record exact current IDE product names and build numbers used for manual validation.
- [ ] Test with modified tracked files.
- [ ] Test with unversioned files.
- [ ] Test with deleted files.
- [ ] Test with moved or renamed files.
- [ ] Test with files in multiple changelists.
- [ ] Test with files across multiple Git roots.
- [ ] Test that ignored files are excluded.
- [ ] Test with Commit only.
- [ ] Test with Commit and Push.
- [ ] Test icon rendering in light and dark themes.
- [ ] Test install/load failure when JetBrains AI Assistant dependency is missing or disabled.
- [ ] Test with AI Assistant present but runtime unavailable or not signed in.
- [ ] Test with Git staging area enabled and disabled.
- [ ] Test in current stable JetBrains IDE builds available through All Products Pack.
- [ ] Add local-repository end-to-end tests where practical.
- [ ] Add local-repository E2E coverage for modified, added, deleted, moved or renamed, unversioned, ignored, multi-changelist, and multi-root states.
- [ ] Add local-repository E2E coverage for commit-only and commit-and-push using a local remote where safe.
- [ ] Keep manual sandbox scenarios for E2E cases that cannot be automated reliably yet.

## 10. Documentation

- [ ] Update `README.md` with setup and usage instructions.
- [x] Document supported IDE versions. (ADR 0008)
- [ ] Document AI Assistant dependency and limitations.
- [ ] Document how to run the sandbox IDE.
- [ ] Document known unsupported cases.
- [x] Document license in `README.md`. (ADR 0018)
- [ ] Document Marketplace source code link.
- [ ] Document Marketplace publication process and required secrets.

## 11. Publishing, Signing, Marketplace, And CI

- [ ] Add Marketplace-ready plugin metadata.
- [ ] Add official source code link to Marketplace metadata.
- [ ] Configure plugin signing through IntelliJ Platform Gradle Plugin 2.x using local properties or CI secrets.
- [ ] Configure `publishPlugin` for official JetBrains Marketplace using a token supplied outside the repository.
- [ ] Document that first Marketplace upload may need to be manual before automated Gradle publishing.
- [ ] Add CI for build, tests, plugin structure verification, and plugin packaging.
- [ ] Add Plugin Verifier CI for target IDE versions.
- [ ] Add a gated/manual release workflow for signing and Marketplace publishing.
- [ ] Ensure pull-request CI does not require or expose Marketplace tokens, signing keys, or certificate passwords.
