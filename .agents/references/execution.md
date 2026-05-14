# Execution Guide

Use this loop for implementation work.

This file also owns AI-facing commit-message rules. The repository root `.gitmessage` is the authoritative commit-message template and example source.

## Loop

1. Frame the behavior: name the user-facing behavior, command, action, or workflow being changed.
2. Identify the owner artifact: find the source, descriptor, docs, or task list that governs the behavior.
3. Update docs or specs when behavior changes: keep `README.md`, `TASKS.md`, and agent guidance aligned with the implementation.
4. Implement the smallest change: stay within the requested scope and existing project shape.
5. Run targeted validation: choose checks from `.agents/references/testing.md` based on the diff.
6. Self-review: use `.agents/references/reviews.md` to check for behavior, compatibility, and validation gaps.
7. Report evidence: summarize changed files, validation run, and any remaining risk.

## Context Rules

- Read only the context needed for the current task.
- Prefer existing IntelliJ Platform and Gradle plugin conventions over custom infrastructure.
- Do not add release, publishing, signing, CI, marketplace, or operations files unless requested.
- If the repo is still unscaffolded, do not assume Gradle, Kotlin, or plugin descriptor files exist.

## Commit Rules

Commit completed work only when the user asks for a commit or the task scope explicitly requires it.

When creating a commit for AI-authored work:

- Use Conventional Commits 1.0.0 style.
- Use `.gitmessage` as the authoritative template, rule set, and example source.
- Include the AI metadata trailer block required by `.gitmessage`.
- Keep all project metadata footers contiguous; do not put blank lines between trailer lines.
- Include `Validation:` with the command and result, or `not run` with a reason.
- When committing non-interactively, use a commit-message file or one final message paragraph for all trailer lines. Do not pass each footer as a separate `git commit -m` argument because Git inserts blank lines between message paragraphs.

Use `Project-Source: prompt` for direct ad hoc user requests, `task` for `TASKS.md` items, `plan` or `plan-task` for accepted plan work, and `manual` only for human-authored commits outside the AI workflow.

## Stop Conditions

Pause and ask for a decision when implementation depends on an unresolved product choice, such as minimum IDE version, Git-only versus all VCS, or direct dependency on proprietary AI Assistant APIs.
