---
name: gh-fix-ci-security-quality
description: GitHub CLI workflow for inspecting and fixing failing GitHub PR checks, GitHub Actions logs, code-scanning alerts, Dependabot alerts, and GitHub Security or quality findings in this repository. Use when a user asks to debug, review, summarize, or fix failing CI, PR checks, GitHub Actions failures, Dependabot alerts, code scanning alerts, security alerts, or quality alerts.
---

# GitHub CI Security Quality Fixes

## Start

- Read `AGENTS.md`, `.agents/references/testing.md`, and `.agents/references/reviews.md`.
- Read `.agents/references/planning.md` when the fix changes plugin behavior, touches multiple areas, or otherwise needs an approved implementation plan.
- Read `.agents/references/documentation.md` when the alert or fix affects governed docs, CI workflow guidance, support policy, release notes, or agent guidance.
- Run `gh auth status` in the repository before inspecting PR checks, workflow runs, code-scanning alerts, or Dependabot alerts.
- Resolve the target: PR checks, security and quality alerts, or both.

This skill intentionally has no bundled scripts. Use direct `gh` commands and small shell filters instead of relying on copied helper scripts.

## CI Check Triage

1. Resolve the pull request.
    - Prefer the current branch PR: `gh pr view --json number,url,headRefName,headRefOid`.
    - If the user provides a PR number or URL, use that target directly.
2. Inspect PR checks.
    - Start with `gh pr checks <pr> --json name,state,bucket,link,startedAt,completedAt,workflow`.
    - If a JSON field is rejected by the installed `gh` version, rerun with the available fields reported by `gh`.
3. Separate GitHub Actions checks from external checks.
    - For GitHub Actions, extract the run ID from the check link and inspect it with `gh run view <run_id> --json name,workflowName,conclusion,status,url,event,headBranch,headSha`.
    - Fetch logs with `gh run view <run_id> --log`; when that is incomplete, inspect jobs with `gh run view <run_id> --json jobs` and then fetch the failing job log with `gh run view <run_id> --job <job_id> --log`.
    - For external checks, report the check name and details URL only unless the user explicitly asks to use that provider.
4. Summarize failures.
    - Report check name, workflow, run URL, status, conclusion, head SHA, failing job or step, and the smallest useful log snippet.
    - Redact tokens, certificates, passwords, private URLs, and user-private data from copied log excerpts.

## Security And Quality Triage

1. Resolve the GitHub repository owner and name with `gh repo view --json owner,name,url`.
2. Inspect open code-scanning alerts.
    - Use `gh api "/repos/<owner>/<repo>/code-scanning/alerts?state=open&per_page=100"`.
    - Report alert number, rule ID, severity, state, file path, line, message, tool name, and URL.
3. Inspect open Dependabot alerts.
    - Use `gh api "/repos/<owner>/<repo>/dependabot/alerts?state=open&per_page=100"`.
    - Report alert number, package, ecosystem, severity, vulnerable version range, first patched version, manifest path, scope, and URL.
4. Group repeated findings.
    - Call out repeated alert families, such as multiple unpinned workflow actions, repeated dependency updates, or duplicate code-scanning rules, so a batch fix is explicit.

## Fix Workflow

1. Preserve repository gates.
    - If the user only asked to inspect or summarize, stop after reporting findings and proposed next steps.
    - If the user asked to fix and the change is small and isolated, state the intended edit briefly and implement it.
    - If the fix is non-trivial, follow `.agents/references/planning.md` and stop after creating or updating the plan until the user approves it.
2. Make the smallest repository-specific fix.
    - Prefer existing Gradle, Kotlin, IntelliJ Platform, documentation, and CI patterns in this repository.
    - Do not dismiss, close, or mark alerts fixed through GitHub APIs unless the user explicitly asks for that action.
    - For workflow action pinning alerts, pin actions to verified full commit SHAs rather than replacing one floating tag with another.
    - Keep security fixes from broadening the plugin's public behavior, support promise, or release workflow unless the governing docs and decisions are updated first.
3. Validate against the changed surface.
    - Use `.agents/references/testing.md` to choose checks.
    - For `.github/workflows/` edits, run docs or YAML validation available in the repo and report any check that cannot be run locally.
    - For dependency or source fixes, run the narrowest useful Gradle checks before broader validation.
4. Recheck GitHub status when possible.
    - For CI, rerun or suggest `gh pr checks <pr>` after pushing or after GitHub has rerun checks.
    - For security alerts, rerun the relevant `gh api` query after the fix is pushed and GitHub has reprocessed the repository.

## Stop Conditions

- `gh` is missing, unauthenticated, or lacks access to the target PR or security endpoints.
- GitHub logs or alert details are unavailable and no actionable failure can be extracted.
- The failing check is external to GitHub Actions and the user has not asked to use that provider.
- The fix requires a repository decision, an ADR, or an approved plan before editing.
- The requested action would dismiss or close an alert without explicit user approval.
- The change risks committing secrets, private log data, generated credentials, or destructive repository state.

## Output

Report the target PR or alert set, failing checks or open alerts, actionable evidence, files changed, validation run, GitHub recheck status when available, skipped checks with reasons, and remaining risk.
