#Requires -Version 5.1
<#
.SYNOPSIS
    BlueNet 本地开发环境构建 / 启动脚本（Windows）

.DESCRIPTION
    通过 -Action 与 -Service 精确控制行为：

    构建（-Action build，按 .github/workflows/ci.yml 流水线执行）：
      api / judge  → mvnw test（测试）→ mvnw clean package -DskipTests（打包）→ docker compose build
      ai           → uv sync --frozen --no-dev → uv run ruff check . → docker compose build
      frontend     → pnpm install --frozen-lockfile → tsc --noEmit → pnpm lint → docker compose build
      infra        → 无镜像可构建，仅确保基础设施运行（compose --profile infra up -d）

    启动（-Action up）：
      先确保基础设施（pgsql / redis / rabbitmq / oss）运行，
      再按 -Service 启动指定服务的容器，最后等待 api 健康检查通过。

.PARAMETER Action
    build：构建指定服务的镜像（含 CI 同款的前置步骤）
    up   ：启动指定服务（先拉起基础设施）

.PARAMETER Service
    服务名，多个用逗号分隔：api、judge、ai、frontend、infra。
    默认 all（build = 全部 4 个镜像；up = 除 infra 外的全部服务）。

.PARAMETER Down
    up 前执行 docker compose down（仅 --profile full，用于重建全部服务）。

.PARAMETER SkipHealthCheck
    up 后不等待 api 健康检查。

.PARAMETER EnvFile
    环境变量文件路径，默认使用脚本所在目录的 .env。
    支持指定其他文件（如 .env.dev、.env.prod），会显式传给 docker compose --env-file。

.EXAMPLE
    .\dev-up.ps1                              # up 全部服务（含基础设施 + 健康检查）
    .\dev-up.ps1 -Action build                # CI 流程构建全部镜像
    .\dev-up.ps1 -Action build -Service api   # 只构建 api 镜像（含 mvnw test + package）
    .\dev-up.ps1 -Action up -Service api      # 只重启 api 容器（基础设施保持运行）
    .\dev-up.ps1 -Action up -Service api,judge
    .\dev-up.ps1 -Action build -Service frontend
    .\dev-up.ps1 -Action up -Down             # 全部重建
    .\dev-up.ps1 -EnvFile .\.env.dev         # 使用 .env.dev 启动全部服务
#>
param(
    [ValidateSet('build', 'up')]
    [string]$Action = 'up',

    [string[]]$Service = @('all'),

    [switch]$Down,

    [switch]$SkipHealthCheck,

    [string]$EnvFile = "$PSScriptRoot/.env"
)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

# 切换到脚本所在目录（docker/），仓库根目录为其上一级
Set-Location $PSScriptRoot
$RepoRoot = Split-Path $PSScriptRoot -Parent

# 服务名 → compose 服务名 / profile
$ServiceMap = @{
    'api'      = @{ compose = 'api-service';   profile = 'api'   }
    'judge'    = @{ compose = 'judge-service'; profile = 'judge' }
    'ai'       = @{ compose = 'ai-service';    profile = 'ai'    }
    'frontend' = @{ compose = 'frontend';      profile = 'frontend' }
    'infra'    = @{ compose = '';              profile = 'infra' }
}
$BuildableServices = @('api', 'judge', 'ai', 'frontend')

# 解析 Service 列表
if ($Service -contains 'all') {
    $Selected = @($ServiceMap.Keys)
} else {
    foreach ($s in $Service) {
        if (-not $ServiceMap.ContainsKey($s)) {
            Write-Host "❌ 未知服务：$s（可选：$($ServiceMap.Keys -join ' / ') / all）" -ForegroundColor Red
            exit 1
        }
    }
    $Selected = $Service
}

function Invoke-Compose { docker compose -p bluenet --env-file $EnvFile @args }

function Invoke-Step($Message) {
    Write-Host "`n=== $Message ===" -ForegroundColor Cyan
}

function Invoke-Native($Message, [scriptblock]$Command) {
    Write-Host "`n>>> $Message" -ForegroundColor Yellow
    & $Command
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ $Message 失败" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

# ---------- 前置检查 ----------
Invoke-Step '前置检查'

try {
    docker version --format '{{.Server.Version}}' | Out-Null
} catch {
    Write-Host '❌ Docker 未运行，请先启动 Docker Desktop' -ForegroundColor Red
    exit 1
}
Write-Host "✅ Docker daemon 正常"

if (-not (Test-Path $EnvFile)) {
    Write-Host "❌ 未找到环境变量文件：$EnvFile，请先参考 README 创建" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 环境变量文件存在：$EnvFile"

if ($Action -eq 'up' -and $Selected -contains 'frontend') {
    # 3000 端口被非 Docker 进程占用时前端会启动失败，提前预警
    $port3000 = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
    if ($port3000) {
        $ownerPid = $port3000[0].OwningProcess
        $proc = Get-Process -Id $ownerPid -ErrorAction SilentlyContinue
        Write-Host "⚠️  端口 3000 已被占用（PID $ownerPid，$($proc.ProcessName)）。" -ForegroundColor Yellow
        Write-Host '   若是已运行的前端开发服务（pnpm dev），请改用 -Service api,judge,ai 跳过前端容器。' -ForegroundColor Yellow
    }
}

# ---------- CI 同款服务级构建 ----------
# 与 .github/workflows/ci.yml 的 test + build job 步骤一一对应：
#   api / judge：mvnw test → mvnw clean package -DskipTests → compose build
#   ai          ：uv sync --frozen --no-dev → uv run ruff check . → compose build
#   frontend    ：pnpm install --frozen-lockfile → tsc --noEmit → pnpm lint → compose build

function Build-Api {
    Invoke-Native 'api：mvnw test（CI: api-service-test）' {
        & "$RepoRoot/src/backend/mvnw.cmd" -B -f "$RepoRoot/src/backend/pom.xml" clean test
    }
    Invoke-Native 'api：mvnw clean package -DskipTests（CI: build-api-service）' {
        & "$RepoRoot/src/backend/mvnw.cmd" -B -f "$RepoRoot/src/backend/pom.xml" clean package -DskipTests
    }
    Invoke-Native 'api：docker compose build api-service' { Invoke-Compose build api-service }
}

function Build-Judge {
    Invoke-Native 'judge：mvnw test（CI: judge-service-test）' {
        & "$RepoRoot/src/backend/mvnw.cmd" -B -f "$RepoRoot/src/judge-service/pom.xml" clean test
    }
    Invoke-Native 'judge：mvnw clean package -DskipTests（CI: build-judge-service）' {
        & "$RepoRoot/src/backend/mvnw.cmd" -B -f "$RepoRoot/src/judge-service/pom.xml" clean package -DskipTests
    }
    Invoke-Native 'judge：docker compose build judge-service' { Invoke-Compose build judge-service }
}

function Build-Ai {
    Invoke-Native 'ai：uv sync --frozen --no-dev（CI: ai-service-test）' {
        & uv sync --frozen --no-dev --project "$RepoRoot/src/ai-service"
    }
    Invoke-Native 'ai：uv run ruff check .（CI: ai-service-test）' {
        Push-Location "$RepoRoot/src/ai-service"
        try { & uv run ruff check . }
        finally { Pop-Location }
    }
    Invoke-Native 'ai：docker compose build ai-service' { Invoke-Compose build ai-service }
}

function Build-Frontend {
    Invoke-Native 'frontend：pnpm install --frozen-lockfile（CI: frontend-test）' {
        Push-Location "$RepoRoot/src/frontend"
        try { & pnpm install --frozen-lockfile }
        finally { Pop-Location }
    }
    Invoke-Native 'frontend：tsc --noEmit（CI: frontend-test）' {
        Push-Location "$RepoRoot/src/frontend"
        try { & npx tsc --noEmit }
        finally { Pop-Location }
    }
    Invoke-Native 'frontend：pnpm lint（CI: frontend-test）' {
        Push-Location "$RepoRoot/src/frontend"
        try { & pnpm lint }
        finally { Pop-Location }
    }
    Invoke-Native 'frontend：docker compose build frontend' { Invoke-Compose build frontend }
}

# ---------- Action: build ----------
if ($Action -eq 'build') {
    foreach ($s in $Selected) {
        if ($s -eq 'infra') {
            Invoke-Step 'infra：无可构建镜像，确保基础设施运行'
            Invoke-Compose --profile infra up -d
            continue
        }
        Invoke-Step "构建 $s（按 CI 流水线）"
        switch ($s) {
            'api'      { Build-Api }
            'judge'    { Build-Judge }
            'ai'       { Build-Ai }
            'frontend' { Build-Frontend }
        }
    }
    Write-Host "`n✅ 构建完成：$($Selected -join ', ')" -ForegroundColor Green
    exit 0
}

# ---------- Action: up ----------
Invoke-Step '启动基础设施（pgsql / redis / rabbitmq / oss）'
Invoke-Compose --profile infra up -d
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$AppServices = $Selected | Where-Object { $_ -ne 'infra' }
if (-not $AppServices) {
    Invoke-Step '仅基础设施启动完成'
    Invoke-Compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
    exit 0
}

if ($Down) {
    Invoke-Step '清理旧容器（-Down，仅 --profile full）'
    Invoke-Compose --profile full down
}

$profiles = @('--profile', 'infra') + ($AppServices | ForEach-Object { @('--profile', $ServiceMap[$_].profile) })
$composeNames = $AppServices | ForEach-Object { $ServiceMap[$_].compose }

Invoke-Step "启动服务：$($composeNames -join ', ')"
Invoke-Compose @profiles up -d $composeNames
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# ---------- 等待 api 健康 ----------
$healthy = $null
if (-not $SkipHealthCheck -and $AppServices -contains 'api') {
    Invoke-Step '等待 api-service 健康检查通过（最长 5 分钟）'
    for ($i = 1; $i -le 60; $i++) {
        # 用 curl.exe 而非 Invoke-RestMethod：PS 5.1 的 HTTP cmdlet 走 IE 代理，探测 localhost 会超时
        $raw = curl.exe -sf --max-time 3 http://localhost:8080/api/v1/health 2>$null
        if ($raw -and ($raw | Select-String -Pattern '"code"\s*:\s*200')) { $healthy = $true; break }
        Write-Host "  等待中... ($i/60)"
        Start-Sleep -Seconds 5
    }
}

# ---------- 输出状态 ----------
Write-Host ''
Invoke-Compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

Write-Host ''
if ($healthy -eq $true) {
    Write-Host '✅ 后端已就绪：http://localhost:8080/api/v1/health -> UP' -ForegroundColor Green
} elseif ($AppServices -contains 'api') {
    Write-Host '⚠️  api 健康检查超时，请查看日志：docker compose -p bluenet logs -f api-service' -ForegroundColor Yellow
}
if ($AppServices -contains 'frontend') {
    Write-Host '✅ 前端地址：http://localhost:3000' -ForegroundColor Green
}
Write-Host "`n常用命令（在 docker/ 目录执行）："
Write-Host '  构建镜像   .\dev-up.ps1 -Action build [-Service api|judge|ai|frontend]'
Write-Host '  查看日志   docker compose -p bluenet --env-file $EnvFile logs -f <服务名>'
Write-Host '  停止全部   docker compose -p bluenet --profile full down'
