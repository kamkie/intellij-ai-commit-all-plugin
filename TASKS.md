# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The plugin has not been published to JetBrains Marketplace.

Completed task history is preserved in [TASKS_ARCHIVE.md](TASKS_ARCHIVE.md).

Notation:

- Every task starts with a `T-AREA-NNN` ref. Keep refs stable when wording, status, or ordering changes. Do not renumber existing refs.
- `resolves: Q-<AREA>-NNN` means the task answers an open question ref.
- `depends on: Q-<AREA>-NNN` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Documentation

- [ ] T-DOC-017: finish deferred user-facing documentation follow-ups after the ADR 0076 rebuild (umbrella for T-DOC-018..T-DOC-020 and T-DOC-023).
- [ ] T-DOC-018: expand `config/intellij-platform/description.html` Marketplace description with feature summary, requirements, AI Assistant dependency, link to source, and license note. (`config/intellij-platform/description.html`)
- [ ] T-DOC-019: keep `config/intellij-platform/change-notes.html` aligned with the `CHANGELOG.md` `Unreleased` and latest tagged release sections during release preparation. (`config/intellij-platform/change-notes.html`, `CHANGELOG.md`)
- [ ] T-DOC-020: add reviewed screenshots or a short animation of the `AI | Commit | Push` control in light and dark themes, and link them from `docs/user-guide.md` and the Marketplace description. (`docs/user-guide.md`, `config/intellij-platform/description.html`, `docs/concepts/graphics/`)
- [ ] T-DOC-023: confirm macOS keymap equivalents for the plugin commit and push shortcuts, then update the user guide shortcut table. (`docs/user-guide.md`, `src/main/resources/META-INF/plugin.xml`)

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
