# Changelog

All notable public plugin-facing changes are documented in this file.

The format is based on Keep a Changelog. Release tags should use semantic version tags in the form `vMAJOR.MINOR.PATCH` for stable releases or `vMAJOR.MINOR.PATCH-PRERELEASE` for prereleases.

This repository has no Marketplace-published plugin version yet. `v0.1.0-beta.1` is the current implementation prerelease candidate prepared from this repository.

This changelog records plugin source and runtime behavior changes, public plugin documentation changes, compatibility and support changes, and CI or release pipeline changes that affect the plugin artifact or publication. Internal AI-agent documentation, repository workflow notes, plans, proposals, ADR maintenance, scenario-register tracking, and test-case inventory changes are intentionally omitted unless they also change public plugin behavior or release artifacts.

## [Unreleased]

### Fixed

- Attached the built plugin distribution ZIP to GitHub Releases created from semantic version tags.
- Fixed Gradle-based GitHub Actions checkouts to fetch tags so Git-derived plugin versions work in CI after semantic release tags.
- Reduced premature AI Commit All stops by boundedly rechecking transient AI Assistant, VCS, Commit tool window, action-routing, and safe-push metadata states before reporting the existing stop or fallback reason.

## [v0.1.0-beta.1] - 2026-05-24

### Changed

- Added generated Marketplace description and change-note metadata from repository source docs, with reviewed control visuals linked from the user guide assets.
- Added an empty-Detekt-baseline guard to Gradle, CI, and release validation so suppressed static-analysis findings cannot return unnoticed.
- Added GitHub Release automation for pushed semantic version tags, with release notes generated from the matching `CHANGELOG.md` release section.
- Expanded Marketplace listing preparation with real-time progress description copy, generated workflow GIF/PNG media, and release checklist validation for Marketplace media rendering.
- Separated Marketplace plugin SemVer from local distribution ZIP naming so release tags drop the leading `v`, post-tag builds use SemVer build metadata, and ZIP filenames omit the `g` prefix before Git hashes.

## [v0.1.0-alpha.10] - 2026-05-24

### Added

- Added Detekt Kotlin static analysis with a checked-in baseline, SARIF uploads to GitHub code scanning, and report artifacts in CI.
- Published Gradle unit test summaries and test report artifacts in GitHub Actions CI.
- Added a Trivy filesystem security workflow with SARIF uploads to GitHub code scanning and report artifacts.

### Changed

- Enabled Gradle configuration cache and made coverage verification cache-compatible for faster local and CI Gradle configuration.
- Updated README validation, prerelease status, CI, and manual validation scope notes to match the current build and workflow configuration.
- Rebuilt public plugin documentation around a concise README, dedicated user guide, troubleshooting FAQ, support policy in `docs/SUPPORT.md`, and a validation-focused behavior specification.
- Reworked validation documentation into a validation index, scenario register, release checklist, and dated validation report structure.
- Expanded the manual release workflow gate to require the requested annotated release tag on `main` and run documentation validation, formatting, Detekt, tests, coverage verification, plugin packaging, and the supported IDE Plugin Verifier matrix before Marketplace signing and publication.
- Validated repository agent artifacts in CI alongside documentation validation.
- Included JetBrains Starter IDE logs and screenshots in Release Matrix UI workflow evidence artifacts.

### Fixed

- Fixed JaCoCo coverage reporting for IntelliJ plugin tests so CI uploads nonzero production coverage instead of an all-uncovered report.
- Replaced the deprecated Codecov test-results action with `codecov-action` test-result uploads and explicit JUnit XML file collection.
- Prevented outgoing-only `Push` from opening the IDE Push dialog when there are no files to commit, while allowing normal tracked-branch outgoing commits to use safe immediate push.
- Removed the deprecated IntelliJ action invocation API from standard commit and push shortcut delegation.
- Waited for IntelliJ smart mode before executing default Commit workflow calls so `Commit` and commit-producing `Push` runs do not stall during project indexing.
- Avoided re-running `git add` on already-staged deleted or renamed paths in the Git staging-area workflow, fixing stale pathspec failures before AI message generation.
- Allowed `Commit` and `Push` to continue when clearing is disabled and AI Assistant reliably completes without changing a non-empty prefilled commit message, while keeping missing completion evidence and empty results fail-closed.

## [v0.1.0-alpha.9] - 2026-05-19

### Changed

- Derive Gradle project and IntelliJ plugin descriptor versions from Git tags and short commit hashes using Palantir `gradle-git-version`.

## [v0.1.0-alpha.8] - 2026-05-19

### Added

- Added a trusted Gradle dependency submission workflow so GitHub Dependency Graph can receive Gradle dependency snapshots from main-branch and manual runs.

### Fixed

- Avoided duplicated standalone Gradle wrapper validation in GitHub Actions while keeping wrapper validation enabled through `setup-gradle`.
- Moved push-only outgoing-commit preparation and post-commit safe immediate pushes off the UI thread while keeping the IDE push fallback on the event dispatch thread.
- Refreshed outgoing-commit availability after Git repository and push completion events so the `Push` section does not stay enabled after outgoing commits are pushed.
- Kept the `Push` section animation active until safe immediate Git pushes report completion instead of stopping immediately after push start.
- Routed the IDE push shortcut, including `Ctrl+Shift+K` on the default Windows/Linux keymap, to the plugin `Push` section while shortcut takeover is enabled.
- Used the safe immediate push path for outgoing-only `Push` clicks before invoking the IDE push workflow, avoiding the Push window for protected tracked branches when no force push is needed.

## [v0.1.0-alpha.7] - 2026-05-18

### Added

- Added CI checks for Gradle wrapper validation, source formatting, Markdown linting, documentation validation, and Kotlin source license headers.
- Added Dependabot, CodeQL scanning, CODEOWNERS, security reporting guidance, and contributor issue and pull request templates.

### Changed

- Replaced the standard Commit tool window `Commit and Push...` toolbar action with the plugin's three-section control, while preserving standard IDE commit-and-push executor and shortcut delegation paths.
- Kept the `Push` section available for already-created outgoing Git commits when there are no committable changes, delegating that case to the IDE push workflow.

### Fixed

- Matched the three-section control corner radius to the IDE button arc so it aligns with neighboring Commit toolbar buttons.
- Progress animation now starts on `AI`, advances to `Commit` after AI generation, switches to `Push` only after commit checks finish, and stays active through the post-commit push handoff.

## [v0.1.0-alpha.6] - 2026-05-18

### Fixed

- Waited through initial AI Assistant startup polling so `Commit` and `Push` workflows do not stop before generation begins.
- Prevented duplicate workflow starts from rapid repeated action or shortcut invocations.
- Allowed safe immediate push on protected branches when no force push is required.
- Bound plugin shortcut actions directly to the IDE commit shortcuts so takeover is registered even when shortcut mirroring is insufficient.
- Cleared commit-message control and document data before AI generation, not only the public `CommitMessageUi` text accessor.
- Moved all-files selection and Git staging confirmation off the UI event thread while returning Commit UI updates and AI invocation to the UI thread, so staging-area mode can reach AI commit-message generation.
- Prevented the running indicator from throwing a `negative dash phase` paint exception after animation starts.
- Increased the default AI generation timeout to 30 seconds so cold AI Assistant requests can finish in the first Commit or Push attempt.

## [v0.1.0-alpha.5] - 2026-05-18

### Fixed

- Preserved Git staging-area paths when synchronizing fallback Commit tool window inclusion, so already staged files are not dropped from the plugin workflow.
- Ensured the three-section control exposes its fallback accessibility description when Swing provides a blank description.

## [v0.1.0-alpha.4] - 2026-05-18

### Fixed

- Refreshed IntelliJ toolbar actions when AI activity starts or finishes so the three-section control can enter and leave its snake running state without waiting for user activity.
- Reloaded externally changed staged files before each Git staging tracker recheck, with bounded retries before AI commit-message generation.

## [v0.1.0-alpha.3] - 2026-05-18

### Changed

- Added default-on IDE commit shortcut takeover for `AI Commit All` `Commit` and `Push` workflows, with a settings opt-out.
- Clear stale commit-message text before AI generation by default, with a settings toggle to keep existing text.
- Implemented the three-section `<AI icon> AI | Commit | Push` Commit tool window control with an AI-only preparation path, commit and push routing, cumulative hover, and violet AI snake running styling.
- Moved the three-section Commit tool window control to the right of the IDE `Commit and Push...` button.

### Fixed

- Confirm Git staging-area inclusion before AI commit-message generation starts, avoiding intermittent first-run generation with incomplete staged input.
- Ensured Commit and Push workflows reuse the shared all-files preparation and AI generation gate before executing.
- Fixed Git staging-area commit workflow support when IntelliJ changelists are disabled.
- Normalized Git staging-area path de-duplication for nested module and product paths with mixed path separators.

## [v0.1.0-alpha.2] - 2026-05-15

### Added

- Added final IntelliJ-style `AI Commit All` and `& Push` action icons.
- Added Commit tool window actions that are visible only in supported Git commit workflow contexts and disabled when no committable Git content is available.
- Added Git change selection for tracked, unversioned, resolved-conflict, multi-changelist, and multi-root commit content while excluding ignored files.
- Added JetBrains AI Assistant commit-message action discovery and invocation through the IntelliJ action system.
- Added AI generation completion gating, configurable timeout and check interval settings, running activity state, and user-edit stop handling.
- Added commit-only and commit-and-push execution through IntelliJ commit workflow executors.
- Added plugin-owned notifications only for workflow stop paths without a more precise platform-owned message.
- Added pull-request CI, Plugin Verifier CI, signing configuration, Marketplace publishing configuration, and a manually gated release workflow.

### Changed

- Updated README and support documentation to describe the implemented but unreleased workflow, source repository, release process, and current validation limits.

### Fixed

- Fixed AI generation completion handling so user edits and still-running generation state stop the workflow without committing or pushing.

## [v0.1.0-alpha.1] - 2026-05-15

### Added

- Added `CHANGELOG.md` to track notable public plugin-facing unreleased and released changes.
- Added `SUPPORT.md` to define the current support policy and issue-reporting expectations.
- Added the initial Gradle Kotlin DSL and Kotlin/JVM scaffold for the IntelliJ Platform plugin.
- Added plugin descriptor metadata and the required JetBrains AI Assistant dependency.

### Changed

- Set the plugin version to `0.1.0-alpha.1`.
