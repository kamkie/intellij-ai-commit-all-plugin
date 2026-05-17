---
proposal_id: PROP-03-repository-quality-lifecycle
generated_at: 2026-05-15T09-57
purpose: Consolidate repository hygiene automation, markdown tooling, contributor intake, and backlog-retirement findings into one active work stream.
scope: GitHub automation, Gradle and Markdown formatting enforcement, security policy docs, contributor files, license headers, `TASKS.md`, and backlog lifecycle references.
supersedes:
    - PROP-repo-hygiene-automation
    - PROP-remove-tasks-md-when-empty
    - PROP-proposal-id-and-markdown-formatting E002-E004 and S001
---

# Repository Quality And Lifecycle Work Stream

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It consolidates repository quality and backlog-lifecycle findings for maintainer triage only; it does not implement changes by itself.

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
    - [E010. Preserve completed task history before backlog retirement](#e010-preserve-completed-task-history-before-backlog-retirement)
    - [E011. Decide the future backlog home](#e011-decide-the-future-backlog-home)
    - [E012. Update references and retire `TASKS.md` only after release](#e012-update-references-and-retire-tasksmd-only-after-release)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- This work stream owns repository automation and lifecycle cleanup that should support release readiness without distracting from product UX.
- It merges source-code formatting, Markdown formatting, `.editorconfig`, IntelliJ code style, and docs validation into one tooling decision to avoid competing formatters.
- `TASKS.md` retirement is intentionally sorted last because the current file still preserves useful history and remains referenced by governing documents.

## Progress Tracker

| Id   | Title                                                      | Priority | Status | Decision |
|------|------------------------------------------------------------|----------|--------|----------|
| E001 | Add Dependabot configuration                               | 1        | open   | accepted |
| E002 | Add CodeQL analysis                                        | 1        | open   | accepted |
| E003 | Add unified formatting and linting enforcement             | 2        | open   |          |
| E004 | Validate Gradle wrapper integrity in CI                    | 2        | open   |          |
| E005 | Add security policy and secret-scanning guidance           | 2        | open   |          |
| E006 | Add contributor intake files                               | 3        | open   |          |
| E007 | Add CODEOWNERS after reviewer identity is known            | 3        | open   |          |
| E008 | Enforce Apache-2.0 source headers                          | 4        | open   |          |
| E009 | Define the `TASKS.md` retirement trigger                   | 4        | open   |          |
| E010 | Preserve completed task history before backlog retirement  | 4        | open   |          |
| E011 | Decide the future backlog home                             | 4        | open   |          |
| E012 | Update references and retire `TASKS.md` only after release | 6        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current timestamp.
- Use `status` for implementation progress and `decision` for maintainer triage.
- Update the Proposal Implementation Summary in `docs/proposals/README.md` for accepted findings with non-terminal implementation status and an evidence path. A `TASKS.md` entry is optional when another evidence path is clearer.
- Leave completed, rejected, or superseded findings in place as history.

## Errors And Mistakes

### E001. Add Dependabot configuration

- Evidence: `PROP-repo-hygiene-automation` `E3` found no `.github/dependabot.yml`.
- Impact: Gradle, IntelliJ Platform Gradle Plugin, Kotlin, and GitHub Actions updates are not surfaced automatically.
- Proposal: Add Dependabot for `github-actions` and `gradle`, with grouped minor and patch updates and separate review for IntelliJ Platform major-line changes.

```yaml
status: open
decision: accepted
priority: 1
owner:
updated: 2026-05-15T11:43:16+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Source: PROP-repo-hygiene-automation E3."
```

### E002. Add CodeQL analysis

- Evidence: `PROP-repo-hygiene-automation` `E1` found no code-scanning workflow.
- Impact: Kotlin/Java weaknesses and supply-chain risks are not surfaced automatically before Marketplace publication.
- Proposal: Add a GitHub CodeQL workflow for Java/Kotlin on pull requests, pushes to `main`, and a scheduled cadence.

```yaml
status: open
decision: accepted
priority: 1
owner:
updated: 2026-05-15T11:43:16+02:00
accepted_at: 2026-05-15T11:43:16+02:00
comment: "Source: PROP-repo-hygiene-automation E1."
```

### E003. Add unified formatting and linting enforcement

- Evidence: `PROP-repo-hygiene-automation` `E2` found no Kotlin/Gradle formatter enforcement. `PROP-proposal-id-and-markdown-formatting` `E002`, `E003`, `E004`, and `S001` found no shared Markdown formatting anchor.
- Impact: Source and documentation style can drift between IntelliJ, AI agents, and CI, creating noisy diffs and manual review burden.
- Proposal: Choose one ADR-gated toolchain: a Gradle-integrated Kotlin/Gradle formatter such as Spotless with ktlint, plus markdownlint and `.editorconfig` for Markdown. Wire checks into CI and document the rules in `.agents/references/code-style.md`.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment: "Sources: PROP-repo-hygiene-automation E2; PROP-proposal-id-and-markdown-formatting E002-E004 and S001."
```

### E004. Validate Gradle wrapper integrity in CI

- Evidence: `PROP-repo-hygiene-automation` `E4` found no Gradle wrapper validation job.
- Impact: Signed release builds depend on a checked-in wrapper jar that is not independently validated in CI.
- Proposal: Add `gradle/wrapper-validation-action` to CI or a dedicated workflow for pull requests and pushes to `main`.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment: "Source: PROP-repo-hygiene-automation E4."
```

### E005. Add security policy and secret-scanning guidance

- Evidence: `PROP-repo-hygiene-automation` `E5` and `E6` found no `SECURITY.md` and no secret-scanning or push-protection guidance for release secrets.
- Impact: External reporters lack a private disclosure path, and maintainers lack a checklist for protecting Marketplace signing and publishing credentials.
- Proposal: Add `SECURITY.md` with supported versions, private reporting channel, response expectations, and secret-rotation notes. Add release guidance to verify GitHub secret scanning and push protection.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment: "Sources: PROP-repo-hygiene-automation E5 and E6."
```

### E006. Add contributor intake files

- Evidence: `PROP-repo-hygiene-automation` `E7` and `E9` found no `CONTRIBUTING.md`, pull request template, or issue templates.
- Impact: External contributors and bug reporters must infer build, validation, and lifecycle expectations from AI-facing docs.
- Proposal: Add a short `CONTRIBUTING.md`, one pull request template, and bug/feature issue templates that link to existing lifecycle, validation, support, and review docs.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment: "Sources: PROP-repo-hygiene-automation E7 and E9."
```

### E007. Add CODEOWNERS after reviewer identity is known

- Evidence: `PROP-repo-hygiene-automation` `E8` found no CODEOWNERS file.
- Impact: Dependabot and contributor PRs cannot request review automatically.
- Proposal: Add `.github/CODEOWNERS` after the maintainer's GitHub handle is known. Map repository-wide ownership and key paths such as `src/`, `.github/workflows/`, `docs/decisions/`, and `build.gradle.kts`.

```yaml
status: open
decision:
priority: 3
owner:
updated: 2026-05-15
comment: "Source: PROP-repo-hygiene-automation E8."
```

### E008. Enforce Apache-2.0 source headers

- Evidence: `PROP-repo-hygiene-automation` `E10` found no source-header enforcement.
- Impact: Per-file license clarity is weaker than expected for an Apache-2.0 Marketplace plugin.
- Proposal: Enforce headers through the formatter chosen in `E003` where possible, so the repository avoids a separate license-header plugin unless needed.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment: "Source: PROP-repo-hygiene-automation E10."
```

### E009. Define the `TASKS.md` retirement trigger

- Evidence: `PROP-remove-tasks-md-when-empty` `E1` identified that `TASKS.md` has an empty open backlog but no accepted retirement condition.
- Impact: The file can be deleted too early or kept forever as historical noise.
- Proposal: ADR-gate a trigger: retire `TASKS.md` only when the open backlog has no unchecked tasks and no active plan references unresolved `T-AREA-NNN` work.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment: "Source: PROP-remove-tasks-md-when-empty E1."
```

### E010. Preserve completed task history before backlog retirement

- Evidence: `PROP-remove-tasks-md-when-empty` `E4` identified that `TASKS.md` is the consolidated index of completed task IDs.
- Impact: Deleting it without an archive would break traceability for historical task IDs used in plans, ADRs, and commits.
- Proposal: Move completed task history to `docs/history/completed-tasks.md` or another accepted archive location before removing `TASKS.md`.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment: "Source: PROP-remove-tasks-md-when-empty E4."
```

### E011. Decide the future backlog home

- Evidence: `PROP-remove-tasks-md-when-empty` `E5` found that `AGENTS.md` and proposal rules still route backlog items to `TASKS.md`.
- Impact: Removing `TASKS.md` without a replacement rule leaves future work intake ambiguous.
- Proposal: Decide whether plans become the sole backlog owner or whether `TASKS.md` is recreated lazily when new open tasks exist.

```yaml
status: open
decision:
priority: 4
owner:
updated: 2026-05-15
comment: "Source: PROP-remove-tasks-md-when-empty E5."
```

### E012. Update references and retire `TASKS.md` only after release

- Evidence: `PROP-remove-tasks-md-when-empty` `E2` and `E3` found many references to `TASKS.md` across governing docs.
- Impact: Removing `TASKS.md` now would create dead links and weaken traceability before first release.
- Proposal: Defer file removal until after first public release or an explicit maintainer decision. When the trigger is met, update every reference and remove the file in the same change.

```yaml
status: open
decision:
priority: 6
owner:
updated: 2026-05-15
comment: "Sources: PROP-remove-tasks-md-when-empty E2 and E3."
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- Prefer one formatting decision over separate source and documentation formatter debates.
- Keep backlog-retirement work separate from release-critical automation unless the maintainer explicitly pulls it forward.

## Suggested Priority Order

1. `E001` - add Dependabot.
2. `E002` - add CodeQL.
3. `E004` - validate the Gradle wrapper.
4. `E005` - add `SECURITY.md` and secret guidance.
5. `E003` - choose and wire formatting/linting after release-safety automation is in place.
6. `E006` and `E007` - add contributor intake once reviewer identity is known.
7. `E008` - enforce license headers through the chosen formatter.
8. `E009`, `E010`, and `E011` - decide backlog retirement after release.
9. `E012` - retire `TASKS.md` only after references and history are ready.

## Out Of Scope

- Plugin runtime UX changes.
- Multi-agent execution topology or worker attribution.
- Marketplace publication itself.
- Legal advice beyond repository policy documentation.
