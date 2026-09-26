#!/usr/bin/env bash
# k3s server 安装脚本（master 节点执行）
# 用法: MASTER_PUBLIC_IP=<master公网IP> ./install-server.sh
set -euo pipefail

MASTER_PUBLIC_IP="${MASTER_PUBLIC_IP:?请设置环境变量 MASTER_PUBLIC_IP（master 节点公网 IP）}"

# 可选固定版本（建议生产锁定），如 INSTALL_K3S_VERSION="v1.30.6+k3s1"
# 不设置则安装最新稳定版
if [ -n "${INSTALL_K3S_VERSION:-}" ]; then
  export INSTALL_K3S_VERSION
fi

curl -sfL https://get.k3s.io | sh -s - server \
  --kubelet-arg=fail-cgroupv1=false \
  --flannel-backend wireguard-native \
  --flannel-external-ip \
  --node-external-ip "${MASTER_PUBLIC_IP}" \
  --tls-san "${MASTER_PUBLIC_IP}" \
  --write-kubeconfig-mode 644

echo "== k3s server 安装完成 =="
echo "集群连接信息:"
echo "  K3S_URL=https://${MASTER_PUBLIC_IP}:6443"
echo "  K3S_TOKEN=$(cat /var/lib/rancher/k3s/server/node-token)"
echo ""
echo "本地/CI 使用: 将 /etc/rancher/k3s/k3s.yaml 中 server 地址确认为 https://${MASTER_PUBLIC_IP}:6443"
