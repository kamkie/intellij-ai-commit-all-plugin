# Target Current IntelliJ Platform And All JetBrains IDEs

Status: Accepted

Date: 2026-05-14

## Context

The minimum supported IntelliJ Platform version and target IDE family were open questions.

The user answered `Q-SCOPE-1` as `current` and `Q-SCOPE-2` as `all`.

As of 2026-05-14, JetBrains identifies IntelliJ IDEA 2026.1 as the current release line in its official release overview. JetBrains has also published a 2026.1.1 patch announcement for that line.

Sources:

- https://www.jetbrains.com/idea/whatsnew/
- https://blog.jetbrains.com/idea/2026/04/intellij-idea-2026-1-1/

## Decision

Use IntelliJ Platform 2026.1 as the minimum supported release line.

When Gradle scaffolding exists, use the latest available 2026.1 patch release for `runIde`, build validation, and plugin verifier inputs unless a later ADR pins an exact patch version.

Target all JetBrains IDEs that expose the VCS Commit tool window and compatible IntelliJ Platform VCS commit workflow APIs.

## Consequences

- The initial plugin scaffold should target the 2026.1 IntelliJ Platform line.
- Implementation should avoid IntelliJ IDEA-only assumptions unless an API or behavior is not available across supported JetBrains IDEs.
- Plugin metadata and user-facing docs should describe support as JetBrains IDEs with the VCS Commit tool window, not IntelliJ IDEA only.
- Compatibility validation still needs concrete IDE products and patch versions selected through validation planning.

## Alternatives Considered

- Support older IntelliJ Platform versions.
  - Why it was not chosen: the user chose the current release line as the support baseline.
- Target IntelliJ IDEA only.
  - Why it was not chosen: the user chose all JetBrains IDEs with the relevant commit workflow.

## Follow-Up

- Remove `Q-SCOPE-1` and `Q-SCOPE-2` from `OPEN_QUESTIONS.md`.
- Remove `Q-SCOPE-1` and `Q-SCOPE-2` dependency markers from `TASKS.md`.
- Update README and AI collaboration docs with the accepted target.
