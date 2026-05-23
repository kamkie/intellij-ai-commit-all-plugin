# Toolchain Upgrade

Create or update a narrow `.agents/plans/PLAN-<short-kebab-slug>.md` for a toolchain upgrade batch.

## Scope

Use this prompt for these owned upgrade surfaces:

- GitHub Actions and CI workflow actions
- Java dependencies
- Gradle plugins and tools
- Gradle wrapper versions
- IntelliJ Platform plugin verifier targets or related release tooling
- Docker images, if Docker files exist in the repository

## Read First

- `AGENTS.md`
- `.agents/references/planning.md`
- `.agents/references/documentation.md`
- `.agents/references/testing.md`
- `.agents/prompts/README.md`
- this prompt
- relevant build, workflow, wrapper, verifier, release, or Docker files
- the alert, version target, dependency report, scan, issue, or tool output that motivates the upgrade

Load `.agents/references/releases.md` when release, signing, publishing, changelog, Marketplace, or plugin artifact behavior is in scope.

## Output

Create or update one plan under `.agents/plans/` using the existing plan filename and `Plan-ID` rules.
Keep the plan narrow enough to review.

Before writing the plan:

- identify where each requested version is owned
- inventory workflow action pins, direct dependencies, transitive constraints or overrides, Gradle plugin versions, Gradle wrapper versions, `buildSrc`, checked-in Gradle or packaging tools, Docker base images, and plugin verifier targets in scope
- call out compatibility risk, rollback or migration concerns, resolved-version proof, and validation needed to keep the repository release-ready
- say explicitly whether the requested upgrades should stay one batch or split into smaller plans

Follow repository plan requirements from `.agents/references/planning.md`; keep this prompt focused on toolchain inventory, risk, batch shape, and validation.

## Non-Goals

- Do not implement upgrades from this prompt unless the user separately approves the resulting plan and asks for execution.
- Do not update `CHANGELOG.md` while drafting the plan; changelog ownership follows release and execution guidance during implementation.
- Do not include unrelated cleanup, formatting, dependency modernization, or CI redesign outside the requested upgrade batch.
