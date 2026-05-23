# Security Policy

This project is a pre-release IntelliJ Platform plugin repository. No Marketplace-published release exists yet.

## Supported Scope

Security support follows the current repository support status in [docs/SUPPORT.md](docs/SUPPORT.md). Until the first Marketplace release, security handling is best-effort for the current `main` branch and current prerelease candidate only.

## Reporting A Vulnerability

Do not open a public issue for a vulnerability, exposed secret, private repository detail, proprietary commit message, or sensitive log content.

Preferred reporting path:

1. Use GitHub private vulnerability reporting if the repository shows a `Report a vulnerability` action.
2. If private vulnerability reporting is unavailable, email the maintainer at `kontakt@devopssolutions.pl` with a minimal report.

Include:

- Affected plugin version, release tag, or commit SHA.
- IDE product and build.
- Operating system.
- Reproduction steps.
- Impact and whether credentials, private repositories, or generated commit messages are involved.
- Sanitized logs or screenshots only when needed.

For a small open-source prerelease project, expect best-effort acknowledgement within 7 days and triage within 30 days. Do not disclose the issue publicly until a fix or mitigation path is agreed.

## Secret Handling

Never commit or post release credentials, signing material, API tokens, private repository content, proprietary commit messages, or unsanitized IDE logs.

Protect these GitHub Actions secret categories:

- `PUBLISH_TOKEN` for JetBrains Marketplace publication.
- `CERTIFICATE_CHAIN` for plugin signing.
- `PRIVATE_KEY` for plugin signing.
- `PRIVATE_KEY_PASSWORD` for the signing private key.

If any secret may have been exposed:

1. Revoke or rotate the exposed credential immediately.
2. Replace the corresponding GitHub Actions secret.
3. Treat values committed to Git history as compromised, even if the file is later deleted.
4. Review recent release workflow runs and Marketplace activity.
5. Record only non-sensitive remediation notes in repository issues or commits.

Before Marketplace release work, maintainers should verify that GitHub secret scanning and push protection are enabled for the repository where available, and that the `jetbrains-marketplace` environment still protects signing and publication.
