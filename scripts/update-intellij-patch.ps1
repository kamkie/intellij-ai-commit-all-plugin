[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $PlatformVersion,

    [Parameter(Mandatory = $true)]
    [string] $AiAssistantPluginVersion
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'

function Invoke-Gradle
{
    param(
        [string] $Title,
        [string[]] $Arguments
    )

    Write-Host ''
    Write-Host "==> $Title"
    & $gradleWrapper @Arguments
    if ($LASTEXITCODE -ne 0)
    {
        throw "$Title failed with exit code $LASTEXITCODE. The working-tree update was not rolled back."
    }
}

if (-not (Test-Path -LiteralPath $gradleWrapper -PathType Leaf))
{
    throw "Gradle wrapper was not found at $gradleWrapper."
}

Push-Location -LiteralPath $repoRoot
try
{
    Invoke-Gradle `
        -Title 'Update IntelliJ patch coordinates' `
        -Arguments @(
        'updateIntellijPatch',
        "-PnewPlatformVersion=$PlatformVersion",
        "-PnewAiAssistantPluginVersion=$AiAssistantPluginVersion"
    )

    Invoke-Gradle `
        -Title 'Verify IntelliJ patch-version contract' `
        -Arguments @('verifyIntelliJPatchVersionContract')

    Invoke-Gradle `
        -Title 'Run focused patch-aware harness tests' `
        -Arguments @(
        'releaseMatrixUiTest',
        '--tests',
        'pl.devopssolutions.aicommitall.integration.ReleaseMatrixUiHarnessTest.intellijReleaseLineMatcherAcceptsBaseAndPatchVersionsOnly',
        '--tests',
        'pl.devopssolutions.aicommitall.integration.ReleaseMatrixUiHarnessTest.intellij2026Point2LicenseRestartContractAcceptsPatchVersionsAndRequiresExactOtherFields',
        '--tests',
        'pl.devopssolutions.aicommitall.integration.ReleaseMatrixUiHarnessTest.licenseRestartMarkerContractAcceptsPatchVersionsAndRequiresExactOtherFields',
        '--tests',
        'pl.devopssolutions.aicommitall.integration.ReleaseMatrixUiHarnessTest.licenseRestartOuterLifecycleRequiresExactMarkerFailureAndCleanFreshContext',
        '-PideProducts=IU',
        "-PideVersion=$PlatformVersion",
        '-Paicommitall.integrationCoverage=false'
    )

    Invoke-Gradle -Title 'Check formatting' -Arguments @('spotlessCheck')
    Invoke-Gradle -Title 'Build plugin' -Arguments @('buildPlugin')
    Invoke-Gradle -Title 'Run IntelliJ Plugin Verifier' -Arguments @('verifyPlugin')

    Invoke-Gradle `
        -Title 'Run PyCharm 2026.2 UI smoke lane' `
        -Arguments @(
        'releaseMatrixUiTest',
        '-PideProducts=PY',
        "-PideVersion=$PlatformVersion",
        '-Paicommitall.integrationCoverage=false'
    )
}
finally
{
    Pop-Location
}
