param(
    [switch] $Check
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$changelogPath = Join-Path $repoRoot 'CHANGELOG.md'
$changeNotesPath = Join-Path $repoRoot 'config/intellij-platform/change-notes.html'

function Get-TextFile {
    param([string] $Path)

    return Get-Content -Raw -LiteralPath $Path
}

function Convert-InlineMarkdownToHtml {
    param([string] $Text)

    $encoded = [System.Net.WebUtility]::HtmlEncode($Text)
    $encoded = [regex]::Replace($encoded, '`([^`]+)`', '<code>$1</code>')
    $encoded = [regex]::Replace($encoded, '\*\*([^*]+)\*\*', '<strong>$1</strong>')
    return $encoded
}

function Get-ChangelogSections {
    param([string] $Text)

    $matches = [regex]::Matches(
        $Text,
        '(?ms)^##\s+\[(?<name>[^\]]+)\](?:\s+-\s+(?<date>\d{4}-\d{2}-\d{2}))?\s*\r?\n(?<body>.*?)(?=^##\s+\[|\z)'
    )
    if ($matches.Count -eq 0) {
        throw 'CHANGELOG.md has no version sections.'
    }

    return @($matches | ForEach-Object {
            [PSCustomObject]@{
                Name = $_.Groups['name'].Value.Trim()
                Date = $_.Groups['date'].Value.Trim()
                Body = $_.Groups['body'].Value.Trim()
            }
        })
}

function Get-ChangelogItems {
    param([string] $SectionBody)

    return @([regex]::Matches($SectionBody, '(?m)^-\s+(.+?)\s*$') |
        ForEach-Object { $_.Groups[1].Value.Trim() })
}

function Add-SectionHtml {
    param(
        [System.Collections.Generic.List[string]] $Lines,
        [string] $Title,
        [array] $Items
    )

    $sectionItems = @($Items | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
    if ($sectionItems.Count -eq 0) {
        return
    }

    $Lines.Add("<p>$(Convert-InlineMarkdownToHtml $Title)</p>") | Out-Null
    $Lines.Add('<ul>') | Out-Null
    foreach ($item in $sectionItems) {
        $Lines.Add("    <li>$(Convert-InlineMarkdownToHtml $item)</li>") | Out-Null
    }
    $Lines.Add('</ul>') | Out-Null
}

function Get-ReleaseTitle {
    param([string] $Version)

    if ($Version -match '-') {
        return "$Version prerelease."
    }

    return "$Version release."
}

$changelogText = Get-TextFile $changelogPath
$sections = Get-ChangelogSections $changelogText
$unreleased = $sections | Where-Object { $_.Name -eq 'Unreleased' } | Select-Object -First 1
if ($null -eq $unreleased) {
    throw 'CHANGELOG.md is missing the [Unreleased] section.'
}

$latestRelease = $sections | Where-Object { $_.Name -ne 'Unreleased' } | Select-Object -First 1
if ($null -eq $latestRelease) {
    throw 'CHANGELOG.md is missing a released version section.'
}

$unreleasedItems = Get-ChangelogItems $unreleased.Body
$releaseItems = Get-ChangelogItems $latestRelease.Body
if (@($releaseItems).Count -eq 0) {
    throw "CHANGELOG.md section [$($latestRelease.Name)] has no release-note items."
}

$lines = New-Object System.Collections.Generic.List[string]
Add-SectionHtml `
    -Lines $lines `
    -Title 'Unreleased changes, not yet included in a Marketplace release.' `
    -Items $unreleasedItems
Add-SectionHtml `
    -Lines $lines `
    -Title (Get-ReleaseTitle $latestRelease.Name) `
    -Items $releaseItems

$generated = ($lines -join "`n") + "`n"

if ($Check) {
    $current = Get-TextFile $changeNotesPath
    if ($current -ne $generated) {
        throw 'config/intellij-platform/change-notes.html is stale. Run scripts/generate-intellij-platform-change-notes.ps1.'
    }

    Write-Output 'Marketplace change notes are up to date.'
    exit 0
}

[System.IO.File]::WriteAllText($changeNotesPath, $generated, [System.Text.UTF8Encoding]::new($false))
Write-Output 'Generated config/intellij-platform/change-notes.html.'
