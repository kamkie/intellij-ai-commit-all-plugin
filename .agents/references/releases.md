# Release Guide

Use this guide only for intentional release preparation after implementation work is complete and integrated on `main`, or when the user explicitly requests release work.

Stay in `.agents/references/planning.md` and `.agents/references/execution.md` while work is still in an implementation plan or local task.

This guide is adapted for this IntelliJ Platform plugin repository from the release and changelog conventions in `https://github.com/kamkie/technical-interview-demo`.

## Versioning And Changelog Rules

- Use semantic version tags in the form `vMAJOR.MINOR.PATCH` for stable releases or `vMAJOR.MINOR.PATCH-PRERELEASE` for prereleases.
- Keep release numbers increasing in `git log --first-parent` order.
- Create releases only from `main` after intended implementation work is integrated.
- Use annotated tags for intentional releases.
- Keep `CHANGELOG.md` aligned with released versions.
- Keep `SUPPORT.md` aligned when supported IDE versions, plugin dependency requirements, Marketplace availability, or support channels change.

## Changelog Ownership

The orchestrator owns `CHANGELOG.md` maintenance for orchestrated plan execution and release preparation.

Task workers may propose changelog entries in their handoff evidence, but they should not decide final wording, category, placement, or whether an entry is notable. The orchestrator decides that while reviewing each task and before starting the next task.

For release preparation, the release orchestrator owns moving accepted `Unreleased` entries into the versioned release section.

## Changelog Entries

Use `CHANGELOG.md` for notable project changes.

Before the first release, keep entries under `## [Unreleased]`.

During release preparation:

- Move released entries from `## [Unreleased]` into a version section named `## [vMAJOR.MINOR.PATCH] - YYYY-MM-DD`.
- Leave a fresh empty `## [Unreleased]` section above the released section.
- Use Keep a Changelog categories when useful: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, and `Security`.
- Do not record unreleased work as released.
- Do not copy routine task execution notes into the changelog unless they matter to users, contributors, release operators, or compatibility.

## Release Preconditions

Do not start release work until all applicable items are true:

- The target implementation plan is fully executed.
- Each plan task was implemented, validated, reviewed, and committed under ADR 0023.
- Required questions and decisions were resolved under ADR 0024.
- The release candidate is on `main`.
- Required docs are aligned through `.agents/references/documentation.md`.
- Required validation from `.agents/references/testing.md` passed for the exact release candidate.
- Marketplace signing, publishing, CI, and secret-handling requirements from ADR 0019 are satisfied for publishable releases.

## Release Handoff

Release preparation should cover:

- Full cross-task review.
- Broader manual checks and tests.
- Documentation update pass.
- Changelog update.
- Support-policy update if supported versions or channels changed.
- Release artifact preparation.
- Tagging and publication only when requested.

## What Not To Do

- Do not tag a commit that has not passed required validation.
- Do not cut a release from unmerged branch or worktree state.
- Do not commit Marketplace tokens, signing keys, certificates, passwords, or private repository data.
- Do not treat generated concept graphics as final plugin UI assets.
- Do not update support promises beyond the actual implemented and validated support scope.
