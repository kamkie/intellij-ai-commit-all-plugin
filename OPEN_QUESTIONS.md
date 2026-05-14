# Open Questions

This file tracks decisions and missing input needed from the user. Keep implementation tasks in `TASKS.md`.

## Scope And Target

- Q-SCOPE-1: What is the minimum supported IntelliJ Platform version?
- Q-SCOPE-2: Should the plugin target IntelliJ IDEA only, or all JetBrains IDEs with the VCS Commit tool window?
- Q-SCOPE-3: Should the first version support only Git, or all VCS integrations exposed through the IntelliJ commit workflow?
- Q-SCOPE-4: Should the first version support projects with multiple VCS roots?

## Commit Flow

- Q-COMMIT-1: Should there be one `AI Commit All` action, separate `AI Commit All` and `AI Commit & Push All` actions, or integration with the existing Commit / Commit and Push choice?
- Q-COMMIT-2: Should the plugin proceed to commit automatically after AI generation, or pause if the generated message needs user review?
- Q-COMMIT-3: What should happen if the user edits or clears the commit message while AI generation is in progress?

## AI Assistant Behavior

- Q-AI-1: What timeout should be used while waiting for AI Assistant to finish generating the commit message?
- Q-AI-2: How long should the generated commit message remain unchanged before it is treated as stable and complete?
- Q-AI-3: What should the plugin do when AI Assistant is missing, disabled, unavailable, or the user is not signed in?
- Q-AI-4: Should the plugin ever fall back to a non-AI/manual commit message flow, or should it stop without committing?

## UX Decisions

- Q-UX-1: What exact action labels should appear in the Commit tool window?
- Q-UX-2: Should the actions use custom icons, IntelliJ platform icons, or no icons initially?
- Q-UX-3: What notification text should be shown for skipped commits, AI failures, timeout, empty change set, and unsupported push?
- Q-UX-4: Should before-commit checks and IDE warnings be the only confirmation barriers, or should the plugin add its own confirmation for risky cases?

## Project Metadata

- Q-META-1: What plugin ID, package name, and vendor name should be used?
- Q-META-2: What license should the repository and plugin use?
- Q-META-3: Should publishing, signing, marketplace metadata, or CI be added in this phase, or deferred?

## Validation Input

- Q-VAL-1: Which IDE versions should be used for manual sandbox testing?
- Q-VAL-2: Should testing cover Git staging area enabled and disabled from the first implementation?
- Q-VAL-3: Are there real project examples or workflows that should be used as acceptance tests?
