---
proposal_id: PROP-repository-analysis
generated_at: 2026-05-15T01-47
purpose: Propose repository cleanup and implementation-readiness improvements after a broad documentation and backlog review.
scope: Covers current documentation, task backlog, ADRs, AI guidance, proposal workflow, and repository scaffold state.
---

# Repository Analysis Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `OPEN_QUESTIONS.md`, `docs/proposals/README.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement the proposed cleanup or plugin behavior.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E1. Stale split-button styling guidance remains in accepted ADRs](#e1-stale-split-button-styling-guidance-remains-in-accepted-adrs)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
    - [D1. Scope decisions are repeated across user and agent documents](#d1-scope-decisions-are-repeated-across-user-and-agent-documents)
    - [D2. Completed decision tasks duplicate ADR history in the backlog](#d2-completed-decision-tasks-duplicate-adr-history-in-the-backlog)
    - [D3. Marketplace documentation and release tasks overlap](#d3-marketplace-documentation-and-release-tasks-overlap)
    - [D4. Multi-task execution rules are repeated across too many guidance files](#d4-multi-task-execution-rules-are-repeated-across-too-many-guidance-files)
- [Simplification Opportunities](#simplification-opportunities)
    - [S1. Add an ADR index or decision map](#s1-add-an-adr-index-or-decision-map)
    - [S2. Add lightweight documentation consistency checks](#s2-add-lightweight-documentation-consistency-checks)
    - [S3. Prioritize a scaffold plan before adding more process rules](#s3-prioritize-a-scaffold-plan-before-adding-more-process-rules)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- No executable plugin code is present yet, so this review found repository and documentation risks rather than runtime defects.
- Targeted checks found no broken local Markdown links, no duplicate `TASKS.md` task IDs, no trailing whitespace in the reviewed Markdown files, and no Gradle/Kotlin/plugin scaffold files.
- The main cleanup theme is decision drift: accepted ADRs, `README.md`, `TASKS.md`, support docs, and AI guidance repeat the same facts in ways that can go stale.
- The most useful next implementation move is an accepted scaffold plan, because many open tasks cannot be validated until Gradle, Kotlin, `plugin.xml`, and `runIde` exist.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id | Title                                                                  | Priority | Status | Decision |
|----|------------------------------------------------------------------------|----------|--------|----------|
| E1 | Stale split-button styling guidance remains in accepted ADRs           | 2        | open   |          |
| D1 | Scope decisions are repeated across user and agent documents           | 2        | open   |          |
| D2 | Completed decision tasks duplicate ADR history in the backlog          | 2        | open   |          |
| D3 | Marketplace documentation and release tasks overlap                    | 2        | open   |          |
| D4 | Multi-task execution rules are repeated across too many guidance files | 3        | open   |          |
| S1 | Add an ADR index or decision map                                       | 3        | open   |          |
| S2 | Add lightweight documentation consistency checks                       | 5        | open   |          |
| S3 | Prioritize a scaffold plan before adding more process rules            | 4        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E1. Stale split-button styling guidance remains in accepted ADRs

- Evidence: `docs/decisions/0006-use-split-button-for-commit-and-push.md:11` says detailed visual styling remains deferred, and `docs/decisions/0006-use-split-button-for-commit-and-push.md:24` says detailed icon and styling choices remain open. `docs/decisions/0016-reuse-standard-intellij-error-messages.md:46` and `docs/decisions/0017-use-standard-ide-confirmation-barriers.md:46` still say to keep `Q-UX-5` open. Later, `docs/decisions/0027-use-generated-placeholder-graphic-for-split-button-styling.md:38` closes `Q-UX-6`, and `OPEN_QUESTIONS.md:5` says there are no open UX questions.
- Impact: A future agent reading only the most specific older ADR can reasonably think split-button styling is still unresolved, even though the current guidance points to the generated placeholder.
- Proposal: Add short "Current state" or "Resolved later by ADR 0027" notes to the stale follow-up areas. Preserve the historical decisions, but make the currently applicable styling owner explicit.

```yaml
status: open
decision: accepted
priority: 2
owner:
updated: 2026-05-15
comment:
```

## Duplications To Remove Or Reduce

### D1. Scope decisions are repeated across user and agent documents

- Evidence: `README.md:7` through `README.md:26` lists the current scope decisions. Similar facts are repeated in `SUPPORT.md:7` through `SUPPORT.md:11`, `docs/WORKING_WITH_AI.md:57` through `docs/WORKING_WITH_AI.md:64`, and the completed decision section of `TASKS.md:11` through `TASKS.md:31`.
- Impact: Supported IDE baseline, Git scope, AI Assistant dependency, Marketplace publication, validation target, license, plugin ID, and vendor metadata now have several text owners. Any future change must update many places or leave contradictions.
- Proposal: Pick a narrower owner for each audience. Keep `README.md` user-facing, keep `SUPPORT.md` support-facing, keep ADRs authoritative for decisions, and make `docs/WORKING_WITH_AI.md` point to those owners instead of restating every value.

```yaml
status: open
decision: rejected
priority: 2
owner:
updated: 2026-05-15
comment: it is as designed. duplication in support is expected. as is WORKING_WITH_AI. TASKS will be cleaned after release.
```

### D2. Completed decision tasks duplicate ADR history in the backlog

- Evidence: `TASKS.md:11` through `TASKS.md:31` contains 21 completed decision tasks that mirror accepted ADRs. A task-ID check found 119 task IDs in `TASKS.md`, with pending implementation tasks starting only at `TASKS.md:35`.
- Impact: The actionable backlog is harder to scan, and completed decision records have two maintenance surfaces: the ADR and the completed `T-DEC-*` task.
- Proposal: Reorder or compact `TASKS.md` so pending implementation work appears first and completed decision history is clearly secondary. Keep stable task IDs intact; do not renumber or reuse them.

```yaml
status: open
decision: rejected
priority: 2
owner:
updated: 2026-05-15
comment:
```

### D3. Marketplace documentation and release tasks overlap

- Evidence: `TASKS.md:138` asks to document the Marketplace source code link, while `TASKS.md:150` asks to add the official source code link to Marketplace metadata. `TASKS.md:139` asks to document Marketplace publication process and secrets, while `TASKS.md:152` and `TASKS.md:153` cover publishing configuration and first-upload documentation. ADR 0018 also calls for source code and metadata updates at `docs/decisions/0018-use-apache-2-license.md:66` and `docs/decisions/0018-use-apache-2-license.md:67`.
- Impact: Release and documentation work can double-count the same Marketplace source-link and publication-process items, or leave unclear whether the owner is metadata, README/support docs, or release automation.
- Proposal: Split ownership by artifact: `T-REL-*` should own Gradle/plugin/Marketplace metadata and publishing mechanics, while `T-DOC-*` should own user- or contributor-facing documentation. Adjust wording to remove duplicate source-link and first-upload responsibilities.

```yaml
status: open
decision: accepted
priority: 2
owner:
updated: 2026-05-15
comment:
```

### D4. Multi-task execution rules are repeated across too many guidance files

- Evidence: Plan readiness, stop-on-question behavior, fresh task workers, orchestrator ownership, and per-task commits are repeated in `.agents/references/planning.md:47` through `.agents/references/planning.md:89`, `.agents/references/execution.md:34` through `.agents/references/execution.md:88`, `.agents/plans/README.md:14` through `.agents/plans/README.md:20`, `docs/DEVELOPMENT_LIFECYCLE.md:28` through `docs/DEVELOPMENT_LIFECYCLE.md:42`, and `docs/WORKING_WITH_AI.md:90` through `docs/WORKING_WITH_AI.md:96`.
- Impact: These rules are important, but spreading full wording across many files makes future process changes churn-heavy and increases the chance that agents follow an older copy.
- Proposal: Make `.agents/references/planning.md` and `.agents/references/execution.md` the detailed AI-facing owners. Shorten higher-level docs to one-sentence summaries plus links. Keep ADRs as history, not live process checklists.

```yaml
status: open
decision: accepted
priority: 3
owner:
updated: 2026-05-15
comment:
```

## Simplification Opportunities

### S1. Add an ADR index or decision map

- Evidence: `docs/decisions/README.md:1` through `docs/decisions/README.md:32` explains how to use ADRs, but it does not list the current 35 ADR files from `0000` through `0034`.
- Impact: Agents and maintainers must search or open many files to find the current owner for a decision. That pushes against the repository rule to keep task context small.
- Proposal: Add a compact table to `docs/decisions/README.md` with ADR number, title, status, date, and topic. This can make targeted reading easier without changing decision content.

```yaml
status: open
decision: accepted
priority: 3
owner:
updated: 2026-05-15
comment: with clickable links
```

### S2. Add lightweight documentation consistency checks

- Evidence: This repository already has stable task IDs, proposal IDs, proposal tracker rules, local Markdown links, and ADR numbering rules. The current review checked several manually: local Markdown links, duplicate `TASKS.md` IDs, trailing whitespace, ADR count, and absence of scaffold files.
- Impact: Manual checks are easy to skip as the documentation set grows. Drift in IDs, proposal trackers, local links, or stale indexes would not be caught until review.
- Proposal: Add a small local validation command later, for example a PowerShell script under `scripts/`, to check local Markdown links, unique task IDs, ADR filename sequence, proposal front matter, and proposal tracker/table parity. Hook it into CI when CI exists.

```yaml
status: open
decision: accepted
priority: 5
owner:
updated: 2026-05-15
comment:
```

### S3. Prioritize a scaffold plan before adding more process rules

- Evidence: `README.md:40` says no Gradle, Kotlin, or IntelliJ plugin scaffold exists. `TASKS.md:35` through `TASKS.md:42` lists scaffold tasks, and `rg --files -g "build.gradle*" -g "settings.gradle*" -g "src/**" -g "*.kt" -g "*.kts"` returned no scaffold or source files.
- Impact: Many backlog items describe implementation behavior that cannot be compiled, run, or validated yet. Adding more process documentation before scaffolding increases documentation surface without reducing plugin delivery risk.
- Proposal: Make the next implementation-oriented artifact an accepted `P-scaffold-plugin-project` plan covering Gradle Kotlin DSL, IntelliJ Platform Gradle Plugin, plugin descriptor, base package, AI Assistant dependency identification, and `runIde` validation.

```yaml
status: open
decision: accepted
priority: 4
owner:
updated: 2026-05-15
comment: i intended to do exactly that after this review.
```

## Smaller / Stylistic Items

- `OPEN_QUESTIONS.md` currently has only `## UX Decisions` plus "No open UX questions." If there are no open questions of any kind, a top-level "No open questions" sentence would be clearer.
    - ok. do that. move OPEN_QUESTIONS.md to docs/decisions/
- `docs/proposals/README.md` has a "Current Proposal" section that lists the current proposal number and title.
    - and? what is you point?
- Consider adding a short "Repository State" line near the top of `TASKS.md` that says the project is documentation-only until the scaffold tasks land.
    - ok. do that
- Keep generated concept artwork clearly separated from final plugin assets; `docs/concepts/graphics/README.md` already does this well.
    - as intended

## Suggested Priority Order

1. `E1` - remove stale styling signals before implementation starts reading accepted ADRs for UI work.
2. `D3` - clarify Marketplace task ownership before release and metadata tasks start.
3. `D1` and `D2` - reduce duplicated decision summaries so future scope changes are easier to apply consistently.
4. `D4` - centralize process rules after the active behavior/docs duplication is handled.
5. `S1` and `S2` - add navigation and consistency support when the maintainer is ready for small tooling/docs maintenance.
6. `S3` - use the cleanup outcome to feed the next implementation plan rather than adding more repository process.

## Out Of Scope

- No plugin runtime behavior, build scaffold, Gradle configuration, Kotlin code, `plugin.xml`, or CI is changed by this proposal.
- No ADR is superseded or rewritten by this proposal.
- No external JetBrains API, Marketplace, or AI Assistant dependency documentation was verified during this local repository analysis.
- No release readiness claim is made; the repository remains pre-scaffold and pre-release.
