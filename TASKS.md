# Build Tasks

Notation:

- `resolves: Q-ID` means the task answers an open question.
- `depends on: Q-ID` means the task should wait until that question is answered or explicitly assumed in an accepted plan or ADR.

## 1. Confirm Blocking Decisions

- [ ] Choose the minimum supported IntelliJ IDE version. (resolves: Q-SCOPE-1)
- [ ] Confirm target IDEs: IntelliJ IDEA only, or all JetBrains IDEs with VCS commit UI. (resolves: Q-SCOPE-2)
- [ ] Decide whether the first version supports only Git or all VCS integrations. (resolves: Q-SCOPE-3)
- [ ] Decide whether the first version supports projects with multiple VCS roots. (resolves: Q-SCOPE-4)
- [ ] Decide whether the action commits automatically after AI generation or pauses for user review. (resolves: Q-COMMIT-2)
- [ ] Decide what happens if the user edits or clears the message while AI generation is in progress. (resolves: Q-COMMIT-3)
- [ ] Choose AI completion timeout and stable-message interval. (resolves: Q-AI-1, Q-AI-2)
- [ ] Decide unavailable-AI and non-AI fallback behavior. (resolves: Q-AI-3, Q-AI-4)
- [ ] Choose icons, split-button styling, notification text, and confirmation behavior. (resolves: Q-UX-2, Q-UX-3, Q-UX-4, Q-UX-5)
- [ ] Choose plugin ID, package name, vendor name, and license. (resolves: Q-META-1, Q-META-2)
- [ ] Decide whether publishing, signing, marketplace metadata, or CI are in this phase. (resolves: Q-META-3)
- [ ] Choose sandbox validation IDE versions, staging-area coverage, and acceptance workflows. (resolves: Q-VAL-1, Q-VAL-2, Q-VAL-3)

## 2. Scaffold Plugin Project

- [ ] Add Gradle Kotlin DSL project files. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-META-1)
- [ ] Configure the IntelliJ Platform Gradle Plugin. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-SCOPE-3)
- [ ] Add Kotlin/JVM configuration. (depends on: Q-SCOPE-1)
- [ ] Add plugin descriptor at `src/main/resources/META-INF/plugin.xml`. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-SCOPE-3, Q-META-1, Q-META-2)
- [ ] Add a base package for plugin code. (depends on: Q-META-1)
- [ ] Verify `runIde` starts a sandbox IDE. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-VAL-1)

## 3. Register Commit Tool Window Actions

- [ ] Add split button to the Commit tool window primary actions group with `AI Commit All` and `& Push` segments. (depends on: Q-UX-2)
- [ ] Wire the `AI Commit All` split-button segment to the commit-only flow.
- [ ] Wire the `& Push` split-button segment to the commit-and-push flow. (depends on: Q-SCOPE-3)
- [ ] Ensure actions are visible only when a project has an active VCS commit workflow. (depends on: Q-SCOPE-2, Q-SCOPE-3, Q-SCOPE-4)
- [ ] Disable actions when no non-ignored committable files exist.

## 4. Include All Files

- [ ] Read all tracked file changes from `ChangeListManager` across all changelists, including modified, added, deleted, moved or renamed, and other committable change types. (depends on: Q-SCOPE-3, Q-SCOPE-4)
- [ ] Read all non-ignored unversioned file paths from `ChangeListManager`. (depends on: Q-SCOPE-3, Q-SCOPE-4)
- [ ] Include resolved conflict paths when IntelliJ exposes them as committable. (depends on: Q-SCOPE-3)
- [ ] Activate the non-modal commit workflow. (depends on: Q-SCOPE-1, Q-SCOPE-2)
- [ ] Set commit state so every non-ignored eligible file is included. (depends on: Q-SCOPE-1, Q-SCOPE-3, Q-SCOPE-4)
- [ ] Confirm behavior when Git staging area is enabled. (depends on: Q-SCOPE-3, Q-VAL-2)

## 5. Trigger AI Commit Message Generation

- [ ] Locate JetBrains AI Assistant's commit-message action through the IntelliJ action system. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-AI-3, Q-AI-4)
- [ ] Prefer known action IDs if available. (depends on: Q-SCOPE-1)
- [ ] Fallback to searching `Vcs.MessageActionGroup` / commit toolbar actions by presentation text. (depends on: Q-SCOPE-1, Q-SCOPE-2)
- [ ] Invoke the action with a data context containing project, commit workflow handler, commit UI, and commit message control. (depends on: Q-SCOPE-1, Q-SCOPE-2)
- [ ] Show a clear notification when AI Assistant is missing, disabled, not signed in, or unavailable. (depends on: Q-AI-3, Q-AI-4, Q-UX-3)

## 6. Wait For AI Completion

- [ ] Capture the commit message before invoking AI.
- [ ] Poll the commit message field after AI action invocation.
- [ ] Treat generation as complete only after the message is non-empty, changed, and stable for a short interval. (depends on: Q-AI-2, Q-COMMIT-2)
- [ ] Add a timeout and report failure without committing. (depends on: Q-AI-1, Q-AI-4, Q-UX-3)
- [ ] Avoid committing if the user edits or clears the message during generation. (depends on: Q-COMMIT-3)

## 7. Commit And Push

- [ ] Commit all included files through the current commit workflow. (depends on: Q-COMMIT-2, Q-UX-4)
- [ ] For push flow, use Git's commit-and-push executor when available. (depends on: Q-SCOPE-3)
- [ ] Respect existing before-commit checks. (depends on: Q-UX-4)
- [ ] Do not bypass commit confirmation/errors from the IDE. (depends on: Q-UX-4)
- [ ] Report unavailable push executor for non-Git or unsupported projects. (depends on: Q-SCOPE-3, Q-UX-3)

## 8. Error Handling And UX

- [ ] Add notification group for plugin messages. (depends on: Q-UX-3)
- [ ] Handle frozen changelist manager state. (depends on: Q-UX-3)
- [ ] Handle background VCS operations already running. (depends on: Q-UX-3)
- [ ] Handle AI timeout. (depends on: Q-AI-1, Q-UX-3)
- [ ] Handle empty commit state. (depends on: Q-UX-3)
- [ ] Handle commit failures without retry loops. (depends on: Q-UX-3, Q-UX-4)

## 9. Validation

- [ ] Run Gradle build. (depends on: Q-SCOPE-1)
- [ ] Run plugin verifier for target IDE versions. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-VAL-1)
- [ ] Manually test in sandbox IDE with Git project. (depends on: Q-SCOPE-3, Q-VAL-1, Q-VAL-3)
- [ ] Test with modified tracked files.
- [ ] Test with unversioned files.
- [ ] Test with deleted files.
- [ ] Test with moved or renamed files.
- [ ] Test with files in multiple changelists.
- [ ] Test that ignored files are excluded.
- [ ] Test with Commit only. (depends on: Q-COMMIT-2)
- [ ] Test with Commit and Push. (depends on: Q-SCOPE-3)
- [ ] Test with AI Assistant unavailable. (depends on: Q-AI-3, Q-AI-4)
- [ ] Test with Git staging area enabled and disabled. (depends on: Q-SCOPE-3, Q-VAL-2)

## 10. Documentation

- [ ] Update `README.md` with setup and usage instructions. (depends on: Q-SCOPE-1, Q-SCOPE-2, Q-META-1, Q-META-2)
- [ ] Document supported IDE versions. (depends on: Q-SCOPE-1, Q-SCOPE-2)
- [ ] Document AI Assistant dependency and limitations. (depends on: Q-AI-3, Q-AI-4)
- [ ] Document how to run the sandbox IDE. (depends on: Q-SCOPE-1, Q-VAL-1)
- [ ] Document known unsupported cases. (depends on: Q-SCOPE-2, Q-SCOPE-3, Q-SCOPE-4, Q-AI-3, Q-AI-4, Q-META-3)
