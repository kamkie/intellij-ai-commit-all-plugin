# Change Closeout

Check whether a completed ordinary change is ready for handoff or commit, without treating it as a release-readiness pass.

## Read First

- `AGENTS.md`
- `.agents/references/execution.md`
- `.agents/references/testing.md`
- `.agents/references/reviews.md`
- `.agents/references/documentation.md`
- `.agents/prompts/README.md`
- this prompt
- the current diff, named task, plan, ADR, validation output, or changed files supplied by the user

Load source files, tests, owner docs, task entries, plans, ADRs, changelog, or support docs only when the diff or named artifact shows they may be affected.
Load `.agents/references/releases.md` only when release, signing, Marketplace, public changelog, or artifact publication readiness is in scope.

## Output

Return a closeout note with:

- change boundary and whether the diff matches the requested scope
- docs, specs, tasks, plans, ADRs, support, changelog, prompt, skill, or reference updates that are required, complete, missing, or not applicable
- validation commands already run and their results
- skipped validation with concrete reasons and remaining risk
- self-review findings: bugs, regressions, compatibility concerns, unsupported behavior claims, or missing tests
- commit readiness, including whether commits are allowed or required by the current request or approved plan
- follow-up actions that must happen before handoff, before commit, or later

For approved plan work, include plan status and task-result evidence that must be current before the next task or commit.

## Non-Goals

- Do not perform release readiness unless the user asks for a release boundary.
- Do not create a commit unless the user asks for it or the approved plan requires it.
- Do not mark tasks, plans, or ADR work complete unless validation and review evidence support that state.
- Do not paste raw test output, logs, or worker transcripts into the closeout note.
