# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The plugin has not been published to JetBrains Marketplace.

Completed task history is preserved in [TASKS_ARCHIVE.md](TASKS_ARCHIVE.md).

Notation:

- Every task starts with a `T-AREA-NNN` ref. Keep refs stable when wording, status, or ordering changes. Do not renumber existing refs.
- `resolves: Q-<AREA>-NNN` means the task answers an open question ref.
- `depends on: Q-<AREA>-NNN` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Testing

- [ ] T-TEST-010: credit release-matrix UI integration coverage in JaCoCo. The agent attaches to the Starter-launched IDE and produces a ~113 KB `releaseMatrixUiTest.exec`, but `jacocoAggregateReport` stays at unit-only line coverage (~80.15%) because the JaCoCo class ids of the plugin classes loaded inside the IDE do not match the report's `build/instrumented/instrumentCode` classes (load-time transformer ordering in the IDE JVM; on-disk bytes are identical, ruling out a build-vs-build mismatch). Tried both a downstream merge job and per-product reports; both stay flat at 80.15%. Lead: add the JaCoCo agent `classdumpdir=` option in `attachCoverageAgentIfRequested` to capture the exact instrumented classes the IDE loaded, then point the integration report's `classDirectories` at those dumped classes. Validate cheaply with a `gh workflow run release-matrix-ui.yml -f ide-products=PY` dispatch and the `ai-commit-all-release-matrix-ui-PY-evidence` artifact's `jacocoAggregateReport.xml` (expect line % above 80.15% with adapters credited). Keep the unit `verifyJacocoCoverageReport` gate independent of the UI lane. (`build.gradle.kts`, `src/integrationTest/kotlin/pl/devopssolutions/aicommitall/integration/ReleaseMatrixUiHarnessTest.kt`, `.github/workflows/release-matrix-ui.yml`)

### Validation

- [ ] T-VAL-024: execute and record the current manual release validation matrix before Marketplace publication, covering final control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior; IDEA deterministic UI automation is present, while live AI Assistant, PyCharm/WebStorm, and platform error observations remain manual. (`docs/validation/release-checklist.md`, `docs/validation/scenario-register.md`, `.agents/plans/archive/PLAN-release-matrix-ui-automation.md`)
