# Build Tasks

Repository state: the executable Gradle/Kotlin IntelliJ plugin scaffold, runtime `AI Commit All` workflow implementation, automated validation coverage, manual sandbox validation records, CI, and gated Marketplace release automation are present. The stable tag `v0.1.0` is on `main` and its GitHub release is published after passing main and tagged validation. The signed Stable Marketplace package was submitted on 2026-10-05 as update `1187809` and is under review. `v0.1.0-beta.10` also remains under review in the beta channel, with no approved public Marketplace release yet. The [v0.1.0 validation report](docs/validation/reports/2026-10-05-v0.1.0.md) records the maintainer-directed automated harness route and remaining manual evidence gaps.

Completed task history is preserved in [TASKS_ARCHIVE.md](TASKS_ARCHIVE.md).

Notation:

- Every task starts with a `T-AREA-NNN` ref. Keep refs stable when wording, status, or ordering changes. Do not renumber existing refs.
- `resolves: Q-<AREA>-NNN` means the task answers an open question ref.
- `depends on: Q-<AREA>-NNN` means the task should wait until that question is answered or explicitly assumed in an approved plan or ADR.

## Open Backlog

### Validation

- [ ] T-VAL-024: execute and record the current manual release validation matrix before Marketplace publication, covering final control rendering, staging-area modes, shortcut takeover, AI Assistant unavailable states, and full commit/push UI behavior; IDEA deterministic UI automation is present, while live AI Assistant, PyCharm/WebStorm, and platform error observations remain manual. For `v0.1.0` only, the maintainer directed the automated IDE harness and historical real-AI evidence route recorded in the [release report](docs/validation/reports/2026-10-05-v0.1.0.md); remaining manual coverage keeps this task open. (`docs/validation/release-checklist.md`, `docs/validation/scenario-register.md`, `.agents/plans/archive/PLAN-release-matrix-ui-automation.md`)
