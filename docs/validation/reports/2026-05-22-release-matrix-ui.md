# Validation Report: 2026-05-22 Release Matrix UI Automation

## Summary

This report preserves the local release-matrix UI automation evidence that was
previously stored in the reusable manual sandbox checklist. It is not a full
manual release validation pass.

## Inputs

- Date: 2026-05-22.
- Artifact:
  `build/distributions/ai-commit-all-v0.1.0-alpha.9-43-gc2fc8e0716.zip`.
- Artifact build command: `.\gradlew.bat buildPlugin`.
- Artifact build date recorded by earlier validation notes: 2026-05-20.

## IDE Matrix

Representative IDE builds were queried from JetBrains product release data on
2026-05-20 and confirmed in local installations:

| Product                | Code  | Build           |
|------------------------|-------|-----------------|
| IntelliJ IDEA 2026.1.2 | `IIU` | `261.24374.151` |
| PyCharm 2026.1.2       | `PCP` | `261.24374.152` |
| WebStorm 2026.1.2      | `WS`  | `261.24374.125` |

Source queries:

- `https://data.services.jetbrains.com/products?code=IU,PY,WS`
- `https://data.services.jetbrains.com/products/releases?code=IU,PY,WS&latest=true&type=release`

## Automated Gates

IDEA release-matrix UI automation used the deterministic local-fixture lane with
a test-only AI Assistant substitute:

```powershell
.\gradlew.bat releaseMatrixUiTest "-PideProducts=IU" "-PideVersion=2026.1.2"
```

Result: 19 passing tests in `ReleaseMatrixUiHarnessTest`.

Covered assertions included:

- Commit tool window launch, control visibility, toolbar replacement,
  accessibility state, and nonblank light/dark screenshots.
- `AI`, `Commit`, `Push`, staging-area enabled and disabled, shortcut takeover,
  local commit, safe local push, and outgoing-only push flows.
- Missing AI dependency, missing AI action, unavailable completion signal, AI
  timeout, empty message, unchanged message, and user-edited message stop paths,
  with unchanged local and remote Git state where applicable.

## Evidence Paths

- Screenshots: `build/reports/releaseMatrixUiTest/screenshots/`.
- Git evidence: `build/reports/releaseMatrixUiTest/git-evidence/`.

## Manual Coverage Retained

Manual scenario execution remains required for:

- PyCharm and WebStorm product coverage.
- Real signed-in JetBrains AI Assistant behavior.
- Platform-owned AI Assistant signed-out messages.
- Before-commit and push error UI.
- Resolved-conflict marking.
- Settings dialog rendering.
- Cases where a deterministic fake action does not own the primary assertion.

## Skipped Checks

- Full manual release checklist: not completed in this report.
- Real JetBrains AI Assistant signed-in and signed-out checks: retained for a
  later manual validation pass.
- PyCharm and WebStorm manual product observations: retained for a later manual
  validation pass.

## Release-Readiness Conclusion

IDEA deterministic release-matrix UI automation passed locally for the recorded
artifact and IDE build. This report does not by itself establish release
readiness because retained manual checks were not completed.
