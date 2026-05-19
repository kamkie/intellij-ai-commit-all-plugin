Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$errors = New-Object System.Collections.Generic.List[string]

function Add-ValidationError {
    param([string] $Message)
    $errors.Add($Message) | Out-Null
}

function Get-RelativePath {
    param([string] $Path)
    $resolved = Resolve-Path -LiteralPath $Path
    return [System.IO.Path]::GetRelativePath($repoRoot.Path, $resolved.Path).Replace('\', '/')
}

function Get-MarkdownHeadingTitle {
    param(
        [string] $Text,
        [string] $RelativePath
    )

    $headings = [regex]::Matches($Text, '(?m)^#\s+(.+?)\s*$')
    if ($headings.Count -eq 0) {
        Add-ValidationError "$RelativePath is missing a level-one title"
        return $null
    }

    if ($headings.Count -gt 1) {
        Add-ValidationError "$RelativePath must contain exactly one level-one title"
        return $headings[0].Groups[1].Value.Trim()
    }

    return $headings[0].Groups[1].Value.Trim()
}

function Test-RequiredHeading {
    param(
        [string] $Text,
        [string] $RelativePath,
        [string] $Heading
    )

    if ($Text -notmatch "(?m)^##\s+$([regex]::Escape($Heading))\s*$") {
        Add-ValidationError "$RelativePath is missing ## $Heading"
    }
}

function Test-AgentSkills {
    $skillsRoot = Join-Path $repoRoot '.agents/skills'
    if (-not (Test-Path -LiteralPath $skillsRoot)) {
        return
    }

    $skillDirectories = Get-ChildItem -LiteralPath $skillsRoot -Directory | Sort-Object Name
    foreach ($skillDirectory in $skillDirectories) {
        $relativeDirectory = Get-RelativePath $skillDirectory.FullName
        if ($skillDirectory.Name -notmatch '^[a-z0-9]+(?:-[a-z0-9]+)*$') {
            Add-ValidationError "$relativeDirectory directory name must use lowercase kebab-case"
        }

        $skillPath = Join-Path $skillDirectory.FullName 'SKILL.md'
        if (-not (Test-Path -LiteralPath $skillPath)) {
            Add-ValidationError "$relativeDirectory is missing SKILL.md"
            continue
        }

        $relative = Get-RelativePath $skillPath
        $text = Get-Content -Raw -LiteralPath $skillPath
        $frontMatterMatch = [regex]::Match($text, '(?s)^---\s(.*?)\s---\s*')
        if (-not $frontMatterMatch.Success) {
            Add-ValidationError "$relative is missing YAML front matter"
            continue
        }

        $frontMatter = $frontMatterMatch.Groups[1].Value
        $nameMatch = [regex]::Match($frontMatter, '(?m)^name:\s+([a-z0-9]+(?:-[a-z0-9]+)*)\s*$')
        if (-not $nameMatch.Success) {
            Add-ValidationError "$relative front matter is missing a lowercase kebab-case name"
        }
        else {
            $skillName = $nameMatch.Groups[1].Value
            if ($skillName -ne $skillDirectory.Name) {
                Add-ValidationError "$relative front matter name '$skillName' must match directory '$($skillDirectory.Name)'"
            }
        }

        $descriptionMatch = [regex]::Match($frontMatter, '(?m)^description:\s+(.+?)\s*$')
        if (-not $descriptionMatch.Success -or [string]::IsNullOrWhiteSpace($descriptionMatch.Groups[1].Value)) {
            Add-ValidationError "$relative front matter is missing description"
        }
        elseif ($descriptionMatch.Groups[1].Value.Trim().Length -lt 40) {
            Add-ValidationError "$relative front matter description is too short to be useful"
        }

        $title = Get-MarkdownHeadingTitle -Text $text -RelativePath $relative
        if ($null -ne $title -and [string]::IsNullOrWhiteSpace($title)) {
            Add-ValidationError "$relative has an empty level-one title"
        }

        Test-RequiredHeading -Text $text -RelativePath $relative -Heading 'Start'
    }
}

function Test-RepositoryPrompts {
    $promptsRoot = Join-Path $repoRoot '.agents/prompts'
    if (-not (Test-Path -LiteralPath $promptsRoot)) {
        return
    }

    $readmePath = Join-Path $promptsRoot 'README.md'
    if (-not (Test-Path -LiteralPath $readmePath)) {
        Add-ValidationError '.agents/prompts is missing README.md'
        return
    }

    $readmeText = Get-Content -Raw -LiteralPath $readmePath
    foreach ($requiredText in @('## Loading Mechanism', '## Rules', '## Current Prompts')) {
        if ($readmeText -notmatch "(?m)^$([regex]::Escape($requiredText))\s*$") {
            Add-ValidationError ".agents/prompts/README.md is missing $requiredText"
        }
    }

    $promptFiles = Get-ChildItem -LiteralPath $promptsRoot -File -Filter '*.md' |
        Where-Object { $_.Name -ne 'README.md' } |
        Sort-Object Name

    foreach ($promptFile in $promptFiles) {
        $relative = Get-RelativePath $promptFile.FullName
        if ($promptFile.Name -notmatch '^[a-z0-9]+(?:-[a-z0-9]+)*\.md$') {
            Add-ValidationError "$relative filename must use lowercase kebab-case"
        }

        $text = Get-Content -Raw -LiteralPath $promptFile.FullName
        $title = Get-MarkdownHeadingTitle -Text $text -RelativePath $relative
        foreach ($heading in @('Read First', 'Output', 'Non-Goals')) {
            Test-RequiredHeading -Text $text -RelativePath $relative -Heading $heading
        }

        $catalogLink = "[$title]($($promptFile.Name))"
        if ($null -ne $title -and -not $readmeText.Contains($catalogLink)) {
            Add-ValidationError ".agents/prompts/README.md is missing catalog link $catalogLink"
        }
    }

    foreach ($catalogLink in [regex]::Matches($readmeText, '\[([^\]]+)\]\(([^)]+\.md)\)')) {
        $target = $catalogLink.Groups[2].Value
        if ($target -eq 'README.md') {
            continue
        }

        $targetPath = Join-Path $promptsRoot $target
        if (-not (Test-Path -LiteralPath $targetPath)) {
            Add-ValidationError ".agents/prompts/README.md links to missing prompt $target"
        }
    }
}

Test-AgentSkills
Test-RepositoryPrompts

if ($errors.Count -gt 0) {
    foreach ($validationError in $errors) {
        Write-Output "ERROR: $validationError"
    }
    exit 1
}

Write-Output 'Agent artifact validation passed.'
