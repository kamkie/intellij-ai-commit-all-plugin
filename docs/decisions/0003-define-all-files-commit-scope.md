# Define All Files Commit Scope

Status: Accepted

Date: 2026-05-14

## Context

The plugin goal uses the phrase `all files`, but the exact commit scope was previously unresolved.

The user clarified that `all files` means unversioned files and changed, removed, or otherwise committable files, as long as they are not ignored.

## Decision

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

## Consequences

- The implementation must include non-ignored unversioned files automatically.
- The implementation must include all changelists in the supported project and VCS scope.
- The action must not commit ignored files.
- The implementation needs explicit validation for modified, deleted, moved or renamed, and non-ignored unversioned files.
- The first implementation supports Git only, including multiple Git roots. Within any supported Git root, all non-ignored committable file changes are in scope.

## Alternatives Considered

- Include only tracked changes.
  - Why it was not chosen: the requested behavior explicitly includes unversioned files.
- Include only files currently selected in the Commit tool window.
  - Why it was not chosen: the requested behavior is an `all files` action, not a commit-current-selection action.
- Parse ignore rules manually.
  - Why it was not chosen: IntelliJ and VCS APIs already expose ignored and committable state, and manual parsing would be more fragile.

## Follow-Up

- Update `README.md`, `TASKS.md`, and `OPEN_QUESTIONS.md` to reflect the accepted scope.
- Validate this scope in sandbox testing after the plugin scaffold exists.
