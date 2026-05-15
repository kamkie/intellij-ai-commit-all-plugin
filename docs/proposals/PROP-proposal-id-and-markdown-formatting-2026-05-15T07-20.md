---
proposal_id: PROP-proposal-id-and-markdown-formatting
generated_at: 2026-05-15T07-20
purpose: Propose fixed-width three-digit IDs for proposal finding points and a unified markdown formatting convention shared by IntelliJ, AI agents, and the docs validation script.
scope: Proposal authoring conventions under `docs/proposals/` (template, README rules, existing files) and the markdown formatting rules consumed by IntelliJ's reformat, AI agents, and `scripts/validate-docs.ps1`.
---

# Proposal Point IDs And Unified Markdown Formatting Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E001. Variable-width point IDs (E1, E10) break visual alignment and sorting](#e001-variable-width-point-ids-e1-e10-break-visual-alignment-and-sorting)
    - [E002. Markdown formatting drifts between IntelliJ reformat, AI agents, and the validation script](#e002-markdown-formatting-drifts-between-intellij-reformat-ai-agents-and-the-validation-script)
    - [E003. Nested list indentation is inconsistent across proposals](#e003-nested-list-indentation-is-inconsistent-across-proposals)
    - [E004. Markdown tables are not consistently column-aligned](#e004-markdown-tables-are-not-consistently-column-aligned)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
    - [S001. Add a shared `.editorconfig` and IntelliJ code style to anchor markdown rules](#s001-add-a-shared-editorconfig-and-intellij-code-style-to-anchor-markdown-rules)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- Proposal point IDs currently use variable widths (`E1` … `E10`), which misaligns tables, anchors, and sort order; switching to a fixed three-digit form (`E001` … `E010` … `E100`) makes lists and tables visually stable and lexicographically sortable.
- Markdown produced by IntelliJ's reformat, by AI agents (per `.agents/references/`), and accepted by `scripts/validate-docs.ps1` is not driven by a single shared convention, so nested lists and tables drift between commits depending on who touched the file last.
- This proposal performs no implementation; each finding is advisory until accepted via the normal ADR/plan flow defined in `docs/decisions/README.md` and `.agents/references/planning.md`.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id   | Title                                                                            | Priority | Status | Decision |
|------|----------------------------------------------------------------------------------|----------|--------|----------|
| E001 | Variable-width point IDs (E1, E10) break visual alignment and sorting            | 1        | open   |          |
| E002 | Markdown formatting drifts between IntelliJ reformat, AI agents, and validation  | 1        | open   |          |
| E003 | Nested list indentation is inconsistent across proposals                         | 2        | open   |          |
| E004 | Markdown tables are not consistently column-aligned                              | 2        | open   |          |
| S001 | Add a shared `.editorconfig` and IntelliJ code style to anchor markdown rules    | 2        | open   |          |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E001. Variable-width point IDs (E1, E10) break visual alignment and sorting

- Evidence: `docs/proposals/PROP-repo-hygiene-automation-2026-05-15T06-45.md` uses `E1` … `E10`, forcing the `Id` column header to be widened only for the last row and producing a lexicographic order where `E10` sorts before `E2`. The Table of Contents anchors mix `e1-...` and `e10-...`. Other active proposals follow the same pattern.
- Impact: Tables in the Progress Tracker need manual re-padding whenever a two-digit ID appears; AI agents and IntelliJ reformat disagree about how to pad the column; sorting findings (in scripts, in `Compare-Object` in `scripts/validate-docs.ps1`, or visually) is order-by-string and therefore misleading. Cross-references in commits, ADRs, and READMEs become ambiguous (`E1` vs. `E10`).
- Proposal: Adopt a fixed three-digit, zero-padded width for all finding IDs in proposals:
    - Errors: `E001`, `E002`, …, `E999`.
    - Duplications: `D001`, `D002`, ….
    - Simplifications: `S001`, `S002`, ….
- Update `docs/proposals/PROPOSAL_TEMPLATE.md`, the rules in `docs/proposals/README.md`, and the regex in `scripts/validate-docs.ps1` (currently `(E\d+|D\d+|S\d+)`) to require exactly three digits: `(E\d{3}|D\d{3}|S\d{3})`. Keep existing proposals as-is for history; renumber only when a proposal is materially updated. Record the change as an ADR before implementation per `docs/decisions/README.md`.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E002. Markdown formatting drifts between IntelliJ reformat, AI agents, and the validation script

- Evidence: There is no `.editorconfig`, no IntelliJ-exported code style under `.idea/codeStyles/`, and no markdown linter configuration (no `.markdownlint.json`, no `markdownlint-cli2` step in `scripts/validate-docs.ps1`). `.agents/references/code-style.md` is referenced by `AGENTS.md` but is not mechanically applied. IntelliJ's default Markdown formatter, by contrast, re-flows tables, normalizes list indents to 4 spaces, and may insert hard-wrap line breaks; AI agents tend to produce 2-space nested indents and ungroomed tables.
- Impact: Each pass by a different tool reformats the same files differently, generating noisy diffs unrelated to content; reviewers cannot tell intent from churn. The `Progress Tracker` tables and nested lists in the proposal template are the most visible casualties.
- Proposal: Adopt a single source of truth for markdown formatting and wire all three consumers to it:
    1. Add `.editorconfig` at the repo root declaring, at minimum: `indent_style = space`, `indent_size = 4` for `*.md` (matches existing TOC indentation), `trim_trailing_whitespace = true` (with `false` override for Markdown hard breaks if used), `insert_final_newline = true`, `end_of_line = lf`.
    2. Export an IntelliJ code style under `.idea/codeStyles/Project.xml` + `codeStyleConfig.xml` pinning the Markdown formatter to those values (list indent 4, no hard wrap, keep table column padding).
    3. Add `.markdownlint.json` (or `markdownlint-cli2.jsonc`) capturing the same rules, and call it from `scripts/validate-docs.ps1` so CI fails on drift; document the rule set in `.agents/references/code-style.md` so AI agents follow it.
- Pick exactly one tool family (markdownlint) to avoid duplicate enforcement. Document the decision in a new ADR before implementation per `docs/decisions/README.md`.

```yaml
status: open
decision:
priority: 1
owner:
updated: 2026-05-15
comment:
```

### E003. Nested list indentation is inconsistent across proposals

- Evidence: `PROPOSAL_TEMPLATE.md` uses 4-space indentation for the nested TOC entry (`    - [E1. Example error]`). Some proposal bodies use 4 spaces for nested sub-items (`PROP-repo-hygiene-automation-2026-05-15T06-45.md` lines around `E2`, `E3`), while others use 2 spaces. IntelliJ's default Markdown reformat collapses to its configured indent, and AI agents default to 2 spaces unless instructed otherwise.
- Impact: Some renderers (GitHub, IntelliJ preview) interpret 2-space nested bullets under a `- ` parent as a continuation paragraph rather than a sub-list, producing a different visual hierarchy than authors intended. Reformatting flips the rendering of nested points between commits.
- Proposal: Standardize on **4-space** indentation per nesting level for unordered lists in markdown (matches the existing TOC convention and IntelliJ's default for `- ` lists). Enforce via the `.editorconfig` + markdownlint rules from E002 (`MD007 { "indent": 4 }`). Forbid mixing tab/space indentation in markdown. Apply the rule to ordered lists as well (`MD029` consistent, `1.` first marker).

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

### E004. Markdown tables are not consistently column-aligned

- Evidence: Progress Tracker tables in different proposals use different column-padding widths; adding a row with a longer title (or moving from `E1` to `E10`) leaves the header separator and other rows un-repadded. `scripts/validate-docs.ps1` validates only the IDs present in the table (regex on `^\|(E\d+|D\d+|S\d+) \|`), not table structure or alignment.
- Impact: Diffs become noisy because reformatting can re-pad all rows; readers cannot scan the tracker easily; AI agents and IntelliJ reformat disagree on whether to pad to the widest cell or to keep author spacing.
- Proposal: Define and enforce a single table style:
    - All pipe tables must have a leading `|` and trailing `|` on every row.
    - Header separator cells must have at least three `-`.
    - All cells in a column padded with spaces to the width of the widest cell in that column.
    - Enable markdownlint `MD055` (table pipe style: `leading_and_trailing`), `MD056` (consistent column count), and `MD058` (blank lines around tables).
- Provide an IntelliJ "Reformat Code" action setting that produces the same output, and instruct AI agents in `.agents/references/code-style.md` to follow it.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

### S001. Add a shared `.editorconfig` and IntelliJ code style to anchor markdown rules

- Evidence: Markdown rules are currently described in prose across `AGENTS.md`, `.agents/references/code-style.md`, and `docs/proposals/README.md`. There is no machine-readable anchor (no `.editorconfig`, no IDE code style export, no linter config). `scripts/validate-docs.ps1` checks structural invariants (front matter, IDs, tracker mirror) but not formatting.
- Impact: Without a single anchor, the rules from E002, E003, and E004 cannot be enforced cheaply; every contributor must remember them.
- Proposal: Land one small `.editorconfig` + one `.markdownlint.json` + one IntelliJ code style export in a single ADR-gated change, then reference them from `AGENTS.md` and `.agents/references/code-style.md`. Treat that change as the prerequisite implementation task for E002–E004.

```yaml
status: open
decision:
priority: 2
owner:
updated: 2026-05-15
comment:
```

## Smaller / Stylistic Items

- Consider widening anchor links in proposal TOCs to match three-digit IDs once E001 is accepted (e.g., `#e001-...`).
- When renumbering legacy proposals (if ever), keep the original filename to preserve the stable `proposal_id` per `docs/proposals/README.md`.

## Suggested Priority Order

1. `E002` - pick the formatting anchor (markdownlint + `.editorconfig` + IntelliJ code style) so subsequent fixes are mechanical.
2. `S001` - land the anchor files; this unblocks E003 and E004 enforcement.
3. `E001` - adopt three-digit IDs in the template, README, and validator regex; new proposals immediately benefit.
4. `E003` - enforce 4-space nested list indentation via the now-available linter.
5. `E004` - enforce table styling via the now-available linter.

## Out Of Scope

- Changes to non-markdown code style (Kotlin, Gradle Kotlin DSL) - covered separately by `PROP-repo-hygiene-automation`.
- Retroactive reformatting of `docs/decisions/` ADRs and `.agents/references/` files; those follow the same rules once accepted but their renumbering/reformatting is out of this proposal's triage.
- Changes to the `proposal_id` naming format (`PROP-<kebab-slug>`) defined in `docs/proposals/README.md` and ARD-0034.
