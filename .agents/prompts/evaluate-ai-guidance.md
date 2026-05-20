# Evaluate AI Guidance

Evaluate the live repository AI guidance for lifecycle conformance, owner clarity, non-current references, duplication, and context-load cost.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `docs/DEVELOPMENT_LIFECYCLE.md`
- `.agents/prompts/README.md`
- this prompt

Load owner guides, active plans, human-facing docs, skills, templates, or archives only when needed to answer a specific finding.
Do not bulk-load `.agents/archive/`, `.agents/plans/archive/`, `docs/proposals/archive/`, or `TASKS_ARCHIVE.md`.

## Output

Write `temp/evaluate-ai-guidance-<timestamp>.md` unless the user explicitly asks for a tracked proposal or another location.
When no additional arguments are passed, report only on the current repository state.
Do comparison or trend analysis only when the user provides explicit git references, such as commits, tags, branches, or a range.

Evaluate:

- ownership clarity and single-source guidance
- lifecycle phase, trigger, artifact, owner, and gate coverage
- ADR, proposal, plan, task, validation, review, release, and changelog routing
- default-load discipline, practical read sets, prompt quality, skill boundaries, active-plan hygiene, and stale-reference risk
- contradictions, policy duplication, missing routing, and recommendations with owner file, expected benefit, context impact, implementation risk, and validation needed

Report shape:

- assessment date, branch, commit boundary, and whether uncommitted changes were included
- comparison boundary only when explicit git references were provided
- method and evidence sources
- executive summary with grade, top risks, and top recommendations
- lifecycle conformance matrix summarized from repository guidance
- context-size and read-set findings, using approximate estimates when no measurement script exists
- file-by-file findings for important owner guides, prompts, and skills
- `do now`, `defer`, and `do not do` recommendations
- caveats about approximate token estimates, inferred load behavior, and historical archive material

## Non-Goals

- Do not implement recommendations during this prompt.
- Do not create ADRs, proposals, plans, or tasks unless the user separately asks.
- For report-only work under `temp/`, do not run builds, tests, Gradle tasks, or heavyweight validation.
- If tracked files are edited after a separate user request, follow `.agents/references/documentation.md` validation rules.
