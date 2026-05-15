---
proposal_id: PROP-remove-tasks-md-when-empty
generated_at: 2026-05-15T06-49
purpose: Propose removing `TASKS.md` once every listed task is completed, and redirecting backlog references to the remaining governing artifacts.
scope: Repository root `TASKS.md`, references to it in `AGENTS.md`, `docs/WORKING_WITH_AI.md`, `.agents/references/`, and any other files that point readers at the backlog.
---

# Remove TASKS.md When All Tasks Are Finished Proposal

This proposal respects `AGENTS.md`, `TASKS.md`, `docs/decisions/OPEN_QUESTIONS.md`, and `docs/decisions/`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Progress Tracker](#progress-tracker)
- [How To Edit The Trackers](#how-to-edit-the-trackers)
- [Errors And Mistakes](#errors-and-mistakes)
    - [E1. Define the "all tasks finished" trigger for removal](#e1-define-the-all-tasks-finished-trigger-for-removal)
    - [E2. Remove `TASKS.md` once the trigger is met](#e2-remove-tasksmd-once-the-trigger-is-met)
    - [E3. Update references to `TASKS.md` across the repository](#e3-update-references-to-tasksmd-across-the-repository)
    - [E4. Preserve completed task history before removal](#e4-preserve-completed-task-history-before-removal)
    - [E5. Decide the future home for any new backlog items](#e5-decide-the-future-home-for-any-new-backlog-items)
- [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
- [Simplification Opportunities](#simplification-opportunities)
- [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- `TASKS.md` currently has an empty `Open Backlog` and a large `Completed Task Archive`; once any future open tasks are also closed, the file no longer carries active information and can be retired.
- Triage scope is the trigger condition for removal, the removal itself, the update of every reference to `TASKS.md`, and the decision on where new backlog items would live afterwards.
- This proposal performs no implementation; each finding is advisory until accepted via the normal ADR/plan flow defined in `docs/decisions/README.md` and `.agents/references/planning.md`.

## Progress Tracker

Compact overview only. Edit the YAML tracker inside each section below; this table mirrors statuses at a glance.

| Id | Title                                                 | Priority | Status   | Decision |
|----|-------------------------------------------------------|----------|----------|----------|
| E1 | Define the "all tasks finished" trigger for removal   | 1        | deferred | deferred |
| E2 | Remove `TASKS.md` once the trigger is met             | 2        | deferred | deferred |
| E3 | Update references to `TASKS.md` across the repository | 2        | deferred | deferred |
| E4 | Preserve completed task history before removal        | 3        | deferred | deferred |
| E5 | Decide the future home for any new backlog items      | 3        | deferred | deferred |

## How To Edit The Trackers

- Edit the fenced `yaml` block inside the finding section.
- Mirror `status`, `decision`, and `priority` to the row above.
- Bump `updated` to the current date.
- Leave completed or rejected findings in place as history.

## Errors And Mistakes

### E1. Define the "all tasks finished" trigger for removal

- Evidence: `TASKS.md` line 15 states "No open implementation tasks remain from `PLAN-fastest-plan-execution`." while lines 17-186 form the `## Completed Task Archive`. There is no documented condition that authorises retiring the file.
- Impact: Without an explicit trigger, the file may be removed prematurely (while a plan still has open `T-AREA-NNN` tasks) or kept indefinitely after it becomes purely historical.
- Proposal: Adopt the rule "remove `TASKS.md` only when both (a) `## Open Backlog` contains no `[ ]` task, and (b) no active plan under `.agents/plans/` references a `T-AREA-NNN` ID that is not marked `[x]`". Capture the rule in an ADR before implementation per `docs/decisions/README.md`.

```yaml
status: deferred
decision: deferred
priority: 1
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-03-repository-quality-lifecycle E009`."
```

### E2. Remove `TASKS.md` once the trigger is met

- Evidence: `TASKS.md` exists at the repository root and is the only file under `## Open Backlog` that the workflow currently inspects for pending work.
- Impact: After E1's trigger fires, keeping `TASKS.md` adds noise to the root, duplicates information already preserved in plans and ADRs, and forces every agent to load it per `AGENTS.md` "Guidance Map".
- Proposal: Delete `TASKS.md` in a dedicated change after E1 is accepted and E3/E4 are prepared. The change should be a single commit that removes the file and lands together with the reference updates from E3.

```yaml
status: deferred
decision: deferred
priority: 2
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-03-repository-quality-lifecycle E012`."
```

### E3. Update references to `TASKS.md` across the repository

- Evidence: `TASKS.md` is referenced by `AGENTS.md` (`Guidance Map`, `Priority Order`, `Working Rules`), `docs/WORKING_WITH_AI.md` (lines 42 and 94), `docs/proposals/README.md` ("Do not create a proposal for: Backlog items; use `TASKS.md`"), and likely `.agents/references/` files such as `planning.md` and `execution.md`.
- Impact: Removing the file without updating these references would leave dead links and contradictory guidance; agents would still be instructed to consult a non-existent backlog.
- Proposal: As part of the same change that removes `TASKS.md`, update every reference to either (a) point at the new backlog home chosen in E5, or (b) state that the backlog is empty and direct readers to plans/ADRs. Use a repository-wide search for `TASKS.md` to enumerate all touch points before editing.

```yaml
status: deferred
decision: deferred
priority: 2
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-03-repository-quality-lifecycle E012`."
```

### E4. Preserve completed task history before removal

- Evidence: `TASKS.md` lines 17-186 are the only consolidated index of completed `T-AREA-NNN` IDs and the plans/ADRs that produced them. The IDs are explicitly declared stable in ARD-0028.
- Impact: Plain deletion would erase the cross-reference between task IDs and the plans/ADRs they belong to, weakening traceability required by `.agents/references/execution.md` and ARD-0023.
- Proposal: Before removal, either (a) move the `## Completed Task Archive` section into `CHANGELOG.md` under an "Archived Backlog" appendix, or (b) relocate it to `docs/history/completed-tasks.md`. Keep all `T-AREA-NNN` IDs verbatim so historical references in commits and ADRs remain resolvable. Decide the destination in the ADR opened by E1.

```yaml
status: deferred
decision: deferred
priority: 3
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-03-repository-quality-lifecycle E010`."
```

### E5. Decide the future home for any new backlog items

- Evidence: `AGENTS.md` "Guidance Map" routes "Implementation backlog" to `TASKS.md`. `docs/proposals/README.md` routes "Backlog items" away from proposals to `TASKS.md`. No alternative is documented.
- Impact: After removal, new implementation work has no documented intake. Agents may default to creating ad hoc lists in plans or proposals, contradicting `AGENTS.md` priority order.
- Proposal: Choose one of:
    1. Treat plans under `.agents/plans/` as the sole backlog; new work starts as a plan after an ADR or proposal is accepted.
    2. Recreate `TASKS.md` lazily, only when at least one new `T-AREA-NNN` task exists; document this in the same ADR opened by E1.
       Pick one explicitly so `AGENTS.md`, `docs/WORKING_WITH_AI.md`, `docs/proposals/README.md`, and `.agents/references/` can be updated consistently in E3.

```yaml
status: deferred
decision: deferred
priority: 3
owner:
updated: 2026-05-15
comment: "Consolidated into `PROP-03-repository-quality-lifecycle E011`."
```

## Duplications To Remove Or Reduce

_No tracked findings._

## Simplification Opportunities

_No tracked findings._

## Smaller / Stylistic Items

- If option E5.2 (lazy recreation) is chosen, add a short note to `AGENTS.md` describing the recreate-on-demand rule so the file's absence is not mistaken for an oversight.
- Consider whether `docs/decisions/OPEN_QUESTIONS.md` should also be re-pointed if any open question currently references a `T-AREA-NNN` task ID.

## Suggested Priority Order

1. `E1` Trigger definition - nothing else can proceed without an agreed condition; needs an ADR.
2. `E4` History preservation - decide and prepare the archive location before deletion lands.
3. `E5` Future backlog home - required input for E3 wording.
4. `E3` Reference updates - executed together with E2 in a single commit to avoid dead links.
5. `E2` File removal - last step, gated on E1 trigger being met.

## Out Of Scope

- Renaming or restructuring `.agents/plans/`, `docs/decisions/`, or `docs/proposals/`.
- Changing the stable `T-AREA-NNN` ID format defined by ARD-0028.
- Editing historical commit messages that reference `TASKS.md`.
- Any implementation work; this proposal stops at maintainer triage per `docs/proposals/README.md`.
