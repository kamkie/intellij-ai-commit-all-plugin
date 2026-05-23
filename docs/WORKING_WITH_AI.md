# Working With AI

Use this guide when asking an AI agent to help with this repository. It is for humans preparing requests, not for the agent's normal instruction path. Do not paste this file into ordinary work requests; the repository AI entry point is `AGENTS.md`.

## Request Basics

- Ask for the outcome you want, not a list of every file to inspect.
- Prefer stable refs when you have them: `T-<AREA>-NNN`, `adr-NNNN`, `PLAN-<slug>`, `PROP-<slug>`, a prompt filename, or a concrete file path.
- State the boundary: design-only, analysis-only, planning-only, implementation, validation, review, commit, or release.
- State constraints that matter: no delegation, read-only review, no commits, exact write scope, no network, specific validation, or manual sandbox scope.
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

For screenshots or concept images, include the file path, what state it shows, and what feedback you want.

## Common Controls

Use these short phrases to steer the session:

- `Do not delegate this work. Use only the current agent session.`
- `Use read-only sidecars only.`
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

- Target IntelliJ Platform version, currently the 2026.1 line.
- Target JetBrains IDEs, currently IDEs with the VCS Commit tool window.
- Git-only behavior and multiple Git root expectations.
- JetBrains AI Assistant dependency and whether proprietary APIs may be used directly. The default is no.
- Three-section `AI | Commit | Push` behavior or styling constraints.
- Plugin ID, package, vendor, license, Marketplace, signing, or CI constraints.
- Manual sandbox validation scope, especially AI Assistant, Git staging area, commit-only, commit-and-push, and push behavior.
- Delegation preference: optional delegation, read-only sidecars only, disjoint write scopes, or no delegation.
- Context protection: `Use subagents/delegation as needed to avoid context compaction.`
- Environment or tool limits: no subagents, no network, no browser tools, read-only filesystem, unavailable validation tools, locked files, or commands that must not be run.

## Development Flow

Most work fits one of these stages. Name the stage when it matters.

### 1. Orient

Understand the current repository state before choosing work.

```text
Summarize the current repository state. Include worktree status, active plans, open questions, notable tasks, and the next 1-3 recommended actions. Do not edit files.
```

Prompt: `repository-state-snapshot.md`.

### 2. Design

Explore visual drafts, UI variants, icons, graphics, or interactions before implementation.

```text
Run a design-only pass for <UI/concept/draft>. Compare variants, note visual risks, and do not implement production plugin UI.
```

Transition:

```text
Design pass is done. Turn the selected option into the next required ADR, plan, or implementation request.
```

Prompt: `design-draft-session.md`.

### 3. Propose

Collect findings, duplicates, simplification options, or tradeoffs before committing to a direction.

```text
Create a proposal for <problem>. I want findings and options for triage, not implementation.
```

Transition:

```text
I accept finding <id>. Turn it into the next required ADR, plan, or direct task.
```

Prompts: `repository-quality-audit.md`, `proposal-consolidation.md`, `compact-ai-guidance.md`.

### 4. Decide

Record durable decisions about project direction, workflow rules, compatibility, validation, user-facing behavior, or maintenance policy.

```text
Draft an ADR for <decision>. Stop after the ADR unless a companion draft plan is clearly required.
```

Approval:

```text
I accept adr-NNNN.
```

Transition:

```text
Turn accepted adr-NNNN into a draft implementation plan.
```

Prompt: `adr-impact-check.md`.

### 5. Plan

Plan work that needs sequencing, task packets, disjoint write scopes, broader validation, or explicit approval before implementation.

```text
Create or update PLAN-<slug> for <goal>. Do not implement until I approve the plan.
```

Approval:

```text
I approve PLAN-<slug>; execute it.
```

For small, already-decided behavior, ask for direct work instead:

```text
Implement <ref or behavior>. Keep it direct if existing ADRs, specs, owner docs, or exact task refs already decide the behavior.
```

### 6. Implement

Ask for implementation when the desired outcome is clear and gates are already satisfied.

```text
Implement <ref or behavior>.
Scope:
Constraints:
Validation expected:
```

For delegated one-off writes, use a compact human brief:

```text
Goal:
Read first:
Forbidden inputs:
Write scope:
Escalate if:
Stop if:
Expected output:
```

Example:

```text
Use read-only sidecars for exploration and review. Keep writes in <files or dirs>. Stop if scope expands.
```

### 7. Validate

Match validation to the risk.

- Documentation or AI-guidance changes: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\validate-docs.ps1` and `git diff --check`.
- Repository refs, skills, prompts, or plans: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts\ai\validate-agent-artifacts.ps1`.
- Kotlin or Gradle changes: `.\gradlew.bat spotlessCheck`, focused tests, and broader tests when shared behavior changes.
- Detekt cleanup: `.\gradlew.bat detekt`.
- Plugin packaging or descriptor changes: `.\gradlew.bat buildPlugin` and `.\gradlew.bat verifyPluginStructure`.
- Compatibility-sensitive changes: plugin verifier and targeted sandbox checks.
- Runtime commit, push, AI Assistant, or UI workflow changes: targeted automated tests plus manual sandbox evidence where the live IDE owns the behavior.

Request:

```text
Run validation for <risk or artifact>. Report commands, results, skipped checks, and remaining risk.
```

If validation is skipped, ask for the concrete reason.

### 8. Review

Ask for findings before edits, or a second pass after implementation.

```text
Review <diff/files/ref> for <risk>. Findings first; do not edit.
```

Review focuses:

- Bugs, regressions, missing validation, compatibility risk, or architecture concerns.
- Commit selection, AI generation, commit execution, push behavior, staging mode, or multi-root risk.
- Documentation that implies unsupported behavior.
- Read-only sidecar review for a second pass without edits.

Prompts: `repository-quality-audit.md`, `plugin-compatibility-sweep.md`, `ci-failure-triage.md`.

### 9. Commit

Ask for a commit explicitly when you want one. Approved plan execution may already require per-task commits.

```text
Commit the completed work.
```

Expect a Conventional Commit message with the metadata trailer block defined in [.gitmessage](../.gitmessage). For approved plans, expect plan status and validation evidence to be current before each task commit.

### 10. Release

Use release requests after implementation is integrated and validation evidence is ready.

```text
Check release readiness for <version or boundary>. Include changelog, support, package, signing, CI, tag, and Marketplace readiness.
```

Prompt: `release-readiness.md`.

## Privacy And Logs

- Do not paste tokens, passwords, private keys, signing certificates, Marketplace secrets, IDE auth state, or private remote URLs.
- Prefer local file paths, sanitized excerpts, or a short description of where the data came from.
- For logs, provide the smallest relevant excerpt when possible and say whether it is sanitized.
- For long logs or IDE log folders, provide the path and the specific behavior or time window to inspect.

## Avoid

- Do not paste `WORKING_WITH_AI.md` into ordinary work requests.
- Do not ask to load every guidance file unless the request is a broad guidance audit.
- Do not paste large logs or generated output when a file path or short excerpt is enough.
- Do not ask for “review only” and “fix it” in the same sentence unless edits are intended.
- Do not ask for commits unless you want commits, except when executing an approved plan that already requires them.
