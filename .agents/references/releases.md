# Release Guide

Use this guide only for intentional release preparation after implementation work is complete and integrated on `main`, or when the user explicitly requests release work.

Stay in `.agents/references/planning.md` and `.agents/references/execution.md` while work is still in an implementation plan or local task.

## Versioning And Changelog Rules

- Use semantic version tags in the form `vMAJOR.MINOR.PATCH` for stable releases or `vMAJOR.MINOR.PATCH-PRERELEASE` for prereleases.
- Build artifact versions are derived from Git metadata with Palantir `gradle-git-version`; package releases only after the intended annotated tag exists on the release commit.
- Keep release numbers increasing in `git log --first-parent` order.
- Create releases only from `main` after intended implementation work is integrated.
- Use annotated tags for intentional releases.
- Keep `CHANGELOG.md` aligned with released versions.
- Treat `CHANGELOG.md` as public release notes for plugin-facing changes, not as an internal repository activity log.
- Keep `SUPPORT.md` aligned when supported IDE versions, plugin dependency requirements, Marketplace availability, or support channels change.

## Changelog Ownership

The orchestrator owns `CHANGELOG.md` maintenance for orchestrated plan execution and release preparation.

Task workers may propose changelog entries in their handoff evidence, but they should not decide final wording, category, placement, or whether an entry is notable. The orchestrator decides that while reviewing each task and before starting the next task.

After every worker handoff for a task that produces a public plugin-facing change, the orchestrator updates the next unreleased `CHANGELOG.md` section before dispatching the next task. Public plugin-facing changes include plugin source or runtime behavior, public plugin documentation such as `README.md`, `SUPPORT.md`, Marketplace text, compatibility or support scope, security or privacy behavior, and CI, signing, publishing, or release workflow changes that affect the plugin artifact or publication.

Omit internal repository activity from `CHANGELOG.md`, including AI-agent documentation, skills, plans, proposals, ADR maintenance, internal workflow rules, scenario-coverage registers, test-case inventories, manual validation logs, and test-only changes unless they also change public plugin behavior, public docs, support promises, or release artifacts.

The changelog edit should ride along in the same task commit when feasible and when it does not break the one-commit-per-task boundary. If a separate orchestrator commit is needed, it must be created before the next task starts and must use the multi-agent attribution trailers required by `.gitmessage` when those trailers apply.

For release preparation, the release orchestrator owns moving accepted `Unreleased` entries into the versioned release section.

## Changelog Entries

Use `CHANGELOG.md` for notable public plugin-facing changes only.

Before the first release, keep entries under `## [Unreleased]`.

During release preparation:

- Move released entries from `## [Unreleased]` into a version section named `## [vMAJOR.MINOR.PATCH] - YYYY-MM-DD`.
- Leave a fresh empty `## [Unreleased]` section above the released section.
- Use Keep a Changelog categories when useful: `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, and `Security`.
- Do not record unreleased work as released.
- Do not copy routine task execution notes into the changelog unless they matter to plugin users, public documentation readers, release operators, compatibility, support, security, privacy, or the published plugin artifact.
- Do not add entries for AI-agent documentation, repository workflow governance, ADR/proposal/plan maintenance, scenario-coverage tracking, validation inventories, or test-case documentation by themselves.

## Release Preconditions

Do not start release work until all applicable items are true:

- The target implementation plan is fully executed.
- Each plan task was implemented, validated, reviewed, and committed under ADR 0023.
- Required questions and decisions were resolved under ADR 0024.
- The release candidate is on `main`.
- Required docs are aligned through `.agents/references/documentation.md`.
- Required validation from `.agents/references/testing.md` passed for the exact release candidate.
- Marketplace signing, publishing, CI, and secret-handling requirements from ADR 0019 are satisfied for publishable releases.
- GitHub secret scanning and push protection are enabled for the repository where available, or the release handoff records why the repository cannot enable them.
- The `jetbrains-marketplace` environment still protects `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, and `PUBLISH_TOKEN`.

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
