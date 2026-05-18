---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T11:21:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Require Plan Worker Count Metadata

## Context and Problem Statement

ADR 0026 allows parallel plan execution only when an approved plan explicitly marks tasks as independent and assigns disjoint write scopes. Existing plan metadata does not declare the intended maximum worker count, so reviewers and validation cannot identify planned parallel execution at a glance.

`docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` finding S001 proposes making the worker count explicit before adding richer multi-agent execution graph rules.

## Decision Drivers

* Make planned worker count visible in every plan.
* Preserve ADR 0026's narrow parallel execution exception.
* Give documentation validation a simple structural rule it can enforce.
* Keep sequential plans simple.
* Prepare plan metadata for later execution graph validation.

## Considered Options

* Require `Workers:` metadata on every plan
* Require worker metadata only for parallel plans
* Keep worker count implicit in plan prose

## Decision Outcome

Chosen option: "Require `Workers:` metadata on every plan", because every plan should make its execution topology visible without requiring reviewers to infer whether worker count was intentionally considered.

Every active and archived plan file scanned by documentation validation must include a `Workers:` field near the existing plan metadata. Sequential plans use:

```text
Workers: 1
```

Parallel plans use:

```text
Workers: N (parallel, tasks: <task ids or labels>)
```

`N` is the maximum intended active worker count for the plan. Parallel values are valid only when the plan also marks the listed tasks independent and assigns disjoint write scopes under ADR 0026.

Implementation updates `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, `.agents/references/planning.md`, and `scripts/validate-docs.ps1`. The field is backfilled on every active and archived plan file that validation scans. Validation requires the field and rejects malformed values. Deeper consistency checks against execution graphs can be handled by the execution graph decision.

### Consequences

* Good, because reviewers can see sequential versus parallel intent in plan metadata.
* Good, because validation can catch plans that imply multiple workers without declaring them.
* Good, because the rule preserves ADR 0026's disjoint write scope requirement.
* Bad, because every existing plan that validation scans needs a metadata backfill.

### Confirmation

Compliance is checked by documentation review and `scripts/validate-docs.ps1` after acceptance:

* `PLAN_TEMPLATE.md` contains the `Workers:` field.
* `.agents/plans/README.md` and `.agents/references/planning.md` describe valid values.
* Active and archived plans scanned by validation include the field.
* `scripts/validate-docs.ps1` rejects missing or malformed worker-count metadata.

## Pros and Cons of the Options

### Require `Workers:` metadata on every plan

* Good, because the field is always present and easy to scan.
* Good, because sequential execution is expressed with a simple `Workers: 1`.
* Good, because future validation can compare the value to execution graphs.
* Bad, because historical plan files need mechanical updates.

### Require worker metadata only for parallel plans

* Good, because it reduces churn in sequential plans.
* Bad, because absence remains ambiguous: no parallelism intended, or metadata forgotten.
* Bad, because validation must infer which plans are sequential.

### Keep worker count implicit in plan prose

* Good, because no files need to change.
* Bad, because reviewers must infer intended parallel execution from prose.
* Bad, because validation cannot reliably catch undeclared worker count.

## More Information

- Source proposal finding: `docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` S001.
- Related decisions: ADR 0023, ADR 0024, ADR 0026, and the proposed plan execution graph ADR.
