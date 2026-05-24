# Marketplace Assets

These generated assets are the source media pack for JetBrains Marketplace
upload. They show the `AI | Commit | Push` control in a realistic Commit tool
window workflow while preserving the actual runtime Swing rendering of the
control.

| File                                                                       | Purpose                                                                                   |
|----------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| [ai-commit-all-realtime-progress.gif](ai-commit-all-realtime-progress.gif) | Animated workflow sequence showing ready, `AI`, `Commit`, `Push`, and complete states.    |
| [ai-commit-all-realtime-progress.png](ai-commit-all-realtime-progress.png) | Static fallback that keeps the same phase sequence visible when animation is unavailable. |

Refresh the assets from the repository root:

```powershell
$env:AICOMMITALL_GENERATE_USER_GUIDE_ASSETS = 'true'
.\gradlew.bat test --tests "pl.devopssolutions.aicommitall.actions.AiCommitAllControlAssetGeneratorTest"
Remove-Item Env:\AICOMMITALL_GENERATE_USER_GUIDE_ASSETS
```

Expected dimensions for Marketplace upload are `1200 x 760` pixels.
