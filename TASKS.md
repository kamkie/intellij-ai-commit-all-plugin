# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The plugin has not been published to JetBrains Marketplace.

Completed task history is preserved in [TASKS_ARCHIVE.md](TASKS_ARCHIVE.md).

Notation:

- Every task starts with a `T-AREA-NNN` ref. Keep refs stable when wording, status, or ordering changes. Do not renumber existing refs.
- `resolves: Q-<AREA>-NNN` means the task answers an open question ref.
- `depends on: Q-<AREA>-NNN` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Staging Reliability

- [ ] T-BUG-017: fallback hardening for staging confirmation, only if the conservative HEAD-identical-path fix (`PLAN-workflow-stop-feedback-and-push-alignment` task T2) proves insufficient in the field: confirm staging against Git command output as ground truth (for example `git status --porcelain` or `git diff --cached --name-only` scoped to the expected paths) instead of relying on `GitStageTracker` UI state, which the June 2026 IDE-log investigation showed can stay stale or omit HEAD-identical paths until a window-focus VFS refresh. (`src/main/kotlin/pl/devopssolutions/aicommitall/workflow/GitStageConfirmation.kt`, `docs/specification.md` REQ-SEL-008)

### Validation

- [ ] T-VAL-024: execute and record the current manual release validation matrix before Marketplace publication, covering final control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior; IDEA deterministic UI automation is present, while live AI Assistant, PyCharm/WebStorm, and platform error observations remain manual. (`docs/validation/release-checklist.md`, `docs/validation/scenario-register.md`, `.agents/plans/archive/PLAN-release-matrix-ui-automation.md`)
