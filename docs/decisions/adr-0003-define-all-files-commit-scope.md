---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Define All Files Commit Scope

## Context and Problem Statement

The plugin goal uses the phrase `all files`, but the exact commit scope was previously unresolved.

The user clarified that `all files` means unversioned files and changed, removed, or otherwise committable files, as long as they are not ignored.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Define All Files Commit Scope
* Include only tracked changes.
* Include only files currently selected in the Commit tool window.
* Parse ignore rules manually.

## Decision Outcome

Chosen option: "Adopt Define All Files Commit Scope", because `All files` means every non-ignored file change that the IDE commit workflow can commit for the selected project and supported VCS scope.

`All files` means every non-ignored file change that the IDE commit workflow can commit for the selected project and supported VCS scope.

This includes, when supported by the target VCS and IntelliJ commit workflow:

- Modified tracked files.
- Added, moved, or renamed files.
- Deleted or removed files.
- Other tracked change types exposed as committable changes.
- Resolved conflict paths when IntelliJ exposes them as committable.
- Non-ignored unversioned files.

Ignored files must not be included.

The first implementation should use IntelliJ Platform VCS and commit workflow APIs to determine this set instead of shelling out to Git or manually parsing ignore files.

### Consequences

- The implementation must include non-ignored unversioned files automatically.
- The implementation must include all changelists in the supported project and VCS scope.
- The action must not commit ignored files.
- The implementation needs explicit validation for modified, deleted, moved or renamed, and non-ignored unversioned files.
- The first implementation supports Git only, including multiple Git roots. Within any supported Git root, all non-ignored committable file changes are in scope.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Define All Files Commit Scope

* Good, because `All files` means every non-ignored file change that the IDE commit workflow can commit for the selected project and supported VCS scope.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Include only tracked changes.

* Bad, because the requested behavior explicitly includes unversioned files.

### Include only files currently selected in the Commit tool window.

* Bad, because the requested behavior is an `all files` action, not a commit-current-selection action.

### Parse ignore rules manually.

* Bad, because IntelliJ and VCS APIs already expose ignored and committable state, and manual parsing would be more fragile.

## More Information

- Update `README.md`, `TASKS.md`, and `docs/decisions/OPEN_QUESTIONS.md` to reflect the accepted scope.
- Validate this scope in sandbox testing after the plugin scaffold exists.
