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

$isoTimestampPattern = '\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:Z|[+-]\d{2}:\d{2})'

function Test-IsoTimestamp
{
    param([string] $Value)
    return $Value -match "^$isoTimestampPattern$"
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
$planStatusesRequiringApprovalIdentity = @('Approved', 'In Progress', 'Blocked', 'Implemented', 'Closed')

$planRoot = Join-Path $repoRoot '.agents/plans'
$planArchiveRoot = Resolve-Path (Join-Path $planRoot 'archive')
$planFiles = Get-ChildItem -LiteralPath $planRoot -Recurse -File -Filter '*.md' |
    Where-Object { $_.Name -notin @('README.md', 'PLAN_TEMPLATE.md') }

foreach ($plan in $planFiles) {
    $relative = Get-RelativePath $plan.FullName
    $text = Get-Content -Raw -LiteralPath $plan.FullName
    $isArchivedPlan = $plan.FullName.StartsWith($planArchiveRoot.Path, [System.StringComparison]::OrdinalIgnoreCase)
    $planIdMatch = [regex]::Match($text, '(?m)^Plan-ID:\s+(PLAN-[A-Za-z0-9][A-Za-z0-9-]*)\s*$')

    if (-not $planIdMatch.Success) {
        Add-ValidationError "$relative is missing a stable Plan-ID"
    } else {
        $planId = $planIdMatch.Groups[1].Value
        if (-not $plan.Name.StartsWith($planId, [System.StringComparison]::Ordinal)) {
            Add-ValidationError "$relative filename must include Plan-ID prefix $planId"
        }
    }

    $planStatus = $null
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

    $readinessMatch = [regex]::Match($text, '(?ms)^## Readiness\s*(.*?)(?=^## |\z)')
    if (-not $readinessMatch.Success) {
        Add-ValidationError "$relative is missing a ## Readiness section"
    } else {
        $readinessText = $readinessMatch.Groups[1].Value
        $approvedByMatch = [regex]::Match($readinessText, '(?m)^-\s+Approved by:\s*(.*?)\s*$')
        $approvedBy = if ($approvedByMatch.Success) { $approvedByMatch.Groups[1].Value.Trim() } else { '' }
        $approvedAtMatch = [regex]::Match($readinessText, '(?m)^-\s+Approved at:\s*(.*?)\s*$')
        $approvedAt = if ($approvedAtMatch.Success)
        {
            $approvedAtMatch.Groups[1].Value.Trim()
        }
        else
        {
            ''
        }

        if ($readinessText -match '(?m)^-\s+Open questions:\s+None known\.\s*$') {
            Add-ValidationError "$relative uses ambiguous readiness wording; use 'Open questions: None.'"
        }

        if ($planStatus -ne $null) {
            $requiresApprovalIdentity = $planStatusesRequiringApprovalIdentity -contains $planStatus
            if ($requiresApprovalIdentity -and [string]::IsNullOrWhiteSpace($approvedBy)) {
                Add-ValidationError "$relative has Status '$planStatus' but is missing Approved by in ## Readiness"
            }

            if ($requiresApprovalIdentity -and -not $isArchivedPlan -and [string]::IsNullOrWhiteSpace($approvedAt))
            {
                Add-ValidationError "$relative has Status '$planStatus' but is missing Approved at in ## Readiness"
            }

            if (-not $requiresApprovalIdentity -and -not [string]::IsNullOrWhiteSpace($approvedBy)) {
                Add-ValidationError "$relative has Status '$planStatus' but must not claim approval in Approved by"
            }

            if (-not $requiresApprovalIdentity -and -not [string]::IsNullOrWhiteSpace($approvedAt))
            {
                Add-ValidationError "$relative has Status '$planStatus' but must not claim approval in Approved at"
            }
        }

        if (-not [string]::IsNullOrWhiteSpace($approvedAt) -and -not (Test-IsoTimestamp $approvedAt))
        {
            Add-ValidationError "$relative Approved at must use ISO 8601 timestamp with timezone offset"
        }

        if (-not [string]::IsNullOrWhiteSpace($approvedBy)) {
            if ($approvedBy -match '(?i)^(user|maintainer|pending|none|none known|unknown|n/a|tbd)$') {
                Add-ValidationError "$relative has ambiguous Approved by value '$approvedBy'"
            }

            if (($approvedBy.Contains('<') -or $approvedBy.Contains('>')) -and
                $approvedBy -notmatch '^[^<>\r\n]+\s+<[^<>\s@]+@[^<>\s@]+\.[^<>\s@]+>$') {
                Add-ValidationError "$relative Approved by must use Name <email> form when an email identity is used"
            }
        }
    }

    $statusHistoryMatch = [regex]::Match($text, '(?ms)^## Status History\s*(.*?)(?=^## |\z)')
    if (-not $isArchivedPlan -and -not $statusHistoryMatch.Success)
    {
        Add-ValidationError "$relative is missing a ## Status History section"
    }

    if ($statusHistoryMatch.Success)
    {
        $statusHistoryText = $statusHistoryMatch.Groups[1].Value
        $statusHistoryEntries = [regex]::Matches(
                $statusHistoryText,
                "(?m)^-\s+($isoTimestampPattern):\s+(.+?)\s+->\s+(.+?)\s+by\s+(.+?);\s+.+$"
        )

        if ($statusHistoryEntries.Count -eq 0)
        {
            Add-ValidationError "$relative has no valid status-history entries"
        }
        elseif ($planStatus -ne $null)
        {
            $latestStatus = $statusHistoryEntries[$statusHistoryEntries.Count - 1].Groups[3].Value.Trim()
            if ($latestStatus -ne $planStatus)
            {
                Add-ValidationError "$relative Status '$planStatus' must match latest status-history target '$latestStatus'"
            }
        }

        foreach ($entry in $statusHistoryEntries)
        {
            $actor = $entry.Groups[4].Value.Trim()
            if ($actor -match '(?i)^(user|maintainer|pending|none|none known|unknown|n/a|tbd)$')
            {
                Add-ValidationError "$relative has ambiguous status-history actor '$actor'"
            }

            if ($actor -notmatch '^[^<>\r\n]+\s+<[^<>\s@]+@[^<>\s@]+\.[^<>\s@]+>$')
            {
                Add-ValidationError "$relative status-history actor must use Name <email> form"
            }
        }
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

    $adrStatusMatch = [regex]::Match($frontMatter, '(?m)^status:\s+(proposed|rejected|accepted|deprecated|superseded by .+)\s*$')
    if (-not $adrStatusMatch.Success)
    {
        Add-ValidationError "$relative front matter has invalid MADR status"
    }

    if ($frontMatter -notmatch '(?m)^date:\s+\d{4}-\d{2}-\d{2}\s*$') {
        Add-ValidationError "$relative front matter has invalid MADR date"
    }

    $acceptedAtMatch = [regex]::Match($frontMatter, '(?m)^accepted_at:\s+(\S+)\s*$')
    if ($acceptedAtMatch.Success -and -not (Test-IsoTimestamp $acceptedAtMatch.Groups[1].Value.Trim()))
    {
        Add-ValidationError "$relative accepted_at must use ISO 8601 timestamp with timezone offset"
    }

    $requiresAcceptedAt = $adrStatusMatch.Success -and
            $adrStatusMatch.Groups[1].Value -eq 'accepted' -and
            [int]$actual -ge 44
    if ($requiresAcceptedAt -and -not $acceptedAtMatch.Success)
    {
        Add-ValidationError "$relative accepted ADRs from ard-0044 onward must include accepted_at"
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

$proposalRoot = Join-Path $repoRoot 'docs/proposals'
$proposalArchiveRoot = Resolve-Path (Join-Path $proposalRoot 'archive')
$proposalFiles = Get-ChildItem -LiteralPath $proposalRoot -Recurse -File -Filter '*.md' |
    Where-Object { $_.Name -notin @('README.md', 'PROPOSAL_TEMPLATE.md') }

foreach ($proposal in $proposalFiles) {
    $relative = Get-RelativePath $proposal.FullName
    $text = Get-Content -Raw -LiteralPath $proposal.FullName
    $isArchivedProposal = $proposal.FullName.StartsWith($proposalArchiveRoot.Path, [System.StringComparison]::OrdinalIgnoreCase)

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
    $tableIds = [regex]::Matches($text, '(?m)^\|\s+([EDS]\d+)\s+\|') | ForEach-Object { $_.Groups[1].Value }
    $findingIdPattern = if ($isArchivedProposal)
    {
        '^[EDS]\d+$'
    }
    else
    {
        '^[EDS]\d{3}$'
    }

    foreach ($sectionId in $sectionIds)
    {
        if ($sectionId -notmatch $findingIdPattern)
        {
            Add-ValidationError "$relative finding $sectionId must use three-digit IDs in active proposals"
        }
    }

    foreach ($tableId in $tableIds)
    {
        if ($tableId -notmatch $findingIdPattern)
        {
            Add-ValidationError "$relative Progress Tracker finding $tableId must use three-digit IDs in active proposals"
        }
    }

    $sectionOnly = Compare-Object $sectionIds $tableIds | Where-Object { $_.SideIndicator -eq '<=' }
    $tableOnly = Compare-Object $sectionIds $tableIds | Where-Object { $_.SideIndicator -eq '=>' }

    foreach ($diff in $sectionOnly) {
        Add-ValidationError "$relative finding $($diff.InputObject) is missing from Progress Tracker"
    }
    foreach ($diff in $tableOnly) {
        Add-ValidationError "$relative Progress Tracker references missing finding $($diff.InputObject)"
    }

    foreach ($findingMatch in [regex]::Matches($text, '(?ms)^###\s+([EDS]\d+)\..*?```yaml\s*(.*?)\s*```'))
    {
        $findingId = $findingMatch.Groups[1].Value
        $yaml = $findingMatch.Groups[2].Value
        $decisionMatch = [regex]::Match($yaml, '(?m)^decision:[ \t]*(.*?)[ \t]*$')
        $decision = if ($decisionMatch.Success)
        {
            $decisionMatch.Groups[1].Value.Trim()
        }
        else
        {
            ''
        }
        $updatedMatch = [regex]::Match($yaml, '(?m)^updated:[ \t]*(.*?)[ \t]*$')
        $updated = if ($updatedMatch.Success)
        {
            $updatedMatch.Groups[1].Value.Trim()
        }
        else
        {
            ''
        }
        $acceptedAtMatch = [regex]::Match($yaml, '(?m)^accepted_at:[ \t]*(.*?)[ \t]*$')
        $acceptedAt = if ($acceptedAtMatch.Success)
        {
            $acceptedAtMatch.Groups[1].Value.Trim()
        }
        else
        {
            ''
        }
        $decidedAtMatch = [regex]::Match($yaml, '(?m)^decided_at:[ \t]*(.*?)[ \t]*$')
        $decidedAt = if ($decidedAtMatch.Success)
        {
            $decidedAtMatch.Groups[1].Value.Trim()
        }
        else
        {
            ''
        }

        if (-not [string]::IsNullOrWhiteSpace($acceptedAt) -and -not (Test-IsoTimestamp $acceptedAt))
        {
            Add-ValidationError "$relative finding $findingId accepted_at must use ISO 8601 timestamp with timezone offset"
        }

        if (-not [string]::IsNullOrWhiteSpace($decidedAt) -and -not (Test-IsoTimestamp $decidedAt))
        {
            Add-ValidationError "$relative finding $findingId decided_at must use ISO 8601 timestamp with timezone offset"
        }

        if (-not $isArchivedProposal)
        {
            if ($decision -eq 'accepted')
            {
                if ( [string]::IsNullOrWhiteSpace($acceptedAt))
                {
                    Add-ValidationError "$relative finding $findingId has decision accepted but is missing accepted_at"
                }
            }
            elseif (-not [string]::IsNullOrWhiteSpace($decision))
            {
                if ( [string]::IsNullOrWhiteSpace($decidedAt))
                {
                    Add-ValidationError "$relative finding $findingId has decision '$decision' but is missing decided_at"
                }
            }
            elseif (-not [string]::IsNullOrWhiteSpace($acceptedAt) -or -not [string]::IsNullOrWhiteSpace($decidedAt))
            {
                Add-ValidationError "$relative finding $findingId has decision timestamps but no decision"
            }

            if (-not [string]::IsNullOrWhiteSpace($decision) -and -not (Test-IsoTimestamp $updated))
            {
                Add-ValidationError "$relative finding $findingId with a decision must use a timestamp in updated"
            }
        }
    }
}

$proposalTemplatePath = Join-Path $proposalRoot 'PROPOSAL_TEMPLATE.md'
if (Test-Path -LiteralPath $proposalTemplatePath)
{
    $templateText = Get-Content -Raw -LiteralPath $proposalTemplatePath
    $templateRelative = Get-RelativePath $proposalTemplatePath
    $templateIds = [regex]::Matches($templateText, '(?m)(?:^###\s+|\|\s+)([EDS]\d+)(?:\.|\s+\|)') |
            ForEach-Object { $_.Groups[1].Value }

    foreach ($templateId in $templateIds)
    {
        if ($templateId -notmatch '^[EDS]\d{3}$')
        {
            Add-ValidationError "$templateRelative example finding $templateId must use three-digit IDs"
        }
    }

    if ($templateText -notmatch '(?m)^decision:[ \t]*$')
    {
        Add-ValidationError "$templateRelative must start example findings with an empty decision"
    }

    foreach ($key in @('accepted_at', 'decided_at'))
    {
        if ($templateText -notmatch "(?m)^${key}:[ \t]*$")
        {
            Add-ValidationError "$templateRelative must include empty $key in example tracker blocks"
        }
    }
}

if ($errors.Count -gt 0) {
    foreach ($validationError in $errors) {
        Write-Error $validationError -ErrorAction Continue
    }
    exit 1
}

Write-Host 'Documentation validation passed.'
