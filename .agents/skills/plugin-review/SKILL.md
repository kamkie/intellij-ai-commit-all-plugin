---
name: plugin-review
description: PR-style and architecture-aware review workflow for this IntelliJ plugin repository. Use when the user asks for a review, audit, second pass, risk check, or architectural assessment of plugin code, tests, Gradle/plugin metadata, VCS commit/push workflow, AI Assistant integration, docs, plans, proposals, release changes, or agent-guidance edits.
---

# Plugin Review

## Start

- Treat review work as read-only unless the user explicitly asks for fixes.
- Read `.agents/references/reviews.md`.
- Read `.agents/references/testing.md` when validation coverage or test adequacy is part of the review.
- Read `.agents/references/code-style.md` when reviewing Kotlin, Gradle, IntelliJ Platform APIs, plugin metadata, UI, VCS, or AI Assistant integration.
- For documentation, proposal, ADR, or agent-guidance reviews, read `.agents/references/documentation.md` and the owning artifact guide.

## Review Focus

- Map the changed or affected behavior boundary before listing findings.
- Prioritize confirmed correctness, security, behavior regression, compatibility, and missing-validation risks over style issues.
- Use the plugin-specific risk order from `.agents/references/reviews.md`.
- Check architecture only where it affects real change risk: ownership boundaries, dependency direction, API compatibility, failure isolation, and complexity added for local problems.
- Distinguish evidence from hypotheses. Mark low-confidence concerns as assumptions or questions.

## Output

- Lead with findings ordered by severity, with file and line references where possible.
- For each finding, explain the user-visible or maintainer-visible impact and the smallest practical mitigation.
- Include open questions or assumptions after findings.
- Keep summaries secondary and concise.
- If no issues are found, say that clearly and still report validation gaps or residual risk.
- Avoid style-only commentary unless the user requested a style review.
