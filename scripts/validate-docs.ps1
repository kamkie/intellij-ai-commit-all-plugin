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

$planFiles = Get-ChildItem -LiteralPath (Join-Path $repoRoot '.agents/plans') -File -Filter '*.md' |
    Where-Object { $_.Name -notin @('README.md', 'PLAN_TEMPLATE.md') }

foreach ($plan in $planFiles) {
    $relative = Get-RelativePath $plan.FullName
    $text = Get-Content -Raw -LiteralPath $plan.FullName
    $planIdMatch = [regex]::Match($text, '(?m)^Plan-ID:\s+(P-[A-Za-z0-9][A-Za-z0-9-]*)\s*$')

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

$adrFiles = Get-ChildItem -LiteralPath (Join-Path $repoRoot 'docs/decisions') -File -Filter '*.md' |
    Where-Object { $_.Name -match '^\d{4}-' } |
    Sort-Object Name
$adrReadmePath = Join-Path $repoRoot 'docs/decisions/README.md'
$adrReadmeText = ''
if (Test-Path -LiteralPath $adrReadmePath) {
    $adrReadmeText = Get-Content -Raw -LiteralPath $adrReadmePath
}

for ($i = 0; $i -lt $adrFiles.Count; $i++) {
    $expected = '{0:D4}' -f $i
    $actual = $adrFiles[$i].Name.Substring(0, 4)
    if ($actual -ne $expected) {
        Add-ValidationError "ADR sequence expected $expected but found $($adrFiles[$i].Name)"
    }

    $expectedIndexEntry = "[$actual]($($adrFiles[$i].Name))"
    if (-not $adrReadmeText.Contains($expectedIndexEntry)) {
        Add-ValidationError "docs/decisions/README.md is missing ADR index entry $expectedIndexEntry"
    }
}

$proposalFiles = Get-ChildItem -LiteralPath (Join-Path $repoRoot 'docs/proposals') -File -Filter '*.md' |
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
