# AI Commit All Files

IntelliJ Platform plugin concept for adding an `AI Commit All Files` action to the Commit tool window.

## Goal

The plugin should provide a one-click commit flow:

1. Select/include all changed and unversioned files.
2. Trigger JetBrains AI Assistant's `Generate commit message with AI Assistant` action.
3. Wait for AI Assistant to finish generating the commit message.
4. Commit all selected files.
5. Push after commit when the push flow was selected.

## Current Status

Repository initialized only. No Gradle, Kotlin, or IntelliJ plugin scaffold has been added yet.

Implementation guidance for future agents is in [AGENTS.md](AGENTS.md).

Guidance for working with AI agents on this repository is in [docs/WORKING_WITH_AI.md](docs/WORKING_WITH_AI.md).
