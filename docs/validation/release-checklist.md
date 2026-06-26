# Release Validation Checklist

Last updated: 2026-06-26

This reusable checklist owns manual release-readiness validation for AI Commit
All. Use it when preparing a release candidate, validating a release-matrix
change, or recording maintainer evidence for scenarios that are not reliable to
automate.

This file does not own stable scenario IDs or dated results. Stable scenario
ownership lives in [Scenario Register](scenario-register.md). Dated local,
release-candidate, and release validation results live under
`docs/validation/reports/`.

Before making release-readiness claims, record the actual artifact, IDE builds,
commands, manual observations, skipped checks, and conclusion in a dated report.

## Required Inputs

Create or update a dated report before running the manual checklist. Record:

- Plugin artifact path and how it was built.
- Source ref, release tag, or commit SHA.
- Operating system and Java runtime if relevant.
- IDE product names, product codes, build numbers, and install locations.
- JetBrains AI Assistant state: installed, enabled, signed in, signed out, or
  intentionally disabled.
- Git fixture path, repository layout, branch and upstream state, and whether
  Git staging area is enabled.
- Temporary local bare remote path for push validation.
- Evidence paths for screenshots, IDE logs, Git command output, and generated
  Gradle reports.
- Marketplace media filenames, upload status, and web or IDE rendering
  observations when validating publication readiness.
- Skipped checks with reasons.

## Product Matrix

Use IntelliJ IDEA first, then repeat representative happy-path and failure-path
coverage in PyCharm and WebStorm where practical. The deterministic
release-matrix UI automation uses the Gradle and Starter product aliases below:
`IU` runs the full harness, while `PY` and `WS` run the smoke subset.

| Product       | Code  | Checklist role                                                              |
|---------------|-------|-----------------------------------------------------------------------------|
| IntelliJ IDEA | `IU`  | Primary full manual pass and deterministic full automation lane.            |
| PyCharm       | `PY`  | Deterministic smoke automation plus representative manual coverage.         |
| WebStorm      | `WS`  | Deterministic smoke automation plus representative manual coverage.         |

When refreshing current builds, query JetBrains product release data and record
the exact product versions in the dated report:

- `https://data.services.jetbrains.com/products?code=IU,PY,WS`
- `https://data.services.jetbrains.com/products/releases?code=IU,PY,WS&latest=true&type=release`

## Automated Gates

Record the relevant automated gates in the dated report. At minimum for release
preparation, run the local prerelease validation script:

```powershell
.\scripts\run-local-prerelease-validation.ps1
```

The script runs documentation checks, agent-artifact validation, formatting,
Detekt, tests, coverage verification, plugin structure validation, and plugin
packaging once. It then runs Plugin Verifier separately for IntelliJ IDEA,
PyCharm, and WebStorm. Keep those verifier invocations split during local
prerelease preparation so a verifier worker failure in one IDE does not force a
full build/test/package rerun. The script preserves per-IDE verifier evidence
under `build/reports/pluginVerifier-local-prerelease/`.

After pushing `main` and the release tag, use the GitHub release-validation
watcher instead of manually polling each workflow:

```powershell
.\scripts\watch-github-release-validation.ps1 -Tag <tag>
```

The watcher waits for the release commit's main push workflows, tag-triggered
GitHub Release workflow, and tag-triggered Release Matrix UI workflow. It also
checks that the GitHub Release exists, is marked as a prerelease for prerelease
tags, and has exactly one ZIP asset. By default it reruns failed Release Matrix
UI jobs once; pass `-ReleaseMatrixReruns 0` when recording the first failure
without an automatic rerun.

For release matrix work, also include:

```powershell
.\gradlew.bat buildPlugin
.\gradlew.bat releaseMatrixUiTest "-PideProducts=IU" "-PideVersion=<version>"
.\gradlew.bat releaseMatrixUiTest "-PideProducts=PY" "-PideVersion=<version>"
.\gradlew.bat releaseMatrixUiTest "-PideProducts=WS" "-PideVersion=<version>"
```

The PyCharm and WebStorm lanes use the fake AI Assistant substitute plugin and
temporary local Git fixtures. They do not replace manual checks that require the
real JetBrains AI Assistant state, product-specific visual review, platform
warnings, or manually observed push behavior.

Use the broader repository validation required by the release task, plan, or
release workflow. Do not duplicate automated test inventories here; the scenario
register points to the primary automated evidence targets.

## Manual Gates

| Gate                                             | Refs                                                                                        | Products                                            | Evidence to record                                                                                                                            |
|--------------------------------------------------|---------------------------------------------------------------------------------------------|-----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| Commit tool window startup and control placement | `T-VAL-003`, `T-IDEA-011`                                                                   | `IU`, plus `PY` and `WS` where practical            | Product/build, fixture path, screenshot or observation showing plugin control visible and standard `Commit and Push...` absent.               |
| Eligible file inclusion and staging behavior     | `T-VAL-005..011`, `T-VAL-017`, `SCN-STAGE-MAN-001..008`, `SCN-SELECT-MAN-001..006`          | `IU`, plus representative non-IDEA IDE              | Commit tool window inclusion state, staging state, changelist/root names, and `git status --porcelain` output.                                |
| Real JetBrains AI Assistant behavior             | `T-VAL-016`, `T-VAL-023`, `SCN-AI-MAN-004`, `SCN-AI-MAN-006`                                | `IU`, plus `PY` and `WS` where practical            | AI Assistant state, generated message, timeout or unavailable UI, and unchanged Git state for stops.                                          |
| Commit and push execution                        | `T-VAL-012`, `T-VAL-013`, `T-BUG-015`, `SCN-WORKFLOW-MAN-004..006`, `SCN-PUSH-MAN-002..006` | `IU` first                                          | Resulting commit hash, local and remote branch hashes, fallback or push-error UI, safe-push completion timing, and no real remote contact.     |
| Visual control review                            | `T-VAL-014`                                                                                 | `IU`, plus representative non-IDEA IDE              | Light and dark screenshots or observations for passive, hover, clicked, running, disabled, and divider states.                                |
| Marketplace media and listing rendering          | `PROP-marketplace-realtime-progress-media`                                                  | Marketplace web listing and IDE Marketplace view    | Uploaded media filenames, web listing screenshot or observation, IDE Marketplace rendering observation or skip reason, and fallback behavior. |
| Shortcut and settings behavior                   | `ADR-0054-1..4`, `SCN-SHORTCUT-MAN-004`, `SCN-SETTINGS-MAN-001..002`                        | `IU`, plus macOS keymap equivalent where practical  | Keymap name, setting values, action result, generated message or commit/push evidence, and persistence after restart or reopen.               |
| Current IDE build representation                 | `T-VAL-018`                                                                                 | `IU`, `PY`, `WS`                                    | Product names, product codes, and build numbers.                                                                                              |
| Platform-owned safeguards                        | `SCN-STAGE-MAN-015..018`, `SCN-WORKFLOW-MAN-007..008`                                       | `IU` first                                          | Before-commit, commit-error, push-error, unsupported API, frozen VCS, or background VCS observations and unchanged Git state where expected.  |

## Manual Case Procedures

Use temporary local Git repositories and local bare remotes for commit-and-push
checks. Do not use a real remote while validating this workflow.

### SCN-STAGE Manual Cases

#### SCN-STAGE-MAN-001: AI Stages Every Supported File-State Path

- Preconditions: JetBrains AI Assistant is installed and signed in; Commit tool
  window uses `Staging area`; the temporary Git repo contains `modified.txt`,
  deleted `delete-me.txt`, `rename-source.txt -> rename-target.txt`,
  `unversioned.txt`, already staged `staged-added.txt`, unchanged
  `unchanged.txt`, and ignored `ignored.txt`.
- Steps: Open the Commit tool window; confirm `Staging area` is enabled; click
  `AI`; watch the staged file list until AI generation starts or the workflow
  stops; run `git status --porcelain --ignored`.
- Expected result: The staged file list never becomes empty after eligible files
  are present. Modified, deleted, renamed, unversioned, and already staged paths
  are staged or remain staged. Ignored and unchanged files are excluded. AI
  generation starts. No commit is created.

#### SCN-STAGE-MAN-002: Commit Stages And Commits Every Supported Path

- Preconditions: Same fixture as `SCN-STAGE-MAN-001`; commit message field is
  empty before the run.
- Steps: Open the Commit tool window; confirm `Staging area` is enabled; click
  `Commit`; wait for AI generation and commit flow to finish; run
  `git show --name-status --oneline HEAD`.
- Expected result: The staged file list never becomes empty before commit
  execution. The workflow does not stop after staging. One new commit is
  created. The commit contains modified, deleted, renamed, unversioned, and
  already staged paths. Ignored and unchanged files are absent.

#### SCN-STAGE-MAN-003: Push Commits And Pushes To A Temporary Local Remote

- Preconditions: Same fixture as `SCN-STAGE-MAN-001`; a temporary local bare
  remote is configured as the tracked upstream; local and upstream branch hashes
  match before the run.
- Steps: Open the Commit tool window; confirm `Staging area` is enabled; click
  `Push`; wait for AI generation, commit, and push to finish; compare local and
  remote branch hashes.
- Expected result: The staged file list never becomes empty before commit
  execution. One new commit is created and pushed to the local remote. No real
  remote is contacted.

#### SCN-STAGE-MAN-004: Already Staged Files Are Not Lost

- Preconditions: `already-staged.txt` is staged; `unstaged.txt` is modified but
  unstaged; `new-file.txt` is unversioned; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm `already-staged.txt` is already
  staged; click `AI`; watch the staged file list until AI generation starts or
  the workflow stops; run `git status --porcelain`.
- Expected result: The staged file list never becomes empty.
  `already-staged.txt`, `unstaged.txt`, and `new-file.txt` are staged. AI
  generation starts. No commit is created.

#### SCN-STAGE-MAN-005: All Intended Files Already Staged

- Preconditions: Modified, deleted, renamed, and unversioned fixture files are
  all staged before clicking the plugin control; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm every intended path is staged;
  click `AI`; watch the staged file list until AI generation starts or the
  workflow stops.
- Expected result: The plugin does not remove or temporarily hide the staged
  set. AI generation starts. No commit is created.

#### SCN-STAGE-MAN-006: No Files Staged Before Run

- Preconditions: Modified, deleted, renamed, and unversioned fixture files
  exist; none are staged; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm the staged list is empty and
  unstaged changes are visible; click `AI`; watch the staged file list until AI
  generation starts or the workflow stops.
- Expected result: Eligible paths move into the staged list without a stop after
  staging. The staged list does not become empty after files are staged. AI
  generation starts. No commit is created.

#### SCN-STAGE-MAN-007: Multiple Git Roots And Nested Paths

- Preconditions: Project has two Git roots; root A contains modified
  `modules/core/build.gradle.kts` and unversioned
  `products/idea/plugin/src/Main.kt`; root B contains staged
  `products/webstorm/plugin/src/Main.kt`; `Staging area` is enabled.
- Steps: Open the Commit tool window; click `AI`; watch the staged file list
  until AI generation starts or the workflow stops; run `git status --porcelain`
  in both roots.
- Expected result: Eligible files from both roots remain visible and staged.
  Nested module and product paths are preserved. AI generation starts. No commit
  is created.

#### SCN-STAGE-MAN-008: Resolved-Conflict Paths Stay Included

- Preconditions: A conflict was resolved by the user and marked resolved in the
  IDE; the resolved path is committable; `Staging area` is enabled.
- Steps: Open the Commit tool window; confirm the resolved-conflict path is
  visible; click `AI`; watch the staged file list until AI generation starts or
  the workflow stops.
- Expected result: The resolved-conflict path remains included and staged. AI
  generation starts. No unresolved-conflict path is committed.

#### SCN-STAGE-MAN-015: Before-Commit Check Or Commit Warning Failure

- Preconditions: Eligible files exist; `Staging area` is enabled; configure a
  before-commit check or warning that blocks commit completion.
- Steps: Click `Commit`; let AI generation complete; let the IDE commit
  workflow reach the blocking check or warning.
- Expected result: The IDE safeguard is not bypassed. No unintended commit or
  push is created. Staged files are not lost.

#### SCN-STAGE-MAN-016: Unsafe Or Ambiguous Push Target Fallback

- Preconditions: Eligible files exist; `Staging area` is enabled; push target is
  missing, unsafe, ambiguous, or not a matching tracked upstream.
- Steps: Click `Push`; let AI generation complete; observe push execution or
  fallback behavior; inspect staged files and local commits.
- Expected result: The workflow does not push to an unsafe target. If the IDE
  fallback is used, staged files remain visible and platform push safeguards
  stay active.

#### SCN-STAGE-MAN-018: Frozen Or Background-Running VCS Operation

- Preconditions: `Staging area` is enabled; a VCS freeze or background VCS
  operation is active before clicking the plugin control.
- Steps: Click `AI`; observe the workflow result and notifications; inspect
  `git status --porcelain`.
- Expected result: The workflow stops before staging mutation. Existing staged
  files remain unchanged.

### Additional Manual Cases

| ID                     | Preconditions                                                                                                                     | Steps                                                    | Expected result                                                                      |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|--------------------------------------------------------------------------------------|
| `SCN-SHORTCUT-MAN-004` | A keymap without expected shortcuts or with unusual bindings is active.                                                           | Inspect action behavior and trigger unrelated shortcuts. | Plugin actions do not intercept unrelated commands.                                  |
| `SCN-AI-MAN-004`       | Supported IDE products with potentially different AI action IDs or labels are available.                                          | Run `AI` in each product.                                | AI action discovery still invokes commit-message generation or fails closed.         |
| `SCN-AI-MAN-006`       | Clear-before-generation setting can be toggled.                                                                                   | Run `AI` once with clearing enabled and once disabled.   | Commit-message field is cleared or preserved according to the setting.               |
| `SCN-SELECT-MAN-001`   | Changelist-backed Commit workflow is active with modified, deleted, renamed, unversioned, resolved, ignored, and unchanged files. | Click `AI` and inspect included files.                   | Eligible files are included and ignored or unchanged files are excluded.             |
| `SCN-SELECT-MAN-002`   | Multiple changelists contain eligible Git changes.                                                                                | Click `AI` and inspect included files.                   | Files from all relevant changelists are included.                                    |
| `SCN-SELECT-MAN-003`   | Project has multiple Git roots with eligible changes.                                                                             | Click `AI` and inspect included files per root.          | Files from every supported root are included.                                        |
| `SCN-SELECT-MAN-004`   | Unsupported or non-Git project state is open.                                                                                     | Trigger plugin control or shortcut.                      | Workflow hides or stops without selection mutation.                                  |
| `SCN-SELECT-MAN-005`   | Changelists disabled and Git staging disabled state is available.                                                                 | Trigger plugin workflow with eligible changes.           | Workflow uses supported selection behavior or fails closed without data loss.        |
| `SCN-SELECT-MAN-006`   | Resolved and unresolved conflict examples are available.                                                                          | Mark one conflict resolved and trigger plugin workflow.  | Resolved paths can be included; unresolved conflicts remain guarded by IDE behavior. |
| `SCN-WORKFLOW-MAN-004` | A blocking before-commit check or warning is configured.                                                                          | Click `Commit` and let AI finish.                        | IDE safeguard blocks as usual and no unintended commit or push occurs.               |
| `SCN-WORKFLOW-MAN-005` | Commit operation can produce a platform commit error.                                                                             | Click `Commit`.                                          | Platform commit error is surfaced without plugin masking.                            |
| `SCN-WORKFLOW-MAN-006` | Push operation can produce a platform push error.                                                                                 | Click `Push`.                                            | Platform push error is surfaced or delegated without plugin masking.                 |
| `SCN-WORKFLOW-MAN-007` | Unsupported Commit workflow API shape is available in a supported IDE build.                                                      | Trigger plugin workflow.                                 | Reflection boundary fails closed without unintended mutation.                        |
| `SCN-WORKFLOW-MAN-008` | VCS freeze or background VCS operation is active.                                                                                 | Trigger plugin workflow.                                 | Workflow stops before mutation and reports the standard or plugin-owned message.     |
| `SCN-PUSH-MAN-002`     | Branch has no tracked upstream.                                                                                                   | Click `Push`.                                            | Workflow falls back to IDE commit-and-push behavior without data loss.               |
| `SCN-PUSH-MAN-003`     | Local branch and upstream have diverged.                                                                                          | Click `Push`.                                            | Immediate push is not used; platform fallback handles the state.                     |
| `SCN-PUSH-MAN-004`     | Push target is new, special, or otherwise ambiguous.                                                                              | Click `Push`.                                            | Immediate push is not used and platform behavior remains in charge.                  |
| `SCN-PUSH-MAN-005`     | Repository state is not normal.                                                                                                   | Click `Push`.                                            | Immediate push is not used.                                                          |
| `SCN-PUSH-MAN-006`     | Real push failure can be produced safely against a local remote.                                                                  | Click `Push`.                                            | Push failure is surfaced, the running indicator does not stop before the push result is reported, and branch hashes show no unintended remote update. |
| `SCN-SETTINGS-MAN-001` | Settings dialog is available.                                                                                                     | Open `Settings > Tools > AI Commit All`.                 | Defaults match documented and automated settings defaults.                           |
| `SCN-SETTINGS-MAN-002` | Settings can be changed and IDE restarted or project reopened.                                                                    | Change settings, restart or reopen, and inspect values.  | Settings persist.                                                                    |

### Marketplace Media Upload Check

- Preconditions: A Marketplace plugin page exists, the release operator has
  Marketplace admin access outside the repository, and accepted media files are
  present under `docs/assets/marketplace/`.
- Steps: Upload the accepted GIF and PNG media through the Marketplace media
  section, verify the public web listing, and verify the IDE Marketplace view
  when the plugin page is visible there.
- Expected result: Media renders without broken links. The web listing shows
  the workflow animation. If the IDE Marketplace view presents the GIF as a
  static frame, the static PNG fallback still communicates the `AI -> Commit ->
  Push` sequence. Do not record credentials or admin-only URLs in the report.

## Run Command

Use the Gradle sandbox as the default implementation sandbox entry point:

```powershell
.\gradlew.bat runIde
```

For release-matrix validation, install the packaged plugin artifact into the
current locally installed products. `runIde` covers the configured Gradle
IntelliJ Platform product and does not exercise the full IDEA, PyCharm, and
WebStorm matrix.

## Evidence Recording

Each dated report should include this minimum shape:

```markdown
# Validation Report: <version-or-cycle>

- Date:
- Source ref:
- Artifact:
- IDE matrix:
- Automated gates:
- Manual checklist:
- Evidence paths:
- Skipped checks:
- Release-readiness conclusion:
```

Link the report from the release task or release preparation notes when using
it as release-readiness evidence.

## Existing Reports

- [2026-06-26 v0.1.0-beta.8 Release Preparation](reports/2026-06-26-v0.1.0-beta.8.md)
- [2026-06-25 v0.1.0-beta.7 Release Preparation](reports/2026-06-25-v0.1.0-beta.7.md)
- [2026-06-24 v0.1.0-beta.6 Release Preparation](reports/2026-06-24-v0.1.0-beta.6.md)
- [2026-06-22 v0.1.0-beta.5 Release Preparation](reports/2026-06-22-v0.1.0-beta.5.md)
- [2026-06-12 v0.1.0-beta.4 Release Preparation](reports/2026-06-12-v0.1.0-beta.4.md)
- [2026-06-04 v0.1.0-beta.3 Release Preparation](reports/2026-06-04-v0.1.0-beta.3.md)
- [2026-05-24 v0.1.0-alpha.10 Release Preparation](reports/2026-05-24-v0.1.0-alpha.10.md)
- [2026-05-22 Release Matrix UI Automation](reports/2026-05-22-release-matrix-ui.md)
