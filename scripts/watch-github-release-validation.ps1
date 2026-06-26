param(
    [Parameter(Mandatory = $true)]
    [string] $Tag,

    [string] $MainBranch = 'main',

    [string] $Repository = '',

    [int] $TimeoutMinutes = 90,

    [int] $PollSeconds = 30,

    [ValidateRange(0, 5)]
    [int] $ReleaseMatrixReruns = 1,

    [switch] $SkipMainBranch
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$semanticTagPattern = '^v[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z][0-9A-Za-z.-]*)?$'
if ($Tag -notmatch $semanticTagPattern)
{
    throw 'Release tag must use vMAJOR.MINOR.PATCH or vMAJOR.MINOR.PATCH-PRERELEASE.'
}

if ($TimeoutMinutes -le 0)
{
    throw 'TimeoutMinutes must be greater than zero.'
}

if ($PollSeconds -le 0)
{
    throw 'PollSeconds must be greater than zero.'
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$githubRunFields = 'databaseId,workflowName,headBranch,headSha,status,conclusion,event,createdAt,url'
$tagWorkflowNames = @('GitHub Release', 'Release Matrix UI')
$mainWorkflowNames = @('CI', 'Security', 'Plugin Verifier', 'CodeQL', 'Dependency Submission')
$rerunCounts = @{ }
$rerunRequestedAt = @{ }
$lastSummary = ''

function Invoke-RepositoryCommand
{
    param(
        [string] $Title,
        [string] $Executable,
        [string[]] $Arguments
    )

    $output = & $Executable @Arguments 2>&1
    if ($LASTEXITCODE -ne 0)
    {
        $renderedOutput = ($output | Out-String).Trim()
        throw "$Title failed with exit code $LASTEXITCODE. $renderedOutput"
    }

    return @($output)
}

function Invoke-GhJson
{
    param([string[]] $Arguments)

    $output = Invoke-RepositoryCommand -Title "gh $( $Arguments -join ' ' )" -Executable 'gh' -Arguments $Arguments
    $json = ($output | Out-String).Trim()
    if ( [string]::IsNullOrWhiteSpace($json))
    {
        return @()
    }

    return @($json | ConvertFrom-Json)
}

function Resolve-Repository
{
    param([string] $ExplicitRepository)

    if (-not [string]::IsNullOrWhiteSpace($ExplicitRepository))
    {
        return $ExplicitRepository
    }

    $repo = Invoke-GhJson -Arguments @('repo', 'view', '--json', 'nameWithOwner')
    return $repo.nameWithOwner
}

function Resolve-TagCommit
{
    param([string] $ReleaseTag)

    $revision = "$ReleaseTag^{commit}"
    $output = Invoke-RepositoryCommand -Title "git rev-parse $revision" -Executable 'git' -Arguments @('rev-parse', $revision)
    return ($output | Select-Object -First 1).Trim()
}

function Get-WorkflowRuns
{
    param([string] $Repo)

    return Invoke-GhJson -Arguments @(
        'run',
        'list',
        '--repo',
        $Repo,
        '--limit',
        '100',
        '--json',
        $githubRunFields
    )
}

function Select-WorkflowRun
{
    param(
        [object[]] $Runs,
        [string] $WorkflowName,
        [string] $BranchOrTag,
        [string] $HeadSha
    )

    $matches = @(
    $Runs |
        Where-Object {
            $_.workflowName -eq $WorkflowName -and
                $_.headBranch -eq $BranchOrTag -and
                $_.headSha -eq $HeadSha -and
                $_.event -eq 'push'
        } |
        Sort-Object -Property createdAt -Descending
    )

    if ($matches.Count -eq 0)
    {
        return $null
    }

    return $matches[0]
}

function Get-ExpectedRunTargets
{
    param(
        [string] $ReleaseTag,
        [string] $Branch,
        [string] $HeadSha,
        [bool] $IncludeMainBranch
    )

    $targets = @()
    foreach ($workflowName in $tagWorkflowNames)
    {
        $targets += [pscustomobject]@{
            WorkflowName = $workflowName
            BranchOrTag = $ReleaseTag
            HeadSha = $HeadSha
        }
    }

    if ($IncludeMainBranch)
    {
        foreach ($workflowName in $mainWorkflowNames)
        {
            $targets += [pscustomobject]@{
                WorkflowName = $workflowName
                BranchOrTag = $Branch
                HeadSha = $HeadSha
            }
        }
    }

    return $targets
}

function Get-RunSummary
{
    param(
        [object] $Target,
        [object] $Run
    )

    if ($null -eq $Run)
    {
        return "$( $Target.WorkflowName ) [$( $Target.BranchOrTag )]: waiting for run"
    }

    $conclusion = if ( [string]::IsNullOrWhiteSpace($Run.conclusion))
    {
        'pending'
    }
    else
    {
        $Run.conclusion
    }
    return "$( $Target.WorkflowName ) [$( $Target.BranchOrTag )]: $( $Run.status )/$conclusion"
}

function Invoke-ReleaseMatrixRerun
{
    param(
        [string] $Repo,
        [object] $Run,
        [int] $Attempt
    )

    Write-Host "Release Matrix UI failed; rerunning failed jobs for run $( $Run.databaseId ) (attempt $Attempt)."
    Invoke-RepositoryCommand `
        -Title "gh run rerun $( $Run.databaseId ) --failed" `
        -Executable 'gh' `
        -Arguments @('run', 'rerun', "$( $Run.databaseId )", '--repo', $Repo, '--failed') |
        Out-Null
}

function Test-RerunPropagationPending
{
    param(
        [string] $RerunKey,
        [int] $PropagationGraceSeconds = 120
    )

    if (-not $rerunRequestedAt.ContainsKey($RerunKey))
    {
        return $false
    }

    $elapsed = (Get-Date) - $rerunRequestedAt[$RerunKey]
    return $elapsed.TotalSeconds -lt $PropagationGraceSeconds
}

function Assert-GitHubRelease
{
    param(
        [string] $Repo,
        [string] $ReleaseTag
    )

    $release = Invoke-GhJson -Arguments @(
        'release',
        'view',
        $ReleaseTag,
        '--repo',
        $Repo,
        '--json',
        'url,isPrerelease,isDraft,assets'
    )

    if ($release.isDraft)
    {
        throw "GitHub release $ReleaseTag is still a draft: $( $release.url )"
    }

    if ($ReleaseTag.Contains('-') -and -not $release.isPrerelease)
    {
        throw "GitHub release $ReleaseTag should be marked as a prerelease: $( $release.url )"
    }

    $zipAssets = @($release.assets | Where-Object { $_.name -like '*.zip' })
    if ($zipAssets.Count -ne 1)
    {
        throw "GitHub release $ReleaseTag should have exactly one ZIP asset; found $( $zipAssets.Count )."
    }

    Write-Host "GitHub release ready: $( $release.url )"
    Write-Host "Release asset: $( $zipAssets[0].name )"
}

Push-Location -LiteralPath $repoRoot
try
{
    $repo = Resolve-Repository -ExplicitRepository $Repository
    $tagCommit = Resolve-TagCommit -ReleaseTag $Tag
    $targets = Get-ExpectedRunTargets `
        -ReleaseTag $Tag `
        -Branch $MainBranch `
        -HeadSha $tagCommit `
        -IncludeMainBranch (-not $SkipMainBranch)

    $deadline = (Get-Date).AddMinutes($TimeoutMinutes)
    Write-Host "Watching GitHub release validation for $Tag at $tagCommit in $repo."
    Write-Host "Timeout: $TimeoutMinutes minutes. Poll interval: $PollSeconds seconds."

    while ($true)
    {
        $runs = @(Get-WorkflowRuns -Repo $repo)
        $summaries = @()
        $pending = @()
        $failed = @()
        $rerunIssued = $false

        foreach ($target in $targets)
        {
            $run = Select-WorkflowRun `
                -Runs $runs `
                -WorkflowName $target.WorkflowName `
                -BranchOrTag $target.BranchOrTag `
                -HeadSha $target.HeadSha

            $summaries += Get-RunSummary -Target $target -Run $run

            if ($null -eq $run)
            {
                $pending += $target
                continue
            }

            if ($run.status -ne 'completed')
            {
                $pending += $target
                continue
            }

            if ($run.conclusion -eq 'success')
            {
                continue
            }

            $rerunKey = "$( $target.WorkflowName ):$( $target.BranchOrTag ):$( $target.HeadSha )"
            if ($target.WorkflowName -eq 'Release Matrix UI' -and
                -not $rerunIssued -and
                (-not $rerunCounts.ContainsKey($rerunKey)) -and
                $ReleaseMatrixReruns -gt 0)
            {
                $rerunCounts[$rerunKey] = 1
                $rerunRequestedAt[$rerunKey] = Get-Date
                Invoke-ReleaseMatrixRerun -Repo $repo -Run $run -Attempt 1
                $rerunIssued = $true
                break
            }

            if ($target.WorkflowName -eq 'Release Matrix UI' -and
                -not $rerunIssued -and
                $rerunCounts.ContainsKey($rerunKey) -and
                $rerunCounts[$rerunKey] -lt $ReleaseMatrixReruns)
            {
                $rerunCounts[$rerunKey]++
                $rerunRequestedAt[$rerunKey] = Get-Date
                Invoke-ReleaseMatrixRerun -Repo $repo -Run $run -Attempt $rerunCounts[$rerunKey]
                $rerunIssued = $true
                break
            }

            if ($target.WorkflowName -eq 'Release Matrix UI' -and
                (Test-RerunPropagationPending -RerunKey $rerunKey))
            {
                $pending += $target
                continue
            }

            $failed += [pscustomobject]@{
                Target = $target
                Run = $run
            }
        }

        $summary = $summaries -join '; '
        if ($summary -ne $lastSummary)
        {
            Write-Host $summary
            $lastSummary = $summary
        }

        if ($rerunIssued)
        {
            Start-Sleep -Seconds ([Math]::Min($PollSeconds, 10))
            continue
        }

        if ($failed.Count -gt 0)
        {
            $failureSummary = @(
            $failed |
                ForEach-Object {
                    "$( $_.Target.WorkflowName ) [$( $_.Target.BranchOrTag )] failed with conclusion $( $_.Run.conclusion ): $( $_.Run.url )"
                }
            ) -join "`n"
            throw "Release validation failed.`n$failureSummary"
        }

        if ($pending.Count -eq 0)
        {
            Assert-GitHubRelease -Repo $repo -ReleaseTag $Tag
            Write-Host "GitHub release validation completed successfully for $Tag."
            break
        }

        if ((Get-Date) -ge $deadline)
        {
            throw "Timed out waiting for GitHub release validation after $TimeoutMinutes minutes. Latest status: $summary"
        }

        Start-Sleep -Seconds $PollSeconds
    }
}
finally
{
    Pop-Location
}
