param(
    [Parameter(Mandatory = $true)]
    [string] $Tag,

    [string] $ChangelogPath = (Join-Path (Resolve-Path (Join-Path $PSScriptRoot '..')) 'CHANGELOG.md'),

    [Parameter(Mandatory = $true)]
    [string] $OutputPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$semanticTagPattern = '^v[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z][0-9A-Za-z.-]*)?$'
if ($Tag -notmatch $semanticTagPattern) {
    throw 'Release tag must use vMAJOR.MINOR.PATCH or vMAJOR.MINOR.PATCH-PRERELEASE.'
}

if (-not (Test-Path -LiteralPath $ChangelogPath -PathType Leaf)) {
    throw "CHANGELOG.md was not found at $ChangelogPath."
}

$changelogText = Get-Content -Raw -LiteralPath $ChangelogPath
$escapedTag = [regex]::Escape($Tag)
$releaseHeadingPattern = "(?ms)^##\s+\[$escapedTag\]\s+-\s+\d{4}-\d{2}-\d{2}\s*\r?\n(?<body>.*?)(?=^##\s+\[|\z)"
$matches = [regex]::Matches($changelogText, $releaseHeadingPattern)

if ($matches.Count -eq 0) {
    $looseHeadingPattern = "(?m)^##\s+\[$escapedTag\].*$"
    if ([regex]::IsMatch($changelogText, $looseHeadingPattern)) {
        throw "CHANGELOG.md section [$Tag] must use heading format: ## [$Tag] - YYYY-MM-DD."
    }

    throw "CHANGELOG.md is missing release section [$Tag]. Expected heading: ## [$Tag] - YYYY-MM-DD."
}

if ($matches.Count -gt 1) {
    throw "CHANGELOG.md contains multiple release sections for [$Tag]."
}

$sectionBody = $matches[0].Groups['body'].Value.Trim()
if ([string]::IsNullOrWhiteSpace($sectionBody)) {
    throw "CHANGELOG.md section [$Tag] is empty."
}

$releaseNoteItems = [regex]::Matches($sectionBody, '(?m)^-\s*(?<item>.*)$')
if ($releaseNoteItems.Count -eq 0) {
    throw "CHANGELOG.md section [$Tag] has no release-note items."
}

foreach ($item in $releaseNoteItems) {
    if ([string]::IsNullOrWhiteSpace($item.Groups['item'].Value)) {
        throw "CHANGELOG.md section [$Tag] has an empty release-note item."
    }
}

$outputDirectory = Split-Path -Parent $OutputPath
if (-not [string]::IsNullOrWhiteSpace($outputDirectory)) {
    New-Item -ItemType Directory -Force -Path $outputDirectory | Out-Null
}

$releaseNotes = ($sectionBody -replace "`r`n", "`n") + "`n"
[System.IO.File]::WriteAllText($OutputPath, $releaseNotes, [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated GitHub release notes for $Tag at $OutputPath."
