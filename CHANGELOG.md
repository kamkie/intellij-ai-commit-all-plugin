# Changelog

All notable changes to this project are documented in this file.

The format is based on Keep a Changelog. Release tags should use semantic version tags in the form `vMAJOR.MINOR.PATCH` for stable releases or `vMAJOR.MINOR.PATCH-PRERELEASE` for prereleases.

This repository has no released plugin version yet.

## [Unreleased]

### Added

- Added `CHANGELOG.md` to track notable unreleased and released project changes.
- Added `SUPPORT.md` to define the current support policy and issue-reporting expectations.
- Added AI-facing release guidance for changelog and support-policy updates during release preparation.
- Added `docs/proposals/` with proposal rules, a proposal template, and archive guidance for repository analysis documents.
- Added a clickable ADR index to `docs/decisions/README.md`.
- Added `scripts/validate-docs.ps1` for documentation consistency checks.
- Added accepted plan `P-scaffold-plugin-project` for the first executable plugin scaffold.
- Added ADR 0035 to store open questions with decision records.
- Added ADR 0036 requiring plan filenames to include their stable `Plan-ID`.
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
- Renamed the active scaffold plan file to include `P-scaffold-plugin-project`.
