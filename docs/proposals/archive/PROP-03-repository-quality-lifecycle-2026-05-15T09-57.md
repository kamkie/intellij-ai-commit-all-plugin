---
proposal_id: PROP-03-repository-quality-lifecycle
generated_at: 2026-05-15T09-57
purpose: Consolidate repository hygiene automation, markdown tooling, contributor intake, and backlog lifecycle findings into a self-contained active work stream.
scope: GitHub automation, Gradle and Markdown formatting enforcement, security policy docs, contributor files, license headers, `TASKS.md`, and backlog lifecycle references.
supersedes:
    - PROP-repo-hygiene-automation
    - PROP-remove-tasks-md-when-empty
    - PROP-proposal-id-and-markdown-formatting E002-E004 and S001
---

# Repository Quality And Lifecycle Work Stream

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It is the active authoritative source for the changes proposed by `E001` through `E012`; superseded proposals are provenance only and are not needed to decide or implement these findings. This proposal is advisory and does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E001. Add Dependabot configuration](#e001-add-dependabot-configuration)
    - [E002. Add CodeQL analysis](#e002-add-codeql-analysis)
    - [E003. Add unified formatting and linting enforcement](#e003-add-unified-formatting-and-linting-enforcement)
    - [E004. Validate Gradle wrapper integrity in CI](#e004-validate-gradle-wrapper-integrity-in-ci)
    - [E005. Add security policy and secret-scanning guidance](#e005-add-security-policy-and-secret-scanning-guidance)
    - [E006. Add contributor intake files](#e006-add-contributor-intake-files)
    - [E007. Add CODEOWNERS after reviewer identity is known](#e007-add-codeowners-after-reviewer-identity-is-known)
    - [E008. Enforce Apache-2.0 source headers](#e008-enforce-apache-20-source-headers)
    - [E009. Define the `TASKS.md` retirement trigger](#e009-define-the-tasksmd-retirement-trigger)
    - [E010. Preserve completed task history outside the active backlog](#e010-preserve-completed-task-history-outside-the-active-backlog)
    - [E011. Decide the future backlog home](#e011-decide-the-future-backlog-home)
    - [E012. Update references and retire `TASKS.md` only after release](#e012-update-references-and-retire-tasksmd-only-after-release)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- This work stream owns repository automation and lifecycle cleanup that should support release readiness without distracting from product UX.
- The accepted automation findings add dependency update PRs, code scanning, security disclosure guidance, contributor intake, CODEOWNERS, and license-header enforcement.
- The accepted formatting finding deliberately combines Kotlin, Gradle Kotlin DSL, Markdown, `.editorconfig`, IntelliJ code style, markdownlint, and docs validation into one toolchain decision so the repository does not accumulate competing formatters.
- `TASKS.md` remains the backlog owner. The rejected retirement findings are kept for history, and the only accepted backlog-lifecycle work is choosing an archive path for completed task history if it moves out of the active backlog file.
- Each accepted implementation finding remains subject to the normal repository flow: create an ADR or approved plan when required, update docs alongside behavior or workflow changes, and run validation that matches the diff.

## Progress Tracker

| Id   | Title                                                      | Priority | Status       | Decision |
|------|------------------------------------------------------------|----------|--------------|----------|
| E001 | Add Dependabot configuration                               | 1        | done         | accepted |
| E002 | Add CodeQL analysis                                        | 1        | done         | accepted |
| E003 | Add unified formatting and linting enforcement             | 2        | done         | accepted |
| E004 | Validate Gradle wrapper integrity in CI                    | 2        | done         | accepted |
| E005 | Add security policy and secret-scanning guidance           | 2        | done         | accepted |
| E006 | Add contributor intake files                               | 3        | done         | accepted |
| E007 | Add CODEOWNERS after reviewer identity is known            | 3        | done         | accepted |
| E008 | Enforce Apache-2.0 source headers                          | 4        | done         | accepted |
| E009 | Define the `TASKS.md` retirement trigger                   | 4        | not-required | rejected |
| E010 | Preserve completed task history outside the active backlog | 4        | done         | accepted |
| E011 | Decide the future backlog home                             | 4        | not-required | accepted |
| E012 | Update references and retire `TASKS.md` only after release | 6        | not-required | rejected |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current timestamp.
- Use `status` for implementation progress and `decision` for maintainer triage.
- Update the Proposal Implementation Summary in `docs/proposals/README.md` for accepted findings with non-terminal implementation status and an evidence path. A `TASKS.md` entry is optional when another evidence path is clearer.
- Leave completed, rejected, or superseded findings in place as history.

## Errors And Mistakes

### E001. Add Dependabot configuration

- Evidence: `.github/dependabot.yml` is absent. `build.gradle.kts`, `gradle.properties`, Gradle wrapper metadata, and `.github/workflows/` pin build plugins, Kotlin, IntelliJ Platform Gradle Plugin configuration, Gradle, and GitHub Actions versions that need periodic review.
- Impact: Security, compatibility, and maintenance updates for Gradle plugins, GitHub Actions, and IntelliJ Platform tooling are not surfaced automatically. Release readiness depends on manual discovery.
- Proposal: Add `.github/dependabot.yml`.
    - Include `github-actions` on a weekly cadence.
    - Include `gradle` on a weekly cadence.
    - Use `main` as the target branch unless repository branching policy changes.
    - Group minor and patch updates where that keeps PR noise low.
    - Keep major updates separate from minor and patch groups.
    - Put IntelliJ Platform major-line updates in a separate group so they can be reviewed against the supported IDE/platform ADRs.
    - Add default reviewers only after `E007` supplies a reviewer identity or CODEOWNERS mapping.
    - Document any non-obvious grouping policy in the same PR or plan that adds the file.

```yaml
status: done
decision: accepted
priority: 1
owner:
updated: 2026-05-18T21:00:34+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Implemented by .github/dependabot.yml with weekly github-actions and gradle update checks, grouped minor/patch updates, a separate IntelliJ Platform major group, and reviewer routing to kamkie."
```

### E002. Add CodeQL analysis

- Evidence: `.github/workflows/` has build, verifier, and release workflows, but no CodeQL or equivalent code-scanning workflow. The repository contains Kotlin/Java plugin code, reflection-heavy IntelliJ Platform integration, Gradle build logic, and release automation.
- Impact: Kotlin/Java weaknesses, unsafe API usage patterns, and supply-chain risks are not surfaced automatically before pull requests merge or Marketplace release artifacts are produced.
- Proposal: Add a GitHub CodeQL workflow.
    - Use CodeQL's Java/Kotlin language support.
    - Run on pull requests that target `main`.
    - Run on pushes to `main`.
    - Run on a scheduled weekly cadence.
    - Upload results to GitHub code scanning.
    - Start with default queries; consider `security-extended` only after signal-to-noise is reviewed.
    - Keep release secrets unavailable to the CodeQL workflow.
    - Document any known CodeQL limitations for IntelliJ Platform plugin code if the first run produces false positives or unsupported build issues.

```yaml
status: done
decision: accepted
priority: 1
owner:
updated: 2026-05-18T21:00:34+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Implemented by .github/workflows/codeql.yml with Java/Kotlin CodeQL analysis on pull requests, pushes to main, weekly schedule, and manual Gradle build mode."
```

### E003. Add unified formatting and linting enforcement

- Evidence: The repository has no `.editorconfig`, no `.markdownlint.json` or `markdownlint-cli2` configuration, no exported IntelliJ project code style, and no Gradle-integrated Kotlin/Gradle formatting plugin such as Spotless or ktlint. `scripts/validate-docs.ps1` validates documentation structure but not Markdown formatting. `.agents/references/code-style.md` gives prose guidance but is not mechanically enforced.
- Impact: Source and documentation style can drift between IntelliJ, AI agents, and CI. Markdown tables, nested lists, and proposal trackers are especially prone to noisy diffs. Kotlin and Gradle Kotlin DSL style remains a manual review concern.
- Proposal: Author one ADR that chooses a single formatting and linting toolchain for both source and documentation.
    - For Kotlin and Gradle Kotlin DSL, choose exactly one Gradle-integrated formatter path, such as Spotless with ktlint rules or the Gradle ktlint plugin. Avoid applying multiple competing Kotlin formatters.
    - For Markdown, choose markdownlint as the mechanical rule checker and wire it into repository validation.
    - Add `.editorconfig` at the repository root with shared line-ending, indentation, trailing-whitespace, and final-newline rules. Use 4-space Markdown nested-list indentation unless the ADR explicitly chooses otherwise.
    - Add an IntelliJ project code style export under `.idea/codeStyles/` only if the ADR accepts committing IDE code-style files.
    - Add `.markdownlint.json` or `markdownlint-cli2` configuration that matches `.editorconfig` and the proposal formatting rules.
    - Enforce leading and trailing pipes, consistent column counts, blank lines around tables, and stable table padding for Markdown tables if the chosen markdownlint rules support it.
    - Update `scripts/validate-docs.ps1` or the CI workflow so formatting checks run with existing documentation validation.
    - Update `.agents/references/code-style.md` with the chosen rules so AI agents follow the same formatting contract as IntelliJ and CI.
    - Wire the source formatter check into CI and provide a local apply/fix command for contributors.
    - Defer broad reformatting until the tooling decision is accepted, then keep reformatting commits isolated from behavior changes where practical.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T20:43:07+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Implemented by ADR 0064 and PLAN-unified-formatting-linting-toolchain with Spotless, ktlint, markdownlint-cli2, .editorconfig, docs validation, and CI wiring."
```

### E004. Validate Gradle wrapper integrity in CI

- Evidence: `gradle/wrapper/gradle-wrapper.jar` is checked into the repository, but no workflow runs a Gradle wrapper validation action.
- Impact: CI, plugin packaging, signing, and publication can run with a tampered or unexpectedly changed wrapper jar before dependency resolution, tests, verifier work, signing, or publishing begins.
- Purpose: Wrapper validation verifies the checked-in Gradle wrapper jar against known Gradle distributions and protects the release pipeline from a compromised build bootstrap file.
- Proposal: Add Gradle wrapper validation.
    - Add `gradle/wrapper-validation-action@v3` or the current accepted equivalent.
    - Run it on pull requests and pushes to `main`.
    - Place it in `ci.yml` or a dedicated workflow; choose the lower-noise option during implementation.
    - Make the validation fail the build before packaging, verifier, signing, or publication jobs can rely on the wrapper.
    - Keep the change separate from broader Gradle tooling unless an approved plan combines them.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T20:43:07+02:00
accepted_at: 2026-05-18T01:52:15+02:00
comment: "Implemented in ci.yml with gradle/actions/wrapper-validation@v3 before Gradle-dependent jobs."
```

### E005. Add security policy and secret-scanning guidance

- Evidence: `SECURITY.md` is absent. Release automation uses Marketplace publishing and signing secrets, but repository docs do not inventory those secrets, instruct maintainers to enable GitHub secret scanning and push protection, or define a private vulnerability disclosure path.
- Impact: External researchers lack a documented private reporting channel. Maintainers lack a release-safety checklist for protecting and rotating Marketplace publishing credentials.
- Proposal: Add security policy and secret-protection guidance.
    - Add root `SECURITY.md`.
    - State supported versions or link to `SUPPORT.md` for support scope.
    - Provide a private reporting channel, such as a security contact email or GitHub Private Vulnerability Reporting once enabled.
    - Set response expectations at a practical level for a small open-source project.
    - List the release secret categories that require protection, including Marketplace publish token, signing private key, private-key password, and certificate chain.
    - Document rotation expectations for suspected exposure.
    - Add release guidance to verify GitHub secret scanning and push protection before Marketplace release work.
    - Keep actual secret values and sensitive operational details out of the repository.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T21:00:34+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Implemented by SECURITY.md, SUPPORT.md, README.md, and release guidance updates covering vulnerability reporting, protected release secrets, rotation expectations, and secret scanning or push protection checks."
```

### E006. Add contributor intake files

- Evidence: `CONTRIBUTING.md`, `.github/pull_request_template.md`, and `.github/ISSUE_TEMPLATE/` are absent. Contributor-facing build, validation, lifecycle, support, and review expectations are spread across `README.md`, `SUPPORT.md`, `docs/DEVELOPMENT_LIFECYCLE.md`, `docs/WORKING_WITH_AI.md`, `.agents/references/`, and `.gitmessage`.
- Impact: External contributors and bug reporters must infer expectations from AI-facing docs and lifecycle references. Bug reports can omit IDE build, OS, plugin version, Git layout, staging/changelist mode, and reproduction information.
- Proposal: Add concise contributor intake files.
    - Add root `CONTRIBUTING.md` for human contributors.
    - Link to local build and sandbox instructions instead of duplicating them.
    - Link to `docs/DEVELOPMENT_LIFECYCLE.md`, validation guidance, `SUPPORT.md`, `.gitmessage`, and decision/proposal rules where relevant.
    - Add one pull request template with a validation checklist, docs/update reminder, and review-risk prompt.
    - Add issue templates for bug reports and feature requests.
    - Bug reports should ask for IDE product/build, OS, plugin version, JetBrains AI Assistant state, Git root layout, changelist/staging mode, reproduction steps, expected behavior, and actual behavior.
    - Add issue-template configuration that points support and security-sensitive reports to the right docs.
    - Keep templates short enough that users can complete them without reading AI workflow internals.

```yaml
status: done
decision: accepted
priority: 3
owner:
updated: 2026-05-18T21:00:34+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Implemented by CONTRIBUTING.md, .github/pull_request_template.md, and bug, feature, and issue-template config files under .github/ISSUE_TEMPLATE/."
```

### E007. Add CODEOWNERS after reviewer identity is known

- Evidence: No `.github/CODEOWNERS` or root `CODEOWNERS` file exists. The repository also lacks a maintainer GitHub handle in this proposal, so a valid CODEOWNERS mapping cannot be completed from repository context alone.
- Impact: Dependabot and contributor pull requests cannot request review automatically. Ownership for source, workflow, build, and decision files remains implicit.
- Proposal: Add CODEOWNERS after the maintainer GitHub handle is known.
    - Prefer `.github/CODEOWNERS`.
    - Map `*` to the primary maintainer or maintainer team.
    - Add explicit entries for `src/`, `.github/workflows/`, `docs/decisions/`, `docs/proposals/`, `.agents/`, `build.gradle.kts`, `settings.gradle.kts`, and Gradle wrapper files.
    - Use GitHub handles or teams that are valid for the repository owner.
    - Coordinate with `E001` so Dependabot PR reviewer routing works after CODEOWNERS lands.
    - Do not invent reviewer identity; treat missing handle/team as a blocker for implementation.

```yaml
status: done
decision: accepted
priority: 3
owner:
updated: 2026-05-18T21:00:34+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Implemented by .github/CODEOWNERS using the repository owner handle @kamkie from the Git remote and matching Dependabot reviewer routing."
```

### E008. Enforce Apache-2.0 source headers

- Evidence: `LICENSE` declares Apache-2.0 for the repository, but source files under `src/` do not have mechanically enforced Apache-2.0 headers. No Gradle license-header or formatter configuration enforces per-file headers.
- Impact: Per-file license clarity is weaker than expected for an Apache-2.0 IntelliJ Platform plugin intended for open-source Marketplace distribution.
- Proposal: Enforce source headers through the formatter/tooling chosen in `E003` where possible.
    - If `E003` chooses Spotless, prefer Spotless `licenseHeader` or the closest supported mechanism.
    - If `E003` does not choose a formatter with license-header support, select one dedicated license-header plugin in the same ADR or follow-up plan.
    - Apply headers to Kotlin source files at minimum.
    - Decide separately whether Gradle Kotlin DSL, XML, and test fixtures need headers.
    - Avoid adding multiple overlapping license-header tools.
    - Keep `NOTICE` decisions separate; add `NOTICE` only if bundled dependencies or attribution requirements make it necessary.

```yaml
status: done
decision: accepted
priority: 4
owner:
updated: 2026-05-18T20:43:07+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Implemented through Spotless licenseHeaderFile enforcement for Kotlin source files."
```

### E009. Define the `TASKS.md` retirement trigger

- Evidence: Earlier backlog-retirement analysis proposed deleting `TASKS.md` when the open backlog became empty. Current governing guidance still names `TASKS.md` as the implementation backlog, and current maintainer triage rejected retirement. `TASKS.md` may contain both open backlog items and completed task history.
- Impact: A retirement trigger would conflict with the accepted direction that `TASKS.md` stays as the backlog owner.
- Proposal: No implementation. Keep this finding as rejected history.
    - Do not create an ADR to retire `TASKS.md`.
    - Do not remove `TASKS.md` when the open backlog is empty.
    - Preserve the repository rule that `TASKS.md` is the first lookup location for `T-<AREA>-NNN` task references.
    - Route any completed-history cleanup through `E010`, not through deletion of the backlog file.

```yaml
status: not-required
decision: rejected
priority: 4
owner:
updated: 2026-05-18T01:47:37+02:00
decided_at: 2026-05-18T01:38:44+02:00
comment: "Rejected because TASKS.md stays as the backlog. Supersedes PROP-remove-tasks-md-when-empty E1."
```

### E010. Preserve completed task history outside the active backlog

- Evidence: `TASKS.md` contains stable `T-<AREA>-NNN` task IDs and a completed task archive that links historical task IDs to plans, ADRs, and implementation work. ADRs, plans, reviews, and commit metadata may reference those task IDs.
- Impact: Keeping all completed history in the active backlog can make current backlog scanning harder, but deleting or rewriting completed entries without an archive would weaken traceability for historical task IDs.
- Proposal: Choose an accepted archive path if completed task history moves out of `TASKS.md`.
    - Keep `TASKS.md` as the active backlog home.
    - Preserve every historical `T-<AREA>-NNN` ID verbatim.
    - Prefer a dedicated archive such as `docs/history/completed-tasks.md` unless an ADR chooses a different home.
    - Move only completed-history material; do not move open backlog items away from `TASKS.md`.
    - Update references that explain where completed task history lives.
    - Keep the archive easy to search from task IDs, plans, ADRs, and commit metadata.
    - Do not mix this history archive with user-facing release notes in `CHANGELOG.md` unless an ADR explicitly chooses that option.

```yaml
status: done
decision: accepted
priority: 4
owner:
updated: 2026-05-18T20:52:35+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Completed task history remains in TASKS.md under Completed Task Archive, with completed active-backlog rows moved out of Open Backlog. Supersedes PROP-remove-tasks-md-when-empty E4."
```

### E011. Decide the future backlog home

- Evidence: `AGENTS.md`, `docs/proposals/README.md`, ADRs, and AI workflow guidance route backlog items and `T-<AREA>-NNN` lookups to `TASKS.md`. Maintainer triage recorded the decision that `TASKS.md` stays.
- Impact: No replacement backlog-home decision is needed. Replacing `TASKS.md` with plans-only intake or lazy recreation would contradict the accepted direction.
- Proposal: Treat the decision as complete and no separate implementation required.
    - `TASKS.md` remains the implementation backlog home.
    - `T-<AREA>-NNN` references continue to resolve through `TASKS.md` first.
    - Proposals remain advisory and must not become the backlog.
    - Plans remain implementation contracts for accepted work, not the sole backlog owner.
    - If completed history is moved later, keep current open backlog intake in `TASKS.md`.

```yaml
status: not-required
decision: accepted
priority: 4
owner:
updated: 2026-05-18T01:47:37+02:00
accepted_at: 2026-05-18T01:38:44+02:00
comment: "Decision recorded: TASKS.md stays as the backlog home. Supersedes PROP-remove-tasks-md-when-empty E5."
```

### E012. Update references and retire `TASKS.md` only after release

- Evidence: Backlog-retirement analysis identified many references to `TASKS.md` across governing docs, workflow docs, plans, ADRs, and proposal rules. Current maintainer triage rejects retiring `TASKS.md`, so those references are not dead links; they describe the accepted backlog owner.
- Impact: Removing or repointing those references would now create contradictory guidance. The root backlog file remains part of the repository workflow.
- Proposal: No implementation for file retirement.
    - Do not remove `TASKS.md`.
    - Do not replace `TASKS.md` references with plans-only or proposal-only routing.
    - Update references only if `E010` moves completed task history to a separate archive; in that case, references should distinguish active backlog ownership from completed-history location.
    - Keep any future retirement proposal separate and ADR-gated if repository direction changes.

```yaml
status: not-required
decision: rejected
priority: 6
owner:
updated: 2026-05-18T01:47:37+02:00
decided_at: 2026-05-18T01:38:44+02:00
comment: "Rejected because TASKS.md is not being retired. Supersedes PROP-remove-tasks-md-when-empty E2 and E3."
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- Prefer one formatting decision over separate source and documentation formatter debates.
- Keep completed-task archival work separate from release-critical automation unless the maintainer explicitly pulls it forward.
- Keep superseded proposals as historical context only; active ADRs and implementation plans should cite this proposal for `E001` through `E012` details.

## Suggested Priority Order

1. `E001` - add Dependabot.
2. `E002` - add CodeQL.
3. `E004` - validate the Gradle wrapper.
4. `E005` - add `SECURITY.md` and secret guidance.
5. `E003` - choose and wire formatting/linting after release-safety automation is in place.
6. `E006` and `E007` - add contributor intake once reviewer identity is known.
7. `E008` - enforce license headers through the chosen formatter.
8. `E010` - preserve completed task history in an accepted archive path if it moves out of `TASKS.md`.
9. No action for `E009`, `E011`, or `E012`; `TASKS.md` stays as the backlog home.

## Out Of Scope

- Plugin runtime UX changes.
- Multi-agent execution topology or worker attribution.
- Marketplace publication itself.
- Legal advice beyond repository policy documentation.
- Retiring `TASKS.md` under the current maintainer decision.
