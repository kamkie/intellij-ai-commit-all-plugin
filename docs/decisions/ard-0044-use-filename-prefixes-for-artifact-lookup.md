---
status: proposed
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Filename Prefixes For Artifact Lookup

## Context and Problem Statement

The repository stores durable decisions, implementation plans, and advisory proposals in separate directories. ADR 0038 and ADR 0039 already make their filename prefixes stable: ADRs use `ard-`, plans use `PLAN-`, and proposals use `PROP-`.

The maintainer requested an explicit instruction to recognize ADR, plan, and proposal artifacts by filename prefix and format so agents search the correct directory by default instead of starting with broad repository searches.

## Decision Drivers

* Keep task context small by searching the most likely artifact owner first.
* Use existing stable filename conventions instead of re-inferring artifact type from prose.
* Reduce accidental edits to the wrong artifact type.
* Preserve fallback behavior when a reference is incomplete or ambiguous.

## Considered Options

* Use filename prefixes for artifact lookup
* Keep broad repository search as the default
* Rely only on the guidance map

## Decision Outcome

Chosen option: "Use filename prefixes for artifact lookup", because artifact filename prefixes are stable, validated, and specific enough to route searches to the correct directory before falling back to broader search.

If accepted, AI-facing guidance should instruct agents to classify artifact references by filename prefix and format before searching:

* `ard-NNNN-<slug>.md` or `ard-NNNN` means search `docs/decisions/` first.
* `PLAN-<short-kebab-slug>.md` or `PLAN-<short-kebab-slug>` means search `.agents/plans/` first, including `.agents/plans/archive/` when the active file is not found.
* `PROP-<short-kebab-slug>-<YYYY-MM-DD>T<HH-MM>.md` or `PROP-<short-kebab-slug>` means search `docs/proposals/` first, including `docs/proposals/archive/` when the active file is not found.

Agents should prefer exact filename lookup when a full filename is supplied. When only an ID or prefix is supplied, agents should use a scoped search in the owning directory before falling back to repository-wide search.

When a reference does not match one of these prefixes, or when scoped lookup finds no match, normal repository search remains allowed.

### Consequences

* Good, because common references such as `ard-0043`, `PLAN-user-documentation`, and `PROP-02-pre-release-ux` go directly to the correct directory.
* Good, because agents can avoid loading unrelated ADRs, plans, proposals, or broad search results.
* Good, because the instruction reinforces existing validated filename conventions.
* Bad, because guidance files need another lookup rule and agents must remember the fallback path for ambiguous references.

### Confirmation

Compliance will be checked through documentation review when AI-facing guidance changes.

`scripts/validate-docs.ps1` already validates the relevant filename prefix conventions for ADRs, plans, and proposals.

## Pros and Cons of the Options

### Use filename prefixes for artifact lookup

* Good, because it turns existing filename conventions into an actionable search rule.
* Good, because it speeds up routine artifact lookup while preserving fallback search.
* Good, because it aligns with the repository rule to keep task context small.
* Bad, because guidance files need a small additional instruction.

### Keep broad repository search as the default

* Good, because it requires no guidance change.
* Bad, because broad search returns unrelated hits for common words such as `proposal`, `plan`, and `decision`.
* Bad, because it encourages loading more context than the task needs.

### Rely only on the guidance map

* Good, because `AGENTS.md` already maps ADRs, plans, and proposals to directories.
* Bad, because the guidance map does not explicitly say how to route an artifact reference by filename prefix.
* Bad, because agents may still perform a broad search before checking the known owner directory.

## More Information

- Related filename conventions: ADR 0038 and ADR 0039.
- Follow-up implementation, after this ADR is accepted: update `AGENTS.md` and the relevant AI-facing reference docs with the prefix lookup rule.
