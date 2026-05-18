---
status: accepted
date: 2026-05-15
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use Git Identity For ADR Decision Makers

## Context and Problem Statement

ADR files now use MADR 4.0.0 front matter, including the `decision-makers` field.

The migration initially used the generic value `Maintainer`, which is less precise than the repository's configured Git identity.

The maintainer requested that `decision-makers` use the Git username and email.

## Decision Drivers

* Keep ADR metadata attributable to the same identity used for repository commits.
* Avoid ambiguous generic decision-maker labels.
* Keep the metadata format easy to validate locally.

## Considered Options

* Use Git username and email in `decision-makers`
* Keep `Maintainer`
* Use only the Git username

## Decision Outcome

Chosen option: "Use Git username and email in `decision-makers`", because it records the concrete decision-maker identity instead of a generic role.

All ADR files must set `decision-makers` to the configured Git identity in `Name <email>` form.

For the current repository, the value is `Kamil Kiewisz <kamkie@outlook.com>`.

### Consequences

* Good, because ADR metadata is attributable to a concrete Git identity.
* Good, because validation can reject generic or incomplete values.
* Bad, because changing the repository decision-maker requires updating ADR metadata intentionally.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when ADR files change.

## Pros and Cons of the Options

### Use Git username and email in `decision-makers`

* Good, because it uses the same concrete identity as local Git configuration.
* Good, because it includes both human-readable name and contact email.
* Bad, because it is more specific than a role label.

### Keep `Maintainer`

* Good, because it is short and stable.
* Bad, because it does not identify who made the decision.

### Use only the Git username

* Good, because it avoids exposing an email address.
* Bad, because the maintainer explicitly requested username and email.

## More Information

- Update existing ADR front matter.
- Extend `scripts/validate-docs.ps1` to require `Name <email>` in `decision-makers`.
