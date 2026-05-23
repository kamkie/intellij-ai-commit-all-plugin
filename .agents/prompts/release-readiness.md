# Release Readiness

Check whether the repository is ready for a plugin release or identify the blockers before release work proceeds.

## Read First

- `AGENTS.md`
- `.agents/references/releases.md`
- `.agents/references/testing.md`
- `.agents/references/documentation.md`
- `CHANGELOG.md`
- `docs/SUPPORT.md`
- `.agents/prompts/README.md`
- this prompt

Load build files, CI workflows, signing configuration, Marketplace docs, plugin descriptors, tasks, ADRs, or plans only when they are relevant to the requested release boundary.

## Output

Produce a release-readiness report with:

- target version, release branch, commit boundary, and whether uncommitted changes are included
- changelog status and whether entries are public plugin-facing
- support policy status
- required validation commands and whether each passed, failed, or still needs to run
- plugin packaging, signing, verification, Marketplace, CI, and tag readiness
- unresolved tasks, open questions, ADR implementation gaps, proposal implementation gaps, or known risks that block release
- explicit go, no-go, or conditional-go recommendation

If the user asks for a release plan, create or update a plan under `.agents/plans/` instead of performing release steps immediately.

## Non-Goals

- Do not publish, sign, tag, or push a release unless the user explicitly asks and required prerequisites are satisfied.
- Do not add internal AI-agent guidance changes to `CHANGELOG.md` unless they affect public plugin behavior, public docs, support promises, or release artifacts.
- Do not skip plugin packaging, compatibility, or CI checks because documentation validation passed.
- Do not invent version numbers or support promises without an accepted source.
