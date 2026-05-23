# Working With AI

Use this guide when asking an AI agent to help with this repository. It is for humans preparing requests, not for the agent's normal instruction path. Do not paste this file into ordinary work requests; the repository AI entry point is `AGENTS.md`.

## Request Basics

- Ask for the outcome you want, not a list of every file to inspect.
- Prefer stable refs when you have them: `T-<AREA>-NNN`, `adr-NNNN`, `PLAN-<slug>`, `PROP-<slug>`, a prompt filename, or a concrete file path.
- State the boundary: design-only, analysis-only, planning-only, implementation, validation, review, commit, or release.
- State constraints that matter: direct one-off no delegation, read-only review, no commits, exact write scope, no network, unavailable sub-agents, specific validation, or manual sandbox scope.
- When behavior matters, include what must be true when the work is done.
- Give approval explicitly when crossing a gate: accepting an ADR, approving a plan, or asking for plan execution.
- Avoid broad context requests such as “read all docs” unless the task is a broad audit.
- Do not mix “review only” with “fix it” unless edits are intended.

Most requests can be a plain sentence or a ref, for example `fix T-BUG-013`, `review this diff`, or `run release readiness`. Use a structured shape only when the work has several constraints:

```text
Task:
Goal:
Scope or target artifacts:
Constraints:
Validation expected:
```

For bug reports, include what happened, what you expected, the action that triggered it, relevant refs or file/log paths, and any validation already run.

Use `bug-report-triage.md` when you want classification before a fix.

For screenshots or concept images, include the file path, what state it shows, and what feedback you want.

## Common Controls

Use these short phrases to steer the session:

When naming a prompt, name the file; do not paste the prompt contents.

- `Do not delegate this direct one-off work. Use only the current agent session.`
- `Use read-only sidecars only for this direct one-off work.`
- `Use read-only exploration sidecars for source-map or artifact lookup.`
- `Use subagents/delegation as needed to avoid context compaction.`
- `Do not commit.`
- `Commit when the approved task is complete.`
- `Keep this as a design-only pass.`
- `Do not implement yet; update the plan only.`
- `Proceed as a direct one-off unless a gate triggers.`
- `Stop and ask if this needs a new ADR or plan.`

## Refs To Name

Name refs or files when they are relevant:

- `T-<AREA>-NNN` for backlog tasks in `TASKS.md` or `TASKS_ARCHIVE.md`.
- `adr-NNNN` for decisions in `docs/decisions/`.
- `PLAN-<slug>` for implementation plans in `.agents/plans/`.
- `PROP-<slug>` for proposals in `docs/proposals/`.
- Prompt filenames such as `backlog-triage.md` for reusable repository prompt recipes.
- Concrete files when the request is intentionally narrow.

## Constraints To State

State constraints that would change implementation, validation, or coordination:

- Target IntelliJ Platform version or IDE support scope when it matters; otherwise rely on the repository support policy and build configuration.
- Git-only behavior and multiple Git root expectations.
- JetBrains AI Assistant dependency and whether proprietary APIs may be used directly. The default is no.
- Three-section `AI | Commit | Push` behavior or styling constraints.
- Plugin ID, package, vendor, license, Marketplace, signing, or CI constraints.
- Manual sandbox validation scope, especially AI Assistant, Git staging area, commit-only, commit-and-push, and push behavior.
- Delegation and context preference for direct one-off work: optional delegation, read-only sidecars only, disjoint write scopes, no delegation, or `Use subagents/delegation as needed to avoid context compaction.`
- Environment or tool limits: no subagents, no network, no browser tools, read-only filesystem, unavailable validation tools, locked files, or commands that must not be run.

## Development Flow

Use these stages to name where the request sits in the lifecycle. Process gates live in `docs/DEVELOPMENT_LIFECYCLE.md`; this section only gives compact request shapes.

| Stage     | Request shape                                                                                                                          | Prompt or owner                                                                      |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| Orient    | `Summarize repository state. Include worktree status, active plans, open questions, and next actions. Do not edit.`                    | `repository-state-snapshot.md`                                                       |
| Design    | `Run a design-only pass for <UI/concept/draft>. Do not implement production plugin UI.`                                                | `design-draft-session.md`                                                            |
| Propose   | `Create a proposal for <problem>. I want findings and options for triage, not implementation.`                                         | `repository-quality-audit.md`, `proposal-consolidation.md`, `compact-ai-guidance.md` |
| Decide    | `Draft an ADR for <decision>. Stop after the ADR unless a companion draft plan is clearly required.`                                   | `adr-impact-check.md`, `docs/decisions/README.md`                                    |
| Plan      | `Create or update PLAN-<slug> for <goal>. Do not implement until I approve the plan.`                                                  | `.agents/references/planning.md`                                                     |
| Implement | `Implement <ref or behavior>. Scope: <scope>. Constraints: <constraints>. Validation expected: <checks>.`                              | `.agents/references/execution.md`                                                    |
| Validate  | `Run validation for <risk or artifact>. Report commands, results, skipped checks, and remaining risk.`                                 | `.agents/references/testing.md`                                                      |
| Review    | `Review <diff/files/ref> for <risk>. Findings first; do not edit.`                                                                     | `change-closeout.md`, `plugin-compatibility-sweep.md`, `ci-failure-triage.md`        |
| Commit    | `Commit the completed work after validation.`                                                                                          | `.gitmessage`                                                                        |
| Release   | `Check release readiness for <version or boundary>. Include changelog, support, package, signing, CI, tag, and Marketplace readiness.` | `release-readiness.md`                                                               |

Useful transitions:

- `Design pass is done. Turn the selected option into the next required ADR, plan, or implementation request.`
- `I accept finding <id>. Turn it into the next required ADR, plan, or direct task.`
- `I accept adr-NNNN.`
- `I approve PLAN-<slug>; execute it.`

For small, already-decided behavior, ask for direct work:

```text
Implement <ref or behavior>. Keep it direct if existing ADRs, specs, owner docs, or exact task refs already decide the behavior.
```

## Privacy And Logs

- Do not paste tokens, passwords, private keys, signing certificates, Marketplace secrets, IDE auth state, or private remote URLs.
- Prefer local file paths, sanitized excerpts, or a short description of where the data came from.
- For logs, provide the smallest relevant excerpt when possible and say whether it is sanitized.
- For long logs or IDE log folders, provide the path and the specific behavior or time window to inspect.

## Avoid

- Do not ask to load every guidance file unless the request is a broad guidance audit.
- Do not paste large logs or generated output when a file path or short excerpt is enough.
