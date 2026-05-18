# Changelog

All notable changes to this project are documented in this file.

The format is based on Keep a Changelog. Release tags should use semantic version tags in the form `vMAJOR.MINOR.PATCH` for stable releases or `vMAJOR.MINOR.PATCH-PRERELEASE` for prereleases.

This repository has no Marketplace-published plugin version yet. `v0.1.0-alpha.3` is the current implementation prerelease candidate prepared from this repository.

## [Unreleased]

## [v0.1.0-alpha.3] - 2026-05-18

### Changed

- Added a lightweight Design task type for early AI-assisted idea shaping before proposals, ADRs, plans, or implementation.
- Added default-on IDE commit shortcut takeover for `AI Commit All` `Commit` and `Push` workflows, with a settings opt-out.
- Clear stale commit-message text before AI generation by default, with a settings toggle to keep existing text.
- Implemented the three-section `<AI icon> AI | Commit | Push` Commit tool window control with an AI-only preparation path, commit and push routing, cumulative hover, and violet AI snake running styling.
- Moved the three-section Commit tool window control to the right of the IDE `Commit and Push...` button.
- Consolidated active proposal findings into four numbered work-stream proposals and archived the superseded source proposals with pointers to the new finding IDs.
- Accepted and implemented artifact governance updates for scoped artifact lookup, proposal finding IDs, ADR acceptance timestamps, plan approval timestamps, and plan status history validation.

### Fixed

- Corrected ADR decision filenames and references from the mistaken `ard-` prefix to the standard `adr-` prefix.
- Confirm Git staging-area inclusion before AI commit-message generation starts, avoiding intermittent first-run generation with incomplete staged input.
- Ensured Commit and Push workflows reuse the shared all-files preparation and AI generation gate before executing.
- Fixed Git staging-area commit workflow support when IntelliJ changelists are disabled.
- Normalized Git staging-area path de-duplication for nested module and product paths with mixed path separators.

## [v0.1.0-alpha.2] - 2026-05-15

### Added

- Added draft active plans for the remaining workflow, validation, documentation, and release automation work.
- Added a draft orchestration plan that makes cross-plan dependencies and safe parallel execution waves explicit.
- Added repository-maintenance proposals for hygiene automation, `TASKS.md` retirement, plugin default settings, split-button look experiments, and proposal formatting conventions.
- Added final IntelliJ-style `AI Commit All` and `& Push` action icons.
- Added Commit tool window actions that are visible only in supported Git commit workflow contexts and disabled when no committable Git content is available.
- Added Git change selection for tracked, unversioned, resolved-conflict, multi-changelist, and multi-root commit content while excluding ignored files.
- Added JetBrains AI Assistant commit-message action discovery and invocation through the IntelliJ action system.
- Added AI generation completion gating, configurable timeout and check interval settings, running activity state, and user-edit stop handling.
- Added commit-only and commit-and-push execution through IntelliJ commit workflow executors.
- Added plugin-owned notifications only for workflow stop paths without a more precise platform-owned message.
- Added local Git repository validation for committable file states, multiple Git roots, and local-only commit-and-push.
- Added a manual sandbox validation record with representative 2026.1 IDE product/build targets.
- Added pull-request CI, Plugin Verifier CI, signing configuration, Marketplace publishing configuration, and a manually gated release workflow.

### Changed

- Changed plan IDs to use the `PLAN-<short-kebab-slug>` format.
- Changed active and archived proposal filename rules so filenames start with their stable `proposal_id`.
- Changed ADR files to use MADR 4.0.0 structure and `adr-0000-<slug>.md` filenames.
- Changed ADR `decision-makers` metadata to use the configured Git username and email.
- Added explicit ADR acceptance and plan approval gates before governed implementation can start.
- Updated README and support documentation to describe the implemented but unreleased workflow, source repository, release process, and current validation limits.

### Fixed

- Fixed AI generation completion handling so user edits and still-running generation state stop the workflow without committing or pushing.

## [v0.1.0-alpha.1] - 2026-05-15

### Added

- Added `CHANGELOG.md` to track notable unreleased and released project changes.
- Added `SUPPORT.md` to define the current support policy and issue-reporting expectations.
- Added AI-facing release guidance for changelog and support-policy updates during release preparation.
- Added `docs/proposals/` with proposal rules, a proposal template, and archive guidance for repository analysis documents.
- Added a clickable ADR index to `docs/decisions/README.md`.
- Added `scripts/validate-docs.ps1` for documentation consistency checks.
- Added accepted plan `PLAN-scaffold-plugin-project` for the first executable plugin scaffold.
- Added ADR 0035 to store open questions with decision records.
- Added ADR 0036 requiring plan filenames to include their stable `Plan-ID`.
- Added ADR 0037 defining the compact plan status lifecycle.
- Added the initial Gradle Kotlin DSL and Kotlin/JVM scaffold for the IntelliJ Platform plugin.
- Added plugin descriptor metadata and the required JetBrains AI Assistant dependency.
- Validated `buildPlugin` and sandbox `runIde` startup for the initial scaffold.

### Changed

- Updated concept graphics documentation to describe and render the split-button placeholder image.
- Assigned changelog maintenance to the orchestrator for orchestrated plan execution and release preparation.
- Added an explicit rule that AI agents should not automatically load every instruction file and should instead load the smallest task-specific guidance set.
- Added stable, human-readable plan IDs that are not strictly number-based.
- Added stable proposal IDs for repository analysis and proposal documents.
- Moved open questions from root `OPEN_QUESTIONS.md` to `docs/decisions/OPEN_QUESTIONS.md`.
- Clarified current split-button styling guidance in older ADR follow-ups.
- Clarified ADR 0006 and ADR 0027 so the current split-button styling owner is unambiguous.
- Allowed the future Gradle Wrapper jar through `.gitignore` and named wrapper files in the scaffold plan.
- Clarified Marketplace documentation versus release metadata task ownership.
- Marked `PROP-repository-analysis` completed after implementing accepted findings.
- Renamed the active scaffold plan file to include `PLAN-scaffold-plugin-project`.
- Updated plan guidance and the plan template to surface readiness, open questions, and implementation progress.
- Marked `PROP-plan-status-vocabulary` completed after implementing accepted findings.
- Archived completed plans, completed proposals, and completed task entries for the first scaffold prerelease.
- Set the plugin version to `0.1.0-alpha.1`.
