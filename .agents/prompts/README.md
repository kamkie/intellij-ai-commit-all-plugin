# Repository Prompts

`.agents/prompts/README.md` owns the catalog, loading mechanism, and maintenance rules for reusable repository prompt recipes.
Use these prompts only for named repository-specific session starters that are more concrete than `.agents/references/` guidance and not substantial enough to become `.agents/skills/`.

## Loading Mechanism

Use repository prompts through a two-stage load:

1. Identify the requested prompt by exact title, filename, or catalog entry.
2. Load only the matching prompt, then follow that prompt's declared read set.

Rules:

- do not bulk-load `.agents/prompts/` to discover intent
- use this catalog or a targeted search only when the requested prompt name is ambiguous
- if more than one prompt matches, ask which prompt to run unless the requested outcome clearly selects one
- treat a prompt's `Read first` section as the prompt-local initial context
- load extra references, docs, plans, reports, or skills only when the prompt or current request gives a concrete trigger
- prompts can narrow or shape context for a session, but they do not override the current user request, `AGENTS.md`, accepted ADRs, approved plans, or executable contract artifacts

## Rules

- keep prompts narrow, single-purpose, and self-contained
- name the smallest useful read set; avoid broad repository scans
- bound outputs by naming the expected report, summary, plan, or artifact location
- keep durable policy in `.agents/references/`, not prompts
- keep implementation sequencing in `.agents/plans/`, not prompts
- keep active backlog and task refs in `TASKS.md` and `TASKS_ARCHIVE.md`, not prompts
- keep executable or strongly repeatable workflows in `.agents/skills/`, not prompts
- do not add metadata preambles; keep catalog metadata here
- update this README when adding, renaming, moving, or removing prompts

## Current Prompts

| Prompt                                                      | Use When                                                                                                                                                     |
|-------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [ADR Impact Check](adr-impact-check.md)                     | A requested change may affect repository decisions, workflow rules, validation policy, compatibility, plans, tasks, or owner docs.                           |
| [Archive Completed Work](archive-completed-work.md)         | Completed task work needs archive readiness checked or moved mechanically from `TASKS.md` to `TASKS_ARCHIVE.md`.                                             |
| [Backlog Triage](backlog-triage.md)                         | `TASKS.md`, `TASKS_ARCHIVE.md`, and open questions need stale, duplicate, blocked, or misplaced work reviewed.                                               |
| [Bug Report Triage](bug-report-triage.md)                   | A reported plugin problem, screenshot, log path, repro note, or validation failure needs ownership, repro status, and next-path classification.              |
| [Change Closeout](change-closeout.md)                       | A completed ordinary change needs handoff or commit readiness checked for scope, docs, validation, review risk, and required follow-up.                      |
| [CI Failure Triage](ci-failure-triage.md)                   | GitHub Actions, local CI, Gradle, lint, docs validation, test, packaging, or verifier failures need a narrow fix path.                                       |
| [Compact AI Guidance](compact-ai-guidance.md)               | Standing AI instruction files need duplicate, stale, or misplaced guidance compacted without changing current policy.                                        |
| [Design Draft Session](design-draft-session.md)             | Concept graphics, UI draft variants, visual state coverage, or design-only iterations need a bounded session before implementation.                          |
| [Evaluate AI Guidance](evaluate-ai-guidance.md)             | A report-only assessment of repository AI guidance, lifecycle coverage, ownership, duplication, or context-load cost is needed.                              |
| [IDE Log Triage](ide-log-triage.md)                         | IntelliJ IDE logs need analysis after the user grants permission for a log folder or provides sanitized excerpts.                                            |
| [Manual Sandbox Validation](manual-sandbox-validation.md)   | Plugin behavior needs manual IntelliJ sandbox scenario planning or reporting beyond automated tests.                                                         |
| [Plugin Compatibility Sweep](plugin-compatibility-sweep.md) | IntelliJ Platform, Gradle IntelliJ Plugin, Kotlin, JDK, plugin descriptor, dependency, verifier, or supported IDE compatibility risk needs review.           |
| [Proposal Consolidation](proposal-consolidation.md)         | Proposal documents need duplicate, supersession, ref, progress-tracker, decision, or implementation-summary consistency reviewed or updated.                 |
| [Repository Quality Audit](repository-quality-audit.md)     | A broad repository audit should identify errors, mistakes, duplication, simplification opportunities, validation gaps, or proposal-worthy findings.          |
| [Repository State Snapshot](repository-state-snapshot.md)   | Worktree, tasks, open questions, ADRs, proposals, plans, and next-task readiness need a concise cross-artifact status report.                                |
| [Release Readiness](release-readiness.md)                   | A plugin release boundary needs blockers, required validations, changelog/support status, packaging, signing, CI, tag, or Marketplace readiness checked.     |
| [Toolchain Upgrade](toolchain-upgrade.md)                   | CI actions, Java dependencies, Gradle plugins/tools, plugin verifier targets, Gradle wrapper versions, or release tool upgrades need an implementation plan. |
