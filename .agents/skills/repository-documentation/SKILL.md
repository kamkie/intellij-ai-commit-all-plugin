---
name: repository-documentation
description: Repository documentation workflow for this IntelliJ plugin. Use when adding, updating, reviewing, or triaging README.md, SUPPORT.md, TASKS.md, CHANGELOG.md, docs/decisions/, docs/proposals/, docs/WORKING_WITH_AI.md, docs/DEVELOPMENT_LIFECYCLE.md, .agents/references/, .agents/plans/, .agents/skills/, AGENTS.md, or other governed contributor and AI-agent documentation.
---

# Repository Documentation

## Start

- Read `AGENTS.md` if it is not already loaded.
- Read `.agents/references/documentation.md` before editing governed docs.
- Read the specific owner guide for the artifact:
    - Proposals: `docs/proposals/README.md`
    - ADRs and open questions: `docs/decisions/README.md`
    - Plans: `.agents/references/planning.md`
    - Testing or validation docs: `.agents/references/testing.md`
    - Reviews: `.agents/references/reviews.md`
    - Release docs or changelog: `.agents/references/releases.md`
- Use the smallest owner-specific context set. Do not load every guidance file by default.

## Editing

- Map the requested change to the owning artifact before editing.
- If a requested documentation or workflow change requires an ADR, create the ADR first and stop until explicit acceptance.
- If a requested change needs an implementation plan, create or update the plan first and stop until explicit approval.
- Keep proposal decisions empty unless the user explicitly triages them; when accepted, set `decision: accepted`, `accepted_at`, and the implementation status required by `docs/proposals/README.md`.
- Preserve stable IDs for tasks, open questions, ADRs, proposals, findings, and plans.
- Update `CHANGELOG.md` for notable user-facing, contributor-facing, compatibility, support, release, or AI workflow changes.
- Keep docs proportional and repository-specific. Do not copy generic Spring, REST, OpenAPI, deployment, operations, benchmark, frontend, or unrelated guidance from external repositories.

## Validation

- Run docs validation after documentation, proposal, ADR, plan, changelog, support, or agent-guidance edits:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1
```

- Run `git diff --check` before handoff.
- Report validation commands, skipped checks with reasons, and any remaining governance risk.
