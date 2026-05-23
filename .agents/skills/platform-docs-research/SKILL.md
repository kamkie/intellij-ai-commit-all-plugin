---
name: platform-docs-research
description: Documentation-backed platform and API research for this IntelliJ plugin repository. Use when verifying version-sensitive behavior, defaults, compatibility, deprecations, or API contracts for IntelliJ Platform SDK, Gradle IntelliJ Platform Plugin, Kotlin, JUnit, JetBrains AI Assistant integration assumptions, Codex/OpenAI products, or other primary-source framework facts before implementation, review, ADR, plan, or documentation work.
---

# Platform Docs Research

## Start

- Read `AGENTS.md` if it is not already loaded.
- Identify the exact behavior, API, option, or compatibility question.
- State the target version or product scope before researching. Use "current as of today" only after checking current primary documentation.
- Check local governing artifacts first when the question affects repository policy: ADRs, approved plans, `README.md`, `docs/SUPPORT.md`, and `.agents/references/`.
- Use primary sources first: JetBrains IntelliJ Platform SDK docs, Gradle IntelliJ Platform Plugin docs, Kotlin docs, JUnit docs, OpenAI docs, official release notes, or source/API references.
- Treat source code as documentation of actual behavior and the final authority. When prose docs and source code disagree, the source code is right for what the system currently does.
- For OpenAI product or API questions, use the `openai-docs` skill and official OpenAI sources only unless the user explicitly asks otherwise.

## Research

- Map the docs question to the repository behavior it would affect: Gradle config, plugin descriptor, actions, services, VCS workflow, AI Assistant invocation, tests, docs, ADR, or release policy.
- Prefer exact documentation sections over broad search results.
- Capture defaults, required configuration, lifecycle constraints, threading rules, deprecations, compatibility notes, and documented failure modes.
- Separate documented facts from inferences. Mark inferences explicitly and explain the source evidence that supports them.
- If sources conflict, rank official versioned docs and release notes above secondary articles, examples, or old answers.
- Do not change files during research unless the user explicitly asks for implementation.

## Return

- Answer the specific question first.
- Include source references for high-impact claims when web research was used.
- Name the versions, dates, or product builds covered by the answer.
- Call out ambiguity, missing docs, or behavior that still requires runtime validation.
- Recommend the smallest next validation step, such as a targeted unit test, `buildPlugin`, `verifyPlugin`, or sandbox `runIde` check.
