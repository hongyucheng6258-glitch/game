# ============================================================
# 锐竞电竞点单系统 - Windows 一键停止脚本
# ============================================================

Write-Host "停止服务..." -ForegroundColor Yellow

# 停止占用 8080 和 3000 端口的进程
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { 
    Write-Host "  停止后端进程 PID: $($_.OwningProcess)" 
    Stop-Process -Id $_.OwningProcess -Force 
}
Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | ForEach-Object { 
    Write-Host "  停止前端进程 PID: $($_.OwningProcess)" 
    Stop-Process -Id $_.OwningProcess -Force 
}

Write-Host "✅ 服务已停止" -ForegroundColor Green
