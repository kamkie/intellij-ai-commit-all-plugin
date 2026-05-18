---
status: accepted
date: 2026-05-18
accepted_at: 2026-05-18T11:21:19+02:00
decision-makers: Kamil Kiewisz <kamkie@outlook.com>
consulted: Codex
informed: Repository contributors
---

# Require Plan Execution Graphs

## Context and Problem Statement

ADR 0026 defines orchestrator and worker roles, and the proposed worker-count metadata decision makes maximum worker count explicit. Plans still do not have to visualize who executes which task, in which wave, under which orchestrator, and in which agent mode.

`docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` finding S006 proposes a required execution graph section for plan files.

## Decision Drivers

* Make task sequencing, parallel waves, and handoffs visible in plans.
* Align plan structure with worker count, agent mode, commit trailers, and synchronization logs.
* Keep graph syntax reviewable in plain Markdown.
* Require a structural section now while deferring deeper graph validation.
* Preserve ADR 0026's disjoint write scope requirement for parallel tasks.

## Considered Options

* Require Mermaid execution graphs in every plan
* Require execution graphs only for parallel plans
* Keep execution topology in prose only

## Decision Outcome

Chosen option: "Require Mermaid execution graphs in every plan", because a consistent graph section makes sequencing and handoff expectations visible for both sequential and parallel execution.

Every plan includes an `## Execution Graph` section with a fenced Mermaid graph. Simple sequential plans may use a compact graph, but the section must exist.

Execution graphs must follow these rules:

* Each orchestrator node is labeled as `O<n>`.
* Each worker node is labeled as `W<n>`.
* Each worker node includes its planned agent mode from the multi-agent commit attribution vocabulary: `code`, `fast-code`, `setup`, `advanced-chat`, `run-verify`, `niche`, or `chat`.
* The graph encodes task assignment by plan task id or stable task label.
* The graph encodes wave or sequence ordering and orchestrator handoff edges.
* Parallel waves shown in the graph must match the `Workers:` field and the disjoint write scopes required by ADR 0026.

Implementation updates `.agents/plans/PLAN_TEMPLATE.md`, `.agents/plans/README.md`, `.agents/references/planning.md`, and `scripts/validate-docs.ps1`. Existing plan files that validation scans are backfilled. Validation requires the section in every plan file; deeper structural validation of nodes and edges may be deferred.

### Consequences

* Good, because reviewers can inspect task order and parallel waves without reconstructing them from prose.
* Good, because graph labels give worker ids and agent modes a planned home.
* Good, because validation can start with a simple required-section check.
* Bad, because every existing plan scanned by validation needs a graph backfill.
* Bad, because Mermaid graph structure can become stale if the plan changes and the graph is not updated.

### Confirmation

Compliance is checked by documentation review and `scripts/validate-docs.ps1` after acceptance:

* Plan template and guidance describe `## Execution Graph`.
* Active and archived plans scanned by validation contain the section.
* Validation rejects plans missing the section.
* Parallel graphs match the declared worker count and disjoint write scope notes during review.

## Pros and Cons of the Options

### Require Mermaid execution graphs in every plan

* Good, because every plan has the same place to describe execution topology.
* Good, because Mermaid is already readable in Markdown and common in repository docs.
* Good, because sequential plans can keep the graph small.
* Bad, because graph maintenance adds another plan consistency point.

### Require execution graphs only for parallel plans

* Good, because sequential plans avoid extra structure.
* Bad, because absence is ambiguous when a plan later grows parallel work.
* Bad, because reviewers must infer whether no graph means sequential execution or missing metadata.

### Keep execution topology in prose only

* Good, because plans stay shorter.
* Bad, because parallelism, handoffs, and modes are harder to scan.
* Bad, because structural validation has no consistent section to check.

## More Information

- Source proposal finding: `docs/proposals/archive/PROP-04-multi-agent-execution-2026-05-15T09-57.md` S006.
- Related decisions: ADR 0023, ADR 0026, the proposed worker-count metadata ADR, and the proposed multi-agent commit attribution ADR.
