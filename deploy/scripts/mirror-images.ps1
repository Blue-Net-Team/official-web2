# 第三方镜像中转脚本（本地电脑执行）
#
# 用途：把 docker.io 上的第三方镜像拉下来、重打标签、推送到阿里云 ACR，
#       使集群节点从 ACR 拉取（国内快且稳定）。
#
# ---------------------------------------------------------------------------
# 使用前必须填写以下参数（不含敏感信息的默认值，请勿把真实凭据提交到仓库）
# ---------------------------------------------------------------------------
#   -Acr        ACR 仓库地址，见阿里云控制台 → 容器镜像服务 → 实例列表
#               个人版示例：crpi-xxxxxxxxxxxx.cn-beijing.personal.cr.aliyuncs.com
#               企业版示例：registry.cn-hangzhou.aliyuncs.com
#   -Namespace  ACR 命名空间（控制台 → 命名空间）
#   -Repo       镜像仓库名（本项目约定所有第三方镜像推入同一个仓库，用 tag 区分）
#   -Username   ACR 登录用户名（阿里云账号名）
#   密码：不写入脚本、不提交仓库，由 docker login 交互式输入
#         （也可用 -Password 传入，但注意不要留在 shell 历史/CI 日志里）
#
# ---------------------------------------------------------------------------
# 用法
# ---------------------------------------------------------------------------
#   powershell -ExecutionPolicy Bypass -File deploy\scripts\mirror-images.ps1 `
#       -Acr <ACR地址> -Namespace <命名空间> -Repo <仓库名> -Username <用户名>
#
#   只处理部分镜像（按 tag 名）：
#       ... -Only redis-7,busybox-1.36
#   只演练不执行：
#       ... -DryRun
#   已登录过，跳过 login：
#       ... -SkipLogin
#
# ---------------------------------------------------------------------------
# 命名约定
# ---------------------------------------------------------------------------
#   目标地址 = <Acr>/<Namespace>/<Repo>:<tag>
#   tag 规则 = <镜像名>-<版本号>，例如 redis:7 → redis-7
#   新增镜像时，在下面的 $map 里加一行即可。

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Acr,
    [Parameter(Mandatory = $true)][string]$Namespace,
    [Parameter(Mandatory = $true)][string]$Repo,
    [Parameter(Mandatory = $true)][string]$Username,
    [string]$Password,
    [string[]]$Only = @(),
    [switch]$SkipLogin,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$registry = "$Acr/$Namespace/$Repo"

# 源镜像 -> tag
$map = [ordered]@{
    "pgvector/pgvector:pg17"              = "pgvector-pg17"
    "redis:7"                             = "redis-7"
    "rabbitmq:3-management"               = "rabbitmq-3-management"
    "kubernetesui/dashboard:v2.7.0"       = "dashboard-v2.7.0"
    "kubernetesui/metrics-scraper:v1.0.8" = "metrics-scraper-v1.0.8"
    "busybox:1.36"                        = "busybox-1.36"
    "alpine:3.20"                         = "alpine-3.20"
}

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-Ok($msg)   { Write-Host "    OK   $msg" -ForegroundColor Green }
function Write-Fail($msg) { Write-Host "    FAIL $msg" -ForegroundColor Red }

# 前置检查
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "未找到 docker 命令，请先启动 Docker Desktop"
}
docker info *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker 未运行，请先启动 Docker Desktop" }

Write-Step "目标仓库: $registry"
if ($Only.Count -gt 0) { Write-Host "    仅处理: $($Only -join ', ')" }

if (-not $SkipLogin -and -not $DryRun) {
    Write-Step "登录 ACR (user=$Username)"
    if ($Password) {
        docker login --username=$Username --password $Password $Acr
    } else {
        docker login --username=$Username $Acr
    }
    if ($LASTEXITCODE -ne 0) { throw "docker login 失败" }
}

$results = @()

foreach ($src in $map.Keys) {
    $tag  = $map[$src]
    $dest = "$registry`:$tag"

    if ($Only.Count -gt 0 -and ($Only -notcontains $tag)) { continue }

    Write-Step "$src  ->  $dest"

    if ($DryRun) { Write-Host "    [DryRun] 跳过实际执行"; continue }

    docker pull $src
    if ($LASTEXITCODE -ne 0) { Write-Fail "拉取失败 $src"; $results += [pscustomobject]@{Image=$src;Tag=$tag;Status="pull-failed"}; continue }

    docker tag $src $dest
    if ($LASTEXITCODE -ne 0) { Write-Fail "打标签失败 $src"; $results += [pscustomobject]@{Image=$src;Tag=$tag;Status="tag-failed"}; continue }

    docker push $dest
    if ($LASTEXITCODE -ne 0) { Write-Fail "推送失败 $dest"; $results += [pscustomobject]@{Image=$src;Tag=$tag;Status="push-failed"}; continue }

    Write-Ok "已推送 $dest"
    $results += [pscustomobject]@{Image=$src;Tag=$tag;Status="ok"}
}

Write-Step "结果汇总"
$results | Format-Table -AutoSize

$failed = @($results | Where-Object { $_.Status -ne "ok" })
if ($failed.Count -gt 0) {
    Write-Fail "$($failed.Count) 个镜像未成功，请单独处理"
    exit 1
}
Write-Ok "全部镜像已推送到 $registry"
Write-Host "`n集群侧下一步：在各节点配置 /etc/rancher/k3s/registries.yaml 使用该 ACR（见 deploy/k3s/README.md）"
