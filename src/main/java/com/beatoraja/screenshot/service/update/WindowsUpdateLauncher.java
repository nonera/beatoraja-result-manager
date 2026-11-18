package com.beatoraja.screenshot.service.update;

import com.beatoraja.screenshot.util.OsInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WindowsUpdateLauncher {

    /**
     * Waits for the app to exit, then copies the new version over the install folder and starts
     * it again. Only the folders the release owns (app, runtime) are mirrored - so files dropped
     * from a release disappear there - while the rest is copied without deleting anything, so
     * files the user put in the install folder survive. The downloaded files and the script
     * itself are removed at the end, whether or not the copy worked.
     */
    static final String SCRIPT = """
            param(
              [Parameter(Mandatory=$true)][int]$ParentPid,
              [Parameter(Mandatory=$true)][string]$SourceDir,
              [Parameter(Mandatory=$true)][string]$TargetDir,
              [Parameter(Mandatory=$true)][string]$ExePath,
              [Parameter(Mandatory=$true)][string]$WorkDir
            )
            $code = 0
            try {
              while (Get-Process -Id $ParentPid -ErrorAction SilentlyContinue) {
                Start-Sleep -Milliseconds 500
              }
              Start-Sleep -Seconds 1
              $owned = @('app', 'runtime')
              $excluded = $owned | ForEach-Object { Join-Path $SourceDir $_ }
              & robocopy $SourceDir $TargetDir /E /XD $excluded /R:2 /W:2 /NFL /NDL /NJH /NJS /NC /NS /NP
              $code = $LASTEXITCODE
              if ($code -lt 8) {
                foreach ($dir in $owned) {
                  $from = Join-Path $SourceDir $dir
                  if (Test-Path -LiteralPath $from) {
                    & robocopy $from (Join-Path $TargetDir $dir) /MIR /R:2 /W:2 /NFL /NDL /NJH /NJS /NC /NS /NP
                    if ($LASTEXITCODE -ge 8) { $code = $LASTEXITCODE; break }
                  }
                }
              }
              if ($code -lt 8) {
                Start-Process -FilePath $ExePath
              }
            } finally {
              Remove-Item -LiteralPath $WorkDir -Recurse -Force -ErrorAction SilentlyContinue
              Remove-Item -LiteralPath $PSCommandPath -Force -ErrorAction SilentlyContinue
            }
            if ($code -ge 8) { exit $code }
            exit 0
            """;

    private WindowsUpdateLauncher() {
    }

    /**
     * Starts the update script in its own PowerShell process. {@code workDir} holds the download
     * and {@code sourceDir}; the script deletes it when done.
     */
    public static void launch(Path sourceDir, Path targetDir, Path executablePath, long parentPid, Path workDir)
            throws IOException {
        if (!OsInfo.isWindows()) {
            throw new IOException("Automatic update is supported on Windows only.");
        }

        Path scriptPath = Files.createTempFile("beatoraja-update-", ".ps1");
        Files.writeString(scriptPath, SCRIPT, StandardCharsets.UTF_8);

        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                scriptPath.toAbsolutePath().toString(),
                "-ParentPid",
                String.valueOf(parentPid),
                "-SourceDir",
                sourceDir.toAbsolutePath().toString(),
                "-TargetDir",
                targetDir.toAbsolutePath().toString(),
                "-ExePath",
                executablePath.toAbsolutePath().toString(),
                "-WorkDir",
                workDir.toAbsolutePath().toString()
        );
        builder.start();
    }
}
