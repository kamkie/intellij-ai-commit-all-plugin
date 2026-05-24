---
status: accepted
date: 2026-05-24
accepted_at: 2026-05-24T02:10:41+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Use Two-Space Markdown List Indentation

## Context and Problem Statement

Repository Markdown validation currently configures markdownlint `MD007` with 4-space unordered-list indentation. IntelliJ formats nested Markdown list items with 2-space indentation in normal editing, which creates churn and can make correctly edited files fail `scripts/validate-docs.ps1`.

Should the repository keep the current 4-space markdownlint rule, or should Markdown validation align with IntelliJ's 2-space list indentation?

## Decision Drivers

* Match the editor behavior maintainers actually use.
* Avoid file-local markdownlint suppressions for ordinary list indentation.
* Keep documentation validation useful and predictable.
* Avoid recurring manual reformatting after IntelliJ edits Markdown files.
* Keep mechanical formatting changes separate from content or policy changes.

## Considered Options

* Use 2-space Markdown list indentation.
* Keep 4-space Markdown list indentation.
* Disable Markdown list indentation checks.

## Decision Outcome

Chosen option: "Use 2-space Markdown list indentation", because validation should enforce the formatting style produced by the maintained editor workflow instead of fighting it.

If accepted:

* Set `.markdownlint-cli2.jsonc` `MD007.indent` to `2`.
* Keep `MD005` enabled so list indentation remains internally consistent.
* Update `.agents/references/code-style.md` to document 2-space Markdown nested-list and continuation indentation.
* Reindent existing tracked Markdown files mechanically so they satisfy the new validation policy.
* Avoid file-local markdownlint suppressions for ordinary nested-list indentation.

### Consequences

* Good, because IntelliJ-formatted Markdown should pass repository validation.
* Good, because the repository keeps indentation validation instead of disabling the checks entirely.
* Good, because future prompt and guidance files can use the same list shape as the editor.
* Bad, because implementation may produce a broad mechanical Markdown diff.
* Bad, because reviewers must distinguish mechanical list indentation from content changes.

### Confirmation

After acceptance and implementation, confirm by checking:

* `.markdownlint-cli2.jsonc` uses `MD007.indent` value `2`.
* `.agents/references/code-style.md` documents 2-space Markdown nested-list indentation.
* Existing tracked Markdown files no longer need local suppressions for IntelliJ-style list indentation.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1` passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Use 2-space Markdown list indentation

This option aligns markdownlint with IntelliJ's Markdown formatter.

* Good, because it removes the recurring formatter mismatch.
* Good, because it preserves list indentation checks through `MD005` and `MD007`.
* Good, because it avoids one-off suppressions in prompt files.
* Bad, because it requires reformatting existing Markdown lists.

### Keep 4-space Markdown list indentation

This option keeps the current validation policy.

* Good, because it avoids a broad mechanical diff.
* Good, because it is already documented in `.agents/references/code-style.md`.
* Bad, because IntelliJ-formatted Markdown can fail validation.
* Bad, because maintainers may need local suppressions or manual indentation fixes.

### Disable Markdown list indentation checks

This option disables `MD005` and `MD007` globally.

* Good, because it avoids formatter conflicts.
* Good, because it requires little or no reindentation.
* Bad, because it removes useful consistency checks.
* Bad, because mixed indentation can accumulate across docs and prompts.

## More Information

This ADR refines ADR 0064 for Markdown list indentation only. It does not change the unified validation toolchain, Markdown table rules, trailing whitespace rules, heading spacing rules, or Kotlin and Gradle formatting rules.

Companion implementation plan: `PLAN-markdown-list-indent-two-spaces`.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date.
