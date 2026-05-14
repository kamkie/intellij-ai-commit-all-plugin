# Detect AI Completion With Configurable Timeout

Status: Accepted

Date: 2026-05-14

## Context

AI completion behavior previously depended on open questions about timeout and message stability.

The user answered `Q-AI-1` by saying the plugin should check whether the AI action is completed instead of waiting, with timeout configuration available in Settings.

This also replaces the earlier idea that the generated message must remain unchanged for a fixed stable interval before completion.

The user answered `Q-AI-2` with 500 ms as a good starting point for a configurable supporting check interval.

The user answered `Q-AI-5` with 5 seconds as a starting default for the configurable AI generation timeout.

## Decision

Prefer an explicit AI action or generation-completion signal over fixed waiting or message-stability timing.

Treat AI generation as complete only when the plugin can determine that the AI Assistant action has completed and the resulting commit message is acceptable for the automated flow.

Use a configurable timeout as a fail-safe. The timeout should be exposed in the plugin's Settings page.

If completion cannot be detected before the configured timeout expires, stop without committing or pushing and report the timeout path.

Do not use a fixed stable-message interval as the primary completion criterion.

When supporting polling or debounce checks are needed, make the completion-check interval configurable and default it to 500 ms.

Default the configurable AI generation timeout to 5 seconds.

## Consequences

- The implementation should search for reliable IntelliJ action, callback, UI state, or commit-message-generation completion signals before falling back to polling.
- Polling the commit message field may still be used as supporting evidence, but it must not be the only completion rule when a better completion signal is available.
- Settings UI and persistent state are now in scope for the timeout configuration and completion-check interval.
- The initial default timeout is intentionally conservative and can be changed later if sandbox validation shows AI generation commonly needs more time.

## Alternatives Considered

- Use a fixed wait duration.
  - Why it was not chosen: it is unreliable and can commit too early or wait longer than needed.
- Treat unchanged message text as completion.
  - Why it was not chosen: message stability is only an indirect signal and can be wrong if generation pauses.

## Follow-Up

- Remove `Q-AI-1` and `Q-AI-2` from `OPEN_QUESTIONS.md`.
- Remove `Q-AI-5` from `OPEN_QUESTIONS.md`.
- Update `TASKS.md` to implement explicit completion detection, Settings-based timeout configuration, and a 500 ms default completion-check interval.
