# Use Business Plugin Identity

Status: Accepted

Date: 2026-05-15

## Context

Plugin ID, package name, and vendor metadata were open as `Q-META-1`.

The user provided identity inputs:

- Name: Kamil Kiewisz.
- Public GitHub profile: https://github.com/kamkie.
- Business: Self-employed at DevOps Solutions Kamil Kiewisz.
- Domains: devopssolutions.pl, devopssolutions.net, kiewisz.com, kiewisz.org, kiewisz.net, kiewisz.pl.
- Vendor contact email: kontakt@devopssolutions.pl.

Relevant JetBrains sources:

- https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html
- https://plugins.jetbrains.com/docs/marketplace/jetbrains-marketplace-approval-guidelines.html
- https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html
- https://plugins.jetbrains.com/docs/marketplace/verified-vendor-badge.html

Key constraints found:

- Plugin ID should be a fully qualified name similar to Java packages and must not collide with existing plugin IDs.
- Plugin name is user-visible, should be concise, original, no more than 30 characters, and should not contain `Plugin`, `IntelliJ`, `JetBrains`, or JetBrains product names.
- Vendor metadata should provide a valid, functional website and email address.
- Verified vendor status for a free plugin requires a registered business entity and an active email address under a domain associated with that entity.

## Decision

Use this project identity:

- Plugin display name: `AI Commit All`.
- Plugin XML ID: `pl.devopssolutions.aicommitall`.
- Base Kotlin package: `pl.devopssolutions.aicommitall`.
- Gradle root project name: `ai-commit-all`.
- Marketplace vendor or organization name: `DevOps Solutions Kamil Kiewisz`.
- Vendor URL: `https://devopssolutions.pl`.
- Vendor contact email: `kontakt@devopssolutions.pl`.
- Source code URL: `https://github.com/kamkie/intellij-ai-commit-all-plugin`.
- License: `Apache-2.0` per ADR 0018.

Use the business domain namespace instead of a personal or GitHub namespace. The business namespace better supports Marketplace vendor verification and long-term ownership than `io.github.kamkie`.

## Consequences

- The plugin ID and package are stable, lowercase, reverse-DNS-style, and tied to a domain the user owns.
- The plugin display name stays aligned with ADR 0005 and Marketplace name guidance.
- The vendor identity is professional and can be backed by business-domain ownership.
- Plugin IDs and packages are expensive to change after publication, so future changes require a superseding ADR.
- Vendor contact metadata can be included in plugin and Marketplace metadata.

## Alternatives Considered

- Personal namespace: `com.kiewisz.aicommitall` or `pl.kiewisz.aicommitall`, vendor `Kamil Kiewisz`.
  - Why it was not chosen as the primary proposal: it is valid, but less aligned with the user's business and possible Marketplace vendor verification.
- GitHub namespace: `io.github.kamkie.aicommitall`, vendor `Kamil Kiewisz`.
  - Why it was not chosen as the primary proposal: it is common for open-source projects, but weaker as a business and Marketplace identity.
- Longer plugin ID: `pl.devopssolutions.intellij.aicommitall`.
  - Why it was not chosen as the primary proposal: the plugin already targets IntelliJ Platform, and avoiding `intellij` in identity metadata reduces trademark and naming noise.
- Dashed plugin ID: `pl.devopssolutions.ai-commit-all`.
  - Why it was not chosen as the primary proposal: JetBrains guidance says plugin IDs should be similar to Java packages, so a lowercase package-like ID is safer.

## Follow-Up

- Remove `Q-META-1` from `docs/decisions/OPEN_QUESTIONS.md`.
- Update `TASKS.md` dependencies and implementation guidance.
- Use this identity when scaffolding Gradle, Kotlin packages, `plugin.xml`, and Marketplace metadata.
