## Context

仓库已有通用 Bluenet Helm Chart 和七份生产 values。本地五节点集群与生产拓扑基本一致，但业务镜像由开发者本地构建，并通过局域网 Registry 分发，不经过 GHCR/ACR 或 GitHub Actions。本地配置必须避免沿用生产域名和生产数据桶，同时复用现有 Chart 模板。

## Goals / Non-Goals

**Goals:**

- 提供 api、frontend、ai、judge、postgres、redis、rabbitmq 七份本地 values。
- 保持生产的副本、滚动更新、探针、资源、节点亲和与持久化配置。
- 使用明确的本地 Registry、域名和 OSS bucket 占位符，禁止写入真实密钥。
- 通过 Helm lint/template 验证所有配置。

**Non-Goals:**

- 不创建集群、Registry、Secret、nginx 配置或实际工作负载。
- 不修改 GitHub Actions、生产 values 或 Chart 模板。
- 不迁移 Docker Compose 数据，不新增 RustFS 本地存储配置。

## Decisions

1. **配置放在 `deploy/charts/bluenet/values/local/`**：与生产 values 并列，调用方式直观，避免修改生产文件。
2. **使用完整独立 values，而非多层叠加**：每份文件可单独阅读和渲染，避免本地配置受生产 values 后续变更的隐式影响。
3. **业务镜像统一使用 `<LOCAL_REGISTRY>/bluenet`，基础镜像使用 `<LOCAL_REGISTRY>/bluenet-base`**：沿用现有单仓库、多服务 tag 模式；部署时通过 `--set image.repository=...` 替换。
4. **本地域名统一使用 `https://bluenet.local` 占位**：Cookie、CORS、OAuth 与前端公开地址保持一致；实际使用前整体替换并重建前端镜像。
5. **对象存储沿用阿里云 OSS provider，bucket 使用显式占位符**：复用当前 Chart，不在本轮引入 RustFS；密钥仍只从预建 Secret 读取。
6. **保留 namespace 无关配置**：values 不固定 namespace，本地独立集群可继续使用 `bluenet`。

## Risks / Trade-offs

- [Registry 占位符未替换导致镜像拉取失败] → 文件头注明必须替换，并在部署命令中使用 `--set` 注入。
- [本地域名未解析或 HTTPS 未配置导致登录失败] → 部署前统一替换域名并配置本地 DNS/hosts、nginx 和证书。
- [OSS bucket 占位符未替换导致存储异常] → 部署前替换为本地专用 bucket，禁止复用生产数据桶。
- [完整 values 与生产配置产生漂移] → 本轮以当前生产配置为基线，后续环境无关的 Chart 改进需同步评估本地文件。

## Migration Plan

本轮仅新增配置。后续实际部署按基础设施、后端、前端的顺序执行；每服务独立 Helm release，可通过既有 Helm 回滚方式恢复。无需数据迁移。

## Open Questions

- 实际局域网 Registry 地址和协议（HTTP/HTTPS）在部署时确认。
- 实际本地域名、证书和 OSS bucket 在部署时确认。
