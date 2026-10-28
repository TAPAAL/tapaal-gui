$ErrorActionPreference = 'Stop'

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$JdkDir = if ($env:TAPAAL_JDK_DIR) { $env:TAPAAL_JDK_DIR } else { Join-Path $ProjectRoot '.tools\jdk-25' }

if (-not (Test-Path (Join-Path $JdkDir 'bin\java.exe'))) {
    $architecture = if ([Environment]::Is64BitOperatingSystem) { 'x64' } else { throw 'A 64-bit Windows installation is required for the Java bootstrap.' }
    $archiveDir = Join-Path ([System.IO.Path]::GetTempPath()) ([System.Guid]::NewGuid().ToString())
    $archive = Join-Path $archiveDir 'temurin-jdk.zip'
    $url = "https://api.adoptium.net/v3/binary/latest/25/ga/windows/$architecture/jdk/hotspot/normal/eclipse"

    New-Item -ItemType Directory -Force -Path $archiveDir | Out-Null
    try {
        Write-Host "Downloading Temurin Java 25 into $JdkDir..."
        Invoke-WebRequest -Uri $url -OutFile $archive
        Expand-Archive -Path $archive -DestinationPath $archiveDir
        $extractedDir = Get-ChildItem -Path $archiveDir -Directory -Filter 'jdk-*' | Select-Object -First 1
        if (-not $extractedDir) { throw 'The Java 25 archive did not contain a JDK directory.' }
        if (Test-Path $JdkDir) { Remove-Item -Recurse -Force $JdkDir }
        New-Item -ItemType Directory -Force -Path (Split-Path $JdkDir) | Out-Null
        Move-Item $extractedDir.FullName $JdkDir
    } finally {
        if (Test-Path $archiveDir) { Remove-Item -Recurse -Force $archiveDir }
    }
}

$env:JAVA_HOME = $JdkDir
& (Join-Path $ProjectRoot 'gradlew.bat') @args
exit $LASTEXITCODE
