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
    $getRelativePathMethod = [System.IO.Path].GetMethods() |
        Where-Object { $_.Name -eq 'GetRelativePath' -and $_.GetParameters().Count -eq 2 } |
        Select-Object -First 1
    if ($null -ne $getRelativePathMethod)
    {
        return [System.IO.Path]::GetRelativePath($repoRoot.Path, $resolved.Path).Replace('\', '/')
    }

    $rootPath = [System.IO.Path]::GetFullPath($repoRoot.Path).TrimEnd('\', '/') + [System.IO.Path]::DirectorySeparatorChar
    $resolvedPath = [System.IO.Path]::GetFullPath($resolved.Path)
    $relativeUri = ([Uri]$rootPath).MakeRelativeUri([Uri]$resolvedPath)
    return [Uri]::UnescapeDataString($relativeUri.ToString()).Replace('\', '/')
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

function Get-AgentMarkdownArtifactFiles
{
    $pathsByFullName = @{ }
    foreach ($relativeRoot in @('.agents/references', '.agents/skills', '.agents/prompts', '.agents/plans'))
    {
        $root = Join-Path $repoRoot $relativeRoot
        if (-not (Test-Path -LiteralPath $root))
        {
            continue
        }

        $files = Get-ChildItem -LiteralPath $root -Recurse -File -Filter '*.md'
        foreach ($file in $files)
        {
            $pathsByFullName[$file.FullName] = $file
        }
    }

    return $pathsByFullName.Values | Sort-Object FullName
}

function Test-AgentReferences
{
    $referencesRoot = Join-Path $repoRoot '.agents/references'
    if (-not (Test-Path -LiteralPath $referencesRoot))
    {
        return
    }

    $referenceFiles = Get-ChildItem -LiteralPath $referencesRoot -File -Filter '*.md' | Sort-Object Name
    foreach ($referenceFile in $referenceFiles)
    {
        $relative = Get-RelativePath $referenceFile.FullName
        $text = Get-Content -Raw -LiteralPath $referenceFile.FullName
        $title = Get-MarkdownHeadingTitle -Text $text -RelativePath $relative
        if ($null -ne $title -and [string]::IsNullOrWhiteSpace($title))
        {
            Add-ValidationError "$relative has an empty level-one title"
        }
    }
}

function Test-AgentBacktickFileReferences
{
    foreach ($file in (Get-AgentMarkdownArtifactFiles))
    {
        $relative = Get-RelativePath $file.FullName
        $text = Get-Content -Raw -LiteralPath $file.FullName
        $matches = [regex]::Matches($text, '`(\.agents[\\/][^`]+)`')
        foreach ($match in $matches)
        {
            $reference = $match.Groups[1].Value.Trim()
            $normalized = $reference.Replace('\', '/')
            $pathOnly = ($normalized -split '#')[0]
            if ([string]::IsNullOrWhiteSpace($pathOnly) -or
                $pathOnly.EndsWith('/') -or
                $pathOnly -match '[*?<>]')
            {
                continue
            }

            $segments = $pathOnly -split '/'
            $leaf = $segments[$segments.Count - 1]
            if ($leaf -notmatch '\.[A-Za-z0-9]+$')
            {
                continue
            }

            $targetRelative = $pathOnly.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
            $targetPath = Join-Path $repoRoot $targetRelative
            if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf))
            {
                Add-ValidationError "$relative references missing agent artifact '$reference'"
            }
        }
    }
}

function Test-PlanCatalogLinks
{
    $plansRoot = Join-Path $repoRoot '.agents/plans'
    $readmePath = Join-Path $plansRoot 'README.md'
    if (-not (Test-Path -LiteralPath $readmePath))
    {
        Add-ValidationError '.agents/plans is missing README.md'
        return
    }

    $readmeText = Get-Content -Raw -LiteralPath $readmePath
    foreach ($catalogLink in [regex]::Matches($readmeText, '\[([^\]]+)\]\(([^)]+\.md)\)'))
    {
        $target = $catalogLink.Groups[2].Value
        $targetPath = Join-Path $plansRoot $target
        if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf))
        {
            Add-ValidationError ".agents/plans/README.md links to missing plan $target"
        }
    }
}

function Test-AgentPlans
{
    $plansRoot = Join-Path $repoRoot '.agents/plans'
    if (-not (Test-Path -LiteralPath $plansRoot))
    {
        return
    }

    Test-PlanCatalogLinks

    $allowedPlanStatuses = @('Draft', 'Approved', 'In Progress', 'Blocked', 'Implemented', 'Closed')
    $statusesRequiringApproval = @('Approved', 'In Progress', 'Blocked', 'Implemented', 'Closed')
    $planFiles = Get-ChildItem -LiteralPath $plansRoot -Recurse -File -Filter '*.md' |
        Where-Object { $_.Name -notin @('README.md', 'PLAN_TEMPLATE.md') } |
        Sort-Object FullName

    foreach ($planFile in $planFiles)
    {
        $relative = Get-RelativePath $planFile.FullName
        $text = Get-Content -Raw -LiteralPath $planFile.FullName

        $planIdMatch = [regex]::Match($text, '(?m)^Plan-ID:\s+(PLAN-[A-Za-z0-9][A-Za-z0-9-]*)\s*$')
        if (-not $planIdMatch.Success)
        {
            Add-ValidationError "$relative is missing Plan-ID metadata"
        }

        $status = $null
        $statusMatch = [regex]::Match($text, '(?m)^Status:\s+(.+?)\s*$')
        if (-not $statusMatch.Success)
        {
            Add-ValidationError "$relative is missing Status metadata"
        }
        else
        {
            $status = $statusMatch.Groups[1].Value.Trim()
            if ($allowedPlanStatuses -notcontains $status)
            {
                Add-ValidationError "$relative has invalid Status '$status'"
            }
        }

        if ([regex]::Matches($text, '(?m)^Workers:\s+(.+?)\s*$').Count -ne 1)
        {
            Add-ValidationError "$relative must contain exactly one Workers metadata line"
        }

        $filenameMatch = [regex]::Match($text, '(?m)^Filename:\s+(.+?)\s*$')
        if (-not $filenameMatch.Success)
        {
            Add-ValidationError "$relative is missing Filename metadata"
        }
        else
        {
            $filenameValue = $filenameMatch.Groups[1].Value.Trim().Trim('`')
            if ($filenameValue.Replace('\', '/') -ne $relative)
            {
                Add-ValidationError "$relative Filename metadata must match its repository path"
            }
        }

        $closeReasonMatch = [regex]::Match($text, '(?m)^Close-Reason:\s+(.+?)\s*$')
        if ($status -eq 'Closed' -and -not $closeReasonMatch.Success)
        {
            Add-ValidationError "$relative has Status 'Closed' but is missing Close-Reason"
        }
        elseif ($null -ne $status -and $status -ne 'Closed' -and $closeReasonMatch.Success)
        {
            Add-ValidationError "$relative has Close-Reason but Status is '$status'"
        }

        foreach ($heading in @('Readiness', 'Status History', 'Execution Graph'))
        {
            Test-RequiredHeading -Text $text -RelativePath $relative -Heading $heading
        }

        $readinessMatch = [regex]::Match($text, '(?ms)^## Readiness\s*(.*?)(?=^## |\z)')
        if ($readinessMatch.Success -and $null -ne $status -and $statusesRequiringApproval -contains $status)
        {
            $readinessText = $readinessMatch.Groups[1].Value
            $approvedByMatch = [regex]::Match($readinessText, '(?m)^-\s+Approved by:\s*(.*?)\s*$')
            $approvedAtMatch = [regex]::Match($readinessText, '(?m)^-\s+Approved at:\s*(.*?)\s*$')
            $approvedBy = if ($approvedByMatch.Success)
            {
                $approvedByMatch.Groups[1].Value.Trim()
            }
            else
            {
                ''
            }
            $approvedAt = if ($approvedAtMatch.Success)
            {
                $approvedAtMatch.Groups[1].Value.Trim()
            }
            else
            {
                ''
            }
            if ( [string]::IsNullOrWhiteSpace($approvedBy))
            {
                Add-ValidationError "$relative has Status '$status' but is missing Approved by in ## Readiness"
            }

            if ( [string]::IsNullOrWhiteSpace($approvedAt))
            {
                Add-ValidationError "$relative has Status '$status' but is missing Approved at in ## Readiness"
            }
        }
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
Test-AgentReferences
Test-RepositoryPrompts
Test-AgentPlans
Test-AgentBacktickFileReferences

if ($errors.Count -gt 0) {
    foreach ($validationError in $errors) {
        Write-Output "ERROR: $validationError"
    }
    exit 1
}

Write-Output 'Agent artifact validation passed.'
exit 0
