[CmdletBinding(PositionalBinding = $false)]
param(
    [string] $ActVersion = 'latest',
    [switch] $InstallOnly,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $ActArguments
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$actToolRoot = Join-Path $repoRoot '.tools/act'

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

function Get-PlatformAssetPattern
{
    $architecture = switch ([System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture)
    {
        'X64' { 'x86_64' }
        'Arm64' { 'arm64' }
        'X86' { 'i386' }
        default { throw "Unsupported CPU architecture: $([System.Runtime.InteropServices.RuntimeInformation]::OSArchitecture)" }
    }

    if ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform(
            [System.Runtime.InteropServices.OSPlatform]::Windows))
    {
        return "^act_Windows_$architecture\.zip$"
    }

    if ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform(
            [System.Runtime.InteropServices.OSPlatform]::Linux))
    {
        return "^act_Linux_$architecture\.tar\.gz$"
    }

    if ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform(
            [System.Runtime.InteropServices.OSPlatform]::OSX))
    {
        return "^act_Darwin_$architecture\.tar\.gz$"
    }

    throw "Unsupported operating system: $([System.Runtime.InteropServices.RuntimeInformation]::OSDescription)"
}

function Get-ActExecutableName
{
    if ([System.Runtime.InteropServices.RuntimeInformation]::IsOSPlatform(
            [System.Runtime.InteropServices.OSPlatform]::Windows))
    {
        return 'act.exe'
    }

    return 'act'
}

function Get-ActRelease
{
    param([string] $Version)

    $headers = @{ 'User-Agent' = 'intellij-ai-commit-all-act-setup' }
    if ($Version -eq 'latest')
    {
        return Invoke-RestMethod `
            -Uri 'https://api.github.com/repos/nektos/act/releases/latest' `
            -Headers $headers
    }

    $tag = if ($Version.StartsWith('v', [System.StringComparison]::OrdinalIgnoreCase))
    {
        $Version
    }
    else
    {
        "v$Version"
    }

    return Invoke-RestMethod `
        -Uri "https://api.github.com/repos/nektos/act/releases/tags/$tag" `
        -Headers $headers
}

function Install-Act
{
    param([string] $Version)

    $release = Get-ActRelease -Version $Version
    $assetPattern = Get-PlatformAssetPattern
    $asset = @($release.assets | Where-Object { $_.name -match $assetPattern } | Select-Object -First 1)

    if ($asset.Count -ne 1)
    {
        throw "Could not find an act release asset matching $assetPattern for $($release.tag_name)."
    }

    $installDirectory = Join-Path $actToolRoot $release.tag_name
    $downloadDirectory = Join-Path $actToolRoot '_download'
    $archivePath = Join-Path $downloadDirectory $asset[0].name

    Assert-PathInsideDirectory -Path $installDirectory -ParentDirectory $actToolRoot
    Assert-PathInsideDirectory -Path $downloadDirectory -ParentDirectory $actToolRoot

    if (Test-Path -LiteralPath (Join-Path $installDirectory (Get-ActExecutableName)) -PathType Leaf)
    {
        return (Join-Path $installDirectory (Get-ActExecutableName))
    }

    New-Item -ItemType Directory -Force -Path $downloadDirectory | Out-Null
    New-Item -ItemType Directory -Force -Path $installDirectory | Out-Null

    Write-Host "Downloading nektos/act $($release.tag_name) to $archivePath."
    Invoke-WebRequest -Uri $asset[0].browser_download_url -OutFile $archivePath

    if ($asset[0].name.EndsWith('.zip', [System.StringComparison]::OrdinalIgnoreCase))
    {
        Expand-Archive -LiteralPath $archivePath -DestinationPath $installDirectory -Force
    }
    else
    {
        & tar -xzf $archivePath -C $installDirectory
        if ($LASTEXITCODE -ne 0)
        {
            throw "Extracting $archivePath failed with exit code $LASTEXITCODE."
        }

        & chmod +x (Join-Path $installDirectory 'act')
        if ($LASTEXITCODE -ne 0)
        {
            throw "Marking act executable failed with exit code $LASTEXITCODE."
        }
    }

    return (Join-Path $installDirectory (Get-ActExecutableName))
}

function Get-ActExecutable
{
    $pathAct = Get-Command act -ErrorAction SilentlyContinue
    if ($null -ne $pathAct)
    {
        return $pathAct.Source
    }

    return Install-Act -Version $ActVersion
}

$actExecutable = Get-ActExecutable

if ($InstallOnly)
{
    Write-Host "act is available at $actExecutable."
    exit 0
}

if ($ActArguments.Count -eq 0)
{
    $ActArguments = @(
        'pull_request',
        '--workflows',
        '.github/workflows/ci.yml',
        '--job',
        'build'
    )
}

Push-Location -LiteralPath $repoRoot
try
{
    & $actExecutable @ActArguments
    $exitCode = $LASTEXITCODE
}
finally
{
    Pop-Location
}

exit $exitCode
