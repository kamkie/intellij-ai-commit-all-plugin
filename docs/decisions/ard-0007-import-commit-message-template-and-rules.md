---
status: accepted
date: 2026-05-14
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Import Commit Message Template And Rules

## Context and Problem Statement

The user asked to import and adjust `.gitmessage` and commit rules from `https://github.com/kamkie/technical-interview-demo`.

The source repository uses Conventional Commits plus an AI-created commit metadata trailer block. This repository has a different scope: it is an IntelliJ Platform plugin repository that is still unscaffolded and already has a rule to commit only when the user asks or task scope explicitly requires it.

## Decision Drivers

* Preserve the repository context and constraints described in this decision.
* Keep future implementation and documentation aligned with the accepted direction.
* Make the decision easy to review, reference, and validate later.

## Considered Options

* Adopt Import Commit Message Template And Rules
* Import the source `.gitmessage` verbatim.
* Import the source execution guide's automatic commit requirement.

## Decision Outcome

Chosen option: "Adopt Import Commit Message Template And Rules", because Add a repository-local `.gitmessage` template adapted from the source repository.

Add a repository-local `.gitmessage` template adapted from the source repository.

Use Conventional Commits 1.0.0 with repository-specific type descriptions and AI metadata footers for AI-created commits.

Keep `.gitmessage` as the authoritative commit-message template and example source. Keep `.agents/references/execution.md` as the AI-facing owner for when and how agents should apply the template.

Preserve this repository's existing commit rule: agents should not create commits unless the user asks for a commit or the task scope explicitly requires one.

### Consequences

- Commit messages have a durable template before plugin scaffolding starts.
- AI-created commits have consistent source, prompt/task, co-author, reference, and validation metadata.
- The imported rule set is scoped to this repository and does not import source-repository Spring, API, release, or operations assumptions.
- Contributors can opt in locally with `git config commit.template .gitmessage`.

### Confirmation

Compliance is checked through documentation review and `scripts/validate-docs.ps1` when the affected guidance or artifacts change.

## Pros and Cons of the Options

### Adopt Import Commit Message Template And Rules

* Good, because Add a repository-local `.gitmessage` template adapted from the source repository.
* Good, because it preserves a durable repository decision for future implementation and review.
* Bad, because future changes must update or supersede this decision when project direction changes.

### Import the source `.gitmessage` verbatim.

* Bad, because it references source-repository roadmap and plan naming conventions that do not fit this repository.

### Import the source execution guide's automatic commit requirement.

* Bad, because this repository explicitly avoids committing unless the user asks or task scope requires it.

## More Information

- Update `.agents/references/execution.md` with AI commit-message rules.
- Point `AGENTS.md` and `docs/WORKING_WITH_AI.md` to `.gitmessage` for commit requests.
