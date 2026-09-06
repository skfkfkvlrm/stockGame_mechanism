# StockGame Full Daemon Orchestrator
$baseDir = "d:\samuel\java\stockGame_mechanism"
$logsDir = Join-Path $baseDir "logs"

if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
}

Write-Host "=== [1/4] Checking MariaDB Docker Container ===" -ForegroundColor Cyan
$dockerStatus = docker inspect -f '{{.State.Running}}' mariadb-stockgame 2>$null
if ($dockerStatus -ne "true") {
    Write-Host "Starting mariadb-stockgame container..." -ForegroundColor Yellow
    docker start mariadb-stockgame | Out-Null
    Start-Sleep -Seconds 3
}
Write-Host "MariaDB (Port 3307) Ready!" -ForegroundColor Green

# Kill existing processes on target ports
$allPorts = @(8761, 8000, 8081, 8082, 8083, 8084, 8085, 8086, 5173, 5174)
foreach ($p in $allPorts) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($c in $conns) {
            Write-Host "Killing existing PID $($c.OwningProcess) on port $p..." -ForegroundColor Yellow
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    }
}

$runningProcs = [System.Collections.Generic.List[System.Diagnostics.Process]]::new()

function Launch-Jar {
    param(
        [string]$moduleName,
        [int]$port,
        [int]$waitSec = 6
    )
    $jarPath = Join-Path $baseDir "$moduleName\target\$moduleName-0.0.1-SNAPSHOT.jar"
    $stdout = Join-Path $logsDir "$moduleName.log"
    $stderr = Join-Path $logsDir "$moduleName-err.log"

    Write-Host "Starting $moduleName (Port: $port)..." -ForegroundColor Cyan
    $p = Start-Process -FilePath "java" `
        -ArgumentList "-Xms128m", "-Xmx256m", "-jar", $jarPath `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -WindowStyle Hidden `
        -PassThru

    $runningProcs.Add($p)

    $attempt = 0
    while ($attempt -lt ($waitSec * 2)) {
        Start-Sleep -Milliseconds 500
        $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conn) {
            Write-Host "  -> $moduleName is UP on port $port!" -ForegroundColor Green
            return
        }
        $attempt++
    }
    Write-Host "  -> $moduleName started (PID $($p.Id))" -ForegroundColor Yellow
}

Write-Host "`n=== [2/4] Starting Discovery & Gateway ===" -ForegroundColor Cyan
Launch-Jar "eureka-server" 8761 10
Launch-Jar "gateway-service" 8000 8

Write-Host "`n=== [3/4] Starting Domain Microservices ===" -ForegroundColor Cyan
Launch-Jar "member-service" 8081 6
Launch-Jar "stock-service" 8082 6
Launch-Jar "point-service" 8083 6
Launch-Jar "coupon-service" 8084 6
Launch-Jar "admin-service" 8085 6
Launch-Jar "ai-news-service" 8086 5

Write-Host "`n=== [4/4] Starting React Frontends ===" -ForegroundColor Cyan
$studentDir = "d:\samuel\java\stockGame_react"
$adminDir = "d:\samuel\java\stockGame_admin_react"

$sp = Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WorkingDirectory $studentDir -WindowStyle Hidden -PassThru
$runningProcs.Add($sp)
$ap = Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WorkingDirectory $adminDir -WindowStyle Hidden -PassThru
$runningProcs.Add($ap)

Write-Host "`n=======================================================" -ForegroundColor Green
Write-Host " StockGame Full-Stack Server Running in Daemon Mode!" -ForegroundColor Green
Write-Host " - Student Web:    http://localhost:5173" -ForegroundColor White
Write-Host " - Admin Web:      http://localhost:5174" -ForegroundColor White
Write-Host " - API Gateway:    http://localhost:8000" -ForegroundColor White
Write-Host " - Eureka Center:  http://localhost:8761" -ForegroundColor White
Write-Host "=======================================================" -ForegroundColor Green

# Keep script alive indefinitely to prevent process tree cleanup
while ($true) {
    Start-Sleep -Seconds 30
}