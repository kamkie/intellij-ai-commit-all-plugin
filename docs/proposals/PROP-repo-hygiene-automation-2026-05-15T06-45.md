---
proposal_id: PROP-repo-hygiene-automation
generated_at: 2026-05-15T06-45
purpose: Propose adding code scanning, formatting enforcement, Dependabot, and other missing repository hygiene automation for maintainer triage.
scope: Repository-wide automation under `.github/`, Gradle build configuration, and contributor-facing policy docs.
---

# Repository Hygiene Automation Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E1. No automated code scanning configured](#e1-no-automated-code-scanning-configured)
    - [E2. No enforced code formatting/linting](#e2-no-enforced-code-formattinglinting)
    - [E3. No Dependabot configuration](#e3-no-dependabot-configuration)
    - [E4. Gradle wrapper integrity is not validated in CI](#e4-gradle-wrapper-integrity-is-not-validated-in-ci)
    - [E5. No secret scanning / push protection guidance](#e5-no-secret-scanning--push-protection-guidance)
    - [E6. No SECURITY.md disclosure policy](#e6-no-securitymd-disclosure-policy)
    - [E7. No CONTRIBUTING.md for external contributors](#e7-no-contributingmd-for-external-contributors)
    - [E8. No CODEOWNERS file](#e8-no-codeowners-file)
    - [E9. No pull request or issue templates](#e9-no-pull-request-or-issue-templates)
    - [E10. No license header enforcement on Kotlin sources](#e10-no-license-header-enforcement-on-kotlin-sources)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The repository ships a working CI/verifier/release pipeline but lacks standard supply-chain and code-quality automation expected for an Apache-2.0 plugin published to the JetBrains Marketplace.
- Triage scope is `.github/` automation (code scanning, Dependabot, wrapper validation, templates), Gradle plugins for formatting and license headers, and contributor-facing policy files (`SECURITY.md`, `CONTRIBUTING.md`, `CODEOWNERS`).
- This proposal performs no implementation; each finding is advisory until accepted via the normal ADR/plan flow defined in `docs/decisions/README.md` and `.agents/references/planning.md`.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id  | Title                                           | Priority | Status | Decision |
|-----|-------------------------------------------------|----------|--------|----------|
| E1 | No automated code scanning configured           | 1        | open   |          |
| E2 | No enforced code formatting/linting             | 1        | open   |          |
| E3 | No Dependabot configuration                     | 1        | open   |          |
| E4 | Gradle wrapper integrity is not validated in CI | 2        | open   |          |
| E5 | No secret scanning / push protection guidance   | 2        | open   |          |
| E6 | No SECURITY.md disclosure policy                | 2        | open   |          |
| E7 | No CONTRIBUTING.md for external contributors    | 3        | open   |          |
| E8 | No CODEOWNERS file                              | 3        | open   |          |
| E9 | No pull request or issue templates              | 3        | open   |          |
| E10 | No license header enforcement on Kotlin sources | 4        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E1. No automated code scanning configured

- Evidence: `.github/workflows/` contains only `ci.yml`, `plugin-verifier.yml`, and `release.yml`. No `codeql.yml` or third-party SAST workflow is present; no `Security` tab integration is configured in repository files.
- Impact: Vulnerabilities and common Kotlin/Java weaknesses (e.g., reflective misuse around the IntelliJ Action System used in `ReflectiveCommitWorkflowSynchronizer`, `AiCommitMessageActionDiscoveryService`) are not surfaced automatically. The plugin is published under Apache-2.0 to the JetBrains Marketplace (ARD-0018, ARD-0019), where supply-chain due diligence is expected.
- Proposal: Add GitHub CodeQL analysis for the `java-kotlin` language with the default query suite, scheduled weekly and on PRs to `main`. Upload SARIF results to GitHub code scanning. Optionally enable `security-extended` queries if signal-to-noise stays acceptable.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E2. No enforced code formatting/linting

- Evidence: `build.gradle.kts` does not apply a formatting plugin (`ktlint`, `spotless`, or `detekt`). No pre-commit hook or CI step rejects style violations. `.agents/references/code-style.md` is referenced but not mechanically enforced.
- Impact: Style drift between human contributors and AI agents is possible; reviewers must enforce style manually, contradicting the "Validation that matches the diff" rule in `AGENTS.md`.
- Proposal: Adopt one of:
    1. `org.jlleitschuh.gradle.ktlint` Gradle plugin with a `ktlintCheck` task wired into the existing `ci.yml` workflow, plus a `ktlintFormat` developer task.
    2. `com.diffplug.spotless` configured for Kotlin (`ktlint` ruleset) and Gradle Kotlin DSL, with `spotlessCheck` in CI and `spotlessApply` for local use.
       Pick exactly one to avoid duplicate enforcement. Document the choice in a new ADR before implementation per `docs/decisions/README.md`.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E3. No Dependabot configuration

- Evidence: No `.github/dependabot.yml` exists. `build.gradle.kts` and `gradle.properties` pin IntelliJ Platform and Kotlin/Gradle dependency versions that need periodic updates aligned with the 2026.1 minimum platform target.
- Impact: Security and compatibility updates for Gradle plugins, GitHub Actions, and the IntelliJ Platform Gradle plugin are not surfaced as PRs. Marketplace publication risk grows as transitive dependencies age.
- Proposal: Add `.github/dependabot.yml` with at least these ecosystems:
    - `github-actions` (weekly, grouped minor+patch).
    - `gradle` (weekly, grouped minor+patch; major updates as separate PRs).
    - Target branch `main`; default reviewer set from `CODEOWNERS` once E8 is accepted.
      Constrain the IntelliJ Platform major version bumps to a separate group so they can be reviewed against ARD-0008 (target platform).

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E4. Gradle wrapper integrity is not validated in CI

- Evidence: `gradle/wrapper/gradle-wrapper.jar` is checked into the repository. No workflow runs `gradle/wrapper-validation-action`.
- Impact: A malicious or accidentally corrupted wrapper jar could ship to the build environment used to produce signed Marketplace artifacts (ARD-0019).
- Proposal: Add a `wrapper-validation` job using `gradle/wrapper-validation-action@v3` to `ci.yml` (or a dedicated workflow) that runs on every PR and push to `main`.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

### E5. No secret scanning / push protection guidance

- Evidence: `release.yml` references signing/publishing secrets but no document instructs maintainers to enable GitHub secret scanning and push protection, nor lists rotation procedure for `PUBLISH_TOKEN`, `SIGNING_PRIVATE_KEY`, `SIGNING_PRIVATE_KEY_PASSWORD`, `CERTIFICATE_CHAIN`.
- Impact: Accidental leakage of Marketplace publishing credentials could enable unauthorized plugin updates.
- Proposal: Document secret inventory and rotation in `SECURITY.md` (see E6) and add a checklist item to `.agents/references/releases.md` to verify GitHub "Secret scanning" and "Push protection" are enabled for the repository. No code change required beyond docs.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

### E6. No SECURITY.md disclosure policy

- Evidence: Repository root lists `LICENSE`, `SUPPORT.md`, `README.md`, `CHANGELOG.md`, but no `SECURITY.md`.
- Impact: External researchers have no documented private disclosure channel. GitHub will not surface a "Report a vulnerability" link without this file (or Private Vulnerability Reporting enabled).
- Proposal: Add `SECURITY.md` covering: supported versions (linked to `SUPPORT.md`), private reporting contact (email or GitHub Private Vulnerability Reporting), response SLA, and out-of-scope items. Enable GitHub Private Vulnerability Reporting.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

### E7. No CONTRIBUTING.md for external contributors

- Evidence: `AGENTS.md` and `docs/WORKING_WITH_AI.md` describe AI-oriented workflows, but there is no human-oriented `CONTRIBUTING.md` covering local build, sandbox validation, commit message rules (`.gitmessage`), and the ADR/plan/proposal flow.
- Impact: External contributors must reverse-engineer the lifecycle from `docs/DEVELOPMENT_LIFECYCLE.md` and AI references.
- Proposal: Add a `CONTRIBUTING.md` that points to `docs/DEVELOPMENT_LIFECYCLE.md`, `docs/validation/manual-sandbox.md`, `.gitmessage`, and `docs/decisions/README.md`. Keep it short; link rather than duplicate.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment:
```

### E8. No CODEOWNERS file

- Evidence: No `.github/CODEOWNERS` or `CODEOWNERS` at repo root.
- Impact: Dependabot PRs (E3) and contributor PRs lack automatic reviewer assignment; review SLAs are informal.
- Proposal: Add `.github/CODEOWNERS` mapping `*` and key paths (`src/`, `.github/workflows/`, `docs/decisions/`, `build.gradle.kts`) to the maintainer's GitHub handle. Coordinate identity with ARD-0040 (git identity for ADR decision makers).

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment:
```

### E9. No pull request or issue templates

- Evidence: `.github/` has only workflows; no `pull_request_template.md` and no `ISSUE_TEMPLATE/` directory.
- Impact: Bug reports lack reproduction steps (IDE build, OS, plugin version, Git root layout), and PRs lack a validation checklist aligned with `.agents/references/reviews.md`.
- Proposal: Add a single PR template referencing the review checklist and the validation matrix in `docs/validation/manual-sandbox.md`. Add two issue templates: `bug_report.yml` and `feature_request.yml`, with `config.yml` linking to `SUPPORT.md`.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment:
```

### E10. No license header enforcement on Kotlin sources

- Evidence: Sources under `src/main/kotlin/pl/devopssolutions/aicommitall/` do not consistently carry an Apache-2.0 header; no Gradle plugin enforces one.
- Impact: ARD-0018 selects Apache-2.0 for the repository and the plugin. Marketplace and downstream redistribution benefit from per-file headers; absence is a minor compliance gap.
- Proposal: If E2 selects Spotless, reuse it with the `licenseHeader` step. Otherwise add `com.github.hierynomus.license` or `org.cadixdev.licenser`. Decide as part of the same ADR that resolves E2 to avoid plugin sprawl.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment:
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- Consider adding `.editorconfig` at repo root if Spotless/ktlint is adopted, so IDE formatting matches CI.
- Consider adding `actions/setup-java` cache hygiene and `gradle/actions/setup-gradle` if not already used in `ci.yml`, to keep Dependabot Gradle bumps fast.
- Consider enabling GitHub Discussions or pointing `SUPPORT.md` at the chosen channel from `CONTRIBUTING.md`.

## Suggested Priority Order

1. `E3` Dependabot - lowest-risk, highest-leverage change; surfaces other updates needed before deeper automation lands.
2. `E1` Code scanning (CodeQL) - independent of build changes; gives baseline security signal immediately.
3. `E2` Formatting/linting enforcement - requires an ADR to choose the toolchain; unblocks `E10`.
4. `E4` Wrapper validation - tiny CI addition that protects the release pipeline.
5. `E6` `SECURITY.md` and `E5` secret scanning guidance - paired docs/process change.
6. `E7`, `E8`, `E9` contributor-facing files - sequenced after maintainer identity (`CODEOWNERS`) is decided.
7. `E10` license headers - executed together with the tooling chosen in `E2`.

## Out Of Scope

- Choice of specific formatter (`ktlint` vs Spotless+ktlint) - deferred to an ADR triggered by `E2`.
- Changes to the existing `plugin-verifier.yml` and `release.yml` workflows beyond adding the wrapper-validation job in `E4`.
- Marketplace listing metadata, signing key rotation procedures beyond documenting them.
- IntelliJ Platform version targeting (governed by ARD-0008) and Git-only behavior (governed by ARD-0009).
- Any implementation work; this proposal stops at maintainer triage per `docs/proposals/README.md`.
