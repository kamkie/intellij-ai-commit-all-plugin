# Manual Sandbox Validation

Last updated: 2026-05-15

This file records the manual sandbox coverage retained for scenarios that are not reliable to automate with the current Gradle, IntelliJ test framework, and CI setup.

## IDE Matrix

Current stable representative IDE builds for manual validation, queried from JetBrains product release data on 2026-05-15:

- IntelliJ IDEA 2026.1.1, build 261.23567.138, product code `IIU`.
- PyCharm 2026.1.1, build 261.23567.174, product code `PCP`.
- WebStorm 2026.1.1, build 261.23567.141, product code `WS`.

Source queries:

- `https://data.services.jetbrains.com/products?code=IU,PY,WS`
- `https://data.services.jetbrains.com/products/releases?code=IU,PY,WS&latest=true&type=release`

## Automated Coverage Added

`src/test/kotlin/pl/devopssolutions/aicommitall/validation/LocalGitRepositoryValidationTest.kt` covers temporary local Git repositories for:

- Modified tracked files.
- Staged added files.
- Deleted files.
- Moved or renamed files.
- Unversioned files.
- Ignored file exclusion.
- Independent multiple Git roots.
- Commit-only and commit-and-push through a temporary local bare remote.

The push scenario uses only repositories under the test temporary directory and does not touch a real remote.

## Manual Sandbox Scenarios

Record manual results in this file or in a linked release validation report before making release-readiness claims.

| ID | Scenario | IDE coverage | Status | Evidence to record |
|----|----------|--------------|--------|--------------------|
| T-VAL-003 | Open a Git project in the sandbox IDE and confirm the Commit tool window is available. | `IIU`, plus `PCP` and `WS` where practical. | Not run in this automated task. | Product name, build, project fixture path, result. |
| T-VAL-005 | Modified tracked file is included by `AI Commit All`. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Commit tool window before/after inclusion state. |
| T-VAL-006 | Unversioned file is included. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Commit tool window before/after inclusion state. |
| T-VAL-007 | Deleted file is included. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Commit tool window before/after inclusion state. |
| T-VAL-008 | Moved or renamed file is included. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Commit tool window before/after inclusion state. |
| T-VAL-009 | Files in multiple changelists are included. | `IIU`. | Not run in this automated task. | Changelist names and inclusion state. |
| T-VAL-010 | Files across multiple Git roots are included. | `IIU`. | Not run in this automated task. | Root paths and inclusion state. |
| T-VAL-011 | Ignored files are excluded. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | `.gitignore` content and absence from inclusion state. |
| T-VAL-012 | Commit-only flow commits after AI message generation. | `IIU`. | Not run in this automated task. | Generated message, resulting commit hash, no push attempted. |
| T-VAL-013 | Commit-and-push flow pushes after a successful commit to a local remote. | `IIU`. | Not run in this automated task. | Local remote path, resulting commit hash, remote branch hash. |
| T-VAL-014 | Icons render in light and dark themes. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Theme names and screenshot or visual confirmation. |
| T-VAL-015 | Missing or disabled JetBrains AI Assistant dependency fails installation or loading. | `IIU`. | Not run in this automated task. | Plugin manager or IDE log evidence. |
| T-VAL-016 | AI Assistant present but unavailable or not signed in stops without commit or push. | `IIU`. | Not run in this automated task. | AI Assistant state, notification/error shown, git log unchanged. |
| T-VAL-017 | Git staging area enabled and disabled both preserve intended inclusion. | `IIU`. | Not run in this automated task. | Staging setting state and inclusion result. |
| T-VAL-018 | Current stable IDE builds are represented. | `IIU`, `PCP`, `WS`. | Product/build matrix recorded above. | Product name and build number. |
| T-VAL-022 | Non-automated E2E scenarios stay on the manual checklist. | `IIU`, plus `PCP` and `WS` where practical. | Checklist retained here. | Completed rows or linked release report. |

## Run Command

Use the Gradle sandbox as the default manual validation entry point:

```powershell
.\gradlew.bat runIde
```

Use temporary local Git repositories and local bare remotes for commit-and-push checks. Do not use a real remote while validating this workflow.
