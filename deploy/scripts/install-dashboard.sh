#!/usr/bin/env bash
# 安装 Kubernetes Dashboard（官方 v2.7.0 manifest 方式）
#
# 用法：
#   ./install-dashboard.sh <ACR-ADDRESS>/<BASE-NAMESPACE>/<BASE-REPO>
# 例：
#   ./install-dashboard.sh crpi-xxxx.<region>.cr.aliyuncs.com/my-ns/my-repo
#
# 说明：
#   - Dashboard 为上游项目，自带完整清单（Deployment/Service/RBAC），无需自写 Deployment
#   - 需要先把两个镜像中转进 ACR（见 deploy/scripts/README.md）：
#       <repo>:dashboard-v2.7.0        （kubernetesui/dashboard:v2.7.0）
#       <repo>:metrics-scraper-v1.0.8  （kubernetesui/metrics-scraper:v1.0.8）
#   - 服务类型为 ClusterIP，不暴露公网；访问方式见文末
set -euo pipefail

REPO="${1:?用法: $0 <ACR-ADDRESS>/<BASE-NAMESPACE>/<BASE-REPO>}"
NS=kubernetes-dashboard
VER=v2.7.0
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "==> 下载官方 manifest（${VER}）"
for url in \
  "https://ghfast.top/https://raw.githubusercontent.com/kubernetes/dashboard/${VER}/aio/deploy/recommended.yaml" \
  "https://raw.githubusercontent.com/kubernetes/dashboard/${VER}/aio/deploy/recommended.yaml" ; do
  if curl -fsSL -m 60 -o "$WORK/recommended.yaml" "$url"; then echo "    OK: ${url:0:60}..."; break; fi
  echo "    失败，重试下一个源"
done
[ -s "$WORK/recommended.yaml" ] || { echo "下载失败"; exit 1; }

echo "==> 替换镜像为 ACR"
sed -e "s|image: kubernetesui/dashboard:v2.7.0|image: ${REPO}:dashboard-v2.7.0|" \
    -e "s|image: kubernetesui/metrics-scraper:v1.0.8|image: ${REPO}:metrics-scraper-v1.0.8|" \
    "$WORK/recommended.yaml" > "$WORK/dashboard.yaml"

echo "==> 应用清单"
kubectl apply -f "$WORK/dashboard.yaml"

echo "==> 创建管理员账号（cluster-admin 绑定）"
kubectl apply -f - <<EOF
apiVersion: v1
kind: ServiceAccount
metadata:
  name: dashboard-admin
  namespace: ${NS}
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: dashboard-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
  - kind: ServiceAccount
    name: dashboard-admin
    namespace: ${NS}
EOF

echo "==> 等待就绪"
kubectl -n "$NS" rollout status deploy/kubernetes-dashboard --timeout=3m
kubectl -n "$NS" rollout status deploy/dashboard-metrics-scraper --timeout=3m

cat <<'TIP'

完成。访问方式：

  # 1) 建立本地隧道（保持窗口不关）
  kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard 8443:443

  # 2) 浏览器打开 https://localhost:8443 （自签证书，忽略告警）

  # 3) 获取登录 token（有效期 30 天，到期重跑）
  kubectl -n kubernetes-dashboard create token dashboard-admin --duration=720h
TIP
