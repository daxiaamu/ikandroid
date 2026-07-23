$ErrorActionPreference = "Stop"
$version = "9.1.0"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$env:GRADLE_USER_HOME = Join-Path $root ".gradle-user"
$installRoot = Join-Path $env:USERPROFILE ".gradle\ikan-dists"
$gradleHome = Join-Path $installRoot "gradle-$version"
$gradleBat = Join-Path $gradleHome "bin\gradle.bat"

if (-not (Test-Path $gradleBat)) {
    New-Item -ItemType Directory -Force -Path $installRoot | Out-Null
    $zip = Join-Path $env:TEMP "gradle-$version-bin.zip"
    $unpack = Join-Path $env:TEMP "ikan-gradle-$version"
    Invoke-WebRequest -UseBasicParsing -Uri "https://services.gradle.org/distributions/gradle-$version-bin.zip" -OutFile $zip
    if (Test-Path $unpack) { Remove-Item -Recurse -Force -LiteralPath $unpack }
    Expand-Archive -Path $zip -DestinationPath $unpack -Force
    Move-Item -LiteralPath (Join-Path $unpack "gradle-$version") -Destination $gradleHome
    Remove-Item -Force -LiteralPath $zip
    Remove-Item -Recurse -Force -LiteralPath $unpack
}

& $gradleBat -p $root @args
exit $LASTEXITCODE
