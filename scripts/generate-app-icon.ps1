param(
    [string]$ProjectRoot = (Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent)
)

$ErrorActionPreference = "Stop"

$png = Join-Path $ProjectRoot "src\main\resources\icons\app-icon.png"
$ico = Join-Path $ProjectRoot "packaging\app-icon.ico"

if (-not (Test-Path $png)) {
    throw "PNG source not found: $png"
}

python -c @"
from PIL import Image
from pathlib import Path
png = Path(r'$png')
ico = Path(r'$ico')
ico.parent.mkdir(parents=True, exist_ok=True)
img = Image.open(png).convert('RGBA')
sizes = [256, 128, 64, 48, 32, 16]
icons = [img.resize((s, s), Image.Resampling.LANCZOS) for s in sizes]
icons[0].save(ico, format='ICO', sizes=[(i.width, i.height) for i in icons])
print(f'Generated {ico}')
"@
