#Requires -Version 7.0
<#
.SYNOPSIS
    Carga en lote imagenes (y opcionalmente palabras) al banco de "cosas" de El Infiltrado via la API REST.

.DESCRIPTION
    1) Hace login como administrador y obtiene el JWT.
    2) Carga imagenes desde una carpeta (tipo IMAGEN, contenido en Base64).
       El nombre de la cosa = nombre del archivo sin extension (en minuscula).
    3) Opcionalmente carga palabras (desde archivo -PalabrasFile o la lista -PalabrasPorDefecto).
    Maneja duplicados (HTTP 409) como "omitido" y entrega un resumen al final.

.NOTES
    - Requiere PowerShell 7+.
    - El usuario debe ser administrador (es_admin = true); el script aborta si no lo es.
    - SEGURIDAD: la contrasena NUNCA se hardcodea. Se resuelve en este orden:
        1) Variable de entorno INFILTRADO_ADMIN_PWD (ejecucion no interactiva).
        2) Prompt seguro (Read-Host -AsSecureString).
    - Las imagenes se envian como Base64 puro (sin prefijo data:). El backend valida
      formato por magic bytes (PNG/JPG/WEBP) y tamano <= 200 KB del binario real.

.EXAMPLE
    # Cargar todas las imagenes de la carpeta por defecto (pedira la contrasena):
    ./cargar-cosas.ps1

.EXAMPLE
    # Ejecucion no interactiva (la contrasena viene de la variable de entorno):
    $env:INFILTRADO_ADMIN_PWD = '***'      # definela en tu sesion, no en el script
    ./cargar-cosas.ps1
    Remove-Item Env:\INFILTRADO_ADMIN_PWD  # limpiala al terminar

.EXAMPLE
    # Tambien cargar palabras desde un archivo:
    ./cargar-cosas.ps1 -PalabrasFile ./palabras.txt
#>

[CmdletBinding()]
param(
    [string] $BaseUrl     = "http://localhost:8093",
    [string] $Email       = "francisco.montoya.l@gmail.com",
    [string] $ImagenesDir  = "C:\feml\Descargas\images\infiltrado",
    [string] $PalabrasFile,                 # .txt con una palabra por linea (opcional)
    [switch] $PalabrasPorDefecto,           # usar la lista de palabras de ejemplo del script
    [int]    $MaxImagenKB  = 200            # limite del banco
)

$ErrorActionPreference = 'Stop'

# --- Lista de palabras de ejemplo (solo si se pasa -PalabrasPorDefecto) ---
$palabrasPorDefecto = @(
    'luna','sol','perro','gato','silla','mesa','arbol','rio','montana','playa',
    'reloj','libro','telefono','guitarra','bicicleta','manzana','cafe','llave',
    'puente','avion','barco','estrella','flor','zapato','sombrero','pelota',
    'camara','lampara','tijera','paraguas'
)

# --- 1) Login admin: devuelve el token JWT ---
function Get-AdminToken {
    param([string]$BaseUrl, [string]$Email, [securestring]$Password)

    # SecureString -> texto plano solo para el cuerpo de la peticion (nunca se loguea).
    $plain = [System.Net.NetworkCredential]::new('', $Password).Password
    $body  = @{ email = $Email; password = $plain } | ConvertTo-Json

    $status = 0
    $resp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post `
                -ContentType 'application/json' -Body $body `
                -SkipHttpErrorCheck -StatusCodeVariable status

    if ($status -ne 200) {
        throw "Login fallo (HTTP $status). Revisa email/contrasena o el rate limiting (5/min)."
    }
    if (-not $resp.esAdmin) {
        throw "El usuario '$Email' no es administrador. Marca es_admin = true en la BD y reintenta."
    }
    return $resp.token
}

# --- 2) Crear una cosa (palabra o imagen). Devuelve el codigo HTTP. ---
function Add-Cosa {
    param(
        [string]$BaseUrl, [string]$Token,
        [string]$Nombre, [string]$Tipo, [string]$ImagenBase64
    )

    $payload = @{ nombre = $Nombre; tipo = $Tipo }
    if ($ImagenBase64) { $payload.imagenBase64 = $ImagenBase64 }

    $status = 0
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/catalogo/cosas" -Method Post `
                -Headers @{ Authorization = "Bearer $Token" } `
                -ContentType 'application/json' -Body ($payload | ConvertTo-Json) `
                -SkipHttpErrorCheck -StatusCodeVariable status

    return $status   # 200/201 = creado | 409 = ya existe | otro = error
}

# === Resolver la contrasena (sin hardcodear) ===
if ($env:INFILTRADO_ADMIN_PWD) {
    $password = ConvertTo-SecureString $env:INFILTRADO_ADMIN_PWD -AsPlainText -Force
    Write-Host "Usando contrasena de la variable de entorno INFILTRADO_ADMIN_PWD." -ForegroundColor DarkGray
}
else {
    $password = Read-Host -AsSecureString -Prompt "Contrasena de $Email"
}

# === Login ===
try {
    $token = Get-AdminToken -BaseUrl $BaseUrl -Email $Email -Password $password
    Write-Host "[OK] Login correcto como admin ($Email)." -ForegroundColor Green
}
catch {
    Write-Error $_
    exit 1
}

$creadas = 0; $omitidas = 0; $fallidas = 0

# --- Imagenes (carga principal) ---
if (-not (Test-Path $ImagenesDir)) {
    Write-Error "No existe la carpeta de imagenes: $ImagenesDir"
    exit 1
}

$archivos = Get-ChildItem -Path $ImagenesDir -File -Recurse |
            Where-Object { $_.Extension -in '.png', '.jpg', '.jpeg', '.webp' }

Write-Host "`nCargando $($archivos.Count) imagenes desde $ImagenesDir..." -ForegroundColor Cyan
foreach ($f in $archivos) {
    # El nombre de la cosa = nombre del archivo sin extension (singular/minuscula).
    $nombre = [System.IO.Path]::GetFileNameWithoutExtension($f.Name).Trim().ToLower()
    $kb = [math]::Round($f.Length / 1KB, 1)

    # Validacion local de tamano antes de gastar la llamada
    if ($f.Length -gt ($MaxImagenKB * 1KB)) {
        Write-Host "  [x] '$nombre' pesa $kb KB (> $MaxImagenKB KB), omitida" -ForegroundColor Red
        $fallidas++
        continue
    }
    try {
        $bytes  = [System.IO.File]::ReadAllBytes($f.FullName)
        $base64 = [System.Convert]::ToBase64String($bytes)
        $st = Add-Cosa -BaseUrl $BaseUrl -Token $token -Nombre $nombre -Tipo 'IMAGEN' -ImagenBase64 $base64
        switch ($st) {
            { $_ -in 200, 201 } { Write-Host "  [+] '$nombre' ($kb KB)" -ForegroundColor Green; $creadas++ }
            409                 { Write-Host "  [~] '$nombre' ya existe, omitida" -ForegroundColor Yellow; $omitidas++ }
            default             { Write-Host "  [x] '$nombre' error HTTP $st" -ForegroundColor Red; $fallidas++ }
        }
    }
    catch {
        Write-Host "  [x] '$nombre' excepcion: $($_.Exception.Message)" -ForegroundColor Red
        $fallidas++
    }
}

# --- Palabras (opcional) ---
$palabras = @()
if ($PalabrasFile) {
    if (-not (Test-Path $PalabrasFile)) { Write-Error "No existe el archivo: $PalabrasFile"; exit 1 }
    $palabras = Get-Content -Path $PalabrasFile -Encoding utf8 | Where-Object { $_.Trim() -ne '' }
}
elseif ($PalabrasPorDefecto) {
    $palabras = $palabrasPorDefecto
}

if ($palabras.Count -gt 0) {
    Write-Host "`nCargando $($palabras.Count) palabras..." -ForegroundColor Cyan
    foreach ($p in $palabras) {
        $nombre = $p.Trim().ToLower()
        try {
            $st = Add-Cosa -BaseUrl $BaseUrl -Token $token -Nombre $nombre -Tipo 'PALABRA'
            switch ($st) {
                { $_ -in 200, 201 } { Write-Host "  [+] palabra '$nombre'" -ForegroundColor Green; $creadas++ }
                409                 { Write-Host "  [~] '$nombre' ya existe, omitida" -ForegroundColor Yellow; $omitidas++ }
                default             { Write-Host "  [x] '$nombre' error HTTP $st" -ForegroundColor Red; $fallidas++ }
            }
        }
        catch {
            Write-Host "  [x] '$nombre' excepcion: $($_.Exception.Message)" -ForegroundColor Red
            $fallidas++
        }
    }
}

# --- Resumen ---
Write-Host "`nResumen: $creadas creadas | $omitidas omitidas (ya existian) | $fallidas fallidas." -ForegroundColor Cyan
