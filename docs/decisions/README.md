# Decision Records

Use this directory for accepted, durable decisions that future work should preserve.

Do not add a decision record for every task. Add one when a choice affects architecture, compatibility, user behavior, validation expectations, or future maintenance.

## When To Add A Decision

Good candidates include:

- Minimum supported IntelliJ Platform version.
- Supported IDE family or Git-only versus broader VCS support.
- Runtime-discovered AI Assistant action versus direct dependency on AI Assistant APIs.
- Separate commit-and-push action versus reusing the IDE's existing commit executor state.
- Compatibility policy for IntelliJ Platform API changes.

## Status Values

- Proposed: under discussion.
- Accepted: current guidance.
- Superseded: replaced by a newer decision.
- Rejected: intentionally not chosen.

## Naming

Use a short numbered filename:

```text
0001-minimum-intellij-platform-version.md
0002-ai-assistant-integration-strategy.md
```

Start from `ADR_TEMPLATE.md`.
