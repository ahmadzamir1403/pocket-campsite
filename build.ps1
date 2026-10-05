param(
    [string]$PrismRoot = (Join-Path $env:APPDATA 'PrismLauncher'),
    [string]$FabricApiJar
)
$ErrorActionPreference='Stop'
$projectRoot=$PSScriptRoot
$depsFolder=Join-Path $projectRoot 'build/deps'
$classFolder=Join-Path $projectRoot 'build/classes'
$distFolder=Join-Path $projectRoot 'dist'
New-Item -ItemType Directory -Path $depsFolder,$classFolder,$distFolder -Force | Out-Null
$gameJar=Join-Path $PrismRoot 'libraries/com/mojang/minecraft/26.3/minecraft-26.3-client.jar'
$compileGameJar=Join-Path $depsFolder 'minecraft-26.3-compile.jar'
if (-not (Test-Path -LiteralPath $gameJar)) { throw "Minecraft 26.3 client JAR not found: $gameJar. Install Minecraft 26.3 in PrismLauncher or pass -PrismRoot." }
& java (Join-Path $projectRoot 'tools/WidenCompileApi.java') $gameJar $compileGameJar
if ($LASTEXITCODE-ne 0) { throw 'Compile API preparation failed' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
if (-not $FabricApiJar) {
    $fabricCandidates = @(Get-ChildItem -LiteralPath (Join-Path $PrismRoot 'instances') -Recurse -File -Filter 'fabric-api-0.161.0+26.3.jar')
    if ($fabricCandidates.Count -eq 0) { throw 'Fabric API 0.161.0+26.3 was not found in PrismLauncher instances. Install it or pass -FabricApiJar.' }
    $FabricApiJar = $fabricCandidates[0].FullName
}
$fabricDepsFolder = Join-Path $projectRoot 'build/fabric-deps'
New-Item -ItemType Directory -Path $fabricDepsFolder -Force | Out-Null
$fabricZip=[IO.Compression.ZipFile]::OpenRead($FabricApiJar)
try {
 $fabricDependencyJars = @()
 foreach ($entry in $fabricZip.Entries) {
  if ($entry.FullName-like 'META-INF/jars/*.jar') {
   $destination = Join-Path $fabricDepsFolder $entry.Name
   [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$destination,$true)
   $fabricDependencyJars += $destination
  }
 }
} finally {$fabricZip.Dispose()}
$dependencyJars=@($compileGameJar)+$fabricDependencyJars+@((Get-ChildItem -LiteralPath (Join-Path $PrismRoot 'libraries') -Recurse -Filter '*.jar'|Where-Object {$_.Name -notlike 'minecraft-*-client.jar'}).FullName)
$compileClassPath=$dependencyJars-join ';'
[IO.File]::WriteAllText((Join-Path $projectRoot 'build/classpath.txt'), $compileClassPath)
# Remove stale classes only within this project's known build/classes directory.
$resolvedClasses = [IO.Path]::GetFullPath($classFolder)
$resolvedBuild = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build')) + [IO.Path]::DirectorySeparatorChar
if (-not $resolvedClasses.StartsWith($resolvedBuild, [StringComparison]::OrdinalIgnoreCase)) { throw 'Invalid classes directory' }
Get-ChildItem -LiteralPath $classFolder -Recurse -File -Filter '*.class' | Remove-Item -Force
$sources=@((Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/main/java') -Recurse -Filter '*.java').FullName)
& javac -proc:none --release 25 -encoding UTF-8 -cp $compileClassPath -d $classFolder @sources
if($LASTEXITCODE-ne 0){throw 'Compilation failed'}
foreach($expected in @('com/pocketcampsite/PocketCampsite.class','com/pocketcampsite/PocketCampsiteEntity.class','com/pocketcampsite/PocketCampsiteMenu.class','com/pocketcampsite/CampBedBlock.class','com/pocketcampsite/client/PocketCampsiteClient.class','com/pocketcampsite/client/PocketCampsiteScreen.class')){
 if(-not(Test-Path -LiteralPath (Join-Path $classFolder $expected))){throw "Missing class: $expected"}
}
$resourceFolder=Join-Path $projectRoot 'src/main/resources'
# JSON is parsed by the Java verifier, which supports valid empty blockstate variant keys.
$artifact=Join-Path $distFolder 'pocketcampsite-1.0.0.jar'
& jar --create --file $artifact -C $classFolder . -C $resourceFolder .
if($LASTEXITCODE-ne 0){throw 'Packaging failed'}
Write-Output "Built $artifact"
& java -cp ($artifact + ';' + $compileClassPath) (Join-Path $projectRoot 'tools/VerifyMod.java') $artifact
if($LASTEXITCODE-ne 0){throw 'Package validation failed'}
