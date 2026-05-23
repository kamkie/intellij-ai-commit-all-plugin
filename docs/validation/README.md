# Validation

This directory owns validation evidence and validation-maintenance documents. It
records how behavior is checked; it does not explain product usage.

## Documents

| File                                                 | Owner                                                                                               |
|------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| [Scenario Register](scenario-register.md)            | Stable scenario refs, scenario counts, execution mode, status, and evidence targets.                |
| [Release Validation Checklist](release-checklist.md) | Reusable release-readiness gates, required inputs, manual procedures, and evidence recording shape. |
| `reports/`                                           | Dated local, release-candidate, and release validation evidence reports.                            |

## Update Rules

- Add or update scenario rows in the scenario register when behavior needs a
  stable validation owner.
- Add reusable manual execution steps to the release checklist.
- Add dated artifacts, IDE builds, command summaries, skipped checks, and
  conclusions to reports.
- Keep user setup, workflow explanation, troubleshooting, and support policy in
  their owning docs instead of duplicating them here.
