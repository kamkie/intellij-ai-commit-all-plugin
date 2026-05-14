# Open Questions

This file tracks decisions and missing input needed from the user. Keep implementation tasks in `TASKS.md`.

## Scope And Target

- What is the minimum supported IntelliJ Platform version?
- Should the plugin target IntelliJ IDEA only, or all JetBrains IDEs with the VCS Commit tool window?
- Should the first version support only Git, or all VCS integrations exposed through the IntelliJ commit workflow?
- Should the first version support projects with multiple VCS roots?

## Commit Flow

- Should there be one `AI Commit All` action, separate `AI Commit All` and `AI Commit & Push All` actions, or integration with the existing Commit / Commit and Push choice?
- Should "all files" include every changelist, only the active/default changelist, or only files visible in the current commit workflow?
- Should resolved conflicts be included automatically when IntelliJ considers them committable?
- Should the action include unversioned files automatically without an extra confirmation?
- Should the plugin proceed to commit automatically after AI generation, or pause if the generated message needs user review?
- What should happen if the user edits or clears the commit message while AI generation is in progress?

## AI Assistant Behavior

- What timeout should be used while waiting for AI Assistant to finish generating the commit message?
- How long should the generated commit message remain unchanged before it is treated as stable and complete?
- What should the plugin do when AI Assistant is missing, disabled, unavailable, or the user is not signed in?
- Should the plugin ever fall back to a non-AI/manual commit message flow, or should it stop without committing?

## UX Decisions

- What exact action labels should appear in the Commit tool window?
- Should the actions use custom icons, IntelliJ platform icons, or no icons initially?
- What notification text should be shown for skipped commits, AI failures, timeout, empty change set, and unsupported push?
- Should before-commit checks and IDE warnings be the only confirmation barriers, or should the plugin add its own confirmation for risky cases?

## Project Metadata

- What plugin ID, package name, and vendor name should be used?
- What license should the repository and plugin use?
- Should publishing, signing, marketplace metadata, or CI be added in this phase, or deferred?

## Validation Input

- Which IDE versions should be used for manual sandbox testing?
- Should testing cover Git staging area enabled and disabled from the first implementation?
- Are there real project examples or workflows that should be used as acceptance tests?
