# ============================================================
# 锐竞电竞点单系统 - Windows 一键配置脚本
# 使用方法: 在 PowerShell 中运行 .\setup.ps1
# ============================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  锐竞电竞点单系统 - 一键环境配置 (Windows)"
Write-Host "==========================================" -ForegroundColor Cyan

# ---------- 1. 检查 Java ----------
Write-Host ""
Write-Host ">>> [1/4] 检查 Java 17..." -ForegroundColor Green

$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "  未检测到 Java，请安装 JDK 17:" -ForegroundColor Yellow
    Write-Host "  下载地址: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Yellow
    Write-Host "  安装后重启 PowerShell，再重新运行此脚本" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  或使用 winget 快速安装:" -ForegroundColor Yellow
    Write-Host "  winget install EclipseAdoptium.Temurin.17.JDK" -ForegroundColor Yellow
    exit 1
}

$javaVer = & java -version 2>&1 | Select-Object -First 1
Write-Host "  $javaVer"

if ($javaVer -notmatch "17") {
    Write-Host "  警告: 需要 Java 17，当前版本可能不兼容" -ForegroundColor Yellow
}

# ---------- 2. 检查 Maven ----------
Write-Host ""
Write-Host ">>> [2/4] 检查 Maven..." -ForegroundColor Green

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnCmd) {
    Write-Host "  未检测到 Maven，正在安装..." -ForegroundColor Yellow
    
    # 下载 Maven
    $mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    $mavenZip = "$env:TEMP\maven.zip"
    $mavenDir = "$env:USERPROFILE\maven"
    
    Write-Host "  下载 Maven 3.9.6..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
    
    Write-Host "  解压..."
    if (Test-Path $mavenDir) { Remove-Item $mavenDir -Recurse -Force }
    Expand-Archive $mavenZip -DestinationPath $env:USERPROFILE -Force
    Rename-Item "$env:USERPROFILE\apache-maven-3.9.6" $mavenDir -ErrorAction SilentlyContinue
    Remove-Item $mavenZip -Force
    
    # 添加到 PATH（当前会话 + 用户永久）
    $env:PATH = "$mavenDir\bin;$env:PATH"
    [Environment]::SetEnvironmentVariable("PATH", "$mavenDir\bin;" + [Environment]::GetEnvironmentVariable("PATH", "User"), "User")
    
    # 配置阿里云镜像
    $mavenConfDir = "$mavenDir\conf"
    if (-not (Test-Path "$env:USERPROFILE\.m2\settings.xml")) {
        $settingsDir = "$env:USERPROFILE\.m2"
        if (-not (Test-Path $settingsDir)) { New-Item -ItemType Directory -Path $settingsDir -Force }
        @"
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
</settings>
"@ | Out-File -FilePath "$settingsDir\settings.xml" -Encoding UTF8
        Write-Host "  已配置阿里云 Maven 镜像"
    }
    
    Write-Host "  Maven 安装完成: $mavenDir" -ForegroundColor Green
} else {
    Write-Host "  Maven 已安装: $($mvnCmd.Source)"
}

# ---------- 3. 检查 Node.js ----------
Write-Host ""
Write-Host ">>> [3/4] 检查 Node.js..." -ForegroundColor Green

$nodeCmd = Get-Command node -ErrorAction SilentlyContinue
if (-not $nodeCmd) {
    Write-Host "  未检测到 Node.js，请安装:" -ForegroundColor Yellow
    Write-Host "  下载地址: https://nodejs.org/" -ForegroundColor Yellow
    Write-Host "  或使用 winget: winget install OpenJS.NodeJS.LTS" -ForegroundColor Yellow
    exit 1
}

$nodeVer = & node -v
$npmVer = & npm -v
Write-Host "  Node: $nodeVer"
Write-Host "  NPM:  $npmVer"

# ---------- 4. 安装前端依赖 ----------
Write-Host ""
Write-Host ">>> [4/4] 安装前端依赖..." -ForegroundColor Green

$frontendDir = Join-Path $PSScriptRoot "frontend"
if (-not (Test-Path "$frontendDir\node_modules")) {
    Push-Location $frontendDir
    & npm install
    Pop-Location
    Write-Host "  前端依赖安装完成" -ForegroundColor Green
} else {
    Write-Host "  node_modules 已存在，跳过" -ForegroundColor Green
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  ✅ 环境配置完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  启动命令: .\start.ps1" -ForegroundColor White
Write-Host "  停止命令: .\stop.ps1" -ForegroundColor White
Write-Host ""
