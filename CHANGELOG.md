# Changelog

All notable public plugin-facing changes are documented in this file.

The format is based on Keep a Changelog. Release tags should use semantic version tags in the form `vMAJOR.MINOR.PATCH` for stable releases or `vMAJOR.MINOR.PATCH-PRERELEASE` for prereleases.

This repository has no Marketplace-published plugin version yet. `v0.1.0-alpha.6` is the current implementation prerelease candidate prepared from this repository.

This changelog records plugin source and runtime behavior changes, public plugin documentation changes, compatibility and support changes, and CI or release pipeline changes that affect the plugin artifact or publication. Internal AI-agent documentation, repository workflow notes, plans, proposals, ADR maintenance, scenario-coverage tracking, and test-case inventory changes are intentionally omitted unless they also change public plugin behavior or release artifacts.

## [Unreleased]

### Fixed

- Matched the three-section control corner radius to the IDE button arc so it aligns with neighboring Commit toolbar buttons.
- Progress animation now starts on `AI`, advances to `Commit` after AI generation, and switches to `Push` only after commit checks finish and the immediate push begins.

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
