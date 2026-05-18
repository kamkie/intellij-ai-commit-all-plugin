---
proposal_id: PROP-import-codex-subagents-as-skills
generated_at: 2026-05-18T13-03
purpose: Propose a narrow import strategy for adapting selected VoltAgent Codex subagents into repository-local skills.
scope: Covers `.agents/skills/`, related agent guidance, and the decision boundary for adapting external subagent prompts into this repository.
---

# Import Codex Subagents As Skills

This proposal respects `AGENTS.md`, ADR 0062, `.agents/references/documentation.md`, and `docs/proposals/README.md`. It lists findings for maintainer triage only; it does not import skills or change agent behavior by itself.

Source reviewed: `https://github.com/VoltAgent/awesome-codex-subagents/`, including the catalog README and representative subagent files such as `docs-researcher.toml`, `documentation-engineer.toml`, `reviewer.toml`, `architect-reviewer.toml`, `kotlin-specialist.toml`, and `test-automator.toml`.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
    - [D001. Merge overlapping Kotlin and testing subagent patterns into existing skills](#d001-merge-overlapping-kotlin-and-testing-subagent-patterns-into-existing-skills)
- [Simplification Opportunities](#simplification-opportunities)
    - [S001. Add a platform docs research skill](#s001-add-a-platform-docs-research-skill)
    - [S002. Add a repository documentation skill](#s002-add-a-repository-documentation-skill)
    - [S003. Add a plugin review skill](#s003-add-a-plugin-review-skill)
    - [S004. Keep the broad subagent catalog out of repo-local skills](#s004-keep-the-broad-subagent-catalog-out-of-repo-local-skills)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- Do not copy the upstream `.toml` subagents directly into `.agents/skills/`; convert only useful workflow patterns into concise `SKILL.md` files.
- Prefer three new narrow skills: `platform-docs-research`, `repository-documentation`, and `plugin-review`.
- Update existing `kotlin-plugin-style` and `plugin-test-tdd` with any useful missing upstream checks instead of creating duplicate `kotlin-specialist` or `test-automator` skills.
- Keep broad frontend, backend, cloud, business, and domain-specialist agents out of this repository until a repeated project-local need appears.
- No implementation is performed by this proposal.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance. `Status` tracks implementation progress. `Decision` records maintainer triage.

| Id   | Title                                                                       | Priority | Status | Decision |
|------|-----------------------------------------------------------------------------|----------|--------|----------|
| D001 | Merge overlapping Kotlin and testing subagent patterns into existing skills | 2        | done   | accepted |
| S001 | Add a platform docs research skill                                          | 2        | done   | accepted |
| S002 | Add a repository documentation skill                                        | 2        | done   | accepted |
| S003 | Add a plugin review skill                                                   | 2        | done   | accepted |
| S004 | Keep the broad subagent catalog out of repo-local skills                    | 1        | done   | accepted |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current timestamp.
- Use `status` for implementation progress and `decision` for maintainer triage.
- Leave `decision` empty when authoring new findings; only maintainer triage fills it.
- Set `accepted_at` when `decision: accepted`; set `decided_at` for any other non-empty decision.
- Update the Proposal Implementation Summary in `docs/proposals/README.md` for accepted findings with non-terminal implementation status and an evidence path. A `TASKS.md` entry is optional when another evidence path is clearer.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

_No tracked findings._

## Duplications To Remove Or Reduce

### D001. Merge overlapping Kotlin and testing subagent patterns into existing skills

- Evidence: This repository already has `.agents/skills/kotlin-plugin-style/SKILL.md` and `.agents/skills/plugin-test-tdd/SKILL.md`. The upstream catalog includes overlapping `kotlin-specialist.toml` and `test-automator.toml` roles with useful but generic Kotlin and test-automation guidance.
- Impact: Importing those upstream roles as separate skills would create two sources of truth for Kotlin style and test workflow, increasing trigger ambiguity and guidance drift.
- Proposal: Do not add new `kotlin-specialist` or `test-automator` skills. Instead, review the upstream files for concise checks worth merging into the existing skills, such as coroutine cancellation assumptions, Java interop nullability, deterministic test fixtures, and mapping tests to behavior contracts. Keep the existing repository-specific names.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T13:10:08+02:00
accepted_at: 2026-05-18T13:10:08+02:00
decided_at:
comment: Implemented in the existing Kotlin and TDD skill guidance.
```

## Simplification Opportunities

### S001. Add a platform docs research skill

- Evidence: Upstream `docs-researcher.toml` is focused on documentation-backed verification of API, version-specific, and framework behavior. This repository repeatedly depends on current IntelliJ Platform, Gradle IntelliJ Platform Plugin, Kotlin, JUnit, and Codex behavior, but currently relies on task-specific browsing plus local references.
- Impact: A dedicated research skill would make version-sensitive checks more consistent and would reduce the risk of agents guessing about IntelliJ Platform APIs, Gradle plugin behavior, JetBrains AI Assistant constraints, or current Codex/OpenAI docs.
- Proposal: Add `.agents/skills/platform-docs-research/SKILL.md`, adapted from the upstream docs-researcher pattern. The skill should require official or primary sources first, explicit version scope, citations for high-impact claims, clear separation between documented fact and inference, and a concrete follow-up validation step when docs are inconclusive. It should not change code unless the user explicitly asks for implementation.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T13:10:08+02:00
accepted_at: 2026-05-18T13:10:08+02:00
decided_at:
comment: Implemented as .agents/skills/platform-docs-research.
```

### S002. Add a repository documentation skill

- Evidence: Upstream `documentation-engineer.toml` focuses on keeping technical documentation faithful to current code, tooling, and operator workflows. This repository has a detailed documentation ownership map in `.agents/references/documentation.md`, proposal governance in `docs/proposals/README.md`, ADR rules, changelog rules, and support-policy rules, but no skill that triggers specifically for repository documentation work.
- Impact: Documentation tasks currently require agents to rediscover which governing document owns each artifact. A skill would give documentation edits a stable entry point without adding more content to `AGENTS.md`.
- Proposal: Add `.agents/skills/repository-documentation/SKILL.md`, adapted from the upstream documentation-engineer pattern and grounded in this repository. It should trigger for `README.md`, `SUPPORT.md`, `TASKS.md`, `CHANGELOG.md`, `docs/decisions/`, `docs/proposals/`, `.agents/references/`, and `.agents/plans/` work; instruct agents to read `.agents/references/documentation.md`; and require `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1` plus `git diff --check` after docs guidance changes.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T13:10:08+02:00
accepted_at: 2026-05-18T13:10:08+02:00
decided_at:
comment: Implemented as .agents/skills/repository-documentation.
```

### S003. Add a plugin review skill

- Evidence: Upstream `reviewer.toml` and `architect-reviewer.toml` provide PR-style and architecture-review patterns. This repository has `.agents/references/reviews.md`, but it does not yet have a skill that triggers on review requests and loads this repository's plugin-specific risk priorities.
- Impact: A review skill would make review behavior more discoverable and keep review output focused on this plugin's highest-risk paths: unintended commits, AI Assistant invocation failures, premature commits, push behavior, IntelliJ API compatibility, and missing sandbox validation.
- Proposal: Add `.agents/skills/plugin-review/SKILL.md`, combining only the useful upstream reviewer and architect-reviewer workflow patterns with local review priorities. It should be read-only by convention, lead with concrete findings, distinguish evidence from hypotheses, avoid style-only commentary, read `.agents/references/reviews.md`, and pull in `.agents/references/testing.md` or `.agents/references/code-style.md` only when the review scope needs them.

```yaml
status: done
decision: accepted
priority: 2
owner:
updated: 2026-05-18T13:10:08+02:00
accepted_at: 2026-05-18T13:10:08+02:00
decided_at:
comment: Implemented as .agents/skills/plugin-review.
```

### S004. Keep the broad subagent catalog out of repo-local skills

- Evidence: The upstream catalog includes broad language, cloud, infrastructure, data, AI, business, product, and orchestration roles. ADR 0062 explicitly chose narrow repository-local skills and rejected importing broad published skills verbatim.
- Impact: A large import would dilute project-specific guidance, increase skill trigger conflicts, and add maintenance burden unrelated to this IntelliJ plugin.
- Proposal: Treat upstream subagents as inspiration only. Import a role as a repository-local skill only when it supports repeated work in this repository and can be rewritten around local governing docs, commands, validation, and risk priorities. Exclude broad roles such as generic backend, frontend, cloud, SEO, sales, business, and domain-specialist agents for now.

```yaml
status: done
decision: accepted
priority: 1
owner:
updated: 2026-05-18T13:10:08+02:00
accepted_at: 2026-05-18T13:10:08+02:00
decided_at:
comment: Implemented by adapting only selected workflow patterns into repository-local skills.
```

## Smaller / Stylistic Items

- If any finding is accepted, generate or refresh `agents/openai.yaml` for new or changed skills so UI metadata matches each `SKILL.md`.
- Conversion rule for accepted imports: map upstream `description` into skill frontmatter, adapt `developer_instructions` into concise repository-specific `SKILL.md` body, and discard upstream `model`, `model_reasoning_effort`, `sandbox_mode`, and MCP configuration unless a separate repository decision explicitly needs them.
- Validate skill metadata with the local skill validator and validate repository docs with `scripts\validate-docs.ps1`.

## Suggested Priority Order

1. `S004` - confirm the guardrail before any import work starts.
2. `D001` - prevent duplicate Kotlin and test skills before adding new ones.
3. `S001` - add the highest-value cross-cutting support skill for version-sensitive platform research.
4. `S002` - add the documentation skill to reduce repository-governance drift.
5. `S003` - add the review skill after the import guardrails and existing-skill dedupe are settled.

## Out Of Scope

- Importing upstream `.toml` files into `.codex/agents/`.
- Creating or modifying actual skills before maintainer triage.
- Changing ADR 0062 or the repository's skill policy.
- Adding generic frontend, backend, cloud, data, business, product, or domain-specialist skills.
- Changing plugin behavior, build configuration, tests, release workflow, or user-facing documentation.
