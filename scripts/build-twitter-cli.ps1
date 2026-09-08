param(
    [string]$ProjectRoot = (Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent)
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectRoot

if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    throw "Python not found. Install Python 3.10+ first."
}

$toolsDir = Join-Path $ProjectRoot "tools"
New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null

Write-Host "==> Installing twitter-cli + pyinstaller"
python -m pip install --upgrade pip pyinstaller "twitter-cli>=0.8.5"

$entryScript = Join-Path $env:TEMP "build-twitter-cli-entry.py"
@'
from twitter_cli.cli import cli

if __name__ == "__main__":
    cli()
'@ | Set-Content -Encoding UTF8 $entryScript

$buildDir = Join-Path $ProjectRoot "build\twitter-cli"
$distDir = Join-Path $ProjectRoot "build\twitter-cli-dist"
if (Test-Path $buildDir) { Remove-Item $buildDir -Recurse -Force }
if (Test-Path $distDir) { Remove-Item $distDir -Recurse -Force }

Write-Host "==> Building tools/twitter.exe"
python -m PyInstaller `
    --onefile `
    --name twitter `
    --distpath $distDir `
    --workpath $buildDir `
    --specpath $buildDir `
    --clean `
    --collect-all twitter_cli `
    $entryScript

$builtExe = Join-Path $distDir "twitter.exe"
if (-not (Test-Path $builtExe)) {
    throw "twitter.exe build failed"
}

Copy-Item $builtExe (Join-Path $toolsDir "twitter.exe") -Force
Write-Host "Created: $(Join-Path $toolsDir 'twitter.exe')"
Write-Host "Done."
