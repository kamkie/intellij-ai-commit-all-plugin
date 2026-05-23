# Requested Features

A curated inventory of plugin features that originate from an explicit user request, user-answered open question, or accepted maintainer decision. Each entry links to the Architecture Decision Record (ADR) that captured the request.

This document is **not** a behavior specification. For the full behavior contract including emerged implementation details, error handling, and validation requirements, see [docs/specification.md](specification.md).

## Scope

Included: ADRs where the user explicitly requested or chose a feature, behavior, or constraint — usually captured by phrases such as "the user selected", "the user chose", "the user answered `Q-…`", or an accepted entry from a maintainer-owned proposal.

Excluded:

- Repository workflow and governance ADRs (ADR template, commit-message format, plan/proposal refs, orchestrator workflow, formatting toolchain, etc.). They describe how the project is run, not what the plugin does.
- Emerged implementation behaviors that exist in the code and validation suite but were not separately requested. Spec REQs covering `VcsFrozen`, `EmptySelection`, and `UnchangedMessage` stops fall in this category — they are platform-level preconditions or implementation safety nets rather than explicit feature asks.

## Legend

- **Shipped** — implemented in the current prerelease (`v0.1.0-alpha.9` line).
- **Planned** — accepted in an ADR but tracked work remains. See [TASKS.md](../TASKS.md).
- **Superseded** — replaced by a later ADR; kept for traceability.

## UI and Control

| Feature                                                                                        | ADR                                                                                          | Status                 | Resolved |
|------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|------------------------|----------|
| Three-section toolbar control (`AI`, `Commit`, `Push`) with cumulative behavior                | [ADR-0052](decisions/adr-0052-use-three-section-ai-commit-push-control.md)                   | Shipped                | —        |
| Violet AI Snake compact style as the final three-section styling                               | [ADR-0053](decisions/adr-0053-select-violet-ai-snake-three-section-control-style.md)         | Shipped                | —        |
| Replace the standard `Commit and Push...` toolbar action with the plugin three-section control | [ADR-0070](decisions/adr-0070-replace-standard-commit-and-push-toolbar-action.md)            | Shipped                | —        |
| Canonical product and action labels (`AI Commit All`, three-section section labels)            | [ADR-0005](decisions/adr-0005-canonical-product-and-action-labels.md)                        | Shipped                | —        |
| AI-generated, IntelliJ-style icon bases                                                        | [ADR-0015](decisions/adr-0015-use-ai-generated-intellij-style-icon-bases.md)                 | Shipped                | Q-UX-2   |
| Iterative styling drafts to choose from before final selection                                 | [ADR-0025](decisions/adr-0025-create-split-button-styling-drafts.md)                         | Shipped                | Q-UX-5   |
| Reuse standard IntelliJ error messages; do not invent custom text where the platform has one   | [ADR-0016](decisions/adr-0016-reuse-standard-intellij-error-messages.md)                     | Shipped                | Q-UX-3   |
| Use standard IDE confirmation barriers; no plugin-specific custom confirmation dialogs         | [ADR-0017](decisions/adr-0017-use-standard-ide-confirmation-barriers.md)                     | Shipped                | Q-UX-4   |
| Split button with `AI Commit All` + `& Push` segments                                          | [ADR-0006](decisions/adr-0006-use-split-button-for-commit-and-push.md)                       | Superseded by ADR-0052 | —        |
| Placeholder graphic as temporary styling reference                                             | [ADR-0027](decisions/adr-0027-use-generated-placeholder-graphic-for-split-button-styling.md) | Superseded by ADR-0053 | Q-UX-6   |

## Commit Behavior

| Feature                                                                                         | ADR                                                                                     | Status  | Resolved   |
|-------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|---------|------------|
| "All files" commit scope = every non-ignored, committable change in the project                 | [ADR-0003](decisions/adr-0003-define-all-files-commit-scope.md)                         | Shipped | —          |
| Auto-commit after AI message generation (no extra confirmation step)                            | [ADR-0010](decisions/adr-0010-auto-commit-after-ai-generation.md)                       | Shipped | Q-COMMIT-2 |
| Stop the workflow when the user edits the commit message while AI generation is running         | [ADR-0011](decisions/adr-0011-stop-when-user-edits-message-during-ai-generation.md)     | Shipped | Q-COMMIT-3 |
| On runtime AI failure: stop without committing, animate the control, show standard notification | [ADR-0014](decisions/adr-0014-stop-on-runtime-ai-failure-with-standard-notification.md) | Shipped | Q-AI-4     |

## Push Behavior

| Feature                                                                                                                   | ADR                                                                                   | Status  | Resolved |
|---------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|---------|----------|
| Safe immediate-push fast path on the push section; fall back to the standard IDE dialog when state is unsafe              | [ADR-0047](decisions/adr-0047-use-safe-immediate-push-fallback.md)                    | Shipped | —        |
| For outgoing-only `Push`, stop instead of opening the IDE Push Commits dialog when safe immediate push cannot be prepared | [ADR-0069](decisions/adr-0069-stop-outgoing-only-push-without-ide-dialog-fallback.md) | Shipped | —        |

## AI Integration

| Feature                                                                                                                       | ADR                                                                              | Status  | Resolved               |
|-------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|---------|------------------------|
| Detect AI generation completion via an explicit AI-action signal, with a supporting check interval and a configurable timeout | [ADR-0012](decisions/adr-0012-detect-ai-completion-with-configurable-timeout.md) | Shipped | Q-AI-1, Q-AI-2, Q-AI-5 |
| 30 second default for the configurable AI generation timeout (narrows ADR-0012's 5 second default)                            | [ADR-0068](decisions/adr-0068-use-30-second-default-ai-generation-timeout.md)    | Shipped | —                      |
| Require JetBrains AI Assistant as a hard plugin dependency; fail install if missing                                           | [ADR-0013](decisions/adr-0013-require-jetbrains-ai-assistant-plugin.md)          | Shipped | Q-AI-3                 |

## Settings and Keymap

| Feature                                                                              | ADR                                                                                | Status  | Resolved       |
|--------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|---------|----------------|
| Configurable AI generation timeout and completion-check interval                     | [ADR-0012](decisions/adr-0012-detect-ai-completion-with-configurable-timeout.md)   | Shipped | Q-AI-1, Q-AI-2 |
| Take over standard IDE commit and push shortcuts by default, with a Settings opt-out | [ADR-0054](decisions/adr-0054-use-vcs-shortcuts-for-ai-commit-all-with-opt-out.md) | Shipped | —              |

## Scope and Compatibility

| Feature                                                                                  | ADR                                                                                   | Status  | Resolved             |
|------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|---------|----------------------|
| Target the current IntelliJ Platform release line and all JetBrains IDEs                 | [ADR-0008](decisions/adr-0008-target-current-intellij-platform-all-jetbrains-ides.md) | Shipped | Q-SCOPE-1, Q-SCOPE-2 |
| Support Git only (for the first implementation), including multi-root repositories       | [ADR-0009](decisions/adr-0009-support-git-only-with-multiple-roots.md)                | Shipped | Q-SCOPE-3, Q-SCOPE-4 |
| Validate against current IDE products; support both changelists and the Git staging area | [ADR-0020](decisions/adr-0020-validate-current-products-changelists-and-staging.md)   | Shipped | Q-VAL-1, Q-VAL-2     |
| Provide local-repository end-to-end tests where possible                                 | [ADR-0021](decisions/adr-0021-use-local-repository-end-to-end-tests.md)               | Shipped | Q-VAL-3              |

## Identity, Licensing, Distribution

| Feature                                                                              | ADR                                                                                   | Status  | Resolved |
|--------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|---------|----------|
| Business plugin identity (`pl.devopssolutions.aicommitall`, DevOps Solutions vendor) | [ADR-0022](decisions/adr-0022-use-business-plugin-identity.md)                        | Shipped | Q-META-1 |
| License the plugin under Apache-2.0                                                  | [ADR-0018](decisions/adr-0018-use-apache-2-license.md)                                | Shipped | Q-META-2 |
| Publish as open source to the official JetBrains Marketplace                         | [ADR-0019](decisions/adr-0019-publish-open-source-plugin-to-jetbrains-marketplace.md) | Planned | Q-META-3 |

## Planned Followups Without a Standalone Feature ADR

The following backlog items refine or document already-accepted features rather than introducing new ones. They live in [TASKS.md](../TASKS.md):

- **T-DOC-018..023** — Marketplace description, change-notes alignment, screenshots/animation of the control, Settings reference, troubleshooting/FAQ, and keymap documentation.
- **T-VAL-024** — Manual sandbox release-matrix execution before Marketplace publication.
- **T-TEST-003..009** — Raise JaCoCo coverage and the verification floor in `build.gradle.kts`.
- **T-DETEKT-001..008** — Empty the Detekt baseline by retiring 118 suppressed findings.

## What This Document Does Not Cover

- Repository workflow and ADR-process decisions (commit-message template, plan/proposal refs, orchestrator rules, formatting toolchain). They are valid maintainer decisions but do not describe plugin features.
- Emerged implementation behaviors that exist in code and the spec but were not separately requested. Examples currently include `REQ-SEL-009` (`VcsFrozen`), `REQ-SEL-011` (`EmptySelection`), and `REQ-AI-013` (`UnchangedMessage`) — the first two are IntelliJ Platform preconditions, and the third is an implementation safety net for "AI Assistant completed without producing new output".
- Internal task refs and validation scenario refs. These live in [TASKS.md](../TASKS.md), [TASKS_ARCHIVE.md](../TASKS_ARCHIVE.md), and [docs/scenario-coverage.md](scenario-coverage.md).

For the registry of all ADRs including governance and supersession history, see [docs/decisions/README.md](decisions/README.md).
