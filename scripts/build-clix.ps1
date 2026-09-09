param(
    [string]$ProjectRoot = (Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent)
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectRoot

if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    throw "Python not found. Install Python 3.11+ first."
}

$toolsDir = Join-Path $ProjectRoot "tools"
New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null

Write-Host "==> Installing clix0 + pyinstaller"
python -m pip install --upgrade pip pyinstaller "clix0>=0.5.1"

$entryScript = Join-Path $env:TEMP "build-clix-entry.py"
@'
from clix.cli.app import app

if __name__ == "__main__":
    app()
'@ | Set-Content -Encoding UTF8 $entryScript

$buildDir = Join-Path $ProjectRoot "build\clix"
$distDir = Join-Path $ProjectRoot "build\clix-dist"
if (Test-Path $buildDir) { Remove-Item $buildDir -Recurse -Force }
if (Test-Path $distDir) { Remove-Item $distDir -Recurse -Force }

Write-Host "==> Building tools/clix.exe"
python -m PyInstaller `
    --onefile `
    --name clix `
    --distpath $distDir `
    --workpath $buildDir `
    --specpath $buildDir `
    --clean `
    --collect-all clix `
    --collect-all curl_cffi `
    --collect-all xclienttransaction `
    $entryScript

$builtExe = Join-Path $distDir "clix.exe"
if (-not (Test-Path $builtExe)) {
    throw "clix.exe build failed"
}

Copy-Item $builtExe (Join-Path $toolsDir "clix.exe") -Force
Write-Host "Created: $(Join-Path $toolsDir 'clix.exe')"
Write-Host "Done."
