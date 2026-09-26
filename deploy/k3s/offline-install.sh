#!/usr/bin/env bash
# k3s 离线安装脚本（国内/无外网环境）
#
# 前置：本目录下必须已有以下 3 个文件（下载方法见 deploy/k3s/README.md 离线安装章节）：
#   - k3s                          k3s 二进制（与 K3S_VERSION 对应）
#   - k3s-airgap-images-amd64.tar.gz  离线镜像包
#   - install.sh                   k3s 官方安装脚本
#
# 用法：
#   master: ./offline-install.sh server <master公网IP> [K3S_VERSION]
#   agent:  ./offline-install.sh agent <master公网IP> <K3S_TOKEN> <本节点公网IP> [K3S_VERSION]
#
# 示例：
#   ./offline-install.sh server 1.2.3.4 v1.30.6+k3s1
#   ./offline-install.sh agent 1.2.3.4 K10xxx::server:xxx 5.6.7.8 v1.30.6+k3s1
set -euo pipefail

MODE="${1:?缺少参数：server 或 agent}"

for f in k3s k3s-airgap-images-amd64.tar.gz install.sh; do
  [ -f "$f" ] || { echo "缺少文件: $f（请先按 README 离线下载）"; exit 1; }
done

install -m 755 k3s /usr/local/bin/k3s
mkdir -p /var/lib/rancher/k3s/agent/images
cp k3s-airgap-images-amd64.tar.gz /var/lib/rancher/k3s/agent/images/

export INSTALL_K3S_SKIP_DOWNLOAD=true

if [ "${MODE}" = "server" ]; then
  MASTER_PUBLIC_IP="${2:?缺少 master 公网 IP}"
  K3S_VERSION="${3:-}"
  [ -n "${K3S_VERSION}" ] && export INSTALL_K3S_VERSION="${K3S_VERSION}"
  chmod +x install.sh
  ./install.sh server \
    --kubelet-arg=fail-cgroupv1=false \
    --flannel-backend wireguard-native \
    --node-external-ip "${MASTER_PUBLIC_IP}" \
    --tls-san "${MASTER_PUBLIC_IP}" \
    --write-kubeconfig-mode 644
  echo "== k3s server 安装完成 =="
  echo "  K3S_URL=https://${MASTER_PUBLIC_IP}:6443"
  echo "  K3S_TOKEN=$(cat /var/lib/rancher/k3s/server/node-token)"
elif [ "${MODE}" = "agent" ]; then
  MASTER_PUBLIC_IP="${2:?缺少 master 公网 IP}"
  K3S_TOKEN="${3:?缺少 K3S_TOKEN}"
  NODE_EXTERNAL_IP="${4:?缺少本节点公网 IP}"
  K3S_VERSION="${5:-}"
  [ -n "${K3S_VERSION}" ] && export INSTALL_K3S_VERSION="${K3S_VERSION}"
  chmod +x install.sh
  K3S_URL="https://${MASTER_PUBLIC_IP}:6443" \
  K3S_TOKEN="${K3S_TOKEN}" \
  INSTALL_K3S_EXEC="agent --kubelet-arg=fail-cgroupv1=false --node-external-ip ${NODE_EXTERNAL_IP}" \
    ./install.sh
  echo "== k3s agent 安装完成，节点 ${NODE_EXTERNAL_IP} 已加入 =="
else
  echo "第一个参数必须是 server 或 agent"; exit 1
fi
