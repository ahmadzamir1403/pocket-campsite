param(
    [string]$PrismRoot = (Join-Path $env:APPDATA 'PrismLauncher'),
    [string]$FabricApiJar
)
$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'build.ps1') -PrismRoot $PrismRoot -FabricApiJar $FabricApiJar
$classPath = [IO.File]::ReadAllText((Join-Path $PSScriptRoot 'build/classpath.txt'))
$compileGameJar = Join-Path $PSScriptRoot 'build/deps/minecraft-26.3-compile.jar'
$gameJar = Join-Path $PrismRoot 'libraries/com/mojang/minecraft/26.3/minecraft-26.3-client.jar'
# Run API checks with the unmodified signed game JAR, never the compile-only copy.
$runtimeDependencies = @($gameJar) + @(($classPath -split ';') | Where-Object { $_ -ne $compileGameJar })
$runtimeClassPath = $runtimeDependencies -join ';'
$checkFolder = Join-Path $PSScriptRoot 'build/checks'
New-Item -ItemType Directory -Path $checkFolder -Force | Out-Null
$checks = @((Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'checks') -Filter '*.java').FullName)
& javac -proc:none --release 25 -encoding UTF-8 -cp $classPath -d $checkFolder @checks
if ($LASTEXITCODE -ne 0) { throw 'Check compilation failed' }
foreach ($check in @('CampsiteContentsCheck', 'CampsiteDeliveryCheck')) {
    & java -Xverify:all -cp ($checkFolder + ';' + $runtimeClassPath) $check
    if ($LASTEXITCODE -ne 0) { throw "Failed check: $check" }
}
