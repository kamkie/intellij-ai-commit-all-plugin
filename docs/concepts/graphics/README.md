# Concept Graphics

This directory stores generated or drafted visual references for design decisions.

Files here are concept inputs, not final plugin UI assets. Final implementation assets must still be adapted to IntelliJ Platform UI and icon conventions before use.

## Split Button Placeholder

Generated placeholder for the `AI Commit All` split-button styling direction from ADR 0027.

It shows the intended visual direction for normal, running, disabled, commit-only, and commit-and-push states. Treat it as a reference for implementation, not as a production bitmap asset.

![Split button placeholder](split-button-placeholder.png)

## Split Button Draft Series

Draft series for ADR 0025 and `PROP-02-pre-release-ux` review:

- [Draft series notes](split-button-drafts/README.md)
- [Decision tree](split-button-drafts/DECISION_TREE.md)

These drafts are concept references only. They intentionally keep the center join clean, with no arrow between the `AI Commit All` and `& Push` segments, and use slightly contrasting segment colors to make the two actions scan as related but distinct.
