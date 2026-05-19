# Plugin Compatibility Sweep

Review compatibility risks across IntelliJ Platform, Gradle IntelliJ Platform Plugin, Kotlin, JDK, plugin descriptors, and supported IDE targets.

## Read First

- `AGENTS.md`
- `.agents/references/testing.md`
- `.agents/references/code-style.md`
- `.agents/references/documentation.md`
- `.agents/prompts/README.md`
- this prompt
- relevant Gradle files, `plugin.xml`, compatibility ADRs, CI workflows, verifier settings, or dependency update request

Use the `platform-docs-research` skill when version-sensitive IntelliJ Platform, Gradle IntelliJ Platform Plugin, Kotlin, JDK, or JetBrains API behavior must be verified from primary sources.

## Output

Produce a compatibility sweep report with:

- target IDE range, Kotlin/JDK/Gradle assumptions, and plugin dependency assumptions found in the repository
- API usage, dependency, descriptor, verifier, build, and CI risks
- suspected internal API, deprecated API, or version-bound behavior that needs verification
- required docs, ADR, support policy, task, changelog, or release guidance updates
- recommended validation commands, including `.\gradlew.bat buildPlugin`, `.\gradlew.bat verifyPlugin` when configured, and relevant tests
- recommendation to proceed, research more, create an ADR, create a plan, or split the compatibility work

Keep findings tied to concrete files, version constraints, or documented API behavior.

## Non-Goals

- Do not upgrade tools or supported IDE versions from this prompt unless the user separately asks for implementation.
- Do not rely on stale memory for current IntelliJ Platform or Gradle IntelliJ Platform behavior when primary-source verification is needed.
- Do not broaden supported IDEs, plugin dependencies, or compatibility promises without an accepted ADR or explicit maintainer decision.
- Do not run heavyweight verifier or sandbox checks unless the user asks for execution and the local environment is ready.
