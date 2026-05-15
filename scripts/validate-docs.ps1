Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
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

$markdownFiles = Get-ChildItem -LiteralPath $repoRoot -Recurse -File -Filter '*.md' |
    Where-Object { $_.FullName -notmatch '\\.git\\' }

foreach ($file in $markdownFiles) {
    $relative = Get-RelativePath $file.FullName
    $lines = Get-Content -LiteralPath $file.FullName

    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '\s+$') {
            Add-ValidationError "${relative}:$($i + 1) has trailing whitespace"
        }
    }

    $text = Get-Content -Raw -LiteralPath $file.FullName
    foreach ($match in [regex]::Matches($text, '!?\[[^\]]*\]\(([^)]+)\)')) {
        $target = $match.Groups[1].Value.Trim()
        if ($target -match '^(https?:|mailto:|#)') {
            continue
        }

        $path = ($target -replace '#.*$', '').Trim('<', '>')
        if ([string]::IsNullOrWhiteSpace($path)) {
            continue
        }

        $candidate = Join-Path $file.DirectoryName $path
        if (-not (Test-Path -LiteralPath $candidate)) {
            Add-ValidationError "$relative links to missing local target: $target"
        }
    }
}

$tasksPath = Join-Path $repoRoot 'TASKS.md'
if (Test-Path -LiteralPath $tasksPath) {
    $taskText = Get-Content -Raw -LiteralPath $tasksPath
    $taskIds = [regex]::Matches($taskText, 'T-[A-Z]+-\d{3}') | ForEach-Object { $_.Value }
    $duplicates = $taskIds | Group-Object | Where-Object { $_.Count -gt 1 }
    foreach ($duplicate in $duplicates) {
        Add-ValidationError "TASKS.md contains duplicate task ID $($duplicate.Name)"
    }
}

$allowedPlanStatuses = @('Draft', 'Approved', 'In Progress', 'Blocked', 'Implemented', 'Closed')
$allowedPlanCloseReasons = @('Released', 'Rejected', 'Superseded', 'Deferred', 'Archived')

$planFiles = Get-ChildItem -LiteralPath (Join-Path $repoRoot '.agents/plans') -Recurse -File -Filter '*.md' |
    Where-Object { $_.Name -notin @('README.md', 'PLAN_TEMPLATE.md') }

foreach ($plan in $planFiles) {
    $relative = Get-RelativePath $plan.FullName
    $text = Get-Content -Raw -LiteralPath $plan.FullName
    $planIdMatch = [regex]::Match($text, '(?m)^Plan-ID:\s+(PLAN-[A-Za-z0-9][A-Za-z0-9-]*)\s*$')

    if (-not $planIdMatch.Success) {
        Add-ValidationError "$relative is missing a stable Plan-ID"
    } else {
        $planId = $planIdMatch.Groups[1].Value
        if (-not $plan.Name.StartsWith($planId, [System.StringComparison]::Ordinal)) {
            Add-ValidationError "$relative filename must include Plan-ID prefix $planId"
        }
    }

    $statusMatch = [regex]::Match($text, '(?m)^Status:\s+(.+?)\s*$')
    if (-not $statusMatch.Success) {
        Add-ValidationError "$relative is missing a Status"
    } else {
        $planStatus = $statusMatch.Groups[1].Value.Trim()
        if ($allowedPlanStatuses -notcontains $planStatus) {
            Add-ValidationError "$relative has invalid Status '$planStatus'; expected one of: $($allowedPlanStatuses -join ', ')"
        }

        if ($planStatus -eq 'Closed') {
            $closeReasonMatch = [regex]::Match($text, '(?m)^Close-Reason:\s+(.+?)\s*$')
            if (-not $closeReasonMatch.Success) {
                Add-ValidationError "$relative has Status 'Closed' but is missing Close-Reason"
            } else {
                $closeReason = $closeReasonMatch.Groups[1].Value.Trim()
                if ($allowedPlanCloseReasons -notcontains $closeReason) {
                    Add-ValidationError "$relative has invalid Close-Reason '$closeReason'; expected one of: $($allowedPlanCloseReasons -join ', ')"
                }
            }
        }
    }

    if ($text -notmatch '(?m)^## Readiness\s*$') {
        Add-ValidationError "$relative is missing a ## Readiness section"
    }
}

$adrDirectory = Join-Path $repoRoot 'docs/decisions'
$legacyAdrFiles = Get-ChildItem -LiteralPath $adrDirectory -File -Filter '*.md' |
    Where-Object { $_.Name -match '^\d{4}-' }
foreach ($legacyAdr in $legacyAdrFiles) {
    $relative = Get-RelativePath $legacyAdr.FullName
    Add-ValidationError "$relative must use ard-0000-<slug>.md filename format"
}

$adrFiles = Get-ChildItem -LiteralPath $adrDirectory -File -Filter 'ard-*.md' |
    Sort-Object Name
$adrReadmePath = Join-Path $repoRoot 'docs/decisions/README.md'
$adrReadmeText = ''
if (Test-Path -LiteralPath $adrReadmePath) {
    $adrReadmeText = Get-Content -Raw -LiteralPath $adrReadmePath
}

$requiredMadrHeadings = @(
    '# ',
    '## Context and Problem Statement',
    '## Decision Drivers',
    '## Considered Options',
    '## Decision Outcome',
    '### Consequences',
    '### Confirmation',
    '## Pros and Cons of the Options',
    '## More Information'
)

for ($i = 0; $i -lt $adrFiles.Count; $i++) {
    $relative = Get-RelativePath $adrFiles[$i].FullName
    $nameMatch = [regex]::Match($adrFiles[$i].Name, '^ard-(\d{4})-[a-z0-9]+(?:-[a-z0-9]+)*\.md$')
    if (-not $nameMatch.Success) {
        Add-ValidationError "$relative must use ard-0000-<slug>.md filename format"
        continue
    }

    $expected = '{0:D4}' -f $i
    $actual = $nameMatch.Groups[1].Value
    if ($actual -ne $expected) {
        Add-ValidationError "ADR sequence expected ard-$expected but found $($adrFiles[$i].Name)"
    }

    $expectedIndexEntry = "[ard-$actual]($($adrFiles[$i].Name))"
    if (-not $adrReadmeText.Contains($expectedIndexEntry)) {
        Add-ValidationError "docs/decisions/README.md is missing ADR index entry $expectedIndexEntry"
    }

    $adrText = Get-Content -Raw -LiteralPath $adrFiles[$i].FullName
    $frontMatterMatch = [regex]::Match($adrText, '(?s)^---\s(.*?)\s---\s*')
    if (-not $frontMatterMatch.Success) {
        Add-ValidationError "$relative is missing MADR YAML front matter"
        continue
    }

    $frontMatter = $frontMatterMatch.Groups[1].Value
    foreach ($key in @('status', 'date', 'decision-makers', 'consulted', 'informed')) {
        if ($frontMatter -notmatch "(?m)^${key}:\s+\S") {
            Add-ValidationError "$relative front matter is missing $key"
        }
    }

    if ($frontMatter -notmatch '(?m)^decision-makers:\s+[^<>\r\n]+\s+<[^<>\s@]+@[^<>\s@]+\.[^<>\s@]+>\s*$') {
        Add-ValidationError "$relative front matter decision-makers must use git username and email in Name <email> form"
    }

    if ($frontMatter -notmatch '(?m)^status:\s+(proposed|rejected|accepted|deprecated|superseded by .+)\s*$') {
        Add-ValidationError "$relative front matter has invalid MADR status"
    }

    if ($frontMatter -notmatch '(?m)^date:\s+\d{4}-\d{2}-\d{2}\s*$') {
        Add-ValidationError "$relative front matter has invalid MADR date"
    }

    if ([regex]::Matches($adrText, '(?m)^#\s+').Count -ne 1) {
        Add-ValidationError "$relative must contain exactly one MADR title heading"
    }

    $lastIndex = -1
    foreach ($heading in $requiredMadrHeadings) {
        $pattern = if ($heading -eq '# ') { '(?m)^#\s+.+' } else { "(?m)^$([regex]::Escape($heading))\s*$" }
        $headingMatch = [regex]::Match($adrText, $pattern)
        if (-not $headingMatch.Success) {
            Add-ValidationError "$relative is missing MADR heading $heading"
            continue
        }

        if ($headingMatch.Index -le $lastIndex) {
            Add-ValidationError "$relative has MADR heading $heading out of order"
        }
        $lastIndex = $headingMatch.Index
    }

    if ($adrText -notmatch '(?m)^Chosen option:\s+".+", because .+\.\s*$') {
        Add-ValidationError "$relative is missing MADR chosen option line"
    }
}

$proposalFiles = Get-ChildItem -LiteralPath (Join-Path $repoRoot 'docs/proposals') -Recurse -File -Filter '*.md' |
    Where-Object { $_.Name -notin @('README.md', 'PROPOSAL_TEMPLATE.md') }

foreach ($proposal in $proposalFiles) {
    $relative = Get-RelativePath $proposal.FullName
    $text = Get-Content -Raw -LiteralPath $proposal.FullName

    if ($text -notmatch '(?s)^---\s.*?\s---') {
        Add-ValidationError "$relative is missing YAML front matter"
        continue
    }

    $frontMatter = [regex]::Match($text, '(?s)^---\s(.*?)\s---').Groups[1].Value
    foreach ($key in @('proposal_id', 'generated_at', 'purpose', 'scope')) {
        if ($frontMatter -notmatch "(?m)^${key}:\s+\S") {
            Add-ValidationError "$relative front matter is missing $key"
        }
    }

    $proposalIdMatch = [regex]::Match($frontMatter, '(?m)^proposal_id:\s+(PROP-[A-Za-z0-9][A-Za-z0-9-]*)\s*$')
    if (-not $proposalIdMatch.Success)
    {
        Add-ValidationError "$relative front matter has invalid proposal_id"
    }
    else
    {
        $proposalId = $proposalIdMatch.Groups[1].Value
        $proposalBaseName = [System.IO.Path]::GetFileNameWithoutExtension($proposal.Name)
        if ($proposalBaseName -ne $proposalId -and -not $proposalBaseName.StartsWith("$proposalId-", [System.StringComparison]::Ordinal))
        {
            Add-ValidationError "$relative filename must include proposal_id prefix $proposalId"
        }
    }

    $sectionIds = [regex]::Matches($text, '(?m)^### ([EDS]\d+)\.') | ForEach-Object { $_.Groups[1].Value }
    $tableIds = [regex]::Matches($text, '(?m)^\| (E\d+|D\d+|S\d+) \|') | ForEach-Object { $_.Groups[1].Value }
    $sectionOnly = Compare-Object $sectionIds $tableIds | Where-Object { $_.SideIndicator -eq '<=' }
    $tableOnly = Compare-Object $sectionIds $tableIds | Where-Object { $_.SideIndicator -eq '=>' }

    foreach ($diff in $sectionOnly) {
        Add-ValidationError "$relative finding $($diff.InputObject) is missing from Progress Tracker"
    }
    foreach ($diff in $tableOnly) {
        Add-ValidationError "$relative Progress Tracker references missing finding $($diff.InputObject)"
    }
}

if ($errors.Count -gt 0) {
    foreach ($validationError in $errors) {
        Write-Error $validationError -ErrorAction Continue
    }
    exit 1
}

Write-Host 'Documentation validation passed.'
