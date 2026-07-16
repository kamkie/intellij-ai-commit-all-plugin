# Validation Report: IntelliJ Platform 2026.2 Upgrade

- Date: 2026-07-16
- Source ref: `codex/intellij-2026-2-upgrade`
- Source SHA: `c25e8f3a4cfee95d0d6b739f7c21dd4f2518b7d2`
- Operating system: Windows
- Java runtime: Zulu OpenJDK `25.0.3+9-LTS`
- Artifact: Not built. A required generated-metadata check failed before the Gradle gates.
- Release-readiness conclusion: Blocked by a non-PyCharm validation failure. T4 is incomplete and no readiness claim is made.

## Product Metadata

JetBrains' [official release feed](https://data.services.jetbrains.com/products/releases?code=IIU,PCP,WS&latest=true&type=release) returned the following stable releases on 2026-07-16:

| Product       | Feed code | Latest stable version | Build          | Date       | T4 state   |
|---------------|-----------|-----------------------|----------------|------------|------------|
| IntelliJ IDEA | `IIU`     | 2026.2                | `262.8665.258` | 2026-07-16 | Available  |
| PyCharm       | `PCP`     | 2026.1.4              | `261.26222.68` | 2026-07-03 | Unavailable at 2026.2 |
| WebStorm      | `WS`      | 2026.2                | `262.8665.259` | 2026-07-16 | Available  |

The [IntelliJ IDEA 2026.2 announcement](https://blog.jetbrains.com/idea/2026/07/intellij-idea-2026-2/) and [WebStorm 2026.2 announcement](https://blog.jetbrains.com/webstorm/2026/07/webstorm-2026-2/) independently confirm both releases. The current [PyCharm What's New page](https://www.jetbrains.com/pycharm/whatsnew/) still identifies 2026.1 as its latest release line.

## Automated Gates

The local prerelease command was started through the managed-jobs controller:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/run-local-prerelease-validation.ps1 -PluginVerifierIdeVersions IU-2026.2,WS-2026.2
```

- Job: `20260716-221032-intellij-2026-2-t4-available-prerelease-912c5d`
- Log: `C:\Users\kamki\.agent-customizations\managed-jobs\logs\20260716-221032-intellij-2026-2-t4-available-prerelease-912c5d.log`
- Structured status: `build/reports/local-prerelease-validation/status.json`
- Result: Failed in 1.9 seconds with exit code 1 at the first required gate.

| Gate | Result | Evidence |
|------|--------|----------|
| Marketplace change notes | Failed | `scripts/generate-intellij-platform-change-notes.ps1 -Check` reported `config/intellij-platform/change-notes.html is stale. Run scripts/generate-intellij-platform-change-notes.ps1.` |
| Marketplace description | Not run | Stopped after the first non-PyCharm failure as required by T4. |
| Documentation and agent-artifact checks | Not run | Stopped after the first non-PyCharm failure. |
| Formatting, Detekt baseline, and Detekt | Not run | Stopped before Gradle gates. |
| Unit tests and JaCoCo coverage gates | Not run | Stopped before Gradle gates. |
| Plugin structure, project configuration, and packaging | Not run | Stopped before Gradle gates. |
| Plugin Verifier for `IU-2026.2` and `WS-2026.2` | Not run | Stopped before verifier lanes. |
| Release-matrix UI for IDEA and WebStorm 2026.2 | Not run | T4 requires escalation on the earlier non-PyCharm failure. |
| Explicit `PY-2026.2` resolution | Not run | T4 stopped before this lane; product metadata still proves that no 2026.2 stable build is published. This is not recorded as a pass. |

## Review Findings

### High: Generated Marketplace change notes were not refreshed

`CHANGELOG.md` adds the public IntelliJ 2026.2 compatibility entry, but its generated Marketplace counterpart in `config/intellij-platform/change-notes.html` was not updated. The repository's required generator check fails, blocking validation and leaving Marketplace release notes inconsistent with the changelog.

Smallest remediation: regenerate `config/intellij-platform/change-notes.html` with `scripts/generate-intellij-platform-change-notes.ps1`, then rerun T4 from the same current head after committing the scoped fix.

No additional confirmed findings were found in the `origin/main..c25e8f3` branch diff. The compatibility-sensitive Git staging reflection boundary and available-product runtime behavior remain unproven because validation stopped before packaging, Plugin Verifier, and release-matrix UI execution.

## Manual Checklist

Manual staging and real JetBrains AI Assistant smoke checks were not run. The non-PyCharm automated failure stopped T4 first; additionally, real AI Assistant validation requires an interactive IDE session and a signed-in user account that this worker did not use. No Git fixture or remote was mutated.

## Readiness

T4 failed and must not be committed as completed. The pull request remains draft. After the generated change notes are repaired in a separately dispatched remediation packet, rerun the complete T4 sequence on the new exact head. PyCharm 2026.2 must remain required and may block only through explicit product-unavailable resolution until JetBrains publishes it.
