---
status: accepted
date: 2026-05-20
accepted_at: 2026-05-20T04:30:12+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Use 30 Second Default AI Generation Timeout

## Context and Problem Statement

ADR 0012 set the default AI generation timeout to 5 seconds, framed as an intentionally conservative starting point that could be changed later if sandbox validation showed AI generation commonly needs more time.

Sandbox runs against current JetBrains AI Assistant builds show that cold-start AI generation typically does not finish inside 5 seconds, especially on the first invocation after IDE launch. Users hitting the timeout see the workflow stop without committing, with no commit message generated. The shipped implementation in `v0.1.0-alpha.9` already uses a 30 second default, but the ADR still records 5 seconds.

## Decision Drivers

* Avoid spurious `AiTimeout` stops on the first Commit or Push attempt after IDE startup.
* Keep the timeout configurable so users with stricter expectations can lower it.
* Preserve the conservative fail-safe role of the timeout: it must still cut off truly stuck generations.
* Match the value already shipped in `v0.1.0-alpha.9` so the ADR reflects the implementation.

## Considered Options

* Set the default timeout to 30 seconds.
* Keep the 5 second default from ADR 0012.
* Set a larger default such as 60 seconds.

## Decision Outcome

Chosen option: "Set the default timeout to 30 seconds.", because empirical sandbox runs show cold-start AI Assistant generations commonly take more than 5 seconds and routinely complete within 30 seconds; 30 seconds avoids the common false-timeout case while remaining short enough that a truly stuck request is surfaced quickly.

This decision narrows only the default timeout value chosen in ADR 0012. ADR 0012 otherwise remains in force: the timeout is still configurable through Settings, the 500 ms supporting check-interval default is unchanged, and the completion-detection model (explicit AI action signal, with polling as supporting evidence) is unchanged.

### Consequences

* Good, because fewer first-run Commit or Push attempts will stop with `AiTimeout` without a generated message.
* Good, because the ADR catches up with the implementation already shipped in `v0.1.0-alpha.9`.
* Bad, because users who rely on a 5 second cutoff will need to lower the value in Settings.
* Bad, because a truly stuck AI Assistant generation will now block the workflow for up to 30 seconds instead of 5.

### Confirmation

After acceptance, confirm the default through:

* `AiCommitAllSettings.DEFAULT_TIMEOUT_MILLIS` resolves to `Duration.ofSeconds(30).toMillis()`.
* `README.md` Settings table shows `30000` for `aiGenerationTimeoutMillis`.
* `docs/specification.md` Section 9 table shows `30000` for `aiGenerationTimeoutMillis`.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` passes.

## Pros and Cons of the Options

### Set the default timeout to 30 seconds.

* Good, because it matches empirical AI Assistant cold-start latency.
* Good, because the value is already used by the implementation, so this ADR closes a code-versus-decision gap.
* Neutral, because users with strict requirements can still lower the timeout in Settings.
* Bad, because a stuck generation now takes longer to surface.

### Keep the 5 second default from ADR 0012.

* Good, because it preserves the original decision text without amendment.
* Bad, because it contradicts shipped behavior.
* Bad, because most cold-start AI generations exceed 5 seconds, leaving the default value unhelpful.

### Set a larger default such as 60 seconds.

* Good, because it provides even more margin for slow cold starts.
* Bad, because it exceeds typical observed completion latencies, so the timeout no longer acts as a useful guard rail.
* Bad, because the user-facing perceived wait grows.

## More Information

- Narrows ADR 0012 only for the default timeout value. ADR 0012 stays in force for completion-detection model, configurability, and check interval default.
- Existing implementation: `src/main/kotlin/pl/devopssolutions/aicommitall/ai/AiGenerationCompletion.kt` `AiGenerationCompletionOptions.DEFAULT`.
- Existing documentation: `README.md` Settings table; `docs/specification.md` Section 9 settings table.
- Existing release note: `CHANGELOG.md` Unreleased entry and `config/intellij-platform/change-notes.html` "Increases the default AI generation timeout to 30 seconds".
- After acceptance, update the ADR Implementation Tracker in `docs/decisions/README.md`, update the source line of `docs/specification.md` `REQ-SET-001..004` to reference this ADR, and update `docs/requested-features.md` to reflect the narrowing.
