# Decision Records

Use this directory for project decisions, repository rule changes, and open questions that future work should preserve.

`ard-NNNN-<slug>.md` Markdown files are ADRs. `OPEN_QUESTIONS.md` tracks unresolved user input and is not an ADR.

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

Use the MADR `status` front matter field with values such as:

- `proposed`: under discussion.
- `accepted`: current guidance.
- `superseded by ard-NNNN`: replaced by a newer decision.
- `rejected`: intentionally not chosen.
- `deprecated`: retained for history but no longer recommended.

## Naming

Use a four-digit sequence and lowercase slug in this filename shape:

```text
ard-0000-initial-repository-creation-and-scaffolding.md
ard-0001-import-lightweight-ai-guidance-model.md
ard-0002-record-rule-changes-and-project-decisions.md
```

Start from `ADR_TEMPLATE.md`, which follows MADR 4.0.0.

Set `decision-makers` to the configured Git identity in `Name <email>` form.

## ADR Index

| ADR                                                                                | Title                                                      | Status   | Date       |
|------------------------------------------------------------------------------------|------------------------------------------------------------|----------|------------|
| [ard-0000](ard-0000-initial-repository-creation-and-scaffolding.md)                | Initial Repository Creation And Scaffolding                | Accepted | 2026-05-14 |
| [ard-0001](ard-0001-import-lightweight-ai-guidance-model.md)                       | Import Lightweight AI Guidance Model                       | Accepted | 2026-05-14 |
| [ard-0002](ard-0002-record-rule-changes-and-project-decisions.md)                  | Record Rule Changes And Project Decisions                  | Accepted | 2026-05-14 |
| [ard-0003](ard-0003-define-all-files-commit-scope.md)                              | Define All Files Commit Scope                              | Accepted | 2026-05-14 |
| [ard-0004](ard-0004-mark-task-dependencies-on-open-questions.md)                   | Mark Task Dependencies On Open Questions                   | Accepted | 2026-05-14 |
| [ard-0005](ard-0005-canonical-product-and-action-labels.md)                        | Canonical Product And Action Labels                        | Accepted | 2026-05-14 |
| [ard-0006](ard-0006-use-split-button-for-commit-and-push.md)                       | Use Split Button For Commit And Push                       | Accepted | 2026-05-14 |
| [ard-0007](ard-0007-import-commit-message-template-and-rules.md)                   | Import Commit Message Template And Rules                   | Accepted | 2026-05-14 |
| [ard-0008](ard-0008-target-current-intellij-platform-all-jetbrains-ides.md)        | Target Current IntelliJ Platform And All JetBrains IDEs    | Accepted | 2026-05-14 |
| [ard-0009](ard-0009-support-git-only-with-multiple-roots.md)                       | Support Git Only With Multiple Roots                       | Accepted | 2026-05-14 |
| [ard-0010](ard-0010-auto-commit-after-ai-generation.md)                            | Auto Commit After AI Generation                            | Accepted | 2026-05-14 |
| [ard-0011](ard-0011-stop-when-user-edits-message-during-ai-generation.md)          | Stop When User Edits Message During AI Generation          | Accepted | 2026-05-14 |
| [ard-0012](ard-0012-detect-ai-completion-with-configurable-timeout.md)             | Detect AI Completion With Configurable Timeout             | Accepted | 2026-05-14 |
| [ard-0013](ard-0013-require-jetbrains-ai-assistant-plugin.md)                      | Require JetBrains AI Assistant Plugin                      | Accepted | 2026-05-14 |
| [ard-0014](ard-0014-stop-on-runtime-ai-failure-with-standard-notification.md)      | Stop On Runtime AI Failure With Standard Notification      | Accepted | 2026-05-15 |
| [ard-0015](ard-0015-use-ai-generated-intellij-style-icon-bases.md)                 | Use AI-Generated IntelliJ-Style Icon Bases                 | Accepted | 2026-05-15 |
| [ard-0016](ard-0016-reuse-standard-intellij-error-messages.md)                     | Reuse Standard IntelliJ Error Messages                     | Accepted | 2026-05-15 |
| [ard-0017](ard-0017-use-standard-ide-confirmation-barriers.md)                     | Use Standard IDE Confirmation Barriers                     | Accepted | 2026-05-15 |
| [ard-0018](ard-0018-use-apache-2-license.md)                                       | Use Apache-2.0 License                                     | Accepted | 2026-05-15 |
| [ard-0019](ard-0019-publish-open-source-plugin-to-jetbrains-marketplace.md)        | Publish Open-Source Plugin To JetBrains Marketplace        | Accepted | 2026-05-15 |
| [ard-0020](ard-0020-validate-current-products-changelists-and-staging.md)          | Validate Current Products, Changelists, And Staging        | Accepted | 2026-05-15 |
| [ard-0021](ard-0021-use-local-repository-end-to-end-tests.md)                      | Use Local Repository End-To-End Tests                      | Accepted | 2026-05-15 |
| [ard-0022](ard-0022-use-business-plugin-identity.md)                               | Use Business Plugin Identity                               | Accepted | 2026-05-15 |
| [ard-0023](ard-0023-commit-each-task-in-multi-task-plans.md)                       | Commit Each Task In Multi-Task Plans                       | Accepted | 2026-05-15 |
| [ard-0024](ard-0024-resolve-plan-questions-before-implementation.md)               | Resolve Plan Questions Before Implementation               | Accepted | 2026-05-15 |
| [ard-0025](ard-0025-create-split-button-styling-drafts.md)                         | Create Split-Button Styling Drafts                         | Accepted | 2026-05-15 |
| [ard-0026](ard-0026-use-orchestrator-and-fresh-task-workers-for-plans.md)          | Use Orchestrator And Fresh Task Workers For Plans          | Accepted | 2026-05-15 |
| [ard-0027](ard-0027-use-generated-placeholder-graphic-for-split-button-styling.md) | Use Generated Placeholder Graphic For Split-Button Styling | Accepted | 2026-05-15 |
| [ard-0028](ard-0028-use-stable-task-ids.md)                                        | Use Stable Task IDs                                        | Accepted | 2026-05-15 |
| [ard-0029](ard-0029-add-changelog-support-and-release-guidance.md)                 | Add Changelog, Support, And Release Guidance               | Accepted | 2026-05-15 |
| [ard-0030](ard-0030-orchestrator-maintains-changelog.md)                           | Orchestrator Maintains Changelog                           | Accepted | 2026-05-15 |
| [ard-0031](ard-0031-do-not-load-all-ai-instruction-files-automatically.md)         | Do Not Load All AI Instruction Files Automatically         | Accepted | 2026-05-15 |
| [ard-0032](ard-0032-use-stable-human-readable-plan-ids.md)                         | Use Stable Human-Readable Plan IDs                         | Accepted | 2026-05-15 |
| [ard-0033](ard-0033-add-proposals-directory-and-rules.md)                          | Add Proposals Directory And Rules                          | Accepted | 2026-05-15 |
| [ard-0034](ard-0034-use-stable-proposal-ids.md)                                    | Use Stable Proposal IDs                                    | Accepted | 2026-05-15 |
| [ard-0035](ard-0035-store-open-questions-with-decisions.md)                        | Store Open Questions With Decisions                        | Accepted | 2026-05-15 |
| [ard-0036](ard-0036-include-plan-ids-in-plan-filenames.md)                         | Include Plan IDs In Plan Filenames                         | Accepted | 2026-05-15 |
| [ard-0037](ard-0037-use-compact-plan-status-lifecycle.md)                          | Use Compact Plan Status Lifecycle                          | Accepted | 2026-05-15 |
| [ard-0038](ard-0038-use-plan-prefix-and-filename-stable-ids.md)                    | Use PLAN Prefix And Filename Stable IDs                    | Accepted | 2026-05-15 |
| [ard-0039](ard-0039-use-madr-adr-format-and-ard-filenames.md)                      | Use MADR ADR Format And ARD Filenames                      | Accepted | 2026-05-15 |
| [ard-0040](ard-0040-use-git-identity-for-adr-decision-makers.md)                   | Use Git Identity For ADR Decision Makers                   | Accepted | 2026-05-15 |
| [ard-0041](ard-0041-gate-adr-and-plan-implementation.md)                           | Gate ADR And Plan Implementation                           | Proposed | 2026-05-15 |
