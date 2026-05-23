# Troubleshooting

Use this reference when validation, sandbox runs, plugin packaging, or manual IntelliJ checks fail.

## Start

- Preserve the first failure output before rerunning commands.
- Identify whether the failure is documentation, Gradle, Kotlin, IntelliJ Platform, sandbox IDE, VCS workflow, or environment setup.
- Prefer the narrowest command that reproduces the failure.
- Do not weaken tests, validation, plugin descriptors, signing checks, or Marketplace metadata to make a failure disappear.
- Record commands, exit status, relevant log path, and any skipped broader validation in the handoff.

## Documentation Validation

- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1`.
- Run `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1` when the failure mentions `.agents/references`, `.agents/skills`, `.agents/prompts`, `.agents/plans`, or agent-artifact links.
- Fix missing ADR index rows, implementation tracker rows, prompt catalog rows, broken local links, malformed refs, or Markdown lint failures at the owning artifact.
- Do not silence markdownlint or remove governance checks unless a new accepted ADR changes the validation policy.

## Gradle And Formatting

- Run `.\gradlew.bat spotlessCheck` for Kotlin and Gradle Kotlin DSL formatting failures.
- Run `.\gradlew.bat spotlessApply` only for mechanical formatting fixes, then inspect the diff.
- If Gradle cannot start, check the local JDK, `JAVA_HOME`, wrapper files, and Gradle user-home errors before editing build logic.
- If dependency resolution fails, preserve the exact module, repository, and version from the first failure.

## IntelliJ Plugin Packaging

- Run `.\gradlew.bat buildPlugin` when plugin descriptors, resources, Gradle IntelliJ Platform configuration, Kotlin code, or packaging changed.
- Run `.\gradlew.bat verifyPlugin` when configured or when compatibility metadata changed.
- Check `src/main/resources/META-INF/plugin.xml` for plugin ID, dependency, action, and extension-point errors.
- Check Gradle IntelliJ Platform plugin configuration before changing source code for packaging-only failures.

## Sandbox IDE

- Run `.\gradlew.bat runIde` for manual sandbox reproduction when the problem involves IDE UI, actions, Commit tool window integration, notifications, or VCS workflow.
- Keep sandbox logs, exact IDE build, installed plugins, and reproduction steps together.
- For UI freezes or slow actions, check Event Dispatch Thread usage, background task boundaries, read/write actions, and cancellation handling.
- For missing actions, check `plugin.xml`, action group placement, dumb-aware behavior, plugin dependencies, and whether the sandbox loaded the latest built plugin.

## Tests

- Rerun the narrowest failing test first:

```powershell
.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.package.ClassTest"
```

- Preserve the first failing stack trace and test report path.
- If the same test alternates pass/fail without source changes, use `.agents/skills/triage-flaky-test/SKILL.md`.
- Check for hidden order dependencies, shared temp directories, real remotes, sleeps, timing assumptions, global service state, leaked disposables, and platform fixture lifecycle issues.

## IDE Logs

- Ask before inspecting local IDE logs unless the user already granted permission for the specific log folder.
- Remind the user not to share secrets, tokens, proprietary source content, or private commit messages.
- For IntelliJ IDEA on Windows, the common log folder is `%LOCALAPPDATA%\JetBrains\IntelliJIdea<version>\log`; ask the user for the exact folder when the IDE version or product is unclear.
- Prefer `idea.log` and recent rotated logs over broad local search.
- Capture only the lines needed to explain the failure.
