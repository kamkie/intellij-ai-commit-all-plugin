# Manual Sandbox Validation

Last updated: 2026-05-20

This file records the manual sandbox coverage retained for scenarios that are not reliable to automate with the current Gradle, IntelliJ test framework, and CI setup.

## IDE Matrix

Representative IDE builds for manual validation, queried from JetBrains product release data on 2026-05-20 and confirmed in local installations:

- IntelliJ IDEA 2026.1.2, build 261.24374.151, release data product code `IIU`.
- PyCharm 2026.1.2, build 261.24374.152, release data product code `PCP`.
- WebStorm 2026.1.2, build 261.24374.125, product code `WS`.

Source queries:

- `https://data.services.jetbrains.com/products?code=IU,PY,WS`
- `https://data.services.jetbrains.com/products/releases?code=IU,PY,WS&latest=true&type=release`

## Current Release Matrix Status

Artifact prepared for the current manual validation cycle:
`build/distributions/ai-commit-all-v0.1.0-alpha.9-43-gc2fc8e0716.zip`, produced by
`.\gradlew.bat buildPlugin` on 2026-05-20.

Manual scenario execution is still pending. The scenario rows below require live IDE observation, current plugin installation state, AI Assistant signed-in and unavailable states, and local Git fixture interaction before Marketplace release readiness can be claimed.

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

`src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllActionsTest.kt` covers:

- `AI`, `Commit`, and `Push` section routing.
- `Push` availability when outgoing commits exist and there are no committable changes.
- Cumulative section highlighting.
- Disabled and running section state.
- Divider shade selection for active-active, active-passive, and passive-passive section boundaries.
- Nonblank custom rendering of the segmented control.

`src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllShortcutActionsTest.kt` and
`src/test/kotlin/pl/devopssolutions/aicommitall/actions/PluginActionRegistrationTest.kt` cover:

- Registration of shortcut-target actions that mirror `CheckinProject` and `Vcs.Push`.
- Registration of the Commit toolbar startup activity that removes the standard `Commit and Push...` toolbar action.
- Routing the commit shortcut action to the `Commit` workflow mode.
- Routing the push shortcut action to the `Push` workflow mode.
- Disabling takeover when the settings opt-out is off.
- Promoting plugin shortcut actions over the mirrored IDE actions only when takeover is available.

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
| T-VAL-012 | `Commit` section commits after AI message generation. | `IIU`. | Not run in this automated task. | Generated message, resulting commit hash, no push attempted. |
| T-VAL-013 | `Push` section pushes after a successful commit to a local remote. | `IIU`. | Not run in this automated task. | Local remote path, resulting commit hash, remote branch hash. |
| T-VAL-014 | Three-section control renders passive, disabled, cumulative hover, running states, and inactive divider shades in light and dark themes. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Theme names and screenshot or visual confirmation, including light `Clicked: Staging + AI` `Commit`/`Push` divider shade. |
| T-VAL-015 | Missing or disabled JetBrains AI Assistant dependency fails installation or loading. | `IIU`. | Not run in this automated task. | Plugin manager or IDE log evidence. |
| T-VAL-016 | AI Assistant present but unavailable or not signed in stops without commit or push. | `IIU`. | Not run in this automated task. | AI Assistant state, notification/error shown, git log unchanged. |
| T-VAL-017 | Git staging area enabled and disabled both preserve intended inclusion. | `IIU`. | Not run in this automated task. | Staging setting state and inclusion result. |
| T-VAL-018 | Current stable IDE builds are represented. | `IIU`, `PCP`, `WS`. | Product/build matrix recorded above. | Product name and build number. |
| T-VAL-022 | Non-automated E2E scenarios stay on the manual checklist. | `IIU`, plus `PCP` and `WS` where practical. | Checklist retained here. | Completed rows or linked release report. |
| T-VAL-023 | `AI` section includes eligible files, generates a message, and stops without commit or push. | `IIU`. | Not run in this automated task. | Generated message, commit log unchanged, remote branch unchanged. |
| T-IDEA-010 | `Push` is enabled and pushes immediately when only safe outgoing commits are present. | `IIU`. | Not run in this automated task. | Local and remote branch hashes before and after push, plus Commit tool window enabled state. |
| T-BUG-015 | `Push` does not open the Push window for protected tracked branches when no force push is required. | `IIU`. | Not run in this automated task. | Protected branch setting, local remote path, resulting commit and remote branch hashes, and no Push window observation. |
| T-IDEA-011 | The plugin control replaces the standard `Commit and Push...` toolbar action in the Commit tool window. | `IIU`, plus representative non-IDEA IDE. | Not run in this automated task. | Commit toolbar screenshot or observation showing plugin control visible and standard action absent. |
| ADR-0054-1 | With shortcut takeover enabled, the IDE commit shortcut runs the `Commit` section workflow. | `IIU`, plus macOS keymap equivalent where practical. | Not run in this automated task. | Keymap name, setting value, generated message, resulting commit hash. |
| ADR-0054-2 | With shortcut takeover enabled, the IDE push shortcut runs the `Push` section workflow. | `IIU`, plus macOS keymap equivalent where practical. | Not run in this automated task. | Keymap name, setting value, local remote path, resulting commit and remote branch hashes. |
| ADR-0054-3 | With shortcut takeover disabled, the IDE commit shortcut runs the standard IDE commit action. | `IIU`, plus macOS keymap equivalent where practical. | Not run in this automated task. | Keymap name, setting value, observed standard Commit action behavior. |
| ADR-0054-4 | With shortcut takeover disabled, the IDE push shortcut runs the standard IDE push action. | `IIU`, plus macOS keymap equivalent where practical. | Not run in this automated task. | Keymap name, setting value, observed standard Push action behavior. |

## Scenario Coverage Register

Project-wide scenario counts, automated coverage, manual coverage, and test cases are tracked in [Scenario Coverage](../scenario-coverage.md).

## Run Command

Use the Gradle sandbox as the default implementation sandbox entry point:

```powershell
.\gradlew.bat runIde
```

For release-matrix validation, install the packaged plugin artifact into the current locally installed products named above. `runIde` covers the configured Gradle IntelliJ Platform product and does not exercise the full IDEA, PyCharm, and WebStorm matrix.

Use temporary local Git repositories and local bare remotes for commit-and-push checks. Do not use a real remote while validating this workflow.
