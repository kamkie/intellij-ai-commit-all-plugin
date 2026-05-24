---
proposal_id: PROP-marketplace-realtime-progress-media
generated_at: 2026-05-24T15-07
created_from: User request to compare JetBrains Marketplace plugin pages and propose extending the description plus animated GIF/PNG media to show real-time progress.
purpose: Propose Marketplace description and media improvements that show AI Commit All workflow progress clearly before first publication.
scope: Covers generated Marketplace description copy, repository-owned visual assets, and release-readiness handling for Marketplace media.
---

# Marketplace Real-Time Progress Media

This proposal respects `AGENTS.md`, `README.md`, `docs/user-guide.md`, `docs/specification.md`, `docs/validation/release-checklist.md`, and `docs/proposals/README.md`. It lists findings for maintainer triage only; it does not implement changes by itself.

## Table of Contents

- [Summary](#summary)
- [Creation Context](#creation-context)
- [Progress Tracker](#progress-tracker)
- [Proposal Items](#proposal-items)
  - [New Features](#new-features)
    - [F001. Add real-time progress copy to the generated Marketplace description](#f001-add-real-time-progress-copy-to-the-generated-marketplace-description)
    - [F002. Produce Marketplace-ready workflow GIF and PNG media](#f002-produce-marketplace-ready-workflow-gif-and-png-media)
    - [F003. Add release validation for Marketplace media upload and rendering](#f003-add-release-validation-for-marketplace-media-upload-and-rendering)
  - [Errors And Mistakes](#errors-and-mistakes)
  - [Duplications To Remove Or Reduce](#duplications-to-remove-or-reduce)
  - [Simplification Opportunities](#simplification-opportunities)
  - [Smaller / Stylistic Items](#smaller--stylistic-items)
- [Suggested Priority Order](#suggested-priority-order)
- [Out Of Scope](#out-of-scope)

## Summary

- The current generated Marketplace description is accurate, but its `Visuals` section only links to small user-guide control assets instead of presenting the workflow as a first-viewport Marketplace media experience.
- The `.ignore` and AsciiDoc Marketplace pages both use prominent top media before or near the description body. `.ignore` leads with wide IDE screenshots, while AsciiDoc leads with an IDE screenshot and then includes a video thumbnail near the quick links.
- JetBrains Marketplace listing guidance treats media as the first element users see, recommends feature screenshots, allows GIFs for action, and recommends at least `1200 x 760` screenshot dimensions. The current `AI Commit All` control PNG/GIF assets are `214 x 54`, so they are useful in the user guide but not sufficient as Marketplace media.
- Proposed direction: keep the existing precise control assets, add a Marketplace-ready animated workflow GIF plus static PNG fallback(s), and extend the generated description with a concise real-time progress section.
- No implementation is performed by this proposal.

## Creation Context

- Why this proposal exists: The request asked to look at `https://plugins.jetbrains.com/plugin/7495--ignore` and `https://plugins.jetbrains.com/plugin/7391-asciidoc`, then propose extending the description and animated GIF/PNG media to show real-time progress.
- How it was created: Compared the two plugin pages in a browser, reviewed JetBrains Marketplace listing guidance, inspected `config/intellij-platform/description.html`, `scripts/generate-intellij-platform-description.ps1`, `README.md`, `docs/user-guide.md`, and existing assets in `docs/assets/user-guide/`.
- Scope guardrails: The proposal does not change runtime plugin behavior. Observable workflow progress must stay aligned with `docs/specification.md`, especially `REQ-UI-009`, `REQ-UI-008`, `REQ-PUSH-009`, and the existing `AI | Commit | Push` behavior.

## Progress Tracker

Compact overview only. The metadata table inside each finding remains the source of truth; this table mirrors statuses at a glance. Tracker mirroring, status and decision vocabulary, and Proposal Implementation Summary updates live in `docs/proposals/README.md`.

| Id   | Title                                                    | Priority | Status | Decision |
|------|----------------------------------------------------------|----------|--------|----------|
| F001 | Add real-time progress copy to the generated description | 2        | done   | accepted |
| F002 | Produce Marketplace-ready workflow GIF and PNG media     | 4        | done   | accepted |
| F003 | Add release validation for media upload and rendering    | 3        | done   | accepted |

## Proposal Items

### New Features

#### F001. Add real-time progress copy to the generated Marketplace description

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T15:19:31+02:00 |
| Priority    | 2                         |
| Owner       |                           |
| Updated     | 2026-05-24T15:19:31+02:00 |

##### Context

- Evidence: `config/intellij-platform/description.html` currently has a concise feature summary, requirements, a `Visuals` section, and source/license links. The `Visuals` section points to `docs/assets/user-guide/` instead of describing what the running user sees.
- Evidence: `scripts/generate-intellij-platform-description.ps1` generates the description from `README.md` and `docs/user-guide.md`, so copy changes should flow through source docs and the generator rather than hand-editing `config/intellij-platform/description.html`.
- Evidence: `docs/specification.md` defines real-time phase progression: `AI` mode stays on `AI`; `Commit` mode advances `AI -> Commit`; `Push` mode advances `AI -> Commit -> Push`, with controls disabled while a plugin-owned workflow runs.
- Impact: Users who find the Marketplace listing will understand the three sections but may not immediately understand that the control shows live progress across AI generation, commit, and push.
- Non-goals:
  - Do not add new progress states or change runtime control behavior.
  - Do not claim custom push or AI status details that the IDE or AI Assistant owns.
- Acceptance criteria:
  - The generated Marketplace description includes a short `Real-Time Progress` or equivalent section.
  - The text explains the visible phase movement and disabled running state without exceeding the current concise Marketplace tone.
  - `scripts/generate-intellij-platform-description.ps1 -Check` passes after regeneration.
  - The description remains consistent with `README.md`, `docs/user-guide.md`, and `docs/specification.md`.

##### Recommended Change

Add a short source paragraph to `docs/user-guide.md` or `README.md` that the generator can consume, then update `scripts/generate-intellij-platform-description.ps1` to emit copy like:

```html
<h3>Real-Time Progress</h3>
<p>The running section shows where the workflow is now: AI while the message is generated, Commit while the IDE commit runs, and Push while the push step is in progress. The control is disabled until the current run finishes.</p>
```

Keep the wording factual and observable. Pair the copy with F002 media instead of trying to embed all visual detail in text.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `docs/user-guide.md`, `scripts/generate-intellij-platform-description.ps1`, and `config/intellij-platform/description.html`.
- Validation: `pwsh -NoProfile -ExecutionPolicy Bypass -File scripts/generate-intellij-platform-description.ps1`; final validation recorded in handoff.

#### F002. Produce Marketplace-ready workflow GIF and PNG media

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T15:19:31+02:00 |
| Priority    | 4                         |
| Owner       |                           |
| Updated     | 2026-05-24T15:19:31+02:00 |

##### Context

- Evidence: The existing user-guide assets are `214 x 54`: `ai-commit-all-control-light.png`, `ai-commit-all-control-dark.png`, and `ai-commit-all-control-running.gif`. They show the control itself, not the plugin in a realistic Commit tool window workflow.
- Evidence: The `.ignore` Marketplace page gives its media carousel prime placement with wide IDE screenshots before the long feature list.
- Evidence: The AsciiDoc Marketplace page places a large IDE screenshot first and a video thumbnail near the quick links and getting-started content.
- Evidence: JetBrains Marketplace listing guidance says the media section is the first element users see, recommends screenshots that display plugin features, recommends minimum `1200 x 760` screenshots, and allows a `.gif` to show action.
- Impact: A full-width animated GIF or PNG set would make the plugin's main differentiator visible immediately: one control progresses through AI, commit, and push in the existing IDE commit workflow.
- Non-goals:
  - Do not replace the small user-guide control assets; they remain useful for exact control rendering.
  - Do not include private repository names, credentials, remotes, tokens, or real user code in media.
  - Do not use marketing-style slides as the only media; the first asset should show the actual IDE workflow.
- Acceptance criteria:
  - Add `docs/assets/marketplace/` with repository-owned source assets for Marketplace upload.
  - Provide at least one animated GIF showing a realistic run: idle control, `AI` running, `Commit` running, `Push` running, and completed idle state.
  - Provide at least one static PNG fallback that is clear when GIF animation is unavailable, preferably a composed frame or sequence showing the same `AI -> Commit -> Push` progression.
  - Use a consistent aspect ratio and dimensions suitable for Marketplace media, with a target of at least `1200 x 760`.
  - Capture the relevant JetBrains IDE interface, especially the Commit tool window and `AI | Commit | Push` control.
  - Keep all visible text legible at Marketplace page size.
  - Document the capture or generation command so the assets can be refreshed.

##### Recommended Change

Create a Marketplace media pack with these files:

- `docs/assets/marketplace/ai-commit-all-realtime-progress.gif`
- `docs/assets/marketplace/ai-commit-all-realtime-progress.png`
- Optional: `docs/assets/marketplace/ai-commit-all-realtime-progress-dark.png`

Preferred storyboard:

1. Start in a Git project with eligible changes visible in the Commit tool window.
2. Activate `Push` so the full workflow can demonstrate all three phases.
3. Show `AI` running while AI Assistant generates the message.
4. Show `Commit` running after AI generation completes and the IDE commit step starts.
5. Show `Push` running while the push step is active.
6. End with the control idle again after completion.

Use the existing runtime Swing rendering or deterministic UI harness where possible. If live AI Assistant capture is impractical, use the test-only AI Assistant substitute or sandbox fixture, but keep the resulting media visually representative of the user-facing IDE workflow.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `docs/assets/marketplace/`, `docs/assets/marketplace/README.md`, and `src/test/kotlin/pl/devopssolutions/aicommitall/actions/AiCommitAllControlAssetGeneratorTest.kt`.
- Validation: Focused asset-generator test and asset dimension check; final validation recorded in handoff.

#### F003. Add release validation for Marketplace media upload and rendering

| Field       | Value                     |
|-------------|---------------------------|
| Status      | done                      |
| Decision    | accepted                  |
| Decision at | 2026-05-24T15:19:31+02:00 |
| Priority    | 3                         |
| Owner       |                           |
| Updated     | 2026-05-24T15:19:31+02:00 |

##### Context

- Evidence: JetBrains Marketplace media is uploaded through the plugin page admin media section after the plugin exists on Marketplace, while the description is extracted from plugin metadata.
- Evidence: `CHANGELOG.md` and `config/intellij-platform/change-notes.html` already note generated Marketplace metadata and reviewed control visuals, but there is no release checklist item that verifies Marketplace media upload and web or IDE rendering.
- Impact: Without a release checklist step, the repository can contain good media while the public Marketplace listing still ships without it or with broken/static rendering.
- Non-goals:
  - Do not store Marketplace credentials or admin-only URLs in repository docs.
  - Do not perform the first upload or publication as part of this proposal.
- Acceptance criteria:
  - `docs/validation/release-checklist.md` includes a Marketplace media readiness step before publication.
  - The step asks the release operator to upload the accepted GIF/PNG media, verify the web listing, and verify the IDE Marketplace view when available.
  - The release evidence report can record media filenames, upload status, and rendering observations without exposing credentials.

##### Recommended Change

Add a release checklist row or subsection for `Marketplace media` that references `docs/assets/marketplace/` and records:

- Files uploaded.
- Web listing rendering check.
- IDE Marketplace rendering check.
- Any fallback used when animated GIF rendering is static.

##### Review Notes

- none

##### Follow-Up

- Artifact: Implemented in `docs/validation/release-checklist.md`.
- Validation: Final documentation validation recorded in handoff.

### Errors And Mistakes

_No tracked findings._

### Duplications To Remove Or Reduce

_No tracked findings._

### Simplification Opportunities

_No tracked findings._

### Smaller / Stylistic Items

- If the media pack is accepted, rename the existing description heading from `Visuals` to `Screenshots And Animation` only if that improves clarity in the final generated HTML.
- Keep first-frame GIF content meaningful because some viewers may show only the initial frame.

## Suggested Priority Order

1. `F002` - decide the media storyboard and asset standard first, because the copy should not promise visuals that do not exist yet.
2. `F001` - extend the generated Marketplace description after the accepted storyboard is clear.
3. `F003` - add the release checklist step before the first Marketplace publication so upload and rendering verification are not missed.

## Out Of Scope

- Runtime UI changes to progress indication.
- Changing AI Assistant integration, commit execution, or push behavior.
- Publishing the plugin or uploading Marketplace media.
- Rewriting README, user guide, troubleshooting, support, or changelog content beyond what accepted findings require.
