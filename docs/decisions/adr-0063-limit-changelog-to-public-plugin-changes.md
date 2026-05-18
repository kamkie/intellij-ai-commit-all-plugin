---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T14:18:58+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Repository maintainer
informed: Future maintainers and AI agents
---

# Limit Changelog To Public Plugin-Facing Changes

## Context and Problem Statement

`CHANGELOG.md` was collecting internal repository activity alongside plugin behavior, including AI-agent documentation, workflow governance, and scenario-coverage tracking. That makes release notes less useful for plugin users and release operators.

The changelog needs a clear inclusion rule: what changes are public enough to record, and what internal repository work belongs elsewhere.

## Decision Drivers

* Keep release notes focused on what plugin users, public documentation readers, and release operators need.
* Preserve visibility for plugin source changes, public documentation, support, compatibility, and release pipeline changes.
* Avoid treating AI-agent workflow, plans, proposals, ADR maintenance, scenario coverage, and test-case inventory as plugin release notes.
* Keep internal evidence in the owning repository artifacts instead of duplicating it in `CHANGELOG.md`.

## Considered Options

* Record all notable repository changes.
* Limit `CHANGELOG.md` to public plugin-facing changes.
* Record only runtime plugin behavior changes.

## Decision Outcome

Chosen option: "Limit `CHANGELOG.md` to public plugin-facing changes", because it keeps the changelog useful as public release notes while still allowing entries for public docs, compatibility, support, and plugin-affecting CI or release pipeline changes.

Public plugin-facing changes include:

* Plugin source or runtime behavior.
* Public plugin documentation such as `README.md`, `SUPPORT.md`, Marketplace text, and release notes.
* Compatibility, support, security, or privacy behavior.
* CI, signing, publishing, or release workflow changes that affect the plugin artifact or publication.

Omit internal repository activity unless it also changes public plugin behavior, public docs, support promises, or release artifacts. Omitted internal activity includes:

* AI-agent documentation and `.agents/` skills or references.
* Plans, proposals, ADR maintenance, and internal workflow rules.
* Scenario-coverage registers, test-case inventories, manual validation logs, and test-only changes.

### Consequences

* Good, because `CHANGELOG.md` stays readable as public release history.
* Good, because internal process changes remain traceable in ADRs, plans, proposals, task records, or agent guidance without becoming release-note noise.
* Bad, because internal repository changes that affect future maintainers are less visible from the changelog alone.

### Confirmation

Confirm compliance by reviewing `CHANGELOG.md` for public plugin-facing scope and checking `.agents/references/releases.md`, `.agents/references/documentation.md`, and related execution guidance for the same inclusion rule.

## Pros and Cons of the Options

### Record all notable repository changes

* Good, because the changelog becomes a broad repository activity log.
* Bad, because plugin users and release operators must sift through internal AI workflow, planning, and validation inventory entries.
* Bad, because internal records already have more precise owners.

### Limit `CHANGELOG.md` to public plugin-facing changes

* Good, because it matches public release-note expectations.
* Good, because it still records public docs, support, compatibility, and plugin-affecting release pipeline changes.
* Good, because it gives agents a concrete include and exclude rule.
* Bad, because maintainers need to look at ADRs, plans, proposals, or scenario-coverage docs for internal workflow history.

### Record only runtime plugin behavior changes

* Good, because the changelog would be very small.
* Bad, because public documentation, support, compatibility, and release pipeline changes can materially affect plugin users and release operators.

## More Information

This ADR narrows the changelog inclusion scope in ADR 0029, ADR 0030, and ADR 0059. Those ADRs still govern changelog existence, ownership, and handoff cadence, but this ADR controls which changes belong in `CHANGELOG.md`.
