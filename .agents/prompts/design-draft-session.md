# Design Draft Session

Explore repository concept graphics or UI design drafts before implementation.
Use this when the user asks for a design-only pass, visual variants, image or SVG comparison, state coverage, color alignment, or draft selection for plugin UI.

## Read First

- `AGENTS.md`
- `.agents/references/documentation.md`
- `docs/concepts/graphics/README.md`
- `.agents/prompts/README.md`
- this prompt
- the named draft file, image attachment, ADR, proposal, or concept directory from the user request

Load production source, `plugin.xml`, build files, ADRs, or proposals only when the user explicitly connects the draft to implementation or an accepted decision.

## Output

Produce a design-session report with:

- source prompt, permission boundary, and whether the session is design-only
- draft files or images inspected
- variants created or changed, with file paths
- state coverage checked, such as passive, hover, clicked/running, disabled, light, and dark modes
- visual risks, including IntelliJ style mismatch, spacing, divider, icon, color, or animation issues
- recommendation for the selected draft or the next design pass
- validation run, or the reason validation was deferred

When the user explicitly asks for a design-only session, keep changes limited to concept drafts and do not run validation until the user ends that mode or asks for checks.

## Non-Goals

- Do not implement production plugin UI from concept drafts unless the user separately asks.
- Do not update ADR decisions, proposal decisions, plans, or tasks from visual preference alone.
- Do not treat generated or drafted concept graphics as final plugin assets without an implementation pass.
- Do not run broad repository validation during a design-only session unless the user asks.
