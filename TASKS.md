# Build Tasks

## 1. Confirm Scope And Target

- [ ] Choose the minimum supported IntelliJ IDE version.
- [ ] Confirm target IDEs: IntelliJ IDEA only, or all JetBrains IDEs with VCS commit UI.
- [ ] Decide whether the first version supports only Git or all VCS integrations.
- [ ] Confirm whether "push was selected" means a separate `AI Commit & Push All` button or reuse of the existing Commit/Commit and Push choice.

## 2. Scaffold Plugin Project

- [ ] Add Gradle Kotlin DSL project files.
- [ ] Configure the IntelliJ Platform Gradle Plugin.
- [ ] Add Kotlin/JVM configuration.
- [ ] Add plugin descriptor at `src/main/resources/META-INF/plugin.xml`.
- [ ] Add a base package for plugin code.
- [ ] Verify `runIde` starts a sandbox IDE.

## 3. Register Commit Tool Window Actions

- [ ] Add `AI Commit All` action to the Commit tool window primary actions group.
- [ ] Add `AI Commit & Push All` action if a separate push action is chosen.
- [ ] Ensure actions are visible only when a project has an active VCS commit workflow.
- [ ] Disable actions when no changed, unversioned, or resolved-conflict files exist.

## 4. Include All Files

- [ ] Read all changed files from `ChangeListManager`.
- [ ] Read all unversioned file paths from `ChangeListManager`.
- [ ] Include resolved conflict paths when relevant.
- [ ] Activate the non-modal commit workflow.
- [ ] Set commit state so every eligible file is included.
- [ ] Confirm behavior when Git staging area is enabled.

## 5. Trigger AI Commit Message Generation

- [ ] Locate JetBrains AI Assistant's commit-message action through the IntelliJ action system.
- [ ] Prefer known action IDs if available.
- [ ] Fallback to searching `Vcs.MessageActionGroup` / commit toolbar actions by presentation text.
- [ ] Invoke the action with a data context containing project, commit workflow handler, commit UI, and commit message control.
- [ ] Show a clear notification when AI Assistant is missing, disabled, not signed in, or unavailable.

## 6. Wait For AI Completion

- [ ] Capture the commit message before invoking AI.
- [ ] Poll the commit message field after AI action invocation.
- [ ] Treat generation as complete only after the message is non-empty, changed, and stable for a short interval.
- [ ] Add a timeout and report failure without committing.
- [ ] Avoid committing if the user edits or clears the message during generation.

## 7. Commit And Push

- [ ] Commit all included files through the current commit workflow.
- [ ] For push flow, use Git's commit-and-push executor when available.
- [ ] Respect existing before-commit checks.
- [ ] Do not bypass commit confirmation/errors from the IDE.
- [ ] Report unavailable push executor for non-Git or unsupported projects.

## 8. Error Handling And UX

- [ ] Add notification group for plugin messages.
- [ ] Handle frozen changelist manager state.
- [ ] Handle background VCS operations already running.
- [ ] Handle AI timeout.
- [ ] Handle empty commit state.
- [ ] Handle commit failures without retry loops.

## 9. Validation

- [ ] Run Gradle build.
- [ ] Run plugin verifier for target IDE versions.
- [ ] Manually test in sandbox IDE with Git project.
- [ ] Test with modified tracked files.
- [ ] Test with unversioned files.
- [ ] Test with Commit only.
- [ ] Test with Commit and Push.
- [ ] Test with AI Assistant unavailable.
- [ ] Test with Git staging area enabled and disabled.

## 10. Documentation

- [ ] Update `README.md` with setup and usage instructions.
- [ ] Document supported IDE versions.
- [ ] Document AI Assistant dependency and limitations.
- [ ] Document how to run the sandbox IDE.
- [ ] Document known unsupported cases.

