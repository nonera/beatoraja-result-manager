param(
    [string]$ProjectRoot = (Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent)
)

$ErrorActionPreference = "Stop"
Set-Location $ProjectRoot

Write-Host "==> Building Java application"
& .\gradlew.bat clean shadowJar
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$shadowJar = Get-ChildItem "build\libs\beatoraja-screenshot-manager-*.jar" | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
if (-not $shadowJar) {
    throw "shadowJar not found"
}

$distDir = Join-Path $ProjectRoot "dist\beatoraja-screenshot-manager"
$toolsDir = Join-Path $distDir "tools"
$inputDir = Join-Path $ProjectRoot "build\release-input"

if (Test-Path $distDir) {
    Remove-Item $distDir -Recurse -Force
}
New-Item -ItemType Directory -Path $inputDir -Force | Out-Null

Copy-Item $shadowJar.FullName (Join-Path $inputDir $shadowJar.Name)

$appVersion = (& .\gradlew.bat -q properties | Where-Object { $_ -match '^version:' } | ForEach-Object { ($_ -split ':', 2)[1].Trim() })
if (-not $appVersion) {
    throw "Could not read project version from Gradle"
}
Write-Host "Application version: $appVersion"

$iconPath = Join-Path $ProjectRoot "packaging\app-icon.ico"
if (-not (Test-Path $iconPath)) {
    throw "Application icon not found: $iconPath"
}

Write-Host "==> Creating app-image with jpackage"
# jpackage refuses to write into a destination that already exists, so it must
# own creation of $distDir itself - do not pre-create it (or any subfolder).
jpackage `
    --type app-image `
    --name beatoraja-screenshot-manager `
    --app-version $appVersion `
    --input $inputDir `
    --main-jar $shadowJar.Name `
    --main-class com.beatoraja.screenshot.Main `
    --dest (Join-Path $ProjectRoot "dist") `
    --java-options "-Dapp.dir=`$BINDIR" `
    --java-options "-Dfile.encoding=UTF-8" `
    --icon $iconPath `
    --add-modules java.base,java.desktop,java.net.http,java.sql,jdk.localedata,jdk.crypto.ec
if ($LASTEXITCODE -ne 0) {
    throw "jpackage failed with exit code $LASTEXITCODE"
}

$appImageDir = Join-Path $ProjectRoot "dist\beatoraja-screenshot-manager"
if (-not (Test-Path $appImageDir)) {
    throw "jpackage output not found: $appImageDir"
}
New-Item -ItemType Directory -Path $toolsDir -Force | Out-Null

Write-Host "==> Building bundled clix.exe (optional, requires Python 3.11+ + clix0)"
$buildClixScript = Join-Path $ProjectRoot "scripts\build-clix.ps1"
$clixExe = Join-Path $appImageDir "tools\clix.exe"
if (Get-Command python -ErrorAction SilentlyContinue) {
    & $buildClixScript -ProjectRoot $ProjectRoot
    Copy-Item (Join-Path $ProjectRoot "tools\clix.exe") $clixExe -Force
    Write-Host "clix.exe bundled"
} else {
    Write-Warning "Python not found. Skipping clix.exe bundling."
    Write-Warning "Run scripts\\build-clix.ps1 later, or place clix.exe into dist/beatoraja-screenshot-manager/tools/"
}

$zipPath = Join-Path $ProjectRoot "dist\beatoraja-screenshot-manager.zip"
if (Test-Path $zipPath) {
    Remove-Item $zipPath -Force
}
Compress-Archive -Path $appImageDir -DestinationPath $zipPath

Write-Host "Release created:"
Write-Host "  $appImageDir"
Write-Host "  $zipPath"
