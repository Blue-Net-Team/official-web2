# deploy/scripts

辅助脚本目录。脚本内**不含任何真实凭据**，运行前由使用者以参数传入。

## mirror-images.ps1

**用途**：在本地电脑（能访问 docker.io）上把第三方镜像中转推送到阿里云 ACR，供集群节点从 ACR 拉取（国内网络直连 docker.io 不稳定）。

**为什么需要**：集群节点在国内，拉取 `docker.io` / `registry.k8s.io` 镜像经常失败。用 ACR 做中转站后，所有第三方镜像一次性推送，集群侧只从 ACR 拉，快且稳。

### 使用前准备

| 参数 | 从哪里获取 |
|------|-----------|
| `-Acr` | 阿里云控制台 → 容器镜像服务 ACR → 实例列表 → 实例的访问域名 |
| `-Namespace` | 控制台 → 命名空间 |
| `-Repo` | 自定义仓库名（本项目约定所有第三方镜像推入同一仓库，用 tag 区分） |
| `-Username` | ACR 登录用户名（通常是阿里云账号全名） |
| 密码 | 不写入脚本、不提交仓库；执行时由 `docker login` 交互式输入 |

### 执行

```powershell
powershell -ExecutionPolicy Bypass -File deploy\scripts\mirror-images.ps1 `
    -Acr <ACR地址> -Namespace <命名空间> -Repo <仓库名> -Username <用户名>
```

常用变体：

```powershell
# 只同步部分镜像（按 tag 名）
... -Only redis-7,busybox-1.36

# 只演练，不实际拉取/推送
... -DryRun

# 已 docker login 过
... -SkipLogin
```

### 新增镜像

编辑脚本中的 `$map`（源镜像 → tag），再执行即可。tag 约定为 `<镜像名>-<版本号>`，例如 `redis:7` → `redis-7`。

### 集群侧配套

推送完成后，需要在**每个节点**配置 `/etc/rancher/k3s/registries.yaml` 写入 ACR 认证信息，并重启 k3s / k3s-agent。模板见 `deploy/k3s/README.md` 的「配置镜像仓库凭据（ACR）」章节。

> 注意：该文件包含密码，**只允许在服务器上手工创建，不得提交到仓库**。
