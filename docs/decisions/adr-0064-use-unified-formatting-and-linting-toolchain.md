---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T20:27:04+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Repository maintainer
informed: Future maintainers and AI agents
---

# Use Unified Formatting And Linting Toolchain

## Context and Problem Statement

`PROP-03-repository-quality-lifecycle` finding `E003` accepted the need for one repository-wide formatting and linting decision before implementation. The repository currently has prose style guidance and documentation validation, but no shared `.editorconfig`, no mechanical Kotlin or Gradle Kotlin DSL formatter, and no Markdown lint configuration.

The repository needs one source-formatting path and one Markdown linting path that IntelliJ, local Gradle validation, CI, and AI agents can follow without competing tools.

## Decision Drivers

* Keep Kotlin and Gradle Kotlin DSL formatting mechanically enforceable from Gradle.
* Avoid multiple competing Kotlin formatters or independent license-header mechanisms.
* Keep Markdown proposal trackers, tables, and nested lists consistent enough to reduce review noise.
* Provide local check and apply commands that contributors and agents can run before CI.
* Keep IntelliJ setup lightweight and avoid committing broad IDE project metadata unless needed.
* Leave room for Apache-2.0 source header enforcement from `PROP-03` finding `E008`.

## Considered Options

* Spotless with ktlint for Kotlin and Gradle Kotlin DSL, markdownlint for Markdown, and `.editorconfig` for shared editor rules.
* Gradle ktlint plugin, markdownlint for Markdown, and `.editorconfig` for shared editor rules.
* IntelliJ project code style files plus documentation-only linting.

## Decision Outcome

Chosen option: "Spotless with ktlint for Kotlin and Gradle Kotlin DSL, markdownlint for Markdown, and `.editorconfig` for shared editor rules", because it gives the repository a single Gradle-integrated source formatter while leaving Markdown checks to the standard Markdown linting tool requested by `PROP-03`.

When accepted, implementation should:

* Add a root `.editorconfig` with shared charset, final-newline, trailing-whitespace, indentation, line-ending, Kotlin, Gradle Kotlin DSL, Markdown, YAML, and PowerShell rules. Markdown nested lists should use 4 spaces.
* Add Spotless to the Gradle build as the only Kotlin and Gradle Kotlin DSL formatter.
* Configure Spotless to use ktlint rules and provide `spotlessCheck` and `spotlessApply` for local validation and fixes.
* Use Spotless license-header support, or the closest Spotless-supported mechanism, for Apache-2.0 headers on Kotlin source files when implementing `PROP-03` finding `E008`.
* Add markdownlint configuration for Markdown style checks, including table pipe style, column consistency, and blank lines around tables where supported.
* Update `scripts/validate-docs.ps1` or CI so markdownlint runs alongside existing documentation validation.
* Wire `spotlessCheck`, markdownlint, existing documentation validation, Gradle wrapper validation, tests, and plugin packaging into CI according to the implementation plan.
* Update `.agents/references/code-style.md`, `CONTRIBUTING.md` if present, and relevant validation guidance with the chosen local commands.
* Do not add `.idea/codeStyles/` initially; rely on `.editorconfig`, ktlint, Spotless, and IntelliJ's existing support for those conventions.

### Consequences

* Good, because source formatting and license-header enforcement can share one Gradle-integrated tool.
* Good, because CI and local validation can run the same source-formatting checks.
* Good, because Markdown style checks can be added without pretending Markdown is formatted by ktlint.
* Good, because `.editorconfig` gives IntelliJ, other editors, and agents a common baseline.
* Bad, because markdownlint usually requires a Node-based CLI or GitHub Action runner unless a packaged alternative is added.
* Bad, because initial adoption may require a dedicated formatting-only change to avoid mixing mechanical churn with behavior changes.

### Confirmation

Confirm implementation by checking that:

* `./gradlew spotlessCheck` validates Kotlin and Gradle Kotlin DSL formatting.
* `./gradlew spotlessApply` is documented as the source-formatting fix command.
* Markdown linting runs locally through documented validation and in CI.
* `scripts/validate-docs.ps1` still validates documentation structure after markdownlint integration.
* CI fails on source formatting, Markdown linting, existing documentation validation, Gradle wrapper validation, tests, or plugin packaging failures.
* Kotlin source headers are enforced through the selected Spotless path when `E008` is implemented.

## Pros and Cons of the Options

### Spotless With Ktlint, Markdownlint, And EditorConfig

* Good, because Spotless can format Kotlin and Gradle Kotlin DSL from Gradle without adding a second Kotlin formatter.
* Good, because Spotless can also support license-header enforcement for Kotlin source files.
* Good, because markdownlint directly targets the Markdown style drift identified in the proposal.
* Good, because `.editorconfig` is understood by IntelliJ and keeps basic editor behavior consistent.
* Bad, because adding markdownlint means choosing how Node-based tooling is installed or invoked in local and CI validation.

### Gradle Ktlint Plugin, Markdownlint, And EditorConfig

* Good, because it keeps Kotlin formatting close to ktlint.
* Good, because it has a narrow Kotlin-focused surface.
* Bad, because license-header enforcement would likely require another Gradle plugin or custom task.
* Bad, because it is less flexible if future formatting needs include more file types.

### IntelliJ Project Code Style Files Plus Documentation-Only Linting

* Good, because IntelliJ users would see IDE formatting choices directly.
* Good, because it avoids adding a source formatter dependency.
* Bad, because CI and non-IntelliJ contributors would not have a single mechanical source-formatting contract.
* Bad, because it does not provide a clear path for Apache-2.0 source-header enforcement.
* Bad, because committed IDE metadata can drift from Gradle and CI unless another enforcement mechanism exists.

## More Information

This ADR is the prerequisite decision for `PROP-03-repository-quality-lifecycle` finding `E003` and should be used by `E008` when source header enforcement is implemented. The broader repository-quality work still needs an approved implementation plan before changing CI workflows, Gradle build logic, contributor intake, security policy, or task-history location.
