param(
    [Parameter(Mandatory = $true)]
    [string] $Tag,

    [switch] $AllowDirty,

    [switch] $AllowExistingTag,

    [switch] $SkipMainBranchCheck
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$betaTagPattern = '^v[0-9]+\.[0-9]+\.[0-9]+-beta\.[0-9]+$'
if ($Tag -notmatch $betaTagPattern)
{
    throw 'Remote-first beta validation requires a tag like vMAJOR.MINOR.PATCH-beta.N.'
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$safeTag = $Tag -replace '[^A-Za-z0-9._-]', '_'
$releaseNotesOutputPath = Join-Path $repoRoot "build/github-release-notes-$safeTag.md"

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

function Assert-MainBranch
{
    if ($SkipMainBranchCheck)
    {
        return
    }

    $branch = (Get-RepositoryCommandOutput `
            -Title 'git branch --show-current' `
            -Executable 'git' `
            -Arguments @('branch', '--show-current') |
        Select-Object -First 1).Trim()

    if ($branch -ne 'main')
    {
        throw "Remote-first beta validation must run from main; current branch is '$branch'."
    }
}

function Assert-CleanWorktree
{
    if ($AllowDirty)
    {
        return
    }

    $status = @(Get-RepositoryCommandOutput `
            -Title 'git status --porcelain' `
            -Executable 'git' `
            -Arguments @('status', '--porcelain'))

    if ($status.Count -gt 0)
    {
        throw "Remote-first beta validation requires a clean worktree. Commit or stash changes first.`n$( $status -join "`n" )"
    }
}

function Assert-TagAvailable
{
    if ($AllowExistingTag)
    {
        return
    }

    & git show-ref --verify --quiet "refs/tags/$Tag"
    if ($LASTEXITCODE -eq 0)
    {
        throw "Tag $Tag already exists. Remote-first beta validation is intended to run before tagging."
    }

    if ($LASTEXITCODE -ne 1)
    {
        throw "Unable to check whether tag $Tag exists; git show-ref exited with $LASTEXITCODE."
    }
}

Push-Location -LiteralPath $repoRoot
try
{
    Assert-MainBranch
    Assert-CleanWorktree
    Assert-TagAvailable

    Invoke-RepositoryCommand `
        -Title 'Check Marketplace change notes' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/generate-intellij-platform-change-notes.ps1', '-Check')

    Invoke-RepositoryCommand `
        -Title 'Check Marketplace description' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/generate-intellij-platform-description.ps1', '-Check')

    Invoke-RepositoryCommand `
        -Title 'Generate GitHub release notes' `
        -Executable 'pwsh' `
        -Arguments @(
        '-NoProfile',
        '-ExecutionPolicy',
        'Bypass',
        '-File',
        'scripts/generate-github-release-notes.ps1',
        '-Tag',
        $Tag,
        '-OutputPath',
        $releaseNotesOutputPath
    )

    Invoke-RepositoryCommand `
        -Title 'Validate documentation' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/validate-docs.ps1')

    Invoke-RepositoryCommand `
        -Title 'Validate agent artifacts' `
        -Executable 'pwsh' `
        -Arguments @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', 'scripts/ai/validate-agent-artifacts.ps1')

    Invoke-RepositoryCommand `
        -Title 'Check working tree whitespace' `
        -Executable 'git' `
        -Arguments @('diff', '--check')

    Invoke-RepositoryCommand `
        -Title 'Check HEAD whitespace' `
        -Executable 'git' `
        -Arguments @('diff-tree', '--check', '--no-commit-id', '--root', '-r', 'HEAD')

    Write-Host ''
    Write-Host "Remote-first beta local validation completed for $Tag."
    Write-Host 'Skipped local Gradle, test, coverage, Plugin Verifier, and release-matrix UI gates by design.'
    Write-Host "After pushing main and $Tag, run: scripts/watch-github-release-validation.ps1 -Tag $Tag"
}
finally
{
    Pop-Location
}
