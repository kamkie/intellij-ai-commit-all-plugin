---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Initial Repository Creation And Scaffolding

## Context and Problem Statement

The repository was created for an IntelliJ Platform plugin concept initially named `AI Commit All Files`.

At creation time, key implementation choices were still unresolved, including minimum supported IntelliJ Platform version, target IDEs, Git-only versus broader VCS support, and commit-and-push behavior.

Starting with a full Gradle/Kotlin IntelliJ plugin scaffold before those choices are settled would risk encoding assumptions into build configuration, package names, plugin descriptor metadata, and validation expectations.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Initial Repository Creation And Scaffolding
* Scaffold the Gradle/Kotlin IntelliJ plugin project immediately.
* Keep only a README without backlog, questions, or AI guidance.

## Decision Outcome

Chosen option: "Adopt Initial Repository Creation And Scaffolding", because Initialize the repository with lightweight project documentation, backlog, open questions, AI-agent guidance, planning structure, and ADR structure.

Initialize the repository with lightweight project documentation, backlog, open questions, AI-agent guidance, planning structure, and ADR structure.

Do not add the Gradle/Kotlin IntelliJ plugin scaffold as part of initial repository creation.

Defer executable plugin scaffolding until the required scope and target decisions are answered or explicitly assumed in a later ADR or accepted plan.

### Consequences

- The repository can capture intent, tasks, missing input, workflow rules, and decisions before implementation starts.
- Future scaffolding work can use settled compatibility and behavior decisions instead of guessing.
- There is no buildable plugin yet, so build and sandbox validation are not available until scaffold files are added.
- Documentation must avoid implying that plugin implementation has started.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Initial Repository Creation And Scaffolding

* Good, because Initialize the repository with lightweight project documentation, backlog, open questions, AI-agent guidance, planning structure, and ADR structure.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Scaffold the Gradle/Kotlin IntelliJ plugin project immediately.

* Bad, because unresolved compatibility and behavior choices could force avoidable rework.

### Keep only a README without backlog, questions, or AI guidance.

* Bad, because the project needs durable task tracking and guidance for future AI-assisted implementation.

## More Information

- Resolve the missing input tracked in `docs/decisions/OPEN_QUESTIONS.md`.
- Record accepted project decisions in `docs/decisions/`.
- Add the Gradle/Kotlin IntelliJ plugin scaffold through a later task, plan, or ADR-backed implementation.
- See ADR 0005 for the accepted product and action labels.
- See ADR 0006 for the accepted split-button commit and push presentation.
