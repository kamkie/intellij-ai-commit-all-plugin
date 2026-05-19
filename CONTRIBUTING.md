# Contributing

This repository is an unreleased IntelliJ Platform plugin project. Keep contributions focused, traceable, and validated against the current support scope.

## Before You Start

- Read [README.md](README.md) for setup, usage, build commands, and current limitations.
- Read [SUPPORT.md](SUPPORT.md) before filing bugs or support requests.
- Use [docs/DEVELOPMENT_LIFECYCLE.md](docs/DEVELOPMENT_LIFECYCLE.md) for changes that affect behavior, repository workflow, release automation, or multiple files.
- Check [TASKS.md](TASKS.md), [docs/decisions/](docs/decisions/), and [docs/proposals/](docs/proposals/) for existing tasks, accepted decisions, and active proposals.
- Do not include secrets, tokens, private repository contents, proprietary commit messages, or unsanitized logs.

## Local Validation

Use the smallest validation set that matches the change. Common commands are:

```powershell
.\gradlew.bat spotlessCheck
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1
.\gradlew.bat test
.\gradlew.bat jacocoTestReport
.\gradlew.bat buildPlugin
.\gradlew.bat runIde
```

For documentation-only changes, run documentation validation and `git diff --check`. For plugin code changes, include targeted tests and build checks. For release, signing, publishing, or supported-scope changes, also review [docs/validation/manual-sandbox.md](docs/validation/manual-sandbox.md) and [.agents/references/releases.md](.agents/references/releases.md).

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
