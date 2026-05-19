# Testing And Validation

Use validation that matches the change. Documentation-only changes do not require plugin builds unless they alter executable examples or build instructions.

## Build Checks

- `gradle spotlessCheck` for Kotlin and Gradle Kotlin DSL formatting and Kotlin source license-header enforcement.
- `npx --yes markdownlint-cli2@0.22.1` for Markdown linting.
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1` for Markdown linting, documentation structure, links, stable IDs, ADR numbering and index, and proposal tracker checks.
- `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1` for repository skill and prompt structure checks when `.agents/skills/` or `.agents/prompts/` changed.
- `gradle buildPlugin` for packaging and basic compile validation once a Gradle scaffold exists.
- `gradle verifyPlugin` when configured.
- Plugin signing and signature verification once signing configuration exists.
- IntelliJ Plugin Verifier for the supported IDE version range once compatibility targets are chosen.
- CI workflow validation for pull-request checks without Marketplace or signing secrets once CI exists.

## Sandbox Checks

Use `gradle runIde` for manual sandbox testing once the scaffold exists.

Use current stable JetBrains IDE builds available through the user's All Products Pack. Record exact product names and build numbers in validation reports.

When a problem is reported from manual testing in a real IDE or Gradle sandbox IDE, ask whether the user wants the agent to analyze the relevant IDE logs before inferring the cause. Request sanitized log excerpts or explicit permission to inspect the local logs folder, and remind the user not to share secrets, tokens, proprietary source content, or private commit messages.

Use `.agents/references/troubleshooting.md` for validation, Gradle, plugin packaging, sandbox IDE, test, and IDE-log failure diagnosis.

Create end-to-end tests against local Git repositories where the IntelliJ test framework, Gradle sandbox, and CI environment make that practical. Keep manual sandbox coverage for scenarios that cannot be automated reliably yet.

Manual scenarios for this plugin:

- Modified tracked file is included.
- Deleted tracked file is included.
- Moved or renamed tracked file is included.
- Unversioned file is included.
- Ignored file is excluded.
- Files from all changelists in the supported VCS scope are included.
- Commit-only flow commits selected files after AI message generation.
- Commit-and-push flow pushes after a successful commit when that flow is selected.
- Existing before-commit checks, commit warnings, and push errors remain active through the IDE workflow.
- JetBrains AI Assistant dependency missing or disabled fails installation/loading.
- AI Assistant present but unavailable or not signed in stops without commit or push.
- Standard IntelliJ, Git, VCS, push, and AI Assistant errors are surfaced or forwarded without being masked by custom plugin text.
- Git staging area enabled.
- Git staging area disabled.
- Local-repository E2E coverage for commit selection, changelists, staging modes, commit-only, and local-remote commit-and-push where safe.
- Empty change set.
- User edits or clears the message while AI generation is in progress.
- Release workflow only through a gated/manual path with secrets supplied outside the repository.

## Review Checks

- Confirm no validation relies on source repo assumptions from unrelated Spring, REST, OpenAPI, release, or operations workflows.
- Confirm failures are reported without committing.
- Confirm platform-owned errors are not replaced by less precise plugin-owned notifications.
- Confirm implementation does not bypass IDE commit or push safeguards.
- Confirm changelists and Git staging enabled/disabled paths remain covered.
- Confirm local-repository E2E tests do not push to real remotes.
- Confirm publishing and signing secrets are not committed or required for pull-request checks.
- Confirm timeout paths do not leave the user with an unintended commit.

## Flaky Test Triage

Use `.agents/skills/triage-flaky-test/SKILL.md` when a test alternates between pass and fail without relevant source changes.

- Preserve the first failure output and exact command before rerunning.
- Rerun the narrowest failing test first, then the smallest enclosing task.
- Diagnose shared state, fixture lifecycle, temp directories, real remotes, sleeps, timing assumptions, platform background work, and environment differences before changing assertions.
- Prefer deterministic setup, cleanup, and synchronization over retries.

## Reporting

When handing off, state:

- Commands run.
- Manual sandbox scenarios tested, if any.
- Checks not run and why.
- Residual IDE compatibility risk.
