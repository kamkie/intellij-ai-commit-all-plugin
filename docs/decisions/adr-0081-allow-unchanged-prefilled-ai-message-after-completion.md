---
status: accepted
date: 2026-05-24
accepted_at: 2026-05-24T01:41:51+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Allow Unchanged Prefilled AI Message After Completion

## Context and Problem Statement

The current AI completion contract treats generation as successful only when the final commit message is non-empty and different from the captured snapshot. That rule was a fail-closed safeguard for earlier uncertainty around silent AI-generation failures: if the plugin could not prove AI Assistant wrote new text, it stopped before commit or push.

The plugin now has more explicit completion handling: it distinguishes missing AI actions, missing completion signals, timeouts, empty results, and user edits during generation. The default setting still clears stale commit text before invoking AI Assistant, but users can disable clearing when they intentionally want AI Assistant to revise an existing commit message.

When clearing is disabled and the user has already generated or written a commit message, an unchanged non-empty result after a reliable AI completion signal can reasonably mean AI Assistant reviewed the message and left it as-is. Should the plugin continue to fail closed for that case, or may it accept the unchanged prefilled message?

## Decision Drivers

* Preserve fail-closed behavior when AI Assistant cannot be invoked, completion cannot be observed, generation times out, the result is empty, or the user edits during generation.
* Keep the default clear-message workflow conservative and easy to reason about.
* Support intentional "revise this existing message" workflows when the user disables message clearing.
* Avoid treating a reliable AI completion signal with unchanged prefilled text as a silent failure.
* Keep `Commit` and `Push` automation from committing a stale message when no reliable AI completion evidence exists.
* Make user-facing documentation precise about when unchanged text is acceptable.

## Considered Options

* Accept unchanged non-empty prefilled messages after reliable AI completion when clearing is disabled.
* Always require generated text to differ from the captured snapshot.
* Ask for user confirmation when AI leaves prefilled text unchanged.
* Remove or ignore the clear-message setting and always clear before generation.

## Decision Outcome

Chosen option: "Accept unchanged non-empty prefilled messages after reliable AI completion when clearing is disabled", because the unchanged-message guard should protect against missing AI output, not reject an intentional revise-existing-message workflow after reliable completion evidence.

If accepted:

* When `clearCommitMessageBeforeGeneration` is enabled, the current behavior remains: clear the message, capture an empty snapshot, and require AI Assistant to produce a non-empty message.
* When clearing is disabled and the captured snapshot is non-empty, an unchanged final message is acceptable only when AI Assistant was invoked, generation completion was reliably observed, the message remains non-empty, and the user did not edit the message during generation.
* Missing AI action, unavailable completion signal, timeout, empty message, and user edit paths remain fail-closed.
* An unchanged empty snapshot remains unsuccessful.
* `UnchangedMessage` remains available for cases where unchanged text still indicates no usable AI output.
* `docs/specification.md` must update REQ-AI-008 and REQ-AI-013 so the accepted unchanged-prefilled path is explicit.
* `docs/user-guide.md` must describe the clear-message setting and unchanged-message guard without implying that AI must always rewrite prefilled text.

### Consequences

* Good, because users can prefill a message, ask AI Assistant to revise it, and still use `Commit` or `Push` if AI leaves the message unchanged after reliable completion.
* Good, because the default clear-message workflow remains conservative.
* Good, because silent AI-generation failures still stop when completion cannot be observed or the result is empty.
* Good, because the behavior matches a clearer mental model: unchanged text is only unsafe when there is no reliable evidence that AI generation completed.
* Bad, because completion detection must distinguish "unchanged but accepted" from `UnchangedMessage` failure cases.
* Bad, because tests and user documentation must cover both clear-message-enabled and clear-message-disabled paths.

### Confirmation

After acceptance and implementation, confirm by checking:

* `docs/specification.md` differentiates unchanged empty snapshots, unchanged non-empty prefilled messages, and missing completion evidence.
* `docs/user-guide.md` explains that clearing disabled lets AI Assistant revise existing text and may leave it unchanged after reliable completion.
* Automated tests cover accepted unchanged non-empty prefilled messages when clearing is disabled.
* Automated tests preserve fail-closed behavior for unchanged empty snapshots, empty messages, timeouts, missing completion signals, and user edits.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.
* Focused AI completion tests pass.
* `git diff --check` passes.

## Pros and Cons of the Options

### Accept unchanged non-empty prefilled messages after reliable AI completion when clearing is disabled

This option narrows the unchanged-message guard to the cases it was meant to protect: no usable AI output or no reliable completion evidence.

* Good, because it supports revise-existing-message workflows.
* Good, because it keeps the default clear-message path unchanged.
* Good, because it still fails closed when AI completion cannot be trusted.
* Bad, because it adds a branch to completion-result classification.

### Always require generated text to differ from the captured snapshot

This option keeps the current behavior.

* Good, because it is simple and conservative.
* Good, because it prevents committing a prefilled message unless AI visibly changes it.
* Bad, because it rejects legitimate "AI reviewed and left it unchanged" outcomes.
* Bad, because it makes the clear-message-disabled setting less useful for revising existing text.

### Ask for user confirmation when AI leaves prefilled text unchanged

This option would keep fail-closed automation but offer an explicit manual continuation path.

* Good, because it avoids committing unchanged text automatically.
* Good, because it gives users a way forward.
* Bad, because it introduces an extra confirmation workflow and UI state.
* Bad, because the plugin currently avoids adding custom confirmation barriers when existing IDE workflow safeguards are sufficient.

### Remove or ignore the clear-message setting and always clear before generation

This option would make AI generation always start from an empty message.

* Good, because it simplifies completion classification.
* Bad, because it removes an intentional setting and prevents revise-existing-message workflows.
* Bad, because it would surprise users who disabled clearing to preserve prefilled text.

## More Information

This ADR refines ADR 0012 and ADR 0014 for the clear-message-disabled, non-empty snapshot case. It does not change the requirement to stop on missing AI action, missing completion signal, timeout, empty message, or user edits during generation.

Companion implementation plan: `PLAN-unchanged-prefilled-ai-message`.

After this ADR is accepted, update the ADR Implementation Tracker in `docs/decisions/README.md` with implementation status, evidence, and last updated date.
