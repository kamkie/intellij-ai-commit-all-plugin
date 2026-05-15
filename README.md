# AI Commit All

IntelliJ Platform plugin concept for adding an `AI Commit All` action to the Commit tool window.

The commit control should be a split button with `AI Commit All` as the primary segment and `& Push` as the push segment.

Current scope decisions:

- Minimum supported IntelliJ Platform release line: 2026.1.
- Target IDEs: all JetBrains IDEs with the VCS Commit tool window and compatible commit workflow APIs.
- First-version VCS support: Git only, including projects with multiple Git roots.
- Commit flow: after AI message generation completes, the selected split-button flow commits automatically.
- User edits during AI message generation stop the automated commit flow without committing or pushing.
- AI completion should be detected from the AI action or generation state, with a Settings-configurable timeout defaulting to 5 seconds and a 500 ms default supporting check interval.
- JetBrains AI Assistant is a required plugin dependency; installation or loading should fail when it is missing or disabled.
- Runtime AI generation failures stop without committing or pushing, show button activity while running, and report through standard IntelliJ notifications.
- Error and notification handling should reuse or forward standard IntelliJ, Git, VCS, push, and AI Assistant messages where possible.
- Confirmation and safety barriers should come from the standard IntelliJ commit and push workflows unless development reveals a concrete uncovered risk.
- Icons should use AI-generated base concepts adapted into IntelliJ-style SVG assets.
- Detailed split-button styling should start from a generated placeholder graphic and be adapted into IntelliJ Platform UI conventions during implementation.
- Distribution target: open-source plugin published to the official JetBrains Marketplace with signing, Marketplace metadata, and CI support.
- Validation target: current stable JetBrains IDE builds available through All Products Pack, with changelists and Git staging enabled and disabled covered.
- End-to-end tests should use local Git repositories where the IntelliJ test framework and sandbox setup make that practical.
- License: Apache License 2.0.
- Plugin ID and base package: `pl.devopssolutions.aicommitall`.
- Vendor: DevOps Solutions Kamil Kiewisz, `https://devopssolutions.pl`, `kontakt@devopssolutions.pl`.

## Goal

The plugin should provide a one-click commit flow:

1. Select/include every non-ignored committable file change, including modified, added, deleted, moved or renamed, and unversioned files.
2. Trigger JetBrains AI Assistant's `Generate commit message with AI Assistant` action.
3. Wait for AI Assistant to finish generating the commit message.
4. Commit all selected files.
5. Push after commit when the push flow was selected.

## Current Status

Initial executable Gradle/Kotlin IntelliJ plugin scaffold is present and sandbox startup has been validated. The `AI Commit All` workflow implementation is still pending.

Implementation guidance for future agents is in [AGENTS.md](AGENTS.md).

Guidance for working with AI agents on this repository is in [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md).

Notable changes are tracked in [CHANGELOG.md](CHANGELOG.md).

Support status and issue-reporting expectations are in [SUPPORT.md](SUPPORT.md).

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE).
