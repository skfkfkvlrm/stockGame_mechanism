# StockGame Total Full-Stack Launcher
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   StockGame Full-Stack Launcher Started  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

& "d:\samuel\java\stockGame_mechanism\start_msa.ps1"
& "d:\samuel\java\stockGame_mechanism\start_frontend.ps1"

Write-Host "
All Systems Operational!" -ForegroundColor Green
Write-Host " Student Web: http://localhost:5173" -ForegroundColor White
Write-Host " Admin Web:   http://localhost:5174" -ForegroundColor White
Write-Host " API Gateway: http://localhost:8000" -ForegroundColor White
Write-Host " Eureka:      http://localhost:8761" -ForegroundColor White
