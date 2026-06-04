param(
    [string] $PluginVerifierIdeVersions = 'IU-2026.1.1,PY-2026.1.1,WS-2026.1.1',
    [string] $PluginPublishChannels = 'default'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
$pluginVerifierReportDirectory = Join-Path $repoRoot 'build/reports/pluginVerifier'
$splitReportDirectory = Join-Path $repoRoot 'build/reports/pluginVerifier-local-prerelease'

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

    $safeIdeVersion = $IdeVersion -replace '[^A-Za-z0-9._-]', '_'
    $targetDirectory = Join-Path $splitReportDirectory $safeIdeVersion
    Assert-PathInsideDirectory -Path $targetDirectory -ParentDirectory (Join-Path $repoRoot 'build')

    New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
    Copy-Item -Path (Join-Path $pluginVerifierReportDirectory '*') -Destination $targetDirectory -Recurse -Force
    Write-Host "Preserved Plugin Verifier report for $IdeVersion at $targetDirectory."
}

if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf))
{
    throw "Gradle wrapper was not found at $gradleWrapper."
}

$ideVersions = Get-IdeVersions $PluginVerifierIdeVersions
Assert-PathInsideDirectory -Path $splitReportDirectory -ParentDirectory (Join-Path $repoRoot 'build')

Push-Location -LiteralPath $repoRoot
try
{
    if (Test-Path -LiteralPath $splitReportDirectory)
    {
        Remove-Item -LiteralPath $splitReportDirectory -Recurse -Force
    }

    Invoke-RepositoryCommand `
        -Title 'Check Marketplace change notes' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/generate-intellij-platform-change-notes.ps1', '-Check')

    Invoke-RepositoryCommand `
        -Title 'Check Marketplace description' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/generate-intellij-platform-description.ps1', '-Check')

    Invoke-RepositoryCommand `
        -Title 'Validate documentation' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/validate-docs.ps1')

    Invoke-RepositoryCommand `
        -Title 'Validate agent artifacts' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/ai/validate-agent-artifacts.ps1')

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

    foreach ($ideVersion in $ideVersions)
    {
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

    Write-Host ''
    Write-Host "Split Plugin Verifier reports: $splitReportDirectory"
    Write-Host "Local prerelease validation completed for Plugin Verifier IDEs: $( $ideVersions -join ', ' )."
}
finally
{
    Pop-Location
}
