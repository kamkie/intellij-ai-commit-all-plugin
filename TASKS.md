# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The plugin has not been published to JetBrains Marketplace.

Completed task history is preserved in [TASKS_ARCHIVE.md](TASKS_ARCHIVE.md).

Notation:

- Every task starts with a `T-AREA-NNN` ref. Keep refs stable when wording, status, or ordering changes. Do not renumber existing refs.
- `resolves: Q-<AREA>-NNN` means the task answers an open question ref.
- `depends on: Q-<AREA>-NNN` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Validation

- [ ] T-VAL-024: execute and record the current manual release validation matrix before Marketplace publication, covering final control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior; IDEA deterministic UI automation is present, while live AI Assistant, PyCharm/WebStorm, and platform error observations remain manual. (`docs/validation/release-checklist.md`, `docs/validation/scenario-register.md`, `.agents/plans/archive/PLAN-release-matrix-ui-automation.md`)
