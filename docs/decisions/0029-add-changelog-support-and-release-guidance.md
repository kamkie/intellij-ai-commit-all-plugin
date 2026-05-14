# Add Changelog, Support, And Release Guidance

Status: Accepted

Date: 2026-05-15

## Context

The repository plans official JetBrains Marketplace publication per ADR 0019, but it did not yet have a changelog, support policy, or release-specific AI guidance.

The user requested adding a changelog and support, and said rules can be imported from `https://github.com/kamkie/technical-interview-demo`.

The source repository has a root `CHANGELOG.md`, contributor/release expectations, and AI-facing release guidance. Those rules need adaptation because this repository is an IntelliJ Platform plugin project, not a Spring Boot application with deployment and runtime operations.

## Decision

Add root `CHANGELOG.md` using Keep a Changelog style and semantic version tag conventions.

Add root `SUPPORT.md` documenting the current pre-release support status, planned supported IDE and VCS scope, issue-reporting context, out-of-scope support, and privacy expectations.

Add `.agents/references/releases.md` as the AI-facing owner for intentional release preparation, changelog updates, support-policy checks, semantic tag rules, and release preconditions.

Keep the imported release rules adapted to this plugin repository:

- Keep Marketplace signing, publishing, CI, and secret handling aligned with ADR 0019.
- Keep supported IDE, Git, and AI Assistant dependency statements aligned with `README.md` and ADRs.
- Do not import source-repository Spring, REST API, database migration, container deployment, operations, or OpenAPI release rules.

## Consequences

- Future release work has a changelog and release guide to update.
- Support expectations are visible before the first Marketplace release.
- AI agents have an owner guide for changelog and support-policy updates during release preparation.
- Documentation ownership guidance needs to include `CHANGELOG.md`, `SUPPORT.md`, and `.agents/references/releases.md`.

## Alternatives Considered

- Add only `CHANGELOG.md`.
  - Why it was not chosen: support expectations are also useful before the first public plugin release.
- Import the source repository release guide verbatim.
  - Why it was not chosen: it contains application deployment, container, database, and API-contract rules that do not fit this plugin repository.
- Defer release guidance until the plugin scaffold exists.
  - Why it was not chosen: the user asked to add changelog support now, and ADR 0019 already makes Marketplace publication in scope.

## Follow-Up

- Add `CHANGELOG.md`.
- Add `SUPPORT.md`.
- Add `.agents/references/releases.md`.
- Update documentation ownership and human-facing guidance.
- Update `TASKS.md` with completed changelog, support, and release-guidance tasks.
