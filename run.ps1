<#
    Compila y ejecuta el buscaminas.

        .\run.ps1            -> solo compila
        .\run.ps1 server     -> compila y arranca el servidor
        .\run.ps1 client     -> compila y arranca un cliente

    Abre el servidor en una terminal y un cliente en otra
    (puedes abrir varios clientes, juegan sobre el mismo tablero).
#>
param(
    [ValidateSet("build", "server", "client")]
    [string] $Target = "build"
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$out  = Join-Path $root "out"

# --- Localizar un JDK (necesita javac, no vale un JRE) ---------------
$javac = $null

if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javac = "$env:JAVA_HOME\bin\javac.exe"
}

if (-not $javac) {
    $cmd = Get-Command javac -ErrorAction SilentlyContinue
    if ($cmd) { $javac = $cmd.Source }
}

if (-not $javac) {
    # JDK embebido en la extension de Java de VS Code
    $embedded = Get-ChildItem `
        "$env:USERPROFILE\.vscode\extensions\redhat.java-*\jre\*\bin\javac.exe" `
        -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($embedded) { $javac = $embedded.FullName }
}

if (-not $javac) {
    Write-Error "No se encontro ningun JDK. Instala un JDK 21 o superior."
    exit 1
}

$java = Join-Path (Split-Path -Parent $javac) "java.exe"

# --- Compilar --------------------------------------------------------
Write-Host "Compilando con $javac" -ForegroundColor DarkGray

if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null

$sources = Get-ChildItem -Path (Join-Path $root "src") -Filter *.java -Recurse |
           Select-Object -ExpandProperty FullName

& $javac -d $out $sources
if ($LASTEXITCODE -ne 0) {
    Write-Error "Fallo la compilacion."
    exit 1
}

Write-Host "Compilado correctamente." -ForegroundColor Green

# --- Ejecutar --------------------------------------------------------
switch ($Target) {
    "server" { & $java -cp $out minesweeper.server.Server }
    "client" { & $java -cp $out minesweeper.client.Client }
}
