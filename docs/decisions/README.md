# Decision Records

Use this directory for project decisions, repository rule changes, and open questions that future work should preserve.

Numbered Markdown files are ADRs. `OPEN_QUESTIONS.md` tracks unresolved user input and is not an ADR.

Every project decision must be recorded as an ADR before or alongside the implementation it affects.

Every repository rule change must be recorded as a new ADR or as an update that supersedes an existing ADR before or alongside the rule edit.

Routine task execution notes do not need ADRs unless they choose or change project direction, repository rules, compatibility, user behavior, validation expectations, or future maintenance policy.

## When To Add A Decision

Required ADR topics include:

- Minimum supported IntelliJ Platform version.
- Supported IDE family or Git-only versus broader VCS support.
- Runtime-discovered AI Assistant action versus direct dependency on AI Assistant APIs.
- Commit/push presentation, such as split-button design versus separate commit-and-push action.
- Compatibility policy for IntelliJ Platform API changes.
- Repository rule or workflow changes.

## Status Values

- Proposed: under discussion.
- Accepted: current guidance.
- Superseded: replaced by a newer decision.
- Rejected: intentionally not chosen.

## Naming

Use a short numbered filename:

```text
0000-initial-repository-creation-and-scaffolding.md
0001-import-lightweight-ai-guidance-model.md
0002-record-rule-changes-and-project-decisions.md
```

Start from `ADR_TEMPLATE.md`.

## ADR Index

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [0000](0000-initial-repository-creation-and-scaffolding.md) | Initial Repository Creation And Scaffolding | Accepted | 2026-05-14 |
| [0001](0001-import-lightweight-ai-guidance-model.md) | Import Lightweight AI Guidance Model | Accepted | 2026-05-14 |
| [0002](0002-record-rule-changes-and-project-decisions.md) | Record Rule Changes And Project Decisions | Accepted | 2026-05-14 |
| [0003](0003-define-all-files-commit-scope.md) | Define All Files Commit Scope | Accepted | 2026-05-14 |
| [0004](0004-mark-task-dependencies-on-open-questions.md) | Mark Task Dependencies On Open Questions | Accepted | 2026-05-14 |
| [0005](0005-canonical-product-and-action-labels.md) | Canonical Product And Action Labels | Accepted | 2026-05-14 |
| [0006](0006-use-split-button-for-commit-and-push.md) | Use Split Button For Commit And Push | Accepted | 2026-05-14 |
| [0007](0007-import-commit-message-template-and-rules.md) | Import Commit Message Template And Rules | Accepted | 2026-05-14 |
| [0008](0008-target-current-intellij-platform-all-jetbrains-ides.md) | Target Current IntelliJ Platform And All JetBrains IDEs | Accepted | 2026-05-14 |
| [0009](0009-support-git-only-with-multiple-roots.md) | Support Git Only With Multiple Roots | Accepted | 2026-05-14 |
| [0010](0010-auto-commit-after-ai-generation.md) | Auto Commit After AI Generation | Accepted | 2026-05-14 |
| [0011](0011-stop-when-user-edits-message-during-ai-generation.md) | Stop When User Edits Message During AI Generation | Accepted | 2026-05-14 |
| [0012](0012-detect-ai-completion-with-configurable-timeout.md) | Detect AI Completion With Configurable Timeout | Accepted | 2026-05-14 |
| [0013](0013-require-jetbrains-ai-assistant-plugin.md) | Require JetBrains AI Assistant Plugin | Accepted | 2026-05-14 |
| [0014](0014-stop-on-runtime-ai-failure-with-standard-notification.md) | Stop On Runtime AI Failure With Standard Notification | Accepted | 2026-05-15 |
| [0015](0015-use-ai-generated-intellij-style-icon-bases.md) | Use AI-Generated IntelliJ-Style Icon Bases | Accepted | 2026-05-15 |
| [0016](0016-reuse-standard-intellij-error-messages.md) | Reuse Standard IntelliJ Error Messages | Accepted | 2026-05-15 |
| [0017](0017-use-standard-ide-confirmation-barriers.md) | Use Standard IDE Confirmation Barriers | Accepted | 2026-05-15 |
| [0018](0018-use-apache-2-license.md) | Use Apache-2.0 License | Accepted | 2026-05-15 |
| [0019](0019-publish-open-source-plugin-to-jetbrains-marketplace.md) | Publish Open-Source Plugin To JetBrains Marketplace | Accepted | 2026-05-15 |
| [0020](0020-validate-current-products-changelists-and-staging.md) | Validate Current Products, Changelists, And Staging | Accepted | 2026-05-15 |
| [0021](0021-use-local-repository-end-to-end-tests.md) | Use Local Repository End-To-End Tests | Accepted | 2026-05-15 |
| [0022](0022-use-business-plugin-identity.md) | Use Business Plugin Identity | Accepted | 2026-05-15 |
| [0023](0023-commit-each-task-in-multi-task-plans.md) | Commit Each Task In Multi-Task Plans | Accepted | 2026-05-15 |
| [0024](0024-resolve-plan-questions-before-implementation.md) | Resolve Plan Questions Before Implementation | Accepted | 2026-05-15 |
| [0025](0025-create-split-button-styling-drafts.md) | Create Split-Button Styling Drafts | Accepted | 2026-05-15 |
| [0026](0026-use-orchestrator-and-fresh-task-workers-for-plans.md) | Use Orchestrator And Fresh Task Workers For Plans | Accepted | 2026-05-15 |
| [0027](0027-use-generated-placeholder-graphic-for-split-button-styling.md) | Use Generated Placeholder Graphic For Split-Button Styling | Accepted | 2026-05-15 |
| [0028](0028-use-stable-task-ids.md) | Use Stable Task IDs | Accepted | 2026-05-15 |
| [0029](0029-add-changelog-support-and-release-guidance.md) | Add Changelog, Support, And Release Guidance | Accepted | 2026-05-15 |
| [0030](0030-orchestrator-maintains-changelog.md) | Orchestrator Maintains Changelog | Accepted | 2026-05-15 |
| [0031](0031-do-not-load-all-ai-instruction-files-automatically.md) | Do Not Load All AI Instruction Files Automatically | Accepted | 2026-05-15 |
| [0032](0032-use-stable-human-readable-plan-ids.md) | Use Stable Human-Readable Plan IDs | Accepted | 2026-05-15 |
| [0033](0033-add-proposals-directory-and-rules.md) | Add Proposals Directory And Rules | Accepted | 2026-05-15 |
| [0034](0034-use-stable-proposal-ids.md) | Use Stable Proposal IDs | Accepted | 2026-05-15 |
| [0035](0035-store-open-questions-with-decisions.md) | Store Open Questions With Decisions | Accepted | 2026-05-15 |
| [0036](0036-include-plan-ids-in-plan-filenames.md) | Include Plan IDs In Plan Filenames | Accepted | 2026-05-15 |
| [0037](0037-use-compact-plan-status-lifecycle.md) | Use Compact Plan Status Lifecycle | Accepted | 2026-05-15 |
