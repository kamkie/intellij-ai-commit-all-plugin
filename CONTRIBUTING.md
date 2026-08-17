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

- JDK 25.
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
.\gradlew.bat verifyPlugin -PpluginVerifierIdeVersions="IU-2026.2,PY-2026.2,WS-2026.2"
```

PyCharm 2026.2 is published, so the complete verifier command now covers all
three required targets. Do not remove, skip, or ignore the PyCharm lane;
release readiness requires the unchanged matrix to pass.

### Updating IntelliJ Patch Coordinates

For a patch update within the approved IntelliJ release line, supply the exact
IntelliJ Platform version and a known-compatible AI Assistant build:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\update-intellij-patch.ps1 `
    -PlatformVersion 2026.2.1 `
    -AiAssistantPluginVersion 262.9000.1
```

The command validates the requested versions against `platformReleaseLine` and
`pluginSinceBuild`, then atomically changes only `platformVersion` and
`aiAssistantPluginVersion` in `gradle.properties`. It preserves UTF-8 content,
LF line endings, comments, property order, and unrelated values, and refuses
missing or duplicate contract keys. It then runs the version contract, focused
patch-aware harness tests, formatting, plugin packaging, Plugin Verifier, and
the PyCharm UI smoke lane.

Invalid input fails before mutation. A later validation failure leaves the
two-property working-tree diff visible for review; the command does not roll it
back, stage it, commit it, or push it. It does not discover an AI Assistant
version. A release-line update such as `2026.2` to `2026.3` must stop here and
follow the separately reviewed compatibility-upgrade process.

To run the pull-request CI workflow locally through `nektos/act`, keep Docker running and use:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\run-act.ps1
```

The wrapper uses a globally installed `act` when available, otherwise it downloads the current `nektos/act` release into `.tools/act/`. The default run is equivalent to:

```powershell
act pull_request --workflows .github/workflows/ci.yml --job build
```

The wrapper enables `act --rm` unless `--reuse`/`-r` is passed, so failed workflow containers are cleaned up automatically. Multi-product release-matrix UI runs are split into one local `act` invocation per IDE product because local matrix containers share host ports.

Pass `act` arguments after the script name for narrower checks, for example:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\run-act.ps1 pull_request --list
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\run-act.ps1 pull_request --workflows .github/workflows/plugin-verifier.yml --job verify --matrix ide:IU-2026.2
```

Local `act` runs skip GitHub-hosted reporting and artifact-upload steps such as CodeQL SARIF upload, unit-test check publishing, and Codecov OIDC upload. Release-matrix UI runs also prepare the local `act` container with Xvfb and the Linux UI libraries needed by the IDE, restore the Gradle wrapper execute bit after the Windows-to-Linux file copy, and skip the per-product coverage report build and Codecov upload because `act` does not provide GitHub's Codecov OIDC token. Reports and plugin ZIPs remain in `build/` locally. Release publication, dependency submission, and Marketplace signing still require GitHub-hosted workflows and repository secrets.

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
