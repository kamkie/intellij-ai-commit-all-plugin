---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T23:31:44+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Centralize Proposal Editing Guidance

## Context and Problem Statement

`docs/proposals/` proposal files currently repeat a required `## How To Edit The Trackers` section in every proposal. That section is workflow guidance, while the proposal file's durable value is the findings, evidence, triage state, and implementation status.

The repeated guidance makes proposal files longer, creates another source of rule drift when proposal tracker rules change, and makes new proposal authoring feel heavier than the advisory triage workflow needs to be. Proposal files also place each finding category as a top-level `##` section, which spreads the proposal payload across the same heading level as summary, tracker, priority, and scope sections.

The current per-finding fenced YAML tracker blocks are practical for validation but visually interrupt each proposal item. The approved draft for `T-IDEA-006` keeps the same tracker semantics in a human-readable metadata table and adds explicit proposal provenance.

## Decision Drivers

* Keep proposal files focused on summary, findings, evidence, triage state, and implementation status.
* Preserve the tracker and validation guarantees from ADR 0033, ADR 0034, ADR 0043, and ADR 0049 while allowing a more readable item layout.
* Avoid copying proposal editing rules into every new proposal file.
* Make proposal findings visually belong to one proposal payload section.
* Capture why and how each proposal was created without burying that context in item comments.
* Support feature proposals alongside errors, duplications, simplifications, and stylistic notes.
* Keep archived proposal history stable.
* Make the proposal template easier to start from without weakening maintainer triage gates.

## Considered Options

* Adopt the approved ergonomic proposal template
* Keep the repeated tracker-editing section in every proposal
* Remove proposal trackers from proposal files
* Keep the required section but shorten the template text

## Decision Outcome

Chosen option: "Adopt the approved ergonomic proposal template", because tracker editing rules are repository workflow guidance and should have one owner, while proposal findings should sit under one clear payload section with readable item metadata.

New proposal files should not require a `## How To Edit The Trackers` section. `docs/proposals/README.md` remains the owner for proposal editing workflow, status and decision vocabularies, tracker mirroring, implementation-summary rules, and archive rules.

New proposal front matter should add `created_from` after `generated_at`:

```yaml
created_from: User request, task ID, review, audit, design pass, or other trigger.
```

New proposal files should include `## Creation Context` after `## Summary`. This section records why the proposal exists, how it was created, and the scope guardrails that shaped the proposal.

New proposal files should group findings under a single `## Proposal Items` section:

* Finding category headings should be `### New Features`, `### Errors And Mistakes`, `### Duplications To Remove Or Reduce`, `### Simplification Opportunities`, and `### Smaller / Stylistic Items`.
* New Features should appear first so proposal categories move from largest change to smallest.
* Tracked finding headings should be `#### <Id>. <Short title>`.
* New feature finding IDs should use `F001`, `F002`, and later values.
* The table of contents should include the `## Proposal Items` entry and its nested category and finding entries when present.
* `docs/proposals/README.md` and `scripts/validate-docs.ps1` should be updated after acceptance so the required structure and finding detection match the new heading levels.

Per-finding fenced YAML tracker blocks should be replaced by one metadata table immediately under each tracked finding heading:

```markdown
| Field       | Value                     |
|-------------|---------------------------|
| Status      | open                      |
| Decision    |                           |
| Decision at |                           |
| Priority    | 1                         |
| Owner       |                           |
| Updated     | YYYY-MM-DDTHH:mm:ss+HH:mm |
```

The per-finding metadata table remains the source of truth for `Status`, `Decision`, `Decision at`, `Priority`, `Owner`, and `Updated`. `Decision at` replaces the older split between `accepted_at` and `decided_at`; leave it empty while `Decision` is empty and fill it whenever `Decision` becomes non-empty.

Each tracked finding should use these content sections after the metadata table:

* `##### Context`, with evidence, impact, non-goals, and acceptance criteria.
* `##### Recommended Change`.
* `##### Review Notes`, defaulting to `- none`.
* `##### Follow-Up`, with expected artifact and validation.

The proposal template should stay content-focused:

* Required front matter.
* Title and short contract paragraph.
* Table of contents.
* Summary.
* Creation context.
* Progress tracker.
* Proposal items section with finding groups.
* Suggested priority order.
* Out of scope.

The template may include one short pointer to `docs/proposals/README.md` near the progress tracker, but it should not duplicate the tracker-editing checklist. The pointer should mention tracker mirroring, status and decision vocabulary, and Proposal Implementation Summary updates.

Archived proposals may keep their existing `## How To Edit The Trackers` sections, fenced YAML tracker blocks, timestamp fields, ID vocabulary, and current heading structure. Active proposals that are materially touched after this decision is implemented may adopt the new front matter, `## Creation Context`, metadata tables, `F` finding IDs, and `## Proposal Items`, but historical proposal files do not need churn solely for this cleanup.

### Consequences

* Good, because proposal files become easier to scan and author.
* Good, because tracker editing rules have one durable owner.
* Good, because proposal findings sit under one recognizable proposal payload section.
* Good, because feature proposals have a first-class category and stable finding IDs.
* Good, because proposal provenance is visible near the top of each file.
* Good, because item metadata becomes readable Markdown instead of fenced YAML.
* Good, because archived proposal history does not need broad rewrites.
* Bad, because readers editing a proposal may need to open `docs/proposals/README.md` for full tracker-editing rules.
* Bad, because documentation validation, proposal heading rules, front matter rules, item metadata parsing, finding ID vocabulary, and the proposal template need coordinated updates after acceptance.
* Bad, because changing tracker representation from YAML to tables increases validator parsing complexity.

### Confirmation

Implementation updates `docs/proposals/README.md`, `docs/proposals/PROPOSAL_TEMPLATE.md`, `scripts/validate-docs.ps1`, and `TASKS.md` together.

Confirmation should include:

* `scripts/validate-docs.ps1` passes.
* `git diff --check` passes.
* Proposal validation still enforces front matter, proposal IDs, progress tracker rows, per-finding metadata tables, status and decision vocabulary, heading-level consistency, finding ID vocabulary, and README implementation-summary consistency.

## Pros and Cons of the Options

### Adopt the approved ergonomic proposal template

* Good, because it removes boilerplate from every new proposal file.
* Good, because `docs/proposals/README.md` already owns the complete proposal workflow.
* Good, because future tracker-rule changes need fewer synchronized text edits.
* Good, because one `## Proposal Items` section separates findings from proposal metadata and wrap-up sections.
* Good, because item metadata stays visible without fenced YAML blocks.
* Good, because `created_from` and `## Creation Context` make proposal provenance explicit.
* Neutral, because proposal authors still need to keep progress tracker rows and item metadata tables in sync.
* Bad, because a proposal file is slightly less self-contained.
* Bad, because heading-level, ID-vocabulary, metadata-table, and timestamp-field changes require validator and README updates, not just template text edits.

### Keep the repeated tracker-editing section in every proposal

* Good, because every proposal remains self-contained for tracker edits.
* Good, because no validation or template change is needed.
* Bad, because every proposal repeats workflow rules that can drift from the README.
* Bad, because proposal files stay noisier than their triage purpose requires.
* Bad, because finding groups remain spread across the same heading level as proposal metadata sections.
* Bad, because fenced YAML tracker blocks remain visually heavier than table metadata.

### Remove proposal trackers from proposal files

* Good, because proposal files would become shorter.
* Bad, because it would undermine ADR 0033 and ADR 0049 by moving triage and implementation status away from the findings they describe.
* Bad, because it would need a larger replacement workflow and validation design.
* Bad, because accepted proposal implementation summary checks would lose their per-finding source of truth.

### Keep the required section but shorten the template text

* Good, because it reduces some immediate authoring noise.
* Good, because it preserves self-contained editing guidance.
* Bad, because the required duplicate section remains a second owner for tracker-editing rules.
* Bad, because it only partially addresses proposal-file ergonomics.
* Bad, because it does not group proposal findings under one payload section.
* Bad, because it does not improve front matter provenance or per-finding tracker readability.

## More Information

- Source task: `TASKS.md` `T-IDEA-006`.
- Related decisions: ADR 0033, ADR 0034, ADR 0043, and ADR 0049.
- Implementation evidence: proposal rules, the proposal template, documentation validation, and the source task were updated together.
