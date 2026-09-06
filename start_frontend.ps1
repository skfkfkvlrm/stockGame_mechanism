# StockGame Frontend Launcher Script
Write-Host "=== Starting StockGame Frontends (React Vite) ===" -ForegroundColor Cyan

# Stop existing Vite processes on 5173, 5174
$ports = @(5173, 5174)
foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    if ($conns) {
        foreach ($c in $conns) {
            Write-Host "Stopping existing Vite on port $p (PID $($c.OwningProcess))..." -ForegroundColor Yellow
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
        }
    }
}

$studentDir = "d:\samuel\java\stockGame_react"
$adminDir = "d:\samuel\java\stockGame_admin_react"

Write-Host "Launching Student UI (http://localhost:5173)..." -ForegroundColor Cyan
Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WorkingDirectory $studentDir -WindowStyle Hidden

Write-Host "Launching Admin UI (http://localhost:5174)..." -ForegroundColor Cyan
Start-Process -FilePath "cmd.exe" -ArgumentList "/c npm run dev" -WorkingDirectory $adminDir -WindowStyle Hidden

Start-Sleep -Seconds 3
Write-Host "`nFrontends launched!" -ForegroundColor Green
Write-Host " Student UI: http://localhost:5173" -ForegroundColor Yellow
Write-Host " Admin UI: http://localhost:5174" -ForegroundColor Yellow