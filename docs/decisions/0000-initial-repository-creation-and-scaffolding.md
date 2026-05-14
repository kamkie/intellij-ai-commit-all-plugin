# Initial Repository Creation And Scaffolding

Status: Accepted

Date: 2026-05-14

## Context

The repository was created for an IntelliJ Platform plugin concept named `AI Commit All Files`.

At creation time, key implementation choices were still unresolved, including minimum supported IntelliJ Platform version, target IDEs, Git-only versus broader VCS support, and commit-and-push behavior.

Starting with a full Gradle/Kotlin IntelliJ plugin scaffold before those choices are settled would risk encoding assumptions into build configuration, package names, plugin descriptor metadata, and validation expectations.

## Decision

Initialize the repository with lightweight project documentation, backlog, open questions, AI-agent guidance, planning structure, and ADR structure.

Do not add the Gradle/Kotlin IntelliJ plugin scaffold as part of initial repository creation.

Defer executable plugin scaffolding until the required scope and target decisions are answered or explicitly assumed in a later ADR or accepted plan.

## Consequences

- The repository can capture intent, tasks, missing input, workflow rules, and decisions before implementation starts.
- Future scaffolding work can use settled compatibility and behavior decisions instead of guessing.
- There is no buildable plugin yet, so build and sandbox validation are not available until scaffold files are added.
- Documentation must avoid implying that plugin implementation has started.

## Alternatives Considered

- Scaffold the Gradle/Kotlin IntelliJ plugin project immediately.
  - Why it was not chosen: unresolved compatibility and behavior choices could force avoidable rework.
- Keep only a README without backlog, questions, or AI guidance.
  - Why it was not chosen: the project needs durable task tracking and guidance for future AI-assisted implementation.

## Follow-Up

- Resolve the missing input tracked in `OPEN_QUESTIONS.md`.
- Record accepted project decisions in `docs/decisions/`.
- Add the Gradle/Kotlin IntelliJ plugin scaffold through a later task, plan, or ADR-backed implementation.
