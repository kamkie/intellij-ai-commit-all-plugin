---
status: accepted
date: 2026-05-30
accepted_at: 2026-05-30T13:59:32+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: OpenAI Codex
informed: Repository contributors
---

# Incorporate Karpathy-Style Agent Rules

## Context and Problem Statement

The user asked the repository to analyze the external
`multica-ai/andrej-karpathy-skills` repository and propose incorporating rules
from its `CLAUDE.md`. That repository packages four agent behavior principles:
think before coding, simplicity first, surgical changes, and goal-driven
execution. This repository already has an AI guidance model with `AGENTS.md` as
the entry point and durable workflow guidance under `.agents/references/`.

How should the repository incorporate the useful parts of those external rules
without adding duplicate instruction files, conflicting with existing ADR and
plan gates, or weakening IntelliJ plugin safety requirements?

## Decision Drivers

* Keep `AGENTS.md` as the single repository AI entry point.
* Keep durable AI-agent workflow guidance in `.agents/references/`.
* Reduce common agent mistakes: hidden assumptions, overcomplication,
  drive-by refactors, and unverified completion.
* Preserve existing ADR, plan, delegation, validation, and dirty-worktree rules.
* Preserve IntelliJ plugin safety requirements for AI Assistant, VCS, commit,
  and push failure handling.
* Avoid copying generic external guidance or tool-specific packaging that this
  repository does not use.

## Considered Options

* Adapt the four principles into existing repository guidance owners.
* Import the upstream `CLAUDE.md` or skill text verbatim.
* Add Claude and Cursor packaging from the upstream repository.
* Keep current guidance unchanged.

## Decision Outcome

Chosen option: "Adapt the four principles into existing repository guidance owners", because it keeps the repository's current guidance architecture while adding the most useful behavior constraints in project-specific terms.

If accepted, implement these guidance changes:

* Do not add a root `CLAUDE.md`, `.claude-plugin/`, or `.cursor/rules/` package
  for this change. This repository continues to use `AGENTS.md` plus
  `.agents/references/` as the governed AI guidance model.
* Update `.agents/references/execution.md` so non-trivial work records or
  communicates material assumptions, tradeoffs, success criteria, and expected
  validation before implementation. Trivial one-liners may stay lightweight.
* Update `.agents/references/execution.md` stop-condition wording so agents stop
  on ambiguity that affects behavior, write scope, validation, ADR gates, plan
  gates, or user intent, while still allowing reasonable low-risk assumptions
  under the existing direct one-off path.
* Update `.agents/references/code-style.md` to make speculative features,
  single-use abstractions, unnecessary configurability, and unrelated cleanup
  out of scope unless requested or required by an accepted governing artifact.
* Update `.agents/references/code-style.md` to require changed lines to trace to
  the user request, governing artifact, validation fix, or cleanup caused by the
  current change. Pre-existing dead code should be mentioned, not deleted,
  unless requested.
* Update `.agents/references/testing.md` to connect success criteria to
  validation: bug fixes should include or identify a reproduction when
  practical, refactors should preserve behavior with before-and-after
  validation, and skipped checks need concrete reasons.
* Update `.agents/references/reviews.md` to add review prompts for hidden
  assumptions, over-abstraction, speculative scope, unrelated edits, and weak or
  missing verification.
* Word any "no extra error handling" guidance narrowly. It must not remove or
  discourage documented handling for AI Assistant, IDE, VCS, commit, push,
  timeout, or compatibility failure states.

This decision does not change the ADR gate, plan approval gate, approved-plan
sub-agent requirement, standing delegation approval, validation command
ownership, changelog ownership, or the rule that agents must not revert
unrelated user changes.

### Consequences

* Good, because agents get clearer prompts to surface assumptions and
  verification before costly mistakes.
* Good, because future diffs should contain fewer speculative abstractions,
  drive-by refactors, and unrelated formatting changes.
* Good, because the external guidance is adapted to the repository's existing
  owner model instead of creating parallel instruction files.
* Good, because review guidance gains concrete checks for scope and verification
  quality.
* Bad, because the repository guidance becomes slightly more prescriptive for
  non-trivial tasks.
* Bad, because agents must apply judgment to distinguish low-risk assumptions
  from ambiguity that should stop work.

### Confirmation

After acceptance, confirm implementation by checking:

* No root `CLAUDE.md`, `.claude-plugin/`, or `.cursor/rules/` package was added
  for this change.
* `.agents/references/execution.md` includes project-specific assumption,
  tradeoff, success-criteria, and ambiguity-stop guidance.
* `.agents/references/code-style.md` includes project-specific simplicity and
  surgical-change guidance.
* `.agents/references/testing.md` connects success criteria to validation for
  bug fixes, refactors, and skipped checks.
* `.agents/references/reviews.md` includes review prompts for hidden
  assumptions, over-abstraction, speculative scope, unrelated edits, and weak
  verification.
* The wording preserves required IntelliJ plugin failure handling.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/ai/validate-agent-artifacts.ps1`
  passes.
* `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/validate-docs.ps1`
  passes.
* `git diff --check` passes.

## Pros and Cons of the Options

### Adapt the four principles into existing repository guidance owners

This option incorporates the substance of the external guidance while keeping
the repository's existing artifact ownership and validation model.

* Good, because it avoids duplicate instruction entry points.
* Good, because the rules can be phrased around this plugin's actual ADR, plan,
  VCS, commit, push, AI Assistant, validation, and review risks.
* Good, because future implementation can be a bounded documentation update.
* Bad, because it requires careful wording instead of a simple file copy.

### Import the upstream `CLAUDE.md` or skill text verbatim

This option copies the external guidance directly into the repository.

* Good, because it is fast and preserves the upstream wording.
* Bad, because a root `CLAUDE.md` would duplicate `AGENTS.md` as the AI entry
  point.
* Bad, because the upstream text is generic and does not account for this
  repository's ADR gates, plan gates, validation owners, or IntelliJ plugin
  safety rules.
* Bad, because verbatim generic guidance would likely drift from local owner
  documents.

### Add Claude and Cursor packaging from the upstream repository

This option imports the upstream `.claude-plugin/`, skill, and Cursor rule
packaging.

* Good, because it would make the guidance reusable in those tools.
* Bad, because the current request is to incorporate rules into this repository,
  not publish a reusable guidance package.
* Bad, because this repository already owns reusable agent workflows under
  `.agents/skills/` and durable guidance under `.agents/references/`.
* Bad, because adding tool-specific packaging would expand maintenance scope
  without a current repository need.

### Keep current guidance unchanged

This option declines the external guidance.

* Good, because the repository already contains context discipline,
  smallest-change, validation, and review guidance.
* Good, because no documentation churn is required.
* Bad, because the current guidance does not state the four principles as
  directly or consistently across execution, style, testing, and review owners.
* Bad, because agents could still miss the connection between success criteria,
  surgical edits, simplicity, and verification.

## More Information

Source reviewed: `https://github.com/multica-ai/andrej-karpathy-skills`,
including `CLAUDE.md`, `README.md`, `skills/karpathy-guidelines/SKILL.md`,
`.cursor/rules/karpathy-guidelines.mdc`, `CURSOR.md`, `.claude-plugin/plugin.json`,
and the example catalog.

Related repository guidance:

* `AGENTS.md` defines the repository AI entry point and artifact lookup rules.
* `.agents/references/documentation.md` owns documentation changes and
  validation commands.
* `.agents/references/execution.md` owns execution routing and stop conditions.
* `.agents/references/code-style.md` owns implementation style and design
  discipline.
* `.agents/references/testing.md` owns validation expectations.
* `.agents/references/reviews.md` owns review output and risk prompts.

No companion implementation plan is used because the implementation is a
bounded AI-agent documentation update after acceptance.

After this ADR is accepted, update the ADR Implementation Tracker in
`docs/decisions/README.md` with implementation status, evidence, and last
updated date.
