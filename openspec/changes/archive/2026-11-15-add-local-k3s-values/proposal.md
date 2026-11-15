## Why

当前 Helm values 绑定了生产域名、生产对象存储和 ACR 镜像，不能直接用于本地五节点 k3s。需要提供一套可提交的本地 values，使开发者能够使用本地构建镜像和本地 Helm 完成部署，同时不改动生产发布链路。

## What Changes

- 新增 `deploy/charts/bluenet/values/local/` 下七个服务的本地 Helm values。
- 本地业务镜像和基础镜像使用可替换的局域网 Registry 占位符，不依赖 ACR 或 GitHub Actions。
- 保留生产拓扑、探针、资源、调度和持久化策略，仅替换本地域名、OAuth/Cookie/CORS 与对象存储 bucket 等环境配置。
- 使用 Helm 静态渲染校验全部本地 values，不连接或修改任何集群。

## Capabilities

### New Capabilities

- `local-k3s-values`: 为本地五节点 k3s 提供与生产 Chart 兼容、可通过本地 Registry 和 Helm 部署的环境配置。

### Modified Capabilities

无。

## Impact

- 新增本地部署配置和对应 OpenSpec 变更文档。
- 不修改生产 values、Chart 模板、业务代码或 GitHub Actions。
- 实际部署前仍需在仓库外准备 Secret，并替换本地 Registry、域名及 OSS bucket 占位符。
