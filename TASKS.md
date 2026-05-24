# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The plugin has not been published to JetBrains Marketplace.

Completed task history is preserved in [TASKS_ARCHIVE.md](TASKS_ARCHIVE.md).

Notation:

- Every task starts with a `T-AREA-NNN` ref. Keep refs stable when wording, status, or ordering changes. Do not renumber existing refs.
- `resolves: Q-<AREA>-NNN` means the task answers an open question ref.
- `depends on: Q-<AREA>-NNN` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Documentation

- [ ] T-DOC-017: finish deferred user-facing documentation follow-ups after the ADR 0076 rebuild. Priority order: generation and Marketplace metadata first (T-DOC-025, T-DOC-018, T-DOC-026, T-DOC-019), shortcut confirmation next (T-DOC-023), reviewed visual assets last after current UI validation (T-DOC-020). Plan: `PLAN-documentation-release-followups`.
- [ ] T-DOC-026: P2; generate `config/intellij-platform/change-notes.html` from `CHANGELOG.md` so release notes do not drift.
- [ ] T-DOC-019: P2; keep the generated Marketplace change notes aligned with the `CHANGELOG.md` `Unreleased` and latest tagged release sections during release preparation. (`config/intellij-platform/change-notes.html`, `CHANGELOG.md`)
- [ ] T-DOC-023: P3; confirm macOS keymap equivalents for the plugin commit and push shortcuts, then update the user guide shortcut table. (`docs/user-guide.md`, `src/main/resources/META-INF/plugin.xml`)
- [ ] T-DOC-020: P4; add reviewed screenshots and a short animation of the current `AI | Commit | Push` control in light and dark themes, then link them from `docs/user-guide.md` and the generated Marketplace description. (`docs/user-guide.md`, `config/intellij-platform/description.html`, `docs/concepts/graphics/`)

### Validation

- [ ] T-VAL-024: execute and record the current manual release validation matrix before Marketplace publication, covering final control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior; IDEA deterministic UI automation is present, while live AI Assistant, PyCharm/WebStorm, and platform error observations remain manual. (`docs/validation/release-checklist.md`, `docs/validation/scenario-register.md`, `.agents/plans/archive/PLAN-release-matrix-ui-automation.md`)

### Detekt Plugin

- [ ] T-DETEKT-001: clean detekt findings (umbrella for T-DETEKT-002..T-DETEKT-008). The baseline at `config/detekt/baseline.xml` lists 118 suppressed findings to retire.
- [ ] T-DETEKT-002: extract named constants for non-color `MagicNumber` findings in `AiCommitAllThreeSectionControl.kt` (geometry, scale factors, animation counts). (`src/main/kotlin/.../actions/AiCommitAllThreeSectionControl.kt`)
- [ ] T-DETEKT-003: replace the `ControlColors` hex `MagicNumber` findings with a named colour container or `JBColor.namedColor` lookups, then remove the matching baseline entries. (`src/main/kotlin/.../actions/AiCommitAllThreeSectionControl.kt`, `config/detekt/baseline.xml`)
- [ ] T-DETEKT-004: resolve `MaxLineLength` findings by refactoring offending declarations or raising the configured threshold with rationale in `config/detekt/`. (`src/main/kotlin/.../`, `src/test/kotlin/.../`, `config/detekt/`)
- [ ] T-DETEKT-005: reduce `ReturnCount` violations in workflow, VCS, and AI services by extracting helpers or using sealed-result early returns. (`src/main/kotlin/.../workflow/`, `src/main/kotlin/.../vcs/`, `src/main/kotlin/.../ai/`)
- [ ] T-DETEKT-006: replace `TooGenericExceptionCaught` (catch `Throwable`) sites with the narrowest safe exception types, keeping platform fail-closed behavior. (`src/main/kotlin/.../workflow/`, `src/main/kotlin/.../vcs/`)
- [ ] T-DETEKT-007: address the remaining baseline findings: `ComplexCondition` in `GitStageSelectionItems`, `TooManyFunctions` in `AiCommitAllThreeSectionControl`, `UnusedParameter` in `PushOnlyWorkflowExecutionService`, and the `GitStageConfirmation` `250` constant. (`src/main/kotlin/.../vcs/`, `src/main/kotlin/.../actions/`, `src/main/kotlin/.../workflow/`)
- [ ] T-DETEKT-008: empty `config/detekt/baseline.xml` once T-DETEKT-002..T-DETEKT-007 are landed and add a CI check that fails when the baseline grows. (`config/detekt/baseline.xml`, `build.gradle.kts`, `.github/workflows/ci.yml`)

### Publishing, Signing, Marketplace, And CI

- [ ] T-REL-017: make GitHub release.
