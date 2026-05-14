# AI Guidelines Import Proposal

Source reviewed: `D:\Projects\Jit\technical-interview-demo`

Reviewed source artifacts:

- `AGENTS.md`
- `docs/WORKING_WITH_AI.md`
- `docs/DEVELOPMENT_LIFECYCLE.md`
- `.agents/references/execution.md`
- `.agents/references/code-style.md`
- `.agents/references/testing.md`
- `.agents/references/reviews.md`
- `.agents/` layout

## Recommendation

Import the AI guidance model, not the whole guidance corpus.

The source repo has a mature, multi-phase workflow system for a production Spring application. This plugin repo is still intentionally small, so copying everything would add more process than value. The useful pattern is:

- root `AGENTS.md` as the AI entry point
- human-facing `docs/WORKING_WITH_AI.md`
- focused `.agents/references/` guides for execution, planning, testing, review, code style, and documentation ownership
- optional `.agents/plans/` only when implementation work becomes multi-step
- ADRs only for durable project decisions

## Proposed Import Shape

Create this lightweight structure:

```text
AGENTS.md
README.md
TASKS.md
AI_GUIDELINES_IMPORT_PROPOSAL.md
docs/
  WORKING_WITH_AI.md
  DEVELOPMENT_LIFECYCLE.md
  decisions/
    README.md
    ADR_TEMPLATE.md
.agents/
  references/
    execution.md
    planning.md
    testing.md
    reviews.md
    code-style.md
    documentation.md
  plans/
    README.md
    PLAN_TEMPLATE.md
```

Do not import these yet:

- `.agents/archive/`
- `.agents/context/`
- `.agents/reports/`
- `.agents/skills/`
- release, deployment, operations, OpenAPI, frontend, benchmark, and Spring-specific guides
- task prompts that assume the source repo's build, API, CI, or release process

## What To Adapt

### `AGENTS.md`

Keep this as the short AI entry point. Add adapted rules from the source repo:

- use the smallest task-shaped context
- identify the behavior and governing artifact before editing
- update specs/docs before or alongside behavior changes
- run validation matching the diff
- review for bugs, missing validation, and API/IDE compatibility risk
- commit completed work when the user asks for commits or the task scope requires it

Avoid importing the source repo's REST/OpenAPI/operations priority order. Replace it with this plugin-specific priority order:

1. current user request
2. `TASKS.md` and accepted plans
3. IntelliJ Platform API constraints and plugin descriptor behavior
4. README/user-facing plugin behavior
5. ADRs and AI guidance

### `docs/WORKING_WITH_AI.md`

Create a human-facing guide explaining how to ask AI to work on this plugin.

Include request shapes such as:

```text
Task:
Goal:
Target IDE version:
Target artifacts:
Constraints:
Validation expected:
```

### `.agents/references/execution.md`

Adapt the source execution loop into a compact version:

1. frame behavior
2. identify owner artifact
3. update docs/specs if behavior changed
4. implement smallest change
5. run targeted validation
6. self-review
7. report evidence

### `.agents/references/code-style.md`

Make this IntelliJ plugin-specific:

- Kotlin preferred
- follow IntelliJ Platform SDK conventions
- keep action code small and explicit
- avoid compile-time dependency on proprietary JetBrains AI Assistant APIs unless explicitly approved
- use the IntelliJ action system for AI Assistant integration
- prefer existing VCS commit workflow APIs
- do not add broad abstractions before there is repetition

### `.agents/references/testing.md`

Replace Spring validation with plugin validation:

- `gradle buildPlugin`
- `gradle verifyPlugin` when configured
- `gradle runIde` for sandbox testing
- IntelliJ Plugin Verifier for supported IDE ranges
- manual sandbox checks for:
  - tracked modified file
  - unversioned file
  - Commit only
  - Commit and Push
  - AI Assistant unavailable
  - Git staging area enabled/disabled

### `.agents/references/reviews.md`

Keep the source review priority model, adapted to plugin risks:

1. incorrect commit selection or unintended commit
2. AI Assistant invocation failures
3. committing before AI generation is complete
4. push behavior mismatch
5. IntelliJ API compatibility or internal API risk
6. missing sandbox validation

### `docs/decisions/`

Add ADRs only for durable choices. The first likely ADRs are:

- minimum supported IntelliJ Platform version
- runtime-discovered AI Assistant action vs direct API dependency
- separate `AI Commit & Push All` button vs reusing an existing push-selected state

## Suggested First Import Task

Create only the lightweight guidance skeleton:

- update `AGENTS.md`
- add `docs/WORKING_WITH_AI.md`
- add `.agents/references/execution.md`
- add `.agents/references/testing.md`
- add `.agents/references/reviews.md`
- add `.agents/references/code-style.md`
- add `.agents/references/documentation.md`

Leave plans, ADRs, skills, release, and operations guides for later.

## Acceptance Criteria

- Guidance fits this repo's current size.
- No Spring/API/OpenAPI/release-specific rules are copied into this plugin repo.
- Future AI agents can identify what to read before planning, implementing, validating, and reviewing.
- Validation guidance names IntelliJ plugin commands and manual sandbox checks.
- The import does not imply implementation has started.

