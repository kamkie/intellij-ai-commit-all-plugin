---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Fixed-Width Proposal Finding IDs And Empty Author Decisions

## Context and Problem Statement

`docs/proposals/` is the repository owner for advisory triage documents. Proposal findings are later referenced from ADRs, plans, reviews, and handoffs, so their identifiers and tracker decisions need to be clear and stable.

Earlier proposals mixed variable-width finding IDs such as `E1` and `E10` with newer zero-padded IDs such as `E001`. One source proposal also pre-filled `decision: accepted`, making it unclear whether acceptance came from the maintainer or from the proposal author.

`PROP-01-proposal-governance` consolidates these issues and asks for proposal finding IDs, tracker decisions, proposal documentation, and validation to be normalized.

## Decision Drivers

* Keep proposal finding references stable, sortable, and easy to scan.
* Preserve the distinction between author recommendations and maintainer triage decisions.
* Avoid retroactive churn in archived proposal history.
* Keep proposal rules, templates, and validation aligned.
* Keep proposal work advisory until an ADR, plan, task, or implementation step accepts it.

## Considered Options

* Use fixed-width proposal finding IDs and empty author decisions
* Keep variable-width finding IDs and rely on review discipline
* Allow authors to pre-fill recommended decisions

## Decision Outcome

Chosen option: "Use fixed-width proposal finding IDs and empty author decisions", because proposal findings need stable references and maintainer decisions must be distinguishable from author recommendations.

If accepted, new proposal findings must use three-digit zero-padded IDs:

* Errors: `E001`, `E002`, and so on.
* Duplications: `D001`, `D002`, and so on.
* Simplifications: `S001`, `S002`, and so on.

When an author creates a new proposal finding, the `decision` field in both the progress tracker row and the finding YAML block must start empty. Only maintainer triage may set `decision` to `accepted`, `rejected`, `deferred`, or another documented value.

Archived proposals may keep historical finding IDs such as `E1` and `S1`. Active consolidated proposals should use the fixed-width form. Existing proposal references in archived files, completed ADRs, plans, commits, and changelog entries do not need retroactive rewriting unless the file is materially updated for another reason.

After acceptance, update `docs/proposals/README.md`, `docs/proposals/PROPOSAL_TEMPLATE.md`, and `scripts/validate-docs.ps1` together so the written rules, starting template, and validation behavior match.

### Consequences

* Good, because proposal finding IDs sort lexicographically in the same order as their numeric sequence.
* Good, because reviewers can tell whether a finding has actually been triaged.
* Good, because future proposal tables and anchors become easier to scan.
* Bad, because existing proposal rules, template examples, and validation regexes need coordinated updates.
* Bad, because historical proposals will contain both legacy and current ID styles unless they are later touched.

### Confirmation

Compliance will be checked through documentation review and `scripts/validate-docs.ps1` after the proposal README, proposal template, and validation script are updated.

The validator should continue to preserve archived history while enforcing the current rules for active proposal files and templates.

## Pros and Cons of the Options

### Use fixed-width proposal finding IDs and empty author decisions

* Good, because `E001`, `E010`, and `E100` sort correctly as strings.
* Good, because new proposal rows can be scanned without special casing one-digit and two-digit IDs.
* Good, because empty author decisions preserve maintainer triage as a separate act.
* Neutral, because archived proposals can keep legacy IDs for history.
* Bad, because the proposal template, README rules, and validation script need to change together.

### Keep variable-width finding IDs and rely on review discipline

* Good, because it avoids changing the existing proposal rules.
* Good, because short IDs such as `E1` are concise in small proposals.
* Bad, because `E10` sorts before `E2` in string ordering.
* Bad, because table alignment and cross-references stay more fragile.

### Allow authors to pre-fill recommended decisions

* Good, because author recommendations can be represented directly in the tracker.
* Bad, because `decision: accepted` becomes ambiguous without reading conversation history.
* Bad, because proposal documents are advisory and should not look accepted before maintainer triage.
* Bad, because it weakens the repository's ADR and plan gates.

## More Information

- Source proposal: `docs/proposals/archive/PROP-01-proposal-governance-2026-05-15T09-57.md`.
- Prior proposal rules: ADR 0033 and ADR 0034.
- Follow-up implementation, after this ADR is accepted: update the proposal README, proposal template, and documentation validation script in one change.
