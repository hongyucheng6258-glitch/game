# ============================================================
# 锐竞电竞点单系统 - Windows 一键启动脚本
# 使用方法: 在 PowerShell 中运行 .\start.ps1
# ============================================================

$ErrorActionPreference = "SilentlyContinue"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  锐竞电竞点单系统 - 一键启动 (Windows)"
Write-Host "==========================================" -ForegroundColor Cyan

# 检查环境
$javaOk = Get-Command java -ErrorAction SilentlyContinue
$nodeOk = Get-Command node -ErrorAction SilentlyContinue
$mvnOk = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $javaOk) { Write-Host "错误: Java 未安装，请先运行 .\setup.ps1" -ForegroundColor Red; exit 1 }
if (-not $nodeOk) { Write-Host "错误: Node.js 未安装，请先运行 .\setup.ps1" -ForegroundColor Red; exit 1 }
if (-not $mvnOk) { Write-Host "错误: Maven 未安装，请先运行 .\setup.ps1" -ForegroundColor Red; exit 1 }

# 杀掉旧进程
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }

$projectDir = $PSScriptRoot
if (-not $projectDir) { $projectDir = Get-Location }

# 启动后端
Write-Host ""
Write-Host ">>> 启动后端 (端口 8080)..." -ForegroundColor Green
$backendDir = Join-Path $projectDir "backend"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$backendDir`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Duser.dir=$backendDir`"" -WindowStyle Normal
Write-Host "  后端已在新窗口启动"

# 启动前端
Write-Host ">>> 启动前端 (端口 3000)..." -ForegroundColor Green
$frontendDir = Join-Path $projectDir "frontend"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$frontendDir`" && npx vite --host 0.0.0.0" -WindowStyle Normal
Write-Host "  前端已在新窗口启动"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  ✅ 启动完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  前端: http://localhost:3000" -ForegroundColor White
Write-Host "  后端: http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host "  等待约 30 秒后访问前端页面" -ForegroundColor Yellow
Write-Host "  关闭窗口即可停止服务" -ForegroundColor Yellow
