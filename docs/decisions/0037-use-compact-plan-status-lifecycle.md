# Use Compact Plan Status Lifecycle

Status: Accepted

Date: 2026-05-15

## Context

Plan files need enough status detail to trace review, readiness, active implementation, blockers, completion, and closure. The previous plan lifecycle used `Draft`, `Accepted`, `Implemented`, and `Superseded`, which did not make plan readiness, open questions, or implementation progress visible enough.

The maintainer asked for the smallest possible status set that still makes tracing possible, and specifically asked to make plan readiness, open questions, and implementation progress more visible.

## Decision

Use these canonical plan statuses:

- Draft.
- Approved.
- In Progress.
- Blocked.
- Implemented.
- Closed.

`Closed` plans must include `Close-Reason:` with one of these values:

- Released.
- Rejected.
- Superseded.
- Deferred.
- Archived.

Use `Approved` instead of the old `Accepted` plan status. Use `In Progress`, not `Implementing`. Use `Deferred`, not `Defered`.

Every plan must keep a short `## Readiness` section near the top that shows:

- Plan readiness.
- Open questions.
- Implementation progress.

## Consequences

Plan status remains small and easy to validate while closure reasons still preserve release, rejection, supersession, deferral, and archival traceability.

Plan readiness, open questions, and implementation progress become visible in the plan body instead of being overloaded into the status value.

Existing guidance that refers to accepted plans should be updated to approved plans when it is live workflow guidance. Historical ADR text may keep its original wording.

## Alternatives Considered

- Use the larger status set from `PROP-plan-status-vocabulary`, including `Under Review`, `Released`, `Rejected`, `Superseded`, `Deferred`, and `Archived` as direct statuses. This was not chosen because it mixes workflow state and final outcome in one field.
- Keep `Accepted` as the implementation-ready status. This was not chosen because `Approved` is clearer for plan readiness and avoids confusion with ADR status values.
- Track readiness only in free-form notes. This was not chosen because the important trace points would remain easy to miss.

## Follow-Up

- Update plan guidance, the plan template, and existing plan files.
- Extend documentation validation for plan status values and readiness sections.
- Mark `PROP-plan-status-vocabulary` completed.
