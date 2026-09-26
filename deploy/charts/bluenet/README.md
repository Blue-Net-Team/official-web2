# bluenet chart — 通用工作负载 chart

**一个 chart 覆盖所有服务**：模板只有一套（Deployment / StatefulSet / CronJob / Service / Secret），
所有差异（端口、挂载、环境变量、资源、调度、特权）都由 values 表达。
**每个服务一个 release、一份 values**，因此发布与回滚都是服务级的。

## 目录结构

```
bluenet/
├── Chart.yaml
├── values.yaml              # 全部可用字段的默认值（字段字典）
├── templates/
│   ├── _helpers.tpl         # bluenet.container / bluenet.podSpec 等公共模板
│   ├── deployment.yaml      # kind=Deployment
│   ├── statefulset.yaml     # kind=StatefulSet（含 volumeClaimTemplates）
│   ├── cronjob.yaml         # kind=CronJob
│   ├── service.yaml         # ClusterIP / NodePort / headless
│   └── secret.yaml          # 可选，仅本地冒烟用；生产一律用预建 Secret 引用
└── values/
    ├── postgres.yaml  ├── redis.yaml  ├── rabbitmq.yaml
    ├── api.yaml       ├── frontend.yaml  ├── ai.yaml  └── judge.yaml
```

## 镜像地址：部署时注入，不入库

chart 里的 `image.repository` 是占位符，**真实的 ACR 地址与 namespace 不在仓库中**（含 CD 使用的 values 文件）：

| 服务类型 | 占位符 | 部署时由谁注入 |
|---------|--------|---------------|
| 业务服务（api/frontend/ai/judge） | `<ACR-ADDRESS>/<SERVICE-NAMESPACE>/<SERVICE-REPO>` | CI：`--set image.repository=$ACR_REPO/$ACR_NAMESPACE/$ACR_REPO_NAME` |
| 基础服务（postgres/redis/rabbitmq） | `<ACR-ADDRESS>/<BASE-NAMESPACE>/<BASE-REPO>` | 手工部署：`--set image.repository=<ACR>/<BASE-NS>/<BASE-REPO>` |

示例（手工部署基础服务）：

```bash
helm upgrade --install bluenet-postgres deploy/charts/bluenet -n bluenet \
  -f deploy/charts/bluenet/values/postgres.yaml \
  --set image.repository=<ACR>/<BASE-NAMESPACE>/<BASE-REPO>
```

> 基础镜像与业务镜像位于同一个 ACR host、不同 namespace；k3s 的 `registries.yaml` 认证按 **host** 生效，因此一份凭据即可覆盖两个 namespace。

## 部署（每服务独立 release）

```bash
NS=bluenet
CHART=deploy/charts/bluenet
ACR="<真实 ACR>/<命名空间>/<仓库名>"     # 不入库，安装/升级时传入

# 有状态（先装）
helm upgrade --install bluenet-postgres $CHART -n $NS -f $CHART/values/postgres.yaml --set image.repository=$ACR
helm upgrade --install bluenet-redis    $CHART -n $NS -f $CHART/values/redis.yaml    --set image.repository=$ACR
helm upgrade --install bluenet-rabbitmq $CHART -n $NS -f $CHART/values/rabbitmq.yaml --set image.repository=$ACR

# 无状态
helm upgrade --install bluenet-api      $CHART -n $NS -f $CHART/values/api.yaml      --set image.repository=$ACR --set image.tag=<版本>
helm upgrade --install bluenet-frontend $CHART -n $NS -f $CHART/values/frontend.yaml --set image.repository=$ACR --set image.tag=<版本>
helm upgrade --install bluenet-ai       $CHART -n $NS -f $CHART/values/ai.yaml       --set image.repository=$ACR --set image.tag=<版本>
helm upgrade --install bluenet-judge    $CHART -n $NS -f $CHART/values/judge.yaml    --set image.repository=$ACR --set image.tag=<版本>

# 只更新某个服务（不影响其他服务）
helm upgrade bluenet-api $CHART -n $NS -f $CHART/values/api.yaml --set image.tag=<新版本>

# 回滚单个服务
helm rollback bluenet-api -n $NS
```

> **为什么 ACR 地址用 `--set` 传**：仓库内不保存真实仓库地址（脱敏要求）。
> 也可以维护一份仓库外的 `values-local.yaml` 用 `-f` 传入。

## values 字段速查（完整默认值见 values.yaml）

| 字段 | 说明 |
|------|------|
| `kind` | `Deployment` / `StatefulSet` / `CronJob` |
| `replicas` | 副本数 |
| `image.repository` / `image.tag` | 镜像地址与版本 |
| `service.enabled/type/ports[]` | 是否建 Service、类型（ClusterIP/NodePort）、端口列表（`name/port/targetPort/nodePort`） |
| `service.headless` | StatefulSet 额外建 headless Service |
| `env[]` / `envFrom[]` | 环境变量（明文或 `secretKeyRef`） |
| `persistence.*` | 仅 StatefulSet：`enabled/storageClassName/size/mountPath`（生成 volumeClaimTemplates） |
| `volumes[]` / `volumeMounts[]` | 任意卷，如 Secret 挂载 PEM、`emptyDir`、`/dev/shm` |
| `containerSecurityContext.privileged` | judge 沙箱需要 |
| `resources` | request/limit（容量规划见 design.md D4） |
| `nodeSelector` / `tolerations` / `affinity` | 调度约束（**仅数据归属需要钉节点**：`bluenet/role=db|mq`） |
| `probes.liveness/readiness/startup` | 探针 |
| `cronjob.*` | `kind=CronJob` 时：schedule/timeZone/并发策略/重启策略 |
| `strategy` | 滚动更新参数（Deployment 为 rollingUpdate，StatefulSet 为 updateStrategy） |
| `secret.enabled/name/stringData` | 可选，仅本地冒烟；生产用预建 Secret + `env[].valueFrom` 引用 |

## 密钥与 Secret 约定

Chart 内**不写任何密码**，全部引用预先创建的 Secret（创建命令见 `deploy/k3s/README.md`）：

| Secret | 键 | 使用者 |
|--------|-----|--------|
| `bluenet-postgres` | `postgres-password` | postgres / api |
| `bluenet-redis` | `redis-password` | redis / api |
| `bluenet-rabbitmq` | `username`, `password`, `erlang-cookie` | rabbitmq / api / judge / ai |
| `bluenet-api-secret` | `jwt-secret`, `oss-ak`, `oss-sk`, `mail-password`, `system-user-password`, `github-client-secret`, `github-app-webhook-secret`, `wps-bind-code` | api |
| `bluenet-ai-secret` | `pgvector-uri`, `siliconflow-api-key`, `deepseek-api-key` | ai |
| `bluenet-github-keys` | `github-issue-private-key.pem`, `github-org-private-key.pem` | api（文件挂载） |

GitHub App 私钥以**文件**形式挂载（应用侧只认路径 `/app/github-issue-private-key.pem`，代码无需改动）：

```yaml
volumes:
  - name: github-keys
    secret:
      secretName: bluenet-github-keys
      defaultMode: 0400
volumeMounts:
  - name: github-keys
    mountPath: /app/github-issue-private-key.pem
    subPath: github-issue-private-key.pem
    readOnly: true
```

## 镜像与回滚约定

**必须使用不可变 tag**，否则无法回滚（旧镜像被同名 tag 覆盖后永久丢失）。

| 项 | 约定 |
|----|------|
| 业务服务 tag | `<服务名>-<git短SHA>`，如 `api-a1b2c3d`（唯一、不可变） |
| 第三方镜像 tag | `<镜像名>-<版本号>`，如 `redis-7`、`pgvector-pg17` |
| 可选辅助 tag | `<服务名>-latest` 仅供本地调试，**不得用于部署** |
| 可选锁定 | `--set image.digest=sha256:...` 锁定内容哈希（防 tag 被重新推送） |

CI 注入方式（values 文件只提供默认/占位值）：

```bash
helm upgrade --install bluenet-api deploy/charts/bluenet -n bluenet \
  -f deploy/charts/bluenet/values/api.yaml \
  --set image.repository=<ACR>/<命名空间>/<仓库名> \
  --set image.tag=api-a1b2c3d
```

回滚（helm 保存了每次 release 的 values 快照）：

```bash
helm history bluenet-api -n bluenet
helm get values bluenet-api -n bluenet --revision 3 | grep tag
helm rollback bluenet-api 3 -n bluenet
```

> 反例：若 CI 始终推 `:api-latest`，则 ① Deployment spec 不变 → 不触发滚动更新；② `IfNotPresent` 不重拉 → 发版不生效；③ rollback 渲染出的还是 `:api-latest` → 静默失败。

## 校验

```bash
helm lint deploy/charts/bluenet
helm template bluenet-api deploy/charts/bluenet -f deploy/charts/bluenet/values/api.yaml | less
```
