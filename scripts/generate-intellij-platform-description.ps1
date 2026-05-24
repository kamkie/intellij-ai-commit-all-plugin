param(
    [switch] $Check
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$readmePath = Join-Path $repoRoot 'README.md'
$userGuidePath = Join-Path $repoRoot 'docs/user-guide.md'
$licensePath = Join-Path $repoRoot 'LICENSE'
$descriptionPath = Join-Path $repoRoot 'config/intellij-platform/description.html'
$sourceUrl = 'https://github.com/kamkie/intellij-ai-commit-all-plugin'
$visualAssetsUrl = "$sourceUrl/tree/main/docs/assets/user-guide"
$marketplaceAssetsUrl = "$sourceUrl/tree/main/docs/assets/marketplace"

function Get-TextFile {
    param([string] $Path)

    return Get-Content -Raw -LiteralPath $Path
}

function Assert-SourceContains {
    param(
        [string] $Text,
        [string] $RelativePath,
        [string] $Pattern,
        [string] $Claim
    )

    if ($Text -notmatch $Pattern) {
        throw "$RelativePath no longer supports Marketplace description claim: $Claim"
    }
}

function Get-MarkdownSection {
    param(
        [string] $Text,
        [string] $Heading
    )

    $escapedHeading = [regex]::Escape($Heading)
    $match = [regex]::Match($Text, "(?ms)^##\s+$escapedHeading\s*\r?\n(.*?)(?=^##\s+|\z)")
    if (-not $match.Success) {
        throw "Missing Markdown section: $Heading"
    }

    return $match.Groups[1].Value.Trim()
}

function Convert-InlineMarkdownToHtml {
    param([string] $Text)

    $encoded = [System.Net.WebUtility]::HtmlEncode($Text)
    $encoded = [regex]::Replace($encoded, '`([^`]+)`', '<code>$1</code>')
    $encoded = [regex]::Replace($encoded, '\*\*([^*]+)\*\*', '<strong>$1</strong>')
    return $encoded
}

function Get-Paragraph {
    param(
        [string] $Text,
        [string] $Pattern
    )

    $match = [regex]::Match($Text, $Pattern)
    if (-not $match.Success) {
        throw "Missing source paragraph for pattern: $Pattern"
    }

    return ($match.Value -replace '\s+', ' ').Trim()
}

function Get-RequirementItems {
    param([string] $ReadmeText)

    $section = Get-MarkdownSection -Text $ReadmeText -Heading 'Requirements'
    $items = [regex]::Matches($section, '(?m)^-\s+(.+?)\s*$') |
        ForEach-Object { $_.Groups[1].Value.Trim() }
    if ($items.Count -eq 0) {
        throw 'README.md Requirements section has no bullet items.'
    }

    return @($items)
}

function Get-FeatureItems {
    param([string] $ReadmeText)

    $section = Get-MarkdownSection -Text $ReadmeText -Heading 'Quick Start'
    $rows = [regex]::Matches($section, '(?m)^\|\s+`([^`]+)`\s+\|\s+(.+?)\s+\|$')
    if ($rows.Count -ne 3) {
        throw 'README.md Quick Start table must contain AI, Commit, and Push rows.'
    }

    return @($rows | ForEach-Object {
            $sectionName = $_.Groups[1].Value.Trim()
            $description = $_.Groups[2].Value.Trim()
            "<strong>$([System.Net.WebUtility]::HtmlEncode($sectionName))</strong>: $(Convert-InlineMarkdownToHtml $description)"
        })
}

$readmeText = Get-TextFile $readmePath
$userGuideText = Get-TextFile $userGuidePath
$licenseText = Get-TextFile $licensePath

Assert-SourceContains $readmeText 'README.md' 'AI \| Commit \| Push' 'three-section Commit tool window control'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'JetBrains AI Assistant is required; there is no non-AI fallback message generator\.' 'JetBrains AI Assistant is required'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'Git is the only supported VCS\.' 'Git is the only supported VCS'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'If JetBrains AI Assistant is missing or disabled, the IDE refuses to load AI Commit All because the AI Assistant dependency is required\.' 'AI Assistant dependency prevents loading when missing or disabled'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'assets/user-guide/ai-commit-all-control-light\.png' 'light control screenshot'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'assets/user-guide/ai-commit-all-control-dark\.png' 'dark control screenshot'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'assets/user-guide/ai-commit-all-control-running\.gif' 'running control animation'
Assert-SourceContains $userGuideText 'docs/user-guide.md' 'assets/marketplace/README\.md' 'Marketplace workflow media pack'
Assert-SourceContains $readmeText 'README.md' ([regex]::Escape($sourceUrl)) 'official source repository link'
Assert-SourceContains $licenseText 'LICENSE' 'Apache License\s+Version 2\.0' 'Apache License 2.0'

$summary = Get-Paragraph `
    -Text $readmeText `
    -Pattern '\*\*AI Commit All\*\* \(`pl\.devopssolutions\.aicommitall`\) is an IntelliJ Platform plugin that adds an `AI \| Commit \| Push` control to the Commit tool window\. It uses JetBrains AI Assistant to draft the commit message, then can commit or commit-and-push eligible Git changes\.'
$requirements = Get-RequirementItems $readmeText
$features = Get-FeatureItems $readmeText
$dependencyNote = Get-Paragraph `
    -Text $readmeText `
    -Pattern 'If AI Assistant is missing or disabled, the IDE refuses to load the plugin through the required dependency\.'
$realTimeProgress = Get-Paragraph `
    -Text $userGuideText `
    -Pattern 'The running section shows where the workflow is now: `AI` while JetBrains AI\s+Assistant generates the message, `Commit` while the IDE commit workflow runs,\s+and `Push` while the push step is in progress\. The control is disabled until\s+the current run finishes\.'

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("<p>$(Convert-InlineMarkdownToHtml $summary)</p>") | Out-Null
$lines.Add('<h3>Feature Summary</h3>') | Out-Null
$lines.Add('<ul>') | Out-Null
foreach ($feature in $features) {
    $lines.Add("    <li>$feature</li>") | Out-Null
}
$lines.Add('</ul>') | Out-Null
$lines.Add('<h3>Requirements</h3>') | Out-Null
$lines.Add('<ul>') | Out-Null
foreach ($requirement in $requirements) {
    $lines.Add("    <li>$(Convert-InlineMarkdownToHtml $requirement)</li>") | Out-Null
}
$lines.Add('</ul>') | Out-Null
$lines.Add("<p>$(Convert-InlineMarkdownToHtml $dependencyNote)</p>") | Out-Null
$lines.Add('<h3>Real-Time Progress</h3>') | Out-Null
$lines.Add("<p>$( Convert-InlineMarkdownToHtml $realTimeProgress )</p>") | Out-Null
$lines.Add('<h3>Screenshots And Animation</h3>') | Out-Null
$lines.Add("<p>Marketplace workflow GIF and PNG media: <a href=""$marketplaceAssetsUrl"">$marketplaceAssetsUrl</a></p>") | Out-Null
$lines.Add("<p>Reviewed exact control renderings: <a href=""$visualAssetsUrl"">$visualAssetsUrl</a></p>") | Out-Null
$lines.Add('<h3>Source And License</h3>') | Out-Null
$lines.Add("<p>Source code: <a href=""$sourceUrl"">$sourceUrl</a></p>") | Out-Null
$lines.Add('<p>Licensed under the Apache License 2.0.</p>') | Out-Null

$generated = ($lines -join "`n") + "`n"

if ($Check) {
    $current = Get-TextFile $descriptionPath
    if ($current -ne $generated) {
        throw 'config/intellij-platform/description.html is stale. Run scripts/generate-intellij-platform-description.ps1.'
    }

    Write-Output 'Marketplace description is up to date.'
    exit 0
}

[System.IO.File]::WriteAllText($descriptionPath, $generated, [System.Text.UTF8Encoding]::new($false))
Write-Output 'Generated config/intellij-platform/description.html.'
