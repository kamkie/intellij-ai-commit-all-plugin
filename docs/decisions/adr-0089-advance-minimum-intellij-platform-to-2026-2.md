---
status: accepted
date: 2026-07-16
accepted_at: 2026-07-16T21:17:58+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Advance Minimum IntelliJ Platform To 2026.2 Before Full Product Availability

## Context and Problem Statement

ADR 0008 sets IntelliJ Platform 2026.1 as the minimum supported release line and requires representative compatibility validation across IntelliJ IDEA, PyCharm, and WebStorm. IntelliJ IDEA 2026.2 and WebStorm 2026.2 were released on 2026-07-16, but JetBrains still reports PyCharm 2026.1.4 as the latest stable PyCharm release.

A direct build-SDK probe against IntelliJ IDEA 2026.2 build `262.8665.258` proved that this is a compatibility migration rather than a property-only update. Platform branch 262 requires Java 25, exposes VCS and DVCS implementation classes through more specific modules, and makes `GitStageCommitWorkflowHandler` Kotlin-internal. The existing plugin therefore does not compile after changing only the platform, since-build, and AI Assistant versions.

Should the plugin advance its minimum platform to 2026.2 immediately, even though the required PyCharm 2026.2 validation target is not yet published?

Sources:

- https://data.services.jetbrains.com/products/releases?code=IIU,PCP,WS&latest=true&type=release
- https://blog.jetbrains.com/idea/2026/07/intellij-idea-2026-2/
- https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html
- https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
- https://github.com/JetBrains/intellij-platform-gradle-plugin/blob/2.18.1/src/main/kotlin/org/jetbrains/intellij/platform/gradle/utils/PlatformJavaVersions.kt
- https://plugins.jetbrains.com/plugin/22282-jetbrains-ai-assistant/versions/stable/1108976

## Decision Drivers

* The maintainer wants the 2026.2 upgrade work available in a pull request immediately rather than waiting for every JetBrains product release.
* The repository must not claim that a missing PyCharm validation passed or suppress that required check.
* IntelliJ Platform Gradle Plugin rejects a 262 build SDK paired with the existing `since-build=261`, so a true SDK bump also changes the minimum supported platform.
* Platform 262 requires Java 25 and includes compatibility-sensitive VCS and Commit API changes that must be migrated and tested together.
* The all-JetBrains-IDE product direction from ADR 0008 remains desired even while one representative product is temporarily unavailable.

## Considered Options

* Advance to 2026.2 now and keep the pull request draft until PyCharm 2026.2 validation passes
* Wait for PyCharm 2026.2 before starting the upgrade
* Keep the 2026.1 build SDK and add forward compatibility checks for available 2026.2 products

## Decision Outcome

Chosen option: "Advance to 2026.2 now and keep the pull request draft until PyCharm 2026.2 validation passes", because it makes the requested migration reviewable immediately without weakening the representative product matrix or publishing an incompletely validated release.

The project will:

* IntelliJ Platform 2026.2, build branch 262, becomes the minimum supported platform and supersedes the 2026.1 minimum from ADR 0008.
* The plugin continues to target all JetBrains IDEs that expose the VCS Commit tool window and compatible Git commit workflow APIs.
* The build uses IntelliJ IDEA 2026.2 and `since-build=262`, keeps `until-build` open, and uses the exact compatible JetBrains AI Assistant dependency `262.8665.258` for the build SDK.
* Java source compatibility, Java target compatibility, Kotlin JVM target, local prerequisites, and CI runtimes move to Java 25. The current IntelliJ Platform Gradle Plugin `2.18.1`, Kotlin `2.4.0`, and Gradle Wrapper `9.6.1` remain unless implementation evidence requires a separate update.
* Platform 262 VCS and DVCS modules are declared explicitly where required, and compile-time use of the now-internal `GitStageCommitWorkflowHandler` is replaced with the smallest fail-closed compatibility boundary that preserves existing staging behavior.
* IntelliJ IDEA, PyCharm, and WebStorm verifier and release-matrix inputs move to 2026.2 together. While PyCharm 2026.2 is unavailable, its dependency-resolution failure is an expected external blocker, not a passing or ignored check.
* The upgrade pull request remains draft and must not be marked ready or merged until PyCharm 2026.2 is published and the full required matrix passes on the current head.
* No 2026.1 compatibility artifact, fallback build, version-specific branch, compatibility shim, or ignored validation failure is added by this decision.

### Consequences

* Good, because the full 2026.2 migration can be implemented and reviewed immediately.
* Good, because the missing PyCharm build remains visible as a real readiness blocker instead of being hidden by conditional CI behavior.
* Good, because the Java 25 and VCS modularization changes are treated as first-class compatibility work.
* Bad, because the pull request is intentionally unable to reach merge readiness until JetBrains publishes PyCharm 2026.2.
* Bad, because users of 2026.1 IDEs will no longer receive releases built from the new baseline after the upgrade is published.
* Bad, because the migration touches build configuration, compatibility-sensitive Kotlin code, CI, validation, support documentation, and Marketplace metadata.

### Confirmation

Compliance is confirmed when:

* the generated plugin descriptor has `since-build="262"` and no `until-build`;
* the project compiles and packages with Java and Kotlin target 25 against IntelliJ IDEA 2026.2 and AI Assistant `262.8665.258`;
* targeted and full tests cover the migrated Commit and Git staging compatibility boundary;
* Plugin Verifier and release-matrix UI validation pass for available 2026.2 products;
* the PyCharm 2026.2 lane remains required and visibly blocked only by product unavailability, then passes unchanged once JetBrains publishes the product;
* user, support, specification, contributor, Marketplace, and release documentation consistently state the 2026.2 minimum;
* the pull request readiness gate is applied again after the complete current-head matrix passes.

## Pros and Cons of the Options

### Advance to 2026.2 now and keep the pull request draft until PyCharm 2026.2 validation passes

* Good, because implementation and review can proceed without waiting on the external release.
* Good, because the final branch already contains the exact matrix that must pass before merge.
* Bad, because a known-red required check can remain on the draft pull request for an unknown period.

### Wait for PyCharm 2026.2 before starting the upgrade

* Good, because all target products could be validated in one uninterrupted implementation cycle.
* Bad, because no reviewable migration work would exist while waiting.

### Keep the 2026.1 build SDK and add forward compatibility checks for available 2026.2 products

* Good, because 2026.1 users and the current PyCharm release would remain supported.
* Good, because JetBrains recommends building against the lowest supported platform when supporting multiple major versions.
* Bad, because this is forward-compatibility validation rather than the requested SDK bump.
* Bad, because it does not expose or resolve the Java 25 and compile-time 262 compatibility changes.

## More Information

- Supersedes ADR 0008. The all-JetBrains-IDE scope is preserved; the minimum release line and rollout timing change.
- Companion approved plan: `PLAN-intellij-2026-2-sdk-upgrade`.
