# StockGame MSA Stop Script
Write-Host "Stopping all StockGame MSA microservices..." -ForegroundColor Yellow
$ports = @(8761, 8000, 8081, 8082, 8083, 8084, 8085, 8086)
foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($c in $conns) {
            Write-Host "Killing PID $($c.OwningProcess) listening on port $p..." -ForegroundColor Yellow
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    }
}
Write-Host "All StockGame MSA microservices stopped." -ForegroundColor Green
