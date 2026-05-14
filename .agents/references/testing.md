# Testing And Validation

Use validation that matches the change. Documentation-only changes do not require plugin builds unless they alter executable examples or build instructions.

## Build Checks

- `gradle buildPlugin` for packaging and basic compile validation once a Gradle scaffold exists.
- `gradle verifyPlugin` when configured.
- IntelliJ Plugin Verifier for the supported IDE version range once compatibility targets are chosen.

## Sandbox Checks

Use `gradle runIde` for manual sandbox testing once the scaffold exists.

Manual scenarios for this plugin:

- Modified tracked file is included.
- Unversioned file is included.
- Commit-only flow commits selected files after AI message generation.
- Commit-and-push flow pushes after a successful commit when that flow is selected.
- AI Assistant unavailable, disabled, not signed in, or missing.
- Git staging area enabled.
- Git staging area disabled.
- Empty change set.
- User edits or clears the message while AI generation is in progress.

## Review Checks

- Confirm no validation relies on source repo assumptions from unrelated Spring, REST, OpenAPI, release, or operations workflows.
- Confirm failures are reported without committing.
- Confirm timeout paths do not leave the user with an unintended commit.

## Reporting

When handing off, state:

- Commands run.
- Manual sandbox scenarios tested, if any.
- Checks not run and why.
- Residual IDE compatibility risk.
