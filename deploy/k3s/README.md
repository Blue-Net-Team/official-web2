# k3s 集群安装指南

BlueNet 边缘集群（5 节点 k3s）安装文档。适用于跨云账号、仅公网互通的 2C2G 服务器。

> 服务器 IP、登录方式等敏感信息不入库，请以各云控制台与运维记录为准。前置条件：**安全组已按设计 D2 开白**（6443/SSH 公网认证、**51820/UDP**（注意是 UDP 不是 TCP）节点互指、NodePort 30000-32767 仅 nginx 节点、5432/6379/5672/15672 不公网、默认拒绝）。

## 一、安装前准备（全部节点执行）

### 1. 加 swap（2C2G 与存量 docker 容器共存期防 OOM）

```bash
fallocate -l 2G /swapfile && chmod 600 /swapfile
mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab
sysctl -w vm.swappiness=10 && echo 'vm.swappiness=10' >> /etc/sysctl.conf
free -m   # 确认 swap 行已生效
```

### 2. 端口自检

```bash
ss -lntup | grep -E ':(6443|10250)\b'   # 应无输出；有输出说明端口被占，先处理
```

## 二、离线安装（国内网络推荐）

GitHub 直连慢/不通时，改用离线方式：在**有外网的机器**（如本地电脑挂代理）下载 3 个文件，上传到服务器后用本地文件安装，全程不访问 GitHub。

### 1. 下载安装包（有外网的机器上执行）

官方下载地址（GitHub Releases，需能访问 GitHub）：

```
二进制:     https://github.com/k3s-io/k3s/releases/download/<VER>/k3s
镜像包:     https://github.com/k3s-io/k3s/releases/download/<VER>/k3s-airgap-images-amd64.tar.gz
安装脚本:   https://raw.githubusercontent.com/k3s-io/k3s/master/install.sh
版本列表:   https://github.com/k3s-io/k3s/releases
```

命令行下载（官方直连方式）：

```bash
export VER="v1.30.6+k3s1"   # 选定版本，示例

curl -fLO "https://github.com/k3s-io/k3s/releases/download/${VER}/k3s"
curl -fLO "https://github.com/k3s-io/k3s/releases/download/${VER}/k3s-airgap-images-amd64.tar.gz"
curl -fsSL -o install.sh "https://raw.githubusercontent.com/k3s-io/k3s/master/install.sh"

chmod +x k3s
ls -lh k3s k3s-airgap-images-amd64.tar.gz install.sh   # 确认三个文件都在
```

GitHub 访问困难时，推荐优先使用国内 github-release 镜像（同一份清华 TUNA 内容，多站互为备份）：

```bash
export VER="v1.37.0+k3s1"
# 任选一个（按可用性轮换；部分站点可能封禁云厂商 IP 段，403 就换下一家）：
BASE="https://mirrors.bfsu.edu.cn/github-release/k3s-io/k3s"        # 北外（清华姊妹站）
# BASE="https://mirror.nju.edu.cn/github-release/k3s-io/k3s"        # 南大
# BASE="https://mirrors.tuna.tsinghua.edu.cn/github-release/k3s-io/k3s"  # 清华（可能封阿里云 IP）
# 注意路径结构不同于 GitHub：/github-release/<org>/<repo>/<tag>/<文件>

curl -fLO "${BASE}/${VER}/k3s"
curl -fLO "${BASE}/${VER}/k3s-airgap-images-amd64.tar.gz"
curl -fsSL -o install.sh "https://ghfast.top/https://raw.githubusercontent.com/k3s-io/k3s/master/install.sh"

chmod +x k3s
```

> 镜像站一般只保留最新若干版本；下载旧版本时改用下面的加速代理。

备选加速代理（2026-09 实测可用，公益站可能失效，多备几个轮换）：

```bash
export VER="v1.37.0+k3s1"

BASE="https://ghfast.top/https://github.com/k3s-io/k3s/releases/download"
# 备选加速：BASE="https://ghproxy.net/https://github.com/k3s-io/k3s/releases/download"
# 备选加速：BASE="https://gh-proxy.com/https://github.com/k3s-io/k3s/releases/download"（小文件可用，大文件可能 403）

curl -fLO "${BASE}/${VER}/k3s"
curl -fLO "${BASE}/${VER}/k3s-airgap-images-amd64.tar.gz"
curl -fsSL -o install.sh "https://ghfast.top/https://raw.githubusercontent.com/k3s-io/k3s/master/install.sh"

chmod +x k3s
```

> 国内无 k3s release 官方镜像站（Rancher 镜像站 rancher-mirror.rancher.cn 仅镜像安装脚本与容器镜像，不托管二进制/airgap 包，已实测 404）。

> 可选校验：从 releases 页面复制对应版本 `sha256sum-amd64.txt`，在服务器上 `sha256sum -c` 核对。

### 2. 上传到服务器

将 `k3s`、`k3s-airgap-images-amd64.tar.gz`、`install.sh`、`offline-install.sh` **四个文件上传到每一台服务器**（master 和 agent 都需要全部 3 个安装包：agent 同样运行 kubelet/containerd，且 flannel 等基础镜像要从 airgap 包导入），放到同一目录（如 `~/k3s-offline/`），scp 或 rz 均可。

### 3. 服务器上执行离线安装

```bash
cd ~/k3s-offline
chmod +x offline-install.sh

# master：
./offline-install.sh server <master公网IP> <K3S_VERSION>
# 例：./offline-install.sh server 1.2.3.4 v1.30.6+k3s1

# agent（逐台，NODE_EXTERNAL_IP 填本机公网 IP）：
./offline-install.sh agent <master公网IP> <K3S_TOKEN> <本节点公网IP> <K3S_VERSION>
# 例：./offline-install.sh agent 1.2.3.4 K10xxx::server:xxx 5.6.7.8 v1.30.6+k3s1
```

安装完成后同样会打印 `K3S_URL` / `K3S_TOKEN`（master）。之后验证步骤与在线安装完全一致（本文第四节）。

## 三、安装 master（在线脚本，网络通时可用）

将 `install-server.sh` 上传到 master 节点（scp 或直接粘贴内容），执行：

```bash
chmod +x install-server.sh
MASTER_PUBLIC_IP=<master公网IP> sudo -E bash install-server.sh
```

- 若登录用户非 root：先 `sudo su -`，再执行 `MASTER_PUBLIC_IP=<master公网IP> bash install-server.sh`
- **锁定版本（推荐）**：五台统一加 `INSTALL_K3S_VERSION="vX.Y.Z+k3s1"`（版本号见 https://github.com/k3s-io/k3s/releases ），不设置则装最新稳定版

安装完成后脚本打印 `K3S_URL` 与 `K3S_TOKEN`，**记下 TOKEN**，所有 agent 加入都要用：

```
K3S_URL=https://<master公网IP>:6443
K3S_TOKEN=K10xxxxxxxx::server:xxxxxxxx
```

## 四、安装 agent（在线脚本）

将 `install-agent.sh` 上传到各 agent 节点，逐台执行：

```bash
chmod +x install-agent.sh
K3S_URL=https://<master公网IP>:6443 \
K3S_TOKEN=<master打印的token> \
NODE_EXTERNAL_IP=<本节点公网IP> \
bash install-agent.sh
```

- `NODE_EXTERNAL_IP` = 当前这台机器的公网 IP（每台不同，勿照抄）
- 锁定版本时与 master 填同一个 `INSTALL_K3S_VERSION`

## 五、验证

master 上执行（或本地配好 kubeconfig 后执行）：

```bash
kubectl get nodes -o wide
# 期望：全部节点 Ready，EXTERNAL-IP 列显示各节点公网 IP

# wireguard 隧道监听（每台节点都应看到 51820/udp）
ss -lnup | grep 51820
```

本地（Windows）接管集群：

```powershell
scp -i <密钥> <用户>@<master公网IP>:/etc/rancher/k3s/k3s.yaml .
# 编辑 k3s.yaml：server 地址由 127.0.0.1 改为 master 公网 IP
$env:KUBECONFIG="C:\path\to\k3s.yaml"
kubectl get nodes
```

## 六、常见问题

| 现象 | 排查 |
|------|------|
| k3s 服务反复重启，日志 `kubelet is configured to not run on a host using cgroup v1` | 宿主为 cgroup v1（老系统）。已在安装脚本中加 `--kubelet-arg=fail-cgroupv1=false`；旧安装可用 `sudo vi /etc/systemd/system/k3s.service` 手动补充后 `systemctl daemon-reload && systemctl restart k3s` |
| agent 加入卡住 "Waiting to retrieve agent configuration" | agent → master 6443 不通：查安全组 6443 来源、master 防火墙 |
| 跨节点 Pod 不通 | 51820 开成 TCP（wireguard 只用 UDP）；或 UDP 来源 IP 不全 |
| `kubectl get nodes` 只看到自己 | agent 未成功 join，查 agent 节点 `journalctl -u k3s-agent` |
| 卸载 k3s | master: `/usr/local/bin/k3s-uninstall.sh`；agent: `/usr/local/bin/k3s-agent-uninstall.sh` |

## 七、下一步

按 `openspec/changes/deploy-k3s-edge-cluster/tasks.md` 继续：节点打标（1.5）→ 基础组件（第 2 组）→ Helm charts（第 3、4 组）。
