package com.beatoraja.screenshot.service.update;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WindowsUpdateLauncher {

    private WindowsUpdateLauncher() {
    }

    public static void launch(Path sourceDir, Path targetDir, Path executablePath, long parentPid) throws IOException {
        if (!isWindows()) {
            throw new IOException("Automatic update is supported on Windows only.");
        }

        Path scriptPath = Files.createTempFile("beatoraja-update-", ".ps1");
        String script = """
                param(
                  [Parameter(Mandatory=$true)][int]$ParentPid,
                  [Parameter(Mandatory=$true)][string]$SourceDir,
                  [Parameter(Mandatory=$true)][string]$TargetDir,
                  [Parameter(Mandatory=$true)][string]$ExePath
                )
                while (Get-Process -Id $ParentPid -ErrorAction SilentlyContinue) {
                  Start-Sleep -Milliseconds 500
                }
                Start-Sleep -Seconds 1
                & robocopy $SourceDir $TargetDir /MIR /R:2 /W:2 /NFL /NDL /NJH /NJS /NC /NS /NP
                $code = $LASTEXITCODE
                if ($code -ge 8) { exit $code }
                Start-Process -FilePath $ExePath
                exit 0
                """;
        Files.writeString(scriptPath, script, StandardCharsets.UTF_8);

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
                executablePath.toAbsolutePath().toString()
        );
        builder.start();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
