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

function Test-ActFlagPresent
{
    param(
        [string[]] $Arguments,
        [string] $LongName,
        [string] $ShortName = ''
    )

    foreach ($argument in $Arguments)
    {
        if ($argument -eq $LongName -or $argument.StartsWith("$LongName=", [System.StringComparison]::OrdinalIgnoreCase))
        {
            return $true
        }

        if ($ShortName -ne '' -and $argument -eq $ShortName)
        {
            return $true
        }
    }

    return $false
}

function Get-ActOptionValues
{
    param(
        [string[]] $Arguments,
        [string] $LongName,
        [string] $ShortName = ''
    )

    $values = @()
    for ($index = 0; $index -lt $Arguments.Count; $index++)
    {
        $argument = $Arguments[$index]

        if ( $argument.StartsWith("$LongName=", [System.StringComparison]::OrdinalIgnoreCase))
        {
            $values += $argument.Substring($argument.IndexOf('=') + 1)
            continue
        }

        if ($ShortName -ne '' -and $argument.StartsWith("$ShortName=", [System.StringComparison]::OrdinalIgnoreCase))
        {
            $values += $argument.Substring($argument.IndexOf('=') + 1)
            continue
        }

        if ($argument -eq $LongName -or ($ShortName -ne '' -and $argument -eq $ShortName))
        {
            if ($index + 1 -lt $Arguments.Count)
            {
                $values += $Arguments[$index + 1]
            }
        }
    }

    return $values
}

function Test-ReleaseMatrixUiWorkflow
{
    param([string[]] $Arguments)

    foreach ($argument in $Arguments)
    {
        $value = if ($argument.StartsWith('--workflows=', [System.StringComparison]::OrdinalIgnoreCase) -or
            $argument.StartsWith('-W=', [System.StringComparison]::OrdinalIgnoreCase))
        {
            $argument.Substring($argument.IndexOf('=') + 1)
        }
        else
        {
            $argument
        }

        $normalized = $value.Replace('\', '/')
        while ( $normalized.StartsWith('./', [System.StringComparison]::Ordinal))
        {
            $normalized = $normalized.Substring(2)
        }

        if ($normalized -eq '.github/workflows/release-matrix-ui.yml' -or
            $normalized.EndsWith('/.github/workflows/release-matrix-ui.yml', [System.StringComparison]::OrdinalIgnoreCase))
        {
            return $true
        }
    }

    return $false
}

function Get-ReleaseMatrixUiProducts
{
    param([string[]] $Arguments)

    $productInput = @(Get-ActOptionValues -Arguments $Arguments -LongName '--input' |
        Where-Object { $_.StartsWith('ide-products=', [System.StringComparison]::OrdinalIgnoreCase) } |
        Select-Object -Last 1)

    $rawProducts = if ($productInput.Count -eq 0)
    {
        'IU,PY,WS'
    }
    else
    {
        $productInput[0].Substring('ide-products='.Length)
    }

    $aliases = @{
        IIU = 'IU'
        PCP = 'PY'
    }
    $supportedProducts = @('IU', 'PY', 'WS')
    $products = @()

    foreach ($rawProduct in $rawProducts.Split(','))
    {
        $product = $rawProduct.Trim().ToUpperInvariant()
        if ($product -eq '')
        {
            continue
        }

        if ( $aliases.ContainsKey($product))
        {
            $product = $aliases[$product]
        }

        if ($product -notin $supportedProducts)
        {
            throw "Unsupported IDE product '$rawProduct'. Supported: IU, PY, WS."
        }

        if ($product -notin $products)
        {
            $products += $product
        }
    }

    if ($products.Count -eq 0)
    {
        throw 'At least one IDE product must be selected.'
    }

    return $products
}

function Remove-ActInput
{
    param(
        [string[]] $Arguments,
        [string] $InputName
    )

    $filteredArguments = @()
    for ($index = 0; $index -lt $Arguments.Count; $index++)
    {
        $argument = $Arguments[$index]

        if ($argument -eq '--input' -and $index + 1 -lt $Arguments.Count)
        {
            $inputValue = $Arguments[$index + 1]
            if ( $inputValue.StartsWith("$InputName=", [System.StringComparison]::OrdinalIgnoreCase))
            {
                $index++
                continue
            }
        }

        if ( $argument.StartsWith('--input=', [System.StringComparison]::OrdinalIgnoreCase))
        {
            $inputValue = $argument.Substring($argument.IndexOf('=') + 1)
            if ( $inputValue.StartsWith("$InputName=", [System.StringComparison]::OrdinalIgnoreCase))
            {
                continue
            }
        }

        $filteredArguments += $argument
    }

    return $filteredArguments
}

function Test-ReleaseMatrixUiSequentialRun
{
    param([string[]] $Arguments)

    if (-not (Test-ReleaseMatrixUiWorkflow -Arguments $Arguments))
    {
        return $false
    }

    if (Test-ActFlagPresent -Arguments $Arguments -LongName '--matrix')
    {
        return $false
    }

    if ((Test-ActFlagPresent -Arguments $Arguments -LongName '--list' -ShortName '-l') -or
        (Test-ActFlagPresent -Arguments $Arguments -LongName '--validate') -or
        (Test-ActFlagPresent -Arguments $Arguments -LongName '--graph' -ShortName '-g') -or
        (Test-ActFlagPresent -Arguments $Arguments -LongName '--dryrun' -ShortName '-n') -or
        (Test-ActFlagPresent -Arguments $Arguments -LongName '--watch' -ShortName '-w'))
    {
        return $false
    }

    $jobs = @(Get-ActOptionValues -Arguments $Arguments -LongName '--job' -ShortName '-j')
    if ($jobs.Count -gt 0 -and 'release-matrix-ui' -notin $jobs)
    {
        return $false
    }

    return (@(Get-ReleaseMatrixUiProducts -Arguments $Arguments).Count -gt 1)
}

function Get-ReleaseMatrixUiLaneArguments
{
    param(
        [string[]] $Arguments,
        [string] $ProductCode
    )

    $laneArguments = @(Remove-ActInput -Arguments $Arguments -InputName 'ide-products')
    return $laneArguments + @('--input', "ide-products=$ProductCode", '--matrix', "code:$ProductCode")
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

if (-not (Test-ActFlagPresent -Arguments $ActArguments -LongName '--rm') -and
    -not (Test-ActFlagPresent -Arguments $ActArguments -LongName '--reuse' -ShortName '-r'))
{
    Write-Host 'Enabling act --rm so failed workflow containers are removed automatically.'
    $ActArguments = @('--rm') + $ActArguments
}

$runReleaseMatrixUiLanes = Test-ReleaseMatrixUiSequentialRun -Arguments $ActArguments
$releaseMatrixUiProducts = if ($runReleaseMatrixUiLanes)
{
    @(Get-ReleaseMatrixUiProducts -Arguments $ActArguments)
}
else
{
    @()
}

Push-Location -LiteralPath $repoRoot
try
{
    if ($runReleaseMatrixUiLanes)
    {
        $exitCode = 0
        foreach ($productCode in $releaseMatrixUiProducts)
        {
            Write-Host "Running release-matrix UI act lane for $productCode."
            $laneArguments = Get-ReleaseMatrixUiLaneArguments -Arguments $ActArguments -ProductCode $productCode
            & $actExecutable @laneArguments
            if ($LASTEXITCODE -ne 0 -and $exitCode -eq 0)
            {
                $exitCode = $LASTEXITCODE
            }
        }
    }
    else
    {
        & $actExecutable @ActArguments
        $exitCode = $LASTEXITCODE
    }
}
finally
{
    Pop-Location
}

exit $exitCode
