Param(
    [int]$DebounceSeconds = 2
)

$ErrorActionPreference = 'SilentlyContinue'

# Ensure we run at repo root
if ($PSScriptRoot) {
    $repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
    Set-Location $repoRoot
}

function Get-ChangedFiles {
    $status = git status --porcelain
    if (-not $status) { return @() }
    $lines = $status -split "`n"
    return $lines | Where-Object { $_.Length -ge 4 } | ForEach-Object { $_.Substring(3).Trim() }
}

function Commit-Changes {
    $files = Get-ChangedFiles
    if (-not $files -or $files.Count -eq 0) { return }

    git add -A | Out-Null

    $unique = $files | Select-Object -Unique
    $head = ($unique | Select-Object -First 5) -join ', '
    if ($unique.Count -gt 5) { $head = "$head, ..." }
    $msg = "chore: auto-commit - $head"

    git commit --no-gpg-sign -m "$msg" | Out-Null
}

# Setup file watcher
$fsw = New-Object System.IO.FileSystemWatcher -Property @{ Path = (Get-Location).Path; IncludeSubdirectories = $true; Filter = '*' }
$fsw.EnableRaisingEvents = $true

$script:pending = $false

$action = {
    param($Source, $EventArgs)
    $path = $EventArgs.FullPath
    if (-not $path) { return }
    if ($path -match "\\\.git\\" -or $path -match "\\node_modules\\" -or $path -match "\\\.cursor\\") { return }
    $script:pending = $true
}

Register-ObjectEvent $fsw Changed -Action $action | Out-Null
Register-ObjectEvent $fsw Created -Action $action | Out-Null
Register-ObjectEvent $fsw Deleted -Action $action | Out-Null
Register-ObjectEvent $fsw Renamed -Action $action | Out-Null

Write-Host "[auto-commit] watching $(Get-Location).Path ..."

while ($true) {
    if ($script:pending) {
        Start-Sleep -Seconds $DebounceSeconds
        $script:pending = $false
        # Ensure there are changes before committing
        if (git status --porcelain) {
            Commit-Changes
        }
    }
    Start-Sleep -Milliseconds 500
}


