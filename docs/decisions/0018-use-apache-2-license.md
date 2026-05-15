# Use Apache-2.0 License

Status: Accepted

Date: 2026-05-15

## Context

The repository and plugin license choice was open as `Q-META-2`.

The user requested research and a formal license proposal. The user also answered `Q-META-3`: this project is planned as an open-source plugin published to the official JetBrains Marketplace.

The user accepted Apache-2.0 for `Q-META-2`.

This is an engineering proposal, not legal advice.

Research sources:

- OSI approved license list and license texts:
  - https://opensource.org/licenses
  - https://opensource.org/license/mit
  - https://opensource.org/license/apache-2.0
  - https://opensource.org/license/gpl-3-0
- SPDX license identifiers:
  - https://spdx.org/licenses
- GitHub open-source licensing guidance:
  - https://docs.github.com/articles/open-source-licensing
- ChooseALicense practical summaries:
  - https://choosealicense.com/licenses/mit/
  - https://choosealicense.com/licenses/gpl-3.0/
- Apache License 2.0 steward text:
  - https://www.apache.org/licenses/LICENSE-2.0.html
- GNU GPLv3 steward text:
  - https://www.gnu.org/licenses/gpl.en.html
- JetBrains Marketplace license requirements:
  - https://plugins.jetbrains.com/docs/marketplace/uploading-a-new-plugin.html
  - https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html
- IntelliJ Platform license context:
  - https://plugins.jetbrains.com/docs/intellij/intellij-platform.html

Key constraints found:

- A public GitHub repository needs an explicit license if others should be allowed to use, modify, and redistribute it.
- JetBrains Marketplace requires a plugin license/EULA for publication.
- If an open-source license is selected for Marketplace publication, JetBrains expects a source code link.
- The IntelliJ Platform is open source under the Apache License.

## Decision

Use Apache License 2.0, SPDX identifier `Apache-2.0`.

Apply Apache-2.0 to:

- Plugin source code.
- Build scripts and repository configuration.
- Project documentation.
- Final committed custom SVG icon assets, unless an asset file explicitly declares a different compatible license.

Third-party dependencies, JetBrains APIs, JetBrains trademarks, generated AI concept inputs, and external references remain governed by their own terms. Do not commit third-party assets unless their license is known and compatible.

Implementation:

- Add a top-level `LICENSE` file with the Apache-2.0 text.
- Add a top-level `NOTICE` file only if required by project attribution needs or bundled dependencies.
- Add README license text: `Licensed under Apache License 2.0`.
- Configure Gradle/plugin metadata and Marketplace metadata to use `Apache-2.0` where supported.
- Add source code link to Marketplace metadata.
- Add license verification to CI once the scaffold exists.

## Consequences

- Apache-2.0 is a permissive OSI-approved license suitable for open-source distribution and Marketplace publication.
- The explicit patent grant and contribution handling are stronger for contributors and downstream users than MIT.
- Apache-2.0 aligns well with the IntelliJ Platform's own license context.
- Downstream users can use, modify, redistribute, and include the plugin in larger works, including commercial contexts, while preserving license notices.
- The license is longer and slightly more operationally strict than MIT, especially around notices and modified-file notices.
- The license does not force forks or derivative works to stay open source.

## Alternatives Considered

- MIT:
  - Pros: very short, widely recognized, low friction.
  - Cons: no explicit patent grant or contribution handling; weaker fit for an IDE plugin intended for broader ecosystem reuse.
- GPL-3.0-only or GPL-3.0-or-later:
  - Pros: strong copyleft; keeps distributed derivatives open.
  - Cons: higher adoption friction for plugin users and contributors; stronger redistribution obligations than currently requested.
- LGPL-3.0 or MPL-2.0:
  - Pros: weaker copyleft options.
  - Cons: more complexity than needed for this small plugin unless the user specifically wants reciprocal licensing.
- Custom EULA:
  - Pros: can express project-specific terms.
  - Cons: conflicts with the stated open-source direction and increases review burden.
- No license:
  - Pros: no setup work.
  - Cons: not acceptable for the intended open-source collaboration and Marketplace publication path.

## Follow-Up

- Remove `Q-META-2` from `docs/decisions/OPEN_QUESTIONS.md`.
- Add `LICENSE` before implementation or Marketplace packaging work.
- Record any future license change in a superseding accepted ADR.
