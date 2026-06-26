param(
    [string] $PluginVerifierIdeVersions = 'IU-2026.1.1,PY-2026.1.1,WS-2026.1.1',
    [string] $PluginPublishChannels = 'default',
    [switch] $Resume
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
$pluginVerifierReportDirectory = Join-Path $repoRoot 'build/reports/pluginVerifier'
$splitReportDirectory = Join-Path $repoRoot 'build/reports/pluginVerifier-local-prerelease'
$statusDirectory = Join-Path $repoRoot 'build/reports/local-prerelease-validation'
$statusPath = Join-Path $statusDirectory 'status.json'
$scriptVersion = 2
$script:validationState = $null

function Get-IdeVersions
{
    param([string] $Value)

    $versions = @(
    $Value.Split(',') |
        ForEach-Object { $_.Trim() } |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )

    if ($versions.Count -eq 0)
    {
        throw 'At least one Plugin Verifier IDE version is required.'
    }

    return $versions
}

function Invoke-RepositoryCommand
{
    param(
        [string] $Title,
        [string] $Executable,
        [string[]] $Arguments
    )

    Write-Host ''
    Write-Host "==> $Title"
    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0)
    {
        throw "$Title failed with exit code $LASTEXITCODE."
    }
}

function Get-RepositoryCommandOutput
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

function Assert-PathInsideDirectory
{
    param(
        [string] $Path,
        [string] $ParentDirectory
    )

    $fullPath = [System.IO.Path]::GetFullPath($Path)
    $fullParentDirectory = [System.IO.Path]::GetFullPath($ParentDirectory).TrimEnd('\', '/') +
        [System.IO.Path]::DirectorySeparatorChar

    if (-not $fullPath.StartsWith($fullParentDirectory, [System.StringComparison]::OrdinalIgnoreCase))
    {
        throw "Refusing to operate outside $fullParentDirectory`: $fullPath"
    }
}

function Copy-PluginVerifierReport
{
    param([string] $IdeVersion)

    if (-not (Test-Path -LiteralPath $pluginVerifierReportDirectory -PathType Container))
    {
        throw "Plugin Verifier report directory was not found at $pluginVerifierReportDirectory."
    }

    $safeIdeVersion = Get-SafeStepName $IdeVersion
    $targetDirectory = Join-Path $splitReportDirectory $safeIdeVersion
    Assert-PathInsideDirectory -Path $targetDirectory -ParentDirectory (Join-Path $repoRoot 'build')

    New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
    Copy-Item -Path (Join-Path $pluginVerifierReportDirectory '*') -Destination $targetDirectory -Recurse -Force
    Write-Host "Preserved Plugin Verifier report for $IdeVersion at $targetDirectory."
}

function Get-SafeStepName
{
    param([string] $Value)

    return $Value -replace '[^A-Za-z0-9._-]', '_'
}

function Get-HeadSha
{
    $output = Get-RepositoryCommandOutput `
        -Title 'git rev-parse HEAD' `
        -Executable 'git' `
        -Arguments @('rev-parse', 'HEAD')

    return ($output | Select-Object -First 1).Trim()
}

function New-ValidationContext
{
    param(
        [string] $HeadSha,
        [string[]] $IdeVersions,
        [string] $PublishChannels
    )

    return [ordered]@{
        ScriptVersion = $scriptVersion
        HeadSha = $HeadSha
        PluginVerifierIdeVersions = ($IdeVersions -join ',')
        PluginPublishChannels = $PublishChannels
    }
}

function New-ValidationState
{
    param([System.Collections.IDictionary] $Context)

    return [ordered]@{
        Context = $Context
        Steps = [ordered]@{ }
    }
}

function Import-ValidationState
{
    if (-not (Test-Path -LiteralPath $statusPath -PathType Leaf))
    {
        return $null
    }

    return Get-Content -Raw -LiteralPath $statusPath | ConvertFrom-Json -AsHashtable
}

function Save-ValidationState
{
    New-Item -ItemType Directory -Force -Path $statusDirectory | Out-Null
    $json = $script:validationState | ConvertTo-Json -Depth 8
    [System.IO.File]::WriteAllText($statusPath, $json,[System.Text.UTF8Encoding]::new($false))
}

function Assert-ResumeContext
{
    param(
        [System.Collections.IDictionary] $Expected,
        [System.Collections.IDictionary] $Actual
    )

    foreach ($key in $Expected.Keys)
    {
        if (-not $Actual.ContainsKey($key) -or "$( $Actual[$key] )" -ne "$( $Expected[$key] )")
        {
            throw "Cannot resume local prerelease validation because saved context '$key' is '$( $Actual[$key] )', expected '$( $Expected[$key] )'. Run without -Resume to start a fresh validation."
        }
    }
}

function Get-StepRecord
{
    param([string] $Id)

    if ( $script:validationState['Steps'].Contains($Id))
    {
        return $script:validationState['Steps'][$Id]
    }

    return $null
}

function Test-StepEvidence
{
    param([string] $EvidencePath)

    if ( [string]::IsNullOrWhiteSpace($EvidencePath))
    {
        return $true
    }

    return Test-Path -LiteralPath $EvidencePath
}

function Invoke-ValidationStep
{
    param(
        [string] $Id,
        [string] $Title,
        [scriptblock] $Action,
        [string] $EvidencePath = ''
    )

    $record = Get-StepRecord -Id $Id
    if ($Resume -and
        $null -ne $record -and
        $record.ContainsKey('Status') -and
        $record['Status'] -eq 'completed' -and
        (Test-StepEvidence -EvidencePath $EvidencePath))
    {
        Write-Host ''
        Write-Host "==> $Title"
        Write-Host "Skipping completed step from $( $record['CompletedAt'] ) ($( $record['DurationSeconds'] )s)."
        return
    }

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    try
    {
        & $Action
        $stopwatch.Stop()
        $script:validationState['Steps'][$Id] = [ordered]@{
            Status = 'completed'
            Title = $Title
            CompletedAt = (Get-Date).ToString('o')
            DurationSeconds = [Math]::Round($stopwatch.Elapsed.TotalSeconds, 3)
            EvidencePath = $EvidencePath
        }
        Save-ValidationState
    }
    catch
    {
        $stopwatch.Stop()
        $script:validationState['Steps'][$Id] = [ordered]@{
            Status = 'failed'
            Title = $Title
            FailedAt = (Get-Date).ToString('o')
            DurationSeconds = [Math]::Round($stopwatch.Elapsed.TotalSeconds, 3)
            Error = $_.Exception.Message
            EvidencePath = $EvidencePath
        }
        Save-ValidationState
        throw
    }
}

function Write-ValidationSummary
{
    Write-Host ''
    Write-Host "Local prerelease validation status: $statusPath"
    foreach ($entry in $script:validationState['Steps'].GetEnumerator())
    {
        $record = $entry.Value
        $status = $record['Status']
        $duration = if ( $record.ContainsKey('DurationSeconds'))
        {
            $record['DurationSeconds']
        }
        else
        {
            'n/a'
        }
        Write-Host "- $( $record['Title'] ): $status (${duration}s)"
    }
}

if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf))
{
    throw "Gradle wrapper was not found at $gradleWrapper."
}

$ideVersions = Get-IdeVersions $PluginVerifierIdeVersions
$headSha = Get-HeadSha
$validationContext = New-ValidationContext `
    -HeadSha $headSha `
    -IdeVersions $ideVersions `
    -PublishChannels $PluginPublishChannels

Assert-PathInsideDirectory -Path $splitReportDirectory -ParentDirectory (Join-Path $repoRoot 'build')
Assert-PathInsideDirectory -Path $statusDirectory -ParentDirectory (Join-Path $repoRoot 'build')

if ($Resume)
{
    $loadedState = Import-ValidationState
    if ($null -eq $loadedState)
    {
        Write-Host "No saved local prerelease validation status found at $statusPath. Starting fresh."
        $script:validationState = New-ValidationState -Context $validationContext
    }
    else
    {
        Assert-ResumeContext -Expected $validationContext -Actual $loadedState['Context']
        $script:validationState = $loadedState
        Write-Host "Resuming local prerelease validation from $statusPath."
    }
}
else
{
    $script:validationState = New-ValidationState -Context $validationContext
}

Push-Location -LiteralPath $repoRoot
try
{
    if (-not $Resume -and (Test-Path -LiteralPath $splitReportDirectory))
    {
        Remove-Item -LiteralPath $splitReportDirectory -Recurse -Force
    }

    Invoke-ValidationStep `
        -Id 'marketplace-change-notes' `
        -Title 'Check Marketplace change notes' `
        -Action {
        Invoke-RepositoryCommand `
            -Title 'Check Marketplace change notes' `
            -Executable 'pwsh' `
            -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/generate-intellij-platform-change-notes.ps1', '-Check')
    }

    Invoke-ValidationStep `
        -Id 'marketplace-description' `
        -Title 'Check Marketplace description' `
        -Action {
        Invoke-RepositoryCommand `
            -Title 'Check Marketplace description' `
            -Executable 'pwsh' `
            -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/generate-intellij-platform-description.ps1', '-Check')
    }

    Invoke-ValidationStep `
        -Id 'validate-docs' `
        -Title 'Validate documentation' `
        -Action {
        Invoke-RepositoryCommand `
            -Title 'Validate documentation' `
            -Executable 'pwsh' `
            -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/validate-docs.ps1')
    }

    Invoke-ValidationStep `
        -Id 'validate-agent-artifacts' `
        -Title 'Validate agent artifacts' `
        -Action {
        Invoke-RepositoryCommand `
            -Title 'Validate agent artifacts' `
            -Executable 'pwsh' `
            -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/ai/validate-agent-artifacts.ps1')
    }

    Invoke-ValidationStep `
        -Id 'gradle-non-verifier-gates' `
        -Title 'Run build, tests, coverage, structure, and packaging gates' `
        -Action {
        Invoke-RepositoryCommand `
            -Title 'Run build, tests, coverage, structure, and packaging gates' `
            -Executable $gradleWrapper `
            -Arguments @(
            'spotlessCheck',
            'verifyDetektBaseline',
            'detekt',
            'test',
            'jacocoTestReport',
            'verifyJacocoCoverageReport',
            'verifyPluginStructure',
            'buildPlugin',
            "-PpluginPublishChannels=$PluginPublishChannels"
        )
    }

    foreach ($ideVersion in $ideVersions)
    {
        $safeIdeVersion = Get-SafeStepName $ideVersion
        $verifierEvidencePath = Join-Path $splitReportDirectory $safeIdeVersion
        Invoke-ValidationStep `
            -Id "plugin-verifier-$safeIdeVersion" `
            -Title "Run Plugin Verifier for $ideVersion" `
            -EvidencePath $verifierEvidencePath `
            -Action {
            Invoke-RepositoryCommand `
                -Title "Run Plugin Verifier for $ideVersion" `
                -Executable $gradleWrapper `
                -Arguments @(
                'verifyPlugin',
                "-PpluginVerifierIdeVersions=$ideVersion",
                "-PpluginPublishChannels=$PluginPublishChannels"
            )
            Copy-PluginVerifierReport $ideVersion
        }
    }

    Write-Host ''
    Write-Host "Split Plugin Verifier reports: $splitReportDirectory"
    Write-Host "Local prerelease validation completed for Plugin Verifier IDEs: $( $ideVersions -join ', ' )."
    Write-ValidationSummary
}
finally
{
    Pop-Location
}
