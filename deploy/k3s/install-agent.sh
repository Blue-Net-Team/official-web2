#!/usr/bin/env bash
# k3s agent 安装脚本（worker 节点执行）
# 用法: K3S_URL=https://<master公网IP>:6443 K3S_TOKEN=<token> NODE_EXTERNAL_IP=<本节点公网IP> ./install-agent.sh
set -euo pipefail

K3S_URL="${K3S_URL:?请设置环境变量 K3S_URL（如 https://172.18.116.241:6443）}"
K3S_TOKEN="${K3S_TOKEN:?请设置环境变量 K3S_TOKEN（master 节点 /var/lib/rancher/k3s/server/node-token）}"
NODE_EXTERNAL_IP="${NODE_EXTERNAL_IP:?请设置环境变量 NODE_EXTERNAL_IP（本节点公网 IP）}"

if [ -n "${INSTALL_K3S_VERSION:-}" ]; then
  export INSTALL_K3S_VERSION
fi

curl -sfL https://get.k3s.io | sh -s - agent \
  --kubelet-arg=fail-cgroupv1=false \
  --server "${K3S_URL}" \
  --token "${K3S_TOKEN}" \
  --node-external-ip "${NODE_EXTERNAL_IP}"

echo "== k3s agent 安装完成，节点 ${NODE_EXTERNAL_IP} 已加入 ${K3S_URL} =="
