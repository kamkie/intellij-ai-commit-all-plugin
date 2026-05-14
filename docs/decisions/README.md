# Decision Records

Use this directory for project decisions and repository rule changes that future work should preserve.

Every project decision must be recorded as an ADR before or alongside the implementation it affects.

Every repository rule change must be recorded as a new ADR or as an update that supersedes an existing ADR before or alongside the rule edit.

Routine task execution notes do not need ADRs unless they choose or change project direction, repository rules, compatibility, user behavior, validation expectations, or future maintenance policy.

## When To Add A Decision

Required ADR topics include:

- Minimum supported IntelliJ Platform version.
- Supported IDE family or Git-only versus broader VCS support.
- Runtime-discovered AI Assistant action versus direct dependency on AI Assistant APIs.
- Separate commit-and-push action versus reusing the IDE's existing commit executor state.
- Compatibility policy for IntelliJ Platform API changes.
- Repository rule or workflow changes.

## Status Values

- Proposed: under discussion.
- Accepted: current guidance.
- Superseded: replaced by a newer decision.
- Rejected: intentionally not chosen.

## Naming

Use a short numbered filename:

```text
0000-initial-repository-creation-and-scaffolding.md
0001-import-lightweight-ai-guidance-model.md
0002-record-rule-changes-and-project-decisions.md
```

Start from `ADR_TEMPLATE.md`.
