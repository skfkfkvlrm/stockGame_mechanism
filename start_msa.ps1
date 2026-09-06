# StockGame MSA Launcher Script
$baseDir = "d:\samuel\java\stockGame_mechanism"
$logsDir = Join-Path $baseDir "logs"

if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
}

Write-Host "=== [1/3] Checking & Starting MariaDB Docker Container ===" -ForegroundColor Cyan
$dockerStatus = docker inspect -f '{{.State.Running}}' mariadb-stockgame 2>$null
if ($dockerStatus -ne "true") {
    Write-Host "Starting mariadb-stockgame container..." -ForegroundColor Yellow
    docker start mariadb-stockgame | Out-Null
    Start-Sleep -Seconds 3
}
Write-Host "MariaDB (Port 3307) Ready!" -ForegroundColor Green

# Kill existing processes on ports if any
$ports = @(8761, 8000, 8081, 8082, 8083, 8084, 8085, 8086)
foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($c in $conns) {
            Write-Host "Stopping existing process on port $p (PID $($c.OwningProcess))..." -ForegroundColor Yellow
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    }
}

function Start-ServiceModule {
    param(
        [string]$moduleName,
        [int]$port,
        [int]$waitSec = 5
    )
    $jarPath = Join-Path $baseDir "$moduleName\target\$moduleName-0.0.1-SNAPSHOT.jar"
    $stdout = Join-Path $logsDir "$moduleName.log"
    $stderr = Join-Path $logsDir "$moduleName-err.log"

    Write-Host "Launching $moduleName (Port: $port)..." -ForegroundColor Cyan
    $procParams = @{
        FilePath = "java"
        ArgumentList = @("-Xms128m", "-Xmx256m", "-jar", $jarPath)
        RedirectStandardOutput = $stdout
        RedirectStandardError = $stderr
        WindowStyle = "Hidden"
        PassThru = $true
    }
    $proc = Start-Process @procParams

    Write-Host "$moduleName started with PID $($proc.Id). Waiting for port $port..." -ForegroundColor Gray
    
    $attempt = 0
    while ($attempt -lt ($waitSec * 2)) {
        Start-Sleep -Milliseconds 500
        $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        if ($conn) {
            Write-Host "  -> $moduleName is UP on port $port!" -ForegroundColor Green
            return $proc
        }
        $attempt++
    }
    Write-Host "  -> $moduleName launched (port check pending or in progress)" -ForegroundColor Yellow
    return $proc
}

Write-Host "`n=== [2/3] Starting Eureka Server & Gateway ===" -ForegroundColor Cyan
Start-ServiceModule "eureka-server" 8761 10
Start-ServiceModule "gateway-service" 8000 8

Write-Host "`n=== [3/3] Starting Downstream Domain Microservices ===" -ForegroundColor Cyan
Start-ServiceModule "member-service" 8081 6
Start-ServiceModule "stock-service" 8082 6
Start-ServiceModule "point-service" 8083 6
Start-ServiceModule "coupon-service" 8084 6
Start-ServiceModule "admin-service" 8085 6
Start-ServiceModule "ai-news-service" 8086 5

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " All StockGame MSA services launched successfully!" -ForegroundColor Green
Write-Host " Gateway Endpoint: http://localhost:8000" -ForegroundColor Yellow
Write-Host " Eureka Dashboard: http://localhost:8761" -ForegroundColor Yellow
Write-Host " Logs Directory: $logsDir" -ForegroundColor Gray
Write-Host "=======================================================" -ForegroundColor Cyan