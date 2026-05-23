# Troubleshooting And FAQ

This page covers common user-facing problem paths for AI Commit All. For
support scope, reporting expectations, and privacy guidance, see
[Support](SUPPORT.md).

## FAQ

### What if JetBrains AI Assistant is missing or disabled?

AI Commit All requires JetBrains AI Assistant. If AI Assistant is missing or
disabled, the IDE refuses to load this plugin through the required plugin
dependency instead of falling back to a non-AI commit message.

Check these first:

- Open `Settings | Plugins` and confirm JetBrains AI Assistant is installed and
  enabled.
- Sign in to AI Assistant if the IDE asks for account access.
- Restart the IDE after enabling AI Assistant or installing AI Commit All from
  disk.
- If the plugin still does not load, open the IDE log and look for plugin
  dependency or `com.intellij.ml.llm` messages.

AI Assistant account, licensing, service availability, and model quality issues
belong to JetBrains AI Assistant support unless AI Commit All loaded correctly
and the failure is specific to the `AI`, `Commit`, or `Push` workflow.

### What happens when AI generation times out?

The default AI generation timeout is `30000` ms. When the timeout expires, the
workflow stops without committing or pushing and shows the plugin-owned timeout
message:

```text
AI Assistant did not finish generating a commit message before the configured timeout.
```

Try these steps:

- Confirm AI Assistant can generate a commit message from the standard IDE
  commit workflow.
- Open `Settings | Tools | AI Commit All` and increase `AI generation timeout
  (ms)` if generation normally takes longer in your IDE or repository.
- Keep both timing settings positive. Non-positive values are rejected or
  normalized by the plugin.
- Avoid editing the commit message while AI generation is running. User edits
  stop the plugin workflow without committing or pushing.

If AI Assistant finishes but the message is empty, unchanged, unavailable, or
cannot expose a completion signal to the plugin, the workflow also stops
without committing or pushing. Some of those states are surfaced by AI
Assistant or the IDE rather than by a plugin notification.

### Why is the control hidden?

The `AI | Commit | Push` control appears only inside a supported Git commit
workflow. It is hidden when the current context is outside the Commit tool
window workflow that the plugin can drive.

Check these first:

- Open a Git project, not a non-Git VCS project.
- Open the non-modal Commit tool window.
- Confirm the IDE is on the supported IntelliJ Platform `2026.1` line.
- Confirm the project has a commit workflow available in the current IDE
  product.
- Confirm AI Commit All and JetBrains AI Assistant are both loaded in
  `Settings | Plugins`.

### Why are some sections disabled?

Disabled sections usually mean the IDE has no safe action for that section in
the current state.

| State | Expected control state |
|-------|------------------------|
| Committable Git changes exist | `AI` is enabled. `Commit` and `Push` depend on the IDE commit and commit-and-push executors. |
| No committable changes exist | `AI` and `Commit` are disabled. `Push` can stay enabled when outgoing commits exist. |
| A plugin workflow is already running | All sections are disabled until the workflow completes. |
| The Commit tool window workflow is unavailable | The control is hidden or the workflow stops before making changes. |

Eligible changes include modified, added, deleted, moved or renamed,
unversioned, and resolved-conflict paths exposed by IntelliJ VCS APIs. Ignored
files are excluded. If expected files are missing, check the IDE Commit tool
window inclusion state, changelist selection, and Git staging-area mode.

### Why did Push open the IDE dialog?

`Push` skips the Push Commits dialog only when every affected Git repository is
safe for immediate push. A push is considered safe only for normal tracked
branches with an unambiguous target, no force-push requirement, no unresolved
conflicts in scope, and a normal repository state.

When committable changes exist and safe immediate push cannot be verified, the
plugin falls back to the IDE commit-and-push executor and Push Commits dialog.
Common fallback causes are:

- No tracked upstream branch.
- Ambiguous or unsupported push target.
- Repository state is not normal.
- Unresolved conflicts are still present.
- Force-push state cannot be verified.
- The IDE push API needed by the plugin is unavailable.

The dialog fallback is expected. Review the IDE dialog, branch target, and push
warnings before continuing.

### Why did outgoing-only push stop instead of showing the dialog?

When there are no committable changes but local outgoing commits exist, `Push`
pushes those outgoing commits directly if safe immediate push can be prepared.
It does not run AI generation or create another commit.

If safe immediate push cannot be prepared in this outgoing-only state, the
workflow stops instead of opening the IDE Push Commits dialog. Use the standard
IDE push action when you need to resolve a missing upstream, ambiguous target,
divergence, force-push question, or other push condition manually.

### What do conflicts do?

Unresolved conflicts are not safe for AI Commit All workflows. Resolve conflicts
first, mark the paths resolved in the IDE or Git, and make sure the Commit tool
window exposes the resolved files as committable.

Resolved-conflict paths are eligible when the IDE exposes them as committable
changes. If a resolved file is not included, refresh VCS state, re-open the
Commit tool window, and confirm the file is no longer listed as unresolved.

### What do background VCS operations do?

AI Commit All stops before changing staging state when the IDE reports that a
background VCS operation is already running or the changelist manager is frozen.
This avoids racing the IDE while it is refreshing, updating, staging, committing,
or otherwise mutating VCS state.

Wait for the IDE background operation to finish, then try again. If the state
does not clear, refresh VCS state, reopen the project, or inspect the IDE log for
the operation that is still active.

### What evidence should I include in a report?

Include enough detail for someone else to reproduce the problem:

- IDE product name and build number.
- AI Commit All version, release tag, or commit SHA.
- Operating system.
- Whether JetBrains AI Assistant is installed, enabled, and signed in.
- Whether the Git staging area is enabled.
- Whether the project has one Git root or multiple Git roots.
- Which section was used: `AI`, `Commit`, `Push`, or a shortcut.
- Whether there were committable changes, outgoing commits, or both.
- Branch and upstream state, without exposing private remote URLs.
- Exact steps to reproduce.
- Expected and actual behavior.
- Relevant IDE notifications, screenshots, or screen recordings.
- Relevant IDE log excerpts with secrets, tokens, private repository contents,
  proprietary commit messages, and private remote paths removed.

Use `Help | Show Log in Explorer` from the IDE to open the active logs folder.
On Windows, IntelliJ Platform IDE logs are usually under
`%LOCALAPPDATA%\JetBrains\<Product><Version>\log`, for example
`%LOCALAPPDATA%\JetBrains\IntelliJIdea<Version>\log`.

### Does AI Commit All support non-Git repositories?

No. The current scope is Git only, including multiple Git roots. Non-Git VCS
contexts do not start AI, commit, or push work.

### Does it bypass IDE commit checks?

No. `Commit` and commit-and-push use the active IntelliJ commit workflow. IDE
before-commit checks, confirmations, warnings, and commit or push errors remain
in charge.

### Why did the commit message disappear before generation?

When `Clear commit message before AI generation` is enabled, the plugin clears
the current commit message before invoking AI Assistant. This prevents stale
text from being mistaken for newly generated output.

### Why did editing the message stop the workflow?

User edits during generation are treated as intentional intervention. The
workflow stops without committing or pushing so the plugin does not commit text
that changed while AI generation was in flight.

### Where are the current validation details?

The behavior contract is in [Plugin Behavior Specification](specification.md).
Maintainer validation artifacts are listed in [Validation](validation/README.md),
with reusable release checks in
[Release Validation Checklist](validation/release-checklist.md).
