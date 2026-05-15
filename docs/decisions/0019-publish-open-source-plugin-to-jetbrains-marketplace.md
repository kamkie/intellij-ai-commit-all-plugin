# Publish Open-Source Plugin To JetBrains Marketplace

Status: Accepted

Date: 2026-05-15

## Context

Publishing, signing, marketplace metadata, and CI scope was open as `Q-META-3`.

The user answered that publishing will be added and that the project is planned as an open-source plugin published to the official JetBrains Marketplace.

Relevant JetBrains sources:

- https://plugins.jetbrains.com/docs/marketplace/uploading-a-new-plugin.html
- https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html
- https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html
- https://plugins.jetbrains.com/docs/intellij/plugin-signing.html
- https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html

## Decision

Official JetBrains Marketplace publication is in scope for this project.

The repository should include publication-ready infrastructure once the plugin scaffold exists:

- Marketplace metadata needed for an open-source plugin listing.
- Source code link in Marketplace metadata.
- Plugin signing configuration.
- `publishPlugin` configuration for official JetBrains Marketplace publication.
- CI for build, verification, and plugin packaging.
- A gated/manual release path for signing and publishing using repository secrets.

Secrets must not be committed. Marketplace tokens, signing certificates, private keys, and passwords must be supplied through local Gradle properties or CI secrets.

The first Marketplace upload is expected to be manual when JetBrains requires initial plugin setup. Later uploads should be automated through Gradle and CI where practical.

## Consequences

- Publishing, signing, Marketplace metadata, and CI tasks are part of the implementation backlog.
- Build scaffolding should use IntelliJ Platform Gradle Plugin 2.x unless later compatibility work proves otherwise.
- Release automation depends on the plugin ID, vendor/organization metadata, license choice, target IDE versions, and Marketplace credentials.
- CI must be designed so pull-request validation can run without publishing secrets.
- Open-source publication increases the importance of a clear license, source code link, README, changelog/release notes, and reproducible validation.

## Alternatives Considered

- Defer publishing, signing, Marketplace metadata, and CI.
  - Why it was not chosen: the user plans official Marketplace publication.
- Publish only a local ZIP artifact.
  - Why it was not chosen: the target distribution channel is the official JetBrains Marketplace.
- Add paid-plugin licensing checks.
  - Why it was not chosen: the project direction is open-source Marketplace publication, not a paid plugin.

## Follow-Up

- Remove `Q-META-3` from `docs/decisions/OPEN_QUESTIONS.md`.
- Add publishing, signing, Marketplace metadata, and CI tasks to `TASKS.md`.
- See ADR 0022 for plugin ID, package name, vendor name, and vendor contact metadata.
- See ADR 0018 for the accepted Apache-2.0 license decision.
