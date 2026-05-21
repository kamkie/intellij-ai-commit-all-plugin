---
status: accepted
date: 2026-05-21
accepted_at: 2026-05-21T21:08:46+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: GitHub Copilot
informed: repository maintainers
---

# Extend Agent Artifact Validation

## Context and Problem Statement

The repository has structured AI-agent guidance across `AGENTS.md`, `.agents/references/`, `.agents/skills/`, `.agents/prompts/`, and `.agents/plans/`. The current `scripts/ai/validate-agent-artifacts.ps1` validates skills and prompts, but it does not validate references, plan catalog links, plan metadata, or broken cross-links between `.agents` artifacts.

As the AI instruction set grows, broken links, stale plan catalog entries, malformed plan metadata, and missing reference targets can silently degrade agent behavior. The repository should decide whether these additional agent-guidance contracts belong in automated validation.

## Decision Drivers

- Keep AI-agent workflow guidance internally consistent.
- Catch broken `.agents` references before handoff.
- Keep validation proportional to repository size and maintenance cost.
- Avoid making historical archived plans fail on harmless formatting differences.
- Preserve the existing minimal-read-set instruction model.

## Considered Options

- Extend `scripts/ai/validate-agent-artifacts.ps1` to validate references and plans.
- Leave validation limited to skills and prompts.
- Move all `.agents` checks into `scripts/validate-docs.ps1` only.

## Decision Outcome

Chosen option: "Extend `scripts/ai/validate-agent-artifacts.ps1` to validate references and plans", because `.agents` artifacts form one agent-instruction system and should have one focused validator for structural consistency.

### Consequences

- Good, because missing `.agents/skills/.../SKILL.md`, `.agents/references/*.md`, `.agents/prompts/*.md`, and `.agents/plans/*.md` links can be caught automatically.
- Good, because active and archived plan catalog drift can be detected earlier.
- Good, because malformed plan metadata can be detected without running plugin builds.
- Bad, because the validation script becomes more complex and needs careful compatibility with archived plans.

### Confirmation

Compliance is confirmed by running:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1
```

The validator should remain permissive of extra plan sections and archived-plan historical content unless a specific accepted rule requires stricter enforcement.

## Pros and Cons of the Options

### Extend `scripts/ai/validate-agent-artifacts.ps1` to validate references and plans

- Good, because it keeps `.agents` guidance validation in the artifact-specific script.
- Good, because it supports the documentation guide's ownership model for `.agents` artifacts.
- Good, because it can validate hidden `.agents` paths by direct filesystem traversal instead of relying on broad glob behavior.
- Neutral, because `scripts/validate-docs.ps1` can still remain the broader documentation validator.
- Bad, because it adds script maintenance burden.

### Leave validation limited to skills and prompts

- Good, because this keeps the current validator simple.
- Bad, because reference-to-skill, skill-to-reference, and plan-catalog drift can persist until manual review.
- Bad, because the validation name suggests broader agent-artifact coverage than it currently provides.

### Move all `.agents` checks into `scripts/validate-docs.ps1` only

- Good, because there would be one documentation validation entry point.
- Bad, because it hides agent-specific structure checks inside a broad documentation script.
- Bad, because `scripts/ai/validate-agent-artifacts.ps1` would remain partial or redundant.

## More Information

If accepted, implementation should:

- Update `scripts/ai/validate-agent-artifacts.ps1` to validate `.agents/references/` files have exactly one level-one heading.
- Validate backtick-delimited `.agents/...` file references in references, skills, prompts, and plans where they point to concrete files.
- Validate `.agents/plans/README.md` catalog links point to existing active or archived plan files.
- Validate active and archived plan files, excluding `README.md` and `PLAN_TEMPLATE.md`, for `Plan-ID`, `Status`, `Workers`, `Filename`, `## Readiness`, `## Status History`, and `## Execution Graph`.
- Require `Close-Reason` only when `Status: Closed`.
- Require non-empty `Approved by:` and `Approved at:` for approved or post-approval plan statuses.
- Keep extra sections allowed.
- Update `.agents/references/testing.md`, `.agents/references/troubleshooting.md`, and `.agents/references/documentation.md` wording if needed to describe the expanded validator scope.
- Tighten `.agents/prompts/ide-log-triage.md` so default-folder access requires confirmation when the user does not name a folder or provide excerpts.
- Refresh the ADR filename example in `docs/decisions/README.md` to use `adr-NNNN-example-decision.md` instead of a real historical number.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date.
