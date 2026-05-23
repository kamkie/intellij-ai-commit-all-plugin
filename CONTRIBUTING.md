# Contributing

This repository is an unreleased IntelliJ Platform plugin project. Keep contributions focused, traceable, and validated against the current support scope.

## Before You Start

- Read [README.md](README.md) for the concise product overview and user entry points.
- Read [docs/user-guide.md](docs/user-guide.md) for user workflow behavior.
- Read [docs/SUPPORT.md](docs/SUPPORT.md) before filing bugs or support requests.
- Use [docs/DEVELOPMENT_LIFECYCLE.md](docs/DEVELOPMENT_LIFECYCLE.md) for changes that affect behavior, repository workflow, release automation, or multiple files.
- Check [TASKS.md](TASKS.md), [docs/decisions/](docs/decisions/), and [docs/proposals/](docs/proposals/) for existing tasks, accepted decisions, and active proposals.
- If you plan to use AI assistance, read [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md) for request examples and useful prompt names.
- Do not include secrets, tokens, private repository contents, proprietary commit messages, or unsanitized logs.

## Prerequisites

- JDK 21.
- Node.js with `npx` for Markdown linting in documentation validation.
- Git for local repository validation and development fixtures.

## Local Setup

Build the plugin ZIP for local installation:

```powershell
.\gradlew.bat buildPlugin
```

Install `build/distributions/ai-commit-all-<version>.zip` from
`Settings | Plugins | Install Plugin from Disk...` in a supported JetBrains IDE.

For day-to-day plugin development, run a sandbox IDE:

```powershell
.\gradlew.bat runIde
```

## Local Validation

Use the smallest validation set that matches the change. Common commands are:

```powershell
.\gradlew.bat spotlessCheck
.\gradlew.bat spotlessApply
.\gradlew.bat detekt
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1
.\gradlew.bat test
.\gradlew.bat jacocoTestReport
.\gradlew.bat verifyJacocoCoverageReport
.\gradlew.bat verifyPluginStructure
.\gradlew.bat buildPlugin
.\gradlew.bat runIde
```

`spotlessApply` applies mechanical source formatting and Kotlin license-header fixes. `detekt` runs Kotlin static analysis against the checked-in baseline.

Run the IntelliJ Plugin Verifier locally with the default verifier target from `gradle.properties`, or against the CI matrix:

```powershell
.\gradlew.bat verifyPlugin
.\gradlew.bat verifyPlugin -PpluginVerifierIdeVersions="IU-2026.1.1,PY-2026.1.1,WS-2026.1.1"
```

For documentation-only changes, run documentation validation and `git diff --check`. For plugin code changes, include targeted tests and build checks. For release, signing, publishing, or supported-scope changes, also review [docs/validation/release-checklist.md](docs/validation/release-checklist.md).

## Pull Requests

- Keep pull requests scoped to one task, proposal finding, or plan task when practical.
- Explain the user-facing behavior, repository workflow, or documentation outcome.
- Include validation results in the pull request.
- Update public docs, support policy, changelog, ADRs, proposals, or tasks when the change affects them.
- Follow the commit-message shape in [.gitmessage](.gitmessage) for local commits.

## Issues

Use the bug or feature request templates. Bug reports should include IDE product and build, operating system, plugin version or commit, JetBrains AI Assistant state, Git root layout, changelist or staging-area mode, reproduction steps, expected behavior, and actual behavior.

For real IDE manual-test failures, provide sanitized logs when possible. The IDE can open its logs from `Help | Show Log in Explorer`; on Windows, logs are usually under `%LOCALAPPDATA%\JetBrains\<Product><Version>\log`.

## Security Reports

Do not report vulnerabilities or exposed secrets in public issues or pull requests. Follow [SECURITY.md](SECURITY.md).
